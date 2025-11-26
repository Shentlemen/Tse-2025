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
    DB_HOST=${PGHOST:-localhost}
    DB_PORT=${PGPORT:-5432}
    DB_NAME=${PGDATABASE:-clinic_db}
    DB_USER=${PGUSER:-postgres}
    DB_PASS=${PGPASSWORD:-sora}
fi

# Obtener puerto de Render (default 8080)
RENDER_PORT=${PORT:-8080}

# Escapar caracteres especiales para XML
DB_PASS_ESC=$(echo "$DB_PASS" | sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g; s/"/\&quot;/g; s/'"'"'/\&apos;/g')

echo "Configurando WildFly con:"
echo "  DB_HOST: $DB_HOST"
echo "  DB_PORT: $DB_PORT"
echo "  DB_NAME: $DB_NAME"
echo "  DB_USER: $DB_USER"
echo "  PORT: $RENDER_PORT"

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
    
    # Cambiar default-bindings para usar ClinicDS (ExampleDS ya fue eliminado en el Dockerfile)
    # Solo actualizar si todavía apunta a ExampleDS (por si acaso)
    if grep -q 'datasource="java:jboss/datasources/ExampleDS"' "$STANDALONE_XML"; then
        echo "Actualizando default-bindings para usar ClinicDS..."
        sed -i 's|datasource="java:jboss/datasources/ExampleDS"|datasource="java:jboss/datasources/ClinicDS"|g' "$STANDALONE_XML"
        echo "Default datasource cambiado a ClinicDS"
    fi
    
    # Verificar si el datasource ClinicDS ya existe y actualizarlo
    if grep -q "jndi-name=\"java:jboss/datasources/ClinicDS\"" "$STANDALONE_XML"; then
        echo "Actualizando datasource ClinicDS existente..."
        # Actualizar connection-url (más específico para evitar reemplazos incorrectos)
        sed -i "/jndi-name=\"java:jboss\/datasources\/ClinicDS\"/,/<\/datasource>/ s|connection-url>jdbc:postgresql://[^<]*<|connection-url>jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}<|g" "$STANDALONE_XML"
        
        # Actualizar security con atributos (formato correcto para WildFly 30)
        # Reemplazar el elemento security completo con el formato de atributos
        sed -i "/jndi-name=\"java:jboss\/datasources\/ClinicDS\"/,/<\/datasource>/ {
            /<security.*\/>/ s|user-name=\"[^\"]*\"|user-name=\"${DB_USER}\"|g
            /<security.*\/>/ s|password=\"[^\"]*\"|password=\"${DB_PASS_ESC}\"|g
            # Si está en formato antiguo (elementos), reemplazarlo por formato de atributos
            /<security>/,/<\/security>/ {
                /<security>/ {
                    s|<security>|<security user-name=\"${DB_USER}\" password=\"${DB_PASS_ESC}\"/>|
                    d
                }
                /<user-name>/d
                /<password>/d
                /<\/security>/d
            }
        }" "$STANDALONE_XML"
        
        echo "Datasource ClinicDS actualizado en standalone.xml"
    else
        echo "Datasource ClinicDS no encontrado. Creándolo en standalone.xml..."
        # Buscar la sección de datasources e insertar ClinicDS antes de la sección de drivers
        if grep -q "<datasources>" "$STANDALONE_XML"; then
            # Insertar antes de la sección <drivers>
            if grep -q "<drivers>" "$STANDALONE_XML"; then
                sed -i '/<drivers>/i\
                <datasource jndi-name="java:jboss/datasources/ClinicDS" pool-name="ClinicDS" enabled="true">\
                    <connection-url>jdbc:postgresql://'${DB_HOST}':'${DB_PORT}'/'${DB_NAME}'</connection-url>\
                    <driver>postgresql</driver>\
                    <security user-name="'${DB_USER}'" password="'${DB_PASS_ESC}'"/>\
                </datasource>' "$STANDALONE_XML"
            else
                # Si no hay drivers, insertar antes del cierre de datasources
                sed -i '/<\/datasources>/i\
                <datasource jndi-name="java:jboss/datasources/ClinicDS" pool-name="ClinicDS" enabled="true">\
                    <connection-url>jdbc:postgresql://'${DB_HOST}':'${DB_PORT}'/'${DB_NAME}'</connection-url>\
                    <driver>postgresql</driver>\
                    <security user-name="'${DB_USER}'" password="'${DB_PASS_ESC}'"/>\
                </datasource>' "$STANDALONE_XML"
            fi
            echo "Datasource ClinicDS creado en standalone.xml"
        else
            echo "Error: No se encontró la sección <datasources> en standalone.xml"
        fi
        
        # Asegurar que el driver PostgreSQL esté registrado
        if ! grep -q "driver name=\"postgresql\"" "$STANDALONE_XML"; then
            echo "Registrando driver PostgreSQL..."
            if grep -q "<drivers>" "$STANDALONE_XML"; then
                # Insertar driver antes del cierre de drivers
                sed -i '/<\/drivers>/i\
                <driver name="postgresql" module="org.postgresql">\
                    <driver-class>org.postgresql.Driver</driver-class>\
                </driver>' "$STANDALONE_XML"
                echo "Driver PostgreSQL registrado"
            else
                echo "Advertencia: No se encontró la sección <drivers> para registrar el driver PostgreSQL"
            fi
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
else
    echo "Advertencia: standalone.xml no encontrado. Se generará al iniciar WildFly."
fi

echo "WildFly configurado exitosamente"

