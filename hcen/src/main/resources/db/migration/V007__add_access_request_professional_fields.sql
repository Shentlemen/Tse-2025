-- ================================================================
-- HCEN - Add Professional Context Fields to Access Requests
-- ================================================================
-- Description: Adds professional and clinic context fields to access_requests table
-- Version: V007
-- Author: TSE 2025 Group 9
-- Date: 2025-11-13
-- ================================================================

-- Add professional and clinic context fields to access_requests table
ALTER TABLE policies.access_requests
    ADD COLUMN IF NOT EXISTS professional_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS specialty VARCHAR(100),
    ADD COLUMN IF NOT EXISTS clinic_id VARCHAR(50),
    ADD COLUMN IF NOT EXISTS clinic_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS document_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS urgency VARCHAR(20) DEFAULT 'ROUTINE'
        CHECK (urgency IN ('ROUTINE', 'URGENT', 'EMERGENCY'));

-- Create index for clinic_id queries
CREATE INDEX IF NOT EXISTS idx_access_requests_clinic_id
    ON policies.access_requests(clinic_id);

-- Create composite index for deduplication query
-- Used to find existing pending requests for same professional/patient/document combination
CREATE INDEX IF NOT EXISTS idx_access_requests_dedup
    ON policies.access_requests(professional_id, patient_ci, document_id, status);

-- Add comments for new columns
COMMENT ON COLUMN policies.access_requests.professional_name IS 'Professional full name for patient display';
COMMENT ON COLUMN policies.access_requests.specialty IS 'Professional specialty (e.g., CARDIOLOGY, PEDIATRICS)';
COMMENT ON COLUMN policies.access_requests.clinic_id IS 'Clinic/provider identifier that submitted the request';
COMMENT ON COLUMN policies.access_requests.clinic_name IS 'Clinic name for patient display';
COMMENT ON COLUMN policies.access_requests.document_type IS 'Document type for patient display (e.g., LAB_RESULT, CLINICAL_NOTE)';
COMMENT ON COLUMN policies.access_requests.urgency IS 'Request urgency level (ROUTINE, URGENT, EMERGENCY)';
