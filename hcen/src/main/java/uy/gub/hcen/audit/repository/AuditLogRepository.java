package uy.gub.hcen.audit.repository;

import uy.gub.hcen.audit.entity.AuditLog;
import uy.gub.hcen.audit.entity.AuditLog.EventType;
import uy.gub.hcen.audit.entity.AuditLog.ActionOutcome;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Audit Log Repository Interface
 *
 * Data access interface for audit log operations.
 * This repository is APPEND-ONLY - no update or delete operations are allowed
 * to ensure audit trail integrity (AC026 compliance).
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 * @see AuditLog
 */
public interface AuditLogRepository {

    /**
     * Saves a new audit log entry
     * This is the ONLY modification operation allowed (append-only)
     *
     * @param auditLog The audit log to save
     * @return The saved audit log with generated ID
     */
    AuditLog save(AuditLog auditLog);

    /**
     * Finds audit logs by actor ID
     * Use case: "Show all actions by user X"
     *
     * @param actorId Actor identifier
     * @param page Page number
     * @param size Page size
     * @return List of audit logs
     */
    List<AuditLog> findByActorId(String actorId, int page, int size);

    /**
     * Finds audit logs by resource (type + ID)
     * Use case: "Show all actions on document Y"
     *
     * @param resourceType Resource type
     * @param resourceId Resource identifier
     * @param page Page number
     * @param size Page size
     * @return List of audit logs
     */
    List<AuditLog> findByResource(String resourceType, String resourceId, int page, int size);

    /**
     * Finds audit logs by event type
     * Use case: "Show all document access events"
     *
     * @param eventType Event type
     * @param page Page number
     * @param size Page size
     * @return List of audit logs
     */
    List<AuditLog> findByEventType(EventType eventType, int page, int size);

    /**
     * Finds audit logs within a date range
     * Use case: "Show all events from last week"
     *
     * @param fromDate Start date (inclusive)
     * @param toDate End date (inclusive)
     * @param page Page number
     * @param size Page size
     * @return List of audit logs
     */
    List<AuditLog> findByDateRange(LocalDateTime fromDate, LocalDateTime toDate, int page, int size);

    /**
     * Gets patient access history - who accessed patient's documents (CU05, AC026)
     * Use case: Patient wants to see who viewed their records
     *
     * @param patientCi Patient's CI
     * @param page Page number
     * @param size Page size
     * @return List of access audit logs for this patient
     */
    List<AuditLog> getPatientAccessHistory(String patientCi, int page, int size);

    /**
     * Searches audit logs with flexible criteria
     *
     * @param eventType Optional event type filter
     * @param actorId Optional actor filter
     * @param resourceType Optional resource type filter
     * @param fromDate Optional start date
     * @param toDate Optional end date
     * @param actionOutcome Optional outcome filter
     * @param page Page number
     * @param size Page size
     * @return List of audit logs matching criteria
     */
    List<AuditLog> search(
            EventType eventType,
            String actorId,
            String resourceType,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            ActionOutcome actionOutcome,
            int page,
            int size
    );

    /**
     * Counts total audit logs
     *
     * @return Total count
     */
    long countAll();

    /**
     * Counts audit logs by event type (for statistics)
     *
     * @param eventType Event type
     * @return Count of logs with this event type
     */
    long countByEventType(EventType eventType);

    /**
     * Counts audit logs by outcome (for statistics)
     *
     * @param actionOutcome Action outcome
     * @return Count of logs with this outcome
     */
    long countByOutcome(ActionOutcome actionOutcome);
}
