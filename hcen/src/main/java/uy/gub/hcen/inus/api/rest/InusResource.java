package uy.gub.hcen.inus.api.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import uy.gub.hcen.api.dto.ErrorResponse;
import uy.gub.hcen.inus.dto.*;
import uy.gub.hcen.inus.entity.InusUser;
import uy.gub.hcen.inus.entity.UserStatus;
import uy.gub.hcen.service.inus.InusService;
import uy.gub.hcen.service.inus.exception.UserNotFoundException;
import uy.gub.hcen.service.inus.exception.UserRegistrationException;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * INUS REST Resource
 * <p>
 * JAX-RS REST API endpoints for INUS (National User Index) operations.
 * This resource provides the HTTP interface for user registration, lookup,
 * update, search, and eligibility validation.
 * <p>
 * Base Path: /api/inus/users
 * <p>
 * Endpoints:
 * - POST   /api/inus/users             - Register new user (AC013)
 * - GET    /api/inus/users/{ci}        - Get user by CI
 * - PUT    /api/inus/users/{ci}        - Update user profile
 * - GET    /api/inus/users             - List/search users (paginated)
 * - GET    /api/inus/users/{ci}/validate - Validate user eligibility
 * <p>
 * Authorization:
 * - User registration: API key from peripheral nodes (not yet implemented)
 * - User lookup/update: JWT with admin role (not yet implemented)
 * - Search/list: JWT with admin role (not yet implemented)
 * <p>
 * Response Formats:
 * - Success: 200 OK, 201 Created, 204 No Content
 * - Client Errors: 400 Bad Request, 404 Not Found, 409 Conflict
 * - Server Errors: 500 Internal Server Error
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 */
@Path("/inus/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InusResource {

    private static final Logger LOGGER = Logger.getLogger(InusResource.class.getName());

    @Inject
    private InusService inusService;

    @Context
    private UriInfo uriInfo;

    // ================================================================
    // POST /api/inus/users - User Registration (AC013)
    // ================================================================

    /**
     * Register a new user in the INUS system.
     * <p>
     * This endpoint is called by peripheral nodes (clinics, health providers),
     * admins, or patients themselves to register new patients in the national user index.
     * <p>
     * Request Body: UserRegistrationRequest (JSON)
     * - ci: Cédula de Identidad (required)
     * - firstName: User's first name (required)
     * - lastName: User's last name (required)
     * - dateOfBirth: Date of birth (required, must be in the past)
     * - email: Email address (optional)
     * - phoneNumber: Phone number (optional)
     * - clinicId: ID of clinic registering the user (optional)
     * <p>
     * Response:
     * - 201 Created: User successfully registered, body contains UserResponse
     * - 400 Bad Request: Validation error
     * - 409 Conflict: User already exists (idempotent - returns existing user)
     * - 500 Internal Server Error: System error
     *
     * @param request User registration request
     * @return Response with UserResponse and Location header
     */
    @POST
    public Response registerUser(@Valid UserRegistrationRequest request) {
        LOGGER.log(Level.INFO, "Processing user registration request for CI: {0}", request.getCi());

        try {
            // Call service to register user
            InusUser user = inusService.registerUser(
                    request.getCi(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getDateOfBirth(),
                    request.getEmail(),
                    request.getPhoneNumber(),
                    request.getClinicId()
            );

            // Convert to DTO
            UserResponse response = new UserResponse(user);

            // Build Location header: /api/inus/users/{ci}
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(user.getCi())
                    .build();

            LOGGER.log(Level.INFO, "User successfully registered - CI: {0}, INUS ID: {1}",
                    new Object[]{user.getCi(), user.getInusId()});

            // Return 201 Created with Location header
            return Response.created(location)
                    .entity(response)
                    .build();

        } catch (UserRegistrationException e) {
            LOGGER.log(Level.WARNING, "User registration failed: " + e.getMessage());

            // Check if it's a duplicate (idempotent behavior)
            // The service returns existing user, but we still return 201
            // If it's a validation error, return 400
            if (e.getMessage().contains("Invalid CI format") ||
                    e.getMessage().contains("cannot be null or empty") ||
                    e.getMessage().contains("cannot be in the future")) {

                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError(e.getMessage()))
                        .build();
            }

            // Other registration errors -> 400 Bad Request
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.badRequest(e.getMessage()))
                    .build();

        } catch (ValidationException e) {
            LOGGER.log(Level.WARNING, "Validation error during user registration: " + e.getMessage());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.validationError(e.getMessage()))
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during user registration", e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError(
                            "An unexpected error occurred during user registration"))
                    .build();
        }
    }

    // ================================================================
    // GET /api/inus/users/{ci} - User Lookup
    // ================================================================

    /**
     * Get user by CI (Cédula de Identidad).
     * <p>
     * Retrieves a single user from the INUS registry by their national ID.
     * Results are cached in Redis for performance (15-minute TTL).
     * <p>
     * Path Parameter:
     * - ci: Cédula de Identidad (e.g., 12345678 or 1.234.567-8)
     * <p>
     * Response:
     * - 200 OK: User found, body contains UserResponse
     * - 404 Not Found: User does not exist
     * - 500 Internal Server Error: System error
     *
     * @param ci User's Cédula de Identidad
     * @return Response with UserResponse
     */
    @GET
    @Path("/{ci}")
    public Response getUserByCi(@PathParam("ci") String ci) {
        LOGGER.log(Level.FINE, "Processing user lookup request for CI: {0}", ci);

        try {
            Optional<InusUser> userOpt = inusService.findUserByCi(ci);

            if (userOpt.isEmpty()) {
                LOGGER.log(Level.FINE, "User not found with CI: {0}", ci);

                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.notFound("User", ci))
                        .build();
            }

            // Convert to DTO
            UserResponse response = new UserResponse(userOpt.get());

            LOGGER.log(Level.FINE, "User found - CI: {0}", ci);

            return Response.ok(response).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during user lookup for CI: " + ci, e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError(
                            "An unexpected error occurred during user lookup"))
                    .build();
        }
    }

    // ================================================================
    // PUT /api/inus/users/{ci} - User Update
    // ================================================================

    /**
     * Update user profile information.
     * <p>
     * Allows updating mutable fields: firstName, lastName, email, phoneNumber.
     * Immutable fields (ci, inusId, dateOfBirth, status, ageVerified) cannot be changed.
     * <p>
     * Path Parameter:
     * - ci: Cédula de Identidad
     * <p>
     * Request Body: UserUpdateRequest (JSON)
     * - firstName: Updated first name (required)
     * - lastName: Updated last name (required)
     * - email: Updated email (optional)
     * - phoneNumber: Updated phone number (optional)
     * <p>
     * Response:
     * - 200 OK: User successfully updated, body contains updated UserResponse
     * - 400 Bad Request: Validation error
     * - 404 Not Found: User does not exist
     * - 500 Internal Server Error: System error
     *
     * @param ci      User's CI
     * @param request User update request
     * @return Response with updated UserResponse
     */
    @PUT
    @Path("/{ci}")
    public Response updateUser(@PathParam("ci") String ci, @Valid UserUpdateRequest request) {
        LOGGER.log(Level.INFO, "Processing user update request for CI: {0}", ci);

        try {
            // Call service to update user
            InusUser updatedUser = inusService.updateUserProfile(
                    ci,
                    request.getFirstName(),
                    request.getLastName(),
                    request.getEmail(),
                    request.getPhoneNumber()
            );

            // Convert to DTO
            UserResponse response = new UserResponse(updatedUser);

            LOGGER.log(Level.INFO, "User successfully updated - CI: {0}", ci);

            return Response.ok(response).build();

        } catch (UserNotFoundException e) {
            LOGGER.log(Level.WARNING, "Update failed - user not found: {0}", ci);

            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.notFound("User", ci))
                    .build();

        } catch (ValidationException e) {
            LOGGER.log(Level.WARNING, "Validation error during user update: " + e.getMessage());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.validationError(e.getMessage()))
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid input during user update: " + e.getMessage());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.badRequest(e.getMessage()))
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during user update for CI: " + ci, e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError(
                            "An unexpected error occurred during user update"))
                    .build();
        }
    }

    // ================================================================
    // GET /api/inus/users - List/Search Users (Paginated)
    // ================================================================

    /**
     * List or search users with pagination.
     * <p>
     * This endpoint supports multiple query modes:
     * - No parameters: List all users (paginated)
     * - search parameter: Full-text search in firstName, lastName, CI, inusId
     * - status parameter: Filter by user status (ACTIVE, INACTIVE, SUSPENDED)
     * <p>
     * Query Parameters:
     * - search: Search query (optional)
     * - status: User status filter (optional)
     * - page: Page number, 0-based (default: 0)
     * - size: Number of items per page (default: 20)
     * <p>
     * Response:
     * - 200 OK: Body contains UserListResponse with pagination metadata
     * - 400 Bad Request: Invalid pagination parameters
     * - 500 Internal Server Error: System error
     *
     * @param search Search query (optional)
     * @param status User status filter (optional)
     * @param page   Page number (0-based, default 0)
     * @param size   Page size (default 20)
     * @return Response with UserListResponse
     */
    @GET
    public Response listUsers(
            @QueryParam("search") String search,
            @QueryParam("status") String status,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        LOGGER.log(Level.FINE, "Processing user list/search request - search: {0}, status: {1}, page: {2}, size: {3}",
                new Object[]{search, status, page, size});

        try {
            // Validate pagination parameters
            if (page < 0 || size <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.badRequest(
                                "Invalid pagination parameters: page must be >= 0 and size must be > 0"))
                        .build();
            }

            // Maximum page size to prevent abuse
            if (size > 100) {
                size = 100;
                LOGGER.log(Level.WARNING, "Page size capped at 100");
            }

            List<InusUser> users;
            long totalCount;

            // Determine query mode
            if (search != null && !search.trim().isEmpty()) {
                // Search mode
                users = inusService.searchUsers(search.trim(), page, size);
                // Note: For production, we should get exact count from repository
                // For now, estimate based on result size
                totalCount = users.size() < size ? page * size + users.size() : (page + 2) * size;

                LOGGER.log(Level.FINE, "Search completed - query: {0}, results: {1}",
                        new Object[]{search, users.size()});

            } else if (status != null && !status.trim().isEmpty()) {
                // Status filter mode
                try {
                    UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());
                    // Note: We need to add findByStatus() method to InusService
                    // For now, fall back to all users and filter
                    List<InusUser> allUsers = inusService.findAllUsers(page, size);
                    users = allUsers.stream()
                            .filter(u -> u.getStatus() == userStatus)
                            .toList();

                    totalCount = inusService.countUsersByStatus(userStatus);

                    LOGGER.log(Level.FINE, "Status filter completed - status: {0}, results: {1}",
                            new Object[]{status, users.size()});

                } catch (IllegalArgumentException e) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(ErrorResponse.badRequest(
                                    "Invalid status value. Must be one of: ACTIVE, INACTIVE, SUSPENDED"))
                            .build();
                }

            } else {
                // List all mode
                users = inusService.findAllUsers(page, size);
                totalCount = inusService.countUsers();

                LOGGER.log(Level.FINE, "User list completed - page: {0}, size: {1}, results: {2}",
                        new Object[]{page, size, users.size()});
            }

            // Build response
            UserListResponse response = new UserListResponse(users, totalCount, page, size);

            return Response.ok(response).build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid parameters for user list: " + e.getMessage());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.badRequest(e.getMessage()))
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during user list/search", e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError(
                            "An unexpected error occurred during user list"))
                    .build();
        }
    }

    // ================================================================
    // GET /api/inus/users/{ci}/validate - Validate User Eligibility
    // ================================================================

    /**
     * Validate if a user is eligible for the health system.
     * <p>
     * Eligibility Criteria:
     * - User exists in INUS registry
     * - User status is ACTIVE
     * - User age has been verified (18+ years old)
     * <p>
     * Path Parameter:
     * - ci: Cédula de Identidad
     * <p>
     * Response:
     * - 200 OK: Body contains EligibilityResponse
     *   - eligible: true/false
     *   - reason: Detailed explanation
     * - 500 Internal Server Error: System error
     *
     * @param ci User's CI
     * @return Response with EligibilityResponse
     */
    @GET
    @Path("/{ci}/validate")
    public Response validateUserEligibility(@PathParam("ci") String ci) {
        LOGGER.log(Level.FINE, "Processing eligibility validation request for CI: {0}", ci);

        try {
            // Call service to validate eligibility
            boolean isEligible = inusService.validateUserEligibility(ci);

            EligibilityResponse response;

            if (isEligible) {
                response = EligibilityResponse.eligible(ci);
            } else {
                // Get detailed reason by checking user
                Optional<InusUser> userOpt = inusService.findUserByCi(ci);

                if (userOpt.isEmpty()) {
                    response = EligibilityResponse.notFound(ci);
                } else {
                    InusUser user = userOpt.get();

                    if (user.getStatus() != UserStatus.ACTIVE) {
                        if (user.getStatus() == UserStatus.SUSPENDED) {
                            response = EligibilityResponse.suspended(ci);
                        } else {
                            response = EligibilityResponse.notActive(ci);
                        }
                    } else if (!user.getAgeVerified()) {
                        response = EligibilityResponse.ageNotVerified(ci);
                    } else {
                        // Should not happen, but handle gracefully
                        response = new EligibilityResponse(ci, false, "Unknown eligibility issue");
                    }
                }
            }

            LOGGER.log(Level.FINE, "Eligibility validation completed - CI: {0}, eligible: {1}",
                    new Object[]{ci, response.isEligible()});

            return Response.ok(response).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during eligibility validation for CI: " + ci, e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError(
                            "An unexpected error occurred during eligibility validation"))
                    .build();
        }
    }
}
