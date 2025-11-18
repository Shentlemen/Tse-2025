-- Migration: Add HCEN integration fields to Clinic table
-- Add API key and JMS URL fields for HCEN communication

ALTER TABLE clinics 
    ADD COLUMN IF NOT EXISTS hcen_jms_url VARCHAR(255),
    ADD COLUMN IF NOT EXISTS api_key VARCHAR(255);

-- Add comments
COMMENT ON COLUMN clinics.hcen_jms_url IS 'URL para conexión JMS con HCEN (ej: http-remoting://localhost:8080)';
COMMENT ON COLUMN clinics.api_key IS 'API key para autenticación REST con HCEN';

-- Migration: Add external document fields to ClinicalDocument table
-- Add fields to track documents downloaded from other clinics

ALTER TABLE clinical_documents
    ADD COLUMN IF NOT EXISTS is_external BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS source_clinic_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS external_clinic_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS external_document_locator TEXT;

-- Add comments
COMMENT ON COLUMN clinical_documents.is_external IS 'Indica si es un documento descargado de otra clínica';
COMMENT ON COLUMN clinical_documents.source_clinic_id IS 'ID de la clínica origen (si es documento externo)';
COMMENT ON COLUMN clinical_documents.external_clinic_name IS 'Nombre de la clínica origen (si es documento externo)';
COMMENT ON COLUMN clinical_documents.external_document_locator IS 'URL original del documento externo';

-- Migration: Create/Update AccessRequest table for access requests to external documents
-- This script handles both creating the table from scratch and adding missing columns if it already exists

-- Create table if it doesn't exist
CREATE TABLE IF NOT EXISTS access_requests (
    id BIGSERIAL PRIMARY KEY,
    patient_ci VARCHAR(100) NOT NULL,
    document_id BIGINT,
    specialties TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    request_reason TEXT NOT NULL,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    responded_at TIMESTAMP,
    hcen_request_id VARCHAR(50),
    urgency VARCHAR(20) DEFAULT 'ROUTINE',
    professional_id BIGINT NOT NULL,
    clinic_id BIGINT NOT NULL
);

-- Add columns that might be missing (if table already existed)
DO $$
BEGIN
    -- Add patient_ci if it doesn't exist (might have been created with patient_id instead)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'access_requests' 
        AND column_name = 'patient_ci'
    ) THEN
        ALTER TABLE access_requests ADD COLUMN patient_ci VARCHAR(100);
        RAISE NOTICE 'Columna patient_ci agregada';
    END IF;
    
    -- Add document_id if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'access_requests' 
        AND column_name = 'document_id'
    ) THEN
        ALTER TABLE access_requests ADD COLUMN document_id BIGINT;
        RAISE NOTICE 'Columna document_id agregada';
    END IF;
    
    -- Add specialties if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'access_requests' 
        AND column_name = 'specialties'
    ) THEN
        ALTER TABLE access_requests ADD COLUMN specialties TEXT;
        RAISE NOTICE 'Columna specialties agregada';
    END IF;
    
    -- Add request_reason if it doesn't exist (might have been created as reason)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'access_requests' 
        AND column_name = 'request_reason'
    ) THEN
        ALTER TABLE access_requests ADD COLUMN request_reason TEXT;
        -- Copy data from 'reason' if it exists
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_schema = 'public' 
            AND table_name = 'access_requests' 
            AND column_name = 'reason'
        ) THEN
            EXECUTE 'UPDATE access_requests SET request_reason = reason WHERE request_reason IS NULL';
            RAISE NOTICE 'Datos copiados de reason a request_reason';
        END IF;
        RAISE NOTICE 'Columna request_reason agregada';
    END IF;
    
    -- Add hcen_request_id if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'access_requests' 
        AND column_name = 'hcen_request_id'
    ) THEN
        ALTER TABLE access_requests ADD COLUMN hcen_request_id VARCHAR(50);
        RAISE NOTICE 'Columna hcen_request_id agregada';
    END IF;
    
    -- Add urgency if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'access_requests' 
        AND column_name = 'urgency'
    ) THEN
        ALTER TABLE access_requests ADD COLUMN urgency VARCHAR(20) DEFAULT 'ROUTINE';
        RAISE NOTICE 'Columna urgency agregada';
    END IF;
    
    -- Ensure status has default value
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'access_requests' 
        AND column_name = 'status'
        AND column_default IS NULL
    ) THEN
        ALTER TABLE access_requests ALTER COLUMN status SET DEFAULT 'PENDING';
        RAISE NOTICE 'Default value agregado a status';
    END IF;
    
    -- Ensure requested_at has default value
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'access_requests' 
        AND column_name = 'requested_at'
        AND column_default IS NULL
    ) THEN
        ALTER TABLE access_requests ALTER COLUMN requested_at SET DEFAULT CURRENT_TIMESTAMP;
        RAISE NOTICE 'Default value agregado a requested_at';
    END IF;
