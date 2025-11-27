#!/bin/bash
set -e

STANDALONE_XML="/opt/jboss/wildfly/standalone/configuration/standalone-full.xml"

echo ">>> Usando archivo: $STANDALONE_XML"

# Leer DATABASE_URL
if [ -z "$DATABASE_URL" ]; then
    echo "ERROR: DATABASE_URL no está definido"
    exit 1
fi

# Parseo seguro del DATABASE_URL
regex="postgres://([^:]+):([^@]+)@([^:]+):([0-9]+)/(.+)"
if [[ $DATABASE_URL =~ $regex ]]; then
    DB_USER="${BASH_REMATCH[1]}"
    DB_PASSWORD="${BASH_REMATCH[2]}"
    DB_HOST="${BASH_REMATCH[3]}"
    DB_PORT="${BASH_REMATCH[4]}"
    DB_NAME="${BASH_REMATCH[5]}"
else
    echo "ERROR: DATABASE_URL no tiene formato válido."
    exit 1
fi

echo ">>> Conexión:"
echo "    Host: $DB_HOST"
echo "    Base: $DB_NAME"
echo "    Usuario: $DB_USER"
echo "    Puerto: $DB_PORT"

# Borrar ExampleDS
perl -i -0pe 's/<datasource[^>]*jndi-name="java:jboss\/datasources\/ExampleDS"[^>]*>.*?<\/datasource>//gs' "$STANDALONE_XML"

# Insertar ClinicDS SIN <security> (válido para WildFly 30)
sed -i "/<\/datasources>/ i \
        <datasource jndi-name=\"java:jboss/datasources/ClinicDS\" pool-name=\"ClinicDS\" enabled=\"true\"> \
            <connection-url>jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?user=${DB_USER}&password=${DB_PASSWORD}</connection-url> \
            <driver>postgresql</driver> \
        </datasource>" "$STANDALONE_XML"

# Agregar driver si falta
if ! grep -q "driver name=\"postgresql\"" "$STANDALONE_XML"; then
    sed -i '/<\/drivers>/ i \
            <driver name="postgresql" module="org.postgresql"> \
                <driver-class>org.postgresql.Driver</driver-class> \
            </driver>' "$STANDALONE_XML"
fi

echo ">>> DataSource configurado correctamente"
