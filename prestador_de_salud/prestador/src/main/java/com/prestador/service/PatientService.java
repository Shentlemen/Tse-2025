package com.prestador.service;

import com.prestador.entity.Patient;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

/**
 * Patient Service
 *
 * EJB service for patient operations with automatic transaction management.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
@Stateless
public class PatientService {

    @PersistenceContext(unitName = "prestador-pu")
    private EntityManager entityManager;

    /**
     * Create a new patient
     */
    public Patient createPatient(Patient patient) {
        entityManager.persist(patient);
        entityManager.flush();
        return patient;
    }

    /**
     * Find patient by ID
     */
    public Patient findById(Long id) {
        return entityManager.find(Patient.class, id);
    }

    /**
     * Find all patients
     */
    public List<Patient> findAll() {
        return entityManager.createQuery(
            "SELECT p FROM Patient p ORDER BY p.id", Patient.class)
            .getResultList();
    }

    /**
     * Update patient
     */
    public Patient updatePatient(Patient patient) {
        return entityManager.merge(patient);
    }

    /**
     * Delete patient
     */
    public void deletePatient(Long id) {
        Patient patient = findById(id);
        if (patient != null) {
            entityManager.remove(patient);
        }
    }
}
