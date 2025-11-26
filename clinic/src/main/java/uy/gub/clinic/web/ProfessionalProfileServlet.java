package uy.gub.clinic.web;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.Professional;
import uy.gub.clinic.entity.User;
import uy.gub.clinic.service.ProfessionalService;
import uy.gub.clinic.service.SpecialtyService;
import uy.gub.clinic.service.UserService;
import uy.gub.clinic.util.PasswordUtil;

import java.io.IOException;
import java.util.Optional;

/**
 * Servlet para gestionar el perfil del profesional
 */
@WebServlet("/professional/profile")
public class ProfessionalProfileServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ProfessionalProfileServlet.class);

    @Inject
    private ProfessionalService professionalService;
    
    @Inject
    private SpecialtyService specialtyService;
    
    @Inject
    private UserService userService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        try {
            // Obtener professionalId de la sesión
            Long professionalId = (Long) request.getSession().getAttribute("professionalId");
            if (professionalId == null) {
                request.setAttribute("error", "Error de sesión: Profesional no identificado");
                request.getRequestDispatcher("/WEB-INF/views/professional/profile.jsp").forward(request, response);
                return;
            }

            // Buscar el profesional
            Optional<Professional> professionalOpt = professionalService.getProfessionalById(professionalId);
            if (professionalOpt.isEmpty()) {
                request.setAttribute("error", "Profesional no encontrado");
                request.getRequestDispatcher("/WEB-INF/views/professional/profile.jsp").forward(request, response);
                return;
            }

            Professional professional = professionalOpt.get();
            
            // Las relaciones lazy ya se cargaron en el servicio

            // Cargar el usuario para mostrar el username
            String username = (String) request.getSession().getAttribute("user");
            Optional<User> userOpt = Optional.empty();
            if (username != null) {
                userOpt = userService.findByUsername(username);
            }
            request.setAttribute("user", userOpt.orElse(null));

            // Cargar especialidades para el selector (si se permite cambiar)
            request.setAttribute("specialties", specialtyService.getAllSpecialties());
            request.setAttribute("professional", professional);
            request.setAttribute("success", request.getParameter("success"));

        } catch (Exception e) {
            logger.error("Error al cargar perfil del profesional", e);
            request.setAttribute("error", "Error al cargar el perfil: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/professional/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            // Obtener professionalId de la sesión
            Long professionalId = (Long) request.getSession().getAttribute("professionalId");
            if (professionalId == null) {
                response.sendRedirect(request.getContextPath() + "/professional/profile?error=session");
                return;
            }

            // Obtener username de la sesión para actualizar si es necesario
            String currentUsername = (String) request.getSession().getAttribute("user");
            Optional<User> userOpt = Optional.empty();
            if (currentUsername != null) {
                userOpt = userService.findByUsername(currentUsername);
            }

            // Obtener parámetros del formulario
            String name = request.getParameter("name");
            String lastName = request.getParameter("lastName");
            String email = request.getParameter("email");
            String licenseNumber = request.getParameter("licenseNumber");
            String phone = request.getParameter("phone");
            String specialtyIdStr = request.getParameter("specialtyId");
            Long specialtyId = (specialtyIdStr != null && !specialtyIdStr.isEmpty()) 
                ? Long.parseLong(specialtyIdStr) : null;
            
            // Obtener nuevo username si se proporcionó
            String newUsername = request.getParameter("username");

            // Validar campos requeridos
            if (name == null || name.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/professional/profile?error=name");
                return;
            }

            // Actualizar username si se proporcionó y es diferente
            if (newUsername != null && !newUsername.trim().isEmpty() && userOpt.isPresent()) {
                User user = userOpt.get();
                String trimmedUsername = newUsername.trim();
                if (!trimmedUsername.equals(user.getUsername())) {
                    try {
                        userService.changeUsername(user.getId(), trimmedUsername);
                        // Actualizar la sesión con el nuevo username
                        request.getSession().setAttribute("user", trimmedUsername);
                    } catch (IllegalArgumentException e) {
                        // Error de validación (username en uso, etc.)
                        response.sendRedirect(request.getContextPath() + "/professional/profile?error=" + 
                            java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
                        return;
                    }
                }
            }

            // Manejar cambio de contraseña si se proporcionaron los campos
            String currentPassword = request.getParameter("currentPassword");
            String newPassword = request.getParameter("newPassword");
            String confirmPassword = request.getParameter("confirmPassword");
            
            // Solo cambiar contraseña si todos los campos están presentes y no vacíos
            if (currentPassword != null && newPassword != null && confirmPassword != null &&
                !currentPassword.trim().isEmpty() && !newPassword.trim().isEmpty() && !confirmPassword.trim().isEmpty()) {
                
                if (!PasswordUtil.isValidPassword(newPassword)) {
                    response.sendRedirect(request.getContextPath() + "/professional/profile?error=password_invalid");
                    return;
                }
                
                if (!newPassword.equals(confirmPassword)) {
                    response.sendRedirect(request.getContextPath() + "/professional/profile?error=password_mismatch");
                    return;
                }
                
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    if (!PasswordUtil.verifyPassword(currentPassword, user.getPassword())) {
                        response.sendRedirect(request.getContextPath() + "/professional/profile?error=password_incorrect");
                        return;
                    }
                    
                    // Cambiar contraseña
                    String hashedPassword = PasswordUtil.hashPassword(newPassword);
                    userService.changePassword(user.getId(), hashedPassword);
                }
            }

            // Actualizar el profesional
            Professional updated = professionalService.updateProfessional(
                professionalId, name, lastName, email, licenseNumber, phone, specialtyId);

            // Redirigir con mensaje de éxito
            response.sendRedirect(request.getContextPath() + "/professional/profile?success=true");

        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al actualizar perfil", e);
            response.sendRedirect(request.getContextPath() + "/professional/profile?error=" + 
                java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        } catch (Exception e) {
            logger.error("Error al actualizar perfil del profesional", e);
            response.sendRedirect(request.getContextPath() + "/professional/profile?error=update");
        }
    }

}

