package uy.gub.hcen.service.inus;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import uy.gub.hcen.cache.UserCacheService;
import uy.gub.hcen.inus.entity.InusUser;
import uy.gub.hcen.inus.entity.UserStatus;
import uy.gub.hcen.inus.repository.InusRepository;
import uy.gub.hcen.service.inus.exception.UserNotFoundException;
import uy.gub.hcen.service.inus.exception.UserRegistrationException;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * INUS Service - National User Index Service
 * <p>
 * Core business logic for managing the INUS (Índice Nacional de Usuarios de Salud),
 * the central registry of all health system users in Uruguay.
 * <p>
 * Key Responsibilities:
 * - User registration from peripheral nodes (AC013)
 * - User lookup with caching (findUserByCi, findUserByInusId)
 * - User profile updates and status management
 * - Eligibility validation and age verification
 * - Administrative search and reporting
 * <p>
 * Integration Points:
 * - InusRepository: Database persistence layer
 * - UserCacheService: Redis caching for performance (15-minute TTL)
 * - PDI Client: External identity validation (to be integrated)
 * - Audit Service: Access and modification logging (to be integrated)
 * <p>
 * Business Rules:
 * - CI must be valid Uruguayan format (7-8 digits with optional verification digit)
 * - Users must be 18+ years old for eligibility (ageVerified = true)
 * - Duplicate registrations are idempotent (returns existing user)
 * - All profile changes invalidate cache
 * - Status changes are logged for audit trail
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 */
@Stateless
public class InusService {

    private static final Logger LOGGER = Logger.getLogger(InusService.class.getName());

    /**
     * Uruguayan CI validation pattern
     * Accepts formats: 1234567-8, 12345678, 1.234.567-8
     */
    private static final Pattern CI_PATTERN = Pattern.compile("^\\d{1,2}(\\.?\\d{3}){2}\\-?\\d$");

    /**
     * Minimum age for health system eligibility (18 years)
     */
    private static final int MINIMUM_AGE = 18;

    @Inject
    private InusRepository inusRepository;

    @Inject
    private UserCacheService userCacheService;

    // ================================================================
    // User Registration (AC013)
    // ================================================================

    /**
     * Register a new user in the INUS system.
     * <p>
     * This method is called when peripheral nodes (clinics, health providers) register
     * new patients in the system. It performs the following steps:
     * 1. Validates CI format
     * 2. Checks for duplicate registration (idempotent behavior)
     * 3. Calculates age and sets age verification status
     * 4. Generates unique INUS ID
     * 5. Persists user to database
     * 6. Caches user profile
     * 7. Logs registration event
     * <p>
     * Future Enhancement: Integrate with PDI for identity validation
     *
     * @param ci          Cédula de Identidad (national ID number)
     * @param firstName   User's first name
     * @param lastName    User's last name
     * @param dateOfBirth User's date of birth
     * @param email       User's email address (optional, can be null)
     * @param phoneNumber User's phone number (optional, can be null)
     * @param clinicId    ID of the clinic registering the user
     * @return Registered InusUser (either newly created or existing)
     * @throws UserRegistrationException if registration fails due to validation or system errors
     */
    public InusUser registerUser(String ci, String firstName, String lastName,
                                  LocalDate dateOfBirth, String email,
                                  String phoneNumber, String clinicId)
            throws UserRegistrationException {

        // Input validation
        validateRegistrationInputs(ci, firstName, lastName, dateOfBirth);

        // Normalize CI (remove dots and dashes for storage)
        String normalizedCi = normalizeCi(ci);

        LOGGER.log(Level.INFO, "Processing user registration for CI: {0} from clinic: {1}",
                new Object[]{normalizedCi, clinicId});

        try {
            // Check for duplicate registration (idempotent behavior)
            Optional<InusUser> existingUser = inusRepository.findByCi(normalizedCi);
            if (existingUser.isPresent()) {
                LOGGER.log(Level.INFO, "User already registered with CI: {0}, returning existing user",
                        normalizedCi);
                return existingUser.get();
            }

            // Calculate age and determine age verification status
            int age = calculateAge(dateOfBirth);
            boolean ageVerified = age >= MINIMUM_AGE;

            // Generate unique INUS ID
            String inusId = generateInusId();

            // Create new user entity
            InusUser newUser = new InusUser(
                    normalizedCi,
                    inusId,
                    firstName,
                    lastName,
                    dateOfBirth,
                    email,
                    phoneNumber,
                    UserStatus.ACTIVE,
                    ageVerified
            );

            // Persist to database
            InusUser savedUser = inusRepository.save(newUser);

            // Cache user profile (15-minute TTL)
            cacheUser(savedUser);

            LOGGER.log(Level.INFO,
                    "Successfully registered user - CI: {0}, INUS ID: {1}, Age Verified: {2}",
                    new Object[]{normalizedCi, inusId, ageVerified});

            return savedUser;

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "User registration failed due to validation error: " + e.getMessage());
            throw new UserRegistrationException("User registration failed: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "User registration failed for CI: " + normalizedCi, e);
            throw new UserRegistrationException(
                    "System error during user registration for CI: " + normalizedCi, e);
        }
    }

