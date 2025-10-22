package uy.gub.hcen.cache;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Redis Connection Provider
 * <p>
 * Provides a singleton JedisPool instance for Redis connections.
 * Configured as ApplicationScoped CDI bean for automatic lifecycle management.
 * <p>
 * Connection Pool Configuration:
 * - Max total connections: 20
 * - Max idle connections: 10
 * - Min idle connections: 5
 * <p>
 * Configuration is read from environment variables:
 * - REDIS_HOST: Redis server hostname (default: localhost)
 * - REDIS_PORT: Redis server port (default: 6379)
 * - REDIS_PASSWORD: Redis password (optional)
 * - REDIS_TIMEOUT: Connection timeout in milliseconds (default: 2000)
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@ApplicationScoped
public class RedisConnectionProvider {

    private static final Logger LOGGER = Logger.getLogger(RedisConnectionProvider.class.getName());

    private JedisPool jedisPool;

    /**
     * Initialize the Jedis connection pool.
     * Called automatically by CDI container after bean construction.
     */
    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "Initializing Redis connection pool");

        try {
            // Configure connection pool
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(20);
            poolConfig.setMaxIdle(10);
            poolConfig.setMinIdle(5);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);

            // Read configuration from environment variables
            String redisHost = System.getenv().getOrDefault("REDIS_HOST", "localhost");
            int redisPort = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
            String redisPassword = System.getenv("REDIS_PASSWORD");
            int redisTimeout = Integer.parseInt(System.getenv().getOrDefault("REDIS_TIMEOUT", "2000"));

            // Create connection pool
            if (redisPassword != null && !redisPassword.trim().isEmpty()) {
                this.jedisPool = new JedisPool(poolConfig, redisHost, redisPort, redisTimeout, redisPassword);
                LOGGER.log(Level.INFO, "Redis connection pool initialized with authentication at {0}:{1}",
                        new Object[]{redisHost, redisPort});
            } else {
                this.jedisPool = new JedisPool(poolConfig, redisHost, redisPort, redisTimeout);
                LOGGER.log(Level.INFO, "Redis connection pool initialized without authentication at {0}:{1}",
                        new Object[]{redisHost, redisPort});
            }

            // Test connection
            testConnection();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize Redis connection pool", e);
            throw new RuntimeException("Redis connection pool initialization failed", e);
        }
    }

    /**
     * Test Redis connection by performing a simple ping operation.
     */
    private void testConnection() {
        try (var jedis = jedisPool.getResource()) {
            String response = jedis.ping();
            LOGGER.log(Level.INFO, "Redis connection test successful. Response: {0}", response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Redis connection test failed", e);
            throw new RuntimeException("Redis connection test failed", e);
        }
    }

    /**
     * Produces the JedisPool instance for CDI injection.
     *
     * @return JedisPool instance
     */
    @Produces
    public JedisPool getJedisPool() {
        return jedisPool;
    }

    /**
     * Cleanup method called automatically by CDI container before bean destruction.
     * Closes the Jedis connection pool gracefully.
     */
    @PreDestroy
    public void cleanup() {
        LOGGER.log(Level.INFO, "Shutting down Redis connection pool");

        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            LOGGER.log(Level.INFO, "Redis connection pool closed successfully");
        }
    }
}
