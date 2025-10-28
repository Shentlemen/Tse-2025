package uy.gub.hcen.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import uy.gub.hcen.config.RedisConfiguration;
import uy.gub.hcen.config.qualifier.SessionPool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Session Manager for storing and retrieving JWT session data in Redis.
 *
 * Sessions are stored in Redis Database 0 with the following characteristics:
 * - Key pattern: session:{jwt_token_id}
 * - Value: JSON with user claims
 * - TTL: Configurable (default 3600 seconds / 1 hour)
 *
 * This enables stateless horizontal scalability while maintaining session state
 * in a distributed cache.
 *
 * @author TSE 2025 - Group 9
 */
@ApplicationScoped
public class SessionManager {

    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());

    @Inject
    @SessionPool
    private JedisPool sessionPool;

    private final ObjectMapper objectMapper;

    public SessionManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Creates a new session in Redis.
     *
     * @param jwtTokenId Unique identifier from JWT (jti claim)
     * @param claims Map of user claims to store
     * @param ttlSeconds Time-to-live in seconds
     * @return true if session was created successfully
     */
    public boolean createSession(String jwtTokenId, Map<String, Object> claims, int ttlSeconds) {
        if (jwtTokenId == null || jwtTokenId.trim().isEmpty()) {
            LOGGER.warning("Cannot create session: jwtTokenId is null or empty");
            return false;
        }

        if (claims == null || claims.isEmpty()) {
            LOGGER.warning("Cannot create session: claims are null or empty");
            return false;
        }

        try (Jedis jedis = sessionPool.getResource()) {
            String key = RedisConfiguration.KeyPatterns.session(jwtTokenId);
            String value = objectMapper.writeValueAsString(claims);

            String result = jedis.setex(key, ttlSeconds, value);

            if ("OK".equals(result)) {
                LOGGER.fine("Session created for token: " + jwtTokenId + " with TTL: " + ttlSeconds + "s");
                return true;
            } else {
                LOGGER.warning("Failed to create session for token: " + jwtTokenId + ", result: " + result);
                return false;
            }

        } catch (JsonProcessingException e) {
            LOGGER.severe("Failed to serialize session claims for token " + jwtTokenId + ": " + e.getMessage());
            return false;
        } catch (Exception e) {
            LOGGER.severe("Failed to create session in Redis for token " + jwtTokenId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates a session with default TTL (1 hour).
     *
     * @param jwtTokenId Unique identifier from JWT (jti claim)
     * @param claims Map of user claims to store
     * @return true if session was created successfully
     */
    public boolean createSession(String jwtTokenId, Map<String, Object> claims) {
        return createSession(jwtTokenId, claims, RedisConfiguration.TTL.SESSION);
    }

    /**
     * Retrieves a session from Redis.
     *
     * @param jwtTokenId Unique identifier from JWT (jti claim)
     * @return Map of user claims, or null if session not found or expired
     */
    public Map<String, Object> getSession(String jwtTokenId) {
        if (jwtTokenId == null || jwtTokenId.trim().isEmpty()) {
            LOGGER.warning("Cannot get session: jwtTokenId is null or empty");
            return null;
        }

        try (Jedis jedis = sessionPool.getResource()) {
            String key = RedisConfiguration.KeyPatterns.session(jwtTokenId);
            String value = jedis.get(key);

            if (value == null) {
                LOGGER.fine("Session not found or expired for token: " + jwtTokenId);
                return null;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> claims = objectMapper.readValue(value, Map.class);

            LOGGER.fine("Session retrieved for token: " + jwtTokenId);
            return claims;

        } catch (IOException e) {
            LOGGER.severe("Failed to deserialize session for token " + jwtTokenId + ": " + e.getMessage());
            return null;
        } catch (Exception e) {
            LOGGER.severe("Failed to retrieve session from Redis for token " + jwtTokenId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Invalidates a session (logout).
     *
     * @param jwtTokenId Unique identifier from JWT (jti claim)
     * @return true if session was invalidated successfully
     */
    public boolean invalidateSession(String jwtTokenId) {
        if (jwtTokenId == null || jwtTokenId.trim().isEmpty()) {
            LOGGER.warning("Cannot invalidate session: jwtTokenId is null or empty");
            return false;
        }

        try (Jedis jedis = sessionPool.getResource()) {
            String key = RedisConfiguration.KeyPatterns.session(jwtTokenId);
            Long deleted = jedis.del(key);

            if (deleted > 0) {
                LOGGER.info("Session invalidated for token: " + jwtTokenId);
                return true;
            } else {
                LOGGER.warning("Session not found for token: " + jwtTokenId);
                return false;
            }

        } catch (Exception e) {
            LOGGER.severe("Failed to invalidate session in Redis for token " + jwtTokenId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Extends the TTL of an existing session.
     *
     * @param jwtTokenId Unique identifier from JWT (jti claim)
     * @param ttlSeconds New time-to-live in seconds
     * @return true if TTL was extended successfully
     */
    public boolean extendSession(String jwtTokenId, int ttlSeconds) {
        if (jwtTokenId == null || jwtTokenId.trim().isEmpty()) {
            LOGGER.warning("Cannot extend session: jwtTokenId is null or empty");
            return false;
        }

        try (Jedis jedis = sessionPool.getResource()) {
            String key = RedisConfiguration.KeyPatterns.session(jwtTokenId);

            // Check if session exists
            if (!jedis.exists(key)) {
                LOGGER.warning("Cannot extend session: session not found for token: " + jwtTokenId);
                return false;
            }

            Long result = jedis.expire(key, ttlSeconds);

            if (result == 1) {
                LOGGER.fine("Session TTL extended for token: " + jwtTokenId + " to " + ttlSeconds + "s");
                return true;
            } else {
                LOGGER.warning("Failed to extend session TTL for token: " + jwtTokenId);
                return false;
            }

        } catch (Exception e) {
            LOGGER.severe("Failed to extend session TTL in Redis for token " + jwtTokenId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a session exists.
     *
     * @param jwtTokenId Unique identifier from JWT (jti claim)
     * @return true if session exists
     */
    public boolean sessionExists(String jwtTokenId) {
        if (jwtTokenId == null || jwtTokenId.trim().isEmpty()) {
            return false;
        }

        try (Jedis jedis = sessionPool.getResource()) {
            String key = RedisConfiguration.KeyPatterns.session(jwtTokenId);
            return jedis.exists(key);
        } catch (Exception e) {
            LOGGER.severe("Failed to check session existence in Redis for token " + jwtTokenId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the remaining TTL of a session.
     *
     * @param jwtTokenId Unique identifier from JWT (jti claim)
     * @return Remaining TTL in seconds, or -1 if session not found, -2 if no TTL set
     */
    public long getSessionTTL(String jwtTokenId) {
        if (jwtTokenId == null || jwtTokenId.trim().isEmpty()) {
            return -1;
        }

        try (Jedis jedis = sessionPool.getResource()) {
            String key = RedisConfiguration.KeyPatterns.session(jwtTokenId);
            return jedis.ttl(key);
        } catch (Exception e) {
            LOGGER.severe("Failed to get session TTL from Redis for token " + jwtTokenId + ": " + e.getMessage());
            return -1;
        }
    }

    /**
     * Updates session claims without changing TTL.
     *
     * @param jwtTokenId Unique identifier from JWT (jti claim)
     * @param claims New claims to store
     * @return true if session was updated successfully
     */
    public boolean updateSession(String jwtTokenId, Map<String, Object> claims) {
        if (jwtTokenId == null || jwtTokenId.trim().isEmpty()) {
            LOGGER.warning("Cannot update session: jwtTokenId is null or empty");
            return false;
        }

        try (Jedis jedis = sessionPool.getResource()) {
            String key = RedisConfiguration.KeyPatterns.session(jwtTokenId);

            // Get current TTL
            long ttl = jedis.ttl(key);
            if (ttl <= 0) {
                LOGGER.warning("Cannot update session: session not found or expired for token: " + jwtTokenId);
                return false;
            }

            String value = objectMapper.writeValueAsString(claims);
            String result = jedis.setex(key, (int) ttl, value);

            if ("OK".equals(result)) {
                LOGGER.fine("Session updated for token: " + jwtTokenId);
                return true;
            } else {
                LOGGER.warning("Failed to update session for token: " + jwtTokenId);
                return false;
            }

        } catch (JsonProcessingException e) {
            LOGGER.severe("Failed to serialize session claims for token " + jwtTokenId + ": " + e.getMessage());
            return false;
        } catch (Exception e) {
            LOGGER.severe("Failed to update session in Redis for token " + jwtTokenId + ": " + e.getMessage());
            return false;
        }
    }
}
