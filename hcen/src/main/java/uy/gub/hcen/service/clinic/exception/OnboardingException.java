package uy.gub.hcen.service.clinic.exception;

/**
 * Exception thrown when clinic onboarding fails.
 *
 * This exception is used in the following scenarios:
 * - Peripheral node communication failure (AC016)
 * - Peripheral node API endpoint unreachable
 * - Peripheral node returns error response
 * - Clinic already onboarded
 * - Clinic not in valid state for onboarding
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
public class OnboardingException extends Exception {

    /**
     * Constructs a new OnboardingException with the specified detail message.
     *
     * @param message the detail message
     */
    public OnboardingException(String message) {
        super(message);
    }

    /**
     * Constructs a new OnboardingException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public OnboardingException(String message, Throwable cause) {
        super(message, cause);
    }
}
