package uy.gub.hcen.messaging.exception;

/**
 * Exception thrown when a message fails validation.
 * <p>
 * This is a permanent error - invalid messages should be moved to DLQ
 * and not retried, as they will never pass validation.
 * <p>
 * Validation Failures:
 * - Missing required fields (messageId, timestamp, sourceSystem, payload)
 * - Invalid data formats (malformed CI, invalid URL, bad hash format)
 * - Business rule violations (age < 18, future dates, etc.)
 * - Message schema mismatch
 * <p>
 * Usage:
 * <pre>
 * if (message.getPayload() == null) {
 *     throw new InvalidMessageException(
 *         "Payload is required",
 *         message.getMessageId()
 *     );
 * }
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class InvalidMessageException extends MessageProcessingException {

    private static final long serialVersionUID = 1L;

    /**
     * Field that failed validation (for specific error reporting).
     */
    private final String invalidField;

    /**
     * Construct exception with message only.
     *
     * @param message Error message describing validation failure
     */
    public InvalidMessageException(String message) {
        super(message, null, null, false); // Not transient
        this.invalidField = null;
    }

    /**
     * Construct exception with message and messageId.
     *
     * @param message   Error message describing validation failure
     * @param messageId ID of invalid message
     */
    public InvalidMessageException(String message, String messageId) {
        super(message, null, messageId, false); // Not transient
        this.invalidField = null;
    }

    /**
     * Construct exception with full context.
     *
     * @param message      Error message describing validation failure
     * @param messageId    ID of invalid message
     * @param invalidField Name of field that failed validation
     */
    public InvalidMessageException(String message, String messageId, String invalidField) {
        super(message, null, messageId, false); // Not transient
        this.invalidField = invalidField;
    }

    /**
     * Get the name of the field that failed validation.
     *
     * @return Field name, or null if not specific to a field
     */
    public String getInvalidField() {
        return invalidField;
    }

    @Override
    public String toString() {
        return "InvalidMessageException{" +
                "message='" + getMessage() + '\'' +
                ", messageId='" + getMessageId() + '\'' +
                ", invalidField='" + invalidField + '\'' +
                '}';
    }
}
