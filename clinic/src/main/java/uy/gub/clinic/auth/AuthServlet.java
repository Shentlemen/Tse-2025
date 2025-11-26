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
import uy.gub.clinic.service.UserService;
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
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        if (requestURI.endsWith("/login")) {
            handleLogin(request, response);
        }
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
            boolean loginExitoso = false;
            String role = null;
            String clinicId = null;
            String clinicName = null;
            Long professionalId = null;
            
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Verificar si el usuario está activo
                if (!user.getActive()) {
                    logger.warn("Intento de login con usuario inactivo: {}", username);
                    request.setAttribute("error", "Usuario desactivado. Contacte al administrador.");
                    request.getRequestDispatcher("/index.jsp").forward(request, response);
                    return;
                }
                
                // Verificar contraseña con BCrypt
                boolean passwordMatch = false;
                
                // Permitir password vacío si el hash en BD está vacío o es NULL
                if ((user.getPassword() == null || user.getPassword().trim().isEmpty()) && 
                    (password == null || password.trim().isEmpty())) {
                    passwordMatch = true;
                } else if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                    logger.warn("Usuario sin password configurado en BD: {}", username);
                    passwordMatch = false;
                } else {
                    passwordMatch = PasswordUtil.verifyPassword(password, user.getPassword());
                }
                
                if (passwordMatch) {
                    loginExitoso = true;
                    role = user.getRole();
                    clinicId = user.getClinic() != null ? user.getClinic().getId() : null;
                    clinicName = user.getClinic() != null ? user.getClinic().getName() : "Sistema";
                    professionalId = user.getProfessional() != null ? user.getProfessional().getId() : null;
                    
                    if (clinicId == null) {
                        logger.warn("Usuario {} logueado sin clínica asignada", username);
                    }
                    
                    logger.info("Login exitoso con usuario de BD: {} ({}) - Clínica: {}", username, role, clinicName);
                    
                    // Actualizar último login
                    userService.updateLastLogin(username);
                }
            }
            
            if (!loginExitoso) {
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
