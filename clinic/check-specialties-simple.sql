-- Verificar especialidades sin referencias a clinic_id
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
    code,
    active
FROM specialties 
ORDER BY id;
