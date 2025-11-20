package uy.gub.clinic.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for clinic registration requests from HCEN
 * <p>
 * This DTO is used when HCEN central registers a new clinic in the peripheral component.
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
 * @since 2025-11-19
 */
public class ClinicRegistrationRequest {

    /**
     * Unique clinic identifier (clinicId from HCEN central)
     * Format: clinic-{uuid}
     */
    @NotBlank(message = "Clinic code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    @JsonProperty("code")
    private String code;

    /**
     * Official name of the clinic or healthcare facility
     */
    @NotBlank(message = "Clinic name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
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
    @Email(message = "Invalid email format")
    @JsonProperty("email")
    private String email;

    /**
     * URL of the HCEN central API endpoint
     * Same for all clinics: https://hcen.uy/api
     */
    @NotBlank(message = "HCEN endpoint is required")
    @JsonProperty("hcen_endpoint")
    private String hcenEndpoint;

    /**
     * Active status (default: true)
     */
    @NotNull(message = "Active status is required")
    @JsonProperty("active")
    private Boolean active;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor (required for Jackson deserialization)
     */
    public ClinicRegistrationRequest() {
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
     */
    public ClinicRegistrationRequest(String code, String name, String description,
                                     String address, String phone, String email,
                                     String hcenEndpoint, Boolean active) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.hcenEndpoint = hcenEndpoint;
        this.active = active != null ? active : true;
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

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "ClinicRegistrationRequest{" +
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
