package uy.gub.hcen.service.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import uy.gub.hcen.audit.entity.AuditLog;
import uy.gub.hcen.audit.entity.AuditLog.ActionOutcome;
import uy.gub.hcen.audit.entity.AuditLog.EventType;
import uy.gub.hcen.audit.repository.AuditLogRepository;
import uy.gub.hcen.rndc.entity.DocumentType;
import uy.gub.hcen.service.audit.dto.AuditEventRequest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Audit Service - Core Audit Logging and Compliance Service
 *
 * Provides comprehensive audit logging for all system operations in HCEN.
 * This service ensures immutable audit trails for compliance requirements
 * (AC026 - patients can view who accessed their records and when).
 *
 * <p>Key Features:
 * <ul>
 *   <li>Immutable audit trail (append-only, no updates/deletes)</li>
 *   <li>Comprehensive event logging (access, modifications, authentication, policies)</li>
 *   <li>Flexible query capabilities for compliance reporting</li>
 *   <li>Fail-safe design (never throws exceptions to business logic)</li>
 *   <li>JSON serialization of contextual details</li>
 *   <li>Patient access history (CU05 - "Who viewed my records?")</li>
 * </ul>
 *
 * Design Principles:
 * <ul>
 *   <li><b>Never Throw Exceptions</b>: Audit failures should not break business operations</li>
 *   <li><b>Fail Gracefully</b>: Log errors but continue processing</li>
 *   <li><b>Immutable Trail</b>: Only save() operations, no updates or deletes</li>
 *   <li><b>Privacy by Design</b>: Don't log sensitive data (passwords, full CI in plaintext)</li>
 *   <li><b>Performance</b>: Async logging recommended for high-traffic operations</li>
 * </ul>
 *
 * Example Usage:
 * <pre>
 * {@literal @}Inject
 * private AuditService auditService;
 *
 * // Log document access
 * auditService.logDocumentAccess(
 *     "prof-123",           // professionalId
 *     "12345678",          // patientCi
 *     456L,                // documentId
 *     DocumentType.LAB_RESULT,
 *     ActionOutcome.SUCCESS,
 *     "192.168.1.100",     // ipAddress
 *     "Mozilla/5.0..."     // userAgent
 * );
 *
 * // Log authentication event
 * auditService.logAuthenticationEvent(
 *     "12345678",          // patientCi
 *     ActionOutcome.SUCCESS,
 *     "192.168.1.100",
 *     "Mozilla/5.0...",
 *     Map.of("method", "gub.uy", "provider", "oidc")
 * );
 *
 * // Query patient access history (CU05)
 * List&lt;AuditLog&gt; history = auditService.getPatientAccessHistory("12345678", 0, 20);
 * </pre>
 *
 * Compliance:
 * <ul>
 *   <li>AC026: Patients can view who accessed their records</li>
 *   <li>Ley N° 18.331 (Data Protection Law of Uruguay)</li>
 *   <li>AGESIC Guidelines for Public Sector Information Security</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 * @see AuditLog
 * @see AuditLogRepository
 * @see AuditEventRequest
 */
@Stateless
public class AuditService {

    private static final Logger LOGGER = Logger.getLogger(AuditService.class.getName());

    /**
     * ObjectMapper for JSON serialization of audit details
     * Reused for performance (thread-safe)
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Inject
    private AuditLogRepository auditLogRepository;

    // =========================================================================
    // Core Logging Methods
    // =========================================================================

    /**
     * Logs an audit event with full parameters
     *
     * This is the primary method for logging audit events. All other logging
     * methods delegate to this one.
     *
     * <p>Important: This method NEVER throws exceptions. If logging fails,
     * it logs the error and continues. Audit failures should not break
     * business operations.
     *
     * @param eventType Type of event (ACCESS, MODIFICATION, CREATION, etc.)
     * @param actorId Identifier of the actor (CI, professional ID, "system")
     * @param actorType Type of actor (PATIENT, PROFESSIONAL, ADMIN, SYSTEM)
     * @param resourceType Type of resource (DOCUMENT, USER, POLICY, CLINIC, etc.)
     * @param resourceId Identifier of the specific resource
     * @param outcome Outcome of the action (SUCCESS, FAILURE, DENIED)
     * @param ipAddress IP address of the actor (optional)
     * @param userAgent User agent string (optional)
     * @param details Additional context as a map (optional, will be serialized to JSON)
     */
    public void logEvent(EventType eventType, String actorId, String actorType,
                         String resourceType, String resourceId, ActionOutcome outcome,
                         String ipAddress, String userAgent, Map<String, Object> details) {

        // Validate required parameters
        if (!validateRequiredParameters(eventType, actorId, actorType, resourceType, resourceId, outcome)) {
            LOGGER.warning("Missing required audit parameters - event not logged");
            return; // Fail gracefully, don't throw exception
        }

        try {
            // Serialize details to JSON (if present)
            String detailsJson = null;
            if (details != null && !details.isEmpty()) {
                detailsJson = serializeDetails(details);
            }

            // Create AuditLog entity using constructor (entity is immutable)
            AuditLog auditLog = new AuditLog(
                    eventType,
                    actorId,
                    actorType,
                    resourceType,
                    resourceId,
                    outcome,
                    ipAddress,
                    userAgent,
                    detailsJson
            );

            // Save to database (append-only)
            auditLogRepository.save(auditLog);

            LOGGER.log(Level.INFO, "Audit event logged: {0} by {1} on {2}/{3} - {4}",
                    new Object[]{eventType, actorId, resourceType, resourceId, outcome});

        } catch (Exception e) {
            // NEVER throw - audit logging should not break business logic
            LOGGER.log(Level.SEVERE, "Failed to save audit log: " + e.getMessage(), e);
        }
    }

