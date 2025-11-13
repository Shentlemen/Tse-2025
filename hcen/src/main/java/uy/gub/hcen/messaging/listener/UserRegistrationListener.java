package uy.gub.hcen.messaging.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import uy.gub.hcen.inus.entity.InusUser;
import uy.gub.hcen.messaging.dto.UserRegistrationMessage;
import uy.gub.hcen.messaging.exception.InvalidMessageException;
import uy.gub.hcen.messaging.exception.MessageProcessingException;
import uy.gub.hcen.messaging.processor.UserRegistrationProcessor;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Message-Driven Bean (MDB) for processing user registration messages.
 * <p>
 * This MDB listens to the "hcen.users.registration" queue and processes
 * user registration events from peripheral nodes (clinics, health providers).
 * <p>
 * Queue Configuration:
 * - Queue Name: hcen.users.registration
 * - JNDI Name: java:/jms/queue/UserRegistration
 * - Connection Factory: java:/JmsXA
 * <p>
 * Processing Flow:
 * 1. ActiveMQ Artemis delivers message to this MDB
 * 2. Container invokes onMessage() in XA transaction
 * 3. Message deserialized from JSON to UserRegistrationMessage
 * 4. UserRegistrationProcessor validates and processes message
 * 5. InusService registers user in database
 * 6. Transaction committed, message acknowledged
 * 7. If error: transaction rolled back, message redelivered or moved to DLQ
 * <p>
 * Error Handling:
 * - InvalidMessageException: Permanent error → Move to DLQ after 1 attempt
 * - MessageProcessingException (transient=false): Permanent error → Move to DLQ
 * - MessageProcessingException (transient=true): Transient error → Redeliver up to 5 times
 * - RuntimeException: Transient error → Redeliver up to 5 times
 * <p>
 * Idempotency:
 * User registration is idempotent. Duplicate messages with same CI return
 * existing user without error. Message IDs can be logged for correlation.
 * <p>
 * XA Transaction:
 * This MDB uses Container-Managed Transactions (CMT) with XA support.
 * The transaction spans:
 * - JMS message consumption (from queue)
 * - Database operations (INUS insert/update)
 * - Audit logging (to be added)
 * <p>
 * If any operation fails, the entire transaction is rolled back atomically.
 * <p>
 * Deployment:
 * Requires WildFly JMS queue configuration in standalone.xml:
 * <pre>
 * &lt;jms-queue name="UserRegistrationQueue" entries="java:/jms/queue/UserRegistration"/&gt;
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@MessageDriven(
        name = "UserRegistrationListener",
        activationConfig = {
                @ActivationConfigProperty(
                        propertyName = "destinationType",
                        propertyValue = "jakarta.jms.Queue"
                ),
                @ActivationConfigProperty(
                        propertyName = "destination",
                        propertyValue = "java:/jms/queue/UserRegistration"
                ),
                @ActivationConfigProperty(
                        propertyName = "acknowledgeMode",
                        propertyValue = "Auto-acknowledge"
                ),
                @ActivationConfigProperty(
                        propertyName = "maxSession",
                        propertyValue = "10" // Concurrent consumers
                ),
                @ActivationConfigProperty(
                        propertyName = "redeliveryDelay",
                        propertyValue = "5000" // 5 seconds between retries
                ),
                @ActivationConfigProperty(
                        propertyName = "maxDeliveryAttempts",
                        propertyValue = "5" // Max retry attempts before DLQ
                )
        }
)
public class UserRegistrationListener implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(UserRegistrationListener.class.getName());

    /**
     * Jackson ObjectMapper for JSON deserialization.
     * Configured with JavaTimeModule for LocalDate/LocalDateTime support.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Inject
    private UserRegistrationProcessor processor;

    /**
     * JMS message callback.
     * <p>
     * Invoked by the container when a message arrives on the queue.
     * Runs within a container-managed XA transaction.
     *
     * @param message JMS message (expected to be TextMessage with JSON payload)
     */
    @Override
    public void onMessage(Message message) {
        String messageId = null;

        try {
            // Step 1: Extract message ID for correlation
            messageId = message.getJMSMessageID();

            LOGGER.log(Level.INFO, "Received user registration message - JMS ID: {0}", messageId);

            // Step 2: Validate message type (must be TextMessage)
            if (!(message instanceof TextMessage)) {
                LOGGER.log(Level.SEVERE,
                        "Unsupported message type: {0}. Expected TextMessage. Moving to DLQ.",
                        message.getClass().getName());

                // Throw permanent error to move to DLQ
                throw new InvalidMessageException(
                        "Unsupported message type: " + message.getClass().getName() +
                                ". Expected TextMessage with JSON payload."
                );
            }

            // Step 3: Extract JSON payload
            TextMessage textMessage = (TextMessage) message;
            String jsonPayload = textMessage.getText();

            if (jsonPayload == null || jsonPayload.trim().isEmpty()) {
                LOGGER.log(Level.SEVERE, "Empty message payload received. Moving to DLQ.");
                throw new InvalidMessageException("Message payload is empty");
            }

            LOGGER.log(Level.FINE, "Message payload: {0}", jsonPayload);

            // Step 4: Deserialize JSON to UserRegistrationMessage
            UserRegistrationMessage userMessage = deserializeMessage(jsonPayload);

            LOGGER.log(Level.INFO, "Deserialized message - ID: {0}, Source: {1}, Event: {2}",
                    new Object[]{
                            userMessage.getMessageId(),
                            userMessage.getSourceSystem(),
                            userMessage.getEventType()
                    });

            // Step 5: Process message via processor
            InusUser registeredUser = processor.process(userMessage);

            // Step 6: Log success
            LOGGER.log(Level.INFO,
                    "User registration completed successfully - Message ID: {0}, CI: {1}, INUS ID: {2}",
                    new Object[]{
                            userMessage.getMessageId(),
                            registeredUser.getCi(),
                            registeredUser.getInusId()
                    });

            // Transaction will be committed automatically by container
            // Message will be acknowledged and removed from queue

        } catch (InvalidMessageException e) {
            // Permanent error: validation failed
            LOGGER.log(Level.SEVERE,
                    "Message validation failed - JMS ID: " + messageId + ", Field: " + e.getInvalidField() +
                            ", Error: " + e.getMessage() + ". Moving to DLQ.");

            // Rethrow as RuntimeException to trigger rollback and DLQ move
            // Container will move message to DLQ after max redelivery attempts
            throw new RuntimeException("Invalid message: " + e.getMessage(), e);

        } catch (MessageProcessingException e) {
            // Check if error is transient
            if (e.isTransient()) {
                // Transient error: retry
                LOGGER.log(Level.WARNING,
                        "Transient error processing message - JMS ID: " + messageId +
                                ", Message ID: " + e.getMessageId() +
                                ", Error: " + e.getMessage() + ". Message will be redelivered.",
                        e);

                // Rethrow to trigger rollback and redelivery
                throw new RuntimeException("Transient error: " + e.getMessage(), e);

            } else {
                // Permanent error: move to DLQ
                LOGGER.log(Level.SEVERE,
                        "Permanent error processing message - JMS ID: " + messageId +
                                ", Message ID: " + e.getMessageId() +
                                ", Error: " + e.getMessage() + ". Moving to DLQ.",
                        e);

                // Rethrow as RuntimeException to trigger DLQ move
                throw new RuntimeException("Permanent processing error: " + e.getMessage(), e);
            }

        } catch (JMSException e) {
            // JMS-specific error (likely transient)
            LOGGER.log(Level.SEVERE,
                    "JMS error processing message - JMS ID: " + messageId +
                            ", Error: " + e.getMessage() + ". Message will be redelivered.",
                    e);

            // Rethrow to trigger rollback and redelivery
            throw new RuntimeException("JMS error: " + e.getMessage(), e);

        } catch (Exception e) {
            // Unexpected error (treat as transient)
            LOGGER.log(Level.SEVERE,
                    "Unexpected error processing message - JMS ID: " + messageId +
                            ", Error: " + e.getMessage() + ". Message will be redelivered.",
                    e);

            // Rethrow to trigger rollback and redelivery
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Deserialize JSON message to UserRegistrationMessage.
     *
     * @param json JSON string
     * @return Deserialized message
     * @throws InvalidMessageException if deserialization fails
     */
    private UserRegistrationMessage deserializeMessage(String json) throws InvalidMessageException {
        try {
            return OBJECT_MAPPER.readValue(json, UserRegistrationMessage.class);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to deserialize message JSON: " + json, e);
            throw new InvalidMessageException(
                    "Failed to deserialize message: " + e.getMessage()
            );
        }
    }
}
