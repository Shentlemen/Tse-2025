package uy.gub.hcen.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OAuth State Manager - Redis OAuth State Storage
 * <p>
 * Manages OAuth state tokens in Redis Database 2 for CSRF protection
 * during gub.uy OIDC authentication flow.
 * <p>
 * Key Pattern: oauth:state:{state_token}
 * Value: JSON with nonce, redirect_uri, client_type
 * TTL: 600 seconds (10 minutes)
 * <p>
 * Usage:
 * - Generate and store state before redirecting to gub.uy authorization endpoint
 * - Validate state when receiving OAuth callback
 * - Consume (get and delete) state atomically to prevent replay attacks
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@Stateless
public class OAuthStateManager {

    private static final Logger LOGGER = Logger.getLogger(OAuthStateManager.class.getName());
    private static final int OAUTH_DB = 2; // Redis database 2
    private static final int STATE_TTL = 600; // 10 minutes in seconds
    private static final String KEY_PREFIX = "oauth:state:";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    private JedisPool jedisPool;

    /**
     * Store OAuth state with associated metadata.
     *
     * @param stateToken  Cryptographically random state token (UUID recommended)
     * @param nonce       Nonce for replay attack protection
     * @param redirectUri Redirect URI after authentication
     * @param clientType  Client type (mobile/web_patient/web_admin)
     * @throws IllegalArgumentException if any parameter is null/empty
     * @throws RuntimeException         if Redis operation fails
     */
    public void storeState(String stateToken, String nonce, String redirectUri, String clientType) {
        if (stateToken == null || stateToken.trim().isEmpty()) {
            throw new IllegalArgumentException("State token cannot be null or empty");
        }

        if (nonce == null || nonce.trim().isEmpty()) {
            throw new IllegalArgumentException("Nonce cannot be null or empty");
        }

        if (redirectUri == null || redirectUri.trim().isEmpty()) {
            throw new IllegalArgumentException("Redirect URI cannot be null or empty");
        }

        if (clientType == null || clientType.trim().isEmpty()) {
            throw new IllegalArgumentException("Client type cannot be null or empty");
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(OAUTH_DB);
            String key = KEY_PREFIX + stateToken;

            // Build state metadata
            Map<String, String> stateData = new HashMap<>();
            stateData.put("nonce", nonce);
            stateData.put("redirectUri", redirectUri);
            stateData.put("clientType", clientType);
            stateData.put("createdAt", String.valueOf(System.currentTimeMillis()));

            // Serialize to JSON
            String jsonValue = objectMapper.writeValueAsString(stateData);

            // Store with TTL (SETEX is atomic)
            jedis.setex(key, STATE_TTL, jsonValue);

            LOGGER.log(Level.INFO, "Stored OAuth state: {0}, client type: {1}, expires in {2} seconds",
                    new Object[]{stateToken, clientType, STATE_TTL});

        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Failed to serialize OAuth state data for token: " + stateToken, e);
            throw new RuntimeException("OAuth state serialization failed", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to store OAuth state for token: " + stateToken, e);
            throw new RuntimeException("OAuth state storage failed", e);
        }
    }

    /**
     * Retrieve OAuth state metadata.
     *
     * @param stateToken OAuth state token
     * @return Optional containing state metadata map (nonce, redirectUri, clientType, createdAt)
     */
    public Optional<Map<String, String>> getState(String stateToken) {
        if (stateToken == null || stateToken.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to get state with null or empty token");
            return Optional.empty();
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(OAUTH_DB);
            String key = KEY_PREFIX + stateToken;

            String jsonValue = jedis.get(key);

            if (jsonValue == null) {
                LOGGER.log(Level.WARNING, "No OAuth state found for token: {0} (expired or invalid)", stateToken);
                return Optional.empty();
            }

            // Deserialize JSON to Map
            Map<String, String> stateData = objectMapper.readValue(jsonValue,
                    new TypeReference<Map<String, String>>() {
                    });

            LOGGER.log(Level.FINE, "Retrieved OAuth state for token: {0}", stateToken);
            return Optional.of(stateData);

        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Failed to deserialize OAuth state data for token: " + stateToken, e);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve OAuth state for token: " + stateToken, e);
            return Optional.empty();
        }
    }

    /**
     * Consume OAuth state (get and delete atomically).
     * This prevents replay attacks by ensuring each state can only be used once.
     *
     * @param stateToken OAuth state token
     * @return Optional containing state metadata if found, empty otherwise
     */
    public Optional<Map<String, String>> consumeState(String stateToken) {
        if (stateToken == null || stateToken.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to consume state with null or empty token");
            return Optional.empty();
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(OAUTH_DB);
            String key = KEY_PREFIX + stateToken;

            // Get the value
            String jsonValue = jedis.get(key);

            if (jsonValue == null) {
                LOGGER.log(Level.WARNING, "No OAuth state found to consume for token: {0} (expired, invalid, or already used)",
                        stateToken);
                return Optional.empty();
            }

            // Delete the key immediately (consume)
            jedis.del(key);

            // Deserialize JSON to Map
            Map<String, String> stateData = objectMapper.readValue(jsonValue,
                    new TypeReference<Map<String, String>>() {
                    });

            LOGGER.log(Level.INFO, "Consumed OAuth state for token: {0}", stateToken);
            return Optional.of(stateData);

        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Failed to deserialize OAuth state data for token: " + stateToken, e);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to consume OAuth state for token: " + stateToken, e);
            return Optional.empty();
        }
    }

    /**
     * Check if OAuth state exists.
     *
     * @param stateToken OAuth state token
     * @return true if state exists, false otherwise
     */
    public boolean stateExists(String stateToken) {
        if (stateToken == null || stateToken.trim().isEmpty()) {
            return false;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(OAUTH_DB);
            String key = KEY_PREFIX + stateToken;

            boolean exists = jedis.exists(key);
            LOGGER.log(Level.FINE, "OAuth state exists check for token {0}: {1}",
                    new Object[]{stateToken, exists});

            return exists;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to check OAuth state existence for token: " + stateToken, e);
            return false;
        }
    }

    /**
     * Invalidate OAuth state (delete without retrieving).
     *
     * @param stateToken OAuth state token
     * @return true if state was deleted, false if it didn't exist
     */
    public boolean invalidateState(String stateToken) {
        if (stateToken == null || stateToken.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to invalidate state with null or empty token");
            return false;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(OAUTH_DB);
            String key = KEY_PREFIX + stateToken;

            Long deleted = jedis.del(key);
            boolean wasDeleted = deleted > 0;

            if (wasDeleted) {
                LOGGER.log(Level.INFO, "Invalidated OAuth state for token: {0}", stateToken);
            } else {
                LOGGER.log(Level.FINE, "No OAuth state found to invalidate for token: {0}", stateToken);
            }

            return wasDeleted;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to invalidate OAuth state for token: " + stateToken, e);
            throw new RuntimeException("OAuth state invalidation failed", e);
        }
    }
}
