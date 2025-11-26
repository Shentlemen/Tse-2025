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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet("/admin/patients-list")
public class PatientServlet extends HttpServlet {

    @Inject
    private PatientService patientService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configurar codificación UTF-8
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        System.out.println("=== PatientServlet.doGet ===");
        System.out.println("PatientService inyectado: " + (patientService != null ? "SÍ" : "NO"));

        if (patientService == null) {
            System.out.println("ERROR: PatientService es NULL - problema de inyección de dependencias");
            request.setAttribute("error", "Error de configuración: PatientService no disponible");
            request.getRequestDispatcher("/WEB-INF/views/admin/patients.jsp").forward(request, response);
            return;
        }

        try {
            // Obtener la clínica del usuario logueado
            String clinicId = (String) request.getSession().getAttribute("clinicId");
            if (clinicId == null) {
                System.out.println("ERROR: No se encontró clinicId en la sesión");
                request.setAttribute("error", "Error de sesión: Clínica no identificada");
                request.getRequestDispatcher("/WEB-INF/views/admin/patients.jsp").forward(request, response);
                return;
            }
            
            // Obtener solo los pacientes de la clínica del usuario
            List<Patient> patients = patientService.getPatientsByClinic(clinicId);
            System.out.println("Pacientes obtenidos de la clínica " + clinicId + ": " + patients.size());

            for (Patient patient : patients) {
                System.out.println("- " + patient.getFullName() + " (" + patient.getDocumentNumber() + ")");
            }

            System.out.println("DEBUG: Estableciendo atributo 'patients' en request con " + patients.size() + " elementos");
            request.setAttribute("patients", patients);
            
            // Verificar que se estableció correctamente
            Object patientsFromRequest = request.getAttribute("patients");
            System.out.println("DEBUG: Verificando atributo 'patients' en request: " + 
                (patientsFromRequest != null ? "NO NULL" : "NULL"));
            if (patientsFromRequest instanceof List) {
                System.out.println("DEBUG: Es una Lista con " + ((List<?>) patientsFromRequest).size() + " elementos");
            }
            
        } catch (Exception e) {
            System.out.println("Error al obtener pacientes: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Error al cargar pacientes: " + e.getMessage());
        }

        // Redirigir a la página de gestión de pacientes
        request.getRequestDispatcher("/WEB-INF/views/admin/patients.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Configurar codificación UTF-8
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        
        System.out.println("=== PatientServlet.doPost ===");
        System.out.println("Action: " + request.getParameter("action"));
        System.out.println("Name: " + request.getParameter("name"));
        System.out.println("Last Name: " + request.getParameter("lastName"));
        System.out.println("Document: " + request.getParameter("documentNumber"));
        System.out.println("INUS ID: " + request.getParameter("inusId"));
        System.out.println("Birth Date: " + request.getParameter("birthDate"));
        System.out.println("Gender: " + request.getParameter("gender"));
        System.out.println("Phone: " + request.getParameter("phone"));
        System.out.println("Email: " + request.getParameter("email"));
        System.out.println("Address: " + request.getParameter("address"));
        System.out.println("Clinic ID: " + request.getParameter("clinicId"));
        
        String action = request.getParameter("action");
        
        try {
            switch (action) {
                case "register":
                    registerPatient(request, response);
                    break;
                case "update":
                    updatePatient(request, response);
                    break;
                case "delete":
                    deletePatient(request, response);
                    break;
                case "activate":
                    togglePatientStatus(request, response, true);
                    break;
                case "deactivate":
                    togglePatientStatus(request, response, false);
                    break;
                default:
                    System.out.println("Acción no válida: " + action);
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no válida");
            }
        } catch (Exception e) {
            System.out.println("Error en doPost: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", e.getMessage());
            doGet(request, response); // Recargar la página con el error
        }
    }
    
