package uy.gub.clinic.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un paciente en la clínica
 */
@Entity
@Table(name = "patients")
@NamedQueries({
    @NamedQuery(name = "Patient.findAll", query = "SELECT p FROM Patient p"),
    @NamedQuery(name = "Patient.findByClinic", query = "SELECT p FROM Patient p WHERE p.clinic.id = :clinicId"),
    @NamedQuery(name = "Patient.findByInusId", query = "SELECT p FROM Patient p WHERE p.inusId = :inusId"),
    @NamedQuery(name = "Patient.findByDocument", query = "SELECT p FROM Patient p WHERE p.documentNumber = :documentNumber"),
    @NamedQuery(name = "Patient.searchByName", query = "SELECT p FROM Patient p WHERE LOWER(p.name) LIKE LOWER(:name)")
})
public class Patient {
    
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
    
    @Size(max = 50)
    @Column(name = "inus_id")
    private String inusId; // ID en el INUS del HCEN central
    
    @Size(max = 50)
    @Column(name = "document_number")
    private String documentNumber;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Size(max = 10)
    @Column(name = "gender")
    private String gender;
    
    @Size(max = 20)
    @Column(name = "phone")
    private String phone;
    
    @Size(max = 255)
    @Column(name = "email")
    private String email;
    
    @Size(max = 500)
    @Column(name = "address")
    private String address;
    
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
    
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClinicalDocument> documents = new ArrayList<>();
    
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccessRequest> accessRequests = new ArrayList<>();
    
    // Constructores
    public Patient() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Patient(String name, String lastName) {
        this();
        this.name = name;
        this.lastName = lastName;
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
    
    public Integer getAge() {
        if (birthDate == null) {
            return null;
        }
        return LocalDate.now().getYear() - birthDate.getYear();
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
    
    public String getInusId() {
        return inusId;
    }
    
    public void setInusId(String inusId) {
        this.inusId = inusId;
    }
    
    public String getDocumentNumber() {
        return documentNumber;
    }
    
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }
    
    public LocalDate getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
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
        return "Patient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", documentNumber='" + documentNumber + '\'' +
                ", inusId='" + inusId + '\'' +
                '}';
    }
}
