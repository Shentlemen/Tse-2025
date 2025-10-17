package uy.gub.hcen.config;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MongoDB Indexes Initializer
 *
 * Creates MongoDB indexes for HCEN collections on application startup.
 * Ensures optimal query performance for authentication and data access operations.
 *
 * Collections and Indexes:
 *
 * 1. refresh_tokens:
 *    - tokenHash (unique) - Fast token lookup
 *    - userCi - User session queries
 *    - expiresAt - TTL index for automatic cleanup
 *    - isRevoked - Query optimization
 *
 * 2. inus_users:
 *    - ci (unique) - Primary user lookup
 *    - email - Secondary lookup
 *    - inusId (unique) - Cross-clinic identifier
 *
 * 3. authentication_sessions:
 *    - sessionId (unique) - Session lookup
 *    - userCi - User session queries
 *    - expiresAt - TTL index for automatic cleanup
 *
 * 4. audit_logs:
 *    - userCi - User audit queries
 *    - timestamp - Time-based queries
 *    - eventType - Event filtering
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-15
 */
@Singleton
@Startup
public class MongoIndexesInitializer {

    private static final Logger logger = LoggerFactory.getLogger(MongoIndexesInitializer.class);

    @Inject
    private MongoDatabase mongoDatabase;

    /**
     * Creates all MongoDB indexes on application startup
     */
    @PostConstruct
    public void createIndexes() {
        logger.info("Starting MongoDB indexes creation...");

        try {
            createRefreshTokensIndexes();
            createInusUsersIndexes();
            createAuthenticationSessionsIndexes();
            createAuditLogsIndexes();

            logger.info("MongoDB indexes created successfully");
        } catch (Exception e) {
            logger.error("Failed to create MongoDB indexes", e);
            // Don't throw exception - application can still run without indexes
            // (though performance will be degraded)
        }
    }

    /**
     * Creates indexes for refresh_tokens collection
     */
    private void createRefreshTokensIndexes() {
        try {
            logger.debug("Creating indexes for refresh_tokens collection");

            MongoCollection<Document> collection = mongoDatabase.getCollection("refresh_tokens");

            // Unique index on tokenHash for fast token lookup
            collection.createIndex(
                    Indexes.ascending("tokenHash"),
                    new IndexOptions().unique(true).name("idx_tokenHash_unique")
            );

            // Index on userCi for user session queries
            collection.createIndex(
                    Indexes.ascending("userCi"),
                    new IndexOptions().name("idx_userCi")
            );

            // TTL index on expiresAt for automatic cleanup
            // MongoDB will automatically delete documents when expiresAt < current time
            collection.createIndex(
                    Indexes.ascending("expiresAt"),
                    new IndexOptions()
                            .expireAfter(0L, java.util.concurrent.TimeUnit.SECONDS)
                            .name("idx_expiresAt_ttl")
            );

            // Index on isRevoked for query optimization
            collection.createIndex(
                    Indexes.ascending("isRevoked"),
                    new IndexOptions().name("idx_isRevoked")
            );

            // Compound index for common queries (userCi + isRevoked)
            collection.createIndex(
                    Indexes.compoundIndex(
                            Indexes.ascending("userCi"),
                            Indexes.ascending("isRevoked")
                    ),
                    new IndexOptions().name("idx_userCi_isRevoked")
            );

            logger.info("Created indexes for refresh_tokens collection");
        } catch (Exception e) {
            logger.error("Failed to create refresh_tokens indexes", e);
        }
    }

    /**
     * Creates indexes for inus_users collection
     */
    private void createInusUsersIndexes() {
        try {
            logger.debug("Creating indexes for inus_users collection");

            MongoCollection<Document> collection = mongoDatabase.getCollection("inus_users");

            // Unique index on ci (primary identifier)
            collection.createIndex(
                    Indexes.ascending("ci"),
                    new IndexOptions().unique(true).name("idx_ci_unique")
            );

            // Unique index on inusId (cross-clinic identifier)
            collection.createIndex(
                    Indexes.ascending("inusId"),
                    new IndexOptions().unique(true).name("idx_inusId_unique")
            );

            // Index on email for user lookup
            collection.createIndex(
                    Indexes.ascending("email"),
                    new IndexOptions().name("idx_email")
            );

            // Index on status for filtering active users
            collection.createIndex(
                    Indexes.ascending("status"),
                    new IndexOptions().name("idx_status")
            );

            logger.info("Created indexes for inus_users collection");
        } catch (Exception e) {
            logger.error("Failed to create inus_users indexes", e);
        }
    }

