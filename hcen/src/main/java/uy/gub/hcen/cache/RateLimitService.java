package uy.gub.hcen.cache;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import uy.gub.hcen.config.qualifier.StatePool;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Rate Limit Service - Redis Rate Limiting
 * <p>
 * Implements rate limiting using Redis Database 2 to prevent abuse
 * and ensure fair resource usage across API endpoints.
 * <p>
 * Key Pattern: ratelimit:{ip_address}:{endpoint}
 * Value: Request count (integer)
 * TTL: 60 seconds (sliding window)
 * <p>
 * Algorithm: Fixed Window Counter
 * - Each request increments the counter
 * - Counter resets after TTL expires
 * - Simple and efficient for most use cases
 * <p>
 * Usage:
 * - Check rate limit before processing API requests
 * - Configure different limits per endpoint
 * - Reject requests that exceed limit with HTTP 429 (Too Many Requests)
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@Stateless
public class RateLimitService {

    private static final Logger LOGGER = Logger.getLogger(RateLimitService.class.getName());
    private static final int RATE_LIMIT_DB = 2; // Redis database 2 (same as OAuth)
    private static final int WINDOW_TTL = 60; // 60 seconds sliding window
    private static final String KEY_PREFIX = "ratelimit:";

    @Inject
    @StatePool
    private JedisPool jedisPool;

