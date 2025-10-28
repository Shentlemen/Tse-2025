package uy.gub.hcen.integration.peripheral;

/**
 * Exception thrown when peripheral node communication fails.
 * <p>
 * This exception is used in the following scenarios:
 * - Peripheral node API endpoint unreachable (AC016, AC015)
 * - Network timeout or connection failure
 * - Peripheral node returns error response (4xx, 5xx)
 * - Invalid peripheral node URL (not HTTPS)
 * - Document integrity verification failure
 * - Circuit breaker is open (too many consecutive failures)
 * - Authentication failure (invalid API key)
 * <p>
 * This exception wraps lower-level I/O exceptions and provides
 * context-specific error messages for peripheral node operations.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
public class PeripheralNodeException extends Exception {

    /**
     * Constructs a new PeripheralNodeException with the specified detail message.
     *
     * @param message the detail message
     */
    public PeripheralNodeException(String message) {
        super(message);
    }

    /**
     * Constructs a new PeripheralNodeException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause (IOException, timeout, etc.)
     */
    public PeripheralNodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
