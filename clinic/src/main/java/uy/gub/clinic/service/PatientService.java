package uy.gub.clinic.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.entity.Patient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar pacientes
 */
@Stateless
public class PatientService {

    @PersistenceContext(unitName = "clinicPU")
    private EntityManager entityManager;

    /**
     * Registra un nuevo paciente en el sistema
     */
    @Transactional
    public Patient registerPatient(String name, String lastName, String documentNumber, 
                                 String inusId, LocalDate birthDate, String gender, 
                                 String phone, String email, String address, Long clinicId) {
        
        // Validar que la clínica existe
        Clinic clinic = entityManager.find(Clinic.class, clinicId);
        if (clinic == null) {
            throw new IllegalArgumentException("Clínica no encontrada con ID: " + clinicId);
        }
        
        // Verificar que el documento no esté en uso
        if (documentNumber != null && !documentNumber.isEmpty()) {
            TypedQuery<Patient> documentQuery = entityManager.createNamedQuery(
                "Patient.findByDocument", Patient.class);
            documentQuery.setParameter("documentNumber", documentNumber);
            if (!documentQuery.getResultList().isEmpty()) {
                throw new IllegalArgumentException("El documento ya está en uso: " + documentNumber);
            }
        }
        
        // Verificar que el INUS ID no esté en uso
        if (inusId != null && !inusId.isEmpty()) {
            TypedQuery<Patient> inusQuery = entityManager.createNamedQuery(
                "Patient.findByInusId", Patient.class);
            inusQuery.setParameter("inusId", inusId);
            if (!inusQuery.getResultList().isEmpty()) {
                throw new IllegalArgumentException("El INUS ID ya está en uso: " + inusId);
            }
        }
        
        // Crear el nuevo paciente
        Patient patient = new Patient();
        patient.setName(name);
        patient.setLastName(lastName);
        patient.setDocumentNumber(documentNumber);
        patient.setInusId(inusId);
        patient.setBirthDate(birthDate);
        patient.setGender(gender);
        patient.setPhone(phone);
        patient.setEmail(email);
        patient.setAddress(address);
        patient.setClinic(clinic);
        patient.setActive(true);
        
        entityManager.persist(patient);
        entityManager.flush();
        
        return patient;
    }
    
    /**
     * Obtiene todos los pacientes
     */
    public List<Patient> getAllPatients() {
        TypedQuery<Patient> query = entityManager.createNamedQuery(
            "Patient.findAll", Patient.class);
        List<Patient> patients = query.getResultList();
        
        // Cargar las relaciones lazy para evitar LazyInitializationException
        for (Patient patient : patients) {
            if (patient.getClinic() != null) {
                patient.getClinic().getName(); // Forzar carga
            }
        }
        
        return patients;
    }
    
    /**
     * Obtiene pacientes por clínica
     */
    public List<Patient> getPatientsByClinic(Long clinicId) {
        List<Patient> patients;
        
        // Si clinicId es 0, significa que es super administrador - devolver todos los pacientes
        if (clinicId != null && clinicId == 0L) {
            TypedQuery<Patient> query = entityManager.createNamedQuery(
                "Patient.findAll", Patient.class);
            patients = query.getResultList();
        } else {
            TypedQuery<Patient> query = entityManager.createNamedQuery(
                "Patient.findByClinic", Patient.class);
            query.setParameter("clinicId", clinicId);
            patients = query.getResultList();
        }
        
        // Cargar las relaciones lazy para evitar LazyInitializationException
        for (Patient patient : patients) {
            if (patient.getClinic() != null) {
                patient.getClinic().getName(); // Forzar carga
            }
        }
        
        return patients;
    }
    
    /**
     * Obtiene un paciente por ID
     */
    public Optional<Patient> getPatientById(Long id) {
        Patient patient = entityManager.find(Patient.class, id);
        return Optional.ofNullable(patient);
    }
    
    /**
     * Obtiene un paciente por número de cédula
     */
    public Optional<Patient> getPatientByDocumentNumber(String documentNumber) {
        if (documentNumber == null || documentNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        
        TypedQuery<Patient> query = entityManager.createNamedQuery(
            "Patient.findByDocumentActive", Patient.class);
        query.setParameter("documentNumber", documentNumber.trim());
        
        List<Patient> results = query.getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        
        Patient patient = results.get(0);
        // Cargar relaciones lazy
        if (patient.getClinic() != null) {
            patient.getClinic().getName();
        }
        
        return Optional.of(patient);
    }
    
    /**
     * Actualiza un paciente existente
     */
    @Transactional
    public Patient updatePatient(Long id, String name, String lastName, 
                               String documentNumber, String inusId, 
                               LocalDate birthDate, String gender, 
                               String phone, String email, String address) {
        
        Patient patient = entityManager.find(Patient.class, id);
        if (patient == null) {
            throw new IllegalArgumentException("Paciente no encontrado con ID: " + id);
        }
        
        // Actualizar campos
        if (name != null) patient.setName(name);
        if (lastName != null) patient.setLastName(lastName);
        if (documentNumber != null) patient.setDocumentNumber(documentNumber);
        if (inusId != null) patient.setInusId(inusId);
        if (birthDate != null) patient.setBirthDate(birthDate);
        if (gender != null) patient.setGender(gender);
        if (phone != null) patient.setPhone(phone);
        if (email != null) patient.setEmail(email);
        if (address != null) patient.setAddress(address);
        
        entityManager.merge(patient);
        return patient;
    }
    
    /**
     * Desactiva un paciente (soft delete)
     */
    @Transactional
    public void deactivatePatient(Long id) {
        Patient patient = entityManager.find(Patient.class, id);
        if (patient != null) {
            patient.setActive(false);
            entityManager.merge(patient);
        }
    }
    
    /**
     * Activa un paciente
     */
    @Transactional
    public void activatePatient(Long id) {
        Patient patient = entityManager.find(Patient.class, id);
        if (patient != null) {
            patient.setActive(true);
            entityManager.merge(patient);
        }
    }
    
    /**
     * Elimina permanentemente un paciente
     */
    @Transactional
    public void deletePatient(Long id) {
        Patient patient = entityManager.find(Patient.class, id);
        if (patient != null) {
            entityManager.remove(patient);
        }
    }
}