    /**
     * Logs an audit event using AuditEventRequest DTO
     *
     * Convenience method for programmatic logging with DTOs.
     *
     * @param request Audit event request
     */
    public void logEvent(AuditEventRequest request) {
        if (request == null) {
            LOGGER.warning("AuditEventRequest is null - event not logged");
            return;
        }

        if (!request.isValid()) {
            LOGGER.warning("AuditEventRequest is invalid - event not logged: " + request);
            return;
        }

        logEvent(
                request.getEventType(),
                request.getActorId(),
                request.getActorType(),
                request.getResourceType(),
                request.getResourceId(),
                request.getActionOutcome(),
                request.getIpAddress(),
                request.getUserAgent(),
                request.getDetails()
        );
    }

    // =========================================================================
    // Convenience Methods - Event Type Shortcuts
    // =========================================================================

    /**
     * Logs an access event (shortcut for EventType.ACCESS)
     *
     * Use this when a user or system accesses a resource.
     *
     * @param actorId Actor identifier
     * @param actorType Type of actor
     * @param resourceType Type of resource
     * @param resourceId Resource identifier
     * @param outcome Outcome of access attempt
     * @param ipAddress IP address (optional)
     * @param userAgent User agent string (optional)
     * @param details Additional context (optional)
     */
    public void logAccessEvent(String actorId, String actorType, String resourceType,
                               String resourceId, ActionOutcome outcome,
                               String ipAddress, String userAgent, Map<String, Object> details) {
        logEvent(EventType.ACCESS, actorId, actorType, resourceType, resourceId,
                outcome, ipAddress, userAgent, details);
    }

    /**
     * Logs a creation event (shortcut for EventType.CREATION)
     *
     * @param actorId Actor identifier
     * @param actorType Type of actor
     * @param resourceType Type of resource
     * @param resourceId Resource identifier
     * @param ipAddress IP address (optional)
     * @param userAgent User agent string (optional)
     * @param details Additional context (optional)
     */
    public void logCreationEvent(String actorId, String actorType, String resourceType,
                                 String resourceId, String ipAddress, String userAgent,
                                 Map<String, Object> details) {
        logEvent(EventType.CREATION, actorId, actorType, resourceType, resourceId,
                ActionOutcome.SUCCESS, ipAddress, userAgent, details);
    }

    /**
     * Logs a modification event (shortcut for EventType.MODIFICATION)
     *
     * @param actorId Actor identifier
     * @param actorType Type of actor
     * @param resourceType Type of resource
     * @param resourceId Resource identifier
     * @param ipAddress IP address (optional)
     * @param userAgent User agent string (optional)
     * @param details Additional context (optional)
     */
    public void logModificationEvent(String actorId, String actorType, String resourceType,
                                     String resourceId, String ipAddress, String userAgent,
                                     Map<String, Object> details) {
        logEvent(EventType.MODIFICATION, actorId, actorType, resourceType, resourceId,
                ActionOutcome.SUCCESS, ipAddress, userAgent, details);
    }

