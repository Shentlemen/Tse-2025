package uy.gub.hcen.policy.repository;

import uy.gub.hcen.policy.entity.AccessRequest;
import uy.gub.hcen.policy.entity.AccessRequest.RequestStatus;

import java.util.List;
import java.util.Optional;

/**
 * Access Request Repository Interface
 * <p>
 * Data access interface for managing professional access requests.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @see AccessRequest
 * @since 2025-10-17
 */
public interface AccessRequestRepository {

    /**
     * Saves a new access request
     *
     * @param request The request to save
     * @return The saved request with generated ID
     */
    AccessRequest save(AccessRequest request);

    /**
     * Finds a request by its ID
     *
     * @param id Request ID
     * @return Optional containing the request if found
     */
    Optional<AccessRequest> findById(Long id);

    /**
     * Finds all pending requests for a patient
     *
     * @param patientCi Patient's CI
     * @return List of pending requests
     */
    List<AccessRequest> findPendingByPatientCi(String patientCi);

    /**
     * Finds all requests for a patient (any status)
     *
     * @param patientCi Patient's CI
     * @param page      Page number
     * @param size      Page size
     * @return List of requests
     */
    List<AccessRequest> findByPatientCi(String patientCi, int page, int size);

    /**
     * Finds requests by patient and status
     *
     * @param patientCi Patient's CI
     * @param status    Request status
     * @param page      Page number
     * @param size      Page size
     * @return List of requests matching criteria
     */
    List<AccessRequest> findByPatientCiAndStatus(String patientCi, RequestStatus status, int page, int size);

    /**
     * Finds all requests made by a professional
     *
     * @param professionalId Professional ID
     * @param page           Page number
     * @param size           Page size
     * @return List of requests
     */
    List<AccessRequest> findByProfessionalId(String professionalId, int page, int size);

    /**
     * Updates a request status
     *
     * @param request The request to update
     * @return The updated request
     */
    AccessRequest update(AccessRequest request);

    /**
     * Finds all expired pending requests
     *
     * @return List of expired requests that are still marked as PENDING
     */
    List<AccessRequest> findExpiredPendingRequests();

    /**
     * Marks expired requests as EXPIRED
     *
     * @return Number of requests updated
     */
    int markExpiredRequests();

    /**
     * Counts pending requests for a patient
     *
     * @param patientCi Patient's CI
     * @return Count of pending requests
     */
    long countPendingByPatientCi(String patientCi);

    /**
     * Counts requests by status (for statistics)
     *
     * @param status Request status
     * @return Count of requests with this status
     */
    long countByStatus(RequestStatus status);

    /**
     * Finds an existing pending request for deduplication
     * <p>
     * Searches for a non-expired PENDING request with the same professional ID,
     * patient CI, and document ID. Used to prevent duplicate access requests.
     *
     * @param professionalId Professional ID
     * @param patientCi      Patient CI
     * @param documentId     Document ID (nullable)
     * @return Optional containing existing pending request if found
     */
    Optional<AccessRequest> findPendingRequest(String professionalId, String patientCi, Long documentId);

    /**
     * Finds an approved access request for a specific specialty and document.
     * Used by PolicyEngine to grant access based on patient approval.
     * <p>
     * This method searches for APPROVED (not expired) requests that match:
     * - specialty (any professional with this specialty can access)
     * - clinicId (must be from the same clinic)
     * - patientCi (patient who approved)
     * - documentId (or documentId IS NULL for general access)
     * <p>
     * This allows specialty-based approval: once a patient approves access for
     * "any cardiologist at Clinic X", all cardiologists from Clinic X can access
     * the approved documents.
     *
     * @param specialty  Medical specialty code (e.g., "CAR", "DER")
     * @param clinicId   Clinic ID
     * @param patientCi  Patient CI
     * @param documentId Document ID (nullable - if null, finds general access requests)
     * @return Optional containing approved request if found
     */
    Optional<AccessRequest> findApprovedRequestBySpecialty(String specialty, String clinicId, String patientCi, Long documentId);
}
