package uy.gub.clinic.integration.dto.hcen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Representa la respuesta del endpoint /clinical-history/documents/{id}/content de HCEN.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HcenDocumentContentResponse {

    private Long documentId;
    private String documentType;
    private String contentType;
    private JsonNode content;
    private HcenDocumentMetadataDTO metadata;
    private boolean available;
    private String message;
    private String contentUrl;
    private Long contentSize;

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

    public JsonNode getContent() {
        return content;
    }

    public void setContent(JsonNode content) {
        this.content = content;
    }

    public HcenDocumentMetadataDTO getMetadata() {
        return metadata;
    }

    public void setMetadata(HcenDocumentMetadataDTO metadata) {
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

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public Long getContentSize() {
        return contentSize;
    }

    public void setContentSize(Long contentSize) {
        this.contentSize = contentSize;
    }
}

