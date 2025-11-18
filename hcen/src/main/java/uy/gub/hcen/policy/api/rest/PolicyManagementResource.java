package uy.gub.hcen.policy.api.rest;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import uy.gub.hcen.api.dto.ErrorResponse;
import uy.gub.hcen.policy.dto.*;
import uy.gub.hcen.policy.entity.MedicalSpecialty;
import uy.gub.hcen.policy.entity.PolicyStatus;
import uy.gub.hcen.policy.service.PolicyManagementService;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Policy Management REST Resource
 *
 * JAX-RS resource providing REST API endpoints for patient access policy management.
 * Updated for the simplified clinic+specialty policy model.
 *
 * Base Path: /api/policies
 *
 * @author TSE 2025 Group 9
 * @version 2.0
 * @since 2025-11-18
 */
@Path("/policies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PolicyManagementResource {

    private static final Logger LOGGER = Logger.getLogger(PolicyManagementResource.class.getName());

    @Inject
    private PolicyManagementService policyManagementService;

    @PersistenceContext(unitName = "hcen-pu")
    private EntityManager entityManager;

    // ================================================================
    // GET /api/policies - Get Patient Policies
    // ================================================================

    @GET
    public Response getPolicies(
            @QueryParam("includeExpired") @DefaultValue("false") boolean includeExpired,
            @QueryParam("patientCi") String patientCiParam,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "GET policies (includeExpired: {0})", includeExpired);

        try {
            // Try to get from query param first (for development), then from SecurityContext
            String patientCi = patientCiParam;
            if (patientCi == null || patientCi.trim().isEmpty()) {
                patientCi = extractPatientCi(securityContext);
            }
            if (patientCi == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.unauthorized("Patient Ci is required."))
                        .build();
            }

            List<PolicyResponse> policies;
            if (includeExpired) {
                policies = policyManagementService.getAllPatientPolicies(patientCi);
            } else {
                policies = policyManagementService.getPatientPolicies(patientCi);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("policies", policies);
            response.put("count", policies.size());

            LOGGER.log(Level.INFO, "Retrieved {0} policies for patient: {1}",
                    new Object[]{policies.size(), patientCi});

            return Response.ok(response).build();

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

    @GET
    @Path("/{policyId}")
    public Response getPolicy(
            @PathParam("policyId") Long policyId,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "GET policy: {0}", policyId);

        try {
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

    @GET
    @Path("/count")
    public Response countPolicies(@Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "GET policy count");

        try {
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

            return Response.ok(response).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting policies", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to count policies: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // GET /api/policies/specialties - Get All Medical Specialties
    // ================================================================

    @GET
    @Path("/specialties")
    public Response getSpecialties() {

        LOGGER.log(Level.INFO, "GET specialties");

        try {
            List<Map<String, String>> specialties = new ArrayList<>();

            for (MedicalSpecialty specialty : MedicalSpecialty.values()) {
                Map<String, String> item = new HashMap<>();
                item.put("value", specialty.name());
                item.put("label", specialty.getDisplayName());
                specialties.add(item);
            }

            LOGGER.log(Level.INFO, "Retrieved {0} specialties", specialties.size());

            return Response.ok(specialties).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving specialties", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to retrieve specialties: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // GET /api/policies/clinics - Get Active Clinics
    // ================================================================

    @GET
    @Path("/clinics")
    public Response getActiveClinics() {

        LOGGER.log(Level.INFO, "GET active clinics");

        try {
            // Query active clinics from database
            TypedQuery<Object[]> query = entityManager.createQuery(
//                    "SELECT c.clinicId, c.clinicName FROM Clinic c WHERE c.status = 'ACTIVE' ORDER BY c.clinicName",
                    "SELECT c.clinicId, c.clinicName FROM Clinic c  ORDER BY c.clinicName",
                    Object[].class
            );

            List<Object[]> results = query.getResultList();
            List<Map<String, String>> clinics = new ArrayList<>();

            for (Object[] row : results) {
                Map<String, String> clinic = new HashMap<>();
                clinic.put("value", (String) row[0]);
                clinic.put("label", (String) row[1]);
                clinics.add(clinic);
            }

            LOGGER.log(Level.INFO, "Retrieved {0} active clinics", clinics.size());

            return Response.ok(clinics).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving clinics", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to retrieve clinics: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // POST /api/policies - Create Policy
    // ================================================================

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPolicy(
            @Valid PolicyCreateRequest request,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "POST create policy");

        try {
            // Try to get from request body first (for development), then from SecurityContext
            String patientCi = request.getPatientCi();
            if (patientCi == null || patientCi.trim().isEmpty()) {
                patientCi = extractPatientCi(securityContext);
            }
            if (patientCi == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.unauthorized("Authentication required"))
                        .build();
            }

            // Ensure request has patient CI
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
    // PUT /api/policies/{policyId}/revoke - Revoke Policy
    // ================================================================

    @PUT
    @Path("/{policyId}/revoke")
    @Produces(MediaType.APPLICATION_JSON)
    public Response revokePolicy(
            @PathParam("policyId") Long policyId,
            @QueryParam("patientCi") String patientCiParam,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "PUT revoke policy: {0}", policyId);

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

            PolicyResponse revokedPolicy = policyManagementService.revokePolicy(policyId, patientCi);

            LOGGER.log(Level.INFO, "Policy revoked successfully: {0} for patient: {1}",
                    new Object[]{policyId, patientCi});

            return Response.ok(revokedPolicy).build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid policy revoke request: " + policyId, e);

            if (e.getMessage().contains("does not belong to")) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(ErrorResponse.forbidden(e.getMessage()))
                        .build();
            }

            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.notFound("Policy", policyId.toString()))
                        .build();
            }

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.validationError(e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error revoking policy: " + policyId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to revoke policy: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // DELETE /api/policies/{policyId} - Delete Policy
    // ================================================================

    @DELETE
    @Path("/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePolicy(
            @PathParam("policyId") Long policyId,
            @QueryParam("patientCi") String patientCiParam,
            @Context SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "DELETE policy: {0}", policyId);

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

            if (e.getMessage().contains("does not belong to")) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(ErrorResponse.forbidden(e.getMessage()))
                        .build();
            }

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

    private String extractPatientCi(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return null;
        }
        return securityContext.getUserPrincipal().getName();
    }
}
