package uy.gub.hcen.config;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * HCEN Central Configuration
 * <p>
 * Loads configuration properties for HCEN Central instance.
 * <p>
 * Configuration properties (from application.properties):
 * - hcen.api.url: Base URL of the HCEN Central API
 * <p>
 * Example configuration:
 * <pre>
 * hcen.api.url=http://localhost:8080/hcen/api
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-19
 */
@ApplicationScoped
public class HcenConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(HcenConfiguration.class);

    private static final String PROPERTIES_FILE = "application.properties";
    private static final String HCEN_API_URL_KEY = "hcen.api.url";
    private static final String DEFAULT_HCEN_API_URL = "http://localhost:8080/hcen/api";

    private String hcenApiUrl;

    /**
     * Loads configuration from application.properties
     */
    @PostConstruct
    public void loadConfiguration() {
        logger.info("Loading HCEN configuration from {}...", PROPERTIES_FILE);

        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                logger.error("Unable to find {}", PROPERTIES_FILE);
                this.hcenApiUrl = DEFAULT_HCEN_API_URL;
                logger.warn("Using default HCEN API URL: {}", this.hcenApiUrl);
                return;
            }

            properties.load(input);

            // Load HCEN API URL
            this.hcenApiUrl = properties.getProperty(HCEN_API_URL_KEY, DEFAULT_HCEN_API_URL);
            if (this.hcenApiUrl != null && !this.hcenApiUrl.isEmpty()) {
                logger.info("HCEN API URL configured: {}", this.hcenApiUrl);
            } else {
                this.hcenApiUrl = DEFAULT_HCEN_API_URL;
                logger.warn("HCEN API URL not configured - using default: {}", this.hcenApiUrl);
            }

        } catch (IOException e) {
            logger.error("Failed to load HCEN configuration", e);
            this.hcenApiUrl = DEFAULT_HCEN_API_URL;
            logger.warn("Using default HCEN API URL: {}", this.hcenApiUrl);
        }
    }

    /**
     * Gets the HCEN Central API base URL
     *
     * @return HCEN API URL (e.g., http://localhost:8080/hcen/api)
     */
    public String getHcenApiUrl() {
        return hcenApiUrl;
    }
}
