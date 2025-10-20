package uy.gub.hcen.policy.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import uy.gub.hcen.policy.entity.AccessPolicy;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyType;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyEffect;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Access Policy Repository Implementation
 *
 * JPA-based implementation for access policy data access.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@Stateless
public class AccessPolicyRepositoryImpl implements AccessPolicyRepository {

    private static final Logger LOGGER = Logger.getLogger(AccessPolicyRepositoryImpl.class.getName());

    @PersistenceContext(unitName = "hcen-pu")
    private EntityManager entityManager;

    @Override
    public AccessPolicy save(AccessPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("Policy cannot be null");
        }

        try {
            if (policy.getId() == null) {
                entityManager.persist(policy);
                LOGGER.log(Level.INFO, "Persisted new policy: {0}", policy.getId());
            } else {
                policy = entityManager.merge(policy);
                LOGGER.log(Level.INFO, "Updated policy: {0}", policy.getId());
            }
            return policy;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving policy", e);
            throw e;
        }
    }

    @Override
    public Optional<AccessPolicy> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        try {
            AccessPolicy policy = entityManager.find(AccessPolicy.class, id);
            return Optional.ofNullable(policy);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding policy by ID: " + id, e);
            return Optional.empty();
        }
    }

    @Override
    public List<AccessPolicy> findByPatientCi(String patientCi) {
        try {
            LocalDateTime now = LocalDateTime.now();
            TypedQuery<AccessPolicy> query = entityManager.createQuery(
                    "SELECT p FROM AccessPolicy p WHERE p.patientCi = :patientCi " +
                    "AND (p.validFrom IS NULL OR p.validFrom <= :now) " +
                    "AND (p.validUntil IS NULL OR p.validUntil >= :now) " +
                    "ORDER BY p.priority DESC, p.createdAt DESC",
                    AccessPolicy.class
            );
            query.setParameter("patientCi", patientCi);
            query.setParameter("now", now);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding policies by patient CI", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AccessPolicy> findAllByPatientCi(String patientCi) {
        try {
            TypedQuery<AccessPolicy> query = entityManager.createQuery(
                    "SELECT p FROM AccessPolicy p WHERE p.patientCi = :patientCi ORDER BY p.priority DESC, p.createdAt DESC",
                    AccessPolicy.class
            );
            query.setParameter("patientCi", patientCi);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding all policies by patient CI", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AccessPolicy> findByPatientCiAndType(String patientCi, PolicyType policyType) {
        try {
            LocalDateTime now = LocalDateTime.now();
            TypedQuery<AccessPolicy> query = entityManager.createQuery(
                    "SELECT p FROM AccessPolicy p WHERE p.patientCi = :patientCi AND p.policyType = :type " +
                    "AND (p.validFrom IS NULL OR p.validFrom <= :now) " +
                    "AND (p.validUntil IS NULL OR p.validUntil >= :now) " +
                    "ORDER BY p.priority DESC",
                    AccessPolicy.class
            );
            query.setParameter("patientCi", patientCi);
            query.setParameter("type", policyType);
            query.setParameter("now", now);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding policies by patient CI and type", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AccessPolicy> findByPatientCiAndTypeAndEffect(
            String patientCi, PolicyType policyType, PolicyEffect policyEffect) {
        try {
            LocalDateTime now = LocalDateTime.now();
            TypedQuery<AccessPolicy> query = entityManager.createQuery(
                    "SELECT p FROM AccessPolicy p WHERE p.patientCi = :patientCi AND p.policyType = :type AND p.policyEffect = :effect " +
                    "AND (p.validFrom IS NULL OR p.validFrom <= :now) " +
                    "AND (p.validUntil IS NULL OR p.validUntil >= :now) " +
                    "ORDER BY p.priority DESC",
                    AccessPolicy.class
            );
            query.setParameter("patientCi", patientCi);
            query.setParameter("type", policyType);
            query.setParameter("effect", policyEffect);
            query.setParameter("now", now);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding policies by patient CI, type, and effect", e);
            return new ArrayList<>();
        }
    }

    @Override
    public AccessPolicy update(AccessPolicy policy) {
        if (policy == null || policy.getId() == null) {
            throw new IllegalArgumentException("Policy and ID cannot be null for update");
        }

        try {
            AccessPolicy updated = entityManager.merge(policy);
            LOGGER.log(Level.INFO, "Updated policy: {0}", policy.getId());
            return updated;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating policy", e);
            throw e;
        }
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }

        try {
            Optional<AccessPolicy> policyOpt = findById(id);
            if (policyOpt.isPresent()) {
                entityManager.remove(policyOpt.get());
                LOGGER.log(Level.INFO, "Deleted policy: {0}", id);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting policy", e);
            return false;
        }
    }

    @Override
    public int deleteByPatientCi(String patientCi) {
        try {
            int deleted = entityManager.createQuery(
                    "DELETE FROM AccessPolicy p WHERE p.patientCi = :patientCi"
            ).setParameter("patientCi", patientCi).executeUpdate();

            LOGGER.log(Level.INFO, "Deleted {0} policies for patient {1}", new Object[]{deleted, patientCi});
            return deleted;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting policies by patient CI", e);
            return 0;
        }
    }

    @Override
    public long countByPatientCi(String patientCi) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(p) FROM AccessPolicy p WHERE p.patientCi = :patientCi",
                    Long.class
            );
            query.setParameter("patientCi", patientCi);

            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting policies by patient CI", e);
            return 0;
        }
    }

    @Override
    public long countByType(PolicyType policyType) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(p) FROM AccessPolicy p WHERE p.policyType = :type",
                    Long.class
            );
            query.setParameter("type", policyType);

            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting policies by type", e);
            return 0;
        }
    }
}
