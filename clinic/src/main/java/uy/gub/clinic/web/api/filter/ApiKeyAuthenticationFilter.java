package uy.gub.clinic.web.api.filter;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.config.ApiConfigurationService;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.service.ClinicService;

import java.util.List;
import java.util.Optional;

/**
 * Filtro de autenticación por API Key para endpoints REST
 * 
 * Valida que las peticiones incluyan un header con API key válido:
 * - Para /api/clinics (POST): Valida contra el API key de configuración (el que envía HCEN)
 * - Para otros endpoints: Valida contra el API key de la clínica (almacenado en la base de datos)
 * 
 * @author TSE 2025 Group 9
 */
@Provider
public class ApiKeyAuthenticationFilter implements ContainerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
    
    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    
    // Endpoints que NO requieren autenticación
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
        "/api/health"
    );
    
    // Endpoint de creación de clínicas - requiere API key de configuración
    private static final String CLINIC_REGISTRATION_PATH = "/api/clinics";
    
    @Inject
    private ClinicService clinicService;
    
    @Inject
    private ApiConfigurationService apiConfigService;
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();
        
        // Permitir endpoints públicos
        if (isPublicEndpoint(path)) {
            logger.debug("Public endpoint accessed: {}", path);
            return;
        }
        
        // Obtener API key del header
        String apiKey = extractApiKey(requestContext);
        
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("API key missing for endpoint: {} {}", method, path);
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"API key required. Please provide X-API-Key or Authorization: Bearer header.\"}")
                    .build()
            );
            return;
        }
        
        // Para el endpoint de creación de clínicas, validar contra API key de configuración
        if (isClinicRegistrationEndpoint(path, method)) {
            if (apiConfigService.validateApiKey(apiKey)) {
                logger.debug("API key validated for clinic registration");
                return; // Permitir la petición
            } else {
                logger.warn("Invalid configuration API key for clinic registration: {}", path);
                requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Invalid API key for clinic registration.\"}")
                        .build()
                );
                return;
            }
        }
        
        // Para otros endpoints, validar contra API key de clínica
        Optional<Clinic> clinicOpt = clinicService.getClinicByApiKey(apiKey);
        
        if (clinicOpt.isEmpty()) {
            logger.warn("Invalid clinic API key for endpoint: {} {}", method, path);
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Invalid API key.\"}")
                    .build()
            );
            return;
        }
        
        Clinic clinic = clinicOpt.get();
        logger.debug("API key validated for clinic: {} (ID: {})", clinic.getName(), clinic.getId());
        
        // Agregar información de la clínica al contexto para uso posterior
        requestContext.setProperty("authenticatedClinic", clinic);
        requestContext.setProperty("authenticatedClinicId", clinic.getId());
    }
    
    /**
     * Extrae la API key de los headers de la petición
     * Busca en X-API-Key o Authorization: Bearer {apiKey}
     */
    private String extractApiKey(ContainerRequestContext requestContext) {
        // Intentar obtener de X-API-Key header
        String apiKey = requestContext.getHeaderString(API_KEY_HEADER);
        
        if (apiKey != null && !apiKey.isEmpty()) {
            return apiKey.trim();
        }
        
        // Intentar obtener de Authorization: Bearer {apiKey}
        String authHeader = requestContext.getHeaderString(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        
        return null;
    }
    
    /**
     * Verifica si el endpoint es público (no requiere autenticación)
     */
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * Verifica si es el endpoint de registro de clínicas
     * Solo el POST requiere autenticación con API key de configuración
     */
    private boolean isClinicRegistrationEndpoint(String path, String method) {
        return path.equals(CLINIC_REGISTRATION_PATH) && "POST".equals(method);
    }
}

