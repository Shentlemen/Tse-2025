package uy.gub.clinic.web;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.entity.Professional;
import uy.gub.clinic.entity.Specialty;
import uy.gub.clinic.entity.User;
import uy.gub.clinic.service.ProfessionalService;
import uy.gub.clinic.service.SpecialtyService;
import uy.gub.clinic.service.UserService;
import uy.gub.clinic.util.PasswordUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servlet para manejar las operaciones CRUD de profesionales
 */
@WebServlet("/admin/professionals")
public class ProfessionalServlet extends HttpServlet {

    @Inject
    private ProfessionalService professionalService;
    
    @Inject
    private SpecialtyService specialtyService;
    
    @Inject
    private UserService userService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configurar codificación UTF-8
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        System.out.println("=== ProfessionalServlet.doGet ===");
        System.out.println("ProfessionalService inyectado: " + (professionalService != null ? "SÍ" : "NO"));

        try {
            // Obtener la clínica del usuario logueado
            Long clinicId = (Long) request.getSession().getAttribute("clinicId");
            if (clinicId == null) {
                System.out.println("ERROR: No se encontró clinicId en la sesión");
                request.setAttribute("error", "Error de sesión: Clínica no identificada");
                request.getRequestDispatcher("/admin/professionals.jsp").forward(request, response);
                return;
            }
            
            // Obtener solo los profesionales de la clínica del usuario
            List<Professional> professionals = professionalService.getProfessionalsByClinic(clinicId);
            System.out.println("Profesionales obtenidos de la clínica " + clinicId + ": " + professionals.size());

            for (Professional prof : professionals) {
                System.out.println("- " + prof.getFullName() + " (" + prof.getLicenseNumber() + ")");
            }

            request.setAttribute("professionals", professionals);
            
            // Cargar especialidades de la clínica para el formulario
            System.out.println("DEBUG: Obteniendo especialidades para clínica ID: " + clinicId);
            // Las especialidades ahora son globales (sin filtrar por clínica)
            List<Specialty> specialties = specialtyService.getAllSpecialties();
            request.setAttribute("specialties", specialties);
            System.out.println("Especialidades cargadas para clínica " + clinicId + ": " + specialties.size());
            
            // Debug: mostrar cada especialidad
            for (Specialty specialty : specialties) {
                System.out.println("  - " + specialty.getName() + " (ID: " + specialty.getId() + 
                                 ", Clínica: " + (specialty.getClinic() != null ? specialty.getClinic().getName() : "NULL") + ")");
            }
            
        } catch (Exception e) {
            System.out.println("Error al obtener profesionales: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Error al cargar profesionales: " + e.getMessage());
        }

