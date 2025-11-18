package uy.gub.clinic.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.AccessRequest;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.entity.Professional;
import uy.gub.clinic.integration.HcenRestClient;
import uy.gub.clinic.integration.dto.AccessRequestCreationRequest;
import uy.gub.clinic.integration.dto.AccessRequestCreationResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de solicitudes de acceso a documentos externos
 * 
 * @author TSE 2025 Group 9
 */
@Stateless
public class AccessRequestService {
    
    private static final Logger logger = LoggerFactory.getLogger(AccessRequestService.class);
    
    @PersistenceContext(unitName = "clinicPU")
    private EntityManager entityManager;
    
    @Inject
    private HcenRestClient hcenRestClient;
    
    /**
     * Crea una nueva solicitud de acceso y la envía al HCEN
     * 
     * @param professional Profesional que solicita acceso
     * @param clinic Clínica del profesional
     * @param patientCI Cédula del paciente
     * @param documentId ID del documento específico (opcional)
     * @param specialties Especialidades solicitadas (comma-separated o JSON array)
     * @param requestReason Razón de la solicitud
     * @param urgency Urgencia (ROUTINE, URGENT, EMERGENCY)
     * @return AccessRequest creada
     */
    @Transactional
    public AccessRequest createAccessRequest(
            Professional professional,
            Clinic clinic,
            String patientCI,
            Long documentId,
            String specialties,
            String requestReason,
            String urgency) {
        
        try {
            // Crear entidad local
            AccessRequest accessRequest = new AccessRequest();
            accessRequest.setProfessional(professional);
            accessRequest.setClinic(clinic);
            accessRequest.setPatientCI(patientCI);
            accessRequest.setDocumentId(documentId);
            accessRequest.setSpecialties(specialties);
            accessRequest.setRequestReason(requestReason);
            accessRequest.setStatus("PENDING");
            accessRequest.setUrgency(urgency != null ? urgency : "ROUTINE");
            
            // Persistir localmente primero
            entityManager.persist(accessRequest);
            entityManager.flush();
            
            logger.info("Access request created locally - ID: {}, Professional: {}, Patient: {}", 
                accessRequest.getId(), professional.getId(), patientCI);
            
            // Enviar al HCEN si tenemos configuración
            if (clinic.getApiKey() != null && clinic.getHcenEndpoint() != null) {
                try {
                    AccessRequestCreationRequest hcenRequest = new AccessRequestCreationRequest();
                    hcenRequest.setProfessionalId("professional-" + professional.getId());
                    hcenRequest.setProfessionalName(professional.getFullName());
                    hcenRequest.setSpecialty(professional.getSpecialty() != null ? 
                        professional.getSpecialty().getName() : null);
                    hcenRequest.setPatientCi(patientCI);
                    hcenRequest.setDocumentId(documentId);
                    hcenRequest.setRequestReason(requestReason);
                    hcenRequest.setUrgency(urgency);
                    
                    AccessRequestCreationResponse hcenResponse = hcenRestClient.createAccessRequest(
                        hcenRequest,
                        clinic.getCode() != null ? clinic.getCode() : "clinic-" + clinic.getId(),
                        clinic.getApiKey(),
                        clinic.getHcenEndpoint()
                    );
                    
                    // Actualizar con respuesta del HCEN
                    accessRequest.setHcenRequestId(hcenResponse.getRequestId() != null ? 
                        hcenResponse.getRequestId().toString() : null);
                    
                    if (hcenResponse.getExpiresAt() != null) {
                        accessRequest.setExpiresAt(hcenResponse.getExpiresAt());
                    }
                    
                    entityManager.merge(accessRequest);
                    
                    logger.info("Access request sent to HCEN - Local ID: {}, HCEN Request ID: {}", 
                        accessRequest.getId(), hcenResponse.getRequestId());
                        
                } catch (Exception e) {
                    logger.error("Failed to send access request to HCEN - Local ID: {}", 
                        accessRequest.getId(), e);
                    // No fallar - la solicitud local ya está guardada
                }
            } else {
                logger.warn("Cannot send access request to HCEN - Clinic missing API key or endpoint");
            }
            
            return accessRequest;
            
        } catch (Exception e) {
            logger.error("Error creating access request", e);
            throw e;
        }
    }
    
