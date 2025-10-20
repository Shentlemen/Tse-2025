package uy.gub.hcen.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Redis Configuration for distributed caching and session management.
 *
 * Redis is used for:
 * - Session Storage (Database 0): JWT session data
 * - Cache Storage (Database 1): Policy decisions, user profiles
 * - State Storage (Database 2): OAuth state, rate limiting
 *
 * Configuration is loaded from application.properties and can be overridden
 * by environment variables or system properties.
 *
 * @author TSE 2025 - Group 9
 */
@ApplicationScoped
public class RedisConfiguration {

    private static final Logger LOGGER = Logger.getLogger(RedisConfiguration.class.getName());

    private JedisPool sessionPool;    // Database 0
    private JedisPool cachePool;      // Database 1
    private JedisPool statePool;      // Database 2

    private Properties properties;

    /**
     * Initialize Redis connection pools on startup.
     * Creates three separate connection pools for different databases.
     */
    @PostConstruct
    public void init() {
        try {
            properties = loadProperties();

            String host = getProperty("redis.host", "localhost");
            int port = Integer.parseInt(getProperty("redis.port", "6379"));
            String password = getProperty("redis.password", null);
            int timeout = Integer.parseInt(getProperty("redis.timeout", "2000"));

            // Create pools for different databases
            sessionPool = createJedisPool(host, port, password, timeout, 0);
            cachePool = createJedisPool(host, port, password, timeout, 1);
            statePool = createJedisPool(host, port, password, timeout, 2);

            LOGGER.info("Redis connection pools initialized successfully");

            // Validate connections
            validateConnection(sessionPool, "Session Pool (DB 0)");
            validateConnection(cachePool, "Cache Pool (DB 1)");
            validateConnection(statePool, "State Pool (DB 2)");

        } catch (Exception e) {
            LOGGER.severe("Failed to initialize Redis connection pools: " + e.getMessage());
            throw new RuntimeException("Redis configuration initialization failed", e);
        }
    }

    /**
     * Creates a JedisPool with the specified configuration.
     */
    private JedisPool createJedisPool(String host, int port, String password, int timeout, int database) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        // Pool sizing
        poolConfig.setMaxTotal(Integer.parseInt(getProperty("redis.pool.max.total", "20")));
        poolConfig.setMaxIdle(Integer.parseInt(getProperty("redis.pool.max.idle", "10")));
        poolConfig.setMinIdle(Integer.parseInt(getProperty("redis.pool.min.idle", "5")));

