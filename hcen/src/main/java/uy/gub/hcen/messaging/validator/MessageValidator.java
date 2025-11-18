package uy.gub.hcen.messaging.validator;

import uy.gub.hcen.messaging.dto.*;
import uy.gub.hcen.messaging.exception.InvalidMessageException;

import jakarta.enterprise.context.ApplicationScoped;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Centralized message validation service for JMS messages.
 * <p>
 * Validates all incoming messages before processing to ensure:
 * - Required fields are present
 * - Data formats are correct
 * - Business rules are satisfied
 * <p>
 * Validation failures throw InvalidMessageException, which causes messages
 * to be moved to DLQ (not retried).
 * <p>
 * Usage:
 * <pre>
 * @Inject
 * private MessageValidator validator;
 *
 * public void onMessage(Message jmsMessage) {
 *     UserRegistrationMessage message = deserialize(jmsMessage);
 *     validator.validateUserRegistrationMessage(message);
 *     // ... process message
 * }
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@ApplicationScoped
public class MessageValidator {

    private static final Logger LOGGER = Logger.getLogger(MessageValidator.class.getName());

    /**
     * Uruguayan CI validation pattern.
     * Accepts formats: 1234567-8, 12345678, 1.234.567-8
     */
    private static final Pattern CI_PATTERN = Pattern.compile("^\\d{1,2}(\\.?\\d{3}){2}\\-?\\d$");

    /**
     * SHA-256 hash pattern for document integrity.
     * Format: sha256:[64 lowercase hex characters]
     */
    private static final Pattern HASH_PATTERN = Pattern.compile("^sha256:[a-f0-9]{64}$");

