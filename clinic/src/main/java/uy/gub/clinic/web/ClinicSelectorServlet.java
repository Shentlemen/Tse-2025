package uy.gub.clinic.web;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.service.ClinicService;

import java.io.IOException;
import java.util.List;

/**
 * Servlet para manejar la selección de clínica para super administradores
 */
public class ClinicSelectorServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(ClinicSelectorServlet.class);
    
    @Inject
    private ClinicService clinicService;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        if (!"SUPER_ADMIN".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
            return;
        }
        
        try {
            List<Clinic> clinics = clinicService.getAllClinics();
            request.setAttribute("clinics", clinics);
            request.getRequestDispatcher("/admin/clinic-selector.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Error al cargar selector de clínica", e);
            request.setAttribute("error", "Error al cargar las clínicas");
            request.getRequestDispatcher("/admin/clinic-selector.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        if (!"SUPER_ADMIN".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
            return;
        }
        
        String clinicIdStr = request.getParameter("clinicId");
        String targetUrl = request.getParameter("targetUrl");
        
        if (clinicIdStr != null && !clinicIdStr.trim().isEmpty()) {
            try {
                Long clinicId = Long.parseLong(clinicIdStr);
                
                // Verificar que la clínica existe
                if (clinicService.getClinicById(clinicId).isPresent()) {
                    Clinic clinic = clinicService.getClinicById(clinicId).get();
                    
                    // Actualizar la sesión con la clínica seleccionada
                    session.setAttribute("clinicId", clinicId);
                    session.setAttribute("clinicName", clinic.getName());
                    
                    logger.info("Super administrador seleccionó clínica: {} (ID: {})", clinic.getName(), clinicId);
                    
                    // Redirigir a la URL objetivo o al dashboard
                    if (targetUrl != null && !targetUrl.trim().isEmpty()) {
                        response.sendRedirect(request.getContextPath() + targetUrl);
                    } else {
                        response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
                    }
                    return;
                }
            } catch (NumberFormatException e) {
                logger.error("ID de clínica inválido: {}", clinicIdStr);
            }
        }
        
        request.setAttribute("error", "Clínica no válida");
        doGet(request, response);
    }
}
