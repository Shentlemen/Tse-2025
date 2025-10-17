package uy.gub.hcen.auth.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.hcen.auth.config.OidcConfiguration.ClientType;
import uy.gub.hcen.auth.dto.*;
import uy.gub.hcen.auth.entity.RefreshToken;
import uy.gub.hcen.auth.exception.AuthenticationException;
import uy.gub.hcen.auth.exception.InvalidStateException;
import uy.gub.hcen.auth.exception.InvalidTokenException;
import uy.gub.hcen.auth.exception.OAuthException;
import uy.gub.hcen.auth.integration.gubuy.GubUyOidcClient;
import uy.gub.hcen.auth.integration.gubuy.GubUyTokenResponse;
import uy.gub.hcen.auth.repository.RefreshTokenRepository;
import uy.gub.hcen.auth.util.StateUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication Service - Central Orchestration
 *
 * Manages the complete authentication flow for HCEN, integrating with gub.uy (ID Uruguay).
 *
 * Responsibilities:
 * - Initiate OAuth 2.0 authorization flow
 * - Handle OAuth callback and token exchange
 * - Generate HCEN JWT tokens
 * - Manage refresh token rotation
 * - Handle logout and token revocation
 * - Create/update INUS users
 *
 * Flow:
 * 1. initiateLogin -> Generate state, build authorization URL
 * 2. handleCallback -> Exchange code for tokens, create/update user, generate HCEN tokens
 * 3. refreshToken -> Rotate refresh token, generate new access token
 * 4. logout -> Revoke all tokens
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-15
 */
