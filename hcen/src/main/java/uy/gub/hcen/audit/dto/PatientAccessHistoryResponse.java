package uy.gub.hcen.audit.dto;

import uy.gub.hcen.audit.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Patient Access History Response DTO
 *
 * Specialized response for AC026 compliance - patients viewing who accessed their records.
 * Transforms raw audit logs into patient-friendly access records.
 *
 * <p>Response Format:
 * <pre>
 * {
 *   "patientCi": "12345678",
 *   "accesses": [
 *     {
 *       "accessorId": "prof-123",
 *       "accessorName": "Dr. Smith",
 *       "specialty": "CARDIOLOGY",
 *       "clinicId": "clinic-001",
 *       "documentType": "LAB_RESULT",
 *       "accessTime": "2025-10-21T14:30:00",
 *       "outcome": "SUCCESS"
 *     },
 *     {
 *       "accessorId": "prof-456",
 *       "clinicId": "clinic-002",
 *       "documentType": "IMAGING",
 *       "accessTime": "2025-10-20T10:15:00",
 *       "outcome": "DENIED"
 *     }
 *   ],
 *   "totalAccesses": 45,
 *   "page": 0,
 *   "size": 20,
 *   "totalPages": 3
 * }
 * </pre>
 *
 * Compliance: AC026 - Patients can view who accessed their records and when
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
public class PatientAccessHistoryResponse {

