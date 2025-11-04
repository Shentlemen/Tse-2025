package uy.gub.hcen.audit.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import uy.gub.hcen.audit.dto.AuditLogListResponse;
import uy.gub.hcen.audit.dto.AuditLogResponse;
import uy.gub.hcen.audit.dto.AuditStatisticsResponse;
import uy.gub.hcen.audit.entity.AuditLog;
import uy.gub.hcen.audit.entity.AuditLog.ActionOutcome;
import uy.gub.hcen.audit.entity.AuditLog.EventType;
import uy.gub.hcen.audit.repository.AuditLogRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Audit Log Service - Patient-Facing Audit Query Service
 *
 * Provides query capabilities for patients to view their audit history.
 * Implements AC026 requirement: patients can view who accessed their records.
 *
 * <p>Key Features:
 * <ul>
 *   <li>Patient access history (who viewed my records)</li>
 *   <li>Filtering by date range and event type</li>
 *   <li>Pagination support</li>
 *   <li>Statistics dashboard (total accesses, event type breakdown)</li>
 * </ul>
 *
 * Design Principles:
 * <ul>
 *   <li><b>Patient Privacy</b>: Only show audit logs related to the authenticated patient</li>
 *   <li><b>Read-Only</b>: No modification of audit logs (immutable trail)</li>
 *   <li><b>Performance</b>: Pagination and indexed queries for large result sets</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-04
 * @see AuditLog
 * @see AuditLogRepository
 */
@Stateless
public class AuditLogService {

    private static final Logger LOGGER = Logger.getLogger(AuditLogService.class.getName());

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    @Inject
    private AuditLogRepository auditLogRepository;

    // =========================================================================
    // Patient Audit History Methods
    // =========================================================================

