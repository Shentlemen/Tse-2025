package com.prestador.service;

import com.prestador.entity.ClinicalDocument;
import com.prestador.entity.Patient;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

/**
 * Clinical Document Service
 *
 * EJB service for clinical document operations with automatic transaction management.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@Stateless
public class ClinicalDocumentService {

    @PersistenceContext(unitName = "prestador-pu")
    private EntityManager entityManager;

    /**
     * Create a new clinical document
     */
    public ClinicalDocument createDocument(ClinicalDocument document) {
        entityManager.persist(document);
        entityManager.flush();
        return document;
    }

    /**
     * Find document by ID
     */
    public ClinicalDocument findById(Long id) {
        return entityManager.find(ClinicalDocument.class, id);
    }

    /**
     * Find patient by ID (for retrieving patient info)
     */
    public Patient findPatientById(Long patientId) {
        return entityManager.find(Patient.class, patientId);
    }

    /**
     * Find all documents
     */
    public List<ClinicalDocument> findAll() {
        return entityManager.createQuery(
            "SELECT d FROM ClinicalDocument d ORDER BY d.dateOfVisit DESC, d.id",
            ClinicalDocument.class)
            .getResultList();
    }

    /**
     * Find documents by patient ID
     */
    public List<ClinicalDocument> findByPatientId(Long patientId) {
        return entityManager.createQuery(
            "SELECT d FROM ClinicalDocument d WHERE d.patientId = :patientId ORDER BY d.dateOfVisit DESC",
            ClinicalDocument.class)
            .setParameter("patientId", patientId)
            .getResultList();
    }

    /**
     * Update document
     */
    public ClinicalDocument updateDocument(ClinicalDocument document) {
        return entityManager.merge(document);
    }

    /**
     * Delete document
     */
    public void deleteDocument(Long id) {
        ClinicalDocument document = findById(id);
        if (document != null) {
            entityManager.remove(document);
        }
    }
}
