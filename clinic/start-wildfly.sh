#!/bin/bash
# Script de inicio para WildFly en Render

set -e

WILDFLY_HOME="/opt/jboss/wildfly"

echo "=== Iniciando Clinic en Render ==="

# Parsear variables de base de datos
if [ -n "$DATABASE_URL" ]; then
    # Usar Python si está disponible para parsear URL correctamente
    if command -v python3 &> /dev/null; then
        DB_USER=$(python3 -c "from urllib.parse import urlparse; print(urlparse('$DATABASE_URL').username or '')")
        DB_PASS=$(python3 -c "from urllib.parse import urlparse; print(urlparse('$DATABASE_URL').password or '')")
        DB_HOST=$(python3 -c "from urllib.parse import urlparse; print(urlparse('$DATABASE_URL').hostname or '')")
        DB_PORT=$(python3 -c "from urllib.parse import urlparse; p=urlparse('$DATABASE_URL'); print(p.port if p.port else '5432')")
        DB_NAME=$(python3 -c "from urllib.parse import urlparse; print(urlparse('$DATABASE_URL').path.lstrip('/').split('?')[0] or '')")
    else
        # Fallback: parseo simple con sed
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
else
    # Usar variables individuales
    DB_HOST=${PGHOST:-localhost}
    DB_PORT=${PGPORT:-5432}
    DB_NAME=${PGDATABASE:-clinic_db}
    DB_USER=${PGUSER:-postgres}
    DB_PASS=${PGPASSWORD:-sora}
fi

RENDER_PORT=${PORT:-8080}

echo "Configuración:"
echo "  Base de datos: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "  Usuario: ${DB_USER}"
echo "  Puerto HTTP: ${RENDER_PORT}"

# Configurar standalone.xml antes de iniciar
$WILDFLY_HOME/bin/configure-wildfly.sh || echo "Advertencia: Configuración previa falló"

# Configurar variables de entorno de Java para Render
export JAVA_OPTS="${JAVA_OPTS} -Xmx512m -Xms256m -XX:MaxMetaspaceSize=256m -Djava.net.preferIPv4Stack=true"

# Iniciar WildFly
echo "Iniciando WildFly..."
exec $WILDFLY_HOME/bin/standalone.sh \
    -b 0.0.0.0 \
    -bmanagement 0.0.0.0 \
    -Djboss.http.port=${RENDER_PORT} \
    -c standalone-full.xml

