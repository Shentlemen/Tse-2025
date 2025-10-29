-- =====================================================
-- ESQUEMA COMPLETO DE BASE DE DATOS - SISTEMA MULTI-TENANT
-- Proyecto: HCEN Componente Periférico de Clínica
-- =====================================================

-- Base de datos: clinic_db
-- Usuario: clinic_user
-- Contraseña: clinic_pass

-- =====================================================
-- 1. TABLA DE CLÍNICAS (MULTI-TENANT)
-- =====================================================
CREATE TABLE IF NOT EXISTS clinics (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(255),
    hcen_endpoint VARCHAR(500),
    logo_path VARCHAR(500),
    theme_colors TEXT,  -- JSON string para colores personalizados
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Comentarios
COMMENT ON TABLE clinics IS 'Tabla de clínicas para el sistema multi-tenant';
COMMENT ON COLUMN clinics.code IS 'Código único identificador de la clínica';
COMMENT ON COLUMN clinics.hcen_endpoint IS 'Endpoint para integración con HCEN';
COMMENT ON COLUMN clinics.theme_colors IS 'Colores personalizados en formato JSON';

-- =====================================================
-- 2. TABLA DE ESPECIALIDADES MÉDICAS
-- =====================================================
CREATE TABLE IF NOT EXISTS specialties (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    code VARCHAR(10),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

COMMENT ON TABLE specialties IS 'Especialidades médicas disponibles';

-- =====================================================
-- 3. TABLA DE PROFESIONALES
-- =====================================================
CREATE TABLE IF NOT EXISTS professionals (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    license_number VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    clinic_id BIGINT NOT NULL,
    specialty_id BIGINT NOT NULL,
    
    -- Foreign keys
    CONSTRAINT fk_professional_clinic 
        FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE CASCADE,
    CONSTRAINT fk_professional_specialty 
        FOREIGN KEY (specialty_id) REFERENCES specialties(id) ON DELETE RESTRICT
);

-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_professionals_clinic_id ON professionals(clinic_id);
CREATE INDEX IF NOT EXISTS idx_professionals_specialty_id ON professionals(specialty_id);
CREATE INDEX IF NOT EXISTS idx_professionals_email ON professionals(email);
CREATE INDEX IF NOT EXISTS idx_professionals_license ON professionals(license_number);
CREATE INDEX IF NOT EXISTS idx_professionals_active ON professionals(active);

COMMENT ON TABLE professionals IS 'Profesionales de salud registrados en el sistema';
COMMENT ON COLUMN professionals.license_number IS 'Número de matrícula profesional';
COMMENT ON COLUMN professionals.active IS 'Indica si el profesional está activo en el sistema';

-- =====================================================
-- 4. TABLA DE PACIENTES
-- =====================================================
CREATE TABLE IF NOT EXISTS patients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    inus_id VARCHAR(50) UNIQUE,  -- ID en el INUS del HCEN central
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

COMMENT ON TABLE patients IS 'Pacientes registrados en el sistema multi-tenant';
COMMENT ON COLUMN patients.inus_id IS 'ID único en el Índice Nacional de Usuarios de Salud';

-- =====================================================
-- 5. TABLA DE USUARIOS DEL SISTEMA
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,  -- En producción usar hash BCrypt
    role VARCHAR(50) NOT NULL,       -- ADMIN_CLINIC, PROFESSIONAL
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

COMMENT ON TABLE users IS 'Usuarios del sistema con acceso a portales';
COMMENT ON COLUMN users.role IS 'Rol del usuario: ADMIN_CLINIC o PROFESSIONAL';

-- =====================================================
-- 6. DATOS INICIALES - ESPECIALIDADES
-- =====================================================
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
('Urología', 'URO', 'Especialidad en sistema urinario y genital masculino', true),
('Neurología', 'NEURO', 'Especialidad médica del sistema nervioso', true)
ON CONFLICT (name) DO NOTHING;

-- =====================================================
-- 7. DATOS INICIALES - CLÍNICAS MULTI-TENANT
-- =====================================================
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

-- =====================================================
-- 8. DATOS INICIALES - PROFESIONALES
-- =====================================================
INSERT INTO professionals (name, last_name, email, license_number, phone, clinic_id, specialty_id, active, created_at) VALUES
-- Dr. Juan Pérez - Cardiología (Clínica del Corazón)
('Dr. Juan', 'Pérez', 'jperez@clinicacorazon.com.uy', 'LIC001', '+598 99 111-2222', 
 (SELECT id FROM clinics WHERE code = 'CLIN001'), 
 (SELECT id FROM specialties WHERE code = 'CAR'), true, CURRENT_TIMESTAMP),

-- Dr. Carlos Rodríguez - Pediatría (Clínica del Corazón)
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

-- =====================================================
-- 9. DATOS INICIALES - PACIENTES
-- =====================================================
INSERT INTO patients (name, last_name, document_number, inus_id, birth_date, gender, phone, email, address, clinic_id, active, created_at) VALUES
-- Pacientes de Clínica del Corazón
('Ana', 'Silva', '12345678', 'INUS001', '1985-05-15', 'F', '+598 99 777-8888', 'ana.silva@email.com', 'Av. Italia 3456, Montevideo', 
 (SELECT id FROM clinics WHERE code = 'CLIN001'), true, CURRENT_TIMESTAMP),

('Lucía', 'Fernández', '11223344', 'INUS003', '1992-08-22', 'F', '+598 99 111-3333', 'lucia.fernandez@email.com', 'Carrasco 456, Montevideo',
 (SELECT id FROM clinics WHERE code = 'CLIN001'), true, CURRENT_TIMESTAMP),

-- Pacientes de Centro Neurológico
('Roberto', 'Martínez', '87654321', 'INUS005', '1978-12-03', 'M', '+598 99 999-0000', 'roberto.martinez@email.com', 'Pocitos 789, Montevideo',
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

-- =====================================================
-- 10. DATOS INICIALES - USUARIOS DEL SISTEMA
-- =====================================================
INSERT INTO users (username, password, role, clinic_id, professional_id, active, created_at) VALUES
-- Usuarios de Clínica del Corazón
('admin', 'admin123', 'ADMIN_CLINIC', 
 (SELECT id FROM clinics WHERE code = 'CLIN001'), NULL, true, CURRENT_TIMESTAMP),

('prof', 'prof123', 'PROFESSIONAL', 
 (SELECT id FROM clinics WHERE code = 'CLIN001'), 
 (SELECT id FROM professionals WHERE license_number = 'LIC001'), true, CURRENT_TIMESTAMP),

-- Usuarios de Centro Neurológico
('admin2', 'admin456', 'ADMIN_CLINIC', 
 (SELECT id FROM clinics WHERE code = 'CLIN002'), NULL, true, CURRENT_TIMESTAMP),

('prof2', 'prof456', 'PROFESSIONAL', 
 (SELECT id FROM clinics WHERE code = 'CLIN002'), 
 (SELECT id FROM professionals WHERE license_number = 'LIC002'), true, CURRENT_TIMESTAMP)

ON CONFLICT (username) DO UPDATE SET
    password = EXCLUDED.password,
    role = EXCLUDED.role,
    clinic_id = EXCLUDED.clinic_id,
    professional_id = EXCLUDED.professional_id,
    updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- 11. CONSULTAS DE VERIFICACIÓN
-- =====================================================

-- Verificar datos multi-tenant
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

-- Mostrar distribución por clínicas
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

-- =====================================================
-- 12. USUARIOS DE PRUEBA
-- =====================================================
/*
CLÍNICA DEL CORAZÓN (CLIN001):
- Admin: admin / admin123
- Profesional: prof / prof123

CENTRO NEUROLÓGICO (CLIN002):
- Admin: admin2 / admin456  
- Profesional: prof2 / prof456
*/

-- =====================================================
-- FIN DEL ESQUEMA
-- =====================================================
