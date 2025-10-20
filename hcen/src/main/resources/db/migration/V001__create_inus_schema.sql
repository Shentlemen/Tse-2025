-- ================================================================
-- HCEN - INUS (Índice Nacional de Usuarios de Salud) Schema
-- ================================================================
-- Description: National registry of all health system users
-- Version: V001
-- Author: TSE 2025 Group 9
-- ================================================================

-- Create INUS schema if not exists
CREATE SCHEMA IF NOT EXISTS inus;

-- Create inus_users table
CREATE TABLE IF NOT EXISTS inus.inus_users (
    -- Primary identification
    ci VARCHAR(20) PRIMARY KEY,

    -- Unique cross-clinic identifier
    inus_id VARCHAR(50) UNIQUE NOT NULL,

    -- Personal information
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,

    -- Contact information
    email VARCHAR(255),
    phone_number VARCHAR(20),

    -- User status with constraint
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),

    -- Age verification flag (for PDI integration)
    age_verified BOOLEAN DEFAULT false,

    -- Audit timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_inus_users_ci ON inus.inus_users(ci);
CREATE INDEX IF NOT EXISTS idx_inus_users_inus_id ON inus.inus_users(inus_id);
CREATE INDEX IF NOT EXISTS idx_inus_users_status ON inus.inus_users(status);
CREATE INDEX IF NOT EXISTS idx_inus_users_email ON inus.inus_users(email);
CREATE INDEX IF NOT EXISTS idx_inus_users_created_at ON inus.inus_users(created_at);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION inus.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_inus_users_updated_at
    BEFORE UPDATE ON inus.inus_users
    FOR EACH ROW
    EXECUTE FUNCTION inus.update_updated_at_column();

-- Add comments for documentation
COMMENT ON SCHEMA inus IS 'National health system user index schema';
COMMENT ON TABLE inus.inus_users IS 'Central registry of all health system users with cross-clinic identification';
COMMENT ON COLUMN inus.inus_users.ci IS 'Cédula de Identidad (national ID) - primary identifier';
COMMENT ON COLUMN inus.inus_users.inus_id IS 'Unique cross-clinic identifier (UUID-based)';
COMMENT ON COLUMN inus.inus_users.status IS 'User status: ACTIVE, INACTIVE, or SUSPENDED';
COMMENT ON COLUMN inus.inus_users.age_verified IS 'Flag indicating if age >= 18 was verified via PDI';
COMMENT ON COLUMN inus.inus_users.created_at IS 'Timestamp of user registration';
COMMENT ON COLUMN inus.inus_users.updated_at IS 'Timestamp of last update (auto-updated via trigger)';
