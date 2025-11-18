package uy.gub.hcen.patient.dto;

/**
 * Patient Dashboard Statistics DTO
 *
 * Data transfer object containing aggregated statistics for the patient dashboard.
 * This DTO provides a summary view of the patient's data in HCEN.
 *
 * <p>Statistics included:
 * <ul>
 *   <li>Total documents registered in RNDC for this patient</li>
 *   <li>Active access policies defined by the patient</li>
 *   <li>Recent access to patient's data by healthcare professionals</li>
 *   <li>Pending access request approvals awaiting patient response</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-18
 */
public class PatientDashboardStatsDTO {

    /**
     * Total count of documents registered in RNDC for this patient.
     * Includes only ACTIVE documents.
     */
    private long totalDocuments;

    /**
     * Count of active access policies defined by the patient.
     * Active policies are those currently within their validity period.
     */
    private long activePolicies;

    /**
     * Count of recent access events to patient's data by healthcare professionals.
     * Typically counts ACCESS events from the last 30 days.
     */
    private long recentAccess;

    /**
     * Count of pending access request approvals awaiting patient response.
     * These are non-expired requests with PENDING status.
     */
    private long pendingApprovals;

    /**
     * Default constructor
     */
    public PatientDashboardStatsDTO() {
    }

    /**
     * Constructor with all fields
     *
     * @param totalDocuments Total documents count
     * @param activePolicies Active policies count
     * @param recentAccess Recent access count
     * @param pendingApprovals Pending approvals count
     */
    public PatientDashboardStatsDTO(long totalDocuments, long activePolicies, long recentAccess, long pendingApprovals) {
        this.totalDocuments = totalDocuments;
        this.activePolicies = activePolicies;
        this.recentAccess = recentAccess;
        this.pendingApprovals = pendingApprovals;
    }

    // Getters and Setters

    public long getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(long totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public long getActivePolicies() {
        return activePolicies;
    }

    public void setActivePolicies(long activePolicies) {
        this.activePolicies = activePolicies;
    }

    public long getRecentAccess() {
        return recentAccess;
    }

    public void setRecentAccess(long recentAccess) {
        this.recentAccess = recentAccess;
    }

    public long getPendingApprovals() {
        return pendingApprovals;
    }

    public void setPendingApprovals(long pendingApprovals) {
        this.pendingApprovals = pendingApprovals;
    }

    @Override
    public String toString() {
        return "PatientDashboardStatsDTO{" +
                "totalDocuments=" + totalDocuments +
                ", activePolicies=" + activePolicies +
                ", recentAccess=" + recentAccess +
                ", pendingApprovals=" + pendingApprovals +
                '}';
    }
}
