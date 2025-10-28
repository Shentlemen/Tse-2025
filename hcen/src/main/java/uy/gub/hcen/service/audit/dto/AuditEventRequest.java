package uy.gub.hcen.service.audit.dto;

import uy.gub.hcen.audit.entity.AuditLog.ActionOutcome;
import uy.gub.hcen.audit.entity.AuditLog.EventType;

import java.util.HashMap;
import java.util.Map;

/**
 * Audit Event Request DTO
 *
 * Data Transfer Object for programmatic audit event logging.
 * Used by services to provide structured audit information to AuditService.
 *
 * <p>This DTO ensures all required audit information is captured consistently:
 * <ul>
 *   <li>Who performed the action (actorId, actorType)</li>
 *   <li>What was done (eventType, resourceType, resourceId)</li>
 *   <li>How did it end (actionOutcome)</li>
 *   <li>Where from (ipAddress, userAgent)</li>
 *   <li>Additional context (details map)</li>
 * </ul>
 *
 * Example Usage:
 * <pre>
 * AuditEventRequest event = new AuditEventRequest();
 * event.setEventType(EventType.ACCESS);
 * event.setActorId("12345678");
 * event.setActorType("PROFESSIONAL");
 * event.setResourceType("DOCUMENT");
 * event.setResourceId("doc-123");
 * event.setActionOutcome(ActionOutcome.SUCCESS);
 * event.addDetail("documentType", "LAB_RESULT");
 * event.addDetail("clinicId", "clinic-001");
 * auditService.logEvent(event);
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 * @see uy.gub.hcen.service.audit.AuditService
 * @see AuditEventBuilder
 */
public class AuditEventRequest {

    /**
     * Type of event being audited
     * Required field
     */
    private EventType eventType;

    /**
     * Identifier of the actor performing the action
     * (CI for patients, professional ID for professionals, "system" for automated)
     * Required field
     */
    private String actorId;

    /**
     * Type of actor (PATIENT, PROFESSIONAL, ADMIN, SYSTEM)
     * Required field
     */
    private String actorType;

    /**
     * Type of resource being acted upon (DOCUMENT, USER, POLICY, CLINIC, etc.)
     * Required field
     */
    private String resourceType;

    /**
     * Identifier of the specific resource
     * Required field
     */
    private String resourceId;

    /**
     * Outcome of the action (SUCCESS, FAILURE, DENIED)
     * Required field
     */
    private ActionOutcome actionOutcome;

    /**
     * IP address of the actor (IPv4 or IPv6)
     * Optional field
     */
    private String ipAddress;

    /**
     * User agent string (browser/client information)
     * Optional field
     */
    private String userAgent;

    /**
     * Additional contextual details
     * Optional field - will be serialized to JSON
     */
    private Map<String, Object> details;

    // =========================================================================
    // Constructors
    // =========================================================================

    /**
     * Default constructor
     */
    public AuditEventRequest() {
        this.details = new HashMap<>();
    }

    /**
     * Full constructor
     *
     * @param eventType Type of event
     * @param actorId Actor identifier
     * @param actorType Type of actor
     * @param resourceType Resource type
     * @param resourceId Resource identifier
     * @param actionOutcome Outcome of action
     */
    public AuditEventRequest(EventType eventType, String actorId, String actorType,
                             String resourceType, String resourceId, ActionOutcome actionOutcome) {
        this.eventType = eventType;
        this.actorId = actorId;
        this.actorType = actorType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.actionOutcome = actionOutcome;
        this.details = new HashMap<>();
    }

    /**
     * Full constructor with optional fields
     *
     * @param eventType Type of event
     * @param actorId Actor identifier
     * @param actorType Type of actor
     * @param resourceType Resource type
     * @param resourceId Resource identifier
     * @param actionOutcome Outcome of action
     * @param ipAddress IP address
     * @param userAgent User agent string
     */
    public AuditEventRequest(EventType eventType, String actorId, String actorType,
                             String resourceType, String resourceId, ActionOutcome actionOutcome,
                             String ipAddress, String userAgent) {
        this(eventType, actorId, actorType, resourceType, resourceId, actionOutcome);
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    // =========================================================================
    // Convenience Methods
    // =========================================================================

    /**
     * Adds a detail to the details map
     * Convenience method for building complex audit context
     *
     * @param key Detail key
     * @param value Detail value
     * @return This request (for method chaining)
     */
    public AuditEventRequest addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
        return this;
    }

    /**
     * Sets multiple details at once
     *
     * @param details Map of details to add
     * @return This request (for method chaining)
     */
    public AuditEventRequest withDetails(Map<String, Object> details) {
        if (details != null) {
            if (this.details == null) {
                this.details = new HashMap<>();
            }
            this.details.putAll(details);
        }
        return this;
    }

    /**
     * Validates that all required fields are present
     *
     * @return true if all required fields are set, false otherwise
     */
    public boolean isValid() {
        return eventType != null &&
               actorId != null && !actorId.isEmpty() &&
               actorType != null && !actorType.isEmpty() &&
               resourceType != null && !resourceType.isEmpty() &&
               resourceId != null && !resourceId.isEmpty() &&
               actionOutcome != null;
    }

    // =========================================================================
    // Getters and Setters
    // =========================================================================

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getActorType() {
        return actorType;
    }

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public ActionOutcome getActionOutcome() {
        return actionOutcome;
    }

    public void setActionOutcome(ActionOutcome actionOutcome) {
        this.actionOutcome = actionOutcome;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    // =========================================================================
    // Object Methods
    // =========================================================================

    @Override
    public String toString() {
        return "AuditEventRequest{" +
               "eventType=" + eventType +
               ", actorId='" + actorId + '\'' +
               ", actorType='" + actorType + '\'' +
               ", resourceType='" + resourceType + '\'' +
               ", resourceId='" + resourceId + '\'' +
               ", actionOutcome=" + actionOutcome +
               ", ipAddress='" + ipAddress + '\'' +
               ", detailsCount=" + (details != null ? details.size() : 0) +
               '}';
    }
}
