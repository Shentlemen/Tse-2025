package uy.gub.hcen.cache;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Policy Cache Service - Redis Policy Evaluation Cache
 * <p>
 * Caches policy evaluation decisions in Redis Database 1 to reduce
 * database load and improve performance for repeated access checks.
 * <p>
 * Key Pattern: policy:cache:{patient_ci}:{specialty}:{document_type}
 * Value: Policy decision (PERMIT/DENY/PENDING as String)
 * TTL: 300 seconds (5 minutes)
 * <p>
 * Usage:
 * - Cache policy decisions after evaluation by PolicyEngine
 * - Check cache before evaluating policies (fast path)
 * - Invalidate cache when patient updates policies
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@Stateless
public class PolicyCacheService {

    private static final Logger LOGGER = Logger.getLogger(PolicyCacheService.class.getName());
    private static final int POLICY_DB = 1; // Redis database 1
    private static final int POLICY_TTL = 300; // 5 minutes in seconds
    private static final String KEY_PREFIX = "policy:cache:";

    @Inject
    private JedisPool jedisPool;

    /**
     * Cache a policy evaluation decision.
     *
     * @param patientCi    Patient CI (cédula de identidad)
     * @param specialty    Professional specialty (e.g., CARDIOLOGY, GENERAL_MEDICINE)
     * @param documentType Document type (e.g., CLINICAL_NOTE, LAB_RESULT)
     * @param decision     Policy decision (PERMIT, DENY, or PENDING)
     * @throws IllegalArgumentException if any parameter is null/empty
     * @throws RuntimeException         if Redis operation fails
     */
    public void cachePolicyDecision(String patientCi, String specialty, String documentType, String decision) {
        if (patientCi == null || patientCi.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient CI cannot be null or empty");
        }

        if (specialty == null || specialty.trim().isEmpty()) {
            throw new IllegalArgumentException("Specialty cannot be null or empty");
        }

        if (documentType == null || documentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Document type cannot be null or empty");
        }

        if (decision == null || decision.trim().isEmpty()) {
            throw new IllegalArgumentException("Decision cannot be null or empty");
        }

        // Validate decision value
        if (!decision.equals("PERMIT") && !decision.equals("DENY") && !decision.equals("PENDING")) {
            throw new IllegalArgumentException("Decision must be PERMIT, DENY, or PENDING");
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(POLICY_DB);
            String key = buildCacheKey(patientCi, specialty, documentType);

            // Store decision with TTL (SETEX is atomic)
            jedis.setex(key, POLICY_TTL, decision);

            LOGGER.log(Level.FINE, "Cached policy decision: {0} for patient: {1}, specialty: {2}, document: {3}",
                    new Object[]{decision, patientCi, specialty, documentType});

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to cache policy decision for patient: " + patientCi, e);
            throw new RuntimeException("Policy cache storage failed", e);
        }
    }

    /**
     * Retrieve cached policy decision.
     *
     * @param patientCi    Patient CI
     * @param specialty    Professional specialty
     * @param documentType Document type
     * @return Optional containing decision (PERMIT/DENY/PENDING) if cached, empty otherwise
     */
    public Optional<String> getCachedDecision(String patientCi, String specialty, String documentType) {
        if (patientCi == null || patientCi.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to get cached decision with null or empty patient CI");
            return Optional.empty();
        }

        if (specialty == null || specialty.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to get cached decision with null or empty specialty");
            return Optional.empty();
        }

        if (documentType == null || documentType.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to get cached decision with null or empty document type");
            return Optional.empty();
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(POLICY_DB);
            String key = buildCacheKey(patientCi, specialty, documentType);

            String decision = jedis.get(key);

            if (decision == null) {
                LOGGER.log(Level.FINE, "Cache miss for policy decision - patient: {0}, specialty: {1}, document: {2}",
                        new Object[]{patientCi, specialty, documentType});
                return Optional.empty();
            }

            LOGGER.log(Level.FINE, "Cache hit for policy decision: {0} - patient: {1}, specialty: {2}, document: {3}",
                    new Object[]{decision, patientCi, specialty, documentType});
            return Optional.of(decision);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve cached policy decision for patient: " + patientCi, e);
            return Optional.empty();
        }
    }

    /**
     * Invalidate all cached policy decisions for a patient.
     * Should be called when patient updates their access policies.
     *
     * @param patientCi Patient CI
     * @return Number of cache entries invalidated
     */
    public long invalidatePolicyCache(String patientCi) {
        if (patientCi == null || patientCi.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to invalidate cache with null or empty patient CI");
            return 0;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(POLICY_DB);

            // Find all keys for this patient
            String pattern = KEY_PREFIX + patientCi + ":*";
            var keys = jedis.keys(pattern);

            if (keys.isEmpty()) {
                LOGGER.log(Level.FINE, "No cached policies found for patient: {0}", patientCi);
                return 0;
            }

            // Delete all keys
            long deleted = jedis.del(keys.toArray(new String[0]));

            LOGGER.log(Level.INFO, "Invalidated {0} cached policy decisions for patient: {1}",
                    new Object[]{deleted, patientCi});

            return deleted;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to invalidate policy cache for patient: " + patientCi, e);
            throw new RuntimeException("Policy cache invalidation failed", e);
        }
    }

    /**
     * Invalidate a specific cached policy decision.
     *
     * @param patientCi    Patient CI
     * @param specialty    Professional specialty
     * @param documentType Document type
     * @return true if cache entry was deleted, false if it didn't exist
     */
    public boolean invalidateSpecificDecision(String patientCi, String specialty, String documentType) {
        if (patientCi == null || patientCi.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to invalidate specific decision with null or empty patient CI");
            return false;
        }

        if (specialty == null || specialty.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to invalidate specific decision with null or empty specialty");
            return false;
        }

        if (documentType == null || documentType.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to invalidate specific decision with null or empty document type");
            return false;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(POLICY_DB);
            String key = buildCacheKey(patientCi, specialty, documentType);

            Long deleted = jedis.del(key);
            boolean wasDeleted = deleted > 0;

            if (wasDeleted) {
                LOGGER.log(Level.FINE, "Invalidated cached decision for patient: {0}, specialty: {1}, document: {2}",
                        new Object[]{patientCi, specialty, documentType});
            }

            return wasDeleted;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to invalidate specific policy decision", e);
            return false;
        }
    }

    /**
     * Build Redis cache key from parameters.
     *
     * @param patientCi    Patient CI
     * @param specialty    Professional specialty
     * @param documentType Document type
     * @return Cache key in format: policy:cache:{patient_ci}:{specialty}:{document_type}
     */
    private String buildCacheKey(String patientCi, String specialty, String documentType) {
        return KEY_PREFIX + patientCi + ":" + specialty + ":" + documentType;
    }
}
