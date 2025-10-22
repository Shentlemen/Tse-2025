package uy.gub.hcen.config.qualifier;

import jakarta.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * CDI qualifier for injecting JedisPool for cache storage (Redis Database 1).
 *
 * Used for policy decisions and user profiles.
 * Key patterns:
 * - policy:cache:{patient_ci}:{specialty}:{doc_type}
 * - user:profile:{ci}
 *
 * Usage:
 * <pre>
 * {@literal @}Inject
 * {@literal @}CachePool
 * private JedisPool cachePool;
 * </pre>
 *
 * @author TSE 2025 - Group 9
 */
@Qualifier
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD, FIELD, PARAMETER})
public @interface CachePool {
}
