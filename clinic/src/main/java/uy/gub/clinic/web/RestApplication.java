package uy.gub.clinic.web;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuración de la aplicación JAX-RS para endpoints REST
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
    // La configuración se hace automáticamente mediante anotaciones

}

