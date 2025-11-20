-- Migration: Ensure explicit public schema configuration
-- This migration ensures all database operations use the public schema explicitly
-- This is important for multi-tenant deployments and schema isolation

-- Set search path for current session
SET search_path TO public;

-- Note: To set database-level default search_path, run as superuser:
-- ALTER DATABASE clinic SET search_path TO public;

-- Verify all tables exist in public schema (no-op if they do)
-- This serves as a validation that the schema structure is correct
DO $$
DECLARE
    table_count INTEGER;
BEGIN
    SELECT COUNT(*)
    INTO table_count
    FROM information_schema.tables
    WHERE table_schema = 'public'
    AND table_name IN (
        'clinics',
        'clinical_documents',
        'specialties',
        'professionals',
        'users',
        'patients',
        'access_requests'
    );

    IF table_count < 7 THEN
        RAISE WARNING 'Not all expected tables found in public schema. Found % out of 7', table_count;
    ELSE
        RAISE NOTICE 'All tables verified in public schema: % tables found', table_count;
    END IF;
END $$;

-- Add comment to document the schema configuration
COMMENT ON SCHEMA public IS 'Clinic application schema - default schema for all clinic tables';
