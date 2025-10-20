package uy.gub.hcen.notification.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Notification Preference Entity
 *
 * Stores patient notification preferences and Firebase Cloud Messaging (FCM) tokens
 * for push notifications in the HCEN mobile application.
 *
 * Features:
 * - Granular notification preferences (access requests, new access, policy changes, new documents)
 * - FCM token storage for mobile push notifications
 * - One-to-one mapping with patient (user_ci)
 * - Auto-updated timestamp on modifications
 *
 * PostgreSQL Table: notification_preferences
 *
 * Related Use Cases: CU04 (configure notifications), CU07 (receive notifications)
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@Entity
@Table(name = "notification_preferences", indexes = {
    @Index(name = "idx_notification_user_ci", columnList = "user_ci", unique = true)
})
public class NotificationPreference {

    // =========================================================================
    // Fields
    // =========================================================================

    /**
     * Unique identifier (auto-generated)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User's Cedula de Identidad (CI) - national ID
     * Unique per patient
     */
    @Column(name = "user_ci", unique = true, nullable = false, length = 20)
    private String userCi;

    /**
     * Notify when a health professional requests access to patient's records
     * Default: true (patients should be notified of access requests)
     */
    @Column(name = "notify_access_request")
    private Boolean notifyAccessRequest = true;

    /**
     * Notify when someone accesses patient's records
     * Default: true (patients should know when their data is accessed)
     */
    @Column(name = "notify_new_access")
    private Boolean notifyNewAccess = true;

    /**
     * Notify when patient's access policies are modified
     * Default: true (patients should be notified of policy changes)
     */
    @Column(name = "notify_policy_change")
    private Boolean notifyPolicyChange = true;

    /**
     * Notify when a new clinical document is added to patient's records
     * Default: false (can be noisy, opt-in)
     */
    @Column(name = "notify_new_document")
    private Boolean notifyNewDocument = false;

    /**
     * Firebase Cloud Messaging token for mobile push notifications
     * Null if patient has not registered a mobile device
     */
    @Column(name = "fcm_token", length = 255)
    private String fcmToken;

    /**
     * Timestamp of last update
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // =========================================================================
    // Constructors
    // =========================================================================

    /**
     * Default constructor (required by JPA)
     */
    public NotificationPreference() {
        this.notifyAccessRequest = true;
        this.notifyNewAccess = true;
        this.notifyPolicyChange = true;
        this.notifyNewDocument = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor with user CI
     *
     * @param userCi User's national ID
     */
    public NotificationPreference(String userCi) {
        this();
        this.userCi = userCi;
    }

    /**
     * Full constructor for creating notification preferences
     *
     * @param userCi User's national ID
     * @param notifyAccessRequest Notify on access requests
     * @param notifyNewAccess Notify on new access
     * @param notifyPolicyChange Notify on policy changes
     * @param notifyNewDocument Notify on new documents
     * @param fcmToken Firebase Cloud Messaging token
     */
    public NotificationPreference(String userCi, Boolean notifyAccessRequest,
                                   Boolean notifyNewAccess, Boolean notifyPolicyChange,
                                   Boolean notifyNewDocument, String fcmToken) {
        this.userCi = userCi;
        this.notifyAccessRequest = notifyAccessRequest != null ? notifyAccessRequest : true;
        this.notifyNewAccess = notifyNewAccess != null ? notifyNewAccess : true;
        this.notifyPolicyChange = notifyPolicyChange != null ? notifyPolicyChange : true;
        this.notifyNewDocument = notifyNewDocument != null ? notifyNewDocument : false;
        this.fcmToken = fcmToken;
        this.updatedAt = LocalDateTime.now();
    }

    // =========================================================================
    // JPA Lifecycle Callbacks
    // =========================================================================

    /**
     * Pre-persist callback - Set initial timestamp
     */
    @PrePersist
    protected void onCreate() {
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Pre-update callback - Update timestamp on modifications
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =========================================================================
    // Getters and Setters
    // =========================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserCi() {
        return userCi;
    }

    public void setUserCi(String userCi) {
        this.userCi = userCi;
    }

    public Boolean getNotifyAccessRequest() {
        return notifyAccessRequest;
    }

    public void setNotifyAccessRequest(Boolean notifyAccessRequest) {
        this.notifyAccessRequest = notifyAccessRequest;
    }

    public Boolean getNotifyNewAccess() {
        return notifyNewAccess;
    }

    public void setNotifyNewAccess(Boolean notifyNewAccess) {
        this.notifyNewAccess = notifyNewAccess;
    }

    public Boolean getNotifyPolicyChange() {
        return notifyPolicyChange;
    }

    public void setNotifyPolicyChange(Boolean notifyPolicyChange) {
        this.notifyPolicyChange = notifyPolicyChange;
    }

    public Boolean getNotifyNewDocument() {
        return notifyNewDocument;
    }

    public void setNotifyNewDocument(Boolean notifyNewDocument) {
        this.notifyNewDocument = notifyNewDocument;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // =========================================================================
    // Utility Methods
    // =========================================================================

    /**
     * Checks if push notifications are enabled (FCM token is registered)
     *
     * @return true if FCM token is set, false otherwise
     */
    public boolean isPushEnabled() {
        return fcmToken != null && !fcmToken.trim().isEmpty();
    }

    /**
     * Checks if any notification is enabled
     *
     * @return true if at least one notification type is enabled
     */
    public boolean hasAnyNotificationEnabled() {
        return Boolean.TRUE.equals(notifyAccessRequest) ||
               Boolean.TRUE.equals(notifyNewAccess) ||
               Boolean.TRUE.equals(notifyPolicyChange) ||
               Boolean.TRUE.equals(notifyNewDocument);
    }

    // =========================================================================
    // Equals, HashCode, and ToString
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationPreference that = (NotificationPreference) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(userCi, that.userCi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userCi);
    }

    @Override
    public String toString() {
        return "NotificationPreference{" +
               "id=" + id +
               ", userCi='" + userCi + '\'' +
               ", notifyAccessRequest=" + notifyAccessRequest +
               ", notifyNewAccess=" + notifyNewAccess +
               ", notifyPolicyChange=" + notifyPolicyChange +
               ", notifyNewDocument=" + notifyNewDocument +
               ", fcmTokenRegistered=" + isPushEnabled() +
               ", updatedAt=" + updatedAt +
               '}';
    }
}
