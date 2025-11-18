package uy.gub.hcen.service.rndc;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import uy.gub.hcen.inus.entity.InusUser;
import uy.gub.hcen.rndc.entity.DocumentStatus;
import uy.gub.hcen.rndc.entity.DocumentType;
import uy.gub.hcen.rndc.entity.RndcDocument;
import uy.gub.hcen.rndc.repository.RndcRepository;
import uy.gub.hcen.service.inus.InusService;
import uy.gub.hcen.service.rndc.exception.DocumentNotFoundException;
import uy.gub.hcen.service.rndc.exception.DocumentRegistrationException;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

/**
 * RNDC Service - National Clinical Document Registry Service
 * <p>
 * Core business logic for managing the RNDC (Registro Nacional de Documentos Clínicos),
 * the national registry of clinical document metadata.
 * <p>
 * Key Responsibilities:
 * - Document registration from peripheral nodes (AC014)
 * - Document search and retrieval
 * - Document status management (active/inactive/deleted)
 * - Document integrity verification (SHA-256 hashing)
 * - Statistics and reporting
 * <p>
 * Important Concepts:
 * - RNDC stores METADATA only, not actual documents
 * - Actual documents remain in peripheral node storage
 * - documentLocator: URL pointing to document in peripheral storage
 * - documentHash: SHA-256 hash for integrity verification
 * - Idempotent registration: duplicate locators return existing document
 * <p>
 * Integration Points:
 * - RndcRepository: Database persistence layer
 * - InusService: Patient CI validation (optional)
 * - PolicyEngine: Access control (to be integrated)
 * - AuditService: Access logging (to be integrated)
 * - PeripheralNodeClient: Document retrieval (to be integrated)
 * <p>
 * Business Rules:
 * - All documents must have valid patient CI, documentType, locator, hash
 * - Document locator must be unique (enforced by database constraint)
 * - Document hash format: "sha256:[64 hex characters]"
 * - Status transitions: any status -> any status (for now)
 * - Soft delete: documents marked DELETED, not physically removed
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 */
@Stateless
public class RndcService {

    private static final Logger LOGGER = Logger.getLogger(RndcService.class.getName());

    /**
     * SHA-256 hash pattern for validation
     * Format: sha256:[64 lowercase hex characters]
     */
    private static final Pattern HASH_PATTERN = Pattern.compile("^sha256:[a-f0-9]{64}$");

    @Inject
    private RndcRepository rndcRepository;

    @Inject
    private InusService inusService;

    // ================================================================
    // Document Registration (AC014)
    // ================================================================

