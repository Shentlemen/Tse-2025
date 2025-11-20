package uy.gub.clinic.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.entity.Professional;
import uy.gub.clinic.entity.Patient;
import uy.gub.clinic.entity.Specialty;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de clínicas usando base de datos
 */
@ApplicationScoped
public class ClinicService {

    private static final Logger logger = LoggerFactory.getLogger(ClinicService.class);

    @PersistenceContext
    private EntityManager entityManager;

    // Métodos para clínicas
    public List<Clinic> getAllClinics() {
        try {
            TypedQuery<Clinic> query = entityManager.createQuery(
                "SELECT c FROM Clinic c WHERE c.active = true ORDER BY c.name",
                Clinic.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al obtener clínicas de la BD", e);
            return new ArrayList<>();
        }
    }

    public Optional<Clinic> getClinicById(String id) {
        try {
            Clinic clinic = entityManager.find(Clinic.class, id);
            return Optional.ofNullable(clinic);
        } catch (Exception e) {
            logger.error("Error al obtener clínica {} de la BD", id, e);
            return Optional.empty();
        }
    }

    public Optional<Clinic> getClinicByCode(String code) {
        try {
            TypedQuery<Clinic> query = entityManager.createQuery(
                "SELECT c FROM Clinic c WHERE c.code = :code AND c.active = true",
                Clinic.class);
            query.setParameter("code", code);
            return query.getResultList().stream().findFirst();
        } catch (Exception e) {
            logger.error("Error al obtener clínica por código {} de la BD", code, e);
            return Optional.empty();
        }
    }

    /**
     * Busca una clínica por su API key
     *
     * @param apiKey API key de la clínica
     * @return Optional con la clínica si se encuentra, vacío si no
     */
    public Optional<Clinic> getClinicByApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            TypedQuery<Clinic> query = entityManager.createQuery(
                "SELECT c FROM Clinic c WHERE c.apiKey = :apiKey AND c.active = true",
                Clinic.class);
            query.setParameter("apiKey", apiKey.trim());
            return query.getResultList().stream().findFirst();
        } catch (Exception e) {
            logger.error("Error al obtener clínica por API key de la BD", e);
            return Optional.empty();
        }
    }

    @Transactional
    public Clinic createClinic(Clinic clinic) {
        try {
            entityManager.persist(clinic);
            entityManager.flush();
            logger.info("Clínica creada en BD: {}", clinic);
            return clinic;
        } catch (Exception e) {
            logger.error("Error al crear clínica en BD", e);
            throw new RuntimeException("Error al crear clínica: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Clinic updateClinic(Clinic clinic) {
        try {
            Clinic updated = entityManager.merge(clinic);
            entityManager.flush();
            logger.info("Clínica actualizada en BD: {}", updated);
            return updated;
        } catch (Exception e) {
            logger.error("Error al actualizar clínica en BD", e);
            throw new RuntimeException("Error al actualizar clínica: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteClinic(String id) {
        try {
            Clinic clinic = entityManager.find(Clinic.class, id);
            if (clinic != null) {
                clinic.setActive(false);
                entityManager.merge(clinic);
                logger.info("Clínica desactivada con ID: {}", id);
            } else {
                throw new IllegalArgumentException("Clínica no encontrada con ID: " + id);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al eliminar clínica de BD", e);
            throw new RuntimeException("Error al eliminar clínica: " + e.getMessage(), e);
        }
    }

    // Métodos para profesionales
    public List<Professional> getAllProfessionals() {
        try {
            TypedQuery<Professional> query = entityManager.createQuery(
                "SELECT p FROM Professional p WHERE p.active = true ORDER BY p.name",
                Professional.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al obtener profesionales de la BD", e);
            return new ArrayList<>();
        }
    }

    public List<Professional> getProfessionalsByClinic(String clinicId) {
        try {
            TypedQuery<Professional> query = entityManager.createQuery(
                "SELECT p FROM Professional p WHERE p.clinic.id = :clinicId AND p.active = true ORDER BY p.name",
                Professional.class);
            query.setParameter("clinicId", clinicId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al obtener profesionales de la clínica {} de la BD", clinicId, e);
            return new ArrayList<>();
        }
    }

    public Optional<Professional> getProfessionalById(Long id) {
        try {
            Professional professional = entityManager.find(Professional.class, id);
            return Optional.ofNullable(professional);
        } catch (Exception e) {
            logger.error("Error al obtener profesional {} de la BD", id, e);
            return Optional.empty();
        }
    }

    @Transactional
    public Professional createProfessional(Professional professional) {
        try {
            entityManager.persist(professional);
            entityManager.flush();
            logger.info("Profesional creado en BD: {}", professional);
            return professional;
        } catch (Exception e) {
            logger.error("Error al crear profesional en BD", e);
            throw new RuntimeException("Error al crear profesional: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Professional updateProfessional(Professional professional) {
        try {
            Professional updated = entityManager.merge(professional);
            entityManager.flush();
            logger.info("Profesional actualizado en BD: {}", updated);
            return updated;
        } catch (Exception e) {
            logger.error("Error al actualizar profesional en BD", e);
            throw new RuntimeException("Error al actualizar profesional: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteProfessional(Long id) {
        try {
            Professional professional = entityManager.find(Professional.class, id);
            if (professional != null) {
                professional.setActive(false);
                entityManager.merge(professional);
                logger.info("Profesional desactivado con ID: {}", id);
            } else {
                throw new IllegalArgumentException("Profesional no encontrado con ID: " + id);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al eliminar profesional de BD", e);
            throw new RuntimeException("Error al eliminar profesional: " + e.getMessage(), e);
        }
    }

    // Métodos para pacientes
    public List<Patient> getAllPatients() {
        try {
            TypedQuery<Patient> query = entityManager.createQuery(
                "SELECT p FROM Patient p WHERE p.active = true ORDER BY p.name",
                Patient.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al obtener pacientes de la BD", e);
            return new ArrayList<>();
        }
    }

    public List<Patient> getPatientsByClinic(String clinicId) {
        try {
            TypedQuery<Patient> query = entityManager.createQuery(
                "SELECT p FROM Patient p WHERE p.clinic.id = :clinicId AND p.active = true ORDER BY p.name",
                Patient.class);
            query.setParameter("clinicId", clinicId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al obtener pacientes de la clínica {} de la BD", clinicId, e);
            return new ArrayList<>();
        }
    }

    public Optional<Patient> getPatientById(Long id) {
        try {
            Patient patient = entityManager.find(Patient.class, id);
            return Optional.ofNullable(patient);
        } catch (Exception e) {
            logger.error("Error al obtener paciente {} de la BD", id, e);
            return Optional.empty();
        }
    }

    public Optional<Patient> getPatientByInusId(String inusId) {
        try {
            TypedQuery<Patient> query = entityManager.createQuery(
                "SELECT p FROM Patient p WHERE p.inusId = :inusId AND p.active = true",
                Patient.class);
            query.setParameter("inusId", inusId);
            return query.getResultList().stream().findFirst();
        } catch (Exception e) {
            logger.error("Error al obtener paciente por INUS ID {} de la BD", inusId, e);
            return Optional.empty();
        }
    }

    public List<Patient> searchPatientsByName(String name) {
        try {
            String searchTerm = "%" + name.toLowerCase() + "%";
            TypedQuery<Patient> query = entityManager.createQuery(
                "SELECT p FROM Patient p WHERE p.active = true AND (LOWER(p.name) LIKE :term OR LOWER(p.lastName) LIKE :term) ORDER BY p.name",
                Patient.class);
            query.setParameter("term", searchTerm);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al buscar pacientes por nombre en la BD", e);
            return new ArrayList<>();
        }
    }

    @Transactional
    public Patient createPatient(Patient patient) {
        try {
            entityManager.persist(patient);
            entityManager.flush();
            logger.info("Paciente creado en BD: {}", patient);
            return patient;
        } catch (Exception e) {
            logger.error("Error al crear paciente en BD", e);
            throw new RuntimeException("Error al crear paciente: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Patient updatePatient(Patient patient) {
        try {
            Patient updated = entityManager.merge(patient);
            entityManager.flush();
            logger.info("Paciente actualizado en BD: {}", updated);
            return updated;
        } catch (Exception e) {
            logger.error("Error al actualizar paciente en BD", e);
            throw new RuntimeException("Error al actualizar paciente: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deletePatient(Long id) {
        try {
            Patient patient = entityManager.find(Patient.class, id);
            if (patient != null) {
                patient.setActive(false);
                entityManager.merge(patient);
                logger.info("Paciente desactivado con ID: {}", id);
            } else {
                throw new IllegalArgumentException("Paciente no encontrado con ID: " + id);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al eliminar paciente de BD", e);
            throw new RuntimeException("Error al eliminar paciente: " + e.getMessage(), e);
        }
    }

    // Métodos para especialidades
    public List<Specialty> getAllSpecialties() {
        try {
            TypedQuery<Specialty> query = entityManager.createQuery(
                "SELECT s FROM Specialty s WHERE s.active = true ORDER BY s.name",
                Specialty.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al obtener especialidades de la BD", e);
            return new ArrayList<>();
        }
    }

    public Optional<Specialty> getSpecialtyById(Long id) {
        try {
            Specialty specialty = entityManager.find(Specialty.class, id);
            return Optional.ofNullable(specialty);
        } catch (Exception e) {
            logger.error("Error al obtener especialidad {} de la BD", id, e);
            return Optional.empty();
        }
    }

    @Transactional
    public Specialty createSpecialty(Specialty specialty) {
        try {
            entityManager.persist(specialty);
            entityManager.flush();
            logger.info("Especialidad creada en BD: {}", specialty);
            return specialty;
        } catch (Exception e) {
            logger.error("Error al crear especialidad en BD", e);
            throw new RuntimeException("Error al crear especialidad: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Specialty updateSpecialty(Specialty specialty) {
        try {
            Specialty updated = entityManager.merge(specialty);
            entityManager.flush();
            logger.info("Especialidad actualizada en BD: {}", updated);
            return updated;
        } catch (Exception e) {
            logger.error("Error al actualizar especialidad en BD", e);
            throw new RuntimeException("Error al actualizar especialidad: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteSpecialty(Long id) {
        try {
            Specialty specialty = entityManager.find(Specialty.class, id);
            if (specialty != null) {
                specialty.setActive(false);
                entityManager.merge(specialty);
                logger.info("Especialidad desactivada con ID: {}", id);
            } else {
                throw new IllegalArgumentException("Especialidad no encontrada con ID: " + id);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al eliminar especialidad de BD", e);
            throw new RuntimeException("Error al eliminar especialidad: " + e.getMessage(), e);
        }
    }
}
