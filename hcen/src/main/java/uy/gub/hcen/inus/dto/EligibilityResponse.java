package uy.gub.hcen.inus.dto;

/**
 * Eligibility Response DTO
 * <p>
 * Data Transfer Object for user eligibility validation responses.
 * This DTO is returned when checking if a user is eligible for
 * the health system based on INUS criteria.
 * <p>
 * Eligibility Criteria:
 * - User exists in INUS registry
 * - User status is ACTIVE
 * - User age has been verified (18+ years old)
 * <p>
 * The reason field provides detailed information about why a user
 * is or is not eligible, which can be displayed to administrators
 * or logged for audit purposes.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 */
public class EligibilityResponse {

    private final String ci;
    private final boolean eligible;
    private final String reason;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Constructor for eligibility response
     *
     * @param ci       User's Cédula de Identidad
     * @param eligible Whether the user is eligible
     * @param reason   Detailed reason for eligibility status
     */
    public EligibilityResponse(String ci, boolean eligible, String reason) {
        this.ci = ci;
        this.eligible = eligible;
        this.reason = reason;
    }

    // ================================================================
    // Factory Methods for Common Cases
    // ================================================================

    /**
     * Create eligibility response for an eligible user
     *
     * @param ci User's CI
     * @return EligibilityResponse indicating user is eligible
     */
    public static EligibilityResponse eligible(String ci) {
        return new EligibilityResponse(ci, true, "User is eligible for the health system");
    }

    /**
     * Create eligibility response for user not found
     *
     * @param ci User's CI
     * @return EligibilityResponse indicating user not found
     */
    public static EligibilityResponse notFound(String ci) {
        return new EligibilityResponse(ci, false, "User not found in INUS registry");
    }

    /**
     * Create eligibility response for inactive user
     *
     * @param ci User's CI
     * @return EligibilityResponse indicating user is not active
     */
    public static EligibilityResponse notActive(String ci) {
        return new EligibilityResponse(ci, false, "User status is not ACTIVE");
    }

    /**
     * Create eligibility response for user with unverified age
     *
     * @param ci User's CI
     * @return EligibilityResponse indicating age not verified
     */
    public static EligibilityResponse ageNotVerified(String ci) {
        return new EligibilityResponse(ci, false, "User age has not been verified (must be 18+)");
    }

    /**
     * Create eligibility response for suspended user
     *
     * @param ci User's CI
     * @return EligibilityResponse indicating user is suspended
     */
    public static EligibilityResponse suspended(String ci) {
        return new EligibilityResponse(ci, false, "User account is suspended");
    }

    // ================================================================
    // Getters Only (Immutable DTO)
    // ================================================================

    public String getCi() {
        return ci;
    }

    public boolean isEligible() {
        return eligible;
    }

    public String getReason() {
        return reason;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "EligibilityResponse{" +
                "ci='" + ci + '\'' +
                ", eligible=" + eligible +
                ", reason='" + reason + '\'' +
                '}';
    }
}
