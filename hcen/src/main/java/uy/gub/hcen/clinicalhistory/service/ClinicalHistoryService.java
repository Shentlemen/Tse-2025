package uy.gub.hcen.clinicalhistory.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import uy.gub.hcen.audit.entity.AuditLog;
import uy.gub.hcen.audit.repository.AuditLogRepository;
import uy.gub.hcen.clinicalhistory.dto.*;
import uy.gub.hcen.patient.dto.PatientDashboardStatsDTO;
import uy.gub.hcen.policy.repository.AccessPolicyRepository;
import uy.gub.hcen.policy.repository.AccessRequestRepository;
import uy.gub.hcen.rndc.entity.DocumentStatus;
import uy.gub.hcen.rndc.entity.DocumentType;
import uy.gub.hcen.rndc.entity.RndcDocument;
import uy.gub.hcen.rndc.repository.RndcRepository;
import uy.gub.hcen.service.audit.AuditService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Clinical History Service
 *
 * Business logic service for patient clinical history visualization.
 * Provides methods to retrieve, filter, and display patient clinical documents.
 *
 * <p>Key Features:
 * <ul>
 *   <li>Fetch patient documents from RNDC with filtering and pagination</li>
 *   <li>Calculate document statistics for dashboard</li>
 *   <li>Prepare documents for display with metadata enrichment (clinic names)</li>
 *   <li>Audit logging of all document access</li>
 *   <li>Integration with peripheral nodes for document content retrieval (FHIR, binary)</li>
 * </ul>
 *
 * <p>Current Implementation:
 * - Queries real RNDC documents only (no mock data fallback)
 * - Enriches document metadata with clinic names from clinic registry
 * - Retrieves actual document content from peripheral nodes on-demand
 * - Supports FHIR document retrieval for frontend display
 *
 * @author TSE 2025 Group 9
 * @version 2.0
 * @since 2025-11-04
 */
@Stateless
public class ClinicalHistoryService {

    private static final Logger LOGGER = Logger.getLogger(ClinicalHistoryService.class.getName());

    @Inject
    private RndcRepository rndcRepository;

    @Inject
    private AuditService auditService;

    @Inject
    private uy.gub.hcen.integration.peripheral.PeripheralNodeClient peripheralNodeClient;

    @Inject
    private uy.gub.hcen.clinic.repository.ClinicRepository clinicRepository;

    @Inject
    private uy.gub.hcen.service.policy.PolicyEngine policyEngine;

    @Inject
    private AccessPolicyRepository accessPolicyRepository;

    @Inject
    private AccessRequestRepository accessRequestRepository;

    @Inject
    private AuditLogRepository auditLogRepository;

