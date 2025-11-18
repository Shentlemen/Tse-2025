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

    MG(1, "MG", "Medicina General", "Atención médica general y preventiva"),
    CAR(2, "CAR", "Cardiología", "Especialidad en enfermedades del corazón"),
    DER(3, "DER", "Dermatología", "Especialidad en enfermedades de la piel"),
    GIN(4, "GIN", "Ginecología", "Especialidad en salud femenina"),
    PED(5, "PED", "Pediatría", "Especialidad en medicina infantil"),
    OFT(6, "OFT", "Oftalmología", "Especialidad en salud ocular"),
    ORL(7, "ORL", "Otorrinolaringología", "Especialidad en oído, nariz y garganta"),
    PSI(8, "PSI", "Psicología", "Especialidad en salud mental"),
    TRAUMATOLOGIA(9, "TRA", "Traumatología", "Especialidad en sistema musculoesquelético"),
    URO(10, "URO", "Urología", "Especialidad en sistema urinario y genital masculino"),
    NEURO(16, "NEURO", "Neurología", "Especialidad médica del sistema nervioso"),
    ELECTRO(30, "ELECTRO", "Electrofisiología", "Estudios eléctricos del corazón"),
    NEURO_CIR(31, "NEURO_CIR", "Neurocirugía", "Cirugía del sistema nervioso"),
    NEURO_PSI(32, "NEURO_PSI", "Neuropsicología", "Evaluación neuropsicológica"),
    EPILEP(33, "EPILEP", "Epileptología", "Especialidad en epilepsia"),
    ESP_PRU(34, "EspPru", "Especialidad de prueba", "Especialidad test"),
    ESP_PRU_2(35, "EspPru2", "Especialidad de prueba 2", "Descripcion de especialidad 2"),
    CAR_INTO(29, "CAR_INTO", "Cardiología Intervencionista", "Procedimientos invasivos del corazón"),
    MF(37, "MF", "Medicina Familiar", "Atención integral a la familia"),
    PSQ(40, "PSQ", "Psiquiatría", "Especialidad en salud mental y trastornos psiquiátricos"),
    PSIC(41, "PSIC", "Psicología Clínica", "Especialidad en salud mental y terapia psicológica"),
    GYN(43, "GYN", "Ginecología y Obstetricia", "Especialidad en salud femenina y embarazo"),
    GER(45, "GER", "Geriatría", "Especialidad en salud del adulto mayor"),
    CG(46, "CG", "Cirugía General", "Cirugía de enfermedades generales"),
    CP(47, "CP", "Cirugía Plástica", "Cirugía reconstructiva y estética"),
    CCV(49, "CCV", "Cirugía Cardiovascular", "Cirugía del corazón y vasos sanguíneos"),
    RAD(53, "RAD", "Radiología", "Diagnóstico por imágenes"),
    AP(54, "AP", "Anatomía Patológica", "Diagnóstico mediante estudio de tejidos"),
    MN(55, "MN", "Medicina Nuclear", "Diagnóstico y tratamiento con isótopos radioactivos"),
    LAB(56, "LAB", "Laboratorio Clínico", "Análisis clínicos y diagnósticos"),
    GAST(57, "GAST", "Gastroenterología", "Especialidad en sistema digestivo"),
    NEUM(58, "NEUM", "Neumología", "Especialidad en enfermedades respiratorias"),
    NEF(59, "NEF", "Nefrología", "Especialidad en riñones y enfermedades renales"),
    END(60, "END", "Endocrinología", "Especialidad en glándulas y hormonas"),
    REU(61, "REU", "Reumatología", "Especialidad en enfermedades reumáticas"),
    HEM(62, "HEM", "Hematología", "Especialidad en sangre y órganos hematopoyéticos"),
    ONC(63, "ONC", "Oncología", "Especialidad en tratamiento del cáncer"),
    TRAUMATOLOGIA_Y_ORTOPEDIA(64, "TRA", "Traumatología y Ortopedia", "Especialidad en sistema musculoesquelético"),
    CPED(65, "CPED", "Cirugía Pediátrica", "Cirugía en pacientes pediátricos"),
    CT(66, "CT", "Cirugía de Tórax", "Cirugía del tórax y pulmones"),
    EMER(67, "EMER", "Medicina de Emergencias", "Atención médica de urgencias"),
    MI(68, "MI", "Medicina Intensiva", "Cuidado crítico y terapia intensiva"),
    MT(69, "MT", "Medicina del Trabajo", "Salud ocupacional y medicina laboral"),
    MD(70, "MD", "Medicina Deportiva", "Medicina del deporte y actividad física"),
    ALER(71, "ALER", "Alergología", "Especialidad en alergias e inmunología"),
    INF(72, "INF", "Infectología", "Especialidad en enfermedades infecciosas"),
    FIS(73, "FIS", "Medicina Física y Rehabilitación", "Rehabilitación y fisiatría"),
    ANE(74, "ANE", "Anestesiología", "Anestesia y cuidados perioperatorios");

    private final Integer id;
    private final String code;
    private final String displayName;
    private final String description;

    MedicalSpecialty(Integer id, String code, String displayName, String description) {
        this.id = id;
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    /**
     * Human-readable name (spanish)
     */
    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Find by code (exact match, case-insensitive).
     */
    public static MedicalSpecialty fromCode(String code) {
        if (code == null) return null;
        for (MedicalSpecialty s : values()) {
            if (s.code.equalsIgnoreCase(code)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Find by display name or enum name (case-insensitive).
     */
    public static MedicalSpecialty fromName(String name) {
        if (name == null) return null;
        for (MedicalSpecialty s : values()) {
            if (s.displayName.equalsIgnoreCase(name) || s.name().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }
}

