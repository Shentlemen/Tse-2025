package uy.gub.clinic.web;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.User;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.entity.Professional;
import uy.gub.clinic.entity.Patient;
import uy.gub.clinic.service.UserService;
import uy.gub.clinic.service.ClinicService;
import uy.gub.clinic.service.ProfessionalService;
import uy.gub.clinic.service.PatientService;
import uy.gub.clinic.util.PasswordUtil;

import java.io.IOException;
import java.util.List;

/**
 * Servlet para gestión del Super Administrador
 */
public class SuperAdminServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(SuperAdminServlet.class);
    
    @Inject
    private UserService userService;
    
    @Inject
    private ClinicService clinicService;
    
    @Inject
    private ProfessionalService professionalService;
    
    @Inject
    private PatientService patientService;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Verificar que el usuario esté autenticado y sea super administrador
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        if (!"SUPER_ADMIN".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado. Solo super administradores.");
            return;
        }
        
        try {
            // Obtener todas las clínicas
            List<Clinic> clinics = clinicService.getAllClinics();
            request.setAttribute("clinics", clinics);
            
            // Obtener todos los usuarios
            List<User> users = userService.findAllActive();
            request.setAttribute("users", users);
            
            // Calcular estadísticas
            int totalClinics = clinics.size();
            int totalUsers = users.size();
            int adminUsers = (int) users.stream().filter(u -> "ADMIN_CLINIC".equals(u.getRole())).count();
            int professionalUsers = (int) professionalService.getAllProfessionals().stream().filter(p -> p.getActive()).count();
            
            request.setAttribute("totalClinics", totalClinics);
            request.setAttribute("totalUsers", totalUsers);
            request.setAttribute("adminUsers", adminUsers);
            request.setAttribute("professionalUsers", professionalUsers);
            
            // Obtener todos los pacientes
            List<Patient> patients = patientService.getAllPatients();
            request.setAttribute("patients", patients);
            int activePatients = (int) patients.stream().filter(p -> p.getActive()).count();
            request.setAttribute("activePatients", activePatients);
            
            request.getRequestDispatcher("/admin/super-admin.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Error al cargar página de super administrador", e);
            request.setAttribute("error", "Error al cargar la información del sistema");
            request.getRequestDispatcher("/admin/super-admin.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Verificar que el usuario esté autenticado y sea super administrador
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        if (!"SUPER_ADMIN".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado. Solo super administradores.");
            return;
        }
        
        String action = request.getParameter("action");
        logger.info("SuperAdminServlet POST - Acción recibida: '{}'", action);
        
        if ("createClinicAdmin".equals(action)) {
            logger.info("Procesando creación de administrador de clínica");
            handleCreateClinicAdmin(request, response);
        } else if ("updateClinicAdmin".equals(action)) {
            logger.info("Procesando actualización de administrador de clínica");
            handleUpdateClinicAdmin(request, response);
        } else {
            logger.error("Acción no válida recibida: '{}'", action);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no válida: " + action);
        }
    }
    
    private void handleCreateClinicAdmin(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String email = request.getParameter("email");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String clinicIdStr = request.getParameter("clinicId");
            
            // Validaciones básicas
            if (username == null || username.trim().isEmpty()) {
                request.setAttribute("error", "El nombre de usuario es requerido");
                doGet(request, response);
                return;
            }
            
            if (password == null || !PasswordUtil.isValidPassword(password)) {
                request.setAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                doGet(request, response);
                return;
            }
            
            if (clinicIdStr == null || clinicIdStr.trim().isEmpty()) {
                request.setAttribute("error", "La clínica es requerida");
                doGet(request, response);
                return;
            }

            String clinicId = clinicIdStr.trim();

            // Verificar que la clínica existe
            if (!clinicService.getClinicById(clinicId).isPresent()) {
                request.setAttribute("error", "La clínica seleccionada no existe");
                doGet(request, response);
                return;
            }
            
            // Verificar que no existe ya un administrador para esta clínica
            List<User> existingAdmins = userService.findByClinic(clinicId);
            boolean hasActiveAdmin = existingAdmins.stream()
                .anyMatch(user -> "ADMIN_CLINIC".equals(user.getRole()) && user.getActive());
            
            if (hasActiveAdmin) {
                request.setAttribute("error", "Esta clínica ya tiene un administrador activo");
                doGet(request, response);
                return;
            }
            
            // Crear el administrador de clínica
            User admin = new User();
            admin.setUsername(username.trim());
            admin.setPassword(PasswordUtil.hashPassword(password)); // Hash correcto con BCrypt
            admin.setEmail(email != null ? email.trim() : null);
            admin.setFirstName(firstName != null ? firstName.trim() : null);
            admin.setLastName(lastName != null ? lastName.trim() : null);
            admin.setRole("ADMIN_CLINIC");
            admin.setActive(true);
            
            // Asociar con la clínica
            Clinic clinic = clinicService.getClinicById(clinicId).get();
            admin.setClinic(clinic);
            
            // Crear el usuario
            userService.createUser(admin);
            
            logger.info("Super administrador creó administrador de clínica: {} para clínica: {}", 
                       username, clinic.getName());
            
            request.setAttribute("success", 
                "Administrador creado exitosamente para " + clinic.getName() + ". " +
                "El usuario puede iniciar sesión con: " + username);
            
            // Recargar datos y mostrar página
            reloadDataAndForward(request, response);
            
        } catch (NumberFormatException e) {
            request.setAttribute("error", "ID de clínica inválido");
            doGet(request, response);
        } catch (Exception e) {
            logger.error("Error al crear administrador de clínica", e);
            request.setAttribute("error", "Error al crear administrador: " + e.getMessage());
            doGet(request, response);
        }
    }
    
    private void handleUpdateClinicAdmin(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String userIdStr = request.getParameter("userId");
            String email = request.getParameter("email");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String password = request.getParameter("password");
            
            // Validaciones básicas
            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                request.setAttribute("error", "ID de usuario requerido");
                doGet(request, response);
                return;
            }
            
            Long userId = Long.parseLong(userIdStr);
            
            // Buscar el usuario
            if (!userService.findById(userId).isPresent()) {
                request.setAttribute("error", "Usuario no encontrado");
                doGet(request, response);
                return;
            }
            
            User user = userService.findById(userId).get();
            
            // Verificar que es un administrador de clínica
            if (!"ADMIN_CLINIC".equals(user.getRole())) {
                request.setAttribute("error", "Solo se pueden editar administradores de clínica");
                doGet(request, response);
                return;
            }
            
            // Actualizar campos
            if (email != null && !email.trim().isEmpty()) {
                user.setEmail(email.trim());
            }
            
            if (firstName != null && !firstName.trim().isEmpty()) {
                user.setFirstName(firstName.trim());
            }
            
            if (lastName != null && !lastName.trim().isEmpty()) {
                user.setLastName(lastName.trim());
            }
            
            // Actualizar contraseña si se proporciona
            if (password != null && !password.trim().isEmpty()) {
                if (PasswordUtil.isValidPassword(password)) {
                    user.setPassword(PasswordUtil.hashPassword(password));
                } else {
                    request.setAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                    doGet(request, response);
                    return;
                }
            }
            
            // Guardar cambios
            userService.updateUser(user);
            
            logger.info("Super administrador actualizó administrador de clínica: {} (ID: {})", 
                       user.getUsername(), userId);
            
            request.setAttribute("success", 
                "Administrador actualizado exitosamente: " + user.getUsername());
            
            // Recargar datos y mostrar página
            reloadDataAndForward(request, response);
            
        } catch (NumberFormatException e) {
            request.setAttribute("error", "ID de usuario inválido");
            doGet(request, response);
        } catch (Exception e) {
            logger.error("Error al actualizar administrador de clínica", e);
            request.setAttribute("error", "Error al actualizar administrador: " + e.getMessage());
            doGet(request, response);
        }
    }
    
    private void reloadDataAndForward(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // Obtener todas las clínicas
            List<Clinic> clinics = clinicService.getAllClinics();
            request.setAttribute("clinics", clinics);
            
            // Obtener todos los usuarios
            List<User> users = userService.findAllActive();
            request.setAttribute("users", users);
            
            // Calcular estadísticas
            int totalClinics = clinics.size();
            int totalUsers = users.size();
            int adminUsers = (int) users.stream().filter(u -> "ADMIN_CLINIC".equals(u.getRole())).count();
            int professionalUsers = (int) professionalService.getAllProfessionals().stream().filter(p -> p.getActive()).count();
            
            request.setAttribute("totalClinics", totalClinics);
            request.setAttribute("totalUsers", totalUsers);
            request.setAttribute("adminUsers", adminUsers);
            request.setAttribute("professionalUsers", professionalUsers);
            
            // Obtener todos los pacientes
            List<Patient> patients = patientService.getAllPatients();
            request.setAttribute("patients", patients);
            int activePatients = (int) patients.stream().filter(p -> p.getActive()).count();
            request.setAttribute("activePatients", activePatients);
            
            request.getRequestDispatcher("/admin/super-admin.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Error al recargar datos", e);
            request.setAttribute("error", "Error al recargar la información del sistema");
            request.getRequestDispatcher("/admin/super-admin.jsp").forward(request, response);
        }
    }
}
