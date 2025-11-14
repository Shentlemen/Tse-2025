-- Script para insertar especialidades médicas en la base de datos
-- Ejecutar en la base de datos clinic_db
-- Uso: psql -U clinic_user -d clinic_db -f insert-specialties.sql

-- Insertar especialidades médicas (evitar duplicados con ON CONFLICT)
INSERT INTO specialties (name, code, description, active) VALUES
-- Especialidades básicas y comunes
('Medicina General', 'MG', 'Atención médica general y preventiva', true),
('Medicina Familiar', 'MF', 'Atención integral a la familia', true),
('Cardiología', 'CAR', 'Especialidad en enfermedades del corazón y sistema cardiovascular', true),
('Neurología', 'NEU', 'Especialidad en enfermedades del sistema nervioso', true),
('Psiquiatría', 'PSQ', 'Especialidad en salud mental y trastornos psiquiátricos', true),
('Psicología Clínica', 'PSIC', 'Especialidad en salud mental y terapia psicológica', true),
('Dermatología', 'DER', 'Especialidad en enfermedades de la piel', true),
('Ginecología y Obstetricia', 'GYN', 'Especialidad en salud femenina y embarazo', true),
('Pediatría', 'PED', 'Especialidad en medicina infantil y del adolescente', true),
('Geriatría', 'GER', 'Especialidad en salud del adulto mayor', true),

-- Especialidades quirúrgicas
('Cirugía General', 'CG', 'Cirugía de enfermedades generales', true),
('Cirugía Plástica', 'CP', 'Cirugía reconstructiva y estética', true),
('Neurocirugía', 'NC', 'Cirugía del sistema nervioso', true),
('Cirugía Cardiovascular', 'CCV', 'Cirugía del corazón y vasos sanguíneos', true),
('Urología', 'URO', 'Especialidad en sistema urinario y genital masculino', true),
('Oftalmología', 'OFT', 'Especialidad en salud ocular y cirugía de ojos', true),
('Otorrinolaringología', 'ORL', 'Especialidad en oído, nariz y garganta', true),

-- Especialidades diagnósticas
('Radiología', 'RAD', 'Diagnóstico por imágenes', true),
('Anatomía Patológica', 'AP', 'Diagnóstico mediante estudio de tejidos', true),
('Medicina Nuclear', 'MN', 'Diagnóstico y tratamiento con isótopos radioactivos', true),
('Laboratorio Clínico', 'LAB', 'Análisis clínicos y diagnósticos', true),

-- Especialidades internas
('Gastroenterología', 'GAST', 'Especialidad en sistema digestivo', true),
('Neumología', 'NEUM', 'Especialidad en enfermedades respiratorias', true),
('Nefrología', 'NEF', 'Especialidad en riñones y enfermedades renales', true),
('Endocrinología', 'END', 'Especialidad en glándulas y hormonas', true),
('Reumatología', 'REU', 'Especialidad en enfermedades reumáticas', true),
('Hematología', 'HEM', 'Especialidad en sangre y órganos hematopoyéticos', true),
('Oncología', 'ONC', 'Especialidad en tratamiento del cáncer', true),

-- Especialidades quirúrgicas específicas
('Traumatología y Ortopedia', 'TRA', 'Especialidad en sistema musculoesquelético', true),
('Cirugía Pediátrica', 'CPED', 'Cirugía en pacientes pediátricos', true),
('Cirugía de Tórax', 'CT', 'Cirugía del tórax y pulmones', true),

-- Especialidades de emergencia y cuidado crítico
('Medicina de Emergencias', 'EMER', 'Atención médica de urgencias', true),
('Medicina Intensiva', 'MI', 'Cuidado crítico y terapia intensiva', true),

-- Otras especialidades
('Medicina del Trabajo', 'MT', 'Salud ocupacional y medicina laboral', true),
('Medicina Deportiva', 'MD', 'Medicina del deporte y actividad física', true),
('Alergología', 'ALER', 'Especialidad en alergias e inmunología', true),
('Infectología', 'INF', 'Especialidad en enfermedades infecciosas', true),
('Medicina Física y Rehabilitación', 'FIS', 'Rehabilitación y fisiatría', true),
('Anestesiología', 'ANE', 'Anestesia y cuidados perioperatorios', true)

ON CONFLICT (name) DO NOTHING;

-- Verificar que se insertaron correctamente
SELECT COUNT(*) as total_especialidades FROM specialties WHERE active = true;

-- Mostrar todas las especialidades insertadas
SELECT id, code, name, description FROM specialties ORDER BY name;

