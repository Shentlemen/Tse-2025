package uy.gub.clinic.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.AccessRequest;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.entity.Patient;
import uy.gub.clinic.entity.Professional;
import uy.gub.clinic.integration.dto.AccessRequestNotificationRequest;
import uy.gub.clinic.integration.dto.DocumentMetadata;
import uy.gub.clinic.service.AccessRequestService;
import uy.gub.clinic.service.ExternalDocumentService;
import uy.gub.clinic.service.PatientService;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

/**
 * Servlet REST para recibir notificaciones del HCEN cuando se aprueban/deniegan access requests
 * 
 * Endpoint: POST /api/clinic/access-requests/notifications
 * 
 * @author TSE 2025 Group 9
 */
@WebServlet("/api/clinic/access-requests/notifications")
public class AccessRequestNotificationServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(AccessRequestNotificationServlet.class);
    
    private final ObjectMapper objectMapper;
    
    @Inject
    private AccessRequestService accessRequestService;
    
    @Inject
    private ExternalDocumentService externalDocumentService;
    
    @Inject
    private PatientService patientService;
    
    public AccessRequestNotificationServlet() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        
        try {
            // Leer body del request
            StringBuilder jsonBuilder = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
            }
            
            String jsonBody = jsonBuilder.toString();
            logger.info("Received access request notification from HCEN: {}", jsonBody);
            
            // Parsear JSON
            AccessRequestNotificationRequest notification = objectMapper.readValue(
                jsonBody, AccessRequestNotificationRequest.class);
            
            // Validar datos básicos
            if (notification.getHcenRequestId() == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, 
                    "Missing hcenRequestId in notification");
                return;
            }
            
            if (notification.getStatus() == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, 
                    "Missing status in notification");
                return;
            }
            
            // Buscar access request por HCEN request ID
            Optional<AccessRequest> accessRequestOpt = accessRequestService.findByHcenRequestId(
                notification.getHcenRequestId().toString());
            
            if (accessRequestOpt.isEmpty()) {
                logger.warn("Access request not found for HCEN ID: {}", notification.getHcenRequestId());
                sendError(response, HttpServletResponse.SC_NOT_FOUND, 
                    "Access request not found with HCEN ID: " + notification.getHcenRequestId());
                return;
            }
            
            AccessRequest accessRequest = accessRequestOpt.get();
            
            // Actualizar estado de la solicitud
            accessRequestService.updateStatus(accessRequest.getId(), notification.getStatus());
            
            logger.info("Access request updated - Local ID: {}, HCEN ID: {}, Status: {}", 
                accessRequest.getId(), notification.getHcenRequestId(), notification.getStatus());
            
            // Si fue aprobada, descargar documentos externos
            if ("APPROVED".equals(notification.getStatus()) && 
                notification.getDocumentMetadata() != null && 
                !notification.getDocumentMetadata().isEmpty()) {
                
                logger.info("Processing {} approved documents for download", 
                    notification.getDocumentMetadata().size());
                
                // Buscar paciente por CI
                Optional<Patient> patientOpt = patientService.getPatientByDocumentNumber(
                    notification.getPatientCi());
                
                if (patientOpt.isEmpty()) {
                    logger.warn("Patient not found for CI: {}", notification.getPatientCi());
                    // Continuar de todos modos - algunos documentos pueden procesarse
                } else {
                    Patient patient = patientOpt.get();
                    Professional professional = accessRequest.getProfessional();
                    Clinic clinic = accessRequest.getClinic();
                    
                    // Descargar cada documento
                    int downloadedCount = 0;
                    for (DocumentMetadata docMetadata : notification.getDocumentMetadata()) {
                        try {
                            externalDocumentService.downloadAndStoreExternalDocument(
                                docMetadata, patient, professional, clinic);
                            downloadedCount++;
                            logger.info("External document downloaded - Document ID: {}, Locator: {}", 
                                docMetadata.getDocumentId(), docMetadata.getDocumentLocator());
                        } catch (Exception e) {
                            logger.error("Failed to download external document - Locator: {}", 
                                docMetadata.getDocumentLocator(), e);
                            // Continuar con los demás documentos
                        }
                    }
                    
                    logger.info("Downloaded {} out of {} external documents for patient CI: {}", 
                        downloadedCount, notification.getDocumentMetadata().size(), 
                        notification.getPatientCi());
                }
            }
            
            // Enviar respuesta exitosa
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"status\":\"success\",\"message\":\"Notification processed\"}");
            
        } catch (Exception e) {
            logger.error("Error processing access request notification from HCEN", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Error processing notification: " + e.getMessage());
        }
    }
    
    private void sendError(HttpServletResponse response, int status, String message) 
            throws IOException {
        response.setStatus(status);
        response.getWriter().write(
            String.format("{\"status\":\"error\",\"message\":\"%s\"}", message));
    }
}

