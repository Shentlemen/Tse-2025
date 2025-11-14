package uy.gub.hcen.fhir.converter;

import jakarta.enterprise.context.ApplicationScoped;
import org.hl7.fhir.r4.model.*;
import uy.gub.hcen.fhir.exception.FhirConversionException;
import uy.gub.hcen.fhir.validation.FhirValidationUtil;
import uy.gub.hcen.inus.dto.UserRegistrationRequest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FHIR Patient Converter
 * <p>
 * Converts FHIR R4 Patient resources to HCEN internal UserRegistrationRequest DTOs.
 * This converter handles the mapping between FHIR standard fields and HCEN-specific
 * user registration requirements.
 * <p>
 * Conversion Mapping:
 * - FHIR Patient.identifier (Uruguay OID) → UserRegistrationRequest.ci
 * - FHIR Patient.name.family → UserRegistrationRequest.lastName
 * - FHIR Patient.name.given → UserRegistrationRequest.firstName
 * - FHIR Patient.telecom (email) → UserRegistrationRequest.email
 * - FHIR Patient.telecom (phone) → UserRegistrationRequest.phoneNumber
 * - FHIR Patient.birthDate → UserRegistrationRequest.dateOfBirth
 * - FHIR Patient.managingOrganization → UserRegistrationRequest.clinicId
 * <p>
 * Usage Example:
 * <pre>
 * &#64;Inject
 * private FhirPatientConverter patientConverter;
 *
 * Patient fhirPatient = parser.parseResource(Patient.class, jsonString);
 * UserRegistrationRequest request = patientConverter.toUserRegistrationRequest(fhirPatient);
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@ApplicationScoped
public class FhirPatientConverter {

    private static final Logger LOGGER = Logger.getLogger(FhirPatientConverter.class.getName());

    /**
     * Convert FHIR Patient resource to UserRegistrationRequest DTO.
     * <p>
     * This method performs the following steps:
     * 1. Validate FHIR Patient resource (throws exception if invalid)
     * 2. Extract Uruguay national ID (CI) from identifiers
     * 3. Extract name (first name and last name)
     * 4. Extract contact information (email and phone)
     * 5. Extract birth date
     * 6. Extract clinic ID from managing organization
     * 7. Build UserRegistrationRequest DTO
     *
     * @param patient FHIR Patient resource
     * @return UserRegistrationRequest DTO for INUS registration
     * @throws FhirConversionException if validation fails or required fields are missing
     */
    public UserRegistrationRequest toUserRegistrationRequest(Patient patient)
            throws FhirConversionException {

        LOGGER.log(Level.FINE, "Converting FHIR Patient resource to UserRegistrationRequest");

        try {
            // Step 1: Validate FHIR Patient resource
            FhirValidationUtil.validatePatient(patient);

            // Step 2: Extract CI (Cédula de Identidad)
            String ci = extractCi(patient);

            // Step 3: Extract name
            HumanName name = patient.getName().get(0);
            String firstName = extractFirstName(name);
            String lastName = extractLastName(name);

            // Step 4: Extract contact information
            String email = extractEmail(patient);
            String phoneNumber = extractPhoneNumber(patient);

            // Step 5: Extract birth date
            LocalDate dateOfBirth = extractBirthDate(patient);

            // Step 6: Extract clinic ID
            String clinicId = extractClinicId(patient);

            // Step 7: Build UserRegistrationRequest
            UserRegistrationRequest request = new UserRegistrationRequest(
                    ci,
                    firstName,
                    lastName,
                    dateOfBirth,
                    email,
                    phoneNumber,
                    clinicId
            );

            LOGGER.log(Level.INFO,
                    "Successfully converted FHIR Patient to UserRegistrationRequest - CI: {0}, Name: {1} {2}",
                    new Object[]{ci, firstName, lastName});

            return request;

        } catch (FhirConversionException e) {
            // Re-throw FHIR conversion exceptions
            throw e;

        } catch (Exception e) {
            // Wrap other exceptions
            LOGGER.log(Level.SEVERE, "Unexpected error during FHIR Patient conversion", e);
            throw new FhirConversionException(
                    "Failed to convert FHIR Patient resource to UserRegistrationRequest", e
            );
        }
    }

