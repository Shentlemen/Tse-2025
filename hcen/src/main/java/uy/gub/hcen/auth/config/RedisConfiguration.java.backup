package uy.gub.hcen.auth.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Configuration for Redis connection pool.
 * Redis is used for:
 * - OAuth state management (short-lived, 5 minute TTL)
 * - Token blacklist (for revoked tokens)
 * - Rate limiting (future enhancement)
 * - Session caching (optional)
 */
@ApplicationScoped
public class RedisConfiguration {

    private static final Logger LOGGER = Logger.getLogger(RedisConfiguration.class.getName());

    private JedisPool jedisPool;
    private Properties properties;

    @PostConstruct
    public void init() {
        try {
            properties = loadProperties();
            createJedisPool();
            LOGGER.info("Redis connection pool initialized successfully");
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize Redis connection pool: " + e.getMessage());
            throw new RuntimeException("Redis configuration initialization failed", e);
        }
    }

    private void createJedisPool() {
        String host = getProperty("redis.host", "localhost");
        int port = Integer.parseInt(getProperty("redis.port", "6379"));
        String password = getProperty("redis.password", null);
        int database = Integer.parseInt(getProperty("redis.database", "0"));
        int timeout = Integer.parseInt(getProperty("redis.timeout", "2000"));

        // Pool configuration
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(Integer.parseInt(getProperty("redis.pool.max.total", "20")));
        poolConfig.setMaxIdle(Integer.parseInt(getProperty("redis.pool.max.idle", "10")));
        poolConfig.setMinIdle(Integer.parseInt(getProperty("redis.pool.min.idle", "5")));
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);

        // Create pool
        if (password != null && !password.isEmpty()) {
            jedisPool = new JedisPool(poolConfig, host, port, timeout, password, database);
        } else {
            jedisPool = new JedisPool(poolConfig, host, port, timeout);
        }

        LOGGER.info("Redis pool created for " + host + ":" + port + " (database: " + database + ")");
    }

    @PreDestroy
    public void cleanup() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            LOGGER.info("Redis connection pool closed");
        }
    }

    /**
     * Produces JedisPool instance for CDI injection.
     *
     * Uses @Dependent scope because JedisPool has final methods and cannot be proxied.
     * The pool is managed by this configuration bean's lifecycle (@PreDestroy).
     *
     * @return JedisPool instance
     */
    @Produces
    public JedisPool getJedisPool() {
        return jedisPool;
    }

    private String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IOException("application.properties not found in classpath");
            }
            props.load(input);
        }
        return props;
    }

    /**
     * Key patterns for Redis
     */
    public static class KeyPatterns {
        public static final String OAUTH_STATE = "oauth:state:%s";           // state → client info
        public static final String TOKEN_BLACKLIST = "token:blacklist:%s";   // jti → revocation time
        public static final String RATE_LIMIT = "ratelimit:%s:%s";          // ip:endpoint → count

        public static String oauthState(String state) {
            return String.format(OAUTH_STATE, state);
        }

        public static String tokenBlacklist(String jti) {
            return String.format(TOKEN_BLACKLIST, jti);
        }

        public static String rateLimit(String ip, String endpoint) {
            return String.format(RATE_LIMIT, ip, endpoint);
        }
    }

    /**
     * TTL constants (in seconds)
     */
    public static class TTL {
        public static final int OAUTH_STATE = 300;        // 5 minutes
        public static final int RATE_LIMIT = 60;          // 1 minute
        // Token blacklist TTL should match token expiration
    }
}
