package uy.gub.hcen.policy.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import uy.gub.hcen.service.audit.AuditService;
import uy.gub.hcen.policy.dto.PolicyCreateRequest;
import uy.gub.hcen.policy.dto.PolicyResponse;
import uy.gub.hcen.policy.dto.PolicyUpdateRequest;
import uy.gub.hcen.policy.entity.AccessPolicy;
import uy.gub.hcen.policy.repository.AccessPolicyRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Policy Management Service
 * <p>
 * Business logic service for managing patient access control policies.
 * Provides high-level operations for policy creation, update, deletion,
 * and validation with integrated auditing and cache management.
 * <p>
 * Service Responsibilities:
 * <ul>
 *   <li>Policy CRUD operations with business logic validation</li>
 *   <li>Policy ownership verification (patients can only manage their own policies)</li>
 *   <li>Automatic audit logging for all policy changes</li>
 *   <li>Cache invalidation on policy modifications</li>
 *   <li>Policy conflict detection and validation</li>
 * </ul>
 * <p>
 * Transaction Management:
 * All mutating operations (create, update, delete) are @Transactional
 * to ensure atomicity with audit log creation.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-04
 */
@ApplicationScoped
public class PolicyManagementService {

    private static final Logger LOGGER = Logger.getLogger(PolicyManagementService.class.getName());

    @Inject
    private AccessPolicyRepository accessPolicyRepository;

    @Inject
    private PolicyCacheService policyCacheService;

    @Inject
    private AuditService auditService;

    // ================================================================
    // Query Operations (Read-Only)
    // ================================================================

