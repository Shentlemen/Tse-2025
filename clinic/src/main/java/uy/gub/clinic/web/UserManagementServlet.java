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
import uy.gub.clinic.service.UserService;
import uy.gub.clinic.service.ClinicService;
import uy.gub.clinic.service.ProfessionalService;
import uy.gub.clinic.util.PasswordUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servlet para gestión de usuarios del sistema
 */
public class UserManagementServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(UserManagementServlet.class);
    
    @Inject
    private UserService userService;
    
    @Inject
    private ClinicService clinicService;
    
    @Inject
    private ProfessionalService professionalService;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Verificar que el usuario esté autenticado y sea administrador
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        if (!"ADMIN_CLINIC".equals(role) && !"SUPER_ADMIN".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado");
            return;
        }
        
        String action = request.getParameter("action");
        
        if ("list".equals(action)) {
            handleListUsers(request, response);
        } else if ("create".equals(action)) {
            handleCreateUserForm(request, response);
        } else if ("edit".equals(action)) {
            handleEditUserForm(request, response);
        } else if ("setup".equals(action)) {
            handleSetupForm(request, response);
        } else {
            // Por defecto, mostrar lista de usuarios
            handleListUsers(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("create".equals(action)) {
            handleCreateUser(request, response);
        } else if ("update".equals(action)) {
            handleUpdateUser(request, response);
        } else if ("toggleStatus".equals(action)) {
            handleToggleUserStatus(request, response);
        } else if ("deactivate".equals(action)) {
            handleDeactivateUser(request, response);
        } else if ("changePassword".equals(action)) {
            handleChangePassword(request, response);
        } else if ("setupSuperAdmin".equals(action)) {
            handleSetupSuperAdmin(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no válida");
        }
    }
    
    private void handleListUsers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            HttpSession session = request.getSession(false);
            Long clinicId = null;
            String role = null;
            
            if (session != null) {
                clinicId = (Long) session.getAttribute("clinicId");
                role = (String) session.getAttribute("role");
            }
            
            System.out.println("=== UserManagementServlet.handleListUsers ===");
            System.out.println("ClinicId desde sesión: " + clinicId);
            System.out.println("Role desde sesión: " + role);
            
            List<User> users;
            
            // Si es Super Admin (clinicId = 0), mostrar todos los usuarios
            if (clinicId != null && clinicId == 0L) {
                System.out.println("Es Super Admin - obteniendo todos los usuarios");
                users = userService.findAllActive();
            } else {
                // Para administradores normales, filtrar por clínica
                System.out.println("Es administrador normal - filtrando por clínica: " + clinicId);
                users = userService.findByClinic(clinicId);
            }
            
            System.out.println("Usuarios obtenidos: " + users.size());
            
            // Debug de usuarios (las relaciones ya están cargadas con JOIN FETCH)
            for (User user : users) {
                System.out.println("  - " + user.getUsername() + " (" + user.getRole() + ", Clínica: " + 
                                 (user.getClinic() != null ? user.getClinic().getName() : "NULL") + ")");
            }
            
            request.setAttribute("users", users);
            
            // Obtener clínicas para el formulario de creación
            List<Clinic> clinics = clinicService.getAllClinics();
            request.setAttribute("clinics", clinics);
            
            request.getRequestDispatcher("/admin/user-management.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Error al obtener lista de usuarios", e);
            request.setAttribute("error", "Error al cargar la lista de usuarios");
            request.getRequestDispatcher("/admin/user-management.jsp").forward(request, response);
        }
    }
    
    private void handleCreateUserForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            List<Clinic> clinics = clinicService.getAllClinics();
            request.setAttribute("clinics", clinics);
            
            request.getRequestDispatcher("/admin/create-user.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Error al cargar formulario de creación de usuario", e);
            request.setAttribute("error", "Error al cargar el formulario");
            request.getRequestDispatcher("/admin/user-management.jsp").forward(request, response);
        }
    }
    
    private void handleEditUserForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String userIdStr = request.getParameter("userId");
            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de usuario requerido");
                return;
            }
            
            Long userId = Long.parseLong(userIdStr);
            Optional<User> userOpt = userService.findById(userId);
            
            if (userOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Usuario no encontrado");
                return;
            }
            
            request.setAttribute("user", userOpt.get());
            
            List<Clinic> clinics = clinicService.getAllClinics();
            request.setAttribute("clinics", clinics);
            
            request.getRequestDispatcher("/admin/edit-user.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de usuario inválido");
        } catch (Exception e) {
            logger.error("Error al cargar formulario de edición de usuario", e);
            request.setAttribute("error", "Error al cargar el formulario");
            request.getRequestDispatcher("/admin/user-management.jsp").forward(request, response);
        }
    }
    
    private void handleSetupForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Verificar si ya existe un super administrador
            boolean hasSuperAdmin = userService.hasSuperAdmin();
            request.setAttribute("hasSuperAdmin", hasSuperAdmin);
            
            request.getRequestDispatcher("/admin/setup.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Error al cargar formulario de configuración", e);
            request.setAttribute("error", "Error al cargar el formulario de configuración");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }
    
    private void handleCreateUser(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String email = request.getParameter("email");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String role = request.getParameter("role");
            String clinicIdStr = request.getParameter("clinicId");
            String professionalIdStr = request.getParameter("professionalId");
            
            // Validaciones básicas
            if (username == null || username.trim().isEmpty()) {
                request.setAttribute("error", "El nombre de usuario es requerido");
                handleCreateUserForm(request, response);
                return;
            }
            
            if (password == null || !PasswordUtil.isValidPassword(password)) {
                request.setAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                handleCreateUserForm(request, response);
                return;
            }
            
            if (role == null || role.trim().isEmpty()) {
                request.setAttribute("error", "El rol es requerido");
                handleCreateUserForm(request, response);
                return;
            }
            
            // Crear usuario
            User user = new User();
            user.setUsername(username.trim());
            user.setPassword(PasswordUtil.hashPassword(password)); // Hash correcto con BCrypt
            user.setEmail(email != null ? email.trim() : null);
            user.setFirstName(firstName != null ? firstName.trim() : null);
            user.setLastName(lastName != null ? lastName.trim() : null);
            user.setRole(role);
            user.setActive(true);
            
            // Configurar clínica si es necesario
            if (clinicIdStr != null && !clinicIdStr.trim().isEmpty()) {
                try {
                    Long clinicId = Long.parseLong(clinicIdStr);
                    Optional<Clinic> clinic = clinicService.getClinicById(clinicId);
                    if (clinic.isPresent()) {
                        user.setClinic(clinic.get());
                    }
                } catch (NumberFormatException e) {
                    logger.warn("ID de clínica inválido: {}", clinicIdStr);
                }
            }
            
            // Configurar profesional si es necesario
            if (professionalIdStr != null && !professionalIdStr.trim().isEmpty()) {
                try {
                    Long professionalId = Long.parseLong(professionalIdStr);
                    Optional<Professional> professional = professionalService.getProfessionalById(professionalId);
                    if (professional.isPresent()) {
                        user.setProfessional(professional.get());
                    }
                } catch (NumberFormatException e) {
                    logger.warn("ID de profesional inválido: {}", professionalIdStr);
                }
            }
            
            userService.createUser(user);
            
            request.setAttribute("success", "Usuario creado exitosamente");
            response.sendRedirect(request.getContextPath() + "/admin/users?action=list");
            
        } catch (Exception e) {
            logger.error("Error al crear usuario", e);
            request.setAttribute("error", "Error al crear usuario: " + e.getMessage());
            handleCreateUserForm(request, response);
        }
    }
    
    private void handleUpdateUser(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String userIdStr = request.getParameter("id"); // Cambiar de "userId" a "id"
            String email = request.getParameter("email");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String password = request.getParameter("password");
            String activeStr = request.getParameter("active");
            
            System.out.println("=== handleUpdateUser ===");
            System.out.println("ID: " + userIdStr);
            System.out.println("Email: " + email);
            System.out.println("FirstName: " + firstName);
            System.out.println("LastName: " + lastName);
            System.out.println("Password provided: " + (password != null && !password.isEmpty()));
            System.out.println("Active: " + activeStr);
            
            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de usuario requerido");
                return;
            }
            
            Long userId = Long.parseLong(userIdStr);
            Optional<User> userOpt = userService.findById(userId);
            
            if (userOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Usuario no encontrado");
                return;
            }
            
            User user = userOpt.get();
            
            // Actualizar datos básicos
            if (email != null && !email.trim().isEmpty()) {
                user.setEmail(email.trim());
            }
            if (firstName != null && !firstName.trim().isEmpty()) {
                user.setFirstName(firstName.trim());
            }
            if (lastName != null && !lastName.trim().isEmpty()) {
                user.setLastName(lastName.trim());
            }
            
            // Actualizar contraseña si se proporcionó
            if (password != null && !password.trim().isEmpty()) {
                String hashedPassword = PasswordUtil.hashPassword(password);
                user.setPassword(hashedPassword);
                System.out.println("Password actualizada para usuario: " + user.getUsername());
            }
            
            // Actualizar estado
            if (activeStr != null) {
                boolean active = Boolean.parseBoolean(activeStr);
                user.setActive(active);
                System.out.println("Estado actualizado para usuario: " + user.getUsername() + " -> " + active);
            }
            
            user.setUpdatedAt(LocalDateTime.now());
            userService.updateUser(user);
            
            System.out.println("Usuario actualizado exitosamente: " + user.getUsername());
            request.setAttribute("success", "Usuario actualizado exitosamente: " + user.getUsername());
            response.sendRedirect(request.getContextPath() + "/admin/users?success=updated");
            
        } catch (NumberFormatException e) {
            logger.error("ID de usuario inválido", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de usuario inválido");
        } catch (Exception e) {
            logger.error("Error al actualizar usuario", e);
            request.setAttribute("error", "Error al actualizar usuario: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users?error=update");
        }
    }
    
    private void handleToggleUserStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String userIdStr = request.getParameter("userId");
            String activeStr = request.getParameter("active");
            
            System.out.println("=== handleToggleUserStatus ===");
            System.out.println("UserID: " + userIdStr);
            System.out.println("Active: " + activeStr);
            
            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de usuario requerido");
                return;
            }
            
            Long userId = Long.parseLong(userIdStr);
            boolean active = Boolean.parseBoolean(activeStr);
            
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Usuario no encontrado");
                return;
            }
            
            User user = userOpt.get();
            user.setActive(active);
            user.setUpdatedAt(LocalDateTime.now());
            userService.updateUser(user);
            
            System.out.println("Usuario " + (active ? "activado" : "desactivado") + " exitosamente: " + user.getUsername());
            
            String successMessage = active ? "Usuario activado exitosamente" : "Usuario desactivado exitosamente";
            response.sendRedirect(request.getContextPath() + "/admin/users?success=toggle");
            
        } catch (NumberFormatException e) {
            logger.error("ID de usuario inválido", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de usuario inválido");
        } catch (Exception e) {
            logger.error("Error al cambiar estado del usuario", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al cambiar estado del usuario");
        }
    }
    
    private void handleDeactivateUser(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String userIdStr = request.getParameter("userId");
            
            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de usuario requerido");
                return;
            }
            
            Long userId = Long.parseLong(userIdStr);
            userService.deactivateUser(userId);
            
            request.setAttribute("success", "Usuario desactivado exitosamente");
            response.sendRedirect(request.getContextPath() + "/admin/users?action=list");
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de usuario inválido");
        } catch (Exception e) {
            logger.error("Error al desactivar usuario", e);
            request.setAttribute("error", "Error al desactivar usuario: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users?action=list");
        }
    }
    
    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String userIdStr = request.getParameter("userId");
            String newPassword = request.getParameter("newPassword");
            
            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de usuario requerido");
                return;
            }
            
            if (newPassword == null || !PasswordUtil.isValidPassword(newPassword)) {
                request.setAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                response.sendRedirect(request.getContextPath() + "/admin/users?action=list");
                return;
            }
            
            Long userId = Long.parseLong(userIdStr);
            userService.changePassword(userId, PasswordUtil.hashPassword(newPassword));
            
            request.setAttribute("success", "Contraseña actualizada exitosamente");
            response.sendRedirect(request.getContextPath() + "/admin/users?action=list");
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de usuario inválido");
        } catch (Exception e) {
            logger.error("Error al cambiar contraseña", e);
            request.setAttribute("error", "Error al cambiar contraseña: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users?action=list");
        }
    }
    
    private void handleSetupSuperAdmin(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String email = request.getParameter("email");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            
            // Validaciones
            if (username == null || username.trim().isEmpty()) {
                request.setAttribute("error", "El nombre de usuario es requerido");
                handleSetupForm(request, response);
                return;
            }
            
            if (password == null || !PasswordUtil.isValidPassword(password)) {
                request.setAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                handleSetupForm(request, response);
                return;
            }
            
            // Crear super administrador
            userService.createSuperAdmin(
                username.trim(),
                PasswordUtil.hashPassword(password),
                email != null ? email.trim() : null,
                firstName != null ? firstName.trim() : null,
                lastName != null ? lastName.trim() : null
            );
            
            request.setAttribute("success", "Super administrador creado exitosamente. Ya puede iniciar sesión.");
            response.sendRedirect(request.getContextPath() + "/");
            
        } catch (Exception e) {
            logger.error("Error al crear super administrador", e);
            request.setAttribute("error", "Error al crear super administrador: " + e.getMessage());
            handleSetupForm(request, response);
        }
    }
}
