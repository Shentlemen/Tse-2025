package uy.gub.hcen.service.policy;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import uy.gub.hcen.cache.PolicyCacheService;
import uy.gub.hcen.policy.entity.AccessPolicy;
import uy.gub.hcen.policy.entity.MedicalSpecialty;
import uy.gub.hcen.policy.repository.AccessPolicyRepository;
import uy.gub.hcen.service.policy.dto.AccessRequest;
import uy.gub.hcen.service.policy.dto.PolicyDecision;
import uy.gub.hcen.service.policy.dto.PolicyEvaluationResult;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Policy Engine - Core Access Control Decision Point
 *
 * The PolicyEngine is the central component responsible for evaluating patient-defined
 * access control policies to determine whether healthcare professionals can access
 * clinical documents.
 *
 * Simplified Evaluation Logic (v2.0):
 * - Check if professional's clinic+specialty matches any GRANTED policy
 * - If matching policy exists -> PERMIT
 * - If no matching policy -> PENDING (requires patient approval)
 *
 * @author TSE 2025 Group 9
 * @version 2.0
 * @since 2025-11-18
 */
@Stateless
public class PolicyEngine {

    private static final Logger LOGGER = Logger.getLogger(PolicyEngine.class.getName());

    @Inject
    private AccessPolicyRepository accessPolicyRepository;

    @Inject
    private PolicyCacheService policyCacheService;

