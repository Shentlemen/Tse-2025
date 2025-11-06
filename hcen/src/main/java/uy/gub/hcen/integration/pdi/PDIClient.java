package uy.gub.hcen.integration.pdi;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PDI SOAP Client
 * <p>
 * Client for integrating with Uruguay's PDI (Plataforma de Datos e Integración)
 * DNIC Servicio Básico de Información for identity validation.
 * <p>
 * Responsibilities:
 * - Query user identity data by CI (Cédula de Identidad)
 * - Verify user age (18+ requirement for HCEN eligibility)
 * - Validate user identity during INUS registration
 * <p>
 * Features:
 * - SOAP 1.1/1.2 XML envelope construction
 * - WS-Security username/password authentication
 * - Circuit breaker pattern for resilience
 * - Retry logic (3 attempts with exponential backoff)
 * - Configurable timeouts (connection: 5s, read: 30s)
 * - HTTPS enforcement (AC002-AC004)
 * <p>
 * Integration Points:
 * - InusService: Age verification during user registration (AC013)
 * <p>
 * Configuration Properties:
 * - pdi.soap.endpoint: PDI SOAP endpoint URL (must be HTTPS)
 * - pdi.soap.username: WS-Security username
 * - pdi.soap.password: WS-Security password
 * - pdi.soap.timeout.connect: Connection timeout in milliseconds (default: 5000)
 * - pdi.soap.timeout.read: Read timeout in milliseconds (default: 30000)
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
@ApplicationScoped
public class PDIClient {

    private static final Logger logger = LoggerFactory.getLogger(PDIClient.class);

    // Configuration properties (loaded from application.properties)
    private String pdiSoapEndpoint;
    private String pdiUsername;
    private String pdiPassword;
    private int connectionTimeoutMs;
    private int readTimeoutMs;

    // Retry configuration
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int INITIAL_RETRY_DELAY_MS = 1000;

    // Circuit breaker configuration
    private static final int CIRCUIT_BREAKER_THRESHOLD = 5;
    private static final long CIRCUIT_BREAKER_RESET_TIMEOUT_MS = 60000; // 60 seconds

    // SOAP namespaces
    private static final String SOAP_ENV_NS = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final String PDI_NS = "http://pdi.hcen.fing/";

    // HTTP client components
    private CloseableHttpClient httpClient;
    private DocumentBuilderFactory documentBuilderFactory;

    // Circuit breaker state
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile long circuitBreakerOpenedAt = 0;

