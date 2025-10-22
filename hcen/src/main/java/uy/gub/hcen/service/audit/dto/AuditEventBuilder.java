package uy.gub.hcen.service.audit.dto;

import uy.gub.hcen.audit.entity.AuditLog.ActionOutcome;
import uy.gub.hcen.audit.entity.AuditLog.EventType;

import java.util.HashMap;
import java.util.Map;

/**
 * Audit Event Builder
 *
 * Fluent builder pattern for creating AuditEventRequest objects.
 * Provides a clean, readable API for constructing audit events with
 * required and optional fields.
 *
 * <p>This builder ensures:
 * <ul>
 *   <li>Type-safe construction of audit events</li>
 *   <li>Readable, self-documenting code</li>
 *   <li>Flexible handling of optional fields</li>
 *   <li>Validation at build time</li>
 * </ul>
 *
 * Example Usage:
 * <pre>
 * AuditEventRequest event = AuditEventBuilder.builder()
 *     .eventType(EventType.ACCESS)
 *     .actorId("12345678")
 *     .actorType("PROFESSIONAL")
 *     .resourceType("DOCUMENT")
 *     .resourceId("doc-123")
 *     .outcome(ActionOutcome.SUCCESS)
 *     .ipAddress("192.168.1.100")
 *     .userAgent("Mozilla/5.0...")
 *     .detail("documentType", "LAB_RESULT")
 *     .detail("clinicId", "clinic-001")
 *     .build();
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 * @see AuditEventRequest
 * @see uy.gub.hcen.service.audit.AuditService
 */
public class AuditEventBuilder {

    private EventType eventType;
    private String actorId;
    private String actorType;
    private String resourceType;
    private String resourceId;
    private ActionOutcome actionOutcome;
    private String ipAddress;
    private String userAgent;
    private Map<String, Object> details;

    /**
     * Private constructor - use builder() static method
     */
    private AuditEventBuilder() {
        this.details = new HashMap<>();
    }

    /**
     * Creates a new builder instance
     *
     * @return New AuditEventBuilder
     */
    public static AuditEventBuilder builder() {
        return new AuditEventBuilder();
    }

    // =========================================================================
    // Required Fields
    // =========================================================================

    /**
     * Sets the event type (required)
     *
     * @param eventType Type of event (ACCESS, MODIFICATION, CREATION, etc.)
     * @return This builder
     */
    public AuditEventBuilder eventType(EventType eventType) {
        this.eventType = eventType;
        return this;
    }

    /**
     * Sets the actor ID (required)
     *
     * @param actorId Identifier of the actor (CI, professional ID, "system")
     * @return This builder
     */
    public AuditEventBuilder actorId(String actorId) {
        this.actorId = actorId;
        return this;
    }

    /**
     * Sets the actor type (required)
     *
     * @param actorType Type of actor (PATIENT, PROFESSIONAL, ADMIN, SYSTEM)
     * @return This builder
     */
    public AuditEventBuilder actorType(String actorType) {
        this.actorType = actorType;
        return this;
    }

    /**
     * Sets the resource type (required)
     *
     * @param resourceType Type of resource (DOCUMENT, USER, POLICY, CLINIC, etc.)
     * @return This builder
     */
    public AuditEventBuilder resourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    /**
     * Sets the resource ID (required)
     *
     * @param resourceId Identifier of the specific resource
     * @return This builder
     */
    public AuditEventBuilder resourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    /**
     * Sets the action outcome (required)
     *
     * @param actionOutcome Outcome of the action (SUCCESS, FAILURE, DENIED)
     * @return This builder
     */
    public AuditEventBuilder outcome(ActionOutcome actionOutcome) {
        this.actionOutcome = actionOutcome;
        return this;
    }

    // =========================================================================
    // Optional Fields
    // =========================================================================

