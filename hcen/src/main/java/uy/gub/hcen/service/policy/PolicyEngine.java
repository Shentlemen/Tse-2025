package uy.gub.hcen.service.policy;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.enterprise.inject.Instance;
import uy.gub.hcen.cache.PolicyCacheService;
import uy.gub.hcen.policy.entity.AccessPolicy;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyType;
import uy.gub.hcen.policy.repository.AccessPolicyRepository;
import uy.gub.hcen.service.policy.dto.AccessRequest;
import uy.gub.hcen.service.policy.dto.PolicyDecision;
import uy.gub.hcen.service.policy.dto.PolicyEvaluationResult;
import uy.gub.hcen.service.policy.evaluator.PolicyEvaluator;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Policy Engine - Core Access Control Decision Point
 *
 * The PolicyEngine is the central component responsible for evaluating patient-defined
 * access control policies to determine whether healthcare professionals can access
 * clinical documents. It implements ABAC (Attribute-Based Access Control) and
 * RBAC (Role-Based Access Control) patterns.
 *
 * <p>Architecture:
 * <ul>
 *   <li><b>Strategy Pattern</b>: Uses PolicyEvaluator implementations for different policy types</li>
 *   <li><b>Caching Layer</b>: Integrates with Redis for 5-minute decision caching</li>
 *   <li><b>Conflict Resolution</b>: Implements priority-based resolution (DENY > PERMIT > PENDING)</li>
 *   <li><b>Audit Integration</b>: Logs all evaluations for compliance and traceability</li>
 * </ul>
 *
 * <p>Evaluation Flow:
 * <ol>
 *   <li>Check Redis cache for previous decision (5-minute TTL)</li>
 *   <li>If cache miss: Load patient's active policies from database</li>
 *   <li>Evaluate each policy using appropriate PolicyEvaluator</li>
 *   <li>Resolve conflicts using priority and decision hierarchy</li>
 *   <li>Cache decision in Redis</li>
 *   <li>Log evaluation result</li>
 *   <li>Return PolicyEvaluationResult</li>
 * </ol>
 *
 * <p>Decision Hierarchy (Conflict Resolution):
 * <ol>
 *   <li><b>DENY</b> - Explicit denial always wins (fail-safe)</li>
 *   <li><b>PERMIT</b> - Explicit permission (if no DENY)</li>
 *   <li><b>PENDING</b> - No applicable policy (requires patient approval)</li>
 * </ol>
 *
 * <p>Usage Example:
 * <pre>
 * AccessRequest request = AccessRequest.builder()
 *     .professionalId("prof-123")
 *     .addSpecialty("CARDIOLOGY")
 *     .clinicId("clinic-001")
 *     .patientCi("12345678")
 *     .documentType(DocumentType.LAB_RESULT)
 *     .build();
 *
 * PolicyEvaluationResult result = policyEngine.evaluateAccess(request);
 *
 * if (result.isPermitted()) {
 *     // Grant access, return document
 * } else if (result.isDenied()) {
 *     // Return 403 Forbidden with reason
 * } else if (result.isPending()) {
 *     // Send push notification to patient, return 202 Accepted
 * }
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
@Stateless
public class PolicyEngine {

    private static final Logger LOGGER = Logger.getLogger(PolicyEngine.class.getName());

    @Inject
    private AccessPolicyRepository accessPolicyRepository;

    @Inject
    private PolicyCacheService policyCacheService;

    @Inject
    private Instance<PolicyEvaluator> evaluators;

