package uy.gub.clinic.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.ClinicalDocument;
import uy.gub.clinic.entity.Patient;
import uy.gub.clinic.service.ClinicalDocumentService;
import uy.gub.clinic.service.FhirMappingService;
import uy.gub.clinic.service.PatientService;

import java.util.List;
import java.util.Optional;

/**
 * Endpoint REST para obtener documentos clínicos en formato FHIR
 */
@Path("/fhir")
@Produces(MediaType.APPLICATION_JSON)
public class FhirDocumentEndpoint {
    
    private static final Logger logger = LoggerFactory.getLogger(FhirDocumentEndpoint.class);
    
    @Inject
    private PatientService patientService;
    
    @Inject
    private ClinicalDocumentService documentService;
    
    @Inject
    private FhirMappingService fhirMappingService;
    
    /**
     * Obtiene todos los documentos clínicos de un paciente por su cédula de identidad
     * 
     * @param cedula Número de cédula de identidad del paciente
     * @return Bundle FHIR con todos los documentos del paciente
     */
    @GET
    @Path("/documents")
    public Response getDocumentsByCedula(@QueryParam("cedula") String cedula) {
        try {
            // Validar parámetro
            if (cedula == null || cedula.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"El parámetro 'cedula' es requerido\"}")
                    .build();
            }
            
            logger.info("Buscando documentos para cédula: {}", cedula);
            
            // Buscar paciente por cédula
            Optional<Patient> patientOpt = patientService.getPatientByDocumentNumber(cedula.trim());
            
            if (patientOpt.isEmpty()) {
                logger.warn("Paciente no encontrado con cédula: {}", cedula);
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Paciente no encontrado con la cédula proporcionada\"}")
                    .build();
            }
            
            Patient patient = patientOpt.get();
            logger.info("Paciente encontrado: {} (ID: {})", patient.getFullName(), patient.getId());
            
            // Obtener todos los documentos del paciente
            List<ClinicalDocument> documents = documentService.findByPatient(patient.getId());
            
            if (documents.isEmpty()) {
                logger.info("No se encontraron documentos para el paciente con cédula: {}", cedula);
                // Retornar un Bundle vacío en lugar de error
                Bundle emptyBundle = new Bundle();
                emptyBundle.setType(Bundle.BundleType.COLLECTION);
                emptyBundle.setId("empty-bundle");
                
                // Incluir al menos el recurso Patient
                org.hl7.fhir.r4.model.Patient fhirPatient = fhirMappingService.convertToFhirPatient(patient);
                emptyBundle.addEntry().setResource(fhirPatient).setFullUrl("Patient/" + fhirPatient.getId());
                
                return Response.ok(fhirMappingService.bundleToJson(emptyBundle))
                    .build();
            }
            
            logger.info("Encontrados {} documentos para el paciente", documents.size());
            
            // Crear Bundle principal que contendrá todos los recursos
            Bundle mainBundle = new Bundle();
            mainBundle.setType(Bundle.BundleType.COLLECTION);
            mainBundle.setId("bundle-patient-" + patient.getId());
            
            // Agregar el recurso Patient una sola vez
            org.hl7.fhir.r4.model.Patient fhirPatient = fhirMappingService.convertToFhirPatient(patient);
            mainBundle.addEntry().setResource(fhirPatient).setFullUrl("Patient/" + fhirPatient.getId());
            
            // Set para rastrear recursos ya agregados (por fullUrl) para evitar duplicados
            java.util.Set<String> addedResources = new java.util.HashSet<>();
            addedResources.add("Patient/" + fhirPatient.getId());
            
            // Convertir cada documento a Bundle y agregar sus recursos al Bundle principal
            for (ClinicalDocument document : documents) {
                Bundle documentBundle = fhirMappingService.convertDocumentToFhirBundle(document);
                
                // Agregar todos los recursos del documento al Bundle principal (evitando duplicados)
                for (Bundle.BundleEntryComponent entry : documentBundle.getEntry()) {
                    String fullUrl = entry.getFullUrl();
                    if (fullUrl == null || fullUrl.isEmpty()) {
                        // Si no tiene fullUrl, generar uno basado en el recurso
                        if (entry.getResource() != null) {
                            fullUrl = entry.getResource().getResourceType().name() + "/" + entry.getResource().getId();
                        }
                    }
                    
                    // Solo agregar si no está duplicado
                    if (fullUrl != null && !addedResources.contains(fullUrl)) {
                        mainBundle.addEntry(entry);
                        addedResources.add(fullUrl);
                    }
                }
            }
            
            logger.info("Bundle FHIR creado exitosamente con {} recursos", mainBundle.getEntry().size());
            
            // Serializar a JSON
            String jsonResponse = fhirMappingService.bundleToJson(mainBundle);
            
            return Response.ok(jsonResponse)
                .header("Content-Type", "application/fhir+json")
                .build();
            
        } catch (Exception e) {
            logger.error("Error al obtener documentos en formato FHIR para cédula: {}", cedula, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Error interno del servidor: " + e.getMessage() + "\"}")
                .build();
        }
    }
    
    /**
     * Endpoint de salud/verificación
     */
    @GET
    @Path("/health")
    public Response health() {
        return Response.ok("{\"status\": \"OK\", \"service\": \"FHIR Document Endpoint\"}").build();
    }
}

