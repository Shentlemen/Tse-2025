#!/bin/bash
# Script para configurar WildFly con variables de entorno de Render
# Este script modifica directamente el archivo standalone.xml

set -e

WILDFLY_HOME="/opt/jboss/wildfly"
STANDALONE_XML="$WILDFLY_HOME/standalone/configuration/standalone-full.xml"

# Parsear DATABASE_URL de Render
# Formato: postgresql://user:password@host:port/dbname
if [ -n "$DATABASE_URL" ]; then
    # Extraer componentes de DATABASE_URL usando Python si está disponible, sino usar sed
    if command -v python3 &> /dev/null; then
        DB_USER=$(python3 -c "from urllib.parse import urlparse; print(urlparse('$DATABASE_URL').username)")
        DB_PASS=$(python3 -c "from urllib.parse import urlparse; print(urlparse('$DATABASE_URL').password)")
        DB_HOST=$(python3 -c "from urllib.parse import urlparse; print(urlparse('$DATABASE_URL').hostname)")
        DB_PORT=$(python3 -c "from urllib.parse import urlparse; p=urlparse('$DATABASE_URL'); print(p.port if p.port else '5432')")
        DB_NAME=$(python3 -c "from urllib.parse import urlparse; print(urlparse('$DATABASE_URL').path.lstrip('/').split('?')[0])")
    else
        # Fallback usando sed (más simple pero menos robusto)
        DB_URL=$(echo $DATABASE_URL | sed -e 's|postgresql://||')
        DB_USER=$(echo $DB_URL | cut -d: -f1)
        DB_PASS=$(echo $DB_URL | cut -d: -f2 | cut -d@ -f1)
        DB_HOST_PORT=$(echo $DB_URL | cut -d@ -f2 | cut -d/ -f1)
        DB_HOST=$(echo $DB_HOST_PORT | cut -d: -f1)
        DB_PORT=$(echo $DB_HOST_PORT | cut -d: -f2)
        DB_NAME=$(echo $DB_URL | cut -d/ -f2 | cut -d? -f1)
        
        # Si no hay puerto, usar el default
        if [ -z "$DB_PORT" ] || [ "$DB_PORT" = "$DB_HOST" ]; then
            DB_PORT="5432"
        fi
    fi
else
    # Usar variables individuales si DATABASE_URL no está disponible
    # Render configura estas variables automáticamente cuando se vincula una base de datos
    if [ -n "$PORT" ]; then
        # Estamos en Render - usar variables PG* que Render configura automáticamente
        DB_HOST=${PGHOST}
        DB_PORT=${PGPORT:-5432}
        DB_NAME=${PGDATABASE}
        DB_USER=${PGUSER}
        DB_PASS=${PGPASSWORD}
        
        # Validar que todas las variables estén configuradas
        if [ -z "$DB_HOST" ] || [ -z "$DB_NAME" ] || [ -z "$DB_USER" ] || [ -z "$DB_PASS" ]; then
            echo "ERROR: En Render, se requieren todas las variables PG* o DATABASE_URL. Variables disponibles:"
            echo "  PGHOST: ${PGHOST:-no configurado}"
            echo "  PGDATABASE: ${PGDATABASE:-no configurado}"
            echo "  PGUSER: ${PGUSER:-no configurado}"
            echo "  PGPASSWORD: ${PGPASSWORD:+configurado (oculto)}"
            exit 1
        fi
        echo ">>> Usando variables PG* de Render (no DATABASE_URL)"
    else
        # Desarrollo local - usar valores por defecto
        DB_HOST=${PGHOST:-localhost}
        DB_PORT=${PGPORT:-5432}
        DB_NAME=${PGDATABASE:-clinic_db}
        DB_USER=${PGUSER:-postgres}
        DB_PASS=${PGPASSWORD:-sora}
        echo ">>> Usando valores por defecto para desarrollo local"
    fi
fi

