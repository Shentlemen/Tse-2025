-- ================================================================
-- Migration: V001 - Create Patients Table
-- Description: Creates the patients table for health provider
-- Author: TSE 2025 Group 9
-- Date: 2025-11-13
-- ================================================================

-- Create schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS health_provider;

CREATE TABLE health_provider.patients (
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
CREATE INDEX idx_patients_document_number ON health_provider.patients(document_number);
CREATE INDEX idx_patients_inus_id ON health_provider.patients(inus_id);
CREATE INDEX idx_patients_clinic_id ON health_provider.patients(clinic_id);
CREATE INDEX idx_patients_email ON health_provider.patients(email);
CREATE INDEX idx_patients_active ON health_provider.patients(active);
CREATE INDEX idx_patients_clinic_active ON health_provider.patients(clinic_id, active);

-- Comments
COMMENT ON TABLE health_provider.patients IS 'Patient information for health provider';
COMMENT ON COLUMN health_provider.patients.document_number IS 'National ID (CI)';
COMMENT ON COLUMN health_provider.patients.inus_id IS 'HCEN INUS ID reference';
