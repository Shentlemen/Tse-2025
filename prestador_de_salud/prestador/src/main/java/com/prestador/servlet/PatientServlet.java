package com.prestador.servlet;

import com.prestador.entity.Patient;
import com.prestador.messaging.HcenMessageSender;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Patient REST API Servlet
 *
 * Provides endpoints for managing patients:
 * - POST /api/patients - Create new patient
 * - GET /api/patients/{id} - Get patient by ID
 * - GET /api/patients - List all patients
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@WebServlet(name = "PatientServlet", urlPatterns = {"/api/patients", "/api/patients/*"})
public class PatientServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PatientServlet.class.getName());
    private static EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        super.init();
        emf = Persistence.createEntityManagerFactory("prestador-pu");
    }

    @Override
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        super.destroy();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Read request body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject json = new JSONObject(sb.toString());

            // Create patient entity
            Patient patient = new Patient();
            patient.setName(json.getString("name"));
            patient.setLastName(json.optString("lastName", null));
            patient.setDocumentNumber(json.optString("documentNumber", null));
            patient.setInusId(json.optString("inusId", null));

            if (json.has("birthDate")) {
                patient.setBirthDate(LocalDate.parse(json.getString("birthDate")));
            }

            patient.setGender(json.optString("gender", null));
            patient.setEmail(json.optString("email", null));
            patient.setPhone(json.optString("phone", null));
            patient.setAddress(json.optString("address", null));
            patient.setClinicId(json.getLong("clinicId"));
            patient.setActive(json.optBoolean("active", true));

            // Persist to database
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();

            try {
                tx.begin();
                em.persist(patient);
                tx.commit();

                LOGGER.log(Level.INFO, "Patient created successfully - ID: {0}, CI: {1}",
                        new Object[]{patient.getId(), patient.getDocumentNumber()});

                // Send patient registration message to HCEN
                try {
                    if (patient.getDocumentNumber() != null && !patient.getDocumentNumber().trim().isEmpty()) {
                        HcenMessageSender.sendPatientRegistration(
                                patient.getDocumentNumber(),
                                patient.getName(),
                                patient.getLastName(),
                                patient.getBirthDate() != null ? patient.getBirthDate().toString() : null,
                                patient.getEmail(),
                                patient.getPhone(),
                                patient.getClinicId()
                        );
                        LOGGER.log(Level.INFO, "Patient registration message sent to HCEN - CI: {0}",
                                patient.getDocumentNumber());
                    } else {
                        LOGGER.log(Level.WARNING,
                                "Patient created without document number, skipping HCEN registration - Patient ID: {0}",
                                patient.getId());
                    }
                } catch (Exception msgEx) {
                    // Log but don't fail the request - patient is already saved
                    LOGGER.log(Level.WARNING,
                            "Failed to send patient registration to HCEN (patient already saved locally) - CI: " +
                            patient.getDocumentNumber(), msgEx);
                }

                // Return created patient
                JSONObject responseJson = patientToJson(patient);
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(responseJson.toString());

            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            } finally {
                em.close();
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject error = new JSONObject();
            error.put("error", "Failed to create patient");
            error.put("message", e.getMessage());
            response.getWriter().write(error.toString());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            EntityManager em = emf.createEntityManager();

            try {
                if (pathInfo == null || pathInfo.equals("/")) {
                    // List all patients
                    List<Patient> patients = em.createQuery(
                        "SELECT p FROM Patient p ORDER BY p.id", Patient.class)
                        .getResultList();

                    org.json.JSONArray jsonArray = new org.json.JSONArray();
                    for (Patient patient : patients) {
                        jsonArray.put(patientToJson(patient));
                    }

                    response.getWriter().write(jsonArray.toString());

                } else {
                    // Get specific patient
                    String idStr = pathInfo.substring(1);
                    Long id = Long.parseLong(idStr);

                    Patient patient = em.find(Patient.class, id);

                    if (patient != null) {
                        response.getWriter().write(patientToJson(patient).toString());
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        JSONObject error = new JSONObject();
                        error.put("error", "Patient not found");
                        response.getWriter().write(error.toString());
                    }
                }
            } finally {
                em.close();
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject error = new JSONObject();
            error.put("error", "Failed to retrieve patient");
            error.put("message", e.getMessage());
            response.getWriter().write(error.toString());
        }
    }

    private JSONObject patientToJson(Patient patient) {
        JSONObject json = new JSONObject();
        json.put("id", patient.getId());
        json.put("name", patient.getName());
        json.put("lastName", patient.getLastName());
        json.put("documentNumber", patient.getDocumentNumber());
        json.put("inusId", patient.getInusId());
        json.put("birthDate", patient.getBirthDate() != null ? patient.getBirthDate().toString() : null);
        json.put("gender", patient.getGender());
        json.put("email", patient.getEmail());
        json.put("phone", patient.getPhone());
        json.put("address", patient.getAddress());
        json.put("active", patient.getActive());
        json.put("clinicId", patient.getClinicId());
        json.put("createdAt", patient.getCreatedAt() != null ? patient.getCreatedAt().toString() : null);
        json.put("updatedAt", patient.getUpdatedAt() != null ? patient.getUpdatedAt().toString() : null);
        return json;
    }
}