# Obtener puerto de Render (default 8080)
RENDER_PORT=${PORT:-8080}

# Escapar caracteres especiales para XML
DB_PASS_ESC=$(echo "$DB_PASS" | sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g; s/"/\&quot;/g; s/'"'"'/\&apos;/g')

echo "=== Configurando WildFly con variables de entorno ==="
echo "  DATABASE_URL presente: $([ -n "$DATABASE_URL" ] && echo 'SÍ' || echo 'NO')"
echo "  PGHOST: ${PGHOST:-no configurado}"
echo "  PGUSER: ${PGUSER:-no configurado}"
echo "  PGPASSWORD: ${PGPASSWORD:+configurado (oculto)}"
echo "  DB_HOST: $DB_HOST"
echo "  DB_PORT: $DB_PORT"
echo "  DB_NAME: $DB_NAME"
echo "  DB_USER: $DB_USER"
echo "  DB_PASS: ${DB_PASS:0:3}*** (oculto por seguridad)"
echo "  PORT: $RENDER_PORT"
echo "======================================================"

# Esperar a que el archivo standalone.xml esté disponible
if [ ! -f "$STANDALONE_XML" ]; then
    echo "Iniciando WildFly en modo admin-only para generar configuración..."
    $WILDFLY_HOME/bin/standalone.sh -c standalone-full.xml --admin-only &
    WILDFLY_PID=$!
    
    # Esperar a que el archivo se genere
    for i in {1..30}; do
        if [ -f "$STANDALONE_XML" ]; then
            break
        fi
        sleep 1
    done
    
    # Detener WildFly
    kill $WILDFLY_PID 2>/dev/null || true
    wait $WILDFLY_PID 2>/dev/null || true
fi

# Modificar standalone.xml directamente usando sed
if [ -f "$STANDALONE_XML" ]; then
    # Configurar puerto HTTP usando variable de sistema (más confiable que modificar XML)
    # Esto se hará en start-wildfly.sh con -Djboss.http.port
    echo "Puerto se configurará vía variable de sistema: ${RENDER_PORT}"
    
    # Eliminar ExampleDS si todavía existe (por si acaso)
    if grep -q "jndi-name=\"java:jboss/datasources/ExampleDS\"" "$STANDALONE_XML"; then
        echo "Eliminando ExampleDS que todavía existe..."
        perl -i -0pe 's/<datasource[^>]*jndi-name="java:jboss\/datasources\/ExampleDS"[^>]*>.*?<\/datasource>//gs' "$STANDALONE_XML"
        sed -i 's|datasource="java:jboss/datasources/ExampleDS"|datasource="java:jboss/datasources/ClinicDS"|g' "$STANDALONE_XML"
        echo "ExampleDS eliminado"
    fi
    
    # El driver PostgreSQL se despliega automáticamente desde el WAR
    # Necesitamos registrar el driver en la sección <drivers> para que pueda ser usado por el datasource
    # El driver se despliega como "clinic.war_org.postgresql.Driver_42_7" pero podemos registrarlo como "postgresql"
    if ! grep -q 'driver name="postgresql"' "$STANDALONE_XML"; then
        echo "Registrando driver PostgreSQL para uso con datasource..."
        if grep -q "<drivers>" "$STANDALONE_XML"; then
            # Registrar el driver usando el módulo que instalamos
            sed -i '/<\/drivers>/i\
                <driver name="postgresql" module="org.postgresql">\
                    <driver-class>org.postgresql.Driver</driver-class>\
                </driver>' "$STANDALONE_XML"
            echo "Driver PostgreSQL registrado"
        else
            echo "ERROR: No se encontró la sección <drivers> para registrar el driver PostgreSQL"
            exit 1
        fi
    else
        echo "Driver PostgreSQL ya está registrado"
    fi
    
    # Verificar si el datasource ClinicDS ya existe y actualizarlo
    if grep -q "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML"; then
        echo ">>> Datasource ClinicDS encontrado. Eliminando para recrearlo con credenciales correctas..."
        
        # Contar cuántos datasources ClinicDS hay antes de eliminar
        BEFORE_COUNT=$(grep -c "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML" || echo "0")
        echo ">>> Número de datasources ClinicDS antes de eliminar: ${BEFORE_COUNT}"
        
        if [ "$BEFORE_COUNT" -gt 1 ]; then
            echo ">>> ADVERTENCIA: Se encontraron ${BEFORE_COUNT} datasources ClinicDS. Eliminando todos..."
            echo ">>> Mostrando todas las ocurrencias antes de eliminar:"
            grep -B 2 -A 5 "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML"
        fi
        
        # Eliminar el datasource existente completamente para recrearlo con las credenciales correctas
        perl -i -0pe 's/<datasource[^>]*jndi-name="java:jboss\/datasources\/ClinicDS"[^>]*>.*?<\/datasource>//gs' "$STANDALONE_XML"
        
        # Verificar que se eliminaron todos
        AFTER_COUNT=$(grep -c "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML" || echo "0")
        echo ">>> Número de datasources ClinicDS después de eliminar: ${AFTER_COUNT}"
        
        if [ "$AFTER_COUNT" -gt 0 ]; then
            echo "ERROR: No se pudieron eliminar todos los datasources ClinicDS. Quedan ${AFTER_COUNT}."
            echo ">>> Mostrando las ocurrencias restantes:"
            grep -B 2 -A 5 "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML"
            exit 1
        fi
        
        echo ">>> Datasource eliminado. Recreando con:"
        echo "    Host: $DB_HOST"
        echo "    Port: $DB_PORT"
        echo "    Database: $DB_NAME"
        echo "    User: $DB_USER"
        
        # Recrear el datasource con las credenciales correctas
        # Usar un archivo temporal para evitar problemas con comillas y caracteres especiales
        TEMP_XML=$(mktemp)
        cat > "$TEMP_XML" <<EOF
                <datasource jndi-name="java:jboss/datasources/ClinicDS" pool-name="ClinicDS" enabled="true">
                    <connection-url>jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}</connection-url>
                    <driver>postgresql</driver>
                    <security user-name="${DB_USER}" password="${DB_PASS_ESC}"/>
                </datasource>