    /**
     * Gets patient audit logs with filtering and pagination
     *
     * Returns audit logs where the patient's data was accessed or modified.
     * Supports filtering by date range and event type.
     *
     * Use case: Patient wants to see "Who accessed my records?"
     *
     * @param patientCi Patient's CI
     * @param fromDate Start date (optional)
     * @param toDate End date (optional)
     * @param eventType Event type filter (optional)
     * @param page Page number (0-based)
     * @param size Page size
     * @return Paginated list of audit log responses
     */
    public AuditLogListResponse getPatientAuditLogs(String patientCi,
                                                     LocalDate fromDate,
                                                     LocalDate toDate,
                                                     String eventType,
                                                     int page,
                                                     int size) {
        // Validate required parameters
        if (patientCi == null || patientCi.trim().isEmpty()) {
            LOGGER.warning("patientCi is required for getPatientAuditLogs");
            return new AuditLogListResponse(Collections.emptyList(), 0, page, size);
        }

        // Sanitize pagination parameters
        page = Math.max(0, page);
        size = Math.max(1, Math.min(MAX_SIZE, size));

        try {
            // Convert dates to LocalDateTime
            LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
            LocalDateTime toDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;

            // Parse event type
            EventType eventTypeEnum = null;
            if (eventType != null && !eventType.trim().isEmpty()) {
                try {
                    eventTypeEnum = EventType.valueOf(eventType.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    LOGGER.log(Level.WARNING, "Invalid event type: " + eventType, e);
                    // Continue with null eventType (no filter)
                }
            }

            // Query audit logs
            // Note: This queries for events where the patient's data was involved
            // The repository should filter by details.patientCi field
            List<AuditLog> logs = auditLogRepository.search(
                    eventTypeEnum,
                    null,  // actorId - we want to see all actors who accessed patient data
                    null,  // resourceType - all resource types
                    fromDateTime,
                    toDateTime,
                    null,  // outcome - all outcomes
                    page,
                    size
            );

            // Filter logs to only include those related to this patient
            // Since search() doesn't filter by patient, we need to do it here
            List<AuditLog> patientLogs = filterLogsByPatient(logs, patientCi);

            // Get total count (for now, we'll use the filtered list size as an approximation)
            // TODO: Add repository method to count patient-specific logs
            long totalCount = patientLogs.size();

            // Convert to DTOs
            List<AuditLogResponse> responses = patientLogs.stream()
                    .map(AuditLogResponse::fromEntity)
                    .collect(Collectors.toList());

            LOGGER.log(Level.INFO, "Retrieved {0} audit logs for patient: {1}",
                    new Object[]{responses.size(), patientCi});

            return new AuditLogListResponse(responses, totalCount, page, size);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving audit logs for patient: " + patientCi, e);
            return new AuditLogListResponse(Collections.emptyList(), 0, page, size);
        }
    }

    /**
     * Gets patient audit statistics
     *
     * Returns summary statistics:
     * - Total access events
     * - Events by type (ACCESS, MODIFICATION, etc.)
     * - Events by outcome (SUCCESS, DENIED, FAILURE)
     * - Top accessors (professionals who accessed most frequently)
     *
     * @param patientCi Patient's CI
     * @return Audit statistics response
     */
    public AuditStatisticsResponse getPatientAuditStatistics(String patientCi) {
        // Validate required parameters
        if (patientCi == null || patientCi.trim().isEmpty()) {
            LOGGER.warning("patientCi is required for getPatientAuditStatistics");
            return new AuditStatisticsResponse(0, Collections.emptyMap(),
                    Collections.emptyMap(), Collections.emptyList());
        }

        try {
            // Get all patient audit logs (no pagination for stats)
            List<AuditLog> allLogs = auditLogRepository.getPatientAccessHistory(patientCi, 0, Integer.MAX_VALUE);

            // Calculate statistics
            long totalEvents = allLogs.size();

            // Group by event type
            Map<String, Long> eventsByType = allLogs.stream()
                    .collect(Collectors.groupingBy(
                            log -> log.getEventType() != null ? log.getEventType().name() : "UNKNOWN",
                            Collectors.counting()
                    ));

            // Group by outcome
            Map<String, Long> eventsByOutcome = allLogs.stream()
                    .collect(Collectors.groupingBy(
                            log -> log.getActionOutcome() != null ? log.getActionOutcome().name() : "UNKNOWN",
                            Collectors.counting()
                    ));

            // Find top actors (professionals who accessed most)
            Map<String, Long> actorCounts = allLogs.stream()
                    .filter(log -> log.getActorId() != null)
                    .filter(log -> !"PATIENT".equals(log.getActorType())) // Exclude patient's own actions
                    .collect(Collectors.groupingBy(
                            AuditLog::getActorId,
                            Collectors.counting()
                    ));

            List<AuditStatisticsResponse.ActorStat> topActors = actorCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10) // Top 10 actors
                    .map(entry -> new AuditStatisticsResponse.ActorStat(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

            LOGGER.log(Level.INFO, "Retrieved audit statistics for patient: {0} - {1} total events",
                    new Object[]{patientCi, totalEvents});

            return new AuditStatisticsResponse(totalEvents, eventsByType, eventsByOutcome, topActors);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving audit statistics for patient: " + patientCi, e);
            return new AuditStatisticsResponse(0, Collections.emptyMap(),
                    Collections.emptyMap(), Collections.emptyList());
        }
    }

    /**
     * Gets recent access count for patient (last 30 days)
     *
     * Used for dashboard statistics.
     *
     * @param patientCi Patient's CI
     * @return Count of access events in last 30 days
     */
    public long getRecentAccessCount(String patientCi) {
        if (patientCi == null || patientCi.trim().isEmpty()) {
            return 0;
        }

        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            LocalDateTime now = LocalDateTime.now();

            List<AuditLog> recentLogs = auditLogRepository.search(
                    EventType.ACCESS,
                    null,
                    null,
                    thirtyDaysAgo,
                    now,
                    ActionOutcome.SUCCESS,
                    0,
                    Integer.MAX_VALUE
            );

            // Filter by patient
            List<AuditLog> patientLogs = filterLogsByPatient(recentLogs, patientCi);

            return patientLogs.size();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving recent access count for patient: " + patientCi, e);
            return 0;
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Filters audit logs to include only those related to a specific patient
     *
     * Checks the "patientCi" field in the details JSON to determine if the
     * log is related to the patient.
     *
     * @param logs List of audit logs
     * @param patientCi Patient's CI
     * @return Filtered list containing only patient-related logs
     */
    private List<AuditLog> filterLogsByPatient(List<AuditLog> logs, String patientCi) {
        if (logs == null || logs.isEmpty()) {
            return Collections.emptyList();
        }

        return logs.stream()
                .filter(log -> isLogRelatedToPatient(log, patientCi))
                .collect(Collectors.toList());
    }

    /**
     * Checks if an audit log is related to a specific patient
     *
     * Examines the details field for a "patientCi" key.
     *
     * @param log Audit log
     * @param patientCi Patient's CI
     * @return true if log is related to patient
     */
    private boolean isLogRelatedToPatient(AuditLog log, String patientCi) {
        if (log == null || log.getDetails() == null) {
            return false;
        }

        // Parse the details JSON to check for patientCi
        try {
            AuditLogResponse response = AuditLogResponse.fromEntity(log);
            Map<String, Object> details = response.getDetails();

            if (details != null && details.containsKey("patientCi")) {
                Object patientCiValue = details.get("patientCi");
                return patientCi.equals(String.valueOf(patientCiValue));
            }

            // Also check if the actor is the patient (for patient's own actions)
            if (patientCi.equals(log.getActorId()) && "PATIENT".equals(log.getActorType())) {
                return true;
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error parsing audit log details for patient filter", e);
        }

        return false;
    }

    /**
     * Validates pagination parameters
     *
     * @param page Page number
     * @param size Page size
     * @throws IllegalArgumentException if parameters are invalid
     */
    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be non-negative");
        }

        if (size <= 0 || size > MAX_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_SIZE);
        }
    }
}
