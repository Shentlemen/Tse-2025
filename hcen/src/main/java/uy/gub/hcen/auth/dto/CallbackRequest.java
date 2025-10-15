package uy.gub.hcen.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uy.gub.hcen.auth.config.OidcConfiguration.ClientType;

/**
 * Request DTO for OAuth 2.0 callback handling.
 *
 * For mobile clients, this comes as a POST request with the code and codeVerifier.
 * For web clients, this comes as query parameters in a GET request.
 */
public class CallbackRequest {

    @NotBlank(message = "Authorization code is required")
    private String code;

    @NotBlank(message = "State is required")
    private String state;

    @NotNull(message = "Client type is required")
    private ClientType clientType;

    @NotBlank(message = "Redirect URI is required")
    private String redirectUri;

    // PKCE code verifier (required for mobile clients)
    private String codeVerifier;

    // Constructors

    public CallbackRequest() {
    }

    public CallbackRequest(String code, String state, ClientType clientType, String redirectUri) {
        this.code = code;
        this.state = state;
        this.clientType = clientType;
        this.redirectUri = redirectUri;
    }

    // Getters and Setters

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

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

    public String getCodeVerifier() {
        return codeVerifier;
    }

    public void setCodeVerifier(String codeVerifier) {
        this.codeVerifier = codeVerifier;
    }

    @Override
    public String toString() {
        return "CallbackRequest{" +
                "hasCode=" + (code != null) +
                ", state='" + state + '\'' +
                ", clientType=" + clientType +
                ", redirectUri='" + redirectUri + '\'' +
                ", hasCodeVerifier=" + (codeVerifier != null) +
                '}';
    }
}
