-- Migration script for refresh_tokens table
-- This table stores refresh tokens for JWT token rotation
-- Database: PostgreSQL

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token_hash VARCHAR(128) UNIQUE NOT NULL,
    user_ci VARCHAR(20) NOT NULL,
    client_type VARCHAR(20) NOT NULL,
    device_id VARCHAR(128),
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_refresh_tokens_user_ci ON refresh_tokens(user_ci);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_is_revoked ON refresh_tokens(is_revoked);

-- Comments
COMMENT ON TABLE refresh_tokens IS 'Stores refresh tokens for JWT authentication with token rotation';
COMMENT ON COLUMN refresh_tokens.token_hash IS 'SHA-256 hash of the refresh token (never store plain tokens)';
COMMENT ON COLUMN refresh_tokens.user_ci IS 'User CI (Cédula de Identidad) - references inus_users.ci';
COMMENT ON COLUMN refresh_tokens.client_type IS 'Client type: MOBILE, WEB_PATIENT, WEB_ADMIN';
COMMENT ON COLUMN refresh_tokens.device_id IS 'Optional device identifier for mobile clients';
COMMENT ON COLUMN refresh_tokens.issued_at IS 'When the token was issued';
COMMENT ON COLUMN refresh_tokens.expires_at IS 'When the token expires';
COMMENT ON COLUMN refresh_tokens.revoked_at IS 'When the token was revoked (NULL if not revoked)';
COMMENT ON COLUMN refresh_tokens.is_revoked IS 'Whether the token has been revoked';
