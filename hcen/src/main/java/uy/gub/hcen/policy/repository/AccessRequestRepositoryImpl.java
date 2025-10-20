package uy.gub.hcen.policy.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import uy.gub.hcen.policy.entity.AccessRequest;
import uy.gub.hcen.policy.entity.AccessRequest.RequestStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Access Request Repository Implementation
 *
 * JPA-based implementation for access request data access.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@Stateless
public class AccessRequestRepositoryImpl implements AccessRequestRepository {

    private static final Logger LOGGER = Logger.getLogger(AccessRequestRepositoryImpl.class.getName());

    @PersistenceContext(unitName = "hcen-pu")
    private EntityManager entityManager;

    @Override
    public AccessRequest save(AccessRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        try {
            if (request.getId() == null) {
                entityManager.persist(request);
                LOGGER.log(Level.INFO, "Persisted new access request: {0}", request.getId());
            } else {
                request = entityManager.merge(request);
                LOGGER.log(Level.INFO, "Updated access request: {0}", request.getId());
            }
            return request;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving access request", e);
            throw e;
        }
    }

    @Override
    public Optional<AccessRequest> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        try {
            AccessRequest request = entityManager.find(AccessRequest.class, id);
            return Optional.ofNullable(request);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding access request by ID: " + id, e);
            return Optional.empty();
        }
    }

    @Override
    public List<AccessRequest> findPendingByPatientCi(String patientCi) {
        try {
            LocalDateTime now = LocalDateTime.now();
            TypedQuery<AccessRequest> query = entityManager.createQuery(
                    "SELECT r FROM AccessRequest r WHERE r.patientCi = :patientCi AND r.status = :status AND r.expiresAt > :now ORDER BY r.requestedAt DESC",
                    AccessRequest.class
            );
            query.setParameter("patientCi", patientCi);
            query.setParameter("status", RequestStatus.PENDING);
            query.setParameter("now", now);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding pending requests by patient CI", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AccessRequest> findByPatientCi(String patientCi, int page, int size) {
        try {
            TypedQuery<AccessRequest> query = entityManager.createQuery(
                    "SELECT r FROM AccessRequest r WHERE r.patientCi = :patientCi ORDER BY r.requestedAt DESC",
                    AccessRequest.class
            );
            query.setParameter("patientCi", patientCi);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding requests by patient CI", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AccessRequest> findByPatientCiAndStatus(String patientCi, RequestStatus status, int page, int size) {
        try {
            TypedQuery<AccessRequest> query = entityManager.createQuery(
                    "SELECT r FROM AccessRequest r WHERE r.patientCi = :patientCi AND r.status = :status ORDER BY r.requestedAt DESC",
                    AccessRequest.class
            );
            query.setParameter("patientCi", patientCi);
            query.setParameter("status", status);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding requests by patient CI and status", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AccessRequest> findByProfessionalId(String professionalId, int page, int size) {
        try {
            TypedQuery<AccessRequest> query = entityManager.createQuery(
                    "SELECT r FROM AccessRequest r WHERE r.professionalId = :professionalId ORDER BY r.requestedAt DESC",
                    AccessRequest.class
            );
            query.setParameter("professionalId", professionalId);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding requests by professional ID", e);
            return new ArrayList<>();
        }
    }

    @Override
    public AccessRequest update(AccessRequest request) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("Request and ID cannot be null for update");
        }

        try {
            AccessRequest updated = entityManager.merge(request);
            LOGGER.log(Level.INFO, "Updated access request: {0}", request.getId());
            return updated;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating access request", e);
            throw e;
        }
    }

    @Override
    public List<AccessRequest> findExpiredPendingRequests() {
        try {
            LocalDateTime now = LocalDateTime.now();
            TypedQuery<AccessRequest> query = entityManager.createQuery(
                    "SELECT r FROM AccessRequest r WHERE r.status = :status AND r.expiresAt <= :now",
                    AccessRequest.class
            );
            query.setParameter("status", RequestStatus.PENDING);
            query.setParameter("now", now);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding expired pending requests", e);
            return new ArrayList<>();
        }
    }

    @Override
    public int markExpiredRequests() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int updated = entityManager.createQuery(
                    "UPDATE AccessRequest r SET r.status = :expiredStatus WHERE r.status = :pendingStatus AND r.expiresAt <= :now"
            ).setParameter("expiredStatus", RequestStatus.EXPIRED)
             .setParameter("pendingStatus", RequestStatus.PENDING)
             .setParameter("now", now)
             .executeUpdate();

            LOGGER.log(Level.INFO, "Marked {0} requests as expired", updated);
            return updated;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error marking expired requests", e);
            return 0;
        }
    }

    @Override
    public long countPendingByPatientCi(String patientCi) {
        try {
            LocalDateTime now = LocalDateTime.now();
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(r) FROM AccessRequest r WHERE r.patientCi = :patientCi AND r.status = :status AND r.expiresAt > :now",
                    Long.class
            );
            query.setParameter("patientCi", patientCi);
            query.setParameter("status", RequestStatus.PENDING);
            query.setParameter("now", now);

            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting pending requests by patient CI", e);
            return 0;
        }
    }

    @Override
    public long countByStatus(RequestStatus status) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(r) FROM AccessRequest r WHERE r.status = :status",
                    Long.class
            );
            query.setParameter("status", status);

            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting requests by status", e);
            return 0;
        }
    }
}
