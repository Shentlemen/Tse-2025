# Infrastructure Requirements - Health Provider Component

This document describes the infrastructure and configuration requirements for the Health Provider Reference Implementation.

## Overview

The Health Provider component is a lightweight Jakarta EE application that provides a REST API for clinical document management and integration with HCEN Central. It requires WildFly application server and PostgreSQL database.

---

## WildFly Configuration

### Version Requirements
- **WildFly**: 27.0.0+ (Jakarta EE 10)
- **Java**: 11 or higher

### Datasource Configuration

Add the following datasource configuration to `standalone.xml`:

```xml
<datasource jndi-name="java:jboss/datasources/HealthProviderDS" pool-name="HealthProviderDS" enabled="true">
    <connection-url>jdbc:postgresql://localhost:5432/health_provider</connection-url>
    <driver>postgresql</driver>
    <security>
        <user-name>postgres</user-name>
        <password>postgres</password>
    </security>
    <validation>
        <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker"/>
        <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter"/>
    </validation>
</datasource>
```

### PostgreSQL JDBC Driver

Ensure the PostgreSQL JDBC driver is installed as a WildFly module:

```xml
<driver name="postgresql" module="org.postgresql">
    <xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>
</driver>
```

---

## API Security Configuration

### API Key Authentication

The Health Provider component uses API key authentication to protect clinical document endpoints (`/api/documents/*`).

#### Configuration File Location

API keys are configured in `api-config.properties`. This file can be placed in:

1. **External configuration** (recommended for production):
   - Path: `${jboss.server.config.dir}/api-config.properties`
   - Example: `C:\wildfly\standalone\configuration\api-config.properties`
   - Example (Linux): `/opt/wildfly/standalone/configuration/api-config.properties`

2. **Classpath resource** (fallback for development):
   - Path: `src/main/resources/api-config.properties`
   - Packaged inside WAR file

#### Configuration Properties

```properties
# API Key for authenticating requests from HCEN Central
# IMPORTANT: Generate a secure random key for production
# Example generation: openssl rand -hex 32
api.key=change-me-in-production-secure-random-key-12345

# Enable/disable API key authentication (default: true)
# Set to false for local development/testing only
api.auth.enabled=true
```

#### Generating Secure API Keys

For production deployments, generate a cryptographically secure API key:

**Linux/macOS:**
```bash
openssl rand -hex 32
```

**Windows (PowerShell):**
```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

**Java:**
```java
SecureRandom random = new SecureRandom();
byte[] bytes = new byte[32];
random.nextBytes(bytes);
String apiKey = Base64.getEncoder().encodeToString(bytes);
```

#### Runtime Configuration Reload

The API configuration supports **hot reload** without application restart:

1. Edit the external `api-config.properties` file
2. Save changes
3. Configuration is automatically reloaded on next request
4. No WildFly restart required

---

## Database Configuration

### PostgreSQL Database

#### Version Requirements
- **PostgreSQL**: 12+ (recommended: 14+)

#### Database Setup

```sql
-- Create database
CREATE DATABASE health_provider;

-- Create user
CREATE USER postgres WITH PASSWORD 'postgres';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE health_provider TO postgres;
```

#### Connection Pool Settings

Recommended connection pool configuration in `standalone.xml`:

```xml
<datasource jndi-name="java:jboss/datasources/HealthProviderDS" pool-name="HealthProviderDS">
    <!-- ... connection settings ... -->
    <pool>
        <min-pool-size>5</min-pool-size>
        <max-pool-size>20</max-pool-size>
        <prefill>true</prefill>
    </pool>
    <timeout>
        <idle-timeout-minutes>15</idle-timeout-minutes>
    </timeout>
</datasource>
```

#### Database Schema Migration

Database schema is managed using **Flyway** migrations:

- Migration scripts location: `src/main/resources/db/migration/`
- Naming convention: `V{version}__{description}.sql`
- Migrations run automatically on application startup

Existing migrations:
- `V001__create_patients_table.sql` - Patient registry
- `V002__create_clinical_documents_table.sql` - Clinical documents metadata

---

## JMS Configuration (HCEN Integration)

### Remote JMS Connection to HCEN Central

The Health Provider sends document metadata to HCEN Central using JMS queues.

#### JNDI Configuration

File: `src/main/resources/jndi.properties`

```properties
# Initial Context Factory
java.naming.factory.initial=org.wildfly.naming.client.WildFlyInitialContextFactory

# HCEN Central WildFly Server URL
java.naming.provider.url=http-remoting://localhost:8080

# Connection Factory JNDI Name
jms.connectionFactoryNames=jms/RemoteConnectionFactory

# Queue JNDI Names
jms.queue.userRegistration=jms/queue/UserRegistration
jms.queue.documentRegistration=jms/queue/DocumentRegistration
```

#### Required WildFly Modules

Ensure the following modules are available (included in WildFly by default):

- `org.wildfly.naming-client`
- `org.jboss.ejb-client`
- `org.jboss.xnio`

#### HCEN Central Endpoint

Update `java.naming.provider.url` to point to the actual HCEN Central server:

**Development:**
```properties
java.naming.provider.url=http-remoting://localhost:8080
```

**Production:**
```properties
java.naming.provider.url=http-remoting://hcen-central.example.uy:8080
```

---

## Network Configuration

### Required Ports

| Port | Protocol | Purpose |
|------|----------|---------|
| 8080 | HTTP | Application server (development) |
| 8443 | HTTPS | Application server (production) |
| 9990 | HTTP | WildFly management console |
| 5432 | TCP | PostgreSQL database |

### Firewall Rules

**Inbound (Health Provider Server):**
- Allow 8080/8443 from HCEN Central IP addresses
- Allow 9990 from management network only
- Allow 5432 from localhost only (do not expose PostgreSQL externally)

**Outbound (Health Provider Server):**
- Allow connection to HCEN Central on port 8080 (JMS remote)

---

## SSL/TLS Configuration (Production)

### HTTPS Certificate

For production deployments, configure HTTPS in `standalone.xml`:

```xml
<security-realm name="ApplicationRealm">
    <server-identities>
        <ssl>
            <keystore path="keystore.jks"
                      relative-to="jboss.server.config.dir"
                      keystore-password="changeit"
                      alias="server"/>
        </ssl>
    </server-identities>
