package uy.gub.hcen.service.clinic.exception;

/**
 * Exception thrown when a requested clinic is not found in the system.
 *
 * This exception is used in the following scenarios:
 * - Clinic lookup by ID fails
 * - Clinic update attempted on non-existent clinic
 * - Clinic deletion attempted on non-existent clinic
 * - Onboarding attempted on non-existent clinic
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
public class ClinicNotFoundException extends Exception {

    /**
     * Constructs a new ClinicNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public ClinicNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ClinicNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public ClinicNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
