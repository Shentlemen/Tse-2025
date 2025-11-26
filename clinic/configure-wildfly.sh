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
    
    # Verificar si el datasource ya existe y actualizarlo
    if grep -q "data-source name=\"ClinicDS\"" "$STANDALONE_XML"; then
        echo "Actualizando datasource ClinicDS existente..."
        # Actualizar connection-url (más específico para evitar reemplazos incorrectos)
        sed -i "/data-source name=\"ClinicDS\"/,/<\/data-source>/ s|connection-url>jdbc:postgresql://[^<]*<|connection-url>jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}<|g" "$STANDALONE_XML"
        
        # Actualizar user-name (dentro del bloque ClinicDS)
        sed -i "/data-source name=\"ClinicDS\"/,/<\/data-source>/ s|<user-name>[^<]*</user-name>|<user-name>${DB_USER}</user-name>|g" "$STANDALONE_XML"
        
        # Actualizar password (dentro del bloque ClinicDS)
        sed -i "/data-source name=\"ClinicDS\"/,/<\/data-source>/ s|<password>[^<]*</password>|<password>${DB_PASS_ESC}</password>|g" "$STANDALONE_XML"
        
        echo "Datasource ClinicDS actualizado en standalone.xml"
    else
        echo "Datasource ClinicDS no encontrado. Se creará vía CLI al iniciar WildFly."
    fi
    
    # Configurar logging para suprimir warnings de CORBA
    if grep -q "logger category=\"javax.enterprise.resource.corba\"" "$STANDALONE_XML"; then
        echo "Logger de CORBA ya existe, actualizando nivel a ERROR..."
        sed -i 's|<logger category="javax.enterprise.resource.corba".*level="[^"]*".*/>|<logger category="javax.enterprise.resource.corba" level="ERROR"/>|g' "$STANDALONE_XML"
    else
        echo "Agregando logger para suprimir warnings de CORBA..."
        # Buscar la sección de loggers dentro del subsystem de logging
        # Insertar después de la etiqueta de apertura del subsystem o después del último logger
        if grep -q "<subsystem xmlns=\"urn:jboss:domain:logging:" "$STANDALONE_XML"; then
            # Buscar la línea que contiene <loggers> o insertar antes de </subsystem>
            if grep -q "<loggers>" "$STANDALONE_XML"; then
                # Insertar después de <loggers>
                sed -i '/<loggers>/a\
            <logger category="javax.enterprise.resource.corba" level="ERROR"/>' "$STANDALONE_XML"
            else
                # Si no hay <loggers>, insertar antes de </subsystem> del logging
                sed -i '/<subsystem xmlns="urn:jboss:domain:logging:/,/<\/subsystem>/{
                    /<\/subsystem>/i\
            <loggers>\
                <logger category="javax.enterprise.resource.corba" level="ERROR"/>\
            </loggers>
                }' "$STANDALONE_XML"
            fi
        fi
    fi
    
    echo "Configuración de standalone.xml completada"
else
    echo "Advertencia: standalone.xml no encontrado. Se generará al iniciar WildFly."
fi

echo "WildFly configurado exitosamente"

