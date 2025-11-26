package uy.gub.clinic.web;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uy.gub.clinic.entity.ClinicalDocument;
import uy.gub.clinic.service.AccessRequestService;
import uy.gub.clinic.service.ClinicalDocumentService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servlet para el dashboard del profesional
 */
@WebServlet("/professional/dashboard")
public class ProfessionalDashboardServlet extends HttpServlet {

    @Inject
    private ClinicalDocumentService documentService;
    
    @Inject
    private AccessRequestService accessRequestService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        // Inicializar valores por defecto
        List<ClinicalDocument> recentDocuments = new ArrayList<>();
        long totalDocuments = 0;
        long pendingRequests = 0;
        List<uy.gub.clinic.entity.AccessRequest> pendingRequestsList = new ArrayList<>();

        try {
            // Obtener professionalId de la sesión
            Object professionalIdObj = request.getSession().getAttribute("professionalId");
            Long professionalId = null;
            
            // Manejar diferentes tipos de conversión
            if (professionalIdObj instanceof Long) {
                professionalId = (Long) professionalIdObj;
            } else if (professionalIdObj instanceof Number) {
                professionalId = ((Number) professionalIdObj).longValue();
            } else if (professionalIdObj != null) {
                try {
                    professionalId = Long.parseLong(professionalIdObj.toString());
                } catch (NumberFormatException e) {
                    // Error al convertir professionalId
                }
            }
            
            if (professionalId == null) {
                request.setAttribute("error", "Error de sesión: Profesional no identificado. Por favor, inicie sesión nuevamente.");
                request.setAttribute("recentDocuments", recentDocuments);
                request.setAttribute("totalDocuments", totalDocuments);
                request.setAttribute("pendingRequests", pendingRequests);
                request.setAttribute("pendingRequestsList", pendingRequestsList);
                request.getRequestDispatcher("/WEB-INF/views/professional/dashboard.jsp").forward(request, response);
                return;
            }

            // Obtener documentos recientes del profesional (últimos 10)
            if (documentService != null) {
                try {
                    List<ClinicalDocument> allDocuments = documentService.findByProfessional(professionalId);
                    
                    if (allDocuments != null && !allDocuments.isEmpty()) {
                        recentDocuments = allDocuments.stream()
                            .sorted((d1, d2) -> {
                                if (d1.getDateOfVisit() == null && d2.getDateOfVisit() == null) return 0;
                                if (d1.getDateOfVisit() == null) return 1;
                                if (d2.getDateOfVisit() == null) return -1;
                                return d2.getDateOfVisit().compareTo(d1.getDateOfVisit());
                            })
                            .limit(10)
                            .collect(Collectors.toList());

                        // Contar documentos totales del profesional
                        totalDocuments = allDocuments.size();
                    }
                } catch (Exception e) {
                    request.setAttribute("error", "Error al cargar documentos: " + e.getMessage());
                }
            } else {
                request.setAttribute("error", "Error de configuración: servicio de documentos no disponible");
            }

            // Obtener solicitudes pendientes
            if (accessRequestService != null) {
                try {
                    pendingRequests = accessRequestService.countPendingByProfessional(professionalId);
                    pendingRequestsList = accessRequestService.findPendingByProfessional(professionalId);
                } catch (Exception e) {
                    if (request.getAttribute("error") == null) {
                        request.setAttribute("error", "Error al cargar solicitudes: " + e.getMessage());
                    }
                }
            } else {
                if (request.getAttribute("error") == null) {
                    request.setAttribute("error", "Error de configuración: servicio de solicitudes no disponible");
                }
            }

        } catch (Exception e) {
            if (request.getAttribute("error") == null) {
                request.setAttribute("error", "Error al cargar el dashboard: " + e.getMessage());
            }
        } finally {
            // Asegurar que siempre se establezcan los atributos, incluso si hay errores
            request.setAttribute("recentDocuments", recentDocuments);
            request.setAttribute("totalDocuments", totalDocuments);
            request.setAttribute("pendingRequests", pendingRequests);
            request.setAttribute("pendingRequestsList", pendingRequestsList);
        }

        request.getRequestDispatcher("/WEB-INF/views/professional/dashboard.jsp").forward(request, response);
    }
}

