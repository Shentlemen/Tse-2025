package uy.gub.hcen.integration.clinic;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.hcen.integration.clinic.dto.ClinicServiceRegistrationRequest;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Clinic Service HTTP Client
 * <p>
 * Enables HCEN Central to communicate with the Clinic Service peripheral component for:
 * 1. Clinic registration - notifying the clinic service when a new clinic is created
 * <p>
 * Features:
 * - Configurable timeouts (connection: 10s, read: 30s)
 * - Retry logic (3 attempts with exponential backoff for network failures)
 * - HTTPS validation and SSL/TLS support
 * - Comprehensive error handling and logging
 * <p>
 * Integration Points:
 * - ClinicManagementService: Registering new clinics
 * <p>
 * Configuration:
 * - Connection timeout: 10 seconds
 * - Read timeout: 30 seconds
 * - Max retry attempts: 3
 * - Retry backoff: Exponential (1s, 2s, 4s)
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@ApplicationScoped
public class ClinicServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ClinicServiceClient.class);

    // Configuration constants
    private static final int CONNECTION_TIMEOUT_SECONDS = 10;
    private static final int READ_TIMEOUT_SECONDS = 30;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int INITIAL_RETRY_DELAY_MS = 1000;

    // HTTP client components
    private CloseableHttpClient httpClient;
    private ObjectMapper objectMapper;

    @Inject
    private ClinicServiceConfiguration config;

    /**
     * Initializes HTTP client with timeouts and JSON mapper
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing Clinic Service HTTP Client...");

        // Configure request timeouts
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                .setResponseTimeout(Timeout.of(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                .build();

        // Build HTTP client
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        this.objectMapper = new ObjectMapper();

        logger.info("Clinic Service HTTP Client initialized successfully (connection timeout: {}s, read timeout: {}s)",
                CONNECTION_TIMEOUT_SECONDS, READ_TIMEOUT_SECONDS);
    }

    /**
     * Cleans up HTTP client resources
     */
    @PreDestroy
    public void cleanup() {
        logger.info("Cleaning up Clinic Service HTTP Client...");
        if (httpClient != null) {
            try {
                httpClient.close();
                logger.info("HTTP client closed successfully");
            } catch (IOException e) {
                logger.error("Failed to close HTTP client", e);
            }
        }
    }

    // ================================================================
    // Clinic Registration
    // ================================================================

    /**
     * Registers a new clinic in the Clinic Service.
     * <p>
     * This method performs the following:
     * 1. Validates clinic service URL is configured
     * 2. Serializes registration request to JSON
     * 3. POSTs to {clinicServiceUrl}/api/clinics
     * 4. Retries on network failures (3 attempts with exponential backoff)
     * 5. Returns true if clinic service confirms (HTTP 201)
     * <p>
     * Example clinic service response:
     * <pre>
     * {
     *   "id": 1,
     *   "code": "clinic-550e8400...",
     *   "name": "Clínica San José",
     *   "active": true,
     *   "created_at": "2025-11-07T10:30:00"
     * }
     * </pre>
     *
     * @param request Clinic registration request
     * @return true if clinic service confirms registration (HTTP 201), false otherwise
     * @throws ClinicServiceException if request fails after retries or clinic service returns error
     */
    public boolean registerClinic(ClinicServiceRegistrationRequest request) throws ClinicServiceException {
        logger.info("Registering clinic in Clinic Service: {}", request.getName());

        // Validate clinic service URL is configured
        String clinicServiceUrl = config.getClinicServiceUrl();
        if (clinicServiceUrl == null || clinicServiceUrl.isEmpty()) {
            logger.warn("Clinic service URL is not configured - skipping clinic registration");
            return false; // Don't fail the entire registration if clinic service is not configured
        }

        // Build registration endpoint URL
        String registrationUrl = buildRegistrationUrl(clinicServiceUrl);

        // Execute with retry logic
        int attempt = 0;
        Exception lastException = null;

        while (attempt < MAX_RETRY_ATTEMPTS) {
            attempt++;

            try {
                logger.debug("Clinic registration attempt {} of {}", attempt, MAX_RETRY_ATTEMPTS);

                // Serialize request to JSON
                String requestJson = objectMapper.writeValueAsString(request);
                logger.debug("Clinic registration request JSON: {}", requestJson);

                // Create HTTP POST request
                HttpPost httpPost = new HttpPost(registrationUrl);
                httpPost.setEntity(new StringEntity(requestJson, ContentType.APPLICATION_JSON));
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("Accept", "application/json");

                // Add API key if configured
                String apiKey = config.getClinicServiceApiKey();
                if (apiKey != null && !apiKey.isEmpty()) {
                    httpPost.setHeader("Authorization", "Bearer " + apiKey);
                }

                // Execute request
                Boolean result = httpClient.execute(httpPost, response -> {
                    int statusCode = response.getCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    logger.debug("Clinic service response: status={}, body={}", statusCode, responseBody);

                    if (statusCode == 201) {
                        logger.info("Clinic service confirmed registration: {}", request.getName());
                        return true;
                    } else if (statusCode == 400) {
                        logger.error("Clinic service rejected registration (bad request): status={}, body={}", statusCode, responseBody);
                        // Don't retry on validation errors - return false to indicate failure
                        // We'll throw exception outside the lambda
                        return null; // Special value to indicate business error
                    } else if (statusCode == 409) {
                        logger.error("Clinic service rejected registration (already exists): status={}, body={}", statusCode, responseBody);
                        // Don't retry on duplicate errors - return false to indicate failure
                        return null; // Special value to indicate business error
                    } else {
                        logger.error("Clinic service returned unexpected status: status={}, body={}", statusCode, responseBody);
                        return false;
                    }
                });

                // Check if we got a business error (null indicates validation/duplicate error)
                if (result == null) {
                    throw new ClinicServiceException("Clinic service rejected registration due to validation or duplicate error");
                }

                return result;

            } catch (ClinicServiceException e) {
                // Don't retry on business logic errors (validation, duplicates, etc.)
                throw e;
            } catch (IOException e) {
                lastException = e;
                logger.warn("Clinic registration attempt {} failed: {}", attempt, e.getMessage());

                // Wait before retry (exponential backoff)
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    int delayMs = INITIAL_RETRY_DELAY_MS * (int) Math.pow(2, attempt - 1);
                    logger.debug("Retrying in {}ms...", delayMs);
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.warn("Retry delay interrupted");
                        break;
                    }
                }
            } catch (Exception e) {
                lastException = e;
                logger.error("Unexpected error during clinic registration", e);
                break; // Don't retry on unexpected errors
            }
        }

        // All retries exhausted
        String errorMsg = String.format("Failed to register clinic in clinic service after %d attempts", MAX_RETRY_ATTEMPTS);
        logger.error(errorMsg);
        throw new ClinicServiceException(errorMsg, lastException);
    }

    // ================================================================
    // Helper Methods
    // ================================================================

    /**
     * Builds clinic registration endpoint URL
     *
     * @param clinicServiceUrl Base URL of clinic service
     * @return Complete registration URL
     */
    private String buildRegistrationUrl(String clinicServiceUrl) {
        // Remove trailing slash if present
        String baseUrl = clinicServiceUrl.endsWith("/")
                ? clinicServiceUrl.substring(0, clinicServiceUrl.length() - 1)
                : clinicServiceUrl;

        return baseUrl + "/api/clinics";
    }
}
