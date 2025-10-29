-- Script para verificar las estadísticas del Super Admin
-- Compara los datos mostrados en el panel con los datos reales de la base de datos

\echo '========================================'
\echo 'VERIFICACIÓN DE ESTADÍSTICAS SUPER ADMIN'
\echo '========================================'

-- 1. Clínicas registradas
\echo ''
\echo '1. CLÍNICAS REGISTRADAS:'
\echo '----------------------'
SELECT 
    COUNT(*) as total_clinicas,
    COUNT(CASE WHEN active = true THEN 1 END) as clinicas_activas,
    COUNT(CASE WHEN active = false THEN 1 END) as clinicas_inactivas
FROM clinics;

\echo ''
\echo 'Detalle de clínicas:'
SELECT id, name, address, phone, active, created_at 
FROM clinics 
ORDER BY id;

-- 2. Usuarios totales
\echo ''
\echo '2. USUARIOS TOTALES:'
\echo '------------------'
SELECT 
    COUNT(*) as total_usuarios,
    COUNT(CASE WHEN active = true THEN 1 END) as usuarios_activos,
    COUNT(CASE WHEN active = false THEN 1 END) as usuarios_inactivos
FROM users;

-- 3. Administradores de clínica
\echo ''
\echo '3. ADMINISTRADORES DE CLÍNICA:'
\echo '-----------------------------'
SELECT 
    COUNT(*) as total_admins,
    COUNT(CASE WHEN active = true THEN 1 END) as admins_activos,
    COUNT(CASE WHEN active = false THEN 1 END) as admins_inactivos
FROM users 
WHERE role = 'ADMIN_CLINIC';

\echo ''
\echo 'Detalle de administradores:'
SELECT 
    u.id, 
    u.username, 
    u.email, 
    u.first_name, 
    u.last_name, 
    u.active,
    c.name as clinic_name
FROM users u
LEFT JOIN clinics c ON u.clinic_id = c.id
WHERE u.role = 'ADMIN_CLINIC'
ORDER BY u.id;

-- 4. Profesionales
\echo ''
\echo '4. PROFESIONALES:'
\echo '----------------'
SELECT 
    COUNT(*) as total_profesionales,
    COUNT(CASE WHEN active = true THEN 1 END) as profesionales_activos,
    COUNT(CASE WHEN active = false THEN 1 END) as profesionales_inactivos
FROM users 
WHERE role = 'PROFESSIONAL';

\echo ''
\echo 'Detalle de profesionales:'
SELECT 
    u.id, 
    u.username, 
    u.email, 
    u.first_name, 
    u.last_name, 
    u.active,
    c.name as clinic_name
FROM users u
LEFT JOIN clinics c ON u.clinic_id = c.id
WHERE u.role = 'PROFESSIONAL'
ORDER BY u.id;

-- 5. Resumen de estadísticas que debería mostrar el Super Admin
\echo ''
\echo '5. RESUMEN PARA SUPER ADMIN:'
\echo '---------------------------'
SELECT 
    'Clínicas Registradas' as estadistica,
    COUNT(*) as cantidad
FROM clinics
UNION ALL
SELECT 
    'Administradores Activos' as estadistica,
    COUNT(*) as cantidad
FROM users 
WHERE role = 'ADMIN_CLINIC' AND active = true
UNION ALL
SELECT 
    'Profesionales Activos' as estadistica,
    COUNT(*) as cantidad
FROM users 
WHERE role = 'PROFESSIONAL' AND active = true
UNION ALL
SELECT 
    'Total Usuarios' as estadistica,
    COUNT(*) as cantidad
FROM users 
WHERE active = true;

-- 6. Verificar que cada clínica tenga un administrador
\echo ''
\echo '6. VERIFICACIÓN DE ADMINISTRADORES POR CLÍNICA:'
\echo '----------------------------------------------'
SELECT 
    c.id as clinic_id,
    c.name as clinic_name,
    c.active as clinic_active,
    COUNT(u.id) as admins_count,
    CASE 
        WHEN COUNT(u.id) = 0 THEN 'SIN ADMINISTRADOR'
        WHEN COUNT(u.id) = 1 THEN 'CON ADMINISTRADOR'
        ELSE 'MÚLTIPLES ADMINISTRADORES'
    END as status
FROM clinics c
LEFT JOIN users u ON c.id = u.clinic_id AND u.role = 'ADMIN_CLINIC' AND u.active = true
GROUP BY c.id, c.name, c.active
ORDER BY c.id;

\echo ''
\echo '========================================'
\echo 'VERIFICACIÓN COMPLETADA'
\echo '========================================'
