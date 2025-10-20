package uy.gub.hcen.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for refreshing an expired access token.
 */
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh token is required")
    @JsonProperty("refresh_token")
    private String refreshToken;

    // Constructors

    public TokenRefreshRequest() {
    }

    public TokenRefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getters and Setters

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "TokenRefreshRequest{hasRefreshToken=" + (refreshToken != null) + '}';
    }
}
