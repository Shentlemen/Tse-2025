package uy.gub.hcen.inus.dto;

import uy.gub.hcen.inus.entity.InusUser;
import uy.gub.hcen.inus.entity.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User Response DTO
 * <p>
 * Immutable Data Transfer Object representing an INUS user for API responses.
 * This DTO is returned by all INUS REST endpoints that return user data.
 * <p>
 * This DTO includes all user fields except sensitive internal data.
 * Timestamps are included to support client-side caching and optimistic locking.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 */
public class UserResponse {

    private final String ci;
    private final String inusId;
    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final String email;
    private final String phoneNumber;
    private final UserStatus status;
    private final Boolean ageVerified;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Constructor that converts from InusUser entity
     *
     * @param user InusUser entity
     */
    public UserResponse(InusUser user) {
        this.ci = user.getCi();
        this.inusId = user.getInusId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.dateOfBirth = user.getDateOfBirth();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.status = user.getStatus();
        this.ageVerified = user.getAgeVerified();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    /**
     * Full constructor for manual creation (mainly for testing)
     */
    public UserResponse(String ci, String inusId, String firstName, String lastName,
                        LocalDate dateOfBirth, String email, String phoneNumber,
                        UserStatus status, Boolean ageVerified,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.ci = ci;
        this.inusId = inusId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.ageVerified = ageVerified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ================================================================
    // Getters Only (Immutable DTO)
    // ================================================================

    public String getCi() {
        return ci;
    }

    public String getInusId() {
        return inusId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Boolean getAgeVerified() {
        return ageVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ================================================================
    // Derived Properties
    // ================================================================

    /**
     * Get user's full name
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

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "UserResponse{" +
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
