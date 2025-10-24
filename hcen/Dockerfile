FROM quay.io/wildfly/wildfly:37.0.0.Final-jdk21
COPY pr1/pr1/build/libs/*.war /opt/jboss/wildfly/standalone/deployments/app.war
# Dockerfile for Jakarta EE app packaged on WildFly
# Assumes WAR built at pr1/pr1/build/libs/*.war
# CI pipeline builds WAR before this image.

FROM quay.io/wildfly/wildfly:37.0.0.Final-jdk21
LABEL maintainer="<your name>" app="jakarta-ee-hcen"

# Copy WAR produced by Gradle. If multiple WARs appear, only first matched will be used.
ARG WAR_PATH=pr1/pr1/build/libs
COPY ${WAR_PATH}/*.war /opt/jboss/wildfly/standalone/deployments/app.war

# Optionally set JVM options via env at runtime (Virtuozzo dashboard)
ENV JAVA_OPTS="-Xms256m -Xmx768m -XX:MaxMetaspaceSize=256m"

EXPOSE 8080

# Base image already defines entrypoint/cmd (standalone mode)
