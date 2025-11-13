package uy.gub.hcen.messaging.processor;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import uy.gub.hcen.inus.entity.InusUser;
import uy.gub.hcen.messaging.dto.UserRegistrationMessage;
import uy.gub.hcen.messaging.dto.UserRegistrationPayload;
import uy.gub.hcen.messaging.exception.InvalidMessageException;
import uy.gub.hcen.messaging.exception.MessageProcessingException;
import uy.gub.hcen.messaging.validator.MessageValidator;
import uy.gub.hcen.service.inus.InusService;
import uy.gub.hcen.service.inus.exception.UserRegistrationException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Message processor for user registration events.
 * <p>
 * This service is the business logic layer between the MDB listener and the
 * domain service (InusService). It handles:
 * - Message validation
 * - Data transformation from DTO to domain model
 * - Business logic orchestration
 * - Error handling and classification (transient vs permanent)
 * - Logging and audit trail
 * <p>
 * Processing Flow:
 * 1. UserRegistrationListener (MDB) receives JMS message
 * 2. MDB deserializes message to UserRegistrationMessage
 * 3. MDB calls this processor's process() method
 * 4. Processor validates message (MessageValidator)
 * 5. Processor calls InusService.registerUser()
 * 6. Processor returns success/failure to MDB
 * 7. MDB acknowledges or redelivers based on result
 * <p>
 * Idempotency:
 * User registration is idempotent at the InusService layer. Duplicate CI
 * registrations return the existing user without error.
 * <p>
 * Transaction Management:
 * This service participates in the MDB's XA transaction. If processing fails,
 * the transaction is rolled back and the message is redelivered (for transient errors)
 * or moved to DLQ (for permanent errors).
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@Stateless
public class UserRegistrationProcessor {

    private static final Logger LOGGER = Logger.getLogger(UserRegistrationProcessor.class.getName());

    @Inject
    private InusService inusService;

    @Inject
    private MessageValidator messageValidator;

    /**
     * Process user registration message.
     * <p>
     * This method validates the message, extracts the payload, and registers
     * the user in the INUS system.
     * <p>
     * Error Handling:
     * - InvalidMessageException: Permanent error, message moved to DLQ
     * - UserRegistrationException: Permanent error (validation), message moved to DLQ
     * - RuntimeException: Transient error (database, network), message redelivered
     *
     * @param message User registration message
     * @return Registered InusUser
     * @throws MessageProcessingException if processing fails
     */
    public InusUser process(UserRegistrationMessage message) throws MessageProcessingException {

        if (message == null) {
            throw new InvalidMessageException("Message cannot be null");
        }

        String messageId = message.getMessageId();

        LOGGER.log(Level.INFO, "Processing user registration message - ID: {0}, Source: {1}",
                new Object[]{messageId, message.getSourceSystem()});

        try {
            // Step 1: Validate message structure and content
            LOGGER.log(Level.FINE, "Validating message: {0}", messageId);
            messageValidator.validateUserRegistrationMessage(message);

            // Step 2: Extract payload
            UserRegistrationPayload payload = message.getPayload();

            LOGGER.log(Level.INFO, "Registering user - CI: {0}, Name: {1} {2}, Clinic: {3}",
                    new Object[]{
                            payload.getCi(),
                            payload.getFirstName(),
                            payload.getLastName(),
                            payload.getClinicId()
                    });

            // Step 3: Register user via InusService
            // Note: InusService.registerUser() is idempotent - duplicate CI returns existing user
            InusUser registeredUser = inusService.registerUser(
                    payload.getCi(),
                    payload.getFirstName(),
                    payload.getLastName(),
                    payload.getDateOfBirth(),
                    payload.getEmail(),
                    payload.getPhoneNumber(),
                    payload.getClinicId()
            );

            LOGGER.log(Level.INFO,
                    "User registration successful - Message ID: {0}, CI: {1}, INUS ID: {2}",
                    new Object[]{messageId, registeredUser.getCi(), registeredUser.getInusId()});

            return registeredUser;

        } catch (InvalidMessageException e) {
            // Permanent error: message validation failed
            LOGGER.log(Level.WARNING,
                    "User registration failed due to invalid message - Message ID: " + messageId +
                            ", Field: " + e.getInvalidField() + ", Error: " + e.getMessage());

            // Rethrow as-is (not transient, will go to DLQ)
            throw e;

        } catch (UserRegistrationException e) {
            // Permanent error: business rule violation or validation error
            LOGGER.log(Level.WARNING,
                    "User registration failed due to business rule violation - Message ID: " + messageId +
                            ", Error: " + e.getMessage());

            // Wrap as permanent error (will go to DLQ)
            throw new MessageProcessingException(
                    "User registration failed: " + e.getMessage(),
                    e,
                    messageId,
                    false // Not transient
            );

        } catch (Exception e) {
            // Transient error: database failure, network issue, etc.
            LOGGER.log(Level.SEVERE,
                    "User registration failed due to system error - Message ID: " + messageId +
                            ", Error: " + e.getMessage(),
                    e);

            // Wrap as transient error (will be redelivered)
            throw new MessageProcessingException(
                    "System error during user registration: " + e.getMessage(),
                    e,
                    messageId,
                    true // Transient - retry
            );
        }
    }

    /**
     * Process message with idempotency check.
     * <p>
     * This overload first checks if the message has already been processed
     * (by looking for existing user with same CI). If found, returns existing
     * user without reprocessing.
     * <p>
     * Note: This is optional. InusService.registerUser() already provides
     * idempotency, so this method provides an early exit to avoid unnecessary
     * service calls.
     *
     * @param message User registration message
     * @return Registered or existing InusUser
     * @throws MessageProcessingException if processing fails
     */
    public InusUser processWithIdempotencyCheck(UserRegistrationMessage message)
            throws MessageProcessingException {

        if (message == null) {
            throw new InvalidMessageException("Message cannot be null");
        }

        String messageId = message.getMessageId();
        UserRegistrationPayload payload = message.getPayload();

        if (payload == null) {
            throw new InvalidMessageException(
                    "Payload is required",
                    messageId,
                    "payload"
            );
        }

        String ci = payload.getCi();

        LOGGER.log(Level.FINE, "Checking for existing user with CI: {0}", ci);

        try {
            // Check if user already exists
            var existingUser = inusService.findUserByCi(ci);

            if (existingUser.isPresent()) {
                LOGGER.log(Level.INFO,
                        "User already registered - Message ID: {0}, CI: {1}, INUS ID: {2}. Skipping reprocessing.",
                        new Object[]{messageId, ci, existingUser.get().getInusId()});

                return existingUser.get();
            }

            // User doesn't exist, proceed with normal processing
            return process(message);

        } catch (Exception e) {
            // If idempotency check fails, fall back to normal processing
            LOGGER.log(Level.WARNING,
                    "Idempotency check failed for message: " + messageId + ", proceeding with normal processing",
                    e);

            return process(message);
        }
    }
}
