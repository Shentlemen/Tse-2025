package uy.gub.hcen.integration.clinic;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Clinic Service Configuration
 * <p>
 * Loads configuration properties for the Clinic Service peripheral component integration.
 * <p>
 * Configuration properties (from application.properties):
 * - clinic.service.url: Base URL of the Clinic Service API
 * - clinic.service.api.key: Optional API key for authentication
 * <p>
 * Example configuration:
 * <pre>
 * clinic.service.url=https://clinics.hcen.uy
 * clinic.service.api.key=your-api-key-here
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@ApplicationScoped
public class ClinicServiceConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ClinicServiceConfiguration.class);

    private static final String PROPERTIES_FILE = "application.properties";
    private static final String CLINIC_SERVICE_URL_KEY = "clinic.service.url";
    private static final String CLINIC_SERVICE_API_KEY_KEY = "clinic.service.api.key";

    private String clinicServiceUrl;
    private String clinicServiceApiKey;

    /**
     * Loads configuration from application.properties
     */
    @PostConstruct
    public void loadConfiguration() {
        logger.info("Loading Clinic Service configuration from {}...", PROPERTIES_FILE);

        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                logger.error("Unable to find {}", PROPERTIES_FILE);
                return;
            }

            properties.load(input);

            // Load clinic service URL
            this.clinicServiceUrl = properties.getProperty(CLINIC_SERVICE_URL_KEY);
            if (this.clinicServiceUrl != null && !this.clinicServiceUrl.isEmpty()) {
                logger.info("Clinic service URL configured: {}", this.clinicServiceUrl);
            } else {
                logger.warn("Clinic service URL is not configured - clinic registration will be skipped");
            }

            // Load optional API key
            this.clinicServiceApiKey = properties.getProperty(CLINIC_SERVICE_API_KEY_KEY);
            if (this.clinicServiceApiKey != null && !this.clinicServiceApiKey.isEmpty()) {
                logger.info("Clinic service API key configured");
            } else {
                logger.debug("Clinic service API key is not configured");
            }

        } catch (IOException e) {
            logger.error("Failed to load Clinic Service configuration", e);
        }
    }

    /**
     * Gets the clinic service base URL
     *
     * @return Clinic service URL (e.g., https://clinics.hcen.uy)
     */
    public String getClinicServiceUrl() {
        return clinicServiceUrl;
    }

    /**
     * Gets the clinic service API key
     *
     * @return API key or null if not configured
     */
    public String getClinicServiceApiKey() {
        return clinicServiceApiKey;
    }

    /**
     * Checks if clinic service integration is enabled
     *
     * @return true if clinic service URL is configured
     */
    public boolean isEnabled() {
        return clinicServiceUrl != null && !clinicServiceUrl.isEmpty();
    }
}