    /**
     * Initializes HTTP client with timeouts and XML parser
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing PDI SOAP Client...");

        // Load configuration from application.properties
        loadConfiguration();

        // Configure request timeouts
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(connectionTimeoutMs, TimeUnit.MILLISECONDS))
                .setResponseTimeout(Timeout.of(readTimeoutMs, TimeUnit.MILLISECONDS))
                .build();

        // Build HTTP client
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        // Initialize XML document builder factory
        this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
        this.documentBuilderFactory.setNamespaceAware(true);

        logger.info("PDI SOAP Client initialized successfully (endpoint: {}, connection timeout: {}ms, read timeout: {}ms)",
                pdiSoapEndpoint, connectionTimeoutMs, readTimeoutMs);
    }

    /**
     * Loads configuration from application.properties
     */
    private void loadConfiguration() {
        try (var inputStream = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream == null) {
                logger.warn("application.properties not found - using default configuration");
                setDefaultConfiguration();
                return;
            }

            java.util.Properties props = new java.util.Properties();
            props.load(inputStream);

            // Load PDI configuration
            pdiSoapEndpoint = props.getProperty("pdi.soap.endpoint", "http://localhost:8080/hcen-pdi-1.0-SNAPSHOT/PDIService");
            pdiUsername = props.getProperty("pdi.soap.username", "HCEN");
            pdiPassword = props.getProperty("pdi.soap.password", "hcen-test-2025");
            connectionTimeoutMs = Integer.parseInt(props.getProperty("pdi.soap.timeout.connect", "5000"));
            readTimeoutMs = Integer.parseInt(props.getProperty("pdi.soap.timeout.read", "30000"));

            logger.info("PDI configuration loaded from application.properties");

        } catch (Exception e) {
            logger.error("Failed to load PDI configuration from application.properties - using defaults", e);
            setDefaultConfiguration();
        }
    }

    /**
     * Sets default configuration values
     */
    private void setDefaultConfiguration() {
        pdiSoapEndpoint = "http://localhost:8080/hcen-pdi-1.0-SNAPSHOT/PDIService";
        pdiUsername = "HCEN";
        pdiPassword = "hcen-test-2025";
        connectionTimeoutMs = 5000;
        readTimeoutMs = 30000;
    }

    /**
     * Cleans up HTTP client resources
     */
    @PreDestroy
    public void cleanup() {
        logger.info("Cleaning up PDI SOAP Client...");
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
    // Public API Methods
    // ================================================================

    /**
     * Queries user data from PDI by CI (Cédula de Identidad).
     * <p>
     * Performs SOAP request to PDI's Servicio Básico de Información de DNIC.
     * Returns user identity data including full name and date of birth.
     * <p>
     * Circuit Breaker: If PDI is experiencing failures, this method will fail fast
     * after the circuit breaker threshold is reached (5 consecutive failures).
     * <p>
     * Retry Logic: Network failures are retried up to 3 times with exponential backoff.
     *
     * @param ci Cédula de Identidad (national ID number) - normalized digits only
     * @return PDIUserData containing user information
     * @throws PDIException if query fails after retries, circuit breaker is open, or user not found
     */
    public PDIUserData consultarUsuario(String ci) throws PDIException {
        if (ci == null || ci.trim().isEmpty()) {
            throw new PDIException("CI cannot be null or empty");
        }

        logger.info("Querying PDI for user with CI: {}", ci);

        // Check circuit breaker
        if (isCircuitBreakerOpen()) {
            logger.error("Circuit breaker is OPEN for PDI - rejecting request");
            throw new PDIException("PDI service unavailable - circuit breaker is open");
        }

        // Execute with retry logic
        int attempt = 0;
        Exception lastException = null;

        while (attempt < MAX_RETRY_ATTEMPTS) {
            attempt++;

            try {
                logger.debug("PDI query attempt {} of {}", attempt, MAX_RETRY_ATTEMPTS);

                // Build SOAP request
                String soapRequest = buildConsultarUsuarioRequest(ci);

                // Execute SOAP call
                String soapResponse = executeSoapRequest(soapRequest);

                // Parse SOAP response
                PDIUserData userData = parseConsultarUsuarioResponse(soapResponse, ci);

                // Success - reset circuit breaker
                recordSuccess();

                logger.info("Successfully retrieved user data from PDI for CI: {}", ci);
                return userData;

            } catch (IOException e) {
                lastException = e;
                logger.warn("PDI query attempt {} failed due to network error: {}", attempt, e.getMessage());

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
            } catch (PDIException e) {
                // Don't retry on business logic errors (user not found, invalid response, etc.)
                lastException = e;
                logger.error("PDI query failed: {}", e.getMessage());
                recordFailure();
                throw e;
            } catch (Exception e) {
                lastException = e;
                logger.error("Unexpected error during PDI query", e);
                break; // Don't retry on unexpected errors
            }
        }

        // All retries exhausted
        recordFailure();
        String errorMsg = String.format("Failed to query PDI after %d attempts", MAX_RETRY_ATTEMPTS);
        logger.error(errorMsg);
        throw new PDIException(errorMsg, lastException);
    }

    /**
     * Verifies if a user meets the minimum age requirement.
     * <p>
     * Queries PDI for user's date of birth and calculates age.
     * <p>
     * Graceful Degradation: If PDI is unavailable, this method logs a warning
     * and returns false (fail-safe behavior - don't block registration).
     *
     * @param ci         Cédula de Identidad
     * @param minimumAge Minimum age requirement (e.g., 18 years)
     * @return true if user is >= minimumAge, false otherwise or if PDI unavailable
     */
    public boolean verifyAge(String ci, int minimumAge) {
        try {
            PDIUserData userData = consultarUsuario(ci);

            if (userData.getFechaNacimiento() == null) {
                logger.warn("PDI returned user data without date of birth for CI: {}", ci);
                return false;
            }

            int age = calculateAge(userData.getFechaNacimiento());
            boolean meetsRequirement = age >= minimumAge;

            logger.info("Age verification for CI {}: {} years old, minimum required: {}, meets requirement: {}",
                    ci, age, minimumAge, meetsRequirement);

            return meetsRequirement;

        } catch (PDIException e) {
            logger.warn("Age verification failed due to PDI error - allowing registration to proceed: {}", e.getMessage());
            // Graceful degradation - don't block user registration if PDI is down
            return false;
        }
    }

    // ================================================================
    // SOAP Request/Response Handling
    // ================================================================

    /**
     * Builds SOAP request envelope for obtPersonaPorDoc operation.
     * <p>
     * Request format:
     * <pre>{@code
     * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
     *                   xmlns:pdi="http://pdi.hcen.fing/">
     *    <soapenv:Body>
     *       <pdi:obtPersonaPorDoc>
     *          <parametros>
     *             <organizacion>hcen-client</organizacion>
     *             <passwordEntidad>secure-password</passwordEntidad>
     *             <NroDocumento>12345678</NroDocumento>
     *             <TipoDocumento>CI</TipoDocumento>
     *          </parametros>
     *       </pdi:obtPersonaPorDoc>
     *    </soapenv:Body>
     * </soapenv:Envelope>
     * }</pre>
     *
     * @param ci User's CI (Cédula de Identidad)
     * @return SOAP XML request string
     */
    private String buildConsultarUsuarioRequest(String ci) {
        StringBuilder soapRequest = new StringBuilder();

        soapRequest.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        soapRequest.append("<soapenv:Envelope ");
        soapRequest.append("xmlns:soapenv=\"").append(SOAP_ENV_NS).append("\" ");
        soapRequest.append("xmlns:pdi=\"").append(PDI_NS).append("\">");

        // SOAP Body - no header needed, authentication is in the body
        soapRequest.append("<soapenv:Body>");
        soapRequest.append("<pdi:obtPersonaPorDoc>");
        soapRequest.append("<parametros>");
        soapRequest.append("<organizacion>").append(escapeXml(pdiUsername)).append("</organizacion>");
        soapRequest.append("<passwordEntidad>").append(escapeXml(pdiPassword)).append("</passwordEntidad>");
        soapRequest.append("<NroDocumento>").append(escapeXml(ci)).append("</NroDocumento>");
        soapRequest.append("<TipoDocumento>CI</TipoDocumento>");
        soapRequest.append("</parametros>");
        soapRequest.append("</pdi:obtPersonaPorDoc>");
        soapRequest.append("</soapenv:Body>");

        soapRequest.append("</soapenv:Envelope>");

        String request = soapRequest.toString();
        logger.debug("Built SOAP request for CI: {}", ci);

        return request;
    }

    /**
     * Executes SOAP request via HTTP POST.
     *
     * @param soapRequest SOAP XML request
     * @return SOAP XML response
     * @throws IOException if HTTP request fails
     */
    private String executeSoapRequest(String soapRequest) throws IOException {
        HttpPost httpPost = new HttpPost(pdiSoapEndpoint);
        httpPost.setEntity(new StringEntity(soapRequest, ContentType.TEXT_XML.withCharset("UTF-8")));
        httpPost.setHeader("Content-Type", "text/xml; charset=UTF-8");
        httpPost.setHeader("SOAPAction", "\"\""); // Empty SOAPAction (SOAP 1.1)

        return httpClient.execute(httpPost, response -> {
            int statusCode = response.getCode();
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

            logger.debug("PDI SOAP response: status={}", statusCode);

            if (statusCode >= 200 && statusCode < 300) {
                return responseBody;
            } else {
                logger.error("PDI SOAP request failed: status={}, body={}", statusCode, responseBody);
                throw new IOException("PDI SOAP request failed with status " + statusCode);
            }
        });
    }

    /**
     * Parses SOAP response from obtPersonaPorDoc operation.
     * <p>
     * Expected response format:
     * <pre>{@code
     * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
     *    <soapenv:Body>
     *       <pdi:obtPersonaPorDocResponse xmlns:pdi="http://pdi.hcen.fing/">
     *          <return>
     *             <Persona>
     *                <NroDocumento>12345678</NroDocumento>
     *                <TipoDocumento>CI</TipoDocumento>
     *                <Nombre1>Juan</Nombre1>
     *                <Nombre2>Carlos</Nombre2>
     *                <Apellido1>Pérez</Apellido1>
     *                <Apellido2>González</Apellido2>
     *                <FechaNacimiento>1990-01-15</FechaNacimiento>
     *                <Sexo>M</Sexo>
     *             </Persona>
     *             <Errores>...</Errores>
     *             <Warnings>...</Warnings>
     *          </return>
     *       </pdi:obtPersonaPorDocResponse>
     *    </soapenv:Body>
     * </soapenv:Envelope>
     * }</pre>
     *
     * @param soapResponse SOAP XML response
     * @param requestedCi  CI that was requested (for validation)
     * @return PDIUserData parsed from response
     * @throws PDIException if parsing fails or user not found
     */
    private PDIUserData parseConsultarUsuarioResponse(String soapResponse, String requestedCi) throws PDIException {
        try {
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(soapResponse.getBytes("UTF-8")));

            // Check for SOAP Fault
            NodeList faultNodes = doc.getElementsByTagNameNS(SOAP_ENV_NS, "Fault");
            if (faultNodes.getLength() > 0) {
                Element faultElement = (Element) faultNodes.item(0);
                String faultString = getElementTextContent(faultElement, "faultstring");
                logger.error("PDI returned SOAP Fault: {}", faultString);
                throw new PDIException("PDI service error: " + faultString);
            }

            // Parse obtPersonaPorDocResponse
            NodeList responseNodes = doc.getElementsByTagNameNS(PDI_NS, "obtPersonaPorDocResponse");
            if (responseNodes.getLength() == 0) {
                logger.error("PDI response missing obtPersonaPorDocResponse element");
                throw new PDIException("Invalid PDI response format - missing obtPersonaPorDocResponse");
            }

            Element responseElement = (Element) responseNodes.item(0);

            // Navigate to return > Persona
            NodeList returnNodes = responseElement.getElementsByTagName("return");
            if (returnNodes.getLength() == 0) {
                logger.error("PDI response missing return element");
                throw new PDIException("Invalid PDI response format - missing return");
            }

            Element returnElement = (Element) returnNodes.item(0);

            // Check for errors
            NodeList errorNodes = returnElement.getElementsByTagName("Errores");
            if (errorNodes.getLength() > 0) {
                Element errorElement = (Element) errorNodes.item(0);
                String errorCode = getElementTextContent(errorElement, "CodMensaje");
                String errorDesc = getElementTextContent(errorElement, "Descripcion");
                logger.error("PDI returned error: {} - {}", errorCode, errorDesc);
                throw new PDIException("PDI error: " + errorDesc);
            }

            // Extract Persona
            NodeList personaNodes = returnElement.getElementsByTagName("Persona");
            if (personaNodes.getLength() == 0) {
                logger.error("PDI response missing Persona element - user not found");
                throw new PDIException("User not found in PDI for CI: " + requestedCi);
            }

            Element personaElement = (Element) personaNodes.item(0);

            // Extract user data
            String nroDocumento = getElementTextContent(personaElement, "NroDocumento");
            String nombre1 = getElementTextContent(personaElement, "Nombre1");
            String nombre2 = getElementTextContent(personaElement, "Nombre2");
            String apellido1 = getElementTextContent(personaElement, "Apellido1");
            String apellido2 = getElementTextContent(personaElement, "Apellido2");
            String fechaNacimientoStr = getElementTextContent(personaElement, "FechaNacimiento");

            // Validate required fields
            if (nroDocumento == null || nroDocumento.isEmpty()) {
                throw new PDIException("PDI response missing NroDocumento");
            }

            if (nombre1 == null || nombre1.isEmpty() || apellido1 == null || apellido1.isEmpty()) {
                throw new PDIException("PDI response missing name fields");
            }

            if (fechaNacimientoStr == null || fechaNacimientoStr.isEmpty()) {
                throw new PDIException("PDI response missing FechaNacimiento");
            }

            // Build full name
            StringBuilder nombreCompleto = new StringBuilder();
            nombreCompleto.append(nombre1);
            if (nombre2 != null && !nombre2.isEmpty()) {
                nombreCompleto.append(" ").append(nombre2);
            }
            nombreCompleto.append(" ").append(apellido1);
            if (apellido2 != null && !apellido2.isEmpty()) {
                nombreCompleto.append(" ").append(apellido2);
            }

            // Validate CI matches request
            if (!nroDocumento.equals(requestedCi)) {
                logger.warn("PDI returned data for different CI: requested={}, received={}", requestedCi, nroDocumento);
            }

            // Parse date of birth
            LocalDate fechaNacimiento = LocalDate.parse(fechaNacimientoStr, DateTimeFormatter.ISO_LOCAL_DATE);

            PDIUserData userData = new PDIUserData(nroDocumento, nombreCompleto.toString(), fechaNacimiento);
            logger.debug("Parsed PDI user data: {}", userData);

            return userData;

        } catch (PDIException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to parse PDI SOAP response", e);
            throw new PDIException("Failed to parse PDI response", e);
        }
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
        if (circuitBreakerOpenedAt == 0) {
            return false;
        }

        long elapsedMs = System.currentTimeMillis() - circuitBreakerOpenedAt;
        if (elapsedMs >= CIRCUIT_BREAKER_RESET_TIMEOUT_MS) {
            logger.info("Circuit breaker reset timeout elapsed - attempting half-open state");
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
            logger.info("PDI request succeeded - circuit breaker reset (previous failures: {})", previousFailures);
        }
        circuitBreakerOpenedAt = 0;
    }

    /**
     * Records failed request (increments failure counter, may open circuit breaker)
     */
    private void recordFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        logger.warn("PDI request failed - consecutive failures: {}/{}", failures, CIRCUIT_BREAKER_THRESHOLD);

        if (failures >= CIRCUIT_BREAKER_THRESHOLD) {
            circuitBreakerOpenedAt = System.currentTimeMillis();
            logger.error("Circuit breaker OPENED for PDI - threshold reached ({} failures)", failures);
        }
    }

    // ================================================================
    // Helper Methods
    // ================================================================

    /**
     * Calculates age from date of birth
     *
     * @param dateOfBirth Date of birth
     * @return Age in years
     */
    private int calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /**
     * Escapes XML special characters
     *
     * @param text Text to escape
     * @return Escaped text
     */
    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * Gets text content of first child element with given tag name
     *
     * @param parent  Parent element
     * @param tagName Tag name to search for
     * @return Text content or null if not found
     */
    private String getElementTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    /**
     * Gets text content of first child element with given namespace and local name
     *
     * @param parent       Parent element
     * @param namespaceURI Namespace URI
     * @param localName    Local name
     * @return Text content or null if not found
     */
    private String getElementTextContentNS(Element parent, String namespaceURI, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS(namespaceURI, localName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
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
