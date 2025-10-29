-- Verificar información de Clínica del Corazón y sus especialidades
SELECT 'CLÍNICAS' as tipo, id, name, code FROM clinics WHERE name LIKE '%Corazón%';

SELECT 'ESPECIALIDADES POR CLÍNICA' as tipo, 
       s.id, 
       s.name, 
       s.code, 
       s.active,
       c.name as clinic_name,
       c.id as clinic_id
FROM specialties s 
JOIN clinics c ON s.clinic_id = c.id 
WHERE c.name LIKE '%Corazón%'
ORDER BY s.name;

-- Contar especialidades por clínica
SELECT 'CONTEO POR CLÍNICA' as tipo,
       c.name as clinic_name,
       COUNT(s.id) as total_especialidades,
       COUNT(CASE WHEN s.active = true THEN 1 END) as especialidades_activas
FROM clinics c
LEFT JOIN specialties s ON c.id = s.clinic_id
WHERE c.name LIKE '%Corazón%'
GROUP BY c.id, c.name;
