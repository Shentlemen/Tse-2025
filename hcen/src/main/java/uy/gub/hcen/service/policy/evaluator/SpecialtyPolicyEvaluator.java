package uy.gub.hcen.service.policy.evaluator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import uy.gub.hcen.policy.entity.AccessPolicy;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyEffect;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyType;
import uy.gub.hcen.service.policy.dto.AccessRequest;
import uy.gub.hcen.service.policy.dto.PolicyDecision;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Specialty Policy Evaluator
 *
 * Evaluates policies based on the medical specialty of the healthcare professional
 * requesting access. Allows patients to grant or deny access to professionals with
 * specific specialties.
 *
 * <p>Policy Configuration Format (JSON):
 * <pre>
 * {
 *   "allowedSpecialties": ["CARDIOLOGY", "GENERAL_MEDICINE", "EMERGENCY_MEDICINE"]
 * }
 * </pre>
 *
 * <p>Use Cases:
 * <ul>
 *   <li>Cardiac patient allows cardiologists and general practitioners to view all records</li>
 *   <li>Patient denies psychiatrists from viewing non-psychiatric records</li>
 *   <li>Patient permits emergency medicine doctors for emergency access</li>
 * </ul>
 *
 * <p>Evaluation Logic:
 * <ol>
 *   <li>Parse policyConfig JSON to extract allowedSpecialties array</li>
 *   <li>Check if ANY of the professional's specialties is in allowedSpecialties</li>
 *   <li>If match found: return policy.policyEffect (PERMIT or DENY)</li>
 *   <li>If no match: return null (policy doesn't apply)</li>
 * </ol>
 *
 * <p>Multi-Specialty Handling:
 * A professional may have multiple specialties (e.g., CARDIOLOGY and INTERNAL_MEDICINE).
 * The policy matches if ANY of the professional's specialties is in the allowed list.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
@ApplicationScoped
public class SpecialtyPolicyEvaluator implements PolicyEvaluator {

    private static final Logger LOGGER = Logger.getLogger(SpecialtyPolicyEvaluator.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Evaluates a specialty policy against an access request.
     *
     * @param policy The access policy to evaluate
     * @param request The access request
     * @return PolicyDecision (PERMIT/DENY) if any specialty matches, null otherwise
     */
    @Override
    public PolicyDecision evaluate(AccessPolicy policy, AccessRequest request) {
        if (policy == null || request == null) {
            LOGGER.log(Level.WARNING, "SpecialtyPolicyEvaluator received null policy or request");
            return null;
        }

        List<String> professionalSpecialties = request.getSpecialties();
        if (professionalSpecialties == null || professionalSpecialties.isEmpty()) {
            LOGGER.log(Level.FINE, "Access request has no specialties, policy not applicable");
            return null;
        }

        try {
            // Parse policy configuration
            JsonNode config = MAPPER.readTree(policy.getPolicyConfig());
            JsonNode allowedSpecialties = config.get("allowedSpecialties");

            if (allowedSpecialties == null || !allowedSpecialties.isArray()) {
                LOGGER.log(Level.WARNING, "Specialty policy {0} has invalid config: missing or invalid allowedSpecialties array",
                        policy.getId());
                return null;
            }

            // Check if any professional specialty matches any allowed specialty
            for (String professionalSpecialty : professionalSpecialties) {
                for (JsonNode specialtyNode : allowedSpecialties) {
                    String allowedSpecialty = specialtyNode.asText();

                    if (allowedSpecialty.equalsIgnoreCase(professionalSpecialty)) {
                        // Match found - return effect
                        PolicyDecision decision = policy.getPolicyEffect() == PolicyEffect.PERMIT
                                ? PolicyDecision.PERMIT
                                : PolicyDecision.DENY;

                        LOGGER.log(Level.FINE, "Specialty policy {0} matched: {1} for specialty {2}",
                                new Object[]{policy.getId(), decision, professionalSpecialty});

                        return decision;
                    }
                }
            }

            // No match - policy doesn't apply
            LOGGER.log(Level.FINE, "Specialty policy {0} does not match any of professional's specialties: {1}",
                    new Object[]{policy.getId(), professionalSpecialties});
            return null;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to evaluate specialty policy " + policy.getId(), e);
            return null;
        }
    }

    /**
     * Checks if this evaluator supports SPECIALTY policies.
     *
     * @param policyType The policy type to check
     * @return true if policyType is SPECIALTY
     */
    @Override
    public boolean supports(PolicyType policyType) {
        return policyType == PolicyType.SPECIALTY;
    }
}
