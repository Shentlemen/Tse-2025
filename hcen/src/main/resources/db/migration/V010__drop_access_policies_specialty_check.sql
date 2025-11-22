-- ================================================================
-- Migration: V010 - Drop access_policies_specialty_check constraint
-- ================================================================
-- Description: Removes the CHECK constraint on specialty column
--              to allow NULL values for general access policies
--              that don't restrict by specialty.
--
-- Author: TSE 2025 Group 9
-- Date: 2025-11-22
-- ================================================================

-- Drop the specialty check constraint
ALTER TABLE policies.access_policies
DROP CONSTRAINT IF EXISTS access_policies_specialty_check;

ALTER TABLE policies.access_policies
DROP CONSTRAINT IF EXISTS chk_access_policies_specialty;

-- Add comment explaining why constraint was removed
COMMENT ON COLUMN policies.access_policies.specialty IS
'Medical specialty allowed to access documents. NULL allows access for all specialties.';
