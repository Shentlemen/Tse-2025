-- =====================================================
-- SCRIPT DE VERIFICACIÓN - PROFESIONALES POR CLÍNICA
-- =====================================================

-- 1. Verificar clínicas existentes
SELECT '=== CLÍNICAS EXISTENTES ===' as info;
SELECT id, name, code, active FROM clinics ORDER BY id;

-- 2. Verificar especialidades existentes
SELECT '=== ESPECIALIDADES EXISTENTES ===' as info;
SELECT id, name, code, active FROM specialties ORDER BY id;

-- 3. Verificar TODOS los profesionales (sin filtros)
SELECT '=== TODOS LOS PROFESIONALES ===' as info;
SELECT 
    p.id,
    p.name || ' ' || p.last_name as nombre_completo,
    p.email,
    p.license_number,
    p.phone,
    p.clinic_id,
    p.specialty_id,
    p.active,
    p.created_at
FROM professionals p
ORDER BY p.clinic_id, p.name;

-- 4. Verificar profesionales por clínica específica
SELECT '=== PROFESIONALES POR CLÍNICA ===' as info;
SELECT 
    c.name as clinica,
    c.id as clinic_id,
    COUNT(p.id) as total_profesionales
FROM clinics c
LEFT JOIN professionals p ON c.id = p.clinic_id
GROUP BY c.id, c.name
ORDER BY c.id;

-- 5. Verificar profesionales del Centro Neurológico específicamente
SELECT '=== PROFESIONALES CENTRO NEUROLÓGICO ===' as info;
SELECT 
    p.name || ' ' || p.last_name as profesional,
    p.email,
    p.license_number,
    s.name as especialidad,
    p.active
FROM professionals p
JOIN clinics c ON p.clinic_id = c.id
LEFT JOIN specialties s ON p.specialty_id = s.id
WHERE c.code = 'CLIN002' OR c.name LIKE '%Neurológico%'
ORDER BY p.name;

-- 6. Verificar si hay errores en las referencias
SELECT '=== VERIFICACIÓN DE REFERENCIAS ===' as info;
SELECT 
    p.name || ' ' || p.last_name as profesional,
    p.clinic_id,
    c.name as clinica_referenciada,
    p.specialty_id,
    s.name as especialidad_referenciada
FROM professionals p
LEFT JOIN clinics c ON p.clinic_id = c.id
LEFT JOIN specialties s ON p.specialty_id = s.id
WHERE c.id IS NULL OR s.id IS NULL;
