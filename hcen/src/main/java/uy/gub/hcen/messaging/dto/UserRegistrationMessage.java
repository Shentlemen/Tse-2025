package uy.gub.hcen.messaging.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JMS message for user registration events from peripheral nodes.
 * <p>
 * This message is sent by clinics/health providers when they register a new user
 * in their local system and want to synchronize with the HCEN Central INUS registry.
 * <p>
 * Queue: hcen.users.registration
 * Event Type: USER_CREATED
 * <p>
 * Processing Flow:
 * 1. Peripheral node sends message to queue
 * 2. UserRegistrationListener (MDB) receives message
 * 3. Message deserialized to this object
 * 4. Message validated
 * 5. UserRegistrationProcessor processes business logic
 * 6. InusService.registerUser() persists to database
 * 7. Success/failure logged to audit system
 * <p>
 * Idempotency:
 * Messages with same messageId are processed only once. Duplicate CI registrations
 * return existing user without error (idempotent behavior at service layer).
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class UserRegistrationMessage extends BaseMessage {

    private static final long serialVersionUID = 1L;

    /**
     * User registration data payload.
     */
    private UserRegistrationPayload payload;

    /**
     * Default constructor for JSON deserialization.
     */
    public UserRegistrationMessage() {
        super();
    }

    /**
     * Constructor with all fields.
     *
     * @param messageId    Unique message identifier
     * @param timestamp    Message creation timestamp
     * @param sourceSystem Originating system identifier
     * @param eventType    Event type (should be "USER_CREATED")
     * @param payload      User registration data
     */
    public UserRegistrationMessage(String messageId, LocalDateTime timestamp,
                                    String sourceSystem, String eventType,
                                    UserRegistrationPayload payload) {
        super(messageId, timestamp, sourceSystem, eventType);
        this.payload = payload;
    }

    /**
     * Convenience constructor with payload only (auto-sets eventType).
     *
     * @param messageId    Unique message identifier
     * @param timestamp    Message creation timestamp
     * @param sourceSystem Originating system identifier
     * @param payload      User registration data
     */
    public UserRegistrationMessage(String messageId, LocalDateTime timestamp,
                                    String sourceSystem, UserRegistrationPayload payload) {
        super(messageId, timestamp, sourceSystem, "USER_CREATED");
        this.payload = payload;
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

    public UserRegistrationPayload getPayload() {
        return payload;
    }

    public void setPayload(UserRegistrationPayload payload) {
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
        UserRegistrationMessage that = (UserRegistrationMessage) o;
        return Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), payload);
    }

    @Override
    public String toString() {
        return "UserRegistrationMessage{" +
                "messageId='" + getMessageId() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", sourceSystem='" + getSourceSystem() + '\'' +
                ", eventType='" + getEventType() + '\'' +
                ", payload=" + payload +
                '}';
    }
}
