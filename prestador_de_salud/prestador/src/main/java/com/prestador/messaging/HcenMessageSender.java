package com.prestador.messaging;

import org.json.JSONObject;

import jakarta.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HCEN Message Sender
 *
 * Utility class for sending messages to HCEN Central JMS queues.
 * Sends patient registration and document metadata events.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class HcenMessageSender {

    private static final Logger LOGGER = Logger.getLogger(HcenMessageSender.class.getName());

    private static final String HCEN_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
    private static final String USER_REGISTRATION_QUEUE = "jms/queue/UserRegistration";
    private static final String DOCUMENT_REGISTRATION_QUEUE = "jms/queue/DocumentRegistration";

    private static final String PRESTADOR_SOURCE_SYSTEM = "prestador-de-salud";

    /**
     * Send patient registration message to HCEN (FHIR format)
     *
     * @param ci Patient document number (CI)
     * @param firstName Patient first name
     * @param lastName Patient last name
     * @param birthDate Date of birth
     * @param gender Gender
     * @param email Email address
     * @param phoneNumber Phone number
     * @param address Address
     * @param clinicId Clinic identifier
     */
    public static void sendPatientRegistration(
            String ci,
            String firstName,
            String lastName,
            java.time.LocalDate birthDate,
            String gender,
            String email,
            String phoneNumber,
            String address,
            Long clinicId) {

        String messageId = "msg-" + UUID.randomUUID().toString();

        try {
            // Build FHIR Patient resource
            String fhirPatient = FhirMessageBuilder.buildPatientResource(
                    ci, firstName, lastName, birthDate, gender,
                    email, phoneNumber, address, clinicId
            );

            // Wrap in message envelope
            String message = FhirMessageBuilder.wrapInMessageEnvelope(
                    fhirPatient,
                    "patient-create",
                    PRESTADOR_SOURCE_SYSTEM
            );

            // Send message
            sendToQueue(USER_REGISTRATION_QUEUE, message);

            LOGGER.log(Level.INFO, "Sent FHIR patient registration to HCEN - Message ID: {0}, CI: {1}",
                    new Object[]{messageId, ci});

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send patient registration to HCEN - CI: " + ci, e);
            // Don't throw - patient was already saved locally, this is just notification
        }
    }

    /**
     * Send clinical document metadata to HCEN RNDC (FHIR format)
     *
     * @param patientCI Patient document number (CI)
     * @param documentId Local document ID
     * @param documentType Type of clinical document
     * @param documentTitle Document title
     * @param documentDescription Document description
     * @param createdBy Professional who created the document
     * @param createdAt Creation timestamp
     * @param clinicId Clinic identifier
     * @param specialtyId Medical specialty ID (optional)
     * @param documentLocatorUrl URL to retrieve the document from this prestador
     */
    public static void sendDocumentMetadata(
            String patientCI,
            Long documentId,
            String documentType,
            String documentTitle,
            String documentDescription,
            String createdBy,
            LocalDateTime createdAt,
            Long clinicId,
            Long specialtyId,
            String documentLocatorUrl) {

        String messageId = "msg-" + UUID.randomUUID().toString();

        try {
            // Generate document hash (simplified - in production use actual file hash)
            String documentHash = "sha256:" + generateSimpleHash(documentId.toString());

            // Build FHIR DocumentReference resource
            String fhirDocRef = FhirMessageBuilder.buildDocumentReferenceResource(
                    patientCI,
                    documentId,
                    documentType,
                    documentTitle,
                    documentDescription,
                    documentLocatorUrl,
                    documentHash,
                    createdBy != null ? createdBy : "professional@prestador.uy",
                    createdAt,
                    clinicId,
                    specialtyId
            );

            // Wrap in message envelope
            String message = FhirMessageBuilder.wrapInMessageEnvelope(
                    fhirDocRef,
                    "document-create",
                    PRESTADOR_SOURCE_SYSTEM
            );

            // Send message
            sendToQueue(DOCUMENT_REGISTRATION_QUEUE, message);

            LOGGER.log(Level.INFO,
                    "Sent FHIR document metadata to HCEN - Message ID: {0}, Patient CI: {1}, Document ID: {2}",
                    new Object[]{messageId, patientCI, documentId});

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to send document metadata to HCEN - Patient CI: " + patientCI +
                    ", Document ID: " + documentId, e);
            // Don't throw - document was already saved locally, this is just notification
        }
    }

    /**
     * Send message to JMS queue
     *
     * @param queueJndiName JNDI name of the queue
     * @param messageText JSON message text
     */
    private static void sendToQueue(String queueJndiName, String messageText) throws Exception {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        try {
            // Get initial context
            Context context = new InitialContext();

            // Lookup connection factory and queue
            ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(HCEN_CONNECTION_FACTORY);
            Queue queue = (Queue) context.lookup(queueJndiName);

            // Create connection
            connection = connectionFactory.createConnection();
            connection.start();

            // Create session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create producer
            producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            // Create and send text message
            TextMessage message = session.createTextMessage(messageText);
            producer.send(message);

            LOGGER.log(Level.FINE, "Message sent to queue: {0}", queueJndiName);

        } finally {
            // Close resources
            if (producer != null) {
                try {
                    producer.close();
                } catch (JMSException e) {
                    LOGGER.log(Level.WARNING, "Error closing producer", e);
                }
            }
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    LOGGER.log(Level.WARNING, "Error closing session", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection", e);
                }
            }
        }
    }

    /**
     * Generate a simple hash for demonstration
     * In production, use actual SHA-256 hash of document content
     *
     * @param input Input string
     * @return Hex hash string
     */
    private static String generateSimpleHash(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error generating hash", e);
            return "00000000000000000000000000000000";
        }
    }

    /**
     * Check if JMS connection is available
     *
     * @return true if connection is available, false otherwise
     */
    public static boolean isHcenConnectionAvailable() {
        try {
            Context context = new InitialContext();
            context.lookup(HCEN_CONNECTION_FACTORY);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "HCEN JMS connection not available", e);
            return false;
        }
    }
}
