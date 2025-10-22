package uy.gub.hcen.audit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import uy.gub.hcen.audit.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Audit Log Response DTO
 *
 * Immutable response object representing an audit log entry.
 * Maps from the AuditLog entity to a JSON-serializable format.
 *
 * <p>Response Format:
 * <pre>
 * {
 *   "id": 1001,
 *   "eventType": "ACCESS",
 *   "actorId": "prof-123",
 *   "actorType": "PROFESSIONAL",
 *   "resourceType": "DOCUMENT",
 *   "resourceId": "456",
 *   "actionOutcome": "SUCCESS",
 *   "ipAddress": "192.168.1.100",
 *   "userAgent": "Mozilla/5.0...",
 *   "timestamp": "2025-10-21T14:30:00",
 *   "details": {
 *     "patientCi": "12345678",
 *     "documentType": "LAB_RESULT",
 *     "clinicId": "clinic-001"
 *   }
 * }
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
public class AuditLogResponse {

    private static final Logger LOGGER = Logger.getLogger(AuditLogResponse.class.getName());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Long id;
    private final String eventType;
    private final String actorId;
    private final String actorType;
    private final String resourceType;
    private final String resourceId;
    private final String actionOutcome;
    private final String ipAddress;
    private final String userAgent;
    private final LocalDateTime timestamp;
    private final Map<String, Object> details;

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Constructor (private - use factory method fromEntity)
     *
     * @param id Audit log ID
     * @param eventType Event type name
     * @param actorId Actor identifier
     * @param actorType Actor type
     * @param resourceType Resource type
     * @param resourceId Resource identifier
     * @param actionOutcome Action outcome name
     * @param ipAddress IP address
     * @param userAgent User agent string
     * @param timestamp Event timestamp
     * @param details Additional details map
     */
    private AuditLogResponse(Long id, String eventType, String actorId, String actorType,
                            String resourceType, String resourceId, String actionOutcome,
                            String ipAddress, String userAgent, LocalDateTime timestamp,
                            Map<String, Object> details) {
        this.id = id;
        this.eventType = eventType;
        this.actorId = actorId;
        this.actorType = actorType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.actionOutcome = actionOutcome;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.timestamp = timestamp;
        this.details = details;
    }

    // =========================================================================
    // Factory Method
    // =========================================================================

    /**
     * Creates AuditLogResponse from AuditLog entity
     *
     * Converts enum values to strings and parses JSON details to Map.
     *
     * @param auditLog AuditLog entity
     * @return AuditLogResponse DTO
     */
    public static AuditLogResponse fromEntity(AuditLog auditLog) {
        if (auditLog == null) {
            throw new IllegalArgumentException("AuditLog cannot be null");
        }

        Map<String, Object> detailsMap = parseDetails(auditLog.getDetails());

        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getEventType() != null ? auditLog.getEventType().name() : null,
                auditLog.getActorId(),
                auditLog.getActorType(),
                auditLog.getResourceType(),
                auditLog.getResourceId(),
                auditLog.getActionOutcome() != null ? auditLog.getActionOutcome().name() : null,
                auditLog.getIpAddress(),
                auditLog.getUserAgent(),
                auditLog.getTimestamp(),
                detailsMap
        );
    }

    /**
     * Parses JSON details string to Map
     *
     * @param detailsJson JSON string from entity
     * @return Map of details (empty if parsing fails)
     */
    private static Map<String, Object> parseDetails(String detailsJson) {
        if (detailsJson == null || detailsJson.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = OBJECT_MAPPER.readValue(detailsJson, Map.class);
            return result != null ? result : new HashMap<>();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse audit details JSON: " + e.getMessage(), e);
            return new HashMap<>();
        }
    }

    // =========================================================================
    // Getters (Immutable)
    // =========================================================================

    /**
     * Gets the audit log ID
     *
     * @return Audit log ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the event type name
     *
     * @return Event type (e.g., "ACCESS", "MODIFICATION")
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Gets the actor identifier
     *
     * @return Actor ID (CI, professional ID, or "system")
     */
    public String getActorId() {
        return actorId;
    }

    /**
     * Gets the actor type
     *
     * @return Actor type (e.g., "PATIENT", "PROFESSIONAL")
     */
    public String getActorType() {
        return actorType;
    }

    /**
     * Gets the resource type
     *
     * @return Resource type (e.g., "DOCUMENT", "USER")
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Gets the resource identifier
     *
     * @return Resource ID
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Gets the action outcome name
     *
     * @return Action outcome (e.g., "SUCCESS", "DENIED")
     */
    public String getActionOutcome() {
        return actionOutcome;
    }

    /**
     * Gets the IP address
     *
     * @return IP address (may be null)
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Gets the user agent string
     *
     * @return User agent (may be null)
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Gets the event timestamp
     *
     * @return Timestamp when event occurred
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the additional details
     *
     * @return Map of additional context (never null)
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    // =========================================================================
    // Object Methods
    // =========================================================================

    @Override
    public String toString() {
        return "AuditLogResponse{" +
                "id=" + id +
                ", eventType='" + eventType + '\'' +
                ", actorId='" + actorId + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", actionOutcome='" + actionOutcome + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
