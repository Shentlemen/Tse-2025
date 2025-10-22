package uy.gub.hcen.audit.dto;

import java.util.List;

/**
 * Audit Log List Response DTO
 *
 * Paginated response wrapper for audit log queries.
 * Provides pagination metadata and helper methods for navigation.
 *
 * <p>Response Format:
 * <pre>
 * {
 *   "logs": [
 *     { "id": 1001, "eventType": "ACCESS", ... },
 *     { "id": 1002, "eventType": "MODIFICATION", ... }
 *   ],
 *   "totalCount": 1500,
 *   "page": 0,
 *   "size": 20,
 *   "totalPages": 75
 * }
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
public class AuditLogListResponse {

    private final List<AuditLogResponse> logs;
    private final long totalCount;
    private final int page;
    private final int size;
    private final int totalPages;

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Constructor with pagination metadata
     *
     * @param logs List of audit log responses
     * @param totalCount Total count of all matching records
     * @param page Current page number (0-based)
     * @param size Page size
     */
    public AuditLogListResponse(List<AuditLogResponse> logs, long totalCount, int page, int size) {
        this.logs = logs != null ? logs : List.of();
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalCount / size) : 0;
    }

    // =========================================================================
    // Pagination Helper Methods
    // =========================================================================

    /**
     * Checks if there is a next page
     *
     * @return true if there are more pages after the current one
     */
    public boolean hasNext() {
        return page < totalPages - 1;
    }

    /**
     * Checks if there is a previous page
     *
     * @return true if there are pages before the current one
     */
    public boolean hasPrevious() {
        return page > 0;
    }

    /**
     * Gets the next page number (if exists)
     *
     * @return Next page number or current page if no next exists
     */
    public int getNextPage() {
        return hasNext() ? page + 1 : page;
    }

    /**
     * Gets the previous page number (if exists)
     *
     * @return Previous page number or current page if no previous exists
     */
    public int getPreviousPage() {
        return hasPrevious() ? page - 1 : page;
    }

    /**
     * Checks if this is the first page
     *
     * @return true if page is 0
     */
    public boolean isFirst() {
        return page == 0;
    }

    /**
     * Checks if this is the last page
     *
     * @return true if page is the last page
     */
    public boolean isLast() {
        return page >= totalPages - 1;
    }

    /**
     * Checks if the result set is empty
     *
     * @return true if no logs in current page
     */
    public boolean isEmpty() {
        return logs.isEmpty();
    }

    /**
     * Gets the number of logs in current page
     *
     * @return Number of logs
     */
    public int getNumberOfElements() {
        return logs.size();
    }

    // =========================================================================
    // Getters
    // =========================================================================

    /**
     * Gets the list of audit logs
     *
     * @return List of audit log responses (never null)
     */
    public List<AuditLogResponse> getLogs() {
        return logs;
    }

    /**
     * Gets the total count of all matching records
     *
     * @return Total count across all pages
     */
    public long getTotalCount() {
        return totalCount;
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
    // Object Methods
    // =========================================================================

    @Override
    public String toString() {
        return "AuditLogListResponse{" +
                "logsCount=" + logs.size() +
                ", totalCount=" + totalCount +
                ", page=" + page +
                ", size=" + size +
                ", totalPages=" + totalPages +
                '}';
    }
}
