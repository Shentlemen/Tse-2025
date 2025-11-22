package uy.gub.hcen.rndc.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import uy.gub.hcen.rndc.entity.RndcDocument;
import uy.gub.hcen.rndc.entity.DocumentType;
import uy.gub.hcen.rndc.entity.DocumentStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RNDC Repository Implementation
 *
 * JPA-based implementation of the RndcRepository interface.
 * Uses EntityManager for database operations and Criteria API for dynamic queries.
 *
 * <p>This implementation is stateless and thread-safe, making it suitable
 * for use in a clustered environment.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 * @see RndcRepository
 * @see RndcDocument
 */
@Stateless
public class RndcRepositoryImpl implements RndcRepository {

    private static final Logger LOGGER = Logger.getLogger(RndcRepositoryImpl.class.getName());

    @PersistenceContext(unitName = "hcen-pu")
    private EntityManager entityManager;

    @Override
    public RndcDocument save(RndcDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }

        try {
            if (document.getId() == null) {
                // New document - persist
                entityManager.persist(document);
                LOGGER.log(Level.INFO, "Persisted new document: {0}", document.getId());
            } else {
                // Existing document - merge
                document = entityManager.merge(document);
                LOGGER.log(Level.INFO, "Updated document: {0}", document.getId());
            }
            return document;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving document", e);
            throw e;
        }
    }

    @Override
    public Optional<RndcDocument> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        try {
            RndcDocument document = entityManager.find(RndcDocument.class, id);
            return Optional.ofNullable(document);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding document by ID: " + id, e);
            return Optional.empty();
        }
    }

    @Override
    public List<RndcDocument> findByPatientCi(String patientCi, int page, int size) {
        return findByPatientCiAndStatus(patientCi, DocumentStatus.ACTIVE, page, size);
    }

    @Override
    public List<RndcDocument> findByPatientCiAndStatus(String patientCi, DocumentStatus status, int page, int size) {
        try {
            TypedQuery<RndcDocument> query = entityManager.createQuery(
                    "SELECT d FROM RndcDocument d WHERE d.patientCi = :patientCi AND d.status = :status ORDER BY d.createdAt DESC",
                    RndcDocument.class
            );
            query.setParameter("patientCi", patientCi);
            query.setParameter("status", status);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding documents by patient CI and status", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<RndcDocument> findByPatientCiAndType(String patientCi, DocumentType type, int page, int size) {
        return findByPatientCiAndTypeAndStatus(patientCi, type, DocumentStatus.ACTIVE, page, size);
    }

    @Override
    public List<RndcDocument> findByPatientCiAndTypeAndStatus(
            String patientCi, DocumentType type, DocumentStatus status, int page, int size) {
        try {
            TypedQuery<RndcDocument> query = entityManager.createQuery(
                    "SELECT d FROM RndcDocument d WHERE d.patientCi = :patientCi AND d.documentType = :type AND d.status = :status ORDER BY d.createdAt DESC",
                    RndcDocument.class
            );
            query.setParameter("patientCi", patientCi);
            query.setParameter("type", type);
            query.setParameter("status", status);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding documents by patient CI, type, and status", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<RndcDocument> findByPatientCiAndDateRange(
            String patientCi, LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {
        try {
            TypedQuery<RndcDocument> query = entityManager.createQuery(
                    "SELECT d FROM RndcDocument d WHERE d.patientCi = :patientCi AND d.createdAt BETWEEN :fromDate AND :toDate AND d.status = :status ORDER BY d.createdAt DESC",
                    RndcDocument.class
            );
            query.setParameter("patientCi", patientCi);
            query.setParameter("fromDate", fromDate);
            query.setParameter("toDate", toDate);
            query.setParameter("status", DocumentStatus.ACTIVE);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding documents by patient CI and date range", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<RndcDocument> findByClinicId(String clinicId, int page, int size) {
        return findByClinicIdAndStatus(clinicId, DocumentStatus.ACTIVE, page, size);
    }

    @Override
    public List<RndcDocument> findByClinicIdAndStatus(String clinicId, DocumentStatus status, int page, int size) {
        try {
            TypedQuery<RndcDocument> query = entityManager.createQuery(
                    "SELECT d FROM RndcDocument d WHERE d.clinicId = :clinicId AND d.status = :status ORDER BY d.createdAt DESC",
                    RndcDocument.class
            );
            query.setParameter("clinicId", clinicId);
            query.setParameter("status", status);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding documents by clinic ID and status", e);
            return new ArrayList<>();
        }
    }

    @Override
    public long countByPatientCi(String patientCi) {
        return countByPatientCiAndStatus(patientCi, DocumentStatus.ACTIVE);
    }

    @Override
    public long countByPatientCiAndStatus(String patientCi, DocumentStatus status) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(d) FROM RndcDocument d WHERE d.patientCi = :patientCi AND d.status = :status",
                    Long.class
            );
            query.setParameter("patientCi", patientCi);
            query.setParameter("status", status);

            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting documents by patient CI and status", e);
            return 0;
        }
    }

    @Override
    public boolean updateStatus(Long id, DocumentStatus status) {
        if (id == null || status == null) {
            return false;
        }

        try {
            Optional<RndcDocument> documentOpt = findById(id);
            if (documentOpt.isPresent()) {
                RndcDocument document = documentOpt.get();

                // Validate status transition
                if (!document.getStatus().canTransitionTo(status)) {
                    LOGGER.log(Level.WARNING, "Invalid status transition from {0} to {1} for document {2}",
                            new Object[]{document.getStatus(), status, id});
                    return false;
                }

                document.setStatus(status);
                entityManager.merge(document);
                LOGGER.log(Level.INFO, "Updated status of document {0} to {1}", new Object[]{id, status});
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating document status", e);
            return false;
        }
    }

    @Override
    public boolean existsByLocator(String locator) {
        if (locator == null || locator.trim().isEmpty()) {
            return false;
        }

        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(d) FROM RndcDocument d WHERE d.documentLocator = :locator",
                    Long.class
            );
            query.setParameter("locator", locator);

            return query.getSingleResult() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if locator exists", e);
            return false;
        }
    }

    @Override
    public Optional<RndcDocument> findByLocator(String locator) {
        if (locator == null || locator.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            TypedQuery<RndcDocument> query = entityManager.createQuery(
                    "SELECT d FROM RndcDocument d WHERE d.documentLocator = :locator",
                    RndcDocument.class
            );
            query.setParameter("locator", locator);

            List<RndcDocument> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding document by locator", e);
            return Optional.empty();
        }
    }

    @Override
    public boolean softDelete(Long id) {
        return updateStatus(id, DocumentStatus.DELETED);
    }

    @Override
    public boolean reactivate(Long id) {
        return updateStatus(id, DocumentStatus.ACTIVE);
    }

    @Override
    public List<RndcDocument> findByCreatedBy(String createdBy, int page, int size) {
        try {
            TypedQuery<RndcDocument> query = entityManager.createQuery(
                    "SELECT d FROM RndcDocument d WHERE d.createdBy = :createdBy ORDER BY d.createdAt DESC",
                    RndcDocument.class
            );
            query.setParameter("createdBy", createdBy);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding documents by creator", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<RndcDocument> search(
            String patientCi,
            DocumentType documentType,
            DocumentStatus status,
            String clinicId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size) {

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<RndcDocument> cq = cb.createQuery(RndcDocument.class);
            Root<RndcDocument> document = cq.from(RndcDocument.class);

            List<Predicate> predicates = new ArrayList<>();

            // Add predicates for each non-null parameter
            if (patientCi != null && !patientCi.trim().isEmpty()) {

                if(!patientCi.startsWith("uy-ci-")){
                    patientCi = "uy-ci-" + patientCi ;
                }

                predicates.add(cb.equal(document.get("patientCi"), patientCi));
            }

            if (documentType != null) {
                predicates.add(cb.equal(document.get("documentType"), documentType));
            }

            if (status != null) {
                predicates.add(cb.equal(document.get("status"), status));
            }

            if (clinicId != null && !clinicId.trim().isEmpty()) {
                predicates.add(cb.equal(document.get("clinicId"), clinicId));
            }

            if (fromDate != null && toDate != null) {
                predicates.add(cb.between(document.get("createdAt"), fromDate, toDate));
            } else if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(document.get("createdAt"), fromDate));
            } else if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(document.get("createdAt"), toDate));
            }

            // Combine all predicates with AND
            cq.where(predicates.toArray(new Predicate[0]));

            // Order by creation date descending
            cq.orderBy(cb.desc(document.get("createdAt")));

            TypedQuery<RndcDocument> query = entityManager.createQuery(cq);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error performing search", e);
            return new ArrayList<>();
        }
    }

    @Override
    public long countAll() {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(d) FROM RndcDocument d",
                    Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting all documents", e);
            return 0;
        }
    }

    @Override
    public long countByStatus(DocumentStatus status) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(d) FROM RndcDocument d WHERE d.status = :status",
                    Long.class
            );
            query.setParameter("status", status);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting documents by status", e);
            return 0;
        }
    }
}
