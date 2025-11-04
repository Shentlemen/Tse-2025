package uy.gub.hcen.clinicalhistory.dto;

/**
 * Document Content Response
 *
 * Response DTO for document content retrieval endpoint.
 * Contains the URL and metadata for downloading/viewing document content.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-04
 */
public class DocumentContentResponse {

    /**
     * URL to retrieve the document content
     * Could be a direct link to peripheral node or a proxy URL
     */
    private String contentUrl;

    /**
     * Content type (MIME type)
     */
    private String contentType;

    /**
     * Content size in bytes (optional)
     */
    private Long contentSize;

    /**
     * Whether content is available
     */
    private boolean available;

    /**
     * Message if content is not available
     */
    private String message;

    /**
     * Document hash for integrity verification
     */
    private String documentHash;

    /**
     * Default constructor
     */
    public DocumentContentResponse() {
    }

    /**
     * Constructor for available content
     */
    public DocumentContentResponse(String contentUrl, String contentType) {
        this.contentUrl = contentUrl;
        this.contentType = contentType;
        this.available = true;
    }

    /**
     * Constructor for unavailable content
     */
    public DocumentContentResponse(String message) {
        this.available = false;
        this.message = message;
    }

    /**
     * Factory method for available content
     */
    public static DocumentContentResponse available(String contentUrl, String contentType, String documentHash) {
        DocumentContentResponse response = new DocumentContentResponse(contentUrl, contentType);
        response.setDocumentHash(documentHash);
        return response;
    }

    /**
     * Factory method for unavailable content
     */
    public static DocumentContentResponse unavailable(String message) {
        return new DocumentContentResponse(message);
    }

    // Getters and Setters

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getContentSize() {
        return contentSize;
    }

    public void setContentSize(Long contentSize) {
        this.contentSize = contentSize;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDocumentHash() {
        return documentHash;
    }

    public void setDocumentHash(String documentHash) {
        this.documentHash = documentHash;
    }

    @Override
    public String toString() {
        return "DocumentContentResponse{" +
                "contentUrl='" + contentUrl + '\'' +
                ", contentType='" + contentType + '\'' +
                ", available=" + available +
                ", message='" + message + '\'' +
                '}';
    }
}
