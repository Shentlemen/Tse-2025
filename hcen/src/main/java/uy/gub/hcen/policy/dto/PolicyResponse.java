package uy.gub.hcen.policy.dto;

import uy.gub.hcen.policy.entity.AccessPolicy;
import uy.gub.hcen.policy.entity.MedicalSpecialty;
import uy.gub.hcen.policy.entity.PolicyStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Policy Response DTO
 *
 * Immutable Data Transfer Object representing an access control policy in API responses.
 * Provides a clean, safe view of policy data without exposing entity internals.
 *
 * Response Example:
 * <pre>
 * {
 *   "id": 123,
 *   "patientCi": "uy-ci-12345678",
 *   "clinicId": "clinic-001",
 *   "clinicName": "Hospital de Clinicas",
 *   "specialty": "CARDIOLOGIA",
 *   "specialtyName": "Cardiologia",
 *   "documentId": null,
 *   "status": "GRANTED",
 *   "statusName": "Otorgado",
 *   "validFrom": "2025-10-21T00:00:00",
 *   "validUntil": "2026-10-21T00:00:00",
 *   "priority": 10,
 *   "createdAt": "2025-10-21T14:30:00",
 *   "updatedAt": "2025-10-21T14:30:00"
 * }
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 2.0
 * @since 2025-11-18
 */
public class PolicyResponse implements Serializable {

    private static final long serialVersionUID = 2L;

    private final Long id;
    private final String patientCi;
    private final String clinicId;
    private final String clinicName;
    private final MedicalSpecialty specialty;
    private final String specialtyName;
    private final Long documentId;
    private final PolicyStatus status;
    private final String statusName;
    private final LocalDateTime validFrom;
    private final LocalDateTime validUntil;
    private final Integer priority;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Full constructor - used by factory methods
     *
     * @param id Policy ID
     * @param patientCi Patient CI
     * @param clinicId Clinic ID
     * @param clinicName Clinic name
     * @param specialty Medical specialty
     * @param documentId Document ID (null for all documents)
     * @param status Policy status
     * @param validFrom Start date
     * @param validUntil End date
     * @param priority Priority level
     * @param createdAt Creation timestamp
     * @param updatedAt Update timestamp
     */
    public PolicyResponse(Long id, String patientCi, String clinicId, String clinicName,
                         MedicalSpecialty specialty, Long documentId, PolicyStatus status,
                         LocalDateTime validFrom, LocalDateTime validUntil,
                         Integer priority, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.patientCi = patientCi;
        this.clinicId = clinicId;
        this.clinicName = clinicName;
        this.specialty = specialty;
        this.specialtyName = specialty != null ? specialty.getDisplayName() : null;
        this.documentId = documentId;
        this.status = status;
        this.statusName = status != null ? status.getDisplayName() : null;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ================================================================
    // Factory Methods
    // ================================================================

    /**
     * Creates a PolicyResponse from an AccessPolicy entity
     *
     * This is the primary factory method for converting entities to DTOs.
     * Ensures safe conversion without exposing internal entity state.
     *
     * @param policy AccessPolicy entity
     * @return PolicyResponse DTO
     * @throws IllegalArgumentException if policy is null
     */
    public static PolicyResponse fromEntity(AccessPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("Policy cannot be null");
        }

        return new PolicyResponse(
                policy.getId(),
                policy.getPatientCi(),
                policy.getClinicId(),
                null, // Clinic name will be populated by service layer if needed
                policy.getSpecialty(),
                policy.getDocumentId(),
                policy.getStatus(),
                policy.getValidFrom(),
                policy.getValidUntil(),
                policy.getPriority(),
                policy.getCreatedAt(),
                policy.getUpdatedAt()
        );
    }

    /**
     * Creates a PolicyResponse from an AccessPolicy entity with clinic name
     *
     * @param policy AccessPolicy entity
     * @param clinicName Clinic display name
     * @return PolicyResponse DTO
     * @throws IllegalArgumentException if policy is null
     */
    public static PolicyResponse fromEntity(AccessPolicy policy, String clinicName) {
        if (policy == null) {
            throw new IllegalArgumentException("Policy cannot be null");
        }

        return new PolicyResponse(
                policy.getId(),
                policy.getPatientCi(),
                policy.getClinicId(),
                clinicName,
                policy.getSpecialty(),
                policy.getDocumentId(),
                policy.getStatus(),
                policy.getValidFrom(),
                policy.getValidUntil(),
                policy.getPriority(),
                policy.getCreatedAt(),
                policy.getUpdatedAt()
        );
    }

    // ================================================================
    // Getters Only (Immutable)
    // ================================================================

    public Long getId() {
        return id;
    }

    public String getPatientCi() {
        return patientCi;
    }

    public String getClinicId() {
        return clinicId;
    }

    public String getClinicName() {
        return clinicName;
    }

    public MedicalSpecialty getSpecialty() {
        return specialty;
    }

    public String getSpecialtyName() {
        return specialtyName;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public PolicyStatus getStatus() {
        return status;
    }

    public String getStatusName() {
        return statusName;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public Integer getPriority() {
        return priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ================================================================
    // Utility Methods
    // ================================================================

    /**
     * Checks if this policy is currently valid based on validity dates and status
     *
     * @return true if policy is within validity period and status is GRANTED
     */
    public boolean isCurrentlyValid() {
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
     * @return true if documentId is null
     */
    public boolean appliesToAllDocuments() {
        return documentId == null;
    }

    /**
     * Checks if this is an active (GRANTED) policy
     *
     * @return true if status is GRANTED
     */
    public boolean isActive() {
        return status == PolicyStatus.GRANTED;
    }

    /**
     * Gets a formatted description of the policy for display
     *
     * @return Human-readable policy description
     */
    public String getDescription() {
        String clinicPart = clinicName != null ? clinicName : clinicId;
        String specialtyPart = specialtyName != null ? specialtyName : (specialty != null ? specialty.name() : "");
        String statusPart = statusName != null ? statusName : (status != null ? status.name() : "");

        return specialtyPart + " de " + clinicPart + " - " + statusPart;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolicyResponse that = (PolicyResponse) o;
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
        return "PolicyResponse{" +
                "id=" + id +
                ", patientCi='" + patientCi + '\'' +
                ", clinicId='" + clinicId + '\'' +
                ", clinicName='" + clinicName + '\'' +
                ", specialty=" + specialty +
                ", documentId=" + documentId +
                ", status=" + status +
                ", priority=" + priority +
                ", validFrom=" + validFrom +
                ", validUntil=" + validUntil +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
