-- =====================================================
-- SCRIPT DE CORRECCIÓN PARA TABLA USERS EXISTENTE
-- Proyecto: HCEN Componente Periférico de Clínica
-- =====================================================

-- Este script corrige la tabla users existente agregando las columnas faltantes

-- =====================================================
-- VERIFICAR ESTRUCTURA ACTUAL DE LA TABLA
-- =====================================================

DO $$
DECLARE
    column_count INTEGER;
    column_names TEXT;
BEGIN
    SELECT COUNT(*) INTO column_count 
    FROM information_schema.columns 
    WHERE table_name = 'users' AND table_schema = 'public';
    
    SELECT string_agg(column_name, ', ' ORDER BY ordinal_position) INTO column_names
    FROM information_schema.columns 
    WHERE table_name = 'users' AND table_schema = 'public';
    
    RAISE NOTICE 'Tabla users actual tiene % columnas:', column_count;
    RAISE NOTICE 'Columnas: %', column_names;
END $$;

-- =====================================================
-- AGREGAR COLUMNAS FALTANTES
-- =====================================================

-- Agregar columna first_name si no existe
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'first_name') THEN
        ALTER TABLE users ADD COLUMN first_name VARCHAR(100);
        RAISE NOTICE '✓ Columna first_name agregada';
    ELSE
        RAISE NOTICE '✓ Columna first_name ya existe';
    END IF;
END $$;

-- Agregar columna last_name si no existe
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'last_name') THEN
        ALTER TABLE users ADD COLUMN last_name VARCHAR(100);
        RAISE NOTICE '✓ Columna last_name agregada';
    ELSE
        RAISE NOTICE '✓ Columna last_name ya existe';
    END IF;
END $$;

-- Agregar columna last_login si no existe
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'last_login') THEN
        ALTER TABLE users ADD COLUMN last_login TIMESTAMP;
        RAISE NOTICE '✓ Columna last_login agregada';
    ELSE
        RAISE NOTICE '✓ Columna last_login ya existe';
    END IF;
END $$;

-- Agregar columna created_by si no existe
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'created_by') THEN
        ALTER TABLE users ADD COLUMN created_by BIGINT;
        RAISE NOTICE '✓ Columna created_by agregada';
    ELSE
        RAISE NOTICE '✓ Columna created_by ya existe';
    END IF;
END $$;

-- =====================================================
-- AGREGAR CLAVE FORÁNEA PARA created_by
-- =====================================================

-- Agregar foreign key para created_by si no existe
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE table_name = 'users' AND constraint_name = 'fk_users_created_by') THEN
        ALTER TABLE users ADD CONSTRAINT fk_users_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id);
        RAISE NOTICE '✓ Foreign key created_by agregada';
    ELSE
        RAISE NOTICE '✓ Foreign key created_by ya existe';
    END IF;
END $$;

-- =====================================================
-- ACTUALIZAR COMENTARIOS DE COLUMNAS
-- =====================================================

-- Agregar comentarios a las nuevas columnas
COMMENT ON COLUMN users.first_name IS 'Nombre del usuario';
COMMENT ON COLUMN users.last_name IS 'Apellido del usuario';
COMMENT ON COLUMN users.last_login IS 'Fecha y hora del último login';
COMMENT ON COLUMN users.created_by IS 'Usuario que creó este registro';

-- =====================================================
-- VERIFICAR ÍNDICES
-- =====================================================

-- Crear índices si no existen
CREATE INDEX IF NOT EXISTS idx_users_first_name ON users(first_name);
CREATE INDEX IF NOT EXISTS idx_users_last_name ON users(last_name);
CREATE INDEX IF NOT EXISTS idx_users_last_login ON users(last_login);
CREATE INDEX IF NOT EXISTS idx_users_created_by ON users(created_by);

-- =====================================================
-- VERIFICAR TRIGGER DE updated_at
-- =====================================================

-- Crear trigger si no existe
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.triggers 
                   WHERE trigger_name = 'trigger_users_updated_at') THEN
        CREATE TRIGGER trigger_users_updated_at
            BEFORE UPDATE ON users
            FOR EACH ROW
            EXECUTE FUNCTION update_users_updated_at();
        RAISE NOTICE '✓ Trigger updated_at agregado';
    ELSE
        RAISE NOTICE '✓ Trigger updated_at ya existe';
    END IF;
END $$;

-- =====================================================
-- VERIFICAR FUNCIÓN update_users_updated_at
-- =====================================================

-- Crear función si no existe
CREATE OR REPLACE FUNCTION update_users_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- VERIFICAR VISTA users_with_clinic
-- =====================================================

-- Crear vista si no existe
CREATE OR REPLACE VIEW users_with_clinic AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.first_name,
    u.last_name,
    u.role,
    u.clinic_id,
    c.name as clinic_name,
    u.professional_id,
    p.first_name as professional_first_name,
    p.last_name as professional_last_name,
    u.active,
    u.last_login,
    u.created_at,
    u.updated_at
FROM users u
LEFT JOIN clinics c ON u.clinic_id = c.id
LEFT JOIN professionals p ON u.professional_id = p.id;

COMMENT ON VIEW users_with_clinic IS 'Vista que incluye información completa de usuarios con datos de clínica y profesional';

-- =====================================================
-- VERIFICACIÓN FINAL
-- =====================================================

DO $$
DECLARE
    column_count INTEGER;
    column_names TEXT;
BEGIN
    SELECT COUNT(*) INTO column_count 
    FROM information_schema.columns 
    WHERE table_name = 'users' AND table_schema = 'public';
    
    SELECT string_agg(column_name, ', ' ORDER BY ordinal_position) INTO column_names
    FROM information_schema.columns 
    WHERE table_name = 'users' AND table_schema = 'public';
    
    RAISE NOTICE '';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'CORRECCIÓN COMPLETADA';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Tabla users ahora tiene % columnas:', column_count;
    RAISE NOTICE 'Columnas: %', column_names;
    RAISE NOTICE '========================================';
END $$;
