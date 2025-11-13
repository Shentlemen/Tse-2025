package uy.gub.hcen.messaging.processor;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import uy.gub.hcen.messaging.dto.DocumentRegistrationMessage;
import uy.gub.hcen.messaging.dto.DocumentRegistrationPayload;
import uy.gub.hcen.messaging.exception.InvalidMessageException;
import uy.gub.hcen.messaging.exception.MessageProcessingException;
import uy.gub.hcen.messaging.validator.MessageValidator;
import uy.gub.hcen.rndc.entity.RndcDocument;
import uy.gub.hcen.service.rndc.RndcService;
import uy.gub.hcen.service.rndc.exception.DocumentRegistrationException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Message processor for clinical document registration events.
 * <p>
 * This service is the business logic layer between the MDB listener and the
 * domain service (RndcService). It handles:
 * - Message validation
 * - Data transformation from DTO to domain model
 * - Business logic orchestration
 * - Error handling and classification (transient vs permanent)
 * - Logging and audit trail
 * <p>
 * Processing Flow:
 * 1. DocumentRegistrationListener (MDB) receives JMS message
 * 2. MDB deserializes message to DocumentRegistrationMessage
 * 3. MDB calls this processor's process() method
 * 4. Processor validates message (MessageValidator)
 * 5. Processor calls RndcService.registerDocument()
 * 6. Processor returns success/failure to MDB
 * 7. MDB acknowledges or redelivers based on result
 * <p>
 * Idempotency:
 * Document registration is idempotent at the RndcService layer. Duplicate
 * documentLocator registrations return the existing document without error.
 * <p>
 * Transaction Management:
 * This service participates in the MDB's XA transaction. If processing fails,
 * the transaction is rolled back and the message is redelivered (for transient errors)
 * or moved to DLQ (for permanent errors).
 * <p>
 * Important: This processor registers METADATA only. The actual document
 * remains in peripheral storage at the location specified by documentLocator.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@Stateless
public class DocumentRegistrationProcessor {

    private static final Logger LOGGER = Logger.getLogger(DocumentRegistrationProcessor.class.getName());

    @Inject
    private RndcService rndcService;

    @Inject
    private MessageValidator messageValidator;

    /**
     * Process document registration message.
     * <p>
     * This method validates the message, extracts the payload, and registers
     * the document metadata in the RNDC system.
     * <p>
     * Error Handling:
     * - InvalidMessageException: Permanent error, message moved to DLQ
     * - DocumentRegistrationException: Permanent error (validation), message moved to DLQ
     * - RuntimeException: Transient error (database, network), message redelivered
     *
     * @param message Document registration message
     * @return Registered RndcDocument
     * @throws MessageProcessingException if processing fails
     */
    public RndcDocument process(DocumentRegistrationMessage message) throws MessageProcessingException {

        if (message == null) {
            throw new InvalidMessageException("Message cannot be null");
        }

        String messageId = message.getMessageId();

        LOGGER.log(Level.INFO, "Processing document registration message - ID: {0}, Source: {1}",
                new Object[]{messageId, message.getSourceSystem()});

        try {
            // Step 1: Validate message structure and content
            LOGGER.log(Level.FINE, "Validating message: {0}", messageId);
            messageValidator.validateDocumentRegistrationMessage(message);

            // Step 2: Extract payload
            DocumentRegistrationPayload payload = message.getPayload();

            LOGGER.log(Level.INFO, "Registering document - Patient CI: {0}, Type: {1}, Clinic: {2}, Locator: {3}",
                    new Object[]{
                            payload.getPatientCI(),
                            payload.getDocumentType(),
                            payload.getClinicId(),
                            payload.getDocumentLocator()
                    });

            // Step 3: Register document via RndcService
            // Note: RndcService.registerDocument() is idempotent - duplicate locator returns existing document
            RndcDocument registeredDocument = rndcService.registerDocument(
                    payload.getPatientCI(),
                    payload.getDocumentType(),
                    payload.getDocumentLocator(),
                    payload.getDocumentHash(),
                    payload.getCreatedBy(),
                    payload.getClinicId(),
                    payload.getDocumentTitle(),
                    payload.getDocumentDescription()
            );

            LOGGER.log(Level.INFO,
                    "Document registration successful - Message ID: {0}, Document ID: {1}, Patient CI: {2}, Type: {3}",
                    new Object[]{
                            messageId,
                            registeredDocument.getId(),
                            registeredDocument.getPatientCi(),
                            registeredDocument.getDocumentType()
                    });

            return registeredDocument;

        } catch (InvalidMessageException e) {
            // Permanent error: message validation failed
            LOGGER.log(Level.WARNING,
                    "Document registration failed due to invalid message - Message ID: " + messageId +
                            ", Field: " + e.getInvalidField() + ", Error: " + e.getMessage());

            // Rethrow as-is (not transient, will go to DLQ)
            throw e;

        } catch (DocumentRegistrationException e) {
            // Permanent error: business rule violation or validation error
            LOGGER.log(Level.WARNING,
                    "Document registration failed due to business rule violation - Message ID: " + messageId +
                            ", Error: " + e.getMessage());

            // Wrap as permanent error (will go to DLQ)
            throw new MessageProcessingException(
                    "Document registration failed: " + e.getMessage(),
                    e,
                    messageId,
                    false // Not transient
            );

        } catch (Exception e) {
            // Transient error: database failure, network issue, etc.
            LOGGER.log(Level.SEVERE,
                    "Document registration failed due to system error - Message ID: " + messageId +
                            ", Error: " + e.getMessage(),
                    e);

            // Wrap as transient error (will be redelivered)
            throw new MessageProcessingException(
                    "System error during document registration: " + e.getMessage(),
                    e,
                    messageId,
                    true // Transient - retry
            );
        }
    }

    /**
     * Process message with idempotency check.
     * <p>
     * This overload first checks if the document has already been registered
     * (by looking for existing document with same locator). If found, returns
     * existing document without reprocessing.
     * <p>
     * Note: This is optional. RndcService.registerDocument() already provides
     * idempotency, so this method provides an early exit to avoid unnecessary
     * service calls.
     *
     * @param message Document registration message
     * @return Registered or existing RndcDocument
     * @throws MessageProcessingException if processing fails
     */
    public RndcDocument processWithIdempotencyCheck(DocumentRegistrationMessage message)
            throws MessageProcessingException {

        if (message == null) {
            throw new InvalidMessageException("Message cannot be null");
        }

        String messageId = message.getMessageId();
        DocumentRegistrationPayload payload = message.getPayload();

        if (payload == null) {
            throw new InvalidMessageException(
                    "Payload is required",
                    messageId,
                    "payload"
            );
        }

        String documentLocator = payload.getDocumentLocator();

        LOGGER.log(Level.FINE, "Checking for existing document with locator: {0}", documentLocator);

        try {
            // Check if document already exists
            var existingDocument = rndcService.getDocumentByLocator(documentLocator);

            if (existingDocument.isPresent()) {
                LOGGER.log(Level.INFO,
                        "Document already registered - Message ID: {0}, Document ID: {1}, Locator: {2}. Skipping reprocessing.",
                        new Object[]{messageId, existingDocument.get().getId(), documentLocator});

                return existingDocument.get();
            }

            // Document doesn't exist, proceed with normal processing
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
