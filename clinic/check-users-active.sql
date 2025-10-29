-- Verificar usuarios y su estado activo/inactivo
SELECT 
    id,
    username,
    role,
    active,
    clinic_id,
    professional_id
FROM users
ORDER BY username;

-- Contar activos e inactivos
SELECT 
    CASE 
        WHEN active = true THEN 'Activo'
        WHEN active = false THEN 'Inactivo'
        ELSE 'NULL'
    END as estado,
    COUNT(*) as cantidad
FROM users
GROUP BY active
ORDER BY estado;

