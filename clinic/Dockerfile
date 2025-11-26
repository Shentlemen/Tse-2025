# Dockerfile para desplegar Clinic en Render
# Usar la imagen oficial de WildFly desde Docker Hub
FROM jboss/wildfly:30.0.1.Final

# Usuario para ejecutar WildFly
USER root

# Instalar herramientas necesarias
RUN microdnf install -y wget unzip && microdnf clean all

# Crear directorio para el driver PostgreSQL
RUN mkdir -p /opt/jboss/wildfly/modules/org/postgresql/main

# Descargar e instalar driver PostgreSQL
RUN cd /opt/jboss/wildfly/modules/org/postgresql/main && \
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
    echo '</module>' >> module.xml

# Copiar el WAR compilado (debe estar en build/libs/clinic.war)
# IMPORTANTE: El proyecto debe compilarse ANTES de construir la imagen Docker
# Opción 1: Compilar localmente y hacer commit del WAR
# Opción 2: Usar un build step en Render que compile antes del Docker build
COPY build/libs/clinic.war /opt/jboss/wildfly/standalone/deployments/

# Script para configurar WildFly con variables de entorno
COPY configure-wildfly.sh /opt/jboss/wildfly/bin/
RUN chmod +x /opt/jboss/wildfly/bin/configure-wildfly.sh

# Script de inicio
COPY start-wildfly.sh /opt/jboss/wildfly/bin/
RUN chmod +x /opt/jboss/wildfly/bin/start-wildfly.sh

# Script para migraciones (opcional, se puede ejecutar manualmente)
COPY run-migrations.sh /opt/jboss/wildfly/bin/
RUN chmod +x /opt/jboss/wildfly/bin/run-migrations.sh

# Copiar Gradle wrapper para ejecutar migraciones (si es necesario)
COPY gradlew /opt/jboss/wildfly/bin/
COPY gradle/ /opt/jboss/wildfly/bin/gradle/
COPY build.gradle /opt/jboss/wildfly/bin/
COPY gradle.properties /opt/jboss/wildfly/bin/ 2>/dev/null || true
COPY src/main/resources/db/migration/ /opt/jboss/wildfly/bin/migrations/

# Volver al usuario jboss
USER jboss

# Exponer puerto (Render lo configurará automáticamente)
EXPOSE 8080

# Comando de inicio
CMD ["/opt/jboss/wildfly/bin/start-wildfly.sh"]
