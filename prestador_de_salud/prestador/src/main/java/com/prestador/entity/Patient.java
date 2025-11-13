package com.prestador.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Patient Entity for Health Provider (Prestador de Salud)
 *
 * Represents a patient registered in the health provider system.
 * This entity matches the HCEN schema for patients.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@Entity
@Table(
    name = "patients",
    schema = "public",
    indexes = {
        @Index(name = "idx_patients_document_number", columnList = "document_number"),
        @Index(name = "idx_patients_inus_id", columnList = "inus_id"),
        @Index(name = "idx_patients_clinic_id", columnList = "clinic_id"),
        @Index(name = "idx_patients_email", columnList = "email"),
        @Index(name = "idx_patients_active", columnList = "active")
    }
)
public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Size(max = 255, message = "Last name must not exceed 255 characters")
    @Column(name = "last_name", length = 255)
    private String lastName;

    @Size(max = 50, message = "Document number must not exceed 50 characters")
    @Column(name = "document_number", length = 50)
    private String documentNumber;

    @Size(max = 50, message = "INUS ID must not exceed 50 characters")
    @Column(name = "inus_id", length = 50)
    private String inusId;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Size(max = 10, message = "Gender must not exceed 10 characters")
    @Column(name = "gender", length = 10)
    private String gender;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(name = "email", length = 255)
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Column(name = "address", length = 500)
    private String address;

    @NotNull(message = "Active status is required")
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @NotNull(message = "Clinic ID is required")
    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;

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
        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ================================================================
    // Constructors
    // ================================================================

    public Patient() {
    }

    public Patient(String name, String lastName, String documentNumber, Long clinicId) {
        this.name = name;
        this.lastName = lastName;
        this.documentNumber = documentNumber;
        this.clinicId = clinicId;
        this.active = true;
    }

    // ================================================================
    // Business Methods
    // ================================================================

    public String getFullName() {
        if (lastName != null && !lastName.trim().isEmpty()) {
            return name + " " + lastName;
        }
        return name;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(this.active);
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

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getInusId() {
        return inusId;
    }

    public void setInusId(String inusId) {
        this.inusId = inusId;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Local birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public Long getClinicId() {
        return clinicId;
    }

    public void setClinicId(Long clinicId) {
        this.clinicId = clinicId;
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
        Patient patient = (Patient) o;
        return Objects.equals(id, patient.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", documentNumber='" + documentNumber + '\'' +
                ", inusId='" + inusId + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                ", clinicId=" + clinicId +
                '}';
    }
}
