-- Script para agregar clinic_id a la tabla specialties y poblar datos existentes

-- 1. Agregar columna clinic_id a la tabla specialties
ALTER TABLE specialties 
ADD COLUMN clinic_id BIGINT;

-- 2. Agregar foreign key constraint
ALTER TABLE specialties 
ADD CONSTRAINT fk_specialties_clinic 
FOREIGN KEY (clinic_id) REFERENCES clinics(id);

-- 3. Crear índice para mejorar performance
CREATE INDEX idx_specialties_clinic_id ON specialties(clinic_id);

-- 4. Poblar datos existentes - asignar especialidades a clínicas específicas
-- Asignar especialidades básicas a todas las clínicas
UPDATE specialties 
SET clinic_id = 1 
WHERE id IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 16);

-- Asignar especialidades específicas a Clínica del Corazón (ID: 4)
INSERT INTO specialties (name, description, code, active, clinic_id) VALUES
('Cardiología Intervencionista', 'Procedimientos invasivos del corazón', 'CAR_INT', true, 4),
('Electrofisiología', 'Estudios eléctricos del corazón', 'ELECTRO', true, 4);

-- Asignar especialidades específicas a Centro Neurológico (ID: 5)
INSERT INTO specialties (name, description, code, active, clinic_id) VALUES
('Neurocirugía', 'Cirugía del sistema nervioso', 'NEURO_CIR', true, 5),
('Neuropsicología', 'Evaluación neuropsicológica', 'NEURO_PSI', true, 5),
('Epileptología', 'Especialidad en epilepsia', 'EPILEP', true, 5);

-- 5. Verificar los datos
SELECT 
    'ESPECIALIDADES POR CLÍNICA' as categoria,
    c.id as clinic_id,
    c.name as clinic_name,
    COUNT(s.id) as especialidades_count
FROM clinics c
LEFT JOIN specialties s ON c.id = s.clinic_id AND s.active = true
GROUP BY c.id, c.name
ORDER BY c.id;

-- 6. Detalle de especialidades con clínica
SELECT 
    'DETALLE DE ESPECIALIDADES CON CLÍNICA' as categoria,
    s.id, 
    s.name, 
    s.description,
    s.code,
    s.active,
    c.name as clinic_name
FROM specialties s
LEFT JOIN clinics c ON s.clinic_id = c.id
ORDER BY s.clinic_id, s.name;
