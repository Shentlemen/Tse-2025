package uy.gub.clinic.auth;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Filtro de autenticación para proteger las rutas /admin/* y /professional/*
 * 
 * Verifica que el usuario esté autenticado antes de permitir el acceso.
 * Si no está autenticado, redirige al login.
 */
public class AuthenticationFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    
    // Rutas que requieren autenticación
    private static final String ADMIN_PATH = "/admin/";
    private static final String PROFESSIONAL_PATH = "/professional/";
    private static final String SUPER_ADMIN_PATH = "/super-admin/";
    
    // Rutas públicas que no requieren autenticación
    private static final String[] PUBLIC_PATHS = {
        "/auth/",
        "/api/",
        "/index.jsp",
        "/",
        "/error/",
        "/test-specialty"
    };
    
    // Extensiones de recursos estáticos que no requieren autenticación
    private static final String[] STATIC_EXTENSIONS = {
        ".css", ".js", ".jpg", ".jpeg", ".png", ".gif", ".ico", 
        ".svg", ".woff", ".woff2", ".ttf", ".eot", ".map"
    };
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("AuthenticationFilter inicializado");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());
        
        // Normalizar el path (eliminar query string si existe)
        if (path.contains("?")) {
            path = path.substring(0, path.indexOf("?"));
        }
        
        logger.debug("Filtro de autenticación - Ruta: {}", path);
        
        // PRIMERO: Verificar si la ruta requiere autenticación (esto tiene prioridad)
        boolean requiresAuth = requiresAuthentication(path);
        
        // SEGUNDO: Verificar si es un recurso estático
        boolean isStatic = isStaticResource(path);
        
        // TERCERO: Verificar si es pública (pero solo si NO requiere autenticación)
        boolean isPublic = !requiresAuth && isPublicPath(path);
        
        // Si es un recurso estático, permitir siempre
        if (isStatic) {
            logger.debug("Recurso estático, permitiendo acceso: {}", path);
            chain.doFilter(request, response);
            return;
        }
        
        // Si es un JSP, verificar si es público (solo index.jsp y error/*.jsp son públicos)
        if (path.endsWith(".jsp")) {
            if (isPublic || path.startsWith("/error/")) {
                logger.debug("JSP público, permitiendo acceso: {}", path);
                chain.doFilter(request, response);
                return;
            }
        } else if (isPublic) {
            logger.debug("Ruta pública, permitiendo acceso: {}", path);
            chain.doFilter(request, response);
            return;
        }
        
        // Verificar autenticación
        HttpSession session = httpRequest.getSession(false);
        
        // Verificar si hay sesión y si el usuario está autenticado
        if (session == null || session.getAttribute("user") == null || session.getAttribute("role") == null) {
            logger.warn("Intento de acceso no autorizado a: {} (sin sesión o usuario no autenticado)", path);
            httpResponse.sendRedirect(contextPath + "/");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        String user = (String) session.getAttribute("user");
        
        // Verificar que el rol tenga acceso a la ruta solicitada
        if (path.startsWith(ADMIN_PATH) || path.startsWith(SUPER_ADMIN_PATH)) {
            if (!"ADMIN_CLINIC".equals(role) && !"SUPER_ADMIN".equals(role)) {
                logger.warn("Intento de acceso no autorizado a área admin por usuario {} con rol {}", user, role);
                httpResponse.sendRedirect(contextPath + "/");
                return;
            }
        } else if (path.startsWith(PROFESSIONAL_PATH)) {
            if (!"PROFESSIONAL".equals(role)) {
                logger.warn("Intento de acceso no autorizado a área profesional por usuario {} con rol {}", user, role);
                httpResponse.sendRedirect(contextPath + "/");
                return;
            }
        }
        
        logger.debug("Usuario autenticado {} (rol: {}) accediendo a: {}", user, role, path);
        
        // Continuar con la cadena de filtros
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        logger.info("AuthenticationFilter destruido");
    }
    
    /**
     * Verifica si la ruta es pública (no requiere autenticación)
     */
    private boolean isPublicPath(String path) {
        // Normalizar path
        if (path == null) {
            return false;
        }
        
        // Verificar rutas exactas primero
        if (path.equals("/") || path.equals("/index.jsp")) {
            return true;
        }
        
        // Verificar rutas que empiezan con paths públicos (pero no /admin o /professional)
        for (String publicPath : PUBLIC_PATHS) {
            // Si es "/", solo permitir si es exactamente "/" (ya verificado arriba)
            if (publicPath.equals("/")) {
                continue; // Ya lo verificamos arriba
            }
            // Para otros paths, verificar que empiece con el path público
            // pero NO que sea parte de /admin o /professional
            if (path.startsWith(publicPath)) {
                // Asegurarse de que no sea una ruta protegida
                if (!path.startsWith(ADMIN_PATH) && !path.startsWith(PROFESSIONAL_PATH) && !path.startsWith(SUPER_ADMIN_PATH)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Verifica si la ruta requiere autenticación
     */
    private boolean requiresAuthentication(String path) {
        return path.startsWith(ADMIN_PATH) || 
               path.startsWith(PROFESSIONAL_PATH) || 
               path.startsWith(SUPER_ADMIN_PATH);
    }
    
    /**
     * Verifica si la ruta es un recurso estático (CSS, JS, imágenes, etc.)
     */
    private boolean isStaticResource(String path) {
        String lowerPath = path.toLowerCase();
        for (String extension : STATIC_EXTENSIONS) {
            if (lowerPath.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}

