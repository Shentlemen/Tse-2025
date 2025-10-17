package uy.gub.hcen.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import uy.gub.hcen.auth.config.JwtConfiguration;
import uy.gub.hcen.auth.exception.InvalidTokenException;
import uy.gub.hcen.auth.exception.TokenExpiredException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Service for generating and validating HCEN-issued JWT tokens.
 *
 * This service creates both access tokens and refresh tokens:
 * - Access tokens: Short-lived (1 hour), used for API authentication
 * - Refresh tokens: Long-lived (30 days), used to obtain new access tokens
 *
 * All tokens are signed using the configured algorithm (HS256 or RS256).
 */
@ApplicationScoped
public class JwtTokenService {

    private static final Logger LOGGER = Logger.getLogger(JwtTokenService.class.getName());

    @Inject
    private JwtConfiguration jwtConfig;

    /**
     * Generates an access token for the authenticated user.
     *
     * @param ci User's CI (Cédula de Identidad)
     * @param inusId User's INUS ID
     * @param role User's role (PATIENT, PROFESSIONAL, ADMIN)
     * @param additionalClaims Optional additional claims
     * @return Signed JWT access token
     */
    public String generateAccessToken(String ci, String inusId, String role,
                                       Map<String, Object> additionalClaims) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(jwtConfig.getAccessTokenTtl());

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", ci);                    // Subject: CI
        claims.put("inusId", inusId);              // INUS unique identifier
        claims.put("role", role);                  // User role
        claims.put("tokenType", "access");         // Distinguish from refresh token

        if (additionalClaims != null) {
            claims.putAll(additionalClaims);
        }

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(jwtConfig.getIssuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .setId(UUID.randomUUID().toString())  // Unique token ID (for revocation)
                .signWith(jwtConfig.getSigningKey(), jwtConfig.getAlgorithm())
                .compact();
    }

    /**
     * Generates a refresh token for the authenticated user.
     *
     * Refresh tokens contain minimal claims and are used only for obtaining new access tokens.
     *
     * @param ci User's CI
     * @param inusId User's INUS ID
     * @return Signed JWT refresh token
     */
    public String generateRefreshToken(String ci, String inusId) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(jwtConfig.getRefreshTokenTtl());

        return Jwts.builder()
                .setSubject(ci)
                .claim("inusId", inusId)
                .claim("tokenType", "refresh")
                .setIssuer(jwtConfig.getIssuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .setId(UUID.randomUUID().toString())
                .signWith(jwtConfig.getSigningKey(), jwtConfig.getAlgorithm())
                .compact();
    }

    /**
     * Validates a JWT token and returns its claims.
     *
     * @param token The JWT token to validate
     * @return Claims from the token
     * @throws InvalidTokenException if the token is invalid
     * @throws TokenExpiredException if the token has expired
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(jwtConfig.getSigningKey())
                    .requireIssuer(jwtConfig.getIssuer())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            LOGGER.warning("Token expired: " + e.getClaims().getId());
            throw new TokenExpiredException("Token has expired");

        } catch (UnsupportedJwtException e) {
            LOGGER.warning("Unsupported JWT token: " + e.getMessage());
            throw new InvalidTokenException("Unsupported JWT token");

        } catch (MalformedJwtException e) {
            LOGGER.warning("Malformed JWT token: " + e.getMessage());
            throw new InvalidTokenException("Malformed JWT token");

        } catch (SignatureException e) {
            LOGGER.warning("Invalid JWT signature: " + e.getMessage());
            throw new InvalidTokenException("Invalid token signature");

        } catch (IllegalArgumentException e) {
            LOGGER.warning("JWT claims string is empty: " + e.getMessage());
            throw new InvalidTokenException("Invalid token");
        }
    }

    /**
     * Validates a token and ensures it's an access token.
     *
     * @param token The token to validate
     * @return Claims from the access token
     * @throws InvalidTokenException if not an access token
     */
    public Claims validateAccessToken(String token) {
        Claims claims = validateToken(token);

        String tokenType = claims.get("tokenType", String.class);
        if (!"access".equals(tokenType)) {
            throw new InvalidTokenException("Token is not an access token");
        }

        return claims;
    }

    /**
     * Validates a token and ensures it's a refresh token.
     *
     * @param token The token to validate
     * @return Claims from the refresh token
     * @throws InvalidTokenException if not a refresh token
     */
    public Claims validateRefreshToken(String token) {
        Claims claims = validateToken(token);

        String tokenType = claims.get("tokenType", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new InvalidTokenException("Token is not a refresh token");
        }

        return claims;
    }

    /**
     * Extracts the subject (CI) from a token without full validation.
     * Useful for logging or when the token is already known to be invalid.
     *
     * @param token The JWT token
     * @return The subject claim, or null if it can't be extracted
     */
    public String extractSubjectUnsafe(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            // Simple extraction without full parsing
            if (payload.contains("\"sub\"")) {
                int start = payload.indexOf("\"sub\":\"") + 7;
                int end = payload.indexOf("\"", start);
                return payload.substring(start, end);
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to extract subject from token: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets the token ID (jti claim) from a token.
     *
     * @param token The JWT token
     * @return Token ID
     */
    public String getTokenId(String token) {
        Claims claims = validateToken(token);
        return claims.getId();
    }

    /**
     * Gets the expiration time of a token.
     *
     * @param token The JWT token
     * @return Expiration timestamp
     */
    public Date getExpirationDate(String token) {
        Claims claims = validateToken(token);
        return claims.getExpiration();
    }

    /**
     * Gets the remaining time until token expiration in seconds.
     *
     * @param token The JWT token
     * @return Remaining seconds, or 0 if expired
     */
    public long getRemainingSeconds(String token) {
        try {
            Claims claims = validateToken(token);
            long expirationMillis = claims.getExpiration().getTime();
            long nowMillis = System.currentTimeMillis();
            long remaining = (expirationMillis - nowMillis) / 1000;
            return Math.max(0, remaining);
        } catch (TokenExpiredException e) {
            return 0;
        }
    }

    /**
     * Checks if a token is expired.
     *
     * @param token The JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            validateToken(token);
            return false;
        } catch (TokenExpiredException e) {
            return true;
        } catch (InvalidTokenException e) {
            return true; // Invalid tokens are treated as expired
        }
    }

    /**
     * Extracts user information from an access token.
     *
     * @param token The access token
     * @return Map with user info (ci, inusId, role)
     */
    public Map<String, Object> extractUserInfo(String token) {
        Claims claims = validateAccessToken(token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("ci", claims.getSubject());
        userInfo.put("inusId", claims.get("inusId", String.class));
        userInfo.put("role", claims.get("role", String.class));

        return userInfo;
    }
}
