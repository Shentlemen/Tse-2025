package uy.gub.clinic.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.AccessRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar solicitudes de acceso a documentos clínicos
 */
@Stateless
public class AccessRequestService {

    private static final Logger logger = LoggerFactory.getLogger(AccessRequestService.class);

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Crear una nueva solicitud de acceso
     */
    public AccessRequest createRequest(AccessRequest request) {
        try {
            if (request.getRequestedAt() == null) {
                request.setRequestedAt(LocalDateTime.now());
            }
            if (request.getStatus() == null || request.getStatus().isEmpty()) {
                request.setStatus("PENDING");
            }
            entityManager.persist(request);
            entityManager.flush();
            logger.info("Solicitud de acceso creada: ID={}", request.getId());
            return request;
        } catch (Exception e) {
            logger.error("Error al crear solicitud de acceso", e);
            throw e;
        }
    }

    /**
     * Obtener solicitud por ID
     */
    public Optional<AccessRequest> findById(Long id) {
        try {
            AccessRequest request = entityManager.find(AccessRequest.class, id);
            if (request != null) {
                // Cargar relaciones lazy
                if (request.getPatient() != null) {
                    request.getPatient().getFullName();
                }
                if (request.getProfessional() != null) {
                    request.getProfessional().getFullName();
                }
                if (request.getClinic() != null) {
                    request.getClinic().getName();
                }
                if (request.getSpecialty() != null) {
                    request.getSpecialty().getName();
                }
            }
            return Optional.ofNullable(request);
        } catch (Exception e) {
            logger.error("Error al buscar solicitud por ID: {}", id, e);
            return Optional.empty();
        }
    }

    /**
     * Obtener todas las solicitudes pendientes de un profesional
     */
    public List<AccessRequest> findPendingByProfessional(Long professionalId) {
        try {
            TypedQuery<AccessRequest> query = entityManager.createNamedQuery(
                "AccessRequest.findByProfessionalAndStatus", AccessRequest.class);
            query.setParameter("professionalId", professionalId);
            query.setParameter("status", "PENDING");
            
            List<AccessRequest> requests = query.getResultList();
            
            // Cargar relaciones lazy
            for (AccessRequest request : requests) {
                if (request.getPatient() != null) {
                    request.getPatient().getFullName();
                }
                if (request.getProfessional() != null) {
                    request.getProfessional().getFullName();
                }
                if (request.getClinic() != null) {
                    request.getClinic().getName();
                }
                if (request.getSpecialty() != null) {
                    request.getSpecialty().getName();
                }
            }
            
            return requests;
        } catch (Exception e) {
            logger.error("Error al obtener solicitudes pendientes del profesional: {}", professionalId, e);
            return List.of();
        }
    }

    /**
     * Obtener todas las solicitudes de un profesional
     */
    public List<AccessRequest> findByProfessional(Long professionalId) {
        try {
            TypedQuery<AccessRequest> query = entityManager.createNamedQuery(
                "AccessRequest.findByProfessional", AccessRequest.class);
            query.setParameter("professionalId", professionalId);
            
            List<AccessRequest> requests = query.getResultList();
            
            // Cargar relaciones lazy
            for (AccessRequest request : requests) {
                if (request.getPatient() != null) {
                    request.getPatient().getFullName();
                }
                if (request.getProfessional() != null) {
                    request.getProfessional().getFullName();
                }
                if (request.getClinic() != null) {
                    request.getClinic().getName();
                }
                if (request.getSpecialty() != null) {
                    request.getSpecialty().getName();
                }
            }
            
            return requests;
        } catch (Exception e) {
            logger.error("Error al obtener solicitudes del profesional: {}", professionalId, e);
            return List.of();
        }
    }

    /**
     * Actualizar solicitud
     */
    public AccessRequest updateRequest(AccessRequest request) {
        try {
            AccessRequest updated = entityManager.merge(request);
            logger.info("Solicitud de acceso actualizada: ID={}", updated.getId());
            return updated;
        } catch (Exception e) {
            logger.error("Error al actualizar solicitud de acceso", e);
            throw e;
        }
    }

    /**
     * Contar solicitudes pendientes de un profesional
     */
    public long countPendingByProfessional(Long professionalId) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(r) FROM AccessRequest r WHERE r.professional.id = :professionalId AND r.status = 'PENDING'",
                Long.class);
            query.setParameter("professionalId", professionalId);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Error al contar solicitudes pendientes del profesional: {}", professionalId, e);
            return 0;
        }
    }
}

