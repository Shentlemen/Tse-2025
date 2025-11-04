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
import uy.gub.hcen.policy.service.PolicyManagementService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Policy Management REST Resource
 *
 * JAX-RS resource providing REST API endpoints for patient access policy management.
 * Enables patients to create, view, update, and delete their access control policies.
 *
 * Base Path: /api/policies
 *
 * Endpoints:
 * - GET /api/policies - Get patient policies (with optional includeExpired flag)
 * - GET /api/policies/{policyId} - Get specific policy by ID
 * - GET /api/policies/count - Count active policies
 * - GET /api/policies/templates - Get policy templates for UI
 * - POST /api/policies - Create new policy
 * - PUT /api/policies/{policyId} - Update existing policy
 * - DELETE /api/policies/{policyId} - Delete specific policy
 *
 * Security:
 * - JWT authentication required (via SecurityContext)
 * - Patients can only manage their own policies
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-04
 */
@Path("/api/policies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PolicyManagementResource {

    private static final Logger LOGGER = Logger.getLogger(PolicyManagementResource.class.getName());

    @Inject
    private PolicyManagementService policyManagementService;

    // ================================================================
    // GET /api/policies - Get Patient Policies
    // ================================================================

    /**
     * Retrieves access policies for the authenticated patient.
     *
     * Query Parameters:
     * - includeExpired (optional): Include expired policies (default: false)
     *
     * @param includeExpired Whether to include expired policies
     * @param securityContext Security context with authenticated user
     * @return 200 OK with list of PolicyResponse
     *         401 Unauthorized if not authenticated
     *         500 Internal Server Error if operation fails
     */
    @GET
    public Response getPolicies(
            @QueryParam("includeExpired") @DefaultValue("false") boolean includeExpired,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "GET policies (includeExpired: {0})", includeExpired);

        try {
            // Extract patient CI from SecurityContext
            String patientCi = extractPatientCi(securityContext);
            if (patientCi == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.unauthorized("Authentication required"))
                        .build();
            }

            List<PolicyResponse> policies;
            if (includeExpired) {
                policies = policyManagementService.getAllPatientPolicies(patientCi);
            } else {
                policies = policyManagementService.getPatientPolicies(patientCi);
            }

            LOGGER.log(Level.INFO, "Retrieved {0} policies for patient: {1}",
                    new Object[]{policies.size(), patientCi});

            return Response.ok(policies).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving policies", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to retrieve policies: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // GET /api/policies/{policyId} - Get Specific Policy
    // ================================================================

    /**
     * Retrieves a specific policy by ID with ownership verification.
     *
     * Path Parameters:
     * - policyId: Policy ID
     *
     * @param policyId Policy ID
     * @param securityContext Security context with authenticated user
     * @return 200 OK with PolicyResponse
     *         401 Unauthorized if not authenticated
     *         403 Forbidden if policy doesn't belong to patient
     *         404 Not Found if policy doesn't exist
     *         500 Internal Server Error if operation fails
     */
    @GET
    @Path("/{policyId}")
    public Response getPolicy(
            @PathParam("policyId") Long policyId,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "GET policy: {0}", policyId);

        try {
            // Extract patient CI from SecurityContext
            String patientCi = extractPatientCi(securityContext);
            if (patientCi == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.unauthorized("Authentication required"))
                        .build();
            }

            Optional<PolicyResponse> policyOpt = policyManagementService.getPolicy(policyId, patientCi);

            if (policyOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.notFound("Policy", policyId.toString()))
                        .build();
            }

            LOGGER.log(Level.INFO, "Retrieved policy: {0} for patient: {1}",
                    new Object[]{policyId, patientCi});

            return Response.ok(policyOpt.get()).build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Unauthorized access to policy: " + policyId, e);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ErrorResponse.forbidden(e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving policy: " + policyId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to retrieve policy: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // GET /api/policies/count - Count Active Policies
    // ================================================================

    /**
     * Counts active policies for the authenticated patient.
     *
     * @param securityContext Security context with authenticated user
     * @return 200 OK with count
     *         401 Unauthorized if not authenticated
     *         500 Internal Server Error if operation fails
     */
    @GET
    @Path("/count")
    public Response countPolicies(@Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "GET policy count");

        try {
            // Extract patient CI from SecurityContext
            String patientCi = extractPatientCi(securityContext);
            if (patientCi == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.unauthorized("Authentication required"))
                        .build();
            }

            long count = policyManagementService.countActivePolicies(patientCi);

            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            response.put("patientCi", patientCi);

            LOGGER.log(Level.INFO, "Patient {0} has {1} active policies",
                    new Object[]{patientCi, count});

            return Response.ok(response).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting policies", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to count policies: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // GET /api/policies/templates - Get Policy Templates
    // ================================================================

    /**
     * Retrieves policy templates for UI configuration.
     *
     * Returns metadata for all available policy types including:
     * - Policy type information
     * - JSON schema for configuration
     * - Example configurations
     *
     * @return 200 OK with list of PolicyTemplateDTO
     */
    @GET
    @Path("/templates")
    public Response getPolicyTemplates() {

        LOGGER.log(Level.INFO, "GET policy templates");

        try {
            List<PolicyTemplateDTO> templates = PolicyTemplateDTO.getAllTemplates();

            LOGGER.log(Level.INFO, "Retrieved {0} policy templates", templates.size());

            return Response.ok(templates).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving policy templates", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to retrieve templates: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // POST /api/policies - Create Policy
    // ================================================================

    /**
     * Creates a new access policy for the authenticated patient.
     *
     * Request Body:
     * - PolicyCreateRequest with policy configuration
     *
     * @param request Policy creation request
     * @param securityContext Security context with authenticated user
     * @return 201 Created with PolicyResponse
     *         400 Bad Request if validation fails
     *         401 Unauthorized if not authenticated
     *         500 Internal Server Error if operation fails
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPolicy(
            @Valid PolicyCreateRequest request,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "POST create policy");

        try {
            // Extract patient CI from SecurityContext
            String patientCi = extractPatientCi(securityContext);
            if (patientCi == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.unauthorized("Authentication required"))
                        .build();
            }

            // Ensure request has patient CI (override if present to prevent tampering)
            request.setPatientCi(patientCi);

            PolicyResponse createdPolicy = policyManagementService.createPolicy(request);

            LOGGER.log(Level.INFO, "Policy created successfully: ID={0} for patient: {1}",
                    new Object[]{createdPolicy.getId(), patientCi});

            return Response.status(Response.Status.CREATED)
                    .entity(createdPolicy)
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid policy creation request", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.validationError(e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating policy", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to create policy: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // PUT /api/policies/{policyId} - Update Policy
    // ================================================================

    /**
     * Updates an existing policy with ownership verification.
     *
     * Path Parameters:
     * - policyId: Policy ID
     *
     * Request Body:
     * - PolicyUpdateRequest with fields to update
     *
     * @param policyId Policy ID
     * @param request Update request
     * @param securityContext Security context with authenticated user
     * @return 200 OK with updated PolicyResponse
     *         400 Bad Request if validation fails
     *         401 Unauthorized if not authenticated
     *         403 Forbidden if policy doesn't belong to patient
     *         404 Not Found if policy doesn't exist
     *         500 Internal Server Error if operation fails
     */
    @PUT
    @Path("/{policyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePolicy(
            @PathParam("policyId") Long policyId,
            @Valid PolicyUpdateRequest request,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "PUT update policy: {0}", policyId);

        try {
            // Extract patient CI from SecurityContext
            String patientCi = extractPatientCi(securityContext);
            if (patientCi == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.unauthorized("Authentication required"))
                        .build();
            }

            PolicyResponse updatedPolicy = policyManagementService.updatePolicy(policyId, request, patientCi);

            LOGGER.log(Level.INFO, "Policy updated successfully: {0} for patient: {1}",
                    new Object[]{policyId, patientCi});

            return Response.ok(updatedPolicy).build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid policy update request: " + policyId, e);

            // Check if it's an authorization error
            if (e.getMessage().contains("does not belong to")) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(ErrorResponse.forbidden(e.getMessage()))
                        .build();
            }

            // Check if it's a not found error
            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.notFound("Policy", policyId.toString()))
                        .build();
            }

            // Other validation errors
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.validationError(e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating policy: " + policyId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to update policy: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // DELETE /api/policies/{policyId} - Delete Policy
    // ================================================================

    /**
     * Deletes a policy with ownership verification.
     *
     * Path Parameters:
     * - policyId: Policy ID
     *
     * @param policyId Policy ID
     * @param securityContext Security context with authenticated user
     * @return 200 OK with success message
     *         401 Unauthorized if not authenticated
     *         403 Forbidden if policy doesn't belong to patient
     *         404 Not Found if policy doesn't exist
     *         500 Internal Server Error if operation fails
     */
    @DELETE
    @Path("/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePolicy(
            @PathParam("policyId") Long policyId,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "DELETE policy: {0}", policyId);

        try {
            // Extract patient CI from SecurityContext
            String patientCi = extractPatientCi(securityContext);
            if (patientCi == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.unauthorized("Authentication required"))
                        .build();
            }

            policyManagementService.deletePolicy(policyId, patientCi);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Policy deleted successfully");
            response.put("policyId", policyId);

            LOGGER.log(Level.INFO, "Policy deleted successfully: {0} for patient: {1}",
                    new Object[]{policyId, patientCi});

            return Response.ok(response).build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid policy deletion request: " + policyId, e);

            // Check if it's an authorization error
            if (e.getMessage().contains("does not belong to")) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(ErrorResponse.forbidden(e.getMessage()))
                        .build();
            }

            // Check if it's a not found error
            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.notFound("Policy", policyId.toString()))
                        .build();
            }

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.validationError(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete policy: " + policyId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError(e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting policy: " + policyId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to delete policy: " + e.getMessage()))
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
