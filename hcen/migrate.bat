@echo off
REM =============================================================================
REM HCEN Database Migration Helper Script
REM =============================================================================
REM This script executes migrations inside the PostgreSQL Docker container
REM =============================================================================

echo ========================================
echo HCEN Database Migration
echo ========================================
echo.

echo Starting PostgreSQL container...
docker-compose -f docker-compose-postgres.yml up -d postgres

echo.
echo Waiting for PostgreSQL to be ready...
timeout /t 5 /nobreak > nul

echo.
echo Running migrations inside container...
docker exec -it hcen-postgres sh /run-migrations.sh

echo.
echo Done!
pause
