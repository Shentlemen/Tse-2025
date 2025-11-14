package uy.gub.hcen.fhir.converter;

import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uy.gub.hcen.fhir.exception.FhirConversionException;
import uy.gub.hcen.rndc.dto.DocumentRegistrationRequest;
import uy.gub.hcen.rndc.entity.DocumentType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FhirDocumentReferenceConverter
 */
class FhirDocumentReferenceConverterTest {

    private FhirDocumentReferenceConverter converter;

    @BeforeEach
    void setUp() {
        converter = new FhirDocumentReferenceConverter();
    }

    @Test
    void testConvertValidDocumentReference() {
        // Given: A valid FHIR DocumentReference resource
        DocumentReference documentReference = createValidDocumentReference();

        // When: Converting to DocumentRegistrationRequest
        DocumentRegistrationRequest request =
                converter.toDocumentRegistrationRequest(documentReference);

        // Then: All fields should be correctly mapped
        assertNotNull(request);
        assertEquals("12345678", request.getPatientCi());
        assertEquals(DocumentType.LAB_RESULT, request.getDocumentType());
        assertEquals("https://clinic-001.hcen.uy/api/documents/abc123", request.getDocumentLocator());
        assertTrue(request.getDocumentHash().startsWith("sha256:"));
        assertEquals("Dr. María García", request.getCreatedBy());
        assertEquals("clinic-001", request.getClinicId());
        assertEquals("Lab Results", request.getDocumentTitle());
        assertEquals("Complete blood count analysis", request.getDocumentDescription());
    }

    @Test
    void testConvertDocumentReferenceWithClinicalNote() {
        // Given: A FHIR DocumentReference with LOINC code for clinical note
        DocumentReference documentReference = createValidDocumentReference();
        documentReference.getType().getCoding().clear();
        documentReference.getType().addCoding()
                .setSystem("http://loinc.org")
                .setCode("11506-3")
                .setDisplay("Progress note");

        // When: Converting to DocumentRegistrationRequest
        DocumentRegistrationRequest request =
                converter.toDocumentRegistrationRequest(documentReference);

        // Then: Document type should be CLINICAL_NOTE
        assertNotNull(request);
        assertEquals(DocumentType.CLINICAL_NOTE, request.getDocumentType());
    }

    @Test
    void testConvertDocumentReferenceWithPrescription() {
        // Given: A FHIR DocumentReference with LOINC code for prescription
        DocumentReference documentReference = createValidDocumentReference();
        documentReference.getType().getCoding().clear();
        documentReference.getType().addCoding()
                .setSystem("http://loinc.org")
                .setCode("57833-6")
                .setDisplay("Prescription for medication");

        // When: Converting to DocumentRegistrationRequest
        DocumentRegistrationRequest request =
                converter.toDocumentRegistrationRequest(documentReference);

        // Then: Document type should be PRESCRIPTION
        assertNotNull(request);
        assertEquals(DocumentType.PRESCRIPTION, request.getDocumentType());
    }

    @Test
    void testConvertDocumentReferenceWithUnknownLoincCode() {
        // Given: A FHIR DocumentReference with unknown LOINC code
        DocumentReference documentReference = createValidDocumentReference();
        documentReference.getType().getCoding().clear();
        documentReference.getType().addCoding()
                .setSystem("http://loinc.org")
                .setCode("99999-9")
                .setDisplay("Unknown document type");

        // When: Converting to DocumentRegistrationRequest
        DocumentRegistrationRequest request =
                converter.toDocumentRegistrationRequest(documentReference);

        // Then: Document type should default to OTHER
        assertNotNull(request);
        assertEquals(DocumentType.OTHER, request.getDocumentType());
    }

    @Test
    void testConvertDocumentReferenceWithoutTitle() {
        // Given: A FHIR DocumentReference without title
        DocumentReference documentReference = createValidDocumentReference();
        documentReference.getContent().get(0).getAttachment().setTitle(null);

        // When: Converting to DocumentRegistrationRequest
        DocumentRegistrationRequest request =
                converter.toDocumentRegistrationRequest(documentReference);

        // Then: Title should be null
        assertNotNull(request);
        assertNull(request.getDocumentTitle());
    }

    @Test
    void testConvertDocumentReferenceWithoutDescription() {
        // Given: A FHIR DocumentReference without description
        DocumentReference documentReference = createValidDocumentReference();
        documentReference.getContext().getRelated().clear();

        // When: Converting to DocumentRegistrationRequest
        DocumentRegistrationRequest request =
                converter.toDocumentRegistrationRequest(documentReference);

        // Then: Description should be null
        assertNotNull(request);
        assertNull(request.getDocumentDescription());
    }

