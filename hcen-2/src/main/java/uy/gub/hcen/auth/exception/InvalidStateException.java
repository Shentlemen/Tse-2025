package uy.gub.hcen.auth.exception;

/**
 * Exception thrown when OAuth state parameter validation fails.
 * This indicates a potential CSRF attack or expired state.
 */
public class InvalidStateException extends AuthenticationException {

    public InvalidStateException(String message) {
        super("INVALID_STATE", message);
    }

    public InvalidStateException(String message, Throwable cause) {
        super("INVALID_STATE", message, cause);
    }
}
