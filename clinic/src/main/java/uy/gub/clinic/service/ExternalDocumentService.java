package uy.gub.clinic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.ClinicalDocument;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.entity.Patient;
import uy.gub.clinic.entity.Professional;
import uy.gub.clinic.entity.Specialty;
import uy.gub.clinic.integration.dto.DocumentMetadata;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para descargar y almacenar documentos externos desde otras clínicas
 * 
 * @author TSE 2025 Group 9
 */
@Stateless
public class ExternalDocumentService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalDocumentService.class);
    
    @PersistenceContext(unitName = "clinicPU")
    private EntityManager entityManager;
    
    @Inject
    private SpecialtyService specialtyService;
    
    private final ObjectMapper objectMapper;
    
    public ExternalDocumentService() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Descarga y almacena un documento externo desde otra clínica
     * 
     * @param metadata Metadatos del documento proporcionados por HCEN
     * @param targetPatient Paciente de esta clínica al que asociar el documento
     * @param targetProfessional Profesional que solicitó el acceso
     * @param targetClinic Clínica destino
     * @return Documento clínico descargado y guardado
     */
    @Transactional
    public ClinicalDocument downloadAndStoreExternalDocument(
            DocumentMetadata metadata,
            Patient targetPatient,
            Professional targetProfessional,
            Clinic targetClinic) {
        
        try {
            logger.info("Downloading external document - Locator: {}, Clinic: {}", 
                metadata.getDocumentLocator(), metadata.getClinicName());
            
            // Descargar documento desde documentLocator
            String documentJson = downloadDocument(metadata.getDocumentLocator());
            
            if (documentJson == null || documentJson.isEmpty()) {
                throw new RuntimeException("Failed to download document from: " + metadata.getDocumentLocator());
            }
            
            // Parsear JSON del documento
            JsonNode documentNode = objectMapper.readTree(documentJson);
            
            // Validar hash (simplificado - en producción debería validar contenido completo)
            String downloadedHash = calculateHash(documentJson);
            if (metadata.getDocumentHash() != null && !downloadedHash.equals(metadata.getDocumentHash())) {
                logger.warn("Document hash mismatch - Expected: {}, Got: {}", 
                    metadata.getDocumentHash(), downloadedHash);
                // En producción esto debería ser un error, pero por ahora solo logueamos
            }
            
            // Crear entidad ClinicalDocument
            ClinicalDocument document = new ClinicalDocument();
            
            // Datos básicos
            document.setTitle(documentNode.has("title") ? documentNode.get("title").asText() : 
                "Documento externo - " + metadata.getDocumentType());
            document.setDescription(documentNode.has("description") ? 
                documentNode.get("description").asText() : null);
            document.setDocumentType(metadata.getDocumentType() != null ? 
                metadata.getDocumentType() : 
                (documentNode.has("documentType") ? documentNode.get("documentType").asText() : "EXTERNO"));
            
            // Fecha de visita
            if (documentNode.has("dateOfVisit")) {
                document.setDateOfVisit(LocalDate.parse(documentNode.get("dateOfVisit").asText()));
            } else {
                document.setDateOfVisit(LocalDate.now());
            }
            
            // Campos médicos
            if (documentNode.has("chiefComplaint")) {
                document.setChiefComplaint(documentNode.get("chiefComplaint").asText());
            }
            if (documentNode.has("currentIllness")) {
                document.setCurrentIllness(documentNode.get("currentIllness").asText());
            }
            if (documentNode.has("physicalExamination")) {
                document.setPhysicalExamination(documentNode.get("physicalExamination").asText());
            }
            if (documentNode.has("diagnosis")) {
                document.setDiagnosis(documentNode.get("diagnosis").asText());
            }
            if (documentNode.has("treatment")) {
                document.setTreatment(documentNode.get("treatment").asText());
            }
            if (documentNode.has("prescriptions")) {
                document.setPrescriptions(documentNode.get("prescriptions").asText());
            }
            if (documentNode.has("vitalSigns")) {
                document.setVitalSigns(documentNode.get("vitalSigns").asText());
            }
            if (documentNode.has("observations")) {
                document.setObservations(documentNode.get("observations").asText());
            }
            if (documentNode.has("attachments")) {
                document.setAttachments(documentNode.get("attachments").toString());
            }
            
            // Información de documento externo
            document.setIsExternal(true);
            document.setSourceClinicId(metadata.getClinicId());
            document.setExternalClinicName(metadata.getClinicName());
            document.setExternalDocumentLocator(metadata.getDocumentLocator());
            
            // Relaciones
            document.setPatient(targetPatient);
            document.setProfessional(targetProfessional);
            document.setClinic(targetClinic);
            
            // Especialidad - intentar encontrar por nombre o usar la del profesional
            Specialty specialty = null;
            if (metadata.getSpecialty() != null) {
                List<Specialty> specialties = specialtyService.getAllSpecialties();
                for (Specialty s : specialties) {
                    if (metadata.getSpecialty().equalsIgnoreCase(s.getName()) || 
                        metadata.getSpecialty().equalsIgnoreCase(s.getCode())) {
                        specialty = s;
                        break;
                    }
                }
            }
            if (specialty == null && targetProfessional.getSpecialty() != null) {
                specialty = targetProfessional.getSpecialty();
            }
            if (specialty == null) {
                // Usar primera especialidad como fallback
                List<Specialty> specialties = specialtyService.getAllSpecialties();
                if (!specialties.isEmpty()) {
                    specialty = specialties.get(0);
                }
            }
            if (specialty != null) {
                document.setSpecialty(specialty);
            }
            
            // Fecha de creación original si está disponible
            if (metadata.getCreatedAt() != null) {
                document.setCreatedAt(metadata.getCreatedAt());
            } else {
                document.setCreatedAt(LocalDateTime.now());
            }
            
            // Guardar documento
            entityManager.persist(document);
            entityManager.flush();
            
            logger.info("External document downloaded and stored - Document ID: {}, Source Clinic: {}", 
                document.getId(), metadata.getClinicName());
            
            return document;
            
        } catch (Exception e) {
            logger.error("Error downloading and storing external document - Locator: {}", 
                metadata.getDocumentLocator(), e);
            throw new RuntimeException("Failed to download external document", e);
        }
    }
    
    /**
     * Descarga el contenido del documento desde la URL proporcionada
     */
    private String downloadDocument(String documentLocator) {
        try {
            URL url = new URL(documentLocator);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10000); // 10 segundos
            connection.setReadTimeout(30000); // 30 segundos
            
            // TODO: Agregar autenticación si es necesaria
            // connection.setRequestProperty("Authorization", "Bearer ...");
            
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] buffer = new byte[8192];
                StringBuilder content = new StringBuilder();
                int bytesRead;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    content.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
                }
                
                return content.toString();
            }
            
        } catch (Exception e) {
            logger.error("Error downloading document from: {}", documentLocator, e);
            return null;
        }
    }
    
    /**
     * Calcula hash SHA-256 del contenido
     */
    private String calculateHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return "sha256:" + hexString.toString();
            
        } catch (Exception e) {
            logger.warn("Error calculating hash", e);
            return "sha256:error";
        }
    }
}

