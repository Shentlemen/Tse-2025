-- ==============================================
-- RNDC (Registro Nacional de Documentos Clínicos)
-- Database Schema Migration
-- ==============================================
-- Version: V002
-- Description: Creates RNDC schema and documents metadata table
-- Author: TSE 2025 Group 9
-- Date: 2025-10-17
-- ==============================================

-- Create RNDC schema if not exists
CREATE SCHEMA IF NOT EXISTS rndc;

-- Set search path to rndc schema
SET search_path TO rndc;

-- Create rndc_documents table
-- This table stores metadata about clinical documents stored in peripheral nodes
-- The actual documents remain in peripheral storage, RNDC only stores pointers and metadata
CREATE TABLE IF NOT EXISTS rndc.rndc_documents (
    -- Primary key
    id BIGSERIAL PRIMARY KEY,

    -- Patient identification (Cédula de Identidad)
    patient_ci VARCHAR(20) NOT NULL,

    -- Document locator URL pointing to peripheral storage
    -- Example: https://clinic-001.hcen.uy/api/documents/abc123
    document_locator VARCHAR(500) NOT NULL,

    -- SHA-256 hash of the document for integrity verification
    -- Format: sha256:hexadecimal_hash
    document_hash VARCHAR(64) NOT NULL,

    -- Type of clinical document
    -- CHECK constraint ensures only valid document types are stored
    document_type VARCHAR(50) NOT NULL
        CHECK (document_type IN (
            'CLINICAL_NOTE',
            'LAB_RESULT',
            'IMAGING',
            'PRESCRIPTION',
            'DISCHARGE_SUMMARY',
            'VACCINATION_RECORD',
            'SURGICAL_REPORT',
            'PATHOLOGY_REPORT',
            'CONSULTATION',
            'EMERGENCY_REPORT',
            'REFERRAL',
            'PROGRESS_NOTE',
            'ALLERGY_RECORD',
            'VITAL_SIGNS',
            'DIAGNOSTIC_REPORT',
            'TREATMENT_PLAN',
            'INFORMED_CONSENT',
            'OTHER'
        )),

    -- Professional who created the document (email or professional ID)
    created_by VARCHAR(100) NOT NULL,

    -- Document creation timestamp
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Document status (active, inactive, or deleted)
    -- Soft delete: documents are marked as DELETED instead of physical removal
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED')),

    -- Clinic/peripheral node identifier that owns this document
    clinic_id VARCHAR(50) NOT NULL,

    -- Optional: Document title for quick identification
    document_title VARCHAR(200),

    -- Optional: Document description or summary
    document_description TEXT,

    -- Constraint: document_locator must be unique (prevent duplicate registrations)
    CONSTRAINT uk_rndc_document_locator UNIQUE (document_locator)
);

-- ==============================================
-- Indexes for performance optimization
-- ==============================================

-- Index on patient_ci for fast patient document lookups
-- Most common query: "Get all documents for patient X"
CREATE INDEX IF NOT EXISTS idx_rndc_patient_ci
    ON rndc.rndc_documents(patient_ci);

-- Index on clinic_id for clinic-specific queries
-- Use case: "Get all documents registered by clinic Y"
CREATE INDEX IF NOT EXISTS idx_rndc_clinic_id
    ON rndc.rndc_documents(clinic_id);

-- Index on document_type for filtering by document type
-- Use case: "Get all lab results for patient X"
CREATE INDEX IF NOT EXISTS idx_rndc_document_type
    ON rndc.rndc_documents(document_type);

-- Composite index on patient_ci and status for filtered patient queries
-- Use case: "Get all active documents for patient X"
-- This is a covering index for common queries
CREATE INDEX IF NOT EXISTS idx_rndc_patient_ci_status
    ON rndc.rndc_documents(patient_ci, status);

-- Index on created_at for chronological sorting
-- Use case: "Get recent documents" or "Get documents in date range"
CREATE INDEX IF NOT EXISTS idx_rndc_created_at
    ON rndc.rndc_documents(created_at DESC);

-- Composite index for complex queries combining patient, type, and status
-- Use case: "Get all active lab results for patient X"
CREATE INDEX IF NOT EXISTS idx_rndc_patient_type_status
    ON rndc.rndc_documents(patient_ci, document_type, status);

-- ==============================================
-- Comments for documentation
-- ==============================================

COMMENT ON SCHEMA rndc IS
    'RNDC (Registro Nacional de Documentos Clínicos) - National Clinical Document Registry. Stores metadata about clinical documents, with actual documents stored in peripheral nodes.';

COMMENT ON TABLE rndc.rndc_documents IS
    'Clinical document metadata registry. Stores pointers to documents in peripheral nodes, along with metadata for indexing and access control.';

COMMENT ON COLUMN rndc.rndc_documents.patient_ci IS
    'Patient Cédula de Identidad (national ID) - references INUS registry';

COMMENT ON COLUMN rndc.rndc_documents.document_locator IS
    'URL to retrieve the actual document from peripheral node storage';

COMMENT ON COLUMN rndc.rndc_documents.document_hash IS
    'SHA-256 hash for document integrity verification';

COMMENT ON COLUMN rndc.rndc_documents.document_type IS
    'Type of clinical document (CLINICAL_NOTE, LAB_RESULT, etc.)';

COMMENT ON COLUMN rndc.rndc_documents.status IS
    'Document status: ACTIVE (available), INACTIVE (archived), DELETED (soft deleted)';

COMMENT ON COLUMN rndc.rndc_documents.clinic_id IS
    'Identifier of the clinic/peripheral node that registered this document';

-- ==============================================
-- Grant permissions (adjust for production)
-- ==============================================

-- Grant usage on schema to application user (adjust user name as needed)
-- GRANT USAGE ON SCHEMA rndc TO hcen_app;
-- GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA rndc TO hcen_app;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA rndc TO hcen_app;

-- ==============================================
-- Migration complete
-- ==============================================
