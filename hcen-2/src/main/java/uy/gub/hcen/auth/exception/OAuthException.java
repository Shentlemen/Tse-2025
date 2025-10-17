package uy.gub.hcen.auth.exception;

/**
 * Exception thrown when OAuth 2.0 / OIDC operations fail.
 * This could be due to gub.uy API errors, network issues, or invalid responses.
 */
public class OAuthException extends AuthenticationException {

    private final String oauthError;
    private final String oauthErrorDescription;

    public OAuthException(String message) {
        super("OAUTH_ERROR", message);
        this.oauthError = null;
        this.oauthErrorDescription = null;
    }

    public OAuthException(String message, Throwable cause) {
        super("OAUTH_ERROR", message, cause);
        this.oauthError = null;
        this.oauthErrorDescription = null;
    }

    public OAuthException(String oauthError, String oauthErrorDescription) {
        super("OAUTH_ERROR", "OAuth error: " + oauthError + " - " + oauthErrorDescription);
        this.oauthError = oauthError;
        this.oauthErrorDescription = oauthErrorDescription;
    }

    public String getOauthError() {
        return oauthError;
    }

    public String getOauthErrorDescription() {
        return oauthErrorDescription;
    }
}
