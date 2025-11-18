-- ================================================================
-- HCEN - Access Policies Refactoring Migration
-- ================================================================
-- Description: Simplify policy system from flexible ABAC to
--              clinic+specialty permission model
-- Version: V008
-- Author: TSE 2025 Group 9
-- Date: 2025-11-18
-- ================================================================

-- WARNING: This is a breaking change - all existing policies will be invalidated
-- Backup existing data if needed before running this migration

-- ================================================================
-- Step 1: Create temporary table to backup existing data (if any)
-- ================================================================
CREATE TABLE IF NOT EXISTS policies.access_policies_backup AS
SELECT * FROM policies.access_policies;

-- ================================================================
-- Step 2: Drop existing indexes that reference old columns
-- ================================================================
DROP INDEX IF EXISTS policies.idx_access_policies_policy_type;
DROP INDEX IF EXISTS policies.idx_access_policies_policy_effect;
DROP INDEX IF EXISTS policies.idx_access_policies_patient_type_effect;

-- ================================================================
-- Step 3: Drop old columns
-- ================================================================
ALTER TABLE policies.access_policies
    DROP COLUMN IF EXISTS policy_type,
    DROP COLUMN IF EXISTS policy_config,
    DROP COLUMN IF EXISTS policy_effect;

-- ================================================================
-- Step 4: Add new columns
-- ================================================================

-- Clinic ID (references clinics.clinics.clinic_id)
ALTER TABLE policies.access_policies
    ADD COLUMN IF NOT EXISTS clinic_id VARCHAR(50) NOT NULL DEFAULT 'MIGRATION_REQUIRED';

-- Medical specialty enum
ALTER TABLE policies.access_policies
    ADD COLUMN IF NOT EXISTS specialty VARCHAR(50) NOT NULL DEFAULT 'MEDICINA_GENERAL';

-- Document ID (nullable - null means ALL documents)
ALTER TABLE policies.access_policies
    ADD COLUMN IF NOT EXISTS document_id BIGINT;

-- Policy status (GRANTED, PENDING, REVOKED)
ALTER TABLE policies.access_policies
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'GRANTED'
    CHECK (status IN ('GRANTED', 'PENDING', 'REVOKED'));

-- ================================================================
-- Step 5: Remove default constraints after adding columns
-- ================================================================
ALTER TABLE policies.access_policies
    ALTER COLUMN clinic_id DROP DEFAULT,
    ALTER COLUMN specialty DROP DEFAULT;

-- ================================================================
-- Step 6: Add new constraint for specialty values
-- ================================================================
ALTER TABLE policies.access_policies
    ADD CONSTRAINT chk_access_policies_specialty
    CHECK (specialty IN (
        'CARDIOLOGIA',
        'MEDICINA_GENERAL',
        'ONCOLOGIA',
        'PEDIATRIA',
        'NEUROLOGIA',
        'CIRUGIA',
        'GINECOLOGIA',
        'DERMATOLOGIA',
        'PSIQUIATRIA',
        'TRAUMATOLOGIA'
    ));

-- ================================================================
-- Step 7: Add foreign key to clinics table
-- ================================================================
ALTER TABLE policies.access_policies
    ADD CONSTRAINT fk_access_policies_clinic
    FOREIGN KEY (clinic_id)
    REFERENCES clinics.clinics(clinic_id)
    ON DELETE CASCADE;

-- ================================================================
-- Step 8: Create new indexes for performance optimization
-- ================================================================

-- Main search index (patient + clinic + specialty)
CREATE INDEX IF NOT EXISTS idx_access_policies_patient_clinic_specialty
    ON policies.access_policies(patient_ci, clinic_id, specialty);

-- Status index for filtering
CREATE INDEX IF NOT EXISTS idx_access_policies_status
    ON policies.access_policies(status);

-- Document ID index for specific document policies
CREATE INDEX IF NOT EXISTS idx_access_policies_document_id
    ON policies.access_policies(document_id)
    WHERE document_id IS NOT NULL;

-- Clinic ID index
CREATE INDEX IF NOT EXISTS idx_access_policies_clinic_id
    ON policies.access_policies(clinic_id);

-- Specialty index
CREATE INDEX IF NOT EXISTS idx_access_policies_specialty
    ON policies.access_policies(specialty);

-- ================================================================
-- Step 9: Clear existing invalid data (policies without valid clinic_id)
-- ================================================================
DELETE FROM policies.access_policies
WHERE clinic_id = 'MIGRATION_REQUIRED';

-- ================================================================
-- Step 10: Update comments
-- ================================================================
COMMENT ON COLUMN policies.access_policies.clinic_id IS 'Clinic identifier (references clinics.clinics.clinic_id)';
COMMENT ON COLUMN policies.access_policies.specialty IS 'Medical specialty: CARDIOLOGIA, MEDICINA_GENERAL, ONCOLOGIA, PEDIATRIA, NEUROLOGIA, CIRUGIA, GINECOLOGIA, DERMATOLOGIA, PSIQUIATRIA, TRAUMATOLOGIA';
COMMENT ON COLUMN policies.access_policies.document_id IS 'Specific document ID (NULL means all documents)';
COMMENT ON COLUMN policies.access_policies.status IS 'Policy status: GRANTED, PENDING, REVOKED';

-- ================================================================
-- Notes:
-- - The backup table (access_policies_backup) can be dropped after
--   verifying the migration was successful
-- - All existing policies are invalidated by this migration
-- - New policies must specify clinic_id and specialty
-- ================================================================
