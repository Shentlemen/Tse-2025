-- =====================================================
-- SCRIPT DE CORRECCIÓN FINAL PARA TRIGGER
-- Proyecto: HCEN Componente Periférico de Clínica
-- =====================================================

-- Crear la función update_users_updated_at
CREATE OR REPLACE FUNCTION update_users_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Crear el trigger
DROP TRIGGER IF EXISTS trigger_users_updated_at ON users;
CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_users_updated_at();

-- Verificar que todo esté funcionando
DO $$
DECLARE
    column_count INTEGER;
    trigger_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO column_count 
    FROM information_schema.columns 
    WHERE table_name = 'users' AND table_schema = 'public';
    
    SELECT COUNT(*) INTO trigger_count 
    FROM information_schema.triggers 
    WHERE event_object_table = 'users' AND trigger_name = 'trigger_users_updated_at';
    
    RAISE NOTICE '========================================';
    RAISE NOTICE 'CORRECCIÓN FINAL COMPLETADA';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Tabla users tiene % columnas', column_count;
    RAISE NOTICE 'Trigger creado: %', CASE WHEN trigger_count > 0 THEN 'SÍ' ELSE 'NO' END;
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Ahora puede ejecutar el script de inicialización';
    RAISE NOTICE '========================================';
END $$;
