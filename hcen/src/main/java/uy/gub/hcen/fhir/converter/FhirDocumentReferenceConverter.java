package uy.gub.hcen.fhir.converter;

import jakarta.enterprise.context.ApplicationScoped;
import org.hl7.fhir.r4.model.*;
import uy.gub.hcen.fhir.exception.FhirConversionException;
import uy.gub.hcen.fhir.validation.FhirValidationUtil;
import uy.gub.hcen.rndc.dto.DocumentRegistrationRequest;
import uy.gub.hcen.rndc.entity.DocumentType;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FHIR DocumentReference Converter
 * <p>
 * Converts FHIR R4 DocumentReference resources to HCEN internal DocumentRegistrationRequest DTOs.
 * This converter handles the mapping between FHIR standard fields and RNDC-specific document
 * registration requirements.
 * <p>
 * Conversion Mapping:
 * - FHIR DocumentReference.subject → DocumentRegistrationRequest.patientCi
 * - FHIR DocumentReference.type (LOINC code) → DocumentRegistrationRequest.documentType
 * - FHIR DocumentReference.content.attachment.url → DocumentRegistrationRequest.documentLocator
 * - FHIR DocumentReference.content.attachment.hash → DocumentRegistrationRequest.documentHash
 * - FHIR DocumentReference.author → DocumentRegistrationRequest.createdBy
 * - FHIR DocumentReference.custodian → DocumentRegistrationRequest.clinicId
 * - FHIR DocumentReference.content.attachment.title → DocumentRegistrationRequest.documentTitle
 * - FHIR DocumentReference.context.related → DocumentRegistrationRequest.documentDescription
 * <p>
 * LOINC Code Mapping:
 * - 11506-3 → CLINICAL_NOTE (Progress note)
 * - 18725-2 → LAB_RESULT (Microbiology studies)
 * - 18748-4 → IMAGING (Diagnostic imaging study)
 * - 57833-6 → PRESCRIPTION (Prescription for medication)
 * - 18842-5 → DISCHARGE_SUMMARY (Discharge summary)
 * - 11369-6 → VACCINATION_RECORD (History of Immunization)
 * - 11504-8 → SURGICAL_REPORT (Surgical operation note)
 * - (default) → OTHER
 * <p>
 * Usage Example:
 * <pre>
 * &#64;Inject
 * private FhirDocumentReferenceConverter docConverter;
 *
 * DocumentReference fhirDoc = parser.parseResource(DocumentReference.class, jsonString);
 * DocumentRegistrationRequest request = docConverter.toDocumentRegistrationRequest(fhirDoc);
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@ApplicationScoped
public class FhirDocumentReferenceConverter {

    private static final Logger LOGGER = Logger.getLogger(FhirDocumentReferenceConverter.class.getName());

    /**
     * LOINC code to DocumentType mapping
     * <p>
     * Maps standard LOINC codes to HCEN internal document types.
     */
    private static final Map<String, DocumentType> LOINC_TO_DOCUMENT_TYPE = new HashMap<>();

