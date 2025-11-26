-- Script para crear usuario administrador con password 'admin123'
-- Hash BCrypt generado usando el código Java (org.mindrot.jbcrypt.BCrypt)
-- Este hash es 100% compatible con el código de la aplicación

-- Insertar usuario administrador
-- Hash generado con: PasswordUtil.hashPassword("admin123")
INSERT INTO users (
    username,
    password,
    role,
    email,
    first_name,
    last_name,
    active,
    clinic_id,
    created_at
) VALUES (
    'admin',
    '$2a$10$1JBUPDDImx1dpBkbkTVwUucZXwng/DyIdM3QziH2h7M4Bm5k8eL/i',  -- Hash BCrypt para 'admin123'
    'ADMIN_CLINIC',
    'admin@clinic.com',
    'Administrador',
    'Sistema',
    TRUE,
    '1',
    CURRENT_TIMESTAMP
)
ON CONFLICT (username) DO UPDATE
SET 
    password = '$2a$10$1JBUPDDImx1dpBkbkTVwUucZXwng/DyIdM3QziH2h7M4Bm5k8eL/i',  -- Hash BCrypt para 'admin123'
    role = 'ADMIN_CLINIC',
    email = 'admin@clinic.com',
    first_name = 'Administrador',
    last_name = 'Sistema',
    active = TRUE,
    clinic_id = '1',
    updated_at = CURRENT_TIMESTAMP;

-- Verificar que se creó correctamente
SELECT 
    id,
    username,
    role,
    email,
    first_name,
    last_name,
    active,
    clinic_id,
    created_at
FROM users
WHERE username = 'admin';

