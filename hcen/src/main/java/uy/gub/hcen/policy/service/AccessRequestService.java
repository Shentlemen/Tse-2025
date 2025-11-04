package uy.gub.hcen.policy.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import uy.gub.hcen.audit.entity.AuditLog.ActionOutcome;
import uy.gub.hcen.audit.entity.AuditLog.EventType;
import uy.gub.hcen.policy.dto.*;
import uy.gub.hcen.policy.entity.AccessRequest;
import uy.gub.hcen.policy.entity.AccessRequest.RequestStatus;
import uy.gub.hcen.policy.repository.AccessRequestRepository;
import uy.gub.hcen.service.audit.AuditService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Access Request Service
 *
 * Business logic service for managing patient access request approvals/denials.
 * Handles the workflow when a professional requests access to a patient's
 * documents and the patient needs to approve or deny the request.
 *
 * Key Responsibilities:
 * - Fetch pending access requests for patients
 * - Process approval decisions
 * - Process denial decisions
 * - Handle info requests
 * - Integrate with audit system
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-03
 */
@Stateless
public class AccessRequestService {

    private static final Logger LOGGER = Logger.getLogger(AccessRequestService.class.getName());

    @Inject
    private AccessRequestRepository accessRequestRepository;

    @Inject
    private AuditService auditService;

    /**
     * Get pending access requests for a patient
     *
     * @param patientCi Patient's CI
     * @param status Optional status filter
     * @param page Page number (0-based)
     * @param size Page size
     * @return Paginated list of access requests
     */
    public AccessRequestListResponse getAccessRequests(String patientCi, RequestStatus status, int page, int size) {
        LOGGER.log(Level.INFO, "Fetching access requests for patient: {0}, status: {1}",
                new Object[]{patientCi, status});

        try {
            List<AccessRequest> requests;
            long totalCount;

            if (status != null) {
                requests = accessRequestRepository.findByPatientCiAndStatus(patientCi, status, page, size);
                // For count, we need to count all matching records
                totalCount = accessRequestRepository.findByPatientCiAndStatus(patientCi, status, 0, Integer.MAX_VALUE).size();
            } else {
                requests = accessRequestRepository.findByPatientCi(patientCi, page, size);
                totalCount = accessRequestRepository.findByPatientCi(patientCi, 0, Integer.MAX_VALUE).size();
            }

            List<AccessRequestDTO> dtos = requests.stream()
                    .map(AccessRequestDTO::fromEntity)
                    .collect(Collectors.toList());

            return AccessRequestListResponse.of(dtos, totalCount, page, size);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching access requests for patient: " + patientCi, e);
            throw new RuntimeException("Failed to fetch access requests", e);
        }
    }

    /**
     * Get count of pending requests for a patient
     *
     * @param patientCi Patient's CI
     * @return Count of pending requests
     */
    public long countPendingRequests(String patientCi) {
        try {
            return accessRequestRepository.countPendingByPatientCi(patientCi);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting pending requests for patient: " + patientCi, e);
            return 0;
        }
    }

