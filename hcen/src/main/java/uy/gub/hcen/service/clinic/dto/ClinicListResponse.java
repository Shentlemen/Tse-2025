package uy.gub.hcen.service.clinic.dto;

import java.util.List;

/**
 * Clinic List Response DTO
 * <p>
 * Data Transfer Object for returning paginated lists of clinics.
 * This DTO is returned in response to:
 * - Clinic search/list (GET /api/admin/clinics)
 * - Clinic filtering by status or city
 * <p>
 * Supports pagination metadata to allow clients to navigate large result sets.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
public class ClinicListResponse {

    /**
     * List of clinics in current page
     */
    private List<ClinicResponse> clinics;

    /**
     * Total number of clinics matching the query (all pages)
     */
    private long totalCount;

    /**
     * Current page number (zero-based)
     */
    private int page;

    /**
     * Number of results per page
     */
    private int size;

    /**
     * Total number of pages
     */
    private int totalPages;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor
     */
    public ClinicListResponse() {
    }

    /**
     * Full constructor with pagination metadata
     *
     * @param clinics List of clinics
     * @param totalCount Total count across all pages
     * @param page Current page (zero-based)
     * @param size Page size
     */
    public ClinicListResponse(List<ClinicResponse> clinics, long totalCount, int page, int size) {
        this.clinics = clinics;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) totalCount / size);
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

    public List<ClinicResponse> getClinics() {
        return clinics;
    }

    public void setClinics(List<ClinicResponse> clinics) {
        this.clinics = clinics;
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

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "ClinicListResponse{" +
                "clinics=" + (clinics != null ? clinics.size() : 0) + " items" +
                ", totalCount=" + totalCount +
                ", page=" + page +
                ", size=" + size +
                ", totalPages=" + totalPages +
                '}';
    }
}
