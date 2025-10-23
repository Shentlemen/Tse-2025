package uy.gub.hcen.integration.pdi;

/**
 * PDI Integration Exception
 * <p>
 * Custom exception for errors during communication with PDI (Plataforma de Datos e Integración)
 * Uruguayan government identity validation service.
 * <p>
 * Common scenarios:
 * - SOAP service unavailable (network timeout, server down)
 * - Invalid SOAP response format
 * - Authentication failure (WS-Security credentials invalid)
 * - User not found in PDI database
 * - Invalid CI format
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
public class PDIException extends Exception {

    /**
     * Constructs a new PDI exception with a message
     *
     * @param message Error message describing the exception
     */
    public PDIException(String message) {
        super(message);
    }

    /**
     * Constructs a new PDI exception with a message and cause
     *
     * @param message Error message describing the exception
     * @param cause   The underlying cause of the exception
     */
    public PDIException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new PDI exception with a cause
     *
     * @param cause The underlying cause of the exception
     */
    public PDIException(Throwable cause) {
        super(cause);
    }
}
