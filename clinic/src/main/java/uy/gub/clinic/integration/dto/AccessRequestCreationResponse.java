package uy.gub.clinic.integration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * DTO de respuesta al crear solicitud de acceso en HCEN
 * 
 * @author TSE 2025 Group 9
 */
public class AccessRequestCreationResponse {
    
    private Long requestId;
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
    
    private String message;
    
    @JsonProperty("isNewRequest")
    private Boolean isNewRequest;
    
    public AccessRequestCreationResponse() {
    }
    
    // Getters y Setters
    
    public Long getRequestId() {
        return requestId;
    }
    
    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Boolean getIsNewRequest() {
        return isNewRequest;
    }
    
    public void setIsNewRequest(Boolean isNewRequest) {
        this.isNewRequest = isNewRequest;
    }
}

