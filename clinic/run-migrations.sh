#!/bin/bash
# Script para ejecutar migraciones de Flyway antes de iniciar WildFly

set -e

echo "=== Ejecutando migraciones de base de datos ==="

# Parsear DATABASE_URL de Render
if [ -n "$DATABASE_URL" ]; then
    if command -v python3 &> /dev/null; then
        DB_USER=$(python3 -c "from urllib.parse import urlparse; print(urlparse('$DATABASE_URL').username or '')")
        DB_PASS=$(python3 -c "from urllib.parse import urlparse; print(urlparse('$DATABASE_URL').password or '')")
        DB_HOST=$(python3 -c "from urllib.parse import urlparse; print(urlparse('$DATABASE_URL').hostname or '')")
        DB_PORT=$(python3 -c "from urllib.parse import urlparse; p=urlparse('$DATABASE_URL'); print(p.port if p.port else '5432')")
        DB_NAME=$(python3 -c "from urllib.parse import urlparse; print(urlparse('$DATABASE_URL').path.lstrip('/').split('?')[0] or '')")
    else
        DB_URL=$(echo $DATABASE_URL | sed -e 's|postgresql://||')
        DB_USER=$(echo $DB_URL | cut -d: -f1)
        DB_PASS=$(echo $DB_URL | cut -d: -f2 | cut -d@ -f1)
        DB_HOST_PORT=$(echo $DB_URL | cut -d@ -f2 | cut -d/ -f1)
        DB_HOST=$(echo $DB_HOST_PORT | cut -d: -f1)
        DB_PORT=$(echo $DB_HOST_PORT | cut -d: -f2)
        DB_NAME=$(echo $DB_URL | cut -d/ -f2 | cut -d? -f1)
        if [ -z "$DB_PORT" ] || [ "$DB_PORT" = "$DB_HOST" ]; then
            DB_PORT="5432"
        fi
    fi
    
    export FLYWAY_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
    export FLYWAY_USER="${DB_USER}"
    export FLYWAY_PASSWORD="${DB_PASS}"
else
    export FLYWAY_URL=${FLYWAY_URL:-"jdbc:postgresql://${PGHOST:-localhost}:${PGPORT:-5432}/${PGDATABASE:-clinic_db}"}
    export FLYWAY_USER=${FLYWAY_USER:-${PGUSER:-postgres}}
    export FLYWAY_PASSWORD=${FLYWAY_PASSWORD:-${PGPASSWORD:-sora}}
fi

echo "Conectando a: ${FLYWAY_URL}"
echo "Usuario: ${FLYWAY_USER}"

# Ejecutar migraciones
./gradlew flywayMigrate || {
    echo "Error ejecutando migraciones. Continuando de todas formas..."
    exit 0  # No fallar el despliegue si las migraciones fallan (puede ser que ya estén aplicadas)
}

echo "Migraciones completadas"

