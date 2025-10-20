package uy.gub.hcen.api.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.Map;

import javax.annotation.processing.Generated;

/**
 * Health check endpoint to verify application is running
 */
@Path("/health")
public class HealthCheckResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response healthCheck() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "HCEN Central Component",
            "timestamp", LocalDateTime.now().toString(),
            "version", "1.0.0-SNAPSHOT"
        );

        return Response.ok(health).build();
    }
    
}
