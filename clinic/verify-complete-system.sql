-- =====================================================
-- VERIFICACIÓN COMPLETA DEL SISTEMA DE USUARIOS
-- Proyecto: HCEN Componente Periférico de Clínica
-- =====================================================

-- Este script verifica que todo el sistema de usuarios esté funcionando correctamente

-- =====================================================
-- 1. VERIFICACIÓN DE ESTRUCTURA DE BASE DE DATOS
-- =====================================================

SELECT 'VERIFICACIÓN DE ESTRUCTURA' as seccion;

-- Verificar que la tabla users existe y tiene todas las columnas
SELECT 
    'Tabla users' as elemento,
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') 
        THEN '✓ EXISTE' 
        ELSE '✗ NO EXISTE' 
    END as estado;

-- Contar columnas de la tabla users
SELECT 
    'Columnas en users' as elemento,
    COUNT(*)::text || ' columnas' as estado
FROM information_schema.columns 
WHERE table_name = 'users' AND table_schema = 'public';

-- Listar todas las columnas de users
SELECT 
    'Columnas disponibles' as elemento,
    string_agg(column_name, ', ' ORDER BY ordinal_position) as estado
FROM information_schema.columns 
WHERE table_name = 'users' AND table_schema = 'public';

-- =====================================================
-- 2. VERIFICACIÓN DE DATOS
-- =====================================================

SELECT 'VERIFICACIÓN DE DATOS' as seccion;

-- Contar usuarios totales
SELECT 
    'Total usuarios' as elemento,
    COUNT(*)::text as estado
FROM users;

-- Contar usuarios activos
SELECT 
    'Usuarios activos' as elemento,
    COUNT(*)::text as estado
FROM users WHERE active = true;

-- Contar por roles
SELECT 
    'Usuarios por rol' as elemento,
    role || ': ' || COUNT(*)::text as estado
FROM users 
WHERE active = true
GROUP BY role
ORDER BY role;

-- =====================================================
-- 3. VERIFICACIÓN DE USUARIOS ESPECÍFICOS
-- =====================================================

SELECT 'VERIFICACIÓN DE USUARIOS ESPECÍFICOS' as seccion;

-- Verificar super administrador
SELECT 
    'Super administrador' as elemento,
    CASE 
        WHEN EXISTS (SELECT 1 FROM users WHERE username = 'superadmin' AND role = 'SUPER_ADMIN' AND active = true)
        THEN '✓ EXISTE (superadmin)'
        ELSE '✗ NO EXISTE'
    END as estado;

-- Verificar usuarios de clínicas
SELECT 
    'Usuarios clínicas' as elemento,
    CASE 
        WHEN EXISTS (SELECT 1 FROM users WHERE username IN ('admin', 'prof', 'admin2', 'prof2') AND active = true)
        THEN '✓ EXISTEN usuarios de clínicas'
        ELSE '✗ NO EXISTEN'
    END as estado;

-- =====================================================
-- 4. VERIFICACIÓN DE INTEGRIDAD
-- =====================================================

SELECT 'VERIFICACIÓN DE INTEGRIDAD' as seccion;

-- Verificar usuarios con clínicas válidas
SELECT 
    'Clínicas válidas' as elemento,
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ TODAS válidas'
        ELSE '✗ ' || COUNT(*)::text || ' usuarios con clínicas inválidas'
    END as estado
FROM users u 
WHERE u.clinic_id IS NOT NULL 
AND NOT EXISTS (SELECT 1 FROM clinics c WHERE c.id = u.clinic_id);

-- Verificar usuarios con profesionales válidos
SELECT 
    'Profesionales válidos' as elemento,
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ TODOS válidos'
        ELSE '✗ ' || COUNT(*)::text || ' usuarios con profesionales inválidos'
    END as estado
FROM users u 
WHERE u.professional_id IS NOT NULL 
AND NOT EXISTS (SELECT 1 FROM professionals p WHERE p.id = u.professional_id);

-- =====================================================
-- 5. VERIFICACIÓN DE FUNCIONES Y TRIGGERS
-- =====================================================

SELECT 'VERIFICACIÓN DE FUNCIONES Y TRIGGERS' as seccion;

-- Verificar función has_users
SELECT 
    'Función has_users' as elemento,
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.routines WHERE routine_name = 'has_users')
        THEN '✓ EXISTE'
        ELSE '✗ NO EXISTE'
    END as estado;

