package uy.gub.hcen.auth.entity;

import org.bson.Document;
import org.bson.types.ObjectId;
import uy.gub.hcen.auth.config.OidcConfiguration.ClientType;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * Refresh Token Entity (MongoDB Document)
 *
 * Represents a refresh token stored in MongoDB for HCEN authentication system.
 * Refresh tokens enable long-lived sessions without requiring users to
 * re-authenticate frequently.
 *
 * Security Features:
 * - Tokens are stored as SHA-256 hashes (never plaintext)
 * - Support for token rotation (single-use refresh tokens)
 * - Revocation support (logout, security breach)
 * - Expiration tracking (30-day TTL)
 * - Device tracking (for multi-device sessions)
 *
 * MongoDB Collection: refresh_tokens
 *
 * Document Structure:
 * {
 *   "_id": ObjectId("..."),
 *   "tokenHash": "sha256:...",
 *   "userCi": "12345678",
 *   "clientType": "MOBILE",
 *   "deviceId": "device-uuid-123",
 *   "issuedAt": ISODate("2025-10-15T10:00:00Z"),
 *   "expiresAt": ISODate("2025-11-14T10:00:00Z"),
 *   "revokedAt": null,
 *   "isRevoked": false
 * }
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-15
 */
public class RefreshToken {

    // MongoDB unique identifier
    private ObjectId id;

    // SHA-256 hash of the actual refresh token (never store plaintext)
    private String tokenHash;

    // User's Cedula de Identidad (CI) - national ID
    private String userCi;

    // Client type that issued this token
    private ClientType clientType;

    // Device identifier (for mobile clients, null for web)
    private String deviceId;

    // Token issuance timestamp
    private LocalDateTime issuedAt;

    // Token expiration timestamp (30 days from issuance)
    private LocalDateTime expiresAt;

    // Token revocation timestamp (null if not revoked)
    private LocalDateTime revokedAt;

    // Revocation flag (quick check without date comparison)
    private boolean isRevoked;

    /**
     * Default constructor (required for object construction)
     */
    public RefreshToken() {
        this.isRevoked = false;
    }

    /**
     * Full constructor for creating new refresh tokens
     *
     * @param tokenHash SHA-256 hash of the token
     * @param userCi User's CI
     * @param clientType Client type
     * @param deviceId Device identifier (can be null)
     * @param issuedAt Issuance timestamp
     * @param expiresAt Expiration timestamp
     */
    public RefreshToken(String tokenHash, String userCi, ClientType clientType,
                        String deviceId, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        this.tokenHash = tokenHash;
        this.userCi = userCi;
        this.clientType = clientType;
        this.deviceId = deviceId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.isRevoked = false;
    }

    /**
     * Converts this RefreshToken to a MongoDB Document for storage
     *
     * @return MongoDB Document representation
     */
    public Document toDocument() {
        Document doc = new Document();

        if (id != null) {
            doc.append("_id", id);
        }

        doc.append("tokenHash", tokenHash)
           .append("userCi", userCi)
           .append("clientType", clientType != null ? clientType.name() : null)
           .append("deviceId", deviceId)
           .append("issuedAt", toDate(issuedAt))
           .append("expiresAt", toDate(expiresAt))
           .append("revokedAt", toDate(revokedAt))
           .append("isRevoked", isRevoked);

        return doc;
    }

    /**
     * Creates a RefreshToken from a MongoDB Document
     *
     * @param doc MongoDB Document
     * @return RefreshToken instance
     */
    public static RefreshToken fromDocument(Document doc) {
        if (doc == null) {
            return null;
        }

        RefreshToken token = new RefreshToken();

        token.id = doc.getObjectId("_id");
        token.tokenHash = doc.getString("tokenHash");
        token.userCi = doc.getString("userCi");

        String clientTypeStr = doc.getString("clientType");
        token.clientType = clientTypeStr != null ? ClientType.valueOf(clientTypeStr) : null;

        token.deviceId = doc.getString("deviceId");
        token.issuedAt = toLocalDateTime(doc.getDate("issuedAt"));
        token.expiresAt = toLocalDateTime(doc.getDate("expiresAt"));
        token.revokedAt = toLocalDateTime(doc.getDate("revokedAt"));
        token.isRevoked = doc.getBoolean("isRevoked", false);

        return token;
    }

    /**
     * Checks if this token is expired
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Checks if this token is valid (not revoked and not expired)
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return !isRevoked && !isExpired();
    }

    /**
     * Revokes this token
     */
    public void revoke() {
        this.isRevoked = true;
        this.revokedAt = LocalDateTime.now();
    }

    // Utility methods for date conversion

    /**
     * Converts LocalDateTime to Date for MongoDB storage
     */
    private static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Converts Date from MongoDB to LocalDateTime
     */
    private static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // Getters and Setters

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public String getUserCi() {
        return userCi;
    }

    public void setUserCi(String userCi) {
        this.userCi = userCi;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public boolean isRevoked() {
        return isRevoked;
    }

    public void setRevoked(boolean revoked) {
        isRevoked = revoked;
    }

    // Equals, HashCode, and ToString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshToken that = (RefreshToken) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(tokenHash, that.tokenHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tokenHash);
    }

    @Override
    public String toString() {
        return "RefreshToken{" +
               "id=" + id +
               ", userCi='" + userCi + '\'' +
               ", clientType=" + clientType +
               ", deviceId='" + deviceId + '\'' +
               ", issuedAt=" + issuedAt +
               ", expiresAt=" + expiresAt +
               ", isRevoked=" + isRevoked +
               '}';
    }
}
