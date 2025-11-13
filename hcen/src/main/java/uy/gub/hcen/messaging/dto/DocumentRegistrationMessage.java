package uy.gub.hcen.messaging.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JMS message for clinical document registration events from peripheral nodes.
 * <p>
 * This message is sent by clinics/health providers when they create a new clinical
 * document and want to register its metadata in the HCEN Central RNDC registry.
 * <p>
 * Queue: hcen.documents.registration
 * Event Type: DOCUMENT_CREATED
 * <p>
 * Processing Flow:
 * 1. Peripheral node creates document in local storage
 * 2. Peripheral node calculates SHA-256 hash
 * 3. Peripheral node sends message to queue
 * 4. DocumentRegistrationListener (MDB) receives message
 * 5. Message deserialized to this object
 * 6. Message validated
 * 7. DocumentRegistrationProcessor processes business logic
 * 8. RndcService.registerDocument() persists metadata to database
 * 9. Success/failure logged to audit system
 * <p>
 * Idempotency:
 * Messages with same messageId are processed only once. Duplicate documentLocator
 * registrations return existing document without error (idempotent behavior at service layer).
 * <p>
 * Important: This message contains METADATA only. The actual document content
 * remains in peripheral storage at the location specified by documentLocator.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class DocumentRegistrationMessage extends BaseMessage {

    private static final long serialVersionUID = 1L;

    /**
     * Document registration data payload.
     */
    private DocumentRegistrationPayload payload;

    /**
     * Default constructor for JSON deserialization.
     */
    public DocumentRegistrationMessage() {
        super();
    }

    /**
     * Constructor with all fields.
     *
     * @param messageId    Unique message identifier
     * @param timestamp    Message creation timestamp
     * @param sourceSystem Originating system identifier
     * @param eventType    Event type (should be "DOCUMENT_CREATED")
     * @param payload      Document registration data
     */
    public DocumentRegistrationMessage(String messageId, LocalDateTime timestamp,
                                        String sourceSystem, String eventType,
                                        DocumentRegistrationPayload payload) {
        super(messageId, timestamp, sourceSystem, eventType);
        this.payload = payload;
    }

    /**
     * Convenience constructor with payload only (auto-sets eventType).
     *
     * @param messageId    Unique message identifier
     * @param timestamp    Message creation timestamp
     * @param sourceSystem Originating system identifier
     * @param payload      Document registration data
     */
    public DocumentRegistrationMessage(String messageId, LocalDateTime timestamp,
                                        String sourceSystem, DocumentRegistrationPayload payload) {
        super(messageId, timestamp, sourceSystem, "DOCUMENT_CREATED");
        this.payload = payload;
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

    public DocumentRegistrationPayload getPayload() {
        return payload;
    }

    public void setPayload(DocumentRegistrationPayload payload) {
        this.payload = payload;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DocumentRegistrationMessage that = (DocumentRegistrationMessage) o;
        return Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), payload);
    }

    @Override
    public String toString() {
        return "DocumentRegistrationMessage{" +
                "messageId='" + getMessageId() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", sourceSystem='" + getSourceSystem() + '\'' +
                ", eventType='" + getEventType() + '\'' +
                ", payload=" + payload +
                '}';
    }
}
