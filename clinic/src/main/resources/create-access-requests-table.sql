-- ============================================================================
-- Script SQL para crear/actualizar la tabla access_requests
-- Base de datos: PostgreSQL
-- Fecha: 2024
-- ============================================================================

-- Verificar si la tabla existe, si no existe crearla
DO $$
BEGIN
    -- Crear la tabla si no existe
    IF NOT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'access_requests') THEN
        CREATE TABLE access_requests (
            id BIGSERIAL PRIMARY KEY,
            status VARCHAR(100) NOT NULL,
            reason VARCHAR(1000),
            response_notes VARCHAR(1000),
            requested_at TIMESTAMP NOT NULL,
            responded_at TIMESTAMP,
            expires_at TIMESTAMP,
            patient_id BIGINT NOT NULL,
            professional_id BIGINT NOT NULL,
            clinic_id BIGINT NOT NULL,
            specialty_id BIGINT
        );
        
        RAISE NOTICE 'Tabla access_requests creada exitosamente';
    ELSE
        RAISE NOTICE 'La tabla access_requests ya existe';
    END IF;
END $$;

-- Agregar la columna specialty_id si no existe (para tablas existentes)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'access_requests' 
        AND column_name = 'specialty_id'
    ) THEN
        ALTER TABLE access_requests ADD COLUMN specialty_id BIGINT;
        RAISE NOTICE 'Columna specialty_id agregada exitosamente';
    ELSE
        RAISE NOTICE 'La columna specialty_id ya existe';
    END IF;
END $$;

-- Eliminar foreign keys existentes si existen (para poder recrearlas)
DO $$
BEGIN
    -- Eliminar foreign key de patient_id si existe
    IF EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE constraint_schema = 'public' 
        AND table_name = 'access_requests' 
        AND constraint_name = 'fk_access_requests_patient'
    ) THEN
        ALTER TABLE access_requests DROP CONSTRAINT fk_access_requests_patient;
        RAISE NOTICE 'Foreign key fk_access_requests_patient eliminada';
    END IF;
    
    -- Eliminar foreign key de professional_id si existe
    IF EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE constraint_schema = 'public' 
        AND table_name = 'access_requests' 
        AND constraint_name = 'fk_access_requests_professional'
    ) THEN
        ALTER TABLE access_requests DROP CONSTRAINT fk_access_requests_professional;
        RAISE NOTICE 'Foreign key fk_access_requests_professional eliminada';
    END IF;
    
    -- Eliminar foreign key de clinic_id si existe
    IF EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE constraint_schema = 'public' 
        AND table_name = 'access_requests' 
        AND constraint_name = 'fk_access_requests_clinic'
    ) THEN
        ALTER TABLE access_requests DROP CONSTRAINT fk_access_requests_clinic;
        RAISE NOTICE 'Foreign key fk_access_requests_clinic eliminada';
    END IF;
    
    -- Eliminar foreign key de specialty_id si existe
    IF EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE constraint_schema = 'public' 
        AND table_name = 'access_requests' 
        AND constraint_name = 'fk_access_requests_specialty'
    ) THEN
        ALTER TABLE access_requests DROP CONSTRAINT fk_access_requests_specialty;
        RAISE NOTICE 'Foreign key fk_access_requests_specialty eliminada';
    END IF;
END $$;

-- Crear foreign keys
DO $$
BEGIN
    -- Foreign key para patient_id
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE constraint_schema = 'public' 
        AND table_name = 'access_requests' 
        AND constraint_name = 'fk_access_requests_patient'
    ) THEN
        ALTER TABLE access_requests 
        ADD CONSTRAINT fk_access_requests_patient 
        FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE;
        RAISE NOTICE 'Foreign key fk_access_requests_patient creada';
    END IF;
    
    -- Foreign key para professional_id
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE constraint_schema = 'public' 
        AND table_name = 'access_requests' 
        AND constraint_name = 'fk_access_requests_professional'
    ) THEN
        ALTER TABLE access_requests 
        ADD CONSTRAINT fk_access_requests_professional 
        FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE CASCADE;
        RAISE NOTICE 'Foreign key fk_access_requests_professional creada';
    END IF;
    
    -- Foreign key para clinic_id
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE constraint_schema = 'public' 
        AND table_name = 'access_requests' 
        AND constraint_name = 'fk_access_requests_clinic'
    ) THEN
        ALTER TABLE access_requests 
        ADD CONSTRAINT fk_access_requests_clinic 
        FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE CASCADE;
        RAISE NOTICE 'Foreign key fk_access_requests_clinic creada';
    END IF;
    
    -- Foreign key para specialty_id (opcional, puede ser NULL)
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE constraint_schema = 'public' 
        AND table_name = 'access_requests' 
        AND constraint_name = 'fk_access_requests_specialty'
    ) THEN
        ALTER TABLE access_requests 
        ADD CONSTRAINT fk_access_requests_specialty 
        FOREIGN KEY (specialty_id) REFERENCES specialties(id) ON DELETE SET NULL;
        RAISE NOTICE 'Foreign key fk_access_requests_specialty creada';
    END IF;
END $$;

