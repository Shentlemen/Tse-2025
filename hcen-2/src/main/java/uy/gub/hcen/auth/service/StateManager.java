package uy.gub.hcen.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import uy.gub.hcen.auth.config.OidcConfiguration.ClientType;
import uy.gub.hcen.auth.config.RedisConfiguration;
import uy.gub.hcen.auth.exception.InvalidStateException;
import uy.gub.hcen.auth.util.StateUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages OAuth state parameters using Redis for storage.
 *
 * The state parameter is used for CSRF protection in OAuth 2.0 flows.
 * It is stored in Redis with a short TTL (5 minutes) and deleted after use.
 */
@ApplicationScoped
public class StateManager {

    private static final Logger LOGGER = Logger.getLogger(StateManager.class.getName());

    @Inject
    private JedisPool jedisPool;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a new OAuth state and stores it in Redis.
     *
     * @param clientType The client type initiating the flow
     * @param redirectUri The redirect URI
     * @param codeChallenge PKCE code challenge (for mobile clients)
     * @return The generated state string
     */
    public String createState(ClientType clientType, String redirectUri, String codeChallenge) {
        String state = StateUtil.generateState();

        Map<String, Object> stateData = new HashMap<>();
        stateData.put("clientType", clientType.name());
        stateData.put("redirectUri", redirectUri);
        stateData.put("createdAt", System.currentTimeMillis());

        if (codeChallenge != null) {
            stateData.put("codeChallenge", codeChallenge);
        }

        try (Jedis jedis = jedisPool.getResource()) {
            String key = RedisConfiguration.KeyPatterns.oauthState(state);
            String value = objectMapper.writeValueAsString(stateData);

            jedis.setex(key, RedisConfiguration.TTL.OAUTH_STATE, value);

            LOGGER.info("Created OAuth state for " + clientType + ": " + state);
            return state;

        } catch (Exception e) {
            LOGGER.severe("Failed to create OAuth state: " + e.getMessage());
            throw new RuntimeException("Failed to create OAuth state", e);
        }
    }

    /**
     * Validates an OAuth state and retrieves the associated data.
     *
     * @param state The state parameter to validate
     * @return State data if valid
     * @throws InvalidStateException if state is invalid or expired
     */
    public Map<String, Object> validateAndConsumeState(String state) {
        if (state == null || state.trim().isEmpty()) {
            throw new InvalidStateException("State parameter is missing");
        }

        try (Jedis jedis = jedisPool.getResource()) {
            String key = RedisConfiguration.KeyPatterns.oauthState(state);
            String value = jedis.get(key);

            if (value == null) {
                LOGGER.warning("Invalid or expired state: " + state);
                throw new InvalidStateException("Invalid or expired state parameter");
            }

            // Delete state after retrieval (single use)
            jedis.del(key);

            @SuppressWarnings("unchecked")
            Map<String, Object> stateData = objectMapper.readValue(value, Map.class);

            LOGGER.info("Validated and consumed OAuth state: " + state);
            return stateData;

        } catch (InvalidStateException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.severe("Failed to validate state: " + e.getMessage());
            throw new InvalidStateException("State validation failed", e);
        }
    }

    /**
     * Checks if a state exists without consuming it.
     *
     * @param state The state to check
     * @return true if state exists, false otherwise
     */
    public boolean stateExists(String state) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = RedisConfiguration.KeyPatterns.oauthState(state);
            return jedis.exists(key);
        }
    }

    /**
     * Deletes a state from Redis (in case of error).
     *
     * @param state The state to delete
     */
    public void deleteState(String state) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = RedisConfiguration.KeyPatterns.oauthState(state);
            jedis.del(key);
            LOGGER.info("Deleted OAuth state: " + state);
        } catch (Exception e) {
            LOGGER.warning("Failed to delete state: " + e.getMessage());
        }
    }
}
