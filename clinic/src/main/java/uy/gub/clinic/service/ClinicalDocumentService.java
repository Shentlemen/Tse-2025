package uy.gub.clinic.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.ClinicalDocument;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.entity.Patient;
import uy.gub.clinic.entity.Professional;
import uy.gub.clinic.entity.Specialty;
import uy.gub.clinic.integration.HcenJmsService;
import uy.gub.clinic.service.FhirMappingService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de documentos clínicos
 */
@Stateless
public class ClinicalDocumentService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClinicalDocumentService.class);
    
    @PersistenceContext(unitName = "clinicPU")
    private EntityManager entityManager;
    
    @Inject
    private HcenJmsService hcenJmsService;

    @Inject
    private FhirMappingService fhirMappingService;
    
    /**
     * Obtener todos los documentos
     */
    public List<ClinicalDocument> findAll() {
        try {
            TypedQuery<ClinicalDocument> query = entityManager.createQuery(
                "SELECT d FROM ClinicalDocument d ORDER BY d.createdAt DESC", 
                ClinicalDocument.class);
            List<ClinicalDocument> documents = query.getResultList();
            
            // Cargar relaciones lazy
            for (ClinicalDocument doc : documents) {
                if (doc.getPatient() != null) doc.getPatient().getName();
                if (doc.getProfessional() != null) doc.getProfessional().getName();
                if (doc.getClinic() != null) doc.getClinic().getName();
                if (doc.getSpecialty() != null) doc.getSpecialty().getName();
            }
            
            return documents;
        } catch (Exception e) {
            logger.error("Error al obtener todos los documentos", e);
            return List.of();
        }
    }
    
    /**
     * Obtener todos los documentos de una clínica
     */
    public List<ClinicalDocument> findByClinic(String clinicId) {
        try {
            TypedQuery<ClinicalDocument> query = entityManager.createNamedQuery(
                "ClinicalDocument.findByClinic", ClinicalDocument.class);
            query.setParameter("clinicId", clinicId);
            List<ClinicalDocument> documents = query.getResultList();
            
            // Cargar relaciones lazy
            for (ClinicalDocument doc : documents) {
                if (doc.getPatient() != null) doc.getPatient().getName();
                if (doc.getProfessional() != null) doc.getProfessional().getName();
                if (doc.getClinic() != null) doc.getClinic().getName();
                if (doc.getSpecialty() != null) doc.getSpecialty().getName();
            }
            
            return documents;
        } catch (Exception e) {
            logger.error("Error al obtener documentos por clínica: {}", clinicId, e);
            return List.of();
        }
    }
    
    /**
     * Obtener documentos por clínica y especialidad
     */
    public List<ClinicalDocument> findByClinicAndSpecialty(String clinicId, Long specialtyId) {
        try {
            TypedQuery<ClinicalDocument> query = entityManager.createNamedQuery(
                "ClinicalDocument.findByClinicAndSpecialty", ClinicalDocument.class);
            query.setParameter("clinicId", clinicId);
            query.setParameter("specialtyId", specialtyId);
            List<ClinicalDocument> documents = query.getResultList();
            
            // Cargar relaciones lazy
            for (ClinicalDocument doc : documents) {
                if (doc.getPatient() != null) doc.getPatient().getName();
                if (doc.getProfessional() != null) doc.getProfessional().getName();
                if (doc.getClinic() != null) doc.getClinic().getName();
                if (doc.getSpecialty() != null) doc.getSpecialty().getName();
            }
            
            return documents;
        } catch (Exception e) {
            logger.error("Error al obtener documentos por clínica y especialidad: {} - {}", clinicId, specialtyId, e);
            return List.of();
        }
    }
    
    /**
     * Obtener documentos por paciente
     */
    public List<ClinicalDocument> findByPatient(Long patientId) {
        try {
            TypedQuery<ClinicalDocument> query = entityManager.createNamedQuery(
                "ClinicalDocument.findByPatient", ClinicalDocument.class);
            query.setParameter("patientId", patientId);
            List<ClinicalDocument> documents = query.getResultList();
            
            // Cargar relaciones lazy para evitar LazyInitializationException
            for (ClinicalDocument doc : documents) {
                if (doc.getPatient() != null) {
                    doc.getPatient().getName(); // Cargar Patient
                    if (doc.getPatient().getClinic() != null) {
                        doc.getPatient().getClinic().getName(); // Cargar Clinic del Patient
                    }
                }
                if (doc.getProfessional() != null) {
                    doc.getProfessional().getName(); // Cargar Professional
                    if (doc.getProfessional().getClinic() != null) {
                        doc.getProfessional().getClinic().getName(); // Cargar Clinic del Professional
                    }
                }
                if (doc.getClinic() != null) doc.getClinic().getName();
                if (doc.getSpecialty() != null) doc.getSpecialty().getName();
            }
            
            return documents;
        } catch (Exception e) {
            logger.error("Error al obtener documentos por paciente: {}", patientId, e);
            return List.of();
        }
    }
    
    /**
     * Obtener documentos por profesional
     */
    public List<ClinicalDocument> findByProfessional(Long professionalId) {
        try {
            TypedQuery<ClinicalDocument> query = entityManager.createNamedQuery(
                "ClinicalDocument.findByProfessional", ClinicalDocument.class);
            query.setParameter("professionalId", professionalId);
            
            List<ClinicalDocument> results = query.getResultList();
            
            // Cargar relaciones lazy
            for (ClinicalDocument doc : results) {
                if (doc.getPatient() != null) {
                    doc.getPatient().getFullName(); // Acceder para cargar
                }
                if (doc.getProfessional() != null) {
                    doc.getProfessional().getFullName(); // Acceder para cargar
                    if (doc.getProfessional().getSpecialty() != null) {
                        doc.getProfessional().getSpecialty().getName(); // Acceder para cargar
                    }
                }
                if (doc.getSpecialty() != null) {
                    doc.getSpecialty().getName(); // Acceder para cargar
                }
                if (doc.getClinic() != null) {
                    doc.getClinic().getName(); // Acceder para cargar
                }
            }
            
            return results;
        } catch (Exception e) {
            logger.error("Error al obtener documentos por profesional: {}", professionalId, e);
            return List.of();
        }
    }
    
    /**
     * Buscar documento por ID
     */
    public Optional<ClinicalDocument> findById(Long id) {
        try {
            ClinicalDocument document = entityManager.find(ClinicalDocument.class, id);
            if (document != null) {
                // Cargar relaciones lazy
                if (document.getPatient() != null) document.getPatient().getName();
                if (document.getProfessional() != null) document.getProfessional().getName();
                if (document.getClinic() != null) document.getClinic().getName();
                if (document.getSpecialty() != null) document.getSpecialty().getName();
            }
            return Optional.ofNullable(document);
        } catch (Exception e) {
            logger.error("Error al buscar documento por ID: {}", id, e);
            return Optional.empty();
        }
    }
    
    /**
     * Crear nuevo documento clínico
     */
    @Transactional
    public ClinicalDocument createDocument(ClinicalDocument document) {
        try {
            // Validar relaciones
            if (document.getPatient() == null || document.getPatient().getId() == null) {
                throw new IllegalArgumentException("El paciente es requerido");
            }
            if (document.getProfessional() == null || document.getProfessional().getId() == null) {
                throw new IllegalArgumentException("El profesional es requerido");
            }
            if (document.getClinic() == null || document.getClinic().getId() == null) {
                throw new IllegalArgumentException("La clínica es requerida");
            }
            if (document.getSpecialty() == null || document.getSpecialty().getId() == null) {
                throw new IllegalArgumentException("La especialidad es requerida");
            }
            
            // Cargar entidades desde la base de datos
            Patient patient = entityManager.find(Patient.class, document.getPatient().getId());
            if (patient == null) {
                throw new IllegalArgumentException("Paciente no encontrado: " + document.getPatient().getId());
            }
            
            Professional professional = entityManager.find(Professional.class, document.getProfessional().getId());
            if (professional == null) {
                throw new IllegalArgumentException("Profesional no encontrado: " + document.getProfessional().getId());
            }
            
            Clinic clinic = entityManager.find(Clinic.class, document.getClinic().getId());
            if (clinic == null) {
                throw new IllegalArgumentException("Clínica no encontrada: " + document.getClinic().getId());
            }
            
            Specialty specialty = entityManager.find(Specialty.class, document.getSpecialty().getId());
            if (specialty == null) {
                throw new IllegalArgumentException("Especialidad no encontrada: " + document.getSpecialty().getId());
            }
            
            // Validar que el paciente pertenezca a la clínica
            if (!patient.getClinic().getId().equals(clinic.getId())) {
                throw new IllegalArgumentException("El paciente no pertenece a la clínica especificada");
            }
            
            // Validar que el profesional pertenezca a la clínica
            if (!professional.getClinic().getId().equals(clinic.getId())) {
                throw new IllegalArgumentException("El profesional no pertenece a la clínica especificada");
            }
            
            // Las especialidades ahora son globales, no es necesario validar que pertenezcan a la clínica
            // (se mantiene la validación de que la especialidad existe)
            
            // Establecer relaciones
            document.setPatient(patient);
            document.setProfessional(professional);
            document.setClinic(clinic);
            document.setSpecialty(specialty);
            
            // Si no hay fecha de visita, usar la fecha actual
            if (document.getDateOfVisit() == null) {
                document.setDateOfVisit(LocalDate.now());
            }
            
            entityManager.persist(document);
            entityManager.flush();
            
            logger.info("Documento clínico creado exitosamente: ID={}, Título={}", 
                document.getId(), document.getTitle());
            
            // Enviar registro al HCEN (RNDC) vía JMS
            // Solo si el paciente tiene número de documento (CI)
            if (patient.getDocumentNumber() != null && !patient.getDocumentNumber().trim().isEmpty()) {
                try {
                    // Construir URL base para documentLocator
                    // El documentLocator apuntará a: {baseUrl}/api/documents/{documentId}
                    // Usar variable de sistema 'clinic.base.url' o fallback a localhost
                    String documentBaseUrl = System.getProperty("clinic.base.url");
                    if (documentBaseUrl == null || documentBaseUrl.isEmpty()) {
                        // Fallback a URL por defecto si no está configurada
                        documentBaseUrl = "http://localhost:8080/clinic";
                        logger.warn("Variable de sistema 'clinic.base.url' no configurada, usando fallback: {}", 
                            documentBaseUrl);
                    }
                    // Asegurar que no termine con /
                    if (documentBaseUrl.endsWith("/")) {
                        documentBaseUrl = documentBaseUrl.substring(0, documentBaseUrl.length() - 1);
                    }
                    
                    String fhirDocumentJson = null;
                    try {
                        fhirDocumentJson = fhirMappingService.convertDocumentToFhirJson(document);
                    } catch (Exception mappingEx) {
                        logger.warn("No se pudo generar la representación FHIR del documento {}: {}", document.getId(), mappingEx.getMessage());
                    }

                    hcenJmsService.sendDocumentRegistration(document, clinic, patient, documentBaseUrl, fhirDocumentJson);
                    logger.info("Document registration sent to HCEN - Document ID: {}, Patient CI: {}", 
                        document.getId(), patient.getDocumentNumber());
                } catch (Exception e) {
                    // No fallar la creación del documento si falla el envío al HCEN
                    logger.error("Failed to send document registration to HCEN - Document ID: {}", 
                        document.getId(), e);
                }
            }
            
            return document;
            
        } catch (Exception e) {
            logger.error("Error al crear documento clínico", e);
            throw e;
        }
    }
    
    /**
     * Actualizar documento clínico
     */
    @Transactional
    public ClinicalDocument updateDocument(ClinicalDocument document) {
        try {
            ClinicalDocument existing = entityManager.find(ClinicalDocument.class, document.getId());
            if (existing == null) {
                throw new IllegalArgumentException("Documento no encontrado: " + document.getId());
            }
            
            // Actualizar campos básicos
            existing.setTitle(document.getTitle());
            existing.setDescription(document.getDescription());
            existing.setDocumentType(document.getDocumentType());
            existing.setDateOfVisit(document.getDateOfVisit());
            
            // Actualizar campos del formulario médico
            existing.setChiefComplaint(document.getChiefComplaint());
            existing.setCurrentIllness(document.getCurrentIllness());
            existing.setVitalSigns(document.getVitalSigns());
            existing.setPhysicalExamination(document.getPhysicalExamination());
            existing.setDiagnosis(document.getDiagnosis());
            existing.setTreatment(document.getTreatment());
            existing.setPrescriptions(document.getPrescriptions());
            existing.setObservations(document.getObservations());
            existing.setNextAppointment(document.getNextAppointment());
            existing.setAttachments(document.getAttachments());
            
            // Actualizar relaciones si se proporcionan
            if (document.getPatient() != null && document.getPatient().getId() != null) {
                Patient patient = entityManager.find(Patient.class, document.getPatient().getId());
                if (patient != null) {
                    existing.setPatient(patient);
                }
            }
            
            if (document.getProfessional() != null && document.getProfessional().getId() != null) {
                Professional professional = entityManager.find(Professional.class, document.getProfessional().getId());
                if (professional != null) {
                    existing.setProfessional(professional);
                }
            }
            
            if (document.getSpecialty() != null && document.getSpecialty().getId() != null) {
                Specialty specialty = entityManager.find(Specialty.class, document.getSpecialty().getId());
                if (specialty != null) {
                    existing.setSpecialty(specialty);
                }
            }
            
            entityManager.merge(existing);
            entityManager.flush();
            
            logger.info("Documento clínico actualizado exitosamente: ID={}", existing.getId());
            
            return existing;
            
        } catch (Exception e) {
            logger.error("Error al actualizar documento clínico: {}", document.getId(), e);
            throw e;
        }
    }
    
    /**
     * Eliminar documento clínico
     */
    @Transactional
    public void deleteDocument(Long id) {
        try {
            ClinicalDocument document = entityManager.find(ClinicalDocument.class, id);
            if (document == null) {
                throw new IllegalArgumentException("Documento no encontrado: " + id);
            }
            
            entityManager.remove(document);
            entityManager.flush();
            
            logger.info("Documento clínico eliminado exitosamente: ID={}", id);
            
        } catch (Exception e) {
            logger.error("Error al eliminar documento clínico: {}", id, e);
            throw e;
        }
    }
    
    /**
     * Buscar documentos con filtros
     */
    public List<ClinicalDocument> searchDocuments(String clinicId, Long specialtyId, Long patientId, 
                                                  Long professionalId, String documentType, 
                                                  LocalDate dateFrom, LocalDate dateTo) {
        try {
            if (clinicId == null) {
                throw new IllegalArgumentException("clinicId no puede ser null");
            }
            
            StringBuilder jpql = new StringBuilder("SELECT d FROM ClinicalDocument d WHERE 1=1");
            jpql.append(" AND d.clinic.id = :clinicId");
            
            if (specialtyId != null) {
                jpql.append(" AND d.specialty.id = :specialtyId");
            }
            if (patientId != null) {
                jpql.append(" AND d.patient.id = :patientId");
            }
            if (professionalId != null) {
                jpql.append(" AND d.professional.id = :professionalId");
            }
            if (documentType != null && !documentType.isEmpty()) {
                jpql.append(" AND d.documentType = :documentType");
            }
            if (dateFrom != null) {
                jpql.append(" AND d.dateOfVisit >= :dateFrom");
            }
            if (dateTo != null) {
                jpql.append(" AND d.dateOfVisit <= :dateTo");
            }
            
            jpql.append(" ORDER BY d.dateOfVisit DESC, d.createdAt DESC");
            
            TypedQuery<ClinicalDocument> query = entityManager.createQuery(jpql.toString(), ClinicalDocument.class);
            query.setParameter("clinicId", clinicId);
            
            if (specialtyId != null) {
                query.setParameter("specialtyId", specialtyId);
            }
            if (patientId != null) {
                query.setParameter("patientId", patientId);
            }
            if (professionalId != null) {
                query.setParameter("professionalId", professionalId);
            }
            if (documentType != null && !documentType.isEmpty()) {
                query.setParameter("documentType", documentType);
            }
            if (dateFrom != null) {
                query.setParameter("dateFrom", dateFrom);
            }
            if (dateTo != null) {
                query.setParameter("dateTo", dateTo);
            }
            
            List<ClinicalDocument> documents = query.getResultList();
            
            // Cargar relaciones lazy
            for (ClinicalDocument doc : documents) {
                if (doc.getPatient() != null) doc.getPatient().getName();
                if (doc.getProfessional() != null) doc.getProfessional().getName();
                if (doc.getClinic() != null) doc.getClinic().getName();
                if (doc.getSpecialty() != null) doc.getSpecialty().getName();
            }
            
            return documents;
            
        } catch (Exception e) {
            logger.error("Error al buscar documentos con filtros", e);
            return List.of();
        }
    }
}

