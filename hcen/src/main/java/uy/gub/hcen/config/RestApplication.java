package uy.gub.hcen.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application configuration
 * All REST endpoints will be available under /api/*
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
    // No additional configuration needed
    // JAX-RS will automatically discover all @Path annotated classes
}
