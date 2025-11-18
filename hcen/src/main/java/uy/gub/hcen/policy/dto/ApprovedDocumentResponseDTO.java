package uy.gub.hcen.policy.dto;

import org.hl7.fhir.r4.model.DocumentReference;

/**
 * Approved Document Response DTO
 *
 * Response DTO for professional document retrieval endpoint.
 * Contains FHIR DocumentReference resource representing the approved clinical document.
 *
 * This DTO is returned when a professional successfully retrieves a document
 * after patient approval via the access request workflow.
 *
 * Endpoint: GET /api/access-requests/{requestId}/approved-document
 *
 * Response Format:
 * {
 *   "requestId": 123,
 *   "status": "APPROVED",
 *   "documentReference": {
 *     "resourceType": "DocumentReference",
 *     "id": "doc-456",
 *     "status": "current",
 *     "type": { ... },
 *     "subject": { ... },
 *     "content": [{
 *       "attachment": {
 *         "contentType": "application/pdf",
 *         "data": "base64-encoded-content...",
 *         "hash": "sha256:abc123..."
 *       }
 *     }]
 *   }
 * }
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-17
 */
public class ApprovedDocumentResponseDTO {

    /**
     * Access request ID
     */
    private Long requestId;

    /**
     * Request status (should always be APPROVED for this response)
     */
    private String status;

    /**
     * FHIR DocumentReference resource containing the clinical document
     * with embedded base64-encoded content
     */
    private DocumentReference documentReference;

    /**
     * Default constructor
     */
    public ApprovedDocumentResponseDTO() {
    }

    /**
     * Constructor with all fields
     *
     * @param requestId Access request ID
     * @param status Request status
     * @param documentReference FHIR DocumentReference resource
     */
    public ApprovedDocumentResponseDTO(Long requestId, String status, DocumentReference documentReference) {
        this.requestId = requestId;
        this.status = status;
        this.documentReference = documentReference;
    }

    /**
     * Static factory method
     *
     * @param requestId Access request ID
     * @param status Request status
     * @param documentReference FHIR DocumentReference resource
     * @return ApprovedDocumentResponseDTO instance
     */
    public static ApprovedDocumentResponseDTO of(Long requestId, String status, DocumentReference documentReference) {
        return new ApprovedDocumentResponseDTO(requestId, status, documentReference);
    }

    // Getters and Setters

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DocumentReference getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(DocumentReference documentReference) {
        this.documentReference = documentReference;
    }

    @Override
    public String toString() {
        return "ApprovedDocumentResponseDTO{" +
                "requestId=" + requestId +
                ", status='" + status + '\'' +
                ", documentReference=" + (documentReference != null ? documentReference.getId() : "null") +
                '}';
    }
}