END $$;

-- Add constraints (will fail silently if they already exist)
DO $$
BEGIN
    -- Foreign key for professional_id
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'fk_access_request_professional'
    ) THEN
        ALTER TABLE access_requests 
        ADD CONSTRAINT fk_access_request_professional 
        FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE CASCADE;
        RAISE NOTICE 'Foreign key fk_access_request_professional agregada';
    END IF;
    
    -- Foreign key for clinic_id
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'fk_access_request_clinic'
    ) THEN
        ALTER TABLE access_requests 
        ADD CONSTRAINT fk_access_request_clinic 
        FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE CASCADE;
        RAISE NOTICE 'Foreign key fk_access_request_clinic agregada';
    END IF;
    
    -- Check constraint for status
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'chk_access_request_status'
    ) THEN
        ALTER TABLE access_requests 
        ADD CONSTRAINT chk_access_request_status 
        CHECK (status IN ('PENDING', 'APPROVED', 'DENIED', 'EXPIRED'));
        RAISE NOTICE 'Check constraint chk_access_request_status agregada';
    END IF;
    
    -- Check constraint for urgency
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'chk_access_request_urgency'
    ) THEN
        ALTER TABLE access_requests 
        ADD CONSTRAINT chk_access_request_urgency 
        CHECK (urgency IN ('ROUTINE', 'URGENT', 'EMERGENCY'));
        RAISE NOTICE 'Check constraint chk_access_request_urgency agregada';
    END IF;
END $$;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_access_requests_professional_id ON access_requests(professional_id);
CREATE INDEX IF NOT EXISTS idx_access_requests_clinic_id ON access_requests(clinic_id);
CREATE INDEX IF NOT EXISTS idx_access_requests_patient_ci ON access_requests(patient_ci);
CREATE INDEX IF NOT EXISTS idx_access_requests_status ON access_requests(status);
CREATE INDEX IF NOT EXISTS idx_access_requests_hcen_request_id ON access_requests(hcen_request_id);
CREATE INDEX IF NOT EXISTS idx_access_requests_requested_at ON access_requests(requested_at);

-- Add comments
COMMENT ON TABLE access_requests IS 'Solicitudes de acceso a documentos externos de otras clínicas';
COMMENT ON COLUMN access_requests.patient_ci IS 'Cédula del paciente';
COMMENT ON COLUMN access_requests.document_id IS 'ID del documento específico (opcional)';
COMMENT ON COLUMN access_requests.specialties IS 'Especialidades solicitadas (JSON array o comma-separated)';
COMMENT ON COLUMN access_requests.status IS 'Estado de la solicitud: PENDING, APPROVED, DENIED, EXPIRED';
COMMENT ON COLUMN access_requests.hcen_request_id IS 'ID retornado por HCEN';
COMMENT ON COLUMN access_requests.urgency IS 'Urgencia: ROUTINE, URGENT, EMERGENCY';

