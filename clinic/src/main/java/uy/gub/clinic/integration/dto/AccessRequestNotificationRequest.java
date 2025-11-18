package uy.gub.clinic.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO para notificación del HCEN cuando se aprueba/deniega un access request
 * 
 * @author TSE 2025 Group 9
 */
public class AccessRequestNotificationRequest {
    
    @JsonProperty("hcenRequestId")
    private Long hcenRequestId;
    
    private String status; // APPROVED, DENIED
    
    @JsonProperty("patientCi")
    private String patientCi;
    
    @JsonProperty("documentMetadata")
    private List<DocumentMetadata> documentMetadata;
    
    // Getters y Setters
    
    public Long getHcenRequestId() {
        return hcenRequestId;
    }
    
    public void setHcenRequestId(Long hcenRequestId) {
        this.hcenRequestId = hcenRequestId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPatientCi() {
        return patientCi;
    }
    
    public void setPatientCi(String patientCi) {
        this.patientCi = patientCi;
    }
    
    public List<DocumentMetadata> getDocumentMetadata() {
        return documentMetadata;
    }
    
    public void setDocumentMetadata(List<DocumentMetadata> documentMetadata) {
        this.documentMetadata = documentMetadata;
    }
}

