package uy.gub.hcen.auth.exception;

/**
 * Exception thrown when a token is invalid, expired, or malformed.
 */
public class InvalidTokenException extends AuthenticationException {

    public InvalidTokenException(String message) {
        super("INVALID_TOKEN", message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super("INVALID_TOKEN", message, cause);
    }
}
