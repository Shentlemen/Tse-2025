package uy.gub.hcen.inus.entity;

/**
 * User Status Enumeration
 *
 * Represents the possible statuses for a user in the INUS
 * (Índice Nacional de Usuarios de Salud) system.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
public enum UserStatus {

    /**
     * User is active and can access the health system
     */
    ACTIVE,

    /**
     * User is inactive (e.g., deceased, moved abroad, etc.)
     * Cannot access the health system until reactivated
     */
    INACTIVE,

    /**
     * User is suspended (e.g., administrative reasons, fraud detection)
     * Cannot access the health system until suspension is lifted
     */
    SUSPENDED;

    /**
     * Get a user-friendly description of the status
     *
     * @return Description string
     */
    public String getDescription() {
        switch (this) {
            case ACTIVE:
                return "Active user with full access to the health system";
            case INACTIVE:
                return "Inactive user - access to the health system is disabled";
            case SUSPENDED:
                return "Suspended user - access temporarily restricted";
            default:
                return "Unknown status";
        }
    }

    /**
     * Check if the status allows access to the health system
     *
     * @return true if status allows access, false otherwise
     */
    public boolean canAccessHealthSystem() {
        return this == ACTIVE;
    }
}
