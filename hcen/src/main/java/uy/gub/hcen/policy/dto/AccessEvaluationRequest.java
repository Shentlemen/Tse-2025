package uy.gub.hcen.policy.dto;

import jakarta.validation.constraints.*;
import uy.gub.hcen.rndc.entity.DocumentType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Access Evaluation Request DTO
 * <p>
 * Data Transfer Object for evaluating access control policies.
 * Used by the /api/policies/evaluate endpoint to determine if a healthcare
 * professional should be granted access to a patient's clinical document.
 * <p>
 * Request Example:
 * <pre>
 * {
 *   "professionalId": "prof-123",
 *   "specialties": ["CARDIOLOGY", "GENERAL_MEDICINE"],
 *   "clinicId": "clinic-001",
 *   "patientCi": "12345678",
 *   "documentId": 456,
 *   "documentType": "LAB_RESULT",
 *   "requestReason": "Reviewing cardiac panel results"
 * }
 * </pre>
 * <p>
 * This DTO is converted to AccessRequest (PolicyEngine DTO) for evaluation.
 * The evaluation result determines if access is PERMITTED, DENIED, or PENDING.
 * <p>
 * Use Cases:
 * <ul>
 *   <li>Document access authorization checks</li>
 *   <li>Pre-flight authorization for document retrieval</li>
 *   <li>Access control testing and validation</li>
 *   <li>Audit trail generation for access attempts</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
public class AccessEvaluationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier of the healthcare professional requesting access
     */
    @NotBlank(message = "Professional ID is required")
    @Size(max = 100, message = "Professional ID must not exceed 100 characters")
    private String professionalId;

    /**
     * List of medical specialties the professional holds
     * Must contain at least one specialty
     */
    @NotNull(message = "Specialties list is required")
    @Size(min = 1, message = "At least one specialty is required")
    private List<String> specialties;

    /**
     * Identifier of the clinic/healthcare facility
     */
    @NotBlank(message = "Clinic ID is required")
    @Size(max = 100, message = "Clinic ID must not exceed 100 characters")
    private String clinicId;

    /**
     * Patient's CI (Cédula de Identidad) - document owner
     */
    @NotBlank(message = "Patient CI is required")
    @Pattern(regexp = "^\\d{7,9}$", message = "Patient CI must be 7-9 digits")
    private String patientCi;

    /**
     * Optional: Specific document ID being accessed
     * If null, this is a general access check
     */
    private Long documentId;

    /**
     * Type of document being accessed
     */
    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    /**
     * Optional: Justification or reason for accessing the document
     * Required for emergency access scenarios
     */
    @Size(max = 500, message = "Request reason must not exceed 500 characters")
    private String requestReason;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor
     */
    public AccessEvaluationRequest() {
        this.specialties = new ArrayList<>();
    }

    /**
     * Full constructor
     *
     * @param professionalId Professional's unique identifier
     * @param specialties List of professional's specialties
     * @param clinicId Clinic identifier
     * @param patientCi Patient's CI
     * @param documentId Document ID (optional)
     * @param documentType Document type
     * @param requestReason Access justification (optional)
     */
    public AccessEvaluationRequest(String professionalId, List<String> specialties, String clinicId,
                                   String patientCi, Long documentId, DocumentType documentType,
                                   String requestReason) {
        this.professionalId = professionalId;
        this.specialties = specialties != null ? new ArrayList<>(specialties) : new ArrayList<>();
        this.clinicId = clinicId;
        this.patientCi = patientCi;
        this.documentId = documentId;
        this.documentType = documentType;
        this.requestReason = requestReason;
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

    public String getProfessionalId() {
        return professionalId;
    }

    public void setProfessionalId(String professionalId) {
        this.professionalId = professionalId;
    }

    public List<String> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<String> specialties) {
        this.specialties = specialties != null ? new ArrayList<>(specialties) : new ArrayList<>();
    }

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
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

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getRequestReason() {
        return requestReason;
    }

    public void setRequestReason(String requestReason) {
        this.requestReason = requestReason;
    }

    // ================================================================
    // Utility Methods
    // ================================================================

    /**
     * Checks if this professional has a specific specialty
     *
     * @param specialty Specialty to check
     * @return true if professional has this specialty
     */
    public boolean hasSpecialty(String specialty) {
        if (specialties == null || specialty == null) {
            return false;
        }
        return specialties.stream()
                .anyMatch(s -> s.equalsIgnoreCase(specialty));
    }

    /**
     * Adds a specialty to the list
     *
     * @param specialty Specialty to add
     */
    public void addSpecialty(String specialty) {
        if (specialty != null && !specialty.trim().isEmpty()) {
            if (this.specialties == null) {
                this.specialties = new ArrayList<>();
            }
            if (!this.specialties.contains(specialty)) {
                this.specialties.add(specialty);
            }
        }
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessEvaluationRequest that = (AccessEvaluationRequest) o;
        return Objects.equals(professionalId, that.professionalId) &&
                Objects.equals(patientCi, that.patientCi) &&
                Objects.equals(documentId, that.documentId) &&
                documentType == that.documentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(professionalId, patientCi, documentId, documentType);
    }

    @Override
    public String toString() {
        return "AccessEvaluationRequest{" +
                "professionalId='" + professionalId + '\'' +
                ", specialties=" + specialties +
                ", clinicId='" + clinicId + '\'' +
                ", patientCi='" + patientCi + '\'' +
                ", documentId=" + documentId +
                ", documentType=" + documentType +
                ", requestReason='" + requestReason + '\'' +
                '}';
    }
}
