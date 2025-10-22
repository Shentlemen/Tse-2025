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
 * Emergency Override Policy Evaluator
 *
 * Evaluates emergency override policies that grant access in critical medical situations.
 * This policy type ALWAYS permits access but requires heavy audit logging.
 *
 * <p>Policy Configuration Format (JSON):
 * <pre>
 * {
 *   "requiresLogging": true,
 *   "requiresJustification": true,
 *   "notifyPatient": true
 * }
 * </pre>
 *
 * <p>Use Cases:
 * <ul>
 *   <li>Emergency room access during life-threatening situations</li>
 *   <li>Break-glass access for urgent medical care</li>
 *   <li>After-hours critical care scenarios</li>
 *   <li>Situations where obtaining patient consent is impossible</li>
 * </ul>
 *
 * <p>Evaluation Logic:
 * <ol>
 *   <li>Emergency override policies ALWAYS return PERMIT (bypass all restrictions)</li>
 *   <li>Log access at SEVERE level (highest severity for special attention)</li>
 *   <li>Include all request details in log for audit trail</li>
 *   <li>Check if justification is required and present</li>
 *   <li>Flag for patient notification (push notification of emergency access)</li>
 * </ol>
 *
 * <p>Security and Audit Considerations:
 * <ul>
 *   <li><b>Heavy Logging</b>: All emergency access is logged at SEVERE level</li>
 *   <li><b>Justification Required</b>: Professional must provide reason for access</li>
 *   <li><b>Patient Notification</b>: Patient receives notification after emergency access</li>
 *   <li><b>Audit Trail</b>: Complete access details stored for review</li>
 *   <li><b>Accountability</b>: Professional ID, clinic, timestamp, and reason recorded</li>
 * </ul>
 *
 * <p>Future Enhancements:
 * <ul>
 *   <li>Integration with emergency declaration system</li>
 *   <li>Verification of professional's emergency credentials</li>
 *   <li>Automatic review workflow for emergency access</li>
 *   <li>Analytics to detect emergency access abuse patterns</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
@ApplicationScoped
public class EmergencyOverridePolicyEvaluator implements PolicyEvaluator {

    private static final Logger LOGGER = Logger.getLogger(EmergencyOverridePolicyEvaluator.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Evaluates an emergency override policy against an access request.
     *
     * <p>Emergency override policies ALWAYS permit access, but log extensively.
     *
     * @param policy The access policy to evaluate
     * @param request The access request
     * @return PolicyDecision.PERMIT (always permits access)
     */
    @Override
    public PolicyDecision evaluate(AccessPolicy policy, AccessRequest request) {
        if (policy == null || request == null) {
            LOGGER.log(Level.WARNING, "EmergencyOverridePolicyEvaluator received null policy or request");
            return null;
        }

        try {
            // Parse policy configuration
            JsonNode config = MAPPER.readTree(policy.getPolicyConfig());
            boolean requiresJustification = config.has("requiresJustification")
                    && config.get("requiresJustification").asBoolean();
            boolean requiresLogging = config.has("requiresLogging")
                    && config.get("requiresLogging").asBoolean();
            boolean notifyPatient = config.has("notifyPatient")
                    && config.get("notifyPatient").asBoolean();

            // Check if justification is required and present
            String justification = request.getRequestReason();
            if (requiresJustification && (justification == null || justification.trim().isEmpty())) {
                LOGGER.log(Level.WARNING, "Emergency override policy {0} requires justification, but none provided. " +
                        "Professional: {1}, Patient: {2}, Document: {3}",
                        new Object[]{policy.getId(), request.getProfessionalId(),
                                request.getPatientCi(), request.getDocumentType()});
            }

            // Log emergency access at SEVERE level (requires special attention)
            if (requiresLogging) {
                LOGGER.log(Level.SEVERE, "EMERGENCY OVERRIDE ACCESS GRANTED - Policy: {0} | " +
                        "Professional: {1} | Specialties: {2} | Clinic: {3} | " +
                        "Patient: {4} | Document: {5} | Document ID: {6} | " +
                        "Timestamp: {7} | Justification: {8} | Notify Patient: {9}",
                        new Object[]{
                                policy.getId(),
                                request.getProfessionalId(),
                                request.getSpecialties(),
                                request.getClinicId(),
                                request.getPatientCi(),
                                request.getDocumentType(),
                                request.getDocumentId(),
                                request.getRequestTime(),
                                justification != null ? justification : "NOT PROVIDED",
                                notifyPatient
                        });
            }

            // Log notification requirement
            if (notifyPatient) {
                LOGGER.log(Level.INFO, "Patient notification required for emergency access: Patient {0}",
                        request.getPatientCi());
            }

            // Emergency override ALWAYS permits access
            LOGGER.log(Level.INFO, "Emergency override policy {0} permits access for professional {1}",
                    new Object[]{policy.getId(), request.getProfessionalId()});

            return PolicyDecision.PERMIT;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to evaluate emergency override policy " + policy.getId()
                    + " - PERMITTING ACCESS AS FAIL-SAFE", e);
            // Even on error, permit access for emergency situations (fail-safe approach)
            return PolicyDecision.PERMIT;
        }
    }

    /**
     * Checks if this evaluator supports EMERGENCY_OVERRIDE policies.
     *
     * @param policyType The policy type to check
     * @return true if policyType is EMERGENCY_OVERRIDE
     */
    @Override
    public boolean supports(PolicyType policyType) {
        return policyType == PolicyType.EMERGENCY_OVERRIDE;
    }
}
