package uy.gub.hcen.inus.dto;

import uy.gub.hcen.inus.entity.InusUser;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User List Response DTO
 * <p>
 * Pagination wrapper for list of users returned by search and list endpoints.
 * This DTO provides pagination metadata along with the actual user data.
 * <p>
 * Standard pagination response format includes:
 * - List of users (converted to UserResponse DTOs)
 * - Total count of all matching records
 * - Current page number (0-based)
 * - Page size
 * - Total number of pages
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 */
public class UserListResponse {

    private final List<UserResponse> users;
    private final long totalCount;
    private final int page;
    private final int size;
    private final int totalPages;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Constructor that converts from list of InusUser entities
     *
     * @param users      List of InusUser entities
     * @param totalCount Total number of matching records in database
     * @param page       Current page number (0-based)
     * @param size       Number of items per page
     */
    public UserListResponse(List<InusUser> users, long totalCount, int page, int size) {
        // Convert entities to DTOs
        this.users = users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());

        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
        this.totalPages = calculateTotalPages(totalCount, size);
    }

    /**
     * Constructor with pre-converted UserResponse DTOs
     *
     * @param users      List of UserResponse DTOs
     * @param totalCount Total number of matching records
     * @param page       Current page number (0-based)
     * @param size       Number of items per page
     */
    public UserListResponse(List<UserResponse> users, long totalCount, int page, int size, boolean isDto) {
        this.users = users;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
        this.totalPages = calculateTotalPages(totalCount, size);
    }

    // ================================================================
    // Helper Methods
    // ================================================================

    /**
     * Calculate total number of pages based on total count and page size
     *
     * @param totalCount Total number of records
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

    public List<UserResponse> getUsers() {
        return users;
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
    // Derived Properties
    // ================================================================

    /**
     * Check if there is a next page
     *
     * @return true if current page is not the last page
     */
    public boolean hasNext() {
        return page < totalPages - 1;
    }

    /**
     * Check if there is a previous page
     *
     * @return true if current page is not the first page
     */
    public boolean hasPrevious() {
        return page > 0;
    }

    /**
     * Check if this is the first page
     *
     * @return true if page number is 0
     */
    public boolean isFirst() {
        return page == 0;
    }

    /**
     * Check if this is the last page
     *
     * @return true if page number equals totalPages - 1
     */
    public boolean isLast() {
        return page == totalPages - 1 || totalPages == 0;
    }

    /**
     * Get the number of users in the current page
     *
     * @return Size of users list
     */
    public int getNumberOfElements() {
        return users.size();
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "UserListResponse{" +
                "numberOfElements=" + users.size() +
                ", totalCount=" + totalCount +
                ", page=" + page +
                ", size=" + size +
                ", totalPages=" + totalPages +
                ", hasNext=" + hasNext() +
                ", hasPrevious=" + hasPrevious() +
                '}';
    }
}
