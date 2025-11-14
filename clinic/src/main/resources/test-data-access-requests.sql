-- Script para insertar datos de prueba de solicitudes de acceso
-- Ejecutar después de tener datos de pacientes, profesionales y especialidades

-- Nota: Ajustar los IDs según los datos reales de tu base de datos
-- Reemplazar los valores de patient_id, professional_id, clinic_id y specialty_id con IDs reales

-- Ejemplo de inserción de solicitudes de acceso pendientes
-- Asegúrate de tener al menos un paciente, un profesional y una especialidad en la base de datos

INSERT INTO access_requests (status, reason, requested_at, patient_id, professional_id, clinic_id, specialty_id)
SELECT 
    'PENDING' as status,
    'Necesito acceso a los documentos clínicos del paciente para continuar el tratamiento' as reason,
    NOW() - INTERVAL '2 days' as requested_at,
    p.id as patient_id,
    pr.id as professional_id,
    c.id as clinic_id,
    s.id as specialty_id
FROM patients p
CROSS JOIN professionals pr
CROSS JOIN clinics c
CROSS JOIN specialties s
WHERE p.active = true 
  AND pr.active = true
  AND pr.clinic_id = c.id
  AND pr.specialty_id = s.id
LIMIT 3;

-- Insertar más solicitudes con diferentes especialidades
INSERT INTO access_requests (status, reason, requested_at, patient_id, professional_id, clinic_id, specialty_id)
SELECT 
    'PENDING' as status,
    'Solicitud de acceso para revisión de historial médico previo' as reason,
    NOW() - INTERVAL '5 days' as requested_at,
    p.id as patient_id,
    pr.id as professional_id,
    c.id as clinic_id,
    s.id as specialty_id
FROM patients p
CROSS JOIN professionals pr
CROSS JOIN clinics c
CROSS JOIN specialties s
WHERE p.active = true 
  AND pr.active = true
  AND pr.clinic_id = c.id
  AND pr.specialty_id = s.id
  AND NOT EXISTS (
    SELECT 1 FROM access_requests ar 
    WHERE ar.patient_id = p.id 
      AND ar.professional_id = pr.id
  )
LIMIT 2;

-- Insertar una solicitud sin especialidad específica (todas las especialidades)
INSERT INTO access_requests (status, reason, requested_at, patient_id, professional_id, clinic_id, specialty_id)
SELECT 
    'PENDING' as status,
    'Acceso completo a todos los documentos del paciente para evaluación integral' as reason,
    NOW() - INTERVAL '1 day' as requested_at,
    p.id as patient_id,
    pr.id as professional_id,
    c.id as clinic_id,
    NULL as specialty_id
FROM patients p
CROSS JOIN professionals pr
CROSS JOIN clinics c
WHERE p.active = true 
  AND pr.active = true
  AND pr.clinic_id = c.id
LIMIT 1;

