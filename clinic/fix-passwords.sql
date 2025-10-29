-- =====================================================
-- SCRIPT PARA ACTUALIZAR CONTRASEÑAS CON HASH CORRECTO
-- Proyecto: HCEN Componente Periférico de Clínica
-- =====================================================

-- Este script actualiza las contraseñas con el hash BCrypt correcto

-- Verificar contraseñas actuales
SELECT 'CONTRASEÑAS ACTUALES:' as info;
SELECT username, password, LENGTH(password) as longitud
FROM users 
WHERE active = true
ORDER BY username;

-- Actualizar contraseñas con hash BCrypt correcto
-- Contraseña: admin123 -> Hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi

UPDATE users 
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi'
WHERE username IN ('admin', 'admin2', 'prof', 'prof2') 
AND active = true;

-- Verificar contraseñas después de la actualización
SELECT 'CONTRASEÑAS DESPUÉS DE LA ACTUALIZACIÓN:' as info;
SELECT username, password, LENGTH(password) as longitud
FROM users 
WHERE active = true
ORDER BY username;

-- Mensaje de confirmación
DO $$
DECLARE
    updated_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO updated_count 
    FROM users 
    WHERE password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi'
    AND active = true;
    
    RAISE NOTICE '';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'CONTRASEÑAS ACTUALIZADAS';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Usuarios actualizados: %', updated_count;
    RAISE NOTICE 'Contraseña para todos: admin123';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Ahora puede probar el login nuevamente';
    RAISE NOTICE '========================================';
END $$;
