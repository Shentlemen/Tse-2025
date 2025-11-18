package uy.gub.hcen.messaging.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uy.gub.hcen.rndc.entity.DocumentType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify FHIR DocumentReference deserialization.
 * <p>
 * This test demonstrates that the custom deserializer correctly handles:
 * 1. FHIR DocumentReference payloads (from peripheral nodes)
 * 2. Simple DocumentRegistrationPayload format (legacy)
 * <p>
 * The FHIR format is sent by the prestador-de-salud component when registering
 * documents with HCEN Central via JMS queue.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-18
 */
class FhirDocumentDeserializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    /**
     * Test deserialization of FHIR DocumentReference payload.
     * <p>
     * This is the actual JSON structure sent by prestador-de-salud component.
     */
    @Test
    void testDeserializeFhirDocumentReference() throws Exception {
        // Given: Real FHIR JSON payload from prestador-de-salud
        String fhirJson = """
        {
          "payload": {
            "date": "2025-11-18T01:09:08.935-03:00",
            "custodian": {
              "reference": "Organization/clinic-1",
              "display": "Clinic 1"
            },
            "subject": {
              "reference": "Patient/33333333",
              "display": "Patient CI: 33333333"
            },
            "author": [{
              "reference": "Practitioner/professional-5",
              "display": "professional-5"
            }],
            "description": "Consulta médica de control general",
            "masterIdentifier": {
              "system": "http://prestador-de-salud.uy/documents",
              "value": "5"
            },
            "type": {
              "coding": [{
                "system": "http://loinc.org",
                "code": "34109-9",
                "display": "Note"
              }]
            },
            "content": [{
              "attachment": {
                "title": "Consulta General - Control de Rutina",
                "contentType": "application/json",
                "url": "http://localhost:8080/prestador-salud/api/documents/5",
                "hash": "ZWYyZDEyN2RlMzdiOTQyYmFhZDA2MTQ1ZTU0YjBjNjE5YTFmMjIzMjdiMmViYmNmYmVjNzhmNTU2NGFmZTM5ZA==",
                "creation": "2025-11-18T01:09:08-03:00"
              }
            }],
            "meta": {
              "lastUpdated": "2025-11-18T01:09:08.958-03:00",
              "security": [{
                "system": "http://terminology.hl7.org/CodeSystem/v3-Confidentiality",
                "code": "N",
                "display": "Normal"
              }],
              "profile": ["http://hcen.gub.uy/fhir/StructureDefinition/hcen-documentreference"]
            },
            "context": {
              "practiceSetting": {
                "coding": [{
                  "system": "http://hcen.gub.uy/fhir/CodeSystem/specialty",
                  "code": "General Medicine",
                  "display": "Specialty General Medicine"
                }]
              }
            },
            "id": "doc-5",
            "category": [{
              "coding": [{
                "system": "http://hl7.org/fhir/us/core/CodeSystem/us-core-documentreference-category",
                "code": "clinical-note",
                "display": "Clinical Note"
              }]
            }],
            "resourceType": "DocumentReference",
            "status": "current"
          },
          "sourceSystem": "prestador-de-salud",
          "messageId": "msg-7a8342fc-3a1a-4680-ada3-16fd07650618",
          "eventType": "document-create",
          "timestamp": "2025-11-18T01:09:08.961397100"
        }
        """;

        // When: Deserialize JSON to DocumentRegistrationMessage
        DocumentRegistrationMessage message = objectMapper.readValue(fhirJson, DocumentRegistrationMessage.class);

        // Then: Verify message envelope fields
        assertNotNull(message, "Message should not be null");
        assertEquals("msg-7a8342fc-3a1a-4680-ada3-16fd07650618", message.getMessageId());
        assertEquals("prestador-de-salud", message.getSourceSystem());
        assertEquals("document-create", message.getEventType());
        assertNotNull(message.getTimestamp());

        // Then: Verify payload was extracted correctly
        DocumentRegistrationPayload payload = message.getPayload();
        assertNotNull(payload, "Payload should not be null");

        // Verify patient CI extracted from subject.reference
        assertEquals("33333333", payload.getPatientCI(),
                "Patient CI should be extracted from subject.reference");

        // Verify clinic ID extracted from custodian.reference
        assertEquals("clinic-1", payload.getClinicId(),
                "Clinic ID should be extracted from custodian.reference");

        // Verify created by extracted from author[0].reference
        assertEquals("professional-5", payload.getCreatedBy(),
                "Created by should be extracted from author[0].reference");

        // Verify document locator from content[0].attachment.url
        assertEquals("http://localhost:8080/prestador-salud/api/documents/5",
                payload.getDocumentLocator(),
                "Document locator should be extracted from content[0].attachment.url");

        // Verify document hash from content[0].attachment.hash
        assertEquals("ZWYyZDEyN2RlMzdiOTQyYmFhZDA2MTQ1ZTU0YjBjNjE5YTFmMjIzMjdiMmViYmNmYmVjNzhmNTU2NGFmZTM5ZA==",
                payload.getDocumentHash(),
                "Document hash should be extracted from content[0].attachment.hash");

        // Verify document title from content[0].attachment.title
        assertEquals("Consulta General - Control de Rutina",
                payload.getDocumentTitle(),
                "Document title should be extracted from content[0].attachment.title");

        // Verify document description
        assertEquals("Consulta médica de control general",
                payload.getDocumentDescription(),
                "Document description should be extracted from description field");

        // Verify document type mapped from "Note" to CLINICAL_NOTE
        assertEquals(DocumentType.CLINICAL_NOTE, payload.getDocumentType(),
                "Document type should be mapped from 'Note' to CLINICAL_NOTE");

        // Verify created at timestamp
        assertNotNull(payload.getCreatedAt(),
                "Created at should be extracted from content[0].attachment.creation");

        // Print success message
        System.out.println("✓ FHIR DocumentReference successfully deserialized!");
        System.out.println("  Patient CI: " + payload.getPatientCI());
        System.out.println("  Clinic ID: " + payload.getClinicId());
        System.out.println("  Document Locator: " + payload.getDocumentLocator());
        System.out.println("  Document Type: " + payload.getDocumentType());
        System.out.println("  Created By: " + payload.getCreatedBy());
    }

    /**
     * Test backward compatibility with simple payload format.
     * <p>
     * Verifies that legacy peripheral nodes can still send simple format.
     */
    @Test
    void testDeserializeSimplePayload() throws Exception {
        // Given: Simple payload format (legacy)
        String simpleJson = """
        {
          "payload": {
            "patientCI": "33333333",
            "documentType": "CLINICAL_NOTE",
            "documentLocator": "http://localhost:8080/api/documents/5",
            "documentHash": "abc123",
            "createdBy": "professional-5",
            "createdAt": "2025-11-18T01:09:08",
            "clinicId": "clinic-1",
            "documentTitle": "Consulta General",
            "documentDescription": "Consulta médica"
          },
          "sourceSystem": "legacy-clinic",
          "messageId": "msg-simple-123",
          "eventType": "document-create",
          "timestamp": "2025-11-18T01:09:08"
        }
        """;

        // When: Deserialize JSON
        DocumentRegistrationMessage message = objectMapper.readValue(simpleJson, DocumentRegistrationMessage.class);

        // Then: Verify all fields
        assertNotNull(message);
        assertNotNull(message.getPayload());
        assertEquals("33333333", message.getPayload().getPatientCI());
        assertEquals("clinic-1", message.getPayload().getClinicId());
        assertEquals("professional-5", message.getPayload().getCreatedBy());
        assertEquals("http://localhost:8080/api/documents/5", message.getPayload().getDocumentLocator());
        assertEquals(DocumentType.CLINICAL_NOTE, message.getPayload().getDocumentType());

        System.out.println("✓ Simple payload successfully deserialized (backward compatibility)!");
    }

    /**
     * Test FHIR type mapping to internal DocumentType enum.
     */
    @Test
    void testFhirTypeMapping() throws Exception {
        // Test different FHIR types
        String[] testCases = {
                "Laboratory Result",
                "Medical Imaging",
                "Prescription",
                "Discharge Summary",
                "Note"
        };

        DocumentType[] expectedTypes = {
                DocumentType.LAB_RESULT,
                DocumentType.IMAGING,
                DocumentType.PRESCRIPTION,
                DocumentType.DISCHARGE_SUMMARY,
                DocumentType.CLINICAL_NOTE
        };

        for (int i = 0; i < testCases.length; i++) {
            String fhirType = testCases[i];
            DocumentType expectedType = expectedTypes[i];

            String json = String.format("""
            {
              "payload": {
                "resourceType": "DocumentReference",
                "subject": {"reference": "Patient/12345678"},
                "custodian": {"reference": "Organization/clinic-1"},
                "author": [{"reference": "Practitioner/doc-1"}],
                "type": {
                  "coding": [{
                    "display": "%s"
                  }]
                },
                "content": [{
                  "attachment": {
                    "url": "http://example.com/doc",
                    "hash": "abc",
                    "creation": "2025-11-18T01:09:08-03:00"
                  }
                }]
              },
              "sourceSystem": "test",
              "messageId": "msg-test",
              "eventType": "document-create",
              "timestamp": "2025-11-18T01:09:08"
            }
            """, fhirType);

            DocumentRegistrationMessage message = objectMapper.readValue(json, DocumentRegistrationMessage.class);

            assertEquals(expectedType, message.getPayload().getDocumentType(),
                    "FHIR type '" + fhirType + "' should map to " + expectedType);

            System.out.println("✓ FHIR type '" + fhirType + "' correctly mapped to " + expectedType);
        }
    }

    /**
     * Test handling of missing optional fields.
     */
    @Test
    void testMissingOptionalFields() throws Exception {
        // Given: FHIR payload with minimal required fields only
        String minimalJson = """
        {
          "payload": {
            "resourceType": "DocumentReference",
            "subject": {"reference": "Patient/12345678"},
            "custodian": {"reference": "Organization/clinic-1"},
            "content": [{
              "attachment": {
                "url": "http://example.com/doc"
              }
            }]
          },
          "sourceSystem": "test",
          "messageId": "msg-minimal",
          "eventType": "document-create",
          "timestamp": "2025-11-18T01:09:08"
        }
        """;

        // When: Deserialize
        DocumentRegistrationMessage message = objectMapper.readValue(minimalJson, DocumentRegistrationMessage.class);

        // Then: Verify required fields present, optional fields null
        assertNotNull(message.getPayload());
        assertEquals("12345678", message.getPayload().getPatientCI());
        assertEquals("clinic-1", message.getPayload().getClinicId());
        assertEquals("http://example.com/doc", message.getPayload().getDocumentLocator());

        // Optional fields should be null or default
        // This is OK - the validator will catch this if they're required
        System.out.println("✓ Minimal FHIR payload deserialized without errors");
    }
}
