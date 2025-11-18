package uy.gub.hcen.policy.dto;

import jakarta.validation.constraints.*;
import uy.gub.hcen.policy.entity.MedicalSpecialty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Policy Create Request DTO
 *
 * Data Transfer Object for creating new patient access control policies.
 * Simplified model: grants access to professionals with a specific specialty from a specific clinic.
 *
 * Request Example:
 * <pre>
 * {
 *   "patientCi": "uy-ci-12345678",
 *   "clinicId": "clinic-001",
 *   "specialty": "CARDIOLOGIA",
 *   "documentId": null,
 *   "validFrom": "2025-10-21T00:00:00",
 *   "validUntil": "2026-10-21T00:00:00"
 * }
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 2.0
 * @since 2025-11-18
 */
public class PolicyCreateRequest implements Serializable {

    private static final long serialVersionUID = 2L;

    /**
     * Patient CI (Cedula de Identidad) - owner of the policy
     * Must be between 7-9 digits (Uruguay CI format)
     */
    @NotBlank(message = "Patient CI is required")
    @Pattern(regexp = "^uy-ci-\\d{7,9}$", message = "Patient CI must be in format uy-ci-XXXXXXXX")
    private String patientCi;

    /**
     * Clinic identifier
     * Must reference an existing clinic in the system
     */
    @NotBlank(message = "Clinic ID is required")
    @Size(max = 50, message = "Clinic ID must not exceed 50 characters")
    private String clinicId;

    /**
     * Medical specialty
     * Determines which professionals can access the documents
     */
    @NotNull(message = "Specialty is required")
    private MedicalSpecialty specialty;

    /**
     * Optional: Specific document ID
     * If null, policy applies to ALL documents
     * If set, policy applies only to that specific document
     */
    private Long documentId;

    /**
     * Optional: Start date for policy validity
     * If null, policy is valid from creation
     */
    private LocalDateTime validFrom;

    /**
     * Optional: End date for policy validity
     * If null, policy is valid indefinitely
     */
    private LocalDateTime validUntil;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor
     */
    public PolicyCreateRequest() {
    }

    /**
     * Constructor with required fields
     *
     * @param patientCi Patient CI
     * @param clinicId Clinic ID
     * @param specialty Medical specialty
     */
    public PolicyCreateRequest(String patientCi, String clinicId, MedicalSpecialty specialty) {
        this.patientCi = patientCi;
        this.clinicId = clinicId;
        this.specialty = specialty;
    }

    /**
     * Full constructor
     *
     * @param patientCi Patient CI
     * @param clinicId Clinic ID
     * @param specialty Medical specialty
     * @param documentId Document ID (optional)
     * @param validFrom Start date (optional)
     * @param validUntil End date (optional)
     */
    public PolicyCreateRequest(String patientCi, String clinicId, MedicalSpecialty specialty,
                               Long documentId, LocalDateTime validFrom, LocalDateTime validUntil) {
        this.patientCi = patientCi;
        this.clinicId = clinicId;
        this.specialty = specialty;
        this.documentId = documentId;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

    public String getPatientCi() {
        return patientCi;
    }

    public void setPatientCi(String patientCi) {
        this.patientCi = patientCi;
    }

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
    }

    public MedicalSpecialty getSpecialty() {
        return specialty;
    }

    public void setSpecialty(MedicalSpecialty specialty) {
        this.specialty = specialty;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolicyCreateRequest that = (PolicyCreateRequest) o;
        return Objects.equals(patientCi, that.patientCi) &&
                Objects.equals(clinicId, that.clinicId) &&
                specialty == that.specialty &&
                Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientCi, clinicId, specialty, documentId);
    }

    @Override
    public String toString() {
        return "PolicyCreateRequest{" +
                "patientCi='" + patientCi + '\'' +
                ", clinicId='" + clinicId + '\'' +
                ", specialty=" + specialty +
                ", documentId=" + documentId +
                ", validFrom=" + validFrom +
                ", validUntil=" + validUntil +
                '}';
    }
}
