package uy.gub.hcen.policy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for creating access requests
 *
 * Used when healthcare professionals (via peripheral nodes) request access to patient documents.
 * This DTO contains all necessary information for creating an access request that requires
 * patient approval.
 *
 * Validation Rules:
 * - professionalId: Required, max 100 characters
 * - patientCi: Required, max 20 characters
 * - requestReason: Required, max 500 characters
 * - All other fields are optional
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class AccessRequestCreationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Requesting professional identifier (required)
     */
    @NotBlank(message = "Professional ID is required")
    @Size(max = 100, message = "Professional ID must not exceed 100 characters")
    private String professionalId;

    /**
     * Professional full name for patient display (optional)
     */
    @Size(max = 200, message = "Professional name must not exceed 200 characters")
    private String professionalName;

    /**
     * Professional specialty (optional)
     */
    @Size(max = 100, message = "Specialty must not exceed 100 characters")
    private String specialty;

    /**
     * Patient CI (Cédula de Identidad) - required
     */
    @NotBlank(message = "Patient CI is required")
    @Size(max = 20, message = "Patient CI must not exceed 20 characters")
    private String patientCi;

    /**
     * Document ID being requested (optional)
     * If null, this is a general access request
     */
    private Long documentId;

    /**
     * Document type for patient context (optional)
     */
    @Size(max = 50, message = "Document type must not exceed 50 characters")
    private String documentType;

    /**
     * Reason for access request (required)
     * Helps patient understand why access is needed
     */
    @NotBlank(message = "Request reason is required")
    @Size(max = 500, message = "Request reason must not exceed 500 characters")
    private String requestReason;

    /**
     * Request urgency level (optional, defaults to ROUTINE)
     * Valid values: ROUTINE, URGENT, EMERGENCY
     */
    private String urgency;

    // Constructors

    /**
     * Default constructor
     */
    public AccessRequestCreationDTO() {
    }

    /**
     * Full constructor
     */
    public AccessRequestCreationDTO(String professionalId, String professionalName, String specialty,
                                    String patientCi, Long documentId, String documentType,
                                    String requestReason, String urgency) {
        this.professionalId = professionalId;
        this.professionalName = professionalName;
        this.specialty = specialty;
        this.patientCi = patientCi;
        this.documentId = documentId;
        this.documentType = documentType;
        this.requestReason = requestReason;
        this.urgency = urgency;
    }

    // Builder pattern for easier construction

    /**
     * Creates a new builder for AccessRequestCreationDTO
     *
     * @return new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for fluent API construction
     */
    public static class Builder {
        private AccessRequestCreationDTO dto = new AccessRequestCreationDTO();

        public Builder professionalId(String professionalId) {
            dto.professionalId = professionalId;
            return this;
        }

        public Builder professionalName(String professionalName) {
            dto.professionalName = professionalName;
            return this;
        }

        public Builder specialty(String specialty) {
            dto.specialty = specialty;
            return this;
        }

        public Builder patientCi(String patientCi) {
            dto.patientCi = patientCi;
            return this;
        }

        public Builder documentId(Long documentId) {
            dto.documentId = documentId;
            return this;
        }

        public Builder documentType(String documentType) {
            dto.documentType = documentType;
            return this;
        }

        public Builder requestReason(String requestReason) {
            dto.requestReason = requestReason;
            return this;
        }

        public Builder urgency(String urgency) {
            dto.urgency = urgency;
            return this;
        }

        public AccessRequestCreationDTO build() {
            return dto;
        }
    }

    // Getters and Setters

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
        this.urgency = urgency;
    }

    @Override
    public String toString() {
        return "AccessRequestCreationDTO{" +
               "professionalId='" + professionalId + '\'' +
               ", professionalName='" + professionalName + '\'' +
               ", specialty='" + specialty + '\'' +
               ", patientCi='" + patientCi + '\'' +
               ", documentId=" + documentId +
               ", documentType='" + documentType + '\'' +
               ", urgency='" + urgency + '\'' +
               '}';
    }
}
