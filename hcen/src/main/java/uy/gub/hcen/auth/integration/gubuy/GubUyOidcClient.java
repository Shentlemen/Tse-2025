package uy.gub.hcen.auth.integration.gubuy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.hcen.auth.config.OidcConfiguration;
import uy.gub.hcen.auth.config.OidcConfiguration.ClientType;
import uy.gub.hcen.auth.exception.OAuthException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * gub.uy OpenID Connect Client
 *
 * Handles OAuth 2.0 / OpenID Connect integration with Uruguay's national identity provider (ID Uruguay).
 *
 * Responsibilities:
 * - Build authorization URLs with proper parameters
 * - Exchange authorization codes for tokens
 * - Validate ID tokens (signature + claims)
 * - Fetch user information from UserInfo endpoint
 * - Manage JWKS key caching
 *
 * Features:
 * - PKCE support for mobile clients
 * - Client secret authentication for web clients
 * - JWKS key caching (1 hour TTL)
 * - Comprehensive error handling
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-15
 */
@ApplicationScoped
public class GubUyOidcClient {

    private static final Logger logger = LoggerFactory.getLogger(GubUyOidcClient.class);

    @Inject
    private OidcConfiguration oidcConfig;

    private CloseableHttpClient httpClient;
    private ObjectMapper objectMapper;

    // JWKS key cache: kid -> Key
    private final Map<String, Key> jwksKeyCache = new ConcurrentHashMap<>();
    private volatile long jwksCacheExpiryTime = 0;
    private static final long JWKS_CACHE_TTL_MS = 3600000; // 1 hour

    /**
     * Initializes HTTP client and JSON mapper
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing gub.uy OIDC client...");

        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();

        logger.info("gub.uy OIDC client initialized successfully");
    }

    /**
     * Builds authorization URL for OAuth 2.0 authorization code flow
     *
     * @param clientType Client type (MOBILE, WEB_PATIENT, WEB_ADMIN)
     * @param redirectUri Redirect URI (must match registered URI)
     * @param state CSRF protection token
     * @param codeChallenge PKCE code challenge (required for mobile, null for web)
     * @return Complete authorization URL
     */
    public String buildAuthorizationUrl(ClientType clientType, String redirectUri,
                                        String state, String codeChallenge) {
        try {
            StringBuilder url = new StringBuilder(oidcConfig.getAuthorizationEndpoint());
            url.append("?response_type=code");
            url.append("&client_id=").append(URLEncoder.encode(oidcConfig.getClientId(clientType), StandardCharsets.UTF_8));
            url.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
            url.append("&scope=").append(URLEncoder.encode(String.join(" ", oidcConfig.getScopes()), StandardCharsets.UTF_8));
            url.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));

            // Add nonce for replay attack prevention
            String nonce = UUID.randomUUID().toString();
            url.append("&nonce=").append(nonce);

            // Add ACR values (authentication context class reference) - require NID 2 or 3
            url.append("&acr_values=").append(URLEncoder.encode("urn:iduruguay:nid:2 urn:iduruguay:nid:3", StandardCharsets.UTF_8));

