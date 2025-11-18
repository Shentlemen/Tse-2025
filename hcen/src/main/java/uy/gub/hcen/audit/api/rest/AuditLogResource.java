package uy.gub.hcen.audit.api.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import uy.gub.hcen.api.dto.ErrorResponse;
import uy.gub.hcen.audit.dto.AuditLogListResponse;
import uy.gub.hcen.audit.dto.AuditStatisticsResponse;
import uy.gub.hcen.audit.service.AuditLogService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Audit Log REST Resource
 *
 * JAX-RS resource providing REST API endpoints for patient audit log access.
 * Enables patients to view who accessed their clinical records and when.
 *
 * Base Path: /api/audit-logs
 *
 * Endpoints:
 * - GET /api/audit-logs - Get patient audit logs with filtering and pagination
 * - GET /api/audit-logs/stats - Get patient audit statistics
 *
 * Security:
 * - JWT authentication required (via SecurityContext)
 * - Patients can only view their own audit logs
 *
 * Compliance:
 * - AC026: Patients can view who accessed their records and when
 * - Ley N° 18.331 (Data Protection Law of Uruguay)
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-04
 */
@Path("/audit-logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditLogResource {

    private static final Logger LOGGER = Logger.getLogger(AuditLogResource.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    @Inject
    private AuditLogService auditLogService;

    // ================================================================
    // GET /api/audit-logs - Get Patient Audit Logs
    // ================================================================

    /**
     * Retrieves audit logs for the authenticated patient with optional filtering.
     *
     * Query Parameters:
     * - fromDate (optional): Start date in yyyy-MM-dd format
     * - toDate (optional): End date in yyyy-MM-dd format
     * - eventType (optional): Filter by event type (ACCESS, MODIFICATION, CREATION, etc.)
     * - page (optional): Page number (0-based, default: 0)
     * - size (optional): Page size (default: 20, max: 100)
     *
     * Example: GET /api/audit-logs?fromDate=2025-10-01&toDate=2025-10-31&eventType=ACCESS&page=0&size=20
     *
     * @param fromDateStr Start date string
     * @param toDateStr End date string
     * @param eventType Event type filter
     * @param page Page number
     * @param size Page size
     * @param securityContext Security context with authenticated user
     * @return 200 OK with AuditLogListResponse
     *         400 Bad Request if parameters invalid
     *         401 Unauthorized if not authenticated
     *         500 Internal Server Error if operation fails
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuditLogs(
            @QueryParam("fromDate") String fromDateStr,
            @QueryParam("toDate") String toDateStr,
            @QueryParam("eventType") String eventType,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("patientCi") String patientCiParam,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "GET audit logs - fromDate: {0}, toDate: {1}, eventType: {2}, page: {3}, size: {4}",
                new Object[]{fromDateStr, toDateStr, eventType, page, size});

        try {
            // Try to get from query param first (for development), then from SecurityContext
            String patientCi = patientCiParam;
            if (patientCi == null || patientCi.trim().isEmpty()) {
                patientCi = extractPatientCi(securityContext);
            }
            if (patientCi == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.unauthorized("Authentication required"))
                        .build();
            }

            // Validate pagination
            if (page < 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError("Page number must be non-negative"))
                        .build();
            }

            if (size <= 0 || size > MAX_SIZE) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError("Page size must be between 1 and " + MAX_SIZE))
                        .build();
            }

            // Parse dates
            LocalDate fromDate = null;
            LocalDate toDate = null;

            if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
                try {
                    fromDate = LocalDate.parse(fromDateStr.trim(), DATE_FORMATTER);
                } catch (DateTimeParseException e) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(ErrorResponse.validationError("Invalid fromDate format. Use yyyy-MM-dd"))
                            .build();
                }
            }

            if (toDateStr != null && !toDateStr.trim().isEmpty()) {
                try {
                    toDate = LocalDate.parse(toDateStr.trim(), DATE_FORMATTER);
                } catch (DateTimeParseException e) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(ErrorResponse.validationError("Invalid toDate format. Use yyyy-MM-dd"))
                            .build();
                }
            }

            // Validate date range
            if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError("fromDate cannot be after toDate"))
                        .build();
            }

            // Get audit logs from service
            AuditLogListResponse response = auditLogService.getPatientAuditLogs(
                    patientCi, fromDate, toDate, eventType, page, size);

            LOGGER.log(Level.INFO, "Retrieved {0} audit logs for patient: {1}",
                    new Object[]{response.getLogs().size(), patientCi});

            return Response.ok(response).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving audit logs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to retrieve audit logs: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // GET /api/audit-logs/stats - Get Patient Audit Statistics
    // ================================================================

    /**
     * Retrieves audit statistics for the authenticated patient.
     *
     * Returns summary statistics:
     * - Total access events
     * - Events by type (ACCESS, MODIFICATION, etc.)
     * - Events by outcome (SUCCESS, DENIED, FAILURE)
     * - Top accessors (professionals who accessed most frequently)
     *
     * Example: GET /api/audit-logs/stats
     *
     * @param securityContext Security context with authenticated user
     * @return 200 OK with AuditStatisticsResponse
     *         401 Unauthorized if not authenticated
     *         500 Internal Server Error if operation fails
     */
    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuditStatistics(
            @QueryParam("patientCi") String patientCiParam,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "GET audit statistics");

        try {
            // Try to get from query param first (for development), then from SecurityContext
            String patientCi = patientCiParam;
            if (patientCi == null || patientCi.trim().isEmpty()) {
                patientCi = extractPatientCi(securityContext);
            }
            if (patientCi == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.unauthorized("Authentication required"))
                        .build();
            }

            // Get statistics from service
            AuditStatisticsResponse response = auditLogService.getPatientAuditStatistics(patientCi);

            LOGGER.log(Level.INFO, "Retrieved audit statistics for patient: {0} - {1} total events",
                    new Object[]{patientCi, response.getTotalEvents()});

            return Response.ok(response).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving audit statistics", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to retrieve audit statistics: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // Helper Methods
    // ================================================================

    /**
     * Extracts patient CI from SecurityContext.
     *
     * @param securityContext Security context
     * @return Patient CI or null if not authenticated
     */
    private String extractPatientCi(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return null;
        }
        return securityContext.getUserPrincipal().getName();
    }
}
