package uy.gub.hcen.rndc.repository;

import uy.gub.hcen.rndc.entity.RndcDocument;
import uy.gub.hcen.rndc.entity.DocumentType;
import uy.gub.hcen.rndc.entity.DocumentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RNDC Repository Interface
 *
 * Data access interface for RNDC (National Clinical Document Registry) operations.
 * Provides methods for CRUD operations and specialized queries for document metadata.
 *
 * <p>This repository follows the Repository pattern, abstracting database access
 * and allowing for easy testing with mock implementations.
 *
 * <p>Key Responsibilities:
 * <ul>
 *   <li>Document metadata persistence (create, update)</li>
 *   <li>Document retrieval by various criteria</li>
 *   <li>Document search with pagination</li>
 *   <li>Document status management</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 * @see RndcDocument
 */
public interface RndcRepository {

    /**
     * Saves a new document metadata entry to the RNDC
     *
     * @param document The document metadata to save
     * @return The saved document with generated ID
     * @throws IllegalArgumentException if document is null or invalid
     */
    RndcDocument save(RndcDocument document);

    /**
     * Finds a document by its unique ID
     *
     * @param id The document ID
     * @return Optional containing the document if found, empty otherwise
     */
    Optional<RndcDocument> findById(Long id);

    /**
     * Finds all documents for a specific patient
     * Returns only ACTIVE documents by default
     *
     * @param patientCi Patient's Cédula de Identidad
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return List of documents for the patient
     */
    List<RndcDocument> findByPatientCi(String patientCi, int page, int size);

    /**
     * Finds all documents for a specific patient with a specific status
     *
     * @param patientCi Patient's Cédula de Identidad
     * @param status Document status filter
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return List of documents matching criteria
     */
    List<RndcDocument> findByPatientCiAndStatus(String patientCi, DocumentStatus status, int page, int size);

    /**
     * Finds documents for a patient by document type
     * Returns only ACTIVE documents
     *
     * @param patientCi Patient's Cédula de Identidad
     * @param type Document type filter
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return List of documents matching criteria
     */
    List<RndcDocument> findByPatientCiAndType(String patientCi, DocumentType type, int page, int size);

    /**
     * Finds documents for a patient by type and status
     *
     * @param patientCi Patient's Cédula de Identidad
     * @param type Document type filter
     * @param status Document status filter
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return List of documents matching criteria
     */
    List<RndcDocument> findByPatientCiAndTypeAndStatus(
            String patientCi, DocumentType type, DocumentStatus status, int page, int size);

    /**
     * Finds documents for a patient within a date range
     * Returns only ACTIVE documents
     *
     * @param patientCi Patient's Cédula de Identidad
     * @param fromDate Start date (inclusive)
     * @param toDate End date (inclusive)
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return List of documents in the date range
     */
    List<RndcDocument> findByPatientCiAndDateRange(
            String patientCi, LocalDateTime fromDate, LocalDateTime toDate, int page, int size);

    /**
     * Finds all documents registered by a specific clinic
     *
     * @param clinicId Clinic identifier
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return List of documents from the clinic
     */
    List<RndcDocument> findByClinicId(String clinicId, int page, int size);

    /**
     * Finds all documents registered by a specific clinic with a specific status
     *
     * @param clinicId Clinic identifier
     * @param status Document status filter
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return List of documents matching criteria
     */
    List<RndcDocument> findByClinicIdAndStatus(String clinicId, DocumentStatus status, int page, int size);

    /**
     * Counts total documents for a patient
     *
     * @param patientCi Patient's Cédula de Identidad
     * @return Total count of ACTIVE documents
     */
    long countByPatientCi(String patientCi);

    /**
     * Counts documents for a patient by status
     *
     * @param patientCi Patient's Cédula de Identidad
     * @param status Document status
     * @return Total count of documents with specified status
     */
    long countByPatientCiAndStatus(String patientCi, DocumentStatus status);

    /**
     * Updates the status of a document
     *
     * @param id Document ID
     * @param status New status
     * @return true if update succeeded, false otherwise
     */
    boolean updateStatus(Long id, DocumentStatus status);

    /**
     * Checks if a document locator already exists in the registry
     * Used for idempotency check during registration
     *
     * @param locator Document locator URL
     * @return true if locator exists, false otherwise
     */
    boolean existsByLocator(String locator);

    /**
     * Finds a document by its locator URL
     *
     * @param locator Document locator URL
     * @return Optional containing the document if found, empty otherwise
     */
    Optional<RndcDocument> findByLocator(String locator);

    /**
     * Soft deletes a document (sets status to DELETED)
     *
     * @param id Document ID
     * @return true if deletion succeeded, false otherwise
     */
    boolean softDelete(Long id);

    /**
     * Reactivates an inactive document (sets status to ACTIVE)
     *
     * @param id Document ID
     * @return true if reactivation succeeded, false otherwise
     */
    boolean reactivate(Long id);

    /**
     * Finds all documents created by a specific professional
     *
     * @param createdBy Professional identifier (email or ID)
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return List of documents created by the professional
     */
    List<RndcDocument> findByCreatedBy(String createdBy, int page, int size);

    /**
     * Searches documents across multiple criteria
     * This is a flexible search method for admin/reporting purposes
     *
     * @param patientCi Optional patient CI filter
     * @param documentType Optional document type filter
     * @param status Optional status filter
     * @param clinicId Optional clinic ID filter
     * @param fromDate Optional start date filter
     * @param toDate Optional end date filter
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return List of documents matching all specified criteria
     */
    List<RndcDocument> search(
            String patientCi,
            DocumentType documentType,
            DocumentStatus status,
            String clinicId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size
    );

    /**
     * Counts total documents in the RNDC
     *
     * @return Total document count (all statuses)
     */
    long countAll();

    /**
     * Counts documents by status
     *
     * @param status Document status
     * @return Count of documents with specified status
     */
    long countByStatus(DocumentStatus status);
}
