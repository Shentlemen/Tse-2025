  #!/bin/bash
# =============================================================================
# HCEN Database Migration Helper Script
# =============================================================================
# This script executes migrations inside the PostgreSQL Docker container
# =============================================================================

echo "========================================"
echo "HCEN Database Migration"
echo "========================================"
echo ""

echo "Starting PostgreSQL container..."
docker-compose -f docker-compose-postgres.yml up -d postgres

echo ""
echo "Waiting for PostgreSQL to be ready..."
sleep 5

echo ""
echo "Running migrations inside container..."
docker exec -it hcen-postgres sh /run-migrations.sh

echo ""
echo "Done!"
