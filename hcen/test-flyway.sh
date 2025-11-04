#!/bin/bash
# ==============================================
# Flyway Migration Test Script - Linux/macOS
# ==============================================
# This script tests the Flyway configuration
# for the HCEN project

set -e  # Exit on error

echo ""
echo "================================================"
echo "HCEN Flyway Migration Test"
echo "================================================"
echo ""

# Step 1: Check if Docker is running
echo "[1/5] Checking Docker status..."
if ! docker info > /dev/null 2>&1; then
    echo "ERROR: Docker is not running!"
    echo "Please start Docker and try again."
    exit 1
fi
echo "✓ Docker is running"

echo ""
echo "[2/5] Starting PostgreSQL container..."
if ! docker-compose -f docker-compose-postgres.yml up -d postgres; then
    echo "ERROR: Failed to start PostgreSQL container!"
    exit 1
fi
echo "✓ PostgreSQL container started"

echo ""
echo "[3/5] Waiting for PostgreSQL to be ready..."
sleep 10
echo "✓ PostgreSQL should be ready"

echo ""
echo "[4/5] Running Flyway Info (checking migration status)..."
./gradlew flywayInfo || echo "WARNING: flywayInfo failed. This might be normal if migrations haven't been applied yet."

echo ""
echo "[5/5] Running Flyway Migrate (applying migrations)..."
if ! ./gradlew flywayMigrate; then
    echo "ERROR: Flyway migration failed!"
    echo "Check the error messages above."
    exit 1
fi
echo "✓ Migrations applied successfully"

echo ""
echo "================================================"
echo "Flyway Migration Test PASSED!"
echo "================================================"
echo ""
echo "Next steps:"
echo "1. Verify migrations: ./gradlew flywayInfo"
echo "2. Connect to database: docker exec -it hcen-postgres psql -U postgres -d hcen"
echo "3. Build project: ./gradlew build"
echo ""
