package uy.gub.hcen.service.policy.evaluator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import uy.gub.hcen.policy.entity.AccessPolicy;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyEffect;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyType;
import uy.gub.hcen.service.policy.dto.AccessRequest;
import uy.gub.hcen.service.policy.dto.PolicyDecision;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clinic Policy Evaluator
 *
 * Evaluates policies based on the clinic or healthcare facility where the professional works.
 * Allows patients to grant or deny access to professionals from specific clinics.
 *
 * <p>Policy Configuration Format (JSON):
 * <pre>
 * {
 *   "allowedClinics": ["clinic-001", "clinic-002", "hospital-asse-123"]
 * }
 * </pre>
 *
 * <p>Use Cases:
 * <ul>
 *   <li>Patient allows access only from their primary care clinic</li>
 *   <li>Patient permits multiple clinics they have visited</li>
 *   <li>Patient denies access from a specific clinic due to privacy concerns</li>
 *   <li>Patient restricts access to public hospitals only (ASSE network)</li>
 * </ul>
 *
 * <p>Evaluation Logic:
 * <ol>
 *   <li>Parse policyConfig JSON to extract allowedClinics array</li>
 *   <li>Check if request.clinicId is in allowedClinics</li>
 *   <li>If match found: return policy.policyEffect (PERMIT or DENY)</li>
 *   <li>If no match: return null (policy doesn't apply)</li>
 * </ol>
 *
 * <p>Clinic Identifiers:
 * Clinic IDs follow the format: {type}-{organization}-{number}
 * Examples: "clinic-001", "hospital-asse-123", "lab-central-456"
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
@ApplicationScoped
public class ClinicPolicyEvaluator implements PolicyEvaluator {

    private static final Logger LOGGER = Logger.getLogger(ClinicPolicyEvaluator.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Evaluates a clinic policy against an access request.
     *
     * @param policy The access policy to evaluate
     * @param request The access request
     * @return PolicyDecision (PERMIT/DENY) if clinic matches, null otherwise
     */
    @Override
    public PolicyDecision evaluate(AccessPolicy policy, AccessRequest request) {
        if (policy == null || request == null) {
            LOGGER.log(Level.WARNING, "ClinicPolicyEvaluator received null policy or request");
            return null;
        }

        String requestClinicId = request.getClinicId();
        if (requestClinicId == null || requestClinicId.trim().isEmpty()) {
            LOGGER.log(Level.FINE, "Access request has no clinic ID, policy not applicable");
            return null;
        }

        try {
            // Parse policy configuration
            JsonNode config = MAPPER.readTree(policy.getPolicyConfig());
            JsonNode allowedClinics = config.get("allowedClinics");

            if (allowedClinics == null || !allowedClinics.isArray()) {
                LOGGER.log(Level.WARNING, "Clinic policy {0} has invalid config: missing or invalid allowedClinics array",
                        policy.getId());
                return null;
            }

            // Check if clinic ID matches
            for (JsonNode clinicNode : allowedClinics) {
                String allowedClinicId = clinicNode.asText();

                if (allowedClinicId.equalsIgnoreCase(requestClinicId)) {
                    // Match found - return effect
                    PolicyDecision decision = policy.getPolicyEffect() == PolicyEffect.PERMIT
                            ? PolicyDecision.PERMIT
                            : PolicyDecision.DENY;

                    LOGGER.log(Level.FINE, "Clinic policy {0} matched: {1} for clinic {2}",
                            new Object[]{policy.getId(), decision, requestClinicId});

                    return decision;
                }
            }

            // No match - policy doesn't apply
            LOGGER.log(Level.FINE, "Clinic policy {0} does not match clinic ID: {1}",
                    new Object[]{policy.getId(), requestClinicId});
            return null;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to evaluate clinic policy " + policy.getId(), e);
            return null;
        }
    }

    /**
     * Checks if this evaluator supports CLINIC policies.
     *
     * @param policyType The policy type to check
     * @return true if policyType is CLINIC
     */
    @Override
    public boolean supports(PolicyType policyType) {
        return policyType == PolicyType.CLINIC;
    }
}
