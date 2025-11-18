package uy.gub.hcen.policy.entity;

/**
 * Policy Status Enumeration
 *
 * Represents the current status of an access policy in the HCEN system.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-18
 */
public enum PolicyStatus {

    /**
     * Policy is active and grants access to professionals
     * matching the specified clinic and specialty.
     */
    GRANTED("Otorgado"),

    /**
     * Policy is pending patient approval.
     * Used when a policy is created from an access request.
     */
    PENDING("Pendiente"),

    /**
     * Policy has been revoked by the patient.
     * Access is no longer granted.
     */
    REVOKED("Revocado");

    private final String displayName;

    /**
     * Constructor with display name.
     *
     * @param displayName Human-readable name in Spanish
     */
    PolicyStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the human-readable display name in Spanish.
     *
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the display name as the string representation.
     *
     * @return Display name
     */
    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Checks if this status represents an active policy.
     *
     * @return true if policy is GRANTED
     */
    public boolean isActive() {
        return this == GRANTED;
    }

    /**
     * Finds a PolicyStatus by its name (case-insensitive).
     *
     * @param name Status name
     * @return PolicyStatus or null if not found
     */
    public static PolicyStatus fromName(String name) {
        if (name == null) {
            return null;
        }

        for (PolicyStatus status : values()) {
            if (status.name().equalsIgnoreCase(name) ||
                status.displayName.equalsIgnoreCase(name)) {
                return status;
            }
        }

        return null;
    }
}
