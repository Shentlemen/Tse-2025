package uy.gub.clinic.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entidad que representa una solicitud de acceso a documentos clínicos
 */
@Entity
@Table(name = "access_requests")
@NamedQueries({
    @NamedQuery(name = "AccessRequest.findAll", query = "SELECT r FROM AccessRequest r"),
    @NamedQuery(name = "AccessRequest.findByPatient", query = "SELECT r FROM AccessRequest r WHERE r.patient.id = :patientId"),
    @NamedQuery(name = "AccessRequest.findByProfessional", query = "SELECT r FROM AccessRequest r WHERE r.professional.id = :professionalId"),
    @NamedQuery(name = "AccessRequest.findByStatus", query = "SELECT r FROM AccessRequest r WHERE r.status = :status")
})
public class AccessRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "status", nullable = false)
    private String status; // PENDING, APPROVED, REJECTED, EXPIRED
    
    @Size(max = 1000)
    @Column(name = "reason")
    private String reason;
    
    @Size(max = 1000)
    @Column(name = "response_notes")
    private String responseNotes;
    
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
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
    public AccessRequest() {
        this.requestedAt = LocalDateTime.now();
        this.status = "PENDING";
    }
    
    public AccessRequest(String reason) {
        this();
        this.reason = reason;
    }
    
    // Métodos de conveniencia
    public boolean isPending() {
        return "PENDING".equals(status);
    }
    
    public boolean isApproved() {
        return "APPROVED".equals(status);
    }
    
    public boolean isRejected() {
        return "REJECTED".equals(status);
    }
    
    public boolean isExpired() {
        return "EXPIRED".equals(status) || (expiresAt != null && LocalDateTime.now().isAfter(expiresAt));
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getResponseNotes() {
        return responseNotes;
    }
    
    public void setResponseNotes(String responseNotes) {
        this.responseNotes = responseNotes;
    }
    
    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
    
    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
    
    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }
    
    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
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
        return "AccessRequest{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", reason='" + reason + '\'' +
                ", requestedAt=" + requestedAt +
                '}';
    }
}
