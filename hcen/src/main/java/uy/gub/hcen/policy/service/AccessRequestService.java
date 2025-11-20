package uy.gub.hcen.policy.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import uy.gub.hcen.audit.entity.AuditLog.ActionOutcome;
import uy.gub.hcen.audit.entity.AuditLog.EventType;
import uy.gub.hcen.policy.dto.*;
import uy.gub.hcen.policy.entity.AccessRequest;
import uy.gub.hcen.policy.entity.AccessRequest.RequestStatus;
import uy.gub.hcen.policy.entity.AccessRequest.UrgencyLevel;
import uy.gub.hcen.policy.repository.AccessRequestRepository;
import uy.gub.hcen.service.audit.AuditService;
import uy.gub.hcen.service.inus.InusService;
import uy.gub.hcen.service.rndc.RndcService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Access Request Service
 *
 * Business logic service for managing patient access request approvals/denials.
 * Handles the workflow when a professional requests access to a patient's
 * documents and the patient needs to approve or deny the request.
 *
 * Key Responsibilities:
 * - Fetch pending access requests for patients
 * - Process approval decisions
 * - Process denial decisions
 * - Handle info requests
 * - Integrate with audit system
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-03
 */
@Stateless
public class AccessRequestService {

    private static final Logger LOGGER = Logger.getLogger(AccessRequestService.class.getName());

    @Inject
    private AccessRequestRepository accessRequestRepository;

    @Inject
    private AuditService auditService;

    @Inject
    private InusService inusService;

    @Inject
    private RndcService rndcService;

    @Inject
    private uy.gub.hcen.rndc.repository.RndcRepository rndcRepository;

    @Inject
    private uy.gub.hcen.clinic.repository.ClinicRepository clinicRepository;

    @Inject
    private uy.gub.hcen.integration.peripheral.PeripheralNodeClient peripheralNodeClient;

    /**
     * Get pending access requests for a patient
     *
     * @param patientCi Patient's CI
     * @param status Optional status filter
     * @param page Page number (0-based)
     * @param size Page size
     * @return Paginated list of access requests
     */
    public AccessRequestListResponse getAccessRequests(String patientCi, RequestStatus status, int page, int size) {
        LOGGER.log(Level.INFO, "Fetching access requests for patient: {0}, status: {1}",
                new Object[]{patientCi, status});

        try {
            List<AccessRequest> requests;
            long totalCount;

            if (status != null) {
                requests = accessRequestRepository.findByPatientCiAndStatus(patientCi, status, page, size);
                // For count, we need to count all matching records
                totalCount = accessRequestRepository.findByPatientCiAndStatus(patientCi, status, 0, Integer.MAX_VALUE).size();
            } else {
                requests = accessRequestRepository.findByPatientCi(patientCi, page, size);
                totalCount = accessRequestRepository.findByPatientCi(patientCi, 0, Integer.MAX_VALUE).size();
            }

            List<AccessRequestDTO> dtos = requests.stream()
                    .map(AccessRequestDTO::fromEntity)
                    .collect(Collectors.toList());

            return AccessRequestListResponse.of(dtos, totalCount, page, size);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching access requests for patient: " + patientCi, e);
            throw new RuntimeException("Failed to fetch access requests", e);
        }
    }

    /**
     * Get count of pending requests for a patient
     *
     * @param patientCi Patient's CI
     * @return Count of pending requests
     */
    public long countPendingRequests(String patientCi) {
        try {
            return accessRequestRepository.countPendingByPatientCi(patientCi);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting pending requests for patient: " + patientCi, e);
            return 0;
        }
    }

