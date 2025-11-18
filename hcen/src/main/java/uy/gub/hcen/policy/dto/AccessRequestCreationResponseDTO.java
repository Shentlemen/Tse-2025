package uy.gub.hcen.policy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import uy.gub.hcen.policy.entity.AccessRequest;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for access request creation response
 *
 * Returned when an access request is successfully created (or when a duplicate
 * pending request is found). Provides the request ID, status, timestamps, and
 * a descriptive message.
 *
 * Response Scenarios:
 * - 201 Created: New request created successfully (isNewRequest = true)
 * - 200 OK: Duplicate pending request found (isNewRequest = false)
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class AccessRequestCreationResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Created (or existing) request ID
     */
    private Long requestId;

    /**
     * Request status (should be PENDING for new requests)
     */
    private String status;

    /**
     * Timestamp when request was created
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Timestamp when request will expire (default: 48 hours from creation)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    /**
     * Success or informational message
     */
    private String message;

    /**
     * Indicates if this is a newly created request (true) or existing request (false)
     * Used to determine HTTP status code (201 vs 200)
     */
    private Boolean isNewRequest;

    // Constructors

    /**
     * Default constructor
     */
    public AccessRequestCreationResponseDTO() {
    }

    /**
     * Full constructor
     */
    public AccessRequestCreationResponseDTO(Long requestId, String status,
                                           LocalDateTime createdAt, LocalDateTime expiresAt,
                                           String message, Boolean isNewRequest) {
        this.requestId = requestId;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.message = message;
        this.isNewRequest = isNewRequest;
    }

    // Factory method

    /**
     * Creates response DTO from AccessRequest entity
     *
     * @param request AccessRequest entity
     * @param isNew true if newly created, false if existing
     * @return AccessRequestCreationResponseDTO
     */
    public static AccessRequestCreationResponseDTO fromEntity(AccessRequest request, boolean isNew) {
        String message = isNew
            ? "Access request created successfully. Patient will be notified."
            : "An identical pending request already exists. Returning existing request.";

        return new AccessRequestCreationResponseDTO(
            request.getId(),
            request.getStatus().name(),
            request.getRequestedAt(),
            request.getExpiresAt(),
            message,
            isNew
        );
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsNewRequest() {
        return isNewRequest;
    }

    public void setIsNewRequest(Boolean isNewRequest) {
        this.isNewRequest = isNewRequest;
    }

    @Override
    public String toString() {
        return "AccessRequestCreationResponseDTO{" +
               "requestId=" + requestId +
               ", status='" + status + '\'' +
               ", createdAt=" + createdAt +
               ", expiresAt=" + expiresAt +
               ", message='" + message + '\'' +
               ", isNewRequest=" + isNewRequest +
               '}';
    }
}
