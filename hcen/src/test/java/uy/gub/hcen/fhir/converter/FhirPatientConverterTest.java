package uy.gub.hcen.fhir.converter;

import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uy.gub.hcen.fhir.exception.FhirConversionException;
import uy.gub.hcen.inus.dto.UserRegistrationRequest;

import java.time.LocalDate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FhirPatientConverter
 */
class FhirPatientConverterTest {

    private FhirPatientConverter converter;

    @BeforeEach
    void setUp() {
        converter = new FhirPatientConverter();
    }

    @Test
    void testConvertValidFhirPatient() {
        // Given: A valid FHIR Patient resource
        Patient patient = createValidPatient();

        // When: Converting to UserRegistrationRequest
        UserRegistrationRequest request = converter.toUserRegistrationRequest(patient);

        // Then: All fields should be correctly mapped
        assertNotNull(request);
        assertEquals("12345678", request.getCi());
        assertEquals("Juan", request.getFirstName());
        assertEquals("Pérez", request.getLastName());
        assertEquals("juan@example.com", request.getEmail());
        assertEquals("099123456", request.getPhoneNumber());
        assertEquals(LocalDate.of(1990, 1, 15), request.getDateOfBirth());
        assertEquals("clinic-001", request.getClinicId());
    }

    @Test
    void testConvertPatientWithoutEmail() {
        // Given: A FHIR Patient without email
        Patient patient = createValidPatient();
        patient.getTelecom().removeIf(t -> t.getSystem() == ContactPoint.ContactPointSystem.EMAIL);

        // When: Converting to UserRegistrationRequest
        UserRegistrationRequest request = converter.toUserRegistrationRequest(patient);

        // Then: Email should be null
        assertNotNull(request);
        assertNull(request.getEmail());
        assertEquals("099123456", request.getPhoneNumber());
    }

    @Test
    void testConvertPatientWithoutPhone() {
        // Given: A FHIR Patient without phone
        Patient patient = createValidPatient();
        patient.getTelecom().removeIf(t -> t.getSystem() == ContactPoint.ContactPointSystem.PHONE);

        // When: Converting to UserRegistrationRequest
        UserRegistrationRequest request = converter.toUserRegistrationRequest(patient);

        // Then: Phone should be null
        assertNotNull(request);
        assertEquals("juan@example.com", request.getEmail());
        assertNull(request.getPhoneNumber());
    }

    @Test
    void testConvertPatientWithoutClinic() {
        // Given: A FHIR Patient without managing organization
        Patient patient = createValidPatient();
        patient.setManagingOrganization(null);

        // When: Converting to UserRegistrationRequest
        UserRegistrationRequest request = converter.toUserRegistrationRequest(patient);

        // Then: ClinicId should be null
        assertNotNull(request);
        assertNull(request.getClinicId());
    }

    @Test
    void testConvertPatientWithMultipleGivenNames() {
        // Given: A FHIR Patient with multiple given names
        Patient patient = createValidPatient();
        patient.getName().get(0).getGiven().clear();
        patient.getName().get(0).addGiven("Juan");
        patient.getName().get(0).addGiven("Carlos");

        // When: Converting to UserRegistrationRequest
        UserRegistrationRequest request = converter.toUserRegistrationRequest(patient);

        // Then: First name should be joined with space
        assertNotNull(request);
        assertEquals("Juan Carlos", request.getFirstName());
    }

    @Test
    void testConvertPatientWithoutUruguayIdentifier() {
        // Given: A FHIR Patient without Uruguay national ID
        Patient patient = createValidPatient();
        patient.getIdentifier().clear();
        patient.addIdentifier()
                .setSystem("http://example.com/fhir/other-system")
                .setValue("OTHER-123");

        // When: Converting to UserRegistrationRequest
        // Then: Should throw FhirConversionException
        assertThrows(FhirConversionException.class, () -> {
            converter.toUserRegistrationRequest(patient);
        });
    }

    @Test
    void testConvertPatientWithoutName() {
        // Given: A FHIR Patient without name
        Patient patient = createValidPatient();
        patient.getName().clear();

        // When: Converting to UserRegistrationRequest
        // Then: Should throw FhirConversionException
        assertThrows(FhirConversionException.class, () -> {
            converter.toUserRegistrationRequest(patient);
        });
    }

    @Test
    void testConvertPatientWithoutBirthDate() {
        // Given: A FHIR Patient without birth date
        Patient patient = createValidPatient();
        patient.setBirthDate(null);

        // When: Converting to UserRegistrationRequest
        // Then: Should throw FhirConversionException
        assertThrows(FhirConversionException.class, () -> {
            converter.toUserRegistrationRequest(patient);
        });
    }

    @Test
    void testConvertNullPatient() {
        // Given: A null patient
        // When: Converting to UserRegistrationRequest
        // Then: Should throw FhirConversionException
        assertThrows(FhirConversionException.class, () -> {
            converter.toUserRegistrationRequest(null);
        });
    }

    /**
     * Create a valid FHIR Patient resource for testing
     */
    private Patient createValidPatient() {
        Patient patient = new Patient();

        // Add Uruguay national ID identifier
        patient.addIdentifier()
                .setSystem("urn:oid:2.16.858.1.113883.3.879.1.1.1")
                .setValue("12345678");

        // Add name
        patient.addName()
                .setFamily("Pérez")
                .addGiven("Juan");

        // Add telecom (email and phone)
        patient.addTelecom()
                .setSystem(ContactPoint.ContactPointSystem.EMAIL)
                .setValue("juan@example.com");

        patient.addTelecom()
                .setSystem(ContactPoint.ContactPointSystem.PHONE)
                .setValue("099123456");

        // Add birth date
        patient.setBirthDate(new Date(631152000000L)); // 1990-01-15

        // Add managing organization (clinic)
        patient.setManagingOrganization(new Reference("Organization/clinic-001"));

        return patient;
    }
}