    /**
     * Convert byte array to lowercase hexadecimal string.
     *
     * @param bytes Byte array to convert
     * @return Hexadecimal string (lowercase)
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // ================================================================
    // Document Search and Retrieval
    // ================================================================

    /**
     * Register a new clinical document in the RNDC.
     * <p>
     * This method is called when peripheral nodes (clinics, health providers) register
     * new clinical documents. It performs the following steps:
     * 1. Validates all required parameters
     * 2. Validates documentLocator URL format
     * 3. Validates documentHash format (sha256:...)
     * 4. Optionally validates patientCi exists in INUS
     * 5. Checks for duplicate registration (idempotent behavior)
     * 6. Creates and persists document metadata
     * 7. Logs registration event
     * <p>
     * Idempotent Behavior:
     * If a document with the same locator already exists, returns the existing document
     * without creating a duplicate. This prevents accidental duplicate registrations.
     * <p>
     * Future Enhancements:
     * - Integration with PolicyEngine for access control
     * - Integration with AuditService for registration logging
     * - Integration with PDI for professional credential validation
     *
     * @param patientCi           Patient's Cédula de Identidad (national ID)
     * @param documentType        Type of clinical document
     * @param documentLocator     URL to retrieve document from peripheral storage
     * @param documentHash        SHA-256 hash of document (format: sha256:hex)
     * @param createdBy           Professional who created the document (email or ID)
     * @param clinicId            Clinic/peripheral node identifier
     * @param documentTitle       Optional document title for quick identification
     * @param documentDescription Optional document description or summary
     * @return Registered RndcDocument (either newly created or existing)
     * @throws DocumentRegistrationException if validation fails or persistence fails
     */
    public RndcDocument registerDocument(String patientCi, DocumentType documentType,
                                         String documentLocator, String documentHash,
                                         String createdBy, String clinicId,
                                         String documentTitle, String documentDescription)
            throws DocumentRegistrationException {

        if(!patientCi.startsWith("uy-ci-")){
            patientCi = "uy-ci-" + patientCi;
        }

        // Input validation
        validateRegistrationInputs(patientCi, documentType, documentLocator, documentHash, createdBy, clinicId);

        LOGGER.log(Level.INFO, "Processing document registration - Patient CI: {0}, Type: {1}, Clinic: {2}",
                new Object[]{patientCi, documentType, clinicId});

        try {
            // Check for duplicate registration (idempotent behavior)
            Optional<RndcDocument> existingDocument = rndcRepository.findByLocator(documentLocator);
            if (existingDocument.isPresent()) {
                LOGGER.log(Level.INFO, "Document already registered with locator: {0}, returning existing document (ID: {1})",
                        new Object[]{documentLocator, existingDocument.get().getId()});
                return existingDocument.get();
            }

            // Optionally validate patient exists in INUS
            validatePatientExists(patientCi);

            // Create new document metadata entity
            RndcDocument document = new RndcDocument(
                    patientCi,
                    documentLocator,
                    documentHash,
                    documentType,
                    createdBy,
                    clinicId
            );

            // Set optional fields
            if (documentTitle != null && !documentTitle.trim().isEmpty()) {
                document.setDocumentTitle(documentTitle);
            }
            if (documentDescription != null && !documentDescription.trim().isEmpty()) {
                document.setDocumentDescription(documentDescription);
            }

            // Persist to database
            RndcDocument savedDocument = rndcRepository.save(document);

            LOGGER.log(Level.INFO,
                    "Successfully registered document - ID: {0}, Patient CI: {1}, Type: {2}, Locator: {3}",
                    new Object[]{savedDocument.getId(), patientCi, documentType, documentLocator});

            return savedDocument;

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Document registration failed due to validation error: " + e.getMessage());
            throw new DocumentRegistrationException("Document registration failed: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Document registration failed for patient CI: " + patientCi, e);
            throw new DocumentRegistrationException(
                    "System error during document registration for patient CI: " + patientCi, e);
        }
    }

    /**
     * Search documents across multiple criteria.
     * <p>
     * This is a flexible search method for clinical, administrative, and reporting purposes.
     * All parameters are optional (null means "no filter").
     * <p>
     * Search Criteria:
     * - patientCi: Filter by patient (null = all patients)
     * - documentType: Filter by document type (null = all types)
     * - status: Filter by status (null = all statuses)
     * - clinicId: Filter by clinic (null = all clinics)
     * - fromDate: Start of date range (null = no start limit)
     * - toDate: End of date range (null = no end limit)
     * <p>
     * Policy Enforcement:
     * For now, this method does NOT check access policies. We'll integrate
     * PolicyEngine later to enforce patient-defined access rules.
     * <p>
     * Pagination:
     * Use page and size parameters for pagination (0-indexed pages).
     *
     * @param patientCi    Optional patient CI filter
     * @param documentType Optional document type filter
     * @param status       Optional status filter
     * @param clinicId     Optional clinic ID filter
     * @param fromDate     Optional start date filter
     * @param toDate       Optional end date filter
     * @param page         Page number (0-indexed)
     * @param size         Page size (items per page)
     * @return List of documents matching all specified criteria
     */
    public List<RndcDocument> searchDocuments(String patientCi, DocumentType documentType,
                                              DocumentStatus status, String clinicId,
                                              LocalDateTime fromDate, LocalDateTime toDate,
                                              int page, int size) {

        // Validate pagination parameters
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters: page=" + page + ", size=" + size);
        }