    /**
     * UUID pattern for message IDs.
     * Format: lowercase UUID (with or without hyphens)
     */
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[a-f0-9]{8}-?[a-f0-9]{4}-?[a-f0-9]{4}-?[a-f0-9]{4}-?[a-f0-9]{12}$"
    );

    /**
     * Maximum age in days for accepting messages (prevent stale messages).
     * Messages older than this are rejected.
     */
    private static final int MAX_MESSAGE_AGE_DAYS = 7;

    // ================================================================
    // User Registration Message Validation
    // ================================================================

    /**
     * Validate user registration message.
     * <p>
     * Checks:
     * - Base message fields (messageId, timestamp, sourceSystem, eventType)
     * - Payload presence
     * - User data (CI, names, dateOfBirth, clinicId)
     *
     * @param message Message to validate
     * @throws InvalidMessageException if validation fails
     */
    public void validateUserRegistrationMessage(UserRegistrationMessage message)
            throws InvalidMessageException {

        if (message == null) {
            throw new InvalidMessageException("Message cannot be null");
        }

        LOGGER.log(Level.FINE, "Validating user registration message: {0}", message.getMessageId());

        // Validate base fields
        validateBaseMessage(message);

        // Validate event type
        if (!"patient-create".equals(message.getEventType())) {
            throw new InvalidMessageException(
                    "Invalid event type for user registration: " + message.getEventType(),
                    message.getMessageId(),
                    "eventType"
            );
        }

        // Validate payload presence
        UserRegistrationPayload payload = message.getPayload();
        if (payload == null) {
            throw new InvalidMessageException(
                    "Payload is required for user registration message",
                    message.getMessageId(),
                    "payload"
            );
        }

        // Validate payload fields
        validateUserRegistrationPayload(payload, message.getMessageId());

        LOGGER.log(Level.FINE, "User registration message validation passed: {0}", message.getMessageId());
    }

    /**
     * Validate user registration payload fields.
     *
     * @param payload   Payload to validate
     * @param messageId Message ID for error reporting
     * @throws InvalidMessageException if validation fails
     */
    private void validateUserRegistrationPayload(UserRegistrationPayload payload, String messageId)
            throws InvalidMessageException {

        // Validate CI
        if (payload.getCi() == null || payload.getCi().trim().isEmpty()) {
            throw new InvalidMessageException(
                    "CI is required",
                    messageId,
                    "payload.ci"
            );
        }

        if (!isValidCi(payload.getCi())) {
            throw new InvalidMessageException(
                    "Invalid CI format: " + payload.getCi(),
                    messageId,
                    "payload.ci"
            );
        }

        // Validate first name
        if (payload.getFirstName() == null || payload.getFirstName().trim().isEmpty()) {
            throw new InvalidMessageException(
                    "First name is required",
                    messageId,
                    "payload.firstName"
            );
        }

        // Validate last name
        if (payload.getLastName() == null || payload.getLastName().trim().isEmpty()) {
            throw new InvalidMessageException(
                    "Last name is required",
                    messageId,
                    "payload.lastName"
            );
        }

        // Validate date of birth
        if (payload.getDateOfBirth() == null) {
            throw new InvalidMessageException(
                    "Date of birth is required",
                    messageId,
                    "payload.dateOfBirth"
            );
        }

        if (payload.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new InvalidMessageException(
                    "Date of birth cannot be in the future: " + payload.getDateOfBirth(),
                    messageId,
                    "payload.dateOfBirth"
            );
        }

        // Validate clinic ID
        if (payload.getClinicId() == null || payload.getClinicId().trim().isEmpty()) {
            throw new InvalidMessageException(
                    "Clinic ID is required",
                    messageId,
                    "payload.clinicId"
            );
        }

        // Optional: validate email format if provided
        if (payload.getEmail() != null && !payload.getEmail().trim().isEmpty()) {
            if (!isValidEmail(payload.getEmail())) {
                throw new InvalidMessageException(
                        "Invalid email format: " + payload.getEmail(),
                        messageId,
                        "payload.email"
                );
            }
        }
    }

    // ================================================================
    // Document Registration Message Validation
    // ================================================================

    /**
     * Validate document registration message.
     * <p>
     * Checks:
     * - Base message fields (messageId, timestamp, sourceSystem, eventType)
     * - Payload presence
     * - Document metadata (patientCI, documentType, locator, hash, etc.)
     *
     * @param message Message to validate
     * @throws InvalidMessageException if validation fails
     */
    public void validateDocumentRegistrationMessage(DocumentRegistrationMessage message)
            throws InvalidMessageException {

        if (message == null) {
            throw new InvalidMessageException("Message cannot be null");
        }

        LOGGER.log(Level.FINE, "Validating document registration message: {0}", message.getMessageId());

        // Validate base fields
        validateBaseMessage(message);

        // Validate event type
        if (!"DOCUMENT_CREATED".equals(message.getEventType())) {
            throw new InvalidMessageException(
                    "Invalid event type for document registration: " + message.getEventType(),
                    message.getMessageId(),
                    "eventType"
            );
        }

        // Validate payload presence
        DocumentRegistrationPayload payload = message.getPayload();
        if (payload == null) {
            throw new InvalidMessageException(
                    "Payload is required for document registration message",
                    message.getMessageId(),
                    "payload"
            );
        }

        // Validate payload fields
        validateDocumentRegistrationPayload(payload, message.getMessageId());

        LOGGER.log(Level.FINE, "Document registration message validation passed: {0}", message.getMessageId());
    }

    /**
     * Validate document registration payload fields.
     *
     * @param payload   Payload to validate
     * @param messageId Message ID for error reporting
     * @throws InvalidMessageException if validation fails
     */
    private void validateDocumentRegistrationPayload(DocumentRegistrationPayload payload, String messageId)
            throws InvalidMessageException {

        // Validate patient CI
        if (payload.getPatientCI() == null || payload.getPatientCI().trim().isEmpty()) {
            throw new InvalidMessageException(
                    "Patient CI is required",
                    messageId,
                    "payload.patientCI"
            );
        }

        if (!isValidCi(payload.getPatientCI())) {
            throw new InvalidMessageException(
                    "Invalid patient CI format: " + payload.getPatientCI(),
                    messageId,
                    "payload.patientCI"
            );
        }

        // Validate document type
        if (payload.getDocumentType() == null) {
            throw new InvalidMessageException(
                    "Document type is required",
                    messageId,
                    "payload.documentType"
            );
        }

        // Validate document locator
        if (payload.getDocumentLocator() == null || payload.getDocumentLocator().trim().isEmpty()) {
            throw new InvalidMessageException(
                    "Document locator is required",
                    messageId,
                    "payload.documentLocator"
            );
        }

        if (!isValidUrl(payload.getDocumentLocator())) {
            throw new InvalidMessageException(
                    "Invalid document locator URL: " + payload.getDocumentLocator(),
                    messageId,
                    "payload.documentLocator"
            );
        }

//        // Validate document hash
//        if (payload.getDocumentHash() == null || payload.getDocumentHash().trim().isEmpty()) {
//            throw new InvalidMessageException(
//                    "Document hash is required",
//                    messageId,
//                    "payload.documentHash"
//            );
//        }

        if (payload.getDocumentHash() != null && !isValidDocumentHash(payload.getDocumentHash())) {
            throw new InvalidMessageException(
                    "Invalid document hash format: " + payload.getDocumentHash() +
                            ". Expected format: sha256:[64 hex characters]",
                    messageId,
                    "payload.documentHash"
            );
        }

        // Validate created by
        if (payload.getCreatedBy() == null || payload.getCreatedBy().trim().isEmpty()) {
            throw new InvalidMessageException(
                    "CreatedBy is required",
                    messageId,
                    "payload.createdBy"
            );
        }

        // Validate created at
        if (payload.getCreatedAt() == null) {
            throw new InvalidMessageException(
                    "CreatedAt timestamp is required",
                    messageId,
                    "payload.createdAt"
            );
        }

        if (payload.getCreatedAt().isAfter(LocalDateTime.now())) {
            throw new InvalidMessageException(
                    "CreatedAt timestamp cannot be in the future: " + payload.getCreatedAt(),
                    messageId,
                    "payload.createdAt"
            );
        }

        // Validate clinic ID
        if (payload.getClinicId() == null || payload.getClinicId().trim().isEmpty()) {
            throw new InvalidMessageException(
                    "Clinic ID is required",
                    messageId,
                    "payload.clinicId"
            );
        }
    }

    // ================================================================
    // Base Message Validation
    // ================================================================

    /**
     * Validate base message fields common to all message types.
     *
     * @param message Message to validate
     * @throws InvalidMessageException if validation fails
     */
    private void validateBaseMessage(BaseMessage message) throws InvalidMessageException {

        // Validate message ID
        if (message.getMessageId() == null || message.getMessageId().trim().isEmpty()) {
            throw new InvalidMessageException(
                    "Message ID is required",
                    null,
                    "messageId"
            );
        }

        // Validate timestamp
        if (message.getTimestamp() == null) {
            throw new InvalidMessageException(
                    "Timestamp is required",
                    message.getMessageId(),
                    "timestamp"
            );
        }

        // Check message staleness
        LocalDateTime maxAge = LocalDateTime.now().minusDays(MAX_MESSAGE_AGE_DAYS);
        if (message.getTimestamp().isBefore(maxAge)) {
            throw new InvalidMessageException(
                    "Message is too old (> " + MAX_MESSAGE_AGE_DAYS + " days): " + message.getTimestamp(),
                    message.getMessageId(),
                    "timestamp"
            );
        }

        // Validate source system
        if (message.getSourceSystem() == null || message.getSourceSystem().trim().isEmpty()) {
            throw new InvalidMessageException(
                    "Source system is required",
                    message.getMessageId(),
                    "sourceSystem"
            );
        }

        // Validate event type
        if (message.getEventType() == null || message.getEventType().trim().isEmpty()) {
            throw new InvalidMessageException(
                    "Event type is required",
                    message.getMessageId(),
                    "eventType"
            );
        }
    }

    // ================================================================
    // Format Validation Helpers
    // ================================================================

    /**
     * Validate Uruguayan CI format.
     *
     * @param ci CI to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidCi(String ci) {
        if (ci == null || ci.trim().isEmpty()) {
            return false;
        }
        return CI_PATTERN.matcher(ci.trim()).matches();
    }

    /**
     * Validate URL format.
     *
     * @param url URL to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Validate document hash format.
     * Expected format: sha256:[64 lowercase hex characters]
     *
     * @param hash Hash to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidDocumentHash(String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            return false;
        }

        return HASH_PATTERN.matcher("sha256:" + hash).matches();
    }

    /**
     * Validate email format (basic check).
     *
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Simple email validation (not RFC 5322 compliant, but good enough)
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
