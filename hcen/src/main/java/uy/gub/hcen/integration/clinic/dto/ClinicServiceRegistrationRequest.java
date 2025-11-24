package uy.gub.hcen.integration.clinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Clinic Service Registration Request DTO
 * <p>
 * Data Transfer Object for registering a new clinic in the Clinic Service peripheral component.
 * This DTO matches the API specification provided by the Clinic Service:
 * <pre>
 * POST https://clinics.hcen.uy/api/clinics
 * </pre>
 * <p>
 * Request Body:
 * <pre>
 * {
 *   "code": "clinic-550e8400-e29b-41d4-a716-446655440000",
 *   "name": "Clínica San José",
 *   "description": "Clínica privada de atención médica integral",
 *   "address": "Av. 18 de Julio 1234",
 *   "phone": "024123456",
 *   "email": "contacto@clinicasanjose.com.uy",
 *   "hcen_endpoint": "https://hcen.uy/api",
 *   "active": true
 * }
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class ClinicServiceRegistrationRequest {

    /**
     * Unique clinic identifier (clinicId from HCEN central)
     * Format: clinic-{uuid}
     */
    @JsonProperty("code")
    private String code;

    /**
     * Official name of the clinic or healthcare facility
     */
    @JsonProperty("name")
    private String name;

    /**
     * Description of the clinic (optional)
     */
    @JsonProperty("description")
    private String description;

    /**
     * Physical address of the clinic
     */
    @JsonProperty("address")
    private String address;

    /**
     * Contact phone number
     */
    @JsonProperty("phone")
    private String phone;

    /**
     * Contact email address
     */
    @JsonProperty("email")
    private String email;

    /**
     * URL of the HCEN central API endpoint
     * Same for all clinics: https://hcen.uy/api
     */
    @JsonProperty("hcen_endpoint")
    private String hcenEndpoint;

    /**
     * Active status (default: true)
     */
    @JsonProperty("active")
    private Boolean active;

    @NotBlank(message = "API key is required")
    @Size(min = 32, message = "API key must be at least 32 characters")
    @JsonProperty("api_key")
    private String apiKey;

    /**
     * Admin password for clinic user creation (transient - not stored in HCEN central)
     * This password is generated during clinic registration and sent to the clinic service
     * for creating the initial admin account. The clinic service must hash and store it securely.
     */
    @JsonProperty("password")
    private String password;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor (required for Jackson deserialization)
     */
    public ClinicServiceRegistrationRequest() {
        this.active = true; // Default to active
    }

    /**
     * Full constructor
     *
     * @param code         Unique clinic identifier
     * @param name         Clinic name
     * @param description  Clinic description
     * @param address      Physical address
     * @param phone        Contact phone
     * @param email        Contact email
     * @param hcenEndpoint HCEN central API URL
     * @param active       Active status
     * @param apiKey       API key for clinic authentication
     * @param password     Admin password for clinic user creation
     */
    public ClinicServiceRegistrationRequest(String code, String name, String description,
                                            String address, String phone, String email,
                                            String hcenEndpoint, Boolean active, String apiKey,
                                            String password) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.hcenEndpoint = hcenEndpoint;
        this.active = active != null ? active : true;
        this.apiKey = apiKey;
        this.password = password;
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getHcenEndpoint() {
        return hcenEndpoint;
    }

    public void setHcenEndpoint(String hcenEndpoint) {
        this.hcenEndpoint = hcenEndpoint;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "ClinicServiceRegistrationRequest{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", hcenEndpoint='" + hcenEndpoint + '\'' +
                ", active=" + active +
                '}';
    }
}
