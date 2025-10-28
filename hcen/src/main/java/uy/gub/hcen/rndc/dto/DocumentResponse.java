package uy.gub.hcen.rndc.dto;

import uy.gub.hcen.rndc.entity.DocumentStatus;
import uy.gub.hcen.rndc.entity.DocumentType;
import uy.gub.hcen.rndc.entity.RndcDocument;

import java.time.LocalDateTime;

/**
 * Document Response DTO
 * <p>
 * Data Transfer Object representing clinical document metadata in API responses.
 * This immutable DTO is used to return document information from the RNDC.
 * <p>
 * Usage Example:
 * <pre>
 * GET /api/rndc/documents/123
 * Response (200 OK):
 * {
 *   "id": 123,
 *   "patientCi": "12345678",
 *   "documentType": "LAB_RESULT",
 *   "documentLocator": "https://clinic-001.hcen.uy/api/documents/abc123",
 *   "documentHash": "sha256:a1b2c3d4e5f67890...",
 *   "createdBy": "doctor@clinic.uy",
 *   "createdAt": "2025-10-21T14:30:00",
 *   "status": "ACTIVE",
 *   "clinicId": "clinic-001",
 *   "documentTitle": "Blood Test Results",
 *   "documentDescription": "Complete blood count analysis"
 * }
 * </pre>
 * <p>
 * This DTO is immutable (no setters) to prevent accidental modifications
 * after creation. Use the constructor or factory method to create instances.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 * @see uy.gub.hcen.rndc.entity.RndcDocument
 * @see DocumentType
 * @see DocumentStatus
 */
public class DocumentResponse {

    private final Long id;
    private final String patientCi;
    private final DocumentType documentType;
    private final String documentLocator;
    private final String documentHash;
    private final String createdBy;
    private final LocalDateTime createdAt;
    private final DocumentStatus status;
    private final String clinicId;
    private final String documentTitle;
    private final String documentDescription;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Full constructor
     *
     * @param id                  Document ID
     * @param patientCi           Patient's CI
     * @param documentType        Type of document
     * @param documentLocator     URL to document in peripheral storage
     * @param documentHash        SHA-256 hash of document
     * @param createdBy           Professional who created the document
     * @param createdAt           Creation timestamp
     * @param status              Document status
     * @param clinicId            Clinic identifier
     * @param documentTitle       Optional document title
     * @param documentDescription Optional document description
     */
    public DocumentResponse(Long id, String patientCi, DocumentType documentType,
                            String documentLocator, String documentHash,
                            String createdBy, LocalDateTime createdAt,
                            DocumentStatus status, String clinicId,
                            String documentTitle, String documentDescription) {
        this.id = id;
        this.patientCi = patientCi;
        this.documentType = documentType;
        this.documentLocator = documentLocator;
        this.documentHash = documentHash;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.status = status;
        this.clinicId = clinicId;
        this.documentTitle = documentTitle;
        this.documentDescription = documentDescription;
    }

    /**
     * Factory method: Create DocumentResponse from RndcDocument entity
     * <p>
     * This is the preferred way to create response DTOs from database entities.
     *
     * @param document The RndcDocument entity
     * @return DocumentResponse DTO
     */
    public static DocumentResponse fromEntity(RndcDocument document) {
        if (document == null) {
            return null;
        }

        return new DocumentResponse(
                document.getId(),
                document.getPatientCi(),
                document.getDocumentType(),
                document.getDocumentLocator(),
                document.getDocumentHash(),
                document.getCreatedBy(),
                document.getCreatedAt(),
                document.getStatus(),
                document.getClinicId(),
                document.getDocumentTitle(),
                document.getDocumentDescription()
        );
    }

    // ================================================================
    // Getters Only (Immutable DTO)
    // ================================================================

    /**
     * Gets the unique document ID
     *
     * @return Document ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the patient's CI (national ID)
     *
     * @return Patient CI
     */
    public String getPatientCi() {
        return patientCi;
    }

    /**
     * Gets the document type
     *
     * @return Document type
     */
    public DocumentType getDocumentType() {
        return documentType;
    }

    /**
     * Gets the document locator URL
     *
     * @return Document locator (URL to peripheral storage)
     */
    public String getDocumentLocator() {
        return documentLocator;
    }

    /**
     * Gets the document hash
     *
     * @return SHA-256 hash of document
     */
    public String getDocumentHash() {
        return documentHash;
    }

    /**
     * Gets the creator identifier
     *
     * @return Professional who created the document
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Gets the creation timestamp
     *
     * @return When the document was created
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the current document status
     *
     * @return Document status (ACTIVE, INACTIVE, DELETED)
     */
    public DocumentStatus getStatus() {
        return status;
    }

    /**
     * Gets the clinic identifier
     *
     * @return Clinic/peripheral node ID
     */
    public String getClinicId() {
        return clinicId;
    }

    /**
     * Gets the optional document title
     *
     * @return Document title, or null if not set
     */
    public String getDocumentTitle() {
        return documentTitle;
    }

    /**
     * Gets the optional document description
     *
     * @return Document description, or null if not set
     */
    public String getDocumentDescription() {
        return documentDescription;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "DocumentResponse{" +
                "id=" + id +
                ", patientCi='" + patientCi + '\'' +
                ", documentType=" + documentType +
                ", documentLocator='" + documentLocator + '\'' +
                ", documentHash='" + (documentHash != null ? "sha256:..." : null) + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                ", status=" + status +
                ", clinicId='" + clinicId + '\'' +
                ", documentTitle='" + documentTitle + '\'' +
                ", hasDescription=" + (documentDescription != null && !documentDescription.isEmpty()) +
                '}';
    }
}
