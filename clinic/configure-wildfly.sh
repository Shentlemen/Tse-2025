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
    # Si no hay DATABASE_URL, usar las credenciales que ya están en standalone-full.xml
    # NO modificar nada si no hay DATABASE_URL
    echo ">>> DATABASE_URL no encontrado. Usando credenciales de standalone-full.xml"
    echo ">>> DataSource ClinicDS listo (sin modificaciones)"
    exit 0
fi

# Validar que se parsearon correctamente las credenciales
if [ -z "$DB_USER" ] || [ -z "$DB_PASSWORD" ] || [ -z "$DB_HOST" ] || [ -z "$DB_NAME" ]; then
    echo ">>> ERROR: No se pudieron parsear las credenciales de DATABASE_URL"
    echo ">>> Usando credenciales de standalone-full.xml"
    exit 0
fi

# Escapar caracteres especiales para XML
DB_PASSWORD_ESC=$(echo "$DB_PASSWORD" | sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g; s/"/\&quot;/g; s/'"'"'/\&apos;/g')

echo ">>> Conexión:"
echo "    Host: $DB_HOST"
echo "    Base: $DB_NAME"
echo "    Usuario: $DB_USER"
echo "    Puerto: $DB_PORT"

# Actualizar credenciales en ClinicDS específicamente
echo ">>> Actualizando credenciales desde DATABASE_URL..."

# Usar Python para hacer el reemplazo de manera precisa
python3 - "$STANDALONE_XML" "$DB_HOST" "$DB_PORT" "$DB_NAME" "$DB_USER" "$DB_PASSWORD_ESC" << 'PYTHON_SCRIPT'
import re
import sys

xml_file = sys.argv[1]
db_host = sys.argv[2]
db_port = sys.argv[3]
db_name = sys.argv[4]
db_user = sys.argv[5]
db_password = sys.argv[6]

# Leer el archivo
with open(xml_file, 'r', encoding='utf-8') as f:
    content = f.read()

# Patrón para encontrar el datasource ClinicDS completo
pattern = r'(<datasource jndi-name="java:jboss/datasources/ClinicDS"[^>]*>.*?<connection-url>)(jdbc:postgresql://[^<]+)(</connection-url>.*?<security user-name=")([^"]+)(" password=")([^"]+)(")'

def replace_datasource(match):
    return f"{match.group(1)}jdbc:postgresql://{db_host}:{db_port}/{db_name}{match.group(3)}{db_user}{match.group(5)}{db_password}{match.group(7)}"

# Reemplazar
content = re.sub(pattern, replace_datasource, content, flags=re.DOTALL)

# Escribir el archivo
with open(xml_file, 'w', encoding='utf-8') as f:
    f.write(content)
PYTHON_SCRIPT

echo ">>> Credenciales actualizadas desde DATABASE_URL"

echo ">>> DataSource ClinicDS listo"
