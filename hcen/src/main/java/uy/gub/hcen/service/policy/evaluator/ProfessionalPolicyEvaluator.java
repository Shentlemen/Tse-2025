package uy.gub.hcen.service.policy.evaluator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import uy.gub.hcen.policy.entity.AccessPolicy;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyType;
import uy.gub.hcen.service.policy.dto.AccessRequest;
import uy.gub.hcen.service.policy.dto.PolicyDecision;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Professional Policy Evaluator
 *
 * Evaluates policies based on specific healthcare professionals (whitelist or blacklist).
 * Allows patients to grant or deny access to individual professionals by their ID.
 *
 * <p>Policy Configuration Format (JSON):
 * <pre>
 * {
 *   "allowedProfessionals": ["prof-123", "prof-456"],
 *   "deniedProfessionals": ["prof-789", "prof-999"]
 * }
 * </pre>
 *
 * <p>Either allowedProfessionals or deniedProfessionals can be specified, or both.
 *
 * <p>Use Cases:
 * <ul>
 *   <li>Patient explicitly allows their primary care physician</li>
 *   <li>Patient blacklists a professional due to privacy concerns</li>
 *   <li>Patient grants access to a trusted specialist</li>
 *   <li>Patient maintains a whitelist of approved professionals</li>
 * </ul>
 *
 * <p>Evaluation Logic (Priority Order):
 * <ol>
 *   <li>Parse policyConfig JSON to extract allowedProfessionals and deniedProfessionals</li>
 *   <li>If professionalId is in deniedProfessionals: return DENY (blacklist takes precedence)</li>
 *   <li>If professionalId is in allowedProfessionals: return PERMIT</li>
 *   <li>If no match: return null (policy doesn't apply)</li>
 * </ol>
 *
 * <p>Important Notes:
 * <ul>
 *   <li>Deny list always takes precedence over allow list</li>
 *   <li>This evaluator ignores policy.policyEffect and uses explicit PERMIT/DENY logic</li>
 *   <li>Both lists are optional (policy may have only allowedProfessionals or only deniedProfessionals)</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
@ApplicationScoped
public class ProfessionalPolicyEvaluator implements PolicyEvaluator {

    private static final Logger LOGGER = Logger.getLogger(ProfessionalPolicyEvaluator.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Evaluates a professional policy against an access request.
     *
     * @param policy The access policy to evaluate
     * @param request The access request
     * @return PolicyDecision (PERMIT/DENY) if professional matches, null otherwise
     */
    @Override
    public PolicyDecision evaluate(AccessPolicy policy, AccessRequest request) {
        if (policy == null || request == null) {
            LOGGER.log(Level.WARNING, "ProfessionalPolicyEvaluator received null policy or request");
            return null;
        }

        String requestProfessionalId = request.getProfessionalId();
        if (requestProfessionalId == null || requestProfessionalId.trim().isEmpty()) {
            LOGGER.log(Level.FINE, "Access request has no professional ID, policy not applicable");
            return null;
        }

        try {
            // Parse policy configuration
            JsonNode config = MAPPER.readTree(policy.getPolicyConfig());
            JsonNode deniedProfessionals = config.get("deniedProfessionals");
            JsonNode allowedProfessionals = config.get("allowedProfessionals");

            // Check denied list first (blacklist takes precedence)
            if (deniedProfessionals != null && deniedProfessionals.isArray()) {
                for (JsonNode professionalNode : deniedProfessionals) {
                    String deniedProfessionalId = professionalNode.asText();

                    if (deniedProfessionalId.equalsIgnoreCase(requestProfessionalId)) {
                        // Professional is blacklisted - always DENY
                        LOGGER.log(Level.FINE, "Professional policy {0} matched (blacklist): DENY for professional {1}",
                                new Object[]{policy.getId(), requestProfessionalId});
                        return PolicyDecision.DENY;
                    }
                }
            }

            // Check allowed list (whitelist)
            if (allowedProfessionals != null && allowedProfessionals.isArray()) {
                for (JsonNode professionalNode : allowedProfessionals) {
                    String allowedProfessionalId = professionalNode.asText();

                    if (allowedProfessionalId.equalsIgnoreCase(requestProfessionalId)) {
                        // Professional is whitelisted - always PERMIT
                        LOGGER.log(Level.FINE, "Professional policy {0} matched (whitelist): PERMIT for professional {1}",
                                new Object[]{policy.getId(), requestProfessionalId});
                        return PolicyDecision.PERMIT;
                    }
                }
            }

            // No match - policy doesn't apply
            LOGGER.log(Level.FINE, "Professional policy {0} does not match professional ID: {1}",
                    new Object[]{policy.getId(), requestProfessionalId});
            return null;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to evaluate professional policy " + policy.getId(), e);
            return null;
        }
    }

    /**
     * Checks if this evaluator supports PROFESSIONAL policies.
     *
     * @param policyType The policy type to check
     * @return true if policyType is PROFESSIONAL
     */
    @Override
    public boolean supports(PolicyType policyType) {
        return policyType == PolicyType.PROFESSIONAL;
    }
}
