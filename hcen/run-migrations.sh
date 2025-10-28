#!/bin/bash
# =============================================================================
# HCEN Database Migration Runner
# =============================================================================
# This script runs all Flyway migrations against the PostgreSQL database
# Designed to run inside the PostgreSQL Docker container
# =============================================================================

echo "========================================"
echo "HCEN Database Migration Runner"
echo "========================================"
echo ""

# Configuration (using container environment)
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=hcen
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

echo "Connecting to database: $POSTGRES_DB"
echo "Host: $POSTGRES_HOST:$POSTGRES_PORT"
echo "User: $POSTGRES_USER"
echo ""

# Set PGPASSWORD to avoid password prompt
export PGPASSWORD=$POSTGRES_PASSWORD

echo "Running migrations..."
echo ""

echo "[1/5] Creating INUS schema..."
psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -f /migrations/V001__create_inus_schema.sql
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to run V001__create_inus_schema.sql"
    exit 1
fi

echo "[2/5] Creating RNDC schema..."
psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -f /migrations/V002__create_rndc_schema.sql
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to run V002__create_rndc_schema.sql"
    exit 1
fi

echo "[3/5] Creating Policies schema..."
psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -f /migrations/V003__create_policies_schema.sql
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to run V003__create_policies_schema.sql"
    exit 1
fi

echo "[4/5] Creating Audit schema..."
psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -f /migrations/V004__create_audit_schema.sql
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to run V004__create_audit_schema.sql"
    exit 1
fi

echo "[5/5] Creating Clinics schema..."
psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -f /migrations/V005__create_clinics_schema.sql
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to run V005__create_clinics_schema.sql"
    exit 1
fi

echo ""
echo "========================================"
echo "All migrations completed successfully!"
echo "========================================"
echo ""

# Clear password
unset PGPASSWORD

echo "Verifying schemas and tables..."
echo ""

psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -c "\dn"
echo ""
psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -c "\dt inus.*"
psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -c "\dt rndc.*"
psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -c "\dt policies.*"
psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -c "\dt audit.*"
psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -c "\dt clinics.*"
psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -c "\dt public.notification_preferences"

echo ""
echo "Migration complete!"
