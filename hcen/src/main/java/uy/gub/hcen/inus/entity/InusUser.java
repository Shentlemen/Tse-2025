package uy.gub.hcen.inus.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * INUS User Entity
 *
 * Represents a user in the INUS (Índice Nacional de Usuarios de Salud) -
 * National Health System User Index. This entity stores the central registry
 * of all health system users with cross-clinic identification.
 *
 * Key Responsibilities:
 * - Central storage of user identification (CI - Cédula de Identidad)
 * - Personal data (name, date of birth, health user status)
 * - Cross-clinic unique identifier (inusId)
 * - Integration with PDI for identity validation
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@Entity
@Table(name = "inus_users", schema = "inus",
    indexes = {
        @Index(name = "idx_inus_users_ci", columnList = "ci"),
        @Index(name = "idx_inus_users_inus_id", columnList = "inus_id"),
        @Index(name = "idx_inus_users_status", columnList = "status"),
        @Index(name = "idx_inus_users_email", columnList = "email"),
        @Index(name = "idx_inus_users_created_at", columnList = "created_at")
    }
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InusUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary identification: Cédula de Identidad (national ID)
     */
    @Id
    @Column(name = "ci", length = 20, nullable = false)
    private String ci;

    /**
     * Unique cross-clinic identifier (UUID-based)
     * This is the global identifier for a user across all healthcare providers
     */
    @Column(name = "inus_id", length = 50, nullable = false, unique = true)
    private String inusId;

    /**
     * User's first name
     */
    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    /**
     * User's last name
     */
    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;

    /**
     * User's date of birth
     */
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    /**
     * User's email address (optional)
     */
    @Column(name = "email", length = 255)
    private String email;

    /**
     * User's phone number (optional)
     */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /**
     * User status (ACTIVE, INACTIVE, SUSPENDED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private UserStatus status;

    /**
     * Flag indicating if age >= 18 was verified via PDI
     * Required for eligibility in the health system
     */
    @Column(name = "age_verified", nullable = false)
    private Boolean ageVerified = false;

    /**
     * Timestamp of user registration
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of last update (auto-updated via @PreUpdate)
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ================================================================
    // Lifecycle Callbacks
    // ================================================================

    /**
     * Pre-persist callback to set creation timestamp
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // Set default status if not set
        if (this.status == null) {
            this.status = UserStatus.ACTIVE;
        }

        // Set default age verification if not set
        if (this.ageVerified == null) {
            this.ageVerified = false;
        }
    }

    /**
     * Pre-update callback to update the modification timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor (required by JPA)
     */
    public InusUser() {
    }

    /**
     * Full constructor for creating a new INUS user
     *
     * @param ci Cédula de Identidad (national ID)
     * @param inusId Unique cross-clinic identifier
     * @param firstName User's first name
     * @param lastName User's last name
     * @param dateOfBirth User's date of birth
     * @param email User's email (optional)
     * @param phoneNumber User's phone number (optional)
     * @param status User status
     * @param ageVerified Whether age has been verified via PDI
     */
    public InusUser(String ci, String inusId, String firstName, String lastName,
                    LocalDate dateOfBirth, String email, String phoneNumber,
                    UserStatus status, Boolean ageVerified) {
        this.ci = ci;
        this.inusId = inusId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.ageVerified = ageVerified;
    }

    /**
     * Minimal constructor for user registration
     *
     * @param ci Cédula de Identidad (national ID)
     * @param inusId Unique cross-clinic identifier
     * @param firstName User's first name
     * @param lastName User's last name
     * @param dateOfBirth User's date of birth
     */
    public InusUser(String ci, String inusId, String firstName, String lastName, LocalDate dateOfBirth) {
        this.ci = ci;
        this.inusId = inusId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.status = UserStatus.ACTIVE;
        this.ageVerified = false;
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

    public String getCi() {
        return ci;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }

    public String getInusId() {
        return inusId;
    }

    public void setInusId(String inusId) {
        this.inusId = inusId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Boolean getAgeVerified() {
        return ageVerified;
    }

    public void setAgeVerified(Boolean ageVerified) {
        this.ageVerified = ageVerified;
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
    // Business Methods
    // ================================================================

    /**
     * Get full name of the user
     *
     * @return Concatenated first name and last name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if user is active
     *
     * @return true if status is ACTIVE
     */
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    /**
     * Check if user is suspended
     *
     * @return true if status is SUSPENDED
     */
    public boolean isSuspended() {
        return UserStatus.SUSPENDED.equals(this.status);
    }

    /**
     * Activate the user
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Deactivate the user
     */
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    /**
     * Suspend the user
     */
    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InusUser inusUser = (InusUser) o;
        return Objects.equals(ci, inusUser.ci);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ci);
    }

    @Override
    public String toString() {
        return "InusUser{" +
                "ci='" + ci + '\'' +
                ", inusId='" + inusId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", status=" + status +
                ", ageVerified=" + ageVerified +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
