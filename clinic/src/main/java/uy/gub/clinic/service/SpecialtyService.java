package uy.gub.clinic.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import uy.gub.clinic.entity.Specialty;
import uy.gub.clinic.entity.Clinic;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar especialidades médicas
 */
@Stateless
public class SpecialtyService {

    @PersistenceContext(unitName = "clinicPU")
    private EntityManager entityManager;

    /**
     * Obtiene todas las especialidades activas
     */
    public List<Specialty> getAllActiveSpecialties() {
        TypedQuery<Specialty> query = entityManager.createNamedQuery(
            "Specialty.findActive", Specialty.class);
        return query.getResultList();
    }
    
    /**
     * Obtiene todas las especialidades
     */
    public List<Specialty> getAllSpecialties() {
        TypedQuery<Specialty> query = entityManager.createQuery(
            "SELECT s FROM Specialty s ORDER BY s.name", Specialty.class);
        List<Specialty> specialties = query.getResultList();
        
        // Cargar las relaciones lazy para evitar LazyInitializationException
        for (Specialty specialty : specialties) {
            if (specialty.getClinic() != null) {
                specialty.getClinic().getName(); // Forzar carga
            }
        }
        
        return specialties;
    }
    
    /**
     * Obtiene especialidades por clínica
     * @deprecated Las especialidades ahora son globales. Use getAllSpecialties() en su lugar.
     * Este método se mantiene por compatibilidad pero siempre devuelve todas las especialidades.
     */
    @Deprecated
    public List<Specialty> getSpecialtiesByClinic(Long clinicId) {
        // Las especialidades ahora son globales, devolver todas sin filtrar por clínica
        return getAllSpecialties();
    }
    
    /**
     * Obtiene especialidades activas por clínica
     * @deprecated Las especialidades ahora son globales. Use getAllActiveSpecialties() en su lugar.
     * Este método se mantiene por compatibilidad pero siempre devuelve todas las especialidades activas.
     */
    @Deprecated
    public List<Specialty> getActiveSpecialtiesByClinic(Long clinicId) {
        // Las especialidades ahora son globales, devolver todas las activas sin filtrar por clínica
        return getAllActiveSpecialties();
    }
    
    /**
     * Obtiene una especialidad por ID
     */
    public Optional<Specialty> getSpecialtyById(Long id) {
        Specialty specialty = entityManager.find(Specialty.class, id);
        return Optional.ofNullable(specialty);
    }
    
    /**
     * Registra una nueva especialidad
     * Las especialidades ahora son globales, no se asocian a una clínica específica
     * @param clinicId Se ignora (mantenido por compatibilidad), pero puede ser null
     */
    @Transactional
    public Specialty registerSpecialty(String name, String code, String description, Long clinicId) {
        // Verificar que el nombre no esté en uso (las especialidades son globales, nombres únicos)
        if (name != null && !name.isEmpty()) {
            TypedQuery<Specialty> nameQuery = entityManager.createQuery(
                "SELECT s FROM Specialty s WHERE s.name = :name", Specialty.class);
            nameQuery.setParameter("name", name);
            if (!nameQuery.getResultList().isEmpty()) {
                throw new IllegalArgumentException("La especialidad ya existe: " + name);
            }
        }
        
        // Crear la nueva especialidad (sin asociar a clínica)
        Specialty specialty = new Specialty();
        specialty.setName(name);
        specialty.setCode(code);
        specialty.setDescription(description);
        specialty.setActive(true);
        specialty.setClinic(null); // Las especialidades son globales
        
        entityManager.persist(specialty);
        entityManager.flush();
        
        return specialty;
    }
    
    /**
     * Actualiza una especialidad existente
     */
    @Transactional
    public Specialty updateSpecialty(Long id, String name, String code, String description) {
        Specialty specialty = entityManager.find(Specialty.class, id);
        if (specialty == null) {
            throw new IllegalArgumentException("Especialidad no encontrada con ID: " + id);
        }
        
        // Verificar que el nombre no esté en uso por otra especialidad
        if (name != null && !name.isEmpty()) {
            TypedQuery<Specialty> nameQuery = entityManager.createQuery(
                "SELECT s FROM Specialty s WHERE s.name = :name AND s.id != :id", Specialty.class);
            nameQuery.setParameter("name", name);
            nameQuery.setParameter("id", id);
            if (!nameQuery.getResultList().isEmpty()) {
                throw new IllegalArgumentException("La especialidad ya existe: " + name);
            }
        }
        
        // Actualizar campos
        if (name != null) specialty.setName(name);
        if (code != null) specialty.setCode(code);
        if (description != null) specialty.setDescription(description);
        
        entityManager.merge(specialty);
        return specialty;
    }
    
    /**
     * Desactiva una especialidad (soft delete)
     */
    @Transactional
    public void deactivateSpecialty(Long id) {
        Specialty specialty = entityManager.find(Specialty.class, id);
        if (specialty != null) {
            specialty.setActive(false);
            entityManager.merge(specialty);
        }
    }
    
    /**
     * Activa una especialidad
     */
    @Transactional
    public void activateSpecialty(Long id) {
        Specialty specialty = entityManager.find(Specialty.class, id);
        if (specialty != null) {
            specialty.setActive(true);
            entityManager.merge(specialty);
        }
    }
    
    /**
     * Elimina permanentemente una especialidad
     */
    @Transactional
    public void deleteSpecialty(Long id) {
        Specialty specialty = entityManager.find(Specialty.class, id);
        if (specialty != null) {
            entityManager.remove(specialty);
        }
    }
}