    static {
        // Initialize LOINC code mapping
        LOINC_TO_DOCUMENT_TYPE.put("11506-3", DocumentType.CLINICAL_NOTE);     // Progress note
        LOINC_TO_DOCUMENT_TYPE.put("18725-2", DocumentType.LAB_RESULT);        // Microbiology studies
        LOINC_TO_DOCUMENT_TYPE.put("18748-4", DocumentType.IMAGING);           // Diagnostic imaging study
        LOINC_TO_DOCUMENT_TYPE.put("57833-6", DocumentType.PRESCRIPTION);      // Prescription for medication
        LOINC_TO_DOCUMENT_TYPE.put("18842-5", DocumentType.DISCHARGE_SUMMARY); // Discharge summary
        LOINC_TO_DOCUMENT_TYPE.put("11369-6", DocumentType.VACCINATION_RECORD);// History of Immunization
        LOINC_TO_DOCUMENT_TYPE.put("11504-8", DocumentType.SURGICAL_REPORT);   // Surgical operation note
        LOINC_TO_DOCUMENT_TYPE.put("60591-5", DocumentType.PATHOLOGY_REPORT);  // Pathology report
        LOINC_TO_DOCUMENT_TYPE.put("34133-9", DocumentType.EMERGENCY_REPORT);  // Emergency department note
        LOINC_TO_DOCUMENT_TYPE.put("57133-1", DocumentType.REFERRAL);          // Referral note
        LOINC_TO_DOCUMENT_TYPE.put("11492-6", DocumentType.PROGRESS_NOTE);     // Progress note (Provider)
        LOINC_TO_DOCUMENT_TYPE.put("48765-2", DocumentType.ALLERGY_RECORD);    // Allergies and adverse reactions
        LOINC_TO_DOCUMENT_TYPE.put("8716-3", DocumentType.VITAL_SIGNS);        // Vital signs
        LOINC_TO_DOCUMENT_TYPE.put("69730-0", DocumentType.DIAGNOSTIC_REPORT); // Diagnostic report
        LOINC_TO_DOCUMENT_TYPE.put("18776-5", DocumentType.TREATMENT_PLAN);    // Treatment plan
        LOINC_TO_DOCUMENT_TYPE.put("59284-0", DocumentType.INFORMED_CONSENT);  // Patient consent
    }

    /**
     * Convert FHIR DocumentReference resource to DocumentRegistrationRequest DTO.
     * <p>
     * This method performs the following steps:
     * 1. Validate FHIR DocumentReference resource
     * 2. Extract patient CI from subject reference
     * 3. Extract document type from LOINC coding
     * 4. Extract document locator from content attachment
     * 5. Extract document hash from attachment
     * 6. Extract creator from author reference
     * 7. Extract clinic ID from custodian reference
     * 8. Extract title and description
     * 9. Build DocumentRegistrationRequest DTO
     *
     * @param documentReference FHIR DocumentReference resource
     * @return DocumentRegistrationRequest DTO for RNDC registration
     * @throws FhirConversionException if validation fails or required fields are missing
     */
    public DocumentRegistrationRequest toDocumentRegistrationRequest(DocumentReference documentReference)
            throws FhirConversionException {

        LOGGER.log(Level.FINE, "Converting FHIR DocumentReference resource to DocumentRegistrationRequest");

        try {
            // Step 1: Validate FHIR DocumentReference resource
            FhirValidationUtil.validateDocumentReference(documentReference);

            // Step 2: Extract patient CI
            String patientCi = extractPatientCi(documentReference);

            // Step 3: Extract document type
            DocumentType documentType = extractDocumentType(documentReference);

            // Step 4: Extract document locator
            String documentLocator = extractDocumentLocator(documentReference);

            // Step 5: Extract document hash
            String documentHash = extractDocumentHash(documentReference);

            // Step 6: Extract creator
            String createdBy = extractCreatedBy(documentReference);

            // Step 7: Extract clinic ID
            String clinicId = extractClinicId(documentReference);

            // Step 8: Extract title and description
            String documentTitle = extractDocumentTitle(documentReference);
            String documentDescription = extractDocumentDescription(documentReference);

            // Step 9: Build DocumentRegistrationRequest
            DocumentRegistrationRequest request = new DocumentRegistrationRequest(
                    patientCi,
                    documentType,
                    documentLocator,
                    documentHash,
                    createdBy,
                    clinicId,
                    documentTitle,
                    documentDescription
            );

            LOGGER.log(Level.INFO,
                    "Successfully converted FHIR DocumentReference to DocumentRegistrationRequest - " +
                            "Patient: {0}, Type: {1}, Clinic: {2}",
                    new Object[]{patientCi, documentType, clinicId});

            return request;

        } catch (FhirConversionException e) {
            // Re-throw FHIR conversion exceptions
            throw e;

        } catch (Exception e) {
            // Wrap other exceptions
            LOGGER.log(Level.SEVERE, "Unexpected error during FHIR DocumentReference conversion", e);
            throw new FhirConversionException(
                    "Failed to convert FHIR DocumentReference resource to DocumentRegistrationRequest", e
            );
        }
    }

