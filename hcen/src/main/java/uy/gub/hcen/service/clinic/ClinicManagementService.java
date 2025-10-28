package uy.gub.hcen.service.clinic;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.hcen.clinic.entity.Clinic;
import uy.gub.hcen.clinic.entity.Clinic.ClinicStatus;
import uy.gub.hcen.clinic.repository.ClinicRepository;
import uy.gub.hcen.inus.repository.InusRepository;
import uy.gub.hcen.rndc.repository.RndcRepository;
import uy.gub.hcen.audit.entity.AuditLog.ActionOutcome;
import uy.gub.hcen.audit.entity.AuditLog.EventType;
import uy.gub.hcen.service.audit.AuditService;
import uy.gub.hcen.integration.peripheral.PeripheralNodeClient;
import uy.gub.hcen.integration.peripheral.PeripheralNodeException;
import uy.gub.hcen.service.clinic.dto.*;
import uy.gub.hcen.service.clinic.exception.ClinicNotFoundException;
import uy.gub.hcen.service.clinic.exception.ClinicRegistrationException;
import uy.gub.hcen.service.clinic.exception.OnboardingException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Clinic Management Service
 * <p>
 * Core business logic for managing clinics and health facilities in the HCEN system.
 * <p>
 * Key Responsibilities:
 * - Clinic registration and API key generation
 * - Clinic profile updates and status management
 * - Clinic onboarding to peripheral nodes (AC016)
 * - Clinic statistics and reporting
 * - Administrative search and filtering
 * <p>
 * Integration Points:
 * - ClinicRepository: Database persistence layer
 * - AuditService: Access and modification logging
 * - InusRepository: User statistics per clinic
 * - RndcRepository: Document statistics per clinic
 * - PeripheralNodeClient: Onboarding communication (AC016)
 * <p>
 * Business Rules:
 * - Clinic IDs are auto-generated (format: clinic-{uuid})
 * - API keys are securely generated (64 bytes, Base64-encoded)
 * - Only ACTIVE clinics can register users and documents
 * - Status transitions: PENDING_ONBOARDING → ACTIVE → INACTIVE
 * - All administrative actions are logged in audit trail
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
@Stateless
public class ClinicManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ClinicManagementService.class);

    /**
     * HCEN Central API base URL (from configuration)
     * TODO: Read from application.properties
     */
    private static final String HCEN_CENTRAL_URL = "https://hcen.uy/api";

    /**
     * Secure random generator for API key generation
     */
    private static final SecureRandom secureRandom = new SecureRandom();

    @Inject
    private ClinicRepository clinicRepository;

    @Inject
    private AuditService auditService;

    @Inject
    private InusRepository inusRepository;

    @Inject
    private RndcRepository rndcRepository;

    @Inject
    private PeripheralNodeClient peripheralNodeClient;

    // ================================================================
    // Clinic Registration (CU10)
    // ================================================================

    /**
     * Register a new clinic in the HCEN system.
     * <p>
     * This method is called when HCEN administrators register new clinics/health facilities.
     * It performs the following steps:
     * 1. Validates clinic information
     * 2. Generates unique clinic ID
     * 3. Generates secure API key for peripheral node authentication
     * 4. Creates clinic with PENDING_ONBOARDING status
     * 5. Persists clinic to database
     * 6. Logs registration event
     * <p>
     * After registration, admin must trigger onboarding via onboardClinic() method.
     *
     * @param request Clinic registration request
     * @return Registered clinic response with unmasked API key (show only once)
     * @throws ClinicRegistrationException if registration fails due to validation or system errors
     */
    public ClinicResponse registerClinic(ClinicRegistrationRequest request) throws ClinicRegistrationException {
        logger.info("Processing clinic registration for: {}", request.getClinicName());

        try {
            // Validate input (additional validation beyond Jakarta Bean Validation)
            validateRegistrationInputs(request);

            // Generate unique clinic ID
            String clinicId = generateClinicId();

            // Generate secure API key
            String apiKey = generateApiKey();

            // Create new clinic entity
            Clinic newClinic = new Clinic(
                    clinicId,
                    request.getClinicName(),
                    request.getAddress(),
                    request.getCity(),
                    request.getPhoneNumber(),
                    request.getEmail(),
                    request.getPeripheralNodeUrl(),
                    apiKey
            );

            // Status is automatically set to PENDING_ONBOARDING by entity constructor

            // Persist to database
            Clinic savedClinic = clinicRepository.save(newClinic);

            logger.info("Successfully registered clinic with ID: {}", clinicId);

            // Log registration in audit trail
            auditService.logEvent(
                    EventType.CREATION,
                    "ADMIN", // TODO: Get actual admin user ID from security context
                    "ADMIN",
                    "CLINIC",
                    clinicId,
                    ActionOutcome.SUCCESS,
                    null, // IP address
                    null, // User agent
                    java.util.Map.of(
                            "clinicName", request.getClinicName(),
                            "city", request.getCity(),
                            "action", "CLINIC_REGISTRATION"
                    )
            );

            // Return response with UNMASKED API key (only shown once at registration)
            return new ClinicResponse(savedClinic, false);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid clinic registration data: {}", e.getMessage());
            throw new ClinicRegistrationException("Invalid clinic data: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to register clinic: {}", request.getClinicName(), e);
            throw new ClinicRegistrationException("Failed to register clinic: " + e.getMessage(), e);
        }
    }

    // ================================================================
    // Clinic Lookup and Search
    // ================================================================

    /**
     * Find a clinic by its unique ID
     *
     * @param clinicId Clinic ID
     * @return Clinic response (with masked API key)
     * @throws ClinicNotFoundException if clinic not found
     */
    public ClinicResponse findClinicById(String clinicId) throws ClinicNotFoundException {
        logger.debug("Looking up clinic by ID: {}", clinicId);

        Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);

        if (clinicOpt.isEmpty()) {
            logger.warn("Clinic not found: {}", clinicId);
            throw new ClinicNotFoundException("Clinic not found with ID: " + clinicId);
        }

        return new ClinicResponse(clinicOpt.get(), true); // Masked API key
    }

    /**
     * List all clinics with pagination
     *
     * @param page Page number (zero-based)
     * @param size Page size
     * @return Paginated list of clinics
     */
    public ClinicListResponse listClinics(int page, int size) {
        logger.debug("Listing clinics (page={}, size={})", page, size);

        List<Clinic> clinics = clinicRepository.findAll(page, size);
        long totalCount = clinicRepository.count();

        List<ClinicResponse> clinicResponses = clinics.stream()
                .map(clinic -> new ClinicResponse(clinic, true))
                .collect(Collectors.toList());

        return new ClinicListResponse(clinicResponses, totalCount, page, size);
    }

    /**
     * Find clinics by status with pagination
     *
     * @param status Clinic status
     * @param page Page number
     * @param size Page size
     * @return Paginated list of clinics
     */
    public ClinicListResponse findClinicsByStatus(ClinicStatus status, int page, int size) {
        logger.debug("Finding clinics by status: {} (page={}, size={})", status, page, size);

        List<Clinic> clinics = clinicRepository.findByStatus(status, page, size);
        long totalCount = clinicRepository.countByStatus(status);

        List<ClinicResponse> clinicResponses = clinics.stream()
                .map(clinic -> new ClinicResponse(clinic, true))
                .collect(Collectors.toList());

        return new ClinicListResponse(clinicResponses, totalCount, page, size);
    }

    /**
     * Find clinics by city with pagination
     *
     * @param city City name
     * @param page Page number
     * @param size Page size
     * @return Paginated list of clinics
     */
    public ClinicListResponse findClinicsByCity(String city, int page, int size) {
        logger.debug("Finding clinics by city: {} (page={}, size={})", city, page, size);

        List<Clinic> clinics = clinicRepository.findByCity(city, page, size);
        // Note: We don't have countByCity in repository, so we count from result
        long totalCount = clinics.size(); // TODO: Add countByCity to repository for accurate pagination

        List<ClinicResponse> clinicResponses = clinics.stream()
                .map(clinic -> new ClinicResponse(clinic, true))
                .collect(Collectors.toList());

        return new ClinicListResponse(clinicResponses, totalCount, page, size);
    }

    // ================================================================
    // Clinic Updates and Status Management
    // ================================================================

    /**
     * Update clinic information
     *
     * @param clinicId Clinic ID
     * @param request Update request
     * @return Updated clinic response
     * @throws ClinicNotFoundException if clinic not found
     */
    public ClinicResponse updateClinic(String clinicId, ClinicUpdateRequest request) throws ClinicNotFoundException {
        logger.info("Updating clinic: {}", clinicId);

        Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);

        if (clinicOpt.isEmpty()) {
            logger.warn("Clinic not found for update: {}", clinicId);
            throw new ClinicNotFoundException("Clinic not found with ID: " + clinicId);
        }

        Clinic clinic = clinicOpt.get();

        // Apply updates (only if fields are provided)
        if (request.getClinicName() != null && !request.getClinicName().isBlank()) {
            clinic.setClinicName(request.getClinicName());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            clinic.setAddress(request.getAddress());
        }
        if (request.getCity() != null && !request.getCity().isBlank()) {
            clinic.setCity(request.getCity());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            clinic.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            clinic.setEmail(request.getEmail());
        }
        if (request.getPeripheralNodeUrl() != null && !request.getPeripheralNodeUrl().isBlank()) {
            clinic.setPeripheralNodeUrl(request.getPeripheralNodeUrl());
        }

        // Update in database
        Clinic updatedClinic = clinicRepository.update(clinic);

        logger.info("Successfully updated clinic: {}", clinicId);

        // Log modification in audit trail
        auditService.logEvent(
                EventType.MODIFICATION,
                "ADMIN", // TODO: Get from security context
                "ADMIN",
                "CLINIC",
                clinicId,
                ActionOutcome.SUCCESS,
                null,
                null,
                java.util.Map.of("action", "CLINIC_UPDATE")
        );

        return new ClinicResponse(updatedClinic, true);
    }

    /**
     * Activate a clinic (transition from PENDING_ONBOARDING or INACTIVE to ACTIVE)
     *
     * @param clinicId Clinic ID
     * @return Updated clinic response
     * @throws ClinicNotFoundException if clinic not found
     */
    public ClinicResponse activateClinic(String clinicId) throws ClinicNotFoundException {
        logger.info("Activating clinic: {}", clinicId);

        Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);

        if (clinicOpt.isEmpty()) {
            logger.warn("Clinic not found for activation: {}", clinicId);
            throw new ClinicNotFoundException("Clinic not found with ID: " + clinicId);
        }

        Clinic clinic = clinicOpt.get();
        clinic.activate(); // Sets status to ACTIVE and sets onboardedAt if null

        clinicRepository.update(clinic);

        logger.info("Successfully activated clinic: {}", clinicId);

        // Log status change in audit trail
        auditService.logEvent(
                EventType.MODIFICATION,
                "ADMIN", // TODO: Get from security context
                "ADMIN",
                "CLINIC",
                clinicId,
                ActionOutcome.SUCCESS,
                null,
                null,
                java.util.Map.of("action", "CLINIC_ACTIVATION", "newStatus", "ACTIVE")
        );

        return new ClinicResponse(clinic, true);
    }

    /**
     * Deactivate a clinic (transition from ACTIVE to INACTIVE)
     *
     * @param clinicId Clinic ID
     * @return Updated clinic response
     * @throws ClinicNotFoundException if clinic not found
     */
    public ClinicResponse deactivateClinic(String clinicId) throws ClinicNotFoundException {
        logger.info("Deactivating clinic: {}", clinicId);

        Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);

        if (clinicOpt.isEmpty()) {
            logger.warn("Clinic not found for deactivation: {}", clinicId);
            throw new ClinicNotFoundException("Clinic not found with ID: " + clinicId);
        }

        Clinic clinic = clinicOpt.get();
        clinic.deactivate(); // Sets status to INACTIVE

        clinicRepository.update(clinic);

        logger.info("Successfully deactivated clinic: {}", clinicId);

        // Log status change in audit trail
        auditService.logEvent(
                EventType.MODIFICATION,
                "ADMIN", // TODO: Get from security context
                "ADMIN",
                "CLINIC",
                clinicId,
                ActionOutcome.SUCCESS,
                null,
                null,
                java.util.Map.of("action", "CLINIC_DEACTIVATION", "newStatus", "INACTIVE")
        );

        return new ClinicResponse(clinic, true);
    }

    // ================================================================
    // Clinic Onboarding (AC016)
    // ================================================================

    /**
     * Onboard a clinic to its peripheral node (AC016).
     * <p>
     * This method sends onboarding configuration to the peripheral node via POST request.
     * The peripheral node receives clinic ID, API key, and HCEN central URL.
     * <p>
     * On successful onboarding, clinic status transitions to ACTIVE.
     *
     * @param clinicId Clinic ID to onboard
     * @return Onboarding response with status
     * @throws ClinicNotFoundException if clinic not found
     * @throws OnboardingException if onboarding fails (network, peripheral node error, etc.)
     */
    public OnboardingResponse onboardClinic(String clinicId) throws ClinicNotFoundException, OnboardingException {
        logger.info("Starting onboarding process for clinic: {}", clinicId);

        Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);

        if (clinicOpt.isEmpty()) {
            logger.warn("Clinic not found for onboarding: {}", clinicId);
            throw new ClinicNotFoundException("Clinic not found with ID: " + clinicId);
        }

        Clinic clinic = clinicOpt.get();

        // Check if already onboarded
        if (clinic.getStatus() == ClinicStatus.ACTIVE && clinic.getOnboardedAt() != null) {
            logger.info("Clinic already onboarded: {}", clinicId);
            return new OnboardingResponse(
                    clinicId,
                    clinic.getClinicName(),
                    "ALREADY_ONBOARDED",
                    "Clinic is already onboarded and active",
                    true
            );
        }

        try {
            // Build onboarding request
            OnboardingRequest onboardingRequest = buildOnboardingRequest(clinic);

            // Send onboarding request to peripheral node (AC016)
            boolean peripheralConfirmed = peripheralNodeClient.sendOnboardingData(
                    clinic.getPeripheralNodeUrl(),
                    onboardingRequest
            );

            if (peripheralConfirmed) {
                // Activate clinic (sets status to ACTIVE and onboardedAt timestamp)
                clinic.activate();
                clinicRepository.update(clinic);

                logger.info("Successfully onboarded clinic: {}", clinicId);

                // Log onboarding event in audit trail
                auditService.logEvent(
                        EventType.MODIFICATION,
                        "ADMIN", // TODO: Get from security context
                        "ADMIN",
                        "CLINIC",
                        clinicId,
                        ActionOutcome.SUCCESS,
                        null,
                        null,
                        java.util.Map.of(
                                "action", "CLINIC_ONBOARDING",
                                "peripheralNodeUrl", clinic.getPeripheralNodeUrl(),
                                "onboardedAt", LocalDateTime.now().toString()
                        )
                );

                return new OnboardingResponse(
                        clinicId,
                        clinic.getClinicName(),
                        "SUCCESS",
                        "Clinic successfully onboarded to peripheral node",
                        true
                );
            } else {
                logger.error("Peripheral node did not confirm onboarding for clinic: {}", clinicId);

                // Log failed onboarding
                auditService.logEvent(
                        EventType.MODIFICATION,
                        "ADMIN",
                        "ADMIN",
                        "CLINIC",
                        clinicId,
                        ActionOutcome.FAILURE,
                        null,
                        null,
                        java.util.Map.of("action", "CLINIC_ONBOARDING_FAILED")
                );

                throw new OnboardingException("Peripheral node did not confirm successful onboarding");
            }

        } catch (PeripheralNodeException e) {
            logger.error("Peripheral node communication failed for clinic: {}", clinicId, e);
            throw new OnboardingException("Failed to communicate with peripheral node: " + e.getMessage(), e);
        } catch (OnboardingException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to onboard clinic: {}", clinicId, e);
            throw new OnboardingException("Failed to onboard clinic: " + e.getMessage(), e);
        }
    }

    /**
     * Build onboarding request for peripheral node
     *
     * @param clinic Clinic entity
     * @return Onboarding request DTO
     */
    private OnboardingRequest buildOnboardingRequest(Clinic clinic) {
        OnboardingRequest.ClinicConfig config = new OnboardingRequest.ClinicConfig(
                clinic.getClinicName(),
                clinic.getAddress(),
                clinic.getCity(),
                clinic.getPhoneNumber(),
                clinic.getEmail()
        );

        return new OnboardingRequest(
                clinic.getClinicId(),
                clinic.getApiKey(),
                HCEN_CENTRAL_URL,
                config
        );
    }

    // ================================================================
    // Clinic Statistics (CU11)
    // ================================================================

    /**
     * Get statistics for a specific clinic
     *
     * @param clinicId Clinic ID
     * @return Clinic statistics (users, documents, policies)
     * @throws ClinicNotFoundException if clinic not found
     */
    public ClinicStatisticsResponse getClinicStatistics(String clinicId) throws ClinicNotFoundException {
        logger.debug("Fetching statistics for clinic: {}", clinicId);

        Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);

        if (clinicOpt.isEmpty()) {
            logger.warn("Clinic not found for statistics: {}", clinicId);
            throw new ClinicNotFoundException("Clinic not found with ID: " + clinicId);
        }

        Clinic clinic = clinicOpt.get();

        // Get statistics from various repositories
        // Note: These queries might need to be added to repositories if not present
        long totalUsers = 0; // TODO: inusRepository.countByClinicId(clinicId)
        long totalDocuments = 0; // TODO: rndcRepository.countByClinicId(clinicId)
        long totalPolicies = 0; // TODO: policyRepository.countByClinicId(clinicId)

        logger.debug("Clinic {} statistics: users={}, documents={}, policies={}",
                clinicId, totalUsers, totalDocuments, totalPolicies);

        return new ClinicStatisticsResponse(
                clinicId,
                clinic.getClinicName(),
                totalUsers,
                totalDocuments,
                totalPolicies,
                clinic.getStatus().name()
        );
    }

    // ================================================================
    // Helper Methods
    // ================================================================

    /**
     * Validate clinic registration inputs (additional validation beyond Bean Validation)
     *
     * @param request Registration request
     * @throws IllegalArgumentException if validation fails
     */
    private void validateRegistrationInputs(ClinicRegistrationRequest request) {
        // Additional custom validation can be added here
        // Bean Validation handles most cases, but we can add business-specific rules

        // Verify peripheral node URL is accessible (future enhancement)
        // validatePeripheralNodeUrl(request.getPeripheralNodeUrl());
    }

    /**
     * Generate unique clinic ID
     * Format: clinic-{uuid}
     *
     * @return Generated clinic ID
     */
    private String generateClinicId() {
        return "clinic-" + UUID.randomUUID().toString();
    }

    /**
     * Generate secure API key for clinic authentication
     * Uses SecureRandom with 64 bytes, Base64-encoded
     *
     * @return Generated API key (Base64-encoded, ~88 characters)
     */
    private String generateApiKey() {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
