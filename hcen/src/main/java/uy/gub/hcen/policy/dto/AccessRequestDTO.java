package uy.gub.hcen.policy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import uy.gub.hcen.policy.entity.AccessRequest;

import java.time.LocalDateTime;

/**
 * Access Request DTO
 *
 * Data Transfer Object for access requests requiring patient approval.
 * Used in REST API responses to display pending access requests to patients.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-03
 */
public class AccessRequestDTO {

    private Long id;
    private String professionalId;
    private String professionalName;
    private String clinicName;
    private String documentType;
    private Long documentId;
    private String requestReason;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    private String status;
    private String patientResponse;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime respondedAt;

    // Constructors

    public AccessRequestDTO() {
    }

    // Factory method to create DTO from entity
    public static AccessRequestDTO fromEntity(AccessRequest request) {
        AccessRequestDTO dto = new AccessRequestDTO();
        dto.setId(request.getId());
        dto.setProfessionalId(request.getProfessionalId());
        dto.setDocumentId(request.getDocumentId());
        dto.setRequestReason(request.getRequestReason());
        dto.setRequestedAt(request.getRequestedAt());
        dto.setExpiresAt(request.getExpiresAt());
        dto.setStatus(request.getStatus().name());
        dto.setPatientResponse(request.getPatientResponse());
        dto.setRespondedAt(request.getRespondedAt());

        // These will be populated by the service layer
        dto.setProfessionalName("Professional " + request.getProfessionalId());
        dto.setClinicName("Unknown Clinic");
        dto.setDocumentType("GENERAL_ACCESS");

        return dto;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getRequestReason() {
        return requestReason;
    }

    public void setRequestReason(String requestReason) {
        this.requestReason = requestReason;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPatientResponse() {
        return patientResponse;
    }

    public void setPatientResponse(String patientResponse) {
        this.patientResponse = patientResponse;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}
