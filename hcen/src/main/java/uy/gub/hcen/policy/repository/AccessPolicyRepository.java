package uy.gub.hcen.policy.repository;

import uy.gub.hcen.policy.entity.AccessPolicy;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyType;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyEffect;

import java.util.List;
import java.util.Optional;

/**
 * Access Policy Repository Interface
 *
 * Data access interface for managing patient-defined access control policies.
 * Provides methods for CRUD operations and policy queries.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 * @see AccessPolicy
 */
public interface AccessPolicyRepository {

    /**
     * Saves a new access policy
     *
     * @param policy The policy to save
     * @return The saved policy with generated ID
     */
    AccessPolicy save(AccessPolicy policy);

    /**
     * Finds a policy by its ID
     *
     * @param id Policy ID
     * @return Optional containing the policy if found
     */
    Optional<AccessPolicy> findById(Long id);

    /**
     * Finds all policies for a specific patient
     * Returns only currently valid policies (within validity period)
     *
     * @param patientCi Patient's CI
     * @return List of active policies
     */
    List<AccessPolicy> findByPatientCi(String patientCi);

    /**
     * Finds all policies for a patient, including expired ones
     *
     * @param patientCi Patient's CI
     * @return List of all policies
     */
    List<AccessPolicy> findAllByPatientCi(String patientCi);

    /**
     * Finds policies by patient and type
     *
     * @param patientCi Patient's CI
     * @param policyType Policy type
     * @return List of policies matching criteria
     */
    List<AccessPolicy> findByPatientCiAndType(String patientCi, PolicyType policyType);

    /**
     * Finds policies by patient, type, and effect
     *
     * @param patientCi Patient's CI
     * @param policyType Policy type
     * @param policyEffect Policy effect
     * @return List of policies matching criteria
     */
    List<AccessPolicy> findByPatientCiAndTypeAndEffect(
            String patientCi, PolicyType policyType, PolicyEffect policyEffect);

    /**
     * Updates an existing policy
     *
     * @param policy The policy to update
     * @return The updated policy
     */
    AccessPolicy update(AccessPolicy policy);

    /**
     * Deletes a policy by ID
     *
     * @param id Policy ID
     * @return true if deleted, false otherwise
     */
    boolean delete(Long id);

    /**
     * Deletes all policies for a patient
     *
     * @param patientCi Patient's CI
     * @return Number of policies deleted
     */
    int deleteByPatientCi(String patientCi);

    /**
     * Counts total policies for a patient
     *
     * @param patientCi Patient's CI
     * @return Count of policies
     */
    long countByPatientCi(String patientCi);

    /**
     * Counts policies by type for statistics
     *
     * @param policyType Policy type
     * @return Count of policies of this type
     */
    long countByType(PolicyType policyType);
}