    /**
     * Gets paginated clinical history for a patient with optional filters
     *
     * @param patientCi Patient's CI
     * @param documentType Optional document type filter
     * @param fromDate Optional start date filter
     * @param toDate Optional end date filter
     * @param clinicId Optional clinic filter
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param requestingClinicId Optional clinic ID from security context (for filtering out clinic's own documents)
     * @return Paginated document list response
     */
    public PaginatedDocumentListResponse getClinicalHistory(
            String patientCi,
            DocumentType documentType,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String clinicId,
            int page,
            int size,
            String requestingClinicId) {

        LOGGER.log(Level.INFO, "Fetching clinical history for patient: {0}, page: {1}, size: {2}, requestingClinicId: {3}",
                new Object[]{patientCi, page, size, requestingClinicId});

        try {
            // Fetch documents from RNDC with filters
            List<RndcDocument> documents = rndcRepository.search(
                    patientCi,
                    documentType,
                    DocumentStatus.ACTIVE, // Only show active documents
                    clinicId,
                    fromDate,
                    toDate,
                    page,
                    size
            );

            // Filter out documents from requesting clinic if applicable
            // Flow 1: Patient accessing their own history -> requestingClinicId is null -> show all documents
            // Flow 2: Clinic accessing patient metadata -> requestingClinicId is set -> filter out clinic's own documents
            List<RndcDocument> filteredDocuments = documents;
            if (requestingClinicId != null && !requestingClinicId.trim().isEmpty()) {
                LOGGER.log(Level.INFO, "Filtering out documents from requesting clinic: {0}", requestingClinicId);
                filteredDocuments = documents.stream()
                        .filter(doc -> !requestingClinicId.equals(doc.getClinicId()))
                        .collect(Collectors.toList());
                LOGGER.log(Level.INFO, "Filtered from {0} to {1} documents after excluding clinic''s own documents",
                        new Object[]{documents.size(), filteredDocuments.size()});
            }

            // Convert to DTOs with clinic name enrichment
            List<DocumentListItemDTO> documentDTOs = filteredDocuments.stream()
                    .map(doc -> enrichDocumentWithClinicName(doc))
                    .collect(Collectors.toList());

            // Get total count (without pagination)
            // Note: This is inefficient - ideally RNDC repository should have a count method with filters
            long totalCount = rndcRepository.countByPatientCiAndStatus(patientCi, DocumentStatus.ACTIVE);

            // Adjust total count if filtering by requesting clinic
            if (requestingClinicId != null && !requestingClinicId.trim().isEmpty()) {
                // Recalculate total count excluding requesting clinic's documents
                List<RndcDocument> allDocuments = rndcRepository.findByPatientCiAndStatus(
                        patientCi, DocumentStatus.ACTIVE, 0, Integer.MAX_VALUE);
                totalCount = allDocuments.stream()
                        .filter(doc -> !requestingClinicId.equals(doc.getClinicId()))
                        .count();
            }

            LOGGER.log(Level.INFO, "Found {0} documents for patient: {1} (total: {2})",
                    new Object[]{documentDTOs.size(), patientCi, totalCount});

            // Log access to clinical history (audit)
            auditService.logAccessEvent(
                    patientCi,
                    "PATIENT",
                    "CLINICAL_HISTORY",
                    patientCi,
                    AuditLog.ActionOutcome.SUCCESS,
                    null, // IP address - should be provided by REST layer
                    null, // User agent - should be provided by REST layer
                    null
            );

            return PaginatedDocumentListResponse.of(documentDTOs, totalCount, page, size);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching clinical history for patient: " + patientCi, e);
            // Return empty response on error
            return PaginatedDocumentListResponse.of(new ArrayList<>(), 0, page, size);
        }
    }

    /**
     * Gets detailed information about a specific document
     *
     * @param documentId Document ID
     * @param patientCi Patient's CI (for authorization check)
     * @return Document detail DTO or null if not found/unauthorized
     */
    public DocumentDetailDTO getDocumentDetail(Long documentId, String patientCi) {
        LOGGER.log(Level.INFO, "Fetching document detail: {0} for patient: {1}",
                new Object[]{documentId, patientCi});

        try {
            Optional<RndcDocument> documentOpt = rndcRepository.findById(documentId);

            if (documentOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Document not found: {0}", documentId);
                return null;
            }

            RndcDocument document = documentOpt.get();

            // Verify patient owns this document
            if (!document.getPatientCi().equals(patientCi)) {
                LOGGER.log(Level.WARNING, "Unauthorized access attempt to document: {0} by patient: {1}",
                        new Object[]{documentId, patientCi});
                // Log attempted unauthorized access
                auditService.logAccessEvent(
                        patientCi,
                        "PATIENT",
                        "DOCUMENT",
                        documentId.toString(),
                        AuditLog.ActionOutcome.DENIED,
                        null,
                        null,
                        null
                );
                return null;
            }

            // Log successful document detail access
            auditService.logDocumentAccess(
                    patientCi,
                    patientCi,
                    documentId,
                    document.getDocumentType(),
                    AuditLog.ActionOutcome.SUCCESS,
                    null,
                    null
            );

            return DocumentDetailDTO.fromEntity(document);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching document detail: " + documentId, e);
            return null;
        }
    }