    /**
     * Sets the IP address (optional)
     *
     * @param ipAddress IP address of the actor (IPv4 or IPv6)
     * @return This builder
     */
    public AuditEventBuilder ipAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    /**
     * Sets the user agent (optional)
     *
     * @param userAgent User agent string (browser/client information)
     * @return This builder
     */
    public AuditEventBuilder userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Adds a detail to the details map (optional)
     *
     * @param key Detail key
     * @param value Detail value
     * @return This builder
     */
    public AuditEventBuilder detail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
        return this;
    }

    /**
     * Sets multiple details at once (optional)
     *
     * @param details Map of details to add
     * @return This builder
     */
    public AuditEventBuilder details(Map<String, Object> details) {
        if (details != null) {
            if (this.details == null) {
                this.details = new HashMap<>();
            }
            this.details.putAll(details);
        }
        return this;
    }

    // =========================================================================
    // Convenience Methods for Common Actor Types
    // =========================================================================

    /**
     * Sets actor as PATIENT
     *
     * @param patientCi Patient's CI
     * @return This builder
     */
    public AuditEventBuilder patient(String patientCi) {
        this.actorId = patientCi;
        this.actorType = "PATIENT";
        return this;
    }

    /**
     * Sets actor as PROFESSIONAL
     *
     * @param professionalId Professional's ID
     * @return This builder
     */
    public AuditEventBuilder professional(String professionalId) {
        this.actorId = professionalId;
        this.actorType = "PROFESSIONAL";
        return this;
    }

    /**
     * Sets actor as ADMIN
     *
     * @param adminId Admin's ID
     * @return This builder
     */
    public AuditEventBuilder admin(String adminId) {
        this.actorId = adminId;
        this.actorType = "ADMIN";
        return this;
    }

    /**
     * Sets actor as SYSTEM
     *
     * @return This builder
     */
    public AuditEventBuilder system() {
        this.actorId = "system";
        this.actorType = "SYSTEM";
        return this;
    }

    // =========================================================================
    // Convenience Methods for Common Event Types
    // =========================================================================

    /**
     * Sets event type as ACCESS
     *
     * @return This builder
     */
    public AuditEventBuilder access() {
        this.eventType = EventType.ACCESS;
        return this;
    }

    /**
     * Sets event type as CREATION
     *
     * @return This builder
     */
    public AuditEventBuilder creation() {
        this.eventType = EventType.CREATION;
        return this;
    }

    /**
     * Sets event type as MODIFICATION
     *
     * @return This builder
     */
    public AuditEventBuilder modification() {
        this.eventType = EventType.MODIFICATION;
        return this;
    }

    /**
     * Sets event type as DELETION
     *
     * @return This builder
     */
    public AuditEventBuilder deletion() {
        this.eventType = EventType.DELETION;
        return this;
    }

    /**
     * Sets event type as POLICY_CHANGE
     *
     * @return This builder
     */
    public AuditEventBuilder policyChange() {
        this.eventType = EventType.POLICY_CHANGE;
        return this;
    }

    // =========================================================================
    // Convenience Methods for Common Outcomes
    // =========================================================================

    /**
     * Sets outcome as SUCCESS
     *
     * @return This builder
     */
    public AuditEventBuilder success() {
        this.actionOutcome = ActionOutcome.SUCCESS;
        return this;
    }

    /**
     * Sets outcome as FAILURE
     *
     * @return This builder
     */
    public AuditEventBuilder failure() {
        this.actionOutcome = ActionOutcome.FAILURE;
        return this;
    }

    /**
     * Sets outcome as DENIED
     *
     * @return This builder
     */
    public AuditEventBuilder denied() {
        this.actionOutcome = ActionOutcome.DENIED;
        return this;
    }

    // =========================================================================
    // Build Method
    // =========================================================================

    /**
     * Builds the AuditEventRequest
     *
     * @return Constructed AuditEventRequest
     * @throws IllegalStateException if required fields are missing
     */
    public AuditEventRequest build() {
        // Validate required fields
        if (eventType == null) {
            throw new IllegalStateException("eventType is required");
        }
        if (actorId == null || actorId.isEmpty()) {
            throw new IllegalStateException("actorId is required");
        }
        if (actorType == null || actorType.isEmpty()) {
            throw new IllegalStateException("actorType is required");
        }
        if (resourceType == null || resourceType.isEmpty()) {
            throw new IllegalStateException("resourceType is required");
        }
        if (resourceId == null || resourceId.isEmpty()) {
            throw new IllegalStateException("resourceId is required");
        }
        if (actionOutcome == null) {
            throw new IllegalStateException("actionOutcome is required");
        }

        // Create and populate the request
        AuditEventRequest request = new AuditEventRequest(
            eventType,
            actorId,
            actorType,
            resourceType,
            resourceId,
            actionOutcome,
            ipAddress,
            userAgent
        );

        if (details != null && !details.isEmpty()) {
            request.setDetails(new HashMap<>(details));
        }

        return request;
    }

    /**
     * Builds the AuditEventRequest without validation
     * Use only when you need to bypass validation for testing
     *
     * @return Constructed AuditEventRequest (may be invalid)
     */
    public AuditEventRequest buildUnsafe() {
        AuditEventRequest request = new AuditEventRequest();
        request.setEventType(eventType);
        request.setActorId(actorId);
        request.setActorType(actorType);
        request.setResourceType(resourceType);
        request.setResourceId(resourceId);
        request.setActionOutcome(actionOutcome);
        request.setIpAddress(ipAddress);
        request.setUserAgent(userAgent);
        if (details != null && !details.isEmpty()) {
            request.setDetails(new HashMap<>(details));
        }
        return request;
    }
}