-- Verificar función get_system_info
SELECT 
    'Función get_system_info' as elemento,
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.routines WHERE routine_name = 'get_system_info')
        THEN '✓ EXISTE'
        ELSE '✗ NO EXISTE'
    END as estado;

-- Verificar trigger
SELECT 
    'Trigger updated_at' as elemento,
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.triggers WHERE trigger_name = 'trigger_users_updated_at')
        THEN '✓ EXISTE'
        ELSE '✗ NO EXISTE'
    END as estado;

-- =====================================================
-- 6. INFORMACIÓN DETALLADA DE USUARIOS
-- =====================================================

SELECT 'INFORMACIÓN DETALLADA DE USUARIOS' as seccion;

-- Mostrar todos los usuarios con información completa
SELECT 
    u.id,
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
    END as last_login,
    to_char(u.created_at, 'DD/MM/YYYY HH24:MI') as created_at
FROM users u
LEFT JOIN clinics c ON u.clinic_id = c.id
ORDER BY u.role, u.username;

-- =====================================================
-- 7. VERIFICACIÓN DE CONTRASEÑAS
-- =====================================================

SELECT 'VERIFICACIÓN DE CONTRASEÑAS' as seccion;

-- Verificar que las contraseñas están hasheadas (no son texto plano)
SELECT 
    'Contraseñas hasheadas' as elemento,
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ TODAS hasheadas'
        ELSE '✗ ' || COUNT(*)::text || ' contraseñas en texto plano'
    END as estado
FROM users 
WHERE password LIKE 'admin%' OR password LIKE 'prof%' OR LENGTH(password) < 20;

-- =====================================================
-- 8. RESUMEN FINAL
-- =====================================================

SELECT 'RESUMEN FINAL' as seccion;

-- Ejecutar función get_system_info si existe
DO $$
DECLARE
    rec RECORD;
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.routines WHERE routine_name = 'get_system_info') THEN
        SELECT * INTO rec FROM get_system_info();
        
        RAISE NOTICE '========================================';
        RAISE NOTICE 'RESUMEN DEL SISTEMA';
        RAISE NOTICE '========================================';
        RAISE NOTICE 'Total usuarios: %', rec.total_users;
        RAISE NOTICE 'Super administradores: %', rec.super_admins;
        RAISE NOTICE 'Administradores de clínicas: %', rec.clinic_admins;
        RAISE NOTICE 'Profesionales: %', rec.professionals;
        RAISE NOTICE 'Clínicas activas: %', rec.active_clinics;
        RAISE NOTICE '========================================';
    END IF;
END $$;

-- Verificar si el sistema está listo
SELECT 
    'Sistema listo' as elemento,
    CASE 
        WHEN EXISTS (SELECT 1 FROM users WHERE active = true) 
        AND EXISTS (SELECT 1 FROM users WHERE role = 'SUPER_ADMIN' AND active = true)
        THEN '✓ SÍ - Puede iniciar sesión'
        ELSE '✗ NO - Ejecute scripts de inicialización'
    END as estado;

-- =====================================================
-- 9. CREDENCIALES DE ACCESO
-- =====================================================

SELECT 'CREDENCIALES DE ACCESO' as seccion;

-- Mostrar usuarios disponibles para login
SELECT 
    'Usuarios para login' as elemento,
    username || ' (' || role || ')' as estado
FROM users 
WHERE active = true
ORDER BY role, username;

-- Mensaje final
SELECT '========================================' as mensaje;
SELECT 'VERIFICACIÓN COMPLETADA' as mensaje;
SELECT '========================================' as mensaje;
SELECT 'Si todo está ✓, el sistema está listo para usar' as mensaje;
SELECT 'Usuarios disponibles:' as mensaje;
SELECT '- superadmin (SUPER_ADMIN) - contraseña: admin123' as mensaje;
SELECT '- admin (ADMIN_CLINIC) - contraseña: admin123' as mensaje;
SELECT '- prof (PROFESSIONAL) - contraseña: admin123' as mensaje;
SELECT '- admin2 (ADMIN_CLINIC) - contraseña: admin123' as mensaje;
SELECT '- prof2 (PROFESSIONAL) - contraseña: admin123' as mensaje;
SELECT '========================================' as mensaje;