        // Connection validation
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);

        // Eviction policy
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);

        // Block when pool is exhausted
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWaitMillis(Duration.ofSeconds(10).toMillis());

        JedisPool pool;
        if (password != null && !password.isEmpty()) {
            pool = new JedisPool(poolConfig, host, port, timeout, password, database);
        } else {
            pool = new JedisPool(poolConfig, host, port, timeout, null, database);
        }

        LOGGER.info(String.format("Created JedisPool for %s:%d (database: %d)", host, port, database));
        return pool;
    }

    /**
     * Validates Redis connection by performing a PING command.
     */
    private void validateConnection(JedisPool pool, String poolName) {
        try (Jedis jedis = pool.getResource()) {
            String response = jedis.ping();
            if ("PONG".equals(response)) {
                LOGGER.info(poolName + " connection validated successfully");
            } else {
                LOGGER.warning(poolName + " returned unexpected response: " + response);
            }
        } catch (Exception e) {
            LOGGER.severe(poolName + " connection validation failed: " + e.getMessage());
            throw new RuntimeException("Redis connection validation failed for " + poolName, e);
        }
    }

    /**
     * Cleanup all Redis connection pools on shutdown.
     */
    @PreDestroy
    public void cleanup() {
        closePool(sessionPool, "Session Pool");
        closePool(cachePool, "Cache Pool");
        closePool(statePool, "State Pool");
        LOGGER.info("All Redis connection pools closed");
    }

    private void closePool(JedisPool pool, String poolName) {
        if (pool != null && !pool.isClosed()) {
            pool.close();
            LOGGER.info(poolName + " closed");
        }
    }

    /**
     * Produces the main JedisPool instance for general use.
     * Defaults to cache pool (database 1).
     *
     * @return JedisPool instance
     */
    @Produces
    public JedisPool getJedisPool() {
        return cachePool;
    }

    /**
     * Produces JedisPool for session storage (Database 0).
     * Used for JWT session data with 1 hour TTL.
     *
     * @return JedisPool for session storage
     */
    @Produces
    @Named("sessionPool")
    public JedisPool getSessionPool() {
        return sessionPool;
    }

    /**
     * Produces JedisPool for cache storage (Database 1).
     * Used for policy decisions and user profiles.
     *
     * @return JedisPool for cache storage
     */
    @Produces
    @Named("cachePool")
    public JedisPool getCachePool() {
        return cachePool;
    }

    /**
     * Produces JedisPool for state storage (Database 2).
     * Used for OAuth state and rate limiting.
     *
     * @return JedisPool for state storage
     */
    @Produces
    @Named("statePool")
    public JedisPool getStatePool() {
        return statePool;
    }

    /**
     * Get property value with fallback to environment variable and default value.
     */
    private String getProperty(String key, String defaultValue) {
        // Check system property first
        String value = System.getProperty(key);
        if (value != null) {
            return value;
        }

        // Check environment variable (convert dots to underscores and uppercase)
        String envKey = key.replace('.', '_').toUpperCase();
        value = System.getenv(envKey);
        if (value != null) {
            return value;
        }

        // Check properties file
        value = properties.getProperty(key);
        if (value != null) {
            return value;
        }

        return defaultValue;
    }

    /**
     * Load properties from application.properties file.
     */
    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
                LOGGER.info("Loaded Redis configuration from application.properties");
            } else {
                LOGGER.warning("application.properties not found, using defaults");
            }
        } catch (IOException e) {
            LOGGER.warning("Failed to load application.properties: " + e.getMessage());
        }
        return props;
    }

    /**
     * Redis key patterns for different storage types.
     */
    public static class KeyPatterns {
        // Session storage (Database 0)
        public static final String SESSION = "session:%s";                      // session:{jwt_token_id}

        // Cache storage (Database 1)
        public static final String POLICY_CACHE = "policy:cache:%s:%s:%s";     // policy:cache:{ci}:{specialty}:{doc_type}
        public static final String USER_PROFILE = "user:profile:%s";           // user:profile:{ci}

        // State storage (Database 2)
        public static final String OAUTH_STATE = "oauth:state:%s";             // oauth:state:{state_token}
        public static final String RATE_LIMIT = "ratelimit:%s:%s";            // ratelimit:{ip}:{endpoint}

        public static String session(String jwtTokenId) {
            return String.format(SESSION, jwtTokenId);
        }

        public static String policyCache(String patientCi, String specialty, String documentType) {
            return String.format(POLICY_CACHE, patientCi, specialty, documentType);
        }

        public static String userProfile(String ci) {
            return String.format(USER_PROFILE, ci);
        }

        public static String oauthState(String state) {
            return String.format(OAUTH_STATE, state);
        }

        public static String rateLimit(String ip, String endpoint) {
            return String.format(RATE_LIMIT, ip, endpoint);
        }
    }

    /**
     * TTL constants (in seconds) for different cache types.
     */
    public static class TTL {
        public static final int SESSION = 3600;           // 1 hour
        public static final int POLICY_CACHE = 300;       // 5 minutes
        public static final int USER_PROFILE = 900;       // 15 minutes
        public static final int OAUTH_STATE = 600;        // 10 minutes
        public static final int RATE_LIMIT = 60;          // 1 minute
    }
}
