-- =====================================================
-- VERIFICACIÓN SIMPLE DEL SISTEMA DE USUARIOS
-- =====================================================

-- 1. Verificar estructura de la tabla
SELECT 'ESTRUCTURA DE LA TABLA USERS:' as verificacion;
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'users' AND table_schema = 'public'
ORDER BY ordinal_position;

-- 2. Contar usuarios
SELECT 'TOTAL DE USUARIOS:' as verificacion, COUNT(*) as cantidad FROM users;

-- 3. Mostrar usuarios activos
SELECT 'USUARIOS ACTIVOS:' as verificacion;
SELECT username, role, active, clinic_id 
FROM users 
WHERE active = true
ORDER BY role, username;

-- 4. Verificar super administrador
SELECT 'SUPER ADMINISTRADOR:' as verificacion;
SELECT username, role, active 
FROM users 
WHERE role = 'SUPER_ADMIN' AND active = true;

-- 5. Verificar contraseñas (deben estar hasheadas)
SELECT 'VERIFICACIÓN DE CONTRASEÑAS:' as verificacion;
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN 'TODAS HASHEADAS ✓'
        ELSE 'HAY ' || COUNT(*) || ' CONTRASEÑAS EN TEXTO PLANO ✗'
    END as estado
FROM users 
WHERE password LIKE 'admin%' OR password LIKE 'prof%' OR LENGTH(password) < 20;

-- 6. Mostrar información completa de usuarios
SELECT 'INFORMACIÓN COMPLETA DE USUARIOS:' as verificacion;
SELECT 
    id,
    username,
    role,
    active,
    clinic_id,
    CASE 
        WHEN last_login IS NOT NULL THEN to_char(last_login, 'DD/MM/YYYY HH24:MI')
        ELSE 'Nunca'
    END as ultimo_login,
    to_char(created_at, 'DD/MM/YYYY HH24:MI') as fecha_creacion
FROM users 
ORDER BY role, username;

-- 7. Resumen final
SELECT 'RESUMEN FINAL:' as verificacion;
SELECT 
    'Sistema listo para usar' as estado,
    CASE 
        WHEN EXISTS (SELECT 1 FROM users WHERE active = true) 
        AND EXISTS (SELECT 1 FROM users WHERE role = 'SUPER_ADMIN' AND active = true)
        THEN 'SÍ ✓'
        ELSE 'NO ✗'
    END as sistema_listo;

-- 8. Usuarios disponibles para login
SELECT 'USUARIOS DISPONIBLES PARA LOGIN:' as verificacion;
SELECT 
    username || ' (' || role || ')' as usuario_disponible,
    'Contraseña: admin123' as contraseña_temporal
FROM users 
WHERE active = true
ORDER BY role, username;
