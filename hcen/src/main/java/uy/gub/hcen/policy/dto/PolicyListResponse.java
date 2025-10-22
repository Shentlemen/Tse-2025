package uy.gub.hcen.policy.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Policy List Response DTO
 * <p>
 * Pagination wrapper for policy list endpoints.
 * Provides paginated access to patient policies with metadata about
 * total count and pagination state.
 * <p>
 * Response Example:
 * <pre>
 * {
 *   "policies": [
 *     {
 *       "id": 123,
 *       "patientCi": "12345678",
 *       "policyType": "DOCUMENT_TYPE",
 *       "policyEffect": "PERMIT",
 *       ...
 *     },
 *     {
 *       "id": 124,
 *       "patientCi": "12345678",
 *       "policyType": "SPECIALTY",
 *       "policyEffect": "DENY",
 *       ...
 *     }
 *   ],
 *   "totalCount": 15,
 *   "page": 0,
 *   "size": 20,
 *   "totalPages": 1
 * }
 * </pre>
 * <p>
 * Pagination Rules:
 * <ul>
 *   <li>Default page size: 20</li>
 *   <li>Maximum page size: 100</li>
 *   <li>Zero-based page numbering</li>
 *   <li>Empty list returned if no policies found (not 404)</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
public class PolicyListResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * List of policy responses for current page
     */
    private final List<PolicyResponse> policies;

    /**
     * Total number of policies across all pages
     */
    private final long totalCount;

    /**
     * Current page number (zero-based)
     */
    private final int page;

    /**
     * Number of items per page
     */
    private final int size;

    /**
     * Total number of pages
     */
    private final int totalPages;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Full constructor
     *
     * @param policies List of policies for current page
     * @param totalCount Total count across all pages
     * @param page Current page number (zero-based)
     * @param size Items per page
     */
    public PolicyListResponse(List<PolicyResponse> policies, long totalCount, int page, int size) {
        this.policies = policies != null ? new ArrayList<>(policies) : new ArrayList<>();
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
        this.totalPages = calculateTotalPages(totalCount, size);
    }

    // ================================================================
    // Factory Methods
    // ================================================================

    /**
     * Creates an empty policy list response
     *
     * @param page Current page number
     * @param size Page size
     * @return Empty PolicyListResponse
     */
    public static PolicyListResponse empty(int page, int size) {
        return new PolicyListResponse(new ArrayList<>(), 0, page, size);
    }

    /**
     * Creates a policy list response from a list of policies
     *
     * @param policies List of policies
     * @param totalCount Total count of policies
     * @param page Current page number
     * @param size Page size
     * @return PolicyListResponse
     */
    public static PolicyListResponse of(List<PolicyResponse> policies, long totalCount, int page, int size) {
        return new PolicyListResponse(policies, totalCount, page, size);
    }

    // ================================================================
    // Getters Only (Immutable)
    // ================================================================

    public List<PolicyResponse> getPolicies() {
        return new ArrayList<>(policies);
    }

    public long getTotalCount() {
        return totalCount;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    // ================================================================
    // Utility Methods
    // ================================================================

    /**
     * Calculates total number of pages
     *
     * @param totalCount Total item count
     * @param size Page size
     * @return Total pages
     */
    private int calculateTotalPages(long totalCount, int size) {
        if (size <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalCount / size);
    }

    /**
     * Checks if there are more pages after the current one
     *
     * @return true if there is a next page
     */
    public boolean hasNext() {
        return page < totalPages - 1;
    }

    /**
     * Checks if there are pages before the current one
     *
     * @return true if there is a previous page
     */
    public boolean hasPrevious() {
        return page > 0;
    }

    /**
     * Checks if the result list is empty
     *
     * @return true if no policies in current page
     */
    public boolean isEmpty() {
        return policies.isEmpty();
    }

    /**
     * Gets the number of policies in current page
     *
     * @return Number of policies in this page
     */
    public int getNumberOfElements() {
        return policies.size();
    }

    /**
     * Checks if this is the first page
     *
     * @return true if this is page 0
     */
    public boolean isFirst() {
        return page == 0;
    }

    /**
     * Checks if this is the last page
     *
     * @return true if this is the last page
     */
    public boolean isLast() {
        return page == totalPages - 1 || totalPages == 0;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolicyListResponse that = (PolicyListResponse) o;
        return totalCount == that.totalCount &&
                page == that.page &&
                size == that.size &&
                Objects.equals(policies, that.policies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policies, totalCount, page, size);
    }

    @Override
    public String toString() {
        return "PolicyListResponse{" +
                "policiesCount=" + policies.size() +
                ", totalCount=" + totalCount +
                ", page=" + page +
                ", size=" + size +
                ", totalPages=" + totalPages +
                ", hasNext=" + hasNext() +
                ", hasPrevious=" + hasPrevious() +
                '}';
    }
}
