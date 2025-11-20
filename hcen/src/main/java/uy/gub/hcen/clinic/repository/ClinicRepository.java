package uy.gub.hcen.clinic.repository;

import uy.gub.hcen.clinic.entity.Clinic;
import uy.gub.hcen.clinic.entity.Clinic.ClinicStatus;

import java.util.List;
import java.util.Optional;

/**
 * Clinic Repository Interface
 *
 * Defines data access operations for the Clinic entity.
 * Implementations should provide efficient querying and pagination support.
 *
 * Operations:
 * - save: Create new clinic
 * - findById: Retrieve clinic by ID
 * - findAll: List all clinics (paginated)
 * - findByStatus: Filter clinics by status (paginated)
 * - findByCity: Filter clinics by city (paginated)
 * - update: Update existing clinic
 * - updateStatus: Change clinic status
 * - existsById: Check if clinic exists
 *
 * Pagination:
 * - page: Zero-based page number
 * - size: Number of results per page
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
public interface ClinicRepository {

    /**
     * Saves a new clinic to the database
     *
     * @param clinic Clinic entity to save
     * @return Saved clinic with generated ID
     * @throws IllegalArgumentException if clinic is null or clinicId is null
     * @throws RuntimeException if save operation fails
     */
    Clinic save(Clinic clinic);

    /**
     * Finds a clinic by its unique identifier
     *
     * @param clinicId Unique clinic ID
     * @return Optional containing the clinic if found, empty otherwise
     * @throws IllegalArgumentException if clinicId is null or empty
     */
    Optional<Clinic> findById(String clinicId);

    /**
     * Retrieves all clinics with pagination
     *
     * @param page Zero-based page number
     * @param size Number of results per page
     * @return List of clinics (empty if no results)
     * @throws IllegalArgumentException if page < 0 or size <= 0
     */
    List<Clinic> findAll(int page, int size);

    /**
     * Finds clinics by status with pagination
     *
     * @param status Clinic status to filter by
     * @param page Zero-based page number
     * @param size Number of results per page
     * @return List of clinics matching the status (empty if no results)
     * @throws IllegalArgumentException if status is null, page < 0, or size <= 0
     */
    List<Clinic> findByStatus(ClinicStatus status, int page, int size);

    /**
     * Finds clinics by city with pagination
     *
     * @param city City name to filter by
     * @param page Zero-based page number
     * @param size Number of results per page
     * @return List of clinics in the specified city (empty if no results)
     * @throws IllegalArgumentException if city is null/empty, page < 0, or size <= 0
     */
    List<Clinic> findByCity(String city, int page, int size);

    /**
     * Updates an existing clinic
     *
     * @param clinic Clinic entity with updated values
     * @return Updated clinic
     * @throws IllegalArgumentException if clinic is null or clinicId is null
     * @throws RuntimeException if clinic not found or update fails
     */
    Clinic update(Clinic clinic);

    /**
     * Updates only the status of a clinic
     * More efficient than full update for status changes
     *
     * @param clinicId Unique clinic ID
     * @param status New status
     * @return true if update successful, false if clinic not found
     * @throws IllegalArgumentException if clinicId or status is null
     * @throws RuntimeException if update operation fails
     */
    boolean updateStatus(String clinicId, ClinicStatus status);

    /**
     * Checks if a clinic exists by ID
     *
     * @param clinicId Unique clinic ID
     * @return true if clinic exists, false otherwise
     * @throws IllegalArgumentException if clinicId is null or empty
     */
    boolean existsById(String clinicId);

    /**
     * Counts total number of clinics
     *
     * @return Total clinic count
     */
    long count();

    /**
     * Counts clinics by status
     *
     * @param status Status to count
     * @return Number of clinics with the specified status
     * @throws IllegalArgumentException if status is null
     */
    long countByStatus(ClinicStatus status);

    /**
     * Finds a clinic by its ID and validates the API key
     * Used for API key authentication from peripheral nodes
     *
     * @param clinicId Unique clinic ID
     * @param apiKey API key to validate
     * @return Optional containing the clinic if found and API key matches, empty otherwise
     * @throws IllegalArgumentException if clinicId or apiKey is null or empty
     */
    Optional<Clinic> findByIdAndApiKey(String clinicId, String apiKey);
}
