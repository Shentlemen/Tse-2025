package uy.gub.clinic.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.integration.dto.AccessRequestCreationRequest;
import uy.gub.clinic.integration.dto.AccessRequestCreationResponse;

import java.util.Base64;

/**
 * Cliente REST para comunicación con HCEN Central
 * Usado para crear access requests y otras operaciones REST
 * 
 * @author TSE 2025 Group 9
 */
@Stateless
public class HcenRestClient {
    
    private static final Logger logger = LoggerFactory.getLogger(HcenRestClient.class);
    
    private final ObjectMapper objectMapper;
    private final Client httpClient;
    
    public HcenRestClient() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.httpClient = ClientBuilder.newClient();
    }
    
    /**
     * Crea una solicitud de acceso en el HCEN
     * 
     * @param request Datos de la solicitud
     * @param clinicId ID de la clínica
     * @param apiKey API key de la clínica
     * @param hcenBaseUrl URL base del HCEN (ej: http://localhost:8080/hcen/api)
     * @return Respuesta del HCEN con el requestId y estado
     */
    public AccessRequestCreationResponse createAccessRequest(
            AccessRequestCreationRequest request,
            String clinicId,
            String apiKey,
            String hcenBaseUrl) {
        
        try {
            String endpoint = hcenBaseUrl;
            if (!endpoint.endsWith("/")) {
                endpoint += "/";
            }
            endpoint += "access-requests";
            
            logger.info("Creating access request in HCEN - Endpoint: {}, Professional: {}, Patient: {}", 
                endpoint, request.getProfessionalId(), request.getPatientCi());
            
            // Construir API key en formato base64(clinicId:apiKey)
//            String authValue = clinicId + ":" + (apiKey != null ? apiKey : "");
//            String apiKeyHeader = Base64.getEncoder().encodeToString(authValue.getBytes());
            
            // Realizar petición POST
            Response response = httpClient.target(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .header("X-API-Key",  apiKey)
                    .header("X-Clinic-Id", clinicId)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));
            
            // Procesar respuesta
            if (response.getStatus() == Response.Status.CREATED.getStatusCode() || 
                response.getStatus() == Response.Status.OK.getStatusCode()) {
                
                String responseJson = response.readEntity(String.class);
                AccessRequestCreationResponse result = objectMapper.readValue(
                    responseJson, AccessRequestCreationResponse.class);
                
                logger.info("Access request created in HCEN - Request ID: {}, Status: {}", 
                    result.getRequestId(), result.getStatus());
                
                return result;
                
            } else {
                String errorMessage = response.readEntity(String.class);
                logger.error("Failed to create access request in HCEN - Status: {}, Error: {}", 
                    response.getStatus(), errorMessage);
                throw new RuntimeException("Failed to create access request: " + errorMessage);
            }
            
        } catch (Exception e) {
            logger.error("Error creating access request in HCEN", e);
            throw new RuntimeException("Error communicating with HCEN", e);
        }
    }
    
    /**
     * Cierra el cliente HTTP (debería llamarse en @PreDestroy)
     */
    public void close() {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}

