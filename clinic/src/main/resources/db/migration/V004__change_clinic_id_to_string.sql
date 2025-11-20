-- Migration: Change clinic_id from BIGINT to VARCHAR(50) for UUID-based IDs
-- Format: "clinic-{uuid}" e.g., "clinic-a63517f0-fb0b-45e9-8f2c-79f9d11faf95"
--
-- NOTE: This migration is no longer needed as V001 was updated to use VARCHAR(50) for clinic_id.
-- This file is kept as a placeholder to maintain migration version sequence.

-- No-op: The schema already has VARCHAR(50) for clinic_id
SELECT 1;

-- Add comment to document the expected ID format
COMMENT ON COLUMN clinics.id IS 'UUID-based clinic ID in format: clinic-{uuid}';
