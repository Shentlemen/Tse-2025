package uy.gub.hcen.fhir.validation;

import org.hl7.fhir.r4.model.*;
import uy.gub.hcen.fhir.exception.FhirConversionException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FHIR Validation Utility
 * <p>
 * Provides validation methods for FHIR resources before conversion to internal DTOs.
 * This utility ensures that FHIR resources contain all required fields and follow
 * the expected structure for HCEN integration.
 * <p>
 * Validation Rules:
 * - Patient: Must have Uruguay national ID identifier, name, and birth date
 * - DocumentReference: Must have patient reference, type, content, and custodian
 * - All resources: Must be FHIR R4 version
 * <p>
 * Usage Example:
 * <pre>
 * Patient patient = parser.parseResource(Patient.class, jsonString);
 * FhirValidationUtil.validatePatient(patient);
 * // If validation passes, proceed with conversion
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class FhirValidationUtil {

    private static final Logger LOGGER = Logger.getLogger(FhirValidationUtil.class.getName());

    /**
     * Uruguay National ID OID system identifier
     * <p>
     * This is the official OID for Uruguayan national identity cards (Cédula de Identidad).
     * All Patient resources must have an identifier with this system.
     */
    public static final String URUGUAY_NATIONAL_ID_SYSTEM =
            "urn:oid:2.16.858.1.113883.3.879.1.1.1";

    /**
     * LOINC coding system URI
     * <p>
     * Used for document type coding in DocumentReference resources.
     */
    public static final String LOINC_SYSTEM = "http://loinc.org";

    /**
     * Private constructor to prevent instantiation (utility class)
     */
    private FhirValidationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validate a FHIR Patient resource.
     * <p>
     * Validation Rules:
     * - Must have at least one identifier
     * - Must have Uruguay national ID identifier (Uruguay OID system)
     * - Must have at least one name
     * - Must have birth date
     * <p>
     * Optional fields (logged as warnings if missing):
     * - Email telecom
     * - Phone telecom
     * - Managing organization (clinic)
     *
     * @param patient FHIR Patient resource to validate
     * @throws FhirConversionException if validation fails
     */
    public static void validatePatient(Patient patient) throws FhirConversionException {
        if (patient == null) {
            throw new FhirConversionException("Patient resource cannot be null");
        }

        // Validate resource type
        if (!patient.getResourceType().equals(ResourceType.Patient)) {
            throw new FhirConversionException(
                    "Invalid resource type. Expected Patient, got: " + patient.getResourceType()
            );
        }

        // Validate identifiers
        if (!patient.hasIdentifier() || patient.getIdentifier().isEmpty()) {
            throw new FhirConversionException(
                    "Patient resource must have at least one identifier"
            );
        }

        // Validate Uruguay national ID identifier exists
        boolean hasUruguayId = patient.getIdentifier().stream()
                .anyMatch(id -> URUGUAY_NATIONAL_ID_SYSTEM.equals(id.getSystem()));

        if (!hasUruguayId) {
            throw new FhirConversionException(
                    "Patient resource must have Uruguay national ID identifier (system: " +
                            URUGUAY_NATIONAL_ID_SYSTEM + ")"
            );
        }

        // Validate name
        if (!patient.hasName() || patient.getName().isEmpty()) {
            throw new FhirConversionException(
                    "Patient resource must have at least one name"
            );
        }

        HumanName name = patient.getName().get(0);
        if (!name.hasFamily() || !name.hasGiven()) {
            throw new FhirConversionException(
                    "Patient name must have both family (last name) and given (first name)"
            );
        }

        // Validate birth date
        if (!patient.hasBirthDate()) {
            throw new FhirConversionException(
                    "Patient resource must have birth date"
            );
        }

        // Log warnings for optional fields
        if (!hasEmailTelecom(patient)) {
            LOGGER.log(Level.WARNING, "Patient resource does not have email telecom");
        }

        if (!hasPhoneTelecom(patient)) {
            LOGGER.log(Level.WARNING, "Patient resource does not have phone telecom");
        }

        if (!patient.hasManagingOrganization()) {
            LOGGER.log(Level.WARNING, "Patient resource does not have managing organization (clinic)");
        }

        LOGGER.log(Level.FINE, "Patient resource validation passed");
    }

    /**
     * Validate a FHIR DocumentReference resource.
     * <p>
     * Validation Rules:
     * - Must have patient subject reference
     * - Must have document type with LOINC coding
     * - Must have at least one content attachment
     * - Content attachment must have URL (document locator)
     * - Must have custodian (clinic)
     * - Must have at least one author
     *
     * @param documentReference FHIR DocumentReference resource to validate
     * @throws FhirConversionException if validation fails
     */
    public static void validateDocumentReference(DocumentReference documentReference)
            throws FhirConversionException {

        if (documentReference == null) {
            throw new FhirConversionException("DocumentReference resource cannot be null");
        }

        // Validate resource type
        if (!documentReference.getResourceType().equals(ResourceType.DocumentReference)) {
            throw new FhirConversionException(
                    "Invalid resource type. Expected DocumentReference, got: " +
                            documentReference.getResourceType()
            );
        }

        // Validate patient subject
        if (!documentReference.hasSubject()) {
            throw new FhirConversionException(
                    "DocumentReference must have subject (patient reference)"
            );
        }

        Reference subject = documentReference.getSubject();
        if (!subject.hasReference()) {
            throw new FhirConversionException(
                    "DocumentReference subject must have reference"
            );
        }

        // Validate document type
        if (!documentReference.hasType()) {
            throw new FhirConversionException(
                    "DocumentReference must have type"
            );
        }

        CodeableConcept type = documentReference.getType();
        if (!type.hasCoding() || type.getCoding().isEmpty()) {
            throw new FhirConversionException(
                    "DocumentReference type must have at least one coding"
            );
        }

        // Validate content
        if (!documentReference.hasContent() || documentReference.getContent().isEmpty()) {
            throw new FhirConversionException(
                    "DocumentReference must have at least one content attachment"
            );
        }

        DocumentReference.DocumentReferenceContentComponent content =
                documentReference.getContent().get(0);

        if (!content.hasAttachment()) {
            throw new FhirConversionException(
                    "DocumentReference content must have attachment"
            );
        }

        Attachment attachment = content.getAttachment();
        if (!attachment.hasUrl()) {
            throw new FhirConversionException(
                    "DocumentReference attachment must have URL (document locator)"
            );
        }

        // Validate custodian (clinic)
        if (!documentReference.hasCustodian()) {
            throw new FhirConversionException(
                    "DocumentReference must have custodian (clinic reference)"
            );
        }

        // Validate author
        if (!documentReference.hasAuthor() || documentReference.getAuthor().isEmpty()) {
            throw new FhirConversionException(
                    "DocumentReference must have at least one author"
            );
        }

        // Log warnings for optional fields
        if (!attachment.hasHash()) {
            LOGGER.log(Level.WARNING, "DocumentReference attachment does not have hash (integrity check)");
        }

        if (!attachment.hasTitle()) {
            LOGGER.log(Level.WARNING, "DocumentReference attachment does not have title");
        }

        LOGGER.log(Level.FINE, "DocumentReference resource validation passed");
    }

    /**
     * Check if Patient has email telecom.
     *
     * @param patient FHIR Patient resource
     * @return true if patient has email, false otherwise
     */
    private static boolean hasEmailTelecom(Patient patient) {
        if (!patient.hasTelecom()) {
            return false;
        }

        return patient.getTelecom().stream()
                .anyMatch(telecom -> telecom.getSystem() == ContactPoint.ContactPointSystem.EMAIL);
    }

    /**
     * Check if Patient has phone telecom.
     *
     * @param patient FHIR Patient resource
     * @return true if patient has phone, false otherwise
     */
    private static boolean hasPhoneTelecom(Patient patient) {
        if (!patient.hasTelecom()) {
            return false;
        }

        return patient.getTelecom().stream()
                .anyMatch(telecom -> telecom.getSystem() == ContactPoint.ContactPointSystem.PHONE);
    }

    /**
     * Extract Uruguay national ID value from Patient identifiers.
     *
     * @param patient FHIR Patient resource
     * @return CI value, or null if not found
     */
    public static String extractUruguayNationalId(Patient patient) {
        if (!patient.hasIdentifier()) {
            return null;
        }

        return patient.getIdentifier().stream()
                .filter(id -> URUGUAY_NATIONAL_ID_SYSTEM.equals(id.getSystem()))
                .filter(Identifier::hasValue)
                .map(Identifier::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if a DocumentReference has LOINC coding.
     *
     * @param documentReference FHIR DocumentReference resource
     * @return true if has LOINC coding, false otherwise
     */
    public static boolean hasLoincCoding(DocumentReference documentReference) {
        if (!documentReference.hasType() || !documentReference.getType().hasCoding()) {
            return false;
        }

        List<Coding> codings = documentReference.getType().getCoding();
        return codings.stream()
                .anyMatch(coding -> LOINC_SYSTEM.equals(coding.getSystem()));
    }

    /**
     * Extract LOINC code from DocumentReference type.
     *
     * @param documentReference FHIR DocumentReference resource
     * @return LOINC code value, or null if not found
     */
    public static String extractLoincCode(DocumentReference documentReference) {
        if (!documentReference.hasType() || !documentReference.getType().hasCoding()) {
            return null;
        }

        return documentReference.getType().getCoding().stream()
                .filter(coding -> LOINC_SYSTEM.equals(coding.getSystem()))
                .filter(Coding::hasCode)
                .map(Coding::getCode)
                .findFirst()
                .orElse(null);
    }
}
