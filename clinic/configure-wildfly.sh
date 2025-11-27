#!/bin/bash
# Script para configurar WildFly con las variables de entorno de Render

set -e

WILDFLY_HOME="/opt/jboss/wildfly"

# Detectar si existe standalone-full.xml o standalone.xml
if [ -f "$WILDFLY_HOME/standalone/configuration/standalone-full.xml" ]; then
    STANDALONE_XML="$WILDFLY_HOME/standalone/configuration/standalone-full.xml"
else
    STANDALONE_XML="$WILDFLY_HOME/standalone/configuration/standalone.xml"
fi

echo ">>> Usando archivo de configuración: $STANDALONE_XML"

# ============================================================
# PARSEAR DATABASE_URL DE RENDER
# ============================================================

if [ -n "$DATABASE_URL" ]; then
    if command -v python3 &> /dev/null; then
        DB_USER=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.username)")
        DB_PASS=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.password)")
        DB_HOST=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.hostname)")
        DB_PORT=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.port if u.port else '5432')")
        DB_NAME=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.path.lstrip('/').split('?')[0])")
    else
        DB_URL=$(echo $DATABASE_URL | sed -e 's|postgresql://||')
        DB_USER=$(echo $DB_URL | cut -d: -f1)
        DB_PASS=$(echo $DB_URL | cut -d: -f2 | cut -d@ -f1)
        DB_HOST_PORT=$(echo $DB_URL | cut -d@ -f2 | cut -d/ -f1)
        DB_HOST=$(echo $DB_HOST_PORT | cut -d: -f1)
        DB_PORT=$(echo $DB_HOST_PORT | cut -d: -f2)
        DB_NAME=$(echo $DB_URL | cut -d/ -f2 | cut -d? -f1)

        [ -z "$DB_PORT" ] && DB_PORT="5432"
    fi
else
    # Render define PG* variables automáticamente
    DB_HOST=${PGHOST}
    DB_PORT=${PGPORT:-5432}
    DB_NAME=${PGDATABASE}
    DB_USER=${PGUSER}
    DB_PASS=${PGPASSWORD}

    # Y si nada existe, modo local
    [ -z "$DB_HOST" ] && DB_HOST="localhost"
    [ -z "$DB_NAME" ] && DB_NAME="clinic_db"
    [ -z "$DB_USER" ] && DB_USER="postgres"
    [ -z "$DB_PASS" ] && DB_PASS="sora"
fi

# Escapar caracteres para XML
DB_PASS_ESC=$(echo "$DB_PASS" | sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g; s/"/\&quot;/g; s/'"'"'/\&apos;/g')

echo ">>> Configuración detectada:"
echo "    Host: $DB_HOST"
echo "    Puerto: $DB_PORT"
echo "    Base: $DB_NAME"
echo "    Usuario: $DB_USER"

if [ ! -f "$STANDALONE_XML" ]; then
    echo "ERROR: No existe el archivo de configuración: $STANDALONE_XML"
    exit 1
fi

# ============================================================
# REMOVER ExampleDS SI EXISTE
# ============================================================

perl -0777 -i -pe 's|<datasource[^>]*jndi-name="java:jboss/datasources/ExampleDS"[\s\S]*?</datasource>||g' "$STANDALONE_XML"

# ============================================================
# REEMPLAZAR O CREAR DATASOURCE ClinicDS
# ============================================================

DATASOURCE_XML="
<datasource jndi-name=\"java:jboss/datasources/ClinicDS\" pool-name=\"ClinicDS\" enabled=\"true\" use-java-context=\"true\">
    <connection-url>jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}</connection-url>
    <driver>postgresql</driver>
    <security>
        <user-name>${DB_USER}</user-name>
        <password>${DB_PASS_ESC}</password>
    </security>
</datasource>
"

# Si existe → reemplazar bloque completo
if grep -q 'jndi-name="java:jboss/datasources/ClinicDS"' "$STANDALONE_XML"; then
    echo ">>> Datasource ClinicDS encontrado; reemplazando..."
    perl -0777 -i -pe "s|<datasource[^>]*jndi-name=\"java:jboss/datasources/ClinicDS\"[\s\S]*?</datasource>|$DATASOURCE_XML|g" "$STANDALONE_XML"
else
    echo ">>> Insertando datasource ClinicDS..."
    perl -0777 -i -pe "s|</datasources>|$DATASOURCE_XML\n</datasources>|" "$STANDALONE_XML"
fi

echo ">>> Datasource ClinicDS configurado correctamente."
