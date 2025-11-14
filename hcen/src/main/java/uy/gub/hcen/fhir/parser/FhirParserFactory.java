package uy.gub.hcen.fhir.parser;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FHIR Parser Factory
 * <p>
 * Singleton factory for creating and managing HAPI FHIR parsers.
 * This factory provides thread-safe access to FHIR parsers for JSON and XML formats.
 * <p>
 * The FhirContext is initialized once at application startup and reused throughout
 * the application lifecycle for optimal performance. FhirContext initialization is
 * expensive, so it's important to reuse the same instance.
 * <p>
 * Usage Example:
 * <pre>
 * &#64;Inject
 * private FhirParserFactory fhirParserFactory;
 *
 * // Get JSON parser
 * IParser parser = fhirParserFactory.getJsonParser();
 *
 * // Parse FHIR Patient resource
 * Patient patient = parser.parseResource(Patient.class, jsonString);
 * </pre>
 * <p>
 * Configuration:
 * - FHIR Version: R4 (4.0.1)
 * - Pretty Print: Disabled for production (compact JSON/XML)
 * - Summary Mode: Disabled (include all fields)
 * - Narrative Mode: Disabled (no narrative generation)
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@ApplicationScoped
public class FhirParserFactory {

    private static final Logger LOGGER = Logger.getLogger(FhirParserFactory.class.getName());

    /**
     * FHIR Context for R4 (singleton instance)
     * <p>
     * This instance is thread-safe and can be reused across the application.
     * FhirContext initialization is expensive (takes several seconds), so we
     * initialize it once and cache it for the lifetime of the application.
     */
    private final FhirContext fhirContext;

    /**
     * Constructor - initializes FHIR context for R4.
     * <p>
     * This constructor is called once by the CDI container when the application starts.
     */
    public FhirParserFactory() {
        LOGGER.log(Level.INFO, "Initializing FHIR Context for R4...");
        long startTime = System.currentTimeMillis();

        // Create FHIR context for R4 (FHIR version 4.0.1)
        this.fhirContext = FhirContext.forR4();

        long endTime = System.currentTimeMillis();
        LOGGER.log(Level.INFO, "FHIR Context initialized successfully in {0}ms",
                (endTime - startTime));
    }

    /**
     * Get a JSON parser instance.
     * <p>
     * Returns a new parser instance configured for JSON parsing.
     * The parser is configured with production-ready settings:
     * - Pretty print: disabled (compact JSON)
     * - Summary mode: disabled (include all fields)
     * <p>
     * Thread Safety: This method returns a new parser instance each time,
     * so it's safe to use in concurrent requests.
     *
     * @return JSON parser instance
     */
    public IParser getJsonParser() {
        return fhirContext.newJsonParser()
                .setPrettyPrint(false)  // Compact JSON for production
                .setSummaryMode(false); // Include all fields
    }

    /**
     * Get an XML parser instance.
     * <p>
     * Returns a new parser instance configured for XML parsing.
     * The parser is configured with production-ready settings.
     * <p>
     * Thread Safety: This method returns a new parser instance each time,
     * so it's safe to use in concurrent requests.
     *
     * @return XML parser instance
     */
    public IParser getXmlParser() {
        return fhirContext.newXmlParser()
                .setPrettyPrint(false)  // Compact XML for production
                .setSummaryMode(false); // Include all fields
    }

    /**
     * Get the underlying FHIR context.
     * <p>
     * This method is useful for advanced use cases where direct access
     * to the FHIR context is needed (e.g., resource validation, custom parsing).
     *
     * @return FHIR context instance
     */
    public FhirContext getFhirContext() {
        return fhirContext;
    }

    /**
     * Parse a FHIR resource from JSON string.
     * <p>
     * Convenience method that creates a JSON parser and parses the resource.
     *
     * @param <T>           FHIR resource type (must extend IBaseResource)
     * @param resourceClass Class of the resource to parse
     * @param jsonString    JSON string containing FHIR resource
     * @return Parsed FHIR resource
     * @throws ca.uhn.fhir.parser.DataFormatException if JSON is malformed
     */
    public <T extends org.hl7.fhir.instance.model.api.IBaseResource> T parseJsonResource(
            Class<T> resourceClass, String jsonString) {
        IParser parser = getJsonParser();
        return parser.parseResource(resourceClass, jsonString);
    }

    /**
     * Parse a FHIR resource from XML string.
     * <p>
     * Convenience method that creates an XML parser and parses the resource.
     *
     * @param <T>           FHIR resource type (must extend IBaseResource)
     * @param resourceClass Class of the resource to parse
     * @param xmlString     XML string containing FHIR resource
     * @return Parsed FHIR resource
     * @throws ca.uhn.fhir.parser.DataFormatException if XML is malformed
     */
    public <T extends org.hl7.fhir.instance.model.api.IBaseResource> T parseXmlResource(
            Class<T> resourceClass, String xmlString) {
        IParser parser = getXmlParser();
        return parser.parseResource(resourceClass, xmlString);
    }

    /**
     * Encode a FHIR resource to JSON string.
     * <p>
     * Convenience method that creates a JSON parser and encodes the resource.
     *
     * @param resource FHIR resource to encode (must extend IBaseResource)
     * @return JSON string representation
     */
    public String encodeToJson(org.hl7.fhir.instance.model.api.IBaseResource resource) {
        IParser parser = getJsonParser();
        return parser.encodeResourceToString(resource);
    }

    /**
     * Encode a FHIR resource to XML string.
     * <p>
     * Convenience method that creates an XML parser and encodes the resource.
     *
     * @param resource FHIR resource to encode (must extend IBaseResource)
     * @return XML string representation
     */
    public String encodeToXml(org.hl7.fhir.instance.model.api.IBaseResource resource) {
        IParser parser = getXmlParser();
        return parser.encodeResourceToString(resource);
    }
}
