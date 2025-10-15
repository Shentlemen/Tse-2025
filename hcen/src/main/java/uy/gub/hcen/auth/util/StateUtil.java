package uy.gub.hcen.auth.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for generating and validating OAuth state parameters.
 *
 * The state parameter is used for CSRF protection in OAuth 2.0 flows.
 */
public class StateUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int STATE_LENGTH = 16; // 16 bytes = 128 bits

    /**
     * Generates a cryptographically random state parameter.
     *
     * @return Random state string (base64url encoded)
     */
    public static String generateState() {
        byte[] randomBytes = new byte[STATE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    /**
     * Generates a cryptographically random nonce for OIDC.
     *
     * @return Random nonce string (base64url encoded)
     */
    public static String generateNonce() {
        return generateState(); // Same generation logic
    }
}