    /**
     * Gets document statistics for a patient
     *
     * @param patientCi Patient's CI
     * @return Document statistics DTO
     */
    public DocumentStatsDTO getDocumentStatistics(String patientCi) {
        LOGGER.log(Level.INFO, "Calculating document statistics for patient: {0}", patientCi);

        try {
            DocumentStatsDTO stats = new DocumentStatsDTO();

            // Get total document count
            long totalCount = rndcRepository.countByPatientCiAndStatus(patientCi, DocumentStatus.ACTIVE);
            stats.setTotalDocuments(totalCount);
            stats.setActiveDocuments(totalCount);

            // Get inactive count
            long inactiveCount = rndcRepository.countByPatientCiAndStatus(patientCi, DocumentStatus.INACTIVE);
            stats.setInactiveDocuments(inactiveCount);

            // Get all active documents for grouping
            List<RndcDocument> allDocuments = rndcRepository.findByPatientCiAndStatus(
                    patientCi, DocumentStatus.ACTIVE, 0, Integer.MAX_VALUE);

            // Group by type
            allDocuments.stream()
                    .collect(Collectors.groupingBy(
                            doc -> doc.getDocumentType().getDisplayName(),
                            Collectors.counting()
                    ))
                    .forEach(stats::addTypeCount);

            // Group by clinic
            allDocuments.stream()
                    .collect(Collectors.groupingBy(
                            RndcDocument::getClinicId,
                            Collectors.counting()
                    ))
                    .forEach(stats::addClinicCount);

            // Group by year
            allDocuments.stream()
                    .collect(Collectors.groupingBy(
                            doc -> String.valueOf(doc.getCreatedAt().getYear()),
                            Collectors.counting()
                    ))
                    .forEach(stats::addYearCount);

            LOGGER.log(Level.INFO, "Statistics for patient {0}: {1}",
                    new Object[]{patientCi, stats});

            return stats;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating statistics for patient: " + patientCi, e);
            return new DocumentStatsDTO(0); // Return empty stats on error
        }
    }

    /**
     * Gets patient dashboard statistics for the patient portal
     *
     * <p>Returns aggregated counts for:
     * <ul>
     *   <li>Total documents in RNDC</li>
     *   <li>Active access policies</li>
     *   <li>Recent access by professionals (last 30 days)</li>
     *   <li>Pending access request approvals</li>
     * </ul>
     *
     * @param patientCi Patient's CI
     * @return PatientDashboardStatsDTO with aggregated counts
     */
    public PatientDashboardStatsDTO getPatientDashboardStats(String patientCi) {
        LOGGER.log(Level.INFO, "Calculating dashboard statistics for patient: {0}", patientCi);

        try {
            PatientDashboardStatsDTO stats = new PatientDashboardStatsDTO();

            // 1. Total documents from RNDC (ACTIVE documents only)
            long totalDocs = rndcRepository.countByPatientCiAndStatus(patientCi, DocumentStatus.ACTIVE);
            stats.setTotalDocuments(totalDocs);

            // 2. Active policies (GRANTED status, regardless of date constraints)
            // For dashboard, we count all GRANTED policies to give patient visibility
            long activePolicies = countGrantedPolicies(patientCi);
            LOGGER.log(Level.INFO, "Setting activePolicies to: {0}", activePolicies);
            stats.setActivePolicies(activePolicies);
            LOGGER.log(Level.INFO, "After setting, stats.getActivePolicies() = {0}", stats.getActivePolicies());

            // 3. Recent access by professionals (last 30 days)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            long recentAccess = auditLogRepository.countRecentAccessByPatient(patientCi, thirtyDaysAgo);
            LOGGER.log(Level.INFO, "Setting recentAccess to: {0}", recentAccess);
            stats.setRecentAccess(recentAccess);
            LOGGER.log(Level.INFO, "After setting, stats.getRecentAccess() = {0}", stats.getRecentAccess());

            // 4. Pending access request approvals
            long pendingApprovals = accessRequestRepository.countPendingByPatientCi(patientCi);
            LOGGER.log(Level.INFO, "Setting pendingApprovals to: {0}", pendingApprovals);
            stats.setPendingApprovals(pendingApprovals);
            LOGGER.log(Level.INFO, "After setting, stats.getPendingApprovals() = {0}", stats.getPendingApprovals());

            LOGGER.log(Level.INFO, "Dashboard statistics for patient {0}: {1}",
                    new Object[]{patientCi, stats});

            return stats;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating dashboard statistics for patient: " + patientCi, e);
            // Return empty stats on error
            return new PatientDashboardStatsDTO(0, 0, 0, 0);
        }
    }

