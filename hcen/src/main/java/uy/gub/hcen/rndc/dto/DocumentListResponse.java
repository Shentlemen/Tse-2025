package uy.gub.hcen.rndc.dto;

import uy.gub.hcen.rndc.entity.RndcDocument;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Document List Response DTO
 * <p>
 * Pagination wrapper for lists of clinical documents.
 * This DTO provides metadata about the current page, total items, and the documents themselves.
 * <p>
 * Usage Example:
 * <pre>
 * GET /api/rndc/documents?patientCi=12345678&page=0&size=20
 * Response (200 OK):
 * {
 *   "documents": [
 *     { "id": 1, "patientCi": "12345678", ... },
 *     { "id": 2, "patientCi": "12345678", ... }
 *   ],
 *   "totalCount": 45,
 *   "page": 0,
 *   "size": 20,
 *   "totalPages": 3
 * }
 * </pre>
 * <p>
 * Pagination Calculation:
 * - page: Current page number (0-indexed)
 * - size: Number of items per page
 * - totalCount: Total number of items across all pages
 * - totalPages: Math.ceil(totalCount / size)
 * <p>
 * This DTO is immutable to prevent accidental modifications after creation.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 * @see DocumentResponse
 * @see uy.gub.hcen.rndc.entity.RndcDocument
 */
public class DocumentListResponse {

    private final List<DocumentResponse> documents;
    private final long totalCount;
    private final int page;
    private final int size;
    private final int totalPages;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Full constructor
     *
     * @param documents  List of document responses
     * @param totalCount Total number of documents across all pages
     * @param page       Current page number (0-indexed)
     * @param size       Number of items per page
     */
    public DocumentListResponse(List<DocumentResponse> documents, long totalCount, int page, int size) {
        this.documents = documents;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
        this.totalPages = calculateTotalPages(totalCount, size);
    }

    /**
     * Factory method: Create DocumentListResponse from list of entities
     * <p>
     * This method converts a list of RndcDocument entities to DocumentResponse DTOs
     * and wraps them in a pagination response.
     * <p>
     * Note: This method does NOT calculate totalCount automatically. You must provide
     * the totalCount from a separate count query (e.g., repository.count()).
     *
     * @param entities   List of RndcDocument entities
     * @param totalCount Total number of documents (from count query)
     * @param page       Current page number (0-indexed)
     * @param size       Number of items per page
     * @return DocumentListResponse with pagination metadata
     */
    public static DocumentListResponse fromEntities(List<RndcDocument> entities, long totalCount, int page, int size) {
        List<DocumentResponse> documentResponses = entities.stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());

        return new DocumentListResponse(documentResponses, totalCount, page, size);
    }

    /**
     * Factory method: Create empty response (for no results)
     *
     * @param page Current page number
     * @param size Page size
     * @return Empty DocumentListResponse
     */
    public static DocumentListResponse empty(int page, int size) {
        return new DocumentListResponse(List.of(), 0, page, size);
    }

    // ================================================================
    // Helper Methods
    // ================================================================

    /**
     * Calculate total number of pages
     *
     * @param totalCount Total number of items
     * @param size       Page size
     * @return Total number of pages
     */
    private int calculateTotalPages(long totalCount, int size) {
        if (size <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalCount / size);
    }

    // ================================================================
    // Getters Only (Immutable DTO)
    // ================================================================

    /**
     * Gets the list of documents in this page
     *
     * @return List of document responses
     */
    public List<DocumentResponse> getDocuments() {
        return documents;
    }

    /**
     * Gets the total number of documents across all pages
     *
     * @return Total count of documents
     */
    public long getTotalCount() {
        return totalCount;
    }

    /**
     * Gets the current page number (0-indexed)
     *
     * @return Current page number
     */
    public int getPage() {
        return page;
    }

    /**
     * Gets the page size (number of items per page)
     *
     * @return Page size
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

    /**
     * Checks if this is the first page
     *
     * @return true if current page is 0
     */
    public boolean isFirstPage() {
        return page == 0;
    }

    /**
     * Checks if this is the last page
     *
     * @return true if current page is the last page
     */
    public boolean isLastPage() {
        return page >= totalPages - 1;
    }

    /**
     * Checks if there are more pages after this one
     *
     * @return true if there are more pages
     */
    public boolean hasNextPage() {
        return page < totalPages - 1;
    }

    /**
     * Checks if there are previous pages before this one
     *
     * @return true if there are previous pages
     */
    public boolean hasPreviousPage() {
        return page > 0;
    }

    /**
     * Gets the number of documents in this page
     *
     * @return Number of documents in current page
     */
    public int getDocumentCount() {
        return documents.size();
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "DocumentListResponse{" +
                "documentCount=" + documents.size() +
                ", totalCount=" + totalCount +
                ", page=" + page +
                ", size=" + size +
                ", totalPages=" + totalPages +
                ", isFirstPage=" + isFirstPage() +
                ", isLastPage=" + isLastPage() +
                '}';
    }
}
