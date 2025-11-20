@echo off
REM =============================================================================
REM HCEN Setup Script - Windows
REM =============================================================================
REM Este script configura y levanta HCEN con su base de datos
REM =============================================================================

echo ========================================
echo HCEN Setup Script
echo ========================================
echo.

REM Step 1: Verificar Docker
echo [1/6] Verificando Docker...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker Desktop no esta corriendo!
    echo Por favor inicia Docker Desktop y vuelve a ejecutar este script.
    pause
    exit /b 1
)
echo ✓ Docker esta corriendo
echo.

REM Step 2: Levantar servicios con docker-compose
echo [2/6] Levantando servicios (PostgreSQL, MongoDB, Redis)...
docker-compose -f docker-compose-postgres.yml up -d
if %errorlevel% neq 0 (
    echo ERROR: No se pudieron levantar los servicios de Docker!
    pause
    exit /b 1
)
echo ✓ Servicios levantados
echo.

REM Step 3: Esperar a que PostgreSQL este listo
echo [3/6] Esperando a que PostgreSQL este listo...
timeout /t 10 /nobreak >nul
echo ✓ PostgreSQL deberia estar listo
echo.

REM Step 4: Aplicar migraciones con Flyway
echo [4/6] Aplicando migraciones de base de datos...
cd %~dp0
gradlew.bat flywayMigrate
if %errorlevel% neq 0 (
    echo ERROR: Las migraciones fallaron!
    echo Verifica los mensajes de error arriba.
    pause
    exit /b 1
)
echo ✓ Migraciones aplicadas exitosamente
echo.

REM Step 5: Verificar migraciones
echo [5/6] Verificando estado de las migraciones...
gradlew.bat flywayInfo
echo.

REM Step 6: Resumen
echo ========================================
echo Setup completado!
echo ========================================
echo.
echo Servicios corriendo:
echo - PostgreSQL: localhost:5432 (usuario: postgres, password: postgres, DB: hcen)
echo - MongoDB: localhost:27017 (usuario: admin, password: admin123, DB: hcen)
echo - Redis: localhost:6379 (password: redis123)
echo.
echo Proximos pasos:
echo 1. Configurar datasource HcenDS en WildFly (ver instrucciones abajo)
echo 2. Levantar WildFly: .\standalone.bat --server-config=standalone-full.xml
echo 3. Desplegar HCEN: gradlew.bat war (y copiar a deployments/)
echo.
echo ========================================
echo Configuracion del datasource en WildFly:
echo ========================================
echo.
echo Necesitas agregar este datasource en standalone-full.xml:
echo.
echo ^<datasource jndi-name="java:jboss/datasources/HcenDS" pool-name="HcenDS" enabled="true"^>
echo     ^<connection-url^>jdbc:postgresql://localhost:5432/hcen^</connection-url^>
echo     ^<driver^>postgresql^</driver^>
echo     ^<security user-name="postgres" password="postgres"/^>
echo ^</datasource^>
echo.
echo El driver de PostgreSQL ya deberia estar configurado.
echo.
pause


