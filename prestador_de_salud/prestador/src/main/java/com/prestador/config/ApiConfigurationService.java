package com.prestador.config;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * API Configuration Service
 *
 * Singleton EJB that manages API configuration properties including API key authentication.
 * This service supports runtime reloading of configuration without requiring application restart.
 *
 * Configuration Priority:
 * 1. External file: ${jboss.server.config.dir}/api-config.properties
 * 2. Classpath resource: /api-config.properties
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-18
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ApiConfigurationService {

    private static final Logger LOGGER = Logger.getLogger(ApiConfigurationService.class.getName());

    private static final String CONFIG_FILE = "api-config.properties";
    private static final String API_KEY_PROPERTY = "api.key";
    private static final String API_AUTH_ENABLED_PROPERTY = "api.auth.enabled";

    private Properties properties;
    private long lastModified;
    private Path externalConfigPath;

    @PostConstruct
    public void init() {
        loadConfiguration();
    }

    /**
     * Load configuration from external file or classpath resource
     */
    private synchronized void loadConfiguration() {
        properties = new Properties();

        // Try to load from external configuration directory first (WildFly standalone/configuration)
        String configDir = System.getProperty("jboss.server.config.dir");
        if (configDir != null) {
            externalConfigPath = Paths.get(configDir, CONFIG_FILE);
            if (Files.exists(externalConfigPath)) {
                try (InputStream input = Files.newInputStream(externalConfigPath)) {
                    properties.load(input);
                    lastModified = Files.getLastModifiedTime(externalConfigPath).toMillis();
                    LOGGER.log(Level.INFO, "API configuration loaded from external file: {0}",
                            externalConfigPath.toString());
                    return;
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to load external API configuration, falling back to classpath", e);
                }
            } else {
                LOGGER.log(Level.INFO, "External API configuration not found at: {0}, using classpath resource",
                        externalConfigPath.toString());
            }
        }

        // Fall back to classpath resource
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                LOGGER.log(Level.INFO, "API configuration loaded from classpath resource");
            } else {
                LOGGER.log(Level.SEVERE, "API configuration file not found in classpath: {0}", CONFIG_FILE);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load API configuration from classpath", e);
        }
    }

    /**
     * Reload configuration from file if it has been modified
     * This allows for runtime configuration updates without application restart
     */
    public synchronized void reloadIfModified() {
        if (externalConfigPath != null && Files.exists(externalConfigPath)) {
            try {
                long currentModified = Files.getLastModifiedTime(externalConfigPath).toMillis();
                if (currentModified > lastModified) {
                    LOGGER.log(Level.INFO, "Configuration file modified, reloading...");
                    loadConfiguration();
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to check configuration file modification time", e);
            }
        }
    }

    /**
     * Get the configured API key
     *
     * @return API key string or null if not configured
     */
    public String getApiKey() {
        reloadIfModified();
        return properties.getProperty(API_KEY_PROPERTY);
    }

    /**
     * Check if API authentication is enabled
     *
     * @return true if authentication is enabled (default), false otherwise
     */
    public boolean isAuthenticationEnabled() {
        reloadIfModified();
        String enabled = properties.getProperty(API_AUTH_ENABLED_PROPERTY, "true");
        return Boolean.parseBoolean(enabled);
    }

    /**
     * Validate an API key against the configured key
     *
     * @param providedKey the API key to validate
     * @return true if the key matches the configured key, false otherwise
     */
    public boolean validateApiKey(String providedKey) {
        if (!isAuthenticationEnabled()) {
            LOGGER.log(Level.WARNING, "API authentication is disabled - allowing request");
            return true;
        }

        if (providedKey == null || providedKey.trim().isEmpty()) {
            return false;
        }

        String configuredKey = getApiKey();
        if (configuredKey == null || configuredKey.trim().isEmpty()) {
            LOGGER.log(Level.SEVERE, "API key not configured - denying request");
            return false;
        }

        // Use constant-time comparison to prevent timing attacks
        return constantTimeEquals(providedKey, configuredKey);
    }

    /**
     * Constant-time string comparison to prevent timing attacks
     *
     * @param a first string
     * @param b second string
     * @return true if strings are equal, false otherwise
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();

        if (aBytes.length != bBytes.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }

        return result == 0;
    }
}
