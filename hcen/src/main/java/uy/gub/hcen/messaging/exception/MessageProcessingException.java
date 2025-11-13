package uy.gub.hcen.messaging.exception;

/**
 * Base exception for message processing errors in JMS message handlers.
 * <p>
 * This exception is thrown when message processing fails due to:
 * - Business logic errors
 * - Database persistence failures
 * - External service integration failures
 * - Message validation errors
 * <p>
 * Exception Handling Strategy:
 * - Transient errors (network, database deadlock): Message returned to queue for retry
 * - Permanent errors (validation, business rule): Message moved to DLQ (Dead Letter Queue)
 * - All exceptions logged with full context for debugging
 * <p>
 * Usage in MDBs:
 * <pre>
 * try {
 *     processMessage(message);
 * } catch (MessageProcessingException e) {
 *     LOGGER.log(Level.SEVERE, "Message processing failed", e);
 *     // Container will handle redelivery based on exception type
 *     throw e;
 * }
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class MessageProcessingException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Message ID that failed processing (for correlation).
     */
    private final String messageId;

    /**
     * Whether this error is transient (should retry) or permanent (move to DLQ).
     */
    private final boolean isTransient;

    /**
     * Construct exception with message only.
     *
     * @param message Error message
     */
    public MessageProcessingException(String message) {
        super(message);
        this.messageId = null;
        this.isTransient = false;
    }

    /**
     * Construct exception with message and cause.
     *
     * @param message Error message
     * @param cause   Root cause exception
     */
    public MessageProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.messageId = null;
        this.isTransient = false;
    }

    /**
     * Construct exception with full context.
     *
     * @param message     Error message
     * @param cause       Root cause exception
     * @param messageId   ID of message that failed
     * @param isTransient Whether error is transient (true = retry, false = DLQ)
     */
    public MessageProcessingException(String message, Throwable cause, String messageId, boolean isTransient) {
        super(message, cause);
        this.messageId = messageId;
        this.isTransient = isTransient;
    }

    /**
     * Get the ID of the message that failed processing.
     *
     * @return Message ID, or null if not set
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Check if this error is transient (should retry).
     * <p>
     * Transient errors: database deadlock, network timeout, service temporarily unavailable
     * Permanent errors: validation failure, business rule violation, malformed message
     *
     * @return true if transient (retry), false if permanent (DLQ)
     */
    public boolean isTransient() {
        return isTransient;
    }

    @Override
    public String toString() {
        return "MessageProcessingException{" +
                "message='" + getMessage() + '\'' +
                ", messageId='" + messageId + '\'' +
                ", isTransient=" + isTransient +
                ", cause=" + getCause() +
                '}';
    }
}
