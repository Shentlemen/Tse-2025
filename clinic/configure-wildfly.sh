#!/bin/bash
# Script para configurar WildFly con las variables de entorno de Render

set -e

WILDFLY_HOME="/opt/jboss/wildfly"

# Detectar archivo de configuración
if [ -f "$WILDFLY_HOME/standalone/configuration/standalone-full.xml" ]; then
    STANDALONE_XML="$WILDFLY_HOME/standalone/configuration/standalone-full.xml"
else
    STANDALONE_XML="$WILDFLY_HOME/standalone/configuration/standalone.xml"
fi

echo ">>> Usando archivo de configuración: $STANDALONE_XML"

###########################################################
# PARSEAR DATABASE_URL DE RENDER O VARIABLES PG*
###########################################################

if [ -n "$DATABASE_URL" ]; then
    echo ">>> Usando DATABASE_URL"

    if command -v python3 &> /dev/null; then
        DB_USER=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.username)")
        DB_PASS=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.password)")
        DB_HOST=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.hostname)")
        DB_PORT=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.port if u.port else '5432')")
        DB_NAME=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.path.lstrip('/').split('?')[0])")
    else
        DB_URL=$(echo "$DATABASE_URL" | sed -e 's|postgresql://||' -e 's|postgres://||')
        DB_USER=$(echo "$DB_URL" | cut -d: -f1)
        DB_PASS=$(echo "$DB_URL" | cut -d: -f2 | cut -d@ -f1)
        HOST_PORT=$(echo "$DB_URL" | cut -d@ -f2 | cut -d/ -f1)
        DB_HOST=$(echo "$HOST_PORT" | cut -d: -f1)
        DB_PORT=$(echo "$HOST_PORT" | cut -d: -f2)
        DB_NAME=$(echo "$DB_URL" | cut -d/ -f2 | cut -d? -f1)
        [ -z "$DB_PORT" ] && DB_PORT="5432"
    fi
else
    echo ">>> Usando variables PG*"
    DB_HOST=${PGHOST:-localhost}
    DB_PORT=${PGPORT:-5432}
    DB_NAME=${PGDATABASE:-clinic_db}
    DB_USER=${PGUSER:-postgres}
    DB_PASS=${PGPASSWORD:-sora}
fi

# Escapar el password para XML
DB_PASS_ESC=$(echo "$DB_PASS" | sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g; s/"/\&quot;/g; s/'"'"'/\&apos;/g')

echo ">>> Conexión:"
echo "    Host: $DB_HOST"
echo "    Base: $DB_NAME"
echo "    Usuario: $DB_USER"
echo "    Puerto DB: $DB_PORT"

###########################################################
# QUITAR ExampleDS
###########################################################
perl -0777 -i -pe 's|<datasource[^>]*jndi-name="java:jboss/datasources/ExampleDS"[\s\S]*?</datasource>||g' "$STANDALONE_XML"

###########################################################
# AGREGAR O REEMPLAZAR ClinicDS
###########################################################

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

if grep -q 'jndi-name="java:jboss/datasources/ClinicDS"' "$STANDALONE_XML"; then
    echo ">>> Reemplazando ClinicDS existente..."
    perl -0777 -i -pe "s|<datasource[^>]*jndi-name=\"java:jboss/datasources/ClinicDS\"[\s\S]*?</datasource>|$DATASOURCE_XML|g" "$STANDALONE_XML"
else
    echo ">>> Insertando ClinicDS..."
    perl -0777 -i -pe "s|</datasources>|$DATASOURCE_XML\n</datasources>|" "$STANDALONE_XML"
fi

###########################################################
# AGREGAR DRIVER POSTGRESQL SI FALTA
###########################################################

if ! grep -q 'driver name="postgresql"' "$STANDALONE_XML"; then
    echo ">>> Registrando driver PostgreSQL..."
    sed -i '/<\/drivers>/i \
        <driver name="postgresql" module="org.postgresql"> \
            <driver-class>org.postgresql.Driver</driver-class> \
        </driver>' "$STANDALONE_XML"
fi

###########################################################
# CORREGIR DEFAULT-BINDINGS
###########################################################
if grep -q "default-bindings" "$STANDALONE_XML"; then
    sed -i 's|datasource=".*"|datasource="java:jboss/datasources/ClinicDS"|' "$STANDALONE_XML"
else
    sed -i '/<\/subsystem>/i \
        <default-bindings datasource="java:jboss/datasources/ClinicDS"/>' "$STANDALONE_XML"
fi

###########################################################
# CONFIGURAR PUERTO HTTP PARA RENDER
###########################################################

RENDER_PORT="${PORT:-8080}"

sed -i "s|<socket-binding name=\"http\" port=\"\${jboss.http.port:[0-9]*}\"/>|<socket-binding name=\"http\" port=\"\${jboss.http.port:${RENDER_PORT}}\"/>|" "$STANDALONE_XML"

echo ">>> Puerto HTTP configurado: $RENDER_PORT"

###########################################################

echo ">>> WildFly configurado correctamente"
