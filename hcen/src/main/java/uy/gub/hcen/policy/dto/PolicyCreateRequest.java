package uy.gub.hcen.policy.dto;

import jakarta.validation.constraints.*;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyType;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyEffect;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Policy Create Request DTO
 * <p>
 * Data Transfer Object for creating new patient access control policies.
 * Includes comprehensive validation annotations to ensure data integrity.
 * <p>
 * Request Example:
 * <pre>
 * {
 *   "patientCi": "12345678",
 *   "policyType": "DOCUMENT_TYPE",
 *   "policyConfig": "{\"allowedTypes\": [\"LAB_RESULT\", \"IMAGING\"]}",
 *   "policyEffect": "PERMIT",
 *   "validFrom": "2025-10-21T00:00:00",
 *   "validUntil": "2026-10-21T00:00:00",
 *   "priority": 10
 * }
 * </pre>
 * <p>
 * Policy Configuration JSON Format Examples:
 * <ul>
 *   <li>DOCUMENT_TYPE: {"allowedTypes": ["LAB_RESULT", "IMAGING"]}</li>
 *   <li>SPECIALTY: {"allowedSpecialties": ["CARDIOLOGY", "GENERAL_MEDICINE"]}</li>
 *   <li>TIME_BASED: {"allowedDays": ["MONDAY", "FRIDAY"], "allowedHours": "09:00-17:00"}</li>
 *   <li>CLINIC: {"allowedClinics": ["clinic-001", "clinic-002"]}</li>
 *   <li>PROFESSIONAL: {"allowedProfessionals": ["prof-123", "prof-456"]}</li>
 *   <li>EMERGENCY_OVERRIDE: {"enabled": true, "requiresAudit": true}</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
public class PolicyCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Patient CI (Cédula de Identidad) - owner of the policy
     * Must be between 7-9 digits (Uruguay CI format)
     */
    @NotBlank(message = "Patient CI is required")
    @Pattern(regexp = "^uy-ci-\\d{7,9}$", message = "Patient CI must be 7-9 digits")
    private String patientCi;

    /**
     * Policy type - determines how the policy is evaluated
     */
    @NotNull(message = "Policy type is required")
    private PolicyType policyType;

    /**
     * Policy configuration as JSON
     * Structure varies by policy type
     * Must be valid JSON and not exceed 5000 characters
     */
    @NotBlank(message = "Policy configuration is required")
    @Size(max = 5000, message = "Policy configuration must not exceed 5000 characters")
    private String policyConfig;

    /**
     * Policy effect - PERMIT (allow) or DENY (block)
     */
    @NotNull(message = "Policy effect is required")
    private PolicyEffect policyEffect;

    /**
     * Optional: Start date for policy validity
     * If null, policy is valid from creation
     */
    private LocalDateTime validFrom;

    /**
     * Optional: End date for policy validity
     * If null, policy is valid indefinitely
     */
    private LocalDateTime validUntil;

    /**
     * Priority for conflict resolution
     * Higher priority wins when policies conflict
     * Default: 0
     */
    @Min(value = 0, message = "Priority must be non-negative")
    @Max(value = 100, message = "Priority must not exceed 100")
    private Integer priority = 0;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor
     */
    public PolicyCreateRequest() {
        this.priority = 0;
    }

    /**
     * Full constructor
     *
     * @param patientCi Patient CI
     * @param policyType Policy type
     * @param policyConfig Policy configuration JSON
     * @param policyEffect Policy effect
     * @param validFrom Start date
     * @param validUntil End date
     * @param priority Priority level
     */
    public PolicyCreateRequest(String patientCi, PolicyType policyType, String policyConfig,
                               PolicyEffect policyEffect, LocalDateTime validFrom,
                               LocalDateTime validUntil, Integer priority) {
        this.patientCi = patientCi;
        this.policyType = policyType;
        this.policyConfig = policyConfig;
        this.policyEffect = policyEffect;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.priority = priority != null ? priority : 0;
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

    public PolicyType getPolicyType() {
        return policyType;
    }

    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
    }

    public String getPolicyConfig() {
        return policyConfig;
    }

    public void setPolicyConfig(String policyConfig) {
        this.policyConfig = policyConfig;
    }

    public PolicyEffect getPolicyEffect() {
        return policyEffect;
    }

    public void setPolicyEffect(PolicyEffect policyEffect) {
        this.policyEffect = policyEffect;
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

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolicyCreateRequest that = (PolicyCreateRequest) o;
        return Objects.equals(patientCi, that.patientCi) &&
                policyType == that.policyType &&
                Objects.equals(policyConfig, that.policyConfig) &&
                policyEffect == that.policyEffect;
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientCi, policyType, policyConfig, policyEffect);
    }

    @Override
    public String toString() {
        return "PolicyCreateRequest{" +
                "patientCi='" + patientCi + '\'' +
                ", policyType=" + policyType +
                ", policyEffect=" + policyEffect +
                ", priority=" + priority +
                ", validFrom=" + validFrom +
                ", validUntil=" + validUntil +
                '}';
    }
}