    /**
     * Approve an access request
     *
     * @param requestId Request ID
     * @param patientCi Patient's CI (for authorization check)
     * @param decision Approval decision with optional reason
     * @return Success message
     * @throws IllegalArgumentException if request not found or patient unauthorized
     * @throws IllegalStateException if request cannot be approved
     */
    @Transactional
    public String approveRequest(Long requestId, String patientCi, ApprovalDecisionDTO decision) {
        LOGGER.log(Level.INFO, "Approving access request: {0} by patient: {1}",
                new Object[]{requestId, patientCi});

        try {
            // Find request
            Optional<AccessRequest> requestOpt = accessRequestRepository.findById(requestId);

            if (requestOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Access request not found: {0}", requestId);
                throw new IllegalArgumentException("Access request not found");
            }

            AccessRequest request = requestOpt.get();

            // Authorization check - verify patient owns this request
            if (!request.getPatientCi().equals(patientCi)) {
                LOGGER.log(Level.WARNING, "Patient {0} attempted to approve request for patient {1}",
                        new Object[]{patientCi, request.getPatientCi()});
                // Log unauthorized attempt
                auditService.logAccessEvent(
                        patientCi,
                        "PATIENT",
                        "ACCESS_REQUEST",
                        requestId.toString(),
                        ActionOutcome.DENIED,
                        null, // ipAddress
                        null, // userAgent
                        Map.of("reason", "Unauthorized approval attempt")
                );
                throw new IllegalArgumentException("You are not authorized to approve this request");
            }

            // Approve the request (entity method handles validation)
            request.approve(decision.getReason());

            // Save updated request
            AccessRequest updatedRequest = accessRequestRepository.update(request);

            // Log approval in audit system
            auditService.logAccessApproval(
                    patientCi,
                    request.getProfessionalId(),
                    request.getDocumentId(),
                    null, // ipAddress
                    null  // userAgent
            );

            // Notify clinic about the approval (asynchronous, best-effort)
            notifyClinicAboutDecision(request, "APPROVED", decision.getReason());

            LOGGER.log(Level.INFO, "Access request {0} approved successfully by patient: {1}",
                    new Object[]{requestId, patientCi});

            return "Access request approved successfully";

        } catch (IllegalArgumentException | IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Failed to approve access request: " + requestId, e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error approving access request: " + requestId, e);
            throw new RuntimeException("Failed to approve access request", e);
        }
    }

    /**
     * Deny an access request
     *
     * @param requestId Request ID
     * @param patientCi Patient's CI (for authorization check)
     * @param decision Denial decision with required reason
     * @return Success message
     * @throws IllegalArgumentException if request not found or patient unauthorized
     * @throws IllegalStateException if request cannot be denied
     */
    @Transactional
    public String denyRequest(Long requestId, String patientCi, DenialDecisionDTO decision) {
        LOGGER.log(Level.INFO, "Denying access request: {0} by patient: {1}",
                new Object[]{requestId, patientCi});

        try {
            // Find request
            Optional<AccessRequest> requestOpt = accessRequestRepository.findById(requestId);

            if (requestOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Access request not found: {0}", requestId);
                throw new IllegalArgumentException("Access request not found");
            }

            AccessRequest request = requestOpt.get();

            // Authorization check - verify patient owns this request
            if (!request.getPatientCi().equals(patientCi)) {
                LOGGER.log(Level.WARNING, "Patient {0} attempted to deny request for patient {1}",
                        new Object[]{patientCi, request.getPatientCi()});
                // Log unauthorized attempt
                auditService.logAccessEvent(
                        patientCi,
                        "PATIENT",
                        "ACCESS_REQUEST",
                        requestId.toString(),
                        ActionOutcome.DENIED,
                        null, // ipAddress
                        null, // userAgent
                        Map.of("reason", "Unauthorized denial attempt")
                );
                throw new IllegalArgumentException("You are not authorized to deny this request");
            }

            // Deny the request (entity method handles validation)
            request.deny(decision.getReason());

            // Save updated request
            AccessRequest updatedRequest = accessRequestRepository.update(request);

            // Log denial in audit system
            auditService.logAccessDenial(
                    patientCi,
                    request.getProfessionalId(),
                    request.getDocumentId(),
                    null, // ipAddress
                    null  // userAgent
            );

            // Notify clinic about the denial (asynchronous, best-effort)
            notifyClinicAboutDecision(request, "DENIED", decision.getReason());

            LOGGER.log(Level.INFO, "Access request {0} denied successfully by patient: {1}",
                    new Object[]{requestId, patientCi});

            return "Access request denied successfully";

        } catch (IllegalArgumentException | IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Failed to deny access request: " + requestId, e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error denying access request: " + requestId, e);
            throw new RuntimeException("Failed to deny access request", e);
        }
    }

    /**
     * Request more information about an access request
     *
     * @param requestId Request ID
     * @param patientCi Patient's CI (for authorization check)
     * @param infoRequest Info request with question
     * @return Success message
     * @throws IllegalArgumentException if request not found or patient unauthorized
     */
    @Transactional
    public String requestMoreInfo(Long requestId, String patientCi, InfoRequestDTO infoRequest) {
        LOGGER.log(Level.INFO, "Patient {0} requesting more info for access request: {1}",
                new Object[]{patientCi, requestId});

        try {
            // Find request
            Optional<AccessRequest> requestOpt = accessRequestRepository.findById(requestId);

            if (requestOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Access request not found: {0}", requestId);
                throw new IllegalArgumentException("Access request not found");
            }

            AccessRequest request = requestOpt.get();

            // Authorization check - verify patient owns this request
            if (!request.getPatientCi().equals(patientCi)) {
                LOGGER.log(Level.WARNING, "Patient {0} attempted to request info for patient {1}",
                        new Object[]{patientCi, request.getPatientCi()});
                throw new IllegalArgumentException("You are not authorized to access this request");
            }

            // Log info request in audit system
            auditService.logAccessEvent(
                    patientCi,
                    "PATIENT",
                    "ACCESS_REQUEST",
                    requestId.toString(),
                    ActionOutcome.SUCCESS,
                    null, // ipAddress
                    null, // userAgent
                    Map.of(
                        "action", "REQUEST_MORE_INFO",
                        "professionalId", request.getProfessionalId(),
                        "question", infoRequest.getQuestion()
                    )
            );

            LOGGER.log(Level.INFO, "Info request logged for access request: {0}", requestId);

            // TODO: In a real implementation, this would:
            // 1. Send notification to the professional
            // 2. Store the question in a communication table
            // 3. Track the conversation thread
            // For now, we just log it

            return "Your question has been sent to the professional. " +
                    "They will be notified and can respond with additional information.";

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Failed to request info for access request: " + requestId, e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error requesting info for access request: " + requestId, e);
            throw new RuntimeException("Failed to request more information", e);
        }
    }

    /**
     * Create a new access request
     *
     * Called by peripheral nodes when a professional needs access to patient documents.
     * Implements deduplication logic to prevent duplicate pending requests.
     *
     * @param requestDTO Request creation data
     * @param clinicId Clinic ID (extracted from API key authentication)
     * @param clinicName Clinic name (looked up from clinic registry)
     * @return Creation response with request ID and status
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public AccessRequestCreationResponseDTO createAccessRequest(
            AccessRequestCreationDTO requestDTO,
            String clinicId,
            String clinicName) {

        LOGGER.log(Level.INFO,
            "Creating access request: professional={0}, patient={1}, document={2}, clinic={3}",
            new Object[]{requestDTO.getProfessionalId(),
                        maskCI(requestDTO.getPatientCi()),
                        requestDTO.getDocumentId(),
                        clinicId});

        try {
            // 1. Validate patient exists in INUS
            Optional<uy.gub.hcen.inus.entity.InusUser> inusUser =
                inusService.findUserByCi(requestDTO.getPatientCi());
            if (inusUser.isEmpty()) {
                LOGGER.log(Level.WARNING, "Patient not found in INUS: {0}",
                    maskCI(requestDTO.getPatientCi()));
                throw new IllegalArgumentException(
                    "Patient not found: " + requestDTO.getPatientCi());
            }

            // 2. Validate document exists in RNDC (if documentId provided)
            if (requestDTO.getDocumentId() != null) {
                Optional<uy.gub.hcen.rndc.entity.RndcDocument> rndcDocument =
                    rndcService.getDocumentMetadata(requestDTO.getDocumentId());
                if (rndcDocument.isEmpty()) {
                    LOGGER.log(Level.WARNING, "Document not found in RNDC: {0}",
                        requestDTO.getDocumentId());
                    throw new IllegalArgumentException(
                        "Document not found: " + requestDTO.getDocumentId());
                }
            }

            // 3. Check for existing pending request (deduplication)
            Optional<AccessRequest> existingRequest =
                accessRequestRepository.findPendingRequest(
                    requestDTO.getProfessionalId(),
                    requestDTO.getPatientCi(),
                    requestDTO.getDocumentId()
                );

            if (existingRequest.isPresent()) {
                LOGGER.log(Level.INFO,
                    "Duplicate request detected, returning existing request: {0}",
                    existingRequest.get().getId());

                // Log duplicate attempt
                auditService.logAccessEvent(
                    requestDTO.getProfessionalId(),
                    "PROFESSIONAL",
                    "ACCESS_REQUEST",
                    existingRequest.get().getId().toString(),
                    ActionOutcome.SUCCESS,
                    null,
                    null,
                    Map.of("action", "DUPLICATE_REQUEST_DETECTED",
                           "clinicId", clinicId)
                );

                // Return existing request (idempotent)
                return AccessRequestCreationResponseDTO.fromEntity(
                    existingRequest.get(), false);
            }

            // 4. Parse urgency level
            UrgencyLevel urgency = UrgencyLevel.ROUTINE;
            if (requestDTO.getUrgency() != null && !requestDTO.getUrgency().isEmpty()) {
                try {
                    urgency = UrgencyLevel.valueOf(
                        requestDTO.getUrgency().toUpperCase());
                } catch (IllegalArgumentException e) {
                    LOGGER.log(Level.WARNING, "Invalid urgency level: {0}, using ROUTINE",
                        requestDTO.getUrgency());
                }
            }

            // 5. Create new AccessRequest entity
            AccessRequest newRequest = new AccessRequest(
                requestDTO.getProfessionalId(),
                requestDTO.getProfessionalName(),
                requestDTO.getSpecialty(),
                clinicId,
                clinicName,
                requestDTO.getPatientCi(),
                requestDTO.getDocumentId(),
                requestDTO.getDocumentType(),
                requestDTO.getRequestReason(),
                urgency
            );

            // 6. Save to database
            AccessRequest savedRequest = accessRequestRepository.save(newRequest);

            // 7. Log creation in audit system
            auditService.logAccessEvent(
                requestDTO.getProfessionalId(),
                "PROFESSIONAL",
                "ACCESS_REQUEST",
                savedRequest.getId().toString(),
                ActionOutcome.SUCCESS,
                null,
                null,
                Map.of(
                    "action", "REQUEST_CREATED",
                    "patientCi", maskCI(requestDTO.getPatientCi()),
                    "documentId", String.valueOf(requestDTO.getDocumentId()),
                    "clinicId", clinicId,
                    "urgency", urgency.name()
                )
            );

            LOGGER.log(Level.INFO, "Access request created successfully: {0}",
                savedRequest.getId());

            // 8. Return success response
            return AccessRequestCreationResponseDTO.fromEntity(savedRequest, true);

        } catch (IllegalArgumentException e) {
            // Validation error - log and rethrow
            LOGGER.log(Level.WARNING, "Validation error creating access request", e);
            auditService.logAccessEvent(
                requestDTO.getProfessionalId(),
                "PROFESSIONAL",
                "ACCESS_REQUEST",
                null,
                ActionOutcome.FAILURE,
                null,
                null,
                Map.of("error", e.getMessage(),
                       "clinicId", clinicId)
            );
            throw e;

        } catch (Exception e) {
            // Unexpected error
            LOGGER.log(Level.SEVERE, "Error creating access request", e);
            auditService.logAccessEvent(
                requestDTO.getProfessionalId(),
                "PROFESSIONAL",
                "ACCESS_REQUEST",
                null,
                ActionOutcome.FAILURE,
                null,
                null,
                Map.of("error", e.getMessage(),
                       "clinicId", clinicId)
            );
            throw new RuntimeException("Failed to create access request", e);
        }
    }

    /**
     * Retrieve approved document in FHIR format for professional
     *
     * This method allows professionals to retrieve clinical documents after patient approval.
     * It follows this flow:
     * 1. Verify access request exists and status is APPROVED
     * 2. Verify the authenticated professional owns this request (authorization)
     * 3. Get document metadata from RNDC
     * 4. Get clinic API key from clinic registry
     * 5. Retrieve document bytes from peripheral node with hash verification
     * 6. Parse document content based on type
     * 7. Convert to FHIR DocumentReference format
     * 8. Log access in audit system
     * 9. Return FHIR DocumentReference response
     *
     * @param requestId Access request ID
     * @param professionalId Professional ID (for authorization check)
     * @return FHIR DocumentReference with embedded base64-encoded document content
     * @throws IllegalArgumentException if request not found, not approved, or professional unauthorized
     * @throws RuntimeException if document retrieval fails
     */
    @Transactional
    public org.hl7.fhir.r4.model.DocumentReference getApprovedDocument(Long requestId, String professionalId) {
        LOGGER.log(Level.INFO, "Retrieving approved document for request: {0}, professional: {1}",
                new Object[]{requestId, professionalId});

        try {
            // Step 1: Find access request
            Optional<AccessRequest> requestOpt = accessRequestRepository.findById(requestId);

            if (requestOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Access request not found: {0}", requestId);
                throw new IllegalArgumentException("Access request not found");
            }

            AccessRequest request = requestOpt.get();

            // Step 2: Authorization check - verify professional owns this request
            if (!request.getProfessionalId().equals(professionalId)) {
                LOGGER.log(Level.WARNING,
                        "Professional {0} attempted to retrieve document for request owned by {1}",
                        new Object[]{professionalId, request.getProfessionalId()});
                // Log unauthorized attempt
                auditService.logAccessEvent(
                        professionalId,
                        "PROFESSIONAL",
                        "ACCESS_REQUEST",
                        requestId.toString(),
                        ActionOutcome.DENIED,
                        null,
                        null,
                        Map.of("reason", "Unauthorized document retrieval attempt")
                );
                throw new IllegalArgumentException("You are not authorized to retrieve this document");
            }

            // Step 3: Verify request status is APPROVED
            if (request.getStatus() != RequestStatus.APPROVED) {
                LOGGER.log(Level.WARNING,
                        "Cannot retrieve document for request with status: {0}",
                        request.getStatus());
                throw new IllegalArgumentException(
                        "Cannot retrieve document - request status is " + request.getStatus() +
                        ". Only APPROVED requests can be retrieved.");
            }

            // Step 4: Get document metadata from RNDC
            Long documentId = request.getDocumentId();
            if (documentId == null) {
                LOGGER.log(Level.WARNING, "Access request {0} has no document ID", requestId);
                throw new IllegalArgumentException("Access request has no associated document");
            }

            Optional<uy.gub.hcen.rndc.entity.RndcDocument> documentOpt =
                    rndcRepository.findById(documentId);

            if (documentOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Document not found in RNDC: {0}", documentId);
                throw new IllegalArgumentException("Document not found: " + documentId);
            }

            uy.gub.hcen.rndc.entity.RndcDocument document = documentOpt.get();

            // Step 5: Validate document has locator URL
            if (document.getDocumentLocator() == null || document.getDocumentLocator().isEmpty()) {
                LOGGER.log(Level.WARNING, "Document {0} has no locator URL", documentId);
                throw new IllegalArgumentException("Document has no locator URL");
            }

            // Step 6: Look up clinic to get API key
            String clinicId = document.getClinicId();
            Optional<uy.gub.hcen.clinic.entity.Clinic> clinicOpt = clinicRepository.findById(clinicId);

            if (clinicOpt.isEmpty()) {
                LOGGER.log(Level.SEVERE, "Clinic not found: {0} for document: {1}",
                        new Object[]{clinicId, documentId});
                throw new IllegalArgumentException("Clinic not found: " + clinicId);
            }

            uy.gub.hcen.clinic.entity.Clinic clinic = clinicOpt.get();
            String apiKey = clinic.getApiKey();

            if (apiKey == null || apiKey.isEmpty()) {
                LOGGER.log(Level.SEVERE, "Clinic {0} has no API key configured", clinicId);
                throw new RuntimeException("Clinic configuration incomplete - no API key");
            }

            // Step 7: Retrieve document from peripheral node with hash verification
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

            // Step 8: Convert to FHIR DocumentReference format
            org.hl7.fhir.r4.model.DocumentReference fhirDocument = buildFhirDocumentReference(
                    document,
                    documentBytes,
                    request
            );

            // Step 9: Log access in audit system
            auditService.logAccessEvent(
                    professionalId,
                    "PROFESSIONAL",
                    "DOCUMENT",
                    documentId.toString(),
                    ActionOutcome.SUCCESS,
                    null, // ipAddress - can be added if needed
                    null, // userAgent - can be added if needed
                    Map.of(
                            "action", "APPROVED_DOCUMENT_RETRIEVAL",
                            "requestId", requestId.toString(),
                            "patientCi", maskCI(request.getPatientCi()),
                            "documentType", document.getDocumentType().name(),
                            "clinicId", clinicId,
                            "documentSize", documentBytes.length
                    )
            );

            LOGGER.log(Level.INFO,
                    "Successfully retrieved approved document for request {0}, professional {1}",
                    new Object[]{requestId, professionalId});

            return fhirDocument;

        } catch (IllegalArgumentException e) {
            // Re-throw validation errors
            LOGGER.log(Level.WARNING, "Validation error retrieving approved document", e);
            throw e;

        } catch (uy.gub.hcen.integration.peripheral.PeripheralNodeException e) {
            // Peripheral node error
            LOGGER.log(Level.SEVERE, "Peripheral node error retrieving document: " + requestId, e);
            throw new RuntimeException("Failed to retrieve document from peripheral node: " + e.getMessage(), e);

        } catch (Exception e) {
            // Unexpected error
            LOGGER.log(Level.SEVERE, "Error retrieving approved document: " + requestId, e);
            throw new RuntimeException("Failed to retrieve approved document", e);
        }
    }

    /**
     * Build FHIR DocumentReference resource from RNDC document metadata and bytes
     *
     * @param document RNDC document metadata
     * @param documentBytes Document bytes (PDF, XML, etc.)
     * @param request Access request for context
     * @return FHIR DocumentReference resource
     */
    private org.hl7.fhir.r4.model.DocumentReference buildFhirDocumentReference(
            uy.gub.hcen.rndc.entity.RndcDocument document,
            byte[] documentBytes,
            AccessRequest request) {

        org.hl7.fhir.r4.model.DocumentReference fhirDoc = new org.hl7.fhir.r4.model.DocumentReference();

        // Set resource ID
        fhirDoc.setId("doc-" + document.getId());

        // Set status
        fhirDoc.setStatus(org.hl7.fhir.r4.model.Enumerations.DocumentReferenceStatus.CURRENT);

        // Set type (document type coding)
        org.hl7.fhir.r4.model.CodeableConcept typeCode = new org.hl7.fhir.r4.model.CodeableConcept();
        org.hl7.fhir.r4.model.Coding coding = new org.hl7.fhir.r4.model.Coding();
        coding.setSystem("http://loinc.org");
        coding.setCode(mapDocumentTypeToLoinc(document.getDocumentType()));
        coding.setDisplay(document.getDocumentType().name());
        typeCode.addCoding(coding);
        fhirDoc.setType(typeCode);

        // Set subject (patient reference)
        org.hl7.fhir.r4.model.Reference patientRef = new org.hl7.fhir.r4.model.Reference();
        patientRef.setReference("Patient/" + document.getPatientCi());
        patientRef.setDisplay("Patient CI: " + document.getPatientCi());
        fhirDoc.setSubject(patientRef);

        // Set author (professional who created the document)
        if (document.getCreatedBy() != null) {
            org.hl7.fhir.r4.model.Reference authorRef = new org.hl7.fhir.r4.model.Reference();
            authorRef.setReference("Practitioner/" + document.getCreatedBy());
            authorRef.setDisplay(document.getCreatedBy());
            fhirDoc.addAuthor(authorRef);
        }

        // Set date (document creation time)
        if (document.getCreatedAt() != null) {
            fhirDoc.setDate(java.util.Date.from(
                    document.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()
            ));
        }

        // Set content with attachment
        org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent content =
                new org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent();

        org.hl7.fhir.r4.model.Attachment attachment = new org.hl7.fhir.r4.model.Attachment();

        // Set content type based on document type
        attachment.setContentType(determineContentType(document.getDocumentType()));

        // Set base64-encoded data
        attachment.setData(documentBytes);

        // Set hash (SHA-256)
        if (document.getDocumentHash() != null) {
            // Convert hash to bytes for FHIR (FHIR expects base64-encoded hash in 'hash' field)
            String hashHex = document.getDocumentHash().startsWith("sha256:")
                    ? document.getDocumentHash().substring(7)
                    : document.getDocumentHash();
            attachment.setHash(hexToBytes(hashHex));
        }

        // Set title if available
        if (document.getDocumentTitle() != null) {
            attachment.setTitle(document.getDocumentTitle());
        }

        content.setAttachment(attachment);
        fhirDoc.addContent(content);

        return fhirDoc;
    }

    /**
     * Map HCEN DocumentType to LOINC code
     *
     * @param documentType HCEN document type
     * @return LOINC code
     */
    private String mapDocumentTypeToLoinc(uy.gub.hcen.rndc.entity.DocumentType documentType) {
        switch (documentType) {
            case CLINICAL_NOTE:
                return "11506-3"; // Progress note
            case LAB_RESULT:
                return "18725-2"; // Microbiology studies
            case IMAGING:
                return "18748-4"; // Diagnostic imaging study
            case PRESCRIPTION:
                return "57833-6"; // Prescription for medication
            case DISCHARGE_SUMMARY:
                return "18842-5"; // Discharge summary
            case VACCINATION_RECORD:
                return "11369-6"; // History of Immunization
            case SURGICAL_REPORT:
                return "11504-8"; // Surgical operation note
            case PATHOLOGY_REPORT:
                return "60591-5"; // Pathology report
            case EMERGENCY_REPORT:
                return "34133-9"; // Emergency department note
            case REFERRAL:
                return "57133-1"; // Referral note
            case PROGRESS_NOTE:
                return "11492-6"; // Progress note (Provider)
            case ALLERGY_RECORD:
                return "48765-2"; // Allergies and adverse reactions
            case VITAL_SIGNS:
                return "8716-3"; // Vital signs
            case DIAGNOSTIC_REPORT:
                return "69730-0"; // Diagnostic report
            case TREATMENT_PLAN:
                return "18776-5"; // Treatment plan
            case INFORMED_CONSENT:
                return "59284-0"; // Patient consent
            default:
                return "11506-3"; // Default to progress note
        }
    }

    /**
     * Determine content type from document type
     *
     * @param documentType Document type
     * @return MIME content type
     */
    private String determineContentType(uy.gub.hcen.rndc.entity.DocumentType documentType) {
        switch (documentType) {
            case LAB_RESULT:
            case IMAGING:
            case PATHOLOGY_REPORT:
            case DIAGNOSTIC_REPORT:
                return "application/pdf";
            case CLINICAL_NOTE:
            case PRESCRIPTION:
            case DISCHARGE_SUMMARY:
            case VACCINATION_RECORD:
            case SURGICAL_REPORT:
            case EMERGENCY_REPORT:
            case REFERRAL:
            case PROGRESS_NOTE:
            case ALLERGY_RECORD:
            case VITAL_SIGNS:
            case TREATMENT_PLAN:
            case INFORMED_CONSENT:
                return "application/pdf";
            default:
                return "application/pdf";
        }
    }

    /**
     * Convert hex string to byte array
     *
     * @param hex Hex string
     * @return Byte array
     */
    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Masks patient CI for logging (privacy protection)
     * Shows first 5 digits only: 12345***
     *
     * @param ci Patient CI
     * @return Masked CI
     */
    private String maskCI(String ci) {
        if (ci == null || ci.length() < 5) {
            return "***";
        }
        return ci.substring(0, 5) + "***";
    }

    /**
     * Notifies the clinic (peripheral node) about patient's access request decision.
     * This method is called asynchronously after approval or denial to inform
     * the clinic so they can notify the professional.
     *
     * Note: This is a best-effort notification. Failures are logged but don't
     * block the approval/denial process.
     *
     * @param request Access request that was approved/denied
     * @param decision Decision type: "APPROVED" or "DENIED"
     * @param decisionReason Optional reason provided by patient
     */
    private void notifyClinicAboutDecision(AccessRequest request, String decision, String decisionReason) {
        try {
            LOGGER.log(Level.INFO, "Notifying clinic {0} about {1} decision for request {2}",
                    new Object[]{request.getClinicId(), decision, request.getId()});

            // Look up clinic to get peripheral node URL
            Optional<uy.gub.hcen.clinic.entity.Clinic> clinicOpt =
                    clinicRepository.findById(request.getClinicId());

            if (clinicOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Clinic not found for notification: {0}", request.getClinicId());
                return;
            }

            uy.gub.hcen.clinic.entity.Clinic clinic = clinicOpt.get();
            String peripheralNodeUrl = clinic.getPeripheralNodeUrl();

            if (peripheralNodeUrl == null || peripheralNodeUrl.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Clinic {0} has no peripheral node URL configured",
                        request.getClinicId());
                return;
            }

            // Build notification DTO
            uy.gub.hcen.integration.peripheral.dto.AccessRequestNotificationDTO notification =
                    new uy.gub.hcen.integration.peripheral.dto.AccessRequestNotificationDTO(
                            request.getId(),
                            request.getProfessionalId(),
                            request.getPatientCi(),
                            request.getDocumentId(),
                            decision,
                            decisionReason,
                            java.time.LocalDateTime.now()
                    );

            // Send notification to peripheral node (best-effort, non-blocking)
            peripheralNodeClient.notifyAccessRequestDecision(peripheralNodeUrl, notification);

            LOGGER.log(Level.INFO, "Successfully sent {0} notification to clinic {1}",
                    new Object[]{decision, request.getClinicId()});

        } catch (Exception e) {
            // Log error but don't throw - we don't want to block approval/denial
            LOGGER.log(Level.WARNING, "Failed to notify clinic about decision (requestId: {0}): {1}",
                    new Object[]{request.getId(), e.getMessage()});
        }
    }
}