    /**
     * Evaluates access policies to determine if a professional can access a document.
     *
     * @param request The access request containing professional, patient, and document context
     * @return PolicyEvaluationResult containing decision (PERMIT/DENY/PENDING) and reasoning
     * @throws IllegalArgumentException if request is null or invalid
     */
    public PolicyEvaluationResult evaluateAccess(AccessRequest request) {
        // Validate request
        if (request == null) {
            throw new IllegalArgumentException("Access request cannot be null");
        }

        if (!request.isValid()) {
            throw new IllegalArgumentException("Access request is invalid: " + request);
        }

        LOGGER.log(Level.INFO, "Evaluating access for professional {0} to patient {1} document type {2}",
                new Object[]{request.getProfessionalId(), request.getPatientCi(), request.getDocumentType()});

        try {
            // Step 1: Check cache
//            PolicyEvaluationResult cachedResult = checkCache(request);
//            if (cachedResult != null) {
//                LOGGER.log(Level.INFO, "Cache hit for policy decision: {0}", cachedResult.getDecision());
//                return cachedResult;
//            }

            // Step 2: Evaluate policies based on clinic and specialty
            PolicyEvaluationResult result = evaluatePolicies(request);

            // Step 3: Cache decision
            cacheDecision(request, result);

            // Step 4: Log result
            logEvaluationResult(request, result);

            return result;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error evaluating access for professional " +
                    request.getProfessionalId() + " to patient " + request.getPatientCi(), e);

            // Fail-safe: DENY on error
            return PolicyEvaluationResult.deny(
                    "Policy evaluation failed: " + e.getMessage(), null);
        }
    }

    /**
     * Evaluates policies based on clinic and specialty match.
     *
     * @param request The access request
     * @return PolicyEvaluationResult with decision and reasoning
     */
    private PolicyEvaluationResult evaluatePolicies(AccessRequest request) {
        String patientCi = request.getPatientCi();
        String clinicId = request.getClinicId();
        List<String> specialties = request.getSpecialties();

        // Check if patient has any policies
        List<AccessPolicy> allPolicies = accessPolicyRepository.findByPatientCi(patientCi);

        if (allPolicies.isEmpty()) {
            LOGGER.log(Level.INFO, "No policies found for patient {0}, returning PENDING",
                    patientCi);
            return PolicyEvaluationResult.pending(
                    "No access policies defined; patient approval required");
        }

        // Check for matching policies (clinic + specialty)
        List<AccessPolicy> matchingPolicies = new ArrayList<>();

        for (String specialty : specialties) {
            MedicalSpecialty medicalSpecialty = MedicalSpecialty.fromName(specialty);
            if (medicalSpecialty != null) {
                List<AccessPolicy> policies = accessPolicyRepository
                        .findByPatientCiAndClinicIdAndSpecialty(patientCi, clinicId, medicalSpecialty);

                // Filter policies that apply to the requested document
                Long documentId = request.getDocumentId();
                for (AccessPolicy policy : policies) {
                    if (policy.appliesToDocument(documentId)) {
                        matchingPolicies.add(policy);
                    }
                }
            }
        }

        if (!matchingPolicies.isEmpty()) {
            // Access permitted - found matching policy
            AccessPolicy decidingPolicy = matchingPolicies.get(0); // Highest priority first
            LOGGER.log(Level.INFO, "Access permitted by policy {0} for professional {1}",
                    new Object[]{decidingPolicy.getId(), request.getProfessionalId()});

            return PolicyEvaluationResult.builder()
                    .decision(PolicyDecision.PERMIT)
                    .reason("Access permitted by policy " + decidingPolicy.getId() +
                            " (" + decidingPolicy.getSpecialty().getDisplayName() + " at " + decidingPolicy.getClinicId() + ")")
                    .evaluatedPolicies(matchingPolicies.stream().map(AccessPolicy::getId).collect(Collectors.toList()))
                    .decidingPolicy(decidingPolicy.getId())
                    .build();
        }

        // No matching policy found - PENDING
        LOGGER.log(Level.INFO, "No matching policy for professional {0} (clinic: {1}, specialties: {2}), returning PENDING",
                new Object[]{request.getProfessionalId(), clinicId, specialties});

        return PolicyEvaluationResult.builder()
                .decision(PolicyDecision.PENDING)
                .reason("No policy grants access for this clinic and specialty; patient approval required")
                .evaluatedPolicies(allPolicies.stream().map(AccessPolicy::getId).collect(Collectors.toList()))
                .build();
    }

    /**
     * Retrieves all applicable (active) policies for a patient.
     *
     * @param patientCi Patient's CI (Cedula de Identidad)
     * @return List of active policies, sorted by priority (descending)
     */
    public List<AccessPolicy> getApplicablePolicies(String patientCi) {
        if (patientCi == null || patientCi.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to get policies for null or empty patient CI");
            return Collections.emptyList();
        }

        try {
            List<AccessPolicy> validPolicies = accessPolicyRepository.findByPatientCi(patientCi);

            LOGGER.log(Level.FINE, "Retrieved {0} valid policies for patient {1}",
                    new Object[]{validPolicies.size(), patientCi});

            return validPolicies;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving policies for patient " + patientCi, e);
            return Collections.emptyList();
        }
    }

    /**
     * Checks if a professional has access to patient documents based on clinic and specialty.
     *
     * @param patientCi Patient's CI
     * @param clinicId Clinic ID
     * @param specialty Medical specialty
     * @return true if access is granted
     */
    public boolean hasAccess(String patientCi, String clinicId, MedicalSpecialty specialty) {
        return accessPolicyRepository.existsActivePolicy(patientCi, clinicId, specialty);
    }

    /**
     * Checks if a professional has access to a specific document.
     *
     * @param patientCi Patient's CI
     * @param clinicId Clinic ID
     * @param specialty Medical specialty
     * @param documentId Document ID
     * @return true if access is granted
     */
    public boolean hasAccessToDocument(String patientCi, String clinicId, MedicalSpecialty specialty, Long documentId) {
        List<AccessPolicy> policies = accessPolicyRepository
                .findByPatientCiAndClinicIdAndSpecialty(patientCi, clinicId, specialty);

        for (AccessPolicy policy : policies) {
            if (policy.appliesToDocument(documentId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks Redis cache for a previous decision.
     *
     * @param request The access request
     * @return Cached PolicyEvaluationResult if found, null otherwise
     */
    private PolicyEvaluationResult checkCache(AccessRequest request) {
        try {
            String specialty = request.getSpecialties().isEmpty()
                    ? "NO_SPECIALTY"
                    : request.getSpecialties().get(0);
            String documentType = request.getDocumentType() != null
                    ? request.getDocumentType().name()
                    : "ALL";

            Optional<String> cachedDecision = policyCacheService.getCachedDecision(
                    request.getPatientCi(), specialty, documentType);

            if (cachedDecision.isPresent()) {
                PolicyDecision decision = PolicyDecision.valueOf(cachedDecision.get());
                return PolicyEvaluationResult.builder()
                        .decision(decision)
                        .reason("Cached decision: " + decision.getDescription())
                        .build();
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking policy cache", e);
        }

        return null;
    }

    /**
     * Caches a policy decision in Redis.
     *
     * @param request The access request
     * @param result The evaluation result to cache
     */
    private void cacheDecision(AccessRequest request, PolicyEvaluationResult result) {
        try {
            String specialty = request.getSpecialties().isEmpty()
                    ? "NO_SPECIALTY"
                    : request.getSpecialties().get(0);
            String documentType = request.getDocumentType() != null
                    ? request.getDocumentType().name()
                    : "ALL";

            policyCacheService.cachePolicyDecision(
                    request.getPatientCi(),
                    specialty,
                    documentType,
                    result.getDecision().name());

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error caching policy decision", e);
        }
    }

    /**
     * Logs the evaluation result for audit trail.
     *
     * @param request The access request
     * @param result The evaluation result
     */
    private void logEvaluationResult(AccessRequest request, PolicyEvaluationResult result) {
        Level logLevel = result.isDenied() ? Level.WARNING : Level.INFO;

        LOGGER.log(logLevel, "Policy evaluation result: {0} | Professional: {1} | Patient: {2} | " +
                        "Clinic: {3} | Specialties: {4} | Reason: {5}",
                new Object[]{
                        result.getDecision(),
                        request.getProfessionalId(),
                        request.getPatientCi(),
                        request.getClinicId(),
                        request.getSpecialties(),
                        result.getReason()
                });
    }
}