    @Test
    void testConvertDocumentReferenceWithoutHash() {
        // Given: A FHIR DocumentReference without hash
        DocumentReference documentReference = createValidDocumentReference();
        documentReference.getContent().get(0).getAttachment().setHash(null);

        // When: Converting to DocumentRegistrationRequest
        DocumentRegistrationRequest request =
                converter.toDocumentRegistrationRequest(documentReference);

        // Then: Hash should be placeholder (all zeros)
        assertNotNull(request);
        assertTrue(request.getDocumentHash().startsWith("sha256:"));
        assertEquals(71, request.getDocumentHash().length()); // "sha256:" + 64 hex chars
    }

    @Test
    void testConvertDocumentReferenceWithoutSubject() {
        // Given: A FHIR DocumentReference without subject
        DocumentReference documentReference = createValidDocumentReference();
        documentReference.setSubject(null);

        // When: Converting to DocumentRegistrationRequest
        // Then: Should throw FhirConversionException
        assertThrows(FhirConversionException.class, () -> {
            converter.toDocumentRegistrationRequest(documentReference);
        });
    }

    @Test
    void testConvertDocumentReferenceWithoutType() {
        // Given: A FHIR DocumentReference without type
        DocumentReference documentReference = createValidDocumentReference();
        documentReference.setType(null);

        // When: Converting to DocumentRegistrationRequest
        // Then: Should throw FhirConversionException
        assertThrows(FhirConversionException.class, () -> {
            converter.toDocumentRegistrationRequest(documentReference);
        });
    }

    @Test
    void testConvertDocumentReferenceWithoutContent() {
        // Given: A FHIR DocumentReference without content
        DocumentReference documentReference = createValidDocumentReference();
        documentReference.getContent().clear();

        // When: Converting to DocumentRegistrationRequest
        // Then: Should throw FhirConversionException
        assertThrows(FhirConversionException.class, () -> {
            converter.toDocumentRegistrationRequest(documentReference);
        });
    }

    @Test
    void testConvertDocumentReferenceWithoutCustodian() {
        // Given: A FHIR DocumentReference without custodian
        DocumentReference documentReference = createValidDocumentReference();
        documentReference.setCustodian(null);

        // When: Converting to DocumentRegistrationRequest
        // Then: Should throw FhirConversionException
        assertThrows(FhirConversionException.class, () -> {
            converter.toDocumentRegistrationRequest(documentReference);
        });
    }

    @Test
    void testConvertNullDocumentReference() {
        // Given: A null DocumentReference
        // When: Converting to DocumentRegistrationRequest
        // Then: Should throw FhirConversionException
        assertThrows(FhirConversionException.class, () -> {
            converter.toDocumentRegistrationRequest(null);
        });
    }

    /**
     * Create a valid FHIR DocumentReference resource for testing
     */
    private DocumentReference createValidDocumentReference() {
        DocumentReference documentReference = new DocumentReference();

        // Set status
        documentReference.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);

        // Set type with LOINC coding
        documentReference.setType(new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem("http://loinc.org")
                        .setCode("18725-2")
                        .setDisplay("Microbiology studies")));

        // Set subject (patient reference)
        documentReference.setSubject(new Reference("Patient/12345678"));

        // Set date
        documentReference.setDate(new java.util.Date());

        // Set author
        documentReference.addAuthor(new Reference()
                .setDisplay("Dr. María García")
                .setReference("Practitioner/doctor@clinic.uy"));

        // Set custodian (clinic)
        documentReference.setCustodian(new Reference("Organization/clinic-001"));

        // Set content with attachment
        DocumentReference.DocumentReferenceContentComponent content =
                new DocumentReference.DocumentReferenceContentComponent();

        Attachment attachment = new Attachment();
        attachment.setUrl("https://clinic-001.hcen.uy/api/documents/abc123");
        attachment.setHash(new byte[]{
                (byte) 0xa1, (byte) 0xb2, (byte) 0xc3, (byte) 0xd4
        });
        attachment.setTitle("Lab Results");

        content.setAttachment(attachment);
        documentReference.addContent(content);

        // Set context with related (description)
        DocumentReference.DocumentReferenceContextComponent context =
                new DocumentReference.DocumentReferenceContextComponent();
        context.addRelated(new Reference()
                .setDisplay("Complete blood count analysis"));
        documentReference.setContext(context);

        return documentReference;
    }
}
