package uy.gub.hcen.admin.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.hcen.admin.dto.SystemStatisticsResponse;
import uy.gub.hcen.clinic.entity.Clinic;
import uy.gub.hcen.clinic.repository.ClinicRepository;
import uy.gub.hcen.inus.entity.UserStatus;
import uy.gub.hcen.inus.repository.InusRepository;
import uy.gub.hcen.policy.repository.AccessPolicyRepository;
import uy.gub.hcen.rndc.repository.RndcRepository;

/**
 * Statistics Service
 * <p>
 * Aggregates system-wide statistics for the HCEN admin dashboard.
 * <p>
 * Responsibilities:
 * - Count users (total, active, inactive)
 * - Count documents in RNDC
 * - Count access policies
 * - Count clinics (total, active, pending, inactive)
 * - Provide aggregated statistics for dashboard
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-24
 */
@ApplicationScoped
public class StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

    @Inject
    private InusRepository inusRepository;

    @Inject
    private RndcRepository rndcRepository;

    @Inject
    private AccessPolicyRepository policyRepository;

    @Inject
    private ClinicRepository clinicRepository;

    /**
     * Get system-wide statistics for admin dashboard
     *
     * @return System statistics
     */
    public SystemStatisticsResponse getSystemStatistics() {
        logger.debug("Calculating system statistics");

        try {
            SystemStatisticsResponse stats = new SystemStatisticsResponse();

            // Count users
            long totalUsers = inusRepository.count();
            long activeUsers = inusRepository.countByStatus(UserStatus.ACTIVE);
            long inactiveUsers = totalUsers - activeUsers;

            stats.setTotalUsers(totalUsers);
            stats.setActiveUsers(activeUsers);
            stats.setInactiveUsers(inactiveUsers);

            // Count documents - using countAll() method from RndcRepository
            long totalDocuments = rndcRepository.countAll();
            stats.setTotalDocuments(totalDocuments);

            // Count policies - note: AccessPolicyRepository doesn't have a total count method
            // For now, we'll set this to 0 or implement a count method in the repository
            // TODO: Add count() method to AccessPolicyRepository implementation
            long totalPolicies = 0; // policyRepository.count() - not available yet
            stats.setTotalPolicies(totalPolicies);

            // Count clinics
            long totalClinics = clinicRepository.count();
            long activeClinics = clinicRepository.countByStatus(Clinic.ClinicStatus.ACTIVE);
            long pendingClinics = clinicRepository.countByStatus(Clinic.ClinicStatus.PENDING_ONBOARDING);
            long inactiveClinics = clinicRepository.countByStatus(Clinic.ClinicStatus.INACTIVE);

            stats.setTotalClinics(totalClinics);
            stats.setActiveClinics(activeClinics);
            stats.setPendingClinics(pendingClinics);
            stats.setInactiveClinics(inactiveClinics);

            logger.info("System statistics calculated: {} users, {} documents, {} policies, {} clinics",
                    totalUsers, totalDocuments, totalPolicies, totalClinics);

            return stats;

        } catch (Exception e) {
            logger.error("Error calculating system statistics", e);
            // Return empty statistics on error
            return new SystemStatisticsResponse(0, 0, 0, 0);
        }
    }

    /**
     * Get user statistics
     *
     * @return Total and active user counts
     */
    public long getTotalUsers() {
        return inusRepository.count();
    }

    /**
     * Get active users count
     *
     * @return Active users count
     */
    public long getActiveUsers() {
        return inusRepository.countByStatus(UserStatus.ACTIVE);
    }

    /**
     * Get document statistics
     *
     * @return Total document count
     */
    public long getTotalDocuments() {
        return rndcRepository.countAll();
    }

    /**
     * Get policy statistics
     *
     * @return Total policy count
     * TODO: Implement count() method in AccessPolicyRepository
     */
    public long getTotalPolicies() {
        return 0; // policyRepository.count() - not implemented yet
    }

    /**
     * Get clinic statistics
     *
     * @return Total clinic count
     */
    public long getTotalClinics() {
        return clinicRepository.count();
    }

    /**
     * Get active clinics count
     *
     * @return Active clinics count
     */
    public long getActiveClinics() {
        return clinicRepository.countByStatus(Clinic.ClinicStatus.ACTIVE);
    }
}
