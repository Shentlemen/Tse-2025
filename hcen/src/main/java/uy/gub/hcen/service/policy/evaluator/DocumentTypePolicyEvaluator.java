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
 * Document Type Policy Evaluator
 *
 * Evaluates policies based on the type of clinical document being accessed.
 * Allows patients to grant or deny access to specific document types.
 *
 * <p>Policy Configuration Format (JSON):
 * <pre>
 * {
 *   "allowedTypes": ["LAB_RESULT", "IMAGING", "CLINICAL_NOTE"]
 * }
 * </pre>
 *
 * <p>Use Cases:
 * <ul>
 *   <li>Patient allows cardiologists to see imaging and lab results only</li>
 *   <li>Patient blocks access to psychiatric notes</li>
 *   <li>Patient permits all professionals to view vaccination records</li>
 * </ul>
 *
 * <p>Evaluation Logic:
 * <ol>
 *   <li>Parse policyConfig JSON to extract allowedTypes array</li>
 *   <li>Check if request.documentType is in allowedTypes</li>
 *   <li>If match found: return policy.policyEffect (PERMIT or DENY)</li>
 *   <li>If no match: return null (policy doesn't apply)</li>
 * </ol>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
@ApplicationScoped
public class DocumentTypePolicyEvaluator implements PolicyEvaluator {

    private static final Logger LOGGER = Logger.getLogger(DocumentTypePolicyEvaluator.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Evaluates a document type policy against an access request.
     *
     * @param policy The access policy to evaluate
     * @param request The access request
     * @return PolicyDecision (PERMIT/DENY) if document type matches, null otherwise
     */
    @Override
    public PolicyDecision evaluate(AccessPolicy policy, AccessRequest request) {
        if (policy == null || request == null) {
            LOGGER.log(Level.WARNING, "DocumentTypePolicyEvaluator received null policy or request");
            return null;
        }

        if (request.getDocumentType() == null) {
            LOGGER.log(Level.FINE, "Access request has no document type, policy not applicable");
            return null;
        }

        try {
            // Parse policy configuration
            JsonNode config = MAPPER.readTree(policy.getPolicyConfig());
            JsonNode allowedTypes = config.get("allowedTypes");

            if (allowedTypes == null || !allowedTypes.isArray()) {
                LOGGER.log(Level.WARNING, "Document type policy {0} has invalid config: missing or invalid allowedTypes array",
                        policy.getId());
                return null;
            }

            // Check if document type matches
            String requestDocType = request.getDocumentType().name();

            for (JsonNode typeNode : allowedTypes) {
                String allowedType = typeNode.asText();

                if (allowedType.equalsIgnoreCase(requestDocType)) {
                    // Policy matches - return effect
                    PolicyDecision decision = policy.getPolicyEffect() == PolicyEffect.PERMIT
                            ? PolicyDecision.PERMIT
                            : PolicyDecision.DENY;

                    LOGGER.log(Level.FINE, "Document type policy {0} matched: {1} for document type {2}",
                            new Object[]{policy.getId(), decision, requestDocType});

                    return decision;
                }
            }

            // No match - policy doesn't apply
            LOGGER.log(Level.FINE, "Document type policy {0} does not match document type {1}",
                    new Object[]{policy.getId(), requestDocType});
            return null;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to evaluate document type policy " + policy.getId(), e);
            return null;
        }
    }

    /**
     * Checks if this evaluator supports DOCUMENT_TYPE policies.
     *
     * @param policyType The policy type to check
     * @return true if policyType is DOCUMENT_TYPE
     */
    @Override
    public boolean supports(PolicyType policyType) {
        return policyType == PolicyType.DOCUMENT_TYPE;
    }
}
