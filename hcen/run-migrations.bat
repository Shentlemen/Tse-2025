@echo off
REM =============================================================================
REM HCEN Database Migration Runner
REM =============================================================================
REM This script runs all Flyway migrations against the PostgreSQL database
REM =============================================================================

echo ========================================
echo HCEN Database Migration Runner
echo ========================================
echo.

REM Configuration
set POSTGRES_HOST=localhost
set POSTGRES_PORT=5432
set POSTGRES_DB=hcen
set POSTGRES_USER=postgres
set POSTGRES_PASSWORD=postgres

echo Connecting to database: %POSTGRES_DB%
echo Host: %POSTGRES_HOST%:%POSTGRES_PORT%
echo User: %POSTGRES_USER%
echo.

REM Set PGPASSWORD to avoid password prompt
set PGPASSWORD=%POSTGRES_PASSWORD%

echo Running migrations...
echo.

echo [1/5] Creating INUS schema...
psql -h %POSTGRES_HOST% -p %POSTGRES_PORT% -U %POSTGRES_USER% -d %POSTGRES_DB% -f "src\main\resources\db\migration\V001__create_inus_schema.sql"
if errorlevel 1 (
    echo ERROR: Failed to run V001__create_inus_schema.sql
    pause
    exit /b 1
)

echo [2/5] Creating RNDC schema...
psql -h %POSTGRES_HOST% -p %POSTGRES_PORT% -U %POSTGRES_USER% -d %POSTGRES_DB% -f "src\main\resources\db\migration\V002__create_rndc_schema.sql"
if errorlevel 1 (
    echo ERROR: Failed to run V002__create_rndc_schema.sql
    pause
    exit /b 1
)

echo [3/5] Creating Policies schema...
psql -h %POSTGRES_HOST% -p %POSTGRES_PORT% -U %POSTGRES_USER% -d %POSTGRES_DB% -f "src\main\resources\db\migration\V003__create_policies_schema.sql"
if errorlevel 1 (
    echo ERROR: Failed to run V003__create_policies_schema.sql
    pause
    exit /b 1
)

echo [4/5] Creating Audit schema...
psql -h %POSTGRES_HOST% -p %POSTGRES_PORT% -U %POSTGRES_USER% -d %POSTGRES_DB% -f "src\main\resources\db\migration\V004__create_audit_schema.sql"
if errorlevel 1 (
    echo ERROR: Failed to run V004__create_audit_schema.sql
    pause
    exit /b 1
)

echo [5/5] Creating Clinics schema...
psql -h %POSTGRES_HOST% -p %POSTGRES_PORT% -U %POSTGRES_USER% -d %POSTGRES_DB% -f "src\main\resources\db\migration\V005__create_clinics_schema.sql"
if errorlevel 1 (
    echo ERROR: Failed to run V005__create_clinics_schema.sql
    pause
    exit /b 1
)

echo.
echo ========================================
echo All migrations completed successfully!
echo ========================================
echo.

REM Clear password
set PGPASSWORD=

echo Verifying schemas and tables...
echo.

psql -h %POSTGRES_HOST% -p %POSTGRES_PORT% -U %POSTGRES_USER% -d %POSTGRES_DB% -c "\dn"
echo.
psql -h %POSTGRES_HOST% -p %POSTGRES_PORT% -U %POSTGRES_USER% -d %POSTGRES_DB% -c "\dt inus.*"
psql -h %POSTGRES_HOST% -p %POSTGRES_PORT% -U %POSTGRES_USER% -d %POSTGRES_DB% -c "\dt rndc.*"
psql -h %POSTGRES_HOST% -p %POSTGRES_PORT% -U %POSTGRES_USER% -d %POSTGRES_DB% -c "\dt policies.*"
psql -h %POSTGRES_HOST% -p %POSTGRES_PORT% -U %POSTGRES_USER% -d %POSTGRES_DB% -c "\dt audit.*"
psql -h %POSTGRES_HOST% -p %POSTGRES_PORT% -U %POSTGRES_USER% -d %POSTGRES_DB% -c "\dt clinics.*"

echo.
echo Press any key to exit...
pause > nul
