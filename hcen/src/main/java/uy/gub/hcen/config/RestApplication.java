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
        resources.add(uy.gub.hcen.inus.api.rest.InusResource.class);
        resources.add(uy.gub.hcen.rndc.api.rest.RndcResource.class);
        resources.add(uy.gub.hcen.policy.api.rest.AccessRequestResource.class);
        resources.add(uy.gub.hcen.policy.api.rest.PolicyManagementResource.class);
        resources.add(uy.gub.hcen.audit.api.rest.AuditResource.class);
        resources.add(uy.gub.hcen.audit.api.rest.AuditLogResource.class);
        resources.add(uy.gub.hcen.service.clinic.api.rest.ClinicResource.class);
        resources.add(uy.gub.hcen.clinicalhistory.api.rest.ClinicalHistoryResource.class);
        resources.add(uy.gub.hcen.admin.rest.StatisticsResource.class);
        resources.add(uy.gub.hcen.patient.api.rest.PatientStatisticsResource.class);

        // Register authentication filters
        resources.add(uy.gub.hcen.auth.filter.JwtAuthenticationFilter.class);
        resources.add(uy.gub.hcen.auth.filter.ClinicApiKeyAuthenticationFilter.class);

        return resources;
    }
}
