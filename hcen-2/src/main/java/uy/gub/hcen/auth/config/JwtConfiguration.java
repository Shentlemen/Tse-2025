package uy.gub.hcen.auth.config;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Configuration for HCEN-issued JWT tokens.
 * Supports both HS256 (symmetric) and RS256 (asymmetric) signing algorithms.
 *
 * For production, RS256 is recommended as it allows token validation
 * without sharing the signing key.
 */
@ApplicationScoped
public class JwtConfiguration {

    private static final Logger LOGGER = Logger.getLogger(JwtConfiguration.class.getName());

    private Properties properties;

    // JWT settings
    private String issuer;
    private int accessTokenTtl;    // seconds
    private int refreshTokenTtl;   // seconds
    private SignatureAlgorithm algorithm;
    private Key signingKey;

    @PostConstruct
    public void init() {
        try {
            properties = loadProperties();
            loadJwtSettings();
            LOGGER.info("JWT configuration initialized successfully with algorithm: " + algorithm);
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize JWT configuration: " + e.getMessage());
            throw new RuntimeException("JWT configuration initialization failed", e);
        }
    }

    private void loadJwtSettings() throws Exception {
        issuer = getRequiredProperty("jwt.issuer");
        accessTokenTtl = Integer.parseInt(getRequiredProperty("jwt.access.token.ttl"));
        refreshTokenTtl = Integer.parseInt(getRequiredProperty("jwt.refresh.token.ttl"));

        String algorithmStr = getRequiredProperty("jwt.algorithm");
        algorithm = SignatureAlgorithm.valueOf(algorithmStr);

        // Load signing key based on algorithm
        if (algorithm == SignatureAlgorithm.HS256) {
            loadHmacKey();
        } else if (algorithm == SignatureAlgorithm.RS256) {
            loadRsaKey();
        } else {
            throw new IllegalStateException("Unsupported JWT algorithm: " + algorithm);
        }
    }

    private void loadHmacKey() {
        String secret = getRequiredProperty("jwt.secret");

        // Ensure the secret is at least 256 bits (32 bytes) for HS256
        if (secret.length() < 32) {
            throw new IllegalStateException(
                "JWT secret must be at least 32 characters for HS256");
        }

        signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        LOGGER.info("HMAC signing key loaded successfully");
    }

    private void loadRsaKey() throws Exception {
        String keyPath = getRequiredProperty("jwt.signing.key.path");

        try {
            // Read private key file
            byte[] keyBytes = Files.readAllBytes(Paths.get(keyPath));
            String keyContent = new String(keyBytes, StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

            byte[] decodedKey = Base64.getDecoder().decode(keyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            signingKey = keyFactory.generatePrivate(keySpec);

            LOGGER.info("RSA private key loaded successfully from: " + keyPath);
        } catch (IOException e) {
            LOGGER.severe("Failed to load RSA private key from: " + keyPath);
            LOGGER.info("Generating temporary RSA key pair for development");

            // For development, generate a temporary key pair
            var keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
            signingKey = keyPair.getPrivate();

            LOGGER.warning("Using temporary RSA key - NOT SUITABLE FOR PRODUCTION");
        }
    }

    private String getRequiredProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required property '" + key + "' is not set");
        }
        return value.trim();
    }

    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IOException("application.properties not found in classpath");
            }
            props.load(input);
        }
        return props;
    }

    // Getters

    public String getIssuer() {
        return issuer;
    }

    public int getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public int getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public SignatureAlgorithm getAlgorithm() {
        return algorithm;
    }

    public Key getSigningKey() {
        return signingKey;
    }

    /**
     * Get access token TTL in milliseconds
     */
    public long getAccessTokenTtlMillis() {
        return accessTokenTtl * 1000L;
    }

    /**
     * Get refresh token TTL in milliseconds
     */
    public long getRefreshTokenTtlMillis() {
        return refreshTokenTtl * 1000L;
    }
}
