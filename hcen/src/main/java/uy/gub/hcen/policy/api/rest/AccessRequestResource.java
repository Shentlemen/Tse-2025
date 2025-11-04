package uy.gub.hcen.policy.api.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import uy.gub.hcen.api.dto.ErrorResponse;
import uy.gub.hcen.policy.dto.*;
import uy.gub.hcen.policy.entity.AccessRequest.RequestStatus;
import uy.gub.hcen.policy.service.AccessRequestService;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Access Request REST Resource
 *
 * JAX-RS resource providing REST API endpoints for patient management of
 * access requests. Enables patients to view, approve, and deny professional
 * access requests to their clinical documents.
 *
 * Base Path: /api/access-requests
 *
 * Endpoints:
 * - GET /api/access-requests?patientCi={ci}&status={status} - Get access requests
 * - POST /api/access-requests/{requestId}/approve - Approve a request
 * - POST /api/access-requests/{requestId}/deny - Deny a request
 * - POST /api/access-requests/{requestId}/request-info - Request more information
 *
 * Security:
 * - JWT authentication required (to be implemented)
 * - Patients can only view/act on their own requests
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-03
 */
@Path("/access-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccessRequestResource {

    private static final Logger LOGGER = Logger.getLogger(AccessRequestResource.class.getName());
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    @Inject
    private AccessRequestService accessRequestService;

    // ================================================================
    // GET /api/access-requests - Get Access Requests
    // ================================================================

    /**
     * Retrieves access requests for a patient with optional status filtering.
     *
     * Query Parameters:
     * - patientCi (required): Patient's CI
     * - status (optional): Filter by status (PENDING, APPROVED, DENIED, EXPIRED)
     * - page (optional): Page number (0-based, default: 0)
     * - size (optional): Page size (default: 20, max: 100)
     *
     * @param patientCi Patient's CI
     * @param statusStr Request status filter
     * @param page Page number
     * @param size Page size
     * @return 200 OK with AccessRequestListResponse
     *         400 Bad Request if parameters invalid
     *         500 Internal Server Error if operation fails
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccessRequests(
            @QueryParam("patientCi") String patientCi,
            @QueryParam("status") String statusStr,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        LOGGER.log(Level.INFO, "GET access requests for patient: {0}, status: {1}, page: {2}, size: {3}",
                new Object[]{patientCi, statusStr, page, size});

        try {
            // Validate required parameters
            if (patientCi == null || patientCi.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError("Patient CI is required"))
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

            // Parse status if provided
            RequestStatus status = null;
            if (statusStr != null && !statusStr.trim().isEmpty()) {
                try {
                    status = RequestStatus.valueOf(statusStr.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(ErrorResponse.validationError("Invalid status: " + statusStr +
                                    ". Valid values: PENDING, APPROVED, DENIED, EXPIRED"))
                            .build();
                }
            }

            // Get requests from service
            AccessRequestListResponse response = accessRequestService.getAccessRequests(
                    patientCi, status, page, size);

            LOGGER.log(Level.INFO, "Retrieved {0} access requests for patient: {1}",
                    new Object[]{response.getRequests().size(), patientCi});

            return Response.ok(response).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving access requests for patient: " + patientCi, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to retrieve access requests: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // POST /api/access-requests/{requestId}/approve - Approve Request
    // ================================================================

    /**
     * Approves an access request.
     *
     * Path Parameters:
     * - requestId: Access request ID
     *
     * Request Body:
     * - ApprovalDecisionDTO with optional reason
     *
     * @param requestId Request ID
     * @param decision Approval decision
     * @return 200 OK with success message
     *         400 Bad Request if validation fails
     *         404 Not Found if request doesn't exist
     *         500 Internal Server Error if operation fails
     */
    @POST
    @Path("/{requestId}/approve")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response approveRequest(
            @PathParam("requestId") Long requestId,
            @Valid ApprovalDecisionDTO decision,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "POST approve request: {0}", requestId);

        try {
            // Extract patient CI from SecurityContext (set by JwtAuthenticationFilter)
            String patientCi = securityContext.getUserPrincipal() != null ?
                    securityContext.getUserPrincipal().getName() : null;

            if (patientCi == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.unauthorized("Authentication required"))
                        .build();
            }

            String message = accessRequestService.approveRequest(requestId, patientCi, decision);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("message", message);

            LOGGER.log(Level.INFO, "Access request {0} approved successfully", requestId);

            return Response.ok(responseBody).build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid approval request: " + requestId, e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.validationError(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Cannot approve request: " + requestId, e);
            return Response.status(Response.Status.CONFLICT)
                    .entity(ErrorResponse.validationError(e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error approving access request: " + requestId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to approve request: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // POST /api/access-requests/{requestId}/deny - Deny Request
    // ================================================================

    /**
     * Denies an access request.
     *
     * Path Parameters:
     * - requestId: Access request ID
     *
     * Request Body:
     * - DenialDecisionDTO with required reason
     *
     * @param requestId Request ID
     * @param decision Denial decision
     * @return 200 OK with success message
     *         400 Bad Request if validation fails
     *         404 Not Found if request doesn't exist
     *         500 Internal Server Error if operation fails
     */
    @POST
    @Path("/{requestId}/deny")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response denyRequest(
            @PathParam("requestId") Long requestId,
            @Valid DenialDecisionDTO decision,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "POST deny request: {0}", requestId);

        try {
            // Extract patient CI from SecurityContext (set by JwtAuthenticationFilter)
            String patientCi = securityContext.getUserPrincipal() != null ?
                    securityContext.getUserPrincipal().getName() : null;

            if (patientCi == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.unauthorized("Authentication required"))
                        .build();
            }

            String message = accessRequestService.denyRequest(requestId, patientCi, decision);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("message", message);

            LOGGER.log(Level.INFO, "Access request {0} denied successfully", requestId);

            return Response.ok(responseBody).build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid denial request: " + requestId, e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.validationError(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Cannot deny request: " + requestId, e);
            return Response.status(Response.Status.CONFLICT)
                    .entity(ErrorResponse.validationError(e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error denying access request: " + requestId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to deny request: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // POST /api/access-requests/{requestId}/request-info - Request Info
    // ================================================================

    /**
     * Requests more information about an access request.
     *
     * Path Parameters:
     * - requestId: Access request ID
     *
     * Request Body:
     * - InfoRequestDTO with required question
     *
     * @param requestId Request ID
     * @param infoRequest Info request
     * @return 200 OK with success message
     *         400 Bad Request if validation fails
     *         404 Not Found if request doesn't exist
     *         500 Internal Server Error if operation fails
     */
    @POST
    @Path("/{requestId}/request-info")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestMoreInfo(
            @PathParam("requestId") Long requestId,
            @Valid InfoRequestDTO infoRequest,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "POST request info for request: {0}", requestId);

        try {
            // Extract patient CI from SecurityContext (set by JwtAuthenticationFilter)
            String patientCi = securityContext.getUserPrincipal() != null ?
                    securityContext.getUserPrincipal().getName() : null;

            if (patientCi == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.unauthorized("Authentication required"))
                        .build();
            }

            String message = accessRequestService.requestMoreInfo(requestId, patientCi, infoRequest);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("message", message);

            LOGGER.log(Level.INFO, "Info request sent for access request: {0}", requestId);

            return Response.ok(responseBody).build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid info request: " + requestId, e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.validationError(e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error requesting info for access request: " + requestId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to request info: " + e.getMessage()))
                    .build();
        }
    }
}
