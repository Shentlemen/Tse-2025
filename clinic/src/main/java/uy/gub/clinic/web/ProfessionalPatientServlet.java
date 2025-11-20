package uy.gub.clinic.web;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uy.gub.clinic.entity.Patient;
import uy.gub.clinic.service.PatientService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servlet para gestionar la vista de pacientes del profesional
 */
@WebServlet("/professional/patients")
public class ProfessionalPatientServlet extends HttpServlet {

    @Inject
    private PatientService patientService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configurar codificación UTF-8
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        try {
            // Obtener la clínica del profesional logueado
            String clinicId = (String) request.getSession().getAttribute("clinicId");
            if (clinicId == null) {
                request.setAttribute("error", "Error de sesión: Clínica no identificada");
                request.getRequestDispatcher("/professional/patients.jsp").forward(request, response);
                return;
            }
            
            // Obtener todos los pacientes de la clínica
            List<Patient> patients = patientService.getPatientsByClinic(clinicId);
            
            // Aplicar filtros si existen
            String searchTerm = request.getParameter("search");
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchLower = searchTerm.toLowerCase().trim();
                patients = patients.stream()
                    .filter(p -> 
                        (p.getName() != null && p.getName().toLowerCase().contains(searchLower)) ||
                        (p.getLastName() != null && p.getLastName().toLowerCase().contains(searchLower)) ||
                        (p.getDocumentNumber() != null && p.getDocumentNumber().contains(searchTerm)) ||
                        (p.getFullName() != null && p.getFullName().toLowerCase().contains(searchLower))
                    )
                    .collect(Collectors.toList());
            }
            
            // Ordenar por nombre completo (apellido, nombre)
            patients = patients.stream()
                .sorted((p1, p2) -> {
                    String name1 = (p1.getFullName() != null) ? p1.getFullName() : "";
                    String name2 = (p2.getFullName() != null) ? p2.getFullName() : "";
                    return name1.compareToIgnoreCase(name2);
                })
                .collect(Collectors.toList());
            
            request.setAttribute("patients", patients);
            request.setAttribute("searchTerm", searchTerm != null ? searchTerm : "");
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error al cargar pacientes: " + e.getMessage());
        }

        // Redirigir a la página de pacientes
        request.getRequestDispatcher("/professional/patients.jsp").forward(request, response);
    }
}

