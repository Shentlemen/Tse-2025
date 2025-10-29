-- Verificar estructura de la tabla specialties
SELECT 
    column_name, 
    data_type, 
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'specialties' 
ORDER BY ordinal_position;

-- Verificar especialidades existentes
SELECT 
    'ESPECIALIDADES EXISTENTES' as categoria,
    COUNT(*) as total_especialidades,
    COUNT(CASE WHEN active = true THEN 1 END) as especialidades_activas,
    COUNT(CASE WHEN active = false THEN 1 END) as especialidades_inactivas
FROM specialties;

-- Detalle de especialidades
SELECT 
    'DETALLE DE ESPECIALIDADES' as categoria,
    id, 
    name, 
    description,
    active,
    clinic_id
FROM specialties 
ORDER BY id;

-- Verificar si hay especialidades sin clínica asociada
SELECT 
    'ESPECIALIDADES SIN CLÍNICA' as categoria,
    COUNT(*) as cantidad
FROM specialties 
WHERE clinic_id IS NULL;

-- Verificar especialidades por clínica
SELECT 
    'ESPECIALIDADES POR CLÍNICA' as categoria,
    c.id as clinic_id,
    c.name as clinic_name,
    COUNT(s.id) as especialidades_count
FROM clinics c
LEFT JOIN specialties s ON c.id = s.clinic_id AND s.active = true
GROUP BY c.id, c.name
ORDER BY c.id;