</security-realm>
```

Update Undertow HTTPS listener:

```xml
<https-listener name="https"
                socket-binding="https"
                security-realm="ApplicationRealm"
                enable-http2="true"/>
```

### Certificate Requirements

- **Certificate type**: X.509 (TLS/SSL)
- **Key algorithm**: RSA 2048-bit or higher
- **Certificate format**: JKS or PKCS12 keystore
- **Certificate location**: `${jboss.server.config.dir}/keystore.jks`

---

## File Storage

### Clinical Document Storage

Clinical document files (PDFs, images, attachments) are stored in the local filesystem.

#### Storage Path Configuration

Default storage path is configured in the application. For production, mount a dedicated volume:

**Linux:**
```
/var/lib/health_provider/documents/
```

**Windows:**
```
C:\health_provider\documents\
```

#### Storage Requirements

- **Initial capacity**: 50 GB (minimum)
- **Growth rate**: Estimate 100 MB per 1000 documents
- **Backup**: Daily incremental backups recommended
- **Retention**: As per organizational policy (typically 7 years for medical records)

#### Permissions

Ensure WildFly process has read/write access to the storage directory:

```bash
# Linux
mkdir -p /var/lib/health_provider/documents
chown wildfly:wildfly /var/lib/health_provider/documents
chmod 750 /var/lib/health_provider/documents
```

---

## Monitoring and Logging

### Application Logging

Logs are written to WildFly server log:

**Location:**
- Linux: `/opt/wildfly/standalone/log/server.log`
- Windows: `C:\wildfly\standalone\log\server.log`

**Log Levels:**
- `ERROR`: System failures, unhandled exceptions
- `WARNING`: API authentication failures, missing patients, JMS send failures
- `INFO`: API authentication success, document creation, HCEN registration
- `DEBUG`: Detailed request/response flow (not recommended for production)

### Health Check Endpoints

**Application Health:**
```
GET /api/documents
```

If the application is running and database is accessible, this will return HTTP 200 (with valid API key).

---

## Deployment Checklist

### Pre-Deployment

- [ ] PostgreSQL database created and accessible
- [ ] WildFly datasource configured in `standalone.xml`
- [ ] PostgreSQL JDBC driver installed in WildFly
- [ ] External `api-config.properties` created with secure API key
- [ ] HCEN Central endpoint configured in `jndi.properties`
- [ ] Document storage directory created with correct permissions
- [ ] SSL/TLS certificate installed (production only)
- [ ] Firewall rules configured

### Deployment

```bash
# Build WAR file
cd prestador
mvn clean package

# Deploy to WildFly
cp target/prestador.war /opt/wildfly/standalone/deployments/

# Verify deployment
tail -f /opt/wildfly/standalone/log/server.log
```

### Post-Deployment

- [ ] Verify application startup in `server.log`
- [ ] Verify database connection (check for Flyway migration logs)
- [ ] Test API endpoint with API key: `GET /api/documents`
- [ ] Test API authentication rejection (invalid key)
- [ ] Verify JMS connection to HCEN Central
- [ ] Create test clinical document
- [ ] Verify document metadata sent to HCEN RNDC

---

## Troubleshooting

### Common Issues

**Issue: "Invalid or missing API key" error**

- **Cause**: API key not provided or incorrect in X-API-Key header
- **Solution**: Verify API key in `api-config.properties` matches the key sent in request header

**Issue: "Database connection pool exhausted"**

- **Cause**: Too many concurrent requests or connection leak
- **Solution**: Increase `max-pool-size` in datasource configuration, check for unclosed connections

**Issue: "Failed to send document metadata to HCEN"**

- **Cause**: JMS connection failure, HCEN Central not accessible
- **Solution**: Verify `jndi.properties` configuration, check network connectivity, verify HCEN queues exist

**Issue: "Configuration file not found"**

- **Cause**: `api-config.properties` not in expected location
- **Solution**: Place file in `${jboss.server.config.dir}/api-config.properties` or ensure classpath resource is packaged in WAR

---

## Security Recommendations

### Production Deployment

1. **API Key Security:**
   - Generate cryptographically secure random API key (32+ bytes)
   - Never commit API key to version control
   - Rotate API key periodically (every 90 days)
   - Use different API keys for each environment (dev/staging/production)

2. **Database Security:**
   - Use strong PostgreSQL password
   - Restrict database access to localhost only
   - Enable SSL/TLS for database connections
   - Regular security patches and updates

3. **Network Security:**
   - Use HTTPS only (disable HTTP in production)
   - Restrict access to management console (port 9990)
   - Implement IP whitelisting for HCEN Central
   - Use VPN for administrative access

4. **Application Security:**
   - Keep WildFly and libraries updated
   - Regular security audits
   - Monitor access logs for suspicious activity
   - Implement rate limiting for API endpoints

---

## Contact Information

**TSE 2025 Group 9**
- German Rodao (4.796.608-7)
- Agustin Silvano (5.096.964-8)
- Piero Santos (6.614.312-9)

---

**Last Updated**: 2025-11-18
