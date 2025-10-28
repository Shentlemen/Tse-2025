package uy.gub.clinic.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.entity.Professional;
import uy.gub.clinic.entity.Patient;
import uy.gub.clinic.entity.Specialty;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de clínicas con datos hardcodeados para desarrollo
 */
@ApplicationScoped
public class ClinicService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClinicService.class);
    
    @PersistenceContext
    private EntityManager entityManager;
    
    // Datos hardcodeados para desarrollo
    private final Map<Long, Clinic> hardcodedClinics = new HashMap<>();
    private final Map<Long, Professional> hardcodedProfessionals = new HashMap<>();
    private final Map<Long, Patient> hardcodedPatients = new HashMap<>();
    private final Map<Long, Specialty> hardcodedSpecialties = new HashMap<>();
    
    public ClinicService() {
        initializeHardcodedData();
    }
    
    /**
     * Inicializa datos hardcodeados para desarrollo
     */
    private void initializeHardcodedData() {
        logger.info("Inicializando datos hardcodeados para desarrollo...");
        
        // Especialidades hardcodeadas
        Specialty cardiologia = new Specialty("Cardiología", "CARD", "Especialidad médica que se ocupa del corazón");
        cardiologia.setId(1L);
        
        Specialty neurologia = new Specialty("Neurología", "NEURO", "Especialidad médica del sistema nervioso");
        neurologia.setId(2L);
        
        Specialty pediatria = new Specialty("Pediatría", "PED", "Medicina especializada en niños");
        pediatria.setId(3L);
        
        Specialty traumatologia = new Specialty("Traumatología", "TRAUM", "Especialidad en huesos y articulaciones");
        traumatologia.setId(4L);
        
        hardcodedSpecialties.put(1L, cardiologia);
        hardcodedSpecialties.put(2L, neurologia);
        hardcodedSpecialties.put(3L, pediatria);
        hardcodedSpecialties.put(4L, traumatologia);
        
        // Clínicas hardcodeadas
        Clinic clinic1 = new Clinic("Clínica del Corazón", "CLIN001");
        clinic1.setId(1L);
        clinic1.setDescription("Clínica especializada en cardiología");
        clinic1.setAddress("Av. 18 de Julio 1234, Montevideo");
        clinic1.setPhone("+598 2 123-4567");
        clinic1.setEmail("info@clinicacorazon.com.uy");
        clinic1.setHcenEndpoint("http://localhost:8080/hcen/api");
        clinic1.setThemeColors("{\"primary\":\"#e74c3c\",\"secondary\":\"#c0392b\"}");
        
        Clinic clinic2 = new Clinic("Centro Neurológico", "CLIN002");
        clinic2.setId(2L);
        clinic2.setDescription("Centro especializado en neurología");
        clinic2.setAddress("Bvar. Artigas 5678, Montevideo");
        clinic2.setPhone("+598 2 987-6543");
        clinic2.setEmail("contacto@centroneurologico.com.uy");
        clinic2.setHcenEndpoint("http://localhost:8080/hcen/api");
        clinic2.setThemeColors("{\"primary\":\"#3498db\",\"secondary\":\"#2980b9\"}");
        
        hardcodedClinics.put(1L, clinic1);
        hardcodedClinics.put(2L, clinic2);
        
        // Profesionales hardcodeados
        Professional prof1 = new Professional("Dr. Juan", "Pérez", "jperez@clinicacorazon.com.uy", "LIC001");
        prof1.setId(1L);
        prof1.setClinic(clinic1);
        prof1.setSpecialty(cardiologia);
        prof1.setPhone("+598 99 111-2222");
        
        Professional prof2 = new Professional("Dra. María", "González", "mgonzalez@centroneurologico.com.uy", "LIC002");
        prof2.setId(2L);
        prof2.setClinic(clinic2);
        prof2.setSpecialty(neurologia);
        prof2.setPhone("+598 99 333-4444");
        
        Professional prof3 = new Professional("Dr. Carlos", "Rodríguez", "crodriguez@clinicacorazon.com.uy", "LIC003");
        prof3.setId(3L);
        prof3.setClinic(clinic1);
        prof3.setSpecialty(pediatria);
        prof3.setPhone("+598 99 555-6666");
        
        hardcodedProfessionals.put(1L, prof1);
        hardcodedProfessionals.put(2L, prof2);
        hardcodedProfessionals.put(3L, prof3);
        
        // Pacientes hardcodeados
        Patient patient1 = new Patient("Ana", "Silva");
        patient1.setId(1L);
        patient1.setClinic(clinic1);
        patient1.setDocumentNumber("12345678");
        patient1.setInusId("INUS001");
        patient1.setBirthDate(java.time.LocalDate.of(1985, 5, 15));
        patient1.setGender("F");
        patient1.setPhone("+598 99 777-8888");
        patient1.setEmail("ana.silva@email.com");
        patient1.setAddress("Av. Italia 3456, Montevideo");
        
        Patient patient2 = new Patient("Roberto", "Martínez");
        patient2.setId(2L);
        patient2.setClinic(clinic2);
        patient2.setDocumentNumber("87654321");
        patient2.setInusId("INUS002");
        patient2.setBirthDate(java.time.LocalDate.of(1978, 12, 3));
        patient2.setGender("M");
        patient2.setPhone("+598 99 999-0000");
        patient2.setEmail("roberto.martinez@email.com");
        patient2.setAddress("Pocitos 789, Montevideo");
        
        Patient patient3 = new Patient("Lucía", "Fernández");
        patient3.setId(3L);
        patient3.setClinic(clinic1);
        patient3.setDocumentNumber("11223344");
        patient3.setInusId("INUS003");
        patient3.setBirthDate(java.time.LocalDate.of(1992, 8, 22));
        patient3.setGender("F");
        patient3.setPhone("+598 99 111-3333");
        patient3.setEmail("lucia.fernandez@email.com");
        patient3.setAddress("Carrasco 456, Montevideo");
        
        hardcodedPatients.put(1L, patient1);
        hardcodedPatients.put(2L, patient2);
        hardcodedPatients.put(3L, patient3);
        
        logger.info("Datos hardcodeados inicializados: {} clínicas, {} profesionales, {} pacientes, {} especialidades",
                hardcodedClinics.size(), hardcodedProfessionals.size(), hardcodedPatients.size(), hardcodedSpecialties.size());
    }
    
    // Métodos para clínicas
    public List<Clinic> getAllClinics() {
        try {
            TypedQuery<Clinic> query = entityManager.createQuery(
                "SELECT c FROM Clinic c WHERE c.active = true ORDER BY c.name", 
                Clinic.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al obtener clínicas de la BD, usando datos hardcodeados", e);
            return new ArrayList<>(hardcodedClinics.values());
        }
    }
    
    public Optional<Clinic> getClinicById(Long id) {
        try {
            Clinic clinic = entityManager.find(Clinic.class, id);
            return Optional.ofNullable(clinic);
        } catch (Exception e) {
            logger.error("Error al obtener clínica {} de la BD, usando datos hardcodeados", id, e);
            return Optional.ofNullable(hardcodedClinics.get(id));
        }
    }
    
    public Optional<Clinic> getClinicByCode(String code) {
        try {
            TypedQuery<Clinic> query = entityManager.createQuery(
                "SELECT c FROM Clinic c WHERE c.code = :code AND c.active = true", 
                Clinic.class);
            query.setParameter("code", code);
            return query.getResultList().stream().findFirst();
        } catch (Exception e) {
            logger.error("Error al obtener clínica {} de la BD, usando datos hardcodeados", code, e);
            return hardcodedClinics.values().stream()
                    .filter(clinic -> clinic.getCode().equals(code))
                    .findFirst();
        }
    }
    
    @Transactional
    public Clinic createClinic(Clinic clinic) {
        Long newId = hardcodedClinics.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1;
        clinic.setId(newId);
        hardcodedClinics.put(newId, clinic);
        logger.info("Clínica creada: {}", clinic);
        return clinic;
    }
    
    @Transactional
    public Clinic updateClinic(Clinic clinic) {
        if (hardcodedClinics.containsKey(clinic.getId())) {
            hardcodedClinics.put(clinic.getId(), clinic);
            logger.info("Clínica actualizada: {}", clinic);
            return clinic;
        }
        throw new IllegalArgumentException("Clínica no encontrada con ID: " + clinic.getId());
    }
    
    @Transactional
    public void deleteClinic(Long id) {
        if (hardcodedClinics.containsKey(id)) {
            hardcodedClinics.remove(id);
            logger.info("Clínica eliminada con ID: {}", id);
        } else {
            throw new IllegalArgumentException("Clínica no encontrada con ID: " + id);
        }
    }
    
    // Métodos para profesionales
    public List<Professional> getAllProfessionals() {
        return new ArrayList<>(hardcodedProfessionals.values());
    }
    
    public List<Professional> getProfessionalsByClinic(Long clinicId) {
        return hardcodedProfessionals.values().stream()
                .filter(prof -> prof.getClinic().getId().equals(clinicId))
                .collect(Collectors.toList());
    }
    
    public Optional<Professional> getProfessionalById(Long id) {
        return Optional.ofNullable(hardcodedProfessionals.get(id));
    }
    
    @Transactional
    public Professional createProfessional(Professional professional) {
        Long newId = hardcodedProfessionals.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1;
        professional.setId(newId);
        hardcodedProfessionals.put(newId, professional);
        logger.info("Profesional creado: {}", professional);
        return professional;
    }
    
    @Transactional
    public Professional updateProfessional(Professional professional) {
        if (hardcodedProfessionals.containsKey(professional.getId())) {
            hardcodedProfessionals.put(professional.getId(), professional);
            logger.info("Profesional actualizado: {}", professional);
            return professional;
        }
        throw new IllegalArgumentException("Profesional no encontrado con ID: " + professional.getId());
    }
    
    @Transactional
    public void deleteProfessional(Long id) {
        if (hardcodedProfessionals.containsKey(id)) {
            hardcodedProfessionals.remove(id);
            logger.info("Profesional eliminado con ID: {}", id);
        } else {
            throw new IllegalArgumentException("Profesional no encontrado con ID: " + id);
        }
    }
    
    // Métodos para pacientes
    public List<Patient> getAllPatients() {
        return new ArrayList<>(hardcodedPatients.values());
    }
    
    public List<Patient> getPatientsByClinic(Long clinicId) {
        return hardcodedPatients.values().stream()
                .filter(patient -> patient.getClinic().getId().equals(clinicId))
                .collect(Collectors.toList());
    }
    
    public Optional<Patient> getPatientById(Long id) {
        return Optional.ofNullable(hardcodedPatients.get(id));
    }
    
    public Optional<Patient> getPatientByInusId(String inusId) {
        return hardcodedPatients.values().stream()
                .filter(patient -> inusId.equals(patient.getInusId()))
                .findFirst();
    }
    
    public List<Patient> searchPatientsByName(String name) {
        String searchTerm = name.toLowerCase();
        return hardcodedPatients.values().stream()
                .filter(patient -> patient.getName().toLowerCase().contains(searchTerm) ||
                                 (patient.getLastName() != null && patient.getLastName().toLowerCase().contains(searchTerm)))
                .collect(Collectors.toList());
    }
    
    @Transactional
    public Patient createPatient(Patient patient) {
        Long newId = hardcodedPatients.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1;
        patient.setId(newId);
        hardcodedPatients.put(newId, patient);
        logger.info("Paciente creado: {}", patient);
        return patient;
    }
    
    @Transactional
    public Patient updatePatient(Patient patient) {
        if (hardcodedPatients.containsKey(patient.getId())) {
            hardcodedPatients.put(patient.getId(), patient);
            logger.info("Paciente actualizado: {}", patient);
            return patient;
        }
        throw new IllegalArgumentException("Paciente no encontrado con ID: " + patient.getId());
    }
    
    @Transactional
    public void deletePatient(Long id) {
        if (hardcodedPatients.containsKey(id)) {
            hardcodedPatients.remove(id);
            logger.info("Paciente eliminado con ID: {}", id);
        } else {
            throw new IllegalArgumentException("Paciente no encontrado con ID: " + id);
        }
    }
    
    // Métodos para especialidades
    public List<Specialty> getAllSpecialties() {
        return new ArrayList<>(hardcodedSpecialties.values());
    }
    
    public Optional<Specialty> getSpecialtyById(Long id) {
        return Optional.ofNullable(hardcodedSpecialties.get(id));
    }
    
    @Transactional
    public Specialty createSpecialty(Specialty specialty) {
        Long newId = hardcodedSpecialties.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1;
        specialty.setId(newId);
        hardcodedSpecialties.put(newId, specialty);
        logger.info("Especialidad creada: {}", specialty);
        return specialty;
    }
    
    @Transactional
    public Specialty updateSpecialty(Specialty specialty) {
        if (hardcodedSpecialties.containsKey(specialty.getId())) {
            hardcodedSpecialties.put(specialty.getId(), specialty);
            logger.info("Especialidad actualizada: {}", specialty);
            return specialty;
        }
        throw new IllegalArgumentException("Especialidad no encontrada con ID: " + specialty.getId());
    }
    
    @Transactional
    public void deleteSpecialty(Long id) {
        if (hardcodedSpecialties.containsKey(id)) {
            hardcodedSpecialties.remove(id);
            logger.info("Especialidad eliminada con ID: {}", id);
        } else {
            throw new IllegalArgumentException("Especialidad no encontrada con ID: " + id);
        }
    }
}
