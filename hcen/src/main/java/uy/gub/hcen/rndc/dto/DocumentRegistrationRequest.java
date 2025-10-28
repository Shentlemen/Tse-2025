package uy.gub.hcen.rndc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import uy.gub.hcen.rndc.entity.DocumentType;

/**
 * Document Registration Request DTO
 * <p>
 * Data Transfer Object for registering new clinical documents in the RNDC.
 * This DTO is used when peripheral nodes (clinics, health providers) register
 * document metadata.
 * <p>
 * Usage Example:
 * <pre>
 * POST /api/rndc/documents
 * {
 *   "patientCi": "12345678",
 *   "documentType": "LAB_RESULT",
 *   "documentLocator": "https://clinic-001.hcen.uy/api/documents/abc123",
 *   "documentHash": "sha256:a1b2c3d4e5f67890...",
 *   "createdBy": "doctor@clinic.uy",
 *   "clinicId": "clinic-001",
 *   "documentTitle": "Blood Test Results",
 *   "documentDescription": "Complete blood count analysis"
 * }
 * </pre>
 * <p>
 * Validation Rules:
 * - patientCi: Required, max 20 characters
 * - documentType: Required, valid DocumentType enum value
 * - documentLocator: Required, valid HTTP/HTTPS URL, max 500 characters
 * - documentHash: Required, format "sha256:[64 hex characters]"
 * - createdBy: Required, max 100 characters
 * - clinicId: Required, max 50 characters
 * - documentTitle: Optional, max 200 characters
 * - documentDescription: Optional, max 5000 characters
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 * @see uy.gub.hcen.rndc.entity.RndcDocument
 * @see DocumentType
 */
public class DocumentRegistrationRequest {

    /**
     * Patient's Cédula de Identidad (CI) - National ID
     */
    @NotBlank(message = "Patient CI is required")
    @Size(max = 20, message = "Patient CI must not exceed 20 characters")
    private String patientCi;

    /**
     * Type of clinical document
     * Valid values: CLINICAL_NOTE, LAB_RESULT, IMAGING, PRESCRIPTION, etc.
     */
    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    /**
     * URL to retrieve the actual document from peripheral node storage
     * Must be a valid HTTP or HTTPS URL
     */
    @NotBlank(message = "Document locator is required")
    @Size(max = 500, message = "Document locator must not exceed 500 characters")
    @Pattern(regexp = "^https?://.*", message = "Invalid URL format - must start with http:// or https://")
    private String documentLocator;

    /**
     * SHA-256 hash of the document for integrity verification
     * Format: sha256:[64 lowercase hexadecimal characters]
     * Example: sha256:a1b2c3d4e5f67890123456789012345678901234567890123456789012345678
     */
    @NotBlank(message = "Document hash is required")
    @Pattern(
            regexp = "^sha256:[a-f0-9]{64}$",
            message = "Invalid hash format - expected format: sha256:[64 hex characters]"
    )
    private String documentHash;

    /**
     * Email or ID of the professional who created the document
     */
    @NotBlank(message = "Creator ID is required")
    @Size(max = 100, message = "Creator ID must not exceed 100 characters")
    private String createdBy;

    /**
     * Identifier of the clinic/peripheral node registering this document
     */
    @NotBlank(message = "Clinic ID is required")
    @Size(max = 50, message = "Clinic ID must not exceed 50 characters")
    private String clinicId;

    /**
     * Optional: Document title for quick identification
     */
    @Size(max = 200, message = "Document title must not exceed 200 characters")
    private String documentTitle;

    /**
     * Optional: Document description or summary
     */
    @Size(max = 5000, message = "Document description must not exceed 5000 characters")
    private String documentDescription;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor for JSON deserialization
     */
    public DocumentRegistrationRequest() {
    }

    /**
     * Constructor with required fields
     *
     * @param patientCi       Patient's CI
     * @param documentType    Type of document
     * @param documentLocator URL to document in peripheral storage
     * @param documentHash    SHA-256 hash of document
     * @param createdBy       Professional who created the document
     * @param clinicId        Clinic identifier
     */
    public DocumentRegistrationRequest(String patientCi, DocumentType documentType,
                                       String documentLocator, String documentHash,
                                       String createdBy, String clinicId) {
        this.patientCi = patientCi;
        this.documentType = documentType;
        this.documentLocator = documentLocator;
        this.documentHash = documentHash;
        this.createdBy = createdBy;
        this.clinicId = clinicId;
    }

    /**
     * Full constructor with all fields
     *
     * @param patientCi           Patient's CI
     * @param documentType        Type of document
     * @param documentLocator     URL to document in peripheral storage
     * @param documentHash        SHA-256 hash of document
     * @param createdBy           Professional who created the document
     * @param clinicId            Clinic identifier
     * @param documentTitle       Optional document title
     * @param documentDescription Optional document description
     */
    public DocumentRegistrationRequest(String patientCi, DocumentType documentType,
                                       String documentLocator, String documentHash,
                                       String createdBy, String clinicId,
                                       String documentTitle, String documentDescription) {
        this.patientCi = patientCi;
        this.documentType = documentType;
        this.documentLocator = documentLocator;
        this.documentHash = documentHash;
        this.createdBy = createdBy;
        this.clinicId = clinicId;
        this.documentTitle = documentTitle;
        this.documentDescription = documentDescription;
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

    public String getPatientCi() {
        return patientCi;
    }

    public void setPatientCi(String patientCi) {
        this.patientCi = patientCi;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getDocumentLocator() {
        return documentLocator;
    }

    public void setDocumentLocator(String documentLocator) {
        this.documentLocator = documentLocator;
    }

    public String getDocumentHash() {
        return documentHash;
    }

    public void setDocumentHash(String documentHash) {
        this.documentHash = documentHash;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getDocumentDescription() {
        return documentDescription;
    }

    public void setDocumentDescription(String documentDescription) {
        this.documentDescription = documentDescription;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "DocumentRegistrationRequest{" +
                "patientCi='" + patientCi + '\'' +
                ", documentType=" + documentType +
                ", documentLocator='" + documentLocator + '\'' +
                ", documentHash='" + (documentHash != null ? "sha256:..." : null) + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", clinicId='" + clinicId + '\'' +
                ", documentTitle='" + documentTitle + '\'' +
                ", hasDescription=" + (documentDescription != null && !documentDescription.isEmpty()) +
                '}';
    }
}