    private void registerPatient(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String name = request.getParameter("name");
        String lastName = request.getParameter("lastName");
        String documentNumber = request.getParameter("documentNumber");
        String inusId = request.getParameter("inusId");
        String birthDateStr = request.getParameter("birthDate");
        String gender = request.getParameter("gender");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String address = request.getParameter("address");
        
        // Obtener clinicId de la sesión o del request
        String clinicId = (String) request.getSession().getAttribute("clinicId");
        if (clinicId == null) {
            String clinicIdStr = request.getParameter("clinicId");
            if (clinicIdStr != null && !clinicIdStr.trim().isEmpty()) {
                clinicId = clinicIdStr.trim();
            }
        }
        
        System.out.println("=== registerPatient ===");
        System.out.println("ClinicId desde sesión: " + clinicId);
        System.out.println("ClinicId desde request: " + request.getParameter("clinicId"));
        
        // Validaciones básicas
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (clinicId == null) {
            throw new IllegalArgumentException("Debe seleccionar una clínica");
        }
        
        System.out.println("Llamando a patientService.registerPatient...");
        
        // Parsear fecha de nacimiento
        LocalDate birthDate = null;
        if (birthDateStr != null && !birthDateStr.trim().isEmpty()) {
            try {
                birthDate = LocalDate.parse(birthDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                System.out.println("Error al parsear fecha: " + e.getMessage());
            }
        }
        
        Patient patient = patientService.registerPatient(
            name, lastName, documentNumber, inusId, birthDate, gender, 
            phone, email, address, clinicId);
        
        System.out.println("Paciente registrado: " + patient.getFullName());
        
        request.setAttribute("success", "Paciente registrado exitosamente: " + patient.getFullName());
        
        // Redirigir para evitar reenvío del formulario
        System.out.println("Redirigiendo a: " + request.getContextPath() + "/admin/patients?success=registered");
        response.sendRedirect(request.getContextPath() + "/admin/patients-list?success=registered");
    }
    
    private void updatePatient(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Long id = Long.valueOf(request.getParameter("id"));
        String name = request.getParameter("name");
        String lastName = request.getParameter("lastName");
        String documentNumber = request.getParameter("documentNumber");
        String inusId = request.getParameter("inusId");
        String birthDateStr = request.getParameter("birthDate");
        String gender = request.getParameter("gender");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String address = request.getParameter("address");
        
        // Parsear fecha de nacimiento
        LocalDate birthDate = null;
        if (birthDateStr != null && !birthDateStr.trim().isEmpty()) {
            try {
                birthDate = LocalDate.parse(birthDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                System.out.println("Error al parsear fecha: " + e.getMessage());
            }
        }
        
        Patient patient = patientService.updatePatient(
            id, name, lastName, documentNumber, inusId, birthDate, 
            gender, phone, email, address);
        
        request.setAttribute("success", "Paciente actualizado exitosamente: " + patient.getFullName());
        response.sendRedirect(request.getContextPath() + "/admin/patients-list?success=updated");
    }
    
    private void deletePatient(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Long id = Long.valueOf(request.getParameter("id"));
        
        patientService.deletePatient(id);
        
        request.setAttribute("success", "Paciente eliminado exitosamente");
        response.sendRedirect(request.getContextPath() + "/admin/patients-list?success=deleted");
    }
    
    private void togglePatientStatus(HttpServletRequest request, HttpServletResponse response, boolean activate)
            throws ServletException, IOException {

        System.out.println("=== togglePatientStatus ===");
        System.out.println("Activate: " + activate);

        Long id = Long.valueOf(request.getParameter("id"));

        if (activate) {
            patientService.activatePatient(id);
            System.out.println("Paciente activado: " + id);
        } else {
            patientService.deactivatePatient(id);
            System.out.println("Paciente desactivado: " + id);
        }

        response.sendRedirect(request.getContextPath() + "/admin/patients-list?success=" +
                            (activate ? "activated" : "deactivated"));
    }
}
