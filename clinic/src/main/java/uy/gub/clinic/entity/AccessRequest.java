package uy.gub.clinic.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entidad que representa una solicitud de acceso a documentos externos
 * Estas solicitudes se crean cuando un profesional solicita acceso a documentos
 * de pacientes de otras clínicas a través del HCEN
 * 
 * @author TSE 2025 Group 9
 */
@Entity
@Table(name = "access_requests")
@NamedQueries({
    @NamedQuery(name = "AccessRequest.findAll", query = "SELECT ar FROM AccessRequest ar LEFT JOIN FETCH ar.patient"),
    @NamedQuery(name = "AccessRequest.findByProfessional", 
                query = "SELECT ar FROM AccessRequest ar LEFT JOIN FETCH ar.patient WHERE ar.professional.id = :professionalId ORDER BY ar.requestedAt DESC"),
    @NamedQuery(name = "AccessRequest.findByClinic", 
                query = "SELECT ar FROM AccessRequest ar LEFT JOIN FETCH ar.patient WHERE ar.clinic.id = :clinicId ORDER BY ar.requestedAt DESC"),
    @NamedQuery(name = "AccessRequest.findByStatus", 
                query = "SELECT ar FROM AccessRequest ar LEFT JOIN FETCH ar.patient WHERE ar.status = :status ORDER BY ar.requestedAt DESC")
})
public class AccessRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "patient_ci", nullable = false)
    private String patientCI; // Cédula del paciente
    
    @Column(name = "document_id")
    private Long documentId; // ID del documento específico (opcional)
    
    @Size(max = 500)
    @Column(name = "specialties", columnDefinition = "TEXT")
    private String specialties; // JSON array o comma-separated de especialidades
    
    @NotBlank
    @Size(max = 20)
    @Column(name = "status", nullable = false)
    private String status; // PENDING, APPROVED, DENIED, EXPIRED
    
    @NotBlank
    @Size(max = 500)
    @Column(name = "request_reason", nullable = false, columnDefinition = "TEXT")
    private String requestReason;
    
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    @Size(max = 50)
    @Column(name = "hcen_request_id")
    private String hcenRequestId; // ID retornado por HCEN
    
    @Size(max = 20)
    @Column(name = "urgency")
    private String urgency; // ROUTINE, URGENT, EMERGENCY
    
    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient; // Paciente local (opcional, puede ser null si es paciente externo)
    
    // Constructores
    public AccessRequest() {
        this.requestedAt = LocalDateTime.now();
        this.status = "PENDING";
        this.urgency = "ROUTINE";
        // Expiración por defecto: 48 horas
        this.expiresAt = LocalDateTime.now().plusHours(48);
    }
    
    // Métodos de callback JPA
    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusHours(48);
        }
        if (status == null) {
            status = "PENDING";
        }
    }
    
    // Getters y Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPatientCI() {
        return patientCI;
    }
    
    public void setPatientCI(String patientCI) {
        this.patientCI = patientCI;
    }
    
    public Long getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    
    public String getSpecialties() {
        return specialties;
    }
    
    public void setSpecialties(String specialties) {
        this.specialties = specialties;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }
    
    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
    
    public String getHcenRequestId() {
        return hcenRequestId;
    }
    
    public void setHcenRequestId(String hcenRequestId) {
        this.hcenRequestId = hcenRequestId;
    }
    
    public String getUrgency() {
        return urgency;
    }
    
    public void setUrgency(String urgency) {
        this.urgency = urgency;
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
    
    public Patient getPatient() {
        return patient;
    }
    
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    
    // Métodos de negocio
    
    public boolean isPending() {
        return "PENDING".equals(status) && (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }
    
    public boolean isExpired() {
        return "PENDING".equals(status) && expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
    
    public void approve() {
        if (!isPending()) {
            throw new IllegalStateException("Cannot approve request with status: " + status);
        }
        this.status = "APPROVED";
        this.respondedAt = LocalDateTime.now();
    }
    
    public void deny() {
        if (!isPending()) {
            throw new IllegalStateException("Cannot deny request with status: " + status);
        }
        this.status = "DENIED";
        this.respondedAt = LocalDateTime.now();
    }
    
    public void expire() {
        if ("PENDING".equals(status)) {
            this.status = "EXPIRED";
            this.respondedAt = LocalDateTime.now();
        }
    }
}
