package uy.gub.clinic.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.entity.Professional;
import uy.gub.clinic.entity.Specialty;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar profesionales de salud
 */
@Stateless
public class ProfessionalService {

    @PersistenceContext(unitName = "clinicPU")
    private EntityManager entityManager;

    /**
     * Registra un nuevo profesional en el sistema
     */
    @Transactional
    public Professional registerProfessional(String name, String lastName, String email, 
                                           String licenseNumber, String phone, 
                                           Long clinicId, Long specialtyId) {
        
        // Validar que la clínica existe
        Clinic clinic = entityManager.find(Clinic.class, clinicId);
        if (clinic == null) {
            throw new IllegalArgumentException("Clínica no encontrada con ID: " + clinicId);
        }
        
        // Validar que la especialidad existe
        Specialty specialty = entityManager.find(Specialty.class, specialtyId);
        if (specialty == null) {
            throw new IllegalArgumentException("Especialidad no encontrada con ID: " + specialtyId);
        }
        
        // Verificar que el email no esté en uso
        if (email != null && !email.isEmpty()) {
            TypedQuery<Professional> emailQuery = entityManager.createNamedQuery(
                "Professional.findByEmail", Professional.class);
            emailQuery.setParameter("email", email);
            if (!emailQuery.getResultList().isEmpty()) {
                throw new IllegalArgumentException("El email ya está en uso: " + email);
            }
        }
        
        // Verificar que el número de matrícula no esté en uso
        if (licenseNumber != null && !licenseNumber.isEmpty()) {
            TypedQuery<Professional> licenseQuery = entityManager.createNamedQuery(
                "Professional.findByLicense", Professional.class);
            licenseQuery.setParameter("licenseNumber", licenseNumber);
            if (!licenseQuery.getResultList().isEmpty()) {
                throw new IllegalArgumentException("El número de matrícula ya está en uso: " + licenseNumber);
            }
        }
        
        // Crear el nuevo profesional
        Professional professional = new Professional();
        professional.setName(name);
        professional.setLastName(lastName);
        professional.setEmail(email);
        professional.setLicenseNumber(licenseNumber);
        professional.setPhone(phone);
        professional.setClinic(clinic);
        professional.setSpecialty(specialty);
        professional.setActive(true);
        
        entityManager.persist(professional);
        entityManager.flush();
        
        return professional;
    }
    
    /**
     * Obtiene todos los profesionales
     */
    public List<Professional> getAllProfessionals() {
        TypedQuery<Professional> query = entityManager.createNamedQuery(
            "Professional.findAll", Professional.class);
        List<Professional> professionals = query.getResultList();
        
        // Cargar las relaciones lazy para evitar LazyInitializationException
        for (Professional professional : professionals) {
            if (professional.getSpecialty() != null) {
                professional.getSpecialty().getName(); // Forzar carga
            }
            if (professional.getClinic() != null) {
                professional.getClinic().getName(); // Forzar carga
            }
        }
        
        return professionals;
    }
    
    /**
     * Obtiene profesionales por clínica
     */
    public List<Professional> getProfessionalsByClinic(Long clinicId) {
        if (clinicId == null) {
            throw new IllegalArgumentException("clinicId no puede ser null");
        }
        
        TypedQuery<Professional> query = entityManager.createNamedQuery(
            "Professional.findByClinic", Professional.class);
        query.setParameter("clinicId", clinicId);
        List<Professional> professionals = query.getResultList();
        
        // Cargar las relaciones lazy para evitar LazyInitializationException
        for (Professional professional : professionals) {
            if (professional.getSpecialty() != null) {
                professional.getSpecialty().getName(); // Forzar carga
            }
            if (professional.getClinic() != null) {
                professional.getClinic().getName(); // Forzar carga
            }
        }
        
        return professionals;
    }
    
    /**
     * Obtiene profesionales por especialidad
     */
    public List<Professional> getProfessionalsBySpecialty(Long specialtyId) {
        TypedQuery<Professional> query = entityManager.createNamedQuery(
            "Professional.findBySpecialty", Professional.class);
        query.setParameter("specialtyId", specialtyId);
        return query.getResultList();
    }
    
    /**
     * Obtiene un profesional por ID
     */
    public Optional<Professional> getProfessionalById(Long id) {
        Professional professional = entityManager.find(Professional.class, id);
        if (professional != null) {
            // Cargar relaciones lazy para evitar LazyInitializationException
            if (professional.getClinic() != null) {
                professional.getClinic().getName(); // Forzar carga
            }
            if (professional.getSpecialty() != null) {
                professional.getSpecialty().getName(); // Forzar carga
            }
        }
        return Optional.ofNullable(professional);
    }
    
    /**
     * Actualiza un profesional existente
     */
    @Transactional
    public Professional updateProfessional(Long id, String name, String lastName, 
                                         String email, String licenseNumber, 
                                         String phone, Long specialtyId) {
        
        Professional professional = entityManager.find(Professional.class, id);
        if (professional == null) {
            throw new IllegalArgumentException("Profesional no encontrado con ID: " + id);
        }
        
        // Actualizar campos
        if (name != null) professional.setName(name);
        if (lastName != null) professional.setLastName(lastName);
        if (email != null) professional.setEmail(email);
        if (licenseNumber != null) professional.setLicenseNumber(licenseNumber);
        if (phone != null) professional.setPhone(phone);
        
        // Actualizar especialidad si se proporciona
        if (specialtyId != null) {
            Specialty specialty = entityManager.find(Specialty.class, specialtyId);
            if (specialty != null) {
                professional.setSpecialty(specialty);
            }
        }
        
        entityManager.merge(professional);
        return professional;
    }
    
    /**
     * Desactiva un profesional (soft delete)
     */
    @Transactional
    public void deactivateProfessional(Long id) {
        Professional professional = entityManager.find(Professional.class, id);
        if (professional != null) {
            professional.setActive(false);
            entityManager.merge(professional);
        }
    }
    
    /**
     * Activa un profesional
     */
    @Transactional
    public void activateProfessional(Long id) {
        Professional professional = entityManager.find(Professional.class, id);
        if (professional != null) {
            professional.setActive(true);
            entityManager.merge(professional);
        }
    }
    
    /**
     * Elimina permanentemente un profesional
     */
    @Transactional
    public void deleteProfessional(Long id) {
        Professional professional = entityManager.find(Professional.class, id);
        if (professional != null) {
            entityManager.remove(professional);
        }
    }
}