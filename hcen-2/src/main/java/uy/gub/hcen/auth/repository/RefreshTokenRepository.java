package uy.gub.hcen.auth.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.hcen.auth.entity.RefreshToken;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Refresh Token Repository - MongoDB Implementation
 *
 * Manages refresh token persistence in MongoDB for the HCEN authentication system.
 * Provides CRUD operations and specialized queries for token management.
 *
 * Collection: refresh_tokens
 *
 * Indexes:
 * - tokenHash (unique) - Fast token lookup
 * - userCi - User token queries
 * - expiresAt - TTL index for automatic expiration
 * - isRevoked - Query optimization
 *
 * Operations:
 * - save: Store new refresh token
 * - findByTokenHash: Retrieve token by hash (authentication)
 * - findByUserCi: Get all tokens for a user (session management)
 * - revokeAllForUser: Revoke all user tokens (logout)
 * - deleteExpired: Cleanup expired tokens (scheduled job)
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-15
 */
@ApplicationScoped
public class RefreshTokenRepository {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenRepository.class);

    private static final String COLLECTION_NAME = "refresh_tokens";

    @Inject
    private MongoDatabase mongoDatabase;

    /**
     * Saves a refresh token to MongoDB
     *
     * @param token RefreshToken to save
     * @throws IllegalArgumentException if token is null or invalid
     */
    public void save(RefreshToken token) {
        if (token == null) {
            throw new IllegalArgumentException("RefreshToken cannot be null");
        }

        if (token.getTokenHash() == null || token.getTokenHash().trim().isEmpty()) {
            throw new IllegalArgumentException("Token hash cannot be null or empty");
        }

        if (token.getUserCi() == null || token.getUserCi().trim().isEmpty()) {
            throw new IllegalArgumentException("User CI cannot be null or empty");
        }

        try {
            MongoCollection<Document> collection = getCollection();
            Document doc = token.toDocument();

            collection.insertOne(doc);

            // Update the token with the generated ID
            token.setId(doc.getObjectId("_id"));

            logger.debug("Saved refresh token for user: {} with client type: {}",
                    token.getUserCi(), token.getClientType());
        } catch (Exception e) {
            logger.error("Failed to save refresh token for user: {}", token.getUserCi(), e);
            throw new RuntimeException("Failed to save refresh token", e);
        }
    }

    /**
     * Finds a refresh token by its hash
     *
     * @param tokenHash SHA-256 hash of the token
     * @return Optional containing the token if found, empty otherwise
     */
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        if (tokenHash == null || tokenHash.trim().isEmpty()) {
            logger.warn("Attempted to find token with null or empty hash");
            return Optional.empty();
        }

        try {
            MongoCollection<Document> collection = getCollection();
            Bson filter = Filters.eq("tokenHash", tokenHash);

            Document doc = collection.find(filter).first();

            if (doc != null) {
                RefreshToken token = RefreshToken.fromDocument(doc);
                logger.debug("Found refresh token for user: {}", token.getUserCi());
                return Optional.of(token);
            }

            logger.debug("No refresh token found with hash: {}", tokenHash.substring(0, 10) + "...");
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Failed to find refresh token by hash", e);
            throw new RuntimeException("Failed to find refresh token", e);
        }
    }

    /**
     * Finds all refresh tokens for a specific user
     *
     * @param userCi User's CI
     * @return List of refresh tokens (empty if none found)
     */
    public List<RefreshToken> findByUserCi(String userCi) {
        if (userCi == null || userCi.trim().isEmpty()) {
            logger.warn("Attempted to find tokens with null or empty user CI");
            return new ArrayList<>();
        }

        try {
            MongoCollection<Document> collection = getCollection();
            Bson filter = Filters.eq("userCi", userCi);

            List<RefreshToken> tokens = new ArrayList<>();

            collection.find(filter).forEach(doc -> {
                RefreshToken token = RefreshToken.fromDocument(doc);
                if (token != null) {
                    tokens.add(token);
                }
            });

            logger.debug("Found {} refresh tokens for user: {}", tokens.size(), userCi);
            return tokens;
        } catch (Exception e) {
            logger.error("Failed to find refresh tokens for user: {}", userCi, e);
            throw new RuntimeException("Failed to find refresh tokens for user", e);
        }
    }

    /**
     * Finds all valid (non-revoked, non-expired) tokens for a user
     *
     * @param userCi User's CI
     * @return List of valid refresh tokens
     */
    public List<RefreshToken> findValidByUserCi(String userCi) {
        if (userCi == null || userCi.trim().isEmpty()) {
            logger.warn("Attempted to find valid tokens with null or empty user CI");
            return new ArrayList<>();
        }

        try {
            MongoCollection<Document> collection = getCollection();

            Bson filter = Filters.and(
                    Filters.eq("userCi", userCi),
                    Filters.eq("isRevoked", false),
                    Filters.gt("expiresAt", new Date())
            );

            List<RefreshToken> tokens = new ArrayList<>();

            collection.find(filter).forEach(doc -> {
                RefreshToken token = RefreshToken.fromDocument(doc);
                if (token != null) {
                    tokens.add(token);
                }
            });

            logger.debug("Found {} valid refresh tokens for user: {}", tokens.size(), userCi);
            return tokens;
        } catch (Exception e) {
            logger.error("Failed to find valid refresh tokens for user: {}", userCi, e);
            throw new RuntimeException("Failed to find valid refresh tokens", e);
        }
    }

    /**
     * Revokes all refresh tokens for a specific user
     * Used for logout and security incidents
     *
     * @param userCi User's CI
     * @return Number of tokens revoked
     */
    public long revokeAllForUser(String userCi) {
        if (userCi == null || userCi.trim().isEmpty()) {
            throw new IllegalArgumentException("User CI cannot be null or empty");
        }

        try {
            MongoCollection<Document> collection = getCollection();

            Bson filter = Filters.and(
                    Filters.eq("userCi", userCi),
                    Filters.eq("isRevoked", false)
            );

            Bson update = Updates.combine(
                    Updates.set("isRevoked", true),
                    Updates.set("revokedAt", new Date())
            );

            UpdateResult result = collection.updateMany(filter, update);
            long modifiedCount = result.getModifiedCount();

            logger.info("Revoked {} refresh tokens for user: {}", modifiedCount, userCi);
            return modifiedCount;
        } catch (Exception e) {
            logger.error("Failed to revoke refresh tokens for user: {}", userCi, e);
            throw new RuntimeException("Failed to revoke refresh tokens", e);
        }
    }

    /**
     * Revokes a specific refresh token
     *
     * @param tokenHash SHA-256 hash of the token
     * @return true if token was revoked, false if not found
     */
    public boolean revokeToken(String tokenHash) {
        if (tokenHash == null || tokenHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Token hash cannot be null or empty");
        }

        try {
            MongoCollection<Document> collection = getCollection();

            Bson filter = Filters.and(
                    Filters.eq("tokenHash", tokenHash),
                    Filters.eq("isRevoked", false)
            );

            Bson update = Updates.combine(
                    Updates.set("isRevoked", true),
                    Updates.set("revokedAt", new Date())
            );

            UpdateResult result = collection.updateOne(filter, update);
            boolean revoked = result.getModifiedCount() > 0;

            if (revoked) {
                logger.info("Revoked refresh token: {}", tokenHash.substring(0, 10) + "...");
            } else {
                logger.debug("Token not found or already revoked: {}", tokenHash.substring(0, 10) + "...");
            }

            return revoked;
        } catch (Exception e) {
            logger.error("Failed to revoke refresh token", e);
            throw new RuntimeException("Failed to revoke refresh token", e);
        }
    }

    /**
     * Deletes all expired refresh tokens
     * Should be called by a scheduled cleanup job
     *
     * @return Number of tokens deleted
     */
    public long deleteExpired() {
        try {
            MongoCollection<Document> collection = getCollection();

            Bson filter = Filters.lt("expiresAt", new Date());

            DeleteResult result = collection.deleteMany(filter);
            long deletedCount = result.getDeletedCount();

            if (deletedCount > 0) {
                logger.info("Deleted {} expired refresh tokens", deletedCount);
            } else {
                logger.debug("No expired refresh tokens to delete");
            }

            return deletedCount;
        } catch (Exception e) {
            logger.error("Failed to delete expired refresh tokens", e);
            throw new RuntimeException("Failed to delete expired tokens", e);
        }
    }

    /**
     * Deletes all revoked tokens older than a specified number of days
     * Useful for cleanup and compliance with data retention policies
     *
     * @param daysOld Number of days to retain revoked tokens
     * @return Number of tokens deleted
     */
    public long deleteOldRevokedTokens(int daysOld) {
        if (daysOld < 0) {
            throw new IllegalArgumentException("Days old must be non-negative");
        }

        try {
            MongoCollection<Document> collection = getCollection();

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
            Date cutoffDateAsDate = Date.from(cutoffDate.atZone(ZoneId.systemDefault()).toInstant());

            Bson filter = Filters.and(
                    Filters.eq("isRevoked", true),
                    Filters.lt("revokedAt", cutoffDateAsDate)
            );

            DeleteResult result = collection.deleteMany(filter);
            long deletedCount = result.getDeletedCount();

            if (deletedCount > 0) {
                logger.info("Deleted {} old revoked tokens (older than {} days)", deletedCount, daysOld);
            }

            return deletedCount;
        } catch (Exception e) {
            logger.error("Failed to delete old revoked tokens", e);
            throw new RuntimeException("Failed to delete old revoked tokens", e);
        }
    }

    /**
     * Counts the number of active (non-revoked, non-expired) tokens for a user
     *
     * @param userCi User's CI
     * @return Number of active tokens
     */
    public long countActiveTokensForUser(String userCi) {
        if (userCi == null || userCi.trim().isEmpty()) {
            return 0;
        }

        try {
            MongoCollection<Document> collection = getCollection();

            Bson filter = Filters.and(
                    Filters.eq("userCi", userCi),
                    Filters.eq("isRevoked", false),
                    Filters.gt("expiresAt", new Date())
            );

            long count = collection.countDocuments(filter);

            logger.debug("User {} has {} active refresh tokens", userCi, count);
            return count;
        } catch (Exception e) {
            logger.error("Failed to count active tokens for user: {}", userCi, e);
            throw new RuntimeException("Failed to count active tokens", e);
        }
    }

    /**
     * Gets the MongoDB collection for refresh tokens
     *
     * @return MongoCollection for refresh_tokens
     */
    private MongoCollection<Document> getCollection() {
        return mongoDatabase.getCollection(COLLECTION_NAME);
    }
}
