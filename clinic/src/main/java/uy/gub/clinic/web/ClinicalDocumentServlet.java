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
import java.util.Collection;
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
        
        String action = request.getParameter("action");
        String documentId = request.getParameter("id");
        
            // Acción de descarga de archivo - procesar primero, antes de establecer contentType HTML
            if ("download".equals(action) && documentId != null) {
                try {
                    String fileIndexStr = request.getParameter("fileIndex");
                    int fileIndex = 0;
                    if (fileIndexStr != null && !fileIndexStr.isEmpty()) {
                        try {
                            fileIndex = Integer.parseInt(fileIndexStr);
                        } catch (NumberFormatException e) {
                            logger.warn("Índice de archivo inválido: {}", fileIndexStr);
                        }
                    }
                    logger.info("Solicitud de descarga: documentId={}, fileIndex={}", documentId, fileIndex);
                    downloadFile(request, response, Long.parseLong(documentId), fileIndex);
                    return;
                } catch (Exception e) {
                    logger.error("Error al procesar solicitud de descarga", e);
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.setContentType("text/plain; charset=UTF-8");
                    response.getWriter().write("Error al descargar archivo: " + e.getMessage());
                    return;
                }
            }
            
            // Acción de eliminar archivo adjunto
            if ("deleteAttachment".equals(action) && documentId != null) {
                try {
                    String fileIndexStr = request.getParameter("fileIndex");
                    if (fileIndexStr != null && !fileIndexStr.isEmpty()) {
                        int fileIndex = Integer.parseInt(fileIndexStr);
                        logger.info("Solicitud de eliminación de archivo: documentId={}, fileIndex={}", documentId, fileIndex);
                        deleteAttachment(request, response, Long.parseLong(documentId), fileIndex);
                        return;
                    }
                } catch (Exception e) {
                    logger.error("Error al procesar solicitud de eliminación de archivo", e);
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.setContentType("text/plain; charset=UTF-8");
                    response.getWriter().write("Error al eliminar archivo: " + e.getMessage());
                    return;
                }
            }
        
        // Para otras acciones, establecer contentType HTML
        response.setContentType("text/html; charset=UTF-8");
        
        try {
            String clinicId = (String) request.getSession().getAttribute("clinicId");
            if (clinicId == null) {
                request.setAttribute("error", "Error de sesión: Clínica no identificada");
                request.getRequestDispatcher("/WEB-INF/views/admin/documents.jsp").forward(request, response);
                return;
            }

            // Acción para abrir modal de agregar documento
            if ("add".equals(action) || "create".equals(action)) {
                request.setAttribute("action", "add");
            }

            // Acción para obtener un documento específico (para ver o editar)
            if (("view".equals(action) || "edit".equals(action)) && documentId != null) {
                Optional<ClinicalDocument> docOpt = documentService.findById(Long.parseLong(documentId));
                if (docOpt.isPresent()) {
                    ClinicalDocument doc = docOpt.get();
                    // Verificar que el documento pertenece a la clínica del usuario
                    String docClinicId = doc.getClinic() != null ? doc.getClinic().getId() : null;
                    if (docClinicId != null && docClinicId.equals(clinicId)) {
                        request.setAttribute("selectedDocument", doc);
                        request.setAttribute("action", action);
                    } else {
                        request.setAttribute("error", "Documento no encontrado o no pertenece a su clínica");
                    }
                } else {
                    request.setAttribute("error", "Documento no encontrado");
                }
            }
            
            // Obtener documentos
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
            
            documents = documentService.searchDocuments(
                clinicId, specialtyId, patientId, professionalId, 
                documentType, dateFrom, dateTo);
            
            request.setAttribute("documents", documents);
            
            // Cargar datos para filtros y formularios
            List<Patient> patients = patientService.getPatientsByClinic(clinicId);
            List<Professional> professionals = professionalService.getProfessionalsByClinic(clinicId);
            // Las especialidades ahora son globales (sin filtrar por clínica)
            List<Specialty> specialties = specialtyService.getAllSpecialties();
            
            request.setAttribute("patients", patients);
            request.setAttribute("professionals", professionals);
            request.setAttribute("specialties", specialties);
            
        } catch (Exception e) {
            logger.error("Error al obtener documentos", e);
            request.setAttribute("error", "Error al cargar documentos: " + e.getMessage());
        }
        
        request.getRequestDispatcher("/WEB-INF/views/admin/documents.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        
        try {
            String clinicId = (String) request.getSession().getAttribute("clinicId");
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
    
    private void createDocument(HttpServletRequest request, HttpServletResponse response, String clinicId)
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
            try {
                Collection<Part> parts = request.getParts();
                if (parts != null) {
                    for (Part part : parts) {
                        if (part.getName() != null && part.getName().equals("attachments")) {
                            // Verificar que sea un archivo (tiene ContentType y SubmittedFileName)
                            String fileName = part.getSubmittedFileName();
                            if (fileName != null && !fileName.isEmpty() && part.getSize() > 0) {
                                logger.info("Procesando archivo adjunto: {} (tamaño: {} bytes)", fileName, part.getSize());
                                String savedFilePath = saveFile(part, clinicId, savedDocument.getId());
                                if (savedFilePath != null) {
                                    ObjectNode attachment = objectMapper.createObjectNode();
                                    attachment.put("fileName", fileName);
                                    attachment.put("filePath", savedFilePath);
                                    attachment.put("fileSize", part.getSize());
                                    String contentType = part.getContentType();
                                    attachment.put("mimeType", contentType != null ? contentType : "application/octet-stream");
                                    attachments.add(attachment);
                                    logger.info("Archivo adjunto guardado: {} -> {}", fileName, savedFilePath);
                                } else {
                                    logger.warn("No se pudo guardar el archivo: {}", fileName);
                                }
                            } else {
                                logger.debug("Part 'attachments' ignorado: fileName={}, size={}", fileName, part.getSize());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error al procesar archivos adjuntos", e);
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
    
    private void updateDocument(HttpServletRequest request, HttpServletResponse response, String clinicId)
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
            try {
                Collection<Part> parts = request.getParts();
                if (parts != null) {
                    for (Part part : parts) {
                        if (part.getName() != null && part.getName().equals("attachments")) {
                            // Verificar que sea un archivo (tiene ContentType y SubmittedFileName)
                            String fileName = part.getSubmittedFileName();
                            if (fileName != null && !fileName.isEmpty() && part.getSize() > 0) {
                                logger.info("Procesando archivo adjunto en actualización: {} (tamaño: {} bytes)", fileName, part.getSize());
                                String savedFilePath = saveFile(part, clinicId, documentId);
                                if (savedFilePath != null) {
                                    ObjectNode attachment = objectMapper.createObjectNode();
                                    attachment.put("fileName", fileName);
                                    attachment.put("filePath", savedFilePath);
                                    attachment.put("fileSize", part.getSize());
                                    String contentType = part.getContentType();
                                    attachment.put("mimeType", contentType != null ? contentType : "application/octet-stream");
                                    newAttachments.add(attachment);
                                    logger.info("Archivo adjunto guardado en actualización: {} -> {}", fileName, savedFilePath);
                                } else {
                                    logger.warn("No se pudo guardar el archivo en actualización: {}", fileName);
                                }
                            } else {
                                logger.debug("Part 'attachments' ignorado en actualización: fileName={}, size={}", fileName, part.getSize());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error al procesar archivos adjuntos en actualización", e);
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
    
    /**
     * Obtiene la ruta base para almacenar archivos.
     * Usa una ruta fija dentro del proyecto clinic para garantizar persistencia.
     */
    private String getUploadBasePath() {
        // Ruta fija dentro del proyecto clinic (ruta absoluta): C:/TSEGrupo/tse-2025/clinic/uploads
        // Esta ruta es estable y no depende de rutas temporales de WildFly
        String clinicUploadsDir = "C:" + File.separator + "TSEGrupo" + File.separator + "tse-2025" + File.separator + "clinic" + File.separator + "uploads";
        
        // Crear el directorio si no existe
        Path uploadsPath = Paths.get(clinicUploadsDir);
        if (!Files.exists(uploadsPath)) {
            try {
                Files.createDirectories(uploadsPath);
                logger.info("Directorio de uploads creado: {}", uploadsPath.toAbsolutePath());
            } catch (IOException e) {
                logger.error("Error al crear directorio de uploads: {}", clinicUploadsDir, e);
                // Fallback a jboss.server.data.dir si falla
                String wildflyDataDir = System.getProperty("jboss.server.data.dir");
                if (wildflyDataDir != null) {
                    logger.warn("Usando jboss.server.data.dir como fallback: {}", wildflyDataDir);
                    return wildflyDataDir;
                }
            }
        }
        
        logger.info("Usando ruta fija para uploads: {}", clinicUploadsDir);
        return clinicUploadsDir;
    }
    
    private String saveFile(Part part, String clinicId, Long documentId) throws IOException {
        if (part.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido (10MB)");
        }
        
        String fileName = part.getSubmittedFileName();
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        
        // Obtener la ruta base usando el método común
        String basePath = getUploadBasePath();
        
        // Crear estructura de directorios: uploads/{clinicId}/{year}/{documentId}/
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String relativePath = UPLOAD_DIR + File.separator + clinicId + File.separator + year + File.separator + documentId;
        Path uploadDir = Paths.get(basePath, relativePath);
        
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
            logger.info("Directorio de uploads creado: {}", uploadDir.toAbsolutePath());
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
            logger.info("Archivo guardado: {} -> {}", fileName, filePath.toAbsolutePath());
        }
        
        // Retornar ruta relativa para almacenar en la base de datos (usando / para compatibilidad)
        String relativePathForDb = UPLOAD_DIR + "/" + clinicId + "/" + year + "/" + documentId + "/" + uniqueFileName;
        logger.info("Ruta relativa guardada en BD: {}", relativePathForDb);
        return relativePathForDb;
    }
    
    private void downloadFile(HttpServletRequest request, HttpServletResponse response, Long documentId, int fileIndex)
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
            
            if (fileIndex < 0 || fileIndex >= attachments.size()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Índice de archivo inválido");
                return;
            }
            
            ObjectNode attachment = (ObjectNode) attachments.get(fileIndex);
            String filePath = attachment.get("filePath").asText();
            String fileName = attachment.get("fileName").asText();
            
            logger.info("Intentando descargar archivo. Ruta en BD: {}", filePath);
            
            // Normalizar separadores de ruta (convertir / a File.separator)
            String normalizedPath = filePath.replace("/", File.separator).replace("\\", File.separator);
            
            // Usar el mismo método para obtener la ruta base que se usó al guardar
            String basePath = getUploadBasePath();
            Path file = Paths.get(basePath, normalizedPath);
            
            logger.info("Buscando archivo en: {}", file.toAbsolutePath());
            
            // Si no existe en la ubicación principal, intentar buscar en ubicaciones alternativas
            // (para archivos antiguos que puedan haber sido guardados en otras ubicaciones)
            if (!Files.exists(file)) {
                logger.warn("Archivo no encontrado en ubicación principal, buscando en ubicaciones alternativas...");
                
                // Lista de ubicaciones alternativas donde pueden estar archivos antiguos
                java.util.List<String> alternativePaths = new java.util.ArrayList<>();
                
                // 1. Intentar con getRealPath (para archivos antiguos en rutas temporales)
                String appPath = getServletContext().getRealPath("/");
                if (appPath != null) {
                    alternativePaths.add(appPath);
                }
                
                // 2. jboss.server.data.dir
                String wildflyDataDir = System.getProperty("jboss.server.data.dir");
                if (wildflyDataDir != null) {
                    alternativePaths.add(wildflyDataDir);
                }
                
                // 3. jboss.server.base.dir
                String wildflyBaseDir = System.getProperty("jboss.server.base.dir");
                if (wildflyBaseDir != null) {
                    alternativePaths.add(wildflyBaseDir);
                }
                
                // 4. user.dir
                String userDir = System.getProperty("user.dir");
                if (userDir != null) {
                    alternativePaths.add(userDir);
                }
                
                // Buscar en todas las ubicaciones alternativas
                for (String altPath : alternativePaths) {
                    if (altPath.equals(basePath)) {
                        continue; // Ya buscamos aquí
                    }
                    Path altFile = Paths.get(altPath, normalizedPath);
                    logger.info("Buscando en ubicación alternativa: {}", altFile.toAbsolutePath());
                    if (Files.exists(altFile)) {
                        file = altFile;
                        logger.info("Archivo encontrado en ubicación alternativa: {}", altPath);
                        break;
                    }
                }
            }
            
            if (!Files.exists(file)) {
                logger.error("Archivo no encontrado después de buscar en todas las ubicaciones");
                logger.error("Ruta en BD: {}", filePath);
                logger.error("Ruta normalizada: {}", normalizedPath);
                logger.error("Base path usado: {}", basePath);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Archivo no encontrado: " + fileName + ". Verifique los logs del servidor.");
                return;
            }
            
            logger.info("Archivo encontrado: {}", file.toAbsolutePath());
            
            String mimeType = attachment.has("mimeType") ? attachment.get("mimeType").asText() : "application/octet-stream";
            
            // Limpiar cualquier header previo y establecer headers correctos
            response.reset();
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setContentLengthLong(Files.size(file));
            
            // Copiar archivo al output stream
            Files.copy(file, response.getOutputStream());
            response.getOutputStream().flush();
            
        } catch (IllegalArgumentException e) {
            logger.error("Error al descargar archivo - Documento no encontrado: {}", e.getMessage());
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Documento no encontrado: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error al descargar archivo", e);
            if (!response.isCommitted()) {
                response.reset();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("Error al descargar archivo: " + e.getMessage());
            }
        }
    }
    
    private void deleteAttachment(HttpServletRequest request, HttpServletResponse response, Long documentId, int fileIndex)
            throws ServletException, IOException {
        
        try {
            // Verificar permisos
            Long clinicId = (Long) request.getSession().getAttribute("clinicId");
            if (clinicId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("No autorizado para eliminar archivos");
                return;
            }
            
            // Obtener el documento
            ClinicalDocument document = documentService.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
            
            // Verificar que el documento pertenece a la clínica del usuario
            if (document.getClinic() == null || !document.getClinic().getId().equals(clinicId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("No tiene permiso para eliminar archivos de este documento");
                return;
            }
            
            if (document.getAttachments() == null || document.getAttachments().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("No hay archivos adjuntos en este documento");
                return;
            }
            
            // Parsear JSON de attachments
            ArrayNode attachments = (ArrayNode) objectMapper.readTree(document.getAttachments());
            if (attachments.size() == 0) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("No hay archivos adjuntos en este documento");
                return;
            }
            
            if (fileIndex < 0 || fileIndex >= attachments.size()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("Índice de archivo inválido");
                return;
            }
            
            // Obtener información del archivo a eliminar
            ObjectNode attachmentToDelete = (ObjectNode) attachments.get(fileIndex);
            String filePath = attachmentToDelete.get("filePath").asText();
            String fileName = attachmentToDelete.get("fileName").asText();
            
            logger.info("Eliminando archivo adjunto: {} (índice: {}) del documento {}", fileName, fileIndex, documentId);
            
            // Eliminar archivo físico
            String normalizedPath = filePath.replace("/", File.separator).replace("\\", File.separator);
            String basePath = getUploadBasePath();
            Path file = Paths.get(basePath, normalizedPath);
            
            // Intentar eliminar el archivo físico
            boolean fileDeleted = false;
            if (Files.exists(file)) {
                try {
                    Files.delete(file);
                    fileDeleted = true;
                    logger.info("Archivo físico eliminado: {}", file.toAbsolutePath());
                    
                    // Intentar eliminar el directorio si está vacío
                    try {
                        Path parentDir = file.getParent();
                        if (parentDir != null && Files.exists(parentDir)) {
                            java.io.File[] filesInDir = parentDir.toFile().listFiles();
                            if (filesInDir == null || filesInDir.length == 0) {
                                Files.delete(parentDir);
                                logger.info("Directorio vacío eliminado: {}", parentDir);
                            }
                        }
                    } catch (Exception e) {
                        // No es crítico si no se puede eliminar el directorio
                        logger.debug("No se pudo eliminar el directorio (puede no estar vacío): {}", e.getMessage());
                    }
                } catch (IOException e) {
                    logger.warn("No se pudo eliminar el archivo físico (puede que ya no exista): {}", file.toAbsolutePath(), e);
                    // Continuar con la eliminación de la referencia en la BD aunque el archivo físico no exista
                }
            } else {
                logger.warn("Archivo físico no encontrado (puede que ya haya sido eliminado): {}", file.toAbsolutePath());
            }
            
            // Eliminar la referencia del JSON
            ArrayNode newAttachments = objectMapper.createArrayNode();
            for (int i = 0; i < attachments.size(); i++) {
                if (i != fileIndex) {
                    newAttachments.add(attachments.get(i));
                }
            }
            
            // Actualizar el documento en la base de datos
            if (newAttachments.size() == 0) {
                document.setAttachments(null);
            } else {
                document.setAttachments(newAttachments.toString());
            }
            
            documentService.updateDocument(document);
            
            logger.info("Archivo adjunto eliminado exitosamente: {} (índice: {}) del documento {}", fileName, fileIndex, documentId);
            
            // Devolver respuesta JSON
            response.setContentType("application/json; charset=UTF-8");
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", true);
            result.put("message", "Archivo eliminado exitosamente");
            result.put("fileDeleted", fileDeleted);
            response.getWriter().write(result.toString());
            
        } catch (IllegalArgumentException e) {
            logger.error("Error al eliminar archivo adjunto - Documento no encontrado: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json; charset=UTF-8");
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("error", "Documento no encontrado: " + e.getMessage());
            response.getWriter().write(result.toString());
        } catch (Exception e) {
            logger.error("Error al eliminar archivo adjunto", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json; charset=UTF-8");
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("error", "Error al eliminar archivo: " + e.getMessage());
            response.getWriter().write(result.toString());
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
            
            // Estrategia mejorada para formularios multipart:
            // 1. Primero intentar obtener parámetros usando getParameter() (funciona para multipart parseado)
            // 2. Si no encontramos nada, usar getParts() como fallback
            java.util.Set<Integer> prescriptionIndices = new java.util.HashSet<>();
            java.util.Map<String, String> paramMap = new java.util.HashMap<>();
            
            // Método 1: Usar getParameterNames (más rápido si el multipart ya está parseado)
            java.util.Enumeration<String> allParamNames = request.getParameterNames();
            while (allParamNames.hasMoreElements()) {
                String paramName = allParamNames.nextElement();
                // Guardar todos los parámetros en un mapa para acceso rápido
                paramMap.put(paramName, request.getParameter(paramName));
            }
            
            // También intentar leer de getParts() si es multipart, para asegurar que capturamos todo
            // Esto puede ayudar si algunos parámetros no se capturaron con getParameter()
            if (request.getContentType() != null && request.getContentType().startsWith("multipart")) {
                try {
                    for (Part part : request.getParts()) {
                        String fieldName = part.getName();
                        // Solo procesar campos de formulario (no archivos)
                        if (part.getContentType() == null || part.getSize() < 1000000) { // Menos de 1MB, probablemente un campo
                            java.io.BufferedReader reader = new java.io.BufferedReader(
                                new java.io.InputStreamReader(part.getInputStream(), "UTF-8"));
                            StringBuilder value = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (value.length() > 0) value.append("\n");
                                value.append(line);
                            }
                            String partValue = value.toString();
                            // Actualizar el mapa si no estaba o si estaba vacío pero ahora tiene valor
                            if (!paramMap.containsKey(fieldName)) {
                                paramMap.put(fieldName, partValue);
                            } else {
                                String existingValue = paramMap.get(fieldName);
                                // Si el valor existente está vacío pero el de Part tiene valor, actualizar
                                if ((existingValue == null || existingValue.isEmpty()) && !partValue.isEmpty()) {
                                    paramMap.put(fieldName, partValue);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error al procesar Parts: {}", e.getMessage());
                }
            }
            
            // Buscar todos los índices de prescripción
            for (String paramName : paramMap.keySet()) {
                if (paramName.startsWith("prescription_medication_")) {
                    try {
                        String indexStr = paramName.substring("prescription_medication_".length());
                        int index = Integer.parseInt(indexStr);
                        prescriptionIndices.add(index);
                    } catch (NumberFormatException e) {
                        // Ignorar índices inválidos
                    }
                }
            }
            
            // Procesar cada índice encontrado (ordenados para mantener consistencia)
            java.util.List<Integer> sortedIndices = new java.util.ArrayList<>(prescriptionIndices);
            java.util.Collections.sort(sortedIndices);
            
            for (Integer index : sortedIndices) {
                // Usar el mapa de parámetros primero, luego getParameter como fallback
                String medication = paramMap.getOrDefault("prescription_medication_" + index, 
                    request.getParameter("prescription_medication_" + index));
                String dosage = paramMap.getOrDefault("prescription_dosage_" + index,
                    request.getParameter("prescription_dosage_" + index));
                String frequency = paramMap.getOrDefault("prescription_frequency_" + index,
                    request.getParameter("prescription_frequency_" + index));
                String duration = paramMap.getOrDefault("prescription_duration_" + index,
                    request.getParameter("prescription_duration_" + index));
                
                // Solo agregar si hay al menos un medicamento (no vacío)
                if (medication != null && !medication.trim().isEmpty()) {
                    ObjectNode prescription = objectMapper.createObjectNode();
                    prescription.put("medication", medication.trim());
                    
                    if (dosage != null && !dosage.trim().isEmpty()) {
                        prescription.put("dosage", dosage.trim());
                    } else {
                        prescription.put("dosage", "");
                    }
                    
                    if (frequency != null && !frequency.trim().isEmpty()) {
                        prescription.put("frequency", frequency.trim());
                    } else {
                        prescription.put("frequency", "");
                    }
                    
                    if (duration != null && !duration.trim().isEmpty()) {
                        prescription.put("duration", duration.trim());
                    } else {
                        prescription.put("duration", "");
                    }
                    
                    prescriptions.add(prescription);
                }
            }
            
            return prescriptions.toString();
            
        } catch (Exception e) {
            logger.error("Error al construir JSON de prescripciones", e);
            return "[]";
        }
    }
}

