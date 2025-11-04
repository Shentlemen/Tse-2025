package uy.gub.hcen.clinicalhistory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import uy.gub.hcen.rndc.entity.DocumentType;
import uy.gub.hcen.rndc.entity.RndcDocument;

import java.time.LocalDateTime;

/**
 * Document List Item DTO
 *
 * Represents a single document in the patient's clinical history list view.
 * Contains summary information for display in cards/lists.
 *
 * <p>This DTO is optimized for list views with minimal data to reduce payload size.
 * For detailed document information, use DocumentDetailDTO.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-04
 */
public class DocumentListItemDTO {

    /**
     * Document ID in RNDC
     */
    private Long id;

    /**
     * Document type enum value (e.g., "LAB_RESULT", "IMAGING")
     */
    private String documentType;

    /**
     * Human-readable document type (e.g., "Laboratory Result", "Medical Imaging")
     */
    private String documentTypeDisplayName;

    /**
     * Document title (e.g., "Hemograma Completo", "Radiografía de Tórax")
     */
    private String title;

    /**
     * Clinic/institution name that created the document
     */
    private String clinicName;

    /**
     * Clinic ID for filtering
     */
    private String clinicId;

    /**
     * Professional who created the document (name or email)
     */
    private String professionalName;

    /**
     * Document creation timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Document status (AVAILABLE, PENDING, UNAVAILABLE)
     */
    private String status;

    /**
     * Whether document content can be retrieved
     */
    private boolean hasContent;

    /**
     * Optional preview URL for thumbnails/quick view
     */
    private String previewUrl;

    /**
     * Document hash for integrity verification
     */
    private String documentHash;

    /**
     * Default constructor
     */
    public DocumentListItemDTO() {
    }

    /**
     * Constructor with all fields
     */
    public DocumentListItemDTO(Long id, String documentType, String documentTypeDisplayName,
                               String title, String clinicName, String clinicId,
                               String professionalName, LocalDateTime createdAt,
                               String status, boolean hasContent, String previewUrl) {
        this.id = id;
        this.documentType = documentType;
        this.documentTypeDisplayName = documentTypeDisplayName;
        this.title = title;
        this.clinicName = clinicName;
        this.clinicId = clinicId;
        this.professionalName = professionalName;
        this.createdAt = createdAt;
        this.status = status;
        this.hasContent = hasContent;
        this.previewUrl = previewUrl;
    }

    /**
     * Creates DTO from RndcDocument entity
     *
     * @param document RNDC document entity
     * @return DocumentListItemDTO
     */
    public static DocumentListItemDTO fromEntity(RndcDocument document) {
        if (document == null) {
            return null;
        }

        DocumentListItemDTO dto = new DocumentListItemDTO();
        dto.setId(document.getId());
        dto.setDocumentType(document.getDocumentType().name());
        dto.setDocumentTypeDisplayName(document.getDocumentType().getDisplayName());
        dto.setTitle(document.getDocumentTitle() != null ? document.getDocumentTitle() : "Documento sin título");
        dto.setClinicName(document.getClinicId()); // TODO: Fetch actual clinic name from ClinicService
        dto.setClinicId(document.getClinicId());
        dto.setProfessionalName(document.getCreatedBy());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setStatus(document.getStatus().name());
        dto.setHasContent(document.getDocumentLocator() != null && !document.getDocumentLocator().isEmpty());
        dto.setDocumentHash(document.getDocumentHash());
        dto.setPreviewUrl(null); // TODO: Implement preview generation service

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

    public String getDocumentTypeDisplayName() {
        return documentTypeDisplayName;
    }

    public void setDocumentTypeDisplayName(String documentTypeDisplayName) {
        this.documentTypeDisplayName = documentTypeDisplayName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isHasContent() {
        return hasContent;
    }

    public void setHasContent(boolean hasContent) {
        this.hasContent = hasContent;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getDocumentHash() {
        return documentHash;
    }

    public void setDocumentHash(String documentHash) {
        this.documentHash = documentHash;
    }

    @Override
    public String toString() {
        return "DocumentListItemDTO{" +
                "id=" + id +
                ", documentType='" + documentType + '\'' +
                ", title='" + title + '\'' +
                ", clinicId='" + clinicId + '\'' +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                '}';
    }
}
