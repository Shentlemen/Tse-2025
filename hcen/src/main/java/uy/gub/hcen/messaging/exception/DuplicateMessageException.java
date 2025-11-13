package uy.gub.hcen.messaging.exception;

/**
 * Exception thrown when a duplicate message is detected.
 * <p>
 * This exception is used for idempotency checks. When a message with the same
 * messageId has already been processed, this exception is thrown to prevent
 * duplicate processing.
 * <p>
 * Note: This is NOT actually an error condition - it's a normal part of
 * idempotent message processing. The exception is caught and logged as INFO,
 * not ERROR.
 * <p>
 * Usage:
 * <pre>
 * if (alreadyProcessed(messageId)) {
 *     throw new DuplicateMessageException(
 *         "Message already processed",
 *         messageId
 *     );
 * }
 * </pre>
 * <p>
 * Handler:
 * <pre>
 * catch (DuplicateMessageException e) {
 *     LOGGER.log(Level.INFO, "Duplicate message ignored: {0}", e.getMessageId());
 *     // Don't rethrow - acknowledge message as processed
 * }
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class DuplicateMessageException extends MessageProcessingException {

    private static final long serialVersionUID = 1L;

    /**
     * Timestamp when message was originally processed.
     */
    private final String originalProcessedTime;

    /**
     * Construct exception with message and messageId.
     *
     * @param message   Error message
     * @param messageId ID of duplicate message
     */
    public DuplicateMessageException(String message, String messageId) {
        super(message, null, messageId, false); // Not transient
        this.originalProcessedTime = null;
    }

    /**
     * Construct exception with full context.
     *
     * @param message               Error message
     * @param messageId             ID of duplicate message
     * @param originalProcessedTime When message was originally processed
     */
    public DuplicateMessageException(String message, String messageId, String originalProcessedTime) {
        super(message, null, messageId, false); // Not transient
        this.originalProcessedTime = originalProcessedTime;
    }

    /**
     * Get the timestamp when message was originally processed.
     *
     * @return Original processing time, or null if not tracked
     */
    public String getOriginalProcessedTime() {
        return originalProcessedTime;
    }

    @Override
    public String toString() {
        return "DuplicateMessageException{" +
                "message='" + getMessage() + '\'' +
                ", messageId='" + getMessageId() + '\'' +
                ", originalProcessedTime='" + originalProcessedTime + '\'' +
                '}';
    }
}
