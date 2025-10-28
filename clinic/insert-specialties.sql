-- =====================================================
-- INSERTAR ESPECIALIDADES MÉDICAS
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

-- Verificar datos insertados
SELECT id, name, code, description, active 
FROM specialties 
ORDER BY name;

