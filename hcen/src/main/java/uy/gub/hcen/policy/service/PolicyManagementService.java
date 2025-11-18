package uy.gub.hcen.policy.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import uy.gub.hcen.service.audit.AuditService;
import uy.gub.hcen.policy.dto.PolicyCreateRequest;
import uy.gub.hcen.policy.dto.PolicyResponse;
import uy.gub.hcen.policy.entity.AccessPolicy;
import uy.gub.hcen.policy.entity.MedicalSpecialty;
import uy.gub.hcen.policy.entity.PolicyStatus;
import uy.gub.hcen.policy.repository.AccessPolicyRepository;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Policy Management Service
 *
 * Business logic service for managing patient access control policies.
 * Simplified for the clinic+specialty permission model.
 *
 * @author TSE 2025 Group 9
 * @version 2.0
 * @since 2025-11-18
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
     * Retrieves all active policies for a specific patient.
     *
     * @param patientCi Patient's CI
     * @return List of PolicyResponse DTOs
     */
    public List<PolicyResponse> getPatientPolicies(String patientCi) {
        LOGGER.log(Level.INFO, "Retrieving policies for patient: {0}", patientCi);

        List<AccessPolicy> policies = accessPolicyRepository.findByPatientCi(patientCi);

        return policies.stream()
                .sorted((p1, p2) -> {
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
     * Retrieves all policies for a patient, including revoked and expired ones.
     *
     * @param patientCi Patient's CI
     * @return List of all PolicyResponse DTOs
     */
    public List<PolicyResponse> getAllPatientPolicies(String patientCi) {
        LOGGER.log(Level.INFO, "Retrieving all policies (including revoked) for patient: {0}", patientCi);

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
        return accessPolicyRepository.countActiveByPatientCi(patientCi);
    }

    // ================================================================
    // Mutation Operations (Transactional)
    // ================================================================

    /**
     * Creates a new access policy for a patient.
     *
     * @param request Policy creation request
     * @return Created PolicyResponse
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public PolicyResponse createPolicy(PolicyCreateRequest request) {
        LOGGER.log(Level.INFO, "Creating policy for patient: {0}, clinic: {1}, specialty: {2}",
                new Object[]{request.getPatientCi(), request.getClinicId(), request.getSpecialty()});

        // Validate request
        validatePolicyRequest(request);

        // Check if policy already exists
        boolean exists = accessPolicyRepository.existsActivePolicy(
                request.getPatientCi(),
                request.getClinicId(),
                request.getSpecialty()
        );

        if (exists && request.getDocumentId() == null) {
            throw new IllegalArgumentException(
                    "A policy already exists for this clinic and specialty. Delete it first or specify a document ID.");
        }

        // Create entity from request
        AccessPolicy policy = new AccessPolicy();
        policy.setPatientCi(request.getPatientCi());
        policy.setClinicId(request.getClinicId());
        policy.setSpecialty(request.getSpecialty());
        policy.setDocumentId(request.getDocumentId());
        policy.setStatus(PolicyStatus.GRANTED);
        policy.setValidFrom(request.getValidFrom());
        policy.setValidUntil(request.getValidUntil());
        policy.setPriority(0); // Default priority

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
     * Updates an existing policy status (revoke/grant).
     *
     * @param policyId Policy ID
     * @param status New status
     * @param patientCi Patient CI (for ownership verification)
     * @return Updated PolicyResponse
     * @throws IllegalArgumentException if policy not found or doesn't belong to patient
     */
    @Transactional
    public PolicyResponse updatePolicyStatus(Long policyId, PolicyStatus status, String patientCi) {
        LOGGER.log(Level.INFO, "Updating policy status: {0} to {1} for patient: {2}",
                new Object[]{policyId, status, patientCi});

        Optional<AccessPolicy> policyOpt = accessPolicyRepository.findById(policyId);

        if (policyOpt.isEmpty()) {
            throw new IllegalArgumentException("Policy not found: " + policyId);
        }

        AccessPolicy policy = policyOpt.get();

        if (!policy.getPatientCi().equals(patientCi)) {
            throw new IllegalArgumentException("Policy " + policyId + " does not belong to patient " + patientCi);
        }

        policy.setStatus(status);

        AccessPolicy updatedPolicy = accessPolicyRepository.update(policy);

        // Create audit log
        auditService.logPolicyChange(patientCi, policyId, "UPDATE_STATUS_" + status, null, null);

        // Invalidate cache
        policyCacheService.invalidatePolicyCache(patientCi);

        LOGGER.log(Level.INFO, "Policy status updated: {0} to {1}",
                new Object[]{policyId, status});

        return PolicyResponse.fromEntity(updatedPolicy);
    }

    /**
     * Revokes a policy.
     *
     * @param policyId Policy ID
     * @param patientCi Patient CI (for ownership verification)
     * @return Updated PolicyResponse
     */
    @Transactional
    public PolicyResponse revokePolicy(Long policyId, String patientCi) {
        return updatePolicyStatus(policyId, PolicyStatus.REVOKED, patientCi);
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

        Optional<AccessPolicy> policyOpt = accessPolicyRepository.findById(policyId);

        if (policyOpt.isEmpty()) {
            throw new IllegalArgumentException("Policy not found: " + policyId);
        }

        AccessPolicy policy = policyOpt.get();

        if (!policy.getPatientCi().equals(patientCi)) {
            throw new IllegalArgumentException("Policy " + policyId + " does not belong to patient " + patientCi);
        }

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

        if (deletedCount > 0) {
            auditService.logPolicyChange(patientCi, 0L, "BULK_DELETE_" + deletedCount, null, null);
        }

        policyCacheService.invalidatePolicyCache(patientCi);

        LOGGER.log(Level.INFO, "Deleted {0} policies for patient: {1}",
                new Object[]{deletedCount, patientCi});

        return deletedCount;
    }

    // ================================================================
    // Validation Helpers
    // ================================================================

    /**
     * Validates policy creation request.
     *
     * @param request Policy creation request
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePolicyRequest(PolicyCreateRequest request) {
        if (request.getPatientCi() == null || request.getPatientCi().trim().isEmpty()) {
            throw new IllegalArgumentException("Patient CI is required");
        }

        if (request.getClinicId() == null || request.getClinicId().trim().isEmpty()) {
            throw new IllegalArgumentException("Clinic ID is required");
        }

        if (request.getSpecialty() == null) {
            throw new IllegalArgumentException("Specialty is required");
        }

        // Validate validity dates
        if (request.getValidFrom() != null && request.getValidUntil() != null) {
            if (request.getValidFrom().isAfter(request.getValidUntil())) {
                throw new IllegalArgumentException("Valid from date must be before valid until date");
            }
        }
    }
}
