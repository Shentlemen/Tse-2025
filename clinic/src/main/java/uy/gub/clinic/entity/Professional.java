package uy.gub.clinic.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un profesional de salud
 */
@Entity
@Table(name = "professionals")
@NamedQueries({
    @NamedQuery(name = "Professional.findAll", query = "SELECT p FROM Professional p"),
    @NamedQuery(name = "Professional.findAllActive", query = "SELECT p FROM Professional p WHERE p.active = true"),
    @NamedQuery(name = "Professional.findByClinic", query = "SELECT p FROM Professional p WHERE p.clinic.id = :clinicId"),
    @NamedQuery(name = "Professional.findByClinicActive", query = "SELECT p FROM Professional p WHERE p.clinic.id = :clinicId AND p.active = true"),
    @NamedQuery(name = "Professional.findBySpecialty", query = "SELECT p FROM Professional p WHERE p.specialty.id = :specialtyId"),
    @NamedQuery(name = "Professional.findBySpecialtyActive", query = "SELECT p FROM Professional p WHERE p.specialty.id = :specialtyId AND p.active = true"),
    @NamedQuery(name = "Professional.findByLicense", query = "SELECT p FROM Professional p WHERE p.licenseNumber = :licenseNumber AND p.active = true"),
    @NamedQuery(name = "Professional.findByEmail", query = "SELECT p FROM Professional p WHERE p.email = :email AND p.active = true")
})
public class Professional {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;
    
    @Size(max = 255)
    @Column(name = "last_name")
    private String lastName;
    
    @Email
    @Size(max = 255)
    @Column(name = "email", unique = true)
    private String email;
    
    @Size(max = 100)
    @Column(name = "license_number", unique = true)
    private String licenseNumber;
    
    @Size(max = 20)
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "active", nullable = false)
    private Boolean active = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;
    
    @OneToMany(mappedBy = "professional", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClinicalDocument> documents = new ArrayList<>();
    
    @OneToMany(mappedBy = "professional", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccessRequest> accessRequests = new ArrayList<>();
    
    // Constructores
    public Professional() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Professional(String name, String lastName, String email, String licenseNumber) {
        this();
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.licenseNumber = licenseNumber;
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
    
    // Métodos de conveniencia
    public String getFullName() {
        return name + (lastName != null ? " " + lastName : "");
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getLicenseNumber() {
        return licenseNumber;
    }
    
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
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
    
    public List<ClinicalDocument> getDocuments() {
        return documents;
    }
    
    public void setDocuments(List<ClinicalDocument> documents) {
        this.documents = documents;
    }
    
    public List<AccessRequest> getAccessRequests() {
        return accessRequests;
    }
    
    public void setAccessRequests(List<AccessRequest> accessRequests) {
        this.accessRequests = accessRequests;
    }
    
    @Override
    public String toString() {
        return "Professional{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", specialty=" + (specialty != null ? specialty.getName() : "null") +
                '}';
    }
}
