-- ================================================================
-- HCEN - POLICIES Schema
-- ================================================================
-- Description: Access policies and access requests for patient data control
-- Version: V003
-- Author: TSE 2025 Group 9
-- ================================================================

-- Create POLICIES schema if not exists
CREATE SCHEMA IF NOT EXISTS policies;

-- ================================================================
-- TABLE: access_policies
-- ================================================================
-- Description: Patient-defined access control policies
-- Purpose: Enable patients to define granular access control rules
--          for their clinical documents
-- ================================================================

CREATE TABLE IF NOT EXISTS policies.access_policies (
    -- Primary key
    id BIGSERIAL PRIMARY KEY,

    -- Patient identification
    patient_ci VARCHAR(20) NOT NULL,

    -- Policy type (DOCUMENT_TYPE, SPECIALTY, TIME_BASED, CLINIC, PROFESSIONAL, EMERGENCY_OVERRIDE)
    policy_type VARCHAR(50) NOT NULL,

    -- Policy configuration (stored as JSONB for flexibility)
    -- Example for DOCUMENT_TYPE: {"allowedTypes": ["LAB_RESULT", "IMAGING"]}
    -- Example for SPECIALTY: {"allowedSpecialties": ["CARDIOLOGY", "GENERAL_MEDICINE"]}
    -- Example for TIME_BASED: {"allowedDays": ["MONDAY", "FRIDAY"], "allowedHours": "09:00-17:00"}
    policy_config JSONB NOT NULL,

    -- Policy effect (PERMIT or DENY)
    policy_effect VARCHAR(10) NOT NULL CHECK (policy_effect IN ('PERMIT', 'DENY')),

    -- Validity period (optional)
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,

    -- Priority for conflict resolution (higher priority wins)
    priority INTEGER DEFAULT 0,

    -- Audit timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_access_policies_patient_ci ON policies.access_policies(patient_ci);
CREATE INDEX IF NOT EXISTS idx_access_policies_policy_type ON policies.access_policies(policy_type);
CREATE INDEX IF NOT EXISTS idx_access_policies_policy_effect ON policies.access_policies(policy_effect);
CREATE INDEX IF NOT EXISTS idx_access_policies_valid_from ON policies.access_policies(valid_from);
CREATE INDEX IF NOT EXISTS idx_access_policies_valid_until ON policies.access_policies(valid_until);
CREATE INDEX IF NOT EXISTS idx_access_policies_priority ON policies.access_policies(priority);

-- Composite index for common query pattern (patient + type + effect)
CREATE INDEX IF NOT EXISTS idx_access_policies_patient_type_effect
    ON policies.access_policies(patient_ci, policy_type, policy_effect);

-- ================================================================
-- TABLE: access_requests
-- ================================================================
-- Description: Professional access requests requiring patient approval
-- Purpose: Enable professionals to request access to restricted documents
--          when policies evaluate to PENDING
-- ================================================================

CREATE TABLE IF NOT EXISTS policies.access_requests (
    -- Primary key
    id BIGSERIAL PRIMARY KEY,

    -- Professional identification
    professional_id VARCHAR(100) NOT NULL,

    -- Patient identification
    patient_ci VARCHAR(20) NOT NULL,

    -- Document requested (optional - can be general access request)
    document_id BIGINT,

    -- Request reason (provided by professional)
    request_reason TEXT,

    -- Request status (PENDING, APPROVED, DENIED, EXPIRED)
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'DENIED', 'EXPIRED')),

    -- Request timestamp
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Response timestamp (when patient approves/denies)
    responded_at TIMESTAMP,

    -- Patient's response explanation (optional)
    patient_response TEXT,

    -- Expiration timestamp (default 48 hours from request)
    expires_at TIMESTAMP NOT NULL
);

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_access_requests_professional_id ON policies.access_requests(professional_id);
CREATE INDEX IF NOT EXISTS idx_access_requests_patient_ci ON policies.access_requests(patient_ci);
CREATE INDEX IF NOT EXISTS idx_access_requests_status ON policies.access_requests(status);
CREATE INDEX IF NOT EXISTS idx_access_requests_requested_at ON policies.access_requests(requested_at);
CREATE INDEX IF NOT EXISTS idx_access_requests_expires_at ON policies.access_requests(expires_at);

-- Composite index for common query patterns
CREATE INDEX IF NOT EXISTS idx_access_requests_patient_ci_status
    ON policies.access_requests(patient_ci, status);
CREATE INDEX IF NOT EXISTS idx_access_requests_status_expires_at
    ON policies.access_requests(status, expires_at);

-- ================================================================
-- TRIGGERS
-- ================================================================

-- Create function to update updated_at timestamp for access_policies
CREATE OR REPLACE FUNCTION policies.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_access_policies_updated_at
    BEFORE UPDATE ON policies.access_policies
    FOR EACH ROW
    EXECUTE FUNCTION policies.update_updated_at_column();

-- ================================================================
-- COMMENTS (Documentation)
-- ================================================================

COMMENT ON SCHEMA policies IS 'Access control policies and access requests schema';

-- access_policies table comments
COMMENT ON TABLE policies.access_policies IS 'Patient-defined access control policies for clinical documents';
COMMENT ON COLUMN policies.access_policies.id IS 'Unique policy identifier';
COMMENT ON COLUMN policies.access_policies.patient_ci IS 'Patient CI (references inus.inus_users.ci)';
COMMENT ON COLUMN policies.access_policies.policy_type IS 'Policy type: DOCUMENT_TYPE, SPECIALTY, TIME_BASED, CLINIC, PROFESSIONAL, EMERGENCY_OVERRIDE';
COMMENT ON COLUMN policies.access_policies.policy_config IS 'Policy configuration as JSONB (flexible structure per policy type)';
COMMENT ON COLUMN policies.access_policies.policy_effect IS 'Policy effect: PERMIT or DENY';
COMMENT ON COLUMN policies.access_policies.valid_from IS 'Policy validity start date (optional)';
COMMENT ON COLUMN policies.access_policies.valid_until IS 'Policy validity end date (optional)';
COMMENT ON COLUMN policies.access_policies.priority IS 'Policy priority for conflict resolution (higher wins)';
COMMENT ON COLUMN policies.access_policies.created_at IS 'Timestamp of policy creation';
COMMENT ON COLUMN policies.access_policies.updated_at IS 'Timestamp of last policy update (auto-updated via trigger)';

-- access_requests table comments
COMMENT ON TABLE policies.access_requests IS 'Professional access requests requiring patient approval';
COMMENT ON COLUMN policies.access_requests.id IS 'Unique request identifier';
COMMENT ON COLUMN policies.access_requests.professional_id IS 'Requesting professional identifier';
COMMENT ON COLUMN policies.access_requests.patient_ci IS 'Patient CI (references inus.inus_users.ci)';
COMMENT ON COLUMN policies.access_requests.document_id IS 'Requested document ID (optional, can be general access)';
COMMENT ON COLUMN policies.access_requests.request_reason IS 'Reason provided by professional for access request';
COMMENT ON COLUMN policies.access_requests.status IS 'Request status: PENDING, APPROVED, DENIED, EXPIRED';
COMMENT ON COLUMN policies.access_requests.requested_at IS 'Timestamp of request creation';
COMMENT ON COLUMN policies.access_requests.responded_at IS 'Timestamp when patient responded';
COMMENT ON COLUMN policies.access_requests.patient_response IS 'Patient response explanation (optional)';
COMMENT ON COLUMN policies.access_requests.expires_at IS 'Request expiration timestamp (default 48 hours)';
