package uy.gub.hcen.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import uy.gub.hcen.config.qualifier.SessionPool;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Session Manager - Redis Session Storage Service
 * <p>
 * Manages user session storage in Redis Database 0.
 * Sessions store JWT token claims for quick lookup without database access.
 * <p>
 * Key Pattern: session:{jwt_token_id}
 * Value: JSON with user claims (ci, name, role, specialties, clinicId)
 * TTL: 3600 seconds (1 hour)
 * <p>
 * Usage:
 * - Store session after successful authentication
 * - Validate session on protected API calls
 * - Invalidate session on logout
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@Stateless
public class SessionManager {

    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());
    private static final int SESSION_DB = 0; // Redis database 0
    private static final int SESSION_TTL = 3600; // 1 hour in seconds
    private static final String KEY_PREFIX = "session:";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    @SessionPool
    private JedisPool jedisPool;

    /**
     * Store user session in Redis.
     *
     * @param tokenId JWT token ID (jti claim)
     * @param claims  User claims map (ci, name, role, specialties, clinicId)
     * @throws IllegalArgumentException if tokenId or claims are null/empty
     * @throws RuntimeException         if Redis operation fails
     */
    public void storeSession(String tokenId, Map<String, Object> claims) {
        if (tokenId == null || tokenId.trim().isEmpty()) {
            throw new IllegalArgumentException("Token ID cannot be null or empty");
        }

        if (claims == null || claims.isEmpty()) {
            throw new IllegalArgumentException("Claims cannot be null or empty");
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(SESSION_DB);
            String key = KEY_PREFIX + tokenId;

            // Convert claims to JSON
            String jsonValue = objectMapper.writeValueAsString(claims);

            // Store with TTL (SETEX is atomic)
            jedis.setex(key, SESSION_TTL, jsonValue);

            LOGGER.log(Level.INFO, "Stored session for token: {0}, expires in {1} seconds",
                    new Object[]{tokenId, SESSION_TTL});

        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Failed to serialize claims to JSON for token: " + tokenId, e);
            throw new RuntimeException("Session serialization failed", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to store session for token: " + tokenId, e);
            throw new RuntimeException("Session storage failed", e);
        }
    }

    /**
     * Retrieve user session from Redis.
     *
     * @param tokenId JWT token ID (jti claim)
     * @return Optional containing claims map if session exists, empty otherwise
     */
    public Optional<Map<String, Object>> getSession(String tokenId) {
        if (tokenId == null || tokenId.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to get session with null or empty token ID");
            return Optional.empty();
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(SESSION_DB);
            String key = KEY_PREFIX + tokenId;

            String jsonValue = jedis.get(key);

            if (jsonValue == null) {
                LOGGER.log(Level.FINE, "No session found for token: {0}", tokenId);
                return Optional.empty();
            }

            // Deserialize JSON to Map
            Map<String, Object> claims = objectMapper.readValue(jsonValue,
                    new TypeReference<Map<String, Object>>() {
                    });

            LOGGER.log(Level.FINE, "Retrieved session for token: {0}", tokenId);
            return Optional.of(claims);

        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Failed to deserialize claims from JSON for token: " + tokenId, e);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve session for token: " + tokenId, e);
            return Optional.empty();
        }
    }

    /**
     * Invalidate user session (logout).
     *
     * @param tokenId JWT token ID (jti claim)
     * @return true if session was deleted, false if session didn't exist
     */
    public boolean invalidateSession(String tokenId) {
        if (tokenId == null || tokenId.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to invalidate session with null or empty token ID");
            return false;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(SESSION_DB);
            String key = KEY_PREFIX + tokenId;

            Long deleted = jedis.del(key);
            boolean wasDeleted = deleted > 0;

            if (wasDeleted) {
                LOGGER.log(Level.INFO, "Invalidated session for token: {0}", tokenId);
            } else {
                LOGGER.log(Level.FINE, "No session found to invalidate for token: {0}", tokenId);
            }

            return wasDeleted;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to invalidate session for token: " + tokenId, e);
            throw new RuntimeException("Session invalidation failed", e);
        }
    }

    /**
     * Check if session exists for given token ID.
     *
     * @param tokenId JWT token ID (jti claim)
     * @return true if session exists, false otherwise
     */
    public boolean sessionExists(String tokenId) {
        if (tokenId == null || tokenId.trim().isEmpty()) {
            return false;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(SESSION_DB);
            String key = KEY_PREFIX + tokenId;

            boolean exists = jedis.exists(key);
            LOGGER.log(Level.FINE, "Session exists check for token {0}: {1}",
                    new Object[]{tokenId, exists});

            return exists;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to check session existence for token: " + tokenId, e);
            return false;
        }
    }

    /**
     * Extend session TTL (refresh session timeout).
     *
     * @param tokenId JWT token ID (jti claim)
     * @return true if session TTL was extended, false if session doesn't exist
     */
    public boolean extendSession(String tokenId) {
        if (tokenId == null || tokenId.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to extend session with null or empty token ID");
            return false;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(SESSION_DB);
            String key = KEY_PREFIX + tokenId;

            Long result = jedis.expire(key, SESSION_TTL);
            boolean extended = result > 0;

            if (extended) {
                LOGGER.log(Level.FINE, "Extended session TTL for token: {0}", tokenId);
            } else {
                LOGGER.log(Level.FINE, "No session found to extend for token: {0}", tokenId);
            }

            return extended;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to extend session for token: " + tokenId, e);
            return false;
        }
    }
}
