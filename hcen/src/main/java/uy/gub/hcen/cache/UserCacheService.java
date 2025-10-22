package uy.gub.hcen.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import uy.gub.hcen.config.qualifier.CachePool;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User Cache Service - Redis User Profile Cache
 * <p>
 * Caches user profiles from INUS in Redis Database 1 to reduce
 * database load for frequently accessed user information.
 * <p>
 * Key Pattern: user:profile:{ci}
 * Value: JSON user profile from INUS (InusUser entity serialized)
 * TTL: 900 seconds (15 minutes)
 * <p>
 * Usage:
 * - Cache user profiles after INUS lookup
 * - Check cache before querying INUS database
 * - Invalidate cache when user profile is updated
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@Stateless
public class UserCacheService {

    private static final Logger LOGGER = Logger.getLogger(UserCacheService.class.getName());
    private static final int USER_DB = 1; // Redis database 1 (same as policy cache)
    private static final int USER_TTL = 900; // 15 minutes in seconds
    private static final String KEY_PREFIX = "user:profile:";
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()); // Support for LocalDateTime serialization

    @Inject
    @CachePool
    private JedisPool jedisPool;

    /**
     * Cache user profile in Redis.
     *
     * @param ci          User CI (cédula de identidad)
     * @param userProfile User profile object (typically InusUser entity)
     * @throws IllegalArgumentException if ci or userProfile are null
     * @throws RuntimeException         if Redis operation or serialization fails
     */
    public void cacheUserProfile(String ci, Object userProfile) {
        if (ci == null || ci.trim().isEmpty()) {
            throw new IllegalArgumentException("CI cannot be null or empty");
        }

        if (userProfile == null) {
            throw new IllegalArgumentException("User profile cannot be null");
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(USER_DB);
            String key = KEY_PREFIX + ci;

            // Serialize user profile to JSON
            String jsonValue = objectMapper.writeValueAsString(userProfile);

            // Store with TTL (SETEX is atomic)
            jedis.setex(key, USER_TTL, jsonValue);

            LOGGER.log(Level.FINE, "Cached user profile for CI: {0}, expires in {1} seconds",
                    new Object[]{ci, USER_TTL});

        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Failed to serialize user profile to JSON for CI: " + ci, e);
            throw new RuntimeException("User profile serialization failed", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to cache user profile for CI: " + ci, e);
            throw new RuntimeException("User profile cache storage failed", e);
        }
    }

    /**
     * Retrieve cached user profile from Redis.
     *
     * @param ci User CI
     * @return Optional containing JSON user profile if cached, empty otherwise
     */
    public Optional<String> getCachedProfile(String ci) {
        if (ci == null || ci.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to get cached profile with null or empty CI");
            return Optional.empty();
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(USER_DB);
            String key = KEY_PREFIX + ci;

            String jsonValue = jedis.get(key);

            if (jsonValue == null) {
                LOGGER.log(Level.FINE, "Cache miss for user profile - CI: {0}", ci);
                return Optional.empty();
            }

            LOGGER.log(Level.FINE, "Cache hit for user profile - CI: {0}", ci);
            return Optional.of(jsonValue);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve cached user profile for CI: " + ci, e);
            return Optional.empty();
        }
    }

    /**
     * Retrieve and deserialize cached user profile.
     *
     * @param ci         User CI
     * @param valueClass Class to deserialize the cached JSON into
     * @param <T>        Type of the user profile object
     * @return Optional containing deserialized user profile if cached, empty otherwise
     */
    public <T> Optional<T> getCachedProfileAs(String ci, Class<T> valueClass) {
        if (ci == null || ci.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to get cached profile with null or empty CI");
            return Optional.empty();
        }

        if (valueClass == null) {
            throw new IllegalArgumentException("Value class cannot be null");
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(USER_DB);
            String key = KEY_PREFIX + ci;

            String jsonValue = jedis.get(key);

            if (jsonValue == null) {
                LOGGER.log(Level.FINE, "Cache miss for user profile - CI: {0}", ci);
                return Optional.empty();
            }

            // Deserialize JSON to object
            T userProfile = objectMapper.readValue(jsonValue, valueClass);

            LOGGER.log(Level.FINE, "Cache hit and deserialized user profile - CI: {0}", ci);
            return Optional.of(userProfile);

        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Failed to deserialize cached user profile for CI: " + ci, e);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve cached user profile for CI: " + ci, e);
            return Optional.empty();
        }
    }

    /**
     * Invalidate cached user profile.
     * Should be called when user profile is updated in INUS.
     *
     * @param ci User CI
     * @return true if cache entry was deleted, false if it didn't exist
     */
    public boolean invalidateUserProfile(String ci) {
        if (ci == null || ci.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to invalidate cache with null or empty CI");
            return false;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(USER_DB);
            String key = KEY_PREFIX + ci;

            Long deleted = jedis.del(key);
            boolean wasDeleted = deleted > 0;

            if (wasDeleted) {
                LOGGER.log(Level.INFO, "Invalidated cached user profile for CI: {0}", ci);
            } else {
                LOGGER.log(Level.FINE, "No cached profile found to invalidate for CI: {0}", ci);
            }

            return wasDeleted;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to invalidate user profile cache for CI: " + ci, e);
            throw new RuntimeException("User profile cache invalidation failed", e);
        }
    }

    /**
     * Check if user profile is cached.
     *
     * @param ci User CI
     * @return true if profile is cached, false otherwise
     */
    public boolean isProfileCached(String ci) {
        if (ci == null || ci.trim().isEmpty()) {
            return false;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(USER_DB);
            String key = KEY_PREFIX + ci;

            boolean exists = jedis.exists(key);
            LOGGER.log(Level.FINE, "Profile cache check for CI {0}: {1}",
                    new Object[]{ci, exists});

            return exists;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to check profile cache existence for CI: " + ci, e);
            return false;
        }
    }

    /**
     * Extend user profile cache TTL (refresh cache timeout).
     *
     * @param ci User CI
     * @return true if cache TTL was extended, false if cache entry doesn't exist
     */
    public boolean extendProfileCache(String ci) {
        if (ci == null || ci.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to extend cache with null or empty CI");
            return false;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(USER_DB);
            String key = KEY_PREFIX + ci;

            Long result = jedis.expire(key, USER_TTL);
            boolean extended = result > 0;

            if (extended) {
                LOGGER.log(Level.FINE, "Extended cache TTL for user profile - CI: {0}", ci);
            } else {
                LOGGER.log(Level.FINE, "No cached profile found to extend for CI: {0}", ci);
            }

            return extended;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to extend profile cache for CI: " + ci, e);
            return false;
        }
    }
}
