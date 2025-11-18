package uy.gub.clinic.integration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * DTO para metadatos de documento externo enviados por HCEN
 * cuando se aprueba un access request
 * 
 * @author TSE 2025 Group 9
 */
public class DocumentMetadata {
    
    private Long documentId;
    
    @JsonProperty("documentType")
    private String documentType;
    
    @JsonProperty("documentLocator")
    private String documentLocator;
    
    @JsonProperty("documentHash")
    private String documentHash;
    
    @JsonProperty("clinicId")
    private String clinicId;
    
    @JsonProperty("clinicName")
    private String clinicName;
    
    private String specialty;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    // Getters y Setters
    
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
    
    public String getDocumentLocator() {
        return documentLocator;
    }
    
    public void setDocumentLocator(String documentLocator) {
        this.documentLocator = documentLocator;
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
    
    public String getClinicName() {
        return clinicName;
    }
    
    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }
    
    public String getSpecialty() {
        return specialty;
    }
    
    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

