package uy.gub.hcen.api.dto;

import java.time.LocalDateTime;

/**
 * Error Response DTO
 * <p>
 * Standard error response format for all HCEN REST API endpoints.
 * This DTO ensures consistent error reporting across the entire application.
 * <p>
 * Error Response Format:
 * {
 *   "error": "USER_NOT_FOUND",
 *   "message": "User with CI 12345678 not found",
 *   "timestamp": "2025-10-21T14:30:00"
 * }
 * <p>
 * The error field contains a machine-readable error code (uppercase, snake_case).
 * The message field contains a human-readable error description.
 * The timestamp field indicates when the error occurred.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 */
public class ErrorResponse {

    private final String error;
    private final String message;
    private final LocalDateTime timestamp;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Constructor with error code and message
     * Timestamp is automatically set to current time
     *
     * @param error   Machine-readable error code
     * @param message Human-readable error message
     */
    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Full constructor with explicit timestamp (mainly for testing)
     *
     * @param error     Machine-readable error code
     * @param message   Human-readable error message
     * @param timestamp Time when error occurred
     */
    public ErrorResponse(String error, String message, LocalDateTime timestamp) {
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
    }

    // ================================================================
    // Factory Methods for Common Error Types
    // ================================================================

    /**
     * Create validation error response
     *
     * @param message Validation error details
     * @return ErrorResponse for validation errors
     */
    public static ErrorResponse validationError(String message) {
        return new ErrorResponse("VALIDATION_ERROR", message);
    }

    /**
     * Create not found error response
     *
     * @param resource Resource type (e.g., "User", "Document")
     * @param id       Resource identifier
     * @return ErrorResponse for not found errors
     */
    public static ErrorResponse notFound(String resource, String id) {
        return new ErrorResponse(
                resource.toUpperCase() + "_NOT_FOUND",
                resource + " with ID " + id + " not found"
        );
    }

    /**
     * Create conflict error response
     *
     * @param message Conflict details
     * @return ErrorResponse for conflict errors
     */
    public static ErrorResponse conflict(String message) {
        return new ErrorResponse("CONFLICT", message);
    }

    /**
     * Create unauthorized error response
     *
     * @param message Authorization failure details
     * @return ErrorResponse for unauthorized errors
     */
    public static ErrorResponse unauthorized(String message) {
        return new ErrorResponse("UNAUTHORIZED", message);
    }

    /**
     * Create forbidden error response
     *
     * @param message Access denial details
     * @return ErrorResponse for forbidden errors
     */
    public static ErrorResponse forbidden(String message) {
        return new ErrorResponse("FORBIDDEN", message);
    }

    /**
     * Create internal server error response
     *
     * @param message Error details (should not expose sensitive information)
     * @return ErrorResponse for internal server errors
     */
    public static ErrorResponse internalServerError(String message) {
        return new ErrorResponse("INTERNAL_SERVER_ERROR", message);
    }

    /**
     * Create bad request error response
     *
     * @param message Bad request details
     * @return ErrorResponse for bad request errors
     */
    public static ErrorResponse badRequest(String message) {
        return new ErrorResponse("BAD_REQUEST", message);
    }

    // ================================================================
    // Getters Only (Immutable DTO)
    // ================================================================

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "error='" + error + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
