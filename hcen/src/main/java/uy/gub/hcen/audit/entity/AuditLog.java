package uy.gub.hcen.audit.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Audit Log Entity - Immutable Event Tracking
 *
 * Represents an immutable audit log entry in the HCEN system.
 * All system events (access, modifications, authentication, policy changes)
 * are recorded here for compliance and traceability.
 *
 * Security Features:
 * - Immutable (no setters for key fields after creation)
 * - Append-only (no @PreUpdate lifecycle)
 * - Comprehensive event tracking (who, what, when, where, outcome)
 * - JSONB details for flexible context storage
 *
 * PostgreSQL Table: audit.audit_logs
 * Retention: 90 days in PostgreSQL, then archived to MongoDB (optional)
 *
 * Compliance: AC026 - Patients can view who accessed their records and when
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@Entity
@Table(name = "audit_logs", schema = "audit", indexes = {
    @Index(name = "idx_audit_event_type", columnList = "event_type"),
    @Index(name = "idx_audit_actor_id", columnList = "actor_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_resource_type_id", columnList = "resource_type, resource_id")
})
public class AuditLog {

    /**
     * Event Type Enumeration
     * Defines the types of events that can be audited in the system
     */
    public enum EventType {
        /** Document access by health professional */
        ACCESS,

        /** Modification of existing resource */
        MODIFICATION,

        /** Creation of new resource */
        CREATION,

        /** Deletion of resource */
        DELETION,

        /** Access policy change by patient */
        POLICY_CHANGE,

        /** Access request by health professional */
        ACCESS_REQUEST,

        /** Access approval by patient */
        ACCESS_APPROVAL,

        /** Access denial by patient */
        ACCESS_DENIAL,

        /** Successful authentication */
        AUTHENTICATION_SUCCESS,

        /** Failed authentication attempt */
        AUTHENTICATION_FAILURE
    }

    /**
     * Action Outcome Enumeration
     * Defines the result of the audited action
     */
    public enum ActionOutcome {
        /** Action completed successfully */
        SUCCESS,

        /** Action failed due to technical error */
        FAILURE,

        /** Action denied due to policy or authorization */
        DENIED
    }

    // =========================================================================
    // Fields
    // =========================================================================

    /**
     * Unique identifier (auto-generated)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Type of event being audited
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    /**
     * Identifier of the actor performing the action
     * (CI for patients, professional ID for professionals, "system" for automated)
     */
    @Column(name = "actor_id", nullable = false, length = 100)
    private String actorId;

    /**
     * Type of actor (PATIENT, PROFESSIONAL, ADMIN, SYSTEM)
     */
    @Column(name = "actor_type", length = 20)
    private String actorType;

    /**
     * Type of resource being acted upon
     * (DOCUMENT, USER, POLICY, CLINIC, etc.)
     */
    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    /**
     * Identifier of the specific resource
     */
    @Column(name = "resource_id", nullable = false, length = 100)
    private String resourceId;

    /**
     * Outcome of the action
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_outcome", nullable = false, length = 20)
    private ActionOutcome actionOutcome;

    /**
     * IP address of the actor (IPv4 or IPv6)
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent string (browser/client information)
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Timestamp of the event
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Additional contextual details in JSON format
     * Stores flexible context like: reason, previous value, new value, error message, etc.
     */
    @Column(name = "details")
    private String details;

    // =========================================================================
    // Constructors
    // =========================================================================

    /**
     * Default constructor (required by JPA)
     */
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Full constructor for creating new audit log entries
     *
     * @param eventType Type of event
     * @param actorId Actor identifier
     * @param actorType Type of actor
     * @param resourceType Resource type
     * @param resourceId Resource identifier
     * @param actionOutcome Outcome of action
     * @param ipAddress IP address
     * @param userAgent User agent string
     * @param details Additional details in JSON format
     */
    public AuditLog(EventType eventType, String actorId, String actorType,
                    String resourceType, String resourceId, ActionOutcome actionOutcome,
                    String ipAddress, String userAgent, String details) {
        this.eventType = eventType;
        this.actorId = actorId;
        this.actorType = actorType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.actionOutcome = actionOutcome;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    // =========================================================================
    // JPA Lifecycle Callbacks
    // =========================================================================

    /**
     * Pre-persist callback - Set timestamp if not already set
     */
    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    // Note: No @PreUpdate - this entity is immutable after creation

    // =========================================================================
    // Getters (NO SETTERS - Immutable Entity)
    // =========================================================================

    public Long getId() {
        return id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getActorId() {
        return actorId;
    }

    public String getActorType() {
        return actorType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public ActionOutcome getActionOutcome() {
        return actionOutcome;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDetails() {
        return details;
    }

    // =========================================================================
    // Equals, HashCode, and ToString
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return Objects.equals(id, auditLog.id) &&
               Objects.equals(timestamp, auditLog.timestamp) &&
               Objects.equals(actorId, auditLog.actorId) &&
               Objects.equals(resourceId, auditLog.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp, actorId, resourceId);
    }

    @Override
    public String toString() {
        return "AuditLog{" +
               "id=" + id +
               ", eventType=" + eventType +
               ", actorId='" + actorId + '\'' +
               ", actorType='" + actorType + '\'' +
               ", resourceType='" + resourceType + '\'' +
               ", resourceId='" + resourceId + '\'' +
               ", actionOutcome=" + actionOutcome +
               ", timestamp=" + timestamp +
               '}';
    }
}
