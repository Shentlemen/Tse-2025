-- Script de prueba para verificar consultas JPA
-- Simular la consulta que hace Specialty.findByClinic

-- Para Super Admin (clinicId = 0) - debería devolver todas las especialidades
SELECT 
    'SUPER ADMIN - TODAS LAS ESPECIALIDADES' as categoria,
    s.id, 
    s.name, 
    s.description,
    s.code,
    s.active,
    s.clinic_id,
    c.name as clinic_name
FROM specialties s
LEFT JOIN clinics c ON s.clinic_id = c.id
ORDER BY s.name;

-- Para Clínica San José (clinicId = 1)
SELECT 
    'CLÍNICA SAN JOSÉ (ID=1)' as categoria,
    s.id, 
    s.name, 
    s.description,
    s.code,
    s.active,
    s.clinic_id,
    c.name as clinic_name
FROM specialties s
LEFT JOIN clinics c ON s.clinic_id = c.id
WHERE s.clinic_id = 1
ORDER BY s.name;

-- Para Clínica del Corazón (clinicId = 4)
SELECT 
    'CLÍNICA DEL CORAZÓN (ID=4)' as categoria,
    s.id, 
    s.name, 
    s.description,
    s.code,
    s.active,
    s.clinic_id,
    c.name as clinic_name
FROM specialties s
LEFT JOIN clinics c ON s.clinic_id = c.id
WHERE s.clinic_id = 4
ORDER BY s.name;

-- Para Centro Neurológico (clinicId = 5)
SELECT 
    'CENTRO NEUROLÓGICO (ID=5)' as categoria,
    s.id, 
    s.name, 
    s.description,
    s.code,
    s.active,
    s.clinic_id,
    c.name as clinic_name
FROM specialties s
LEFT JOIN clinics c ON s.clinic_id = c.id
WHERE s.clinic_id = 5
ORDER BY s.name;
