# Dockerfile para desplegar Clinic en Render
# Usar imagen base con Java 21 e instalar WildFly manualmente
FROM eclipse-temurin:21-jdk-ubi9-minimal

# Usuario root para instalación
USER root

# Instalar herramientas necesarias
RUN microdnf install -y wget unzip tar gzip && microdnf clean all

# Variables de entorno
ENV WILDFLY_VERSION=30.0.1.Final
ENV WILDFLY_HOME=/opt/jboss/wildfly
ENV JBOSS_HOME=$WILDFLY_HOME

# Crear usuario jboss
RUN groupadd -r jboss && useradd -r -g jboss -d $WILDFLY_HOME -s /sbin/nologin jboss

# Descargar e instalar WildFly
RUN cd /opt/jboss && \
    wget -q https://github.com/wildfly/wildfly/releases/download/${WILDFLY_VERSION}/wildfly-${WILDFLY_VERSION}.tar.gz && \
    tar -xzf wildfly-${WILDFLY_VERSION}.tar.gz && \
    mv wildfly-${WILDFLY_VERSION} wildfly && \
    rm wildfly-${WILDFLY_VERSION}.tar.gz && \
    chown -R jboss:jboss $WILDFLY_HOME

# Crear directorio para el driver PostgreSQL
RUN mkdir -p $WILDFLY_HOME/modules/org/postgresql/main

# Descargar e instalar driver PostgreSQL
RUN cd $WILDFLY_HOME/modules/org/postgresql/main && \
    wget -q https://jdbc.postgresql.org/download/postgresql-42.7.0.jar && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > module.xml && \
    echo '<module xmlns="urn:jboss:module:1.9" name="org.postgresql">' >> module.xml && \
    echo '  <resources>' >> module.xml && \
    echo '    <resource-root path="postgresql-42.7.0.jar"/>' >> module.xml && \
    echo '  </resources>' >> module.xml && \
    echo '  <dependencies>' >> module.xml && \
    echo '    <module name="jakarta.api"/>' >> module.xml && \
    echo '    <module name="jakarta.transaction.api"/>' >> module.xml && \
    echo '  </dependencies>' >> module.xml && \
    echo '</module>' >> module.xml && \
    chown -R jboss:jboss $WILDFLY_HOME/modules

# Copiar el WAR compilado
COPY build/libs/clinic.war $WILDFLY_HOME/standalone/deployments/
RUN chown jboss:jboss $WILDFLY_HOME/standalone/deployments/clinic.war

# Script para configurar WildFly con variables de entorno
COPY configure-wildfly.sh $WILDFLY_HOME/bin/
RUN chmod +x $WILDFLY_HOME/bin/configure-wildfly.sh && \
    chown jboss:jboss $WILDFLY_HOME/bin/configure-wildfly.sh

# Script de inicio
COPY start-wildfly.sh $WILDFLY_HOME/bin/
RUN chmod +x $WILDFLY_HOME/bin/start-wildfly.sh && \
    chown jboss:jboss $WILDFLY_HOME/bin/start-wildfly.sh

# Script para migraciones (opcional, se puede ejecutar manualmente)
COPY run-migrations.sh $WILDFLY_HOME/bin/
RUN chmod +x $WILDFLY_HOME/bin/run-migrations.sh && \
    chown jboss:jboss $WILDFLY_HOME/bin/run-migrations.sh

# Copiar Gradle wrapper para ejecutar migraciones (si es necesario)
COPY gradlew $WILDFLY_HOME/bin/
COPY gradle/ $WILDFLY_HOME/bin/gradle/
COPY build.gradle $WILDFLY_HOME/bin/
COPY gradle.properties $WILDFLY_HOME/bin/ 2>/dev/null || true
COPY src/main/resources/db/migration/ $WILDFLY_HOME/bin/migrations/
RUN chown -R jboss:jboss $WILDFLY_HOME/bin

# Volver al usuario jboss
USER jboss

# Exponer puerto (Render lo configurará automáticamente)
EXPOSE 8080

# Comando de inicio
CMD ["/opt/jboss/wildfly/bin/start-wildfly.sh"]
