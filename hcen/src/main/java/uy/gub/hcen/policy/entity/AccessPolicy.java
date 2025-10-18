package uy.gub.hcen.policy.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Access Policy Entity
 *
 * Represents a patient-defined access control policy for clinical documents in the HCEN system.
 * Policies enable patients to define granular access control rules based on various attributes
 * such as document type, professional specialty, clinic, time constraints, or specific professionals.
 *
 * Policy Evaluation:
 * - Multiple policies can exist for a single patient
 * - Policies are evaluated by the PolicyEngine during document access requests
 * - Conflict resolution is based on priority (higher priority wins)
 * - Policy effect can be PERMIT (allow access) or DENY (block access)
 *
 * Policy Types:
 * - DOCUMENT_TYPE: Allow/deny by document type (e.g., only lab results)
 * - SPECIALTY: Allow/deny by professional specialty (e.g., only cardiologists)
 * - TIME_BASED: Allow/deny by time/day (e.g., business hours only)
 * - CLINIC: Allow/deny by clinic (e.g., only my primary care clinic)
 * - PROFESSIONAL: Allow/deny specific professionals (whitelist/blacklist)
 * - EMERGENCY_OVERRIDE: Emergency access (requires heavy audit logging)
 *
 * Database Schema: policies.access_policies
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@Entity
@Table(name = "access_policies", schema = "policies", indexes = {
    @Index(name = "idx_access_policies_patient_ci", columnList = "patient_ci"),
    @Index(name = "idx_access_policies_policy_type", columnList = "policy_type"),
    @Index(name = "idx_access_policies_policy_effect", columnList = "policy_effect"),
    @Index(name = "idx_access_policies_priority", columnList = "priority"),
    @Index(name = "idx_access_policies_patient_type_effect", columnList = "patient_ci, policy_type, policy_effect")
})
public class AccessPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Policy Type Enumeration
     */
    public enum PolicyType {
        /**
         * Policy based on document type (e.g., LAB_RESULT, IMAGING, CLINICAL_NOTE)
         */
        DOCUMENT_TYPE,

        /**
         * Policy based on professional specialty (e.g., CARDIOLOGY, PEDIATRICS)
         */
        SPECIALTY,

        /**
         * Policy based on time constraints (e.g., business hours, specific days)
         */
        TIME_BASED,

        /**
         * Policy based on clinic (e.g., allow only specific clinics)
         */
        CLINIC,

        /**
         * Policy for specific professionals (whitelist or blacklist)
         */
        PROFESSIONAL,

        /**
         * Emergency override policy (bypass other restrictions, requires audit)
         */
        EMERGENCY_OVERRIDE
    }

    /**
     * Policy Effect Enumeration
     */
    public enum PolicyEffect {
        /**
         * Allow access (whitelist)
         */
        PERMIT,

        /**
         * Deny access (blacklist)
         */
        DENY
    }

    /**
     * Unique policy identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Patient CI (Cédula de Identidad)
     * References: inus.inus_users.ci
     */
    @Column(name = "patient_ci", nullable = false, length = 20)
    private String patientCi;

    /**
     * Policy type
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false, length = 50)
    private PolicyType policyType;

    /**
     * Policy configuration (stored as JSONB)
     * Structure varies by policy type
     *
     * Examples:
     * - DOCUMENT_TYPE: {"allowedTypes": ["LAB_RESULT", "IMAGING"]}
     * - SPECIALTY: {"allowedSpecialties": ["CARDIOLOGY", "GENERAL_MEDICINE"]}
     * - TIME_BASED: {"allowedDays": ["MONDAY", "FRIDAY"], "allowedHours": "09:00-17:00"}
     * - CLINIC: {"allowedClinics": ["clinic-001", "clinic-002"]}
     * - PROFESSIONAL: {"allowedProfessionals": ["prof-123", "prof-456"]}
     */
    @Column(name = "policy_config", nullable = false, columnDefinition = "jsonb")
    private String policyConfig;

    /**
     * Policy effect (PERMIT or DENY)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "policy_effect", nullable = false, length = 10)
    private PolicyEffect policyEffect;

    /**
     * Policy validity start date (optional)
     * If null, policy is valid from creation
     */
    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    /**
     * Policy validity end date (optional)
     * If null, policy is valid indefinitely
     */
    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    /**
     * Priority for conflict resolution
     * Higher priority wins when policies conflict
     * Default: 0
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    /**
     * Timestamp of policy creation
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of last policy update
     * Auto-updated by database trigger
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Default constructor
     */
    public AccessPolicy() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor with required fields
     *
     * @param patientCi Patient CI
     * @param policyType Policy type
     * @param policyConfig Policy configuration (JSON)
     * @param policyEffect Policy effect
     */
    public AccessPolicy(String patientCi, PolicyType policyType, String policyConfig, PolicyEffect policyEffect) {
        this();
        this.patientCi = patientCi;
        this.policyType = policyType;
        this.policyConfig = policyConfig;
        this.policyEffect = policyEffect;
    }

    /**
     * Checks if this policy is currently valid based on validity dates
     *
     * @return true if policy is valid, false otherwise
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();

        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }

        if (validUntil != null && now.isAfter(validUntil)) {
            return false;
        }

        return true;
    }

    /**
     * Pre-persist callback
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Pre-update callback
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientCi() {
        return patientCi;
    }

    public void setPatientCi(String patientCi) {
        this.patientCi = patientCi;
    }

    public PolicyType getPolicyType() {
        return policyType;
    }

    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
    }

    public String getPolicyConfig() {
        return policyConfig;
    }

    public void setPolicyConfig(String policyConfig) {
        this.policyConfig = policyConfig;
    }

    public PolicyEffect getPolicyEffect() {
        return policyEffect;
    }

    public void setPolicyEffect(PolicyEffect policyEffect) {
        this.policyEffect = policyEffect;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Equals, HashCode, and ToString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessPolicy that = (AccessPolicy) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(patientCi, that.patientCi) &&
               policyType == that.policyType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, patientCi, policyType);
    }

    @Override
    public String toString() {
        return "AccessPolicy{" +
               "id=" + id +
               ", patientCi='" + patientCi + '\'' +
               ", policyType=" + policyType +
               ", policyEffect=" + policyEffect +
               ", priority=" + priority +
               ", validFrom=" + validFrom +
               ", validUntil=" + validUntil +
               ", createdAt=" + createdAt +
               '}';
    }
}
