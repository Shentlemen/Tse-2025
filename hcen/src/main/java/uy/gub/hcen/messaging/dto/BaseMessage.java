package uy.gub.hcen.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Base message class for all JMS messages in HCEN Central.
 * <p>
 * Provides common fields for message identification, traceability, and audit purposes.
 * All specific message types should extend this class to ensure consistent message structure.
 * <p>
 * Key Features:
 * - Idempotency: messageId ensures duplicate messages can be detected and handled
 * - Traceability: timestamp and sourceSystem enable audit trails
 * - Type Safety: eventType discriminator for message routing and processing
 * <p>
 * Message Flow:
 * Peripheral Node → JMS Queue → MDB Listener → Message Processor → Service Layer → Database
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique message identifier (UUID format recommended).
     * <p>
     * Used for:
     * - Idempotency: prevent duplicate processing of the same message
     * - Correlation: track message lifecycle across systems
     * - Debugging: identify specific messages in logs
     * <p>
     * Example: "msg-550e8400-e29b-41d4-a716-446655440000"
     */
    private String messageId;

    /**
     * Timestamp when message was created by the source system.
     * <p>
     * Used for:
     * - Audit trails: when did the event occur?
     * - Message ordering: process messages chronologically if needed
     * - Staleness detection: reject messages older than threshold
     * <p>
     * Format: ISO-8601 (e.g., "2025-11-13T10:30:00Z")
     */
    private LocalDateTime timestamp;

    /**
     * Identifier of the system that originated this message.
     * <p>
     * Examples:
     * - "clinic-001" (peripheral clinic node)
     * - "provider-hospital-X" (health provider)
     * - "mobile-app" (mobile application)
     * <p>
     * Used for:
     * - Source tracking: which system sent this event?
     * - Security: validate source is authorized to send this message type
     * - Debugging: identify problematic source systems
     */
    private String sourceSystem;

    /**
     * Event type discriminator for message routing and processing.
     * <p>
     * Supported values:
     * - USER_CREATED: New user registration
     * - DOCUMENT_CREATED: New clinical document registration
     * <p>
     * Used for:
     * - Message routing: direct to appropriate queue
     * - Processing logic: select correct handler
     * - Monitoring: track event type distribution
     */
    private String eventType;

    /**
     * Default constructor for JSON deserialization.
     */
    public BaseMessage() {
    }

    /**
     * Constructor with all fields.
     *
     * @param messageId    Unique message identifier
     * @param timestamp    Message creation timestamp
     * @param sourceSystem Originating system identifier
     * @param eventType    Event type discriminator
     */
    public BaseMessage(String messageId, LocalDateTime timestamp, String sourceSystem, String eventType) {
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.sourceSystem = sourceSystem;
        this.eventType = eventType;
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseMessage that = (BaseMessage) o;
        return Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }

    @Override
    public String toString() {
        return "BaseMessage{" +
                "messageId='" + messageId + '\'' +
                ", timestamp=" + timestamp +
                ", sourceSystem='" + sourceSystem + '\'' +
                ", eventType='" + eventType + '\'' +
                '}';
    }
}
