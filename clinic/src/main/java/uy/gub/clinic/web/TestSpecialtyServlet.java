package uy.gub.clinic.web;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import uy.gub.clinic.entity.Specialty;
import uy.gub.clinic.service.SpecialtyService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet de prueba para verificar consultas de especialidades
 */
public class TestSpecialtyServlet extends HttpServlet {

    @Inject
    private SpecialtyService specialtyService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<html><head><title>Test Specialty</title></head><body>");
        out.println("<h1>Test de Especialidades</h1>");
        
        try {
            // Obtener el contexto de clínica del usuario
            HttpSession session = request.getSession(false);
            Long clinicId = null;
            
            if (session != null) {
                clinicId = (Long) session.getAttribute("clinicId");
                out.println("<p><strong>ClinicId desde sesión:</strong> " + clinicId + "</p>");
            } else {
                out.println("<p><strong>No hay sesión activa</strong></p>");
            }
            
            // Probar consulta directa
            out.println("<h2>Prueba 1: getAllSpecialties()</h2>");
            List<Specialty> allSpecialties = specialtyService.getAllSpecialties();
            out.println("<p><strong>Total especialidades:</strong> " + allSpecialties.size() + "</p>");
            
            for (Specialty specialty : allSpecialties) {
                out.println("<p>- " + specialty.getName() + " (ID: " + specialty.getId() + 
                           ", Clínica: " + (specialty.getClinic() != null ? specialty.getClinic().getName() : "NULL") + ")</p>");
            }
            
            // Nota: Las especialidades ahora son globales
            out.println("<h2>Prueba 2: getSpecialtiesByClinic(" + clinicId + ") - DEPRECATED</h2>");
            out.println("<p><strong>Nota:</strong> Este método está deprecated. Las especialidades son globales y no se filtran por clínica.</p>");
            List<Specialty> specialtiesByClinic = specialtyService.getSpecialtiesByClinic(clinicId);
            out.println("<p><strong>Especialidades devueltas (todas, sin filtrar):</strong> " + specialtiesByClinic.size() + "</p>");
            
            for (Specialty specialty : specialtiesByClinic) {
                out.println("<p>- " + specialty.getName() + " (ID: " + specialty.getId() + 
                           ", Clínica asociada (legacy): " + (specialty.getClinic() != null ? specialty.getClinic().getName() : "NULL") + ")</p>");
            }
            
        } catch (Exception e) {
            out.println("<p><strong>Error:</strong> " + e.getMessage() + "</p>");
            e.printStackTrace();
        }
        
        out.println("</body></html>");
    }
}
