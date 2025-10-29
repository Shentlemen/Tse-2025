-- Script para probar diferentes contraseñas
-- Vamos a crear un usuario de prueba con contraseña conocida

-- Primero, vamos a ver el hash actual de Gerberto
SELECT 
    'HASH ACTUAL DE GERBERTO' as info,
    username,
    password,
    LENGTH(password) as hash_length
FROM users 
WHERE username = 'L77';

-- Vamos a crear un usuario de prueba con contraseña prof123
-- Primero eliminamos si existe
DELETE FROM users WHERE username = 'TEST123';

-- Insertamos usuario de prueba
INSERT INTO users (
    username, 
    password, 
    role, 
    email, 
    first_name, 
    last_name, 
    clinic_id, 
    active, 
    created_at
) VALUES (
    'TEST123',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- Este es el hash de prof123 que funciona
    'PROFESSIONAL',
    'test@test.com',
    'Test',
    'User',
    4,
    true,
    NOW()
);

-- Verificar que se creó
SELECT 
    'USUARIO DE PRUEBA CREADO' as info,
    username,
    password,
    LENGTH(password) as hash_length
FROM users 
WHERE username = 'TEST123';