    /**
     * Retrieves actual document content from peripheral node
     *
     * <p>This method performs the complete document retrieval flow:
     * <ol>
     *   <li>Validates document exists in RNDC and belongs to patient</li>
     *   <li><b>Evaluates access policies if professional/clinic is requesting</b></li>
     *   <li>Looks up clinic configuration to get API key</li>
     *   <li>Calls peripheral node to retrieve actual document bytes</li>
     *   <li>Verifies document integrity using SHA-256 hash</li>
     *   <li>Returns document bytes for client download</li>
     * </ol>
     *
     * <p>Policy Evaluation:
     * <ul>
     *   <li>Patient accessing own document: No policy check (always permitted)</li>
     *   <li>Clinic/Professional accessing: Policy evaluated based on clinic + specialty</li>
     *   <li>PERMIT: Document is retrieved and returned</li>
     *   <li>PENDING or DENY: AccessDeniedException is thrown (403 Forbidden)</li>
     * </ul>
     *
     * @param documentId Document ID
     * @param patientCi Patient's CI (for authorization)
     * @param requestingClinicId Optional clinic ID (for policy evaluation, null if patient request)
     * @param professionalSpecialty Optional specialty (for policy evaluation, null if patient request)
     * @param professionalId Optional professional ID (for audit logging, null if patient request)
     * @return Document content as byte array, or null if unavailable/error
     * @throws DocumentRetrievalException if retrieval fails (peripheral node down, hash mismatch, etc.)
     * @throws ClinicalDocumentAccessDenied if policy evaluation denies access
     */
    public byte[] getDocumentContent(Long documentId, String patientCi, String requestingClinicId, String professionalSpecialty, String professionalId) {
        LOGGER.log(Level.INFO, "Retrieving document content for document: {0}, patient: {1}, clinicId: {2}, specialty: {3}",
                new Object[]{documentId, patientCi, requestingClinicId, professionalSpecialty});

        try {
            // Step 1: Get document metadata from RNDC
            Optional<RndcDocument> documentOpt = rndcRepository.findById(documentId);

            if (documentOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Document not found in RNDC: {0}", documentId);
                throw new DocumentRetrievalException("Documento no encontrado");
            }

            RndcDocument document = documentOpt.get();

            // Step 2: Verify patient owns this document (authorization check)
            if (!document.getPatientCi().equals(patientCi)) {
                LOGGER.log(Level.WARNING, "Unauthorized content access attempt - document: {0}, requestor: {1}, owner: {2}",
                        new Object[]{documentId, patientCi, document.getPatientCi()});
                throw new DocumentRetrievalException("Acceso no autorizado");
            }

            // Step 3: Evaluate access policies if this is a professional/clinic request
            if (requestingClinicId != null && !requestingClinicId.trim().isEmpty()) {
                LOGGER.log(Level.INFO, "Professional/clinic request detected - evaluating access policies");
                evaluateAccessPolicies(document, requestingClinicId, professionalSpecialty, professionalId);
            } else {
                LOGGER.log(Level.INFO, "Patient request - skipping policy evaluation");
            }

            // Step 3: Validate document has locator URL
            if (document.getDocumentLocator() == null || document.getDocumentLocator().isEmpty()) {
                LOGGER.log(Level.WARNING, "Document has no locator URL: {0}", documentId);
                throw new DocumentRetrievalException("Contenido no disponible - localizador faltante");
            }

            // Step 4: Look up clinic to get API key
            String clinicId = document.getClinicId();
            Optional<uy.gub.hcen.clinic.entity.Clinic> clinicOpt = clinicRepository.findById(clinicId);

            if (clinicOpt.isEmpty()) {
                LOGGER.log(Level.SEVERE, "Clinic not found: {0} for document: {1}",
                        new Object[]{clinicId, documentId});
                throw new DocumentRetrievalException("Clínica no encontrada");
            }

            uy.gub.hcen.clinic.entity.Clinic clinic = clinicOpt.get();
            String apiKey = clinic.getApiKey();

            if (apiKey == null || apiKey.isEmpty()) {
                LOGGER.log(Level.SEVERE, "Clinic {0} has no API key configured", clinicId);
                throw new DocumentRetrievalException("Configuración de clínica incompleta");
            }

            // Step 5: Retrieve document from peripheral node with hash verification
            String documentLocator = document.getDocumentLocator();
            String expectedHash = document.getDocumentHash();

            LOGGER.log(Level.INFO, "Fetching document from peripheral node - locator: {0}, clinic: {1}",
                    new Object[]{documentLocator, clinicId});

            byte[] documentBytes = peripheralNodeClient.retrieveDocument(
                    documentLocator,
                    apiKey,
                    expectedHash  // PeripheralNodeClient will verify hash automatically
            );

            LOGGER.log(Level.INFO, "Successfully retrieved document {0} ({1} bytes)",
                    new Object[]{documentId, documentBytes.length});

            return documentBytes;

        } catch (ClinicalDocumentAccessDenied e) {
            // Re-throw policy access denial exceptions (403 Forbidden)
            LOGGER.log(Level.WARNING, "Access denied by policy: {0}", e.getMessage());
            throw e;
        } catch (DocumentRetrievalException e) {
            // Re-throw business logic exceptions
            throw e;
        } catch (uy.gub.hcen.integration.peripheral.PeripheralNodeException e) {
            LOGGER.log(Level.SEVERE, "Peripheral node error retrieving document: " + documentId, e);
            throw new DocumentRetrievalException("Error comunicándose con nodo periférico: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error retrieving document content: " + documentId, e);
            throw new DocumentRetrievalException("Error inesperado al recuperar documento", e);
        }
    }

