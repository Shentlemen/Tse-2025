package uy.gub.hcen.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uy.gub.hcen.rndc.entity.DocumentType;

import java.io.Serializable;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;

/**
 * Payload for clinical document registration messages.
 * <p>
 * Contains all data necessary to register document metadata in the RNDC
 * (Registro Nacional de Documentos Clínicos) system.
 * <p>
 * Important: RNDC stores METADATA only, not actual documents.
 * The actual document remains in peripheral storage (documentLocator URL).
 * <p>
 * Data Flow:
 * Peripheral Node → Message Queue → This Payload → RndcService.registerDocument()
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentRegistrationPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Patient's Cédula de Identidad (national ID).
     * <p>
     * The patient whose clinical record this document belongs to.
     */
    private String patientCI;

    /**
     * Type of clinical document.
     * <p>
     * Examples:
     * - CLINICAL_NOTE
     * - LAB_RESULT
     * - IMAGING
     * - PRESCRIPTION
     * - DISCHARGE_SUMMARY
     */
    private DocumentType documentType;

    /**
     * URL pointing to the actual document in peripheral storage.
     * <p>
     * Format: HTTPS URL
     * Example: "https://clinic-001.hcen.uy/api/documents/doc-550e8400"
     * <p>
     * This URL is called by HCEN Central when professionals request document access.
     * Must be unique across all documents (enforced by database constraint).
     */
    private String documentLocator;

    /**
     * SHA-256 hash of the document content for integrity verification.
     * <p>
     * Format: "sha256:[64 lowercase hex characters]"
     * Example: "sha256:a1b2c3d4e5f678901234567890123456789012345678901234567890123456"
     * <p>
     * Used to detect document tampering or corruption during retrieval.
     */
    private String documentHash;

    /**
     * Identifier of the professional who created the document.
     * <p>
     * Format: Email or professional ID
     * Examples: "doctor@clinic.com", "prof-12345"
     */
    private String createdBy;

    /**
     * Timestamp when document was created in the peripheral system.
     * <p>
     * Format: ISO-8601 (e.g., "2025-11-13T10:30:00Z")
     */
    private LocalDateTime createdAt;

    /**
     * Identifier of the clinic/peripheral node that created the document.
     * <p>
     * Examples: "clinic-001", "hospital-montevideo"
     */
    private String clinicId;

    /**
     * Human-readable title for the document (optional).
     * <p>
     * Examples: "Consulta general", "Análisis de sangre", "Radiografía de tórax"
     */
    private String documentTitle;

    /**
     * Brief description or summary of the document content (optional).
     * <p>
     * Helps professionals identify relevant documents without retrieving them.
     */
    private String documentDescription;

    /**
     * Default constructor for JSON deserialization.
     */
    public DocumentRegistrationPayload() {
    }

    /**
     * Constructor with required fields.
     */
    public DocumentRegistrationPayload(String patientCI, DocumentType documentType,
                                        String documentLocator, String documentHash,
                                        String createdBy, LocalDateTime createdAt,
                                        String clinicId) {
        this.patientCI = patientCI;
        this.documentType = documentType;
        this.documentLocator = documentLocator;
        this.documentHash = documentHash;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.clinicId = clinicId;
    }

    /**
     * Constructor with all fields.
     */
    public DocumentRegistrationPayload(String patientCI, DocumentType documentType,
                                        String documentLocator, String documentHash,
                                        String createdBy, LocalDateTime createdAt,
                                        String clinicId, String documentTitle,
                                        String documentDescription) {
        this.patientCI = patientCI;
        this.documentType = documentType;
        this.documentLocator = documentLocator;
        this.documentHash = documentHash;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.clinicId = clinicId;
        this.documentTitle = documentTitle;
        this.documentDescription = documentDescription;
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

    public String getPatientCI() {
        return patientCI;
    }

    public void setPatientCI(String patientCI) {
        this.patientCI = patientCI;
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

        String sha256 = null;
        try{
         sha256 = sha256OfBase64String(documentHash);
        }
        catch(Exception e){}

        return sha256;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentRegistrationPayload that = (DocumentRegistrationPayload) o;
        return Objects.equals(documentLocator, that.documentLocator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentLocator);
    }

    @Override
    public String toString() {
        return "DocumentRegistrationPayload{" +
                "patientCI='" + patientCI + '\'' +
                ", documentType=" + documentType +
                ", documentLocator='" + documentLocator + '\'' +
                ", documentHash='" + documentHash + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                ", clinicId='" + clinicId + '\'' +
                ", documentTitle='" + documentTitle + '\'' +
                ", documentDescription='" + documentDescription + '\'' +
                '}';
    }

    private String sha256OfBase64String(String base64Input) throws Exception {
        // Step 1: decode base64
        byte[] decoded = Base64.getDecoder().decode(base64Input);

        // Step 2: compute SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(decoded);

        // Step 3: convert to hex (common format)
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
