package uy.gub.hcen.auth.filter;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import uy.gub.hcen.auth.dto.ErrorResponse;
import uy.gub.hcen.auth.service.JwtTokenService;
import uy.gub.hcen.config.qualifier.StatePool;

import java.security.Principal;
import java.util.Map;

/**
 * JWT Authentication Filter
 *
 * JAX-RS filter that validates JWT tokens on protected endpoints.
 *
 * Responsibilities:
 * - Extract JWT from Authorization header
 * - Validate token signature and expiration
 * - Check token blacklist (Redis)
 * - Set SecurityContext with user info
 * - Abort request with 401 if authentication fails
 *
 * Protected Endpoints:
 * - Any endpoint except /auth/login/* and /auth/callback
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-15
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenticationFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Inject
    private JwtTokenService jwtService;

    @Inject
    @StatePool
    private JedisPool jedisPool;

    /**
     * Filters incoming requests to validate JWT authentication
     *
     * @param requestContext Request context
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            logger.debug("Skipping authentication for public endpoint: {}", path);
            return;
        }

        // Extract Authorization header
        String authHeader = requestContext.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header for path: {}", path);
            abortWithUnauthorized(requestContext, "Missing or invalid Authorization header");
            return;
        }

        // Extract token
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            // Validate token
            Map<String, Object> claims = jwtService.validateToken(token);

            // Check if token is blacklisted
            if (isTokenBlacklisted(token)) {
                logger.warn("Token is blacklisted");
                abortWithUnauthorized(requestContext, "Token has been revoked");
                return;
            }

            // Extract user info
            String userCi = (String) claims.get("sub");
            String role = (String) claims.get("role");

            // Set security context
            SecurityContext securityContext = new JwtSecurityContext(userCi, role);
            requestContext.setSecurityContext(securityContext);

            logger.debug("Authentication successful for user: {}", userCi);

        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            abortWithUnauthorized(requestContext, "Invalid or expired token");
        }
    }

    /**
     * Checks if endpoint is public (doesn't require authentication)
     *
     * @param path Request path
     * @return true if public, false otherwise
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("auth/login") ||
               path.startsWith("auth/callback") ||
               path.equals("health") ||
               path.startsWith("api/health");
    }

    /**
     * Checks if token is blacklisted (revoked)
     *
     * @param token JWT token
     * @return true if blacklisted, false otherwise
     */
    private boolean isTokenBlacklisted(String token) {
        try (Jedis jedis = jedisPool.getResource()) {
            // Extract JTI (JWT ID) from token if available
            // For now, use token hash as key
            String tokenKey = "token:blacklist:" + hashToken(token);
            return jedis.exists(tokenKey);
        } catch (Exception e) {
            logger.error("Failed to check token blacklist", e);
            // Fail open for availability (could fail closed for security)
            return false;
        }
    }

    /**
     * Hashes token for blacklist key
     *
     * @param token JWT token
     * @return Hashed token
     */
    private String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            logger.error("Failed to hash token", e);
            return token; // Fallback to plaintext (not ideal)
        }
    }

    /**
     * Aborts request with 401 Unauthorized
     *
     * @param requestContext Request context
     * @param message Error message
     */
    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", message))
                        .build()
        );
    }

    /**
     * Custom SecurityContext implementation for JWT authentication
     */
    private static class JwtSecurityContext implements SecurityContext {

        private final String userCi;
        private final String role;

        public JwtSecurityContext(String userCi, String role) {
            this.userCi = userCi;
            this.role = role;
        }

        @Override
        public Principal getUserPrincipal() {
            return () -> userCi;
        }

        @Override
        public boolean isUserInRole(String role) {
            return this.role != null && this.role.equals(role);
        }

        @Override
        public boolean isSecure() {
            return true; // HTTPS is enforced
        }

        @Override
        public String getAuthenticationScheme() {
            return "Bearer";
        }
    }
}