    /**
     * Obtiene todas las solicitudes de acceso de un profesional
     */
    public List<AccessRequest> findByProfessional(Long professionalId) {
        TypedQuery<AccessRequest> query = entityManager.createNamedQuery(
            "AccessRequest.findByProfessional", AccessRequest.class);
        query.setParameter("professionalId", professionalId);
        return query.getResultList();
    }
    
    /**
     * Cuenta las solicitudes pendientes de un profesional
     */
    public long countPendingByProfessional(Long professionalId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(ar) FROM AccessRequest ar WHERE ar.professional.id = :professionalId AND ar.status = 'PENDING'",
            Long.class);
        query.setParameter("professionalId", professionalId);
        return query.getSingleResult() != null ? query.getSingleResult() : 0L;
    }
    
    /**
     * Obtiene las solicitudes pendientes de un profesional
     */
    public List<AccessRequest> findPendingByProfessional(Long professionalId) {
        TypedQuery<AccessRequest> query = entityManager.createQuery(
            "SELECT ar FROM AccessRequest ar WHERE ar.professional.id = :professionalId AND ar.status = 'PENDING' ORDER BY ar.requestedAt DESC",
            AccessRequest.class);
        query.setParameter("professionalId", professionalId);
        return query.getResultList();
    }
    
    /**
     * Obtiene todas las solicitudes de acceso de una clínica
     */
    public List<AccessRequest> findByClinic(Long clinicId) {
        TypedQuery<AccessRequest> query = entityManager.createNamedQuery(
            "AccessRequest.findByClinic", AccessRequest.class);
        query.setParameter("clinicId", clinicId);
        return query.getResultList();
    }
    
    /**
     * Obtiene solicitudes por estado
     */
    public List<AccessRequest> findByStatus(String status) {
        TypedQuery<AccessRequest> query = entityManager.createNamedQuery(
            "AccessRequest.findByStatus", AccessRequest.class);
        query.setParameter("status", status);
        return query.getResultList();
    }
    
    /**
     * Obtiene una solicitud por ID
     */
    public Optional<AccessRequest> findById(Long id) {
        AccessRequest request = entityManager.find(AccessRequest.class, id);
        return Optional.ofNullable(request);
    }
    
    /**
     * Obtiene una solicitud por HCEN request ID
     */
    public Optional<AccessRequest> findByHcenRequestId(String hcenRequestId) {
        TypedQuery<AccessRequest> query = entityManager.createQuery(
            "SELECT ar FROM AccessRequest ar WHERE ar.hcenRequestId = :hcenRequestId",
            AccessRequest.class);
        query.setParameter("hcenRequestId", hcenRequestId);
        
        List<AccessRequest> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * Actualiza el estado de una solicitud (cuando HCEN notifica aprobación/denegación)
     */
    @Transactional
    public AccessRequest updateStatus(Long id, String status) {
        AccessRequest request = entityManager.find(AccessRequest.class, id);
        if (request == null) {
            throw new IllegalArgumentException("Access request not found: " + id);
        }
        
        request.setStatus(status);
        request.setRespondedAt(LocalDateTime.now());
        
        if ("APPROVED".equals(status)) {
            request.approve();
        } else if ("DENIED".equals(status)) {
            request.deny();
        } else if ("EXPIRED".equals(status)) {
            request.expire();
        }
        
        entityManager.merge(request);
        return request;
    }
    
    /**
     * Actualiza el estado de una solicitud por HCEN request ID
     */
    @Transactional
    public AccessRequest updateStatusByHcenRequestId(String hcenRequestId, String status) {
        Optional<AccessRequest> requestOpt = findByHcenRequestId(hcenRequestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Access request not found with HCEN ID: " + hcenRequestId);
        }
        
        AccessRequest request = requestOpt.get();
        return updateStatus(request.getId(), status);
    }
}
