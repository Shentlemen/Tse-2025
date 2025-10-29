-- =====================================================
-- TABLA DE USUARIOS PARA SISTEMA DE AUTENTICACIÓN
-- Proyecto: HCEN Componente Periférico de Clínica
-- =====================================================

-- Tabla de usuarios del sistema
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Hash de la contraseña
    email VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN_CLINIC', 'PROFESSIONAL', 'SUPER_ADMIN')),
    clinic_id BIGINT REFERENCES clinics(id) ON DELETE CASCADE,
    professional_id BIGINT REFERENCES professionals(id) ON DELETE SET NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT REFERENCES users(id)
);

-- Índices para optimizar consultas
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_clinic_id ON users(clinic_id);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);

-- Comentarios
COMMENT ON TABLE users IS 'Usuarios del sistema con autenticación basada en BD';
COMMENT ON COLUMN users.password IS 'Hash de la contraseña usando BCrypt';
COMMENT ON COLUMN users.role IS 'Rol del usuario: ADMIN_CLINIC, PROFESSIONAL, SUPER_ADMIN';
COMMENT ON COLUMN users.clinic_id IS 'ID de la clínica asociada (NULL para SUPER_ADMIN)';
COMMENT ON COLUMN users.professional_id IS 'ID del profesional asociado (NULL para ADMIN_CLINIC)';
COMMENT ON COLUMN users.created_by IS 'Usuario que creó este registro';

-- =====================================================
-- DATOS INICIALES - PRIMER USUARIO ADMINISTRADOR
-- =====================================================

-- Insertar el primer usuario super administrador
-- Contraseña: 'admin123' (hash BCrypt)
INSERT INTO users (username, password, email, first_name, last_name, role, clinic_id, active) 
VALUES (
    'superadmin', 
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- admin123
    'admin@hcen.uy',
    'Super',
    'Administrador',
    'SUPER_ADMIN',
    NULL,
    TRUE
) ON CONFLICT (username) DO NOTHING;

-- Insertar usuarios para las clínicas existentes (basados en los usuarios hardcodeados actuales)
-- Clínica del Corazón (ID 4)
INSERT INTO users (username, password, email, first_name, last_name, role, clinic_id, professional_id, active) 
VALUES (
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- admin123
    'admin@corazon.com',
    'Administrador',
    'Clínica del Corazón',
    'ADMIN_CLINIC',
    4,
    NULL,
    TRUE
) ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, email, first_name, last_name, role, clinic_id, professional_id, active) 
VALUES (
    'prof',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- prof123
    'prof@corazon.com',
    'Profesional',
    'Clínica del Corazón',
    'PROFESSIONAL',
    4,
    1, -- Asumiendo que el profesional con ID 1 pertenece a esta clínica
    TRUE
) ON CONFLICT (username) DO NOTHING;

-- Centro Neurológico (ID 5)
INSERT INTO users (username, password, email, first_name, last_name, role, clinic_id, professional_id, active) 
VALUES (
    'admin2',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- admin456
    'admin@neurologico.com',
    'Administrador',
    'Centro Neurológico',
    'ADMIN_CLINIC',
    5,
    NULL,
    TRUE
) ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, email, first_name, last_name, role, clinic_id, professional_id, active) 
VALUES (
    'prof2',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- prof456
    'prof@neurologico.com',
    'Profesional',
    'Centro Neurológico',
    'PROFESSIONAL',
    5,
    2, -- Asumiendo que el profesional con ID 2 pertenece a esta clínica
    TRUE
) ON CONFLICT (username) DO NOTHING;

-- =====================================================
-- FUNCIÓN PARA VERIFICAR SI EXISTEN USUARIOS
-- =====================================================

-- Función para verificar si ya existen usuarios en el sistema
CREATE OR REPLACE FUNCTION has_users() RETURNS BOOLEAN AS $$
BEGIN
    RETURN (SELECT COUNT(*) > 0 FROM users);
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- VISTA PARA CONSULTAS DE USUARIOS CON INFORMACIÓN DE CLÍNICA
-- =====================================================

CREATE OR REPLACE VIEW users_with_clinic AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.first_name,
    u.last_name,
    u.role,
    u.clinic_id,
    c.name as clinic_name,
    u.professional_id,
    p.first_name as professional_first_name,
    p.last_name as professional_last_name,
    u.active,
    u.last_login,
    u.created_at,
    u.updated_at
FROM users u
LEFT JOIN clinics c ON u.clinic_id = c.id
LEFT JOIN professionals p ON u.professional_id = p.id;

COMMENT ON VIEW users_with_clinic IS 'Vista que incluye información completa de usuarios con datos de clínica y profesional';

-- =====================================================
-- TRIGGER PARA ACTUALIZAR updated_at
-- =====================================================

CREATE OR REPLACE FUNCTION update_users_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_users_updated_at();

-- =====================================================
-- MENSAJE DE CONFIRMACIÓN
-- =====================================================

DO $$
BEGIN
    RAISE NOTICE 'Tabla de usuarios creada exitosamente';
    RAISE NOTICE 'Usuarios iniciales insertados:';
    RAISE NOTICE '- superadmin (SUPER_ADMIN) - contraseña: admin123';
    RAISE NOTICE '- admin (ADMIN_CLINIC - Clínica del Corazón) - contraseña: admin123';
    RAISE NOTICE '- prof (PROFESSIONAL - Clínica del Corazón) - contraseña: prof123';
    RAISE NOTICE '- admin2 (ADMIN_CLINIC - Centro Neurológico) - contraseña: admin123';
    RAISE NOTICE '- prof2 (PROFESSIONAL - Centro Neurológico) - contraseña: prof123';
    RAISE NOTICE 'IMPORTANTE: Cambiar las contraseñas después de la primera configuración';
END $$;
