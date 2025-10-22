package uy.gub.hcen.service.policy.evaluator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import uy.gub.hcen.policy.entity.AccessPolicy;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyEffect;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyType;
import uy.gub.hcen.service.policy.dto.AccessRequest;
import uy.gub.hcen.service.policy.dto.PolicyDecision;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Time-Based Policy Evaluator
 *
 * Evaluates policies based on the time and day when access is requested.
 * Allows patients to restrict access to specific days of the week and time windows.
 *
 * <p>Policy Configuration Format (JSON):
 * <pre>
 * {
 *   "allowedDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
 *   "allowedHours": "09:00-17:00"
 * }
 * </pre>
 *
 * <p>Alternative Configuration (24/7 access):
 * <pre>
 * {
 *   "allowedDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"],
 *   "allowedHours": "00:00-23:59"
 * }
 * </pre>
 *
 * <p>Use Cases:
 * <ul>
 *   <li>Patient allows access only during business hours (9 AM - 5 PM, weekdays)</li>
 *   <li>Patient denies access on weekends for non-emergency situations</li>
 *   <li>Patient permits access only during clinic operating hours</li>
 * </ul>
 *
 * <p>Evaluation Logic:
 * <ol>
 *   <li>Parse policyConfig JSON to extract allowedDays and allowedHours</li>
 *   <li>Get request timestamp (request.requestTime or current time)</li>
 *   <li>Check if request day is in allowedDays</li>
 *   <li>Check if request time is within allowedHours range</li>
 *   <li>If both match: return policy.policyEffect (PERMIT or DENY)</li>
 *   <li>If no match: return null (policy doesn't apply)</li>
 * </ol>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
@ApplicationScoped
public class TimeBasedPolicyEvaluator implements PolicyEvaluator {

    private static final Logger LOGGER = Logger.getLogger(TimeBasedPolicyEvaluator.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Evaluates a time-based policy against an access request.
     *
     * @param policy The access policy to evaluate
     * @param request The access request
     * @return PolicyDecision (PERMIT/DENY) if time/day matches, null otherwise
     */
    @Override
    public PolicyDecision evaluate(AccessPolicy policy, AccessRequest request) {
        if (policy == null || request == null) {
            LOGGER.log(Level.WARNING, "TimeBasedPolicyEvaluator received null policy or request");
            return null;
        }

        try {
            // Parse policy configuration
            JsonNode config = MAPPER.readTree(policy.getPolicyConfig());
            JsonNode allowedDays = config.get("allowedDays");
            JsonNode allowedHours = config.get("allowedHours");

            if (allowedDays == null || !allowedDays.isArray()) {
                LOGGER.log(Level.WARNING, "Time-based policy {0} has invalid config: missing or invalid allowedDays array",
                        policy.getId());
                return null;
            }

            if (allowedHours == null || !allowedHours.isTextual()) {
                LOGGER.log(Level.WARNING, "Time-based policy {0} has invalid config: missing or invalid allowedHours",
                        policy.getId());
                return null;
            }

            // Get request time (use current time if not specified)
            LocalDateTime requestTime = request.getRequestTime();
            if (requestTime == null) {
                requestTime = LocalDateTime.now();
            }

            DayOfWeek requestDay = requestTime.getDayOfWeek();
            LocalTime requestTimeOfDay = requestTime.toLocalTime();

            // Check if day matches
            boolean dayMatches = false;
            for (JsonNode dayNode : allowedDays) {
                String allowedDay = dayNode.asText();
                try {
                    DayOfWeek allowedDayOfWeek = DayOfWeek.valueOf(allowedDay.toUpperCase());
                    if (requestDay == allowedDayOfWeek) {
                        dayMatches = true;
                        break;
                    }
                } catch (IllegalArgumentException e) {
                    LOGGER.log(Level.WARNING, "Invalid day in policy {0}: {1}",
                            new Object[]{policy.getId(), allowedDay});
                }
            }

            if (!dayMatches) {
                LOGGER.log(Level.FINE, "Time-based policy {0} does not match day: {1}",
                        new Object[]{policy.getId(), requestDay});
                return null;
            }

            // Check if time matches
            String timeRange = allowedHours.asText();
            boolean timeMatches = isTimeInRange(requestTimeOfDay, timeRange);

            if (!timeMatches) {
                LOGGER.log(Level.FINE, "Time-based policy {0} does not match time: {1}",
                        new Object[]{policy.getId(), requestTimeOfDay});
                return null;
            }

            // Both day and time match - return effect
            PolicyDecision decision = policy.getPolicyEffect() == PolicyEffect.PERMIT
                    ? PolicyDecision.PERMIT
                    : PolicyDecision.DENY;

            LOGGER.log(Level.FINE, "Time-based policy {0} matched: {1} for day {2} and time {3}",
                    new Object[]{policy.getId(), decision, requestDay, requestTimeOfDay});

            return decision;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to evaluate time-based policy " + policy.getId(), e);
            return null;
        }
    }

    /**
     * Checks if a time is within a time range.
     *
     * @param time The time to check
     * @param range Time range in format "HH:mm-HH:mm" (e.g., "09:00-17:00")
     * @return true if time is within range, false otherwise
     */
    private boolean isTimeInRange(LocalTime time, String range) {
        try {
            String[] parts = range.split("-");
            if (parts.length != 2) {
                LOGGER.log(Level.WARNING, "Invalid time range format: {0}", range);
                return false;
            }

            LocalTime startTime = LocalTime.parse(parts[0].trim(), TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse(parts[1].trim(), TIME_FORMATTER);

            // Handle overnight ranges (e.g., "22:00-06:00")
            if (endTime.isBefore(startTime)) {
                // Time is in range if it's after start OR before end
                return time.isAfter(startTime) || time.equals(startTime)
                        || time.isBefore(endTime) || time.equals(endTime);
            } else {
                // Normal range
                return (time.isAfter(startTime) || time.equals(startTime))
                        && (time.isBefore(endTime) || time.equals(endTime));
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse time range: " + range, e);
            return false;
        }
    }

    /**
     * Checks if this evaluator supports TIME_BASED policies.
     *
     * @param policyType The policy type to check
     * @return true if policyType is TIME_BASED
     */
    @Override
    public boolean supports(PolicyType policyType) {
        return policyType == PolicyType.TIME_BASED;
    }
}
