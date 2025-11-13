package uy.gub.hcen.clinicalhistory.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Document Content Response
 *
 * Response DTO for document content retrieval endpoint.
 * Supports both inline content display (JSON/XML/text) and binary content (PDF).
 *
 * <p>Response Modes:
 * <ul>
 *   <li>Inline (JSON/FHIR/HL7): Returns parsed content in the 'content' field</li>
 *   <li>Binary (PDF): Returns raw bytes with inline disposition header</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 2.0
 * @since 2025-11-13
 */
public class DocumentContentResponse {

    /**
     * Document ID
     */
    private Long documentId;

    /**
     * Document type (APPOINTMENT, PRESCRIPTION, LAB_RESULT, etc.)
     */
    private String documentType;

    /**
     * Content type (MIME type): application/fhir+json, application/pdf, etc.
     */
    private String contentType;

    /**
     * Parsed document content (for structured formats: JSON, XML, FHIR)
     * For JSON/FHIR: Contains the parsed JSON object
     * For XML/HL7: Contains the formatted XML string
     * For binary formats (PDF): This field is null, binary data returned separately
     */
    private Object content;

    /**
     * Document metadata (patient, professional, clinic, dates)
     */
    private DocumentMetadata metadata;

    /**
     * Whether content is available
     */
    private boolean available;

    /**
     * Message if content is not available
     */
    private String message;

    /**
     * Legacy: URL to retrieve the document content (for backward compatibility)
     * @deprecated Use inline content display instead
     */
    @Deprecated
    private String contentUrl;

    /**
     * Content size in bytes (optional)
     */
    private Long contentSize;

    /**
     * Default constructor
     */
    public DocumentContentResponse() {
    }

    /**
     * Constructor for inline content (structured formats)
     */
    public DocumentContentResponse(Long documentId, String documentType, String contentType,
                                   Object content, DocumentMetadata metadata) {
        this.documentId = documentId;
        this.documentType = documentType;
        this.contentType = contentType;
        this.content = content;
        this.metadata = metadata;
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
     * Factory method for inline structured content (JSON, FHIR, HL7)
     */
    public static DocumentContentResponse inline(Long documentId, String documentType,
                                                 String contentType, Object content,
                                                 DocumentMetadata metadata) {
        return new DocumentContentResponse(documentId, documentType, contentType, content, metadata);
    }

    /**
     * Factory method for unavailable content
     */
    public static DocumentContentResponse unavailable(String message) {
        return new DocumentContentResponse(message);
    }

    /**
     * Factory method for available content (legacy)
     * @deprecated Use inline() method instead
     */
    @Deprecated
    public static DocumentContentResponse available(String contentUrl, String contentType, String documentHash) {
        DocumentContentResponse response = new DocumentContentResponse();
        response.setContentUrl(contentUrl);
        response.setContentType(contentType);
        response.setAvailable(true);
        if (response.getMetadata() == null) {
            response.setMetadata(new DocumentMetadata());
        }
        response.getMetadata().setDocumentHash(documentHash);
        return response;
    }

    // Getters and Setters

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public DocumentMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(DocumentMetadata metadata) {
        this.metadata = metadata;
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

    @Deprecated
    public String getContentUrl() {
        return contentUrl;
    }

    @Deprecated
    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public Long getContentSize() {
        return contentSize;
    }

    public void setContentSize(Long contentSize) {
        this.contentSize = contentSize;
    }

    @Override
    public String toString() {
        return "DocumentContentResponse{" +
                "documentId=" + documentId +
                ", documentType='" + documentType + '\'' +
                ", contentType='" + contentType + '\'' +
                ", available=" + available +
                ", message='" + message + '\'' +
                ", hasContent=" + (content != null) +
                '}';
    }
}
