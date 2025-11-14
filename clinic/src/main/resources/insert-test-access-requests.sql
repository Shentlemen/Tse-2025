-- ============================================================================
-- Script SQL para insertar solicitudes de acceso de prueba
-- Profesional: usuario L77
-- Pacientes: cédulas 11223345 y 33445566
-- ============================================================================

-- Verificar que los datos existen antes de insertar
DO $$
DECLARE
    v_professional_id BIGINT;
    v_clinic_id BIGINT;
    v_specialty_id BIGINT;
    v_patient_1_id BIGINT;
    v_patient_2_id BIGINT;
    v_request_id BIGINT;
BEGIN
    -- Buscar el profesional asociado al usuario L77
    SELECT p.id, p.clinic_id, p.specialty_id
    INTO v_professional_id, v_clinic_id, v_specialty_id
    FROM professionals p
    INNER JOIN users u ON u.professional_id = p.id
    WHERE u.username = 'L77' AND u.active = true AND p.active = true;
    
    IF v_professional_id IS NULL THEN
        RAISE EXCEPTION 'No se encontró un profesional activo asociado al usuario L77';
    END IF;
    
    RAISE NOTICE 'Profesional encontrado: ID = %, Clínica ID = %, Especialidad ID = %', 
        v_professional_id, v_clinic_id, v_specialty_id;
    
    -- Buscar paciente con cédula 11223345
    SELECT id INTO v_patient_1_id
    FROM patients
    WHERE document_number = '11223345' AND active = true;
    
    IF v_patient_1_id IS NULL THEN
        RAISE EXCEPTION 'No se encontró un paciente activo con cédula 11223345';
    END IF;
    
    RAISE NOTICE 'Paciente 1 encontrado: ID = % (cédula 11223345)', v_patient_1_id;
    
    -- Buscar paciente con cédula 33445566
    SELECT id INTO v_patient_2_id
    FROM patients
    WHERE document_number = '33445566' AND active = true;
    
    IF v_patient_2_id IS NULL THEN
        RAISE EXCEPTION 'No se encontró un paciente activo con cédula 33445566';
    END IF;
    
    RAISE NOTICE 'Paciente 2 encontrado: ID = % (cédula 33445566)', v_patient_2_id;
    
    -- Insertar solicitud para paciente 1 (cédula 11223345) con especialidad específica
    INSERT INTO access_requests (
        status, 
        reason, 
        requested_at, 
        patient_id, 
        professional_id, 
        clinic_id, 
        specialty_id
    )
    VALUES (
        'PENDING',
        'Necesito acceso a los documentos clínicos del paciente para continuar el tratamiento y revisar el historial médico previo.',
        NOW() - INTERVAL '2 days',
        v_patient_1_id,
        v_professional_id,
        v_clinic_id,
        v_specialty_id  -- Con especialidad específica
    )
    RETURNING id INTO v_request_id;
    
    RAISE NOTICE 'Solicitud 1 creada: ID = % (Paciente cédula 11223345, con especialidad)', v_request_id;
    
    -- Insertar solicitud para paciente 2 (cédula 33445566) sin especialidad específica (todas las especialidades)
    INSERT INTO access_requests (
        status, 
        reason, 
        requested_at, 
        patient_id, 
        professional_id, 
        clinic_id, 
        specialty_id
    )
    VALUES (
        'PENDING',
        'Solicitud de acceso completo a todos los documentos clínicos del paciente para evaluación integral y seguimiento del caso.',
        NOW() - INTERVAL '1 day',
        v_patient_2_id,
        v_professional_id,
        v_clinic_id,
        NULL  -- Sin especialidad específica (todas las especialidades)
    )
    RETURNING id INTO v_request_id;
    
    RAISE NOTICE 'Solicitud 2 creada: ID = % (Paciente cédula 33445566, todas las especialidades)', v_request_id;
    
    -- Insertar una solicitud adicional para paciente 1 con diferente motivo y fecha
    INSERT INTO access_requests (
        status, 
        reason, 
        requested_at, 
        patient_id, 
        professional_id, 
        clinic_id, 
        specialty_id
    )
    VALUES (
        'PENDING',
        'Revisión de estudios previos y análisis de laboratorio para diagnóstico diferencial.',
        NOW() - INTERVAL '5 days',
        v_patient_1_id,
        v_professional_id,
        v_clinic_id,
        v_specialty_id
    )
    RETURNING id INTO v_request_id;
    
    RAISE NOTICE 'Solicitud 3 creada: ID = % (Paciente cédula 11223345, segunda solicitud)', v_request_id;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE 'RESUMEN:';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Se crearon 3 solicitudes de acceso:';
    RAISE NOTICE '- 2 solicitudes para paciente cédula 11223345';
    RAISE NOTICE '- 1 solicitud para paciente cédula 33445566';
    RAISE NOTICE 'Todas las solicitudes están en estado PENDING';
    RAISE NOTICE 'Profesional: usuario L77 (ID: %)', v_professional_id;
    RAISE NOTICE '========================================';
    
EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error al insertar solicitudes: %', SQLERRM;
END $$;

-- Verificar las solicitudes creadas
SELECT 
    ar.id,
    ar.status,
    ar.reason,
    ar.requested_at,
    p.document_number as paciente_cedula,
    p.name || ' ' || COALESCE(p.last_name, '') as paciente_nombre,
    pr.name || ' ' || COALESCE(pr.last_name, '') as profesional_nombre,
    c.name as clinica_nombre,
    COALESCE(s.name, 'Todas las especialidades') as especialidad
FROM access_requests ar
INNER JOIN patients p ON ar.patient_id = p.id
INNER JOIN professionals pr ON ar.professional_id = pr.id
INNER JOIN clinics c ON ar.clinic_id = c.id
LEFT JOIN specialties s ON ar.specialty_id = s.id
INNER JOIN users u ON u.professional_id = pr.id
WHERE u.username = 'L77'
  AND p.document_number IN ('11223345', '33445566')
  AND ar.status = 'PENDING'
ORDER BY ar.requested_at DESC;

