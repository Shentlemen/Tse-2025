package uy.gub.hcen.policy.dto;

import uy.gub.hcen.policy.entity.AccessPolicy.PolicyType;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyEffect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Policy Template DTO
 * <p>
 * Provides metadata and schema information for available policy types.
 * Used by client applications to dynamically generate policy configuration UIs.
 * <p>
 * Example Response:
 * <pre>
 * {
 *   "policyType": "DOCUMENT_TYPE",
 *   "displayName": "Política por Tipo de Documento",
 *   "description": "Controlar acceso por tipo de documento clínico",
 *   "availableEffects": ["PERMIT", "DENY"],
 *   "configurationSchema": {
 *     "type": "object",
 *     "properties": {
 *       "allowedTypes": {
 *         "type": "array",
 *         "items": {"type": "string", "enum": ["LAB_RESULT", "IMAGING", "PRESCRIPTION"]}
 *       }
 *     }
 *   },
 *   "exampleConfiguration": "{\"allowedTypes\": [\"LAB_RESULT\", \"IMAGING\"]}"
 * }
 * </pre>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-04
 */
public class PolicyTemplateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private PolicyType policyType;
    private String displayName;
    private String description;
    private List<PolicyEffect> availableEffects;
    private Map<String, Object> configurationSchema;
    private String exampleConfiguration;
    private int defaultPriority;

    // ================================================================
    // Constructors
    // ================================================================

    public PolicyTemplateDTO() {
        this.availableEffects = new ArrayList<>();
        this.configurationSchema = new HashMap<>();
        this.defaultPriority = 0;
    }

    public PolicyTemplateDTO(PolicyType policyType, String displayName, String description,
                             List<PolicyEffect> availableEffects, Map<String, Object> configurationSchema,
                             String exampleConfiguration, int defaultPriority) {
        this.policyType = policyType;
        this.displayName = displayName;
        this.description = description;
        this.availableEffects = availableEffects;
        this.configurationSchema = configurationSchema;
        this.exampleConfiguration = exampleConfiguration;
        this.defaultPriority = defaultPriority;
    }

    // ================================================================
    // Factory Methods - Template Builders
    // ================================================================

    /**
     * Creates all available policy templates.
     *
     * @return List of all policy templates
     */
    public static List<PolicyTemplateDTO> getAllTemplates() {
        List<PolicyTemplateDTO> templates = new ArrayList<>();

        templates.add(createDocumentTypeTemplate());
        templates.add(createProfessionalTemplate());
        templates.add(createSpecialtyTemplate());
        templates.add(createClinicTemplate());
        templates.add(createTimeBasedTemplate());
        templates.add(createEmergencyOverrideTemplate());

        return templates;
    }

    /**
     * DOCUMENT_TYPE policy template.
     */
    private static PolicyTemplateDTO createDocumentTypeTemplate() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> allowedTypes = new HashMap<>();
        allowedTypes.put("type", "array");
        allowedTypes.put("description", "Lista de tipos de documentos permitidos");

        Map<String, Object> items = new HashMap<>();
        items.put("type", "string");
        items.put("enum", List.of("LAB_RESULT", "IMAGING", "PRESCRIPTION", "CLINICAL_NOTE",
                "DISCHARGE_SUMMARY", "VACCINATION_RECORD", "MENTAL_HEALTH", "GENETIC_TEST"));

        allowedTypes.put("items", items);
        properties.put("allowedTypes", allowedTypes);

        schema.put("properties", properties);

        return new PolicyTemplateDTO(
                PolicyType.DOCUMENT_TYPE,
                "Política por Tipo de Documento",
                "Controlar acceso según el tipo de documento clínico (ej: solo resultados de laboratorio)",
                List.of(PolicyEffect.PERMIT, PolicyEffect.DENY),
                schema,
                "{\"allowedTypes\": [\"LAB_RESULT\", \"IMAGING\"]}",
                10
        );
    }

    /**
     * PROFESSIONAL policy template (whitelist/blacklist).
     */
    private static PolicyTemplateDTO createProfessionalTemplate() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> allowedProfessionals = new HashMap<>();
        allowedProfessionals.put("type", "array");
        allowedProfessionals.put("description", "IDs de profesionales permitidos (lista blanca)");

        Map<String, Object> items = new HashMap<>();
        items.put("type", "string");

        allowedProfessionals.put("items", items);
        properties.put("allowedProfessionals", allowedProfessionals);

        Map<String, Object> deniedProfessionals = new HashMap<>();
        deniedProfessionals.put("type", "array");
        deniedProfessionals.put("description", "IDs de profesionales denegados (lista negra)");
        deniedProfessionals.put("items", items);
        properties.put("deniedProfessionals", deniedProfessionals);

        schema.put("properties", properties);

        return new PolicyTemplateDTO(
                PolicyType.PROFESSIONAL,
                "Política por Profesional Específico",
                "Permitir o denegar acceso a profesionales específicos por su ID",
                List.of(PolicyEffect.PERMIT, PolicyEffect.DENY),
                schema,
                "{\"allowedProfessionals\": [\"prof-123\", \"prof-456\"]}",
                20
        );
    }

    /**
     * SPECIALTY policy template.
     */
    private static PolicyTemplateDTO createSpecialtyTemplate() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> allowedSpecialties = new HashMap<>();
        allowedSpecialties.put("type", "array");
        allowedSpecialties.put("description", "Especialidades médicas permitidas");

        Map<String, Object> items = new HashMap<>();
        items.put("type", "string");
        items.put("enum", List.of("CARDIOLOGY", "GENERAL_MEDICINE", "PEDIATRICS", "PSYCHIATRY",
                "SURGERY", "GYNECOLOGY", "DERMATOLOGY", "NEUROLOGY", "ONCOLOGY", "RADIOLOGY"));

        allowedSpecialties.put("items", items);
        properties.put("allowedSpecialties", allowedSpecialties);

        schema.put("properties", properties);

        return new PolicyTemplateDTO(
                PolicyType.SPECIALTY,
                "Política por Especialidad Médica",
                "Controlar acceso según la especialidad del profesional de salud",
                List.of(PolicyEffect.PERMIT, PolicyEffect.DENY),
                schema,
                "{\"allowedSpecialties\": [\"CARDIOLOGY\", \"GENERAL_MEDICINE\"]}",
                15
        );
    }

    /**
     * CLINIC policy template.
     */
    private static PolicyTemplateDTO createClinicTemplate() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> allowedClinics = new HashMap<>();
        allowedClinics.put("type", "array");
        allowedClinics.put("description", "IDs de clínicas/centros de salud permitidos");

        Map<String, Object> items = new HashMap<>();
        items.put("type", "string");

        allowedClinics.put("items", items);
        properties.put("allowedClinics", allowedClinics);

        schema.put("properties", properties);

        return new PolicyTemplateDTO(
                PolicyType.CLINIC,
                "Política por Centro de Salud",
                "Permitir acceso solo desde clínicas o centros de salud específicos",
                List.of(PolicyEffect.PERMIT, PolicyEffect.DENY),
                schema,
                "{\"allowedClinics\": [\"clinic-001\", \"clinic-002\"]}",
                12
        );
    }

    /**
     * TIME_BASED policy template.
     */
    private static PolicyTemplateDTO createTimeBasedTemplate() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> allowedDays = new HashMap<>();
        allowedDays.put("type", "array");
        allowedDays.put("description", "Días de la semana permitidos");

        Map<String, Object> dayItems = new HashMap<>();
        dayItems.put("type", "string");
        dayItems.put("enum", List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"));

        allowedDays.put("items", dayItems);
        properties.put("allowedDays", allowedDays);

        Map<String, Object> allowedHours = new HashMap<>();
        allowedHours.put("type", "string");
        allowedHours.put("description", "Rango de horario permitido (formato HH:MM-HH:MM)");
        allowedHours.put("pattern", "^\\d{2}:\\d{2}-\\d{2}:\\d{2}$");
        properties.put("allowedHours", allowedHours);

        schema.put("properties", properties);

        return new PolicyTemplateDTO(
                PolicyType.TIME_BASED,
                "Política por Horario",
                "Restringir acceso a días y horarios específicos (ej: solo horario de oficina)",
                List.of(PolicyEffect.PERMIT, PolicyEffect.DENY),
                schema,
                "{\"allowedDays\": [\"MONDAY\", \"TUESDAY\", \"WEDNESDAY\", \"THURSDAY\", \"FRIDAY\"], \"allowedHours\": \"09:00-17:00\"}",
                5
        );
    }

    /**
     * EMERGENCY_OVERRIDE policy template.
     */
    private static PolicyTemplateDTO createEmergencyOverrideTemplate() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> enabled = new HashMap<>();
        enabled.put("type", "boolean");
        enabled.put("description", "Permitir acceso de emergencia (con auditoría estricta)");
        properties.put("enabled", enabled);

        Map<String, Object> requiresAudit = new HashMap<>();
        requiresAudit.put("type", "boolean");
        requiresAudit.put("description", "Requiere registro de auditoría detallado");
        properties.put("requiresAudit", requiresAudit);

        schema.put("properties", properties);

        return new PolicyTemplateDTO(
                PolicyType.EMERGENCY_OVERRIDE,
                "Política de Emergencia",
                "Permitir acceso de emergencia con auditoría estricta (romper el vidrio)",
                List.of(PolicyEffect.PERMIT),
                schema,
                "{\"enabled\": true, \"requiresAudit\": true}",
                100
        );
    }

    // ================================================================
    // Getters and Setters
    // ================================================================

    public PolicyType getPolicyType() {
        return policyType;
    }

    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PolicyEffect> getAvailableEffects() {
        return availableEffects;
    }

    public void setAvailableEffects(List<PolicyEffect> availableEffects) {
        this.availableEffects = availableEffects;
    }

    public Map<String, Object> getConfigurationSchema() {
        return configurationSchema;
    }

    public void setConfigurationSchema(Map<String, Object> configurationSchema) {
        this.configurationSchema = configurationSchema;
    }

    public String getExampleConfiguration() {
        return exampleConfiguration;
    }

    public void setExampleConfiguration(String exampleConfiguration) {
        this.exampleConfiguration = exampleConfiguration;
    }

    public int getDefaultPriority() {
        return defaultPriority;
    }

    public void setDefaultPriority(int defaultPriority) {
        this.defaultPriority = defaultPriority;
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public String toString() {
        return "PolicyTemplateDTO{" +
                "policyType=" + policyType +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", defaultPriority=" + defaultPriority +
                '}';
    }
}