    // ================================================================
    // User Lookup Operations (with Caching)
    // ================================================================

    /**
     * Find user by CI (Cédula de Identidad) with caching.
     * <p>
     * Caching Strategy:
     * 1. Check UserCacheService (Redis DB 1) - key: user:profile:{ci}
     * 2. If cache hit: deserialize and return
     * 3. If cache miss: query database, cache result, return
     *
     * @param ci National ID number
     * @return Optional containing user if found, empty otherwise
     */
    public Optional<InusUser> findUserByCi(String ci) {
        if (ci == null || ci.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to find user with null or empty CI");
            return Optional.empty();
        }

        String normalizedCi = normalizeCi(ci);

        try {
            // Try cache first
            Optional<InusUser> cachedUser = userCacheService.getCachedProfileAs(normalizedCi, InusUser.class);
            if (cachedUser.isPresent()) {
                LOGGER.log(Level.FINE, "Cache hit for user lookup - CI: {0}", normalizedCi);
                return cachedUser;
            }

            // Cache miss - query database
            LOGGER.log(Level.FINE, "Cache miss for user lookup - CI: {0}, querying database", normalizedCi);
            Optional<InusUser> user = inusRepository.findByCi(normalizedCi);

            // Cache the result if found
            user.ifPresent(this::cacheUser);

            return user;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during user lookup by CI: " + normalizedCi, e);
            // Fall back to database query
            return inusRepository.findByCi(normalizedCi);
        }
    }

    /**
     * Find user by INUS ID (cross-clinic unique identifier) with caching.
     * <p>
     * Note: Cache is keyed by CI, so we query database first, then cache by CI.
     *
     * @param inusId Unique cross-clinic identifier
     * @return Optional containing user if found, empty otherwise
     */
    public Optional<InusUser> findUserByInusId(String inusId) {
        if (inusId == null || inusId.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to find user with null or empty INUS ID");
            return Optional.empty();
        }

        try {
            // Query by INUS ID
            Optional<InusUser> user = inusRepository.findByInusId(inusId);

            // Cache by CI if found
            user.ifPresent(this::cacheUser);

            return user;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during user lookup by INUS ID: " + inusId, e);
            return Optional.empty();
        }
    }

    // ================================================================
    // User Profile Update Operations
    // ================================================================

    /**
     * Update user profile information.
     * <p>
     * Allows updating: firstName, lastName, email, phoneNumber
     * Immutable fields: ci, inusId, dateOfBirth, status, ageVerified
     *
     * @param ci          User's CI
     * @param firstName   Updated first name
     * @param lastName    Updated last name
     * @param email       Updated email (can be null)
     * @param phoneNumber Updated phone number (can be null)
     * @return Updated user entity
     * @throws UserNotFoundException if user does not exist
     */
    public InusUser updateUserProfile(String ci, String firstName, String lastName,
                                      String email, String phoneNumber)
            throws UserNotFoundException {

        if (ci == null || ci.trim().isEmpty()) {
            throw new IllegalArgumentException("CI cannot be null or empty");
        }

        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }

        String normalizedCi = normalizeCi(ci);

        LOGGER.log(Level.INFO, "Updating user profile for CI: {0}", normalizedCi);

        try {
            // Find existing user
            InusUser user = inusRepository.findByCi(normalizedCi)
                    .orElseThrow(() -> new UserNotFoundException("User not found with CI: " + normalizedCi));

            // Update mutable fields
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);

            // Persist changes
            InusUser updatedUser = inusRepository.update(user);

            // Invalidate cache to ensure fresh data on next lookup
            userCacheService.invalidateUserProfile(normalizedCi);

            LOGGER.log(Level.INFO, "Successfully updated user profile for CI: {0}", normalizedCi);

