package uy.gub.hcen.policy.dto;

import jakarta.validation.constraints.*;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyEffect;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Policy Update Request DTO
 * <p>
 * Data Transfer Object for updating existing patient access control policies.
 * All fields are optional - only non-null fields will be updated.
 * At least one field must be provided for the update to be valid.
 * <p>
 * Request Example:
 * <pre>
 * {
 *   "policyConfig": "{\"allowedTypes\": [\"LAB_RESULT\", \"IMAGING\", \"PRESCRIPTION\"]}",
 *   "policyEffect": "DENY",
 *   "priority": 20
 * }
 * </pre>
 * <p>
 * Update Rules:
 * <ul>
 *   <li>Only non-null fields are updated</li>
 *   <li>Policy type and patient CI cannot be changed (create new policy instead)</li>
 *   <li>At least one field must be provided</li>
 *   <li>Policy configuration must be valid JSON if provided</li>
 *   <li>Cache is invalidated automatically after update</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
public class PolicyUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Optional: Updated policy configuration as JSON
     * Must be valid JSON and not exceed 5000 characters if provided
     */
    @Size(max = 5000, message = "Policy configuration must not exceed 5000 characters")
    private String policyConfig;

    /**
     * Optional: Updated policy effect - PERMIT or DENY
     */
    private PolicyEffect policyEffect;

    /**
     * Optional: Updated start date for policy validity
     */
    private LocalDateTime validFrom;

    /**
     * Optional: Updated end date for policy validity
     */
    private LocalDateTime validUntil;

    /**
     * Optional: Updated priority for conflict resolution
     */
    @Min(value = 0, message = "Priority must be non-negative")
    @Max(value = 100, message = "Priority must not exceed 100")
    private Integer priority;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor
     */
    public PolicyUpdateRequest() {
    }

    /**
     * Full constructor
     *
     * @param policyConfig Policy configuration JSON
     * @param policyEffect Policy effect
     * @param validFrom Start date
     * @param validUntil End date
     * @param priority Priority level
     */
    public PolicyUpdateRequest(String policyConfig, PolicyEffect policyEffect,
                               LocalDateTime validFrom, LocalDateTime validUntil, Integer priority) {
        this.policyConfig = policyConfig;
        this.policyEffect = policyEffect;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.priority = priority;
    }

    // ================================================================
    // Validation Methods
    // ================================================================

    /**
     * Checks if at least one field is set for update
     *
     * @return true if at least one field is non-null
     */
    public boolean hasAtLeastOneField() {
        return policyConfig != null ||
                policyEffect != null ||
                validFrom != null ||
                validUntil != null ||
                priority != null;
    }

    /**
     * Checks if this is a valid update request
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return hasAtLeastOneField();
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

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
        PolicyUpdateRequest that = (PolicyUpdateRequest) o;
        return Objects.equals(policyConfig, that.policyConfig) &&
                policyEffect == that.policyEffect &&
                Objects.equals(validFrom, that.validFrom) &&
                Objects.equals(validUntil, that.validUntil) &&
                Objects.equals(priority, that.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policyConfig, policyEffect, validFrom, validUntil, priority);
    }

    @Override
    public String toString() {
        return "PolicyUpdateRequest{" +
                "policyConfig='" + (policyConfig != null ? "***" : null) + '\'' +
                ", policyEffect=" + policyEffect +
                ", validFrom=" + validFrom +
                ", validUntil=" + validUntil +
                ", priority=" + priority +
                '}';
    }
}
