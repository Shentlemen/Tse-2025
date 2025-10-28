-- Script para verificar usuarios profesionales en la base de datos

-- Ver todos los usuarios profesionales
SELECT 
    u.id,
    u.username,
    u.role,
    u.email,
    u.first_name,
    u.last_name,
    u.active,
    u.password,
    c.name as clinic_name,
    p.name as professional_name,
    p.last_name as professional_last_name,
    p.license_number
FROM users u
LEFT JOIN clinics c ON u.clinic_id = c.id
LEFT JOIN professionals p ON u.professional_id = p.id
WHERE u.role = 'PROFESSIONAL'
ORDER BY u.created_at DESC;

-- Verificar si hay usuarios con matrícula específica
SELECT 
    u.username,
    u.password,
    u.active,
    p.license_number,
    c.name as clinic_name
FROM users u
LEFT JOIN professionals p ON u.professional_id = p.id
LEFT JOIN clinics c ON u.clinic_id = c.id
WHERE u.username LIKE '%123%' OR p.license_number LIKE '%123%';

-- Contar usuarios por rol
SELECT 
    role,
    COUNT(*) as total,
    COUNT(CASE WHEN active = true THEN 1 END) as active_count,
    COUNT(CASE WHEN active = false THEN 1 END) as inactive_count
FROM users
GROUP BY role;

-- Verificar estructura de la tabla users
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'users'
ORDER BY ordinal_position;
