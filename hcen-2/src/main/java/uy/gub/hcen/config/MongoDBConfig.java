package uy.gub.hcen.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * MongoDB configuration and connection management
 * Provides MongoDatabase instance for audit logs, notifications, and system events
 */
@ApplicationScoped
public class MongoDBConfig {

    private static final Logger LOGGER = Logger.getLogger(MongoDBConfig.class.getName());

    private MongoClient mongoClient;
    private Properties properties;

    @PostConstruct
    public void init() {
        try {
            properties = loadProperties();
            String uri = properties.getProperty("mongodb.uri");

            ConnectionString connectionString = new ConnectionString(uri);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();

            mongoClient = MongoClients.create(settings);

            LOGGER.info("MongoDB connection initialized successfully");
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize MongoDB connection: " + e.getMessage());
            throw new RuntimeException("MongoDB initialization failed", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (mongoClient != null) {
            mongoClient.close();
            LOGGER.info("MongoDB connection closed");
        }
    }

    @Produces
    @ApplicationScoped
    public MongoDatabase getDatabase() {
        String databaseName = properties.getProperty("mongodb.database", "hcen_audit");
        return mongoClient.getDatabase(databaseName);
    }

    public String getCollectionName(String key) {
        return properties.getProperty(key);
    }

    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("mongodb.properties")) {
            if (input == null) {
                throw new IOException("mongodb.properties not found in classpath");
            }
            props.load(input);
        }
        return props;
    }
}
