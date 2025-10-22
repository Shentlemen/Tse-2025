package uy.gub.hcen.config.qualifier;

import jakarta.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * CDI qualifier for injecting JedisPool for session storage (Redis Database 0).
 *
 * Used for JWT session data with 1 hour TTL.
 * Key pattern: session:{jwt_token_id}
 *
 * Usage:
 * <pre>
 * {@literal @}Inject
 * {@literal @}SessionPool
 * private JedisPool sessionPool;
 * </pre>
 *
 * @author TSE 2025 - Group 9
 */
@Qualifier
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD, FIELD, PARAMETER})
public @interface SessionPool {
}
