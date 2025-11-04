package uy.gub.hcen.clinicalhistory.dto;

import java.util.List;

/**
 * Paginated Document List Response
 *
 * Response DTO for paginated clinical history document lists.
 * Contains the list of documents and pagination metadata.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-04
 */
public class PaginatedDocumentListResponse {

    /**
     * List of documents for the current page
     */
    private List<DocumentListItemDTO> documents;

    /**
     * Current page number (0-indexed)
     */
    private int currentPage;

    /**
     * Total number of pages
     */
    private int totalPages;

    /**
     * Total number of documents across all pages
     */
    private long totalDocuments;

    /**
     * Number of documents per page
     */
    private int pageSize;

    /**
     * Whether there is a next page
     */
    private boolean hasNext;

    /**
     * Whether there is a previous page
     */
    private boolean hasPrevious;

    /**
     * Default constructor
     */
    public PaginatedDocumentListResponse() {
    }

    /**
     * Constructor with all fields
     */
    public PaginatedDocumentListResponse(List<DocumentListItemDTO> documents,
                                         int currentPage,
                                         int totalPages,
                                         long totalDocuments,
                                         int pageSize) {
        this.documents = documents;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalDocuments = totalDocuments;
        this.pageSize = pageSize;
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
    }

    /**
     * Factory method for creating paginated response
     *
     * @param documents List of documents
     * @param totalCount Total number of documents
     * @param page Current page number
     * @param size Page size
     * @return PaginatedDocumentListResponse
     */
    public static PaginatedDocumentListResponse of(List<DocumentListItemDTO> documents,
                                                    long totalCount,
                                                    int page,
                                                    int size) {
        int totalPages = (int) Math.ceil((double) totalCount / size);
        return new PaginatedDocumentListResponse(documents, page, totalPages, totalCount, size);
    }

    // Getters and Setters

    public List<DocumentListItemDTO> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentListItemDTO> documents) {
        this.documents = documents;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(long totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    @Override
    public String toString() {
        return "PaginatedDocumentListResponse{" +
                "documentCount=" + (documents != null ? documents.size() : 0) +
                ", currentPage=" + currentPage +
                ", totalPages=" + totalPages +
                ", totalDocuments=" + totalDocuments +
                ", pageSize=" + pageSize +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                '}';
    }
}
