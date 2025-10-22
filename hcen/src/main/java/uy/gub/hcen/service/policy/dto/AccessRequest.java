package uy.gub.hcen.service.policy.dto;

import uy.gub.hcen.rndc.entity.DocumentType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Access Request DTO
 *
 * Data Transfer Object representing a request to access a clinical document.
 * Contains all the contextual information needed by the PolicyEngine to evaluate
 * access policies and make authorization decisions.
 *
 * <p>This DTO encapsulates:
 * <ul>
 *   <li>Professional identity (who is requesting access)</li>
 *   <li>Professional attributes (specialties, clinic affiliation)</li>
 *   <li>Resource information (document type, document ID)</li>
 *   <li>Patient identity (whose document is being accessed)</li>
 *   <li>Request context (time, justification)</li>
 * </ul>
 *
 * <p>Used by:
 * <ul>
 *   <li>PolicyEngine for policy evaluation</li>
 *   <li>Individual PolicyEvaluators for attribute-based decisions</li>
 *   <li>Audit logging system</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
public class AccessRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier of the healthcare professional requesting access
     * This is the professional's ID in the HCEN system
     */
    private String professionalId;

    /**
     * List of medical specialties the professional holds
     * Examples: ["CARDIOLOGY", "GENERAL_MEDICINE", "PEDIATRICS"]
     * Used by SpecialtyPolicyEvaluator for specialty-based access control
     */
    private List<String> specialties;

    /**
     * Identifier of the clinic/healthcare facility where the professional works
     * Used by ClinicPolicyEvaluator for clinic-based access control
     */
    private String clinicId;

    /**
     * Patient's CI (Cédula de Identidad) - the owner of the document being accessed
     * References: inus.inus_users.ci
     */
    private String patientCi;

    /**
     * Optional: Specific document ID being accessed
     * If null, this is a general access check (e.g., for search/list operations)
     */
    private Long documentId;

    /**
     * Type of document being accessed
     * Used by DocumentTypePolicyEvaluator for document-type-based access control
     */
    private DocumentType documentType;

    /**
     * Timestamp when the access request is made
     * Used by TimeBasedPolicyEvaluator for time-based access control
     * If null, defaults to current time
     */
    private LocalDateTime requestTime;

    /**
     * Optional: Justification or reason for accessing the document
     * Required for emergency access scenarios
     * Logged in audit trail for accountability
     */
    private String requestReason;

    /**
     * Default constructor
     */
    public AccessRequest() {
        this.specialties = new ArrayList<>();
        this.requestTime = LocalDateTime.now();
    }

    /**
     * Constructor with essential fields
     *
     * @param professionalId Professional's unique identifier
     * @param patientCi Patient's CI
     * @param documentType Document type being accessed
     */
    public AccessRequest(String professionalId, String patientCi, DocumentType documentType) {
        this();
        this.professionalId = professionalId;
        this.patientCi = patientCi;
        this.documentType = documentType;
    }

    /**
     * Full constructor
     *
     * @param professionalId Professional's unique identifier
     * @param specialties List of professional's medical specialties
     * @param clinicId Clinic identifier
     * @param patientCi Patient's CI
     * @param documentId Optional document ID
     * @param documentType Document type
     * @param requestTime Request timestamp
     * @param requestReason Optional justification
     */
    public AccessRequest(String professionalId, List<String> specialties, String clinicId,
                        String patientCi, Long documentId, DocumentType documentType,
                        LocalDateTime requestTime, String requestReason) {
        this.professionalId = professionalId;
        this.specialties = specialties != null ? new ArrayList<>(specialties) : new ArrayList<>();
        this.clinicId = clinicId;
        this.patientCi = patientCi;
        this.documentId = documentId;
        this.documentType = documentType;
        this.requestTime = requestTime != null ? requestTime : LocalDateTime.now();
        this.requestReason = requestReason;
    }

    /**
     * Builder pattern for fluent construction
     *
     * @return New Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters

    public String getProfessionalId() {
        return professionalId;
    }

    public void setProfessionalId(String professionalId) {
        this.professionalId = professionalId;
    }

    public List<String> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<String> specialties) {
        this.specialties = specialties != null ? new ArrayList<>(specialties) : new ArrayList<>();
    }

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
    }

    public String getPatientCi() {
        return patientCi;
    }

    public void setPatientCi(String patientCi) {
        this.patientCi = patientCi;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public String getRequestReason() {
        return requestReason;
    }

    public void setRequestReason(String requestReason) {
        this.requestReason = requestReason;
    }

    // Utility methods

    /**
     * Validates that this access request contains all required fields
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return professionalId != null && !professionalId.trim().isEmpty()
                && patientCi != null && !patientCi.trim().isEmpty()
                && documentType != null
                && requestTime != null;
    }

    /**
     * Checks if this professional has a specific specialty
     *
     * @param specialty Specialty to check
     * @return true if professional has this specialty
     */
    public boolean hasSpecialty(String specialty) {
        if (specialties == null || specialty == null) {
            return false;
        }
        return specialties.stream()
                .anyMatch(s -> s.equalsIgnoreCase(specialty));
    }

    // Equals, HashCode, and ToString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessRequest that = (AccessRequest) o;
        return Objects.equals(professionalId, that.professionalId) &&
                Objects.equals(patientCi, that.patientCi) &&
                Objects.equals(documentId, that.documentId) &&
                documentType == that.documentType &&
                Objects.equals(requestTime, that.requestTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(professionalId, patientCi, documentId, documentType, requestTime);
    }

    @Override
    public String toString() {
        return "AccessRequest{" +
                "professionalId='" + professionalId + '\'' +
                ", specialties=" + specialties +
                ", clinicId='" + clinicId + '\'' +
                ", patientCi='" + patientCi + '\'' +
                ", documentId=" + documentId +
                ", documentType=" + documentType +
                ", requestTime=" + requestTime +
                ", requestReason='" + requestReason + '\'' +
                '}';
    }

    /**
     * Builder class for fluent AccessRequest construction
     */
    public static class Builder {
        private String professionalId;
        private List<String> specialties = new ArrayList<>();
        private String clinicId;
        private String patientCi;
        private Long documentId;
        private DocumentType documentType;
        private LocalDateTime requestTime = LocalDateTime.now();
        private String requestReason;

        public Builder professionalId(String professionalId) {
            this.professionalId = professionalId;
            return this;
        }

        public Builder specialties(List<String> specialties) {
            this.specialties = specialties != null ? new ArrayList<>(specialties) : new ArrayList<>();
            return this;
        }

        public Builder addSpecialty(String specialty) {
            if (specialty != null && !specialty.trim().isEmpty()) {
                this.specialties.add(specialty);
            }
            return this;
        }

        public Builder clinicId(String clinicId) {
            this.clinicId = clinicId;
            return this;
        }

        public Builder patientCi(String patientCi) {
            this.patientCi = patientCi;
            return this;
        }

        public Builder documentId(Long documentId) {
            this.documentId = documentId;
            return this;
        }

        public Builder documentType(DocumentType documentType) {
            this.documentType = documentType;
            return this;
        }

        public Builder requestTime(LocalDateTime requestTime) {
            this.requestTime = requestTime;
            return this;
        }

        public Builder requestReason(String requestReason) {
            this.requestReason = requestReason;
            return this;
        }

        public AccessRequest build() {
            return new AccessRequest(professionalId, specialties, clinicId, patientCi,
                    documentId, documentType, requestTime, requestReason);
        }
    }
}
