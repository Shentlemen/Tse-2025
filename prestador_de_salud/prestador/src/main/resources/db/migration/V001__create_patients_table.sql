-- ================================================================
-- Migration: V001 - Create Patients Table
-- Description: Creates the patients table for health provider
-- Author: TSE 2025 Group 9
-- Date: 2025-11-13
-- ================================================================

CREATE TABLE public.patients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    document_number VARCHAR(50),
    inus_id VARCHAR(50),
    birth_date DATE,
    gender VARCHAR(10),
    email VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    clinic_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6)
);

-- Indexes
CREATE INDEX idx_patients_document_number ON public.patients(document_number);
CREATE INDEX idx_patients_inus_id ON public.patients(inus_id);
CREATE INDEX idx_patients_clinic_id ON public.patients(clinic_id);
CREATE INDEX idx_patients_email ON public.patients(email);
CREATE INDEX idx_patients_active ON public.patients(active);
CREATE INDEX idx_patients_clinic_active ON public.patients(clinic_id, active);

-- Comments
COMMENT ON TABLE public.patients IS 'Patient information for health provider';
COMMENT ON COLUMN public.patients.document_number IS 'National ID (CI)';
COMMENT ON COLUMN public.patients.inus_id IS 'HCEN INUS ID reference';
