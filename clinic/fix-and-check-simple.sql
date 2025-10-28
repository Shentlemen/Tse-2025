-- =====================================================
-- SCRIPT DE VERIFICACIÓN Y CORRECCIÓN SIMPLE
-- Proyecto: HCEN Componente Periférico de Clínica
-- =====================================================

-- Primero, verificar qué columnas existen actualmente
SELECT 'Columnas actuales en tabla users:' as info;
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'users' AND table_schema = 'public'
ORDER BY ordinal_position;

-- Agregar columnas faltantes una por una
DO $$
BEGIN
    -- Agregar first_name si no existe
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'first_name') THEN
        ALTER TABLE users ADD COLUMN first_name VARCHAR(100);
        RAISE NOTICE '✓ Columna first_name agregada';
    ELSE
        RAISE NOTICE '✓ Columna first_name ya existe';
    END IF;
    
    -- Agregar last_name si no existe
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'last_name') THEN
        ALTER TABLE users ADD COLUMN last_name VARCHAR(100);
        RAISE NOTICE '✓ Columna last_name agregada';
    ELSE
        RAISE NOTICE '✓ Columna last_name ya existe';
    END IF;
    
    -- Agregar last_login si no existe
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'last_login') THEN
        ALTER TABLE users ADD COLUMN last_login TIMESTAMP;
        RAISE NOTICE '✓ Columna last_login agregada';
    ELSE
        RAISE NOTICE '✓ Columna last_login ya existe';
    END IF;
    
    -- Agregar created_by si no existe
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'created_by') THEN
        ALTER TABLE users ADD COLUMN created_by BIGINT;
        RAISE NOTICE '✓ Columna created_by agregada';
    ELSE
        RAISE NOTICE '✓ Columna created_by ya existe';
    END IF;
END $$;

-- Verificar columnas después de la corrección
SELECT 'Columnas después de la corrección:' as info;
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'users' AND table_schema = 'public'
ORDER BY ordinal_position;

-- Verificar si hay usuarios en la tabla
SELECT 'Usuarios en la tabla:' as info;
SELECT COUNT(*) as total_usuarios FROM users;

-- Si no hay usuarios, mostrar mensaje
DO $$
DECLARE
    user_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    
    IF user_count = 0 THEN
        RAISE NOTICE '';
        RAISE NOTICE '========================================';
        RAISE NOTICE 'NO HAY USUARIOS EN LA TABLA';
        RAISE NOTICE '========================================';
        RAISE NOTICE 'Debe ejecutar el script initialize-users-system.sql';
        RAISE NOTICE 'para crear los usuarios por defecto.';
        RAISE NOTICE '========================================';
    ELSE
        RAISE NOTICE '';
        RAISE NOTICE '========================================';
        RAISE NOTICE 'TABLA CORREGIDA EXITOSAMENTE';
        RAISE NOTICE '========================================';
        RAISE NOTICE 'Total usuarios: %', user_count;
        RAISE NOTICE 'Ahora puede ejecutar initialize-users-system.sql';
        RAISE NOTICE '========================================';
    END IF;
END $$;
