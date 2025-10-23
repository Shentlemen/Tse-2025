package uy.gub.hcen.service.clinic.dto;

import uy.gub.hcen.clinic.entity.Clinic;
import uy.gub.hcen.clinic.entity.Clinic.ClinicStatus;

import java.time.LocalDateTime;

/**
 * Clinic Response DTO
 * <p>
 * Data Transfer Object for returning clinic information to clients.
 * This DTO is returned in response to:
 * - Clinic registration (POST /api/admin/clinics)
 * - Clinic lookup (GET /api/admin/clinics/{clinicId})
 * - Clinic update (PUT /api/admin/clinics/{clinicId})
 * <p>
 * Note: API key is masked for security (not exposed in responses).
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
public class ClinicResponse {

    private String clinicId;
    private String clinicName;
    private String address;
    private String city;
    private String phoneNumber;
    private String email;
    private String peripheralNodeUrl;
    private String apiKey; // Masked (e.g., "****...1234")
    private ClinicStatus status;
    private LocalDateTime onboardedAt;
    private LocalDateTime createdAt;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor
     */
    public ClinicResponse() {
    }

    /**
     * Constructor from Clinic entity
     *
     * @param clinic Clinic entity
     * @param maskApiKey Whether to mask the API key (true for external responses)
     */
    public ClinicResponse(Clinic clinic, boolean maskApiKey) {
        this.clinicId = clinic.getClinicId();
        this.clinicName = clinic.getClinicName();
        this.address = clinic.getAddress();
        this.city = clinic.getCity();
        this.phoneNumber = clinic.getPhoneNumber();
        this.email = clinic.getEmail();
        this.peripheralNodeUrl = clinic.getPeripheralNodeUrl();
        this.apiKey = maskApiKey ? maskApiKey(clinic.getApiKey()) : clinic.getApiKey();
        this.status = clinic.getStatus();
        this.onboardedAt = clinic.getOnboardedAt();
        this.createdAt = clinic.getCreatedAt();
    }

    /**
     * Convenience constructor with API key masking enabled by default
     *
     * @param clinic Clinic entity
     */
    public ClinicResponse(Clinic clinic) {
        this(clinic, true);
    }

    // ================================================================
    // Helper Methods
    // ================================================================

    /**
     * Masks the API key for security
     * Shows only last 4 characters: "****...1234"
     *
     * @param apiKey Full API key
     * @return Masked API key
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 4) {
            return "****";
        }
        return "****..." + apiKey.substring(apiKey.length() - 4);
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
    }

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

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public ClinicStatus getStatus() {
        return status;
    }

    public void setStatus(ClinicStatus status) {
        this.status = status;
    }

    public LocalDateTime getOnboardedAt() {
        return onboardedAt;
    }

    public void setOnboardedAt(LocalDateTime onboardedAt) {
        this.onboardedAt = onboardedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "ClinicResponse{" +
                "clinicId='" + clinicId + '\'' +
                ", clinicName='" + clinicName + '\'' +
                ", city='" + city + '\'' +
                ", status=" + status +
                ", onboardedAt=" + onboardedAt +
                ", createdAt=" + createdAt +
                '}';
    }
}
