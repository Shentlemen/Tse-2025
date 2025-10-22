package uy.gub.hcen.rndc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uy.gub.hcen.rndc.entity.DocumentStatus;

/**
 * Document Status Update Request DTO
 * <p>
 * Data Transfer Object for updating the status of clinical documents in the RNDC.
 * This DTO is used when changing document status (ACTIVE, INACTIVE, DELETED).
 * <p>
 * Usage Example:
 * <pre>
 * PATCH /api/rndc/documents/123/status
 * {
 *   "status": "INACTIVE",
 *   "updatedBy": "doctor@clinic.uy"
 * }
 * </pre>
 * <p>
 * Valid Status Transitions:
 * - ACTIVE → INACTIVE (archival)
 * - ACTIVE → DELETED (soft delete)
 * - INACTIVE → ACTIVE (reactivation)
 * - INACTIVE → DELETED (soft delete)
 * - DELETED → (no transitions allowed, terminal state)
 * <p>
 * Validation Rules:
 * - status: Required, must be valid DocumentStatus enum value
 * - updatedBy: Required, max 100 characters
 * <p>
 * Authorization (Future):
 * Only the document creator or administrators should be allowed to change status.
 * This will be enforced when JWT authentication is integrated.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 * @see uy.gub.hcen.rndc.entity.DocumentStatus
 * @see uy.gub.hcen.rndc.entity.RndcDocument
 */
public class DocumentStatusUpdateRequest {

    /**
     * New status for the document
     * Valid values: ACTIVE, INACTIVE, DELETED
     */
    @NotNull(message = "Status is required")
    private DocumentStatus status;

    /**
     * Email or ID of the professional performing the status update
     * This is used for audit logging purposes
     */
    @NotBlank(message = "Updated by is required")
    @Size(max = 100, message = "Updated by must not exceed 100 characters")
    private String updatedBy;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Default constructor for JSON deserialization
     */
    public DocumentStatusUpdateRequest() {
    }

    /**
     * Constructor with all fields
     *
     * @param status    New document status
     * @param updatedBy Professional performing the update
     */
    public DocumentStatusUpdateRequest(DocumentStatus status, String updatedBy) {
        this.status = status;
        this.updatedBy = updatedBy;
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

    /**
     * Gets the new status
     *
     * @return Document status
     */
    public DocumentStatus getStatus() {
        return status;
    }

    /**
     * Sets the new status
     *
     * @param status Document status
     */
    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    /**
     * Gets the updater identifier
     *
     * @return Professional who performed the update
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Sets the updater identifier
     *
     * @param updatedBy Professional who performed the update
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "DocumentStatusUpdateRequest{" +
                "status=" + status +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}
