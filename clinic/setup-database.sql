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

-- 2. Conectar a la base de datos con el usuario postgres
\c clinic_db;

-- 3. Mensaje de confirmación
SELECT 'Base de datos clinic_db configurada correctamente' as status;
