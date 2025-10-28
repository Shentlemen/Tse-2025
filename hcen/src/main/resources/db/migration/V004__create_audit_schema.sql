-- =============================================================================
-- HCEN Database Migration V004: Audit Logs & Notification Preferences Schema
-- =============================================================================
-- Description: Creates the audit logging system and notification preferences
--              for patient notifications
-- Author: TSE 2025 Group 9
-- Version: 1.0
-- Date: 2025-10-17
-- =============================================================================

-- Create audit schema if not exists
CREATE SCHEMA IF NOT EXISTS audit;

-- =============================================================================
-- Table: audit_logs
-- Purpose: Immutable audit trail for all system events
-- Notes: APPEND-ONLY - No updates or deletes allowed. This ensures
--        complete traceability as required by AC026
-- =============================================================================

CREATE TABLE audit.audit_logs (
    id BIGSERIAL PRIMARY KEY,

    -- Event classification
    event_type VARCHAR(50) NOT NULL,

    -- Actor information
    actor_id VARCHAR(100) NOT NULL,
    actor_type VARCHAR(20),

    -- Resource information
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(100) NOT NULL,

    -- Action result
    action_outcome VARCHAR(20) NOT NULL,

    -- Technical details
    ip_address VARCHAR(45),  -- IPv4 (15) or IPv6 (45)
    user_agent VARCHAR(500),

    -- Timestamp
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Additional context (JSONB for flexible structure)
    details VARCHAR(1000),

    -- Constraints
    CONSTRAINT chk_action_outcome CHECK (action_outcome IN ('SUCCESS', 'FAILURE', 'DENIED'))
);

-- =============================================================================
-- Indexes for audit_logs
-- Purpose: Optimize common query patterns
-- =============================================================================

-- Index on event_type for filtering by event type
CREATE INDEX idx_audit_event_type ON audit.audit_logs(event_type);

-- Index on actor_id for user-specific queries
CREATE INDEX idx_audit_actor_id ON audit.audit_logs(actor_id);

-- Index on timestamp for time-based queries and sorting
CREATE INDEX idx_audit_timestamp ON audit.audit_logs(timestamp DESC);

-- Composite index on resource_type and resource_id for resource-specific queries
CREATE INDEX idx_audit_resource_type_id ON audit.audit_logs(resource_type, resource_id);

-- =============================================================================
-- Table Comment
-- =============================================================================

COMMENT ON TABLE audit.audit_logs IS
'Append-only audit log - no updates or deletes allowed.
All system events are recorded here for compliance and traceability (AC026).
Retention: 90 days in PostgreSQL, then archived to MongoDB (optional).';

-- =============================================================================
-- Table: notification_preferences
-- Purpose: Store patient notification preferences and FCM tokens
-- Notes: One row per user (patient), used for push notifications (CU04, CU07)
-- =============================================================================

CREATE TABLE notification_preferences (
    id BIGSERIAL PRIMARY KEY,

    -- User identifier (unique per patient)
    user_ci VARCHAR(20) UNIQUE NOT NULL,

    -- Notification preferences (boolean flags)
    notify_access_request BOOLEAN DEFAULT true,   -- Notify when professional requests access
    notify_new_access BOOLEAN DEFAULT true,       -- Notify when someone accesses records
    notify_policy_change BOOLEAN DEFAULT true,    -- Notify when policies are modified
    notify_new_document BOOLEAN DEFAULT false,    -- Notify when new document is added

    -- Firebase Cloud Messaging token (for mobile push notifications)
    fcm_token VARCHAR(255),

    -- Timestamp
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- Index for notification_preferences
-- =============================================================================

-- Unique index on user_ci (already enforced by UNIQUE constraint, but explicit)
CREATE UNIQUE INDEX idx_notification_user_ci ON notification_preferences(user_ci);

-- =============================================================================
-- Table Comment
-- =============================================================================

COMMENT ON TABLE notification_preferences IS
'Patient notification preferences and FCM tokens for push notifications.
Used by NotificationService to determine which events to send to patients (CU04, CU07).';

-- =============================================================================
-- Grants (if needed for specific roles)
-- =============================================================================

-- Grant read-only access to audit_logs for audit reviewers
-- GRANT SELECT ON audit.audit_logs TO audit_reviewer_role;

-- Grant full access to notification_preferences for application
-- GRANT SELECT, INSERT, UPDATE ON notification_preferences TO hcen_app_role;

-- =============================================================================
-- End of Migration V004
-- =============================================================================
