package uy.gub.hcen.service.policy.evaluator;

import uy.gub.hcen.policy.entity.AccessPolicy;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyType;
import uy.gub.hcen.service.policy.dto.AccessRequest;
import uy.gub.hcen.service.policy.dto.PolicyDecision;

/**
 * Policy Evaluator Interface
 *
 * Strategy pattern interface for evaluating different types of access control policies.
 * Each policy type (DOCUMENT_TYPE, SPECIALTY, TIME_BASED, etc.) has its own evaluator
 * implementation with specific logic for that policy type.
 *
 * <p>Design Pattern: Strategy Pattern
 * <ul>
 *   <li>Context: PolicyEngine</li>
 *   <li>Strategy: PolicyEvaluator (this interface)</li>
 *   <li>Concrete Strategies: DocumentTypePolicyEvaluator, SpecialtyPolicyEvaluator, etc.</li>
 * </ul>
 *
 * <p>Implementation Guidelines:
 * <ul>
 *   <li>Return PolicyDecision.PERMIT if policy matches and effect is PERMIT</li>
 *   <li>Return PolicyDecision.DENY if policy matches and effect is DENY</li>
 *   <li>Return null if policy doesn't apply to this request</li>
 *   <li>Parse policyConfig JSON to extract policy-specific parameters</li>
 *   <li>Handle JSON parsing errors gracefully (log and return null)</li>
 *   <li>Use @ApplicationScoped for CDI injection</li>
 * </ul>
 *
 * <p>CDI Integration:
 * All implementations are automatically discovered and injected into PolicyEngine
 * via Jakarta CDI's @Inject Instance&lt;PolicyEvaluator&gt; mechanism.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
public interface PolicyEvaluator {

    /**
     * Evaluates whether this policy applies to the given access request.
     *
     * <p>Return Values:
     * <ul>
     *   <li><b>PolicyDecision.PERMIT</b>: Policy matches and allows access</li>
     *   <li><b>PolicyDecision.DENY</b>: Policy matches and denies access</li>
     *   <li><b>null</b>: Policy does not apply to this request</li>
     * </ul>
     *
     * <p>Example Logic (DocumentTypePolicyEvaluator):
     * <pre>
     * JsonNode config = parseConfig(policy.getPolicyConfig());
     * JsonNode allowedTypes = config.get("allowedTypes");
     *
     * for (JsonNode type : allowedTypes) {
     *     if (type.asText().equals(request.getDocumentType().name())) {
     *         // Policy matches
     *         return policy.getPolicyEffect() == PolicyEffect.PERMIT
     *             ? PolicyDecision.PERMIT
     *             : PolicyDecision.DENY;
     *     }
     * }
     *
     * // Policy doesn't match
     * return null;
     * </pre>
     *
     * @param policy The access policy to evaluate
     * @param request The access request containing context (professional, document, etc.)
     * @return PolicyDecision (PERMIT/DENY) if policy applies, null if not applicable
     */
    PolicyDecision evaluate(AccessPolicy policy, AccessRequest request);

    /**
     * Checks if this evaluator supports the given policy type.
     *
     * <p>Used by PolicyEngine to select the appropriate evaluator for each policy.
     *
     * <p>Example Implementation:
     * <pre>
     * public boolean supports(PolicyType policyType) {
     *     return policyType == PolicyType.DOCUMENT_TYPE;
     * }
     * </pre>
     *
     * @param policyType The policy type to check
     * @return true if this evaluator can evaluate this policy type, false otherwise
     */
    boolean supports(PolicyType policyType);
}
