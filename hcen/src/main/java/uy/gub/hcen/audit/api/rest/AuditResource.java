package uy.gub.hcen.audit.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uy.gub.hcen.api.dto.ErrorResponse;
import uy.gub.hcen.audit.dto.AuditLogListResponse;
import uy.gub.hcen.audit.dto.AuditLogResponse;
import uy.gub.hcen.audit.dto.AuditStatisticsResponse;
import uy.gub.hcen.audit.dto.PatientAccessHistoryResponse;
import uy.gub.hcen.audit.entity.AuditLog;
import uy.gub.hcen.audit.entity.AuditLog.ActionOutcome;
import uy.gub.hcen.audit.entity.AuditLog.EventType;
import uy.gub.hcen.service.audit.AuditService;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Audit Resource - JAX-RS REST API for Audit System
 *
 * Provides read-only REST endpoints for querying audit logs.
 * All endpoints are GET methods (immutable audit trail).
 *
 * <p>Compliance: AC026 - Patients can view who accessed their records
 *
 * <p>Base Path: /api/audit
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /api/audit/patients/{patientCi} - Patient access history (AC026, CU05)</li>
 *   <li>GET /api/audit/logs - Search audit logs (admin)</li>
 *   <li>GET /api/audit/statistics - Audit statistics (admin)</li>
 *   <li>GET /api/audit/actors/{actorId} - Actor audit trail</li>
 * </ul>
 *
 * <p>All endpoints support pagination:
 * - page: Page number (0-based, default: 0)
 * - size: Page size (1-100, default: 20)
 *
 * <p>Authentication: JWT tokens (not implemented yet - placeholder for future)
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
@Path("/audit")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditResource {

    private static final Logger LOGGER = Logger.getLogger(AuditResource.class.getName());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Pagination constraints
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MIN_PAGE_SIZE = 1;

    @Inject
    private AuditService auditService;

    // =========================================================================
    // Patient Access History Endpoint (AC026 Compliance)
    // =========================================================================

    /**
     * GET /api/audit/patients/{patientCi} - Patient Access History
     *
     * Returns all accesses to a patient's documents.
     * This endpoint fulfills AC026 compliance requirement:
     * "Patients can view who accessed their records and when"
     *
     * <p>Use Case: CU05 - Patient wants to see who viewed their records
     *
     * <p>Authorization: Patient or Admin (JWT validation - not yet implemented)
     *
     * <p>Response Example:
     * <pre>
     * {
     *   "patientCi": "12345678",
     *   "accesses": [
     *     {
     *       "accessorId": "prof-123",
     *       "accessorName": "Dr. Smith",
     *       "specialty": "CARDIOLOGY",
     *       "clinicId": "clinic-001",
     *       "documentType": "LAB_RESULT",
     *       "accessTime": "2025-10-21T14:30:00",
     *       "outcome": "SUCCESS"
     *     }
     *   ],
     *   "totalAccesses": 45,
     *   "page": 0,
     *   "size": 20,
     *   "totalPages": 3
     * }
     * </pre>
     *
     * @param patientCi Patient's CI (national ID)
     * @param page Page number (0-based, default: 0)
     * @param size Page size (1-100, default: 20)
     * @return 200 OK with PatientAccessHistoryResponse (may be empty list)
     */
    @GET
    @Path("/patients/{patientCi}")
    public Response getPatientAccessHistory(@PathParam("patientCi") String patientCi,
                                           @QueryParam("page") @DefaultValue("0") int page,
                                           @QueryParam("size") @DefaultValue("20") int size) {
        LOGGER.log(Level.INFO, "GET /api/audit/patients/{0}?page={1}&size={2}",
                new Object[]{patientCi, page, size});

        // Validate patientCi
        if (patientCi == null || patientCi.trim().isEmpty()) {
            ErrorResponse error = ErrorResponse.badRequest("Patient CI is required");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build();
        }

        // Sanitize pagination
        page = Math.max(0, page);
        size = Math.max(MIN_PAGE_SIZE, Math.min(MAX_PAGE_SIZE, size));

        // Query audit service
        List<AuditLog> auditLogs = auditService.getPatientAccessHistory(patientCi, page, size);

        // Get total count (estimate - in production would use dedicated count query)
        // For now, if we get a full page, assume there might be more
        int estimatedTotal = auditLogs.size();
        if (auditLogs.size() >= size) {
            // Estimate: assume at least one more page exists
            estimatedTotal = (page + 2) * size;
        }

        // Convert to response DTO
        PatientAccessHistoryResponse response = PatientAccessHistoryResponse.fromAuditLogs(
                patientCi,
                auditLogs,
                estimatedTotal,
                page,
                size
        );

        // Meta-audit: Log that patient viewed their access history
        auditService.logAccessEvent(
                patientCi,
                "PATIENT",
                "AUDIT_LOG",
                patientCi,
                ActionOutcome.SUCCESS,
                null, // IP address would come from request context
                null, // User agent would come from request context
                Map.of("action", "VIEW_ACCESS_HISTORY", "page", page, "size", size)
        );

        return Response.ok(response).build();
    }

    // =========================================================================
    // Search Audit Logs Endpoint (Admin)
    // =========================================================================

    /**
     * GET /api/audit/logs - Search Audit Logs
     *
     * Flexible search for audit logs with multiple optional filters.
     * All filters are optional - omit to search all logs.
     *
     * <p>Authorization: Admin only (JWT validation - not yet implemented)
     *
     * <p>Query Parameters:
     * - eventType: Filter by event type (ACCESS, MODIFICATION, etc.)
     * - actorId: Filter by actor identifier
     * - resourceType: Filter by resource type (DOCUMENT, USER, etc.)
     * - fromDate: Start date (ISO-8601: 2025-10-21T00:00:00)
     * - toDate: End date (ISO-8601: 2025-10-22T23:59:59)
     * - outcome: Filter by outcome (SUCCESS, DENIED, FAILURE)
     * - page: Page number (0-based, default: 0)
     * - size: Page size (1-100, default: 20)
     *
     * <p>Response Example:
     * <pre>
     * {
     *   "logs": [
     *     {
     *       "id": 1001,
     *       "eventType": "ACCESS",
     *       "actorId": "prof-123",
     *       "actorType": "PROFESSIONAL",
     *       "resourceType": "DOCUMENT",
     *       "resourceId": "456",
     *       "actionOutcome": "SUCCESS",
     *       "ipAddress": "192.168.1.100",
     *       "userAgent": "Mozilla/5.0...",
     *       "timestamp": "2025-10-21T14:30:00",
     *       "details": {
     *         "patientCi": "12345678",
     *         "documentType": "LAB_RESULT"
     *       }
     *     }
     *   ],
     *   "totalCount": 1500,
     *   "page": 0,
     *   "size": 20,
     *   "totalPages": 75
     * }
     * </pre>
     *
     * @param eventTypeStr Event type filter (optional)
     * @param actorId Actor ID filter (optional)
     * @param resourceType Resource type filter (optional)
     * @param fromDateStr Start date filter (optional, ISO-8601)
     * @param toDateStr End date filter (optional, ISO-8601)
     * @param outcomeStr Outcome filter (optional)
     * @param page Page number (0-based)
     * @param size Page size
     * @return 200 OK with AuditLogListResponse, 400 Bad Request if invalid parameters
     */
    @GET
    @Path("/logs")
    public Response searchAuditLogs(@QueryParam("eventType") String eventTypeStr,
                                   @QueryParam("actorId") String actorId,
                                   @QueryParam("resourceType") String resourceType,
                                   @QueryParam("fromDate") String fromDateStr,
                                   @QueryParam("toDate") String toDateStr,
                                   @QueryParam("outcome") String outcomeStr,
                                   @QueryParam("page") @DefaultValue("0") int page,
                                   @QueryParam("size") @DefaultValue("20") int size) {
        LOGGER.log(Level.INFO, "GET /api/audit/logs with filters: eventType={0}, actorId={1}, " +
                        "resourceType={2}, fromDate={3}, toDate={4}, outcome={5}, page={6}, size={7}",
                new Object[]{eventTypeStr, actorId, resourceType, fromDateStr, toDateStr,
                        outcomeStr, page, size});

        // Sanitize pagination
        page = Math.max(0, page);
        size = Math.max(MIN_PAGE_SIZE, Math.min(MAX_PAGE_SIZE, size));

        // Parse enum filters
        EventType eventType = null;
        if (eventTypeStr != null && !eventTypeStr.trim().isEmpty()) {
            try {
                eventType = EventType.valueOf(eventTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                ErrorResponse error = ErrorResponse.badRequest(
                        "Invalid eventType: " + eventTypeStr +
                                ". Valid values: " + Arrays.toString(EventType.values()));
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(error)
                        .build();
            }
        }

        ActionOutcome outcome = null;
        if (outcomeStr != null && !outcomeStr.trim().isEmpty()) {
            try {
                outcome = ActionOutcome.valueOf(outcomeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                ErrorResponse error = ErrorResponse.badRequest(
                        "Invalid outcome: " + outcomeStr +
                                ". Valid values: " + Arrays.toString(ActionOutcome.values()));
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(error)
                        .build();
            }
        }

        // Parse date filters (ISO-8601)
        LocalDateTime fromDate = null;
        if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
            try {
                fromDate = LocalDateTime.parse(fromDateStr);
            } catch (DateTimeParseException e) {
                ErrorResponse error = ErrorResponse.badRequest(
                        "Invalid fromDate format: " + fromDateStr +
                                ". Expected ISO-8601: 2025-10-21T00:00:00");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(error)
                        .build();
            }
        }

        LocalDateTime toDate = null;
        if (toDateStr != null && !toDateStr.trim().isEmpty()) {
            try {
                toDate = LocalDateTime.parse(toDateStr);
            } catch (DateTimeParseException e) {
                ErrorResponse error = ErrorResponse.badRequest(
                        "Invalid toDate format: " + toDateStr +
                                ". Expected ISO-8601: 2025-10-22T23:59:59");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(error)
                        .build();
            }
        }

        // Query audit service
        List<AuditLog> auditLogs = auditService.searchAuditLogs(
                eventType,
                actorId,
                resourceType,
                fromDate,
                toDate,
                outcome,
                page,
                size
        );

        // Convert to response DTOs
        List<AuditLogResponse> logResponses = auditLogs.stream()
                .map(AuditLogResponse::fromEntity)
                .collect(Collectors.toList());

        // Estimate total count
        int estimatedTotal = auditLogs.size();
        if (auditLogs.size() >= size) {
            estimatedTotal = (page + 2) * size;
        }

        AuditLogListResponse response = new AuditLogListResponse(
                logResponses,
                estimatedTotal,
                page,
                size
        );

        // Meta-audit: Log admin search
        Map<String, Object> searchDetails = new HashMap<>();
        searchDetails.put("action", "SEARCH_AUDIT_LOGS");
        if (eventType != null) searchDetails.put("eventType", eventType.name());
        if (actorId != null) searchDetails.put("actorId", actorId);
        if (resourceType != null) searchDetails.put("resourceType", resourceType);
        if (fromDate != null) searchDetails.put("fromDate", fromDate.toString());
        if (toDate != null) searchDetails.put("toDate", toDate.toString());
        if (outcome != null) searchDetails.put("outcome", outcome.name());
        searchDetails.put("page", page);
        searchDetails.put("size", size);
        searchDetails.put("resultsCount", auditLogs.size());

        auditService.logAccessEvent(
                "admin", // TODO: Extract from JWT
                "ADMIN",
                "AUDIT_LOG",
                "search",
                ActionOutcome.SUCCESS,
                null,
                null,
                searchDetails
        );

        return Response.ok(response).build();
    }

    // =========================================================================
    // Audit Statistics Endpoint (Admin)
    // =========================================================================

    /**
     * GET /api/audit/statistics - Audit Statistics
     *
     * Returns system-wide audit statistics for administrative dashboards.
     * Includes event counts by type, outcome, and top actors.
     *
     * <p>Authorization: Admin only (JWT validation - not yet implemented)
     *
     * <p>Response Example:
     * <pre>
     * {
     *   "totalEvents": 10000,
     *   "eventsByType": {
     *     "ACCESS": 6000,
     *     "MODIFICATION": 2000,
     *     "CREATION": 1500,
     *     "DELETION": 500
     *   },
     *   "eventsByOutcome": {
     *     "SUCCESS": 9500,
     *     "DENIED": 400,
     *     "FAILURE": 100
     *   },
     *   "topActors": [
     *     { "actorId": "prof-123", "eventCount": 450 },
     *     { "actorId": "prof-456", "eventCount": 320 }
     *   ]
     * }
     * </pre>
     *
     * @return 200 OK with AuditStatisticsResponse
     */
    @GET
    @Path("/statistics")
    public Response getAuditStatistics() {
        LOGGER.log(Level.INFO, "GET /api/audit/statistics");

        // Get total count
        long totalEvents = auditService.getTotalAuditLogCount();

        // Count by event type
        Map<String, Long> eventsByType = new HashMap<>();
        for (EventType eventType : EventType.values()) {
            long count = auditService.countEventsByType(eventType);
            if (count > 0) {
                eventsByType.put(eventType.name(), count);
            }
        }

        // Count by outcome
        Map<String, Long> eventsByOutcome = new HashMap<>();
        for (ActionOutcome outcome : ActionOutcome.values()) {
            long count = auditService.countEventsByOutcome(outcome);
            if (count > 0) {
                eventsByOutcome.put(outcome.name(), count);
            }
        }

        // Top actors (for now, return empty list - would require GROUP BY query)
        // TODO: Implement getTopActors() in AuditService with GROUP BY actorId ORDER BY COUNT(*) DESC LIMIT 10
        List<AuditStatisticsResponse.ActorStat> topActors = new ArrayList<>();

        AuditStatisticsResponse response = new AuditStatisticsResponse(
                totalEvents,
                eventsByType,
                eventsByOutcome,
                topActors
        );

        // Meta-audit: Log statistics view
        auditService.logAccessEvent(
                "admin", // TODO: Extract from JWT
                "ADMIN",
                "AUDIT_LOG",
                "statistics",
                ActionOutcome.SUCCESS,
                null,
                null,
                Map.of("action", "VIEW_STATISTICS", "totalEvents", totalEvents)
        );

        return Response.ok(response).build();
    }

    // =========================================================================
    // Actor Audit Trail Endpoint
    // =========================================================================

    /**
     * GET /api/audit/actors/{actorId} - Actor Audit Trail
     *
     * Returns all audit logs for a specific actor (all actions they performed).
     *
     * <p>Authorization: Admin or the actor themselves (JWT validation - not yet implemented)
     *
     * <p>Response Example:
     * <pre>
     * {
     *   "logs": [
     *     {
     *       "id": 1001,
     *       "eventType": "ACCESS",
     *       "actorId": "prof-123",
     *       "resourceType": "DOCUMENT",
     *       "resourceId": "456",
     *       "actionOutcome": "SUCCESS",
     *       "timestamp": "2025-10-21T14:30:00"
     *     }
     *   ],
     *   "totalCount": 450,
     *   "page": 0,
     *   "size": 20,
     *   "totalPages": 23
     * }
     * </pre>
     *
     * @param actorId Actor identifier
     * @param page Page number (0-based)
     * @param size Page size
     * @return 200 OK with AuditLogListResponse (may be empty)
     */
    @GET
    @Path("/actors/{actorId}")
    public Response getActorAuditTrail(@PathParam("actorId") String actorId,
                                       @QueryParam("page") @DefaultValue("0") int page,
                                       @QueryParam("size") @DefaultValue("20") int size) {
        LOGGER.log(Level.INFO, "GET /api/audit/actors/{0}?page={1}&size={2}",
                new Object[]{actorId, page, size});

        // Validate actorId
        if (actorId == null || actorId.trim().isEmpty()) {
            ErrorResponse error = ErrorResponse.badRequest("Actor ID is required");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build();
        }

        // Sanitize pagination
        page = Math.max(0, page);
        size = Math.max(MIN_PAGE_SIZE, Math.min(MAX_PAGE_SIZE, size));

        // Query audit service
        List<AuditLog> auditLogs = auditService.getAuditLogsByActor(actorId, page, size);

        // Convert to response DTOs
        List<AuditLogResponse> logResponses = auditLogs.stream()
                .map(AuditLogResponse::fromEntity)
                .collect(Collectors.toList());

        // Estimate total count
        int estimatedTotal = auditLogs.size();
        if (auditLogs.size() >= size) {
            estimatedTotal = (page + 2) * size;
        }

        AuditLogListResponse response = new AuditLogListResponse(
                logResponses,
                estimatedTotal,
                page,
                size
        );

        // Meta-audit: Log actor trail view
        auditService.logAccessEvent(
                "admin", // TODO: Extract from JWT
                "ADMIN",
                "AUDIT_LOG",
                actorId,
                ActionOutcome.SUCCESS,
                null,
                null,
                Map.of("action", "VIEW_ACTOR_TRAIL", "targetActorId", actorId,
                        "page", page, "size", size)
        );

        return Response.ok(response).build();
    }

    // =========================================================================
    // Health Check Endpoint (for testing)
    // =========================================================================

    /**
     * GET /api/audit/health - Health Check
     *
     * Simple endpoint to verify the audit API is running.
     *
     * @return 200 OK with status message
     */
    @GET
    @Path("/health")
    public Response healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "OK");
        status.put("service", "Audit API");
        status.put("timestamp", LocalDateTime.now().toString());
        status.put("totalEvents", auditService.getTotalAuditLogCount());

        return Response.ok(status).build();
    }
}
