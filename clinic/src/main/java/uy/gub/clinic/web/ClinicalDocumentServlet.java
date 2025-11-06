package uy.gub.clinic.web;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.ClinicalDocument;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.entity.Patient;
import uy.gub.clinic.entity.Professional;
import uy.gub.clinic.entity.Specialty;
import uy.gub.clinic.service.ClinicalDocumentService;
import uy.gub.clinic.service.PatientService;
import uy.gub.clinic.service.ProfessionalService;
import uy.gub.clinic.service.SpecialtyService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Servlet para gestión de documentos clínicos
 * Mapeado en web.xml para evitar conflictos con @WebServlet
 */
@MultipartConfig(
    maxFileSize = 10485760, // 10MB
    maxRequestSize = 52428800 // 50MB total
)
public class ClinicalDocumentServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(ClinicalDocumentServlet.class);
    private static final String UPLOAD_DIR = "uploads";
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Inject
    private ClinicalDocumentService documentService;
    
    @Inject
    private PatientService patientService;
    
    @Inject
    private ProfessionalService professionalService;
    
    @Inject
    private SpecialtyService specialtyService;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        
        String action = request.getParameter("action");
        String documentId = request.getParameter("id");
        
        try {
            String userRole = (String) request.getSession().getAttribute("role");
            boolean isSuperAdmin = "SUPER_ADMIN".equals(userRole);
            
            Long clinicId = (Long) request.getSession().getAttribute("clinicId");
            if (clinicId == null && !isSuperAdmin) {
                request.setAttribute("error", "Error de sesión: Clínica no identificada");
                request.getRequestDispatcher("/admin/documents.jsp").forward(request, response);
                return;
            }
            
            // Acción de descarga de archivo
            if ("download".equals(action) && documentId != null) {
                downloadFile(request, response, Long.parseLong(documentId));
                return;
            }
            
            // Acción para obtener un documento específico (para ver o editar)
            if (("view".equals(action) || "edit".equals(action)) && documentId != null) {
                Optional<ClinicalDocument> docOpt = documentService.findById(Long.parseLong(documentId));
                if (docOpt.isPresent()) {
                    ClinicalDocument doc = docOpt.get();
                    // Si es superadmin, puede ver cualquier documento. Si no, verificar que pertenece a su clínica
                    if (isSuperAdmin) {
                        request.setAttribute("selectedDocument", doc);
                        request.setAttribute("action", action);
                    } else {
                        Long docClinicId = doc.getClinic() != null ? doc.getClinic().getId() : null;
                        if (docClinicId != null && docClinicId.equals(clinicId)) {
                            request.setAttribute("selectedDocument", doc);
                            request.setAttribute("action", action);
                        } else {
                            request.setAttribute("error", "Documento no encontrado o no pertenece a su clínica");
                        }
                    }
                } else {
                    request.setAttribute("error", "Documento no encontrado");
                }
            }
            
            // Obtener documentos - si es superadmin, pasar null para obtener todos
            List<ClinicalDocument> documents;
            
            // Aplicar filtros si existen
            String specialtyIdStr = request.getParameter("specialtyId");
            String patientIdStr = request.getParameter("patientId");
            String professionalIdStr = request.getParameter("professionalId");
            String documentType = request.getParameter("documentType");
            String dateFromStr = request.getParameter("dateFrom");
            String dateToStr = request.getParameter("dateTo");
            
            Long specialtyId = specialtyIdStr != null && !specialtyIdStr.isEmpty() ? Long.parseLong(specialtyIdStr) : null;
            Long patientId = patientIdStr != null && !patientIdStr.isEmpty() ? Long.parseLong(patientIdStr) : null;
            Long professionalId = professionalIdStr != null && !professionalIdStr.isEmpty() ? Long.parseLong(professionalIdStr) : null;
            LocalDate dateFrom = null;
            LocalDate dateTo = null;
            
            try {
                if (dateFromStr != null && !dateFromStr.isEmpty()) {
                    dateFrom = LocalDate.parse(dateFromStr);
                }
                if (dateToStr != null && !dateToStr.isEmpty()) {
                    dateTo = LocalDate.parse(dateToStr);
                }
            } catch (DateTimeParseException e) {
                logger.warn("Error al parsear fechas de filtro", e);
            }
            
            // Si es superadmin, pasar null como clinicId para obtener todos los documentos
            Long searchClinicId = isSuperAdmin ? null : clinicId;
            documents = documentService.searchDocuments(
                searchClinicId, specialtyId, patientId, professionalId, 
                documentType, dateFrom, dateTo);
            
            request.setAttribute("documents", documents);
            
            // Cargar datos para filtros y formularios
            // Si es superadmin, cargar de todas las clínicas
            List<Patient> patients;
            List<Professional> professionals;
            List<Specialty> specialties;
            
            if (isSuperAdmin) {
                // Para superadmin, obtener todos los datos
                patients = patientService.getAllPatients();
                professionals = professionalService.getAllProfessionals();
                specialties = specialtyService.getAllSpecialties();
            } else {
                patients = patientService.getPatientsByClinic(clinicId);
                professionals = professionalService.getProfessionalsByClinic(clinicId);
                specialties = specialtyService.getSpecialtiesByClinic(clinicId);
            }
            
            request.setAttribute("patients", patients);
            request.setAttribute("professionals", professionals);
            request.setAttribute("specialties", specialties);
            
        } catch (Exception e) {
            logger.error("Error al obtener documentos", e);
            request.setAttribute("error", "Error al cargar documentos: " + e.getMessage());
        }
        
        request.getRequestDispatcher("/admin/documents.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        
        try {
            Long clinicId = (Long) request.getSession().getAttribute("clinicId");
            if (clinicId == null) {
                request.setAttribute("error", "Error de sesión: Clínica no identificada");
                doGet(request, response);
                return;
            }
            
            switch (action) {
                case "create":
                    createDocument(request, response, clinicId);
                    break;
                case "update":
                    updateDocument(request, response, clinicId);
                    break;
                case "delete":
                    deleteDocument(request, response);
                    break;
                default:
                    request.setAttribute("error", "Acción no válida: " + action);
                    doGet(request, response);
            }
        } catch (Exception e) {
            logger.error("Error en doPost", e);
            request.setAttribute("error", "Error: " + e.getMessage());
            doGet(request, response);
        }
    }
    
    private void createDocument(HttpServletRequest request, HttpServletResponse response, Long clinicId)
            throws ServletException, IOException {
        
        try {
            ClinicalDocument document = new ClinicalDocument();
            
            // Campos básicos
            document.setTitle(request.getParameter("title"));
            document.setDescription(request.getParameter("description"));
            document.setDocumentType(request.getParameter("documentType"));
            
            // Fecha de consulta
            String dateOfVisitStr = request.getParameter("dateOfVisit");
            if (dateOfVisitStr != null && !dateOfVisitStr.isEmpty()) {
                document.setDateOfVisit(LocalDate.parse(dateOfVisitStr));
            } else {
                document.setDateOfVisit(LocalDate.now());
            }
            
            // Campos del formulario médico
            document.setChiefComplaint(request.getParameter("chiefComplaint"));
            document.setCurrentIllness(request.getParameter("currentIllness"));
            document.setPhysicalExamination(request.getParameter("physicalExamination"));
            document.setDiagnosis(request.getParameter("diagnosis"));
            document.setTreatment(request.getParameter("treatment"));
            document.setObservations(request.getParameter("observations"));
            
            // Signos vitales (JSON)
            String vitalSigns = buildVitalSignsJson(request);
            document.setVitalSigns(vitalSigns);
            
            // Prescripciones (JSON)
            String prescriptions = buildPrescriptionsJson(request);
            document.setPrescriptions(prescriptions);
            
            // Próxima cita
            String nextAppointmentStr = request.getParameter("nextAppointment");
            if (nextAppointmentStr != null && !nextAppointmentStr.isEmpty()) {
                document.setNextAppointment(LocalDate.parse(nextAppointmentStr));
            }
            
            // Relaciones
            Long patientId = Long.parseLong(request.getParameter("patientId"));
            Long professionalId = Long.parseLong(request.getParameter("professionalId"));
            Long specialtyId = Long.parseLong(request.getParameter("specialtyId"));
            
            Patient patient = new Patient();
            patient.setId(patientId);
            document.setPatient(patient);
            
            Professional professional = new Professional();
            professional.setId(professionalId);
            document.setProfessional(professional);
            
            Specialty specialty = new Specialty();
            specialty.setId(specialtyId);
            document.setSpecialty(specialty);
            
            Clinic clinic = new Clinic();
            clinic.setId(clinicId);
            document.setClinic(clinic);
            
            // Guardar documento primero para obtener el ID
            ClinicalDocument savedDocument = documentService.createDocument(document);
            
            // Procesar archivos adjuntos
            List<ObjectNode> attachments = new ArrayList<>();
            if (request.getParts() != null) {
                for (Part part : request.getParts()) {
                    if (part.getName().equals("attachments") && part.getSize() > 0) {
                        String fileName = part.getSubmittedFileName();
                        if (fileName != null && !fileName.isEmpty()) {
                            String savedFilePath = saveFile(part, clinicId, savedDocument.getId());
                            if (savedFilePath != null) {
                                ObjectNode attachment = objectMapper.createObjectNode();
                                attachment.put("fileName", fileName);
                                attachment.put("filePath", savedFilePath);
                                attachment.put("fileSize", part.getSize());
                                attachment.put("mimeType", part.getContentType());
                                attachments.add(attachment);
                            }
                        }
                    }
                }
            }
            
            // Actualizar documento con información de archivos
            if (!attachments.isEmpty()) {
                ArrayNode attachmentsArray = objectMapper.valueToTree(attachments);
                savedDocument.setAttachments(attachmentsArray.toString());
                documentService.updateDocument(savedDocument);
            }
            
            request.setAttribute("success", "Documento creado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al crear documento", e);
            request.setAttribute("error", "Error al crear documento: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Redirigir en lugar de hacer forward para evitar problemas con POST
        try {
            response.sendRedirect(request.getContextPath() + "/admin/documents");
        } catch (Exception redirectEx) {
            logger.error("Error al redirigir después de crear documento", redirectEx);
            doGet(request, response);
        }
    }
    
    private void updateDocument(HttpServletRequest request, HttpServletResponse response, Long clinicId)
            throws ServletException, IOException {
        
        try {
            Long documentId = Long.parseLong(request.getParameter("documentId"));
            ClinicalDocument document = documentService.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
            
            // Actualizar campos
            document.setTitle(request.getParameter("title"));
            document.setDescription(request.getParameter("description"));
            document.setDocumentType(request.getParameter("documentType"));
            
            String dateOfVisitStr = request.getParameter("dateOfVisit");
            if (dateOfVisitStr != null && !dateOfVisitStr.isEmpty()) {
                document.setDateOfVisit(LocalDate.parse(dateOfVisitStr));
            }
            
            document.setChiefComplaint(request.getParameter("chiefComplaint"));
            document.setCurrentIllness(request.getParameter("currentIllness"));
            document.setPhysicalExamination(request.getParameter("physicalExamination"));
            document.setDiagnosis(request.getParameter("diagnosis"));
            document.setTreatment(request.getParameter("treatment"));
            document.setObservations(request.getParameter("observations"));
            
            String vitalSigns = buildVitalSignsJson(request);
            document.setVitalSigns(vitalSigns);
            
            String prescriptions = buildPrescriptionsJson(request);
            document.setPrescriptions(prescriptions);
            
            String nextAppointmentStr = request.getParameter("nextAppointment");
            if (nextAppointmentStr != null && !nextAppointmentStr.isEmpty()) {
                document.setNextAppointment(LocalDate.parse(nextAppointmentStr));
            } else {
                document.setNextAppointment(null);
            }
            
            // Actualizar relaciones si se proporcionan
            String patientIdStr = request.getParameter("patientId");
            if (patientIdStr != null && !patientIdStr.isEmpty()) {
                Patient patient = new Patient();
                patient.setId(Long.parseLong(patientIdStr));
                document.setPatient(patient);
            }
            
            String professionalIdStr = request.getParameter("professionalId");
            if (professionalIdStr != null && !professionalIdStr.isEmpty()) {
                Professional professional = new Professional();
                professional.setId(Long.parseLong(professionalIdStr));
                document.setProfessional(professional);
            }
            
            String specialtyIdStr = request.getParameter("specialtyId");
            if (specialtyIdStr != null && !specialtyIdStr.isEmpty()) {
                Specialty specialty = new Specialty();
                specialty.setId(Long.parseLong(specialtyIdStr));
                document.setSpecialty(specialty);
            }
            
            // Procesar nuevos archivos adjuntos
            List<ObjectNode> newAttachments = new ArrayList<>();
            if (request.getParts() != null) {
                for (Part part : request.getParts()) {
                    if (part.getName().equals("attachments") && part.getSize() > 0) {
                        String fileName = part.getSubmittedFileName();
                        if (fileName != null && !fileName.isEmpty()) {
                            String savedFilePath = saveFile(part, clinicId, documentId);
                            if (savedFilePath != null) {
                                ObjectNode attachment = objectMapper.createObjectNode();
                                attachment.put("fileName", fileName);
                                attachment.put("filePath", savedFilePath);
                                attachment.put("fileSize", part.getSize());
                                attachment.put("mimeType", part.getContentType());
                                newAttachments.add(attachment);
                            }
                        }
                    }
                }
            }
            
            // Combinar archivos existentes con nuevos
            List<ObjectNode> allAttachments = new ArrayList<>();
            if (document.getAttachments() != null && !document.getAttachments().isEmpty()) {
                try {
                    ArrayNode existingAttachments = (ArrayNode) objectMapper.readTree(document.getAttachments());
                    for (int i = 0; i < existingAttachments.size(); i++) {
                        allAttachments.add((ObjectNode) existingAttachments.get(i));
                    }
                } catch (Exception e) {
                    logger.warn("Error al parsear archivos existentes", e);
                }
            }
            allAttachments.addAll(newAttachments);
            
            if (!allAttachments.isEmpty()) {
                ArrayNode attachmentsArray = objectMapper.valueToTree(allAttachments);
                document.setAttachments(attachmentsArray.toString());
            }
            
            documentService.updateDocument(document);
            
            request.setAttribute("success", "Documento actualizado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al actualizar documento", e);
            request.setAttribute("error", "Error al actualizar documento: " + e.getMessage());
        }
        
        // Redirigir en lugar de hacer forward
        try {
            response.sendRedirect(request.getContextPath() + "/admin/documents");
        } catch (Exception redirectEx) {
            logger.error("Error al redirigir después de actualizar documento", redirectEx);
            doGet(request, response);
        }
    }
    
    private void deleteDocument(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            Long documentId = Long.parseLong(request.getParameter("documentId"));
            documentService.deleteDocument(documentId);
            request.setAttribute("success", "Documento eliminado exitosamente");
        } catch (Exception e) {
            logger.error("Error al eliminar documento", e);
            request.setAttribute("error", "Error al eliminar documento: " + e.getMessage());
        }
        
        // Redirigir en lugar de hacer forward
        try {
            response.sendRedirect(request.getContextPath() + "/admin/documents");
        } catch (Exception redirectEx) {
            logger.error("Error al redirigir después de eliminar documento", redirectEx);
            doGet(request, response);
        }
    }
    
    private String saveFile(Part part, Long clinicId, Long documentId) throws IOException {
        if (part.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido (10MB)");
        }
        
        String fileName = part.getSubmittedFileName();
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        
        // Crear estructura de directorios: uploads/{clinicId}/{year}/{documentId}/
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String uploadPath = UPLOAD_DIR + File.separator + clinicId + File.separator + year + File.separator + documentId;
        
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // Generar nombre único para el archivo
        String extension = "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = fileName.substring(lastDot);
        }
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        
        Path filePath = uploadDir.resolve(uniqueFileName);
        
        // Guardar archivo
        try (InputStream inputStream = part.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        // Retornar ruta relativa
        return uploadPath + File.separator + uniqueFileName;
    }
    
    private void downloadFile(HttpServletRequest request, HttpServletResponse response, Long documentId)
            throws ServletException, IOException {
        
        try {
            ClinicalDocument document = documentService.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
            
            if (document.getAttachments() == null || document.getAttachments().isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No hay archivos adjuntos");
                return;
            }
            
            // Parsear JSON de attachments
            ArrayNode attachments = (ArrayNode) objectMapper.readTree(document.getAttachments());
            if (attachments.size() == 0) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No hay archivos adjuntos");
                return;
            }
            
            // Por ahora, descargar el primer archivo
            // TODO: Permitir seleccionar qué archivo descargar
            ObjectNode firstAttachment = (ObjectNode) attachments.get(0);
            String filePath = firstAttachment.get("filePath").asText();
            String fileName = firstAttachment.get("fileName").asText();
            
            Path file = Paths.get(filePath);
            if (!Files.exists(file)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Archivo no encontrado");
                return;
            }
            
            response.setContentType(firstAttachment.get("mimeType").asText());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            
            Files.copy(file, response.getOutputStream());
            
        } catch (Exception e) {
            logger.error("Error al descargar archivo", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al descargar archivo");
        }
    }
    
    private String buildVitalSignsJson(HttpServletRequest request) {
        try {
            ObjectNode vitalSigns = objectMapper.createObjectNode();
            
            String pressure = request.getParameter("vitalPressure");
            String temperature = request.getParameter("vitalTemperature");
            String pulse = request.getParameter("vitalPulse");
            String respiratoryRate = request.getParameter("vitalRespiratoryRate");
            String o2Saturation = request.getParameter("vitalO2Saturation");
            String weight = request.getParameter("vitalWeight");
            String height = request.getParameter("vitalHeight");
            
            if (pressure != null && !pressure.isEmpty()) vitalSigns.put("pressure", pressure);
            if (temperature != null && !temperature.isEmpty()) vitalSigns.put("temperature", temperature);
            if (pulse != null && !pulse.isEmpty()) vitalSigns.put("pulse", pulse);
            if (respiratoryRate != null && !respiratoryRate.isEmpty()) vitalSigns.put("respiratoryRate", respiratoryRate);
            if (o2Saturation != null && !o2Saturation.isEmpty()) vitalSigns.put("o2Saturation", o2Saturation);
            if (weight != null && !weight.isEmpty()) vitalSigns.put("weight", weight);
            if (height != null && !height.isEmpty()) vitalSigns.put("height", height);
            
            // Calcular IMC si se proporcionan peso y altura
            if (weight != null && !weight.isEmpty() && height != null && !height.isEmpty()) {
                try {
                    double weightKg = Double.parseDouble(weight);
                    double heightM = Double.parseDouble(height) / 100.0; // convertir cm a m
                    if (heightM > 0) {
                        double bmi = weightKg / (heightM * heightM);
                        vitalSigns.put("bmi", String.format("%.2f", bmi));
                    }
                } catch (NumberFormatException e) {
                    // Ignorar si no se pueden parsear
                }
            }
            
            return vitalSigns.toString();
        } catch (Exception e) {
            logger.warn("Error al construir JSON de signos vitales", e);
            return "{}";
        }
    }
    
    private String buildPrescriptionsJson(HttpServletRequest request) {
        try {
            ArrayNode prescriptions = objectMapper.createArrayNode();
            
            // Buscar todos los parámetros de prescripciones
            // Formato esperado: prescription_medication_0, prescription_dosage_0, etc.
            // Buscar hasta encontrar un rango sin parámetros (buscar hasta índice 100 como máximo)
            int maxIndex = 100;
            int foundCount = 0;
            
            for (int index = 0; index < maxIndex; index++) {
                String medication = request.getParameter("prescription_medication_" + index);
                String dosage = request.getParameter("prescription_dosage_" + index);
                String frequency = request.getParameter("prescription_frequency_" + index);
                String duration = request.getParameter("prescription_duration_" + index);
                
                // Si no hay ningún parámetro para este índice, y ya encontramos al menos uno antes,
                // puede que hayamos llegado al final. Pero continuamos por si hay huecos.
                if (medication == null && dosage == null && frequency == null && duration == null) {
                    // Si ya no encontramos nada en los últimos 5 índices y encontramos algo antes, parar
                    if (foundCount > 0 && index > foundCount + 5) {
                        break;
                    }
                    continue;
                }
                
                // Solo agregar si hay al menos un medicamento (no vacío)
                if (medication != null && !medication.trim().isEmpty()) {
                    ObjectNode prescription = objectMapper.createObjectNode();
                    prescription.put("medication", medication.trim());
                    if (dosage != null && !dosage.trim().isEmpty()) {
                        prescription.put("dosage", dosage.trim());
                    }
                    if (frequency != null && !frequency.trim().isEmpty()) {
                        prescription.put("frequency", frequency.trim());
                    }
                    if (duration != null && !duration.trim().isEmpty()) {
                        prescription.put("duration", duration.trim());
                    }
                    
                    prescriptions.add(prescription);
                    foundCount = index + 1;
                }
            }
            
            return prescriptions.toString();
        } catch (Exception e) {
            logger.warn("Error al construir JSON de prescripciones", e);
            return "[]";
        }
    }
}

