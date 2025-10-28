package uy.gub.hcen.service.rndc.exception;

/**
 * Exception thrown when document registration fails in the RNDC.
 * <p>
 * This exception is thrown when:
 * <ul>
 *   <li>Required parameters are missing or invalid</li>
 *   <li>Document locator format is invalid</li>
 *   <li>Document hash format is invalid</li>
 *   <li>Patient CI does not exist in INUS</li>
 *   <li>Database persistence fails</li>
 * </ul>
 * <p>
 * This is a checked exception to force callers to handle registration failures explicitly.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 */
public class DocumentRegistrationException extends Exception {

    /**
     * Constructs a new DocumentRegistrationException with the specified detail message.
     *
     * @param message The detail message explaining why registration failed
     */
    public DocumentRegistrationException(String message) {
        super(message);
    }

    /**
     * Constructs a new DocumentRegistrationException with the specified detail message and cause.
     *
     * @param message The detail message explaining why registration failed
     * @param cause   The underlying cause of the failure
     */
    public DocumentRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