@ApplicationScoped
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    @Inject
    private GubUyOidcClient gubUyClient;

    @Inject
    private JwtTokenService jwtService;

    @Inject
    private StateManager stateManager;

    @Inject
    private RefreshTokenRepository refreshTokenRepo;

    @Inject
    private MongoDatabase mongoDatabase;

    /**
     * Initiates OAuth 2.0 authorization flow
     *
     * @param request Login initiation request
     * @return Authorization URL and state
     * @throws AuthenticationException if flow initiation fails
     */
    public LoginInitiateResponse initiateLogin(LoginInitiateRequest request) {
        try {
            logger.info("Initiating login for client type: {}", request.getClientType());

            // Generate and store state in Redis with metadata
            // Note: createState() generates the state internally and returns it
            String state = stateManager.createState(request.getClientType(), request.getRedirectUri(),
                                   request.getCodeChallenge());

            // Build authorization URL
            String authorizationUrl = gubUyClient.buildAuthorizationUrl(
                    request.getClientType(),
                    request.getRedirectUri(),
                    state,
                    request.getCodeChallenge()
            );

            logger.info("Login initiation successful for client type: {}", request.getClientType());

            return new LoginInitiateResponse(authorizationUrl, state, 300); // 5 minutes TTL

        } catch (Exception e) {
            logger.error("Failed to initiate login", e);
            throw new AuthenticationException("Failed to initiate login", e);
        }
    }

    /**
     * Handles OAuth 2.0 callback after user authentication at gub.uy
     *
     * Flow:
     * 1. Validate state (CSRF protection)
     * 2. Exchange authorization code for gub.uy tokens
     * 3. Validate ID token
     * 4. Extract user CI from claims
     * 5. Create/update user in INUS
     * 6. Generate HCEN JWT tokens
     * 7. Store refresh token
     * 8. Return tokens to client
     *
     * @param request Callback request with authorization code
     * @return HCEN tokens and user info
     * @throws AuthenticationException if callback handling fails
     */
    public TokenResponse handleCallback(CallbackRequest request) {
        try {
            logger.info("Handling OAuth callback for client type: {}", request.getClientType());

            // 1. Validate state (CSRF protection)
            validateState(request.getState(), request.getClientType());

            // 2. Exchange authorization code for tokens
            GubUyTokenResponse gubUyTokens = gubUyClient.exchangeCode(
                    request.getCode(),
                    request.getRedirectUri(),
                    request.getCodeVerifier(),
                    request.getClientType()
            );

            // 3. Validate ID token and extract claims
            Map<String, Object> idTokenClaims = gubUyClient.validateIdToken(gubUyTokens.getIdToken());

            // 4. Extract user CI from claims (uid or sub)
            String userCi = extractUserCi(idTokenClaims);

            // 5. Create or update user in INUS
            UserInfoDTO userInfo = createOrUpdateInusUser(userCi, idTokenClaims);

            // 6. Generate HCEN JWT tokens
            String accessToken = jwtService.generateAccessToken(userCi, userInfo.getInusId(), "PATIENT", Map.of());
            String refreshToken = jwtService.generateRefreshToken(userCi, userInfo.getInusId());

            // 7. Store refresh token in MongoDB
            storeRefreshToken(refreshToken, userCi, request.getClientType(), request.getDeviceId());

            // 8. Log authentication event
            logAuthenticationEvent(userCi, request.getClientType(), true, null);

            logger.info("Authentication successful for user: {}", userCi);

            // 9. Return response
            return new TokenResponse(accessToken, refreshToken, 3600, userInfo);

        } catch (InvalidStateException | OAuthException e) {
            logger.error("OAuth callback failed", e);
            throw new AuthenticationException("OAuth callback failed", e);
        } catch (Exception e) {
            logger.error("Unexpected error during callback handling", e);
            throw new AuthenticationException("Authentication failed", e);
        }
    }

    /**
     * Refreshes access token using refresh token
     *
     * Implements refresh token rotation for enhanced security
     *
     * @param refreshTokenValue Refresh token value
     * @return New access token and rotated refresh token
     * @throws InvalidTokenException if refresh token is invalid or revoked
     */
    public TokenResponse refreshToken(String refreshTokenValue) {
        try {
            logger.debug("Refreshing access token");

            // 1. Hash refresh token
            String tokenHash = hashToken(refreshTokenValue);

            // 2. Find token in database
            Optional<RefreshToken> tokenOpt = refreshTokenRepo.findByTokenHash(tokenHash);

            if (tokenOpt.isEmpty()) {
                logger.warn("Refresh token not found");
                throw new InvalidTokenException("Invalid refresh token");
            }

            RefreshToken token = tokenOpt.get();

            // 3. Validate token (not revoked, not expired)
            if (!token.isValid()) {
                logger.warn("Refresh token is revoked or expired for user: {}", token.getUserCi());
                throw new InvalidTokenException("Refresh token is revoked or expired");
            }

            // 4. Revoke old refresh token (rotation)
            refreshTokenRepo.revokeToken(tokenHash);

            // 5. Generate new tokens
            String userCi = token.getUserCi();
            String inusId = getInusIdForUser(userCi);

            String newAccessToken = jwtService.generateAccessToken(userCi, inusId, "PATIENT", Map.of());
            String newRefreshToken = jwtService.generateRefreshToken(userCi, inusId);

            // 6. Store new refresh token
            storeRefreshToken(newRefreshToken, userCi, token.getClientType(), token.getDeviceId());

            logger.info("Access token refreshed successfully for user: {}", userCi);

            // 7. Return new tokens
            UserInfoDTO userInfo = getUserInfo(userCi, inusId);
            return new TokenResponse(newAccessToken, newRefreshToken, 3600, userInfo);

        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to refresh token", e);
            throw new AuthenticationException("Token refresh failed", e);
        }
    }

    /**
     * Logs out user by revoking all their refresh tokens
     *
     * @param userCi User's CI
     */
    public void logout(String userCi) {
        try {
            logger.info("Logging out user: {}", userCi);

            // Revoke all refresh tokens for user
            long revokedCount = refreshTokenRepo.revokeAllForUser(userCi);

            logger.info("Revoked {} refresh tokens for user: {}", revokedCount, userCi);

            // Log logout event
            logAuthenticationEvent(userCi, null, false, "logout");

        } catch (Exception e) {
            logger.error("Failed to logout user: {}", userCi, e);
            throw new AuthenticationException("Logout failed", e);
        }
    }

    // Private Helper Methods

    /**
     * Validates OAuth state parameter
     */
    private void validateState(String state, ClientType clientType) throws InvalidStateException {
        // validateAndConsumeState returns Map<String, Object> with state data
        // It throws InvalidStateException if state is invalid or expired
        Map<String, Object> stateData = stateManager.validateAndConsumeState(state);

        // Optionally verify client type matches
        String storedClientType = (String) stateData.get("clientType");
        if (!clientType.name().equals(storedClientType)) {
            logger.warn("Client type mismatch. Expected: {}, Got: {}", storedClientType, clientType);
            throw new InvalidStateException("Client type mismatch in state validation");
        }
    }

    /**
     * Extracts user CI from ID token claims
     */
    private String extractUserCi(Map<String, Object> claims) {
        // Try 'uid' first (gub.uy specific), then 'sub' (standard)
        String userCi = (String) claims.get("uid");

        if (userCi == null) {
            userCi = (String) claims.get("sub");
        }

        if (userCi == null || userCi.trim().isEmpty()) {
            throw new AuthenticationException("User CI not found in ID token");
        }

        return userCi;
    }

    /**
     * Creates or updates INUS user from gub.uy claims
     *
     * @param userCi User's CI
     * @param claims ID token claims
     * @return UserInfoDTO
     */
    private UserInfoDTO createOrUpdateInusUser(String userCi, Map<String, Object> claims) {
        try {
            MongoCollection<Document> collection = mongoDatabase.getCollection("inus_users");

            // Check if user exists
            Document existingUser = collection.find(new Document("ci", userCi)).first();

            String inusId;
            String firstName;
            String lastName;

            if (existingUser != null) {
                // Update existing user
                logger.debug("Updating existing INUS user: {}", userCi);

                inusId = existingUser.getString("inusId");
                firstName = (String) claims.getOrDefault("primer_nombre", existingUser.getString("firstName"));
                lastName = (String) claims.getOrDefault("primer_apellido", existingUser.getString("lastName"));

                Document updateDoc = new Document("$set", new Document()
                        .append("firstName", firstName)
                        .append("lastName", lastName)
                        .append("updatedAt", LocalDateTime.now()));

                collection.updateOne(new Document("ci", userCi), updateDoc);

            } else {
                // Create new user
                logger.info("Creating new INUS user: {}", userCi);

                inusId = "inus-" + java.util.UUID.randomUUID().toString();
                firstName = (String) claims.get("primer_nombre");
                lastName = (String) claims.get("primer_apellido");

                Document newUser = new Document()
                        .append("_id", userCi)
                        .append("inusId", inusId)
                        .append("ci", userCi)
                        .append("firstName", firstName)
                        .append("lastName", lastName)
                        .append("status", "ACTIVE")
                        .append("createdAt", LocalDateTime.now())
                        .append("updatedAt", LocalDateTime.now());

                collection.insertOne(newUser);
            }

            return new UserInfoDTO(userCi, inusId, firstName, lastName, "PATIENT");

        } catch (Exception e) {
            logger.error("Failed to create/update INUS user", e);
            throw new AuthenticationException("Failed to create/update user", e);
        }
    }

    /**
     * Stores refresh token in MongoDB
     */
    private void storeRefreshToken(String refreshToken, String userCi, ClientType clientType, String deviceId) {
        try {
            String tokenHash = hashToken(refreshToken);

            RefreshToken token = new RefreshToken(
                    tokenHash,
                    userCi,
                    clientType,
                    deviceId,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(30) // 30-day expiration
            );

            refreshTokenRepo.save(token);

            logger.debug("Stored refresh token for user: {}", userCi);

        } catch (Exception e) {
            logger.error("Failed to store refresh token", e);
            throw new AuthenticationException("Failed to store refresh token", e);
        }
    }

    /**
     * Hashes token using SHA-256
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    /**
     * Gets INUS ID for user from MongoDB
     */
    private String getInusIdForUser(String userCi) {
        MongoCollection<Document> collection = mongoDatabase.getCollection("inus_users");
        Document user = collection.find(new Document("ci", userCi)).first();

        if (user == null) {
            throw new AuthenticationException("User not found: " + userCi);
        }

        return user.getString("inusId");
    }

    /**
     * Gets user info for user
     */
    private UserInfoDTO getUserInfo(String userCi, String inusId) {
        MongoCollection<Document> collection = mongoDatabase.getCollection("inus_users");
        Document user = collection.find(new Document("ci", userCi)).first();

        if (user == null) {
            throw new AuthenticationException("User not found: " + userCi);
        }

        return new UserInfoDTO(
                userCi,
                inusId,
                user.getString("firstName"),
                user.getString("lastName"),
                "PATIENT"
        );
    }

    /**
     * Logs authentication event to audit collection
     */
    private void logAuthenticationEvent(String userCi, ClientType clientType, boolean success, String reason) {
        try {
            MongoCollection<Document> collection = mongoDatabase.getCollection("audit_logs");

            Document event = new Document()
                    .append("eventType", success ? "AUTHENTICATION_SUCCESS" : "AUTHENTICATION_FAILURE")
                    .append("userCi", userCi)
                    .append("clientType", clientType != null ? clientType.name() : null)
                    .append("timestamp", LocalDateTime.now())
                    .append("success", success);

            if (reason != null) {
                event.append("reason", reason);
            }

            collection.insertOne(event);

            logger.debug("Logged authentication event for user: {}", userCi);

        } catch (Exception e) {
            logger.error("Failed to log authentication event", e);
            // Don't throw exception - logging failure shouldn't break authentication
        }
    }
}
