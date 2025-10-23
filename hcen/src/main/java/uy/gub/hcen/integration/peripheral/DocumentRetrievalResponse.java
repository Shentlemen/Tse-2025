package uy.gub.hcen.integration.peripheral;

/**
 * Document Retrieval Response DTO
 * <p>
 * Data Transfer Object for document retrieval responses from peripheral nodes (AC015).
 * Wraps document bytes along with metadata for validation and tracking.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
public class DocumentRetrievalResponse {

    /**
     * Document bytes (PDF, XML, FHIR, etc.)
     */
    private byte[] documentBytes;

    /**
     * Document size in bytes
     */
    private long documentSize;

    /**
     * Content type (e.g., application/pdf, application/xml, application/fhir+json)
     */
    private String contentType;

    /**
     * SHA-256 hash of document (for integrity verification)
     */
    private String documentHash;

    /**
     * Whether integrity was verified
     */
    private boolean integrityVerified;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor
     */
    public DocumentRetrievalResponse() {
    }

    /**
     * Constructor with document bytes only
     *
     * @param documentBytes Document bytes
     */
    public DocumentRetrievalResponse(byte[] documentBytes) {
        this.documentBytes = documentBytes;
        this.documentSize = documentBytes != null ? documentBytes.length : 0;
        this.integrityVerified = false;
    }

    /**
     * Full constructor
     *
     * @param documentBytes     Document bytes
     * @param contentType       Content type
     * @param documentHash      SHA-256 hash
     * @param integrityVerified Whether integrity was verified
     */
    public DocumentRetrievalResponse(byte[] documentBytes, String contentType,
                                     String documentHash, boolean integrityVerified) {
        this.documentBytes = documentBytes;
        this.documentSize = documentBytes != null ? documentBytes.length : 0;
        this.contentType = contentType;
        this.documentHash = documentHash;
        this.integrityVerified = integrityVerified;
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

    public byte[] getDocumentBytes() {
        return documentBytes;
    }

    public void setDocumentBytes(byte[] documentBytes) {
        this.documentBytes = documentBytes;
        this.documentSize = documentBytes != null ? documentBytes.length : 0;
    }

    public long getDocumentSize() {
        return documentSize;
    }

    public void setDocumentSize(long documentSize) {
        this.documentSize = documentSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDocumentHash() {
        return documentHash;
    }

    public void setDocumentHash(String documentHash) {
        this.documentHash = documentHash;
    }

    public boolean isIntegrityVerified() {
        return integrityVerified;
    }

    public void setIntegrityVerified(boolean integrityVerified) {
        this.integrityVerified = integrityVerified;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "DocumentRetrievalResponse{" +
                "documentSize=" + documentSize +
                ", contentType='" + contentType + '\'' +
                ", integrityVerified=" + integrityVerified +
                '}';
    }
}
