package uy.gub.hcen.integration.peripheral;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.hc.client5.http.classic.methods.HttpGet;
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
import uy.gub.hcen.service.clinic.dto.OnboardingRequest;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Peripheral Node HTTP Client
 * <p>
 * Enables HCEN Central to communicate with peripheral nodes (clinics) for:
 * 1. Clinic onboarding (AC016) - sending configuration to peripheral nodes
 * 2. Document retrieval (AC015) - fetching actual clinical documents
 * <p>
 * Features:
 * - Circuit breaker pattern for resilience (tracks failures, opens circuit after threshold)
 * - Configurable timeouts (connection: 5s, read: 30s)
 * - Retry logic (3 attempts with exponential backoff for network failures)
 * - HTTPS validation and SSL/TLS support
 * - Document integrity verification (SHA-256 hash)
 * - Comprehensive error handling and logging
 * <p>
 * Integration Points:
 * - ClinicManagementService: Onboarding clinics (AC016)
 * - DocumentRetrievalService: Fetching documents from peripheral storage (AC015)
 * <p>
 * Configuration:
 * - Connection timeout: 5 seconds
 * - Read timeout: 30 seconds
 * - Max retry attempts: 3
 * - Retry backoff: Exponential (1s, 2s, 4s)
 * - Circuit breaker threshold: 5 consecutive failures
 * - Circuit breaker reset timeout: 60 seconds
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
@ApplicationScoped
public class PeripheralNodeClient {

    private static final Logger logger = LoggerFactory.getLogger(PeripheralNodeClient.class);

    // Configuration constants
    private static final int CONNECTION_TIMEOUT_SECONDS = 5;
    private static final int READ_TIMEOUT_SECONDS = 30;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int INITIAL_RETRY_DELAY_MS = 1000;
    private static final int CIRCUIT_BREAKER_THRESHOLD = 5;
    private static final long CIRCUIT_BREAKER_RESET_TIMEOUT_MS = 60000; // 60 seconds

    // HTTP client components
    private CloseableHttpClient httpClient;
    private ObjectMapper objectMapper;

    // Circuit breaker state
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile long circuitBreakerOpenedAt = 0;

