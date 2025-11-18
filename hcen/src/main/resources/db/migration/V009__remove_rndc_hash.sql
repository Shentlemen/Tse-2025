-- ================================================================
-- HCEN - Access Policies Refactoring Migration
-- ================================================================
-- Description: Simplify policy system from flexible ABAC to
--              clinic+specialty permission model
-- Version: V009
-- Author: TSE 2025 Group 9
-- Date: 2025-11-18

-- ================================================================
-- Step 1: Remove not null constraint
-- ================================================================
ALTER TABLE rndc.rndc_documents
        ALTER COLUMN document_hash DROP NOT NULL;