    /**
     * Logs a deletion event (shortcut for EventType.DELETION)
     *
     * @param actorId Actor identifier
     * @param actorType Type of actor
     * @param resourceType Type of resource
     * @param resourceId Resource identifier
     * @param ipAddress IP address (optional)
     * @param userAgent User agent string (optional)
     * @param details Additional context (optional)
     */
    public void logDeletionEvent(String actorId, String actorType, String resourceType,
                                 String resourceId, String ipAddress, String userAgent,
                                 Map<String, Object> details) {
        logEvent(EventType.DELETION, actorId, actorType, resourceType, resourceId,
                ActionOutcome.SUCCESS, ipAddress, userAgent, details);
    }

    // =========================================================================
    // Domain-Specific Logging Methods
    // =========================================================================

    /**
     * Logs authentication event (login attempt)
     *
     * Used for tracking successful and failed authentication attempts.
     *
     * @param actorId Actor identifier (CI or username)
     * @param outcome Outcome (SUCCESS or FAILURE)
     * @param ipAddress IP address
     * @param userAgent User agent string
     * @param details Additional context (e.g., authentication method, provider)
     */
    public void logAuthenticationEvent(String actorId, ActionOutcome outcome,
                                       String ipAddress, String userAgent,
                                       Map<String, Object> details) {
        EventType eventType = (outcome == ActionOutcome.SUCCESS)
                ? EventType.AUTHENTICATION_SUCCESS
                : EventType.AUTHENTICATION_FAILURE;

        // Enhance details with authentication-specific info
        Map<String, Object> enhancedDetails = details != null ? new HashMap<>(details) : new HashMap<>();
        enhancedDetails.put("timestamp", LocalDateTime.now().toString());

        logEvent(eventType, actorId, "PATIENT", "SESSION", actorId,
                outcome, ipAddress, userAgent, enhancedDetails);
    }

    /**
     * Logs policy change by patient
     *
     * Tracks when patients modify their access control policies.
     * Compliance requirement: AC026 (audit trail of policy changes)
     *
     * @param patientCi Patient's CI
     * @param policyId Policy identifier
     * @param action Action performed (CREATE, UPDATE, DELETE)
     * @param ipAddress IP address
     * @param userAgent User agent string
     */
    public void logPolicyChange(String patientCi, Long policyId, String action,
                                String ipAddress, String userAgent) {
        Map<String, Object> details = new HashMap<>();
        details.put("policyId", policyId);
        details.put("action", action);

        logEvent(EventType.POLICY_CHANGE, patientCi, "PATIENT", "POLICY",
                policyId.toString(), ActionOutcome.SUCCESS, ipAddress, userAgent, details);
    }

    /**
     * Logs document access by health professional
     *
     * Tracks every access to patient documents for compliance (AC026).
     * Patients can view this history via CU05 (getPatientAccessHistory).
     *
     * @param professionalId Professional's ID
     * @param patientCi Patient's CI
     * @param documentId Document ID
     * @param documentType Type of document
     * @param outcome Outcome (SUCCESS, DENIED)
     * @param ipAddress IP address
     * @param userAgent User agent string
     */
    public void logDocumentAccess(String professionalId, String patientCi, Long documentId,
                                  DocumentType documentType, ActionOutcome outcome,
                                  String ipAddress, String userAgent) {
        Map<String, Object> details = new HashMap<>();
        details.put("patientCi", patientCi);
        details.put("documentType", documentType.name());
        details.put("documentDisplayName", documentType.getDisplayName());

        logEvent(EventType.ACCESS, professionalId, "PROFESSIONAL", "DOCUMENT",
                documentId.toString(), outcome, ipAddress, userAgent, details);
    }

    /**
     * Logs document registration in RNDC
     *
     * Tracks when new documents are registered by peripheral nodes.
     *
     * @param clinicId Clinic identifier
     * @param documentId Document ID
     * @param patientCi Patient's CI
     * @param documentType Type of document
     * @param createdBy Professional who created the document
     */
    public void logDocumentRegistration(String clinicId, Long documentId, String patientCi,
                                        DocumentType documentType, String createdBy) {
        Map<String, Object> details = new HashMap<>();
        details.put("patientCi", patientCi);
        details.put("documentType", documentType.name());
        details.put("createdBy", createdBy);
        details.put("clinicId", clinicId);

        logEvent(EventType.CREATION, "system", "SYSTEM", "DOCUMENT",
                documentId.toString(), ActionOutcome.SUCCESS, null, null, details);
    }

