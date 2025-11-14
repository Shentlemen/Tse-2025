package uy.gub.hcen.fhir.exception;

/**
 * FHIR Conversion Exception
 * <p>
 * Custom exception thrown when FHIR resource conversion fails.
 * This exception is used to indicate errors during FHIR resource parsing,
 * validation, or conversion to internal DTOs.
 * <p>
 * Common scenarios:
 * - Invalid FHIR structure (malformed JSON/XML)
 * - Missing required fields in FHIR resource
 * - Unsupported FHIR resource version
 * - Invalid identifier system (not Uruguay OID)
 * - Invalid LOINC codes or coding systems
 * - Type conversion errors (e.g., invalid date formats)
 * <p>
 * Usage Example:
 * <pre>
 * if (!patient.hasIdentifier()) {
 *     throw new FhirConversionException(
 *         "FHIR Patient resource must have at least one identifier"
 *     );
 * }
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class FhirConversionException extends RuntimeException {

    /**
     * Constructs a new FHIR conversion exception with the specified detail message.
     *
     * @param message the detail message explaining the conversion error
     */
    public FhirConversionException(String message) {
        super(message);
    }

    /**
     * Constructs a new FHIR conversion exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the conversion error
     * @param cause   the cause of the conversion error (e.g., parsing exception)
     */
    public FhirConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new FHIR conversion exception with the specified cause.
     *
     * @param cause the cause of the conversion error
     */
    public FhirConversionException(Throwable cause) {
        super(cause);
    }
}