        try {
            LOGGER.log(Level.FINE, "Searching documents - Patient CI: {0}, Type: {1}, Status: {2}, Clinic: {3}",
                    new Object[]{patientCi, documentType, status, clinicId});

            // Delegate to repository search method
            List<RndcDocument> documents = rndcRepository.search(
                    patientCi,
                    documentType,
                    status,
                    clinicId,
                    fromDate,
                    toDate,
                    page,
                    size
            );

            LOGGER.log(Level.FINE, "Search returned {0} documents", documents.size());

            return documents;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during document search", e);
            return List.of();
        }
    }

    /**
     * Get all documents for a specific patient.
     * <p>
     * Returns only ACTIVE documents by default.
     * Use searchDocuments() for more advanced filtering.
     *
     * @param patientCi Patient's Cédula de Identidad
     * @param page      Page number (0-indexed)
     * @param size      Page size
     * @return List of patient's documents
     */
    public List<RndcDocument> getPatientDocuments(String patientCi, int page, int size) {
        if (patientCi == null || patientCi.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient CI cannot be null or empty");
        }

        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters: page=" + page + ", size=" + size);
        }

        try {
            return rndcRepository.findByPatientCi(patientCi, page, size);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving documents for patient CI: " + patientCi, e);
            return List.of();
        }
    }

    /**
     * Get patient's documents by document type.
     * <p>
     * Returns only ACTIVE documents.
     *
     * @param patientCi    Patient's Cédula de Identidad
     * @param documentType Document type filter
     * @param page         Page number (0-indexed)
     * @param size         Page size
     * @return List of patient's documents of specified type
     */
    public List<RndcDocument> getPatientDocumentsByType(String patientCi, DocumentType documentType,
                                                        int page, int size) {
        if (patientCi == null || patientCi.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient CI cannot be null or empty");
        }

        if (documentType == null) {
            throw new IllegalArgumentException("Document type cannot be null");
        }

        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters: page=" + page + ", size=" + size);
        }

        try {
            return rndcRepository.findByPatientCiAndType(patientCi, documentType, page, size);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving documents for patient CI: " + patientCi + ", type: " + documentType, e);
            return List.of();
        }
    }

    /**
     * Get document metadata by ID.
     * <p>
     * Returns the document metadata (not the actual document content).
     * To retrieve the actual document, use the documentLocator URL.
     *
     * @param documentId Document ID
     * @return Optional containing document if found, empty otherwise
     */
    public Optional<RndcDocument> getDocumentMetadata(Long documentId) {
        if (documentId == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }

        try {
            return rndcRepository.findById(documentId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving document metadata for ID: " + documentId, e);
            return Optional.empty();
        }
    }

    // ================================================================
    // Document Status Management
    // ================================================================

    /**
     * Get document by its locator URL.
     * <p>
     * Useful for verifying if a document is already registered.
     *
     * @param documentLocator Document locator URL
     * @return Optional containing document if found, empty otherwise
     */
    public Optional<RndcDocument> getDocumentByLocator(String documentLocator) {
        if (documentLocator == null || documentLocator.trim().isEmpty()) {
            throw new IllegalArgumentException("Document locator cannot be null or empty");
        }

        try {
            return rndcRepository.findByLocator(documentLocator);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving document by locator: " + documentLocator, e);
            return Optional.empty();
        }
    }

    /**
     * Mark a document as INACTIVE.
     * <p>
     * Inactive documents are hidden from default searches but can be reactivated.
     * Use cases: temporary removal, under review, pending correction.
     * <p>
     * Authorization Note: For now, anyone can change status. We'll add
     * "only creator or admin" authorization logic later.
     *
     * @param documentId Document ID
     * @param updatedBy  Professional performing the status change
     * @throws DocumentNotFoundException if document does not exist
     */
    public void markDocumentAsInactive(Long documentId, String updatedBy)
            throws DocumentNotFoundException {

        if (documentId == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }

        if (updatedBy == null || updatedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Updated by cannot be null or empty");
        }

        LOGGER.log(Level.INFO, "Marking document as INACTIVE - ID: {0}, Updated by: {1}",
                new Object[]{documentId, updatedBy});

        try {
            // Find document
            RndcDocument document = rndcRepository.findById(documentId)
                    .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + documentId));

            DocumentStatus oldStatus = document.getStatus();

            // Update status
            document.markAsInactive();

            // Persist changes
            rndcRepository.updateStatus(documentId, DocumentStatus.INACTIVE);

            LOGGER.log(Level.INFO, "Successfully marked document as INACTIVE - ID: {0}, Status: {1} -> {2}",
                    new Object[]{documentId, oldStatus, DocumentStatus.INACTIVE});

        } catch (DocumentNotFoundException e) {
            LOGGER.log(Level.WARNING, "Status update failed - document not found: " + documentId);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error marking document as inactive: " + documentId, e);
            throw new RuntimeException("System error during document status update", e);
        }
    }

    /**
     * Mark a document as DELETED (soft delete).
     * <p>
     * Deleted documents are permanently hidden and cannot be reactivated.
     * Use cases: document created in error, patient request for removal, legal requirement.
     * <p>
     * Note: This is a soft delete - the metadata remains in the database for audit purposes.
     * <p>
     * Authorization Note: For now, anyone can delete. We'll add
     * "only creator or admin" authorization logic later.
     *
     * @param documentId Document ID
     * @param updatedBy  Professional performing the deletion
     * @throws DocumentNotFoundException if document does not exist
     */
    public void markDocumentAsDeleted(Long documentId, String updatedBy)
            throws DocumentNotFoundException {

        if (documentId == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }

        if (updatedBy == null || updatedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Updated by cannot be null or empty");
        }

        LOGGER.log(Level.INFO, "Marking document as DELETED - ID: {0}, Updated by: {1}",
                new Object[]{documentId, updatedBy});

        try {
            // Find document
            RndcDocument document = rndcRepository.findById(documentId)
                    .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + documentId));

            DocumentStatus oldStatus = document.getStatus();

            // Update status
            document.markAsDeleted();

            // Persist changes
            rndcRepository.updateStatus(documentId, DocumentStatus.DELETED);

            LOGGER.log(Level.INFO, "Successfully marked document as DELETED - ID: {0}, Status: {1} -> {2}",
                    new Object[]{documentId, oldStatus, DocumentStatus.DELETED});

        } catch (DocumentNotFoundException e) {
            LOGGER.log(Level.WARNING, "Status update failed - document not found: " + documentId);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error marking document as deleted: " + documentId, e);
            throw new RuntimeException("System error during document deletion", e);
        }
    }

    // ================================================================
    // Document Integrity Verification
    // ================================================================

    /**
     * Reactivate an inactive document (set status to ACTIVE).
     * <p>
     * Use cases: restore document after review, correct accidental deactivation.
     * <p>
     * Note: Cannot reactivate DELETED documents (status transition not allowed).
     * For now, we allow reactivation from any status. We can add state machine
     * validation later if needed.
     * <p>
     * Authorization Note: For now, anyone can reactivate. We'll add
     * "only creator or admin" authorization logic later.
     *
     * @param documentId Document ID
     * @param updatedBy  Professional performing the reactivation
     * @throws DocumentNotFoundException if document does not exist
     */
    public void reactivateDocument(Long documentId, String updatedBy)
            throws DocumentNotFoundException {

        if (documentId == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }

        if (updatedBy == null || updatedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Updated by cannot be null or empty");
        }

        LOGGER.log(Level.INFO, "Reactivating document - ID: {0}, Updated by: {1}",
                new Object[]{documentId, updatedBy});

        try {
            // Find document
            RndcDocument document = rndcRepository.findById(documentId)
                    .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + documentId));

            DocumentStatus oldStatus = document.getStatus();

            // Update status
            document.setStatus(DocumentStatus.ACTIVE);

            // Persist changes
            rndcRepository.updateStatus(documentId, DocumentStatus.ACTIVE);

            LOGGER.log(Level.INFO, "Successfully reactivated document - ID: {0}, Status: {1} -> {2}",
                    new Object[]{documentId, oldStatus, DocumentStatus.ACTIVE});

        } catch (DocumentNotFoundException e) {
            LOGGER.log(Level.WARNING, "Reactivation failed - document not found: " + documentId);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error reactivating document: " + documentId, e);
            throw new RuntimeException("System error during document reactivation", e);
        }
    }

    /**
     * Verify document hash for integrity checking.
     * <p>
     * This is a stub implementation that validates hash format only.
     * <p>
     * Future Enhancement:
     * 1. Fetch actual document from peripheral node via HTTP GET (using documentLocator)
     * 2. Calculate SHA-256 hash of document content
     * 3. Compare with expectedHash
     * 4. Return true if hashes match, false otherwise
     * <p>
     * This will require PeripheralNodeClient integration.
     *
     * @param documentLocator URL to retrieve document from peripheral storage
     * @param expectedHash    Expected SHA-256 hash (format: sha256:hex)
     * @return true if hash format is valid (stub implementation)
     */
    public boolean verifyDocumentHash(String documentLocator, String expectedHash) {
        if (documentLocator == null || documentLocator.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Document locator is null or empty");
            return false;
        }

        if (expectedHash == null || expectedHash.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Expected hash is null or empty");
            return false;
        }

        // For now, just validate hash format
        boolean validFormat = isValidDocumentHash(expectedHash);

        if (!validFormat) {
            LOGGER.log(Level.WARNING, "Invalid hash format: {0}", expectedHash);
        }

        // TODO: Implement actual document retrieval and hash calculation
        // 1. Fetch document from documentLocator (HTTP GET)
        // 2. Calculate SHA-256 hash
        // 3. Compare with expectedHash
        // For now, return true if format is valid (stub)

        return validFormat;
    }

    // ================================================================
    // Statistics and Reporting
    // ================================================================

    /**
     * Calculate SHA-256 hash of document content.
     * <p>
     * This method is used by peripheral nodes to calculate document hashes
     * before registering documents in RNDC.
     * <p>
     * Hash Format: sha256:[64 lowercase hex characters]
     * Example: sha256:a1b2c3d4e5f6789012345678901234567890123456789012345678901234
     *
     * @param documentContent Document content as byte array
     * @return SHA-256 hash in format: sha256:hex
     * @throws IllegalArgumentException if documentContent is null or empty
     * @throws RuntimeException         if SHA-256 algorithm is not available
     */
    public String calculateDocumentHash(byte[] documentContent) {
        if (documentContent == null || documentContent.length == 0) {
            throw new IllegalArgumentException("Document content cannot be null or empty");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(documentContent);
            String hex = bytesToHex(hashBytes);
            return "sha256:" + hex;

        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "SHA-256 algorithm not available", e);
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Count total documents for a patient.
     * <p>
     * Counts only ACTIVE documents by default.
     *
     * @param patientCi Patient's Cédula de Identidad
     * @return Total count of ACTIVE documents
     */
    public long countDocumentsByPatient(String patientCi) {
        if (patientCi == null || patientCi.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient CI cannot be null or empty");
        }

        try {
            return rndcRepository.countByPatientCi(patientCi);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting documents for patient CI: " + patientCi, e);
            return 0L;
        }
    }

    /**
     * Count documents for a patient by status.
     *
     * @param patientCi Patient's Cédula de Identidad
     * @param status    Document status filter
     * @return Total count of documents with specified status
     */
    public long countDocumentsByPatientAndStatus(String patientCi, DocumentStatus status) {
        if (patientCi == null || patientCi.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient CI cannot be null or empty");
        }

        if (status == null) {
            throw new IllegalArgumentException("Document status cannot be null");
        }

        try {
            return rndcRepository.countByPatientCiAndStatus(patientCi, status);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting documents for patient CI: " + patientCi + ", status: " + status, e);
            return 0L;
        }
    }

    /**
     * Count documents registered by a specific clinic.
     * <p>
     * This method queries the repository to count all documents (regardless of status)
     * registered by a specific clinic.
     *
     * @param clinicId Clinic identifier
     * @return Total count of documents from the clinic
     */
    public long countDocumentsByClinic(String clinicId) {
        if (clinicId == null || clinicId.trim().isEmpty()) {
            throw new IllegalArgumentException("Clinic ID cannot be null or empty");
        }

        try {
            // Query documents by clinic (all statuses, all pages)
            // Since repository doesn't have countByClinicId, we'll search and count
            List<RndcDocument> documents = rndcRepository.search(
                    null,           // patientCi - all patients
                    null,           // documentType - all types
                    null,           // status - all statuses
                    clinicId,       // clinicId - filter by clinic
                    null,           // fromDate - no date filter
                    null,           // toDate - no date filter
                    0,              // page
                    Integer.MAX_VALUE // size - get all
            );
            return documents.size();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting documents for clinic: " + clinicId, e);
            return 0L;
        }
    }

    // ================================================================
    // Private Helper Methods
    // ================================================================

    /**
     * Get document type distribution for a patient.
     * <p>
     * Returns a map of document types to counts, showing how many documents
     * of each type the patient has.
     * <p>
     * Example:
     * {
     * CLINICAL_NOTE: 25,
     * LAB_RESULT: 12,
     * IMAGING: 5,
     * PRESCRIPTION: 18
     * }
     *
     * @param patientCi Patient's Cédula de Identidad
     * @return Map of document types to counts
     */
    public Map<DocumentType, Long> getDocumentTypeDistribution(String patientCi) {
        if (patientCi == null || patientCi.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient CI cannot be null or empty");
        }

        try {
            // Query all patient documents (all statuses, all pages)
            List<RndcDocument> documents = rndcRepository.search(
                    patientCi,      // patientCi - filter by patient
                    null,           // documentType - all types
                    null,           // status - all statuses
                    null,           // clinicId - all clinics
                    null,           // fromDate - no date filter
                    null,           // toDate - no date filter
                    0,              // page
                    Integer.MAX_VALUE // size - get all
            );

            // Group by document type and count
            return documents.stream()
                    .collect(Collectors.groupingBy(
                            RndcDocument::getDocumentType,
                            Collectors.counting()
                    ));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating document type distribution for patient CI: " + patientCi, e);
            return Map.of();
        }
    }

    /**
     * Validate document registration inputs.
     *
     * @param patientCi       Patient CI to validate
     * @param documentType    Document type to validate
     * @param documentLocator Document locator URL to validate
     * @param documentHash    Document hash to validate
     * @param createdBy       Creator to validate
     * @param clinicId        Clinic ID to validate
     * @throws DocumentRegistrationException if any validation fails
     */
    private void validateRegistrationInputs(String patientCi, DocumentType documentType,
                                            String documentLocator, String documentHash,
                                            String createdBy, String clinicId)
            throws DocumentRegistrationException {

        if (patientCi == null || patientCi.trim().isEmpty()) {
            throw new DocumentRegistrationException("Patient CI cannot be null or empty");
        }

        if (documentType == null) {
            throw new DocumentRegistrationException("Document type cannot be null");
        }

        if (documentLocator == null || documentLocator.trim().isEmpty()) {
            throw new DocumentRegistrationException("Document locator cannot be null or empty");
        }

        if (!isValidUrl(documentLocator)) {
            throw new DocumentRegistrationException("Invalid document locator URL format: " + documentLocator);
        }

        if (documentHash == null || documentHash.trim().isEmpty()) {
            throw new DocumentRegistrationException("Document hash cannot be null or empty");
        }

        if (!isValidDocumentHash(documentHash)) {
            throw new DocumentRegistrationException(
                    "Invalid document hash format: " + documentHash +
                            ". Expected format: sha256:[64 hex characters]");
        }

        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new DocumentRegistrationException("Created by cannot be null or empty");
        }

        if (clinicId == null || clinicId.trim().isEmpty()) {
            throw new DocumentRegistrationException("Clinic ID cannot be null or empty");
        }
    }

    /**
     * Validate URL format.
     *
     * @param url URL to validate
     * @return true if valid URL format, false otherwise
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Validate document hash format.
     * <p>
     * Expected format: sha256:[64 lowercase hex characters]
     *
     * @param hash Hash to validate
     * @return true if valid format, false otherwise
     */
    private boolean isValidDocumentHash(String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            return false;
        }

        return HASH_PATTERN.matcher("sha256:"+hash).matches();
    }

    /**
     * Validate that patient exists in INUS.
     * <p>
     * This is an optional validation step. If InusService is not available
     * or patient lookup fails, we log a warning but don't fail registration.
     * <p>
     * Future Enhancement: Make this validation mandatory (throw exception if patient not found).
     *
     * @param patientCi Patient's CI
     */
    private void validatePatientExists(String patientCi) {
        try {
            Optional<InusUser> patient = inusService.findUserByCi(patientCi);
            if (patient.isEmpty()) {
                LOGGER.log(Level.WARNING,
                        "Patient not found in INUS - CI: {0}. Proceeding with registration anyway.",
                        patientCi);
                // For now, don't fail registration if patient not found
                // In production, you might want to enforce this:
                // throw new DocumentRegistrationException("Patient not found in INUS with CI: " + patientCi);
            } else {
                LOGGER.log(Level.FINE, "Validated patient exists in INUS - CI: {0}", patientCi);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to validate patient in INUS - CI: " + patientCi, e);
            // Don't fail registration if INUS lookup fails
        }
    }
}