    /**
     * Evaluates access policies to determine if a professional can access a document.
     *
     * <p>This is the main entry point for policy evaluation. It:
     * <ul>
     *   <li>Validates the access request</li>
     *   <li>Checks cache for previous decision</li>
     *   <li>Loads and evaluates applicable policies</li>
     *   <li>Resolves conflicts</li>
     *   <li>Caches and returns the decision</li>
     * </ul>
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
            PolicyEvaluationResult cachedResult = checkCache(request);
            if (cachedResult != null) {
                LOGGER.log(Level.INFO, "Cache hit for policy decision: {0}", cachedResult.getDecision());
                return cachedResult;
            }

            // Step 2: Load patient's active policies
            List<AccessPolicy> policies = getApplicablePolicies(request.getPatientCi());

            if (policies.isEmpty()) {
                LOGGER.log(Level.INFO, "No policies found for patient {0}, returning PENDING",
                        request.getPatientCi());
                PolicyEvaluationResult result = PolicyEvaluationResult.pending(
                        "No access policies defined; patient approval required");
                cacheDecision(request, result);
                return result;
            }

            LOGGER.log(Level.FINE, "Found {0} active policies for patient {1}",
                    new Object[]{policies.size(), request.getPatientCi()});

            // Step 3: Evaluate policies
            PolicyEvaluationResult result = evaluatePolicies(policies, request);

            // Step 4: Cache decision
            cacheDecision(request, result);

            // Step 5: Log result
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
     * Retrieves all applicable (active) policies for a patient.
     *
     * <p>A policy is applicable if:
     * <ul>
     *   <li>It belongs to the specified patient</li>
     *   <li>It is within its validity period (validFrom/validUntil)</li>
     * </ul>
     *
     * <p>Policies are sorted by priority (higher priority first) for conflict resolution.
     *
     * @param patientCi Patient's CI (Cédula de Identidad)
     * @return List of active policies, sorted by priority (descending)
     */
    public List<AccessPolicy> getApplicablePolicies(String patientCi) {
        if (patientCi == null || patientCi.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to get policies for null or empty patient CI");
            return Collections.emptyList();
        }

        try {
            // Load all policies for patient
            List<AccessPolicy> allPolicies = accessPolicyRepository.findByPatientCi(patientCi);

            // Filter to only valid policies and sort by priority
            List<AccessPolicy> validPolicies = allPolicies.stream()
                    .filter(AccessPolicy::isValid)
                    .sorted(Comparator.comparing(AccessPolicy::getPriority).reversed())
                    .collect(Collectors.toList());

            LOGGER.log(Level.FINE, "Retrieved {0} valid policies (from {1} total) for patient {2}",
                    new Object[]{validPolicies.size(), allPolicies.size(), patientCi});

            return validPolicies;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving policies for patient " + patientCi, e);
            return Collections.emptyList();
        }
    }

    /**
     * Evaluates a list of policies against an access request.
     *
     * <p>For each policy:
     * <ol>
     *   <li>Select appropriate evaluator based on policy type</li>
     *   <li>Evaluate policy to get decision (PERMIT/DENY/null)</li>
     *   <li>Collect all decisions</li>
     *   <li>Resolve conflicts using decision hierarchy</li>
     * </ol>
     *
     * @param policies List of policies to evaluate
     * @param request The access request
     * @return PolicyEvaluationResult with final decision and reasoning
     */
    private PolicyEvaluationResult evaluatePolicies(List<AccessPolicy> policies, AccessRequest request) {
        List<PolicyDecision> decisions = new ArrayList<>();
        List<Long> evaluatedPolicyIds = new ArrayList<>();
        Map<PolicyDecision, Long> decisionToPolicyId = new HashMap<>();

        for (AccessPolicy policy : policies) {
            evaluatedPolicyIds.add(policy.getId());

            // Find appropriate evaluator for this policy type
            PolicyEvaluator evaluator = findEvaluator(policy.getPolicyType());

            if (evaluator == null) {
                LOGGER.log(Level.WARNING, "No evaluator found for policy type {0}, skipping policy {1}",
                        new Object[]{policy.getPolicyType(), policy.getId()});
                continue;
            }

            // Evaluate policy
            try {
                PolicyDecision decision = evaluator.evaluate(policy, request);

                if (decision != null) {
                    decisions.add(decision);
                    // Track which policy made which decision (for reporting)
                    if (!decisionToPolicyId.containsKey(decision)) {
                        decisionToPolicyId.put(decision, policy.getId());
                    }

                    LOGGER.log(Level.FINE, "Policy {0} ({1}) evaluated to: {2}",
                            new Object[]{policy.getId(), policy.getPolicyType(), decision});
                } else {
                    LOGGER.log(Level.FINE, "Policy {0} ({1}) not applicable to this request",
                            new Object[]{policy.getId(), policy.getPolicyType()});
                }

            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error evaluating policy " + policy.getId(), e);
            }
        }

        // Resolve conflicts
        PolicyDecision finalDecision = resolveConflicts(decisions);
        Long decidingPolicyId = decisionToPolicyId.get(finalDecision);
        String reason = buildReason(finalDecision, decidingPolicyId, decisions.size());

        return PolicyEvaluationResult.builder()
                .decision(finalDecision)
                .reason(reason)
                .evaluatedPolicies(evaluatedPolicyIds)
                .decidingPolicy(decidingPolicyId)
                .build();
    }

