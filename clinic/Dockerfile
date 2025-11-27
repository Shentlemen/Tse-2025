# Imagen base con Java 21
FROM eclipse-temurin:21-jdk-ubi9-minimal

USER root

# Instalar herramientas necesarias
RUN microdnf install -y wget unzip tar gzip perl shadow-utils && \
    microdnf clean all

# Variables
ENV WILDFLY_VERSION=30.0.1.Final
ENV WILDFLY_HOME=/opt/jboss/wildfly
ENV JBOSS_HOME=$WILDFLY_HOME

# Crear usuario jboss
RUN mkdir -p /opt/jboss && \
    groupadd -r jboss && \
    useradd -r -g jboss -d $WILDFLY_HOME -s /sbin/nologin jboss

# Descargar e instalar WildFly
RUN cd /opt/jboss && \
    wget -q https://github.com/wildfly/wildfly/releases/download/${WILDFLY_VERSION}/wildfly-${WILDFLY_VERSION}.tar.gz && \
    tar -xzf wildfly-${WILDFLY_VERSION}.tar.gz && \
    mv wildfly-${WILDFLY_VERSION} wildfly && \
    rm wildfly-${WILDFLY_VERSION}.tar.gz && \
    chown -R jboss:jboss $WILDFLY_HOME

# Instalar driver PostgreSQL como módulo
RUN mkdir -p $WILDFLY_HOME/modules/org/postgresql/main && \
    cd $WILDFLY_HOME/modules/org/postgresql/main && \
    wget -q https://jdbc.postgresql.org/download/postgresql-42.7.0.jar && \
    cat <<EOF > module.xml
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.9" name="org.postgresql">
  <resources>
    <resource-root path="postgresql-42.7.0.jar"/>
  </resources>
  <dependencies>
    <module name="jakarta.api"/>
    <module name="javaee.api" optional="true"/>
  </dependencies>
</module>
EOF
RUN chown -R jboss:jboss $WILDFLY_HOME/modules

# Modificar standalone-full.xml (eliminar ExampleDS y registrar driver PostgreSQL)
RUN STANDALONE_XML="$WILDFLY_HOME/standalone/configuration/standalone-full.xml" && \
    if [ -f "$STANDALONE_XML" ]; then \
        perl -i -0pe 's/<datasource[^>]*jndi-name="java:jboss\/datasources\/ExampleDS"[^>]*>.*?<\/datasource>//gs' "$STANDALONE_XML" && \
        sed -i 's|datasource="java:jboss/datasources/ExampleDS"|datasource="java:jboss/datasources/ClinicDS"|g' "$STANDALONE_XML" && \
        if ! grep -q 'driver name="postgresql"' "$STANDALONE_XML"; then \
            sed -i '/<\/drivers>/i\
                <driver name="postgresql" module="org.postgresql">\
                    <driver-class>org.postgresql.Driver</driver-class>\
                </driver>' "$STANDALONE_XML"; \
        fi; \
    fi && \
    chown -R jboss:jboss $WILDFLY_HOME

# Copiar WAR
COPY build/libs/clinic.war $WILDFLY_HOME/standalone/deployments/
RUN chown jboss:jboss $WILDFLY_HOME/standalone/deployments/clinic.war && \
    touch $WILDFLY_HOME/standalone/deployments/clinic.war.dodeploy && \
    chown jboss:jboss $WILDFLY_HOME/standalone/deployments/clinic.war.dodeploy

# Copiar scripts
COPY configure-wildfly.sh $WILDFLY_HOME/bin/
COPY start-wildfly.sh $WILDFLY_HOME/bin/
COPY run-migrations.sh $WILDFLY_HOME/bin/

RUN chmod +x $WILDFLY_HOME/bin/*.sh && \
    chown -R jboss:jboss $WILDFLY_HOME/bin

# Crear carpeta de migraciones (opcional)
RUN mkdir -p $WILDFLY_HOME/bin/migrations && \
    chown -R jboss:jboss $WILDFLY_HOME/bin/migrations

# Crear usuario admin (si falla, no rompe el build)
# Nota: Usar espacios en lugar de = para los parámetros
RUN $WILDFLY_HOME/bin/add-user.sh \
    --silent \
    --user admin \
    --password admin \
    --realm ManagementRealm || true

# Cambiar a usuario jboss
USER jboss

EXPOSE 8080

# Iniciar WildFly con nuestro script personalizado
CMD ["/opt/jboss/wildfly/bin/start-wildfly.sh"]
