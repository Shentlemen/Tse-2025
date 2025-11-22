package uy.gub.clinic.auth;

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
import uy.gub.clinic.service.UserService;
import uy.gub.clinic.service.ClinicService;
import uy.gub.clinic.util.PasswordUtil;

import java.io.IOException;
import java.util.Optional;

/**
 * Servlet para manejo de autenticación basada en base de datos
 */
public class AuthServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);
    
    @Inject
    private UserService userService;

    @Inject
    private ClinicService clinicService;
    
    // Usuarios hardcodeados para desarrollo (TEMPORAL)
    // Estos se usarán hasta que el sistema interno de gestión esté listo
    private static final String SUPER_ADMIN_USER = "superadmin";
    private static final String SUPER_ADMIN_PASS = "super123";
    
    private static final String ADMIN_USER_CLINIC1 = "admin";
    private static final String ADMIN_PASS_CLINIC1 = "admin123";
    private static final String PROF_USER_CLINIC1 = "prof";
    private static final String PROF_PASS_CLINIC1 = "prof123";
    
    private static final String ADMIN_USER_CLINIC2 = "admin2";
    private static final String ADMIN_PASS_CLINIC2 = "admin456";
    private static final String PROF_USER_CLINIC2 = "prof2";
    private static final String PROF_PASS_CLINIC2 = "prof456";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        if (requestURI.endsWith("/login")) {
            handleLogin(request, response);
        }
    }

    private String resolveClinicId(String clinicName, String fallbackId) {
        if (clinicService != null && clinicName != null) {
            try {
                return clinicService.getAllClinics().stream()
                        .filter(c -> clinicName.equalsIgnoreCase(c.getName()))
                        .map(Clinic::getId)
                        .findFirst()
                        .orElse(fallbackId);
            } catch (Exception ex) {
                logger.warn("No se pudo resolver la clínica '{}' desde la BD, usando fallback {}", clinicName, fallbackId, ex);
            }
        }
        return fallbackId;
    }

    private String getClinicName(String clinicId, String fallbackName) {
        if (clinicService != null && clinicId != null) {
            try {
                return clinicService.getClinicById(clinicId)
                        .map(Clinic::getName)
                        .orElse(fallbackName);
            } catch (Exception ex) {
                logger.warn("No se pudo obtener el nombre para la clínica {}, usando fallback {}", clinicId, fallbackName, ex);
            }
        }
        return fallbackName;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        if (requestURI.endsWith("/logout")) {
            handleLogout(request, response);
        } else if (requestURI.endsWith("/login")) {
            // Si alguien accede directamente a /auth/login, redirigir a la página principal
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
    
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        logger.info("Intento de login para usuario: {}", username);
        
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            request.setAttribute("error", "Usuario y contraseña son requeridos");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            return;
        }
        
        // Obtener o crear sesión
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30 * 60); // 30 minutos
        
        // Limpiar atributos anteriores si existen
        session.removeAttribute("user");
        session.removeAttribute("role");
        session.removeAttribute("clinicId");
        session.removeAttribute("clinicName");
        session.removeAttribute("professionalId");
        session.removeAttribute("userId");
        
        logger.info("Sesión preparada con ID: {}", session.getId());
        
        try {
            // SISTEMA HÍBRIDO: Primero verificar usuarios hardcodeados (TEMPORAL)
            // Luego verificar base de datos para usuarios creados internamente
            
            boolean loginExitoso = false;
            String role = null;
            String clinicId = null;
            String clinicName = null;
            Long professionalId = null;

            // Verificar usuarios hardcodeados primero
            if (SUPER_ADMIN_USER.equals(username) && SUPER_ADMIN_PASS.equals(password)) {
                loginExitoso = true;
                role = "SUPER_ADMIN";
                clinicId = "clinic-00000000-0000-0000-0000-000000000000"; // ID especial para super admin (acceso completo)
                clinicName = "Super Administrador - Acceso Completo";
                professionalId = null;
                logger.info("Login exitoso con super administrador: {}", username);

            } else if (ADMIN_USER_CLINIC1.equals(username) && ADMIN_PASS_CLINIC1.equals(password)) {
                loginExitoso = true;
                role = "ADMIN_CLINIC";
                clinicId = resolveClinicId("Clínica del Corazón", "clinic-00000000-0000-0000-0000-000000000004");
                clinicName = getClinicName(clinicId, "Clínica del Corazón");
                professionalId = null;
                logger.info("Login exitoso con usuario hardcodeado: {} (Clínica del Corazón)", username);

            } else if (PROF_USER_CLINIC1.equals(username) && PROF_PASS_CLINIC1.equals(password)) {
                loginExitoso = true;
                role = "PROFESSIONAL";
                clinicId = resolveClinicId("Clínica del Corazón", "clinic-00000000-0000-0000-0000-000000000004");
                clinicName = getClinicName(clinicId, "Clínica del Corazón");
                professionalId = 1L;
                logger.info("Login exitoso con usuario hardcodeado: {} (Clínica del Corazón)", username);

            } else if (ADMIN_USER_CLINIC2.equals(username) && ADMIN_PASS_CLINIC2.equals(password)) {
                loginExitoso = true;
                role = "ADMIN_CLINIC";
                clinicId = resolveClinicId("Centro Neurológico", "clinic-00000000-0000-0000-0000-000000000005");
                clinicName = getClinicName(clinicId, "Centro Neurológico");
                professionalId = null;
                logger.info("Login exitoso con usuario hardcodeado: {} (Centro Neurológico)", username);

            } else if (PROF_USER_CLINIC2.equals(username) && PROF_PASS_CLINIC2.equals(password)) {
                loginExitoso = true;
                role = "PROFESSIONAL";
                clinicId = resolveClinicId("Centro Neurológico", "clinic-00000000-0000-0000-0000-000000000005");
                clinicName = getClinicName(clinicId, "Centro Neurológico");
                professionalId = 2L;
                logger.info("Login exitoso con usuario hardcodeado: {} (Centro Neurológico)", username);
                
            } else {
                // Si no es usuario hardcodeado, verificar en base de datos
                System.out.println("=== DEBUG LOGIN ===");
                System.out.println("Username ingresado: " + username);
                System.out.println("Password ingresado: " + password);
                
                Optional<User> userOpt = userService.findByUsername(username);
                
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    System.out.println("Usuario encontrado en BD:");
                    System.out.println("  - ID: " + user.getId());
                    System.out.println("  - Username: " + user.getUsername());
                    System.out.println("  - Role: " + user.getRole());
                    System.out.println("  - Active: " + user.getActive());
                    System.out.println("  - Email: " + user.getEmail());
                    System.out.println("  - Password hash: " + user.getPassword());
                    System.out.println("  - Clinic: " + (user.getClinic() != null ? user.getClinic().getName() : "NULL"));
                    System.out.println("  - Professional: " + (user.getProfessional() != null ? user.getProfessional().getFullName() : "NULL"));
                    
                    // Verificar si el usuario está activo
                    if (!user.getActive()) {
                        System.out.println("ERROR: Usuario inactivo");
                        logger.warn("Intento de login con usuario inactivo: {}", username);
                        request.setAttribute("error", "Usuario desactivado. Contacte al administrador.");
                        request.getRequestDispatcher("/index.jsp").forward(request, response);
                        return;
                    }
                    
                    // Verificar contraseña con BCrypt
                    System.out.println("Verificando contraseña...");
                    boolean passwordMatch = PasswordUtil.verifyPassword(password, user.getPassword());
                    System.out.println("Password match: " + passwordMatch);
                    
                    if (passwordMatch) {
                        loginExitoso = true;
                        role = user.getRole();
                        clinicId = user.getClinic() != null ? user.getClinic().getId() : null;
                        clinicName = user.getClinic() != null ? user.getClinic().getName() : "Sistema";
                        professionalId = user.getProfessional() != null ? user.getProfessional().getId() : null;
                        logger.info("Login exitoso con usuario de BD: {} ({})", username, role);
                        
                        // Actualizar último login
                        userService.updateLastLogin(username);
                    } else {
                        System.out.println("ERROR: Contraseña incorrecta");
                    }
                } else {
                    System.out.println("ERROR: Usuario no encontrado en BD");
                }
            }
            
            if (!loginExitoso) {
                System.out.println("=== LOGIN FALLIDO ===");
                logger.warn("Intento de login fallido para usuario: {}", username);
                request.setAttribute("error", "Usuario o contraseña incorrectos");
                request.getRequestDispatcher("/index.jsp").forward(request, response);
                return;
            }
            
            // Login exitoso - configurar sesión
            session.setAttribute("user", username);
            session.setAttribute("role", role);
            session.setAttribute("clinicId", clinicId);
            session.setAttribute("clinicName", clinicName);
            session.setAttribute("professionalId", professionalId);
            
            // Configurar redirección según el rol
            if ("SUPER_ADMIN".equals(role)) {
                logger.info("Redirigiendo super administrador a dashboard");
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                
            } else if ("ADMIN_CLINIC".equals(role)) {
                logger.info("Redirigiendo administrador de clínica {} a dashboard", clinicName);
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                
            } else if ("PROFESSIONAL".equals(role)) {
                logger.info("Redirigiendo profesional de clínica {} a dashboard", clinicName);
                response.sendRedirect(request.getContextPath() + "/professional/dashboard");
                
            } else {
                logger.error("Rol no reconocido: {}", role);
                request.setAttribute("error", "Error de configuración del usuario. Contacte al administrador.");
                request.getRequestDispatcher("/index.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            logger.error("Error durante el proceso de login para usuario: {}", username, e);
            request.setAttribute("error", "Error interno del servidor. Intente nuevamente.");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }
    
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            String user = (String) session.getAttribute("user");
            String clinicName = (String) session.getAttribute("clinicName");
            logger.info("Logout para usuario: {} de clínica: {}", user, clinicName);
            
            // Solo limpiar los atributos de sesión, no invalidar la sesión completa
            session.removeAttribute("user");
            session.removeAttribute("role");
            session.removeAttribute("clinicId");
            session.removeAttribute("clinicName");
            session.removeAttribute("professionalId");
            session.removeAttribute("userId");
        }
        
        logger.info("Atributos de sesión limpiados, redirigiendo a login");
        response.sendRedirect(request.getContextPath() + "/");
    }
}