    /**
     * Extract Cédula de Identidad (CI) from Patient identifiers.
     * <p>
     * Looks for identifier with Uruguay national ID system OID.
     *
     * @param patient FHIR Patient resource
     * @return CI value
     * @throws FhirConversionException if CI not found
     */
    private String extractCi(Patient patient) throws FhirConversionException {
        String ci = FhirValidationUtil.extractUruguayNationalId(patient);

        if (ci == null || ci.trim().isEmpty()) {
            throw new FhirConversionException(
                    "Patient resource must have Uruguay national ID identifier with value"
            );
        }

        // Normalize CI format (remove dots and dashes if present)
        // Example: 1.234.567-8 → 12345678
        String normalizedCi = ci.replaceAll("[.\\-]", "");

        LOGGER.log(Level.FINE, "Extracted CI: {0} (normalized: {1})", new Object[]{ci, normalizedCi});

        return normalizedCi;
    }

    /**
     * Extract first name from HumanName.
     * <p>
     * If multiple given names exist, joins them with space.
     *
     * @param name FHIR HumanName
     * @return First name
     * @throws FhirConversionException if no given name found
     */
    private String extractFirstName(HumanName name) throws FhirConversionException {
        if (!name.hasGiven() || name.getGiven().isEmpty()) {
            throw new FhirConversionException("Patient name must have given name (first name)");
        }

        // Join multiple given names with space (e.g., "Juan Carlos" → "Juan Carlos")
        StringBuilder firstName = new StringBuilder();
        for (int i = 0; i < name.getGiven().size(); i++) {
            if (i > 0) {
                firstName.append(" ");
            }
            firstName.append(name.getGiven().get(i).getValue());
        }

        return firstName.toString().trim();
    }

    /**
     * Extract last name from HumanName.
     *
     * @param name FHIR HumanName
     * @return Last name
     * @throws FhirConversionException if no family name found
     */
    private String extractLastName(HumanName name) throws FhirConversionException {
        if (!name.hasFamily()) {
            throw new FhirConversionException("Patient name must have family name (last name)");
        }

        return name.getFamily();
    }

    /**
     * Extract email from Patient telecom.
     * <p>
     * Returns first email found, or null if no email exists.
     *
     * @param patient FHIR Patient resource
     * @return Email address, or null
     */
    private String extractEmail(Patient patient) {
        if (!patient.hasTelecom()) {
            return null;
        }

        return patient.getTelecom().stream()
                .filter(telecom -> telecom.getSystem() == ContactPoint.ContactPointSystem.EMAIL)
                .filter(ContactPoint::hasValue)
                .map(ContactPoint::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Extract phone number from Patient telecom.
     * <p>
     * Returns first phone number found, or null if no phone exists.
     *
     * @param patient FHIR Patient resource
     * @return Phone number, or null
     */
    private String extractPhoneNumber(Patient patient) {
        if (!patient.hasTelecom()) {
            return null;
        }

        return patient.getTelecom().stream()
                .filter(telecom -> telecom.getSystem() == ContactPoint.ContactPointSystem.PHONE)
                .filter(ContactPoint::hasValue)
                .map(ContactPoint::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Extract birth date from Patient.
     *
     * @param patient FHIR Patient resource
     * @return Birth date as LocalDate
     * @throws FhirConversionException if birth date is missing or invalid
     */
    private LocalDate extractBirthDate(Patient patient) throws FhirConversionException {
        if (!patient.hasBirthDate()) {
            throw new FhirConversionException("Patient resource must have birth date");
        }

        Date birthDate = patient.getBirthDate();

        // Convert java.util.Date to LocalDate
        LocalDate localDate = birthDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LOGGER.log(Level.FINE, "Extracted birth date: {0}", localDate);

        return localDate;
    }

    /**
     * Extract clinic ID from Patient managing organization.
     * <p>
     * Returns clinic ID from organization reference, or null if not specified.
     * <p>
     * Expected format: Organization/clinic-001
     * Extracted value: clinic-001
     *
     * @param patient FHIR Patient resource
     * @return Clinic ID, or null
     */
    private String extractClinicId(Patient patient) {
        if (!patient.hasManagingOrganization()) {
            LOGGER.log(Level.FINE, "Patient does not have managing organization (clinic)");
            return null;
        }

        Reference orgRef = patient.getManagingOrganization();

        if (!orgRef.hasReference()) {
            LOGGER.log(Level.WARNING, "Patient managing organization reference is empty");
            return null;
        }

        String reference = orgRef.getReference();

        // Extract ID from reference (e.g., "Organization/clinic-001" → "clinic-001")
        if (reference.contains("/")) {
            String clinicId = reference.substring(reference.lastIndexOf('/') + 1);
            LOGGER.log(Level.FINE, "Extracted clinic ID: {0}", clinicId);
            return clinicId;
        }

        // If no slash, assume entire reference is the clinic ID
        LOGGER.log(Level.FINE, "Extracted clinic ID: {0}", reference);
        return reference;
    }
}