    /**
     * Initializes HTTP client with timeouts and JSON mapper
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing Peripheral Node HTTP Client...");

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

        logger.info("Peripheral Node HTTP Client initialized successfully (connection timeout: {}s, read timeout: {}s)",
                CONNECTION_TIMEOUT_SECONDS, READ_TIMEOUT_SECONDS);
    }

    /**
     * Cleans up HTTP client resources
     */
    @PreDestroy
    public void cleanup() {
        logger.info("Cleaning up Peripheral Node HTTP Client...");
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
    // Clinic Onboarding (AC016)
    // ================================================================

    /**
     * Sends onboarding configuration to peripheral node (AC016).
     * <p>
     * This method performs the following:
     * 1. Validates peripheral node URL (must be HTTPS)
     * 2. Checks circuit breaker state
     * 3. Serializes onboarding request to JSON
     * 4. POSTs to {peripheralNodeUrl}/api/onboard
     * 5. Retries on network failures (3 attempts with exponential backoff)
     * 6. Updates circuit breaker state
     * 7. Returns true if peripheral node confirms (HTTP 200-299)
     * <p>
     * Example peripheral node response:
     * <pre>
     * {
     *   "status": "SUCCESS",
     *   "message": "Clinic onboarded successfully",
     *   "clinicId": "clinic-123"
     * }
     * </pre>
     *
     * @param peripheralNodeUrl Peripheral node base URL (e.g., https://clinic-001.hcen.uy)
     * @param request           Onboarding request with clinic configuration
     * @return true if peripheral node confirms onboarding (HTTP 200-299), false otherwise
     * @throws PeripheralNodeException if request fails after retries or circuit breaker is open
     */
    public boolean sendOnboardingData(String peripheralNodeUrl, OnboardingRequest request)
            throws PeripheralNodeException {

        logger.info("Sending onboarding data to peripheral node: {}", peripheralNodeUrl);

        // Validate URL
        validatePeripheralNodeUrl(peripheralNodeUrl);

        // Check circuit breaker
        if (isCircuitBreakerOpen()) {
            logger.error("Circuit breaker is OPEN for peripheral nodes - rejecting request");
            throw new PeripheralNodeException("Circuit breaker is open - too many consecutive failures");
        }

        // Build onboarding endpoint URL
        String onboardingUrl = buildOnboardingUrl(peripheralNodeUrl);

        // Execute with retry logic
        int attempt = 0;
        Exception lastException = null;

        while (attempt < MAX_RETRY_ATTEMPTS) {
            attempt++;

            try {
                logger.debug("Onboarding request attempt {} of {}", attempt, MAX_RETRY_ATTEMPTS);

                // Serialize request to JSON
                String requestJson = objectMapper.writeValueAsString(request);

                // Create HTTP POST request
                HttpPost httpPost = new HttpPost(onboardingUrl);
                httpPost.setEntity(new StringEntity(requestJson, ContentType.APPLICATION_JSON));
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("Accept", "application/json");

                // Execute request
                return httpClient.execute(httpPost, response -> {
                    int statusCode = response.getCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    logger.debug("Peripheral node response: status={}, body={}", statusCode, responseBody);

                    if (statusCode >= 200 && statusCode < 300) {
                        logger.info("Peripheral node confirmed onboarding: {}", peripheralNodeUrl);
                        recordSuccess();
                        return true;
                    } else {
                        logger.error("Peripheral node rejected onboarding: status={}, body={}", statusCode, responseBody);
                        recordFailure();
                        return false;
                    }
                });

            } catch (IOException e) {
                lastException = e;
                logger.warn("Onboarding request attempt {} failed: {}", attempt, e.getMessage());

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
                logger.error("Unexpected error during onboarding request", e);
                break; // Don't retry on unexpected errors
            }
        }

        // All retries exhausted
        recordFailure();
        String errorMsg = String.format("Failed to send onboarding data after %d attempts", MAX_RETRY_ATTEMPTS);
        logger.error(errorMsg);
        throw new PeripheralNodeException(errorMsg, lastException);
    }

    // ================================================================
    // Document Retrieval (AC015)
    // ================================================================

    /**
     * Retrieves clinical document from peripheral node (AC015).
     * <p>
     * This method performs the following:
     * 1. Validates document locator URL (must be HTTPS)
     * 2. Checks circuit breaker state
     * 3. GETs document from peripheral node with authentication
     * 4. Verifies document integrity (SHA-256 hash) if provided
     * 5. Retries on network failures (3 attempts with exponential backoff)
     * 6. Returns document bytes (PDF, XML, FHIR)
     * <p>
     * Document locator format: https://clinic-001.hcen.uy/api/documents/{documentId}
     * <p>
     * Authentication: API key passed in Authorization: Bearer {apiKey} header
     *
     * @param documentLocator URL to document in peripheral node storage
     * @param apiKey          Clinic API key for authentication
     * @return Document bytes (PDF, XML, FHIR, etc.)
     * @throws PeripheralNodeException if retrieval fails after retries or validation fails
     */
    public byte[] retrieveDocument(String documentLocator, String apiKey) throws PeripheralNodeException {
        return retrieveDocument(documentLocator, apiKey, null);
    }

    /**
     * Retrieves clinical document from peripheral node with hash verification (AC015).
     * <p>
     * Same as {@link #retrieveDocument(String, String)} but also verifies document integrity
     * using SHA-256 hash from RNDC metadata.
     *
     * @param documentLocator     URL to document in peripheral node storage
     * @param apiKey              Clinic API key for authentication
     * @param expectedDocumentHash Expected SHA-256 hash (from RNDC), null to skip verification
     * @return Document bytes (PDF, XML, FHIR, etc.)
     * @throws PeripheralNodeException if retrieval fails or hash verification fails
     */
    public byte[] retrieveDocument(String documentLocator, String apiKey, String expectedDocumentHash)
            throws PeripheralNodeException {

        logger.info("Retrieving document from peripheral node: {}", documentLocator);

        // Validate URL
        validatePeripheralNodeUrl(documentLocator);

        // Check circuit breaker
        if (isCircuitBreakerOpen()) {
            logger.error("Circuit breaker is OPEN for peripheral nodes - rejecting request");
            throw new PeripheralNodeException("Circuit breaker is open - too many consecutive failures");
        }

        // Execute with retry logic
        int attempt = 0;
        Exception lastException = null;

        while (attempt < MAX_RETRY_ATTEMPTS) {
            attempt++;

            try {
                logger.debug("Document retrieval attempt {} of {}", attempt, MAX_RETRY_ATTEMPTS);

                // Create HTTP GET request
                HttpGet httpGet = new HttpGet(documentLocator);
                httpGet.setHeader("Authorization", "Bearer " + apiKey);
                httpGet.setHeader("Accept", "application/octet-stream, application/pdf, application/xml, application/fhir+json");

                // Execute request
                byte[] documentBytes = httpClient.execute(httpGet, response -> {
                    int statusCode = response.getCode();

                    if (statusCode >= 200 && statusCode < 300) {
                        // Read document bytes
                        byte[] bytes = EntityUtils.toByteArray(response.getEntity());
                        logger.info("Successfully retrieved document ({} bytes) from peripheral node", bytes.length);
                        return bytes;
                    } else {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        logger.error("Document retrieval failed: status={}, body={}", statusCode, responseBody);
                        throw new IOException("Document retrieval failed with status " + statusCode + ": " + responseBody);
                    }
                });

                // Verify document integrity if hash provided
                if (expectedDocumentHash != null && !expectedDocumentHash.isEmpty()) {
                    verifyDocumentIntegrity(documentBytes, expectedDocumentHash);
                }

                recordSuccess();
                return documentBytes;

            } catch (PeripheralNodeException e) {
                // Don't retry on business logic errors (auth, not found, etc.)
                recordFailure();
                throw e;
            } catch (IOException e) {
                lastException = e;
                logger.warn("Document retrieval attempt {} failed: {}", attempt, e.getMessage());

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
                logger.error("Unexpected error during document retrieval", e);
                break; // Don't retry on unexpected errors
            }
        }

        // All retries exhausted
        recordFailure();
        String errorMsg = String.format("Failed to retrieve document after %d attempts", MAX_RETRY_ATTEMPTS);
        logger.error(errorMsg);
        throw new PeripheralNodeException(errorMsg, lastException);
    }

    // ================================================================
    // Circuit Breaker Pattern
    // ================================================================

    /**
     * Checks if circuit breaker is open (too many failures)
     *
     * @return true if circuit breaker is open
     */
    private boolean isCircuitBreakerOpen() {
        // Check if circuit breaker was opened
        if (circuitBreakerOpenedAt == 0) {
            return false;
        }

        // Check if reset timeout has elapsed
        long elapsedMs = System.currentTimeMillis() - circuitBreakerOpenedAt;
        if (elapsedMs >= CIRCUIT_BREAKER_RESET_TIMEOUT_MS) {
            logger.info("Circuit breaker reset timeout elapsed - attempting half-open state");
            // Reset circuit breaker (half-open state)
            circuitBreakerOpenedAt = 0;
            consecutiveFailures.set(0);
            return false;
        }

        return true;
    }

    /**
     * Records successful request (resets circuit breaker)
     */
    private void recordSuccess() {
        int previousFailures = consecutiveFailures.getAndSet(0);
        if (previousFailures > 0) {
            logger.info("Request succeeded - circuit breaker reset (previous failures: {})", previousFailures);
        }
        circuitBreakerOpenedAt = 0;
    }

    /**
     * Records failed request (increments failure counter, may open circuit breaker)
     */
    private void recordFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        logger.warn("Request failed - consecutive failures: {}/{}", failures, CIRCUIT_BREAKER_THRESHOLD);

        if (failures >= CIRCUIT_BREAKER_THRESHOLD) {
            circuitBreakerOpenedAt = System.currentTimeMillis();
            logger.error("Circuit breaker OPENED - threshold reached ({} failures)", failures);
        }
    }

