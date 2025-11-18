package uy.gub.clinic.integration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * DTO para mensaje de registro de usuario en HCEN (INUS)
 * 
 * @author TSE 2025 Group 9
 */
public class UserRegistrationMessage {
    
    private String messageId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String sourceSystem;
    private String eventType;
    private UserRegistrationPayload payload;
    
    public UserRegistrationMessage() {
        this.sourceSystem = "clinic";
        this.eventType = "USER_CREATED";
        this.timestamp = LocalDateTime.now();
    }
    
    public UserRegistrationMessage(String messageId, UserRegistrationPayload payload) {
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
    
    public UserRegistrationPayload getPayload() {
        return payload;
    }
    
    public void setPayload(UserRegistrationPayload payload) {
        this.payload = payload;
    }
    
    /**
     * Payload interno para datos del usuario
     */
    public static class UserRegistrationPayload {
        private String ci;
        private String firstName;
        private String lastName;
        private String dateOfBirth;
        private String email;
        private String phoneNumber;
        private String clinicId;
        
        public UserRegistrationPayload() {
        }
        
        public UserRegistrationPayload(String ci, String firstName, String lastName, 
                                     String dateOfBirth, String email, String phoneNumber, 
                                     String clinicId) {
            this.ci = ci;
            this.firstName = firstName;
            this.lastName = lastName;
            this.dateOfBirth = dateOfBirth;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.clinicId = clinicId;
        }
        
        // Getters y Setters
        
        public String getCi() {
            return ci;
        }
        
        public void setCi(String ci) {
            this.ci = ci;
        }
        
        public String getFirstName() {
            return firstName;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public String getDateOfBirth() {
            return dateOfBirth;
        }
        
        public void setDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPhoneNumber() {
            return phoneNumber;
        }
        
        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
        
        public String getClinicId() {
            return clinicId;
        }
        
        public void setClinicId(String clinicId) {
            this.clinicId = clinicId;
        }
    }
}

