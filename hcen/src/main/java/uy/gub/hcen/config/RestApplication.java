package uy.gub.hcen.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application configuration
 * All REST endpoints will be available under /api/*
 */
@ApplicationPath("/api")
public class RestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();

        // Register all REST resources explicitly
        resources.add(uy.gub.hcen.auth.api.rest.AuthenticationResource.class);
        resources.add(uy.gub.hcen.api.rest.HealthCheckResource.class);

        return resources;
    }
}
