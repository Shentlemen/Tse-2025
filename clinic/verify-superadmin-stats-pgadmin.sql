-- Script para verificar las estadísticas del Super Admin
-- Versión compatible con pgAdmin (solo SQL puro)

-- 1. Clínicas registradas
SELECT 
    'CLÍNICAS REGISTRADAS' as categoria,
    COUNT(*) as total_clinicas,
    COUNT(CASE WHEN active = true THEN 1 END) as clinicas_activas,
    COUNT(CASE WHEN active = false THEN 1 END) as clinicas_inactivas
FROM clinics;

-- Detalle de clínicas
SELECT 
    'DETALLE DE CLÍNICAS' as categoria,
    id, 
    name, 
    address, 
    phone, 
    active, 
    created_at 
FROM clinics 
ORDER BY id;

-- 2. Usuarios totales
SELECT 
    'USUARIOS TOTALES' as categoria,
    COUNT(*) as total_usuarios,
    COUNT(CASE WHEN active = true THEN 1 END) as usuarios_activos,
    COUNT(CASE WHEN active = false THEN 1 END) as usuarios_inactivos
FROM users;

-- 3. Administradores de clínica
SELECT 
    'ADMINISTRADORES DE CLÍNICA' as categoria,
    COUNT(*) as total_admins,
    COUNT(CASE WHEN active = true THEN 1 END) as admins_activos,
    COUNT(CASE WHEN active = false THEN 1 END) as admins_inactivos
FROM users 
WHERE role = 'ADMIN_CLINIC';

-- Detalle de administradores
SELECT 
    'DETALLE DE ADMINISTRADORES' as categoria,
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
SELECT 
    'PROFESIONALES' as categoria,
    COUNT(*) as total_profesionales,
    COUNT(CASE WHEN active = true THEN 1 END) as profesionales_activos,
    COUNT(CASE WHEN active = false THEN 1 END) as profesionales_inactivos
FROM users 
WHERE role = 'PROFESSIONAL';

-- Detalle de profesionales
SELECT 
    'DETALLE DE PROFESIONALES' as categoria,
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
SELECT 
    'RESUMEN PARA SUPER ADMIN' as categoria,
    'Clínicas Registradas' as estadistica,
    COUNT(*) as cantidad
FROM clinics
UNION ALL
SELECT 
    'RESUMEN PARA SUPER ADMIN' as categoria,
    'Administradores Activos' as estadistica,
    COUNT(*) as cantidad
FROM users 
WHERE role = 'ADMIN_CLINIC' AND active = true
UNION ALL
SELECT 
    'RESUMEN PARA SUPER ADMIN' as categoria,
    'Profesionales Activos' as estadistica,
    COUNT(*) as cantidad
FROM users 
WHERE role = 'PROFESSIONAL' AND active = true
UNION ALL
SELECT 
    'RESUMEN PARA SUPER ADMIN' as categoria,
    'Total Usuarios' as estadistica,
    COUNT(*) as cantidad
FROM users 
WHERE active = true;

-- 6. Verificar que cada clínica tenga un administrador
SELECT 
    'VERIFICACIÓN DE ADMINISTRADORES POR CLÍNICA' as categoria,
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
