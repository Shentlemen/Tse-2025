package uy.gub.hcen.audit.dto;

import java.util.List;
import java.util.Map;

/**
 * Audit Statistics Response DTO
 *
 * Provides system-wide audit statistics for administrative dashboards.
 * Includes counts by event type, outcome, and top actors.
 *
 * <p>Response Format:
 * <pre>
 * {
 *   "totalEvents": 10000,
 *   "eventsByType": {
 *     "ACCESS": 6000,
 *     "MODIFICATION": 2000,
 *     "CREATION": 1500,
 *     "DELETION": 500
 *   },
 *   "eventsByOutcome": {
 *     "SUCCESS": 9500,
 *     "DENIED": 400,
 *     "FAILURE": 100
 *   },
 *   "topActors": [
 *     { "actorId": "prof-123", "eventCount": 450 },
 *     { "actorId": "prof-456", "eventCount": 320 }
 *   ]
 * }
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
public class AuditStatisticsResponse {

    private final long totalEvents;
    private final Map<String, Long> eventsByType;
    private final Map<String, Long> eventsByOutcome;
    private final List<ActorStat> topActors;

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Constructor for statistics response
     *
     * @param totalEvents Total number of audit events
     * @param eventsByType Map of event type to count
     * @param eventsByOutcome Map of outcome to count
     * @param topActors List of top actors by event count
     */
    public AuditStatisticsResponse(long totalEvents,
                                   Map<String, Long> eventsByType,
                                   Map<String, Long> eventsByOutcome,
                                   List<ActorStat> topActors) {
        this.totalEvents = totalEvents;
        this.eventsByType = eventsByType;
        this.eventsByOutcome = eventsByOutcome;
        this.topActors = topActors != null ? topActors : List.of();
    }

    // =========================================================================
    // Getters
    // =========================================================================

    /**
     * Gets the total number of audit events
     *
     * @return Total event count
     */
    public long getTotalEvents() {
        return totalEvents;
    }

    /**
     * Gets event counts grouped by event type
     *
     * @return Map of event type name to count
     */
    public Map<String, Long> getEventsByType() {
        return eventsByType;
    }

    /**
     * Gets event counts grouped by outcome
     *
     * @return Map of outcome name to count
     */
    public Map<String, Long> getEventsByOutcome() {
        return eventsByOutcome;
    }

    /**
     * Gets the top actors by event count
     *
     * @return List of actor statistics
     */
    public List<ActorStat> getTopActors() {
        return topActors;
    }

    // =========================================================================
    // Inner Class: ActorStat
    // =========================================================================

    /**
     * Actor Statistic - Represents activity count for an actor
     *
     * Used to show the most active users/professionals in the system.
     */
    public static class ActorStat {

        private final String actorId;
        private final long eventCount;

        /**
         * Constructor for ActorStat
         *
         * @param actorId Actor identifier
         * @param eventCount Number of events by this actor
         */
        public ActorStat(String actorId, long eventCount) {
            this.actorId = actorId;
            this.eventCount = eventCount;
        }

        /**
         * Gets the actor ID
         *
         * @return Actor identifier
         */
        public String getActorId() {
            return actorId;
        }

        /**
         * Gets the event count for this actor
         *
         * @return Number of events
         */
        public long getEventCount() {
            return eventCount;
        }

        @Override
        public String toString() {
            return "ActorStat{" +
                    "actorId='" + actorId + '\'' +
                    ", eventCount=" + eventCount +
                    '}';
        }
    }

    // =========================================================================
    // Object Methods
    // =========================================================================

    @Override
    public String toString() {
        return "AuditStatisticsResponse{" +
                "totalEvents=" + totalEvents +
                ", eventTypesCount=" + eventsByType.size() +
                ", outcomesCount=" + eventsByOutcome.size() +
                ", topActorsCount=" + topActors.size() +
                '}';
    }
}