    // ================================================================
    // Helper Methods
    // ================================================================

    /**
     * Validates peripheral node URL
     * - Must be HTTPS (security requirement AC002-AC004)
     * - Must not be null or empty
     *
     * @param url URL to validate
     * @throws PeripheralNodeException if URL is invalid
     */
    private void validatePeripheralNodeUrl(String url) throws PeripheralNodeException {
        if (url == null || url.isEmpty()) {
            throw new PeripheralNodeException("Peripheral node URL cannot be null or empty");
        }

        // TODO: Uncomment this when deploying the code to the cloud.
//        if (!url.startsWith("https://")) {
//            throw new PeripheralNodeException("Peripheral node URL must use HTTPS: " + url);
//        }
    }

    /**
     * Builds onboarding endpoint URL
     *
     * @param peripheralNodeUrl Base URL
     * @return Complete onboarding URL
     */
    private String buildOnboardingUrl(String peripheralNodeUrl) {
        // Remove trailing slash if present
        String baseUrl = peripheralNodeUrl.endsWith("/")
                ? peripheralNodeUrl.substring(0, peripheralNodeUrl.length() - 1)
                : peripheralNodeUrl;

        return baseUrl + "/api/onboard";
    }

    /**
     * Verifies document integrity using SHA-256 hash
     *
     * @param documentBytes  Document bytes
     * @param expectedHash   Expected SHA-256 hash (format: "sha256:..." or raw hex)
     * @throws PeripheralNodeException if hash verification fails
     */
    private void verifyDocumentIntegrity(byte[] documentBytes, String expectedHash) throws PeripheralNodeException {
        try {
            logger.debug("Verifying document integrity (expected hash: {})", expectedHash);

            // Calculate SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(documentBytes);
            String actualHash = Base64.getEncoder().encodeToString(hashBytes);

            // Normalize expected hash (remove "sha256:" prefix if present)
            String normalizedExpectedHash = expectedHash.startsWith("sha256:")
                    ? expectedHash.substring(7)
                    : expectedHash;

            // Convert to hex if needed (for comparison)
            String actualHashHex = bytesToHex(hashBytes);

            // Compare hashes (support both Base64 and hex formats)
            boolean hashMatch = actualHash.equals(normalizedExpectedHash)
                    || actualHashHex.equalsIgnoreCase(normalizedExpectedHash);

            if (!hashMatch) {
                logger.error("Document integrity verification FAILED: expected={}, actual={}", expectedHash, actualHashHex);
                throw new PeripheralNodeException("Document integrity verification failed - hash mismatch");
            }

            logger.debug("Document integrity verified successfully");

        } catch (Exception e) {
            logger.error("Failed to verify document integrity", e);
            throw new PeripheralNodeException("Failed to verify document integrity", e);
        }
    }

    /**
     * Converts byte array to hex string
     *
     * @param bytes Byte array
     * @return Hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Gets circuit breaker state (for monitoring/health checks)
     *
     * @return Circuit breaker state string
     */
    public String getCircuitBreakerState() {
        if (circuitBreakerOpenedAt == 0) {
            return "CLOSED";
        }

        long elapsedMs = System.currentTimeMillis() - circuitBreakerOpenedAt;
        if (elapsedMs >= CIRCUIT_BREAKER_RESET_TIMEOUT_MS) {
            return "HALF_OPEN";
        }

        return String.format("OPEN (failures: %d, opened: %dms ago)",
                consecutiveFailures.get(), elapsedMs);
    }
}
