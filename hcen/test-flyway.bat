@echo off
REM ==============================================
REM Flyway Migration Test Script - Windows
REM ==============================================
REM This script tests the Flyway configuration
REM for the HCEN project

echo.
echo ================================================
echo HCEN Flyway Migration Test
echo ================================================
echo.

REM Step 1: Check if Docker is running
echo [1/5] Checking Docker status...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker is not running!
    echo Please start Docker Desktop and try again.
    exit /b 1
)
echo ✓ Docker is running

echo.
echo [2/5] Starting PostgreSQL container...
docker-compose -f docker-compose-postgres.yml up -d postgres
if %errorlevel% neq 0 (
    echo ERROR: Failed to start PostgreSQL container!
    exit /b 1
)
echo ✓ PostgreSQL container started

echo.
echo [3/5] Waiting for PostgreSQL to be ready...
timeout /t 10 /nobreak >nul
echo ✓ PostgreSQL should be ready

echo.
echo [4/5] Running Flyway Info (checking migration status)...
gradlew.bat flywayInfo
if %errorlevel% neq 0 (
    echo WARNING: flywayInfo failed. This might be normal if migrations haven't been applied yet.
)

echo.
echo [5/5] Running Flyway Migrate (applying migrations)...
gradlew.bat flywayMigrate
if %errorlevel% neq 0 (
    echo ERROR: Flyway migration failed!
    echo Check the error messages above.
    exit /b 1
)
echo ✓ Migrations applied successfully

echo.
echo ================================================
echo Flyway Migration Test PASSED!
echo ================================================
echo.
echo Next steps:
echo 1. Verify migrations: gradlew.bat flywayInfo
echo 2. Connect to database: docker exec -it hcen-postgres psql -U postgres -d hcen
echo 3. Build project: gradlew.bat build
echo.

pause
