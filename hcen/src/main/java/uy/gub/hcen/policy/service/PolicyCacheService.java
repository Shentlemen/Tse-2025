package uy.gub.hcen.policy.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import uy.gub.hcen.config.RedisConfiguration;
import uy.gub.hcen.config.qualifier.CachePool;

import java.util.logging.Logger;

/**
 * Policy Cache Service for caching policy evaluation decisions.
 *
 * Caches policy decisions in Redis Database 1 to reduce database load
 * and improve policy evaluation performance. Decisions are cached with
 * a 5-minute TTL to balance freshness and performance.
 *
 * Key pattern: policy:cache:{patient_ci}:{specialty}:{document_type}
 * Value: Policy decision (PERMIT/DENY/PENDING)
 * TTL: 300 seconds (5 minutes)
 *
 * Cache is invalidated when:
 * - Patient modifies access policies
 * - Policy expiration time is reached
 * - Manual invalidation by admin
 *
 * @author TSE 2025 - Group 9
 */
@ApplicationScoped
public class PolicyCacheService {

    private static final Logger LOGGER = Logger.getLogger(PolicyCacheService.class.getName());

    @Inject
    @CachePool
    private JedisPool cachePool;

    /**
     * Policy decision types that can be cached.
     */
    public enum PolicyDecision {
        PERMIT,
        DENY,
        PENDING
    }

    /**
     * Caches a policy decision.
     *
     * @param patientCi CI of the patient
     * @param specialty Professional's specialty
     * @param documentType Type of document being accessed
     * @param decision Policy decision (PERMIT/DENY/PENDING)
     * @return true if cached successfully
     */
    public boolean cachePolicyDecision(String patientCi, String specialty, String documentType, String decision) {
        if (!isValidInput(patientCi, specialty, documentType, decision)) {
            return false;
        }

        try (Jedis jedis = cachePool.getResource()) {
            String key = RedisConfiguration.KeyPatterns.policyCache(patientCi, specialty, documentType);
            String result = jedis.setex(key, RedisConfiguration.TTL.POLICY_CACHE, decision);

            if ("OK".equals(result)) {
                LOGGER.fine(String.format("Cached policy decision for patient=%s, specialty=%s, docType=%s: %s",
                        patientCi, specialty, documentType, decision));
                return true;
            } else {
                LOGGER.warning(String.format("Failed to cache policy decision for patient=%s", patientCi));
                return false;
            }

        } catch (Exception e) {
            LOGGER.severe("Failed to cache policy decision: " + e.getMessage());
            return false;
        }
    }

    /**
     * Caches a policy decision using enum type.
     *
     * @param patientCi CI of the patient
     * @param specialty Professional's specialty
     * @param documentType Type of document being accessed
     * @param decision Policy decision enum
     * @return true if cached successfully
     */
    public boolean cachePolicyDecision(String patientCi, String specialty, String documentType, PolicyDecision decision) {
        return cachePolicyDecision(patientCi, specialty, documentType, decision.name());
    }

