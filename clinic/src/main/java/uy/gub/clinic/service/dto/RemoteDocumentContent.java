package uy.gub.clinic.service.dto;

/**
 * Representa el contenido de un documento obtenido desde HCEN.
 */
public class RemoteDocumentContent {

    private boolean binary;
    private byte[] binaryData;
    private String contentType;
    private String suggestedFileName;
    private String inlineContent;

    public static RemoteDocumentContent binary(byte[] data, String contentType, String fileName) {
        RemoteDocumentContent content = new RemoteDocumentContent();
        content.binary = true;
        content.binaryData = data;
        content.contentType = contentType;
        content.suggestedFileName = fileName;
        return content;
    }

    public static RemoteDocumentContent inline(String content, String contentType) {
        RemoteDocumentContent result = new RemoteDocumentContent();
        result.binary = false;
        result.inlineContent = content;
        result.contentType = contentType;
        return result;
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public byte[] getBinaryData() {
        return binaryData;
    }

    public void setBinaryData(byte[] binaryData) {
        this.binaryData = binaryData;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getSuggestedFileName() {
        return suggestedFileName;
    }

    public void setSuggestedFileName(String suggestedFileName) {
        this.suggestedFileName = suggestedFileName;
    }

    public String getInlineContent() {
        return inlineContent;
    }

    public void setInlineContent(String inlineContent) {
        this.inlineContent = inlineContent;
    }
}

