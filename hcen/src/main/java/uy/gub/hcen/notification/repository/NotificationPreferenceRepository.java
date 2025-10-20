package uy.gub.hcen.notification.repository;

import uy.gub.hcen.notification.entity.NotificationPreference;

import java.util.Optional;

/**
 * Notification Preference Repository Interface
 *
 * Data access interface for patient notification preferences.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 * @see NotificationPreference
 */
public interface NotificationPreferenceRepository {

    /**
     * Saves or updates notification preferences
     *
     * @param preference The preference to save
     * @return The saved preference
     */
    NotificationPreference save(NotificationPreference preference);

    /**
     * Finds preferences by user CI
     *
     * @param userCi User's CI
     * @return Optional containing preferences if found
     */
    Optional<NotificationPreference> findByUserCi(String userCi);

    /**
     * Updates notification preferences
     *
     * @param preference The preference to update
     * @return The updated preference
     */
    NotificationPreference update(NotificationPreference preference);

    /**
     * Updates FCM token for a user
     *
     * @param userCi User's CI
     * @param fcmToken New FCM token
     * @return true if updated, false otherwise
     */
    boolean updateFcmToken(String userCi, String fcmToken);

    /**
     * Deletes preferences for a user
     *
     * @param userCi User's CI
     * @return true if deleted, false otherwise
     */
    boolean deleteByUserCi(String userCi);

    /**
     * Checks if preferences exist for a user
     *
     * @param userCi User's CI
     * @return true if preferences exist
     */
    boolean existsByUserCi(String userCi);
}