    /**
     * Retrieves a cached policy decision.
     *
     * @param patientCi CI of the patient
     * @param specialty Professional's specialty
     * @param documentType Type of document being accessed
     * @return Cached policy decision, or null if not found or expired
     */
    public String getPolicyDecision(String patientCi, String specialty, String documentType) {
        if (patientCi == null || patientCi.trim().isEmpty()) {
            LOGGER.warning("Cannot get policy decision: patientCi is null or empty");
            return null;
        }

        try (Jedis jedis = cachePool.getResource()) {
            String key = RedisConfiguration.KeyPatterns.policyCache(patientCi, specialty, documentType);
            String decision = jedis.get(key);

            if (decision != null) {
                LOGGER.fine(String.format("Cache hit for patient=%s, specialty=%s, docType=%s: %s",
                        patientCi, specialty, documentType, decision));
            } else {
                LOGGER.fine(String.format("Cache miss for patient=%s, specialty=%s, docType=%s",
                        patientCi, specialty, documentType));
            }

            return decision;

        } catch (Exception e) {
            LOGGER.severe("Failed to get policy decision from cache: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves a cached policy decision as enum.
     *
     * @param patientCi CI of the patient
     * @param specialty Professional's specialty
     * @param documentType Type of document being accessed
     * @return Cached policy decision enum, or null if not found or expired
     */
    public PolicyDecision getPolicyDecisionEnum(String patientCi, String specialty, String documentType) {
        String decision = getPolicyDecision(patientCi, specialty, documentType);
        if (decision == null) {
            return null;
        }

        try {
            return PolicyDecision.valueOf(decision);
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Invalid policy decision in cache: " + decision);
            return null;
        }
    }

    /**
     * Invalidates all cached policy decisions for a patient.
     * Called when patient modifies their access policies.
     *
     * @param patientCi CI of the patient
     * @return Number of cache entries invalidated
     */
    public long invalidatePolicyCache(String patientCi) {
        if (patientCi == null || patientCi.trim().isEmpty()) {
            LOGGER.warning("Cannot invalidate cache: patientCi is null or empty");
            return 0;
        }

        try (Jedis jedis = cachePool.getResource()) {
            // Use pattern matching to find all keys for this patient
            String pattern = String.format("policy:cache:%s:*", patientCi);
            var keys = jedis.keys(pattern);

            if (keys.isEmpty()) {
                LOGGER.fine("No cache entries found for patient: " + patientCi);
                return 0;
            }

            long deleted = jedis.del(keys.toArray(new String[0]));

            LOGGER.info(String.format("Invalidated %d policy cache entries for patient: %s", deleted, patientCi));
            return deleted;

        } catch (Exception e) {
            LOGGER.severe("Failed to invalidate policy cache for patient " + patientCi + ": " + e.getMessage());
            return 0;
        }
    }

    /**
     * Invalidates a specific cached policy decision.
     *
     * @param patientCi CI of the patient
     * @param specialty Professional's specialty
     * @param documentType Type of document being accessed
     * @return true if invalidated successfully
     */
    public boolean invalidateSpecificDecision(String patientCi, String specialty, String documentType) {
        if (!isValidInput(patientCi, specialty, documentType, "dummy")) {
            return false;
        }

        try (Jedis jedis = cachePool.getResource()) {
            String key = RedisConfiguration.KeyPatterns.policyCache(patientCi, specialty, documentType);
            Long deleted = jedis.del(key);

            if (deleted > 0) {
                LOGGER.fine(String.format("Invalidated policy cache for patient=%s, specialty=%s, docType=%s",
                        patientCi, specialty, documentType));
                return true;
            } else {
                LOGGER.fine(String.format("No cache entry found to invalidate for patient=%s", patientCi));
                return false;
            }

        } catch (Exception e) {
            LOGGER.severe("Failed to invalidate specific policy cache: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a policy decision is cached.
     *
     * @param patientCi CI of the patient
     * @param specialty Professional's specialty
     * @param documentType Type of document being accessed
     * @return true if decision is cached
     */
    public boolean isCached(String patientCi, String specialty, String documentType) {
        try (Jedis jedis = cachePool.getResource()) {
            String key = RedisConfiguration.KeyPatterns.policyCache(patientCi, specialty, documentType);
            return jedis.exists(key);
        } catch (Exception e) {
            LOGGER.severe("Failed to check cache existence: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the remaining TTL of a cached policy decision.
     *
     * @param patientCi CI of the patient
     * @param specialty Professional's specialty
     * @param documentType Type of document being accessed
     * @return Remaining TTL in seconds, or -1 if not found, -2 if no TTL set
     */
    public long getCacheTTL(String patientCi, String specialty, String documentType) {
        try (Jedis jedis = cachePool.getResource()) {
            String key = RedisConfiguration.KeyPatterns.policyCache(patientCi, specialty, documentType);
            return jedis.ttl(key);
        } catch (Exception e) {
            LOGGER.severe("Failed to get cache TTL: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Gets cache statistics for monitoring.
     *
     * @return Map with cache statistics (total keys, etc.)
     */
    public long getCacheSize() {
        try (Jedis jedis = cachePool.getResource()) {
            String pattern = "policy:cache:*";
            var keys = jedis.keys(pattern);
            return keys.size();
        } catch (Exception e) {
            LOGGER.severe("Failed to get cache size: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Clears all policy cache entries (admin operation).
     *
     * @return Number of entries cleared
     */
    public long clearAllCache() {
        try (Jedis jedis = cachePool.getResource()) {
            String pattern = "policy:cache:*";
            var keys = jedis.keys(pattern);

            if (keys.isEmpty()) {
                LOGGER.info("No policy cache entries to clear");
                return 0;
            }

            long deleted = jedis.del(keys.toArray(new String[0]));

            LOGGER.warning(String.format("Cleared ALL policy cache (%d entries)", deleted));
            return deleted;

        } catch (Exception e) {
            LOGGER.severe("Failed to clear all policy cache: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Validates input parameters.
     */
    private boolean isValidInput(String patientCi, String specialty, String documentType, String decision) {
        if (patientCi == null || patientCi.trim().isEmpty()) {
            LOGGER.warning("Invalid input: patientCi is null or empty");
            return false;
        }

        if (specialty == null || specialty.trim().isEmpty()) {
            LOGGER.warning("Invalid input: specialty is null or empty");
            return false;
        }

        if (documentType == null || documentType.trim().isEmpty()) {
            LOGGER.warning("Invalid input: documentType is null or empty");
            return false;
        }

        if (decision == null || decision.trim().isEmpty()) {
            LOGGER.warning("Invalid input: decision is null or empty");
            return false;
        }

        return true;
    }
}
