package uy.gub.hcen.admin.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.hcen.admin.dto.SystemStatisticsResponse;
import uy.gub.hcen.admin.service.StatisticsService;
import uy.gub.hcen.api.dto.ErrorResponse;

/**
 * Statistics REST Resource
 * <p>
 * JAX-RS REST API endpoint for system statistics.
 * Provides aggregated metrics for the HCEN admin dashboard.
 * <p>
 * Base Path: /api/admin/statistics
 * <p>
 * Endpoints:
 * - GET /api/admin/statistics - Get system-wide statistics
 * <p>
 * Authorization:
 * - Requires JWT with ADMIN role (to be enforced with JwtAuthenticationFilter)
 * <p>
 * Response Format:
 * - Success: 200 OK with SystemStatisticsResponse
 * - Server Errors: 500 Internal Server Error
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-24
 */
@Path("/admin/statistics")
@Produces(MediaType.APPLICATION_JSON)
public class StatisticsResource {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsResource.class);

    @Inject
    private StatisticsService statisticsService;

    /**
     * Get system-wide statistics
     * <p>
     * Returns aggregated statistics for the HCEN admin dashboard:
     * - Total users (INUS)
     * - Total documents (RNDC)
     * - Total policies
     * - Total clinics (with breakdown by status)
     * <p>
     * Response:
     * - 200 OK: SystemStatisticsResponse with statistics
     * - 500 Internal Server Error: System error
     *
     * @return Response with SystemStatisticsResponse
     */
    @GET
    public Response getSystemStatistics() {
        logger.debug("Fetching system statistics for admin dashboard");

        try {
            SystemStatisticsResponse statistics = statisticsService.getSystemStatistics();

            logger.debug("Returning system statistics: {}", statistics);

            return Response.ok(statistics).build();

        } catch (Exception e) {
            logger.error("Unexpected error while fetching system statistics", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
                    .build();
        }
    }
}
