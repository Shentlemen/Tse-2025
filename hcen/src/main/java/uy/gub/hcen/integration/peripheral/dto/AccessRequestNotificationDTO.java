package uy.gub.hcen.integration.peripheral.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Access Request Notification DTO
 *
 * Payload sent to peripheral nodes (clinics) when a patient approves or denies
 * an access request. The clinic can then notify the professional and take
 * appropriate action.
 *
 * Endpoint: POST {clinicPeripheralNodeUrl}/api/access-request-notifications
 *
 * Example JSON:
 * <pre>
 * {
 *   "requestId": 123,
 *   "professionalId": "prof-456",
 *   "patientCi": "12345678",
 *   "documentId": 789,
 *   "decision": "APPROVED",
 *   "decisionReason": "Patient approved access for emergency treatment",
 *   "decidedAt": "2025-11-20T14:30:00"
 * }
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-20
 */
public class AccessRequestNotificationDTO {

    /**
     * Access request ID from HCEN central
     */
    @JsonProperty("requestId")
    private Long requestId;

    /**
     * Professional ID who requested access
     */
    @JsonProperty("professionalId")
    private String professionalId;

    /**
     * Patient CI
     */
    @JsonProperty("patientCi")
    private String patientCi;

    /**
     * Document ID (null if general access request)
     */
    @JsonProperty("documentId")
    private Long documentId;

    /**
     * Patient's decision: APPROVED or DENIED
     */
    @JsonProperty("decision")
    private String decision;

    /**
     * Optional reason provided by patient
     */
    @JsonProperty("decisionReason")
    private String decisionReason;

    /**
     * Timestamp when patient made the decision
     */
    @JsonProperty("decidedAt")
    private LocalDateTime decidedAt;

    // Constructors

    public AccessRequestNotificationDTO() {
    }

    public AccessRequestNotificationDTO(Long requestId, String professionalId, String patientCi,
                                        Long documentId, String decision, String decisionReason,
                                        LocalDateTime decidedAt) {
        this.requestId = requestId;
        this.professionalId = professionalId;
        this.patientCi = patientCi;
        this.documentId = documentId;
        this.decision = decision;
        this.decisionReason = decisionReason;
        this.decidedAt = decidedAt;
    }

    // Getters and Setters

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
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

    @Override
    public String toString() {
        return "AccessRequestNotificationDTO{" +
                "requestId=" + requestId +
                ", professionalId='" + professionalId + '\'' +
                ", patientCi='" + patientCi + '\'' +
                ", documentId=" + documentId +
                ", decision='" + decision + '\'' +
                ", decidedAt=" + decidedAt +
                '}';
    }
}
