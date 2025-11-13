package uy.gub.hcen.integration.clinic;

/**
 * Clinic Service Exception
 * <p>
 * Thrown when communication with the Clinic Service peripheral component fails.
 * This includes:
 * - Network connectivity issues
 * - Invalid responses from clinic service
 * - Validation errors from clinic service
 * - Registration conflicts (duplicate clinics)
 * <p>
 * This exception is checked to force callers to handle integration failures explicitly.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class ClinicServiceException extends Exception {

    /**
     * Constructs a new ClinicServiceException with the specified detail message.
     *
     * @param message The detail message
     */
    public ClinicServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new ClinicServiceException with the specified detail message and cause.
     *
     * @param message The detail message
     * @param cause   The cause of the exception
     */
    public ClinicServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
