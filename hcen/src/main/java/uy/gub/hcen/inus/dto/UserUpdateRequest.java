package uy.gub.hcen.inus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * User Update Request DTO
 * <p>
 * Data Transfer Object for updating existing user profile information.
 * Only mutable fields can be updated through this DTO.
 * <p>
 * Immutable fields (cannot be updated):
 * - ci (Cédula de Identidad)
 * - inusId (cross-clinic identifier)
 * - dateOfBirth
 * - status (use separate endpoints for status changes)
 * - ageVerified
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 */
public class UserUpdateRequest {

    /**
     * Updated first name
     */
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    /**
     * Updated last name
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    /**
     * Updated email address (optional)
     */
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * Updated phone number (optional)
     * Expected format: 099123456 (Uruguayan mobile phone format)
     */
    @Pattern(
            regexp = "^(09[0-9]{7,8})?$",
            message = "Invalid phone number format. Expected format: 099123456"
    )
    private String phoneNumber;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor (required for JAX-RS deserialization)
     */
    public UserUpdateRequest() {
    }

    /**
     * Full constructor for testing
     */
    public UserUpdateRequest(String firstName, String lastName, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

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

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "UserUpdateRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
