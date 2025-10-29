-- Script para configurar la base de datos PostgreSQL para el proyecto Clinic
-- Ejecutar como superusuario de PostgreSQL

-- 1. Crear la base de datos
CREATE DATABASE clinic_db 
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'es_UY.UTF-8'
    LC_CTYPE = 'es_UY.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- 2. Crear usuario específico para la aplicación
CREATE USER clinic_user WITH PASSWORD 'clinic_pass';

-- 3. Otorgar permisos al usuario
GRANT ALL PRIVILEGES ON DATABASE clinic_db TO clinic_user;

-- 4. Conectar a la base de datos
\c clinic_db;

-- 5. Otorgar permisos en el esquema público
GRANT ALL ON SCHEMA public TO clinic_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO clinic_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO clinic_user;

-- 6. Configurar permisos por defecto para objetos futuros
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO clinic_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO clinic_user;

-- 7. Ejecutar el script de creación de tablas
\i src/main/resources/db/schema.sql

-- Mensaje de confirmación
SELECT 'Base de datos clinic_db configurada correctamente' as status;
