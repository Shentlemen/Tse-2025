package uy.gub.hcen.notification.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import uy.gub.hcen.notification.entity.NotificationPreference;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Notification Preference Repository Implementation
 *
 * JPA-based implementation for notification preference data access.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@Stateless
public class NotificationPreferenceRepositoryImpl implements NotificationPreferenceRepository {

    private static final Logger LOGGER = Logger.getLogger(NotificationPreferenceRepositoryImpl.class.getName());

    @PersistenceContext(unitName = "hcen-pu")
    private EntityManager entityManager;

    @Override
    public NotificationPreference save(NotificationPreference preference) {
        if (preference == null) {
            throw new IllegalArgumentException("Preference cannot be null");
        }

        try {
            if (preference.getId() == null) {
                entityManager.persist(preference);
                LOGGER.log(Level.INFO, "Persisted notification preference for user: {0}", preference.getUserCi());
            } else {
                preference = entityManager.merge(preference);
                LOGGER.log(Level.INFO, "Updated notification preference for user: {0}", preference.getUserCi());
            }
            return preference;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving notification preference", e);
            throw e;
        }
    }

    @Override
    public Optional<NotificationPreference> findByUserCi(String userCi) {
        if (userCi == null || userCi.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            TypedQuery<NotificationPreference> query = entityManager.createQuery(
                    "SELECT n FROM NotificationPreference n WHERE n.userCi = :userCi",
                    NotificationPreference.class
            );
            query.setParameter("userCi", userCi);

            List<NotificationPreference> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding notification preference by user CI", e);
            return Optional.empty();
        }
    }

    @Override
    public NotificationPreference update(NotificationPreference preference) {
        if (preference == null || preference.getId() == null) {
            throw new IllegalArgumentException("Preference and ID cannot be null for update");
        }

        try {
            NotificationPreference updated = entityManager.merge(preference);
            LOGGER.log(Level.INFO, "Updated notification preference: {0}", preference.getId());
            return updated;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating notification preference", e);
            throw e;
        }
    }

    @Override
    public boolean updateFcmToken(String userCi, String fcmToken) {
        if (userCi == null || userCi.trim().isEmpty()) {
            return false;
        }

        try {
            Optional<NotificationPreference> preferenceOpt = findByUserCi(userCi);
            if (preferenceOpt.isPresent()) {
                NotificationPreference preference = preferenceOpt.get();
                preference.setFcmToken(fcmToken);
                entityManager.merge(preference);
                LOGGER.log(Level.INFO, "Updated FCM token for user: {0}", userCi);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating FCM token", e);
            return false;
        }
    }

    @Override
    public boolean deleteByUserCi(String userCi) {
        if (userCi == null || userCi.trim().isEmpty()) {
            return false;
        }

        try {
            Optional<NotificationPreference> preferenceOpt = findByUserCi(userCi);
            if (preferenceOpt.isPresent()) {
                entityManager.remove(preferenceOpt.get());
                LOGGER.log(Level.INFO, "Deleted notification preference for user: {0}", userCi);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting notification preference", e);
            return false;
        }
    }

    @Override
    public boolean existsByUserCi(String userCi) {
        if (userCi == null || userCi.trim().isEmpty()) {
            return false;
        }

        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(n) FROM NotificationPreference n WHERE n.userCi = :userCi",
                    Long.class
            );
            query.setParameter("userCi", userCi);

            return query.getSingleResult() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if notification preference exists", e);
            return false;
        }
    }
}
