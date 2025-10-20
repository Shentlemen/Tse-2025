package uy.gub.clinic.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NamedQuery(name = "ClinicalDocument.findByClinic", query = "SELECT d FROM ClinicalDocument d WHERE d.clinic.id = :clinicId")
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
    private String documentType; // CONSULTA, DIAGNOSTICO, TRATAMIENTO, etc.
    
    @Size(max = 500)
    @Column(name = "file_path")
    private String filePath;
    
    @Size(max = 100)
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Size(max = 100)
    @Column(name = "mime_type")
    private String mimeType;
    
    @Size(max = 100)
    @Column(name = "rndc_id")
    private String rndcId; // ID en el Registro Nacional de Documentos Clínicos
    
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
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
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
    
    @Override
    public String toString() {
        return "ClinicalDocument{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", documentType='" + documentType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
