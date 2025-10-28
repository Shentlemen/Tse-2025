package uy.gub.hcen.service.inus.exception;

/**
 * User Registration Exception
 * <p>
 * Thrown when user registration in INUS fails due to validation errors,
 * duplicate users, or system errors during the registration process.
 * <p>
 * Common scenarios:
 * - Invalid CI format
 * - Duplicate user registration attempt
 * - PDI integration failure
 * - Database persistence failure
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 */
public class UserRegistrationException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new UserRegistrationException with the specified detail message.
     *
     * @param message Detail message explaining the registration failure
     */
    public UserRegistrationException(String message) {
        super(message);
    }

    /**
     * Constructs a new UserRegistrationException with the specified detail message and cause.
     *
     * @param message Detail message explaining the registration failure
     * @param cause   Underlying cause of the registration failure
     */
    public UserRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
