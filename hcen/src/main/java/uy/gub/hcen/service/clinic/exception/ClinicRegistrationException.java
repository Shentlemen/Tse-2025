package uy.gub.hcen.service.clinic.exception;

/**
 * Exception thrown when clinic registration fails.
 *
 * This exception is used in the following scenarios:
 * - Invalid clinic data (missing required fields, invalid format)
 * - Duplicate clinic ID
 * - Database persistence failure
 * - API key generation failure
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
public class ClinicRegistrationException extends Exception {

    /**
     * Constructs a new ClinicRegistrationException with the specified detail message.
     *
     * @param message the detail message
     */
    public ClinicRegistrationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ClinicRegistrationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public ClinicRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
