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
4. [PostgreSQL Database](#postgresql-database)
5. [Redis Cache](#redis-cache)
6. [MongoDB](#mongodb)
7. [SSL/TLS Certificates](#ssltls-certificates)
8. [Network Configuration](#network-configuration)
9. [Environment Variables](#environment-variables)
10. [Deployment Checklist](#deployment-checklist)

---

## Overview

The HCEN authentication system provides centralized OAuth 2.0 / OpenID Connect authentication for three client types:
- **Mobile App** (React Native) - Public client with PKCE
- **Patient Web Portal** - Confidential client
- **Admin Web Portal** - Confidential client

All clients authenticate through gub.uy (ID Uruguay) and receive HCEN-issued JWT tokens for API access.

---

## External Dependencies

### 1. gub.uy (ID Uruguay) - OpenID Connect Provider

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

#### 1. PostgreSQL Datasource

```xml
<datasource jndi-name="java:jboss/datasources/HcenDS" pool-name="HcenDS" enabled="true">
    <connection-url>jdbc:postgresql://localhost:5432/hcen</connection-url>
    <driver>postgresql</driver>
    <security>
        <user-name>hcen_user</user-name>
        <password>CHANGE_ME_SECURE_PASSWORD</password>
    </security>
    <validation>
        <validate-on-match>true</validate-on-match>
        <background-validation>false</background-validation>
        <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker"/>
        <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter"/>
    </validation>
    <pool>
        <min-pool-size>5</min-pool-size>
        <max-pool-size>20</max-pool-size>
    </pool>
</datasource>

<drivers>
    <driver name="postgresql" module="org.postgresql">
        <xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>
    </driver>
</drivers>
```

#### 2. HTTPS/SSL Configuration

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

#### 3. CORS Configuration

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

## PostgreSQL Database

### Version

- **PostgreSQL**: 14+ recommended
- **Extensions**: `uuid-ossp`, `pgcrypto`

### Database Setup

```sql
-- Create database
CREATE DATABASE hcen
    WITH OWNER = hcen_user
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

-- Connect to database
\c hcen

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create user (if not exists)
CREATE USER hcen_user WITH PASSWORD 'CHANGE_ME_SECURE_PASSWORD';
GRANT ALL PRIVILEGES ON DATABASE hcen TO hcen_user;
```

### Required Tables

#### 1. refresh_tokens

Created by migration script: `V001__create_refresh_tokens_table.sql`

```sql
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token_hash VARCHAR(128) UNIQUE NOT NULL,
    user_ci VARCHAR(20) NOT NULL,
    client_type VARCHAR(20) NOT NULL,
    device_id VARCHAR(128),
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**Indexes**:
- `idx_refresh_tokens_user_ci` on `user_ci`
- `idx_refresh_tokens_token_hash` on `token_hash`
- `idx_refresh_tokens_expires_at` on `expires_at`
- `idx_refresh_tokens_is_revoked` on `is_revoked`

### Connection Pool

- **Minimum Connections**: 5
- **Maximum Connections**: 20
- **Idle Timeout**: 10 minutes
- **Max Lifetime**: 30 minutes

### Performance Tuning

```sql
-- PostgreSQL configuration recommendations
-- File: postgresql.conf

max_connections = 100
shared_buffers = 256MB
effective_cache_size = 1GB
maintenance_work_mem = 64MB
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100
random_page_cost = 1.1
effective_io_concurrency = 200
work_mem = 4MB
min_wal_size = 1GB
max_wal_size = 4GB
```

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

## MongoDB

### Version

- **MongoDB**: 6.0+ recommended
- **Deployment**: Replica Set (for production)

### Purpose

- Authentication event logs
- Audit logs for compliance (AC026)
- System event tracking

### Configuration

```javascript
// MongoDB setup
use hcen_audit;

// Create collections
db.createCollection("authentication_events");
db.createCollection("audit_logs");

// Indexes for authentication_events
db.authentication_events.createIndex({ "timestamp": -1 });
db.authentication_events.createIndex({ "user_ci": 1 });
db.authentication_events.createIndex({ "event_type": 1 });
db.authentication_events.createIndex({ "client_type": 1 });

// Indexes for audit_logs
db.audit_logs.createIndex({ "timestamp": -1 });
db.audit_logs.createIndex({ "actor_id": 1 });
db.audit_logs.createIndex({ "resource_type": 1, "resource_id": 1 });

// TTL index for automatic cleanup (optional, 7 years retention)
db.authentication_events.createIndex(
    { "timestamp": 1 },
    { expireAfterSeconds: 220898400 }  // 7 years
);
```

### Connection String

```
mongodb://hcen_user:PASSWORD@localhost:27017/hcen_audit?authSource=admin
```

For replica set:
```
mongodb://hcen_user:PASSWORD@host1:27017,host2:27017,host3:27017/hcen_audit?replicaSet=hcenrs&authSource=admin
```

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
| 5432 | TCP | PostgreSQL | Internal |
| 6379 | TCP | Redis | Internal |
| 27017 | TCP | MongoDB | Internal |

### Firewall Rules

```bash
# WildFly HTTPS (public access)
sudo ufw allow 8443/tcp comment "HCEN HTTPS"

# PostgreSQL (internal only)
sudo ufw allow from 10.0.0.0/8 to any port 5432 comment "PostgreSQL"

# Redis (internal only)
sudo ufw allow from 10.0.0.0/8 to any port 6379 comment "Redis"

# MongoDB (internal only)
sudo ufw allow from 10.0.0.0/8 to any port 27017 comment "MongoDB"

# Block HTTP in production
sudo ufw deny 8080/tcp comment "Block HTTP"
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

```bash
# Application
export HCEN_ENV=production
export HCEN_LOG_LEVEL=INFO

# Database
export HCEN_DB_HOST=localhost
export HCEN_DB_PORT=5432
export HCEN_DB_NAME=hcen
export HCEN_DB_USER=hcen_user
export HCEN_DB_PASSWORD=CHANGE_ME

# Redis
export HCEN_REDIS_HOST=localhost
export HCEN_REDIS_PORT=6379
export HCEN_REDIS_PASSWORD=CHANGE_ME

# MongoDB
export HCEN_MONGO_URI="mongodb://localhost:27017"

# gub.uy OAuth
export GUBUY_CLIENT_SECRET_WEB_PATIENT=CHANGE_ME
export GUBUY_CLIENT_SECRET_WEB_ADMIN=CHANGE_ME

# JWT
export JWT_SIGNING_KEY_PATH=/opt/hcen/keys/jwt-private-pkcs8.pem
```

---

## Deployment Checklist

### Pre-Deployment

- [ ] Register OAuth clients with AGESIC (gub.uy)
- [ ] Obtain SSL certificates for hcen.uy and subdomains
- [ ] Generate JWT signing keys
- [ ] Set up PostgreSQL database and run migrations
- [ ] Set up Redis with password authentication
- [ ] Set up MongoDB replica set
- [ ] Configure WildFly datasources
- [ ] Configure WildFly HTTPS listener
- [ ] Configure CORS settings
- [ ] Update application.properties with production values
- [ ] Set environment variables
- [ ] Configure firewalls and network security

### Deployment

- [ ] Build WAR file: `./gradlew clean build`
- [ ] Run tests: `./gradlew test` (ensure 80% coverage)
- [ ] Deploy WAR to WildFly: `cp hcen.war $WILDFLY_HOME/standalone/deployments/`
- [ ] Verify deployment in WildFly logs
- [ ] Test health check: `curl https://hcen.uy/api/health`

### Post-Deployment

- [ ] Test OAuth flow with gub.uy testing environment
- [ ] Verify JWT token generation and validation
- [ ] Test mobile app authentication
- [ ] Test patient portal authentication
- [ ] Test admin portal authentication
- [ ] Verify audit logging to MongoDB
- [ ] Monitor WildFly logs for errors
- [ ] Set up monitoring (Prometheus/Grafana)
- [ ] Configure alerting for authentication failures
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

**Issue**: "Database connection pool exhausted"
- **Solution**: Increase pool size or optimize slow queries

### Logs

- **WildFly**: `$WILDFLY_HOME/standalone/log/server.log`
- **PostgreSQL**: `/var/log/postgresql/postgresql-14-main.log`
- **Redis**: `/var/log/redis/redis-server.log`
- **MongoDB**: `/var/log/mongodb/mongod.log`

---

**Document Version**: 1.0
**Last Updated**: 2025-10-14
**Maintained By**: TSE 2025 Group 9