-- Crear índices para mejorar el rendimiento de las consultas
DO $$
BEGIN
    -- Índice para búsquedas por profesional
    IF NOT EXISTS (
        SELECT 1 
        FROM pg_indexes 
        WHERE schemaname = 'public' 
        AND tablename = 'access_requests' 
        AND indexname = 'idx_access_requests_professional_id'
    ) THEN
        CREATE INDEX idx_access_requests_professional_id ON access_requests(professional_id);
        RAISE NOTICE 'Índice idx_access_requests_professional_id creado';
    END IF;
    
    -- Índice para búsquedas por paciente
    IF NOT EXISTS (
        SELECT 1 
        FROM pg_indexes 
        WHERE schemaname = 'public' 
        AND tablename = 'access_requests' 
        AND indexname = 'idx_access_requests_patient_id'
    ) THEN
        CREATE INDEX idx_access_requests_patient_id ON access_requests(patient_id);
        RAISE NOTICE 'Índice idx_access_requests_patient_id creado';
    END IF;
    
    -- Índice para búsquedas por estado
    IF NOT EXISTS (
        SELECT 1 
        FROM pg_indexes 
        WHERE schemaname = 'public' 
        AND tablename = 'access_requests' 
        AND indexname = 'idx_access_requests_status'
    ) THEN
        CREATE INDEX idx_access_requests_status ON access_requests(status);
        RAISE NOTICE 'Índice idx_access_requests_status creado';
    END IF;
    
    -- Índice compuesto para búsquedas por profesional y estado (usado en findByProfessionalAndStatus)
    IF NOT EXISTS (
        SELECT 1 
        FROM pg_indexes 
        WHERE schemaname = 'public' 
        AND tablename = 'access_requests' 
        AND indexname = 'idx_access_requests_professional_status'
    ) THEN
        CREATE INDEX idx_access_requests_professional_status ON access_requests(professional_id, status);
        RAISE NOTICE 'Índice idx_access_requests_professional_status creado';
    END IF;
    
    -- Índice para specialty_id (si se usa frecuentemente en consultas)
    IF NOT EXISTS (
        SELECT 1 
        FROM pg_indexes 
        WHERE schemaname = 'public' 
        AND tablename = 'access_requests' 
        AND indexname = 'idx_access_requests_specialty_id'
    ) THEN
        CREATE INDEX idx_access_requests_specialty_id ON access_requests(specialty_id);
        RAISE NOTICE 'Índice idx_access_requests_specialty_id creado';
    END IF;
    
    -- Índice para fecha de solicitud (para ordenar por fecha)
    IF NOT EXISTS (
        SELECT 1 
        FROM pg_indexes 
        WHERE schemaname = 'public' 
        AND tablename = 'access_requests' 
        AND indexname = 'idx_access_requests_requested_at'
    ) THEN
        CREATE INDEX idx_access_requests_requested_at ON access_requests(requested_at DESC);
        RAISE NOTICE 'Índice idx_access_requests_requested_at creado';
    END IF;
END $$;

-- Agregar comentarios a la tabla y columnas para documentación
COMMENT ON TABLE access_requests IS 'Tabla que almacena las solicitudes de acceso a documentos clínicos de pacientes en otras clínicas (HCEN)';
COMMENT ON COLUMN access_requests.id IS 'Identificador único de la solicitud';
COMMENT ON COLUMN access_requests.status IS 'Estado de la solicitud: PENDING, APPROVED, REJECTED, EXPIRED';
COMMENT ON COLUMN access_requests.reason IS 'Motivo de la solicitud de acceso';
COMMENT ON COLUMN access_requests.response_notes IS 'Notas de respuesta a la solicitud';
COMMENT ON COLUMN access_requests.requested_at IS 'Fecha y hora en que se realizó la solicitud';
COMMENT ON COLUMN access_requests.responded_at IS 'Fecha y hora en que se respondió la solicitud';
COMMENT ON COLUMN access_requests.expires_at IS 'Fecha y hora de expiración de la solicitud (si aplica)';
COMMENT ON COLUMN access_requests.patient_id IS 'ID del paciente para el cual se solicita acceso';
COMMENT ON COLUMN access_requests.professional_id IS 'ID del profesional que realiza la solicitud';
COMMENT ON COLUMN access_requests.clinic_id IS 'ID de la clínica del profesional que solicita';
COMMENT ON COLUMN access_requests.specialty_id IS 'ID de la especialidad sobre la cual se solicitan los documentos (NULL = todas las especialidades)';

-- Verificar que todo se creó correctamente
DO $$
DECLARE
    table_exists BOOLEAN;
    column_count INTEGER;
    constraint_count INTEGER;
    index_count INTEGER;
BEGIN
    -- Verificar tabla
    SELECT EXISTS (
        SELECT FROM pg_tables 
        WHERE schemaname = 'public' 
        AND tablename = 'access_requests'
    ) INTO table_exists;
    
    -- Contar columnas
    SELECT COUNT(*) INTO column_count
    FROM information_schema.columns
    WHERE table_schema = 'public' 
    AND table_name = 'access_requests';
    
    -- Contar constraints
    SELECT COUNT(*) INTO constraint_count
    FROM information_schema.table_constraints
    WHERE constraint_schema = 'public' 
    AND table_name = 'access_requests'
    AND constraint_type = 'FOREIGN KEY';
    
    -- Contar índices
    SELECT COUNT(*) INTO index_count
    FROM pg_indexes
    WHERE schemaname = 'public' 
    AND tablename = 'access_requests';
    
    RAISE NOTICE '========================================';
    RAISE NOTICE 'RESUMEN DE LA CREACIÓN:';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Tabla existe: %', table_exists;
    RAISE NOTICE 'Número de columnas: %', column_count;
    RAISE NOTICE 'Número de foreign keys: %', constraint_count;
    RAISE NOTICE 'Número de índices: %', index_count;
    RAISE NOTICE '========================================';
END $$;

