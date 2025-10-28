package uy.gub.hcen.service.policy.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Policy Evaluation Result DTO
 *
 * Data Transfer Object representing the outcome of a policy evaluation by the PolicyEngine.
 * Contains the final decision, reasoning, and metadata about which policies were evaluated.
 *
 * <p>This result is used by:
 * <ul>
 *   <li>REST API endpoints to determine HTTP response (200/403/202)</li>
 *   <li>Audit logging system to record authorization decisions</li>
 *   <li>Frontend applications to display access status to users</li>
 *   <li>Notification system to send approval requests to patients</li>
 * </ul>
 *
 * <p>Result Components:
 * <ul>
 *   <li><b>decision</b>: Final outcome (PERMIT, DENY, PENDING)</li>
 *   <li><b>reason</b>: Human-readable explanation for the decision</li>
 *   <li><b>evaluatedPolicies</b>: IDs of all policies that were considered</li>
 *   <li><b>decidingPolicy</b>: ID of the policy that determined the final decision</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
public class PolicyEvaluationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Final policy decision
     * One of: PERMIT, DENY, or PENDING
     */
    private PolicyDecision decision;

    /**
     * Human-readable explanation for the decision
     *
     * <p>Examples:
     * <ul>
     *   <li>"Access permitted by specialty policy (CARDIOLOGY allowed)"</li>
     *   <li>"Access denied by professional blacklist policy"</li>
     *   <li>"No applicable policy found; patient approval required"</li>
     *   <li>"Emergency override policy permits access"</li>
     * </ul>
     */
    private String reason;

    /**
     * List of policy IDs that were evaluated
     * Includes all policies considered, even if they didn't match
     * Used for audit trail and debugging
     */
    private List<Long> evaluatedPolicies;

    /**
     * ID of the policy that made the final decision
     * Optional - may be null if no policy matched (PENDING)
     * For DENY: points to the denying policy (highest priority if multiple)
     * For PERMIT: points to the permitting policy (highest priority if multiple)
     */
    private Long decidingPolicy;

    /**
     * Default constructor
     */
    public PolicyEvaluationResult() {
        this.evaluatedPolicies = new ArrayList<>();
    }

    /**
     * Constructor with decision and reason
     *
     * @param decision Policy decision
     * @param reason Explanation for the decision
     */
    public PolicyEvaluationResult(PolicyDecision decision, String reason) {
        this();
        this.decision = decision;
        this.reason = reason;
    }

    /**
     * Full constructor
     *
     * @param decision Policy decision
     * @param reason Explanation for the decision
     * @param evaluatedPolicies List of evaluated policy IDs
     * @param decidingPolicy ID of the deciding policy
     */
    public PolicyEvaluationResult(PolicyDecision decision, String reason,
                                 List<Long> evaluatedPolicies, Long decidingPolicy) {
        this.decision = decision;
        this.reason = reason;
        this.evaluatedPolicies = evaluatedPolicies != null ? new ArrayList<>(evaluatedPolicies) : new ArrayList<>();
        this.decidingPolicy = decidingPolicy;
    }

    /**
     * Builder pattern for fluent construction
     *
     * @return New Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a PERMIT result
     *
     * @param reason Reason for permit
     * @param decidingPolicyId ID of the permitting policy
     * @return New PolicyEvaluationResult with PERMIT decision
     */
    public static PolicyEvaluationResult permit(String reason, Long decidingPolicyId) {
        PolicyEvaluationResult result = new PolicyEvaluationResult(PolicyDecision.PERMIT, reason);
        result.setDecidingPolicy(decidingPolicyId);
        return result;
    }

    /**
     * Creates a DENY result
     *
     * @param reason Reason for denial
     * @param decidingPolicyId ID of the denying policy
     * @return New PolicyEvaluationResult with DENY decision
     */
    public static PolicyEvaluationResult deny(String reason, Long decidingPolicyId) {
        PolicyEvaluationResult result = new PolicyEvaluationResult(PolicyDecision.DENY, reason);
        result.setDecidingPolicy(decidingPolicyId);
        return result;
    }

    /**
     * Creates a PENDING result
     *
     * @param reason Reason for pending status
     * @return New PolicyEvaluationResult with PENDING decision
     */
    public static PolicyEvaluationResult pending(String reason) {
        return new PolicyEvaluationResult(PolicyDecision.PENDING, reason);
    }

    // Getters and Setters

    public PolicyDecision getDecision() {
        return decision;
    }

    public void setDecision(PolicyDecision decision) {
        this.decision = decision;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<Long> getEvaluatedPolicies() {
        return evaluatedPolicies;
    }

    public void setEvaluatedPolicies(List<Long> evaluatedPolicies) {
        this.evaluatedPolicies = evaluatedPolicies != null ? new ArrayList<>(evaluatedPolicies) : new ArrayList<>();
    }

    public Long getDecidingPolicy() {
        return decidingPolicy;
    }

    public void setDecidingPolicy(Long decidingPolicy) {
        this.decidingPolicy = decidingPolicy;
    }

    // Utility methods

    /**
     * Adds a policy ID to the list of evaluated policies
     *
     * @param policyId Policy ID to add
     */
    public void addEvaluatedPolicy(Long policyId) {
        if (policyId != null && !this.evaluatedPolicies.contains(policyId)) {
            this.evaluatedPolicies.add(policyId);
        }
    }

    /**
     * Checks if access was granted
     *
     * @return true if decision is PERMIT
     */
    public boolean isPermitted() {
        return decision != null && decision.isPermitted();
    }

    /**
     * Checks if access was denied
     *
     * @return true if decision is DENY
     */
    public boolean isDenied() {
        return decision != null && decision.isDenied();
    }

    /**
     * Checks if access requires patient approval
     *
     * @return true if decision is PENDING
     */
    public boolean isPending() {
        return decision != null && decision.isPending();
    }

    /**
     * Gets the HTTP status code for this result
     *
     * @return HTTP status code
     */
    public int getHttpStatusCode() {
        return decision != null ? decision.getHttpStatusCode() : 500;
    }

    // Equals, HashCode, and ToString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolicyEvaluationResult that = (PolicyEvaluationResult) o;
        return decision == that.decision &&
                Objects.equals(reason, that.reason) &&
                Objects.equals(decidingPolicy, that.decidingPolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(decision, reason, decidingPolicy);
    }

    @Override
    public String toString() {
        return "PolicyEvaluationResult{" +
                "decision=" + decision +
                ", reason='" + reason + '\'' +
                ", evaluatedPolicies=" + evaluatedPolicies +
                ", decidingPolicy=" + decidingPolicy +
                '}';
    }

    /**
     * Builder class for fluent PolicyEvaluationResult construction
     */
    public static class Builder {
        private PolicyDecision decision;
        private String reason;
        private List<Long> evaluatedPolicies = new ArrayList<>();
        private Long decidingPolicy;

        public Builder decision(PolicyDecision decision) {
            this.decision = decision;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder evaluatedPolicies(List<Long> evaluatedPolicies) {
            this.evaluatedPolicies = evaluatedPolicies != null ? new ArrayList<>(evaluatedPolicies) : new ArrayList<>();
            return this;
        }

        public Builder addEvaluatedPolicy(Long policyId) {
            if (policyId != null && !this.evaluatedPolicies.contains(policyId)) {
                this.evaluatedPolicies.add(policyId);
            }
            return this;
        }

        public Builder decidingPolicy(Long decidingPolicy) {
            this.decidingPolicy = decidingPolicy;
            return this;
        }

        public PolicyEvaluationResult build() {
            return new PolicyEvaluationResult(decision, reason, evaluatedPolicies, decidingPolicy);
        }
    }
}