    /**
     * Logs access request by professional (pending patient approval)
     *
     * Tracks when a professional requests access to a document that
     * requires patient approval.
     *
     * @param professionalId Professional's ID
     * @param patientCi Patient's CI
     * @param documentId Document ID
     * @param ipAddress IP address
     * @param userAgent User agent string
     */
    public void logAccessRequest(String professionalId, String patientCi, Long documentId,
                                 String ipAddress, String userAgent) {
        Map<String, Object> details = new HashMap<>();
        details.put("patientCi", patientCi);
        details.put("status", "PENDING");

        logEvent(EventType.ACCESS_REQUEST, professionalId, "PROFESSIONAL", "DOCUMENT",
                documentId.toString(), ActionOutcome.SUCCESS, ipAddress, userAgent, details);
    }

    /**
     * Logs access approval by patient
     *
     * @param patientCi Patient's CI
     * @param professionalId Professional's ID
     * @param documentId Document ID
     * @param ipAddress IP address
     * @param userAgent User agent string
     */
    public void logAccessApproval(String patientCi, String professionalId, Long documentId,
                                  String ipAddress, String userAgent) {
        Map<String, Object> details = new HashMap<>();
        details.put("professionalId", professionalId);

        logEvent(EventType.ACCESS_APPROVAL, patientCi, "PATIENT", "DOCUMENT",
                documentId.toString(), ActionOutcome.SUCCESS, ipAddress, userAgent, details);
    }

    /**
     * Logs access denial by patient
     *
     * @param patientCi Patient's CI
     * @param professionalId Professional's ID
     * @param documentId Document ID
     * @param ipAddress IP address
     * @param userAgent User agent string
     */
    public void logAccessDenial(String patientCi, String professionalId, Long documentId,
                                String ipAddress, String userAgent) {
        Map<String, Object> details = new HashMap<>();
        details.put("professionalId", professionalId);

        logEvent(EventType.ACCESS_DENIAL, patientCi, "PATIENT", "DOCUMENT",
                documentId.toString(), ActionOutcome.DENIED, ipAddress, userAgent, details);
    }

    // =========================================================================
    // Query Methods
    // =========================================================================