    /**
     * Resolves conflicts when multiple policies return different decisions.
     *
     * <p>Resolution hierarchy:
     * <ol>
     *   <li><b>DENY</b> - Explicit denial always wins (fail-safe approach)</li>
     *   <li><b>PERMIT</b> - Explicit permission (if no DENY exists)</li>
     *   <li><b>PENDING</b> - No applicable policy (default when no decisions made)</li>
     * </ol>
     *
     * @param decisions List of policy decisions
     * @return Final resolved decision
     */
    public PolicyDecision resolveConflicts(List<PolicyDecision> decisions) {
        if (decisions == null || decisions.isEmpty()) {
            return PolicyDecision.PENDING;
        }

        // DENY always wins (fail-safe)
        if (decisions.contains(PolicyDecision.DENY)) {
            LOGGER.log(Level.FINE, "Conflict resolution: DENY wins (fail-safe)");
            return PolicyDecision.DENY;
        }

        // PERMIT wins if no DENY
        if (decisions.contains(PolicyDecision.PERMIT)) {
            LOGGER.log(Level.FINE, "Conflict resolution: PERMIT (no DENY found)");
            return PolicyDecision.PERMIT;
        }

        // Default to PENDING
        LOGGER.log(Level.FINE, "Conflict resolution: PENDING (no applicable policies)");
        return PolicyDecision.PENDING;
    }

    /**
     * Finds the appropriate evaluator for a given policy type.
     *
     * @param policyType The policy type
     * @return PolicyEvaluator that supports this type, or null if none found
     */
    private PolicyEvaluator findEvaluator(PolicyType policyType) {
        for (PolicyEvaluator evaluator : evaluators) {
            if (evaluator.supports(policyType)) {
                return evaluator;
            }
        }
        return null;
    }

    /**
     * Checks Redis cache for a previous decision.
     *
     * @param request The access request
     * @return Cached PolicyEvaluationResult if found, null otherwise
     */
    private PolicyEvaluationResult checkCache(AccessRequest request) {
        try {
            // Build cache key components
            String specialty = request.getSpecialties().isEmpty()
                    ? "NO_SPECIALTY"
                    : request.getSpecialties().get(0);
            String documentType = request.getDocumentType().name();

            // Check cache
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
            String documentType = request.getDocumentType().name();

            policyCacheService.cachePolicyDecision(
                    request.getPatientCi(),
                    specialty,
                    documentType,
                    result.getDecision().name());

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error caching policy decision", e);
            // Don't fail the request if caching fails
        }
    }

    /**
     * Builds a human-readable reason for the decision.
     *
     * @param decision The final decision
     * @param decidingPolicyId ID of the policy that made the decision
     * @param evaluatedCount Number of policies evaluated
     * @return Human-readable reason string
     */
    private String buildReason(PolicyDecision decision, Long decidingPolicyId, int evaluatedCount) {
        if (decision == PolicyDecision.PERMIT) {
            return decidingPolicyId != null
                    ? "Access permitted by policy " + decidingPolicyId + " (evaluated " + evaluatedCount + " policies)"
                    : "Access permitted (evaluated " + evaluatedCount + " policies)";
        } else if (decision == PolicyDecision.DENY) {
            return decidingPolicyId != null
                    ? "Access denied by policy " + decidingPolicyId + " (evaluated " + evaluatedCount + " policies)"
                    : "Access denied (evaluated " + evaluatedCount + " policies)";
        } else {
            return evaluatedCount > 0
                    ? "No applicable policy found among " + evaluatedCount + " policies; patient approval required"
                    : "No policies defined; patient approval required";
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
                        "Document: {3} | Reason: {4} | Evaluated policies: {5} | Deciding policy: {6}",
                new Object[]{
                        result.getDecision(),
                        request.getProfessionalId(),
                        request.getPatientCi(),
                        request.getDocumentType(),
                        result.getReason(),
                        result.getEvaluatedPolicies().size(),
                        result.getDecidingPolicy()
                });

        // Log emergency overrides at SEVERE level (already done in evaluator, but confirm here)
        if (result.getDecision() == PolicyDecision.PERMIT && result.getDecidingPolicy() != null) {
            try {
                Optional<AccessPolicy> decidingPolicy = accessPolicyRepository.findById(result.getDecidingPolicy());
                if (decidingPolicy.isPresent() && decidingPolicy.get().getPolicyType() == PolicyType.EMERGENCY_OVERRIDE) {
                    LOGGER.log(Level.SEVERE, "EMERGENCY OVERRIDE CONFIRMED: Professional {0} accessed patient {1} records",
                            new Object[]{request.getProfessionalId(), request.getPatientCi()});
                }
            } catch (Exception e) {
                // Don't fail if we can't confirm emergency override
            }
        }
    }
}
