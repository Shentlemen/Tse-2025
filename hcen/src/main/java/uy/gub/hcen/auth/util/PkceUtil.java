package uy.gub.hcen.auth.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for PKCE (Proof Key for Code Exchange) operations.
 *
 * PKCE is required for mobile applications to prevent authorization code
 * interception attacks. Implements RFC 7636.
 */
public class PkceUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int CODE_VERIFIER_LENGTH = 43; // Minimum length for PKCE

    /**
     * Validates that a code challenge matches the code verifier.
     *
     * @param codeVerifier The code verifier (plain text)
     * @param codeChallenge The code challenge (SHA-256 hash, base64url encoded)
     * @return true if they match, false otherwise
     */
    public static boolean validateCodeChallenge(String codeVerifier, String codeChallenge) {
        if (codeVerifier == null || codeChallenge == null) {
            return false;
        }

        try {
            String computedChallenge = generateCodeChallenge(codeVerifier);
            return computedChallenge.equals(codeChallenge);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generates a code challenge from a code verifier using SHA-256.
     *
     * @param codeVerifier The code verifier
     * @return Base64URL-encoded SHA-256 hash of the code verifier
     * @throws NoSuchAlgorithmException if SHA-256 is not available
     */
    public static String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return base64UrlEncode(hash);
    }

    /**
     * Generates a cryptographically random code verifier.
     *
     * @return Base64URL-encoded random string
     */
    public static String generateCodeVerifier() {
        byte[] randomBytes = new byte[32]; // 32 bytes = 256 bits
        SECURE_RANDOM.nextBytes(randomBytes);
        return base64UrlEncode(randomBytes);
    }

    /**
     * Base64URL encoding without padding (as per RFC 7636).
     *
     * @param data The data to encode
     * @return Base64URL-encoded string
     */
    private static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(data);
    }

    /**
     * Validates code verifier format (43-128 characters, unreserved chars only).
     *
     * @param codeVerifier The code verifier to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCodeVerifier(String codeVerifier) {
        if (codeVerifier == null) {
            return false;
        }

        int length = codeVerifier.length();
        if (length < 43 || length > 128) {
            return false;
        }

        // Should only contain unreserved characters: [A-Z] / [a-z] / [0-9] / "-" / "." / "_" / "~"
        return codeVerifier.matches("[A-Za-z0-9\\-._~]+");
    }
}
