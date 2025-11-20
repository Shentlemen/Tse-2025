package uy.gub.hcen.auth.filter;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import uy.gub.hcen.api.dto.ErrorResponse;
import uy.gub.hcen.clinic.entity.Clinic;
import uy.gub.hcen.clinic.repository.ClinicRepository;

import java.security.Principal;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clinic API Key Authentication Filter
 *
 * JAX-RS filter that validates API key authentication for peripheral node requests.
 *
 * Responsibilities:
 * - Extract clinic ID and API key from request headers (X-Clinic-Id, X-API-Key)
 * - Validate clinic exists and API key matches
 * - Verify clinic is in ACTIVE status
 * - Set SecurityContext with clinic ID as principal
 * - Abort request with 401/403 if authentication fails
 *
 * Protected Endpoints:
 * - POST /api/access-requests (clinic creates access request)
 * - GET /api/access-requests/{requestId}/approved-document (professional retrieves document)
 * - Any other clinic-to-central communication
 *
 * Header Format:
 * - X-Clinic-Id: clinic-001
 * - X-API-Key: abc123xyz456...
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-20
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class ClinicApiKeyAuthenticationFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(ClinicApiKeyAuthenticationFilter.class.getName());

    private static final String CLINIC_ID_HEADER = "X-Clinic-Id";
    private static final String API_KEY_HEADER = "X-API-Key";

    @Inject
    private ClinicRepository clinicRepository;

    /**
     * Filters incoming requests to validate clinic API key authentication
     *
     * @param requestContext Request context
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();

        // Check if this is a JWT Bearer token request
        // If so, let JwtAuthenticationFilter handle it
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            LOGGER.log(Level.FINE, "Skipping clinic API key authentication for JWT Bearer request: {0}", path);
            return;
        }

        // Only apply this filter to clinic-related endpoints
        if (!requiresClinicAuthentication(path)) {
            LOGGER.log(Level.FINE, "Skipping clinic API key authentication for path: {0}", path);
            return;
        }

        // Extract headers
        String clinicId = requestContext.getHeaderString(CLINIC_ID_HEADER);
        String apiKey = requestContext.getHeaderString(API_KEY_HEADER);

        // Validate headers are present
        if (clinicId == null || clinicId.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Missing {0} header for path: {1}", new Object[]{CLINIC_ID_HEADER, path});
            abortWithUnauthorized(requestContext, "Missing " + CLINIC_ID_HEADER + " header");
            return;
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Missing {0} header for path: {1}", new Object[]{API_KEY_HEADER, path});
            abortWithUnauthorized(requestContext, "Missing " + API_KEY_HEADER + " header");
            return;
        }

        try {
            // Validate clinic ID and API key
            Optional<Clinic> clinicOpt = clinicRepository.findByIdAndApiKey(clinicId, apiKey);

            if (clinicOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Invalid clinic ID or API key for clinic: {0}", clinicId);
                abortWithUnauthorized(requestContext, "Invalid clinic credentials");
                return;
            }

            Clinic clinic = clinicOpt.get();

            // Verify clinic is active
            if (!clinic.isActive()) {
                LOGGER.log(Level.WARNING, "Clinic {0} is not active (status: {1})",
                        new Object[]{clinicId, clinic.getStatus()});
                abortWithForbidden(requestContext, "Clinic is not active");
                return;
            }

            // Set security context with clinic ID as principal
            SecurityContext securityContext = new ClinicSecurityContext(clinic.getClinicId());
            requestContext.setSecurityContext(securityContext);

            LOGGER.log(Level.INFO, "Clinic API key authentication successful for clinic: {0}", clinicId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during clinic API key authentication", e);
            abortWithUnauthorized(requestContext, "Authentication failed");
        }
    }

    /**
     * Checks if the endpoint requires clinic authentication
     *
     * @param path Request path
     * @return true if clinic authentication required, false otherwise
     */
    private boolean requiresClinicAuthentication(String path) {
        // Endpoints that require clinic API key authentication
        return path.startsWith("api/access-requests") && !path.contains("/approve") && !path.contains("/deny");
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
                        .entity(ErrorResponse.unauthorized(message))
                        .build()
        );
    }

    /**
     * Aborts request with 403 Forbidden
     *
     * @param requestContext Request context
     * @param message Error message
     */
    private void abortWithForbidden(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                        .entity(ErrorResponse.forbidden(message))
                        .build()
        );
    }

    /**
     * Custom SecurityContext implementation for clinic API key authentication
     */
    private static class ClinicSecurityContext implements SecurityContext {

        private final String clinicId;

        public ClinicSecurityContext(String clinicId) {
            this.clinicId = clinicId;
        }

        @Override
        public Principal getUserPrincipal() {
            return () -> clinicId;
        }

        @Override
        public boolean isUserInRole(String role) {
            return "CLINIC".equals(role);
        }

        @Override
        public boolean isSecure() {
            return true; // HTTPS is enforced
        }

        @Override
        public String getAuthenticationScheme() {
            return "API-Key";
        }
    }
}