            return updatedUser;

        } catch (UserNotFoundException e) {
            LOGGER.log(Level.WARNING, "Update failed - user not found: " + normalizedCi);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating user profile for CI: " + normalizedCi, e);
            throw new RuntimeException("System error during user profile update", e);
        }
    }

    // ================================================================
    // User Status Management
    // ================================================================

    /**
     * Activate a user (set status to ACTIVE).
     *
     * @param ci User's CI
     * @throws UserNotFoundException if user does not exist
     */
    public void activateUser(String ci) throws UserNotFoundException {
        updateUserStatus(ci, UserStatus.ACTIVE, "activated");
    }

    /**
     * Deactivate a user (set status to INACTIVE).
     * <p>
     * Use cases: user deceased, moved abroad, voluntarily left system
     *
     * @param ci User's CI
     * @throws UserNotFoundException if user does not exist
     */
    public void deactivateUser(String ci) throws UserNotFoundException {
        updateUserStatus(ci, UserStatus.INACTIVE, "deactivated");
    }

    /**
     * Suspend a user (set status to SUSPENDED).
     * <p>
     * Use cases: administrative reasons, fraud detection, policy violations
     *
     * @param ci User's CI
     * @throws UserNotFoundException if user does not exist
     */
    public void suspendUser(String ci) throws UserNotFoundException {
        updateUserStatus(ci, UserStatus.SUSPENDED, "suspended");
    }

    /**
     * Internal method to update user status.
     *
     * @param ci        User's CI
     * @param newStatus New status to set
     * @param action    Action description for logging
     * @throws UserNotFoundException if user does not exist
     */
    private void updateUserStatus(String ci, UserStatus newStatus, String action)
            throws UserNotFoundException {

        if (ci == null || ci.trim().isEmpty()) {
            throw new IllegalArgumentException("CI cannot be null or empty");
        }

        String normalizedCi = normalizeCi(ci);

        LOGGER.log(Level.INFO, "Attempting to {0} user with CI: {1}",
                new Object[]{action, normalizedCi});

        try {
            // Find existing user
            InusUser user = inusRepository.findByCi(normalizedCi)
                    .orElseThrow(() -> new UserNotFoundException("User not found with CI: " + normalizedCi));

            UserStatus oldStatus = user.getStatus();

            // Update status
            user.setStatus(newStatus);

            // Persist changes
            inusRepository.update(user);

            // Invalidate cache
            userCacheService.invalidateUserProfile(normalizedCi);

            LOGGER.log(Level.INFO, "Successfully {0} user - CI: {1}, Status: {2} -> {3}",
                    new Object[]{action, normalizedCi, oldStatus, newStatus});

        } catch (UserNotFoundException e) {
            LOGGER.log(Level.WARNING, "Status update failed - user not found: " + normalizedCi);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating user status for CI: " + normalizedCi, e);
            throw new RuntimeException("System error during user status update", e);
        }
    }

    // ================================================================
    // Eligibility and Validation
    // ================================================================

    /**
     * Validate if user is eligible for the health system.
     * <p>
     * Eligibility criteria:
     * - User exists in INUS
     * - Status is ACTIVE
     * - Age is verified (18+ years old)
     *
     * @param ci User's CI
     * @return true if user is eligible, false otherwise
     */
    public boolean validateUserEligibility(String ci) {
        if (ci == null || ci.trim().isEmpty()) {
            return false;
        }

        String normalizedCi = normalizeCi(ci);

        try {
            Optional<InusUser> userOpt = findUserByCi(normalizedCi);

            if (userOpt.isEmpty()) {
                LOGGER.log(Level.FINE, "Eligibility check failed - user not found: {0}", normalizedCi);
                return false;
            }

            InusUser user = userOpt.get();
            boolean eligible = user.getStatus() == UserStatus.ACTIVE && user.getAgeVerified();

            LOGGER.log(Level.FINE, "Eligibility check for CI {0}: {1} (Status: {2}, Age Verified: {3})",
                    new Object[]{normalizedCi, eligible, user.getStatus(), user.getAgeVerified()});

            return eligible;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during eligibility validation for CI: " + normalizedCi, e);
            return false;
        }
    }

    /**
     * Check if user's age has been verified (18+ years old).
     *
     * @param ci User's CI
     * @return true if age is verified, false otherwise
     */
    public boolean isAgeVerified(String ci) {
        if (ci == null || ci.trim().isEmpty()) {
            return false;
        }

        String normalizedCi = normalizeCi(ci);

        try {
            Optional<InusUser> userOpt = findUserByCi(normalizedCi);

            if (userOpt.isEmpty()) {
                return false;
            }

            return userOpt.get().getAgeVerified();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking age verification for CI: " + normalizedCi, e);
            return false;
        }
    }

    // ================================================================
    // Search and Admin Operations
    // ================================================================

    /**
     * Search users by query string.
     * <p>
     * Searches in: firstName, lastName, CI, inusId
     *
     * @param query Search query
     * @param page  Page number (0-based)
     * @param size  Number of items per page
     * @return List of users matching the query
     */
    public List<InusUser> searchUsers(String query, int page, int size) {
        if (query == null || query.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Search attempted with empty query, returning empty list");
            return List.of();
        }

        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters: page=" + page + ", size=" + size);
        }

        try {
            return inusRepository.searchUsers(query.trim(), page, size);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error searching users with query: " + query, e);
            return List.of();
        }
    }

    /**
     * Find all users with pagination.
     *
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @return List of users for the requested page
     */
    public List<InusUser> findAllUsers(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters: page=" + page + ", size=" + size);
        }

        try {
            return inusRepository.findAll(page, size);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all users (page: " + page + ", size: " + size + ")", e);
            return List.of();
        }
    }

    /**
     * Count total number of users in the system.
     *
     * @return Total user count
     */
    public long countUsers() {
        try {
            return inusRepository.count();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting users", e);
            return 0L;
        }
    }

    /**
     * Count users by status.
     *
     * @param status User status to count
     * @return Number of users with the specified status
     */
    public long countUsersByStatus(UserStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        try {
            return inusRepository.countByStatus(status);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting users by status: " + status, e);
            return 0L;
        }
    }

    // ================================================================
    // Private Helper Methods
    // ================================================================

    /**
     * Validate registration inputs.
     *
     * @param ci          CI to validate
     * @param firstName   First name to validate
     * @param lastName    Last name to validate
     * @param dateOfBirth Date of birth to validate
     * @throws UserRegistrationException if any validation fails
     */
    private void validateRegistrationInputs(String ci, String firstName, String lastName, LocalDate dateOfBirth)
            throws UserRegistrationException {

        if (ci == null || ci.trim().isEmpty()) {
            throw new UserRegistrationException("CI cannot be null or empty");
        }

        if (!isValidCi(ci)) {
            throw new UserRegistrationException("Invalid CI format: " + ci);
        }

        if (firstName == null || firstName.trim().isEmpty()) {
            throw new UserRegistrationException("First name cannot be null or empty");
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            throw new UserRegistrationException("Last name cannot be null or empty");
        }

        if (dateOfBirth == null) {
            throw new UserRegistrationException("Date of birth cannot be null");
        }

        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new UserRegistrationException("Date of birth cannot be in the future");
        }
    }

    /**
     * Validate Uruguayan CI format.
     * <p>
     * Accepts formats: 1234567-8, 12345678, 1.234.567-8
     *
     * @param ci CI to validate
     * @return true if valid format, false otherwise
     */
    private boolean isValidCi(String ci) {
        if (ci == null || ci.trim().isEmpty()) {
            return false;
        }
        return CI_PATTERN.matcher(ci.trim()).matches();
    }

    /**
     * Normalize CI by removing dots and dashes.
     * <p>
     * Examples:
     * - "1.234.567-8" -> "12345678"
     * - "1234567-8" -> "12345678"
     * - "12345678" -> "12345678"
     *
     * @param ci CI to normalize
     * @return Normalized CI (digits only)
     */
    private String normalizeCi(String ci) {
        if (ci == null) {
            return null;
        }
        return ci.replaceAll("[.\\-]", "").trim();
    }

    /**
     * Calculate age from date of birth.
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
     * Generate unique INUS ID using UUID.
     * <p>
     * Format: INUS-{UUID}
     * Example: INUS-550e8400-e29b-41d4-a716-446655440000
     *
     * @return Unique INUS ID
     */
    private String generateInusId() {
        return "INUS-" + UUID.randomUUID().toString();
    }

    /**
     * Cache user profile in Redis.
     *
     * @param user User to cache
     */
    private void cacheUser(InusUser user) {
        if (user == null) {
            return;
        }

        try {
            userCacheService.cacheUserProfile(user.getCi(), user);
        } catch (Exception e) {
            // Don't fail the operation if caching fails, just log it
            LOGGER.log(Level.WARNING, "Failed to cache user profile for CI: " + user.getCi(), e);
        }
    }
}