    /**
     * Check if request is allowed within rate limit.
     * Increments counter atomically if allowed.
     *
     * @param ipAddress   Client IP address
     * @param endpoint    API endpoint path (e.g., /api/auth/login)
     * @param maxRequests Maximum requests allowed in time window
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String ipAddress, String endpoint, int maxRequests) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted rate limit check with null or empty IP address");
            return false;
        }

        if (endpoint == null || endpoint.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted rate limit check with null or empty endpoint");
            return false;
        }

        if (maxRequests <= 0) {
            throw new IllegalArgumentException("Max requests must be greater than 0");
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(RATE_LIMIT_DB);
            String key = buildRateLimitKey(ipAddress, endpoint);

            // Get current count
            String currentCountStr = jedis.get(key);
            int currentCount = 0;

            if (currentCountStr != null) {
                try {
                    currentCount = Integer.parseInt(currentCountStr);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Invalid rate limit counter value, resetting: {0}", currentCountStr);
                    jedis.del(key);
                }
            }

            // Check if limit exceeded
            if (currentCount >= maxRequests) {
                LOGGER.log(Level.WARNING, "Rate limit exceeded for IP: {0}, endpoint: {1}, count: {2}/{3}",
                        new Object[]{ipAddress, endpoint, currentCount, maxRequests});
                return false;
            }

            // Increment counter
            long newCount = jedis.incr(key);

            // Set TTL on first request (when counter is created)
            if (newCount == 1) {
                jedis.expire(key, WINDOW_TTL);
                LOGGER.log(Level.FINE, "Rate limit window started for IP: {0}, endpoint: {1}, expires in {2} seconds",
                        new Object[]{ipAddress, endpoint, WINDOW_TTL});
            }

            LOGGER.log(Level.FINE, "Request allowed for IP: {0}, endpoint: {1}, count: {2}/{3}",
                    new Object[]{ipAddress, endpoint, newCount, maxRequests});

            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to check rate limit for IP: " + ipAddress + ", endpoint: " + endpoint, e);
            // Fail open: allow request if Redis is unavailable (graceful degradation)
            return true;
        }
    }

    /**
     * Get current request count for IP and endpoint.
     *
     * @param ipAddress Client IP address
     * @param endpoint  API endpoint path
     * @return Current request count in window, 0 if no requests yet
     */
    public int getCurrentCount(String ipAddress, String endpoint) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to get count with null or empty IP address");
            return 0;
        }

        if (endpoint == null || endpoint.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to get count with null or empty endpoint");
            return 0;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(RATE_LIMIT_DB);
            String key = buildRateLimitKey(ipAddress, endpoint);

            String currentCountStr = jedis.get(key);

            if (currentCountStr == null) {
                return 0;
            }

            try {
                int count = Integer.parseInt(currentCountStr);
                LOGGER.log(Level.FINE, "Current count for IP: {0}, endpoint: {1}: {2}",
                        new Object[]{ipAddress, endpoint, count});
                return count;
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid rate limit counter value: {0}", currentCountStr);
                return 0;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get current count for IP: " + ipAddress + ", endpoint: " + endpoint, e);
            return 0;
        }
    }

    /**
     * Get remaining requests in current window.
     *
     * @param ipAddress   Client IP address
     * @param endpoint    API endpoint path
     * @param maxRequests Maximum requests allowed in time window
     * @return Number of requests remaining, 0 if limit exceeded
     */
    public int getRemainingRequests(String ipAddress, String endpoint, int maxRequests) {
        int currentCount = getCurrentCount(ipAddress, endpoint);
        int remaining = Math.max(0, maxRequests - currentCount);

        LOGGER.log(Level.FINE, "Remaining requests for IP: {0}, endpoint: {1}: {2}",
                new Object[]{ipAddress, endpoint, remaining});

        return remaining;
    }

    /**
     * Reset rate limit for IP and endpoint.
     * Useful for manual override or testing.
     *
     * @param ipAddress Client IP address
     * @param endpoint  API endpoint path
     * @return true if limit was reset, false if no limit existed
     */
    public boolean resetLimit(String ipAddress, String endpoint) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to reset limit with null or empty IP address");
            return false;
        }

        if (endpoint == null || endpoint.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to reset limit with null or empty endpoint");
            return false;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(RATE_LIMIT_DB);
            String key = buildRateLimitKey(ipAddress, endpoint);

            Long deleted = jedis.del(key);
            boolean wasDeleted = deleted > 0;

            if (wasDeleted) {
                LOGGER.log(Level.INFO, "Reset rate limit for IP: {0}, endpoint: {1}",
                        new Object[]{ipAddress, endpoint});
            } else {
                LOGGER.log(Level.FINE, "No rate limit found to reset for IP: {0}, endpoint: {1}",
                        new Object[]{ipAddress, endpoint});
            }

            return wasDeleted;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to reset rate limit for IP: " + ipAddress + ", endpoint: " + endpoint, e);
            throw new RuntimeException("Rate limit reset failed", e);
        }
    }

    /**
     * Reset all rate limits for a specific IP address.
     * Useful for whitelisting or emergency access.
     *
     * @param ipAddress Client IP address
     * @return Number of rate limits reset
     */
    public long resetAllLimitsForIp(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to reset all limits with null or empty IP address");
            return 0;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(RATE_LIMIT_DB);

            // Find all keys for this IP
            String pattern = KEY_PREFIX + ipAddress + ":*";
            var keys = jedis.keys(pattern);

            if (keys.isEmpty()) {
                LOGGER.log(Level.FINE, "No rate limits found for IP: {0}", ipAddress);
                return 0;
            }

            // Delete all keys
            long deleted = jedis.del(keys.toArray(new String[0]));

            LOGGER.log(Level.INFO, "Reset {0} rate limits for IP: {1}",
                    new Object[]{deleted, ipAddress});

            return deleted;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to reset all limits for IP: " + ipAddress, e);
            throw new RuntimeException("Rate limit reset failed", e);
        }
    }

    /**
     * Get time until rate limit window resets.
     *
     * @param ipAddress Client IP address
     * @param endpoint  API endpoint path
     * @return Seconds until reset, -1 if no limit exists
     */
    public long getTimeUntilReset(String ipAddress, String endpoint) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to get TTL with null or empty IP address");
            return -1;
        }

        if (endpoint == null || endpoint.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to get TTL with null or empty endpoint");
            return -1;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(RATE_LIMIT_DB);
            String key = buildRateLimitKey(ipAddress, endpoint);

            Long ttl = jedis.ttl(key);

            if (ttl == -2) {
                // Key doesn't exist
                LOGGER.log(Level.FINE, "No rate limit found for IP: {0}, endpoint: {1}",
                        new Object[]{ipAddress, endpoint});
                return -1;
            }

            if (ttl == -1) {
                // Key exists but has no TTL (should not happen with our implementation)
                LOGGER.log(Level.WARNING, "Rate limit key exists without TTL for IP: {0}, endpoint: {1}",
                        new Object[]{ipAddress, endpoint});
                return -1;
            }

            LOGGER.log(Level.FINE, "Time until reset for IP: {0}, endpoint: {1}: {2} seconds",
                    new Object[]{ipAddress, endpoint, ttl});

            return ttl;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get TTL for IP: " + ipAddress + ", endpoint: " + endpoint, e);
            return -1;
        }
    }

    /**
     * Build Redis rate limit key from IP address and endpoint.
     *
     * @param ipAddress Client IP address
     * @param endpoint  API endpoint path
     * @return Rate limit key in format: ratelimit:{ip_address}:{endpoint}
     */
    private String buildRateLimitKey(String ipAddress, String endpoint) {
        // Sanitize endpoint to remove leading slashes and replace slashes with colons
        String sanitizedEndpoint = endpoint.replaceFirst("^/+", "").replace("/", ":");
        return KEY_PREFIX + ipAddress + ":" + sanitizedEndpoint;
    }
}
