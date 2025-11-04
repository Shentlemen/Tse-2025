package uy.gub.hcen.clinicalhistory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import uy.gub.hcen.rndc.entity.RndcDocument;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Document Detail DTO
 *
 * Detailed document information for document detail view/modal.
 * Contains all metadata and additional context about the clinical document.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-04
 */
public class DocumentDetailDTO {

    /**
     * Document ID
     */
    private Long id;

    /**
     * Document type
     */
    private String documentType;

    /**
     * Document title
     */
    private String title;

    /**
     * Document description/summary
     */
    private String description;

    /**
     * Clinic name
     */
    private String clinicName;

    /**
     * Professional name who created the document
     */
    private String professionalName;

    /**
     * Creation timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Last modified timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModified;

    /**
     * Content type (e.g., "application/pdf", "image/jpeg")
     */
    private String contentType;

    /**
     * Content size in bytes
     */
    private long contentSize;

    /**
     * Document hash for integrity verification
     */
    private String documentHash;

    /**
     * Document status
     */
    private String status;

    /**
     * Document locator URL (for retrieval from peripheral node)
     */
    private String documentLocator;

    /**
     * Additional metadata (flexible key-value pairs)
     */
    private Map<String, Object> metadata;

    /**
     * Default constructor
     */
    public DocumentDetailDTO() {
        this.metadata = new HashMap<>();
    }

    /**
     * Creates DTO from RndcDocument entity
     *
     * @param document RNDC document entity
     * @return DocumentDetailDTO
     */
    public static DocumentDetailDTO fromEntity(RndcDocument document) {
        if (document == null) {
            return null;
        }

        DocumentDetailDTO dto = new DocumentDetailDTO();
        dto.setId(document.getId());
        dto.setDocumentType(document.getDocumentType().getDisplayName());
        dto.setTitle(document.getDocumentTitle() != null ? document.getDocumentTitle() : "Documento sin título");
        dto.setDescription(document.getDocumentDescription());
        dto.setClinicName(document.getClinicId()); // TODO: Fetch actual clinic name
        dto.setProfessionalName(document.getCreatedBy());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setLastModified(document.getCreatedAt()); // RNDC doesn't track modifications currently
        dto.setContentType("application/pdf"); // TODO: Determine from document locator or metadata
        dto.setContentSize(0); // TODO: Fetch from peripheral node when content is retrieved
        dto.setDocumentHash(document.getDocumentHash());
        dto.setStatus(document.getStatus().name());
        dto.setDocumentLocator(document.getDocumentLocator());

        // Add metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentTypeCode", document.getDocumentType().name());
        metadata.put("clinicId", document.getClinicId());
        metadata.put("isSensitive", document.getDocumentType().isSensitive());
        metadata.put("isLabDocument", document.getDocumentType().isLabDocument());
        metadata.put("isImagingDocument", document.getDocumentType().isImagingDocument());
        dto.setMetadata(metadata);

        return dto;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getProfessionalName() {
        return professionalName;
    }

    public void setProfessionalName(String professionalName) {
        this.professionalName = professionalName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getContentSize() {
        return contentSize;
    }

    public void setContentSize(long contentSize) {
        this.contentSize = contentSize;
    }

    public String getDocumentHash() {
        return documentHash;
    }

    public void setDocumentHash(String documentHash) {
        this.documentHash = documentHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDocumentLocator() {
        return documentLocator;
    }

    public void setDocumentLocator(String documentLocator) {
        this.documentLocator = documentLocator;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "DocumentDetailDTO{" +
                "id=" + id +
                ", documentType='" + documentType + '\'' +
                ", title='" + title + '\'' +
                ", clinicName='" + clinicName + '\'' +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                '}';
    }
}
