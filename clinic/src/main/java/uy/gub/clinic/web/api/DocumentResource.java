package uy.gub.clinic.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.ClinicalDocument;
import uy.gub.clinic.service.ClinicalDocumentService;

import java.util.Optional;

/**
 * Endpoint REST para obtener documentos clínicos individuales
 * Usado por HCEN y otras clínicas para descargar documentos mediante documentLocator
 * 
 * Endpoint: GET /api/documents/{id}
 * 
 * @author TSE 2025 Group 9
 */
@Path("/documents")
@Produces(MediaType.APPLICATION_JSON)
public class DocumentResource {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentResource.class);
    
    private final ObjectMapper objectMapper;
    
    @Inject
    private ClinicalDocumentService documentService;
    
    public DocumentResource() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Obtiene un documento clínico por ID
     * Este endpoint es usado por HCEN y otras clínicas para descargar documentos
     * cuando se aprueba un access request
     * 
     * @param id ID del documento
     * @return Documento clínico en formato JSON
     */
    @GET
    @Path("/{id}")
    public Response getDocument(@PathParam("id") Long id) {
        try {
            logger.info("Retrieving document - ID: {}", id);
            
            Optional<ClinicalDocument> docOpt = documentService.findById(id);
            
            if (docOpt.isEmpty()) {
                logger.warn("Document not found - ID: {}", id);
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Document not found\"}")
                    .build();
            }
            
            ClinicalDocument document = docOpt.get();
            
            // Construir JSON del documento
            String jsonDocument = buildDocumentJson(document);
            
            logger.info("Document retrieved successfully - ID: {}", id);
            
            return Response.ok(jsonDocument)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .build();
            
        } catch (Exception e) {
            logger.error("Error retrieving document - ID: {}", id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Error retrieving document: " + e.getMessage() + "\"}")
                .build();
        }
    }
    
    /**
     * Construye el JSON del documento para respuesta
     */
    private String buildDocumentJson(ClinicalDocument document) {
        try {
            com.fasterxml.jackson.databind.node.ObjectNode json = objectMapper.createObjectNode();
            
            json.put("id", document.getId());
            json.put("title", document.getTitle() != null ? document.getTitle() : "");
            json.put("description", document.getDescription() != null ? document.getDescription() : "");
            json.put("documentType", document.getDocumentType());
            
            if (document.getDateOfVisit() != null) {
                json.put("dateOfVisit", document.getDateOfVisit().toString());
            }
            
            if (document.getChiefComplaint() != null) {
                json.put("chiefComplaint", document.getChiefComplaint());
            }
            if (document.getCurrentIllness() != null) {
                json.put("currentIllness", document.getCurrentIllness());
            }
            if (document.getVitalSigns() != null) {
                json.put("vitalSigns", document.getVitalSigns());
            }
            if (document.getPhysicalExamination() != null) {
                json.put("physicalExamination", document.getPhysicalExamination());
            }
            if (document.getDiagnosis() != null) {
                json.put("diagnosis", document.getDiagnosis());
            }
            if (document.getTreatment() != null) {
                json.put("treatment", document.getTreatment());
            }
            if (document.getPrescriptions() != null) {
                json.put("prescriptions", document.getPrescriptions());
            }
            if (document.getObservations() != null) {
                json.put("observations", document.getObservations());
            }
            if (document.getNextAppointment() != null) {
                json.put("nextAppointment", document.getNextAppointment().toString());
            }
            if (document.getAttachments() != null) {
                json.put("attachments", document.getAttachments());
            }
            
            // Información del paciente
            if (document.getPatient() != null) {
                com.fasterxml.jackson.databind.node.ObjectNode patientJson = objectMapper.createObjectNode();
                patientJson.put("id", document.getPatient().getId());
                patientJson.put("documentNumber", document.getPatient().getDocumentNumber() != null ? 
                    document.getPatient().getDocumentNumber() : "");
                patientJson.put("name", document.getPatient().getName() != null ? 
                    document.getPatient().getName() : "");
                patientJson.put("lastName", document.getPatient().getLastName() != null ? 
                    document.getPatient().getLastName() : "");
                json.set("patient", patientJson);
            }
            
            // Información del profesional
            if (document.getProfessional() != null) {
                com.fasterxml.jackson.databind.node.ObjectNode professionalJson = objectMapper.createObjectNode();
                professionalJson.put("id", document.getProfessional().getId());
                professionalJson.put("name", document.getProfessional().getName() != null ? 
                    document.getProfessional().getName() : "");
                professionalJson.put("lastName", document.getProfessional().getLastName() != null ? 
                    document.getProfessional().getLastName() : "");
                if (document.getProfessional().getSpecialty() != null) {
                    professionalJson.put("specialty", document.getProfessional().getSpecialty().getName() != null ? 
                        document.getProfessional().getSpecialty().getName() : "");
                }
                json.set("professional", professionalJson);
            }
            
            // Información de la clínica
            if (document.getClinic() != null) {
                com.fasterxml.jackson.databind.node.ObjectNode clinicJson = objectMapper.createObjectNode();
                clinicJson.put("id", document.getClinic().getId());
                clinicJson.put("name", document.getClinic().getName() != null ? 
                    document.getClinic().getName() : "");
                clinicJson.put("code", document.getClinic().getCode() != null ? 
                    document.getClinic().getCode() : "");
                json.set("clinic", clinicJson);
            }
            
            // Fechas
            if (document.getCreatedAt() != null) {
                json.put("createdAt", document.getCreatedAt().toString());
            }
            if (document.getUpdatedAt() != null) {
                json.put("updatedAt", document.getUpdatedAt().toString());
            }
            
            return objectMapper.writeValueAsString(json);
            
        } catch (Exception e) {
            logger.error("Error building document JSON", e);
            throw new RuntimeException("Error serializing document", e);
        }
    }
}