EOF
        
        echo ">>> Contenido del archivo temporal antes de insertar:"
        cat "$TEMP_XML" | sed 's/password="[^"]*"/password="***"/g'
        
        if grep -q "<drivers>" "$STANDALONE_XML"; then
            # Insertar antes de <drivers>
            sed -i "/<drivers>/r $TEMP_XML" "$STANDALONE_XML" || {
                echo "ERROR: No se pudo insertar el datasource antes de <drivers>"
                rm -f "$TEMP_XML"
                exit 1
            }
        else
            # Insertar antes de </datasources>
            sed -i "/<\/datasources>/r $TEMP_XML" "$STANDALONE_XML" || {
                echo "ERROR: No se pudo insertar el datasource antes de </datasources>"
                rm -f "$TEMP_XML"
                exit 1
            }
        fi
        rm -f "$TEMP_XML"
        
        # Verificar que el datasource se insertó correctamente
        if ! grep -q "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML"; then
            echo "ERROR: El datasource ClinicDS no se encontró en standalone-full.xml después de la inserción"
            exit 1
        fi
        
        # Mostrar el bloque completo del datasource insertado para verificación
        echo ">>> Bloque del datasource insertado (ocultando password):"
        grep -A 5 "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML" | head -5 | sed 's/password="[^"]*"/password="***"/g'
        
        echo ">>> Verificado: Datasource ClinicDS presente en standalone-full.xml"
        
        echo ">>> Datasource ClinicDS recreado exitosamente"
    else
        echo "Datasource ClinicDS no encontrado. Creándolo en standalone.xml..."
        # Buscar la sección de datasources e insertar ClinicDS antes de la sección de drivers
        # Usaremos el driver que se despliega automáticamente desde el WAR
        # El nombre del driver será detectado automáticamente por WildFly
        if grep -q "<datasources>" "$STANDALONE_XML"; then
            # Crear el XML del datasource usando un archivo temporal
            TEMP_XML=$(mktemp)
            cat > "$TEMP_XML" <<EOF
                <datasource jndi-name="java:jboss/datasources/ClinicDS" pool-name="ClinicDS" enabled="true">
                    <connection-url>jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}</connection-url>
                    <driver>postgresql</driver>
                    <security>
                        <user-name>${DB_USER}</user-name>
                        <password>${DB_PASS_ESC}</password>
                    </security>
                </datasource>
