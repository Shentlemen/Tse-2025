-- ================================================================
-- Migration: V002 - Create Clinical Documents Table
-- Description: Creates the clinical_documents table
-- Author: TSE 2025 Group 9
-- Date: 2025-11-13
-- ================================================================

CREATE TABLE public.clinical_documents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    document_type VARCHAR(100) NOT NULL,
    patient_id BIGINT NOT NULL,
    clinic_id BIGINT NOT NULL,
    professional_id BIGINT NOT NULL,
    specialty_id BIGINT,
    date_of_visit DATE NOT NULL,

    -- File information
    file_name VARCHAR(100),
    file_path VARCHAR(500),
    file_size BIGINT,
    mime_type VARCHAR(100),

    -- RNDC reference
    rndc_id VARCHAR(100),

    -- Clinical information
    chief_complaint TEXT,
    current_illness TEXT,
    vital_signs TEXT,
    physical_examination TEXT,
    diagnosis TEXT,
    treatment TEXT,
    prescriptions TEXT,
    observations TEXT,
    next_appointment DATE,
    attachments TEXT,

    -- Audit fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6)
);

-- Indexes
CREATE INDEX idx_clinical_documents_patient_id ON public.clinical_documents(patient_id);
CREATE INDEX idx_clinical_documents_clinic_id ON public.clinical_documents(clinic_id);
CREATE INDEX idx_clinical_documents_professional_id ON public.clinical_documents(professional_id);
CREATE INDEX idx_clinical_documents_date_of_visit ON public.clinical_documents(date_of_visit);
CREATE INDEX idx_clinical_documents_document_type ON public.clinical_documents(document_type);
CREATE INDEX idx_clinical_documents_rndc_id ON public.clinical_documents(rndc_id);
CREATE INDEX idx_clinical_documents_patient_date ON public.clinical_documents(patient_id, date_of_visit DESC);
CREATE INDEX idx_clinical_documents_clinic_date ON public.clinical_documents(clinic_id, date_of_visit DESC);

-- Comments
COMMENT ON TABLE public.clinical_documents IS 'Clinical documents and medical records';
COMMENT ON COLUMN public.clinical_documents.rndc_id IS 'Reference to HCEN RNDC';
