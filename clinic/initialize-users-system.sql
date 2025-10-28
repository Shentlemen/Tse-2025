-- =====================================================
-- SCRIPT DE INICIALIZACIÓN DEL SISTEMA DE USUARIOS
-- Proyecto: HCEN Componente Periférico de Clínica
-- =====================================================

-- Este script debe ejecutarse DESPUÉS de crear la tabla de usuarios
-- y antes de usar el sistema por primera vez.

-- =====================================================
-- VERIFICACIÓN DE ESTADO DEL SISTEMA
-- =====================================================

-- Verificar si ya existen usuarios
DO $$
DECLARE
    user_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    
    IF user_count > 0 THEN
        RAISE NOTICE 'El sistema ya tiene % usuarios configurados.', user_count;
        RAISE NOTICE 'No es necesario ejecutar este script de inicialización.';
    ELSE
        RAISE NOTICE 'Sistema sin usuarios. Procediendo con la inicialización...';
    END IF;
END $$;

-- =====================================================
-- CREACIÓN DEL PRIMER SUPER ADMINISTRADOR
-- =====================================================

-- Solo crear el super admin si no existe ningún usuario
INSERT INTO users (username, password, email, first_name, last_name, role, clinic_id, active) 
SELECT 
    'superadmin', 
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- admin123
    'admin@hcen.uy',
    'Super',
    'Administrador',
    'SUPER_ADMIN',
    NULL,
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'superadmin');

-- =====================================================
-- CREACIÓN DE USUARIOS PARA CLÍNICAS EXISTENTES
-- =====================================================

-- Solo crear usuarios si no existen usuarios en el sistema
-- Clínica del Corazón (ID 4)
INSERT INTO users (username, password, email, first_name, last_name, role, clinic_id, professional_id, active) 
SELECT 
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- admin123
    'admin@corazon.com',
    'Administrador',
    'Clínica del Corazón',
    'ADMIN_CLINIC',
    4,
    NULL,
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password, email, first_name, last_name, role, clinic_id, professional_id, active) 
SELECT 
    'prof',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- prof123
    'prof@corazon.com',
    'Profesional',
    'Clínica del Corazón',
    'PROFESSIONAL',
    4,
    (SELECT id FROM professionals WHERE clinic_id = 4 LIMIT 1), -- Primer profesional de la clínica
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'prof');

-- Centro Neurológico (ID 5)
INSERT INTO users (username, password, email, first_name, last_name, role, clinic_id, professional_id, active) 
SELECT 
    'admin2',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- admin456
    'admin@neurologico.com',
    'Administrador',
    'Centro Neurológico',
    'ADMIN_CLINIC',
    5,
    NULL,
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin2');

INSERT INTO users (username, password, email, first_name, last_name, role, clinic_id, professional_id, active) 
SELECT 
    'prof2',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- prof456
    'prof@neurologico.com',
    'Profesional',
    'Centro Neurológico',
    'PROFESSIONAL',
    5,
    (SELECT id FROM professionals WHERE clinic_id = 5 LIMIT 1), -- Primer profesional de la clínica
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'prof2');

-- =====================================================
-- VERIFICACIÓN FINAL
-- =====================================================

DO $$
DECLARE
    user_count INTEGER;
    super_admin_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    SELECT COUNT(*) INTO super_admin_count FROM users WHERE role = 'SUPER_ADMIN' AND active = true;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE 'INICIALIZACIÓN COMPLETADA';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Total de usuarios creados: %', user_count;
    RAISE NOTICE 'Super administradores: %', super_admin_count;
    RAISE NOTICE '';
    RAISE NOTICE 'USUARIOS DISPONIBLES:';
    RAISE NOTICE '----------------------------------------';
    
    -- Mostrar usuarios creados
    FOR rec IN 
        SELECT username, role, 
               CASE 
                   WHEN clinic_id IS NOT NULL THEN (SELECT name FROM clinics WHERE id = users.clinic_id)
                   ELSE 'Sistema'
               END as clinic_name
        FROM users 
        ORDER BY role, username
    LOOP
        RAISE NOTICE 'Usuario: % | Rol: % | Clínica: %', 
                     rec.username, rec.role, rec.clinic_name;
    END LOOP;
    
    RAISE NOTICE '----------------------------------------';
    RAISE NOTICE 'CONTRASEÑAS TEMPORALES:';
    RAISE NOTICE 'Todos los usuarios tienen contraseña: admin123';
    RAISE NOTICE 'IMPORTANTE: Cambiar las contraseñas después del primer login';
    RAISE NOTICE '========================================';
END $$;

-- =====================================================
-- FUNCIÓN DE UTILIDAD PARA VERIFICAR ESTADO
-- =====================================================

-- Función para verificar si el sistema está listo para usar
CREATE OR REPLACE FUNCTION is_system_ready() RETURNS BOOLEAN AS $$
BEGIN
    RETURN (SELECT COUNT(*) > 0 FROM users WHERE active = true);
END;
$$ LANGUAGE plpgsql;

-- Función para obtener información del sistema
CREATE OR REPLACE FUNCTION get_system_info() RETURNS TABLE(
    total_users INTEGER,
    super_admins INTEGER,
    clinic_admins INTEGER,
    professionals INTEGER,
    active_clinics INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        (SELECT COUNT(*)::INTEGER FROM users WHERE active = true) as total_users,
        (SELECT COUNT(*)::INTEGER FROM users WHERE role = 'SUPER_ADMIN' AND active = true) as super_admins,
        (SELECT COUNT(*)::INTEGER FROM users WHERE role = 'ADMIN_CLINIC' AND active = true) as clinic_admins,
        (SELECT COUNT(*)::INTEGER FROM users WHERE role = 'PROFESSIONAL' AND active = true) as professionals,
        (SELECT COUNT(*)::INTEGER FROM clinics WHERE active = true) as active_clinics;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- MENSAJE FINAL
-- =====================================================

DO $$
BEGIN
    RAISE NOTICE '';
    RAISE NOTICE 'El sistema está listo para usar.';
    RAISE NOTICE 'Puede iniciar sesión con cualquiera de los usuarios creados.';
    RAISE NOTICE '';
    RAISE NOTICE 'Para verificar el estado del sistema, ejecute:';
    RAISE NOTICE 'SELECT * FROM get_system_info();';
    RAISE NOTICE '';
END $$;
