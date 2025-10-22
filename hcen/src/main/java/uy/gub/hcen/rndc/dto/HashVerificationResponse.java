package uy.gub.hcen.rndc.dto;

/**
 * Hash Verification Response DTO
 * <p>
 * Data Transfer Object for document integrity verification results.
 * This DTO is returned when verifying the SHA-256 hash of a clinical document
 * to ensure it has not been tampered with or corrupted.
 * <p>
 * Usage Example:
 * <pre>
 * GET /api/rndc/documents/123/verify
 * Response (200 OK):
 * {
 *   "documentLocator": "https://clinic-001.hcen.uy/api/documents/abc123",
 *   "expectedHash": "sha256:a1b2c3d4e5f67890...",
 *   "actualHash": "sha256:a1b2c3d4e5f67890...",
 *   "valid": true,
 *   "message": "Document hash verified successfully"
 * }
 * </pre>
 * <p>
 * Fields:
 * - documentLocator: URL to the document in peripheral storage
 * - expectedHash: Hash stored in RNDC metadata
 * - actualHash: Hash calculated from actual document (null if not calculated yet)
 * - valid: true if hashes match, false otherwise
 * - message: Human-readable verification result
 * <p>
 * Current Implementation:
 * This is a stub implementation. The actualHash field will be null until we integrate
 * PeripheralNodeClient to fetch and hash actual documents.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 * @see uy.gub.hcen.rndc.entity.RndcDocument
 */
public class HashVerificationResponse {

    private final String documentLocator;
    private final String expectedHash;
    private final String actualHash;
    private final boolean valid;
    private final String message;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Full constructor
     *
     * @param documentLocator URL to document in peripheral storage
     * @param expectedHash    Hash stored in RNDC
     * @param actualHash      Hash calculated from actual document (may be null)
     * @param valid           true if hashes match
     * @param message         Verification result message
     */
    public HashVerificationResponse(String documentLocator, String expectedHash,
                                     String actualHash, boolean valid, String message) {
        this.documentLocator = documentLocator;
        this.expectedHash = expectedHash;
        this.actualHash = actualHash;
        this.valid = valid;
        this.message = message;
    }

    // ================================================================
    // Factory Methods
    // ================================================================

    /**
     * Create response for successful hash verification
     *
     * @param documentLocator Document URL
     * @param expectedHash    Expected hash
     * @param actualHash      Actual hash (calculated from document)
     * @return HashVerificationResponse indicating success
     */
    public static HashVerificationResponse success(String documentLocator, String expectedHash, String actualHash) {
        return new HashVerificationResponse(
                documentLocator,
                expectedHash,
                actualHash,
                true,
                "Document hash verified successfully"
        );
    }

    /**
     * Create response for failed hash verification (mismatch)
     *
     * @param documentLocator Document URL
     * @param expectedHash    Expected hash
     * @param actualHash      Actual hash (calculated from document)
     * @return HashVerificationResponse indicating failure
     */
    public static HashVerificationResponse failure(String documentLocator, String expectedHash, String actualHash) {
        return new HashVerificationResponse(
                documentLocator,
                expectedHash,
                actualHash,
                false,
                "Document hash verification failed - document may have been modified or corrupted"
        );
    }

    /**
     * Create response when document cannot be retrieved
     *
     * @param documentLocator Document URL
     * @param expectedHash    Expected hash
     * @param errorMessage    Error message
     * @return HashVerificationResponse indicating error
     */
    public static HashVerificationResponse error(String documentLocator, String expectedHash, String errorMessage) {
        return new HashVerificationResponse(
                documentLocator,
                expectedHash,
                null,
                false,
                "Cannot verify hash: " + errorMessage
        );
    }

    /**
     * Create response for stub implementation (hash format validation only)
     * <p>
     * This is used until PeripheralNodeClient integration is complete.
     *
     * @param documentLocator Document URL
     * @param expectedHash    Expected hash
     * @return HashVerificationResponse indicating stub status
     */
    public static HashVerificationResponse stub(String documentLocator, String expectedHash) {
        return new HashVerificationResponse(
                documentLocator,
                expectedHash,
                null,
                true,
                "Hash format is valid (actual verification pending PeripheralNodeClient integration)"
        );
    }

    // ================================================================
    // Getters Only (Immutable DTO)
    // ================================================================

    /**
     * Gets the document locator URL
     *
     * @return Document locator
     */
    public String getDocumentLocator() {
        return documentLocator;
    }

    /**
     * Gets the expected hash (from RNDC metadata)
     *
     * @return Expected hash
     */
    public String getExpectedHash() {
        return expectedHash;
    }

    /**
     * Gets the actual hash (calculated from document)
     * <p>
     * May be null if document was not retrieved/hashed.
     *
     * @return Actual hash, or null if not calculated
     */
    public String getActualHash() {
        return actualHash;
    }

    /**
     * Checks if hash verification passed
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Gets the verification result message
     *
     * @return Human-readable message
     */
    public String getMessage() {
        return message;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "HashVerificationResponse{" +
                "documentLocator='" + documentLocator + '\'' +
                ", expectedHash='" + (expectedHash != null ? "sha256:..." : null) + '\'' +
                ", actualHash='" + (actualHash != null ? "sha256:..." : null) + '\'' +
                ", valid=" + valid +
                ", message='" + message + '\'' +
                '}';
    }
}
