package uy.gub.clinic.web;

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
import uy.gub.clinic.entity.Professional;
import uy.gub.clinic.service.AccessRequestService;
import uy.gub.clinic.service.ClinicService;
import uy.gub.clinic.service.ProfessionalService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servlet para gestionar la vista de solicitudes del profesional
 */
@WebServlet("/professional/requests")
public class ProfessionalRequestsServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ProfessionalRequestsServlet.class);

    @Inject
    private AccessRequestService accessRequestService;
    
    @Inject
    private ProfessionalService professionalService;
    
    @Inject
    private ClinicService clinicService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        try {
            // Obtener el ID del profesional logueado
            Long professionalId = (Long) request.getSession().getAttribute("professionalId");
            if (professionalId == null) {
                request.setAttribute("error", "Error de sesión: Profesional no identificado");
                request.getRequestDispatcher("/WEB-INF/views/professional/requests.jsp").forward(request, response);
                return;
            }

            // Obtener todas las solicitudes pendientes del profesional
            List<AccessRequest> pendingRequests = accessRequestService.findPendingByProfessional(professionalId);
            
            // Ordenar por fecha de solicitud descendente (más recientes primero)
            pendingRequests.sort((r1, r2) -> {
                if (r1.getRequestedAt() == null && r2.getRequestedAt() == null) return 0;
                if (r1.getRequestedAt() == null) return 1;
                if (r2.getRequestedAt() == null) return -1;
                return r2.getRequestedAt().compareTo(r1.getRequestedAt());
            });

            request.setAttribute("requests", pendingRequests);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error al cargar solicitudes: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/professional/requests.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        try {
            Long professionalId = (Long) request.getSession().getAttribute("professionalId");
            String clinicId = (String) request.getSession().getAttribute("clinicId");

            if (professionalId == null || clinicId == null) {
                request.setAttribute("error", "Error de sesión: Profesional o clínica no identificados");
                doGet(request, response);
                return;
            }
            
            String action = request.getParameter("action");
            
            if ("create".equals(action)) {
                // Obtener datos del formulario
                String patientCI = request.getParameter("patientCI");
                String specialties = request.getParameter("specialties");
                String requestReason = request.getParameter("requestReason");
                String urgency = request.getParameter("urgency");
                String documentIdStr = request.getParameter("documentId");
                
                // Validar datos básicos
                if (patientCI == null || patientCI.trim().isEmpty()) {
                    request.setAttribute("error", "La cédula del paciente es requerida");
                    doGet(request, response);
                    return;
                }
                
                if (requestReason == null || requestReason.trim().isEmpty()) {
                    request.setAttribute("error", "La razón de la solicitud es requerida");
                    doGet(request, response);
                    return;
                }
                
                // Cargar profesional y clínica
                Professional professional = professionalService.getProfessionalById(professionalId)
                    .orElseThrow(() -> new ServletException("Profesional no encontrado"));
                
                Clinic clinic = clinicService.getClinicById(clinicId)
                    .orElseThrow(() -> new ServletException("Clínica no encontrada"));
                
                // Parsear documentId si existe
                Long documentId = null;
                if (documentIdStr != null && !documentIdStr.trim().isEmpty()) {
                    try {
                        documentId = Long.parseLong(documentIdStr);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid documentId format: {}", documentIdStr);
                    }
                }
                
                // Crear solicitud de acceso
                AccessRequest accessRequest = accessRequestService.createAccessRequest(
                    professional,
                    clinic,
                    patientCI.trim(),
                    documentId,
                    specialties != null ? specialties.trim() : null,
                    requestReason.trim(),
                    urgency != null ? urgency : "ROUTINE"
                );
                
                logger.info("Access request created - ID: {}, Professional: {}, Patient CI: {}", 
                    accessRequest.getId(), professionalId, patientCI);
                
                request.setAttribute("success", "Solicitud de acceso creada exitosamente. Será procesada por HCEN.");
                
            } else {
                request.setAttribute("error", "Acción no válida: " + action);
            }
            
        } catch (Exception e) {
            logger.error("Error processing POST request", e);
            request.setAttribute("error", "Error al procesar solicitud: " + e.getMessage());
        }
        
        doGet(request, response);
    }
}

