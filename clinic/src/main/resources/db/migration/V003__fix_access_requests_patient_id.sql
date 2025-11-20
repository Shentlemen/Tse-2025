-- Migration: Fix access_requests table - make patient_id nullable
-- The table has a patient_id column that is NOT NULL but the entity doesn't map it
-- We use patient_ci instead, so patient_id should be nullable

-- Make patient_id nullable if it exists
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
        AND table_name = 'access_requests'
        AND column_name = 'patient_id'
    ) THEN
        ALTER TABLE access_requests ALTER COLUMN patient_id DROP NOT NULL;
        RAISE NOTICE 'Columna patient_id ahora es nullable';
    END IF;
END $$;
