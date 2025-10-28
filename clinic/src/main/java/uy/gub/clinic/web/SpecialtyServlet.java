package uy.gub.clinic.web;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import uy.gub.clinic.entity.Specialty;
import uy.gub.clinic.service.SpecialtyService;

import java.io.IOException;
import java.util.List;

/**
 * Servlet para manejar las operaciones CRUD de especialidades
 */
@WebServlet("/admin/specialties-list")
public class SpecialtyServlet extends HttpServlet {

    @Inject
    private SpecialtyService specialtyService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configurar codificación UTF-8
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        System.out.println("=== SpecialtyServlet.doGet ===");
        System.out.println("SpecialtyService inyectado: " + (specialtyService != null ? "SÍ" : "NO"));

        if (specialtyService == null) {
            System.out.println("ERROR: SpecialtyService es NULL - problema de inyección de dependencias");
            request.setAttribute("error", "Error de configuración: SpecialtyService no disponible");
            request.getRequestDispatcher("/admin/specialties.jsp").forward(request, response);
            return;
        }

        try {
            // Obtener el contexto de clínica del usuario
            HttpSession session = request.getSession(false);
            Long clinicId = null;
            
            if (session != null) {
                clinicId = (Long) session.getAttribute("clinicId");
                System.out.println("ClinicId desde sesión: " + clinicId);
            }
            
            // Obtener especialidades según el contexto
            System.out.println("DEBUG SpecialtyServlet: Obteniendo especialidades para clínica ID: " + clinicId);
            System.out.println("Llamando a specialtyService.getSpecialtiesByClinic(" + clinicId + ")...");
            List<Specialty> specialties = specialtyService.getSpecialtiesByClinic(clinicId);
            System.out.println("Especialidades obtenidas: " + specialties.size());

            for (Specialty specialty : specialties) {
                System.out.println("  - " + specialty.getName() + " (ID: " + specialty.getId() + 
                                 ", Clínica: " + (specialty.getClinic() != null ? specialty.getClinic().getName() : "NULL") + ")");
            }

            System.out.println("DEBUG: Estableciendo atributo 'specialties' en request con " + specialties.size() + " elementos");
            request.setAttribute("specialties", specialties);
            
            // Verificar que se estableció correctamente
            Object specialtiesFromRequest = request.getAttribute("specialties");
            System.out.println("DEBUG: Verificando atributo 'specialties' en request: " + 
                (specialtiesFromRequest != null ? "NO NULL" : "NULL"));
            if (specialtiesFromRequest instanceof List) {
                System.out.println("DEBUG: Es una Lista con " + ((List<?>) specialtiesFromRequest).size() + " elementos");
            }
        } catch (Exception e) {
            System.out.println("Error al obtener especialidades: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Error al cargar especialidades: " + e.getMessage());
        }

        // Redirigir a la página de gestión de especialidades
        request.getRequestDispatcher("/admin/specialties.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Configurar codificación UTF-8
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        
        System.out.println("=== SpecialtyServlet.doPost ===");
        System.out.println("Action: " + request.getParameter("action"));
        System.out.println("Name: " + request.getParameter("name"));
        System.out.println("Code: " + request.getParameter("code"));
        System.out.println("Description: " + request.getParameter("description"));
        
        String action = request.getParameter("action");
        
        try {
            switch (action) {
                case "register":
                    registerSpecialty(request, response);
                    break;
                case "update":
                    updateSpecialty(request, response);
                    break;
                case "delete":
                    deleteSpecialty(request, response);
                    break;
                case "activate":
                    toggleSpecialtyStatus(request, response, true);
                    break;
                case "deactivate":
                    toggleSpecialtyStatus(request, response, false);
                    break;
                default:
                    System.out.println("Acción no válida: " + action);
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no válida");
            }
        } catch (Exception e) {
            System.out.println("Error en doPost: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", e.getMessage());
            doGet(request, response); // Recargar la página con el error
        }
    }
    
    private void registerSpecialty(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== registerSpecialty ===");
        
        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String description = request.getParameter("description");
        
        // Obtener la clínica del usuario
        HttpSession session = request.getSession(false);
        Long clinicId = null;
        
        if (session != null) {
            clinicId = (Long) session.getAttribute("clinicId");
            System.out.println("ClinicId desde sesión: " + clinicId);
        }
        
        System.out.println("Datos recibidos:");
        System.out.println("- Name: " + name);
        System.out.println("- Code: " + code);
        System.out.println("- Description: " + description);
        System.out.println("- ClinicId: " + clinicId);
        
        // Validaciones básicas
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        
        if (clinicId == null) {
            throw new IllegalArgumentException("No se pudo determinar la clínica del usuario");
        }
        
        System.out.println("Llamando a specialtyService.registerSpecialty...");
        
        Specialty specialty = specialtyService.registerSpecialty(name, code, description, clinicId);
        
        System.out.println("Especialidad registrada: " + specialty.getName());
        
        request.setAttribute("success", "Especialidad registrada exitosamente: " + specialty.getName());
        
        // Redirigir para evitar reenvío del formulario
        System.out.println("Redirigiendo a: " + request.getContextPath() + "/admin/specialties-list?success=registered");
        response.sendRedirect(request.getContextPath() + "/admin/specialties-list?success=registered");
    }
    
    private void updateSpecialty(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== updateSpecialty ===");
        
        Long id = Long.valueOf(request.getParameter("id"));
        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String description = request.getParameter("description");
        
        System.out.println("ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Code: " + code);
        System.out.println("Description: " + description);
        
        Specialty specialty = specialtyService.updateSpecialty(id, name, code, description);
        
        System.out.println("Especialidad actualizada: " + specialty.getName());
        
        request.setAttribute("success", "Especialidad actualizada exitosamente: " + specialty.getName());
        response.sendRedirect(request.getContextPath() + "/admin/specialties-list?success=updated");
    }
    
    private void deleteSpecialty(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Long id = Long.valueOf(request.getParameter("id"));
        
        specialtyService.deleteSpecialty(id);
        
        request.setAttribute("success", "Especialidad eliminada exitosamente");
        response.sendRedirect(request.getContextPath() + "/admin/specialties-list?success=deleted");
    }
    
    private void toggleSpecialtyStatus(HttpServletRequest request, HttpServletResponse response, boolean activate) 
            throws ServletException, IOException {
        
        System.out.println("=== toggleSpecialtyStatus ===");
        System.out.println("Activate: " + activate);
        
        Long id = Long.valueOf(request.getParameter("id"));
        
        if (activate) {
            specialtyService.activateSpecialty(id);
            System.out.println("Especialidad activada: " + id);
        } else {
            specialtyService.deactivateSpecialty(id);
            System.out.println("Especialidad desactivada: " + id);
        }
        
        response.sendRedirect(request.getContextPath() + "/admin/specialties-list?success=" + 
                            (activate ? "activated" : "deactivated"));
    }
}

