# ==============================================
# HCEN Central - Production Dockerfile
# ==============================================
# WildFly 37.0.0 (Jakarta EE 10) with Java 21
# Builds containerized deployment for ANTEL mi-nube
#
# Build context: project root (tse-2025/hcen/)
# WAR artifact: pr1/pr1/build/libs/hcen-1.0-SNAPSHOT.war
#
# Build: docker build -t hcen:latest .
# Run:   docker run -p 8080:8080 -e JAVA_OPTS="-Xmx1g" hcen:latest
# ==============================================

FROM quay.io/wildfly/wildfly:37.0.0.Final-jdk21

LABEL maintainer="TSE 2025 Group 9" \
      app="hcen-central" \
      version="1.0.0" \
      description="HCEN - Historia Clínica Electrónica Nacional"

# Copy WAR from Gradle build output
# When building from repo root: docker build hcen/
# When building from hcen dir: docker build .
# GitLab CI builds from repo root, so paths are relative to hcen/
ARG WAR_PATH=build/libs
COPY ${WAR_PATH}/*.war /opt/jboss/wildfly/standalone/deployments/hcen.war

# Create directories for configuration and keys
RUN mkdir -p /opt/hcen/config \
    && mkdir -p /opt/hcen/keys \
    && mkdir -p /opt/hcen/logs \
    && chown -R jboss:jboss /opt/hcen

# JVM memory configuration
# Recommended for production: -Xms1g -Xmx2g -XX:MaxMetaspaceSize=512m
# ANTEL mi-nube: Adjust based on container resources
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:MaxMetaspaceSize=256m \
    -Djava.net.preferIPv4Stack=true \
    -Djboss.modules.system.pkgs=org.jboss.byteman \
    -Djava.awt.headless=true"

# WildFly management configuration (optional - for monitoring)
# Uncomment to enable management console
# ENV WILDFLY_ADMIN_USER=admin
# ENV WILDFLY_ADMIN_PASSWORD=admin123

# Application configuration override path
# Mount production application.properties at runtime:
# -v /path/to/application.properties:/opt/hcen/config/application.properties
ENV HCEN_CONFIG_PATH=/opt/hcen/config/application.properties

# Expose HTTP port (WildFly default)
EXPOSE 8080

# Expose management port (optional - for health checks)
EXPOSE 9990

# Health check endpoint
# WildFly provides /health endpoint by default
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:9990/health || exit 1

# WildFly standalone mode with custom configuration
# Base image already defines ENTRYPOINT/CMD for standalone.sh
# Custom standalone.xml can be mounted at:
# /opt/jboss/wildfly/standalone/configuration/standalone.xml

# User: jboss (non-root, defined in base image)
USER jboss

# Working directory
WORKDIR /opt/jboss/wildfly
