package uy.gub.hcen.auth.api.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.hcen.auth.dto.*;
import uy.gub.hcen.auth.exception.AuthenticationException;
import uy.gub.hcen.auth.exception.InvalidTokenException;
import uy.gub.hcen.auth.service.AuthenticationService;

/**
 * Authentication REST Resource
 *
 * Exposes RESTful endpoints for HCEN authentication using gub.uy OpenID Connect.
 *
 * Endpoints:
 * - POST /auth/login/initiate: Start OAuth 2.0 flow
 * - POST /auth/callback: Handle OAuth callback (mobile)
 * - GET /auth/callback: Handle OAuth callback (web)
 * - POST /auth/token/refresh: Refresh access token
 * - GET /auth/session: Get current session info
 * - POST /auth/logout: Logout and revoke tokens
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-15
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthenticationResource {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationResource.class);

    @Inject
    private AuthenticationService authService;

    /**
     * POST /auth/login/initiate
     *
     * Initiates OAuth 2.0 authorization flow.
     *
     * For mobile clients: Returns JSON with authorization URL
     * For web clients: Can redirect directly or return URL
     *
     * @param request Login initiation request
     * @return 200 OK with authorization URL
     */
    @POST
    @Path("/login/initiate")
    public Response initiateLogin(@Valid LoginInitiateRequest request) {
        try {
            logger.info("Login initiation requested for client type: {}", request.getClientType());

            LoginInitiateResponse response = authService.initiateLogin(request);

            // For web clients, we could optionally return a 302 redirect instead
            // For now, return JSON for all clients for consistency
            return Response.ok(response).build();

        } catch (AuthenticationException e) {
            logger.error("Login initiation failed", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("LOGIN_INITIATION_FAILED", e.getMessage()))
                    .build();
        } catch (Exception e) {
            logger.error("Unexpected error during login initiation", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
                    .build();
        }
    }

    /**
     * POST /auth/callback
     *
     * Handles OAuth 2.0 callback from gub.uy (primarily for mobile clients).
     *
     * Mobile clients POST the authorization code and code verifier.
     * Web clients typically use GET /auth/callback.
     *
     * @param request Callback request
     * @return 200 OK with HCEN tokens
     */
    @POST
    @Path("/callback")
    public Response handleCallbackPost(@Valid CallbackRequest request) {
        try {
            logger.info("OAuth callback received (POST) for client type: {}", request.getClientType());

            TokenResponse response = authService.handleCallback(request);

            return Response.ok(response).build();

        } catch (AuthenticationException e) {
            logger.error("OAuth callback failed", e);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("AUTHENTICATION_FAILED", e.getMessage()))
                    .build();
        } catch (Exception e) {
            logger.error("Unexpected error during callback", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
                    .build();
        }
    }

    /**
     * GET /auth/callback
     *
     * Handles OAuth 2.0 callback from gub.uy (primarily for web clients).
     *
     * gub.uy redirects to this endpoint with code and state as query parameters.
     *
     * @param code Authorization code
     * @param state CSRF protection state
     * @param error OAuth error (if authentication failed)
     * @param errorDescription OAuth error description
     * @return 302 redirect to dashboard (web) or 200 OK with tokens (could be used for testing)
     */
    @GET
    @Path("/callback")
    @Produces(MediaType.TEXT_HTML) // Will redirect, so HTML is appropriate
    public Response handleCallbackGet(
            @QueryParam("code") String code,
            @QueryParam("state") String state,
            @QueryParam("error") String error,
            @QueryParam("error_description") String errorDescription) {

        try {
            // Handle OAuth errors
            if (error != null) {
                logger.warn("OAuth authentication failed: {} - {}", error, errorDescription);
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("<html><body><h1>Authentication Failed</h1><p>" + errorDescription + "</p></body></html>")
                        .build();
            }

            // Validate required parameters
            if (code == null || state == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("<html><body><h1>Invalid Request</h1><p>Missing code or state parameter</p></body></html>")
                        .build();
            }

            logger.info("OAuth callback received (GET)");

            // Create callback request (assume WEB_PATIENT for now, could be detected from state)
            CallbackRequest request = new CallbackRequest();
            request.setCode(code);
            request.setState(state);
            request.setClientType(uy.gub.hcen.auth.config.OidcConfiguration.ClientType.WEB_PATIENT);
            request.setRedirectUri("https://hcen.uy/api/auth/callback"); // Should match registered URI

            TokenResponse response = authService.handleCallback(request);

            // For web clients, set HttpOnly cookie and redirect to dashboard
            // For now, return simple success page
            return Response.ok()
                    .entity("<html><body><h1>Authentication Successful</h1><p>Redirecting to dashboard...</p>" +
                            "<script>setTimeout(function(){ window.location.href='/'; }, 2000);</script></body></html>")
                    .build();

        } catch (AuthenticationException e) {
            logger.error("OAuth callback failed", e);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("<html><body><h1>Authentication Failed</h1><p>" + e.getMessage() + "</p></body></html>")
                    .build();
        } catch (Exception e) {
            logger.error("Unexpected error during callback", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("<html><body><h1>Error</h1><p>An unexpected error occurred</p></body></html>")
                    .build();
        }
    }

    /**
     * POST /auth/token/refresh
     *
     * Refreshes access token using refresh token.
     *
     * Implements refresh token rotation for security.
     *
     * @param request Token refresh request
     * @return 200 OK with new tokens
     */
    @POST
    @Path("/token/refresh")
    public Response refreshToken(@Valid TokenRefreshRequest request) {
        try {
            logger.debug("Token refresh requested");

            TokenResponse response = authService.refreshToken(request.getRefreshToken());

            return Response.ok(response).build();

        } catch (InvalidTokenException e) {
            logger.warn("Invalid refresh token", e);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("INVALID_TOKEN", e.getMessage()))
                    .build();
        } catch (AuthenticationException e) {
            logger.error("Token refresh failed", e);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("TOKEN_REFRESH_FAILED", e.getMessage()))
                    .build();
        } catch (Exception e) {
            logger.error("Unexpected error during token refresh", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
                    .build();
        }
    }

    /**
     * GET /auth/session
     *
     * Gets current session information.
     *
     * Requires valid JWT access token in Authorization header.
     *
     * @param securityContext Security context (injected by filter)
     * @return 200 OK with session info
     */
    @GET
    @Path("/session")
    public Response getSession(@Context SecurityContext securityContext) {
        try {
            // Extract user info from security context
            // This requires JwtAuthenticationFilter to be implemented and set the security context
            String userCi = securityContext.getUserPrincipal() != null ?
                    securityContext.getUserPrincipal().getName() : null;

            if (userCi == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No active session"))
                        .build();
            }

            logger.debug("Session info requested for user: {}", userCi);

            // Return session info (placeholder implementation)
            SessionInfoResponse response = new SessionInfoResponse();
//            response.setAuthenticated(true);

            // TODO: Populate with actual user info and session details

            return Response.ok(response).build();

        } catch (Exception e) {
            logger.error("Failed to get session info", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
                    .build();
        }
    }

    /**
     * POST /auth/logout
     *
     * Logs out user and revokes all refresh tokens.
     *
     * Requires valid JWT access token in Authorization header.
     *
     * @param securityContext Security context (injected by filter)
     * @return 204 No Content
     */
    @POST
    @Path("/logout")
    public Response logout(@Context SecurityContext securityContext) {
        try {
            String userCi = securityContext.getUserPrincipal() != null ?
                    securityContext.getUserPrincipal().getName() : null;

            if (userCi == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("UNAUTHORIZED", "No active session"))
                        .build();
            }

            logger.info("Logout requested for user: {}", userCi);

            authService.logout(userCi);

            return Response.noContent().build();

        } catch (AuthenticationException e) {
            logger.error("Logout failed", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("LOGOUT_FAILED", e.getMessage()))
                    .build();
        } catch (Exception e) {
            logger.error("Unexpected error during logout", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
                    .build();
        }
    }
}
