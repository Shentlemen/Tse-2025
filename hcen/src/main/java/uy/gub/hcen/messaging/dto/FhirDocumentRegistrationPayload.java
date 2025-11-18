package uy.gub.hcen.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hl7.fhir.r4.model.DocumentReference;
import uy.gub.hcen.rndc.entity.DocumentType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * FHIR-aware payload for clinical document registration messages.
 * <p>
 * This DTO can deserialize both:
 * 1. Simple DocumentRegistrationPayload format (legacy)
 * 2. FHIR DocumentReference format (from peripheral nodes using FHIR)
 * <p>
 * When receiving a FHIR DocumentReference, this class automatically extracts:
 * - patientCI from subject.reference
 * - documentLocator from content[0].attachment.url
 * - documentHash from content[0].attachment.hash
 * - createdBy from author[0].reference
 * - createdAt from content[0].attachment.creation
 * - clinicId from custodian.reference
 * - documentTitle from content[0].attachment.title
 * - documentDescription from description
 * - documentType from type.coding[0].display
 * <p>
 * This allows HCEN Central to receive FHIR messages from peripheral nodes
 * without requiring the sender to transform to a proprietary format.
 *
 * @author TSE 2025 Group 9
 * @version 1.1
 * @since 2025-11-18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FhirDocumentRegistrationPayload extends DocumentRegistrationPayload implements Serializable {

    private static final long serialVersionUID = 2L;

    /**
     * FHIR DocumentReference resource (if message contains FHIR format).
     * <p>
     * This field is populated during deserialization if the payload is a FHIR
     * DocumentReference. After deserialization, fields from parent class
     * (DocumentRegistrationPayload) are automatically populated by extracting
     * values from this FHIR resource.
     */
    @JsonProperty("resourceType")
    private String resourceType;

    /**
     * FHIR DocumentReference ID
     */
    @JsonProperty("id")
    private String fhirId;

    /**
     * FHIR DocumentReference status
     */
    @JsonProperty("status")
    private String status;

    /**
     * FHIR subject (patient reference)
     */
    @JsonProperty("subject")
    private FhirReference subject;

    /**
     * FHIR custodian (organization/clinic reference)
     */
    @JsonProperty("custodian")
    private FhirReference custodian;

    /**
     * FHIR authors (professional references)
     */
    @JsonProperty("author")
    private FhirReference[] author;

    /**
     * FHIR description
     */
    @JsonProperty("description")
    private String description;

    /**
     * FHIR type (document type)
     */
    @JsonProperty("type")
    private FhirCodeableConcept type;

    /**
     * FHIR content (attachments)
     */
    @JsonProperty("content")
    private FhirContent[] content;

    /**
     * FHIR date
     */
    @JsonProperty("date")
    private String date;

    /**
     * Default constructor for JSON deserialization.
     */
    public FhirDocumentRegistrationPayload() {
        super();
    }

    /**
     * Extract values from FHIR fields to parent DocumentRegistrationPayload fields.
     * <p>
     * Called after Jackson deserialization to populate the parent class fields
     * from the FHIR DocumentReference structure.
     */
    @JsonProperty("subject")
    public void setSubject(FhirReference subject) {
        this.subject = subject;
        if (subject != null && subject.reference != null) {
            // Extract patient CI from "Patient/33333333"
            String patientCI = extractIdFromReference(subject.reference);
            super.setPatientCI(patientCI);
        }
    }

    @JsonProperty("custodian")
    public void setCustodian(FhirReference custodian) {
        this.custodian = custodian;
        if (custodian != null && custodian.reference != null) {
            // Extract clinic ID from "Organization/clinic-1"
            String clinicId = extractIdFromReference(custodian.reference);
            super.setClinicId(clinicId);
        }
    }

    @JsonProperty("author")
    public void setAuthor(FhirReference[] author) {
        this.author = author;
        if (author != null && author.length > 0 && author[0] != null && author[0].reference != null) {
            // Extract professional ID from "Practitioner/professional-5"
            String createdBy = extractIdFromReference(author[0].reference);
            super.setCreatedBy(createdBy);
        }
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
        if (description != null) {
            super.setDocumentDescription(description);
        }
    }

    @JsonProperty("type")
    public void setType(FhirCodeableConcept type) {
        this.type = type;
        if (type != null && type.coding != null && type.coding.length > 0) {
            // Map FHIR type to DocumentType enum
            String typeDisplay = type.coding[0].display;
            DocumentType documentType = mapFhirTypeToDocumentType(typeDisplay);
            super.setDocumentType(documentType);
        }
    }

    @JsonProperty("content")
    public void setContent(FhirContent[] content) {
        this.content = content;
        if (content != null && content.length > 0 && content[0] != null && content[0].attachment != null) {
            FhirAttachment attachment = content[0].attachment;

            // Extract document locator (URL)
            if (attachment.url != null) {
                super.setDocumentLocator(attachment.url);
            }

            // Extract document hash
            if (attachment.hash != null) {
                super.setDocumentHash(attachment.hash);
            }

            // Extract document title
            if (attachment.title != null) {
                super.setDocumentTitle(attachment.title);
            }

            // Extract creation date
            if (attachment.creation != null) {
                LocalDateTime createdAt = parseIso8601DateTime(attachment.creation);
                super.setCreatedAt(createdAt);
            }
        }
    }

    @JsonProperty("date")
    public void setDate(String date) {
        this.date = date;
        if (date != null && super.getCreatedAt() == null) {
            // Use document date as fallback if creation date not in attachment
            LocalDateTime createdAt = parseIso8601DateTime(date);
            super.setCreatedAt(createdAt);
        }
    }

    // ================================================================
    // Helper Methods
    // ================================================================

    /**
     * Extract ID from FHIR reference.
     * <p>
     * Examples:
     * - "Patient/33333333" → "33333333"
     * - "Organization/clinic-1" → "clinic-1"
     * - "Practitioner/professional-5" → "professional-5"
     *
     * @param reference FHIR reference string
     * @return Extracted ID
     */
    private String extractIdFromReference(String reference) {
        if (reference == null) {
            return null;
        }

        int slashIndex = reference.indexOf('/');
        if (slashIndex >= 0 && slashIndex < reference.length() - 1) {
            return reference.substring(slashIndex + 1);
        }

        return reference;
    }

    /**
     * Map FHIR document type to internal DocumentType enum.
     * <p>
     * This is a simple mapping. In production, use a proper terminology service.
     *
     * @param fhirType FHIR type display string
     * @return DocumentType enum
     */
    private DocumentType mapFhirTypeToDocumentType(String fhirType) {
        if (fhirType == null) {
            return DocumentType.CLINICAL_NOTE; // Default
        }

        // Simple string matching (case-insensitive)
        String typeUpper = fhirType.toUpperCase();

        if (typeUpper.contains("LAB") || typeUpper.contains("LABORATORY")) {
            return DocumentType.LAB_RESULT;
        } else if (typeUpper.contains("IMAGE") || typeUpper.contains("IMAGING") || typeUpper.contains("RADIOLOGY")) {
            return DocumentType.IMAGING;
        } else if (typeUpper.contains("PRESCRIPTION") || typeUpper.contains("MEDICATION")) {
            return DocumentType.PRESCRIPTION;
        } else if (typeUpper.contains("DISCHARGE")) {
            return DocumentType.DISCHARGE_SUMMARY;
        } else if (typeUpper.contains("REFERRAL")) {
            return DocumentType.REFERRAL;
        } else if (typeUpper.contains("CONSENT")) {
            return DocumentType.INFORMED_CONSENT;
        } else if (typeUpper.contains("SURGICAL") || typeUpper.contains("SURGERY")) {
            return DocumentType.SURGICAL_REPORT;
        } else if (typeUpper.contains("PATHOLOGY")) {
            return DocumentType.PATHOLOGY_REPORT;
        } else if (typeUpper.contains("CONSULTATION")) {
            return DocumentType.CONSULTATION;
        } else if (typeUpper.contains("EMERGENCY")) {
            return DocumentType.EMERGENCY_REPORT;
        } else if (typeUpper.contains("PROGRESS")) {
            return DocumentType.PROGRESS_NOTE;
        } else if (typeUpper.contains("ALLERGY") || typeUpper.contains("ALLERGIES")) {
            return DocumentType.ALLERGY_RECORD;
        } else if (typeUpper.contains("VITAL")) {
            return DocumentType.VITAL_SIGNS;
        } else if (typeUpper.contains("DIAGNOSTIC")) {
            return DocumentType.DIAGNOSTIC_REPORT;
        } else if (typeUpper.contains("TREATMENT") || typeUpper.contains("CARE PLAN")) {
            return DocumentType.TREATMENT_PLAN;
        } else if (typeUpper.contains("VACCINATION") || typeUpper.contains("IMMUNIZATION")) {
            return DocumentType.VACCINATION_RECORD;
        } else if (typeUpper.contains("NOTE")) {
            return DocumentType.CLINICAL_NOTE;
        } else {
            // Default to OTHER for unknown types
            return DocumentType.OTHER;
        }
    }

    /**
     * Parse ISO-8601 date-time string to LocalDateTime.
     * <p>
     * Handles formats like:
     * - "2025-11-18T01:09:08-03:00"
     * - "2025-11-18T01:09:08.935-03:00"
     * - "2025-11-18T04:09:08Z"
     *
     * @param dateTimeStr ISO-8601 date-time string
     * @return LocalDateTime
     */
    private LocalDateTime parseIso8601DateTime(String dateTimeStr) {
        if (dateTimeStr == null) {
            return null;
        }

        try {
            // Parse using java.time API
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME;
            return java.time.ZonedDateTime.parse(dateTimeStr, formatter).toLocalDateTime();
        } catch (Exception e) {
            // Fallback: try to parse as ISO instant and convert to LocalDateTime
            try {
                return LocalDateTime.parse(dateTimeStr.substring(0, 19));
            } catch (Exception ex) {
                return LocalDateTime.now(); // Fallback to current time
            }
        }
    }

    // ================================================================
    // Getters and Setters for FHIR fields
    // ================================================================

    public String getResourceType() {
        return resourceType;
    }

    @JsonProperty("resourceType")
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getFhirId() {
        return fhirId;
    }

    @JsonProperty("id")
    public void setFhirId(String fhirId) {
        this.fhirId = fhirId;
    }

    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    public FhirReference getSubject() {
        return subject;
    }

    public FhirReference getCustodian() {
        return custodian;
    }

    public FhirReference[] getAuthor() {
        return author;
    }

    public FhirCodeableConcept getType() {
        return type;
    }

    public FhirContent[] getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }

    // ================================================================
    // Inner Classes for FHIR Structure
    // ================================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FhirReference {
        @JsonProperty("reference")
        public String reference;

        @JsonProperty("display")
        public String display;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FhirCodeableConcept {
        @JsonProperty("coding")
        public FhirCoding[] coding;

        @JsonProperty("text")
        public String text;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FhirCoding {
        @JsonProperty("system")
        public String system;

        @JsonProperty("code")
        public String code;

        @JsonProperty("display")
        public String display;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FhirContent {
        @JsonProperty("attachment")
        public FhirAttachment attachment;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FhirAttachment {
        @JsonProperty("url")
        public String url;

        @JsonProperty("title")
        public String title;

        @JsonProperty("contentType")
        public String contentType;

        @JsonProperty("hash")
        public String hash;

        @JsonProperty("creation")
        public String creation;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "FhirDocumentRegistrationPayload{" +
                "resourceType='" + resourceType + '\'' +
                ", fhirId='" + fhirId + '\'' +
                ", status='" + status + '\'' +
                ", patientCI='" + getPatientCI() + '\'' +
                ", documentType=" + getDocumentType() +
                ", documentLocator='" + getDocumentLocator() + '\'' +
                ", documentHash='" + getDocumentHash() + '\'' +
                ", createdBy='" + getCreatedBy() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", clinicId='" + getClinicId() + '\'' +
                ", documentTitle='" + getDocumentTitle() + '\'' +
                ", documentDescription='" + getDocumentDescription() + '\'' +
                '}';
    }
}
