package uy.gub.hcen.messaging.config;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JMS Queue Initializer - Startup Singleton EJB
 *
 * Creates JMS queues programmatically on application startup to avoid
 * manual WildFly configuration and orphaned JNDI binding issues.
 *
 * Queues Created:
 * - UserRegistrationQueue: java:/jms/queue/UserRegistration
 * - DocumentRegistrationQueue: java:/jms/queue/DocumentRegistration
 *
 * This bean executes once during deployment, before any MDBs activate.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@Singleton
@Startup
public class JmsQueueInitializer {

    private static final Logger LOGGER = Logger.getLogger(JmsQueueInitializer.class.getName());

    /**
     * Queue JNDI names that MDBs expect
     */
    private static final String USER_REGISTRATION_QUEUE = "java:/jms/queue/UserRegistration";
    private static final String DOCUMENT_REGISTRATION_QUEUE = "java:/jms/queue/DocumentRegistration";

    /**
     * Default connection factory provided by WildFly
     */
    private static final String CONNECTION_FACTORY_JNDI = "java:/ConnectionFactory";

    /**
     * Initialize JMS queues on startup.
     *
     * This method is called automatically by the container after the bean is constructed
     * and dependency injection is complete, but before any MDBs activate.
     */
    @PostConstruct
    public void initializeQueues() {
        LOGGER.log(Level.INFO, "=== JMS Queue Initializer: Starting queue initialization ===");

        InitialContext ctx = null; // Declare ctx outside the try block
        try {
            ctx = new InitialContext();

            // Step 1: Lookup connection factory
            ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup(CONNECTION_FACTORY_JNDI);

            // Step 2: Create JMS context (auto-closeable)
            try (JMSContext jmsContext = connectionFactory.createContext()) {
                // Step 3: Create/verify queues
                createQueue(jmsContext, ctx, USER_REGISTRATION_QUEUE, "UserRegistration");
                createQueue(jmsContext, ctx, DOCUMENT_REGISTRATION_QUEUE, "DocumentRegistration");

                LOGGER.log(Level.INFO, "=== JMS Queue Initializer: All queues initialized successfully ===");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "JMS Queue Initializer: Failed to initialize queues. MDBs may fail to activate.", e);
            // ... (error logging)
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    LOGGER.log(Level.WARNING, "Failed to close InitialContext.", e);
                }
            }
        }
    }

    /**
     * Create or verify a JMS queue exists.
     *
     * @param jmsContext JMS context for queue creation
     * @param namingContext JNDI context for lookups
     * @param queueJndiName JNDI name of the queue
     * @param queueLogicalName Logical name for logging
     */
    private void createQueue(JMSContext jmsContext, InitialContext namingContext,
                           String queueJndiName, String queueLogicalName) {

        try {
            // Try to lookup existing queue
            Queue existingQueue = (Queue) namingContext.lookup(queueJndiName);

            LOGGER.log(Level.INFO,
                "Queue already exists: {0} -> {1}",
                new Object[]{queueLogicalName, queueJndiName});

        } catch (NamingException e) {
            // Queue doesn't exist - create it
            LOGGER.log(Level.INFO,
                "Queue not found in JNDI, creating: {0} -> {1}",
                new Object[]{queueLogicalName, queueJndiName});

            try {
                // Create queue using physical name (without java:/ prefix for Artemis)
                String physicalName = queueJndiName.replace("java:/jms/queue/", "");
                Queue newQueue = jmsContext.createQueue(physicalName);

                LOGGER.log(Level.INFO,
                    "Queue created successfully: {0} (physical name: {1})",
                    new Object[]{queueLogicalName, physicalName});

            } catch (Exception createEx) {
                LOGGER.log(Level.WARNING,
                    "Failed to create queue: {0}. Error: {1}",
                    new Object[]{queueLogicalName, createEx.getMessage()});
            }
        }
    }
}
