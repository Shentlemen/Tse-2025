package uy.gub.hcen.clinicalhistory.dto;

import java.time.LocalDateTime;

/**
 * Document Metadata DTO
 *
 * Contains contextual metadata about a clinical document for inline display.
 * This information enriches the document viewer with details about the document's
 * origin, authorship, and clinical context.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class DocumentMetadata {

    /**
     * Patient's full name
     */
    private String patientName;

    /**
     * Healthcare professional's full name
     */
    private String professionalName;

    /**
     * Clinic/institution name
     */
    private String clinicName;

    /**
     * Document creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Document title
     */
    private String documentTitle;

    /**
     * Document description or summary
     */
    private String documentDescription;

    /**
     * Document type display name
     */
    private String documentType;

    /**
     * Document hash for integrity verification
     */
    private String documentHash;

    /**
     * Clinic ID
     */
    private String clinicId;

    /**
     * Professional specialty (if applicable)
     */
    private String professionalSpecialty;

    /**
     * Default constructor
     */
    public DocumentMetadata() {
    }

    /**
     * Full constructor
     */
    public DocumentMetadata(String patientName, String professionalName, String clinicName,
                           LocalDateTime createdAt, String documentTitle, String documentDescription,
                           String documentType, String documentHash) {
        this.patientName = patientName;
        this.professionalName = professionalName;
        this.clinicName = clinicName;
        this.createdAt = createdAt;
        this.documentTitle = documentTitle;
        this.documentDescription = documentDescription;
        this.documentType = documentType;
        this.documentHash = documentHash;
    }

    // Getters and Setters

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getProfessionalName() {
        return professionalName;
    }

    public void setProfessionalName(String professionalName) {
        this.professionalName = professionalName;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getDocumentDescription() {
        return documentDescription;
    }

    public void setDocumentDescription(String documentDescription) {
        this.documentDescription = documentDescription;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentHash() {
        return documentHash;
    }

    public void setDocumentHash(String documentHash) {
        this.documentHash = documentHash;
    }

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
    }

    public String getProfessionalSpecialty() {
        return professionalSpecialty;
    }

    public void setProfessionalSpecialty(String professionalSpecialty) {
        this.professionalSpecialty = professionalSpecialty;
    }

    @Override
    public String toString() {
        return "DocumentMetadata{" +
                "patientName='" + patientName + '\'' +
                ", professionalName='" + professionalName + '\'' +
                ", clinicName='" + clinicName + '\'' +
                ", createdAt=" + createdAt +
                ", documentTitle='" + documentTitle + '\'' +
                ", documentType='" + documentType + '\'' +
                '}';
    }
}
