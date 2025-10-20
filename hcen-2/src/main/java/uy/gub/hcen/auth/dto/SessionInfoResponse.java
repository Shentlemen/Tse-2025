package uy.gub.hcen.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Response DTO for session information.
 */
public class SessionInfoResponse {

    private UserInfoDTO user;
    private SessionDetails session;

    // Constructors

    public SessionInfoResponse() {
    }

    public SessionInfoResponse(UserInfoDTO user, SessionDetails session) {
        this.user = user;
        this.session = session;
    }

    // Getters and Setters

    public UserInfoDTO getUser() {
        return user;
    }

    public void setUser(UserInfoDTO user) {
        this.user = user;
    }

    public SessionDetails getSession() {
        return session;
    }

    public void setSession(SessionDetails session) {
        this.session = session;
    }

    /**
     * Nested class for session details
     */
    public static class SessionDetails {

        @JsonProperty("authenticated_at")
        private LocalDateTime authenticatedAt;

        @JsonProperty("expires_at")
        private LocalDateTime expiresAt;

        @JsonProperty("remaining_seconds")
        private long remainingSeconds;

        // Constructors

        public SessionDetails() {
        }

        public SessionDetails(LocalDateTime authenticatedAt, LocalDateTime expiresAt, long remainingSeconds) {
            this.authenticatedAt = authenticatedAt;
            this.expiresAt = expiresAt;
            this.remainingSeconds = remainingSeconds;
        }

        // Getters and Setters

        public LocalDateTime getAuthenticatedAt() {
            return authenticatedAt;
        }

        public void setAuthenticatedAt(LocalDateTime authenticatedAt) {
            this.authenticatedAt = authenticatedAt;
        }

        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
        }

        public long getRemainingSeconds() {
            return remainingSeconds;
        }

        public void setRemainingSeconds(long remainingSeconds) {
            this.remainingSeconds = remainingSeconds;
        }
    }

    @Override
    public String toString() {
        return "SessionInfoResponse{" +
                "user=" + user +
                ", session=" + session +
                '}';
    }
}
