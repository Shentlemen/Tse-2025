package uy.gub.clinic.web;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uy.gub.clinic.entity.AccessRequest;
import uy.gub.clinic.service.AccessRequestService;

import java.io.IOException;
import java.util.List;

/**
 * Servlet para gestionar la vista de solicitudes del profesional
 */
@WebServlet("/professional/requests")
public class ProfessionalRequestsServlet extends HttpServlet {

    @Inject
    private AccessRequestService accessRequestService;

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
                request.getRequestDispatcher("/professional/requests.jsp").forward(request, response);
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

        request.getRequestDispatcher("/professional/requests.jsp").forward(request, response);
    }
}