        // Redirigir a la página de gestión de profesionales
        request.getRequestDispatcher("/admin/professionals.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Configurar codificación UTF-8
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        
        System.out.println("=== ProfessionalServlet.doPost ===");
        System.out.println("Action: " + request.getParameter("action"));
        System.out.println("Name: " + request.getParameter("name"));
        System.out.println("Last Name: " + request.getParameter("lastName"));
        System.out.println("Email: " + request.getParameter("email"));
        System.out.println("License: " + request.getParameter("licenseNumber"));
        System.out.println("Phone: " + request.getParameter("phone"));
        System.out.println("Clinic ID: " + request.getParameter("clinicId"));
        System.out.println("Specialty ID: " + request.getParameter("specialtyId"));
        
        String action = request.getParameter("action");
        
        try {
            switch (action) {
                case "register":
                    registerProfessional(request, response);
                    break;
                case "update":
                    updateProfessional(request, response);
                    break;
                case "delete":
                    deleteProfessional(request, response);
                    break;
                case "activate":
                    toggleProfessionalStatus(request, response, true);
                    break;
                case "deactivate":
                    toggleProfessionalStatus(request, response, false);
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
    
    private void registerProfessional(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== registerProfessional ===");
        
        String name = request.getParameter("name");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String licenseNumber = request.getParameter("licenseNumber");
        String phone = request.getParameter("phone");
        
        // Obtener clinicId de la sesión o del request
        Long clinicId = (Long) request.getSession().getAttribute("clinicId");
        if (clinicId == null) {
            String clinicIdStr = request.getParameter("clinicId");
            if (clinicIdStr != null && !clinicIdStr.trim().isEmpty()) {
                clinicId = Long.valueOf(clinicIdStr);
            }
        }
        
        Long specialtyId = Long.valueOf(request.getParameter("specialtyId"));
        
        System.out.println("Datos recibidos:");
        System.out.println("- Name: " + name);
        System.out.println("- LastName: " + lastName);
        System.out.println("- Email: " + email);
        System.out.println("- License: " + licenseNumber);
        System.out.println("- Phone: " + phone);
        System.out.println("- ClinicId: " + clinicId);
        System.out.println("- SpecialtyId: " + specialtyId);
        
        // Validaciones básicas
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (clinicId == null) {
            throw new IllegalArgumentException("Debe seleccionar una clínica");
        }
        if (specialtyId == null) {
            throw new IllegalArgumentException("Debe seleccionar una especialidad");
        }
        
        System.out.println("Llamando a professionalService.registerProfessional...");
        
        Professional professional = professionalService.registerProfessional(
            name, lastName, email, licenseNumber, phone, clinicId, specialtyId);
        
        System.out.println("Profesional registrado: " + professional.getFullName());
        
        // Crear usuario para el profesional
        try {
            System.out.println("Creando usuario para el profesional con matrícula: " + licenseNumber);
            
            // Generar password por defecto (puedes cambiarlo por lo que prefieras)
            String defaultPassword = "prof123"; // Password por defecto
            String hashedPassword = PasswordUtil.hashPassword(defaultPassword);
            
            System.out.println("Password original: " + defaultPassword);
            System.out.println("Password hasheado: " + hashedPassword);
            
            // Crear usuario
            User user = new User();
            user.setUsername(licenseNumber);  // username = matrícula
            user.setPassword(hashedPassword); // password hasheado
            user.setRole("PROFESSIONAL"); // role
            user.setEmail(email);
            user.setFirstName(name);
            user.setLastName(lastName);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            
            // Asociar con clínica
            Clinic clinic = new Clinic();
            clinic.setId(clinicId);
            user.setClinic(clinic);
            
            // Asociar con profesional
            user.setProfessional(professional);
            
            User createdUser = userService.createUser(user);
            
            System.out.println("Usuario creado para profesional: " + createdUser.getUsername());
            
            request.setAttribute("success", 
                "Profesional registrado exitosamente: " + professional.getFullName() + 
                ". Usuario creado con matrícula: " + licenseNumber + " y password: " + defaultPassword);
                
        } catch (Exception e) {
            System.out.println("Error al crear usuario para el profesional: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("success", 
                "Profesional registrado exitosamente: " + professional.getFullName() + 
                ". Error al crear usuario: " + e.getMessage());
        }
        
        // Redirigir para evitar reenvío del formulario
        System.out.println("Redirigiendo a: " + request.getContextPath() + "/admin/professionals?success=registered");
        response.sendRedirect(request.getContextPath() + "/admin/professionals?success=registered");
    }
    
    private void updateProfessional(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== updateProfessional ===");
        
        Long id = Long.valueOf(request.getParameter("id"));
        String name = request.getParameter("name");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String licenseNumber = request.getParameter("licenseNumber");
        String phone = request.getParameter("phone");
        Long specialtyId = Long.valueOf(request.getParameter("specialtyId"));
        
        System.out.println("ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("LastName: " + lastName);
        System.out.println("Email: " + email);
        System.out.println("License: " + licenseNumber);
        System.out.println("Phone: " + phone);
        System.out.println("SpecialtyId: " + specialtyId);
        
        Professional professional = professionalService.updateProfessional(
            id, name, lastName, email, licenseNumber, phone, specialtyId);
        
        System.out.println("Profesional actualizado: " + professional.getFullName());
        System.out.println("Nueva especialidad: " + (professional.getSpecialty() != null ? professional.getSpecialty().getName() : "NULL"));
        
        request.setAttribute("success", "Profesional actualizado exitosamente: " + professional.getFullName());
        response.sendRedirect(request.getContextPath() + "/admin/professionals?success=updated");
    }
    
    private void deleteProfessional(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Long id = Long.valueOf(request.getParameter("id"));
        String deleteType = request.getParameter("deleteType");
        
        if ("soft".equals(deleteType)) {
            professionalService.deactivateProfessional(id);
            request.setAttribute("success", "Profesional desactivado exitosamente");
        } else {
            professionalService.deleteProfessional(id);
            request.setAttribute("success", "Profesional eliminado permanentemente");
        }
        
        response.sendRedirect(request.getContextPath() + "/admin/professionals?success=deleted");
    }
    
    private void toggleProfessionalStatus(HttpServletRequest request, HttpServletResponse response, boolean activate) 
            throws ServletException, IOException {
        
        System.out.println("=== toggleProfessionalStatus ===");
        System.out.println("Activate: " + activate);
        
        Long id = Long.valueOf(request.getParameter("id"));
        
        if (activate) {
            professionalService.activateProfessional(id);
            System.out.println("Profesional activado: " + id);
        } else {
            professionalService.deactivateProfessional(id);
            System.out.println("Profesional desactivado: " + id);
        }
        
        response.sendRedirect(request.getContextPath() + "/admin/professionals?success=" + 
                            (activate ? "activated" : "deactivated"));
    }
}
