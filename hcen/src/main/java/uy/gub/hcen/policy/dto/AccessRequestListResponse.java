package uy.gub.hcen.policy.dto;

import java.util.List;

/**
 * Access Request List Response DTO
 *
 * Paginated response containing list of access requests.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-03
 */
public class AccessRequestListResponse {

    private List<AccessRequestDTO> requests;
    private long totalCount;
    private int page;
    private int size;
    private int totalPages;

    // Constructors

    public AccessRequestListResponse() {
    }

    public static AccessRequestListResponse of(List<AccessRequestDTO> requests, long totalCount, int page, int size) {
        AccessRequestListResponse response = new AccessRequestListResponse();
        response.setRequests(requests);
        response.setTotalCount(totalCount);
        response.setPage(page);
        response.setSize(size);
        response.setTotalPages((int) Math.ceil((double) totalCount / size));
        return response;
    }

    // Getters and Setters

    public List<AccessRequestDTO> getRequests() {
        return requests;
    }

    public void setRequests(List<AccessRequestDTO> requests) {
        this.requests = requests;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
