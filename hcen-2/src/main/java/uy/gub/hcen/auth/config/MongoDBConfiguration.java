package uy.gub.hcen.auth.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * MongoDB Configuration for HCEN Authentication System
 *
 * Provides MongoDB connectivity for:
 * - Refresh token storage (authentication_sessions collection)
 * - INUS user data (inus_users collection)
 * - Audit logs (audit_logs collection)
 *
 * Uses MongoDB Java Driver (Sync) instead of JPA for flexibility and performance.
 *
 * Collections:
 * - refresh_tokens: Stores hashed refresh tokens with metadata
 * - authentication_sessions: Session tracking
 * - inus_users: National health user registry
 * - rndc_documents: Document metadata registry
 * - access_policies: User access control policies
 * - audit_logs: Comprehensive audit trail
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-15
 */
@ApplicationScoped
public class MongoDBConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBConfiguration.class);

    // Configuration property keys
    private static final String MONGO_URI_KEY = "mongodb.uri";
    private static final String MONGO_DATABASE_KEY = "mongodb.database";

    // Default values
    private static final String DEFAULT_MONGO_URI = "mongodb://localhost:27017";
    private static final String DEFAULT_DATABASE = "hcen";

    // MongoDB client instance
    private MongoClient mongoClient;

    // Configuration properties
    private String mongoUri;
    private String databaseName;

    /**
     * Initializes MongoDB configuration by loading properties from application.properties
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing MongoDB configuration...");

        try {
            loadConfiguration();
            createMongoClient();
            logger.info("MongoDB configuration initialized successfully");
            logger.info("Connected to MongoDB at: {} / Database: {}", mongoUri, databaseName);
        } catch (Exception e) {
            logger.error("Failed to initialize MongoDB configuration", e);
            throw new RuntimeException("MongoDB configuration initialization failed", e);
        }
    }

    /**
     * Loads MongoDB configuration from application.properties
     */
    private void loadConfiguration() throws IOException {
        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.warn("application.properties not found, using default MongoDB configuration");
                useDefaultConfiguration();
                return;
            }

            properties.load(input);

            mongoUri = properties.getProperty(MONGO_URI_KEY, DEFAULT_MONGO_URI);
            databaseName = properties.getProperty(MONGO_DATABASE_KEY, DEFAULT_DATABASE);

            // Validate configuration
            if (mongoUri == null || mongoUri.trim().isEmpty()) {
                logger.warn("MongoDB URI is empty, using default: {}", DEFAULT_MONGO_URI);
                mongoUri = DEFAULT_MONGO_URI;
            }

            if (databaseName == null || databaseName.trim().isEmpty()) {
                logger.warn("MongoDB database name is empty, using default: {}", DEFAULT_DATABASE);
                databaseName = DEFAULT_DATABASE;
            }

            logger.debug("Loaded MongoDB configuration: URI={}, Database={}", mongoUri, databaseName);
        }
    }

    /**
     * Uses default configuration values
     */
    private void useDefaultConfiguration() {
        this.mongoUri = DEFAULT_MONGO_URI;
        this.databaseName = DEFAULT_DATABASE;
        logger.info("Using default MongoDB configuration: {}:{}", mongoUri, databaseName);
    }

    /**
     * Creates MongoDB client instance
     */
    private void createMongoClient() {
        try {
            this.mongoClient = MongoClients.create(mongoUri);

            // Test connection by pinging the database
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            database.runCommand(new org.bson.Document("ping", 1));

            logger.info("MongoDB connection established successfully");
        } catch (Exception e) {
            logger.error("Failed to create MongoDB client", e);
            throw new RuntimeException("MongoDB client creation failed", e);
        }
    }

    /**
     * Produces MongoClient bean for CDI injection
     *
     * @return MongoClient instance
     */
    @Produces
    @ApplicationScoped
    public MongoClient getMongoClient() {
        if (mongoClient == null) {
            throw new IllegalStateException("MongoClient not initialized");
        }
        return mongoClient;
    }

    /**
     * Produces MongoDatabase bean for CDI injection
     *
     * @return MongoDatabase instance for HCEN database
     */
    @Produces
    @ApplicationScoped
    public MongoDatabase getMongoDatabase() {
        if (mongoClient == null) {
            throw new IllegalStateException("MongoClient not initialized");
        }
        return mongoClient.getDatabase(databaseName);
    }

    /**
     * Returns the configured database name
     *
     * @return Database name
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Returns the configured MongoDB URI
     *
     * @return MongoDB connection URI
     */
    public String getMongoUri() {
        return mongoUri;
    }

    /**
     * Closes MongoDB client connection on application shutdown
     */
    @PreDestroy
    public void cleanup() {
        if (mongoClient != null) {
            try {
                logger.info("Closing MongoDB connection...");
                mongoClient.close();
                logger.info("MongoDB connection closed successfully");
            } catch (Exception e) {
                logger.error("Error closing MongoDB connection", e);
            }
        }
    }
}
