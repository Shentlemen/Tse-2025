#!/bin/bash
# Script de inicio para WildFly en Render

set -e

WILDFLY_HOME="/opt/jboss/wildfly"

echo "=== Iniciando Clinic en Render ==="

##########################################
# PARSEAR LA DATABASE_URL DE RENDER
##########################################
if [ -n "$DATABASE_URL" ]; then
    echo "Usando DATABASE_URL de Render..."

    # Parser robusto con python (si existe)
    if command -v python3 &> /dev/null; then
        DB_USER=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.username or '')")
        DB_PASS=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.password or '')")
        DB_HOST=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.hostname or '')")
        DB_PORT=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.port or '5432')")
        DB_NAME=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.path.lstrip('/') or '')")
    else
        echo "Python no está disponible, usando parser simple..."
        DB_URL=$(echo "$DATABASE_URL" | sed -e 's|postgres://||' -e 's|postgresql://||')
        DB_USER=$(echo "$DB_URL" | cut -d: -f1)
        DB_PASS=$(echo "$DB_URL" | cut -d: -f2 | cut -d@ -f1)
        DB_HOST=$(echo "$DB_URL" | cut -d@ -f2 | cut -d/ -f1 | cut -d: -f1)
        DB_PORT=$(echo "$DB_URL" | cut -d@ -f2 | cut -d/ -f1 | cut -d: -f2)
        DB_NAME=$(echo "$DB_URL" | cut -d/ -f2 | cut -d? -f1)
        [ -z "$DB_PORT" ] && DB_PORT=5432
    fi
else
    echo "DATABASE_URL no existe. Usando variables individuales."
    DB_HOST=${PGHOST:-localhost}
    DB_PORT=${PGPORT:-5432}
    DB_NAME=${PGDATABASE:-clinic_db}
    DB_USER=${PGUSER:-postgres}
    DB_PASS=${PGPASSWORD:-sora}
fi

# Puerto HTTP de Render
RENDER_PORT=${PORT:-8080}

echo "Configuración detectada:"
echo "  Host: ${DB_HOST}"
echo "  Puerto: ${DB_PORT}"
echo "  Base: ${DB_NAME}"
echo "  Usuario: ${DB_USER}"
echo "  Puerto HTTP: ${RENDER_PORT}"

##########################################
# EXPORTAR VARIABLES PARA configure-wildfly
##########################################

export DB_HOST
export DB_PORT
export DB_NAME
export DB_USER
export DB_PASS
export RENDER_PORT

##########################################
# CONFIGURAR WILDFLY
##########################################

echo "Ejecutando configure-wildfly.sh..."
if ! "$WILDFLY_HOME/bin/configure-wildfly.sh"; then
    echo "ERROR: configure-wildfly.sh falló."
    exit 1
fi

##########################################
# JAVA OPTS
##########################################
export JAVA_OPTS="$JAVA_OPTS \
    -Xmx512m \
    -Xms256m \
    -XX:MaxMetaspaceSize=256m \
    -Djava.net.preferIPv4Stack=true \
    -Djboss.http.port=${RENDER_PORT}
"

##########################################
# INICIAR WILDFLY
##########################################
echo "=== Lanzando WildFly en Render ==="

exec "$WILDFLY_HOME/bin/standalone.sh" \
    -b 0.0.0.0 \
    -bmanagement 0.0.0.0 \
    -Djboss.http.port="${RENDER_PORT}" \
    -c standalone-full.xml
