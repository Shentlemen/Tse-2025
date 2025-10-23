package uy.gub.hcen.service.clinic.dto;

import jakarta.validation.constraints.*;

/**
 * Clinic Registration Request DTO
 * <p>
 * Data Transfer Object for clinic registration requests from HCEN administrators.
 * This DTO is used when admins register new clinics/health facilities in the system.
 * <p>
 * All fields are validated using Jakarta Bean Validation annotations to ensure
 * data integrity before processing.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
public class ClinicRegistrationRequest {

    /**
     * Official name of the clinic or healthcare facility
     */
    @NotBlank(message = "Clinic name is required")
    @Size(min = 3, max = 200, message = "Clinic name must be between 3 and 200 characters")
    private String clinicName;

    /**
     * Physical address of the clinic
     */
    @NotBlank(message = "Address is required")
    @Size(max = 300, message = "Address must not exceed 300 characters")
    private String address;

    /**
     * City where the clinic is located
     */
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    /**
     * Contact phone number
     * Expected format: Uruguayan phone numbers (landline or mobile)
     */
    @Pattern(
            regexp = "^(0[1-9][0-9]{6,7}|09[0-9]{7,8})$",
            message = "Invalid phone number format. Expected: 024123456 (landline) or 099123456 (mobile)"
    )
    private String phoneNumber;

    /**
     * Contact email address
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * URL of the clinic's peripheral node API endpoint
     * Format: https://clinic-name.hcen.uy/api
     */
    @NotBlank(message = "Peripheral node URL is required")
    @Pattern(
            regexp = "^https?://[\\w.-]+(:\\d+)?(/.*)?$",
            message = "Invalid URL format. Expected: https://clinic.example.com/api"
    )
    @Size(max = 500, message = "Peripheral node URL must not exceed 500 characters")
    private String peripheralNodeUrl;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor (required for JAX-RS deserialization)
     */
    public ClinicRegistrationRequest() {
    }

    /**
     * Full constructor for testing
     */
    public ClinicRegistrationRequest(String clinicName, String address, String city,
                                       String phoneNumber, String email, String peripheralNodeUrl) {
        this.clinicName = clinicName;
        this.address = address;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.peripheralNodeUrl = peripheralNodeUrl;
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPeripheralNodeUrl() {
        return peripheralNodeUrl;
    }

    public void setPeripheralNodeUrl(String peripheralNodeUrl) {
        this.peripheralNodeUrl = peripheralNodeUrl;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "ClinicRegistrationRequest{" +
                "clinicName='" + clinicName + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", peripheralNodeUrl='" + peripheralNodeUrl + '\'' +
                '}';
    }
}
