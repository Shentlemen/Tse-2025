package com.prestador.servlet;

import com.prestador.entity.ClinicalDocument;
import com.prestador.entity.Patient;
import com.prestador.messaging.HcenMessageSender;
import com.prestador.service.ClinicalDocumentService;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clinical Document REST API Servlet
 *
 * Provides endpoints for managing clinical documents:
 * - POST /api/documents - Create new document
 * - GET /api/documents/{id} - Get document by ID
 * - GET /api/documents - List all documents
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@WebServlet(name = "ClinicalDocumentServlet", urlPatterns = {"/api/documents", "/api/documents/*"})
public class ClinicalDocumentServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ClinicalDocumentServlet.class.getName());

    @EJB
    private ClinicalDocumentService documentService;

    @EJB
    private HcenMessageSender messageSender;

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

            // Create document entity
            ClinicalDocument document = new ClinicalDocument();
            document.setTitle(json.getString("title"));
            document.setDescription(json.optString("description", null));
            document.setDocumentType(json.getString("documentType"));
            document.setPatientId(json.getString("patientId"));
            document.setClinicId(json.getString("clinicId"));
            document.setProfessionalId(json.getString("professionalId"));

            if (json.has("specialtyId")) {
                document.setSpecialtyId(json.getString("specialtyId"));
            }

            document.setDateOfVisit(LocalDate.parse(json.getString("dateOfVisit")));

            // File information (optional)
            document.setFileName(json.optString("fileName", null));
            document.setFilePath(json.optString("filePath", null));
            if (json.has("fileSize")) {
                document.setFileSize(json.getLong("fileSize"));
            }
            document.setMimeType(json.optString("mimeType", null));

            // RNDC reference (optional)
            document.setRndcId(json.optString("rndcId", null));

            // Clinical information (optional)
            document.setChiefComplaint(json.optString("chiefComplaint", null));
            document.setCurrentIllness(json.optString("currentIllness", null));
            document.setVitalSigns(json.optString("vitalSigns", null));
            document.setPhysicalExamination(json.optString("physicalExamination", null));
            document.setDiagnosis(json.optString("diagnosis", null));
            document.setTreatment(json.optString("treatment", null));
            document.setPrescriptions(json.optString("prescriptions", null));
            document.setObservations(json.optString("observations", null));

            if (json.has("nextAppointment")) {
                document.setNextAppointment(LocalDate.parse(json.getString("nextAppointment")));
            }

            document.setAttachments(json.optString("attachments", null));

            // Persist to database using EJB service (automatic transaction management)
            document = documentService.createDocument(document);

            LOGGER.log(Level.INFO, "Clinical document created successfully - ID: {0}, Patient ID: {1}, Type: {2}",
                    new Object[]{document.getId(), document.getPatientId(), document.getDocumentType()});

            // Send document metadata to HCEN RNDC
            try {
                // Get patient CI for HCEN registration
                Patient patient = documentService.findPatientByDocumentNumber(document.getPatientId());

                    if (patient != null && patient.getDocumentNumber() != null &&
                        !patient.getDocumentNumber().trim().isEmpty()) {

                        // Build document locator URL for HCEN to retrieve the document
                        String baseUrl = request.getScheme() + "://" + request.getServerName() +
                                ":" + request.getServerPort() + request.getContextPath();
                        String documentLocatorUrl = baseUrl + "/api/documents/" + document.getId();

                        // Send metadata to HCEN (FHIR format)
                        messageSender.sendDocumentMetadata(
                                patient.getDocumentNumber(),  // Patient CI
                                document.getId(),              // Local document ID
                                document.getDocumentType(),    // Document type
                                document.getTitle(),           // Document title
                                document.getDescription(),     // Document description
                                "professional-" + document.getProfessionalId(), // Created by
                                document.getCreatedAt(),       // Creation timestamp (LocalDateTime)
                                document.getClinicId(),        // Clinic ID
                                document.getSpecialtyId(),     // Specialty ID (optional)
                                documentLocatorUrl             // URL to retrieve document from prestador
                        );

                        LOGGER.log(Level.INFO,
                                "Document metadata sent to HCEN RNDC - Document ID: {0}, Patient CI: {1}, Locator: {2}",
                                new Object[]{document.getId(), patient.getDocumentNumber(), documentLocatorUrl});

                    } else {
                        LOGGER.log(Level.WARNING,
                                "Patient not found or missing CI, skipping HCEN registration - Patient ID: {0}, Document ID: {1}",
                                new Object[]{document.getPatientId(), document.getId()});
                    }

                } catch (Exception msgEx) {
                    // Log but don't fail the request - document is already saved
                    LOGGER.log(Level.WARNING,
                            "Failed to send document metadata to HCEN (document already saved locally) - Document ID: " +
                            document.getId(), msgEx);
                }

            // Return created document
            JSONObject responseJson = documentToJson(document);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(responseJson.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject error = new JSONObject();
            error.put("error", "Failed to create clinical document");
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
            if (pathInfo == null || pathInfo.equals("/")) {
                // List all documents
                List<ClinicalDocument> documents = documentService.findAll();

                org.json.JSONArray jsonArray = new org.json.JSONArray();
                for (ClinicalDocument document : documents) {
                    jsonArray.put(documentToJson(document));
                }

                response.getWriter().write(jsonArray.toString());

            } else {
                // Get specific document
                String idStr = pathInfo.substring(1);
                Long id = Long.parseLong(idStr);

                ClinicalDocument document = documentService.findById(id);

                if (document != null) {
                    response.getWriter().write(documentToJson(document).toString());
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    JSONObject error = new JSONObject();
                    error.put("error", "Clinical document not found");
                    response.getWriter().write(error.toString());
                }
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject error = new JSONObject();
            error.put("error", "Failed to retrieve clinical document");
            error.put("message", e.getMessage());
            response.getWriter().write(error.toString());
        }
    }

    private JSONObject documentToJson(ClinicalDocument document) {
        JSONObject json = new JSONObject();
        json.put("id", document.getId());
        json.put("title", document.getTitle());
        json.put("description", document.getDescription());
        json.put("documentType", document.getDocumentType());
        json.put("patientId", document.getPatientId());
        json.put("clinicId", document.getClinicId());
        json.put("professionalId", document.getProfessionalId());
        json.put("specialtyId", document.getSpecialtyId());
        json.put("dateOfVisit", document.getDateOfVisit() != null ? document.getDateOfVisit().toString() : null);
        json.put("fileName", document.getFileName());
        json.put("filePath", document.getFilePath());
        json.put("fileSize", document.getFileSize());
        json.put("mimeType", document.getMimeType());
        json.put("rndcId", document.getRndcId());
        json.put("chiefComplaint", document.getChiefComplaint());
        json.put("currentIllness", document.getCurrentIllness());
        json.put("vitalSigns", document.getVitalSigns());
        json.put("physicalExamination", document.getPhysicalExamination());
        json.put("diagnosis", document.getDiagnosis());
        json.put("treatment", document.getTreatment());
        json.put("prescriptions", document.getPrescriptions());
        json.put("observations", document.getObservations());
        json.put("nextAppointment", document.getNextAppointment() != null ? document.getNextAppointment().toString() : null);
        json.put("attachments", document.getAttachments());
        json.put("createdAt", document.getCreatedAt() != null ? document.getCreatedAt().toString() : null);
        json.put("updatedAt", document.getUpdatedAt() != null ? document.getUpdatedAt().toString() : null);
        return json;
    }
}
