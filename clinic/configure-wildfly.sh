#!/bin/bash
set -e

STANDALONE_XML="/opt/jboss/wildfly/standalone/configuration/standalone-full.xml"

echo ">>> Usando archivo: $STANDALONE_XML"

# Parsear DATABASE_URL de Render
# Formato: postgresql://user:password@host:port/dbname o postgresql://user:password@host/dbname
if [ -n "$DATABASE_URL" ]; then
    # Usar Python si está disponible para parsear URL correctamente
    if command -v python3 &> /dev/null; then
        DB_USER=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.username or '')")
        DB_PASSWORD=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.password or '')")
        DB_HOST=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.hostname or '')")
        DB_PORT=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.port if u.port else '5432')")
        DB_NAME=$(python3 -c "from urllib.parse import urlparse; u=urlparse('$DATABASE_URL'); print(u.path.lstrip('/').split('?')[0] or '')")
    else
        # Fallback: parseo simple con sed (puerto opcional)
        DB_URL=$(echo $DATABASE_URL | sed -e 's|postgresql://||' -e 's|postgres://||')
        DB_USER=$(echo $DB_URL | cut -d: -f1)
        DB_PASS_HOST=$(echo $DB_URL | cut -d: -f2-)
        DB_PASSWORD=$(echo $DB_PASS_HOST | cut -d@ -f1)
        DB_HOST_PORT=$(echo $DB_PASS_HOST | cut -d@ -f2 | cut -d/ -f1)
        DB_HOST=$(echo $DB_HOST_PORT | cut -d: -f1)
        DB_PORT=$(echo $DB_HOST_PORT | cut -d: -f2)
        DB_NAME=$(echo $DB_URL | cut -d/ -f2 | cut -d? -f1)
        
        # Si no hay puerto, usar el default
        [ -z "$DB_PORT" ] && DB_PORT="5432"
    fi
else
    # Usar variables individuales si DATABASE_URL no está disponible
    DB_HOST=${PGHOST:-localhost}
    DB_PORT=${PGPORT:-5432}
    DB_NAME=${PGDATABASE:-clinic_db}
    DB_USER=${PGUSER:-postgres}
    DB_PASSWORD=${PGPASSWORD:-sora}
fi

# Escapar caracteres especiales para XML
DB_PASSWORD_ESC=$(echo "$DB_PASSWORD" | sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g; s/"/\&quot;/g; s/'"'"'/\&apos;/g')

echo ">>> Conexión:"
echo "    Host: $DB_HOST"
echo "    Base: $DB_NAME"
echo "    Usuario: $DB_USER"
echo "    Puerto: $DB_PORT"

# Borrar ExampleDS si existe
perl -i -0pe 's/<datasource[^>]*jndi-name="java:jboss\/datasources\/ExampleDS"[^>]*>.*?<\/datasource>//gs' "$STANDALONE_XML"
sed -i 's|datasource="java:jboss/datasources/ExampleDS"|datasource="java:jboss/datasources/ClinicDS"|g' "$STANDALONE_XML"

# Eliminar ClinicDS existente si existe
perl -i -0pe 's/<datasource[^>]*jndi-name="java:jboss\/datasources\/ClinicDS"[^>]*>.*?<\/datasource>//gs' "$STANDALONE_XML"

# Insertar ClinicDS con formato correcto para WildFly 30
TEMP_XML=$(mktemp)
cat > "$TEMP_XML" <<EOF
<datasource jndi-name="java:jboss/datasources/ClinicDS" pool-name="ClinicDS" enabled="true" use-java-context="true" statistics-enabled="true">
    <connection-url>jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}</connection-url>
    <driver>postgresql</driver>
    <security>
        <user-name>${DB_USER}</user-name>
        <password>${DB_PASSWORD_ESC}</password>
    </security>
</datasource>
EOF

# Insertar antes de </datasources>
perl -0777 -i -pe "s|</datasources>|$(cat $TEMP_XML)\n</datasources>|" "$STANDALONE_XML"
rm -f "$TEMP_XML"

# Agregar driver si falta
if ! grep -q "driver name=\"postgresql\"" "$STANDALONE_XML"; then
    sed -i '/<\/drivers>/ i \
            <driver name="postgresql" module="org.postgresql"> \
                <driver-class>org.postgresql.Driver</driver-class> \
            </driver>' "$STANDALONE_XML"
fi

echo ">>> DataSource configurado correctamente"
