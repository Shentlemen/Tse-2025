package uy.gub.hcen.inus.repository;

import uy.gub.hcen.inus.entity.InusUser;
import uy.gub.hcen.inus.entity.UserStatus;

import java.util.List;
import java.util.Optional;

/**
 * INUS Repository Interface
 *
 * Data access layer for INUS (Índice Nacional de Usuarios de Salud) users.
 * Provides methods for user registration, lookup, and management.
 *
 * Key Operations:
 * - User registration (from peripheral nodes)
 * - User lookup by CI
 * - User lookup by INUS ID
 * - User search for admin functionality
 * - Existence checks
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
public interface InusRepository {

    /**
     * Find user by CI (Cédula de Identidad)
     *
     * @param ci National ID number
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<InusUser> findByCi(String ci);

    /**
     * Find user by INUS ID (cross-clinic unique identifier)
     *
     * @param inusId Unique cross-clinic identifier
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<InusUser> findByInusId(String inusId);

    /**
     * Check if a user with the given CI exists
     *
     * @param ci National ID number
     * @return true if user exists, false otherwise
     */
    boolean existsByCi(String ci);

    /**
     * Check if a user with the given INUS ID exists
     *
     * @param inusId Unique cross-clinic identifier
     * @return true if user exists, false otherwise
     */
    boolean existsByInusId(String inusId);

    /**
     * Save a new user to the database
     *
     * @param user User entity to save
     * @return Persisted user entity
     * @throws IllegalArgumentException if user with same CI already exists
     */
    InusUser save(InusUser user);

    /**
     * Update an existing user in the database
     *
     * @param user User entity to update
     * @return Updated user entity
     * @throws IllegalArgumentException if user does not exist
     */
    InusUser update(InusUser user);

    /**
     * Find all users with pagination
     *
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @return List of users for the requested page
     */
    List<InusUser> findAll(int page, int size);

    /**
     * Search users by query string (searches in firstName, lastName, CI, inusId)
     * Used for admin search functionality
     *
     * @param query Search query string
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @return List of users matching the query
     */
    List<InusUser> searchUsers(String query, int page, int size);

    /**
     * Find users by status
     *
     * @param status User status to filter by
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @return List of users with the specified status
     */
    List<InusUser> findByStatus(UserStatus status, int page, int size);

    /**
     * Find users by email
     *
     * @param email Email address
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<InusUser> findByEmail(String email);

    /**
     * Count total number of users in the system
     *
     * @return Total user count
     */
    long count();

    /**
     * Count users by status
     *
     * @param status User status to count
     * @return Number of users with the specified status
     */
    long countByStatus(UserStatus status);

    /**
     * Delete a user by CI (soft delete - sets status to INACTIVE)
     * Note: Hard deletes are not allowed for audit trail purposes
     *
     * @param ci National ID number
     * @return true if user was deactivated, false if not found
     */
    boolean deactivateByCi(String ci);

    /**
     * Find users created within a date range
     *
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @return List of users created within the date range
     */
    List<InusUser> findByCreatedAtBetween(java.time.LocalDateTime startDate,
                                          java.time.LocalDateTime endDate,
                                          int page, int size);
}
