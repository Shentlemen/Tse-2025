package uy.gub.clinic.web;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.service.ClinicService;
import uy.gub.clinic.service.ProfessionalService;
import uy.gub.clinic.service.PatientService;
import uy.gub.clinic.service.SpecialtyService;

import java.io.IOException;
import java.util.List;

/**
 * Servlet para el dashboard del administrador
 */
public class DashboardServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServlet.class);

    @Inject
    private ClinicService clinicService;

    @Inject
    private ProfessionalService professionalService;

    @Inject
    private PatientService patientService;

    @Inject
    private SpecialtyService specialtyService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            HttpSession session = request.getSession(false);
            
            if (session == null) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }

            Long clinicId = (Long) session.getAttribute("clinicId");
            String role = (String) session.getAttribute("role");

            System.out.println("=== DashboardServlet.doGet ===");
            System.out.println("ClinicId desde sesión: " + clinicId);
            System.out.println("Role desde sesión: " + role);
            logger.info("Dashboard - ClinicId: {}, Role: {}", clinicId, role);

            // Obtener estadísticas según el rol
            long professionalsCount;
            long patientsCount;
            long specialtiesCount;

            if (clinicId != null && clinicId == 0L) {
                // Super Admin - ver todos los datos
                professionalsCount = professionalService.getAllProfessionals().stream()
                        .filter(p -> p.getActive()).count();
                patientsCount = patientService.getAllPatients().stream()
                        .filter(p -> p.getActive()).count();
                specialtiesCount = specialtyService.getAllSpecialties().stream()
                        .filter(s -> s.getActive()).count();
            } else {
                // Administrador de clínica - datos de su clínica
                professionalsCount = professionalService.getProfessionalsByClinic(clinicId).stream()
                        .filter(p -> p.getActive()).count();
                patientsCount = patientService.getPatientsByClinic(clinicId).stream()
                        .filter(p -> p.getActive()).count();
                specialtiesCount = specialtyService.getSpecialtiesByClinic(clinicId).stream()
                        .filter(s -> s.getActive()).count();
            }

            // Establecer atributos en la request
            request.setAttribute("professionalsCount", professionalsCount);
            request.setAttribute("patientsCount", patientsCount);
            request.setAttribute("specialtiesCount", specialtiesCount);
            // Documentos - dejamos en 0 por ahora
            request.setAttribute("documentsCount", 0);

            logger.info("Dashboard stats - Profesionales: {}, Pacientes: {}, Especialidades: {}", 
                        professionalsCount, patientsCount, specialtiesCount);

            // Forward a dashboard.jsp
            request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            logger.error("Error al cargar dashboard", e);
            request.setAttribute("error", "Error al cargar el dashboard: " + e.getMessage());
            request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
        }
    }
}