    /**
     * Retrieves all policies for a specific patient.
     * <p>
     * Returns only currently valid policies (within validity period).
     * Policies are ordered by priority (descending) and creation date (descending).
     *
     * @param patientCi Patient's CI
     * @return List of PolicyResponse DTOs
     */
    public List<PolicyResponse> getPatientPolicies(String patientCi) {
        LOGGER.log(Level.INFO, "Retrieving policies for patient: {0}", patientCi);

        List<AccessPolicy> policies = accessPolicyRepository.findByPatientCi(patientCi);

        return policies.stream()
                .filter(AccessPolicy::isValid)
                .sorted((p1, p2) -> {
                    // Sort by priority (descending), then by creation date (descending)
                    int priorityComparison = Integer.compare(p2.getPriority(), p1.getPriority());
                    if (priorityComparison != 0) {
                        return priorityComparison;
                    }
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                })
                .map(PolicyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all policies for a patient, including expired ones.
     * <p>
     * Useful for audit trail and policy history.
     *
     * @param patientCi Patient's CI
     * @return List of all PolicyResponse DTOs
     */
    public List<PolicyResponse> getAllPatientPolicies(String patientCi) {
        LOGGER.log(Level.INFO, "Retrieving all policies (including expired) for patient: {0}", patientCi);

        List<AccessPolicy> policies = accessPolicyRepository.findAllByPatientCi(patientCi);

        return policies.stream()
                .map(PolicyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific policy by ID with ownership verification.
     *
     * @param policyId Policy ID
     * @param patientCi Patient CI (for ownership verification)
     * @return Optional PolicyResponse
     * @throws IllegalArgumentException if policy doesn't belong to patient
     */
    public Optional<PolicyResponse> getPolicy(Long policyId, String patientCi) {
        LOGGER.log(Level.INFO, "Retrieving policy: {0} for patient: {1}", new Object[]{policyId, patientCi});

        Optional<AccessPolicy> policyOpt = accessPolicyRepository.findById(policyId);

        if (policyOpt.isEmpty()) {
            return Optional.empty();
        }

        AccessPolicy policy = policyOpt.get();

        // Verify ownership
        if (!policy.getPatientCi().equals(patientCi)) {
            throw new IllegalArgumentException("Policy " + policyId + " does not belong to patient " + patientCi);
        }

        return Optional.of(PolicyResponse.fromEntity(policy));
    }

    /**
     * Counts active policies for a patient.
     *
     * @param patientCi Patient's CI
     * @return Count of active policies
     */
    public long countActivePolicies(String patientCi) {
        return accessPolicyRepository.countByPatientCi(patientCi);
    }

    // ================================================================
    // Mutation Operations (Transactional)
    // ================================================================

    /**
     * Creates a new access policy for a patient.
     * <p>
     * Validates policy configuration, creates audit log, and invalidates cache.
     *
     * @param request Policy creation request
     * @return Created PolicyResponse
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public PolicyResponse createPolicy(PolicyCreateRequest request) {
        LOGGER.log(Level.INFO, "Creating policy for patient: {0}, type: {1}",
                new Object[]{request.getPatientCi(), request.getPolicyType()});

        // Validate policy configuration
        validatePolicyConfiguration(request.getPolicyConfig(), request.getPolicyType());

        // Create entity from request
        AccessPolicy policy = new AccessPolicy();
        policy.setPatientCi(request.getPatientCi());
        policy.setPolicyType(request.getPolicyType());
        policy.setPolicyConfig(request.getPolicyConfig());
        policy.setPolicyEffect(request.getPolicyEffect());
        policy.setValidFrom(request.getValidFrom());
        policy.setValidUntil(request.getValidUntil());
        policy.setPriority(request.getPriority() != null ? request.getPriority() : 0);

        // Save policy
        AccessPolicy savedPolicy = accessPolicyRepository.save(policy);

        // Create audit log
        auditService.logPolicyChange(request.getPatientCi(), savedPolicy.getId(),
                "CREATE", null, null);

        // Invalidate cache
        policyCacheService.invalidatePolicyCache(request.getPatientCi());

        LOGGER.log(Level.INFO, "Policy created successfully: ID={0} for patient: {1}",
                new Object[]{savedPolicy.getId(), request.getPatientCi()});

        return PolicyResponse.fromEntity(savedPolicy);
    }

    /**
     * Updates an existing policy with ownership verification.
     * <p>
     * Only non-null fields in the request are updated.
     *
     * @param policyId Policy ID
     * @param request Update request
     * @param patientCi Patient CI (for ownership verification)
     * @return Updated PolicyResponse
     * @throws IllegalArgumentException if policy not found or doesn't belong to patient
     */
    @Transactional
    public PolicyResponse updatePolicy(Long policyId, PolicyUpdateRequest request, String patientCi) {
        LOGGER.log(Level.INFO, "Updating policy: {0} for patient: {1}", new Object[]{policyId, patientCi});

        // Load existing policy
        Optional<AccessPolicy> policyOpt = accessPolicyRepository.findById(policyId);

        if (policyOpt.isEmpty()) {
            throw new IllegalArgumentException("Policy not found: " + policyId);
        }

        AccessPolicy policy = policyOpt.get();

        // Verify ownership
        if (!policy.getPatientCi().equals(patientCi)) {
            throw new IllegalArgumentException("Policy " + policyId + " does not belong to patient " + patientCi);
        }

        // Track if any changes were made
        boolean hasChanges = false;

        // Update non-null fields
        if (request.getPolicyConfig() != null) {
            validatePolicyConfiguration(request.getPolicyConfig(), policy.getPolicyType());
            policy.setPolicyConfig(request.getPolicyConfig());
            hasChanges = true;
        }

        if (request.getPolicyEffect() != null) {
            policy.setPolicyEffect(request.getPolicyEffect());
            hasChanges = true;
        }

        if (request.getValidFrom() != null) {
            policy.setValidFrom(request.getValidFrom());
            hasChanges = true;
        }

        if (request.getValidUntil() != null) {
            policy.setValidUntil(request.getValidUntil());
            hasChanges = true;
        }

        if (request.getPriority() != null) {
            policy.setPriority(request.getPriority());
            hasChanges = true;
        }

        if (!hasChanges) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }

        // Save updated policy
        AccessPolicy updatedPolicy = accessPolicyRepository.update(policy);

        // Create audit log
        auditService.logPolicyChange(patientCi, policyId, "UPDATE", null, null);

        // Invalidate cache
        policyCacheService.invalidatePolicyCache(patientCi);

        LOGGER.log(Level.INFO, "Policy updated successfully: {0} for patient: {1}",
                new Object[]{policyId, patientCi});

        return PolicyResponse.fromEntity(updatedPolicy);
    }

    /**
     * Deletes a policy with ownership verification.
     *
     * @param policyId Policy ID
     * @param patientCi Patient CI (for ownership verification)
     * @throws IllegalArgumentException if policy not found or doesn't belong to patient
     */
    @Transactional
    public void deletePolicy(Long policyId, String patientCi) {
        LOGGER.log(Level.INFO, "Deleting policy: {0} for patient: {1}", new Object[]{policyId, patientCi});

        // Verify ownership before deletion
        Optional<AccessPolicy> policyOpt = accessPolicyRepository.findById(policyId);

        if (policyOpt.isEmpty()) {
            throw new IllegalArgumentException("Policy not found: " + policyId);
        }

        AccessPolicy policy = policyOpt.get();

        if (!policy.getPatientCi().equals(patientCi)) {
            throw new IllegalArgumentException("Policy " + policyId + " does not belong to patient " + patientCi);
        }

        // Delete policy
        boolean deleted = accessPolicyRepository.delete(policyId);

        if (!deleted) {
            throw new IllegalStateException("Failed to delete policy: " + policyId);
        }

        // Create audit log
        auditService.logPolicyChange(patientCi, policyId, "DELETE", null, null);

        // Invalidate cache
        policyCacheService.invalidatePolicyCache(patientCi);

        LOGGER.log(Level.INFO, "Policy deleted successfully: {0} for patient: {1}",
                new Object[]{policyId, patientCi});
    }

    /**
     * Deletes all policies for a patient (e.g., account deletion).
     *
     * @param patientCi Patient's CI
     * @return Number of policies deleted
     */
    @Transactional
    public int deleteAllPatientPolicies(String patientCi) {
        LOGGER.log(Level.INFO, "Deleting all policies for patient: {0}", patientCi);

        int deletedCount = accessPolicyRepository.deleteByPatientCi(patientCi);

        // Create audit log for bulk deletion (using generic event logging)
        if (deletedCount > 0) {
            auditService.logPolicyChange(patientCi, 0L, "BULK_DELETE_" + deletedCount, null, null);
        }

        // Invalidate cache
        policyCacheService.invalidatePolicyCache(patientCi);

        LOGGER.log(Level.INFO, "Deleted {0} policies for patient: {1}",
                new Object[]{deletedCount, patientCi});

        return deletedCount;
    }

    // ================================================================
    // Validation Helpers
    // ================================================================

    /**
     * Validates policy configuration JSON format and structure.
     * <p>
     * Basic validation: ensures JSON is valid and contains expected fields
     * for the policy type. More specific validation is done by PolicyEvaluators.
     *
     * @param policyConfig Policy configuration JSON
     * @param policyType Policy type
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePolicyConfiguration(String policyConfig, AccessPolicy.PolicyType policyType) {
        if (policyConfig == null || policyConfig.trim().isEmpty()) {
            throw new IllegalArgumentException("Policy configuration cannot be empty");
        }

        // Basic JSON validation (format check)
        if (!policyConfig.trim().startsWith("{") || !policyConfig.trim().endsWith("}")) {
            throw new IllegalArgumentException("Policy configuration must be a valid JSON object");
        }

        // Type-specific validation (basic checks)
        switch (policyType) {
            case DOCUMENT_TYPE:
                if (!policyConfig.contains("allowedTypes") && !policyConfig.contains("deniedTypes")) {
                    throw new IllegalArgumentException("DOCUMENT_TYPE policy must specify allowedTypes or deniedTypes");
                }
                break;

            case SPECIALTY:
                if (!policyConfig.contains("allowedSpecialties") && !policyConfig.contains("deniedSpecialties")) {
                    throw new IllegalArgumentException("SPECIALTY policy must specify allowedSpecialties or deniedSpecialties");
                }
                break;

            case CLINIC:
                if (!policyConfig.contains("allowedClinics") && !policyConfig.contains("deniedClinics")) {
                    throw new IllegalArgumentException("CLINIC policy must specify allowedClinics or deniedClinics");
                }
                break;

            case PROFESSIONAL:
                if (!policyConfig.contains("allowedProfessionals") && !policyConfig.contains("deniedProfessionals")) {
                    throw new IllegalArgumentException("PROFESSIONAL policy must specify allowedProfessionals or deniedProfessionals");
                }
                break;

            case TIME_BASED:
                if (!policyConfig.contains("allowedDays") && !policyConfig.contains("allowedHours")) {
                    throw new IllegalArgumentException("TIME_BASED policy must specify allowedDays or allowedHours");
                }
                break;

            case EMERGENCY_OVERRIDE:
                if (!policyConfig.contains("enabled")) {
                    throw new IllegalArgumentException("EMERGENCY_OVERRIDE policy must specify enabled flag");
                }
                break;

            default:
                LOGGER.log(Level.WARNING, "Unknown policy type: {0}. Skipping specific validation.", policyType);
        }
    }
}
