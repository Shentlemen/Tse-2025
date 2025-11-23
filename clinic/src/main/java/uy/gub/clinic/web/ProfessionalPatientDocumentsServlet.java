package uy.gub.clinic.web;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
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
import uy.gub.clinic.entity.AccessRequest;
import uy.gub.clinic.integration.dto.hcen.HcenDocumentDetailDTO;
import uy.gub.clinic.integration.dto.hcen.HcenDocumentListItemDTO;
import uy.gub.clinic.service.ClinicalDocumentService;
import uy.gub.clinic.service.PatientService;
import uy.gub.clinic.service.ProfessionalService;
import uy.gub.clinic.service.SpecialtyService;
import uy.gub.clinic.service.AccessRequestService;
import uy.gub.clinic.service.ClinicService;
import uy.gub.clinic.service.HcenDocumentService;
import uy.gub.clinic.service.dto.RemoteDocumentContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servlet para gestionar la vista de documentos de un paciente para profesionales
 */
@MultipartConfig(
    maxFileSize = 10485760, // 10MB
    maxRequestSize = 52428800 // 50MB total
)
@WebServlet("/professional/patient-documents")
public class ProfessionalPatientDocumentsServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ProfessionalPatientDocumentsServlet.class);
    private static final String UPLOAD_DIR = "uploads";
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    private PatientService patientService;
    
    @Inject
    private ClinicalDocumentService documentService;
    
    @Inject
    private ProfessionalService professionalService;
    
    @Inject
    private SpecialtyService specialtyService;
    
    @Inject
    private AccessRequestService accessRequestService;
    
    @Inject
    private ClinicService clinicService;

    @Inject
    private HcenDocumentService hcenDocumentService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        try {
            // Validar sesión
            String clinicId = (String) request.getSession().getAttribute("clinicId");
            if (clinicId == null) {
                request.setAttribute("error", "Error de sesión: Clínica no identificada");
                request.getRequestDispatcher("/professional/patient-documents.jsp").forward(request, response);
                return;
            }

            Clinic clinicEntity = clinicService.getClinicById(clinicId)
                .orElseThrow(() -> new ServletException("Clínica no encontrada"));
            
            // Obtener ID del paciente
            String patientIdStr = request.getParameter("patientId");
            if (patientIdStr == null || patientIdStr.trim().isEmpty()) {
                request.setAttribute("error", "ID de paciente no proporcionado");
                request.getRequestDispatcher("/professional/patient-documents.jsp").forward(request, response);
                return;
            }
            
            Long patientId = Long.parseLong(patientIdStr);
            
            // Buscar paciente
            Optional<Patient> patientOpt = patientService.getPatientById(patientId);
            if (patientOpt.isEmpty()) {
                request.setAttribute("error", "Paciente no encontrado");
                request.getRequestDispatcher("/professional/patient-documents.jsp").forward(request, response);
                return;
            }
            
            Patient patient = patientOpt.get();
            
            // Verificar que el paciente pertenece a la clínica del profesional
            if (patient.getClinic() == null || !patient.getClinic().getId().equals(clinicId)) {
                request.setAttribute("error", "El paciente no pertenece a su clínica");
                request.getRequestDispatcher("/professional/patient-documents.jsp").forward(request, response);
                return;
            }
            
            // Verificar si es acción de ver documento
            String action = request.getParameter("action");
            String documentIdStr = request.getParameter("documentId");
            String remoteDocumentIdStr = request.getParameter("remoteDocumentId");
            
            if ("downloadRemote".equals(action) && remoteDocumentIdStr != null) {
                handleRemoteDocumentDownload(request, response, clinicEntity, patient, remoteDocumentIdStr);
                return;
            }

            if ("view".equals(action) && documentIdStr != null) {
                // Cargar el documento para verlo en el modal
                Optional<ClinicalDocument> docOpt = documentService.findById(Long.parseLong(documentIdStr));
                if (docOpt.isPresent()) {
                    ClinicalDocument doc = docOpt.get();
                    // Verificar que el documento pertenece a la clínica del profesional
                    if (doc.getClinic() != null && doc.getClinic().getId().equals(clinicId)) {
                        request.setAttribute("selectedDocument", doc);
                        request.setAttribute("viewDocument", true);
                    } else {
                        request.setAttribute("error", "Documento no encontrado o no pertenece a su clínica");
                    }
                } else {
                    request.setAttribute("error", "Documento no encontrado");
                }
            }

            if ("viewRemote".equals(action) && remoteDocumentIdStr != null) {
                prepareRemoteDocumentView(request, clinicEntity, patient, remoteDocumentIdStr);
            }
            
            if ("edit".equals(action) && documentIdStr != null) {
                // Cargar el documento para editarlo en el modal
                Optional<ClinicalDocument> docOpt = documentService.findById(Long.parseLong(documentIdStr));
                if (docOpt.isPresent()) {
                    ClinicalDocument doc = docOpt.get();
                    // Verificar que el documento pertenece a la clínica del profesional
                    if (doc.getClinic() != null && doc.getClinic().getId().equals(clinicId)) {
                        request.setAttribute("selectedDocument", doc);
                        request.setAttribute("editDocument", true);
                    } else {
                        request.setAttribute("error", "Documento no encontrado o no pertenece a su clínica");
                    }
                } else {
                    request.setAttribute("error", "Documento no encontrado");
                }
            }
            
            // Obtener todos los documentos del paciente
            List<ClinicalDocument> documents = documentService.findByPatient(patientId);

            // Obtener documentos disponibles en HCEN
            if (patient.getDocumentNumber() != null && !patient.getDocumentNumber().isBlank()) {
                try {
                    List<HcenDocumentListItemDTO> externalDocs =
                            hcenDocumentService.fetchDocumentsForPatient(clinicEntity, patient.getDocumentNumber());
                    request.setAttribute("externalDocuments", externalDocs);
                } catch (Exception ex) {
                    logger.error("Error al obtener documentos externos de HCEN", ex);
                    request.setAttribute("externalDocuments", java.util.Collections.emptyList());
                    request.setAttribute("externalDocumentsError",
                            "No se pudieron obtener los documentos disponibles en HCEN.");
                }
            } else {
                request.setAttribute("externalDocuments",
                        java.util.Collections.emptyList());
                request.setAttribute("externalDocumentsInfo",
                        "El paciente no tiene cédula registrada, por lo que no es posible consultar HCEN.");
            }
            
            // Ordenar por fecha de visita descendente (más recientes primero)
            documents.sort((d1, d2) -> {
                if (d1.getDateOfVisit() == null && d2.getDateOfVisit() == null) return 0;
                if (d1.getDateOfVisit() == null) return 1;
                if (d2.getDateOfVisit() == null) return -1;
                return d2.getDateOfVisit().compareTo(d1.getDateOfVisit());
            });
            
            // Cargar datos para el formulario de nuevo documento (si es necesario)
            List<Professional> professionals = professionalService.getProfessionalsByClinic(clinicId);
            List<Specialty> specialties = specialtyService.getAllSpecialties();
            
            // Obtener el ID del profesional logueado para pre-seleccionarlo
            Long loggedProfessionalId = (Long) request.getSession().getAttribute("professionalId");
            
            request.setAttribute("patient", patient);
            request.setAttribute("documents", documents);
            request.setAttribute("professionals", professionals);
            request.setAttribute("specialties", specialties);
            request.setAttribute("loggedProfessionalId", loggedProfessionalId);
            
        } catch (NumberFormatException e) {
            request.setAttribute("error", "ID de paciente inválido");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error al cargar documentos: " + e.getMessage());
        }

        request.getRequestDispatcher("/professional/patient-documents.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        
        try {
            String clinicId = (String) request.getSession().getAttribute("clinicId");
            Long professionalId = (Long) request.getSession().getAttribute("professionalId");

            if (clinicId == null) {
                request.setAttribute("error", "Error de sesión: Clínica no identificada");
                doGet(request, response);
                return;
            }
            
            if (professionalId == null) {
                request.setAttribute("error", "Error de sesión: Profesional no identificado");
                doGet(request, response);
                return;
            }
            
            switch (action) {
                case "create":
                    createDocument(request, response, clinicId, professionalId);
                    break;
                case "update":
                    updateDocument(request, response, clinicId, professionalId);
                    break;
                case "requestAccess":
                    requestAccessToHCEN(request, response, clinicId, professionalId);
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

    private void prepareRemoteDocumentView(HttpServletRequest request,
                                           Clinic clinic,
                                           Patient patient,
                                           String remoteDocumentIdStr) {
        try {
            if (patient.getDocumentNumber() == null || patient.getDocumentNumber().isBlank()) {
                request.setAttribute("error", "El paciente no tiene cédula registrada para consultar HCEN.");
                return;
            }

            Long documentId = Long.parseLong(remoteDocumentIdStr);

            Optional<HcenDocumentDetailDTO> detailOpt = hcenDocumentService.fetchDocumentDetail(
                    clinic,
                    patient.getDocumentNumber(),
                    documentId
            );

            if (detailOpt.isEmpty()) {
                request.setAttribute("error", "No se encontró el documento en HCEN o no tiene acceso.");
                return;
            }

            request.setAttribute("selectedRemoteDocument", detailOpt.get());
            request.setAttribute("viewRemoteDocument", true);

            // Get professional's specialty name
            String specialtyName = getProfessionalSpecialtyName(request);

            Optional<RemoteDocumentContent> contentOpt = hcenDocumentService.fetchDocumentContent(
                    clinic,
                    patient.getDocumentNumber(),
                    documentId,
                    specialtyName
            );

            if (contentOpt.isPresent()) {
                RemoteDocumentContent content = contentOpt.get();
                if (content.isBinary()) {
                    content.setBinaryData(null); // liberar memoria, solo necesitamos saber que es binario
                    request.setAttribute("remoteDocumentHasBinaryContent", true);
                    request.setAttribute("remoteDocumentBinaryId", documentId);
                    request.setAttribute("remoteDocumentBinaryContentType", content.getContentType());
                } else if (content.getInlineContent() != null) {
                    request.setAttribute("remoteDocumentInlineContent", content.getInlineContent());
                    request.setAttribute("remoteDocumentInlineContentType", content.getContentType());
                }
            }
        } catch (Exception ex) {
            // Check if the exception is or wraps an AccessDeniedException
            uy.gub.clinic.exception.AccessDeniedException accessDenied = extractAccessDeniedException(ex);
            if (accessDenied != null) {
                logger.info("Access denied for document {} - patient CI: {}", accessDenied.getDocumentId(), accessDenied.getPatientCi());
                request.setAttribute("accessDeniedError", accessDenied.getMessage());
                request.setAttribute("deniedDocumentId", accessDenied.getDocumentId());
                // Keep viewRemoteDocument = true so the modal shows with the error message
                // User can manually request access from the modal
            } else if (ex instanceof NumberFormatException) {
                request.setAttribute("error", "Identificador de documento externo inválido.");
            } else {
                logger.error("Error al preparar la vista de documento externo", ex);
                request.setAttribute("error", "Ocurrió un error al consultar el documento en HCEN.");
            }
        }
    }

    private void handleRemoteDocumentDownload(HttpServletRequest request,
                                              HttpServletResponse response,
                                              Clinic clinic,
                                              Patient patient,
                                              String remoteDocumentIdStr) throws ServletException, IOException {
        if (patient.getDocumentNumber() == null || patient.getDocumentNumber().isBlank()) {
            throw new ServletException("El paciente no tiene cédula registrada para consultar HCEN.");
        }

        try {
            Long documentId = Long.parseLong(remoteDocumentIdStr);

            // Get professional's specialty name
            String specialtyName = getProfessionalSpecialtyName(request);

            Optional<RemoteDocumentContent> contentOpt = hcenDocumentService.fetchDocumentContent(
                    clinic,
                    patient.getDocumentNumber(),
                    documentId,
                    specialtyName
            );

            if (contentOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se pudo obtener el documento desde HCEN.");
                return;
            }

            RemoteDocumentContent content = contentOpt.get();
            if (content.isBinary() && content.getBinaryData() != null) {
                response.setContentType(content.getContentType() != null ? content.getContentType()
                        : "application/octet-stream");
                String fileName = content.getSuggestedFileName() != null
                        ? content.getSuggestedFileName()
                        : ("document-" + documentId);
                response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
                response.setContentLength(content.getBinaryData().length);
                response.getOutputStream().write(content.getBinaryData());
                response.flushBuffer();
            } else if (content.getInlineContent() != null) {
                response.setContentType(content.getContentType() != null ? content.getContentType()
                        : "application/json");
                byte[] data = content.getInlineContent().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                response.setHeader("Content-Disposition", "inline; filename=\"document-" + documentId + ".json\"");
                response.setContentLength(data.length);
                response.getOutputStream().write(data);
                response.flushBuffer();
            } else {
                response.sendError(HttpServletResponse.SC_NO_CONTENT, "El documento no tiene contenido disponible.");
            }
        } catch (Exception ex) {
            // Check if the exception is or wraps an AccessDeniedException
            uy.gub.clinic.exception.AccessDeniedException accessDenied = extractAccessDeniedException(ex);
            if (accessDenied != null) {
                logger.info("Access denied for document download {} - patient CI: {}", accessDenied.getDocumentId(), accessDenied.getPatientCi());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDenied.getMessage());
            } else if (ex instanceof NumberFormatException) {
                throw new ServletException("Identificador de documento externo inválido.", ex);
            } else {
                logger.error("Error al descargar documento externo desde HCEN", ex);
                throw new ServletException("No se pudo descargar el documento solicitado.", ex);
            }
        }
    }
    
    private void createDocument(HttpServletRequest request, HttpServletResponse response, String clinicId, Long professionalId)
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
            Long specialtyId = Long.parseLong(request.getParameter("specialtyId"));
            
            Patient patient = new Patient();
            patient.setId(patientId);
            document.setPatient(patient);
            
            Professional professional = new Professional();
            professional.setId(professionalId); // Usar el profesional de la sesión
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
                                }
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
        
        // Redirigir de vuelta a la página de documentos del paciente
        String patientIdStr = request.getParameter("patientId");
        response.sendRedirect(request.getContextPath() + "/professional/patient-documents?patientId=" + patientIdStr);
    }
    
    private void updateDocument(HttpServletRequest request, HttpServletResponse response, String clinicId, Long professionalId)
            throws ServletException, IOException {
        
        try {
            Long documentId = Long.parseLong(request.getParameter("documentId"));
            
            Optional<ClinicalDocument> docOpt = documentService.findById(documentId);
            if (docOpt.isEmpty()) {
                request.setAttribute("error", "Documento no encontrado");
                String patientIdStr = request.getParameter("patientId");
                response.sendRedirect(request.getContextPath() + "/professional/patient-documents?patientId=" + patientIdStr);
                return;
            }
            
            ClinicalDocument document = docOpt.get();
            
            // Verificar que el documento pertenece a la clínica del profesional
            if (document.getClinic() == null || !document.getClinic().getId().equals(clinicId)) {
                request.setAttribute("error", "No tiene permiso para editar este documento");
                String patientIdStr = request.getParameter("patientId");
                response.sendRedirect(request.getContextPath() + "/professional/patient-documents?patientId=" + patientIdStr);
                return;
            }
            
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
            
            // Actualizar especialidad si se proporciona
            String specialtyIdStr = request.getParameter("specialtyId");
            if (specialtyIdStr != null && !specialtyIdStr.isEmpty()) {
                Specialty specialty = new Specialty();
                specialty.setId(Long.parseLong(specialtyIdStr));
                document.setSpecialty(specialty);
            }
            
            // Procesar archivos adjuntos nuevos
            List<ObjectNode> newAttachments = new ArrayList<>();
            try {
                Collection<Part> parts = request.getParts();
                if (parts != null) {
                    for (Part part : parts) {
                        if (part.getName() != null && part.getName().equals("attachments")) {
                            String fileName = part.getSubmittedFileName();
                            if (fileName != null && !fileName.isEmpty() && part.getSize() > 0) {
                                String savedFilePath = saveFile(part, clinicId, documentId);
                                if (savedFilePath != null) {
                                    ObjectNode attachment = objectMapper.createObjectNode();
                                    attachment.put("fileName", fileName);
                                    attachment.put("filePath", savedFilePath);
                                    attachment.put("fileSize", part.getSize());
                                    String contentType = part.getContentType();
                                    attachment.put("mimeType", contentType != null ? contentType : "application/octet-stream");
                                    newAttachments.add(attachment);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error al procesar archivos adjuntos", e);
            }
            
            // Combinar archivos existentes con nuevos
            if (!newAttachments.isEmpty()) {
                ArrayNode existingAttachments = null;
                if (document.getAttachments() != null && !document.getAttachments().isEmpty()) {
                    try {
                        existingAttachments = (ArrayNode) objectMapper.readTree(document.getAttachments());
                    } catch (Exception e) {
                        logger.warn("Error al parsear archivos existentes", e);
                    }
                }
                
                if (existingAttachments == null) {
                    existingAttachments = objectMapper.createArrayNode();
                }
                
                for (ObjectNode newAtt : newAttachments) {
                    existingAttachments.add(newAtt);
                }
                
                document.setAttachments(existingAttachments.toString());
            }
            
            documentService.updateDocument(document);
            request.setAttribute("success", "Documento actualizado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al actualizar documento", e);
            request.setAttribute("error", "Error al actualizar documento: " + e.getMessage());
        }
        
        String patientIdStr = request.getParameter("patientId");
        response.sendRedirect(request.getContextPath() + "/professional/patient-documents?patientId=" + patientIdStr);
    }
    
    private String getUploadBasePath() {
        String clinicUploadsDir = "C:" + File.separator + "TSEGrupo" + File.separator + "tse-2025" + File.separator + "clinic" + File.separator + "uploads";
        Path uploadsPath = Paths.get(clinicUploadsDir);
        if (!Files.exists(uploadsPath)) {
            try {
                Files.createDirectories(uploadsPath);
                logger.info("Directorio de uploads creado: {}", uploadsPath.toAbsolutePath());
            } catch (IOException e) {
                logger.error("Error al crear directorio de uploads: {}", clinicUploadsDir, e);
            }
        }
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
        
        String basePath = getUploadBasePath();
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String relativePath = UPLOAD_DIR + File.separator + clinicId + File.separator + year + File.separator + documentId;
        Path uploadDir = Paths.get(basePath, relativePath);
        
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        String extension = "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = fileName.substring(lastDot);
        }
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        
        Path filePath = uploadDir.resolve(uniqueFileName);
        
        try (InputStream inputStream = part.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        String relativePathForDb = UPLOAD_DIR + "/" + clinicId + "/" + year + "/" + documentId + "/" + uniqueFileName;
        return relativePathForDb;
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
            
            if (weight != null && !weight.isEmpty() && height != null && !height.isEmpty()) {
                try {
                    double weightKg = Double.parseDouble(weight);
                    double heightM = Double.parseDouble(height) / 100.0;
                    if (heightM > 0) {
                        double bmi = weightKg / (heightM * heightM);
                        vitalSigns.put("bmi", String.format("%.2f", bmi));
                    }
                } catch (NumberFormatException e) {
                    // Ignorar
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
            java.util.Set<Integer> prescriptionIndices = new java.util.HashSet<>();
            java.util.Map<String, String> paramMap = new java.util.HashMap<>();
            
            java.util.Enumeration<String> allParamNames = request.getParameterNames();
            while (allParamNames.hasMoreElements()) {
                String paramName = allParamNames.nextElement();
                paramMap.put(paramName, request.getParameter(paramName));
            }
            
            if (request.getContentType() != null && request.getContentType().startsWith("multipart")) {
                try {
                    for (Part part : request.getParts()) {
                        String fieldName = part.getName();
                        if (part.getContentType() == null || part.getSize() < 1000000) {
                            java.io.BufferedReader reader = new java.io.BufferedReader(
                                new java.io.InputStreamReader(part.getInputStream(), "UTF-8"));
                            StringBuilder value = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (value.length() > 0) value.append("\n");
                                value.append(line);
                            }
                            String partValue = value.toString();
                            if (!paramMap.containsKey(fieldName)) {
                                paramMap.put(fieldName, partValue);
                            } else {
                                String existingValue = paramMap.get(fieldName);
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
            
            for (String paramName : paramMap.keySet()) {
                if (paramName.startsWith("prescription_medication_")) {
                    try {
                        String indexStr = paramName.substring("prescription_medication_".length());
                        int index = Integer.parseInt(indexStr);
                        prescriptionIndices.add(index);
                    } catch (NumberFormatException e) {
                        // Ignorar
                    }
                }
            }
            
            java.util.List<Integer> sortedIndices = new java.util.ArrayList<>(prescriptionIndices);
            java.util.Collections.sort(sortedIndices);
            
            for (Integer index : sortedIndices) {
                String medication = paramMap.getOrDefault("prescription_medication_" + index, 
                    request.getParameter("prescription_medication_" + index));
                String dosage = paramMap.getOrDefault("prescription_dosage_" + index,
                    request.getParameter("prescription_dosage_" + index));
                String frequency = paramMap.getOrDefault("prescription_frequency_" + index,
                    request.getParameter("prescription_frequency_" + index));
                String duration = paramMap.getOrDefault("prescription_duration_" + index,
                    request.getParameter("prescription_duration_" + index));
                
                if (medication != null && !medication.trim().isEmpty()) {
                    ObjectNode prescription = objectMapper.createObjectNode();
                    prescription.put("medication", medication.trim());
                    prescription.put("dosage", dosage != null && !dosage.trim().isEmpty() ? dosage.trim() : "");
                    prescription.put("frequency", frequency != null && !frequency.trim().isEmpty() ? frequency.trim() : "");
                    prescription.put("duration", duration != null && !duration.trim().isEmpty() ? duration.trim() : "");
                    prescriptions.add(prescription);
                }
            }
            
            return prescriptions.toString();
        } catch (Exception e) {
            logger.warn("Error al construir JSON de prescripciones", e);
            return "[]";
        }
    }
    
    private void requestAccessToHCEN(HttpServletRequest request, HttpServletResponse response, String clinicId, Long professionalId)
            throws ServletException, IOException {
        
        try {
            // Obtener datos del formulario
            String patientCI = request.getParameter("patientCI");
            String specialtySelection = request.getParameter("specialtySelection");
            String requestReason = request.getParameter("requestReason");
            String urgency = request.getParameter("urgency");
            String documentIdStr = request.getParameter("documentId");
            
            // Validar datos básicos
            if (patientCI == null || patientCI.trim().isEmpty()) {
                request.setAttribute("error", "La cédula del paciente es requerida");
                doGet(request, response);
                return;
            }
            
            if (requestReason == null || requestReason.trim().isEmpty()) {
                request.setAttribute("error", "La razón de la solicitud es requerida");
                doGet(request, response);
                return;
            }
            
            // Cargar profesional y clínica
            Professional professional = professionalService.getProfessionalById(professionalId)
                .orElseThrow(() -> new ServletException("Profesional no encontrado"));
            
            Clinic clinic = clinicService.getClinicById(clinicId)
                .orElseThrow(() -> new ServletException("Clínica no encontrada"));
            
            // Construir string de especialidades
            String specialtiesStr = null;
            if (specialtySelection != null && !"ALL".equalsIgnoreCase(specialtySelection)) {
                try {
                    Long specialtyId = Long.parseLong(specialtySelection);
                    Optional<Specialty> specialtyOpt = specialtyService.getSpecialtyById(specialtyId);
                    if (specialtyOpt.isPresent()) {
                        specialtiesStr = specialtyOpt.get().getName();
                    } else {
                        logger.warn("Especialidad no encontrada para ID {}", specialtySelection);
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Invalid specialty ID: {}", specialtySelection);
                }
            }
            // Si se selecciona "ALL" o es inválida, specialtiesStr queda null (solicitar todas las especialidades)
            
            // Parsear documentId si existe
            Long documentId = null;
            if (documentIdStr != null && !documentIdStr.trim().isEmpty()) {
                try {
                    documentId = Long.parseLong(documentIdStr.trim());
                } catch (NumberFormatException e) {
                    logger.warn("Invalid documentId format: {}", documentIdStr);
                }
            }
            
            // Crear solicitud de acceso
            AccessRequest accessRequest = accessRequestService.createAccessRequest(
                professional,
                clinic,
                patientCI.trim(),
                documentId,
                specialtiesStr,
                requestReason.trim(),
                urgency != null ? urgency : "ROUTINE"
            );
            
            logger.info("Access request created from patient-documents page - ID: {}, Professional: {}, Patient CI: {}", 
                accessRequest.getId(), professionalId, patientCI);
            
            request.setAttribute("success", "Solicitud de acceso creada exitosamente. Será procesada por HCEN.");
            
        } catch (Exception e) {
            logger.error("Error al crear solicitud de acceso HCEN", e);
            request.setAttribute("error", "Error al crear solicitud de acceso: " + e.getMessage());
        }
        
        // Redirigir de vuelta a la página de documentos del paciente
        String patientIdStr = request.getParameter("patientId");
        response.sendRedirect(request.getContextPath() + "/professional/patient-documents?patientId=" + patientIdStr);
    }

    /**
     * Helper method to get the specialty code of the logged-in professional.
     * Returns null if the professional is not found or has no specialty.
     */
    private String getProfessionalSpecialtyName(HttpServletRequest request) {
        try {
            Long professionalId = (Long) request.getSession().getAttribute("professionalId");
            if (professionalId == null) {
                logger.warn("No professional ID found in session");
                return null;
            }

            Optional<Professional> professionalOpt = professionalService.getProfessionalById(professionalId);
            if (professionalOpt.isEmpty()) {
                logger.warn("Professional with ID {} not found", professionalId);
                return null;
            }

            Professional professional = professionalOpt.get();
            Specialty specialty = professional.getSpecialty();
            if (specialty == null) {
                logger.debug("Professional {} has no specialty assigned", professionalId);
                return null;
            }

            return specialty.getCode();
        } catch (Exception ex) {
            logger.error("Error retrieving professional's specialty code", ex);
            return null;
        }
    }

    /**
     * Extracts AccessDeniedException from an exception chain.
     * This is needed because EJB container wraps exceptions in EJBTransactionRolledbackException.
     * @param ex The exception to unwrap
     * @return The AccessDeniedException if found, null otherwise
     */
    private uy.gub.clinic.exception.AccessDeniedException extractAccessDeniedException(Throwable ex) {
        if (ex == null) {
            return null;
        }

        // Check if the exception itself is AccessDeniedException
        if (ex instanceof uy.gub.clinic.exception.AccessDeniedException) {
            return (uy.gub.clinic.exception.AccessDeniedException) ex;
        }

        // Walk through the cause chain to find AccessDeniedException
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof uy.gub.clinic.exception.AccessDeniedException) {
                return (uy.gub.clinic.exception.AccessDeniedException) cause;
            }
            cause = cause.getCause();
        }

        return null;
    }
}

