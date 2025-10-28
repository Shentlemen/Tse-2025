-- Script para poblar la base de datos con datos multi-tenant
-- Ejecutar después de crear las tablas con schema.sql

-- Conectar a la base de datos
\c clinic_db;

-- Limpiar datos existentes (opcional - solo para desarrollo)
-- DELETE FROM professionals WHERE clinic_id IN (SELECT id FROM clinics WHERE code IN ('CLIN001', 'CLIN002'));
-- DELETE FROM clinics WHERE code IN ('CLIN001', 'CLIN002');

-- Insertar las clínicas del sistema multi-tenant
INSERT INTO clinics (name, code, description, address, phone, email, hcen_endpoint, theme_colors, active, created_at) VALUES
-- Clínica del Corazón
('Clínica del Corazón', 'CLIN001', 'Clínica especializada en cardiología', 
 'Av. 18 de Julio 1234, Montevideo', '+598 2 123-4567', 'info@clinicacorazon.com.uy', 
 'http://localhost:8080/hcen/api', '{"primary":"#e74c3c","secondary":"#c0392b"}', true, CURRENT_TIMESTAMP),

-- Centro Neurológico
('Centro Neurológico', 'CLIN002', 'Centro especializado en neurología', 
 'Bvar. Artigas 5678, Montevideo', '+598 2 987-6543', 'contacto@centroneurologico.com.uy', 
 'http://localhost:8080/hcen/api', '{"primary":"#3498db","secondary":"#2980b9"}', true, CURRENT_TIMESTAMP)

ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    address = EXCLUDED.address,
    phone = EXCLUDED.phone,
    email = EXCLUDED.email,
    hcen_endpoint = EXCLUDED.hcen_endpoint,
    theme_colors = EXCLUDED.theme_colors,
    updated_at = CURRENT_TIMESTAMP;

-- Insertar especialidades específicas para las clínicas
INSERT INTO specialties (name, code, description, active) VALUES
('Cardiología', 'CARD', 'Especialidad médica que se ocupa del corazón', true),
('Neurología', 'NEURO', 'Especialidad médica del sistema nervioso', true),
('Pediatría', 'PED', 'Medicina especializada en niños', true),
('Traumatología', 'TRAUM', 'Especialidad en huesos y articulaciones', true)

ON CONFLICT (name) DO NOTHING;

-- Insertar profesionales para Clínica del Corazón
INSERT INTO professionals (name, last_name, email, license_number, phone, clinic_id, specialty_id, active, created_at) VALUES
-- Dr. Juan Pérez - Cardiología
('Dr. Juan', 'Pérez', 'jperez@clinicacorazon.com.uy', 'LIC001', '+598 99 111-2222', 
 (SELECT id FROM clinics WHERE code = 'CLIN001'), 
 (SELECT id FROM specialties WHERE code = 'CARD'), true, CURRENT_TIMESTAMP),

-- Dr. Carlos Rodríguez - Pediatría
('Dr. Carlos', 'Rodríguez', 'crodriguez@clinicacorazon.com.uy', 'LIC003', '+598 99 555-6666',
 (SELECT id FROM clinics WHERE code = 'CLIN001'),
 (SELECT id FROM specialties WHERE code = 'PED'), true, CURRENT_TIMESTAMP),

-- Dra. María González - Neurología (Centro Neurológico)
('Dra. María', 'González', 'mgonzalez@centroneurologico.com.uy', 'LIC002', '+598 99 333-4444',
 (SELECT id FROM clinics WHERE code = 'CLIN002'),
 (SELECT id FROM specialties WHERE code = 'NEURO'), true, CURRENT_TIMESTAMP)

ON CONFLICT (license_number) DO UPDATE SET
    name = EXCLUDED.name,
    last_name = EXCLUDED.last_name,
    email = EXCLUDED.email,
    phone = EXCLUDED.phone,
    clinic_id = EXCLUDED.clinic_id,
    specialty_id = EXCLUDED.specialty_id,
    updated_at = CURRENT_TIMESTAMP;

-- Crear tabla de pacientes si no existe
CREATE TABLE IF NOT EXISTS patients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    inus_id VARCHAR(50),
    document_number VARCHAR(50),
    birth_date DATE,
    gender VARCHAR(10),
    phone VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    clinic_id BIGINT NOT NULL,
    
    CONSTRAINT fk_patient_clinic 
        FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE CASCADE
);

-- Índices para pacientes
CREATE INDEX IF NOT EXISTS idx_patients_clinic_id ON patients(clinic_id);
CREATE INDEX IF NOT EXISTS idx_patients_inus_id ON patients(inus_id);
CREATE INDEX IF NOT EXISTS idx_patients_document_number ON patients(document_number);
CREATE INDEX IF NOT EXISTS idx_patients_active ON patients(active);