EOF
            
            echo ">>> Contenido del archivo temporal antes de insertar:"
            cat "$TEMP_XML" | sed 's/password="[^"]*"/password="***"/g'
            
            # Insertar antes de la sección <drivers> o antes del cierre de datasources
            if grep -q "<drivers>" "$STANDALONE_XML"; then
                # Insertar antes de <drivers>
                sed -i "/<drivers>/r $TEMP_XML" "$STANDALONE_XML" || {
                    echo "ERROR: No se pudo insertar el datasource antes de <drivers>"
                    rm -f "$TEMP_XML"
                    exit 1
                }
            else
                # Si no hay drivers, insertar antes del cierre de datasources
                sed -i "/<\/datasources>/r $TEMP_XML" "$STANDALONE_XML" || {
                    echo "ERROR: No se pudo insertar el datasource antes de </datasources>"
                    rm -f "$TEMP_XML"
                    exit 1
                }
            fi
            rm -f "$TEMP_XML"
            
            # Verificar que el datasource se insertó correctamente
            if ! grep -q "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML"; then
                echo "ERROR: El datasource ClinicDS no se encontró en standalone-full.xml después de la inserción"
                exit 1
            fi
            
            # Mostrar el bloque completo del datasource insertado para verificación
            echo ">>> Bloque del datasource insertado (ocultando password):"
            grep -A 5 "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML" | head -5 | sed 's/password="[^"]*"/password="***"/g'
            
            echo ">>> Verificado: Datasource ClinicDS creado y presente en standalone-full.xml"
        else
            echo "Error: No se encontró la sección <datasources> en standalone.xml"
            exit 1
        fi
    fi
    
    # Configurar logging para suprimir warnings de CORBA
    if grep -q "logger category=\"javax.enterprise.resource.corba\"" "$STANDALONE_XML"; then
        echo "Logger de CORBA ya existe, actualizando nivel a ERROR..."
        # Actualizar el nivel si ya existe
        sed -i '/<logger category="javax.enterprise.resource.corba">/,/<\/logger>/{
            s|<level name="[^"]*"/>|<level name="ERROR"/>|
        }' "$STANDALONE_XML"
    else
        echo "Agregando logger para suprimir warnings de CORBA..."
        # Insertar después del cierre del logger sun.rmi
        # Buscar la línea </logger> que viene después de sun.rmi y antes de root-logger
        if grep -q "logger category=\"sun.rmi\"" "$STANDALONE_XML"; then
            # Usar un enfoque simple: buscar la línea </logger> que está entre sun.rmi y root-logger
            # e insertar después de esa línea específica
            sed -i '/<logger category="sun.rmi">/,/<root-logger>/{
                /^[[:space:]]*<\/logger>$/a\
            <logger category="javax.enterprise.resource.corba">\
                <level name="ERROR"/>\
            </logger>
            }' "$STANDALONE_XML"
        elif grep -q "<subsystem xmlns=\"urn:jboss:domain:logging:" "$STANDALONE_XML"; then
            # Si no hay sun.rmi, insertar antes de root-logger
            sed -i '/<root-logger>/i\
            <logger category="javax.enterprise.resource.corba">\
                <level name="ERROR"/>\
            </logger>' "$STANDALONE_XML"
        fi
    fi
    
    echo "Configuración de standalone.xml completada"
    
    # Verificar que el datasource existe y tiene las credenciales correctas
    echo ">>> Verificando configuración final del datasource..."
    echo ">>> Buscando TODOS los datasources en standalone-full.xml:"
    grep -n "jndi-name.*datasources" "$STANDALONE_XML" | head -10
    echo ">>> Contando cuántos datasources ClinicDS hay:"
    CLINIC_DS_COUNT=$(grep -c "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML" || echo "0")
    echo ">>> Número de datasources ClinicDS encontrados: ${CLINIC_DS_COUNT}"
    
    if [ "$CLINIC_DS_COUNT" -gt 1 ]; then
        echo "ERROR: Se encontraron ${CLINIC_DS_COUNT} datasources ClinicDS. Esto causará problemas."
        echo ">>> Mostrando todas las ocurrencias:"
        grep -B 2 -A 10 "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML"
        exit 1
    fi
    
    if grep -q "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML"; then
        echo ">>> Datasource ClinicDS encontrado en standalone-full.xml"
        
        # Extraer el bloque completo del datasource para verificación
        DS_BLOCK=$(grep -A 10 "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML" | head -10)
        echo ">>> Bloque completo del datasource:"
        echo "$DS_BLOCK" | sed 's/password="[^"]*"/password="***"/g'
        
        # Extraer y mostrar el usuario configurado
        DS_USER=$(grep -A 10 "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML" | grep "user-name" | sed -n 's/.*user-name="\([^"]*\)".*/\1/p' | head -1)
        DS_HOST=$(grep -A 10 "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML" | grep "connection-url" | sed -n 's/.*jdbc:postgresql:\/\/\([^:]*\):.*/\1/p' | head -1)
        DS_URL=$(grep -A 10 "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML" | grep "connection-url" | sed -n 's/.*connection-url>\([^<]*\)<.*/\1/p' | head -1)
        
        echo ">>> Usuario configurado en datasource: '${DS_USER}'"
        echo ">>> Host configurado en datasource: '${DS_HOST}'"
        echo ">>> URL completa: '${DS_URL}'"
        echo ">>> Usuario esperado: '${DB_USER}'"
        
        if [ -z "$DS_USER" ]; then
            echo "ERROR: No se pudo extraer el usuario del datasource. El XML podría estar mal formado."
            echo ">>> Buscando todas las ocurrencias de ClinicDS:"
            grep -n "ClinicDS" "$STANDALONE_XML" | head -20
            exit 1
        fi
        
        if [ "$DS_USER" != "$DB_USER" ]; then
            echo "ERROR: El usuario en el datasource ('${DS_USER}') no coincide con el esperado ('${DB_USER}')"
            echo ">>> Mostrando el bloque completo del datasource para debug:"
            grep -A 15 "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML" | head -15
            exit 1
        fi
        echo ">>> Verificación exitosa: Usuario correcto en datasource"
        
        # Verificación final: mostrar el XML completo del datasource tal como está en el archivo
        echo ">>> Verificación final: XML completo del datasource ClinicDS en standalone-full.xml:"
        awk '/jndi-name="java:jboss\/datasources\/ClinicDS"/,/<\/datasource>/' "$STANDALONE_XML" | sed 's/password="[^"]*"/password="***"/g'
    else
        echo "ERROR: Datasource ClinicDS no encontrado en standalone-full.xml después de la configuración"
        echo ">>> Buscando datasources en el archivo:"
        grep -n "jndi-name.*datasources" "$STANDALONE_XML" | head -10
        exit 1
    fi
else
    echo "Advertencia: standalone.xml no encontrado. Se generará al iniciar WildFly."
fi

echo "WildFly configurado exitosamente"