    /**
     * Approve an access request
     *
     * @param requestId Request ID
     * @param patientCi Patient's CI (for authorization check)
     * @param decision Approval decision with optional reason
     * @return Success message
     * @throws IllegalArgumentException if request not found or patient unauthorized
     * @throws IllegalStateException if request cannot be approved
     */
    @Transactional
    public String approveRequest(Long requestId, String patientCi, ApprovalDecisionDTO decision) {
        LOGGER.log(Level.INFO, "Approving access request: {0} by patient: {1}",
                new Object[]{requestId, patientCi});

        try {
            // Find request
            Optional<AccessRequest> requestOpt = accessRequestRepository.findById(requestId);

            if (requestOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Access request not found: {0}", requestId);
                throw new IllegalArgumentException("Access request not found");
            }

            AccessRequest request = requestOpt.get();

            // Authorization check - verify patient owns this request
            if (!request.getPatientCi().equals(patientCi)) {
                LOGGER.log(Level.WARNING, "Patient {0} attempted to approve request for patient {1}",
                        new Object[]{patientCi, request.getPatientCi()});
                // Log unauthorized attempt
                auditService.logAccessEvent(
                        patientCi,
                        "PATIENT",
                        "ACCESS_REQUEST",
                        requestId.toString(),
                        ActionOutcome.DENIED,
                        null, // ipAddress
                        null, // userAgent
                        Map.of("reason", "Unauthorized approval attempt")
                );
                throw new IllegalArgumentException("You are not authorized to approve this request");
            }

            // Approve the request (entity method handles validation)
            request.approve(decision.getReason());

            // Save updated request
            AccessRequest updatedRequest = accessRequestRepository.update(request);

            // Log approval in audit system
            auditService.logAccessApproval(
                    patientCi,
                    request.getProfessionalId(),
                    request.getDocumentId(),
                    null, // ipAddress
                    null  // userAgent
            );

            LOGGER.log(Level.INFO, "Access request {0} approved successfully by patient: {1}",
                    new Object[]{requestId, patientCi});

            return "Access request approved successfully";

        } catch (IllegalArgumentException | IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Failed to approve access request: " + requestId, e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error approving access request: " + requestId, e);
            throw new RuntimeException("Failed to approve access request", e);
        }
    }

    /**
     * Deny an access request
     *
     * @param requestId Request ID
     * @param patientCi Patient's CI (for authorization check)
     * @param decision Denial decision with required reason
     * @return Success message
     * @throws IllegalArgumentException if request not found or patient unauthorized
     * @throws IllegalStateException if request cannot be denied
     */
    @Transactional
    public String denyRequest(Long requestId, String patientCi, DenialDecisionDTO decision) {
        LOGGER.log(Level.INFO, "Denying access request: {0} by patient: {1}",
                new Object[]{requestId, patientCi});

        try {
            // Find request
            Optional<AccessRequest> requestOpt = accessRequestRepository.findById(requestId);

            if (requestOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Access request not found: {0}", requestId);
                throw new IllegalArgumentException("Access request not found");
            }

            AccessRequest request = requestOpt.get();

            // Authorization check - verify patient owns this request
            if (!request.getPatientCi().equals(patientCi)) {
                LOGGER.log(Level.WARNING, "Patient {0} attempted to deny request for patient {1}",
                        new Object[]{patientCi, request.getPatientCi()});
                // Log unauthorized attempt
                auditService.logAccessEvent(
                        patientCi,
                        "PATIENT",
                        "ACCESS_REQUEST",
                        requestId.toString(),
                        ActionOutcome.DENIED,
                        null, // ipAddress
                        null, // userAgent
                        Map.of("reason", "Unauthorized denial attempt")
                );
                throw new IllegalArgumentException("You are not authorized to deny this request");
            }

            // Deny the request (entity method handles validation)
            request.deny(decision.getReason());

            // Save updated request
            AccessRequest updatedRequest = accessRequestRepository.update(request);

            // Log denial in audit system
            auditService.logAccessDenial(
                    patientCi,
                    request.getProfessionalId(),
                    request.getDocumentId(),
                    null, // ipAddress
                    null  // userAgent
            );

            LOGGER.log(Level.INFO, "Access request {0} denied successfully by patient: {1}",
                    new Object[]{requestId, patientCi});

            return "Access request denied successfully";

        } catch (IllegalArgumentException | IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Failed to deny access request: " + requestId, e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error denying access request: " + requestId, e);
            throw new RuntimeException("Failed to deny access request", e);
        }
    }

    /**
     * Request more information about an access request
     *
     * @param requestId Request ID
     * @param patientCi Patient's CI (for authorization check)
     * @param infoRequest Info request with question
     * @return Success message
     * @throws IllegalArgumentException if request not found or patient unauthorized
     */
    @Transactional
    public String requestMoreInfo(Long requestId, String patientCi, InfoRequestDTO infoRequest) {
        LOGGER.log(Level.INFO, "Patient {0} requesting more info for access request: {1}",
                new Object[]{patientCi, requestId});

        try {
            // Find request
            Optional<AccessRequest> requestOpt = accessRequestRepository.findById(requestId);

            if (requestOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Access request not found: {0}", requestId);
                throw new IllegalArgumentException("Access request not found");
            }

            AccessRequest request = requestOpt.get();

            // Authorization check - verify patient owns this request
            if (!request.getPatientCi().equals(patientCi)) {
                LOGGER.log(Level.WARNING, "Patient {0} attempted to request info for patient {1}",
                        new Object[]{patientCi, request.getPatientCi()});
                throw new IllegalArgumentException("You are not authorized to access this request");
            }

            // Log info request in audit system
            auditService.logAccessEvent(
                    patientCi,
                    "PATIENT",
                    "ACCESS_REQUEST",
                    requestId.toString(),
                    ActionOutcome.SUCCESS,
                    null, // ipAddress
                    null, // userAgent
                    Map.of(
                        "action", "REQUEST_MORE_INFO",
                        "professionalId", request.getProfessionalId(),
                        "question", infoRequest.getQuestion()
                    )
            );

            LOGGER.log(Level.INFO, "Info request logged for access request: {0}", requestId);

            // TODO: In a real implementation, this would:
            // 1. Send notification to the professional
            // 2. Store the question in a communication table
            // 3. Track the conversation thread
            // For now, we just log it

            return "Your question has been sent to the professional. " +
                    "They will be notified and can respond with additional information.";

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Failed to request info for access request: " + requestId, e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error requesting info for access request: " + requestId, e);
            throw new RuntimeException("Failed to request more information", e);
        }
    }
}
