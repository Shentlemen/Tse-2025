-- Script para insertar pacientes de prueba
-- Clínicas: Clínica del Corazón (CLIN001) y Centro Neurológico (CLIN002)

-- Limpiar pacientes de prueba anteriores (opcional)
-- DELETE FROM patients WHERE inus_id LIKE 'TEST%';

-- Pacientes para Clínica del Corazón (clinic_id=1)
INSERT INTO patients (name, last_name, document_number, inus_id, birth_date, gender, phone, email, address, clinic_id, active, created_at) VALUES
-- Pacientes activos
('María', 'González', '11223345', 'TEST001', '1990-03-15', 'F', '+598 99 123-4567', 'maria.gonzalez@email.com', 'Av. 18 de Julio 2345, Montevideo',
 1, true, CURRENT_TIMESTAMP),

('Carlos', 'Pérez', '22334455', 'TEST002', '1985-07-22', 'M', '+598 99 234-5678', 'carlos.perez@email.com', 'Pocitos 567, Montevideo',
 1, true, CURRENT_TIMESTAMP),

('Laura', 'Rodríguez', '33445566', 'TEST003', '1992-11-08', 'F', '+598 99 345-6789', 'laura.rodriguez@email.com', 'Carrasco 890, Montevideo',
 1, true, CURRENT_TIMESTAMP),

('Pedro', 'López', '44556677', 'TEST004', '1975-02-14', 'M', '+598 99 456-7890', 'pedro.lopez@email.com', 'Malvín Norte 1234, Montevideo',
 1, true, CURRENT_TIMESTAMP),

('Isabel', 'Fernández', '55667788', 'TEST005', '1988-09-30', 'F', '+598 99 567-8901', 'isabel.fernandez@email.com', 'Cordón 4567, Montevideo',
 1, true, CURRENT_TIMESTAMP),

-- Pacientes para Centro Neurológico (clinic_id=5)
('Diego', 'Sánchez', '66778899', 'TEST006', '1983-05-18', 'M', '+598 99 678-9012', 'diego.sanchez@email.com', 'Centro 789, Montevideo',
 5, true, CURRENT_TIMESTAMP),

('Carolina', 'Torres', '77889900', 'TEST007', '1995-01-25', 'F', '+598 99 789-0123', 'carolina.torres@email.com', 'Prado 2345, Montevideo',
 5, true, CURRENT_TIMESTAMP),

('Martín', 'Vásquez', '88990011', 'TEST008', '1980-06-12', 'M', '+598 99 890-1234', 'martin.vasquez@email.com', 'Sayago 6789, Montevideo',
 5, true, CURRENT_TIMESTAMP),

('Patricia', 'Morales', '99001122', 'TEST009', '1993-04-03', 'F', '+598 99 901-2345', 'patricia.morales@email.com', 'Capurro 3456, Montevideo',
 5, true, CURRENT_TIMESTAMP),

('Fernando', 'Gutiérrez', '00112233', 'TEST010', '1977-10-19', 'M', '+598 99 012-3456', 'fernando.gutierrez@email.com', 'Aguada 7890, Montevideo',
 5, true, CURRENT_TIMESTAMP)

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
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP;