    /**
     * Extract patient CI from DocumentReference subject reference.
     * <p>
     * Expected format: Patient/12345678
     * Extracted value: 12345678
     *
     * @param documentReference FHIR DocumentReference resource
     * @return Patient CI
     * @throws FhirConversionException if patient reference is missing or invalid
     */
    private String extractPatientCi(DocumentReference documentReference) throws FhirConversionException {
        Reference subject = documentReference.getSubject();

        if (!subject.hasReference()) {
            throw new FhirConversionException(
                    "DocumentReference subject must have reference"
            );
        }

        String reference = subject.getReference();

        // Extract patient ID from reference (e.g., "Patient/12345678" → "12345678")
        if (reference.contains("/")) {
            String patientCi = reference.substring(reference.lastIndexOf('/') + 1);
            LOGGER.log(Level.FINE, "Extracted patient CI: {0}", patientCi);
            return patientCi;
        }

        // If no slash, assume entire reference is the patient CI
        LOGGER.log(Level.FINE, "Extracted patient CI: {0}", reference);
        return reference;
    }

    /**
     * Extract document type from DocumentReference type coding.
     * <p>
     * Maps LOINC code to DocumentType enum.
     *
     * @param documentReference FHIR DocumentReference resource
     * @return DocumentType enum value
     * @throws FhirConversionException if type coding is missing or invalid
     */
    private DocumentType extractDocumentType(DocumentReference documentReference)
            throws FhirConversionException {

        String loincCode = FhirValidationUtil.extractLoincCode(documentReference);

        if (loincCode == null || loincCode.trim().isEmpty()) {
            // If no LOINC code, try to get code from any coding system
            LOGGER.log(Level.WARNING, "DocumentReference does not have LOINC coding, using first available code");

            if (!documentReference.getType().hasCoding() ||
                    documentReference.getType().getCoding().isEmpty()) {
                throw new FhirConversionException("DocumentReference type must have at least one coding");
            }

            Coding coding = documentReference.getType().getCoding().get(0);
            if (coding.hasCode()) {
                loincCode = coding.getCode();
            } else {
                // If no code at all, default to OTHER
                LOGGER.log(Level.WARNING, "DocumentReference type coding has no code, defaulting to OTHER");
                return DocumentType.OTHER;
            }
        }

        // Map LOINC code to DocumentType
        DocumentType documentType = LOINC_TO_DOCUMENT_TYPE.getOrDefault(loincCode, DocumentType.OTHER);

        LOGGER.log(Level.FINE, "Mapped LOINC code {0} to DocumentType {1}",
                new Object[]{loincCode, documentType});

        return documentType;
    }

    /**
     * Extract document locator from DocumentReference content attachment.
     *
     * @param documentReference FHIR DocumentReference resource
     * @return Document locator URL
     * @throws FhirConversionException if attachment URL is missing
     */
    private String extractDocumentLocator(DocumentReference documentReference)
            throws FhirConversionException {

        DocumentReference.DocumentReferenceContentComponent content =
                documentReference.getContent().get(0);

        Attachment attachment = content.getAttachment();

        if (!attachment.hasUrl()) {
            throw new FhirConversionException(
                    "DocumentReference attachment must have URL (document locator)"
            );
        }

        String documentLocator = attachment.getUrl();

        LOGGER.log(Level.FINE, "Extracted document locator: {0}", documentLocator);

        return documentLocator;
    }

    /**
     * Extract document hash from DocumentReference attachment.
     * <p>
     * FHIR hash is base64-encoded. We decode it and convert to HCEN format: sha256:[hex]
     *
     * @param documentReference FHIR DocumentReference resource
     * @return Document hash in sha256:hex format
     */
    private String extractDocumentHash(DocumentReference documentReference) {
        DocumentReference.DocumentReferenceContentComponent content =
                documentReference.getContent().get(0);

        Attachment attachment = content.getAttachment();

        if (!attachment.hasHash()) {
            LOGGER.log(Level.WARNING, "DocumentReference attachment does not have hash, generating placeholder");
            // Return placeholder hash (will fail validation in production)
            return "sha256:0000000000000000000000000000000000000000000000000000000000000000";
        }

        byte[] hashBytes = attachment.getHash();

        // Convert bytes to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        String documentHash = "sha256:" + hexString.toString();

        LOGGER.log(Level.FINE, "Extracted document hash: {0}", documentHash);

        return documentHash;
    }

