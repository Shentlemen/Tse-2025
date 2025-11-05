package uy.gub.hcen.inus.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * User Registration Request DTO
 * <p>
 * Data Transfer Object for user registration requests from peripheral nodes.
 * This DTO is used when clinics and health providers register new patients
 * in the INUS (National User Index) system.
 * <p>
 * All fields are validated using Jakarta Bean Validation annotations to ensure
 * data integrity before processing.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 */
public class UserRegistrationRequest {

    /**
     * Cédula de Identidad (Uruguayan National ID)
     * <p>
     * Accepts formats:
     * - 12345678 (8 digits)
     * - 1234567-8 (with verification digit)
     * - 1.234.567-8 (with dots and dash)
     */
    @NotBlank(message = "CI is required")
    @Pattern(
            regexp = "^\\d{1,2}(\\.?\\d{3}){2}\\-?\\d$",
            message = "Invalid CI format. Expected format: 1234567-8 or 1.234.567-8"
    )
    private String ci;

    /**
     * User's first name
     */
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    /**
     * User's last name
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    /**
     * User's date of birth
     * Must be in the past
     */
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    /**
     * User's email address (optional)
     */
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * User's phone number (optional)
     * Expected format: 099123456 (Uruguayan mobile phone format)
     */
    @Pattern(
            regexp = "^(09[0-9]{7,8})?$",
            message = "Invalid phone number format. Expected format: 099123456"
    )
    private String phoneNumber;

    /**
     * ID of the clinic registering the user (optional)
     * <p>
     * This field is required when peripheral nodes (clinics) register users.
     * For admin registration or patient self-registration, this can be null.
     */
    @Size(max = 50, message = "Clinic ID must not exceed 50 characters")
    private String clinicId;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor (required for JAX-RS deserialization)
     */
    public UserRegistrationRequest() {
    }

    /**
     * Full constructor for testing
     */
    public UserRegistrationRequest(String ci, String firstName, String lastName,
                                    LocalDate dateOfBirth, String email,
                                    String phoneNumber, String clinicId) {
        this.ci = ci;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.clinicId = clinicId;
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

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "UserRegistrationRequest{" +
                "ci='" + ci + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", clinicId='" + clinicId + '\'' +
                '}';
    }
}
