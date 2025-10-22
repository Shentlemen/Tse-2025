package uy.gub.clinic.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Servlet para manejo de autenticación simple
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/auth/login", "/auth/logout"})
public class AuthServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);
    
    // Usuarios hardcodeados para desarrollo
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";
    private static final String PROFESSIONAL_USER = "prof";
    private static final String PROFESSIONAL_PASS = "prof123";
    
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
        
        // Validación simple de usuarios hardcodeados
        if (ADMIN_USER.equals(username) && ADMIN_PASS.equals(password)) {
            // Usuario administrador
            HttpSession session = request.getSession(true);
            session.setAttribute("user", username);
            session.setAttribute("role", "ADMIN_CLINIC");
            session.setAttribute("clinicId", 1L); // Clínica por defecto
            session.setMaxInactiveInterval(30 * 60); // 30 minutos
            
            logger.info("Login exitoso para administrador: {}", username);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
            
        } else if (PROFESSIONAL_USER.equals(username) && PROFESSIONAL_PASS.equals(password)) {
            // Usuario profesional
            HttpSession session = request.getSession(true);
            session.setAttribute("user", username);
            session.setAttribute("role", "PROFESSIONAL");
            session.setAttribute("professionalId", 1L); // Profesional por defecto
            session.setAttribute("clinicId", 1L); // Clínica por defecto
            session.setMaxInactiveInterval(30 * 60); // 30 minutos
            
            logger.info("Login exitoso para profesional: {}", username);
            response.sendRedirect(request.getContextPath() + "/professional/dashboard.jsp");
            
        } else {
            // Credenciales inválidas
            request.setAttribute("error", "Usuario o contraseña incorrectos");
            logger.warn("Intento de login fallido para usuario: {}", username);
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }
    
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            String user = (String) session.getAttribute("user");
            logger.info("Logout para usuario: {}", user);
            session.invalidate();
        }
        
        response.sendRedirect(request.getContextPath() + "/");
    }
}