    /**
     * Retrieves FHIR-formatted document from peripheral node
     *
     * <p>This method retrieves the clinical document in FHIR format and returns it as a JSON string
     * without any transformation. The flow is:
     * <ol>
     *   <li>Validates document exists in RNDC and belongs to patient</li>
     *   <li>Looks up clinic configuration to get API key</li>
     *   <li>Calls peripheral node with Accept: application/fhir+json header</li>
     *   <li>Returns raw FHIR JSON string (no parsing or transformation)</li>
     * </ol>
     *
     * <p>The peripheral node is expected to return FHIR-compliant resources such as:
     * <ul>
     *   <li>Bundle (type: document)</li>
     *   <li>DocumentReference</li>
     *   <li>DiagnosticReport</li>
     *   <li>Observation</li>
     * </ul>
     *
     * @param documentId Document ID from RNDC
     * @param patientCi Patient's CI (for authorization)
     * @return FHIR document as JSON string
     * @throws DocumentRetrievalException if retrieval fails or patient is not authorized
     */
    public String getFhirDocument(Long documentId, String patientCi) {
        LOGGER.log(Level.INFO, "Retrieving FHIR document for documentId: {0}, patient: {1}",
                new Object[]{documentId, patientCi});

        try {
            // Step 1: Get document metadata from RNDC
            Optional<RndcDocument> documentOpt = rndcRepository.findById(documentId);

            if (documentOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Document not found in RNDC: {0}", documentId);
                throw new DocumentRetrievalException("Documento no encontrado");
            }

            RndcDocument document = documentOpt.get();

            // Step 2: Verify patient owns this document (authorization check)
            if (!document.getPatientCi().equals(patientCi)) {
                LOGGER.log(Level.WARNING, "Unauthorized FHIR document access attempt - document: {0}, requestor: {1}, owner: {2}",
                        new Object[]{documentId, patientCi, document.getPatientCi()});

                // Log unauthorized access attempt
                auditService.logAccessEvent(
                        patientCi,
                        "PATIENT",
                        "DOCUMENT",
                        documentId.toString(),
                        AuditLog.ActionOutcome.DENIED,
                        null,
                        null,
                        null
                );

                throw new DocumentRetrievalException("Acceso no autorizado");
            }

            // Step 3: Validate document has locator URL
            if (document.getDocumentLocator() == null || document.getDocumentLocator().isEmpty()) {
                LOGGER.log(Level.WARNING, "Document has no locator URL: {0}", documentId);
                throw new DocumentRetrievalException("Contenido no disponible - localizador faltante");
            }

            // Step 4: Look up clinic to get API key
            String clinicId = document.getClinicId();
            Optional<uy.gub.hcen.clinic.entity.Clinic> clinicOpt = clinicRepository.findById(clinicId);

            if (clinicOpt.isEmpty()) {
                LOGGER.log(Level.SEVERE, "Clinic not found: {0} for document: {1}",
                        new Object[]{clinicId, documentId});
                throw new DocumentRetrievalException("Clínica no encontrada");
            }

            uy.gub.hcen.clinic.entity.Clinic clinic = clinicOpt.get();
            String apiKey = clinic.getApiKey();

            if (apiKey == null || apiKey.isEmpty()) {
                LOGGER.log(Level.SEVERE, "Clinic {0} has no API key configured", clinicId);
                throw new DocumentRetrievalException("Configuración de clínica incompleta");
            }

            // Step 5: Retrieve document from peripheral node
            String documentLocator = document.getDocumentLocator();

            LOGGER.log(Level.INFO, "Fetching FHIR document from peripheral node - locator: {0}, clinic: {1}",
                    new Object[]{documentLocator, clinicId});

            byte[] documentBytes = peripheralNodeClient.retrieveDocument(
                    documentLocator,
                    apiKey,
                    null  // Skip hash verification for FHIR (content may be dynamically generated)
            );

            // Step 6: Convert bytes to JSON string
            String fhirJson = new String(documentBytes, java.nio.charset.StandardCharsets.UTF_8);

            // Step 7: Basic validation - ensure it's valid JSON
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.readTree(fhirJson); // Parse to validate JSON structure
                LOGGER.log(Level.INFO, "Successfully retrieved and validated FHIR document {0} ({1} bytes)",
                        new Object[]{documentId, documentBytes.length});
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Retrieved content is not valid JSON: {0}", documentId);
                throw new DocumentRetrievalException("Formato de documento inválido - se esperaba JSON FHIR");
            }

            // Step 8: Log successful access
            auditService.logDocumentAccess(
                    patientCi,
                    patientCi,
                    documentId,
                    document.getDocumentType(),
                    AuditLog.ActionOutcome.SUCCESS,
                    null, // IP address should be provided by REST layer
                    null  // User agent should be provided by REST layer
            );

            return fhirJson;

        } catch (DocumentRetrievalException e) {
            // Re-throw business logic exceptions
            throw e;
        } catch (uy.gub.hcen.integration.peripheral.PeripheralNodeException e) {
            LOGGER.log(Level.SEVERE, "Peripheral node error retrieving FHIR document: " + documentId, e);
            throw new DocumentRetrievalException("Error comunicándose con nodo periférico: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error retrieving FHIR document: " + documentId, e);
            throw new DocumentRetrievalException("Error inesperado al recuperar documento FHIR", e);
        }
    }

    /**
     * Counts GRANTED policies for a patient (for dashboard statistics).
     * This counts policies with GRANTED status regardless of date constraints,
     * providing better visibility to patients about their active permissions.
     *
     * @param patientCi Patient's CI
     * @return Count of GRANTED policies
     */
    private long countGrantedPolicies(String patientCi) {
        try {
            // Get all policies and count those with GRANTED status
            List<uy.gub.hcen.policy.entity.AccessPolicy> allPolicies =
                    accessPolicyRepository.findAllByPatientCi(patientCi);

            LOGGER.log(Level.INFO, "Found {0} total policies for patient {1}",
                    new Object[]{allPolicies.size(), patientCi});

            long grantedCount = allPolicies.stream()
                    .filter(policy -> {
                        boolean isGranted = policy.getStatus() == uy.gub.hcen.policy.entity.PolicyStatus.GRANTED;
                        LOGGER.log(Level.INFO, "Policy ID {0}: status={1}, isGranted={2}",
                                new Object[]{policy.getId(), policy.getStatus(), isGranted});
                        return isGranted;
                    })
                    .count();

            LOGGER.log(Level.INFO, "Counted {0} GRANTED policies for patient {1}",
                    new Object[]{grantedCount, patientCi});

            return grantedCount;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting granted policies for patient: " + patientCi, e);
            return 0;
        }
    }

    /**
     * Enriches a document DTO with actual clinic name
     *
     * <p>Converts RndcDocument entity to DTO and looks up the clinic name
     * from the clinic repository. If clinic is not found, uses clinic ID as fallback.
     *
     * @param document RNDC document entity
     * @return Enriched DocumentListItemDTO with clinic name
     */
    private DocumentListItemDTO enrichDocumentWithClinicName(RndcDocument document) {
        // Convert entity to DTO
        DocumentListItemDTO dto = DocumentListItemDTO.fromEntity(document);

        // Look up actual clinic name
        if (document.getClinicId() != null && !document.getClinicId().isEmpty()) {
            try {
                Optional<uy.gub.hcen.clinic.entity.Clinic> clinicOpt =
                    clinicRepository.findById(document.getClinicId());

                if (clinicOpt.isPresent()) {
                    dto.setClinicName(clinicOpt.get().getClinicName());
                } else {
                    // Fallback: Use clinic ID if clinic not found in registry
                    LOGGER.log(Level.WARNING, "Clinic not found in registry: {0} (document {1})",
                            new Object[]{document.getClinicId(), document.getId()});
                    dto.setClinicName("Clínica " + document.getClinicId());
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error looking up clinic name for: " + document.getClinicId(), e);
                dto.setClinicName("Clínica " + document.getClinicId());
            }
        } else {
            dto.setClinicName("Clínica Desconocida");
        }

        return dto;
    }

    /**
     * Evaluates access policies for professional/clinic document access
     *
     * <p>This method checks if the requesting clinic + specialty combination
     * has permission to access the document based on patient-defined access policies.
     *
     * @param document Document being accessed
     * @param clinicId Requesting clinic ID
     * @param specialtyCode Professional specialty code (e.g., "CAR", "MG")
     * @param professionalId Professional identifier (for audit logging, may be null)
     * @throws ClinicalDocumentAccessDenied if access is denied or pending
     */
    private void evaluateAccessPolicies(RndcDocument document, String clinicId, String specialtyCode, String professionalId) {
        try {
            // Validate specialty is provided
            if (specialtyCode == null || specialtyCode.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Missing specialty for policy evaluation - clinic: {0}, document: {1}",
                        new Object[]{clinicId, document.getId()});
                throw new ClinicalDocumentAccessDenied("Specialty is required for document access");
            }

            // Convert specialty code to enum
            uy.gub.hcen.policy.entity.MedicalSpecialty specialty;
            try {
                specialty = uy.gub.hcen.policy.entity.MedicalSpecialty.fromCode(specialtyCode.toUpperCase());
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Invalid specialty code: {0}", specialtyCode);
                throw new ClinicalDocumentAccessDenied("Invalid specialty code: " + specialtyCode);
            }

            // Build AccessRequest for policy evaluation
            // Use professionalId if provided, otherwise use clinicId for audit trail
            String actorId = (professionalId != null && !professionalId.trim().isEmpty())
                    ? professionalId
                    : clinicId;

            uy.gub.hcen.service.policy.dto.AccessRequest accessRequest =
                    uy.gub.hcen.service.policy.dto.AccessRequest.builder()
                            .professionalId(actorId) // For audit logging
                            .addSpecialty(specialty.name())
                            .clinicId(clinicId)
                            .patientCi(document.getPatientCi())
                            .documentId(document.getId())
                            .documentType(document.getDocumentType())
                            .requestTime(java.time.LocalDateTime.now())
                            .build();

            LOGGER.log(Level.INFO, "Evaluating access policy - patient: {0}, clinic: {1}, specialty: {2}, docType: {3}",
                    new Object[]{document.getPatientCi(), clinicId, specialty.getDisplayName(), document.getDocumentType()});

            // Evaluate policy using PolicyEngine
            uy.gub.hcen.service.policy.dto.PolicyEvaluationResult result =
                    policyEngine.evaluateAccess(accessRequest);

            LOGGER.log(Level.INFO, "Policy evaluation result: {0} - {1}",
                    new Object[]{result.getDecision(), result.getReason()});

            // Handle decision
            if (result.isPermitted()) {
                LOGGER.log(Level.INFO, "Access GRANTED for clinic {0} with specialty {1} to document {2}",
                        new Object[]{clinicId, specialty.getDisplayName(), document.getId()});
                // Access granted - continue with document retrieval
            } else {
                // PENDING or DENY - both treated as forbidden
                LOGGER.log(Level.WARNING, "Access DENIED for clinic {0} with specialty {1} to document {2} - reason: {3}",
                        new Object[]{clinicId, specialty.getDisplayName(), document.getId(), result.getReason()});

                // Log denial in audit system
                java.util.Map<String, Object> auditDetails = new java.util.HashMap<>();
                auditDetails.put("reason", result.getReason());
                auditDetails.put("clinicId", clinicId);
                auditDetails.put("specialty", specialty.getDisplayName());
                auditDetails.put("documentType", document.getDocumentType().name());

                auditService.logAccessEvent(
                        actorId, // Use professionalId if available, otherwise clinicId
                        "CLINIC",
                        "DOCUMENT",
                        document.getId().toString(),
                        AuditLog.ActionOutcome.DENIED,
                        null,
                        null,
                        auditDetails
                );

                throw new ClinicalDocumentAccessDenied(
                        "Access denied: " + result.getReason() +
                                ". Patient has not granted access to this document for your clinic/specialty."
                );
            }

        } catch (ClinicalDocumentAccessDenied e) {
            // Re-throw access denied exceptions
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error evaluating access policies", e);
            // On policy evaluation error, deny access by default (fail-safe)
            throw new ClinicalDocumentAccessDenied("Unable to evaluate access policies: " + e.getMessage());
        }
    }

    /**
     * Custom exception for document retrieval errors
     */
    public static class DocumentRetrievalException extends RuntimeException {
        public DocumentRetrievalException(String message) {
            super(message);
        }

        public DocumentRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }




    // =========================================================================
    // MOCK DATA METHODS (DEPRECATED - TO BE REMOVED)
    // =========================================================================
    // These methods are kept for backward compatibility during transition period
    // but are no longer used by the service. They will be removed in a future release.
    // =========================================================================

    /**
     * @deprecated No longer used - service now returns real RNDC data only
     * Returns mock documents for development when RNDC has no data
     *
     * @param patientCi Patient's CI
     * @param page Page number
     * @param size Page size
     * @return List of mock document DTOs
     */
    @Deprecated
    private List<DocumentListItemDTO> getMockDocuments(String patientCi, int page, int size) {
        LOGGER.log(Level.INFO, "Generating mock documents for patient: {0}", patientCi);

        List<DocumentListItemDTO> mockDocs = new ArrayList<>();

        // Create diverse mock documents
        mockDocs.add(new DocumentListItemDTO(
                1L,
                "LAB_RESULT",
                "Resultados de Laboratorio",
                "Hemograma Completo",
                "Hospital de Clínicas",
                "clinic-hc",
                "Dr. Juan García - Hematología",
                LocalDateTime.now().minusDays(5),
                "ACTIVE",
                true,
                null
        ));

        mockDocs.add(new DocumentListItemDTO(
                2L,
                "IMAGING",
                "Estudios de Imágenes",
                "Radiografía de Tórax PA",
                "Hospital Británico",
                "clinic-hb",
                "Dra. María Rodríguez - Radiología",
                LocalDateTime.now().minusDays(15),
                "ACTIVE",
                true,
                null
        ));

        mockDocs.add(new DocumentListItemDTO(
                3L,
                "PRESCRIPTION",
                "Receta Médica",
                "Medicación - Hipertensión",
                "Clínica Modelo",
                "clinic-cm",
                "Dr. Carlos Méndez - Cardiología",
                LocalDateTime.now().minusDays(30),
                "ACTIVE",
                true,
                null
        ));

        mockDocs.add(new DocumentListItemDTO(
                4L,
                "CLINICAL_NOTE",
                "Nota Clínica",
                "Consulta General - Control Anual",
                "ASSE - Centro de Salud Sayago",
                "clinic-asse-sayago",
                "Dra. Ana Pérez - Medicina General",
                LocalDateTime.now().minusDays(45),
                "ACTIVE",
                true,
                null
        ));

        mockDocs.add(new DocumentListItemDTO(
                5L,
                "VACCINATION_RECORD",
                "Registro de Vacunación",
                "Vacuna Antigripal 2024",
                "Vacunatorio Central",
                "clinic-vac-central",
                "Enf. Laura Martínez",
                LocalDateTime.now().minusDays(60),
                "ACTIVE",
                true,
                null
        ));

        mockDocs.add(new DocumentListItemDTO(
                6L,
                "LAB_RESULT",
                "Resultados de Laboratorio",
                "Perfil Lipídico",
                "Laboratorio Clínico del Sur",
                "clinic-lab-sur",
                "Bioq. Roberto Silva",
                LocalDateTime.now().minusDays(75),
                "ACTIVE",
                true,
                null
        ));

        mockDocs.add(new DocumentListItemDTO(
                7L,
                "IMAGING",
                "Estudios de Imágenes",
                "Ecografía Abdominal",
                "Centro de Diagnóstico por Imágenes",
                "clinic-cdi",
                "Dr. Fernando López - Ecografía",
                LocalDateTime.now().minusDays(90),
                "ACTIVE",
                true,
                null
        ));

        mockDocs.add(new DocumentListItemDTO(
                8L,
                "DISCHARGE_SUMMARY",
                "Resumen de Alta",
                "Alta Hospitalaria - Apendicectomía",
                "Hospital Maciel",
                "clinic-maciel",
                "Dr. Pablo Sánchez - Cirugía General",
                LocalDateTime.now().minusDays(180),
                "ACTIVE",
                true,
                null
        ));

        // Apply pagination
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, mockDocs.size());

        if (fromIndex >= mockDocs.size()) {
            return new ArrayList<>();
        }

        return mockDocs.subList(fromIndex, toIndex);
    }

    /**
     * @deprecated No longer used - service now returns real RNDC counts only
     * Returns mock document count
     *
     * @param patientCi Patient's CI
     * @return Total mock document count
     */
    @Deprecated
    private long getMockDocumentCount(String patientCi) {
        return 8; // Match the number of mock documents above
    }
}
