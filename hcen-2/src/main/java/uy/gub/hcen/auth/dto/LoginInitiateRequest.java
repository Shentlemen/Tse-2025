package uy.gub.hcen.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uy.gub.hcen.auth.config.OidcConfiguration.ClientType;

/**
 * Request DTO for initiating OAuth 2.0 authorization flow.
 *
 * For mobile clients, codeChallenge and codeChallengeMethod are required (PKCE).
 * For web clients, these fields are not used.
 */
public class LoginInitiateRequest {

    @NotNull(message = "Client type is required")
    private ClientType clientType;

    @NotBlank(message = "Redirect URI is required")
    private String redirectUri;

    // PKCE parameters (required for mobile clients)
    private String codeChallenge;
    private String codeChallengeMethod;  // Should be "S256"

    // Optional client-provided state (for additional CSRF protection)
    private String state;

    // Constructors

    public LoginInitiateRequest() {
    }

    public LoginInitiateRequest(ClientType clientType, String redirectUri) {
        this.clientType = clientType;
        this.redirectUri = redirectUri;
    }

    // Getters and Setters

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public void setCodeChallenge(String codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public void setCodeChallengeMethod(String codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "LoginInitiateRequest{" +
                "clientType=" + clientType +
                ", redirectUri='" + redirectUri + '\'' +
                ", codeChallengeMethod='" + codeChallengeMethod + '\'' +
                ", hasCodeChallenge=" + (codeChallenge != null) +
                ", hasState=" + (state != null) +
                '}';
    }
}
