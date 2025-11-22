package uy.gub.clinic.integration.dto.hcen;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class HcenDocumentMetadataDTO {

    private String patientName;
    private String professionalName;
    private String clinicName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private String documentTitle;
    private String documentDescription;
    private String documentType;
    private String documentHash;
    private String clinicId;
    private String professionalSpecialty;

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
}