-- Insertar pacientes de ejemplo para cada clínica
INSERT INTO patients (name, last_name, document_number, inus_id, birth_date, gender, phone, email, address, clinic_id, active, created_at) VALUES
-- Pacientes de Clínica del Corazón
('Ana', 'Silva', '12345678', 'INUS001', '1985-05-15', 'F', '+598 99 777-8888', 'ana.silva@email.com', 'Av. Italia 3456, Montevideo', 
 (SELECT id FROM clinics WHERE code = 'CLIN001'), true, CURRENT_TIMESTAMP),

('Lucía', 'Fernández', '11223344', 'INUS003', '1992-08-22', 'F', '+598 99 111-3333', 'lucia.fernandez@email.com', 'Carrasco 456, Montevideo',
 (SELECT id FROM clinics WHERE code = 'CLIN001'), true, CURRENT_TIMESTAMP),

-- Pacientes de Centro Neurológico
('Roberto', 'Martínez', '87654321', 'INUS002', '1978-12-03', 'M', '+598 99 999-0000', 'roberto.martinez@email.com', 'Pocitos 789, Montevideo',
 (SELECT id FROM clinics WHERE code = 'CLIN002'), true, CURRENT_TIMESTAMP)

ON CONFLICT (inus_id) DO UPDATE SET
    name = EXCLUDED.name,
    last_name = EXCLUDED.last_name,
    document_number = EXCLUDED.document_number,
    birth_date = EXCLUDED.birth_date,
    gender = EXCLUDED.gender,
    phone = EXCLUDED.phone,
    email = EXCLUDED.email,
    address = EXCLUDED.address,
    clinic_id = EXCLUDED.clinic_id,
    updated_at = CURRENT_TIMESTAMP;

-- Crear tabla de usuarios del sistema
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    clinic_id BIGINT NOT NULL,
    professional_id BIGINT,
    
    CONSTRAINT fk_user_clinic 
        FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_professional 
        FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE SET NULL
);

-- Índices para usuarios
CREATE INDEX IF NOT EXISTS idx_users_clinic_id ON users(clinic_id);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Insertar usuarios del sistema (con contraseñas hasheadas)
-- Nota: En producción, usar BCrypt para hashear las contraseñas
INSERT INTO users (username, password_hash, role, clinic_id, professional_id, active, created_at) VALUES
-- Usuarios de Clínica del Corazón
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'ADMIN_CLINIC', 
 (SELECT id FROM clinics WHERE code = 'CLIN001'), NULL, true, CURRENT_TIMESTAMP),

('prof', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'PROFESSIONAL', 
 (SELECT id FROM clinics WHERE code = 'CLIN001'), 
 (SELECT id FROM professionals WHERE license_number = 'LIC001'), true, CURRENT_TIMESTAMP),

-- Usuarios de Centro Neurológico
('admin2', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'ADMIN_CLINIC', 
 (SELECT id FROM clinics WHERE code = 'CLIN002'), NULL, true, CURRENT_TIMESTAMP),

('prof2', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'PROFESSIONAL', 
 (SELECT id FROM clinics WHERE code = 'CLIN002'), 
 (SELECT id FROM professionals WHERE license_number = 'LIC002'), true, CURRENT_TIMESTAMP)

ON CONFLICT (username) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    role = EXCLUDED.role,
    clinic_id = EXCLUDED.clinic_id,
    professional_id = EXCLUDED.professional_id,
    updated_at = CURRENT_TIMESTAMP;

-- Verificar datos insertados
SELECT 'Datos multi-tenant insertados correctamente' as status;

-- Mostrar resumen de datos
SELECT 
    'Clínicas' as tabla,
    COUNT(*) as total
FROM clinics 
WHERE code IN ('CLIN001', 'CLIN002')

UNION ALL

SELECT 
    'Profesionales' as tabla,
    COUNT(*) as total
FROM professionals p
JOIN clinics c ON p.clinic_id = c.id
WHERE c.code IN ('CLIN001', 'CLIN002')

UNION ALL

SELECT 
    'Pacientes' as tabla,
    COUNT(*) as total
FROM patients p
JOIN clinics c ON p.clinic_id = c.id
WHERE c.code IN ('CLIN001', 'CLIN002')

UNION ALL

SELECT 
    'Usuarios' as tabla,
    COUNT(*) as total
FROM users u
JOIN clinics c ON u.clinic_id = c.id
WHERE c.code IN ('CLIN001', 'CLIN002');

-- Mostrar clínicas con sus datos
SELECT 
    c.name as clinica,
    c.code,
    COUNT(DISTINCT p.id) as profesionales,
    COUNT(DISTINCT pat.id) as pacientes,
    COUNT(DISTINCT u.id) as usuarios
FROM clinics c
LEFT JOIN professionals p ON c.id = p.clinic_id
LEFT JOIN patients pat ON c.id = pat.clinic_id  
LEFT JOIN users u ON c.id = u.clinic_id
WHERE c.code IN ('CLIN001', 'CLIN002')
GROUP BY c.id, c.name, c.code
ORDER BY c.code;
