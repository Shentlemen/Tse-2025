package uy.gub.hcen.clinic.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import uy.gub.hcen.clinic.entity.Clinic;
import uy.gub.hcen.clinic.entity.Clinic.ClinicStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clinic Repository Implementation
 *
 * JPA-based implementation for clinic data access.
 * Provides efficient querying and pagination for clinic management.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@Stateless
public class ClinicRepositoryImpl implements ClinicRepository {

    private static final Logger LOGGER = Logger.getLogger(ClinicRepositoryImpl.class.getName());

    @PersistenceContext(unitName = "hcen-pu")
    private EntityManager entityManager;

    @Override
    public Clinic save(Clinic clinic) {
        if (clinic == null) {
            throw new IllegalArgumentException("Clinic cannot be null");
        }
        if (clinic.getClinicId() == null || clinic.getClinicId().trim().isEmpty()) {
            throw new IllegalArgumentException("Clinic ID cannot be null or empty");
        }

        try {
            entityManager.persist(clinic);
            LOGGER.log(Level.INFO, "Persisted new clinic: {0}", clinic.getClinicId());
            return clinic;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving clinic: " + clinic.getClinicId(), e);
            throw e;
        }
    }

    @Override
    public Optional<Clinic> findById(String clinicId) {
        if (clinicId == null || clinicId.trim().isEmpty()) {
            throw new IllegalArgumentException("Clinic ID cannot be null or empty");
        }

        try {
            Clinic clinic = entityManager.find(Clinic.class, clinicId);
            return Optional.ofNullable(clinic);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding clinic by ID: " + clinicId, e);
            return Optional.empty();
        }
    }

    @Override
    public List<Clinic> findAll(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }

        try {
            TypedQuery<Clinic> query = entityManager.createQuery(
                    "SELECT c FROM Clinic c ORDER BY c.createdAt DESC",
                    Clinic.class
            );
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding all clinics", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Clinic> findByStatus(ClinicStatus status, int page, int size) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }

        try {
            TypedQuery<Clinic> query = entityManager.createQuery(
                    "SELECT c FROM Clinic c WHERE c.status = :status ORDER BY c.createdAt DESC",
                    Clinic.class
            );
            query.setParameter("status", status);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding clinics by status: " + status, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Clinic> findByCity(String city, int page, int size) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City cannot be null or empty");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }

        try {
            TypedQuery<Clinic> query = entityManager.createQuery(
                    "SELECT c FROM Clinic c WHERE c.city = :city ORDER BY c.clinicName ASC",
                    Clinic.class
            );
            query.setParameter("city", city);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding clinics by city: " + city, e);
            return new ArrayList<>();
        }
    }

    @Override
    public Clinic update(Clinic clinic) {
        if (clinic == null) {
            throw new IllegalArgumentException("Clinic cannot be null");
        }
        if (clinic.getClinicId() == null || clinic.getClinicId().trim().isEmpty()) {
            throw new IllegalArgumentException("Clinic ID cannot be null or empty for update");
        }

        try {
            Clinic updated = entityManager.merge(clinic);
            LOGGER.log(Level.INFO, "Updated clinic: {0}", clinic.getClinicId());
            return updated;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating clinic: " + clinic.getClinicId(), e);
            throw e;
        }
    }

    @Override
    public boolean updateStatus(String clinicId, ClinicStatus status) {
        if (clinicId == null || clinicId.trim().isEmpty()) {
            throw new IllegalArgumentException("Clinic ID cannot be null or empty");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        try {
            Optional<Clinic> clinicOpt = findById(clinicId);
            if (clinicOpt.isPresent()) {
                Clinic clinic = clinicOpt.get();
                clinic.setStatus(status);

                // Set onboardedAt timestamp if transitioning to ACTIVE
                if (status == ClinicStatus.ACTIVE && clinic.getOnboardedAt() == null) {
                    clinic.activate(); // Uses the entity's activate() method
                }

                entityManager.merge(clinic);
                LOGGER.log(Level.INFO, "Updated status for clinic {0} to {1}",
                          new Object[]{clinicId, status});
                return true;
            }

            LOGGER.log(Level.WARNING, "Clinic not found for status update: {0}", clinicId);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating status for clinic: " + clinicId, e);
            throw e;
        }
    }

    @Override
    public boolean existsById(String clinicId) {
        if (clinicId == null || clinicId.trim().isEmpty()) {
            throw new IllegalArgumentException("Clinic ID cannot be null or empty");
        }

        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(c) FROM Clinic c WHERE c.clinicId = :clinicId",
                    Long.class
            );
            query.setParameter("clinicId", clinicId);

            return query.getSingleResult() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if clinic exists: " + clinicId, e);
            return false;
        }
    }

    @Override
    public long count() {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(c) FROM Clinic c",
                    Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting clinics", e);
            return 0;
        }
    }

    @Override
    public long countByStatus(ClinicStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(c) FROM Clinic c WHERE c.status = :status",
                    Long.class
            );
            query.setParameter("status", status);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting clinics by status: " + status, e);
            return 0;
        }
    }

    @Override
    public Optional<Clinic> findByIdAndApiKey(String clinicId, String apiKey) {
        if (clinicId == null || clinicId.trim().isEmpty()) {
            throw new IllegalArgumentException("Clinic ID cannot be null or empty");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }

        try {
            TypedQuery<Clinic> query = entityManager.createQuery(
                    "SELECT c FROM Clinic c WHERE c.clinicId = :clinicId AND c.apiKey = :apiKey",
                    Clinic.class
            );
            query.setParameter("clinicId", clinicId);
            query.setParameter("apiKey", apiKey);

            List<Clinic> results = query.getResultList();
            if (results.isEmpty()) {
                LOGGER.log(Level.WARNING, "Clinic not found or API key mismatch for clinic ID: {0}", clinicId);
                return Optional.empty();
            }

            LOGGER.log(Level.INFO, "Successfully authenticated clinic: {0}", clinicId);
            return Optional.of(results.get(0));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding clinic by ID and API key: " + clinicId, e);
            return Optional.empty();
        }
    }
}
