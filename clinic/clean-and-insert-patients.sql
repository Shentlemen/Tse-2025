-- Script para limpiar e insertar pacientes de prueba
-- Clínicas: Clínica del Corazón (clinic_id=4) y Centro Neurológico (clinic_id=5)

-- =====================================================
-- PASO 1: ELIMINAR TODOS LOS PACIENTES EXISTENTES
-- =====================================================

DELETE FROM patients;

-- Opcional: Reiniciar la secuencia del ID (solo para pruebas)
ALTER SEQUENCE patients_id_seq RESTART WITH 1;

-- =====================================================
-- PASO 2: INSERTAR PACIENTES DE PRUEBA ACTIVOS
-- =====================================================

-- Pacientes para Clínica del Corazón (clinic_id=4)
INSERT INTO patients (name, last_name, document_number, inus_id, birth_date, gender, phone, email, address, clinic_id, active, created_at) VALUES
('María', 'González', '11223345', 'INUS001', '1990-03-15', 'F', '+598 99 123-4567', 'maria.gonzalez@email.com', 'Av. 18 de Julio 2345, Montevideo',
 4, true, CURRENT_TIMESTAMP),

('Carlos', 'Pérez', '22334455', 'INUS002', '1985-07-22', 'M', '+598 99 234-5678', 'carlos.perez@email.com', 'Pocitos 567, Montevideo',
 4, true, CURRENT_TIMESTAMP),

('Laura', 'Rodríguez', '33445566', 'INUS003', '1992-11-08', 'F', '+598 99 345-6789', 'laura.rodriguez@email.com', 'Carrasco 890, Montevideo',
 4, true, CURRENT_TIMESTAMP),

('Pedro', 'López', '44556677', 'INUS004', '1975-02-14', 'M', '+598 99 456-7890', 'pedro.lopez@email.com', 'Malvín Norte 1234, Montevideo',
 4, true, CURRENT_TIMESTAMP),

('Isabel', 'Fernández', '55667788', 'INUS005', '1988-09-30', 'F', '+598 99 567-8901', 'isabel.fernandez@email.com', 'Cordón 4567, Montevideo',
 4, true, CURRENT_TIMESTAMP),

('Sofía', 'Martínez', '66778899', 'INUS006', '1993-04-18', 'F', '+598 99 678-9012', 'sofia.martinez@email.com', 'Parque Rodó 789, Montevideo',
 4, true, CURRENT_TIMESTAMP),

('Miguel', 'García', '77889900', 'INUS007', '1982-12-05', 'M', '+598 99 789-0123', 'miguel.garcia@email.com', 'Buceo 2345, Montevideo',
 4, true, CURRENT_TIMESTAMP);

-- Pacientes para Centro Neurológico (clinic_id=5)
INSERT INTO patients (name, last_name, document_number, inus_id, birth_date, gender, phone, email, address, clinic_id, active, created_at) VALUES
('Roberto', 'Martínez', '87654321', 'INUS008', '1978-12-03', 'M', '+598 99 999-0000', 'roberto.martinez@email.com', 'Pocitos 789, Montevideo',
 5, true, CURRENT_TIMESTAMP),

('Diego', 'Sánchez', '66778899', 'INUS009', '1983-05-18', 'M', '+598 99 678-9012', 'diego.sanchez@email.com', 'Centro 789, Montevideo',
 5, true, CURRENT_TIMESTAMP),

('Carolina', 'Torres', '77889900', 'INUS010', '1995-01-25', 'F', '+598 99 789-0123', 'carolina.torres@email.com', 'Prado 2345, Montevideo',
 5, true, CURRENT_TIMESTAMP),

('Martín', 'Vásquez', '88990011', 'INUS011', '1980-06-12', 'M', '+598 99 890-1234', 'martin.vasquez@email.com', 'Sayago 6789, Montevideo',
 5, true, CURRENT_TIMESTAMP),

('Patricia', 'Morales', '99001122', 'INUS012', '1993-04-03', 'F', '+598 99 901-2345', 'patricia.morales@email.com', 'Capurro 3456, Montevideo',
 5, true, CURRENT_TIMESTAMP),

('Fernando', 'Gutiérrez', '00112233', 'INUS013', '1977-10-19', 'M', '+598 99 012-3456', 'fernando.gutierrez@email.com', 'Aguada 7890, Montevideo',
 5, true, CURRENT_TIMESTAMP),

('Ana', 'Ruiz', '11223344', 'INUS014', '1989-08-27', 'F', '+598 99 123-4567', 'ana.ruiz@email.com', 'Reducto 4567, Montevideo',
 5, true, CURRENT_TIMESTAMP),

('Ricardo', 'Mendoza', '22334455', 'INUS015', '1984-03-14', 'M', '+598 99 234-5678', 'ricardo.mendoza@email.com', 'Cerro 5678, Montevideo',
 5, true, CURRENT_TIMESTAMP);

-- =====================================================
-- VERIFICACIÓN: Contar pacientes por clínica
-- =====================================================

SELECT 
    c.name as clinica,
    COUNT(p.id) as total_pacientes
FROM clinics c
LEFT JOIN patients p ON c.id = p.clinic_id AND p.active = true
GROUP BY c.id, c.name
ORDER BY c.id;

