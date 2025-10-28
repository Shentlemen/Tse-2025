-- =====================================================
-- SCRIPT DE VERIFICACIÓN DEL SISTEMA DE USUARIOS
-- Proyecto: HCEN Componente Periférico de Clínica
-- =====================================================

-- Este script verifica que el sistema de usuarios esté funcionando correctamente

-- =====================================================
-- VERIFICACIÓN DE ESTRUCTURA DE BASE DE DATOS
-- =====================================================

DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE 'VERIFICACIÓN DEL SISTEMA DE USUARIOS';
    RAISE NOTICE '========================================';
    RAISE NOTICE '';
END $$;

-- Verificar que la tabla users existe
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') THEN
        RAISE NOTICE '✓ Tabla users existe';
    ELSE
        RAISE NOTICE '✗ ERROR: Tabla users NO existe';
    END IF;
END $$;

-- Verificar columnas de la tabla users
DO $$
DECLARE
    column_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO column_count 
    FROM information_schema.columns 
    WHERE table_name = 'users' AND table_schema = 'public';
    
    IF column_count >= 10 THEN
        RAISE NOTICE '✓ Tabla users tiene % columnas (esperado: 10+)', column_count;
    ELSE
        RAISE NOTICE '✗ ERROR: Tabla users tiene solo % columnas', column_count;
    END IF;
END $$;

-- =====================================================
-- VERIFICACIÓN DE DATOS
-- =====================================================

-- Verificar si hay usuarios en el sistema
DO $$
DECLARE
    user_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    
    IF user_count > 0 THEN
        RAISE NOTICE '✓ Sistema tiene % usuarios', user_count;
    ELSE
        RAISE NOTICE '✗ ERROR: Sistema NO tiene usuarios';
    END IF;
END $$;

-- Verificar super administrador
DO $$
DECLARE
    super_admin_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO super_admin_count 
    FROM users 
    WHERE role = 'SUPER_ADMIN' AND active = true;
    
    IF super_admin_count > 0 THEN
        RAISE NOTICE '✓ Sistema tiene % super administrador(es)', super_admin_count;
    ELSE
        RAISE NOTICE '✗ ERROR: Sistema NO tiene super administrador';
    END IF;
END $$;

-- Verificar usuarios de clínicas
DO $$
DECLARE
    clinic_admin_count INTEGER;
    professional_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO clinic_admin_count 
    FROM users 
    WHERE role = 'ADMIN_CLINIC' AND active = true;
    
    SELECT COUNT(*) INTO professional_count 
    FROM users 
    WHERE role = 'PROFESSIONAL' AND active = true;
    
    RAISE NOTICE '✓ Administradores de clínicas: %', clinic_admin_count;
    RAISE NOTICE '✓ Profesionales: %', professional_count;
END $$;

-- =====================================================
-- VERIFICACIÓN DE INTEGRIDAD
-- =====================================================

-- Verificar usuarios con clínicas válidas
DO $$
DECLARE
    invalid_clinic_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO invalid_clinic_count 
    FROM users u 
    WHERE u.clinic_id IS NOT NULL 
    AND NOT EXISTS (SELECT 1 FROM clinics c WHERE c.id = u.clinic_id);
    
    IF invalid_clinic_count = 0 THEN
        RAISE NOTICE '✓ Todos los usuarios tienen clínicas válidas';
    ELSE
        RAISE NOTICE '✗ ERROR: % usuarios tienen clínicas inválidas', invalid_clinic_count;
    END IF;
END $$;

-- Verificar usuarios con profesionales válidos
DO $$
DECLARE
    invalid_professional_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO invalid_professional_count 
    FROM users u 
    WHERE u.professional_id IS NOT NULL 
    AND NOT EXISTS (SELECT 1 FROM professionals p WHERE p.id = u.professional_id);
    
    IF invalid_professional_count = 0 THEN
        RAISE NOTICE '✓ Todos los usuarios tienen profesionales válidos';
    ELSE
        RAISE NOTICE '✗ ERROR: % usuarios tienen profesionales inválidos', invalid_professional_count;
    END IF;
