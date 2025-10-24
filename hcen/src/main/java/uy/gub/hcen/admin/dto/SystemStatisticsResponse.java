package uy.gub.hcen.admin.dto;

/**
 * System Statistics Response DTO
 * <p>
 * Returns aggregated statistics for the HCEN admin dashboard.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-24
 */
public class SystemStatisticsResponse {

    private long totalUsers;
    private long totalDocuments;
    private long totalPolicies;
    private long totalClinics;
    private long activeClinics;
    private long pendingClinics;
    private long inactiveClinics;
    private long activeUsers;
    private long inactiveUsers;

    public SystemStatisticsResponse() {
    }

    public SystemStatisticsResponse(long totalUsers, long totalDocuments, long totalPolicies, long totalClinics) {
        this.totalUsers = totalUsers;
        this.totalDocuments = totalDocuments;
        this.totalPolicies = totalPolicies;
        this.totalClinics = totalClinics;
    }

    // Getters and Setters

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

    public long getTotalClinics() {
        return totalClinics;
    }

    public void setTotalClinics(long totalClinics) {
        this.totalClinics = totalClinics;
    }

    public long getActiveClinics() {
        return activeClinics;
    }

    public void setActiveClinics(long activeClinics) {
        this.activeClinics = activeClinics;
    }

    public long getPendingClinics() {
        return pendingClinics;
    }

    public void setPendingClinics(long pendingClinics) {
        this.pendingClinics = pendingClinics;
    }

    public long getInactiveClinics() {
        return inactiveClinics;
    }

    public void setInactiveClinics(long inactiveClinics) {
        this.inactiveClinics = inactiveClinics;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getInactiveUsers() {
        return inactiveUsers;
    }

    public void setInactiveUsers(long inactiveUsers) {
        this.inactiveUsers = inactiveUsers;
    }

    @Override
    public String toString() {
        return "SystemStatisticsResponse{" +
                "totalUsers=" + totalUsers +
                ", totalDocuments=" + totalDocuments +
                ", totalPolicies=" + totalPolicies +
                ", totalClinics=" + totalClinics +
                ", activeClinics=" + activeClinics +
                ", pendingClinics=" + pendingClinics +
                ", inactiveClinics=" + inactiveClinics +
                ", activeUsers=" + activeUsers +
                ", inactiveUsers=" + inactiveUsers +
                '}';
    }
}
