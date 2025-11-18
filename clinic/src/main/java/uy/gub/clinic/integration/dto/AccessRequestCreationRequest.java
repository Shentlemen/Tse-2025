package uy.gub.clinic.integration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear solicitud de acceso en HCEN
 * 
 * @author TSE 2025 Group 9
 */
public class AccessRequestCreationRequest {
    
    @NotBlank
    @Size(max = 100)
    private String professionalId;
    
    @Size(max = 255)
    private String professionalName;
    
    @Size(max = 100)
    private String specialty;
    
    @NotBlank
    @Size(max = 20)
    private String patientCi;
    
    private Long documentId;
    
    @Size(max = 50)
    private String documentType;
    
    @NotBlank
    @Size(max = 500)
    private String requestReason;
    
    private String urgency; // ROUTINE, URGENT, EMERGENCY
    
    public AccessRequestCreationRequest() {
        this.urgency = "ROUTINE";
    }
    
    // Getters y Setters
    
    public String getProfessionalId() {
        return professionalId;
    }
    
    public void setProfessionalId(String professionalId) {
        this.professionalId = professionalId;
    }
    
    public String getProfessionalName() {
        return professionalName;
    }
    
    public void setProfessionalName(String professionalName) {
        this.professionalName = professionalName;
    }
    
    public String getSpecialty() {
        return specialty;
    }
    
    public void setSpecialty(String specialty) {
        this.specialty = specialty;
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
    
    public String getDocumentType() {
        return documentType;
    }
    
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
    
    public String getRequestReason() {
        return requestReason;
    }
    
    public void setRequestReason(String requestReason) {
        this.requestReason = requestReason;
    }
    
    public String getUrgency() {
        return urgency;
    }
    
    public void setUrgency(String urgency) {
        this.urgency = urgency != null ? urgency.toUpperCase() : "ROUTINE";
    }
}

