package uy.gub.hcen.service.clinic.dto;

/**
 * Onboarding Response DTO
 * <p>
 * Data Transfer Object returned after successful clinic onboarding (AC016).
 * This DTO contains the result of the onboarding operation and clinic details.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
public class OnboardingResponse {

    /**
     * Clinic ID
     */
    private String clinicId;

    /**
     * Clinic name
     */
    private String clinicName;

    /**
     * Onboarding status (SUCCESS, FAILED, ALREADY_ONBOARDED)
     */
    private String status;

    /**
     * Human-readable message describing the result
     */
    private String message;

    /**
     * Whether peripheral node confirmed successful onboarding
     */
    private boolean peripheralNodeConfirmed;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor
     */
    public OnboardingResponse() {
    }

    /**
     * Full constructor
     *
     * @param clinicId Clinic ID
     * @param clinicName Clinic name
     * @param status Onboarding status
     * @param message Result message
     * @param peripheralNodeConfirmed Whether peripheral node confirmed
     */
    public OnboardingResponse(String clinicId, String clinicName, String status,
                               String message, boolean peripheralNodeConfirmed) {
        this.clinicId = clinicId;
        this.clinicName = clinicName;
        this.status = status;
        this.message = message;
        this.peripheralNodeConfirmed = peripheralNodeConfirmed;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isPeripheralNodeConfirmed() {
        return peripheralNodeConfirmed;
    }

    public void setPeripheralNodeConfirmed(boolean peripheralNodeConfirmed) {
        this.peripheralNodeConfirmed = peripheralNodeConfirmed;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "OnboardingResponse{" +
                "clinicId='" + clinicId + '\'' +
                ", clinicName='" + clinicName + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", peripheralNodeConfirmed=" + peripheralNodeConfirmed +
                '}';
    }
}
