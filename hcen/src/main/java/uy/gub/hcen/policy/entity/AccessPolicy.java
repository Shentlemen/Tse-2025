package uy.gub.hcen.policy.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Access Policy Entity
 *
 * Represents a patient-defined access control policy for clinical documents in the HCEN system.
 * Policies grant access to healthcare professionals with a specific specialty from a specific clinic.
 *
 * Simplified Policy Model:
 * - Patient grants access to professionals with a specific specialty from a specific clinic
 * - Policies are created with GRANTED status by default
 * - Document scope: null = ALL documents, specific documentId = only that document
 *
 * Database Schema: policies.access_policies
 *
 * @author TSE 2025 Group 9
 * @version 2.0
 * @since 2025-11-18
 */
@Entity
@Table(name = "access_policies", schema = "policies", indexes = {
    @Index(name = "idx_access_policies_patient_ci", columnList = "patient_ci"),
    @Index(name = "idx_access_policies_clinic_id", columnList = "clinic_id"),
    @Index(name = "idx_access_policies_specialty", columnList = "specialty"),
    @Index(name = "idx_access_policies_status", columnList = "status"),
    @Index(name = "idx_access_policies_patient_clinic_specialty", columnList = "patient_ci, clinic_id, specialty")
})
public class AccessPolicy implements Serializable {

    private static final long serialVersionUID = 2L;

    /**
     * Unique policy identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Patient CI (Cedula de Identidad)
     * References: inus.inus_users.ci
     */
    @Column(name = "patient_ci", nullable = false, length = 20)
    private String patientCi;

    /**
     * Clinic identifier
     * References: clinics.clinics.clinic_id
     */
    @Column(name = "clinic_id", nullable = false, length = 50)
    private String clinicId;

    /**
     * Medical specialty
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "specialty", nullable = false, length = 50)
    private MedicalSpecialty specialty;

    /**
     * Specific document ID (optional)
     * If null, policy applies to ALL documents
     * If set, policy applies only to that specific document
     */
    @Column(name = "document_id")
    private Long documentId;

    /**
     * Policy status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PolicyStatus status = PolicyStatus.GRANTED;

    /**
     * Policy validity start date (optional)
     * If null, policy is valid from creation
     */
    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    /**
     * Policy validity end date (optional)
     * If null, policy is valid indefinitely
     */
    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    /**
     * Priority for conflict resolution
     * Higher priority wins when policies conflict
     * Default: 0
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    /**
     * Timestamp of policy creation
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of last policy update
     * Auto-updated by database trigger
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Default constructor
     */
    public AccessPolicy() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = PolicyStatus.GRANTED;
        this.priority = 0;
    }

    /**
     * Constructor with required fields
     *
     * @param patientCi Patient CI
     * @param clinicId Clinic ID
     * @param specialty Medical specialty
     */
    public AccessPolicy(String patientCi, String clinicId, MedicalSpecialty specialty) {
        this();
        this.patientCi = patientCi;
        this.clinicId = clinicId;
        this.specialty = specialty;
    }

    /**
     * Full constructor
     *
     * @param patientCi Patient CI
     * @param clinicId Clinic ID
     * @param specialty Medical specialty
     * @param documentId Document ID (null for all documents)
     * @param status Policy status
     */
    public AccessPolicy(String patientCi, String clinicId, MedicalSpecialty specialty,
                        Long documentId, PolicyStatus status) {
        this(patientCi, clinicId, specialty);
        this.documentId = documentId;
        this.status = status;
    }

    /**
     * Checks if this policy is currently valid based on validity dates and status
     *
     * @return true if policy is valid and active, false otherwise
     */
    public boolean isValid() {
        // Check status
        if (status != PolicyStatus.GRANTED) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }

        if (validUntil != null && now.isAfter(validUntil)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if this policy applies to all documents
     *
     * @return true if documentId is null (applies to all documents)
     */
    public boolean appliesToAllDocuments() {
        return documentId == null;
    }

    /**
     * Checks if this policy applies to a specific document
     *
     * @param docId Document ID to check
     * @return true if policy applies to all documents or to the specific document
     */
    public boolean appliesToDocument(Long docId) {
        return documentId == null || documentId.equals(docId);
    }

    /**
     * Pre-persist callback
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = PolicyStatus.GRANTED;
        }
        if (priority == null) {
            priority = 0;
        }
    }

    /**
     * Pre-update callback
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
    }

    public MedicalSpecialty getSpecialty() {
        return specialty;
    }

    public void setSpecialty(MedicalSpecialty specialty) {
        this.specialty = specialty;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public PolicyStatus getStatus() {
        return status;
    }

    public void setStatus(PolicyStatus status) {
        this.status = status;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Equals, HashCode, and ToString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessPolicy that = (AccessPolicy) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(patientCi, that.patientCi) &&
               Objects.equals(clinicId, that.clinicId) &&
               specialty == that.specialty;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, patientCi, clinicId, specialty);
    }

    @Override
    public String toString() {
        return "AccessPolicy{" +
               "id=" + id +
               ", patientCi='" + patientCi + '\'' +
               ", clinicId='" + clinicId + '\'' +
               ", specialty=" + specialty +
               ", documentId=" + documentId +
               ", status=" + status +
               ", priority=" + priority +
               ", validFrom=" + validFrom +
               ", validUntil=" + validUntil +
               ", createdAt=" + createdAt +
               '}';
    }
}