    /**
     * Gets patient access history - who accessed patient's documents (AC026)
     *
     * Use case: Patient wants to see who viewed their records (CU05)
     * Returns all ACCESS events for documents belonging to this patient.
     *
     * @param patientCi Patient's CI
     * @param page Page number (0-based)
     * @param size Page size (max 100)
     * @return List of access audit logs
     */
    public List<AuditLog> getPatientAccessHistory(String patientCi, int page, int size) {
        if (patientCi == null || patientCi.isEmpty()) {
            LOGGER.warning("patientCi is required for getPatientAccessHistory");
            return Collections.emptyList();
        }

        // Validate and sanitize pagination
        page = Math.max(0, page);
        size = Math.max(1, Math.min(100, size));

        try {
            return auditLogRepository.getPatientAccessHistory(patientCi, page, size);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve patient access history: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Searches audit logs with flexible criteria
     *
     * All parameters are optional (null = no filter on that criterion).
     *
     * @param eventType Optional event type filter
     * @param actorId Optional actor filter
     * @param resourceType Optional resource type filter
     * @param fromDate Optional start date
     * @param toDate Optional end date
     * @param outcome Optional outcome filter
     * @param page Page number (0-based)
     * @param size Page size (max 100)
     * @return List of audit logs matching criteria
     */
    public List<AuditLog> searchAuditLogs(EventType eventType, String actorId, String resourceType,
                                          LocalDateTime fromDate, LocalDateTime toDate,
                                          ActionOutcome outcome, int page, int size) {
        // Validate and sanitize pagination
        page = Math.max(0, page);
        size = Math.max(1, Math.min(100, size));

        try {
            return auditLogRepository.search(eventType, actorId, resourceType,
                    fromDate, toDate, outcome, page, size);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to search audit logs: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets audit logs by actor ID
     *
     * @param actorId Actor identifier
     * @param page Page number (0-based)
     * @param size Page size (max 100)
     * @return List of audit logs
     */
    public List<AuditLog> getAuditLogsByActor(String actorId, int page, int size) {
        if (actorId == null || actorId.isEmpty()) {
            LOGGER.warning("actorId is required for getAuditLogsByActor");
            return Collections.emptyList();
        }

        page = Math.max(0, page);
        size = Math.max(1, Math.min(100, size));

        try {
            return auditLogRepository.findByActorId(actorId, page, size);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve audit logs by actor: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets audit logs by resource
     *
     * @param resourceType Resource type
     * @param resourceId Resource identifier
     * @param page Page number (0-based)
     * @param size Page size (max 100)
     * @return List of audit logs
     */
    public List<AuditLog> getAuditLogsByResource(String resourceType, String resourceId,
                                                 int page, int size) {
        if (resourceType == null || resourceType.isEmpty() || resourceId == null || resourceId.isEmpty()) {
            LOGGER.warning("resourceType and resourceId are required for getAuditLogsByResource");
            return Collections.emptyList();
        }

        page = Math.max(0, page);
        size = Math.max(1, Math.min(100, size));

        try {
            return auditLogRepository.findByResource(resourceType, resourceId, page, size);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve audit logs by resource: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets audit logs by event type
     *
     * @param eventType Event type
     * @param page Page number (0-based)
     * @param size Page size (max 100)
     * @return List of audit logs
     */
    public List<AuditLog> getAuditLogsByEventType(EventType eventType, int page, int size) {
        if (eventType == null) {
            LOGGER.warning("eventType is required for getAuditLogsByEventType");
            return Collections.emptyList();
        }

        page = Math.max(0, page);
        size = Math.max(1, Math.min(100, size));

        try {
            return auditLogRepository.findByEventType(eventType, page, size);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve audit logs by event type: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets audit logs within a date range
     *
     * @param fromDate Start date (inclusive)
     * @param toDate End date (inclusive)
     * @param page Page number (0-based)
     * @param size Page size (max 100)
     * @return List of audit logs
     */
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime fromDate, LocalDateTime toDate,
                                                  int page, int size) {
        if (fromDate == null || toDate == null) {
            LOGGER.warning("fromDate and toDate are required for getAuditLogsByDateRange");
            return Collections.emptyList();
        }

        page = Math.max(0, page);
        size = Math.max(1, Math.min(100, size));

        try {
            return auditLogRepository.findByDateRange(fromDate, toDate, page, size);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve audit logs by date range: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // =========================================================================
    // Statistics Methods
    // =========================================================================

    /**
     * Counts events by event type
     *
     * @param eventType Event type
     * @return Count of events with this type
     */
    public long countEventsByType(EventType eventType) {
        if (eventType == null) {
            LOGGER.warning("eventType is required for countEventsByType");
            return 0;
        }

        try {
            return auditLogRepository.countByEventType(eventType);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to count events by type: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Counts events by outcome
     *
     * @param outcome Action outcome
     * @return Count of events with this outcome
     */
    public long countEventsByOutcome(ActionOutcome outcome) {
        if (outcome == null) {
            LOGGER.warning("outcome is required for countEventsByOutcome");
            return 0;
        }

        try {
            return auditLogRepository.countByOutcome(outcome);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to count events by outcome: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Counts events by actor
     *
     * @param actorId Actor identifier
     * @return Count of events by this actor
     */
    public long countEventsByActor(String actorId) {
        if (actorId == null || actorId.isEmpty()) {
            LOGGER.warning("actorId is required for countEventsByActor");
            return 0;
        }

        try {
            // Note: This requires a custom query in repository
            // For now, use findByActorId and count results
            // TODO: Add countByActorId to repository for efficiency
            List<AuditLog> logs = auditLogRepository.findByActorId(actorId, 0, Integer.MAX_VALUE);
            return logs.size();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to count events by actor: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Gets total audit log count
     *
     * @return Total count of all audit logs
     */
    public long getTotalAuditLogCount() {
        try {
            return auditLogRepository.countAll();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to count total audit logs: " + e.getMessage(), e);
            return 0;
        }
    }

    // =========================================================================
    // Private Helper Methods
    // =========================================================================

    /**
     * Validates required parameters for audit logging
     *
     * @return true if all required parameters are present
     */
    private boolean validateRequiredParameters(EventType eventType, String actorId, String actorType,
                                                String resourceType, String resourceId,
                                                ActionOutcome outcome) {
        return eventType != null &&
               actorId != null && !actorId.isEmpty() &&
               actorType != null && !actorType.isEmpty() &&
               resourceType != null && !resourceType.isEmpty() &&
               resourceId != null && !resourceId.isEmpty() &&
               outcome != null;
    }

    /**
     * Serializes details map to JSON string
     *
     * @param details Map of details
     * @return JSON string or null if serialization fails
     */
    private String serializeDetails(Map<String, Object> details) {
        try {
            return OBJECT_MAPPER.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.WARNING, "Failed to serialize audit details: " + e.getMessage(), e);
            // Return a simple error indicator
            return "{\"error\":\"Failed to serialize details\"}";
        }
    }
}
