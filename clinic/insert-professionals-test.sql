-- =====================================================
-- CONSULTA SQL PARA AGREGAR PROFESIONALES DE PRUEBA
-- Sistema Multi-Tenant HCEN Clínica
-- =====================================================

-- Verificar que las clínicas existan
SELECT 'Verificando clínicas...' as status;
SELECT id, name, code FROM clinics ORDER BY id;

-- Si no existen las clínicas, crearlas primero
INSERT INTO clinics (name, code, description, address, phone, email, hcen_endpoint, theme_colors, active, created_at) VALUES
-- Clínica del Corazón
('Clínica del Corazón', 'CLIN001', 'Clínica especializada en cardiología', 
 'Av. 18 de Julio 1234, Montevideo', '+598 2 123-4567', 'info@clinicacorazon.com.uy', 
 'http://localhost:8080/hcen/api', '{"primary":"#e74c3c","secondary":"#c0392b"}', true, CURRENT_TIMESTAMP),
-- Centro Neurológico
('Centro Neurológico', 'CLIN002', 'Centro especializado en neurología', 
 'Bvar. Artigas 5678, Montevideo', '+598 2 987-6543', 'contacto@centroneurologico.com.uy', 
 'http://localhost:8080/hcen/api', '{"primary":"#3498db","secondary":"#2980b9"}', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Verificar nuevamente las clínicas después de la inserción
SELECT 'Verificando clínicas después de inserción...' as status;
SELECT id, name, code FROM clinics ORDER BY id;

-- Verificar que las especialidades existan
SELECT 'Verificando especialidades...' as status;
SELECT id, name, code FROM specialties ORDER BY id;

-- Si no existen las especialidades, crearlas primero
INSERT INTO specialties (name, code, description, active) VALUES
('Medicina General', 'MG', 'Atención médica general y preventiva', true),
('Cardiología', 'CAR', 'Especialidad en enfermedades del corazón', true),
('Dermatología', 'DER', 'Especialidad en enfermedades de la piel', true),
('Ginecología', 'GIN', 'Especialidad en salud femenina', true),
('Pediatría', 'PED', 'Especialidad en medicina infantil', true),
('Oftalmología', 'OFT', 'Especialidad en salud ocular', true),
('Otorrinolaringología', 'ORL', 'Especialidad en oído, nariz y garganta', true),
('Psicología', 'PSI', 'Especialidad en salud mental', true),
('Traumatología', 'TRA', 'Especialidad en sistema musculoesquelético', true),
('Urología', 'URO', 'Especialidad en sistema urinario y genital masculino', true)
ON CONFLICT (name) DO NOTHING;

-- Verificar nuevamente las especialidades después de la inserción
SELECT 'Verificando especialidades después de inserción...' as status;
SELECT id, name, code FROM specialties ORDER BY id;

-- Verificar números de licencia existentes
SELECT 'Verificando números de licencia existentes...' as status;
SELECT license_number, name || ' ' || last_name as profesional FROM professionals WHERE license_number LIKE 'LP%' ORDER BY license_number;

-- =====================================================
-- INSERTAR PROFESIONALES DE PRUEBA
-- =====================================================

-- Profesionales para Clínica del Corazón
INSERT INTO professionals (name, last_name, email, license_number, phone, clinic_id, specialty_id, active, created_at) VALUES

-- Cardiología
('Dr. Juan Carlos', 'Pérez Carvalho', 'juan.perez.carvalho@clinicacorazon.com.uy', 'LP50001', '+598 99 123 456', 
 (SELECT id FROM clinics WHERE code = 'CLIN001'), 
 (SELECT id FROM specialties WHERE code = 'CAR'), true, CURRENT_TIMESTAMP),

('Dra. María Elena', 'González Silva', 'maria.gonzalez.silva@clinicacorazon.com.uy', 'LP50002', '+598 99 234 567', 
 (SELECT id FROM clinics WHERE code = 'CLIN001'), 
 (SELECT id FROM specialties WHERE code = 'CAR'), true, CURRENT_TIMESTAMP),

-- Medicina General
('Dr. Roberto', 'Martínez López', 'roberto.martinez.lopez@clinicacorazon.com.uy', 'LP50003', '+598 99 345 678', 
 (SELECT id FROM clinics WHERE code = 'CLIN001'), 
 (SELECT id FROM specialties WHERE code = 'MG'), true, CURRENT_TIMESTAMP),

('Dra. Ana Lucía', 'Fernández Torres', 'ana.fernandez.torres@clinicacorazon.com.uy', 'LP50004', '+598 99 456 789', 
 (SELECT id FROM clinics WHERE code = 'CLIN001'), 
 (SELECT id FROM specialties WHERE code = 'MG'), true, CURRENT_TIMESTAMP),

-- Pediatría
('Dra. Carmen', 'Rodríguez Vargas', 'carmen.rodriguez.vargas@clinicacorazon.com.uy', 'LP50005', '+598 99 567 890', 
 (SELECT id FROM clinics WHERE code = 'CLIN001'), 
 (SELECT id FROM specialties WHERE code = 'PED'), true, CURRENT_TIMESTAMP),

-- Psicología
('Lic. Diego', 'Silva Morales', 'diego.silva.morales@clinicacorazon.com.uy', 'LP50006', '+598 99 678 901', 
 (SELECT id FROM clinics WHERE code = 'CLIN001'), 
 (SELECT id FROM specialties WHERE code = 'PSI'), true, CURRENT_TIMESTAMP);

-- Profesionales para Centro Neurológico
INSERT INTO professionals (name, last_name, email, license_number, phone, clinic_id, specialty_id, active, created_at) VALUES

-- Neurología (usando Cardiología como fallback)
('Dr. Carlos', 'López Herrera', 'carlos.lopez.herrera@centroneurologico.com.uy', 'LP60001', '+598 99 789 012', 
 (SELECT id FROM clinics WHERE code = 'CLIN002'), 
 (SELECT id FROM specialties WHERE code = 'CAR'), true, CURRENT_TIMESTAMP),

('Dra. Patricia', 'Méndez Rojas', 'patricia.mendez.rojas@centroneurologico.com.uy', 'LP60002', '+598 99 890 123', 
 (SELECT id FROM clinics WHERE code = 'CLIN002'), 
 (SELECT id FROM specialties WHERE code = 'CAR'), true, CURRENT_TIMESTAMP),

-- Medicina General
('Dr. Fernando', 'García Morales', 'fernando.garcia.morales@centroneurologico.com.uy', 'LP60003', '+598 99 901 234', 
 (SELECT id FROM clinics WHERE code = 'CLIN002'), 
 (SELECT id FROM specialties WHERE code = 'MG'), true, CURRENT_TIMESTAMP),

('Dra. Laura', 'Herrera Vargas', 'laura.herrera.vargas@centroneurologico.com.uy', 'LP60004', '+598 99 012 345', 
 (SELECT id FROM clinics WHERE code = 'CLIN002'), 
 (SELECT id FROM specialties WHERE code = 'MG'), true, CURRENT_TIMESTAMP),

-- Pediatría
('Dra. Sofía', 'Vargas Pérez', 'sofia.vargas.perez@centroneurologico.com.uy', 'LP60005', '+598 99 123 456', 
 (SELECT id FROM clinics WHERE code = 'CLIN002'), 
 (SELECT id FROM specialties WHERE code = 'PED'), true, CURRENT_TIMESTAMP),

-- Psicología
('Lic. Martín', 'Rojas González', 'martin.rojas.gonzalez@centroneurologico.com.uy', 'LP60006', '+598 99 234 567', 
 (SELECT id FROM clinics WHERE code = 'CLIN002'), 
 (SELECT id FROM specialties WHERE code = 'PSI'), true, CURRENT_TIMESTAMP),

-- Traumatología
('Dr. Alejandro', 'Morales Martínez', 'alejandro.morales.martinez@centroneurologico.com.uy', 'LP60007', '+598 99 345 678', 
 (SELECT id FROM clinics WHERE code = 'CLIN002'), 
 (SELECT id FROM specialties WHERE code = 'TRA'), true, CURRENT_TIMESTAMP);

-- =====================================================
-- CONSULTAS DE VERIFICACIÓN
-- =====================================================

-- Verificar los profesionales insertados
SELECT 'Profesionales insertados exitosamente' as status;

-- Mostrar todos los profesionales por clínica
SELECT 
    c.name as clinica,
    p.name || ' ' || p.last_name as profesional,
    p.email,
    p.license_number,
    p.phone,
    s.name as especialidad,
    p.active
FROM professionals p
JOIN clinics c ON p.clinic_id = c.id
JOIN specialties s ON p.specialty_id = s.id
ORDER BY c.name, p.name;

-- Contar profesionales por clínica
SELECT 
    c.name as clinica,
    COUNT(p.id) as total_profesionales
FROM clinics c
LEFT JOIN professionals p ON c.id = p.clinic_id
GROUP BY c.id, c.name
ORDER BY c.name;

-- =====================================================
-- NOTAS IMPORTANTES:
-- =====================================================
-- 1. Asegúrate de que las clínicas existan (ID 1 y 2)
-- 2. Asegúrate de que las especialidades existan
-- 3. Si alguna especialidad no existe, se usará Cardiología como fallback
-- 4. Los emails son únicos, así que no se pueden duplicar
-- 5. Los números de licencia son únicos también
-- =====================================================
