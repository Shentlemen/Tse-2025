package uy.gub.hcen.clinicalhistory.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import uy.gub.hcen.audit.entity.AuditLog;
import uy.gub.hcen.clinicalhistory.dto.*;
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
 *   <li>Prepare documents for display with metadata enrichment</li>
 *   <li>Audit logging of all document access</li>
 *   <li>Future: Integration with peripheral nodes for document content retrieval</li>
 * </ul>
 *
 * <p>Current Implementation:
 * - Uses RNDC data if available
 * - Falls back to mock data for development/testing
 * - Contains placeholder methods for future peripheral node integration
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-04
 */
@Stateless
public class ClinicalHistoryService {

    private static final Logger LOGGER = Logger.getLogger(ClinicalHistoryService.class.getName());

    @Inject
    private RndcRepository rndcRepository;

    @Inject
    private AuditService auditService;

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
     * @return Paginated document list response
     */
    public PaginatedDocumentListResponse getClinicalHistory(
            String patientCi,
            DocumentType documentType,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String clinicId,
            int page,
            int size) {

        LOGGER.log(Level.INFO, "Fetching clinical history for patient: {0}, page: {1}, size: {2}",
                new Object[]{patientCi, page, size});

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

            // Convert to DTOs
            List<DocumentListItemDTO> documentDTOs = documents.stream()
                    .map(DocumentListItemDTO::fromEntity)
                    .collect(Collectors.toList());

            // Get total count (without pagination)
            // Note: This is inefficient - ideally RNDC repository should have a count method with filters
            long totalCount = rndcRepository.countByPatientCiAndStatus(patientCi, DocumentStatus.ACTIVE);

            LOGGER.log(Level.INFO, "Found {0} documents for patient: {1} (total: {2})",
                    new Object[]{documentDTOs.size(), patientCi, totalCount});

            // If no documents found in RNDC, use mock data for development
            if (documentDTOs.isEmpty()) {
                LOGGER.log(Level.WARNING, "No documents found in RNDC for patient: {0}, using mock data", patientCi);
                documentDTOs = getMockDocuments(patientCi, page, size);
                totalCount = getMockDocumentCount(patientCi);
            }

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
     * Gets document content URL (placeholder for future peripheral node integration)
     *
     * @param documentId Document ID
     * @param patientCi Patient's CI (for authorization)
     * @return Document content response with URL or unavailable message
     */
    public DocumentContentResponse getDocumentContent(Long documentId, String patientCi) {
        LOGGER.log(Level.INFO, "Retrieving document content URL for document: {0}", documentId);

        try {
            Optional<RndcDocument> documentOpt = rndcRepository.findById(documentId);

            if (documentOpt.isEmpty()) {
                return DocumentContentResponse.unavailable("Documento no encontrado");
            }

            RndcDocument document = documentOpt.get();

            // Verify patient owns this document
            if (!document.getPatientCi().equals(patientCi)) {
                LOGGER.log(Level.WARNING, "Unauthorized content access attempt for document: {0}", documentId);
                return DocumentContentResponse.unavailable("Acceso no autorizado");
            }

            // Check if document has content available
            if (document.getDocumentLocator() == null || document.getDocumentLocator().isEmpty()) {
                return DocumentContentResponse.unavailable("Contenido no disponible");
            }

            // TODO: Implement peripheral node integration
            // For now, return the locator URL directly (in production, this should proxy through HCEN)
            // This is a PLACEHOLDER - actual implementation requires:
            // 1. Call peripheral node API with authentication
            // 2. Verify document hash for integrity
            // 3. Return content or proxy URL
            // 4. Handle errors from peripheral node

            LOGGER.log(Level.WARNING, "Peripheral node integration not yet implemented. Returning locator URL directly.");

            return DocumentContentResponse.available(
                    document.getDocumentLocator(),
                    "application/pdf", // TODO: Determine actual content type
                    document.getDocumentHash()
            );

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving document content: " + documentId, e);
            return DocumentContentResponse.unavailable("Error al recuperar el documento");
        }
    }

    // =========================================================================
    // PLACEHOLDER METHODS FOR FUTURE PERIPHERAL NODE INTEGRATION
    // =========================================================================

    /**
     * Fetches document content from peripheral node
     * TODO: Implement when peripheral node API is ready
     *
     * @param locatorUrl Document locator URL from RNDC
     * @return Document content as byte array or null
     */
    private byte[] fetchDocumentContentFromPeripheral(String locatorUrl) {
        LOGGER.log(Level.WARNING, "Peripheral node integration not yet implemented. Locator: {0}", locatorUrl);

        // TODO: Implementation steps:
        // 1. Parse locator URL to extract clinic ID and document ID
        // 2. Look up clinic credentials/API key
        // 3. Make authenticated HTTP request to peripheral node
        // 4. Download document content
        // 5. Verify document hash matches RNDC metadata
        // 6. Return content or throw exception

        return null; // Return null for now
    }

    /**
     * Verifies document hash for integrity
     *
     * @param content Document content
     * @param expectedHash Expected hash from RNDC
     * @return true if hash matches, false otherwise
     */
    private boolean verifyDocumentHash(byte[] content, String expectedHash) {
        // TODO: Implement SHA-256 hash verification
        LOGGER.log(Level.WARNING, "Document hash verification not yet implemented");
        return true; // Placeholder
    }

    // =========================================================================
    // MOCK DATA METHODS (FOR DEVELOPMENT/TESTING)
    // =========================================================================

    /**
     * Returns mock documents for development when RNDC has no data
     *
     * @param patientCi Patient's CI
     * @param page Page number
     * @param size Page size
     * @return List of mock document DTOs
     */
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
     * Returns mock document count
     *
     * @param patientCi Patient's CI
     * @return Total mock document count
     */
    private long getMockDocumentCount(String patientCi) {
        return 8; // Match the number of mock documents above
    }
}
