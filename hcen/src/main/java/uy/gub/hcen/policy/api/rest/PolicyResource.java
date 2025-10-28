package uy.gub.hcen.policy.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import uy.gub.hcen.api.dto.ErrorResponse;
import uy.gub.hcen.cache.PolicyCacheService;
import uy.gub.hcen.policy.dto.*;
import uy.gub.hcen.policy.entity.AccessPolicy;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyType;
import uy.gub.hcen.policy.repository.AccessPolicyRepository;
import uy.gub.hcen.service.policy.PolicyEngine;
import uy.gub.hcen.service.policy.dto.AccessRequest;
import uy.gub.hcen.service.policy.dto.PolicyEvaluationResult;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Policy Management REST Resource
 * <p>
 * JAX-RS resource providing REST API endpoints for managing patient access control policies.
 * Enables patients to create, view, update, and delete their policies, and provides
 * an evaluation endpoint for access authorization checks.
 * <p>
 * Base Path: /api/policies
 * <p>
 * Endpoints:
 * <ul>
 *   <li>POST /api/policies - Create new policy (CU03)</li>
 *   <li>GET /api/policies/patient/{patientCi} - Get patient policies</li>
 *   <li>GET /api/policies/{id} - Get specific policy</li>
 *   <li>PUT /api/policies/{id} - Update policy</li>
 *   <li>DELETE /api/policies/{id} - Delete policy</li>
 *   <li>POST /api/policies/evaluate - Evaluate access (internal use)</li>
 * </ul>
 * <p>
 * Security:
 * <ul>
 *   <li>JWT authentication required (to be implemented)</li>
 *   <li>Patients can only manage their own policies</li>
 *   <li>Admins can view all policies</li>
 *   <li>Cache invalidation on policy changes</li>
 * </ul>
 * <p>
 * Error Handling:
 * <ul>
 *   <li>400 Bad Request - Validation errors, invalid JSON</li>
 *   <li>404 Not Found - Policy not found</li>
 *   <li>500 Internal Server Error - Database/system errors</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
