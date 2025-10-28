-- Verificar usuarios y sus clínicas
SELECT 
    'USUARIOS POR CLÍNICA' as info,
    u.id,
    u.username,
    u.role,
    u.first_name,
    u.last_name,
    u.clinic_id,
    c.name as clinic_name,
    u.active,
    u.created_at
FROM users u
LEFT JOIN clinics c ON u.clinic_id = c.id
WHERE u.active = true
ORDER BY u.clinic_id, u.username;

-- Verificar específicamente usuarios de Clínica del Corazón (ID 4)
SELECT 
    'USUARIOS CLÍNICA DEL CORAZÓN' as info,
    u.id,
    u.username,
    u.role,
    u.first_name,
    u.last_name,
    u.clinic_id,
    c.name as clinic_name
FROM users u
LEFT JOIN clinics c ON u.clinic_id = c.id
WHERE u.clinic_id = 4 AND u.active = true;

-- Contar usuarios por clínica
SELECT 
    'CONTEO POR CLÍNICA' as info,
    c.id as clinic_id,
    c.name as clinic_name,
    COUNT(u.id) as total_usuarios,
    COUNT(CASE WHEN u.role = 'ADMIN_CLINIC' THEN 1 END) as administradores,
    COUNT(CASE WHEN u.role = 'PROFESSIONAL' THEN 1 END) as profesionales
FROM clinics c
LEFT JOIN users u ON c.id = u.clinic_id AND u.active = true
GROUP BY c.id, c.name
ORDER BY c.id;
