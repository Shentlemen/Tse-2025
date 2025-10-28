-- Verificar la estructura de la tabla professionals
SELECT 
    column_name, 
    data_type, 
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'professionals' 
ORDER BY ordinal_position;

-- Verificar profesionales con la estructura correcta
SELECT 
    'PROFESIONALES EN TABLA PROFESSIONALS' as categoria,
    COUNT(*) as total_profesionales,
    COUNT(CASE WHEN active = true THEN 1 END) as profesionales_activos,
    COUNT(CASE WHEN active = false THEN 1 END) as profesionales_inactivos
FROM professionals;

-- Detalle de profesionales con columnas correctas
SELECT 
    'DETALLE DE PROFESIONALES' as categoria,
    id, 
    name, 
    last_name, 
    license_number,
    active,
    clinic_id
FROM professionals 
ORDER BY id;

-- Comparar con usuarios de rol PROFESSIONAL
SELECT 
    'USUARIOS CON ROL PROFESSIONAL' as categoria,
    COUNT(*) as total_usuarios_professional,
    COUNT(CASE WHEN active = true THEN 1 END) as usuarios_professional_activos
FROM users 
WHERE role = 'PROFESSIONAL';