@Path("/policies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PolicyResource {

    private static final Logger LOGGER = Logger.getLogger(PolicyResource.class.getName());
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    @Inject
    private AccessPolicyRepository accessPolicyRepository;

    @Inject
    private PolicyCacheService policyCacheService;

    @Inject
    private PolicyEngine policyEngine;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ================================================================
    // POST /api/policies - Create Policy
    // ================================================================

    /**
     * Creates a new access control policy for a patient.
     * <p>
     * This endpoint allows patients to define granular access rules for their
     * clinical documents. Policies can restrict access by document type, specialty,
     * clinic, time, or specific professionals.
     * <p>
     * After creating a policy, the patient's policy cache is invalidated to ensure
     * immediate enforcement of the new rule.
     *
     * @param request Policy creation request with validation
     * @return 201 Created with PolicyResponse and Location header
     *         400 Bad Request if validation fails or JSON is invalid
     *         500 Internal Server Error if database operation fails
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPolicy(@Valid PolicyCreateRequest request) {
        LOGGER.log(Level.INFO, "Creating policy for patient: {0}, type: {1}",
                new Object[]{request.getPatientCi(), request.getPolicyType()});

        try {
            // Validate JSON format
            if (!isValidJson(request.getPolicyConfig())) {
                LOGGER.log(Level.WARNING, "Invalid JSON in policy configuration");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError("Policy configuration must be valid JSON"))
                        .build();
            }

            // Create entity from request
            AccessPolicy policy = new AccessPolicy();
            policy.setPatientCi(request.getPatientCi());
            policy.setPolicyType(request.getPolicyType());
            policy.setPolicyConfig(request.getPolicyConfig());
            policy.setPolicyEffect(request.getPolicyEffect());
            policy.setValidFrom(request.getValidFrom());
            policy.setValidUntil(request.getValidUntil());
            policy.setPriority(request.getPriority());

            // Save policy
            AccessPolicy savedPolicy = accessPolicyRepository.save(policy);

            // Invalidate cache for this patient
            policyCacheService.invalidatePolicyCache(request.getPatientCi());

            LOGGER.log(Level.INFO, "Policy created successfully: ID={0} for patient: {1}",
                    new Object[]{savedPolicy.getId(), request.getPatientCi()});

            // Build Location URI
            URI location = UriBuilder.fromPath("/api/policies/{id}")
                    .build(savedPolicy.getId());

            // Convert to response DTO
            PolicyResponse response = PolicyResponse.fromEntity(savedPolicy);

            return Response.created(location)
                    .entity(response)
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating policy for patient: " + request.getPatientCi(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to create policy: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // GET /api/policies/patient/{patientCi} - Get Patient Policies
    // ================================================================

    /**
     * Retrieves all policies for a specific patient with optional filtering.
     * <p>
     * Supports pagination and filtering by policy type. Returns an empty list
     * if no policies are found (not 404).
     *
     * @param patientCi Patient's CI (7-9 digits)
     * @param policyType Optional filter by policy type
     * @param page Page number (zero-based, default: 0)
     * @param size Page size (default: 20, max: 100)
     * @return 200 OK with PolicyListResponse (paginated)
     *         400 Bad Request if pagination parameters are invalid
     *         500 Internal Server Error if database operation fails
     */
    @GET
    @Path("/patient/{patientCi}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPatientPolicies(
            @PathParam("patientCi") String patientCi,
            @QueryParam("policyType") String policyType,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        LOGGER.log(Level.INFO, "Retrieving policies for patient: {0}, type: {1}, page: {2}, size: {3}",
                new Object[]{patientCi, policyType, page, size});

        try {
            // Validate pagination parameters
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

            // Load policies
            List<AccessPolicy> policies;
            if (policyType != null && !policyType.trim().isEmpty()) {
                try {
                    PolicyType type = PolicyType.valueOf(policyType.trim().toUpperCase());
                    policies = accessPolicyRepository.findByPatientCiAndType(patientCi, type);
                } catch (IllegalArgumentException e) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(ErrorResponse.validationError("Invalid policy type: " + policyType))
                            .build();
                }
            } else {
                policies = accessPolicyRepository.findByPatientCi(patientCi);
            }

            // Calculate pagination
            long totalCount = policies.size();
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, policies.size());

            // Apply pagination
            List<PolicyResponse> policyResponses;
            if (fromIndex >= policies.size()) {
                policyResponses = List.of();
            } else {
                policyResponses = policies.subList(fromIndex, toIndex).stream()
                        .map(PolicyResponse::fromEntity)
                        .collect(Collectors.toList());
            }

            PolicyListResponse response = PolicyListResponse.of(policyResponses, totalCount, page, size);

            LOGGER.log(Level.INFO, "Retrieved {0} policies for patient: {1}",
                    new Object[]{policyResponses.size(), patientCi});

            return Response.ok(response).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving policies for patient: " + patientCi, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to retrieve policies: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // GET /api/policies/{id} - Get Specific Policy
    // ================================================================

    /**
     * Retrieves a specific policy by its ID.
     *
     * @param id Policy ID
     * @return 200 OK with PolicyResponse
     *         404 Not Found if policy doesn't exist
     *         500 Internal Server Error if database operation fails
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPolicy(@PathParam("id") Long id) {
        LOGGER.log(Level.INFO, "Retrieving policy: {0}", id);

        try {
            Optional<AccessPolicy> policyOpt = accessPolicyRepository.findById(id);

            if (policyOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Policy not found: {0}", id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.notFound("Policy", id.toString()))
                        .build();
            }

            PolicyResponse response = PolicyResponse.fromEntity(policyOpt.get());

            LOGGER.log(Level.INFO, "Policy retrieved successfully: {0}", id);

            return Response.ok(response).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving policy: " + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to retrieve policy: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // PUT /api/policies/{id} - Update Policy
    // ================================================================

    /**
     * Updates an existing policy.
     * <p>
     * Only non-null fields in the request are updated. At least one field must
     * be provided. After update, the patient's policy cache is invalidated.
     *
     * @param id Policy ID
     * @param request Policy update request with optional fields
     * @return 200 OK with updated PolicyResponse
     *         400 Bad Request if validation fails or no fields provided
     *         404 Not Found if policy doesn't exist
     *         500 Internal Server Error if database operation fails
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePolicy(@PathParam("id") Long id, @Valid PolicyUpdateRequest request) {
        LOGGER.log(Level.INFO, "Updating policy: {0}", id);

        try {
            // Validate at least one field is provided
            if (!request.hasAtLeastOneField()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError("At least one field must be provided for update"))
                        .build();
            }

            // Load existing policy
            Optional<AccessPolicy> policyOpt = accessPolicyRepository.findById(id);

            if (policyOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Policy not found for update: {0}", id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.notFound("Policy", id.toString()))
                        .build();
            }

            AccessPolicy policy = policyOpt.get();
            String patientCi = policy.getPatientCi(); // For cache invalidation

            // Validate JSON if policyConfig is being updated
            if (request.getPolicyConfig() != null && !isValidJson(request.getPolicyConfig())) {
                LOGGER.log(Level.WARNING, "Invalid JSON in policy configuration update");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError("Policy configuration must be valid JSON"))
                        .build();
            }

            // Update non-null fields
            if (request.getPolicyConfig() != null) {
                policy.setPolicyConfig(request.getPolicyConfig());
            }
            if (request.getPolicyEffect() != null) {
                policy.setPolicyEffect(request.getPolicyEffect());
            }
            if (request.getValidFrom() != null) {
                policy.setValidFrom(request.getValidFrom());
            }
            if (request.getValidUntil() != null) {
                policy.setValidUntil(request.getValidUntil());
            }
            if (request.getPriority() != null) {
                policy.setPriority(request.getPriority());
            }

            // Save updated policy
            AccessPolicy updatedPolicy = accessPolicyRepository.update(policy);

            // Invalidate cache for this patient
            policyCacheService.invalidatePolicyCache(patientCi);

            LOGGER.log(Level.INFO, "Policy updated successfully: {0} for patient: {1}",
                    new Object[]{id, patientCi});

            PolicyResponse response = PolicyResponse.fromEntity(updatedPolicy);

            return Response.ok(response).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating policy: " + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to update policy: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // DELETE /api/policies/{id} - Delete Policy
    // ================================================================

    /**
     * Deletes a policy by its ID.
     * <p>
     * After deletion, the patient's policy cache is invalidated.
     *
     * @param id Policy ID
     * @return 204 No Content on success
     *         404 Not Found if policy doesn't exist
     *         500 Internal Server Error if database operation fails
     */
    @DELETE
    @Path("/{id}")
    public Response deletePolicy(@PathParam("id") Long id) {
        LOGGER.log(Level.INFO, "Deleting policy: {0}", id);

        try {
            // Get patient CI before deletion for cache invalidation
            Optional<AccessPolicy> policyOpt = accessPolicyRepository.findById(id);

            if (policyOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Policy not found for deletion: {0}", id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.notFound("Policy", id.toString()))
                        .build();
            }

            String patientCi = policyOpt.get().getPatientCi();

            // Delete policy
            boolean deleted = accessPolicyRepository.delete(id);

            if (!deleted) {
                LOGGER.log(Level.WARNING, "Failed to delete policy: {0}", id);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(ErrorResponse.internalServerError("Failed to delete policy"))
                        .build();
            }

            // Invalidate cache for this patient
            policyCacheService.invalidatePolicyCache(patientCi);

            LOGGER.log(Level.INFO, "Policy deleted successfully: {0} for patient: {1}",
                    new Object[]{id, patientCi});

            return Response.noContent().build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting policy: " + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to delete policy: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // POST /api/policies/evaluate - Evaluate Access
    // ================================================================

    /**
     * Evaluates access policies to determine if a professional can access a document.
     * <p>
     * This endpoint is intended for internal use by document access endpoints (RNDC).
     * It converts the REST request to PolicyEngine format, evaluates all applicable
     * policies, and returns a decision (PERMIT/DENY/PENDING).
     * <p>
     * Decision Meanings:
     * <ul>
     *   <li>PERMIT - Grant access (HTTP 200)</li>
     *   <li>DENY - Block access (HTTP 403)</li>
     *   <li>PENDING - Requires patient approval (HTTP 202)</li>
     * </ul>
     *
     * @param request Access evaluation request with professional and document context
     * @return 200 OK with AccessEvaluationResponse
     *         400 Bad Request if validation fails
     *         500 Internal Server Error if evaluation fails
     */
    @POST
    @Path("/evaluate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response evaluateAccess(@Valid AccessEvaluationRequest request) {
        LOGGER.log(Level.INFO, "Evaluating access for professional: {0} to patient: {1} document: {2}",
                new Object[]{request.getProfessionalId(), request.getPatientCi(), request.getDocumentType()});

        try {
            // Convert REST DTO to PolicyEngine DTO
            AccessRequest accessRequest = AccessRequest.builder()
                    .professionalId(request.getProfessionalId())
                    .specialties(request.getSpecialties())
                    .clinicId(request.getClinicId())
                    .patientCi(request.getPatientCi())
                    .documentId(request.getDocumentId())
                    .documentType(request.getDocumentType())
                    .requestReason(request.getRequestReason())
                    .build();

            // Evaluate access using PolicyEngine
            PolicyEvaluationResult result = policyEngine.evaluateAccess(accessRequest);

            // Convert to REST response DTO
            AccessEvaluationResponse response = AccessEvaluationResponse.fromPolicyEvaluationResult(result);

            LOGGER.log(Level.INFO, "Access evaluation result: {0} for professional: {1} to patient: {2}",
                    new Object[]{response.getDecision(), request.getProfessionalId(), request.getPatientCi()});

            return Response.ok(response).build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid access evaluation request: " + e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.validationError("Invalid access request: " + e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error evaluating access for professional: " +
                    request.getProfessionalId() + " to patient: " + request.getPatientCi(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to evaluate access: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // Utility Methods
    // ================================================================

    /**
     * Validates if a string is valid JSON.
     *
     * @param json JSON string to validate
     * @return true if valid JSON, false otherwise
     */
    private boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }

        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Invalid JSON: " + e.getMessage());
            return false;
        }
    }
}
