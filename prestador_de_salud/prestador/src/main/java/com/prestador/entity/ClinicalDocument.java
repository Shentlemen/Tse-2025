package com.prestador.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Clinical Document Entity for Health Provider (Prestador de Salud)
 *
 * Represents a clinical document created in the health provider system.
 * Stores complete document information including metadata and clinical findings.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@Entity
@Table(
    name = "clinical_documents",
    schema = "public",
    indexes = {
        @Index(name = "idx_clinical_documents_patient_id", columnList = "patient_id"),
        @Index(name = "idx_clinical_documents_clinic_id", columnList = "clinic_id"),
        @Index(name = "idx_clinical_documents_professional_id", columnList = "professional_id"),
        @Index(name = "idx_clinical_documents_date_of_visit", columnList = "date_of_visit"),
        @Index(name = "idx_clinical_documents_document_type", columnList = "document_type"),
        @Index(name = "idx_clinical_documents_rndc_id", columnList = "rndc_id")
    }
)
public class ClinicalDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description", length = 1000)
    private String description;

    @NotBlank(message = "Document type is required")
    @Size(max = 100, message = "Document type must not exceed 100 characters")
    @Column(name = "document_type", nullable = false, length = 100)
    private String documentType;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @NotNull(message = "Clinic ID is required")
    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;

    @NotNull(message = "Professional ID is required")
    @Column(name = "professional_id", nullable = false)
    private Long professionalId;

    @Column(name = "specialty_id")
    private Long specialtyId;

    @NotNull(message = "Date of visit is required")
    @Column(name = "date_of_visit", nullable = false)
    private LocalDate dateOfVisit;

    // File information
    @Size(max = 100, message = "File name must not exceed 100 characters")
    @Column(name = "file_name", length = 100)
    private String fileName;

    @Size(max = 500, message = "File path must not exceed 500 characters")
    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Size(max = 100, message = "MIME type must not exceed 100 characters")
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    // RNDC reference
    @Size(max = 100, message = "RNDC ID must not exceed 100 characters")
    @Column(name = "rndc_id", length = 100)
    private String rndcId;

    // Clinical information
    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint;

    @Column(name = "current_illness", columnDefinition = "TEXT")
    private String currentIllness;

    @Column(name = "vital_signs", columnDefinition = "TEXT")
    private String vitalSigns;

    @Column(name = "physical_examination", columnDefinition = "TEXT")
    private String physicalExamination;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "treatment", columnDefinition = "TEXT")
    private String treatment;

    @Column(name = "prescriptions", columnDefinition = "TEXT")
    private String prescriptions;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "next_appointment")
    private LocalDate nextAppointment;

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

    // Audit fields
    @NotNull(message = "Creation timestamp is required")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================================================================
    // Lifecycle Callbacks
    // ================================================================

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ================================================================
    // Constructors
    // ================================================================

    public ClinicalDocument() {
    }

    public ClinicalDocument(String title, String documentType, Long patientId,
                           Long clinicId, Long professionalId, LocalDate dateOfVisit) {
        this.title = title;
        this.documentType = documentType;
        this.patientId = patientId;
        this.clinicId = clinicId;
        this.professionalId = professionalId;
        this.dateOfVisit = dateOfVisit;
    }

    // ================================================================
    // Business Methods
    // ================================================================

    public boolean hasFile() {
        return filePath != null && !filePath.trim().isEmpty();
    }

    public boolean isRegisteredInRndc() {
        return rndcId != null && !rndcId.trim().isEmpty();
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getClinicId() {
        return clinicId;
    }

    public void setClinicId(Long clinicId) {
        this.clinicId = clinicId;
    }

    public Long getProfessionalId() {
        return professionalId;
    }

    public void setProfessionalId(Long professionalId) {
        this.professionalId = professionalId;
    }

    public Long getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(Long specialtyId) {
        this.specialtyId = specialtyId;
    }

    public LocalDate getDateOfVisit() {
        return dateOfVisit;
    }

    public void setDateOfVisit(LocalDate dateOfVisit) {
        this.dateOfVisit = dateOfVisit;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getRndcId() {
        return rndcId;
    }

    public void setRndcId(String rndcId) {
        this.rndcId = rndcId;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getCurrentIllness() {
        return currentIllness;
    }

    public void setCurrentIllness(String currentIllness) {
        this.currentIllness = currentIllness;
    }

    public String getVitalSigns() {
        return vitalSigns;
    }

    public void setVitalSigns(String vitalSigns) {
        this.vitalSigns = vitalSigns;
    }

    public String getPhysicalExamination() {
        return physicalExamination;
    }

    public void setPhysicalExamination(String physicalExamination) {
        this.physicalExamination = physicalExamination;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(String prescriptions) {
        this.prescriptions = prescriptions;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public LocalDate getNextAppointment() {
        return nextAppointment;
    }

    public void setNextAppointment(LocalDate nextAppointment) {
        this.nextAppointment = nextAppointment;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClinicalDocument that = (ClinicalDocument) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ClinicalDocument{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", documentType='" + documentType + '\'' +
                ", patientId=" + patientId +
                ", clinicId=" + clinicId +
                ", professionalId=" + professionalId +
                ", dateOfVisit=" + dateOfVisit +
                ", rndcId='" + rndcId + '\'' +
                '}';
    }
}
