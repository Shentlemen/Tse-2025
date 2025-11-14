package com.prestador.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database Migration Listener
 *
 * Runs Flyway database migrations on application startup.
 * This ensures the database schema is up-to-date before the application starts.
 *
 * Configuration:
 * - Database URL: jdbc:postgresql://localhost:5432/hcen
 * - Schema: health_provider
 * - Migrations location: classpath:db/migration
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@WebListener
public class DatabaseMigrationListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(DatabaseMigrationListener.class.getName());

    // Database configuration
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/health_provider";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";
    private static final String DB_SCHEMA = "health_provider";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("==============================================");
        LOGGER.info("Starting Flyway database migrations...");
        LOGGER.info("==============================================");

        try {
            // Create Flyway instance
            Flyway flyway = Flyway.configure()
                    .dataSource(DB_URL, DB_USER, DB_PASSWORD)
                    .schemas(DB_SCHEMA)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)  // Allow migration on existing database
                    .baselineVersion("0")      // Start versioning from 0
                    .validateOnMigrate(true)   // Validate migrations before applying
                    .cleanDisabled(true)       // Disable clean for safety
                    .load();

            // Log current database state
            LOGGER.log(Level.INFO, "Database URL: {0}", DB_URL);
            LOGGER.log(Level.INFO, "Target schema: {0}", DB_SCHEMA);

            // Run migrations
            int migrationsApplied = flyway.migrate().migrationsExecuted;

            if (migrationsApplied > 0) {
                LOGGER.log(Level.INFO, "==============================================");
                LOGGER.log(Level.INFO, "Successfully applied {0} migration(s)", migrationsApplied);
                LOGGER.log(Level.INFO, "==============================================");
            } else {
                LOGGER.info("==============================================");
                LOGGER.info("Database schema is up-to-date. No migrations applied.");
                LOGGER.info("==============================================");
            }

            // Display migration info
            flyway.info().all();

        } catch (FlywayException e) {
            LOGGER.log(Level.SEVERE, "==============================================", e);
            LOGGER.log(Level.SEVERE, "Flyway migration FAILED!", e);
            LOGGER.log(Level.SEVERE, "==============================================", e);
            LOGGER.log(Level.SEVERE, "Error: " + e.getMessage(), e);

            // Optionally fail application startup on migration failure
            throw new RuntimeException("Database migration failed. Application cannot start.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Application shutting down...");
    }
}