    private final String patientCi;
    private final List<AccessRecord> accesses;
    private final int totalAccesses;
    private final int page;
    private final int size;
    private final int totalPages;

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Constructor with pagination metadata
     *
     * @param patientCi Patient's CI
     * @param accesses List of access records
     * @param totalAccesses Total count across all pages
     * @param page Current page (0-based)
     * @param size Page size
     */
    public PatientAccessHistoryResponse(String patientCi, List<AccessRecord> accesses,
                                       int totalAccesses, int page, int size) {
        this.patientCi = patientCi;
        this.accesses = accesses != null ? accesses : new ArrayList<>();
        this.totalAccesses = totalAccesses;
        this.page = page;
        this.size = size;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalAccesses / size) : 0;
    }

    // =========================================================================
    // Factory Method
    // =========================================================================

    /**
     * Creates PatientAccessHistoryResponse from list of AuditLog entities
     *
     * Transforms audit logs into patient-friendly access records by extracting
     * relevant information from the details JSON field.
     *
     * @param patientCi Patient's CI
     * @param auditLogs List of audit logs (ACCESS events)
     * @param totalCount Total count of access events
     * @param page Current page
     * @param size Page size
     * @return PatientAccessHistoryResponse
     */
    public static PatientAccessHistoryResponse fromAuditLogs(String patientCi,
                                                             List<AuditLog> auditLogs,
                                                             int totalCount,
                                                             int page,
                                                             int size) {
        List<AccessRecord> accessRecords = auditLogs.stream()
                .map(AccessRecord::fromAuditLog)
                .collect(Collectors.toList());

        return new PatientAccessHistoryResponse(
                patientCi,
                accessRecords,
                totalCount,
                page,
                size
        );
    }

    // =========================================================================
    // Getters
    // =========================================================================

    /**
     * Gets the patient's CI
     *
     * @return Patient CI
     */
    public String getPatientCi() {
        return patientCi;
    }

    /**
     * Gets the list of access records
     *
     * @return List of access records (never null)
     */
    public List<AccessRecord> getAccesses() {
        return accesses;
    }

    /**
     * Gets the total number of accesses
     *
     * @return Total accesses across all pages
     */
    public int getTotalAccesses() {
        return totalAccesses;
    }

    /**
     * Gets the current page number
     *
     * @return Page number (0-based)
     */
    public int getPage() {
        return page;
    }

    /**
     * Gets the page size
     *
     * @return Number of records per page
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the total number of pages
     *
     * @return Total pages
     */
    public int getTotalPages() {
        return totalPages;
    }

    // =========================================================================
    // Inner Class: AccessRecord
    // =========================================================================

    /**
     * Access Record - Represents a single document access event
     *
     * Contains patient-friendly information about who accessed what and when.
     */
    public static class AccessRecord {

        private final String accessorId;
        private final String accessorName;
        private final String specialty;
        private final String clinicId;
        private final String documentType;
        private final LocalDateTime accessTime;
        private final String outcome;

        /**
         * Constructor for AccessRecord
         *
         * @param accessorId Professional ID
         * @param accessorName Professional name (optional)
         * @param specialty Professional specialty (optional)
         * @param clinicId Clinic identifier
         * @param documentType Type of document accessed
         * @param accessTime Timestamp of access
         * @param outcome Outcome (SUCCESS, DENIED)
         */
        public AccessRecord(String accessorId, String accessorName, String specialty,
                           String clinicId, String documentType, LocalDateTime accessTime,
                           String outcome) {
            this.accessorId = accessorId;
            this.accessorName = accessorName;
            this.specialty = specialty;
            this.clinicId = clinicId;
            this.documentType = documentType;
            this.accessTime = accessTime;
            this.outcome = outcome;
        }

        /**
         * Creates AccessRecord from AuditLog entity
         *
         * Extracts relevant fields from audit log and details JSON.
         *
         * @param auditLog AuditLog entity
         * @return AccessRecord
         */
        public static AccessRecord fromAuditLog(AuditLog auditLog) {
            // Parse details JSON to extract context
            Map<String, Object> details = AuditLogResponse.fromEntity(auditLog).getDetails();

            String accessorId = auditLog.getActorId();
            String accessorName = extractString(details, "accessorName");
            String specialty = extractString(details, "specialty");
            String clinicId = extractString(details, "clinicId");
            String documentType = extractString(details, "documentType");
            LocalDateTime accessTime = auditLog.getTimestamp();
            String outcome = auditLog.getActionOutcome() != null ?
                    auditLog.getActionOutcome().name() : "UNKNOWN";

            return new AccessRecord(
                    accessorId,
                    accessorName,
                    specialty,
                    clinicId,
                    documentType,
                    accessTime,
                    outcome
            );
        }

        /**
         * Safely extracts string value from details map
         *
         * @param details Details map
         * @param key Key to extract
         * @return String value or null
         */
        private static String extractString(Map<String, Object> details, String key) {
            if (details == null || !details.containsKey(key)) {
                return null;
            }
            Object value = details.get(key);
            return value != null ? value.toString() : null;
        }

        // Getters

        /**
         * Gets the accessor's ID (professional ID)
         *
         * @return Accessor ID
         */
        public String getAccessorId() {
            return accessorId;
        }

        /**
         * Gets the accessor's name (if available)
         *
         * @return Accessor name or null
         */
        public String getAccessorName() {
            return accessorName;
        }

        /**
         * Gets the accessor's specialty (if available)
         *
         * @return Specialty or null
         */
        public String getSpecialty() {
            return specialty;
        }

        /**
         * Gets the clinic ID where access occurred
         *
         * @return Clinic ID or null
         */
        public String getClinicId() {
            return clinicId;
        }

        /**
         * Gets the type of document accessed
         *
         * @return Document type or null
         */
        public String getDocumentType() {
            return documentType;
        }

        /**
         * Gets the timestamp when access occurred
         *
         * @return Access timestamp
         */
        public LocalDateTime getAccessTime() {
            return accessTime;
        }

        /**
         * Gets the outcome of the access attempt
         *
         * @return Outcome (SUCCESS, DENIED, UNKNOWN)
         */
        public String getOutcome() {
            return outcome;
        }

        @Override
        public String toString() {
            return "AccessRecord{" +
                    "accessorId='" + accessorId + '\'' +
                    ", specialty='" + specialty + '\'' +
                    ", clinicId='" + clinicId + '\'' +
                    ", documentType='" + documentType + '\'' +
                    ", accessTime=" + accessTime +
                    ", outcome='" + outcome + '\'' +
                    '}';
        }
    }

    // =========================================================================
    // Object Methods
    // =========================================================================

    @Override
    public String toString() {
        return "PatientAccessHistoryResponse{" +
                "patientCi='" + patientCi + '\'' +
                ", accessesCount=" + accesses.size() +
                ", totalAccesses=" + totalAccesses +
                ", page=" + page +
                ", size=" + size +
                ", totalPages=" + totalPages +
                '}';
    }
}
