package uy.gub.clinic.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.config.ApiConfigurationService;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.entity.User;
import uy.gub.clinic.service.ClinicService;
import uy.gub.clinic.service.UserService;
import uy.gub.clinic.util.PasswordUtil;
import uy.gub.clinic.web.api.dto.ClinicRegistrationRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servlet for clinic registration from HCEN
 *
 * This servlet handles clinic registration requests sent by HCEN central component
 * when a new clinic is onboarded to the platform.
 *
 * Endpoint: POST /api/clinics
 *
 * Authentication: API key via X-API-Key header
 *
 * @author TSE 2025 Group 9
 */
@WebServlet("/api/clinics")
public class ClinicRegistrationServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ClinicRegistrationServlet.class);

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Inject
    private ClinicService clinicService;

    @Inject
    private UserService userService;

    @Inject
    private ApiConfigurationService apiConfigService;

    private final ObjectMapper objectMapper;

    public ClinicRegistrationServlet() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Validate API key
            String apiKey = extractApiKey(request);
            if (apiKey == null || apiKey.isEmpty()) {
                logger.warn("API key missing for clinic registration");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "API key required. Please provide X-API-Key or Authorization: Bearer header.");
                return;
            }

            if (!apiConfigService.validateApiKey(apiKey)) {
                logger.warn("Invalid API key for clinic registration");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid API key for clinic registration.");
                return;
            }

            // Parse request body
            String requestBody = readRequestBody(request);
            ClinicRegistrationRequest registrationRequest = objectMapper.readValue(requestBody, ClinicRegistrationRequest.class);

            logger.info("Clinic registration request received - code: {}, name: {}",
                    registrationRequest.getCode(), registrationRequest.getName());

            // Validate required fields
            if (registrationRequest.getCode() == null || registrationRequest.getCode().trim().isEmpty()) {
                logger.warn("Clinic registration failed - missing code");
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Clinic code is required");
                return;
            }

            if (registrationRequest.getName() == null || registrationRequest.getName().trim().isEmpty()) {
                logger.warn("Clinic registration failed - missing name");
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Clinic name is required");
                return;
            }

            if (registrationRequest.getHcenEndpoint() == null || registrationRequest.getHcenEndpoint().trim().isEmpty()) {
                logger.warn("Clinic registration failed - missing HCEN endpoint");
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "HCEN endpoint is required");
                return;
            }

            if (registrationRequest.getPassword() == null || registrationRequest.getPassword().trim().isEmpty()) {
                logger.warn("Clinic registration failed - missing admin password");
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Admin password is required");
                return;
            }

            if (!PasswordUtil.isValidPassword(registrationRequest.getPassword())) {
                logger.warn("Clinic registration failed - invalid password (minimum 6 characters required)");
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Password must be at least 6 characters");
                return;
            }

            // Check if clinic with this code already exists
            Optional<Clinic> existingClinic = clinicService.getClinicByCode(registrationRequest.getCode());
            if (existingClinic.isPresent()) {
                logger.warn("Clinic registration failed - duplicate code: {}", registrationRequest.getCode());
                sendErrorResponse(response, HttpServletResponse.SC_CONFLICT,
                        "Clinic with code '" + registrationRequest.getCode() + "' already exists");
                return;
            }

            // Map DTO to entity
            Clinic clinic = mapToEntity(registrationRequest);

            // Create clinic
            Clinic createdClinic = clinicService.createClinic(clinic);

            logger.info("Clinic registered successfully - ID: {}, code: {}, name: {}",
                    createdClinic.getId(), createdClinic.getCode(), createdClinic.getName());

            // Create admin user for the clinic
            User adminUser = createAdminUser(createdClinic, registrationRequest);
            logger.info("Admin user created for clinic - username: {}, email: {}",
                    adminUser.getUsername(), adminUser.getEmail());

            // Build response
            ClinicRegistrationResponse responseDto = buildResponse(createdClinic);

            response.setStatus(HttpServletResponse.SC_CREATED);
            PrintWriter out = response.getWriter();
            out.print(objectMapper.writeValueAsString(responseDto));
            out.flush();

        } catch (Exception e) {
            logger.error("Clinic registration failed - internal error", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error during clinic registration: " + e.getMessage());
        }
    }

    private String extractApiKey(HttpServletRequest request) {
        // Try X-API-Key header
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey != null && !apiKey.isEmpty()) {
            return apiKey.trim();
        }

        // Try Authorization: Bearer header
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }

        return null;
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private Clinic mapToEntity(ClinicRegistrationRequest request) {
        Clinic clinic = new Clinic();

        // Set ID from code (HCEN sends the clinic ID as code)
        clinic.setId(request.getCode());

        // Set basic fields
        clinic.setName(request.getName());
        clinic.setCode(request.getCode());
        clinic.setDescription(request.getDescription());
        clinic.setAddress(request.getAddress());
        clinic.setPhone(request.getPhone());
        clinic.setEmail(request.getEmail());
        clinic.setHcenEndpoint(request.getHcenEndpoint());
        clinic.setHcenJmsUrl(request.getHcenJmsUrl());
        clinic.setApiKey(request.getApiKey());
        clinic.setActive(request.getActive() != null ? request.getActive() : true);
        clinic.setHcenEndpoint(apiConfigService.getHcenUrl());

        return clinic;
    }

    private ClinicRegistrationResponse buildResponse(Clinic clinic) {
        ClinicRegistrationResponse response = new ClinicRegistrationResponse();
        response.setId(clinic.getId());
        response.setCode(clinic.getCode());
        response.setName(clinic.getName());
        response.setDescription(clinic.getDescription());
        response.setAddress(clinic.getAddress());
        response.setPhone(clinic.getPhone());
        response.setEmail(clinic.getEmail());
        response.setHcenEndpoint(clinic.getHcenEndpoint());
        response.setActive(clinic.getActive());
        response.setCreatedAt(clinic.getCreatedAt());
        return response;
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        out.print("{\"error\": \"" + message.replace("\"", "\\\"") + "\"}");
        out.flush();
    }

    private User createAdminUser(Clinic clinic, ClinicRegistrationRequest request) {
        User adminUser = new User();

        // Use email as username
        adminUser.setUsername(request.getEmail());
        adminUser.setEmail(request.getEmail());

        // Use the dedicated password field, hashed with BCrypt
        // Note: API key is used for API authentication, not for user login
        String hashedPassword = PasswordUtil.hashPassword(request.getPassword());
        adminUser.setPassword(hashedPassword);

        // Set name
        adminUser.setFirstName("Super");
        adminUser.setLastName("Admin");

        // Set role and status
        adminUser.setRole("ADMIN_CLINIC");
        adminUser.setActive(true);

        // Associate with clinic
        adminUser.setClinic(clinic);

        // Create the user
        return userService.createUser(adminUser);
    }

    /**
     * Response DTO for clinic registration
     */
    public static class ClinicRegistrationResponse {
        private String id;
        private String code;
        private String name;
        private String description;
        private String address;
        private String phone;
        private String email;
        private String hcenEndpoint;
        private Boolean active;
        private LocalDateTime createdAt;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getHcenEndpoint() { return hcenEndpoint; }
        public void setHcenEndpoint(String hcenEndpoint) { this.hcenEndpoint = hcenEndpoint; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
