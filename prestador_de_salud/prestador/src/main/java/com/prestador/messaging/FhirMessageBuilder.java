package com.prestador.messaging;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

/**
 * FHIR Message Builder
 *
 * Utility class for building FHIR R4 resources for HCEN integration.
 * Converts local entities to standardized FHIR format.
 *
 * FHIR Resources Used:
 * - Patient: Patient demographic and identification
 * - DocumentReference: Clinical document metadata
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class FhirMessageBuilder {

    private static final FhirContext fhirContext = FhirContext.forR4();
    private static final IParser jsonParser = fhirContext.newJsonParser().setPrettyPrint(true);

    private static final String PRESTADOR_SYSTEM = "http://prestador-de-salud.uy";
    private static final String HCEN_SYSTEM = "http://hcen.gub.uy";

    /**
     * Build FHIR Patient resource
     *
     * @param ci Document number (Cédula de Identidad)
     * @param firstName First name
     * @param lastName Last name
     * @param birthDate Date of birth
     * @param gender Gender (M/F/Other)
     * @param email Email address
     * @param phone Phone number
     * @param address Address
     * @param clinicId Clinic identifier
     * @return FHIR Patient resource as JSON string
     */
    public static String buildPatientResource(
            String ci,
            String firstName,
            String lastName,
            LocalDate birthDate,
            String gender,
            String email,
            String phone,
            String address,
            Long clinicId) {

        Patient patient = new Patient();

        // Logical ID (use CI as identifier)
        patient.setId(ci);

        // Identifier: National ID (CI)
        Identifier ciIdentifier = new Identifier();
        ciIdentifier.setSystem(HCEN_SYSTEM + "/identifiers/ci");
        ciIdentifier.setValue(ci);
        ciIdentifier.setUse(Identifier.IdentifierUse.OFFICIAL);
        ciIdentifier.getType()
                .addCoding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
                .setCode("NN")
                .setDisplay("National unique individual identifier");
        patient.addIdentifier(ciIdentifier);

        // Name
        HumanName name = new HumanName();
        name.setUse(HumanName.NameUse.OFFICIAL);
        name.setFamily(lastName);
        name.addGiven(firstName);
        name.setText(firstName + " " + lastName);
        patient.addName(name);

        // Gender
        if (gender != null) {
            switch (gender.toUpperCase()) {
                case "M":
                case "MALE":
                    patient.setGender(Enumerations.AdministrativeGender.MALE);
                    break;
                case "F":
                case "FEMALE":
                    patient.setGender(Enumerations.AdministrativeGender.FEMALE);
                    break;
                default:
                    patient.setGender(Enumerations.AdministrativeGender.OTHER);
                    break;
            }
        }

        // Birth date
        if (birthDate != null) {
            patient.setBirthDate(Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        // Contact - Email
        if (email != null && !email.trim().isEmpty()) {
            ContactPoint emailContact = new ContactPoint();
            emailContact.setSystem(ContactPoint.ContactPointSystem.EMAIL);
            emailContact.setValue(email);
            emailContact.setUse(ContactPoint.ContactPointUse.HOME);
            patient.addTelecom(emailContact);
        }

        // Contact - Phone
        if (phone != null && !phone.trim().isEmpty()) {
            ContactPoint phoneContact = new ContactPoint();
            phoneContact.setSystem(ContactPoint.ContactPointSystem.PHONE);
            phoneContact.setValue(phone);
            phoneContact.setUse(ContactPoint.ContactPointUse.MOBILE);
            patient.addTelecom(phoneContact);
        }

        // Address
        if (address != null && !address.trim().isEmpty()) {
            Address addr = new Address();
            addr.setUse(Address.AddressUse.HOME);
            addr.setText(address);
            addr.setCity("Montevideo");
            addr.setCountry("UY");
            patient.addAddress(addr);
        }

        // Managing Organization (Clinic)
        if (clinicId != null) {
            Reference orgRef = new Reference();
            orgRef.setReference("Organization/clinic-" + clinicId);
            orgRef.setDisplay("Clinic " + clinicId);
            patient.setManagingOrganization(orgRef);
        }

        // Active status
        patient.setActive(true);

        // Meta
        Meta meta = new Meta();
        meta.setLastUpdated(new Date());
        meta.addProfile(HCEN_SYSTEM + "/fhir/StructureDefinition/hcen-patient");
        patient.setMeta(meta);

        // Convert to JSON
        return jsonParser.encodeResourceToString(patient);
    }

    /**
     * Build FHIR DocumentReference resource
     *
     * Represents metadata about a clinical document, with a URL to retrieve the actual content.
     *
     * @param patientCI Patient's CI
     * @param documentId Local document ID
     * @param documentType Type of document
     * @param documentTitle Document title
     * @param documentDescription Document description
     * @param documentLocatorUrl URL to retrieve the document
     * @param documentHash SHA-256 hash of document
     * @param createdBy Professional who created the document
     * @param createdAt Creation timestamp
     * @param clinicId Clinic identifier
     * @param specialtyId Specialty identifier (optional)
     * @return FHIR DocumentReference resource as JSON string
     */
    public static String buildDocumentReferenceResource(
            String patientCI,
            Long documentId,
            String documentType,
            String documentTitle,
            String documentDescription,
            String documentLocatorUrl,
            String documentHash,
            String createdBy,
            LocalDateTime createdAt,
            Long clinicId,
            Long specialtyId) {

        DocumentReference docRef = new DocumentReference();

        // Logical ID
        docRef.setId("doc-" + documentId);

        // Status
        docRef.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);

        // Type - Document type coding
        CodeableConcept type = new CodeableConcept();
        Coding typeCoding = new Coding();
        typeCoding.setSystem("http://loinc.org");
        typeCoding.setDisplay(documentType);

        // Map common document types to LOINC codes
        switch (documentType) {
            case "CLINICAL_NOTE":
                typeCoding.setCode("34109-9");
                typeCoding.setDisplay("Note");
                break;
            case "LAB_RESULT":
                typeCoding.setCode("11502-2");
                typeCoding.setDisplay("Laboratory report");
                break;
            case "IMAGING":
                typeCoding.setCode("18748-4");
                typeCoding.setDisplay("Diagnostic imaging study");
                break;
            case "PRESCRIPTION":
                typeCoding.setCode("57833-6");
                typeCoding.setDisplay("Prescription for medication");
                break;
            case "DISCHARGE_SUMMARY":
                typeCoding.setCode("18842-5");
                typeCoding.setDisplay("Discharge summary");
                break;
            default:
                typeCoding.setCode("34133-9");
                typeCoding.setDisplay("Summarization of episode note");
                break;
        }
        type.addCoding(typeCoding);
        docRef.setType(type);

        // Category - Clinical document
        CodeableConcept category = new CodeableConcept();
        category.addCoding()
                .setSystem("http://hl7.org/fhir/us/core/CodeSystem/us-core-documentreference-category")
                .setCode("clinical-note")
                .setDisplay("Clinical Note");
        docRef.addCategory(category);

        // Subject - Reference to Patient
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/" + patientCI);
        patientRef.setDisplay("Patient CI: " + patientCI);
        docRef.setSubject(patientRef);

        // Date - Document creation date
        if (createdAt != null) {
            docRef.setDate(Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant()));
        }

        // Author - Professional who created the document
        if (createdBy != null) {
            Reference authorRef = new Reference();
            authorRef.setReference("Practitioner/" + createdBy);
            authorRef.setDisplay(createdBy);
            docRef.addAuthor(authorRef);
        }

        // Custodian - Organization (Clinic)
        if (clinicId != null) {
            Reference custodianRef = new Reference();
            custodianRef.setReference("Organization/clinic-" + clinicId);
            custodianRef.setDisplay("Clinic " + clinicId);
            docRef.setCustodian(custodianRef);
        }

        // Context - Specialty
        if (specialtyId != null) {
            DocumentReference.DocumentReferenceContextComponent context =
                    new DocumentReference.DocumentReferenceContextComponent();

            CodeableConcept practiceSettingCode = new CodeableConcept();
            practiceSettingCode.addCoding()
                    .setSystem(HCEN_SYSTEM + "/fhir/CodeSystem/specialty")
                    .setCode(String.valueOf(specialtyId))
                    .setDisplay("Specialty " + specialtyId);
            context.setPracticeSetting(practiceSettingCode);

            docRef.setContext(context);
        }

        // Content - Document attachment
        DocumentReference.DocumentReferenceContentComponent content =
                new DocumentReference.DocumentReferenceContentComponent();

        Attachment attachment = new Attachment();
        attachment.setContentType("application/json"); // or application/pdf, etc.
        attachment.setUrl(documentLocatorUrl); // URL to retrieve the actual document
        attachment.setTitle(documentTitle);

        if (documentHash != null) {
            attachment.setHash(documentHash.replace("sha256:", "").getBytes());
        }

        attachment.setCreation(createdAt != null ?
                Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant()) : new Date());

        content.setAttachment(attachment);
        docRef.addContent(content);

        // Description
        if (documentDescription != null) {
            docRef.setDescription(documentDescription);
        }

        // Identifier - Local document ID
        Identifier docIdentifier = new Identifier();
        docIdentifier.setSystem(PRESTADOR_SYSTEM + "/documents");
        docIdentifier.setValue(String.valueOf(documentId));
        docRef.setMasterIdentifier(docIdentifier);

        // Meta
        Meta meta = new Meta();
        meta.setLastUpdated(new Date());
        meta.addProfile(HCEN_SYSTEM + "/fhir/StructureDefinition/hcen-documentreference");
        docRef.setMeta(meta);

        // Security label - Normal confidentiality
        meta.addSecurity()
                .setSystem("http://terminology.hl7.org/CodeSystem/v3-Confidentiality")
                .setCode("N")
                .setDisplay("Normal");

        // Convert to JSON
        return jsonParser.encodeResourceToString(docRef);
    }

    /**
     * Wrap FHIR resource in a message envelope
     *
     * @param resourceJson FHIR resource as JSON
     * @param eventType Event type (e.g., "patient-create", "document-create")
     * @param sourceSystem Source system identifier
     * @return Message envelope with FHIR resource
     */
    public static String wrapInMessageEnvelope(String resourceJson, String eventType, String sourceSystem) {
        org.json.JSONObject envelope = new org.json.JSONObject();
        envelope.put("messageId", "msg-" + UUID.randomUUID().toString());
        envelope.put("timestamp", LocalDateTime.now().toString());
        envelope.put("sourceSystem", sourceSystem);
        envelope.put("eventType", eventType);
        envelope.put("fhirVersion", "R4");
        envelope.put("resource", new org.json.JSONObject(resourceJson));

        return envelope.toString();
    }

    /**
     * Parse FHIR Patient resource from JSON
     *
     * @param json FHIR Patient JSON
     * @return Patient resource
     */
    public static Patient parsePatient(String json) {
        return jsonParser.parseResource(Patient.class, json);
    }

    /**
     * Parse FHIR DocumentReference resource from JSON
     *
     * @param json FHIR DocumentReference JSON
     * @return DocumentReference resource
     */
    public static DocumentReference parseDocumentReference(String json) {
        return jsonParser.parseResource(DocumentReference.class, json);
    }
}
