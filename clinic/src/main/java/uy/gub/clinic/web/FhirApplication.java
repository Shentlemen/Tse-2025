package uy.gub.clinic.web;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Configuración de la aplicación JAX-RS para endpoints FHIR
 */
@ApplicationPath("/api")
public class FhirApplication extends Application {
    // La configuración se hace automáticamente mediante anotaciones
}

