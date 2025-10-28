-- Verificar que las especialidades tienen clinic_id correcto
SELECT 
    'VERIFICACIÓN DE CLINIC_ID' as categoria,
    s.id, 
    s.name, 
    s.clinic_id,
    c.name as clinic_name
FROM specialties s
LEFT JOIN clinics c ON s.clinic_id = c.id
ORDER BY s.clinic_id, s.name;

-- Verificar especialidades activas por clínica
SELECT 
    'ESPECIALIDADES ACTIVAS POR CLÍNICA' as categoria,
    c.id as clinic_id,
    c.name as clinic_name,
    COUNT(s.id) as especialidades_activas
FROM clinics c
LEFT JOIN specialties s ON c.id = s.clinic_id AND s.active = true
GROUP BY c.id, c.name
ORDER BY c.id;
