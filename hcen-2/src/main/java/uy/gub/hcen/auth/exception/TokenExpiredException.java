package uy.gub.hcen.auth.exception;

/**
 * Exception thrown when a token has expired.
 */
public class TokenExpiredException extends InvalidTokenException {

    public TokenExpiredException(String message) {
        super(message);
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
