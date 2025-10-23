package uy.gub.hcen.service.clinic.api.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.hcen.api.dto.ErrorResponse;
import uy.gub.hcen.clinic.entity.Clinic.ClinicStatus;
import uy.gub.hcen.service.clinic.ClinicManagementService;
import uy.gub.hcen.service.clinic.dto.*;
import uy.gub.hcen.service.clinic.exception.ClinicNotFoundException;
import uy.gub.hcen.service.clinic.exception.ClinicRegistrationException;
import uy.gub.hcen.service.clinic.exception.OnboardingException;

import java.net.URI;

/**
 * Clinic Management REST Resource
 * <p>
 * JAX-RS REST API endpoints for clinic management operations.
 * This resource provides the HTTP interface for clinic registration, lookup,
 * update, activation/deactivation, onboarding, and statistics.
 * <p>
 * Base Path: /api/admin/clinics
 * <p>
 * Endpoints:
 * - POST   /api/admin/clinics                    - Register new clinic (CU10)
 * - GET    /api/admin/clinics                    - List/search clinics (paginated)
 * - GET    /api/admin/clinics/{clinicId}         - Get clinic by ID
 * - PUT    /api/admin/clinics/{clinicId}         - Update clinic
 * - POST   /api/admin/clinics/{clinicId}/onboard - Onboard clinic (AC016)
 * - DELETE /api/admin/clinics/{clinicId}         - Deactivate clinic (soft delete)
 * <p>
 * Authorization:
 * - All endpoints require JWT with ADMIN role (to be implemented with JwtAuthenticationFilter)
 * <p>
 * Response Formats:
 * - Success: 200 OK, 201 Created, 204 No Content
 * - Client Errors: 400 Bad Request, 404 Not Found, 409 Conflict
 * - Server Errors: 500 Internal Server Error
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
@Path("/admin/clinics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClinicResource {

    private static final Logger logger = LoggerFactory.getLogger(ClinicResource.class);

    @Inject
    private ClinicManagementService clinicManagementService;

    @Context
    private UriInfo uriInfo;

    // ================================================================
    // POST /api/admin/clinics - Clinic Registration (CU10)
    // ================================================================

    /**
     * Register a new clinic in the HCEN system.
     * <p>
     * This endpoint is called by HCEN administrators to register new clinics/health facilities.
     * <p>
     * Request Body: ClinicRegistrationRequest (JSON)
     * - clinicName: Official clinic name (required)
     * - address: Physical address (required)
     * - city: City location (required)
     * - phoneNumber: Contact phone (required, format: 024123456 or 099123456)
     * - email: Contact email (required)
     * - peripheralNodeUrl: Peripheral node API URL (required, format: https://...)
     * <p>
     * Response:
     * - 201 Created: Clinic successfully registered, body contains ClinicResponse with UNMASKED API key
     * - 400 Bad Request: Validation error
     * - 500 Internal Server Error: System error
     * <p>
     * Security Note: API key is returned UNMASKED only in this response. Store it securely.
     * Subsequent responses will mask the API key for security.
     *
     * @param request Clinic registration request
     * @return Response with ClinicResponse (unmasked API key) and Location header
     */
    @POST
    public Response registerClinic(@Valid ClinicRegistrationRequest request) {
        logger.info("Processing clinic registration request for: {}", request.getClinicName());

        try {
            // Register clinic
            ClinicResponse response = clinicManagementService.registerClinic(request);

            // Build Location header (URI of newly created clinic)
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(response.getClinicId())
                    .build();

            logger.info("Successfully registered clinic with ID: {}", response.getClinicId());

            // Return 201 Created with Location header
            return Response.created(location)
                    .entity(response)
                    .build();

        } catch (ClinicRegistrationException e) {
            logger.error("Clinic registration failed: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("CLINIC_REGISTRATION_FAILED", e.getMessage()))
                    .build();
        } catch (Exception e) {
            logger.error("Unexpected error during clinic registration", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
                    .build();
        }
    }

    // ================================================================
    // GET /api/admin/clinics - List/Search Clinics
    // ================================================================

    /**
     * List or search clinics with pagination and filtering.
     * <p>
     * Query Parameters:
     * - status: Filter by status (ACTIVE, INACTIVE, PENDING_ONBOARDING) [optional]
     * - city: Filter by city [optional]
     * - page: Page number (zero-based, default: 0) [optional]
     * - size: Page size (default: 20, max: 100) [optional]
     * <p>
     * Response:
     * - 200 OK: ClinicListResponse with clinics and pagination metadata
     * - 400 Bad Request: Invalid query parameters
     * - 500 Internal Server Error: System error
     *
     * @param status Filter by status (optional)
     * @param city Filter by city (optional)
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @return Response with ClinicListResponse
     */
    @GET
    public Response listClinics(
            @QueryParam("status") String status,
            @QueryParam("city") String city,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        logger.debug("Listing clinics (status={}, city={}, page={}, size={})", status, city, page, size);

        try {
            // Validate pagination parameters
            if (page < 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("INVALID_PARAMETER", "Page number cannot be negative"))
                        .build();
            }
            if (size <= 0 || size > 100) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("INVALID_PARAMETER", "Page size must be between 1 and 100"))
                        .build();
            }

            ClinicListResponse response;

            // Apply filters
            if (status != null && !status.isBlank()) {
                // Filter by status
                try {
                    ClinicStatus clinicStatus = ClinicStatus.valueOf(status.toUpperCase());
                    response = clinicManagementService.findClinicsByStatus(clinicStatus, page, size);
                } catch (IllegalArgumentException e) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorResponse("INVALID_STATUS",
                                    "Invalid status. Must be one of: ACTIVE, INACTIVE, PENDING_ONBOARDING"))
                            .build();
                }
            } else if (city != null && !city.isBlank()) {
                // Filter by city
                response = clinicManagementService.findClinicsByCity(city, page, size);
            } else {
                // List all
                response = clinicManagementService.listClinics(page, size);
            }

            logger.debug("Returning {} clinics (page {}/{})", response.getClinics().size(),
                    page + 1, response.getTotalPages());

            return Response.ok(response).build();

        } catch (Exception e) {
            logger.error("Unexpected error while listing clinics", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
                    .build();
        }
    }

    // ================================================================
    // GET /api/admin/clinics/{clinicId} - Get Clinic by ID
    // ================================================================

    /**
     * Get clinic details by clinic ID.
     * <p>
     * Path Parameters:
     * - clinicId: Unique clinic identifier
     * <p>
     * Response:
     * - 200 OK: ClinicResponse with clinic details (masked API key)
     * - 404 Not Found: Clinic not found
     * - 500 Internal Server Error: System error
     *
     * @param clinicId Clinic ID
     * @return Response with ClinicResponse
     */
    @GET
    @Path("/{clinicId}")
    public Response getClinic(@PathParam("clinicId") String clinicId) {
        logger.debug("Getting clinic by ID: {}", clinicId);

        try {
            ClinicResponse response = clinicManagementService.findClinicById(clinicId);

            return Response.ok(response).build();

        } catch (ClinicNotFoundException e) {
            logger.warn("Clinic not found: {}", clinicId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("CLINIC_NOT_FOUND", e.getMessage()))
                    .build();
        } catch (Exception e) {
            logger.error("Unexpected error while fetching clinic: {}", clinicId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
                    .build();
        }
    }

    // ================================================================
    // PUT /api/admin/clinics/{clinicId} - Update Clinic
    // ================================================================

    /**
     * Update clinic information.
     * <p>
     * Path Parameters:
     * - clinicId: Unique clinic identifier
     * <p>
     * Request Body: ClinicUpdateRequest (JSON)
     * - clinicName: Updated clinic name (optional)
     * - address: Updated address (optional)
     * - city: Updated city (optional)
     * - phoneNumber: Updated phone (optional)
     * - email: Updated email (optional)
     * - peripheralNodeUrl: Updated peripheral node URL (optional)
     * <p>
     * Note: Only fields provided in the request are updated.
     * Clinic ID and status cannot be updated through this endpoint.
     * <p>
     * Response:
     * - 200 OK: ClinicResponse with updated clinic details
     * - 400 Bad Request: Validation error
     * - 404 Not Found: Clinic not found
     * - 500 Internal Server Error: System error
     *
     * @param clinicId Clinic ID
     * @param request Clinic update request
     * @return Response with ClinicResponse
     */
    @PUT
    @Path("/{clinicId}")
    public Response updateClinic(
            @PathParam("clinicId") String clinicId,
            @Valid ClinicUpdateRequest request) {

        logger.info("Updating clinic: {}", clinicId);

        try {
            ClinicResponse response = clinicManagementService.updateClinic(clinicId, request);

            logger.info("Successfully updated clinic: {}", clinicId);

            return Response.ok(response).build();

        } catch (ClinicNotFoundException e) {
            logger.warn("Clinic not found for update: {}", clinicId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("CLINIC_NOT_FOUND", e.getMessage()))
                    .build();
        } catch (Exception e) {
            logger.error("Unexpected error while updating clinic: {}", clinicId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
                    .build();
        }
    }

    // ================================================================
    // POST /api/admin/clinics/{clinicId}/onboard - Onboard Clinic (AC016)
    // ================================================================

    /**
     * Onboard a clinic to its peripheral node (AC016).
     * <p>
     * This endpoint triggers the onboarding process, which sends configuration data
     * to the peripheral node. On successful onboarding, clinic status transitions to ACTIVE.
     * <p>
     * Path Parameters:
     * - clinicId: Unique clinic identifier
     * <p>
     * Response:
     * - 200 OK: OnboardingResponse with success status
     * - 404 Not Found: Clinic not found
     * - 409 Conflict: Clinic already onboarded
     * - 500 Internal Server Error: System error or peripheral node error
     *
     * @param clinicId Clinic ID
     * @return Response with OnboardingResponse
     */
    @POST
    @Path("/{clinicId}/onboard")
    public Response onboardClinic(@PathParam("clinicId") String clinicId) {
        logger.info("Initiating onboarding for clinic: {}", clinicId);

        try {
            OnboardingResponse response = clinicManagementService.onboardClinic(clinicId);

            if ("ALREADY_ONBOARDED".equals(response.getStatus())) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(response)
                        .build();
            }

            logger.info("Successfully onboarded clinic: {}", clinicId);

            return Response.ok(response).build();

        } catch (ClinicNotFoundException e) {
            logger.warn("Clinic not found for onboarding: {}", clinicId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("CLINIC_NOT_FOUND", e.getMessage()))
                    .build();
        } catch (OnboardingException e) {
            logger.error("Onboarding failed for clinic: {}", clinicId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("ONBOARDING_FAILED", e.getMessage()))
                    .build();
        } catch (Exception e) {
            logger.error("Unexpected error during onboarding: {}", clinicId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
                    .build();
        }
    }

    // ================================================================
    // DELETE /api/admin/clinics/{clinicId} - Deactivate Clinic (Soft Delete)
    // ================================================================

    /**
     * Deactivate a clinic (soft delete).
     * <p>
     * This endpoint sets the clinic status to INACTIVE. The clinic record is NOT deleted
     * from the database, but the clinic can no longer register users or documents.
     * <p>
     * Path Parameters:
     * - clinicId: Unique clinic identifier
     * <p>
     * Response:
     * - 204 No Content: Clinic successfully deactivated
     * - 404 Not Found: Clinic not found
     * - 500 Internal Server Error: System error
     *
     * @param clinicId Clinic ID
     * @return Response with no content
     */
    @DELETE
    @Path("/{clinicId}")
    public Response deactivateClinic(@PathParam("clinicId") String clinicId) {
        logger.info("Deactivating clinic: {}", clinicId);

        try {
            clinicManagementService.deactivateClinic(clinicId);

            logger.info("Successfully deactivated clinic: {}", clinicId);

            return Response.noContent().build();

        } catch (ClinicNotFoundException e) {
            logger.warn("Clinic not found for deactivation: {}", clinicId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("CLINIC_NOT_FOUND", e.getMessage()))
                    .build();
        } catch (Exception e) {
            logger.error("Unexpected error while deactivating clinic: {}", clinicId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
                    .build();
        }
    }

    // ================================================================
    // GET /api/admin/clinics/{clinicId}/statistics - Get Clinic Statistics
    // ================================================================

    /**
     * Get statistics for a specific clinic (CU11).
     * <p>
     * Returns statistics including:
     * - Total users registered by clinic
     * - Total documents registered by clinic
     * - Total policies involving clinic
     * <p>
     * Path Parameters:
     * - clinicId: Unique clinic identifier
     * <p>
     * Response:
     * - 200 OK: ClinicStatisticsResponse with statistics
     * - 404 Not Found: Clinic not found
     * - 500 Internal Server Error: System error
     *
     * @param clinicId Clinic ID
     * @return Response with ClinicStatisticsResponse
     */
    @GET
    @Path("/{clinicId}/statistics")
    public Response getClinicStatistics(@PathParam("clinicId") String clinicId) {
        logger.debug("Fetching statistics for clinic: {}", clinicId);

        try {
            ClinicStatisticsResponse response = clinicManagementService.getClinicStatistics(clinicId);

            return Response.ok(response).build();

        } catch (ClinicNotFoundException e) {
            logger.warn("Clinic not found for statistics: {}", clinicId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("CLINIC_NOT_FOUND", e.getMessage()))
                    .build();
        } catch (Exception e) {
            logger.error("Unexpected error while fetching clinic statistics: {}", clinicId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
                    .build();
        }
    }
}