            // Add PKCE parameters for mobile clients
            if (clientType == ClientType.MOBILE && codeChallenge != null) {
                url.append("&code_challenge=").append(URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8));
                url.append("&code_challenge_method=S256");
            }

            String authUrl = url.toString();
            logger.debug("Built authorization URL for client type: {}", clientType);

            return authUrl;
        } catch (Exception e) {
            logger.error("Failed to build authorization URL", e);
            throw new OAuthException("Failed to build authorization URL", e);
        }
    }

    /**
     * Exchanges authorization code for tokens
     *
     * @param code Authorization code from gub.uy
     * @param redirectUri Redirect URI (must match authorization request)
     * @param codeVerifier PKCE code verifier (required for mobile, null for web)
     * @param clientType Client type
     * @return Token response from gub.uy
     * @throws OAuthException if token exchange fails
     */
    public GubUyTokenResponse exchangeCode(String code, String redirectUri,
                                            String codeVerifier, ClientType clientType) {
        HttpPost request = null;

        try {
            logger.debug("Exchanging authorization code for tokens (client type: {})", clientType);

            request = new HttpPost(oidcConfig.getTokenEndpoint());

            // Build form parameters
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "authorization_code"));
            params.add(new BasicNameValuePair("code", code));
            params.add(new BasicNameValuePair("redirect_uri", redirectUri));
            params.add(new BasicNameValuePair("client_id", oidcConfig.getClientId(clientType)));

            // Add PKCE code verifier for mobile clients
            if (clientType == ClientType.MOBILE && codeVerifier != null) {
                params.add(new BasicNameValuePair("code_verifier", codeVerifier));
                logger.debug("Added PKCE code verifier for mobile client");
            }

            // Add client secret for web clients (confidential clients)
            if (clientType != ClientType.MOBILE) {
                String clientSecret = oidcConfig.getClientSecret(clientType);
                if (clientSecret != null) {
                    params.add(new BasicNameValuePair("client_secret", clientSecret));
                    logger.debug("Added client secret for web client");
                }
            }

            request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");

            // Execute request
            return httpClient.execute(request, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode == 200) {
                    GubUyTokenResponse tokenResponse = objectMapper.readValue(responseBody, GubUyTokenResponse.class);
                    logger.info("Successfully exchanged code for tokens");
                    return tokenResponse;
                } else {
                    logger.error("Token exchange failed with status {}: {}", statusCode, responseBody);
                    throw new OAuthException("Token exchange failed: " + responseBody);
                }
            });

        } catch (IOException e) {
            logger.error("Failed to exchange authorization code", e);
            throw new OAuthException("Failed to exchange authorization code", e);
        }
    }

    /**
     * Validates ID token from gub.uy
     *
     * Performs:
     * - Signature verification using JWKS
     * - Claims validation (issuer, audience, expiration)
     * - Extracts user claims
     *
     * @param idToken ID token JWT string
     * @return Map of claims from the ID token
     * @throws OAuthException if validation fails
     */
    public Map<String, Object> validateIdToken(String idToken) {
        try {
            logger.debug("Validating ID token");

            // Parse token header to get key ID (kid)
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                throw new OAuthException("Invalid ID token format");
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            Map<String, Object> header = objectMapper.readValue(headerJson, Map.class);
            String kid = (String) header.get("kid");

            if (kid == null) {
                throw new OAuthException("ID token missing 'kid' claim in header");
            }

            // Get signing key from JWKS
            Key signingKey = getSigningKey(kid);

            // Parse and validate token
            Claims claims = Jwts.parser()
                    .verifyWith((java.security.PublicKey) signingKey)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();

            // Validate issuer
            String issuer = claims.getIssuer();
            if (!oidcConfig.getIssuer().equals(issuer)) {
                throw new OAuthException("Invalid issuer: " + issuer);
            }

            // Validate audience (should be our client ID)
            // Note: audience can be a string or list
            Object audience = claims.get("aud");
            boolean validAudience = false;

            if (audience instanceof String) {
                validAudience = oidcConfig.getClientId(ClientType.MOBILE).equals(audience) ||
                                oidcConfig.getClientId(ClientType.WEB_PATIENT).equals(audience) ||
                                oidcConfig.getClientId(ClientType.WEB_ADMIN).equals(audience);
            } else if (audience instanceof List) {
                List<String> audList = (List<String>) audience;
                validAudience = audList.contains(oidcConfig.getClientId(ClientType.MOBILE)) ||
                                audList.contains(oidcConfig.getClientId(ClientType.WEB_PATIENT)) ||
                                audList.contains(oidcConfig.getClientId(ClientType.WEB_ADMIN));
            }

            if (!validAudience) {
                throw new OAuthException("Invalid audience: " + audience);
            }

            // Validate expiration
            Date expiration = claims.getExpiration();
            if (expiration == null || expiration.before(new Date())) {
                throw new OAuthException("ID token expired");
            }

            // Convert claims to map
            Map<String, Object> claimsMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                claimsMap.put(entry.getKey(), entry.getValue());
            }

            logger.info("ID token validated successfully for subject: {}", claims.getSubject());
            return claimsMap;

        } catch (SignatureException e) {
            logger.error("ID token signature verification failed", e);
            throw new OAuthException("ID token signature verification failed", e);
        } catch (Exception e) {
            logger.error("Failed to validate ID token", e);
            throw new OAuthException("Failed to validate ID token", e);
        }
    }

    /**
     * Fetches user information from gub.uy UserInfo endpoint
     *
     * @param accessToken Access token from gub.uy
     * @return Map of user claims
     * @throws OAuthException if fetch fails
     */
    public Map<String, Object> getUserInfo(String accessToken) {
        HttpGet request = null;

        try {
            logger.debug("Fetching user info from gub.uy");

            request = new HttpGet(oidcConfig.getUserInfoEndpoint());
            request.setHeader("Authorization", "Bearer " + accessToken);

            return httpClient.execute(request, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode == 200) {
                    Map<String, Object> userInfo = objectMapper.readValue(responseBody, Map.class);
                    logger.info("Successfully fetched user info");
                    return userInfo;
                } else {
                    logger.error("UserInfo fetch failed with status {}: {}", statusCode, responseBody);
                    throw new OAuthException("UserInfo fetch failed: " + responseBody);
                }
            });

        } catch (IOException e) {
            logger.error("Failed to fetch user info", e);
            throw new OAuthException("Failed to fetch user info", e);
        }
    }

    /**
     * Gets signing key from JWKS endpoint
     *
     * Implements caching with 1-hour TTL
     *
     * @param kid Key ID
     * @return Signing key
     * @throws OAuthException if key not found
     */
    private Key getSigningKey(String kid) throws OAuthException {
        try {
            // Check cache
            if (System.currentTimeMillis() < jwksCacheExpiryTime && jwksKeyCache.containsKey(kid)) {
                logger.debug("Using cached JWKS key: {}", kid);
                return jwksKeyCache.get(kid);
            }

            // Fetch JWKS
            logger.debug("Fetching JWKS from gub.uy");
            HttpGet request = new HttpGet(oidcConfig.getJwksUri());

            return httpClient.execute(request, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode != 200) {
                    throw new OAuthException("JWKS fetch failed with status: " + statusCode);
                }

                Map<String, Object> jwks = objectMapper.readValue(responseBody, Map.class);
                List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");

                if (keys == null || keys.isEmpty()) {
                    throw new OAuthException("No keys found in JWKS");
                }

                // Find key with matching kid
                for (Map<String, Object> keyData : keys) {
                    String keyId = (String) keyData.get("kid");
                    if (kid.equals(keyId)) {
                        // Parse RSA public key
                        String n = (String) keyData.get("n"); // Modulus
                        String e = (String) keyData.get("e"); // Exponent

                        if (n == null || e == null) {
                            throw new OAuthException("Invalid key data in JWKS");
                        }

                        // Build public key (simplified - in production use a proper JWKS library)
                        // For now, we'll return a placeholder and log a warning
                        logger.warn("JWKS key parsing not fully implemented - using placeholder");

                        // Cache the key
                        Key signingKey = parseRsaPublicKey(n, e);
                        jwksKeyCache.put(kid, signingKey);
                        jwksCacheExpiryTime = System.currentTimeMillis() + JWKS_CACHE_TTL_MS;

                        logger.info("Cached JWKS key: {}", kid);
                        return signingKey;
                    }
                }

                throw new OAuthException("Key not found in JWKS: " + kid);
            });

        } catch (IOException e) {
            logger.error("Failed to fetch JWKS", e);
            throw new OAuthException("Failed to fetch JWKS", e);
        }
    }

    /**
     * Parses RSA public key from JWK components
     *
     * @param nBase64Url Modulus (base64url encoded)
     * @param eBase64Url Exponent (base64url encoded)
     * @return RSA public key
     */
    private Key parseRsaPublicKey(String nBase64Url, String eBase64Url) throws OAuthException {
        try {
            byte[] nBytes = Base64.getUrlDecoder().decode(nBase64Url);
            byte[] eBytes = Base64.getUrlDecoder().decode(eBase64Url);

            // Build RSA public key spec
            java.math.BigInteger modulus = new java.math.BigInteger(1, nBytes);
            java.math.BigInteger exponent = new java.math.BigInteger(1, eBytes);

            java.security.spec.RSAPublicKeySpec spec = new java.security.spec.RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");

            return factory.generatePublic(spec);
        } catch (Exception e) {
            logger.error("Failed to parse RSA public key", e);
            throw new OAuthException("Failed to parse RSA public key", e);
        }
    }

    /**
     * Clears JWKS key cache
     * Useful for testing and key rotation
     */
    public void clearJwksCache() {
        jwksKeyCache.clear();
        jwksCacheExpiryTime = 0;
        logger.info("JWKS cache cleared");
    }
}
