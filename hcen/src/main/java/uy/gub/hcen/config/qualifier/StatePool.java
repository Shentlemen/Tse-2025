package uy.gub.hcen.config.qualifier;

import jakarta.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * CDI qualifier for injecting JedisPool for state storage (Redis Database 2).
 *
 * Used for OAuth state and rate limiting.
 * Key patterns:
 * - oauth:state:{state_token}
 * - ratelimit:{ip}:{endpoint}
 *
 * Usage:
 * <pre>
 * {@literal @}Inject
 * {@literal @}StatePool
 * private JedisPool statePool;
 * </pre>
 *
 * @author TSE 2025 - Group 9
 */
@Qualifier
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD, FIELD, PARAMETER})
public @interface StatePool {
}
