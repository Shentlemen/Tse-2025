package uy.gub.hcen.service.clinic.dto;

/**
 * Onboarding Request DTO
 * <p>
 * Data Transfer Object sent to peripheral nodes during clinic onboarding (AC016).
 * This DTO contains all configuration needed for the peripheral node to integrate with HCEN central.
 * <p>
 * Sent via POST to: {peripheralNodeUrl}/api/onboard
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
public class OnboardingRequest {

    /**
     * Unique clinic identifier assigned by HCEN central
     */
    private String clinicId;

    /**
     * API key for authenticating peripheral node requests to HCEN central
     */
    private String apiKey;

    /**
     * HCEN central API base URL
     * Example: https://hcen.uy/api
     */
    private String hcenCentralUrl;

    /**
     * Clinic configuration (name, address, contact info)
     */
    private ClinicConfig config;

    /**
     * Nested configuration class
     */
    public static class ClinicConfig {
        private String clinicName;
        private String address;
        private String city;
        private String phoneNumber;
        private String email;

        // Constructors

        public ClinicConfig() {
        }

        public ClinicConfig(String clinicName, String address, String city,
                             String phoneNumber, String email) {
            this.clinicName = clinicName;
            this.address = address;
            this.city = city;
            this.phoneNumber = phoneNumber;
            this.email = email;
        }

        // Getters and Setters

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

        @Override
        public String toString() {
            return "ClinicConfig{" +
                    "clinicName='" + clinicName + '\'' +
                    ", city='" + city + '\'' +
                    '}';
        }
    }

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor
     */
    public OnboardingRequest() {
    }

    /**
     * Full constructor
     *
     * @param clinicId Clinic ID
     * @param apiKey API key
     * @param hcenCentralUrl HCEN central URL
     * @param config Clinic configuration
     */
    public OnboardingRequest(String clinicId, String apiKey, String hcenCentralUrl, ClinicConfig config) {
        this.clinicId = clinicId;
        this.apiKey = apiKey;
        this.hcenCentralUrl = hcenCentralUrl;
        this.config = config;
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

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getHcenCentralUrl() {
        return hcenCentralUrl;
    }

    public void setHcenCentralUrl(String hcenCentralUrl) {
        this.hcenCentralUrl = hcenCentralUrl;
    }

    public ClinicConfig getConfig() {
        return config;
    }

    public void setConfig(ClinicConfig config) {
        this.config = config;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "OnboardingRequest{" +
                "clinicId='" + clinicId + '\'' +
                ", hcenCentralUrl='" + hcenCentralUrl + '\'' +
                ", config=" + config +
                '}';
    }
}
