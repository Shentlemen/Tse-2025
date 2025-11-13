# HCEN Authentication System - Infrastructure Requirements

This document outlines the infrastructure requirements for the HCEN OpenID Connect authentication system integrated with gub.uy (Uruguay's national identity provider).

**Last Updated**: 2025-10-14
**Component**: HCEN Central Component (hcen/)
**Author**: TSE 2025 Group 9

---

## Table of Contents

1. [Overview](#overview)
2. [External Dependencies](#external-dependencies)
3. [WildFly Configuration](#wildfly-configuration)
4. [MongoDB Database](#mongodb-database)
5. [Redis Cache](#redis-cache)
6. [SSL/TLS Certificates](#ssltls-certificates)
7. [Network Configuration](#network-configuration)
8. [Environment Variables](#environment-variables)
9. [Deployment Checklist](#deployment-checklist)

---

## Overview

The HCEN authentication system provides centralized OAuth 2.0 / OpenID Connect authentication for three client types:
- **Mobile App** (React Native) - Public client with PKCE
- **Patient Web Portal** - Confidential client
- **Admin Web Portal** - Confidential client

All clients authenticate through gub.uy (ID Uruguay) and receive HCEN-issued JWT tokens for API access.

---

## External Dependencies

### 1. PDI (Plataforma de Datos e Integración) - Identity Validation Service

**Purpose**: Government identity validation service for age verification during INUS registration

**Service**: DNIC Servicio Básico de Información (Basic Information Service)

**Protocol**: SOAP 1.1/1.2 over HTTPS

**Registration Required**: Yes
- Contact AGESIC to register HCEN as a PDI client
- Provide: Application description, technical contact, purpose of integration
- Receive: WS-Security username and password credentials

**Testing Environment**:
- Endpoint: `https://pdi-testing.gub.uy/dnic/servicio-basico`
- WSDL: `https://pdi-testing.gub.uy/dnic/servicio-basico?wsdl`
- Authentication: WS-Security UsernameToken
- Timeout: Connection 5s, Read 30s

**Production Environment**:
- Replace `pdi-testing` with `pdi` in endpoint URL
- Use production credentials
- Verify SSL certificate

**Operations**:

1. **ConsultarUsuario** (Query User)
   - Input: CI (Cédula de Identidad)
   - Output: Full name, date of birth
   - Use Case: Age verification (18+ requirement) during INUS registration

**Error Handling**:
- **Graceful Degradation**: If PDI is unavailable, HCEN falls back to local age calculation
- **Circuit Breaker**: Automatic fail-fast after 5 consecutive failures (60-second reset timeout)
- **Retry Logic**: 3 attempts with exponential backoff (1s, 2s, 4s) for network failures

**Security**:
- HTTPS/TLS required (AC002-AC004)
- WS-Security username/password authentication
- No sensitive data logged (CI numbers masked in logs)

**Integration Point**:
- `InusService.registerUser()` - Called during user registration to verify age

**Configuration Properties**:
```properties
pdi.soap.endpoint=https://pdi-testing.gub.uy/dnic/servicio-basico
pdi.soap.username=hcen-client
pdi.soap.password=CHANGE_ME_SECURE_PASSWORD
pdi.soap.timeout.connect=5000
pdi.soap.timeout.read=30000
```

---

### 2. gub.uy (ID Uruguay) - OpenID Connect Provider

**Purpose**: National authentication system for Uruguayan citizens

**Registration Required**: Yes
- Contact AGESIC to register HCEN as an OIDC client
- Separate registrations for testing and production environments
- Provide: redirect URIs, application description, contact information
- Receive: client IDs and client secrets

**Testing Environment**:
- Issuer: `https://auth-testing.iduruguay.gub.uy`
- Authorization Endpoint: `https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize`
- Token Endpoint: `https://auth-testing.iduruguay.gub.uy/oidc/v1/token`
- UserInfo Endpoint: `https://auth-testing.iduruguay.gub.uy/oidc/v1/userinfo`
- JWKS URI: `https://auth-testing.iduruguay.gub.uy/oidc/v1/jwks`

**Production Environment**:
- Replace `auth-testing` with `auth` in all URLs
- Use production client credentials
- Verify SSL certificate pinning

**Registered Clients**:

1. **Mobile App (Public Client)**
   - Client ID: `hcen-mobile-app`
   - Client Type: Public (no client secret)
   - Redirect URIs: `hcenuy://auth/callback`, `uy.gub.hcen://auth/callback`
   - Grant Types: `authorization_code`
   - PKCE: Required

2. **Patient Web Portal (Confidential Client)**
   - Client ID: `hcen-web-patient`
   - Client Secret: (provided by AGESIC)
   - Redirect URIs: `https://hcen.uy/api/auth/callback`
   - Grant Types: `authorization_code`

3. **Admin Web Portal (Confidential Client)**
   - Client ID: `hcen-web-admin`
   - Client Secret: (provided by AGESIC)
   - Redirect URIs: `https://admin.hcen.uy/api/auth/callback`
   - Grant Types: `authorization_code`

---

## WildFly Configuration

### Version

- **WildFly**: 27+ (Jakarta EE 10)
- **Java**: 17+

### Standalone Configuration

File: `$WILDFLY_HOME/standalone/configuration/standalone-full.xml`

#### 1. HTTPS/SSL Configuration

```xml
<security-realm name="ApplicationRealm">
    <server-identities>
        <ssl>
            <keystore path="hcen-keystore.jks"
                      relative-to="jboss.server.config.dir"
                      keystore-password="KEYSTORE_PASSWORD"
                      alias="hcen"
                      key-password="KEY_PASSWORD"/>
        </ssl>
    </server-identities>
</security-realm>

<server name="default-server">
    <https-listener name="https" socket-binding="https"
                    security-realm="ApplicationRealm"
                    verify-client="REQUESTED"/>
</server>
```

#### 2. CORS Configuration

For mobile app and web clients to access the API:

```xml
<subsystem xmlns="urn:jboss:domain:undertow:12.0">
    <server name="default-server">
        <host name="default-host" alias="localhost">
            <filter-ref name="cors-filter"/>
        </host>
    </server>
    <filters>
        <response-header name="cors-filter" header-name="Access-Control-Allow-Origin" header-value="https://hcen.uy,https://admin.hcen.uy"/>
        <response-header name="cors-methods" header-name="Access-Control-Allow-Methods" header-value="GET, POST, PUT, DELETE, OPTIONS"/>
        <response-header name="cors-headers" header-name="Access-Control-Allow-Headers" header-value="Content-Type, Authorization"/>
        <response-header name="cors-credentials" header-name="Access-Control-Allow-Credentials" header-value="true"/>
    </filters>
</subsystem>
```

---

## MongoDB Database

### Version

- **MongoDB**: 6.0+ recommended
- **Deployment**: Standalone (development), Replica Set (production)

### Windows Installation

**Option 1: Docker (Recommended for Development)**

```powershell
# Pull MongoDB image
docker pull mongo:6.0

# Run MongoDB container
docker run -d `
  --name mongodb `
  -p 27017:27017 `
  -e MONGO_INITDB_ROOT_USERNAME=hcen_admin `
  -e MONGO_INITDB_ROOT_PASSWORD=CHANGE_ME_SECURE_PASSWORD `
  -v mongodb_data:/data/db `
  mongo:6.0

# Verify MongoDB is running
docker ps | findstr mongodb
```

**Option 2: Native Installation**

1. Download MongoDB Community Edition for Windows:
   - Visit: https://www.mongodb.com/try/download/community
   - Select Windows x64, MSI installer
   - Run installer and choose "Complete" installation
   - Select "Install MongoDB as a Service"

2. Verify installation:
```powershell
# Check MongoDB service status
Get-Service MongoDB

# Connect to MongoDB
mongosh
```

### Database Setup

**Create HCEN Database and Collections:**

```javascript
// Connect to MongoDB
mongosh "mongodb://localhost:27017" -u hcen_admin -p CHANGE_ME_SECURE_PASSWORD

// Create HCEN database
use hcen

// Create user for HCEN application
db.createUser({
  user: "hcen_user",
  pwd: "CHANGE_ME_APP_PASSWORD",
  roles: [
    { role: "readWrite", db: "hcen" }
  ]
})

// Create collections
db.createCollection("refresh_tokens")
db.createCollection("authentication_sessions")
db.createCollection("inus_users")
db.createCollection("rndc_documents")
db.createCollection("access_policies")
db.createCollection("audit_logs")
```

### Required Collections and Indexes

#### 1. refresh_tokens

**Document Schema**:
```json
{
  "_id": ObjectId("..."),
  "tokenHash": "sha256:...",
  "userCi": "12345678",
  "clientType": "MOBILE",
  "deviceId": "device-123",
  "issuedAt": ISODate("2025-10-15T10:00:00Z"),
  "expiresAt": ISODate("2025-11-14T10:00:00Z"),
  "revokedAt": null,
  "isRevoked": false
}
```

**Indexes**:
```javascript
db.refresh_tokens.createIndex({ "tokenHash": 1 }, { unique: true })
db.refresh_tokens.createIndex({ "userCi": 1 })
db.refresh_tokens.createIndex({ "expiresAt": 1 })
db.refresh_tokens.createIndex({ "isRevoked": 1 })

// TTL index for automatic expiration (cleanup expired tokens)
db.refresh_tokens.createIndex(
  { "expiresAt": 1 },
  { expireAfterSeconds: 0 }
)
```

#### 2. authentication_sessions

**Document Schema**:
```json
{
  "_id": ObjectId("..."),
  "sessionId": "uuid-session-id",
  "userCi": "12345678",
  "clientType": "WEB_PATIENT",
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "createdAt": ISODate("2025-10-15T10:00:00Z"),
  "lastAccessedAt": ISODate("2025-10-15T10:30:00Z"),
  "expiresAt": ISODate("2025-10-15T18:00:00Z")
}
```

**Indexes**:
```javascript
db.authentication_sessions.createIndex({ "sessionId": 1 }, { unique: true })
db.authentication_sessions.createIndex({ "userCi": 1 })
db.authentication_sessions.createIndex({ "expiresAt": 1 })

// TTL index for automatic session cleanup
db.authentication_sessions.createIndex(
  { "expiresAt": 1 },
  { expireAfterSeconds: 0 }
)
```

#### 3. audit_logs

**Document Schema**:
```json
{
  "_id": ObjectId("..."),
  "eventType": "AUTHENTICATION_SUCCESS",
  "actorId": "12345678",
  "resourceType": "SESSION",
  "resourceId": "session-uuid",
  "actionOutcome": "SUCCESS",
  "ipAddress": "192.168.1.100",
  "userAgent": "HCEN Mobile/1.0.0",
  "timestamp": ISODate("2025-10-15T10:00:00Z"),
  "details": {
    "clientType": "MOBILE",
    "acr": "urn:iduruguay:nid:2"
  }
}
```

**Indexes**:
```javascript
db.audit_logs.createIndex({ "timestamp": -1 })
db.audit_logs.createIndex({ "actorId": 1 })
db.audit_logs.createIndex({ "eventType": 1 })
db.audit_logs.createIndex({ "resourceType": 1, "resourceId": 1 })

// TTL index for audit log retention (7 years for Uruguayan law compliance)
db.audit_logs.createIndex(
  { "timestamp": 1 },
  { expireAfterSeconds: 220898400 }  // 7 years in seconds
)
```

### Connection Configuration

**Connection String (Development)**:
```
mongodb://hcen_user:CHANGE_ME_APP_PASSWORD@localhost:27017/hcen?authSource=hcen
```

**Connection String (Production with Replica Set)**:
```
mongodb://hcen_user:CHANGE_ME_APP_PASSWORD@host1:27017,host2:27017,host3:27017/hcen?replicaSet=hcenrs&authSource=hcen
```

**Java Connection Settings**:
```java
// In MongoConfiguration.java
MongoClientSettings settings = MongoClientSettings.builder()
    .applyConnectionString(new ConnectionString(mongoUri))
    .applyToConnectionPoolSettings(builder ->
        builder.maxSize(20)
               .minSize(5)
               .maxWaitTime(10, TimeUnit.SECONDS)
               .maxConnectionIdleTime(30, TimeUnit.MINUTES))
    .build();

MongoClient mongoClient = MongoClients.create(settings);
```

### Performance Tuning

**MongoDB Configuration (mongod.cfg for Windows)**:

```yaml
# Network settings
net:
  port: 27017
  bindIp: 127.0.0.1

# Storage settings
storage:
  dbPath: C:\data\db
  journal:
    enabled: true
  wiredTiger:
    engineConfig:
      cacheSizeGB: 2

# Security
security:
  authorization: enabled

# Operation profiling (development only)
operationProfiling:
  mode: slowOp
  slowOpThresholdMs: 100
```

**Best Practices**:
- Use connection pooling (5-20 connections)
- Enable journaling for data durability
- Configure appropriate cache size (50-80% of available RAM)
- Use indexes for all query fields
- Monitor slow queries with profiling
- Regular backups with mongodump

---

## Redis Cache

### Version

- **Redis**: 6.0+ recommended
- **Deployment**: Standalone or Sentinel (for HA)

### Purpose

- OAuth state management (short-lived, 5 minute TTL)
- Token blacklist (for revoked tokens)
- Rate limiting (future enhancement)
- Session caching (optional)

### Configuration

```conf
# redis.conf

# Bind to localhost (or specific IP for network access)
bind 127.0.0.1

# Port
port 6379

# Password (strongly recommended for production)
requirepass CHANGE_ME_REDIS_PASSWORD

# Database count
databases 16

# Memory policy
maxmemory 256mb
maxmemory-policy allkeys-lru

# Persistence (optional for state management, but recommended)
save 900 1
save 300 10
save 60 10000

# Log level
loglevel notice
logfile /var/log/redis/redis-server.log

# Snapshotting
dir /var/lib/redis
dbfilename dump.rdb
```

### Key Patterns

| Pattern | Purpose | TTL | Example |
|---------|---------|-----|---------|
| `oauth:state:{state}` | OAuth state storage | 300s (5 min) | `oauth:state:abc123xyz` |
| `token:blacklist:{jti}` | Revoked token list | Token lifetime | `token:blacklist:uuid-1234` |
| `ratelimit:{ip}:{endpoint}` | Rate limiting | 60s | `ratelimit:192.168.1.1:/api/auth/login` |

### Connection Pool

- **Max Total**: 20
- **Max Idle**: 10
- **Min Idle**: 5
- **Timeout**: 2000ms

---


## SSL/TLS Certificates

### Requirements

All communications MUST use HTTPS (AC002-AC004).

### Certificate Types

1. **Server Certificate** (for WildFly HTTPS)
   - Domain: `hcen.uy`, `*.hcen.uy`
   - Issued by: Trusted CA (e.g., Let's Encrypt, DigiCert)
   - Key Size: 2048-bit RSA or 256-bit ECC
   - Validity: 1 year (auto-renew)

2. **JWT Signing Certificate** (for RS256)
   - Type: RSA 2048-bit or 4096-bit
   - Format: PKCS#8 PEM
   - Storage: Secure filesystem (`/opt/hcen/keys/jwt-private.pem`)
   - Permissions: 600 (read-only by WildFly user)

### Generating JWT Signing Keys

```bash
# Generate RSA private key (4096-bit)
openssl genrsa -out jwt-private.pem 4096

# Extract public key (for verification)
openssl rsa -in jwt-private.pem -pubout -out jwt-public.pem

# Convert to PKCS#8 format (if needed)
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt \
    -in jwt-private.pem -out jwt-private-pkcs8.pem

# Set permissions
chmod 600 jwt-private-pkcs8.pem
chown wildfly:wildfly jwt-private-pkcs8.pem
```

### Certificate Locations

```
/opt/hcen/
├── keys/
│   ├── jwt-private.pem       (JWT signing key)
│   └── jwt-public.pem        (JWT verification key)
└── certs/
    ├── hcen-keystore.jks     (SSL server certificate for WildFly)
    └── truststore.jks        (CA certificates for external services)
```

---

## Network Configuration

### Required Ports

| Port | Protocol | Service | Access |
|------|----------|---------|--------|
| 8080 | HTTP | WildFly (development only) | Internal |
| 8443 | HTTPS | WildFly (production) | Public |
| 9990 | HTTPS | WildFly Management | Internal |
| 6379 | TCP | Redis | Internal |
| 27017 | TCP | MongoDB | Internal |

### Firewall Rules

```bash
# WildFly HTTPS (public access)
netsh advfirewall firewall add rule name="HCEN HTTPS" dir=in action=allow protocol=TCP localport=8443

# Redis (internal only - if not using Docker)
netsh advfirewall firewall add rule name="Redis" dir=in action=allow protocol=TCP localport=6379 remoteip=LocalSubnet

# MongoDB (internal only - if not using Docker)
netsh advfirewall firewall add rule name="MongoDB" dir=in action=allow protocol=TCP localport=27017 remoteip=LocalSubnet

# Block HTTP in production
netsh advfirewall firewall add rule name="Block HTTP" dir=in action=block protocol=TCP localport=8080
```

### DNS Configuration

| Domain | Type | Value |
|--------|------|-------|
| hcen.uy | A | 203.0.113.10 |
| admin.hcen.uy | A | 203.0.113.10 |
| api.hcen.uy | CNAME | hcen.uy |

---

## Environment Variables

Set these in WildFly startup script or systemd service file:

**Windows (PowerShell):**

```powershell
# Application
$env:HCEN_ENV="production"
$env:HCEN_LOG_LEVEL="INFO"

# MongoDB
$env:HCEN_MONGO_URI="mongodb://hcen_user:CHANGE_ME_APP_PASSWORD@localhost:27017/hcen?authSource=hcen"
$env:HCEN_MONGO_DATABASE="hcen"

# Redis
$env:HCEN_REDIS_HOST="localhost"
$env:HCEN_REDIS_PORT="6379"
$env:HCEN_REDIS_PASSWORD="CHANGE_ME"

# gub.uy OAuth
$env:GUBUY_CLIENT_SECRET_WEB_PATIENT="CHANGE_ME"
$env:GUBUY_CLIENT_SECRET_WEB_ADMIN="CHANGE_ME"

# JWT
$env:JWT_SIGNING_KEY_PATH="C:\opt\hcen\keys\jwt-private-pkcs8.pem"
```

**Windows (System Environment Variables - Permanent):**

1. Open System Properties > Environment Variables
2. Add new System variables:
   - `HCEN_MONGO_URI` = `mongodb://hcen_user:PASSWORD@localhost:27017/hcen?authSource=hcen`
   - `HCEN_REDIS_HOST` = `localhost`
   - `HCEN_REDIS_PASSWORD` = `your_redis_password`
   - `GUBUY_CLIENT_SECRET_WEB_PATIENT` = `your_client_secret`
   - `GUBUY_CLIENT_SECRET_WEB_ADMIN` = `your_client_secret`
   - `JWT_SIGNING_KEY_PATH` = `C:\opt\hcen\keys\jwt-private-pkcs8.pem`

---

## Deployment Checklist

### Pre-Deployment

- [ ] Register OAuth clients with AGESIC (gub.uy)
- [ ] Register PDI client with AGESIC (for identity validation)
- [ ] Obtain SSL certificates for hcen.uy and subdomains
- [ ] Generate JWT signing keys
- [ ] Set up MongoDB database and create collections
- [ ] Create MongoDB indexes (including TTL indexes)
- [ ] Set up Redis with password authentication
- [ ] Configure WildFly HTTPS listener
- [ ] Configure CORS settings
- [ ] Update application.properties with production values (including PDI credentials)
- [ ] Set environment variables (Windows system variables)
- [ ] Configure firewalls and network security
- [ ] Test PDI connectivity and SOAP service availability

### Deployment

- [ ] Build WAR file: `./gradlew clean build`
- [ ] Run tests: `./gradlew test` (ensure 80% coverage)
- [ ] Deploy WAR to WildFly: `cp hcen.war $WILDFLY_HOME/standalone/deployments/`
- [ ] Verify deployment in WildFly logs
- [ ] Test health check: `curl https://hcen.uy/api/health`

### Post-Deployment

- [ ] Test OAuth flow with gub.uy testing environment
- [ ] Test PDI identity validation with test CI numbers
- [ ] Verify PDI circuit breaker and graceful degradation
- [ ] Verify JWT token generation and validation
- [ ] Test mobile app authentication
- [ ] Test patient portal authentication
- [ ] Test admin portal authentication
- [ ] Test INUS user registration with PDI age verification
- [ ] Verify audit logging to MongoDB
- [ ] Monitor WildFly logs for errors
- [ ] Monitor PDI integration errors and circuit breaker state
- [ ] Set up monitoring (Prometheus/Grafana)
- [ ] Configure alerting for authentication failures
- [ ] Configure alerting for PDI service unavailability
- [ ] Document rollback procedure

### Production Cutover

- [ ] Update to production gub.uy endpoints
- [ ] Use production OAuth client credentials
- [ ] Enable SSL certificate pinning in mobile app
- [ ] Configure rate limiting
- [ ] Set up backup and disaster recovery
- [ ] Perform load testing
- [ ] Create runbook for operations team

---

## Security Considerations

1. **Never commit secrets to version control**
   - Use environment variables or secrets management system
   - Keep `application.properties` in `.gitignore`

2. **Rotate credentials regularly**
   - JWT signing keys: Every 6 months
   - Database passwords: Every 3 months
   - OAuth client secrets: On compromise

3. **Monitor authentication failures**
   - Set up alerts for repeated failures
   - Implement account lockout after N failures
   - Log all authentication attempts

4. **Keep dependencies updated**
   - Regularly update WildFly, PostgreSQL, Redis, MongoDB
   - Monitor CVE databases for vulnerabilities
   - Test updates in staging before production

5. **Backup critical data**
   - Daily PostgreSQL backups (retention: 30 days)
   - MongoDB audit logs (retention: 7 years per Uruguayan law)
   - JWT signing keys (encrypted off-site storage)

---

## Support and Troubleshooting

### Common Issues

**Issue**: "Failed to connect to Redis"
- **Solution**: Verify Redis is running, check password, verify network connectivity

**Issue**: "OAuth state invalid or expired"
- **Solution**: State has 5-minute TTL. User may have waited too long. Restart flow.

**Issue**: "JWT token expired"
- **Solution**: Token has 1-hour TTL. Refresh using refresh token endpoint.

**Issue**: "MongoDB connection pool exhausted"
- **Solution**: Increase pool size in MongoConfiguration or optimize slow queries

**Issue**: "MongoDB TTL index not cleaning up expired documents"
- **Solution**: Verify TTL index exists (`db.collection.getIndexes()`), MongoDB runs cleanup every 60 seconds

### Logs

**Windows Locations:**

- **WildFly**: `%WILDFLY_HOME%\standalone\log\server.log`
- **MongoDB** (if installed as service): `C:\Program Files\MongoDB\Server\6.0\log\mongod.log`
- **Redis** (if running native): Check console output or configured log file
- **Docker containers**:
  ```powershell
  # View MongoDB logs
  docker logs mongodb

  # View Redis logs
  docker logs redis

  # Follow logs in real-time
  docker logs -f mongodb
  ```

---

---

## ActiveMQ Artemis (JMS Broker)

### Overview

HCEN Central uses **ActiveMQ Artemis** (embedded in WildFly) for asynchronous message processing. Peripheral nodes (clinics, health providers) send user registration and document registration messages to JMS queues, which are processed by Message-Driven Beans (MDBs).

### Version

- **ActiveMQ Artemis**: 2.31+ (embedded in WildFly 31+)
- **Configuration Profile**: `standalone-full.xml` (required for JMS support)

### JMS Queue Configuration

Add the following queue definitions to `standalone-full.xml` under `<subsystem xmlns="urn:jboss:domain:messaging-activemq:...">`:

```xml
<subsystem xmlns="urn:jboss:domain:messaging-activemq:17.0">
    <server name="default">
        <!-- ... existing configuration ... -->

        <!-- JMS Queues for HCEN Message Processing -->
        <jms-queue name="UserRegistrationQueue"
                   entries="java:/jms/queue/UserRegistration">
            <durable>true</durable>
        </jms-queue>

        <jms-queue name="DocumentRegistrationQueue"
                   entries="java:/jms/queue/DocumentRegistration">
            <durable>true</durable>
        </jms-queue>

        <!-- Dead Letter Queue (DLQ) for failed messages -->
        <jms-queue name="DLQ"
                   entries="java:/jms/queue/DLQ">
            <durable>true</durable>
        </jms-queue>

        <!-- Expiry Queue for expired messages -->
        <jms-queue name="ExpiryQueue"
                   entries="java:/jms/queue/ExpiryQueue">
            <durable>true</durable>
        </jms-queue>
    </server>
</subsystem>
```

### Address Settings for Message Redelivery

Configure message redelivery and DLQ behavior:

```xml
<subsystem xmlns="urn:jboss:domain:messaging-activemq:17.0">
    <server name="default">
        <!-- ... existing configuration ... -->

        <address-setting name="jms.queue.UserRegistration"
                         dead-letter-address="jms.queue.DLQ"
                         expiry-address="jms.queue.ExpiryQueue"
                         redelivery-delay="5000"
                         max-delivery-attempts="5"
                         max-size-bytes="10485760"
                         message-counter-history-day-limit="10"/>

        <address-setting name="jms.queue.DocumentRegistration"
                         dead-letter-address="jms.queue.DLQ"
                         expiry-address="jms.queue.ExpiryQueue"
                         redelivery-delay="5000"
                         max-delivery-attempts="5"
                         max-size-bytes="10485760"
                         message-counter-history-day-limit="10"/>

        <!-- Default DLQ setting -->
        <address-setting name="jms.queue.DLQ"
                         max-size-bytes="52428800"
                         address-full-policy="PAGE"/>
    </server>
</subsystem>
```

**Redelivery Configuration Explained**:
- `redelivery-delay="5000"`: Wait 5 seconds before redelivering failed message
- `max-delivery-attempts="5"`: Retry up to 5 times before moving to DLQ
- `max-size-bytes="10485760"`: 10 MB max queue size
- `message-counter-history-day-limit="10"`: Keep 10 days of message statistics

### CLI Commands to Create Queues

Alternatively, use WildFly CLI to create queues at runtime:

```bash
# Connect to WildFly CLI
./bin/jboss-cli.sh --connect

# Create User Registration Queue
/subsystem=messaging-activemq/server=default/jms-queue=UserRegistrationQueue:add(entries=["java:/jms/queue/UserRegistration"], durable=true)

# Create Document Registration Queue
/subsystem=messaging-activemq/server=default/jms-queue=DocumentRegistrationQueue:add(entries=["java:/jms/queue/DocumentRegistration"], durable=true)

# Configure address settings for UserRegistration queue
/subsystem=messaging-activemq/server=default/address-setting=jms.queue.UserRegistration:add(dead-letter-address="jms.queue.DLQ", expiry-address="jms.queue.ExpiryQueue", redelivery-delay=5000, max-delivery-attempts=5, max-size-bytes=10485760)

# Configure address settings for DocumentRegistration queue
/subsystem=messaging-activemq/server=default/address-setting=jms.queue.DocumentRegistration:add(dead-letter-address="jms.queue.DLQ", expiry-address="jms.queue.ExpiryQueue", redelivery-delay=5000, max-delivery-attempts=5, max-size-bytes=10485760)

# Reload server
reload
```

### Queue Details

| Queue Name                | JNDI Name                            | Purpose                                       | Max Delivery Attempts | Redelivery Delay |
|---------------------------|--------------------------------------|-----------------------------------------------|----------------------|------------------|
| UserRegistrationQueue     | java:/jms/queue/UserRegistration     | User creation events from peripheral nodes    | 5                    | 5000 ms          |
| DocumentRegistrationQueue | java:/jms/queue/DocumentRegistration | Document creation events from peripheral nodes| 5                    | 5000 ms          |
| DLQ (Dead Letter Queue)   | java:/jms/queue/DLQ                  | Failed messages after max redelivery attempts | N/A                  | N/A              |
| ExpiryQueue               | java:/jms/queue/ExpiryQueue          | Expired messages (TTL exceeded)               | N/A                  | N/A              |

### Message Flow

#### User Registration Flow

1. Peripheral node creates user locally
2. Peripheral node sends JSON message to `UserRegistrationQueue`
3. `UserRegistrationListener` (MDB) receives message
4. MDB validates message structure
5. `UserRegistrationProcessor` processes business logic
6. `InusService` registers user in INUS database
7. Transaction committed, message acknowledged
8. On error: Transaction rolled back, message redelivered (up to 5 times) or moved to DLQ

#### Document Registration Flow

1. Peripheral node creates document in local storage
2. Peripheral node calculates SHA-256 hash
3. Peripheral node sends JSON message to `DocumentRegistrationQueue`
4. `DocumentRegistrationListener` (MDB) receives message
5. MDB validates message structure
6. `DocumentRegistrationProcessor` processes business logic
7. `RndcService` registers document metadata in RNDC database
8. Transaction committed, message acknowledged
9. On error: Transaction rolled back, message redelivered (up to 5 times) or moved to DLQ

### Message Format

#### User Registration Message (JSON)

```json
{
  "messageId": "msg-550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-11-13T10:30:00Z",
  "sourceSystem": "clinic-001",
  "eventType": "USER_CREATED",
  "payload": {
    "ci": "12345678",
    "firstName": "Juan",
    "lastName": "Pérez",
    "dateOfBirth": "1990-01-15",
    "email": "juan.perez@example.com",
    "phoneNumber": "099123456",
    "clinicId": "clinic-001"
  }
}
```

#### Document Registration Message (JSON)

```json
{
  "messageId": "msg-660e8400-e29b-41d4-a716-446655440001",
  "timestamp": "2025-11-13T10:30:00Z",
  "sourceSystem": "clinic-001",
  "eventType": "DOCUMENT_CREATED",
  "payload": {
    "patientCI": "12345678",
    "documentType": "CLINICAL_NOTE",
    "documentLocator": "https://clinic-001.hcen.uy/api/documents/doc-123",
    "documentHash": "sha256:a1b2c3d4e5f678901234567890123456789012345678901234567890123456",
    "createdBy": "doctor@clinic.com",
    "createdAt": "2025-11-13T10:30:00Z",
    "clinicId": "clinic-001",
    "documentTitle": "Consulta general",
    "documentDescription": "Consulta de control"
  }
}
```

### Error Handling

**Error Types**:

1. **Validation Errors** (Permanent)
   - Invalid message format
   - Missing required fields
   - Invalid data formats (malformed CI, bad URL, invalid hash)
   - Result: Message moved to DLQ after first attempt

2. **Business Rule Violations** (Permanent)
   - Age verification failure
   - Duplicate registration (handled idempotently)
   - Result: Message moved to DLQ

3. **Transient Errors** (Retry)
   - Database connection failure
   - Network timeout
   - Temporary service unavailability
   - Result: Message redelivered up to 5 times with 5-second delay

### Monitoring

Monitor queue metrics via WildFly Management Console:

**Queue Metrics**:
- Queue depth (messages waiting)
- Message rate (messages/second)
- DLQ size (failed messages)
- Consumer count (active MDBs)
- Message age (oldest message)

**CLI Commands for Monitoring**:

```bash
# View queue statistics
/subsystem=messaging-activemq/server=default/jms-queue=UserRegistrationQueue:read-resource(include-runtime=true)

# View DLQ messages
/subsystem=messaging-activemq/server=default/jms-queue=DLQ:count-messages()

# Browse DLQ messages
/subsystem=messaging-activemq/server=default/jms-queue=DLQ:list-messages()
```

### Journal Configuration (Production)

For production, configure journal persistence to ensure message durability:

```xml
<subsystem xmlns="urn:jboss:domain:messaging-activemq:17.0">
    <server name="default">
        <journal-type>ASYNCIO</journal-type>
        <journal-buffer-timeout>4096000</journal-buffer-timeout>
        <journal-file-size>10485760</journal-file-size>
        <journal-min-files>2</journal-min-files>
        <journal-pool-files>10</journal-pool-files>
        <journal-compact-percentage>30</journal-compact-percentage>
        <journal-compact-min-files>10</journal-compact-min-files>
    </server>
</subsystem>
```

### Testing JMS Queues

#### Send Test Message to UserRegistrationQueue

Use WildFly CLI to send test messages:

```bash
# Connect to CLI
./bin/jboss-cli.sh --connect

# Send test message
/subsystem=messaging-activemq/server=default/jms-queue=UserRegistrationQueue:send-text-message(body='{"messageId":"test-001","timestamp":"2025-11-13T10:00:00Z","sourceSystem":"test","eventType":"USER_CREATED","payload":{"ci":"12345678","firstName":"Test","lastName":"User","dateOfBirth":"1990-01-01","clinicId":"test-clinic"}}')
```

#### View Queue Metrics

```bash
# Count messages in queue
/subsystem=messaging-activemq/server=default/jms-queue=UserRegistrationQueue:count-messages()

# List messages
/subsystem=messaging-activemq/server=default/jms-queue=UserRegistrationQueue:list-messages()
```

### Troubleshooting

**Problem**: Messages stuck in queue, not being consumed

**Solution**:
1. Check MDB deployment: `./bin/jboss-cli.sh --connect --command="/deployment=hcen.war:read-resource"`
2. Verify MDB is listening: Check `server.log` for "UserRegistrationListener deployed"
3. Check consumer count in queue metrics (should be > 0)
4. Review server logs for MDB exceptions

**Problem**: Messages going to DLQ immediately

**Solution**:
1. Check message format (must be valid JSON)
2. Verify message has all required fields
3. Review DLQ messages to identify validation errors
4. Check `server.log` for InvalidMessageException

**Problem**: Messages redelivered repeatedly, never succeed

**Solution**:
1. Check for database connection issues
2. Verify INUS/RNDC database schemas exist
3. Check for transaction timeout (increase if needed)
4. Review processor logs for specific errors

---

**Document Version**: 2.0
**Last Updated**: 2025-11-13
**Maintained By**: TSE 2025 Group 9
