package uy.gub.hcen.policy.entity;

/**
 * Medical Specialty Enumeration
 *
 * Represents the medical specialties available in the HCEN system.
 * Used for defining access policies based on healthcare professional specialties.
 *
 * Display names are in Spanish as per HCEN system requirements.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-18
 */
public enum MedicalSpecialty {

    /**
     * Cardiology - Heart and cardiovascular system
     */
    CARDIOLOGIA("Cardiologia"),

    /**
     * General Medicine - Primary care and general health
     */
    MEDICINA_GENERAL("Medicina General"),

    /**
     * Oncology - Cancer treatment and research
     */
    ONCOLOGIA("Oncologia"),

    /**
     * Pediatrics - Child and adolescent medicine
     */
    PEDIATRIA("Pediatria"),

    /**
     * Neurology - Nervous system disorders
     */
    NEUROLOGIA("Neurologia"),

    /**
     * Surgery - Surgical procedures
     */
    CIRUGIA("Cirugia"),

    /**
     * Gynecology - Women's reproductive health
     */
    GINECOLOGIA("Ginecologia"),

    /**
     * Dermatology - Skin conditions
     */
    DERMATOLOGIA("Dermatologia"),

    /**
     * Psychiatry - Mental health
     */
    PSIQUIATRIA("Psiquiatria"),

    /**
     * Traumatology - Musculoskeletal injuries
     */
    TRAUMATOLOGIA("Traumatologia");

    private final String displayName;

    /**
     * Constructor with display name.
     *
     * @param displayName Human-readable name in Spanish
     */
    MedicalSpecialty(String displayName) {
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
     * Finds a MedicalSpecialty by its name (case-insensitive).
     *
     * @param name Specialty name
     * @return MedicalSpecialty or null if not found
     */
    public static MedicalSpecialty fromName(String name) {
        if (name == null) {
            return null;
        }

        for (MedicalSpecialty specialty : values()) {
            if (specialty.name().equalsIgnoreCase(name) ||
                specialty.displayName.equalsIgnoreCase(name)) {
                return specialty;
            }
        }

        return null;
    }
}
