package uy.gub.hcen.rndc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * RNDC Document Entity
 *
 * Represents a clinical document metadata entry in the National Clinical Document Registry (RNDC).
 * The RNDC stores only metadata about documents; actual documents remain in peripheral node storage.
 *
 * <p>Key Concepts:
 * <ul>
 *   <li>Document Locator: URL pointing to the actual document in peripheral storage</li>
 *   <li>Document Hash: SHA-256 hash for integrity verification</li>
 *   <li>Soft Delete: Documents are marked as DELETED rather than physically removed</li>
 *   <li>Audit Trail: All document access is logged via the audit system</li>
 * </ul>
 *
 * <p>Usage Example:
 * <pre>
 * RndcDocument document = new RndcDocument();
 * document.setPatientCi("12345678");
 * document.setDocumentType(DocumentType.LAB_RESULT);
 * document.setDocumentLocator("https://clinic-001.hcen.uy/documents/abc123");
 * document.setDocumentHash("sha256:a1b2c3d4...");
 * document.setCreatedBy("doctor@clinic.uy");
 * document.setClinicId("clinic-001");
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 * @see DocumentType
 * @see DocumentStatus
 */
@Entity
@Table(
    name = "rndc_documents",
    schema = "rndc",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_rndc_document_locator",
            columnNames = "document_locator"
        )
    },
    indexes = {
        @Index(name = "idx_rndc_patient_ci", columnList = "patient_ci"),
        @Index(name = "idx_rndc_clinic_id", columnList = "clinic_id"),
        @Index(name = "idx_rndc_document_type", columnList = "document_type"),
        @Index(name = "idx_rndc_patient_ci_status", columnList = "patient_ci, status"),
        @Index(name = "idx_rndc_created_at", columnList = "created_at"),
        @Index(name = "idx_rndc_patient_type_status", columnList = "patient_ci, document_type, status")
    }
)
public class RndcDocument {

    /**
     * Unique identifier for the document metadata entry
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Patient's Cédula de Identidad (CI) - National ID
     * References the patient in INUS (National User Index)
     */
    @NotBlank(message = "Patient CI is required")
    @Size(max = 20, message = "Patient CI must not exceed 20 characters")
    @Column(name = "patient_ci", nullable = false, length = 20)
    private String patientCi;

    /**
     * URL to retrieve the actual document from peripheral node storage
     * Example: https://clinic-001.hcen.uy/api/documents/abc123
     */
    @NotBlank(message = "Document locator is required")
    @Size(max = 500, message = "Document locator must not exceed 500 characters")
    @Column(name = "document_locator", nullable = false, length = 500)
    private String documentLocator;

    /**
     * SHA-256 hash of the document for integrity verification
     * Format: sha256:hexadecimal_hash
     */
    @NotBlank(message = "Document hash is required")
    @Size(max = 64, message = "Document hash must not exceed 64 characters")
    @Column(name = "document_hash", nullable = false, length = 64)
    private String documentHash;

    /**
     * Type of clinical document
     * @see DocumentType
     */
    @NotNull(message = "Document type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    /**
     * Email or ID of the professional who created the document
     */
    @NotBlank(message = "Creator information is required")
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    /**
     * Timestamp when the document was created
     * Automatically set on persist
     */
    @NotNull(message = "Creation timestamp is required")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Current status of the document
     * @see DocumentStatus
     */
    @NotNull(message = "Document status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DocumentStatus status;

    /**
     * Identifier of the clinic/peripheral node that registered this document
     */
    @NotBlank(message = "Clinic ID is required")
    @Size(max = 50, message = "Clinic ID must not exceed 50 characters")
    @Column(name = "clinic_id", nullable = false, length = 50)
    private String clinicId;

    /**
     * Optional: Document title for quick identification
     */
    @Size(max = 200, message = "Document title must not exceed 200 characters")
    @Column(name = "document_title", length = 200)
    private String documentTitle;

    /**
     * Optional: Document description or summary
     */
    @Column(name = "document_description", columnDefinition = "TEXT")
    private String documentDescription;

    /**
     * Default constructor
     */
    public RndcDocument() {
        this.status = DocumentStatus.ACTIVE;
    }

    /**
     * Constructor with required fields
     *
     * @param patientCi Patient's CI
     * @param documentLocator URL to document in peripheral storage
     * @param documentHash SHA-256 hash of the document
     * @param documentType Type of clinical document
     * @param createdBy Professional who created the document
     * @param clinicId Clinic identifier
     */
    public RndcDocument(String patientCi, String documentLocator, String documentHash,
                        DocumentType documentType, String createdBy, String clinicId) {
        this.patientCi = patientCi;
        this.documentLocator = documentLocator;
        this.documentHash = documentHash;
        this.documentType = documentType;
        this.createdBy = createdBy;
        this.clinicId = clinicId;
        this.status = DocumentStatus.ACTIVE;
    }

    /**
     * JPA lifecycle callback: Set creation timestamp before persist
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = DocumentStatus.ACTIVE;
        }
    }

    /**
     * Marks this document as inactive
     */
    public void markAsInactive() {
        this.status = DocumentStatus.INACTIVE;
    }

    /**
     * Marks this document as deleted (soft delete)
     */
    public void markAsDeleted() {
        this.status = DocumentStatus.DELETED;
    }

    /**
     * Checks if this document is active
     *
     * @return true if status is ACTIVE, false otherwise
     */
    public boolean isActive() {
        return this.status == DocumentStatus.ACTIVE;
    }

    /**
     * Checks if this document is deleted
     *
     * @return true if status is DELETED, false otherwise
     */
    public boolean isDeleted() {
        return this.status == DocumentStatus.DELETED;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientCi() {
        return patientCi;
    }

    public void setPatientCi(String patientCi) {
        this.patientCi = patientCi;
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

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
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

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
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

    // Equals, HashCode, and ToString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RndcDocument that = (RndcDocument) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(documentLocator, that.documentLocator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, documentLocator);
    }

    @Override
    public String toString() {
        return "RndcDocument{" +
               "id=" + id +
               ", patientCi='" + patientCi + '\'' +
               ", documentType=" + documentType +
               ", status=" + status +
               ", clinicId='" + clinicId + '\'' +
               ", createdAt=" + createdAt +
               ", documentTitle='" + documentTitle + '\'' +
               '}';
    }
}
