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
echo ">>> Ejecutando script Python para actualizar XML..."
python3 - "$STANDALONE_XML" "$DB_HOST" "$DB_PORT" "$DB_NAME" "$DB_USER" "$DB_PASSWORD_ESC" << 'PYTHON_SCRIPT'
import re
import sys

xml_file = sys.argv[1]
db_host = sys.argv[2]
db_port = sys.argv[3]
db_name = sys.argv[4]
db_user = sys.argv[5]
db_password = sys.argv[6]

print(f"Python: Leyendo archivo {xml_file}")
print(f"Python: Host={db_host}, Port={db_port}, DB={db_name}, User={db_user}")

# Leer el archivo
with open(xml_file, 'r', encoding='utf-8') as f:
    content = f.read()

# Buscar y deshabilitar cualquier datasource con usuario 'postgres' que no sea ClinicDS
print("Python: Buscando datasources con usuario 'postgres'...")
pattern_all_postgres = r'(<datasource[^>]*jndi-name="java:jboss/datasources/(?!ClinicDS)[^"]*"[^>]*>.*?<security user-name=")postgres(")'
matches_all = re.findall(pattern_all_postgres, content, flags=re.DOTALL)
if matches_all:
    print(f"Python: ADVERTENCIA: Se encontraron {len(matches_all)} datasources con usuario 'postgres' que no son ClinicDS")
    # Deshabilitar estos datasources
    content = re.sub(
        r'(<datasource[^>]*jndi-name="java:jboss/datasources/(?!ClinicDS)[^"]*"[^>]*)enabled="true"',
        r'\1enabled="false"',
        content,
        flags=re.DOTALL
    )
    print("Python: Datasources no-ClinicDS deshabilitados")

# Patrón más flexible para encontrar el datasource ClinicDS
# Buscar el bloque completo del datasource ClinicDS
clinicds_pattern = r'<datasource[^>]*jndi-name="java:jboss/datasources/ClinicDS"[^>]*>(.*?)</datasource>'
clinicds_match = re.search(clinicds_pattern, content, flags=re.DOTALL)

if clinicds_match:
    clinicds_block = clinicds_match.group(0)
    print("Python: Encontrado datasource ClinicDS")
    
    # Reemplazar connection-url
    clinicds_block = re.sub(
        r'(<connection-url>)jdbc:postgresql://[^<]+(</connection-url>)',
        f'\\1jdbc:postgresql://{db_host}:{db_port}/{db_name}\\2',
        clinicds_block,
        flags=re.DOTALL
    )
    
    # Reemplazar user-name y password en security
    # Buscar el usuario actual
    user_match = re.search(r'<security[^>]*user-name="([^"]+)"[^>]*password="([^"]+)"', clinicds_block)
    if user_match:
        old_user = user_match.group(1)
        print(f"Python: Usuario actual en ClinicDS: {old_user}, cambiando a: {db_user}")
        clinicds_block = re.sub(
            r'(<security[^>]*user-name=")[^"]+(".*?password=")[^"]+(")',
            f'\\1{db_user}\\2{db_password}\\3',
            clinicds_block,
            flags=re.DOTALL
        )
    else:
        # Si no encuentra el patrón con atributos, buscar con elementos anidados
        clinicds_block = re.sub(
            r'(<security[^>]*>.*?<user-name>)[^<]+(</user-name>.*?<password>)[^<]+(</password>)',
            f'\\1{db_user}\\2{db_password}\\3',
            clinicds_block,
            flags=re.DOTALL
        )
    
    # Reemplazar el bloque completo en el contenido
    content = content.replace(clinicds_match.group(0), clinicds_block)
    print("Python: ClinicDS actualizado correctamente")
else:
    print("Python: ERROR: No se encontró el datasource ClinicDS en el XML")
    sys.exit(1)

# Verificar que no quede ningún "postgres" en datasources activos
if 'user-name="postgres"' in content:
    print("Python: ADVERTENCIA: Todavía hay referencias a 'postgres' en el XML")
    # Buscar y mostrar qué datasources tienen postgres y a qué base de datos intentan conectarse
    postgres_pattern = r'<datasource[^>]*jndi-name="([^"]*)"[^>]*>.*?<connection-url>(jdbc:postgresql://[^<]+)</connection-url>.*?<security user-name="postgres"'
    postgres_matches = re.findall(postgres_pattern, content, flags=re.DOTALL)
    if postgres_matches:
        print(f"Python: Datasources con 'postgres' encontrados:")
        for jndi_name, connection_url in postgres_matches:
            print(f"  - JNDI: {jndi_name}")
            print(f"    URL: {connection_url}")
            print(f"    Usuario: postgres")
            print(f"    ESTE DATASOURCE ESTÁ INTENTANDO CONECTARSE CON USUARIO 'postgres'")
    
    # Intentar deshabilitar estos datasources
    for jndi_name, _ in postgres_matches:
        if 'ClinicDS' not in jndi_name:
            print(f"Python: Deshabilitando datasource {jndi_name} que usa 'postgres'")
            content = re.sub(
                f'(<datasource[^>]*jndi-name="{re.escape(jndi_name)}"[^>]*)enabled="true"',
                r'\1enabled="false"',
                content,
                flags=re.DOTALL
            )

# Verificar el estado final del ClinicDS
clinicds_final = re.search(r'<datasource[^>]*jndi-name="java:jboss/datasources/ClinicDS"[^>]*>(.*?)</datasource>', content, flags=re.DOTALL)
if clinicds_final:
    clinicds_url = re.search(r'<connection-url>(jdbc:postgresql://[^<]+)</connection-url>', clinicds_final.group(0))
    clinicds_user = re.search(r'<security[^>]*user-name="([^"]+)"', clinicds_final.group(0))
    if clinicds_url and clinicds_user:
        print(f"Python: Estado final de ClinicDS:")
        print(f"  URL: {clinicds_url.group(1)}")
        print(f"  Usuario: {clinicds_user.group(1)}")
        if clinicds_user.group(1) == "postgres":
            print("  ERROR: ClinicDS todavía tiene usuario 'postgres'!")
            sys.exit(1)

# Escribir el archivo
with open(xml_file, 'w', encoding='utf-8') as f:
    f.write(content)
print("Python: Archivo XML actualizado correctamente")
PYTHON_SCRIPT

if [ $? -ne 0 ]; then
    echo ">>> ERROR: El script Python falló"
    exit 1
fi

echo ">>> Credenciales actualizadas desde DATABASE_URL"

echo ">>> DataSource ClinicDS listo"
