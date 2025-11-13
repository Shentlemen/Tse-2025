package uy.gub.hcen.messaging.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Payload for user registration messages.
 * <p>
 * Contains all data necessary to register a new user in the INUS
 * (Índice Nacional de Usuarios de Salud) system.
 * <p>
 * Data Flow:
 * Peripheral Node → Message Queue → This Payload → InusService.registerUser()
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class UserRegistrationPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * User's Cédula de Identidad (national ID number).
     * <p>
     * Format: 7-8 digits, optionally with dots and dash (e.g., "1.234.567-8")
     * Normalized format: digits only (e.g., "12345678")
     */
    private String ci;

    /**
     * User's first name.
     */
    private String firstName;

    /**
     * User's last name.
     */
    private String lastName;

    /**
     * User's date of birth.
     * <p>
     * Used for age verification (18+ requirement for eligibility).
     * Format: YYYY-MM-DD
     */
    private LocalDate dateOfBirth;

    /**
     * User's email address (optional).
     * <p>
     * Used for account notifications and password recovery.
     */
    private String email;

    /**
     * User's phone number (optional).
     * <p>
     * Format: Uruguayan phone number (e.g., "099123456")
     */
    private String phoneNumber;

    /**
     * Identifier of the clinic registering the user.
     * <p>
     * Examples: "clinic-001", "hospital-montevideo", "provider-xyz"
     */
    private String clinicId;

    /**
     * Default constructor for JSON deserialization.
     */
    public UserRegistrationPayload() {
    }

    /**
     * Constructor with all fields.
     */
    public UserRegistrationPayload(String ci, String firstName, String lastName,
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRegistrationPayload that = (UserRegistrationPayload) o;
        return Objects.equals(ci, that.ci);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ci);
    }

    @Override
    public String toString() {
        return "UserRegistrationPayload{" +
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
