package uy.gub.hcen.policy.dto;

import uy.gub.hcen.service.policy.dto.PolicyEvaluationResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Access Evaluation Response DTO
 * <p>
 * Data Transfer Object representing the result of an access control policy evaluation.
 * Returned by the /api/policies/evaluate endpoint to indicate whether access
 * should be granted, denied, or requires patient approval.
 * <p>
 * Response Example:
 * <pre>
 * {
 *   "decision": "PERMIT",
 *   "reason": "Access granted by DOCUMENT_TYPE policy (id: 456)",
 *   "evaluatedPolicies": [456, 457, 458],
 *   "decidingPolicy": 456
 * }
 * </pre>
 * <p>
 * Decision Values:
 * <ul>
 *   <li><b>PERMIT</b>: Access granted - return document (HTTP 200)</li>
 *   <li><b>DENY</b>: Access denied - return forbidden (HTTP 403)</li>
 *   <li><b>PENDING</b>: Requires patient approval - send notification (HTTP 202)</li>
 * </ul>
 * <p>
 * Usage:
 * <ul>
 *   <li>REST API endpoints use this to determine HTTP response code</li>
 *   <li>Frontend displays decision and reason to users</li>
 *   <li>Audit system logs evaluated policies and outcome</li>
 *   <li>Notification system triggered for PENDING decisions</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
public class AccessEvaluationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Final access decision
     * One of: "PERMIT", "DENY", or "PENDING"
     */
    private final String decision;

    /**
     * Human-readable explanation for the decision
     */
    private final String reason;

    /**
     * IDs of all policies that were evaluated
     * Used for audit trail and debugging
     */
    private final List<Long> evaluatedPolicies;

    /**
     * ID of the policy that made the final decision
     * Null if no policy matched (PENDING decision)
     */
    private final Long decidingPolicy;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Full constructor
     *
     * @param decision Access decision
     * @param reason Explanation for decision
     * @param evaluatedPolicies List of evaluated policy IDs
     * @param decidingPolicy ID of deciding policy
     */
    public AccessEvaluationResponse(String decision, String reason,
                                    List<Long> evaluatedPolicies, Long decidingPolicy) {
        this.decision = decision;
        this.reason = reason;
        this.evaluatedPolicies = evaluatedPolicies != null ? new ArrayList<>(evaluatedPolicies) : new ArrayList<>();
        this.decidingPolicy = decidingPolicy;
    }

    // ================================================================
    // Factory Methods
    // ================================================================

    /**
     * Creates an AccessEvaluationResponse from PolicyEvaluationResult
     * <p>
     * Converts internal PolicyEngine result to external REST API response format.
     *
     * @param result PolicyEvaluationResult from PolicyEngine
     * @return AccessEvaluationResponse for REST API
     * @throws IllegalArgumentException if result is null
     */
    public static AccessEvaluationResponse fromPolicyEvaluationResult(PolicyEvaluationResult result) {
        if (result == null) {
            throw new IllegalArgumentException("PolicyEvaluationResult cannot be null");
        }

        return new AccessEvaluationResponse(
                result.getDecision().name(),
                result.getReason(),
                result.getEvaluatedPolicies(),
                result.getDecidingPolicy()
        );
    }

    /**
     * Creates a PERMIT response
     *
     * @param reason Reason for permit
     * @param decidingPolicyId ID of permitting policy
     * @param evaluatedPolicies List of evaluated policies
     * @return AccessEvaluationResponse with PERMIT decision
     */
    public static AccessEvaluationResponse permit(String reason, Long decidingPolicyId,
                                                   List<Long> evaluatedPolicies) {
        return new AccessEvaluationResponse("PERMIT", reason, evaluatedPolicies, decidingPolicyId);
    }

    /**
     * Creates a DENY response
     *
     * @param reason Reason for denial
     * @param decidingPolicyId ID of denying policy
     * @param evaluatedPolicies List of evaluated policies
     * @return AccessEvaluationResponse with DENY decision
     */
    public static AccessEvaluationResponse deny(String reason, Long decidingPolicyId,
                                                List<Long> evaluatedPolicies) {
        return new AccessEvaluationResponse("DENY", reason, evaluatedPolicies, decidingPolicyId);
    }

    /**
     * Creates a PENDING response
     *
     * @param reason Reason for pending status
     * @param evaluatedPolicies List of evaluated policies
     * @return AccessEvaluationResponse with PENDING decision
     */
    public static AccessEvaluationResponse pending(String reason, List<Long> evaluatedPolicies) {
        return new AccessEvaluationResponse("PENDING", reason, evaluatedPolicies, null);
    }

    // ================================================================
    // Getters Only (Immutable)
    // ================================================================

    public String getDecision() {
        return decision;
    }

    public String getReason() {
        return reason;
    }

    public List<Long> getEvaluatedPolicies() {
        return new ArrayList<>(evaluatedPolicies);
    }

    public Long getDecidingPolicy() {
        return decidingPolicy;
    }

    // ================================================================
    // Utility Methods
    // ================================================================

    /**
     * Checks if access was granted
     *
     * @return true if decision is PERMIT
     */
    public boolean isPermitted() {
        return "PERMIT".equals(decision);
    }

    /**
     * Checks if access was denied
     *
     * @return true if decision is DENY
     */
    public boolean isDenied() {
        return "DENY".equals(decision);
    }

    /**
     * Checks if access requires patient approval
     *
     * @return true if decision is PENDING
     */
    public boolean isPending() {
        return "PENDING".equals(decision);
    }

    /**
     * Gets the HTTP status code for this decision
     *
     * @return HTTP status code (200, 403, or 202)
     */
    public int getHttpStatusCode() {
        switch (decision) {
            case "PERMIT":
                return 200; // OK
            case "DENY":
                return 403; // Forbidden
            case "PENDING":
                return 202; // Accepted (pending approval)
            default:
                return 500; // Internal Server Error
        }
    }

    /**
     * Gets the number of policies that were evaluated
     *
     * @return Count of evaluated policies
     */
    public int getEvaluatedPolicyCount() {
        return evaluatedPolicies.size();
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessEvaluationResponse that = (AccessEvaluationResponse) o;
        return Objects.equals(decision, that.decision) &&
                Objects.equals(reason, that.reason) &&
                Objects.equals(decidingPolicy, that.decidingPolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(decision, reason, decidingPolicy);
    }

    @Override
    public String toString() {
        return "AccessEvaluationResponse{" +
                "decision='" + decision + '\'' +
                ", reason='" + reason + '\'' +
                ", evaluatedPoliciesCount=" + evaluatedPolicies.size() +
                ", decidingPolicy=" + decidingPolicy +
                '}';
    }
}
