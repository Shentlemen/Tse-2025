-- Script de creación de base de datos para el sistema de gestión clínica
-- PostgreSQL Database Schema

-- Crear la base de datos (ejecutar como superusuario)
-- CREATE DATABASE clinic_db;
-- \c clinic_db;

-- Tabla de clínicas (multi-tenant)
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
    theme_colors TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Tabla de especialidades médicas
CREATE TABLE IF NOT EXISTS specialties (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    code VARCHAR(10),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla de profesionales
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

-- Insertar datos iniciales

-- Especialidades médicas básicas
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

-- Clínica de ejemplo
INSERT INTO clinics (name, code, description, address, phone, email, active, created_at) VALUES
('Clínica San José', 'CSJ', 'Clínica privada de atención médica integral', 
 'Av. 18 de Julio 1234, Montevideo', '098123456', 'info@clinicasanjose.com.uy', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Profesionales de ejemplo (opcional, para pruebas)
INSERT INTO professionals (name, last_name, email, license_number, phone, clinic_id, specialty_id, active, created_at) VALUES
('Juan Carlos', 'Pérez', 'juan.perez@clinicasanjose.com.uy', 'LP12345', '098123456', 
 (SELECT id FROM clinics WHERE code = 'CSJ'), 
 (SELECT id FROM specialties WHERE code = 'MG'), true, CURRENT_TIMESTAMP),
('María Elena', 'González', 'maria.gonzalez@clinicasanjose.com.uy', 'LP67890', '099234567',
 (SELECT id FROM clinics WHERE code = 'CSJ'),
 (SELECT id FROM specialties WHERE code = 'CAR'), true, CURRENT_TIMESTAMP)
ON CONFLICT (license_number) DO NOTHING;

-- Comentarios sobre la estructura
COMMENT ON TABLE clinics IS 'Tabla de clínicas para el sistema multi-tenant';
COMMENT ON TABLE specialties IS 'Especialidades médicas disponibles';
COMMENT ON TABLE professionals IS 'Profesionales de salud registrados en el sistema';

COMMENT ON COLUMN clinics.code IS 'Código único identificador de la clínica';
COMMENT ON COLUMN clinics.hcen_endpoint IS 'Endpoint para integración con HCEN';
COMMENT ON COLUMN clinics.theme_colors IS 'Colores personalizados en formato JSON';

COMMENT ON COLUMN professionals.license_number IS 'Número de matrícula profesional';
COMMENT ON COLUMN professionals.active IS 'Indica si el profesional está activo en el sistema';
