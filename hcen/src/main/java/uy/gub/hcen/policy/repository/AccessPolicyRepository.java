package uy.gub.hcen.policy.repository;

import uy.gub.hcen.policy.entity.AccessPolicy;
import uy.gub.hcen.policy.entity.MedicalSpecialty;
import uy.gub.hcen.policy.entity.PolicyStatus;

import java.util.List;
import java.util.Optional;

/**
 * Access Policy Repository Interface
 *
 * Data access interface for managing patient-defined access control policies.
 * Provides methods for CRUD operations and policy queries based on clinic and specialty.
 *
 * @author TSE 2025 Group 9
 * @version 2.0
 * @since 2025-11-18
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
     * Returns only currently valid policies (GRANTED status, within validity period)
     *
     * @param patientCi Patient's CI
     * @return List of active policies
     */
    List<AccessPolicy> findByPatientCi(String patientCi);

    /**
     * Finds all policies for a patient, including revoked and expired ones
     *
     * @param patientCi Patient's CI
     * @return List of all policies
     */
    List<AccessPolicy> findAllByPatientCi(String patientCi);

    /**
     * Finds policies by patient and clinic
     *
     * @param patientCi Patient's CI
     * @param clinicId Clinic ID
     * @return List of policies matching criteria
     */
    List<AccessPolicy> findByPatientCiAndClinicId(String patientCi, String clinicId);

    /**
     * Finds policies by patient and specialty
     *
     * @param patientCi Patient's CI
     * @param specialty Medical specialty
     * @return List of policies matching criteria
     */
    List<AccessPolicy> findByPatientCiAndSpecialty(String patientCi, MedicalSpecialty specialty);

    /**
     * Finds policies by patient, clinic, and specialty
     * This is the primary query for policy evaluation
     *
     * @param patientCi Patient's CI
     * @param clinicId Clinic ID
     * @param specialty Medical specialty
     * @return List of policies matching criteria
     */
    List<AccessPolicy> findByPatientCiAndClinicIdAndSpecialty(
            String patientCi, String clinicId, MedicalSpecialty specialty);

    /**
     * Finds policies by patient, clinic, specialty, and status
     *
     * @param patientCi Patient's CI
     * @param clinicId Clinic ID
     * @param specialty Medical specialty
     * @param status Policy status
     * @return List of policies matching criteria
     */
    List<AccessPolicy> findByPatientCiAndClinicIdAndSpecialtyAndStatus(
            String patientCi, String clinicId, MedicalSpecialty specialty, PolicyStatus status);

    /**
     * Finds policies by patient and document ID
     * For finding policies specific to a document
     *
     * @param patientCi Patient's CI
     * @param documentId Document ID
     * @return List of policies for the specific document
     */
    List<AccessPolicy> findByPatientCiAndDocumentId(String patientCi, Long documentId);

    /**
     * Checks if a policy exists for the given patient, clinic, and specialty
     *
     * @param patientCi Patient's CI
     * @param clinicId Clinic ID
     * @param specialty Medical specialty
     * @return true if a GRANTED policy exists
     */
    boolean existsActivePolicy(String patientCi, String clinicId, MedicalSpecialty specialty);

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
     * Counts active (GRANTED) policies for a patient
     *
     * @param patientCi Patient's CI
     * @return Count of active policies
     */
    long countActiveByPatientCi(String patientCi);

    /**
     * Counts policies by status
     *
     * @param status Policy status
     * @return Count of policies with this status
     */
    long countByStatus(PolicyStatus status);

    /**
     * Counts policies by clinic
     *
     * @param clinicId Clinic ID
     * @return Count of policies for this clinic
     */
    long countByClinicId(String clinicId);
}
