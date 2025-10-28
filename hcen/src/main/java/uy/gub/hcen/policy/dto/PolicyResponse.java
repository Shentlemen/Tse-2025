package uy.gub.hcen.policy.dto;

import uy.gub.hcen.policy.entity.AccessPolicy;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyType;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyEffect;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Policy Response DTO
 * <p>
 * Immutable Data Transfer Object representing an access control policy in API responses.
 * Provides a clean, safe view of policy data without exposing entity internals.
 * <p>
 * Response Example:
 * <pre>
 * {
 *   "id": 123,
 *   "patientCi": "12345678",
 *   "policyType": "DOCUMENT_TYPE",
 *   "policyConfig": "{\"allowedTypes\": [\"LAB_RESULT\", \"IMAGING\"]}",
 *   "policyEffect": "PERMIT",
 *   "validFrom": "2025-10-21T00:00:00",
 *   "validUntil": "2026-10-21T00:00:00",
 *   "priority": 10,
 *   "createdAt": "2025-10-21T14:30:00",
 *   "updatedAt": "2025-10-21T14:30:00"
 * }
 * </pre>
 * <p>
 * Design Principles:
 * <ul>
 *   <li>Immutable - all fields are final (getters only)</li>
 *   <li>Safe - no internal entity references exposed</li>
 *   <li>Complete - includes all policy information for client consumption</li>
 *   <li>Timestamped - includes creation and update timestamps for audit</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
public class PolicyResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String patientCi;
    private final PolicyType policyType;
    private final String policyConfig;
    private final PolicyEffect policyEffect;
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
     * @param policyType Policy type
     * @param policyConfig Policy configuration JSON
     * @param policyEffect Policy effect
     * @param validFrom Start date
     * @param validUntil End date
     * @param priority Priority level
     * @param createdAt Creation timestamp
     * @param updatedAt Update timestamp
     */
    public PolicyResponse(Long id, String patientCi, PolicyType policyType, String policyConfig,
                         PolicyEffect policyEffect, LocalDateTime validFrom, LocalDateTime validUntil,
                         Integer priority, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.patientCi = patientCi;
        this.policyType = policyType;
        this.policyConfig = policyConfig;
        this.policyEffect = policyEffect;
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
     * <p>
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
                policy.getPolicyType(),
                policy.getPolicyConfig(),
                policy.getPolicyEffect(),
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

    public PolicyType getPolicyType() {
        return policyType;
    }

    public String getPolicyConfig() {
        return policyConfig;
    }

    public PolicyEffect getPolicyEffect() {
        return policyEffect;
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
     * Checks if this policy is currently valid based on validity dates
     *
     * @return true if policy is within validity period
     */
    public boolean isCurrentlyValid() {
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
     * Checks if this is a PERMIT policy
     *
     * @return true if policy effect is PERMIT
     */
    public boolean isPermitPolicy() {
        return policyEffect == PolicyEffect.PERMIT;
    }

    /**
     * Checks if this is a DENY policy
     *
     * @return true if policy effect is DENY
     */
    public boolean isDenyPolicy() {
        return policyEffect == PolicyEffect.DENY;
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
                policyType == that.policyType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, patientCi, policyType);
    }

    @Override
    public String toString() {
        return "PolicyResponse{" +
                "id=" + id +
                ", patientCi='" + patientCi + '\'' +
                ", policyType=" + policyType +
                ", policyEffect=" + policyEffect +
                ", priority=" + priority +
                ", validFrom=" + validFrom +
                ", validUntil=" + validUntil +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
