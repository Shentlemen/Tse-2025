package uy.gub.clinic.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un documento clínico
 */
@Entity
@Table(name = "clinical_documents")
@NamedQueries({
    @NamedQuery(name = "ClinicalDocument.findAll", query = "SELECT d FROM ClinicalDocument d"),
    @NamedQuery(name = "ClinicalDocument.findByPatient", query = "SELECT d FROM ClinicalDocument d WHERE d.patient.id = :patientId"),
    @NamedQuery(name = "ClinicalDocument.findByProfessional", query = "SELECT d FROM ClinicalDocument d WHERE d.professional.id = :professionalId"),
    @NamedQuery(name = "ClinicalDocument.findByClinic", query = "SELECT d FROM ClinicalDocument d WHERE d.clinic.id = :clinicId ORDER BY d.dateOfVisit DESC, d.createdAt DESC"),
    @NamedQuery(name = "ClinicalDocument.findByClinicAndSpecialty", query = "SELECT d FROM ClinicalDocument d WHERE d.clinic.id = :clinicId AND d.specialty.id = :specialtyId ORDER BY d.dateOfVisit DESC, d.createdAt DESC")
})
public class ClinicalDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "title", nullable = false)
    private String title;
    
    @Size(max = 1000)
    @Column(name = "description")
    private String description;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "document_type", nullable = false)
    private String documentType; // CONSULTA, DIAGNOSTICO, TRATAMIENTO, EVOLUCION, etc.
    
    @Column(name = "date_of_visit", nullable = false)
    private LocalDate dateOfVisit;
    
    // Campos del formulario médico
    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint; // Motivo de consulta
    
    @Column(name = "current_illness", columnDefinition = "TEXT")
    private String currentIllness; // Historia de la enfermedad actual
    
    @Column(name = "vital_signs", columnDefinition = "TEXT")
    private String vitalSigns; // JSON: {pressure, temperature, pulse, respiratoryRate, o2Saturation, weight, height, bmi}
    
    @Column(name = "physical_examination", columnDefinition = "TEXT")
    private String physicalExamination; // Examen físico
    
    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis; // Diagnóstico
    
    @Column(name = "treatment", columnDefinition = "TEXT")
    private String treatment; // Tratamiento/Indicaciones
    
    @Column(name = "prescriptions", columnDefinition = "TEXT")
    private String prescriptions; // JSON array: [{medication, dosage, frequency, duration}]
    
    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations; // Observaciones adicionales
    
    @Column(name = "next_appointment")
    private LocalDate nextAppointment; // Próxima cita
    
    // Archivos adjuntos (JSON array para múltiples archivos)
    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments; // JSON array: [{fileName, filePath, fileSize, mimeType}]
    
    @Size(max = 100)
    @Column(name = "rndc_id")
    private String rndcId; // ID en el Registro Nacional de Documentos Clínicos
    
    @Column(name = "is_external", nullable = false)
    private Boolean isExternal = false; // Indica si es un documento descargado de otra clínica
    
    @Size(max = 100)
    @Column(name = "source_clinic_id")
    private String sourceClinicId; // ID de la clínica origen (si es documento externo)
    
    @Size(max = 255)
    @Column(name = "external_clinic_name")
    private String externalClinicName; // Nombre de la clínica origen (si es documento externo)
    
    @Column(name = "external_document_locator", columnDefinition = "TEXT")
    private String externalDocumentLocator; // URL original del documento externo
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;
    
    // Constructores
    public ClinicalDocument() {
        this.createdAt = LocalDateTime.now();
    }
    
    public ClinicalDocument(String title, String documentType) {
        this();
        this.title = title;
        this.documentType = documentType;
    }
    
    // Métodos de callback JPA
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters y Setters
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
    
    public String getRndcId() {
        return rndcId;
    }
    
    public void setRndcId(String rndcId) {
        this.rndcId = rndcId;
    }
    
    public Boolean getIsExternal() {
        return isExternal != null ? isExternal : false;
    }
    
    public void setIsExternal(Boolean isExternal) {
        this.isExternal = isExternal != null ? isExternal : false;
    }
    
    public String getSourceClinicId() {
        return sourceClinicId;
    }
    
    public void setSourceClinicId(String sourceClinicId) {
        this.sourceClinicId = sourceClinicId;
    }
    
    public String getExternalClinicName() {
        return externalClinicName;
    }
    
    public void setExternalClinicName(String externalClinicName) {
        this.externalClinicName = externalClinicName;
    }
    
    public String getExternalDocumentLocator() {
        return externalDocumentLocator;
    }
    
    public void setExternalDocumentLocator(String externalDocumentLocator) {
        this.externalDocumentLocator = externalDocumentLocator;
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
    
    public Patient getPatient() {
        return patient;
    }
    
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    
    public Professional getProfessional() {
        return professional;
    }
    
    public void setProfessional(Professional professional) {
        this.professional = professional;
    }
    
    public Clinic getClinic() {
        return clinic;
    }
    
    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }
    
    public Specialty getSpecialty() {
        return specialty;
    }
    
    public void setSpecialty(Specialty specialty) {
        this.specialty = specialty;
    }
    
    // Getters y Setters para nuevos campos
    public LocalDate getDateOfVisit() {
        return dateOfVisit;
    }
    
    public void setDateOfVisit(LocalDate dateOfVisit) {
        this.dateOfVisit = dateOfVisit;
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
    
    @Override
    public String toString() {
        return "ClinicalDocument{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", documentType='" + documentType + '\'' +
                ", dateOfVisit=" + dateOfVisit +
                ", createdAt=" + createdAt +
                '}';
    }
}
