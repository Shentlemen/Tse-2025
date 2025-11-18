package uy.gub.hcen.messaging.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uy.gub.hcen.messaging.dto.DocumentRegistrationPayload;
import uy.gub.hcen.messaging.dto.FhirDocumentRegistrationPayload;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom Jackson deserializer for document registration payload.
 * <p>
 * This deserializer automatically detects the payload format:
 * - FHIR DocumentReference: Has "resourceType": "DocumentReference"
 * - Simple Payload: Direct DocumentRegistrationPayload fields
 * <p>
 * Detection Strategy:
 * 1. Check if JSON has "resourceType" field
 * 2. If yes → Deserialize as FhirDocumentRegistrationPayload
 * 3. If no → Deserialize as DocumentRegistrationPayload
 * <p>
 * This allows HCEN Central to accept messages from both:
 * - Legacy peripheral nodes (simple format)
 * - FHIR-compliant peripheral nodes (FHIR DocumentReference)
 * <p>
 * Example FHIR Payload:
 * <pre>
 * {
 *   "payload": {
 *     "resourceType": "DocumentReference",
 *     "id": "doc-5",
 *     "subject": {"reference": "Patient/33333333"},
 *     "custodian": {"reference": "Organization/clinic-1"},
 *     "content": [{
 *       "attachment": {
 *         "url": "http://localhost:8080/api/documents/5",
 *         "hash": "abc123...",
 *         "creation": "2025-11-18T01:09:08-03:00"
 *       }
 *     }]
 *   }
 * }
 * </pre>
 * <p>
 * Example Simple Payload:
 * <pre>
 * {
 *   "payload": {
 *     "patientCI": "33333333",
 *     "documentLocator": "http://localhost:8080/api/documents/5",
 *     "documentHash": "abc123...",
 *     "documentType": "CLINICAL_NOTE"
 *   }
 * }
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-18
 */
public class DocumentPayloadDeserializer extends JsonDeserializer<DocumentRegistrationPayload> {

    private static final Logger LOGGER = Logger.getLogger(DocumentPayloadDeserializer.class.getName());

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Override
    public DocumentRegistrationPayload deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {

        JsonNode node = parser.getCodec().readTree(parser);

        // Check if this is a FHIR DocumentReference
        boolean isFhir = node.has("resourceType");

        if (isFhir) {
            String resourceType = node.get("resourceType").asText();

            LOGGER.log(Level.FINE, "Detected FHIR payload with resourceType: {0}", resourceType);

            if (!"DocumentReference".equals(resourceType)) {
                LOGGER.log(Level.WARNING,
                        "Expected resourceType=DocumentReference, but got: {0}. Attempting to deserialize anyway.",
                        resourceType);
            }

            // Deserialize as FHIR payload
            FhirDocumentRegistrationPayload fhirPayload = OBJECT_MAPPER.treeToValue(
                    node,
                    FhirDocumentRegistrationPayload.class
            );

            LOGGER.log(Level.INFO,
                    "Successfully deserialized FHIR payload - Patient CI: {0}, Locator: {1}, Type: {2}",
                    new Object[]{
                            fhirPayload.getPatientCI(),
                            fhirPayload.getDocumentLocator(),
                            fhirPayload.getDocumentType()
                    });

            return fhirPayload;

        } else {
            // Deserialize as simple payload
            LOGGER.log(Level.FINE, "Detected simple (non-FHIR) payload");

            DocumentRegistrationPayload simplePayload = OBJECT_MAPPER.treeToValue(
                    node,
                    DocumentRegistrationPayload.class
            );

            LOGGER.log(Level.INFO,
                    "Successfully deserialized simple payload - Patient CI: {0}, Locator: {1}, Type: {2}",
                    new Object[]{
                            simplePayload.getPatientCI(),
                            simplePayload.getDocumentLocator(),
                            simplePayload.getDocumentType()
                    });

            return simplePayload;
        }
    }
}
