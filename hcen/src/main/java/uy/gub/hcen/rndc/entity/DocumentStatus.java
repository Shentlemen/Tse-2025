package uy.gub.hcen.rndc.entity;

/**
 * Document Status Enumeration
 *
 * Defines the lifecycle states of clinical documents in the RNDC.
 * Documents use a soft-delete pattern where they are marked as DELETED
 * rather than physically removed from the database.
 *
 * <p>Status Transitions:
 * <pre>
 * ACTIVE → INACTIVE (archival)
 * ACTIVE → DELETED (soft delete)
 * INACTIVE → ACTIVE (reactivation)
 * INACTIVE → DELETED (soft delete)
 * DELETED → (no transitions, permanent state)
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
public enum DocumentStatus {

    /**
     * Document is active and available for access
     * This is the default state for newly registered documents
     */
    ACTIVE("Active", "Document is available and accessible"),

    /**
     * Document is inactive/archived but not deleted
     * Inactive documents are not shown in normal searches but can be retrieved if needed
     */
    INACTIVE("Inactive", "Document is archived"),

    /**
     * Document has been soft-deleted
     * Deleted documents are excluded from all searches and access requests
     * Physical deletion is not performed to maintain audit trail
     */
    DELETED("Deleted", "Document has been removed");

    private final String displayName;
    private final String description;

    /**
     * Constructor
     *
     * @param displayName Human-readable display name
     * @param description Description of this status
     */
    DocumentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the human-readable display name
     *
     * @return Display name for UI presentation
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the description of this status
     *
     * @return Description text
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if documents with this status can be accessed
     *
     * @return true if status allows access, false otherwise
     */
    public boolean isAccessible() {
        return this == ACTIVE || this == INACTIVE;
    }

    /**
     * Checks if documents with this status are included in normal searches
     *
     * @return true if status is included in searches, false otherwise
     */
    public boolean isSearchable() {
        return this == ACTIVE;
    }

    /**
     * Checks if documents with this status can transition to another status
     *
     * @param targetStatus The target status to transition to
     * @return true if transition is allowed, false otherwise
     */
    public boolean canTransitionTo(DocumentStatus targetStatus) {
        if (this == targetStatus) {
            return false; // No self-transition
        }

        switch (this) {
            case ACTIVE:
                // ACTIVE can transition to INACTIVE or DELETED
                return targetStatus == INACTIVE || targetStatus == DELETED;

            case INACTIVE:
                // INACTIVE can transition to ACTIVE or DELETED
                return targetStatus == ACTIVE || targetStatus == DELETED;

            case DELETED:
                // DELETED is a terminal state, no transitions allowed
                return false;

            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}