END $$;

-- =====================================================
-- MOSTRAR RESUMEN DE USUARIOS
-- =====================================================

DO $$
DECLARE
    rec RECORD;
BEGIN
    RAISE NOTICE '';
    RAISE NOTICE 'RESUMEN DE USUARIOS:';
    RAISE NOTICE '----------------------------------------';
    
    FOR rec IN 
        SELECT 
            u.username,
            u.role,
            u.active,
            CASE 
                WHEN u.clinic_id IS NOT NULL THEN c.name
                ELSE 'Sistema'
            END as clinic_name,
            CASE 
                WHEN u.last_login IS NOT NULL THEN to_char(u.last_login, 'DD/MM/YYYY HH24:MI')
                ELSE 'Nunca'
            END as last_login
        FROM users u
        LEFT JOIN clinics c ON u.clinic_id = c.id
        ORDER BY u.role, u.username
    LOOP
        RAISE NOTICE 'Usuario: % | Rol: % | Activo: % | Clínica: % | Último login: %', 
                     rec.username, rec.role, rec.active, rec.clinic_name, rec.last_login;
    END LOOP;
    
    RAISE NOTICE '----------------------------------------';
END $$;

-- =====================================================
-- VERIFICACIÓN DE FUNCIONES
-- =====================================================

-- Verificar función has_users()
DO $$
DECLARE
    has_users_result BOOLEAN;
BEGIN
    SELECT has_users() INTO has_users_result;
    
    IF has_users_result THEN
        RAISE NOTICE '✓ Función has_users() funciona correctamente';
    ELSE
        RAISE NOTICE '✗ ERROR: Función has_users() no funciona';
    END IF;
END $$;

-- Verificar función get_system_info()
DO $$
DECLARE
    rec RECORD;
BEGIN
    SELECT * INTO rec FROM get_system_info();
    
    RAISE NOTICE '';
    RAISE NOTICE 'INFORMACIÓN DEL SISTEMA:';
    RAISE NOTICE '----------------------------------------';
    RAISE NOTICE 'Total usuarios: %', rec.total_users;
    RAISE NOTICE 'Super administradores: %', rec.super_admins;
    RAISE NOTICE 'Administradores de clínicas: %', rec.clinic_admins;
    RAISE NOTICE 'Profesionales: %', rec.professionals;
    RAISE NOTICE 'Clínicas activas: %', rec.active_clinics;
    RAISE NOTICE '----------------------------------------';
END $$;

-- =====================================================
-- VERIFICACIÓN DE CONTRASEÑAS
-- =====================================================

-- Verificar que las contraseñas están hasheadas (no son texto plano)
DO $$
DECLARE
    plain_password_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO plain_password_count 
    FROM users 
    WHERE password LIKE 'admin%' OR password LIKE 'prof%';
    
    IF plain_password_count = 0 THEN
        RAISE NOTICE '✓ Todas las contraseñas están hasheadas';
    ELSE
        RAISE NOTICE '✗ ERROR: % contraseñas parecen estar en texto plano', plain_password_count;
    END IF;
END $$;

-- =====================================================
-- RESULTADO FINAL
-- =====================================================

DO $$
DECLARE
    system_ready BOOLEAN;
    user_count INTEGER;
    super_admin_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users WHERE active = true;
    SELECT COUNT(*) INTO super_admin_count FROM users WHERE role = 'SUPER_ADMIN' AND active = true;
    
    system_ready := (user_count > 0 AND super_admin_count > 0);
    
    RAISE NOTICE '';
    RAISE NOTICE '========================================';
    IF system_ready THEN
        RAISE NOTICE '✓ SISTEMA LISTO PARA USAR';
        RAISE NOTICE '✓ Puede iniciar sesión con los usuarios creados';
    ELSE
        RAISE NOTICE '✗ SISTEMA NO ESTÁ LISTO';
        RAISE NOTICE '✗ Ejecute el script de inicialización';
    END IF;
    RAISE NOTICE '========================================';
END $$;
