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
 * - POST /api/access-requests - Create access request (professional/clinic)
 * - POST /api/access-requests/{requestId}/approve - Approve a request
 * - POST /api/access-requests/{requestId}/deny - Deny a request
 * - POST /api/access-requests/{requestId}/request-info - Request more information
 * - GET /api/access-requests/{requestId}/approved-document - Retrieve approved document (FHIR)
 *
 * Security:
 * - JWT authentication required for patient endpoints
 * - Clinic API key authentication for creation endpoint
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
    // POST /api/access-requests - Create Access Request
    // ================================================================

    /**
     * Creates a new access request from a professional/clinic.
     *
     * This endpoint is called by peripheral nodes when a professional needs access
     * to patient documents but doesn't have explicit permission via policies.
     *
     * Request Body:
     * - AccessRequestCreationDTO with professional/patient/document info
     *
     * @param request Access request creation data
     * @param securityContext Security context (contains clinic ID from API key)
     * @return 201 Created with new request details
     *         200 OK if duplicate request (idempotent)
     *         400 Bad Request if validation fails
     *         401 Unauthorized if clinic not authenticated
     *         404 Not Found if patient/document not found
     *         500 Internal Server Error if operation fails
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccessRequest(
            @Valid AccessRequestCreationDTO request,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "POST create access request: professional={0}, patient={1}",
                new Object[]{request.getProfessionalId(), request.getPatientCi()});

        try {
            // Extract clinic ID from SecurityContext (set by API key authentication filter)
            // For MVP, we'll use a simple approach - the clinic ID comes from the authenticated principal
            String clinicId = securityContext.getUserPrincipal() != null ?
                    securityContext.getUserPrincipal().getName() : null;

            if (clinicId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.unauthorized("Clinic authentication required"))
                        .build();
            }

            // TODO: Look up clinic name from clinic registry
            // For now, use clinic ID as name
            String clinicName = "Clinic " + clinicId;

            // Create access request
            AccessRequestCreationResponseDTO response =
                    accessRequestService.createAccessRequest(request, clinicId, clinicName);

            // Determine status code based on whether it's a new request or duplicate
            int statusCode = response.getIsNewRequest() ?
                    Response.Status.CREATED.getStatusCode() :
                    Response.Status.OK.getStatusCode();

            LOGGER.log(Level.INFO, "Access request created: requestId={0}, isNew={1}",
                    new Object[]{response.getRequestId(), response.getIsNewRequest()});

            return Response.status(statusCode)
                    .entity(response)
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid access request creation", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.validationError(e.getMessage()))
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating access request", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError(
                        "Failed to create access request: " + e.getMessage()))
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

    // ================================================================
    // GET /api/access-requests/{requestId}/approved-document - Get Approved Document
    // ================================================================

    /**
     * Retrieves the approved document in FHIR format for a professional.
     *
     * This endpoint allows professionals to retrieve clinical documents after patient approval.
     * The document is returned as a FHIR R4 DocumentReference resource with embedded
     * base64-encoded document content.
     *
     * Path Parameters:
     * - requestId: Access request ID
     *
     * Security:
     * - Professional authentication required (via clinic API key or JWT)
     * - Professional must own the access request
     * - Request must be in APPROVED status
     *
     * Response Format:
     * {
     *   "resourceType": "DocumentReference",
     *   "id": "doc-456",
     *   "status": "current",
     *   "type": {
     *     "coding": [{
     *       "system": "http://loinc.org",
     *       "code": "18725-2",
     *       "display": "LAB_RESULT"
     *     }]
     *   },
     *   "subject": {
     *     "reference": "Patient/12345678",
     *     "display": "Patient CI: 12345678"
     *   },
     *   "content": [{
     *     "attachment": {
     *       "contentType": "application/pdf",
     *       "data": "base64-encoded-content...",
     *       "hash": "sha256:abc123..."
     *     }
     *   }]
     * }
     *
     * @param requestId Request ID
     * @param securityContext Security context (contains professional ID from authentication)
     * @return 200 OK with FHIR DocumentReference resource
     *         400 Bad Request if request not approved or validation fails
     *         403 Forbidden if professional doesn't own the request
     *         404 Not Found if request, document, or clinic not found
     *         500 Internal Server Error if document retrieval fails
     *         502 Bad Gateway if peripheral node unavailable
     */
    @GET
    @Path("/{requestId}/approved-document")
    @Produces("application/fhir+json")
    public Response getApprovedDocument(
            @PathParam("requestId") Long requestId,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "GET /api/access-requests/{0}/approved-document", requestId);

        try {
            // Extract professional ID from SecurityContext (set by clinic API key authentication)
            String professionalId = securityContext.getUserPrincipal() != null ?
                    securityContext.getUserPrincipal().getName() : null;

            if (professionalId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.unauthorized("Professional authentication required"))
                        .build();
            }

            // Retrieve approved document from service
            org.hl7.fhir.r4.model.DocumentReference fhirDocument =
                    accessRequestService.getApprovedDocument(requestId, professionalId);

            // Serialize FHIR resource to JSON
            ca.uhn.fhir.context.FhirContext fhirContext = ca.uhn.fhir.context.FhirContext.forR4();
            String fhirJson = fhirContext.newJsonParser()
                    .setPrettyPrint(true)
                    .encodeResourceToString(fhirDocument);

            LOGGER.log(Level.INFO, "Successfully retrieved approved document for request: {0}", requestId);

            return Response.ok(fhirJson, "application/fhir+json")
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid approved document request: " + requestId, e);

            // Determine appropriate status code based on error message
            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.notFound("Resource", requestId.toString()))
                        .build();
            } else if (e.getMessage().contains("not authorized")) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(ErrorResponse.forbidden(e.getMessage()))
                        .build();
            } else if (e.getMessage().contains("status is")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError(e.getMessage()))
                        .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError(e.getMessage()))
                        .build();
            }

        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving approved document: " + requestId, e);

            // Check if it's a peripheral node error
            if (e.getMessage() != null && e.getMessage().contains("peripheral node")) {
                return Response.status(Response.Status.BAD_GATEWAY)
                        .entity(ErrorResponse.internalServerError(
                                "Peripheral node unavailable: " + e.getMessage()))
                        .build();
            }

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError(
                            "Failed to retrieve approved document: " + e.getMessage()))
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error retrieving approved document: " + requestId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError(
                            "Unexpected error: " + e.getMessage()))
                    .build();
        }
    }
}
