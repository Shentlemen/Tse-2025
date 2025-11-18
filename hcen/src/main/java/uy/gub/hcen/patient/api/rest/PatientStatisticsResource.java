package uy.gub.hcen.patient.api.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uy.gub.hcen.api.dto.ErrorResponse;
import uy.gub.hcen.patient.dto.PatientDashboardStatsDTO;
import uy.gub.hcen.clinicalhistory.service.ClinicalHistoryService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Patient Statistics REST Resource
 *
 * JAX-RS resource providing REST API endpoints for patient statistics.
 * This resource provides document counts and other aggregated data for the patient dashboard.
 *
 * <p>Base Path: /api/patient
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /api/patient/{patientCi}/statistics - Get document statistics for patient</li>
 * </ul>
 *
 * <p>Security:
 * <ul>
 *   <li>JWT authentication required (to be implemented via SecurityContext)</li>
 *   <li>Patients can only access their own statistics</li>
 * </ul>
 *
 * <p>Error Handling:
 * <ul>
 *   <li>400 Bad Request - Invalid parameters</li>
 *   <li>500 Internal Server Error - System errors</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-18
 */
@Path("/patients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PatientStatisticsResource {

    private static final Logger LOGGER = Logger.getLogger(PatientStatisticsResource.class.getName());

    @Inject
    private ClinicalHistoryService clinicalHistoryService;

    // ================================================================
    // GET /api/patients/{patientCi}/statistics - Get Patient Dashboard Statistics
    // ================================================================

    /**
     * Retrieves dashboard statistics for a patient.
     *
     * <p>Returns aggregated counts for the patient dashboard:
     * <ul>
     *   <li>totalDocuments - Total documents in RNDC for this patient</li>
     *   <li>activePolicies - Active access policies defined by the patient</li>
     *   <li>recentAccess - Recent access to patient's data by professionals (last 30 days)</li>
     *   <li>pendingApprovals - Pending access request approvals</li>
     * </ul>
     *
     * <p>This endpoint is used by the patient dashboard to display summary statistics.
     *
     * <p>Example:
     * GET /api/patients/12345678/statistics
     *
     * <p>Example Response:
     * <pre>
     * {
     *   "totalDocuments": 5,
     *   "activePolicies": 3,
     *   "recentAccess": 12,
     *   "pendingApprovals": 2
     * }
     * </pre>
     *
     * @param patientCi Patient's CI (Cedula de Identidad)
     * @return 200 OK with PatientDashboardStatsDTO
     *         400 Bad Request if patientCi is invalid
     *         500 Internal Server Error if operation fails
     */
    @GET
    @Path("/{patientCi}/statistics")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPatientStatistics(@PathParam("patientCi") String patientCi) {

        LOGGER.log(Level.INFO, "GET /api/patients/{0}/statistics", patientCi);

        try {
            // Validate patientCi
            if (patientCi == null || patientCi.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Invalid patientCi parameter: null or empty");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError("Patient CI is required"))
                        .build();
            }

            // Clean up the CI (remove whitespace)
            String cleanCi = patientCi.trim();

            // TODO: Extract patientCi from JWT SecurityContext and verify it matches the path parameter
            // For now, accept the path parameter directly for development
            // In production:
            // @Context SecurityContext securityContext;
            // String authenticatedCi = securityContext.getUserPrincipal().getName();
            // if (!authenticatedCi.equals(cleanCi)) {
            //     return Response.status(Response.Status.FORBIDDEN)
            //             .entity(ErrorResponse.forbidden("Cannot access statistics for another patient"))
            //             .build();
            // }

            // Call service to get patient dashboard statistics
            PatientDashboardStatsDTO stats = clinicalHistoryService.getPatientDashboardStats(cleanCi);

            LOGGER.log(Level.INFO, "Returning dashboard statistics for patient {0}: totalDocuments={1}, activePolicies={2}, recentAccess={3}, pendingApprovals={4}",
                    new Object[]{cleanCi, stats.getTotalDocuments(), stats.getActivePolicies(), stats.getRecentAccess(), stats.getPendingApprovals()});

            return Response.ok(stats).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving statistics for patient: " + patientCi, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to retrieve patient statistics: " + e.getMessage()))
                    .build();
        }
    }
}
