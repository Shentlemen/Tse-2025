package uy.gub.hcen.policy.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Access Request Entity
 *
 * Represents a professional's request to access patient data when policies evaluate to PENDING.
 * When a professional attempts to access a document but patient policies don't explicitly permit
 * or deny the access, an access request is created for patient approval.
 *
 * Workflow:
 * 1. Professional requests access to patient document
 * 2. PolicyEngine evaluates policies and returns PENDING decision
 * 3. AccessRequest is created with status PENDING
 * 4. Push notification sent to patient (Firebase)
 * 5. Patient approves or denies request via mobile app or web portal
 * 6. Request status updated to APPROVED or DENIED
 * 7. If not responded within expiration period (default 48 hours), status becomes EXPIRED
 *
 * Request Status:
 * - PENDING: Awaiting patient response
 * - APPROVED: Patient approved the request
 * - DENIED: Patient denied the request
 * - EXPIRED: Request expired without response
 *
 * Database Schema: policies.access_requests
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@Entity
@Table(name = "access_requests", schema = "policies", indexes = {
    @Index(name = "idx_access_requests_professional_id", columnList = "professional_id"),
    @Index(name = "idx_access_requests_patient_ci", columnList = "patient_ci"),
    @Index(name = "idx_access_requests_status", columnList = "status"),
    @Index(name = "idx_access_requests_patient_ci_status", columnList = "patient_ci, status"),
    @Index(name = "idx_access_requests_status_expires_at", columnList = "status, expires_at")
})
public class AccessRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Default expiration period in hours (48 hours)
     */
    private static final int DEFAULT_EXPIRATION_HOURS = 48;

    /**
     * Request Status Enumeration
     */
    public enum RequestStatus {
        /**
         * Awaiting patient response
         */
        PENDING,

        /**
         * Patient approved the request
         */
        APPROVED,

        /**
         * Patient denied the request
         */
        DENIED,

        /**
         * Request expired without response
         */
        EXPIRED
    }

    /**
     * Unique request identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Requesting professional identifier
     */
    @Column(name = "professional_id", nullable = false, length = 100)
    private String professionalId;

    /**
     * Patient CI (Cédula de Identidad)
     * References: inus.inus_users.ci
     */
    @Column(name = "patient_ci", nullable = false, length = 20)
    private String patientCi;

    /**
     * Document ID being requested (optional)
     * If null, this is a general access request
     * References: rndc.rndc_documents.id
     */
    @Column(name = "document_id")
    private Long documentId;

    /**
     * Reason for access request (provided by professional)
     * This helps patient understand why access is needed
     */
    @Column(name = "request_reason", columnDefinition = "TEXT")
    private String requestReason;

    /**
     * Request status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStatus status;

    /**
     * Timestamp when request was created
     */
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    /**
     * Timestamp when patient responded to request
     */
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    /**
     * Patient's response explanation (optional)
     * Patient can provide reason for approval or denial
     */
    @Column(name = "patient_response", columnDefinition = "TEXT")
    private String patientResponse;

    /**
     * Timestamp when request expires
     * After this time, request status should be changed to EXPIRED
     * Default: 48 hours from requested_at
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Default constructor
     */
    public AccessRequest() {
        this.status = RequestStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(DEFAULT_EXPIRATION_HOURS);
    }

    /**
     * Constructor with required fields
     *
     * @param professionalId Professional ID
     * @param patientCi Patient CI
     * @param requestReason Request reason
     */
    public AccessRequest(String professionalId, String patientCi, String requestReason) {
        this();
        this.professionalId = professionalId;
        this.patientCi = patientCi;
        this.requestReason = requestReason;
    }

    /**
     * Constructor with document ID
     *
     * @param professionalId Professional ID
     * @param patientCi Patient CI
     * @param documentId Document ID
     * @param requestReason Request reason
     */
    public AccessRequest(String professionalId, String patientCi, Long documentId, String requestReason) {
        this(professionalId, patientCi, requestReason);
        this.documentId = documentId;
    }

    /**
     * Checks if this request is expired
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Checks if this request is pending
     *
     * @return true if pending, false otherwise
     */
    public boolean isPending() {
        return status == RequestStatus.PENDING && !isExpired();
    }

    /**
     * Approves this request
     *
     * @param patientResponse Patient response explanation (optional)
     */
    public void approve(String patientResponse) {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("Cannot approve request with status: " + status);
        }
        if (isExpired()) {
            throw new IllegalStateException("Cannot approve expired request");
        }
        this.status = RequestStatus.APPROVED;
        this.respondedAt = LocalDateTime.now();
        this.patientResponse = patientResponse;
    }

    /**
     * Denies this request
     *
     * @param patientResponse Patient response explanation (optional)
     */
    public void deny(String patientResponse) {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("Cannot deny request with status: " + status);
        }
        if (isExpired()) {
            throw new IllegalStateException("Cannot deny expired request");
        }
        this.status = RequestStatus.DENIED;
        this.respondedAt = LocalDateTime.now();
        this.patientResponse = patientResponse;
    }

    /**
     * Marks this request as expired
     */
    public void expire() {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("Cannot expire request with status: " + status);
        }
        this.status = RequestStatus.EXPIRED;
    }

    /**
     * Pre-persist callback
     */
    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            expiresAt = requestedAt.plusHours(DEFAULT_EXPIRATION_HOURS);
        }
        if (status == null) {
            status = RequestStatus.PENDING;
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProfessionalId() {
        return professionalId;
    }

    public void setProfessionalId(String professionalId) {
        this.professionalId = professionalId;
    }

    public String getPatientCi() {
        return patientCi;
    }

    public void setPatientCi(String patientCi) {
        this.patientCi = patientCi;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getRequestReason() {
        return requestReason;
    }

    public void setRequestReason(String requestReason) {
        this.requestReason = requestReason;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public String getPatientResponse() {
        return patientResponse;
    }

    public void setPatientResponse(String patientResponse) {
        this.patientResponse = patientResponse;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    // Equals, HashCode, and ToString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessRequest that = (AccessRequest) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(professionalId, that.professionalId) &&
               Objects.equals(patientCi, that.patientCi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, professionalId, patientCi);
    }

    @Override
    public String toString() {
        return "AccessRequest{" +
               "id=" + id +
               ", professionalId='" + professionalId + '\'' +
               ", patientCi='" + patientCi + '\'' +
               ", documentId=" + documentId +
               ", status=" + status +
               ", requestedAt=" + requestedAt +
               ", expiresAt=" + expiresAt +
               '}';
    }
}
