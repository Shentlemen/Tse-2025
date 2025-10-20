package uy.gub.hcen.audit.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import uy.gub.hcen.audit.entity.AuditLog;
import uy.gub.hcen.audit.entity.AuditLog.EventType;
import uy.gub.hcen.audit.entity.AuditLog.ActionOutcome;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Audit Log Repository Implementation
 *
 * JPA-based implementation for audit log data access.
 * This is an APPEND-ONLY repository - no updates or deletes allowed.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@Stateless
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private static final Logger LOGGER = Logger.getLogger(AuditLogRepositoryImpl.class.getName());

    @PersistenceContext(unitName = "hcen-pu")
    private EntityManager entityManager;

    @Override
    public AuditLog save(AuditLog auditLog) {
        if (auditLog == null) {
            throw new IllegalArgumentException("Audit log cannot be null");
        }

        try {
            entityManager.persist(auditLog);
            LOGGER.log(Level.FINE, "Persisted audit log: {0}", auditLog.getId());
            return auditLog;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving audit log", e);
            throw e;
        }
    }

    @Override
    public List<AuditLog> findByActorId(String actorId, int page, int size) {
        try {
            TypedQuery<AuditLog> query = entityManager.createQuery(
                    "SELECT a FROM AuditLog a WHERE a.actorId = :actorId ORDER BY a.timestamp DESC",
                    AuditLog.class
            );
            query.setParameter("actorId", actorId);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding audit logs by actor ID", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AuditLog> findByResource(String resourceType, String resourceId, int page, int size) {
        try {
            TypedQuery<AuditLog> query = entityManager.createQuery(
                    "SELECT a FROM AuditLog a WHERE a.resourceType = :resourceType AND a.resourceId = :resourceId ORDER BY a.timestamp DESC",
                    AuditLog.class
            );
            query.setParameter("resourceType", resourceType);
            query.setParameter("resourceId", resourceId);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding audit logs by resource", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AuditLog> findByEventType(EventType eventType, int page, int size) {
        try {
            TypedQuery<AuditLog> query = entityManager.createQuery(
                    "SELECT a FROM AuditLog a WHERE a.eventType = :eventType ORDER BY a.timestamp DESC",
                    AuditLog.class
            );
            query.setParameter("eventType", eventType);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding audit logs by event type", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AuditLog> findByDateRange(LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {
        try {
            TypedQuery<AuditLog> query = entityManager.createQuery(
                    "SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :fromDate AND :toDate ORDER BY a.timestamp DESC",
                    AuditLog.class
            );
            query.setParameter("fromDate", fromDate);
            query.setParameter("toDate", toDate);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding audit logs by date range", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AuditLog> getPatientAccessHistory(String patientCi, int page, int size) {
        try {
            // Find all ACCESS events where the resource is a document for this patient
            // This requires joining with RNDC or storing patient CI in audit details
            TypedQuery<AuditLog> query = entityManager.createQuery(
                    "SELECT a FROM AuditLog a WHERE a.eventType = :eventType " +
                    "AND a.resourceType = 'DOCUMENT' " +
                    "AND a.details LIKE :patientCiPattern " +
                    "ORDER BY a.timestamp DESC",
                    AuditLog.class
            );
            query.setParameter("eventType", EventType.ACCESS);
            query.setParameter("patientCiPattern", "%\"patientCi\":\"" + patientCi + "\"%");
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting patient access history", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AuditLog> search(
            EventType eventType,
            String actorId,
            String resourceType,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            ActionOutcome actionOutcome,
            int page,
            int size) {

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<AuditLog> cq = cb.createQuery(AuditLog.class);
            Root<AuditLog> auditLog = cq.from(AuditLog.class);

            List<Predicate> predicates = new ArrayList<>();

            if (eventType != null) {
                predicates.add(cb.equal(auditLog.get("eventType"), eventType));
            }

            if (actorId != null && !actorId.trim().isEmpty()) {
                predicates.add(cb.equal(auditLog.get("actorId"), actorId));
            }

            if (resourceType != null && !resourceType.trim().isEmpty()) {
                predicates.add(cb.equal(auditLog.get("resourceType"), resourceType));
            }

            if (fromDate != null && toDate != null) {
                predicates.add(cb.between(auditLog.get("timestamp"), fromDate, toDate));
            } else if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(auditLog.get("timestamp"), fromDate));
            } else if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(auditLog.get("timestamp"), toDate));
            }

            if (actionOutcome != null) {
                predicates.add(cb.equal(auditLog.get("actionOutcome"), actionOutcome));
            }

            cq.where(predicates.toArray(new Predicate[0]));
            cq.orderBy(cb.desc(auditLog.get("timestamp")));

            TypedQuery<AuditLog> query = entityManager.createQuery(cq);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error performing audit log search", e);
            return new ArrayList<>();
        }
    }

    @Override
    public long countAll() {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(a) FROM AuditLog a",
                    Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting all audit logs", e);
            return 0;
        }
    }

    @Override
    public long countByEventType(EventType eventType) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(a) FROM AuditLog a WHERE a.eventType = :eventType",
                    Long.class
            );
            query.setParameter("eventType", eventType);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting audit logs by event type", e);
            return 0;
        }
    }

    @Override
    public long countByOutcome(ActionOutcome actionOutcome) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(a) FROM AuditLog a WHERE a.actionOutcome = :actionOutcome",
                    Long.class
            );
            query.setParameter("actionOutcome", actionOutcome);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting audit logs by outcome", e);
            return 0;
        }
    }
}