    /**
     * Creates indexes for authentication_sessions collection
     */
    private void createAuthenticationSessionsIndexes() {
        try {
            logger.debug("Creating indexes for authentication_sessions collection");

            MongoCollection<Document> collection = mongoDatabase.getCollection("authentication_sessions");

            // Unique index on sessionId for fast session lookup
            collection.createIndex(
                    Indexes.ascending("sessionId"),
                    new IndexOptions().unique(true).name("idx_sessionId_unique")
            );

            // Index on userCi for user session queries
            collection.createIndex(
                    Indexes.ascending("userCi"),
                    new IndexOptions().name("idx_userCi")
            );

            // TTL index on expiresAt for automatic cleanup
            collection.createIndex(
                    Indexes.ascending("expiresAt"),
                    new IndexOptions()
                            .expireAfter(0L, java.util.concurrent.TimeUnit.SECONDS)
                            .name("idx_expiresAt_ttl")
            );

            logger.info("Created indexes for authentication_sessions collection");
        } catch (Exception e) {
            logger.error("Failed to create authentication_sessions indexes", e);
        }
    }

    /**
     * Creates indexes for audit_logs collection
     */
    private void createAuditLogsIndexes() {
        try {
            logger.debug("Creating indexes for audit_logs collection");

            MongoCollection<Document> collection = mongoDatabase.getCollection("audit_logs");

            // Index on userCi for user audit queries
            collection.createIndex(
                    Indexes.ascending("userCi"),
                    new IndexOptions().name("idx_userCi")
            );

            // Index on timestamp for time-based queries (descending for recent-first)
            collection.createIndex(
                    Indexes.descending("timestamp"),
                    new IndexOptions().name("idx_timestamp_desc")
            );

            // Index on eventType for event filtering
            collection.createIndex(
                    Indexes.ascending("eventType"),
                    new IndexOptions().name("idx_eventType")
            );

            // Compound index for common audit queries (userCi + timestamp)
            collection.createIndex(
                    Indexes.compoundIndex(
                            Indexes.ascending("userCi"),
                            Indexes.descending("timestamp")
                    ),
                    new IndexOptions().name("idx_userCi_timestamp")
            );

            // Compound index for event type queries (eventType + timestamp)
            collection.createIndex(
                    Indexes.compoundIndex(
                            Indexes.ascending("eventType"),
                            Indexes.descending("timestamp")
                    ),
                    new IndexOptions().name("idx_eventType_timestamp")
            );

            logger.info("Created indexes for audit_logs collection");
        } catch (Exception e) {
            logger.error("Failed to create audit_logs indexes", e);
        }
    }

    /**
     * Drops all indexes (useful for development/testing)
     * WARNING: Do not call in production without careful consideration
     */
    public void dropAllIndexes() {
        logger.warn("Dropping all MongoDB indexes (except _id)");

        try {
            mongoDatabase.getCollection("refresh_tokens").dropIndexes();
            mongoDatabase.getCollection("inus_users").dropIndexes();
            mongoDatabase.getCollection("authentication_sessions").dropIndexes();
            mongoDatabase.getCollection("audit_logs").dropIndexes();

            logger.info("Dropped all MongoDB indexes");
        } catch (Exception e) {
            logger.error("Failed to drop MongoDB indexes", e);
        }
    }

    /**
     * Recreates all indexes (useful for index updates)
     */
    public void recreateIndexes() {
        logger.info("Recreating all MongoDB indexes");

        dropAllIndexes();
        createIndexes();

        logger.info("Recreated all MongoDB indexes");
    }
}
