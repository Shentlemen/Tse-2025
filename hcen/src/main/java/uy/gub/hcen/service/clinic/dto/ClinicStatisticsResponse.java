package uy.gub.hcen.service.clinic.dto;

/**
 * Clinic Statistics Response DTO
 * <p>
 * Data Transfer Object for returning clinic-specific statistics.
 * This DTO is returned when requesting detailed statistics for a specific clinic.
 * <p>
 * Statistics include:
 * - Number of users registered by this clinic
 * - Number of documents registered by this clinic
 * - Clinic operational status
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
public class ClinicStatisticsResponse {

    /**
     * Clinic ID
     */
    private String clinicId;

    /**
     * Clinic name
     */
    private String clinicName;

    /**
     * Total number of users registered by this clinic in INUS
     */
    private long totalUsers;

    /**
     * Total number of documents registered by this clinic in RNDC
     */
    private long totalDocuments;

    /**
     * Number of active policies involving this clinic
     */
    private long totalPolicies;

    /**
     * Clinic status (ACTIVE, INACTIVE, PENDING_ONBOARDING)
     */
    private String status;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor
     */
    public ClinicStatisticsResponse() {
    }

    /**
     * Full constructor
     *
     * @param clinicId Clinic ID
     * @param clinicName Clinic name
     * @param totalUsers Total users registered
     * @param totalDocuments Total documents registered
     * @param totalPolicies Total policies involving clinic
     * @param status Clinic status
     */
    public ClinicStatisticsResponse(String clinicId, String clinicName, long totalUsers,
                                      long totalDocuments, long totalPolicies, String status) {
        this.clinicId = clinicId;
        this.clinicName = clinicName;
        this.totalUsers = totalUsers;
        this.totalDocuments = totalDocuments;
        this.totalPolicies = totalPolicies;
        this.status = status;
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

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(long totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public long getTotalPolicies() {
        return totalPolicies;
    }

    public void setTotalPolicies(long totalPolicies) {
        this.totalPolicies = totalPolicies;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "ClinicStatisticsResponse{" +
                "clinicId='" + clinicId + '\'' +
                ", clinicName='" + clinicName + '\'' +
                ", totalUsers=" + totalUsers +
                ", totalDocuments=" + totalDocuments +
                ", totalPolicies=" + totalPolicies +
                ", status='" + status + '\'' +
                '}';
    }
}
