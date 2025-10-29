-- =====================================================
-- DIAGNÓSTICO ESPECÍFICO - CENTRO NEUROLÓGICO
-- =====================================================

-- 1. Verificar si existe la clínica CLIN002
SELECT '=== VERIFICAR CLÍNICA CLIN002 ===' as info;
SELECT id, name, code FROM clinics WHERE code = 'CLIN002';

-- 2. Verificar si existe la clínica por nombre
SELECT '=== VERIFICAR CLÍNICA POR NOMBRE ===' as info;
SELECT id, name, code FROM clinics WHERE name LIKE '%Neurológico%';

-- 3. Verificar TODAS las clínicas
SELECT '=== TODAS LAS CLÍNICAS ===' as info;
SELECT id, name, code FROM clinics ORDER BY id;

-- 4. Verificar profesionales que tengan clinic_id = 2
SELECT '=== PROFESIONALES CON CLINIC_ID = 2 ===' as info;
SELECT 
    p.name || ' ' || p.last_name as profesional,
    p.clinic_id,
    p.license_number
FROM professionals p 
WHERE p.clinic_id = 2;

-- 5. Verificar profesionales que tengan clinic_id = (SELECT id FROM clinics WHERE code = 'CLIN002')
SELECT '=== PROFESIONALES CON CLINIC_ID DINÁMICO ===' as info;
SELECT 
    p.name || ' ' || p.last_name as profesional,
    p.clinic_id,
    p.license_number
FROM professionals p 
WHERE p.clinic_id = (SELECT id FROM clinics WHERE code = 'CLIN002');

-- 6. Verificar si hay profesionales con números de licencia LP60001-LP60007
SELECT '=== PROFESIONALES CON LICENCIAS LP6000X ===' as info;
SELECT 
    p.name || ' ' || p.last_name as profesional,
    p.license_number,
    p.clinic_id
FROM professionals p 
WHERE p.license_number LIKE 'LP6000%';

-- 7. Verificar si hay profesionales con emails @centroneurologico
SELECT '=== PROFESIONALES CON EMAIL CENTRO NEUROLÓGICO ===' as info;
SELECT 
    p.name || ' ' || p.last_name as profesional,
    p.email,
    p.clinic_id
FROM professionals p 
WHERE p.email LIKE '%centroneurologico%';

-- 8. Verificar el ID real de la clínica del Centro Neurológico
SELECT '=== ID REAL DE CENTRO NEUROLÓGICO ===' as info;
SELECT 
    id as clinic_id_real,
    name,
    code
FROM clinics 
WHERE name LIKE '%Neurológico%' OR code = 'CLIN002';

-- 9. Buscar profesionales que puedan estar asociados a esa clínica
SELECT '=== PROFESIONALES ASOCIADOS A CENTRO NEUROLÓGICO ===' as info;
SELECT 
    p.name || ' ' || p.last_name as profesional,
    p.clinic_id,
    c.name as clinica,
    c.code as codigo_clinica
FROM professionals p
LEFT JOIN clinics c ON p.clinic_id = c.id
WHERE c.name LIKE '%Neurológico%' OR c.code = 'CLIN002' OR p.clinic_id = (
    SELECT id FROM clinics WHERE name LIKE '%Neurológico%' OR code = 'CLIN002'
);
