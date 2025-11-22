package uy.gub.clinic.integration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * DTO para mensaje de registro de documento en HCEN (RNDC)
 * 
 * @author TSE 2025 Group 9
 */
public class DocumentRegistrationMessage {
    
    private String messageId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String sourceSystem;
    private String eventType;
    private DocumentRegistrationPayload payload;
    
    public DocumentRegistrationMessage() {
        this.sourceSystem = "clinic";
        this.eventType = "DOCUMENT_CREATED";
        this.timestamp = LocalDateTime.now();
    }
    
    public DocumentRegistrationMessage(String messageId, DocumentRegistrationPayload payload) {
        this();
        this.messageId = messageId;
        this.payload = payload;
    }
    
    // Getters y Setters
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSourceSystem() {
        return sourceSystem;
    }
    
    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public DocumentRegistrationPayload getPayload() {
        return payload;
    }
    
    public void setPayload(DocumentRegistrationPayload payload) {
        this.payload = payload;
    }
    
    /**
     * Payload interno para datos del documento
     */
    public static class DocumentRegistrationPayload {
        private String patientCI;
        private String documentType;
        private String documentLocator;
        private String documentHash;
        private String createdBy;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
        
        private String clinicId;
        private Long specialtyId;
        private String documentTitle;
        private String documentDescription;
        /**
         * Representación FHIR (DocumentReference/Bundle) del documento recién creado.
         * Permite al HCEN reconstruir la información clínica completa.
         */
        private String fhirDocument;
        
        public DocumentRegistrationPayload() {
        }
        
        public DocumentRegistrationPayload(String patientCI, String documentType, 
                                         String documentLocator, String documentHash,
                                         String createdBy, LocalDateTime createdAt,
                                         String clinicId, Long specialtyId,
                                         String documentTitle, String documentDescription) {
            this.patientCI = patientCI;
            this.documentType = documentType;
            this.documentLocator = documentLocator;
            this.documentHash = documentHash;
            this.createdBy = createdBy;
            this.createdAt = createdAt;
            this.clinicId = clinicId;
            this.specialtyId = specialtyId;
            this.documentTitle = documentTitle;
            this.documentDescription = documentDescription;
        }
        
        // Getters y Setters
        
        public String getPatientCI() {
            return patientCI;
        }
        
        public void setPatientCI(String patientCI) {
            this.patientCI = patientCI;
        }
        
        public String getDocumentType() {
            return documentType;
        }
        
        public void setDocumentType(String documentType) {
            this.documentType = documentType;
        }
        
        public String getDocumentLocator() {
            return documentLocator;
        }
        
        public void setDocumentLocator(String documentLocator) {
            this.documentLocator = documentLocator;
        }
        
        public String getDocumentHash() {
            return documentHash;
        }
        
        public void setDocumentHash(String documentHash) {
            this.documentHash = documentHash;
        }
        
        public String getCreatedBy() {
            return createdBy;
        }
        
        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }
        
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
        
        public String getClinicId() {
            return clinicId;
        }
        
        public void setClinicId(String clinicId) {
            this.clinicId = clinicId;
        }
        
        public Long getSpecialtyId() {
            return specialtyId;
        }
        
        public void setSpecialtyId(Long specialtyId) {
            this.specialtyId = specialtyId;
        }
        
        public String getDocumentTitle() {
            return documentTitle;
        }
        
        public void setDocumentTitle(String documentTitle) {
            this.documentTitle = documentTitle;
        }
        
        public String getDocumentDescription() {
            return documentDescription;
        }
        
        public void setDocumentDescription(String documentDescription) {
            this.documentDescription = documentDescription;
        }

        public String getFhirDocument() {
            return fhirDocument;
        }

        public void setFhirDocument(String fhirDocument) {
            this.fhirDocument = fhirDocument;
        }
    }
}

