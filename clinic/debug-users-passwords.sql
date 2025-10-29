-- Verificar usuarios y sus contraseñas
SELECT 
    id,
    username,
    role,
    email,
    first_name,
    last_name,
    clinic_id,
    professional_id,
    active,
    created_at,
    last_login,
    -- Mostrar solo los primeros caracteres del hash para verificar que no esté vacío
    CASE 
        WHEN password IS NULL THEN 'NULL'
        WHEN password = '' THEN 'EMPTY'
        ELSE 'HASHED (' || LENGTH(password) || ' chars)'
    END as password_status,
    SUBSTRING(password, 1, 10) as password_preview
FROM users 
WHERE active = true
ORDER BY id;

-- Verificar específicamente el usuario Gerberto
SELECT 
    'USUARIO GERBERTO' as info,
    id,
    username,
    role,
    email,
    first_name,
    last_name,
    clinic_id,
    professional_id,
    active,
    CASE 
        WHEN password IS NULL THEN 'NULL'
        WHEN password = '' THEN 'EMPTY'
        ELSE 'HASHED (' || LENGTH(password) || ' chars)'
    END as password_status
FROM users 
WHERE username = 'L77' OR first_name = 'Gerberto';

-- Verificar clínicas para entender el filtrado
SELECT 
    'CLINICAS' as info,
    id,
    name,
    code
FROM clinics 
ORDER BY id;