    /**
     * Extract creator (author) from DocumentReference.
     * <p>
     * Uses first author's display name or reference.
     *
     * @param documentReference FHIR DocumentReference resource
     * @return Creator identifier (email or name)
     */
    private String extractCreatedBy(DocumentReference documentReference) {
        Reference author = documentReference.getAuthor().get(0);

        // Prefer display name (e.g., "Dr. María García")
        if (author.hasDisplay()) {
            String createdBy = author.getDisplay();
            LOGGER.log(Level.FINE, "Extracted creator from display: {0}", createdBy);
            return createdBy;
        }

        // Fall back to reference (e.g., "Practitioner/doctor@clinic.uy")
        if (author.hasReference()) {
            String reference = author.getReference();

            // Extract ID from reference
            if (reference.contains("/")) {
                String createdBy = reference.substring(reference.lastIndexOf('/') + 1);
                LOGGER.log(Level.FINE, "Extracted creator from reference: {0}", createdBy);
                return createdBy;
            }

            LOGGER.log(Level.FINE, "Extracted creator from reference: {0}", reference);
            return reference;
        }

        // If no display or reference, use placeholder
        LOGGER.log(Level.WARNING, "DocumentReference author has no display or reference, using placeholder");
        return "unknown";
    }

    /**
     * Extract clinic ID from DocumentReference custodian.
     * <p>
     * Expected format: Organization/clinic-001
     * Extracted value: clinic-001
     *
     * @param documentReference FHIR DocumentReference resource
     * @return Clinic ID
     * @throws FhirConversionException if custodian is missing
     */
    private String extractClinicId(DocumentReference documentReference) throws FhirConversionException {
        Reference custodian = documentReference.getCustodian();

        if (!custodian.hasReference()) {
            throw new FhirConversionException(
                    "DocumentReference custodian must have reference"
            );
        }

        String reference = custodian.getReference();

        // Extract clinic ID from reference (e.g., "Organization/clinic-001" → "clinic-001")
        if (reference.contains("/")) {
            String clinicId = reference.substring(reference.lastIndexOf('/') + 1);
            LOGGER.log(Level.FINE, "Extracted clinic ID: {0}", clinicId);
            return clinicId;
        }

        // If no slash, assume entire reference is the clinic ID
        LOGGER.log(Level.FINE, "Extracted clinic ID: {0}", reference);
        return reference;
    }

    /**
     * Extract document title from DocumentReference attachment.
     *
     * @param documentReference FHIR DocumentReference resource
     * @return Document title, or null if not specified
     */
    private String extractDocumentTitle(DocumentReference documentReference) {
        DocumentReference.DocumentReferenceContentComponent content =
                documentReference.getContent().get(0);

        Attachment attachment = content.getAttachment();

        if (attachment.hasTitle()) {
            String title = attachment.getTitle();
            LOGGER.log(Level.FINE, "Extracted document title: {0}", title);
            return title;
        }

        LOGGER.log(Level.FINE, "DocumentReference attachment does not have title");
        return null;
    }

    /**
     * Extract document description from DocumentReference context.
     * <p>
     * Uses first related resource display text as description.
     *
     * @param documentReference FHIR DocumentReference resource
     * @return Document description, or null if not specified
     */
    private String extractDocumentDescription(DocumentReference documentReference) {
        if (!documentReference.hasContext()) {
            return null;
        }

        DocumentReference.DocumentReferenceContextComponent context = documentReference.getContext();

        if (!context.hasRelated() || context.getRelated().isEmpty()) {
            return null;
        }

        Reference related = context.getRelated().get(0);

        if (related.hasDisplay()) {
            String description = related.getDisplay();
            LOGGER.log(Level.FINE, "Extracted document description: {0}", description);
            return description;
        }

        LOGGER.log(Level.FINE, "DocumentReference context.related does not have display");
        return null;
    }
}
