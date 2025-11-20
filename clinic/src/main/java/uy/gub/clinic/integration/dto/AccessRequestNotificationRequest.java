package uy.gub.clinic.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para notificación del HCEN cuando se aprueba/deniega un access request
 *
 * Matches the format sent from HCEN Central's AccessRequestNotificationDTO
 *
 * @author TSE 2025 Group 9
 */
public class AccessRequestNotificationRequest {

    @JsonProperty("requestId")
    private Long requestId; // Changed from hcenRequestId to match HCEN's format

    @JsonProperty("professionalId")
    private String professionalId;

    @JsonProperty("patientCi")
    private String patientCi;

    @JsonProperty("documentId")
    private Long documentId;

    @JsonProperty("decision")
    private String decision; // APPROVED or DENIED (renamed from status)

    @JsonProperty("decisionReason")
    private String decisionReason;

    @JsonProperty("decidedAt")
    private LocalDateTime decidedAt;

    @JsonProperty("documentMetadata")
    private List<DocumentMetadata> documentMetadata;
    
    // Getters and Setters

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    // Legacy getter for backward compatibility
    @Deprecated
    public Long getHcenRequestId() {
        return requestId;
    }

    public String getProfessionalId() {
        return professionalId;
    }

    public void setProfessionalId(String professionalId) {
        this.professionalId = professionalId;
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

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    // Legacy getter for backward compatibility
    @Deprecated
    public String getStatus() {
        return decision;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    public List<DocumentMetadata> getDocumentMetadata() {
        return documentMetadata;
    }

    public void setDocumentMetadata(List<DocumentMetadata> documentMetadata) {
        this.documentMetadata = documentMetadata;
    }
}

