package uy.gub.clinic.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.entity.ClinicalDocument;
import uy.gub.clinic.entity.Patient;
import uy.gub.clinic.integration.dto.DocumentRegistrationMessage;
import uy.gub.clinic.integration.dto.UserRegistrationMessage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Servicio para comunicación con HCEN Central mediante JMS
 * Envía mensajes para registro de usuarios (INUS) y documentos (RNDC)
 * 
 * @author TSE 2025 Group 9
 */
@Stateless
public class HcenJmsService {
    
    private static final Logger logger = LoggerFactory.getLogger(HcenJmsService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final ObjectMapper objectMapper;
    
    // Recursos JMS - Los buscamos dinámicamente para que sean opcionales
    // Si no están configurados, el servicio simplemente no enviará mensajes
    private ConnectionFactory connectionFactory;
    private Queue userRegistrationQueue;
    private Queue documentRegistrationQueue;
    
    public HcenJmsService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @PostConstruct
    public void init() {
        // Intentar obtener recursos JMS de forma opcional
        try {
            InitialContext initialContext = new InitialContext();
            
            try {
                connectionFactory = (ConnectionFactory) initialContext.lookup("java:/ConnectionFactory");
                logger.info("JMS ConnectionFactory encontrado");
            } catch (NamingException e) {
                logger.warn("JMS ConnectionFactory no disponible: {}", e.getMessage());
            }
            
            try {
                userRegistrationQueue = (Queue) initialContext.lookup("java:/jms/queue/UserRegistration");
                logger.info("JMS Queue UserRegistration encontrada");
            } catch (NamingException e) {
                logger.warn("JMS Queue UserRegistration no disponible: {}", e.getMessage());
            }
            
            try {
                documentRegistrationQueue = (Queue) initialContext.lookup("java:/jms/queue/DocumentRegistration");
                logger.info("JMS Queue DocumentRegistration encontrada");
            } catch (NamingException e) {
                logger.warn("JMS Queue DocumentRegistration no disponible: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            logger.warn("No se pudo inicializar contexto JNDI para JMS: {}", e.getMessage());
        }
    }
    
    /**
     * Envía mensaje de registro de usuario al HCEN (INUS)
     * 
     * @param patient Paciente a registrar
     * @param clinic Clínica del paciente
     */
    public void sendUserRegistration(Patient patient, Clinic clinic) {
        if (patient == null || clinic == null) {
            logger.warn("Cannot send user registration: patient or clinic is null");
            return;
        }
        
        // Verificar si los recursos JMS están disponibles
        if (connectionFactory == null || userRegistrationQueue == null) {
            logger.warn("JMS resources not available - skipping user registration message");
            return;
        }
        
        try (JMSContext context = connectionFactory.createContext()) {
            String messageId = "msg-" + UUID.randomUUID().toString();
            
            // Construir payload
            UserRegistrationMessage.UserRegistrationPayload payload = 
                new UserRegistrationMessage.UserRegistrationPayload();
            
            payload.setCi(patient.getDocumentNumber());
            payload.setFirstName(patient.getName());
            payload.setLastName(patient.getLastName());
            
            if (patient.getBirthDate() != null) {
                payload.setDateOfBirth(patient.getBirthDate().format(DATE_FORMATTER));
            }
            
            payload.setEmail(patient.getEmail());
            payload.setPhoneNumber(patient.getPhone());
            payload.setClinicId(clinic.getCode() != null ? clinic.getCode() : "clinic-" + clinic.getId());
            
            // Construir mensaje completo
            UserRegistrationMessage message = new UserRegistrationMessage(messageId, payload);
            
            // Serializar a JSON
            String jsonMessage = objectMapper.writeValueAsString(message);
            
            // Enviar mensaje
            context.createProducer().send(userRegistrationQueue, jsonMessage);
            
            logger.info("User registration message sent to HCEN - Message ID: {}, Patient CI: {}, Clinic: {}", 
                messageId, patient.getDocumentNumber(), clinic.getCode());
                
        } catch (Exception e) {
            logger.error("Failed to send user registration to HCEN - Patient: {}, Clinic: {}", 
                patient.getDocumentNumber(), clinic.getId(), e);
            // No lanzar excepción - degradación graceful
        }
    }
    
    /**
     * Envía mensaje de registro de documento al HCEN (RNDC)
     * 
     * @param document Documento clínico a registrar
     * @param clinic Clínica del documento
     * @param patient Paciente del documento
     * @param documentBaseUrl URL base para construir el documentLocator (ej: http://clinic.uy/clinic)
     */
    public void sendDocumentRegistration(ClinicalDocument document, Clinic clinic, 
                                        Patient patient, String documentBaseUrl) {
        if (document == null || clinic == null || patient == null) {
            logger.warn("Cannot send document registration: document, clinic or patient is null");
            return;
        }
        
        // Verificar si los recursos JMS están disponibles
        if (connectionFactory == null || documentRegistrationQueue == null) {
            logger.warn("JMS resources not available - skipping document registration message");
            return;
        }
        
        try (JMSContext context = connectionFactory.createContext()) {
            String messageId = "msg-" + UUID.randomUUID().toString();
            
            // Construir documentLocator URL
            String documentLocator = documentBaseUrl + "/api/documents/" + document.getId();
            
            // Calcular hash SHA-256 del documento (simplificado - en producción incluir contenido real)
            String documentHash = calculateDocumentHash(document);
            
            // Construir payload
            DocumentRegistrationMessage.DocumentRegistrationPayload payload = 
                new DocumentRegistrationMessage.DocumentRegistrationPayload();
            
            payload.setPatientCI(patient.getDocumentNumber());
            payload.setDocumentType(document.getDocumentType());
            payload.setDocumentLocator(documentLocator);
            payload.setDocumentHash(documentHash);
            payload.setCreatedBy("professional-" + document.getProfessional().getId());
            payload.setCreatedAt(document.getCreatedAt() != null ? document.getCreatedAt() : java.time.LocalDateTime.now());
            payload.setClinicId(clinic.getCode() != null ? clinic.getCode() : "clinic-" + clinic.getId());
            
            if (document.getSpecialty() != null) {
                payload.setSpecialtyId(document.getSpecialty().getId());
            }
            
            payload.setDocumentTitle(document.getTitle());
            payload.setDocumentDescription(document.getDescription());
            
            // Construir mensaje completo
            DocumentRegistrationMessage message = new DocumentRegistrationMessage(messageId, payload);
            
            // Serializar a JSON
            String jsonMessage = objectMapper.writeValueAsString(message);
            
            // Enviar mensaje
            context.createProducer().send(documentRegistrationQueue, jsonMessage);
            
            logger.info("Document registration message sent to HCEN - Message ID: {}, Document ID: {}, Patient CI: {}", 
                messageId, document.getId(), patient.getDocumentNumber());
                
        } catch (Exception e) {
            logger.error("Failed to send document registration to HCEN - Document ID: {}, Patient: {}", 
                document.getId(), patient.getDocumentNumber(), e);
            // No lanzar excepción - degradación graceful
        }
    }
    
    /**
     * Calcula hash SHA-256 del documento
     * Por ahora es una versión simplificada. En producción debería incluir:
     * - Contenido del documento (todos los campos)
     * - Archivos adjuntos si existen
     * 
     * @param document Documento clínico
     * @return Hash SHA-256 en formato "sha256:{hex}"
     */
    private String calculateDocumentHash(ClinicalDocument document) {
        try {
            // Construir string con todos los campos relevantes del documento
            StringBuilder content = new StringBuilder();
            content.append(document.getId()).append("|");
            content.append(document.getTitle()).append("|");
            content.append(document.getDocumentType()).append("|");
            if (document.getDiagnosis() != null) content.append(document.getDiagnosis()).append("|");
            if (document.getTreatment() != null) content.append(document.getTreatment()).append("|");
            if (document.getPatient() != null) content.append(document.getPatient().getId()).append("|");
            if (document.getCreatedAt() != null) content.append(document.getCreatedAt().toString());
            
            // Calcular SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content.toString().getBytes(StandardCharsets.UTF_8));
            
            // Convertir a hex string
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
            logger.warn("Error calculating document hash, using placeholder", e);
            // Retornar hash placeholder si hay error
            return "sha256:error-" + document.getId();
        }
    }
}

