package uy.gub.hcen.auth.dto;

/**
 * Response DTO for login initiation.
 *
 * Contains the authorization URL that the client should redirect to,
 * along with the state parameter for CSRF protection.
 */
public class LoginInitiateResponse {

    private String authorizationUrl;
    private String state;
    private int expiresIn;  // seconds until state expires

    // Constructors

    public LoginInitiateResponse() {
    }

    public LoginInitiateResponse(String authorizationUrl, String state, int expiresIn) {
        this.authorizationUrl = authorizationUrl;
        this.state = state;
        this.expiresIn = expiresIn;
    }

    // Getters and Setters

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    @Override
    public String toString() {
        return "LoginInitiateResponse{" +
                "authorizationUrl='" + authorizationUrl + '\'' +
                ", state='" + state + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}
