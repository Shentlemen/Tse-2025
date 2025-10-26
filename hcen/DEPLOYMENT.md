# HCEN - Deployment Guide

Production deployment guide for **HCEN (Historia Clínica Electrónica Nacional)** to ANTEL mi-nube (Virtuozzo Platform).

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Architecture Overview](#architecture-overview)
3. [Environment Configuration](#environment-configuration)
4. [Local Testing](#local-testing)
5. [GitLab CI/CD Pipeline](#gitlab-cicd-pipeline)
6. [ANTEL mi-nube Deployment](#antel-mi-nube-deployment)
7. [Post-Deployment Verification](#post-deployment-verification)
8. [Monitoring & Maintenance](#monitoring--maintenance)
9. [Rollback Procedures](#rollback-procedures)
10. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Services & Credentials

Before deployment, ensure you have obtained:

#### 1. **AGESIC Credentials (gub.uy Authentication)**
- Production OIDC client ID and secret for:
  - Web Patient Portal
  - Web Admin Portal
  - Mobile Application
- Production endpoints (replace testing URLs)
- Contact: AGESIC Integration Team

#### 2. **AGESIC PDI Access**
- PDI SOAP endpoint (production)
- WS-Security credentials
- Contact: AGESIC PDI Team

#### 3. **ANTEL mi-nube Account**
- Virtuozzo environment created (`hcen-prod`)
- API access token or credentials
- Environment dashboard access: https://paas.minube.antel.com.uy

#### 4. **GitLab Container Registry**
- GitLab project with Container Registry enabled
- CI/CD variables configured (see below)

#### 5. **SSL Certificates**
- Valid SSL certificate for `hcen.uy`
- Certificate chain and private key
- Place in `ssl/` directory

#### 6. **JWT Signing Keys**
- RSA key pair for JWT signing (RS256)
- Generate with: `openssl genrsa -out jwt-private.pem 2048`
- Extract public key: `openssl rsa -in jwt-private.pem -pubout -out jwt-public.pem`
- Place in `keys/` directory

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    ANTEL mi-nube (Virtuozzo)                │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                    Nginx (Reverse Proxy)             │  │
│  │             HTTPS/SSL Termination (Port 443)          │  │
│  └──────────────┬───────────────────────────────────────┘  │
│                 │                                            │
│  ┌──────────────▼───────────────────────────────────────┐  │
│  │          HCEN Application (WildFly + WAR)            │  │
│  │         Jakarta EE 10 / Java 21 (Port 8080)          │  │
│  └──┬──────────┬──────────┬──────────┬──────────────────┘  │
│     │          │          │          │                      │
│  ┌──▼───┐  ┌──▼───┐  ┌──▼────┐  ┌──▼───────┐             │
│  │Postgre││MongoDB││ Redis  ││ External │             │
│  │ SQL  ││       ││        ││ Services │             │
│  └──────┘  └──────┘  └───────┘  └──────────┘             │
│                                   │                          │
│                                   ├─ gub.uy (OIDC)          │
│                                   └─ PDI (SOAP)             │
└─────────────────────────────────────────────────────────────┘
```

### Components:

- **HCEN Application**: WildFly 37.0.0 with deployed WAR
- **PostgreSQL 15**: INUS, RNDC, Policies, Clinics
- **MongoDB 7.0**: Audit logs, authentication events
- **Redis 7**: Session store, JWKS cache, distributed cache
- **Nginx**: Reverse proxy, SSL termination, load balancing

---

## Environment Configuration

### 1. Copy Environment Template

```bash
cd hcen
cp .env.example .env
```

### 2. Configure Production Values

Edit `.env` and fill in all required values:

```bash
# Database passwords
DB_PASSWORD=<strong-password-here>
MONGO_ROOT_PASSWORD=<mongo-password>
REDIS_PASSWORD=<redis-password>

# gub.uy Production Credentials (from AGESIC)
GUBUY_CLIENT_WEB_PATIENT_ID=<client-id>
GUBUY_CLIENT_WEB_PATIENT_SECRET=<client-secret>
GUBUY_CLIENT_WEB_ADMIN_ID=<admin-client-id>
GUBUY_CLIENT_WEB_ADMIN_SECRET=<admin-secret>

# PDI Credentials (from AGESIC)
PDI_SOAP_USERNAME=<pdi-username>
PDI_SOAP_PASSWORD=<pdi-password>

# SSL Configuration
SSL_KEYSTORE_PASSWORD=<keystore-password>

# Virtuozzo (set in GitLab CI/CD Variables)
VZ_API_HOST=paas.minube.antel.com.uy
VZ_ENV_NAME=hcen-prod
VZ_NODE_ID=<your-node-id>
VZ_TOKEN=<virtuozzo-session-token>
```

### 3. NEVER Commit Secrets

Add to `.gitignore`:

```bash
echo ".env" >> .gitignore
echo "keys/*.pem" >> .gitignore
echo "ssl/*.key" >> .gitignore
```

---

## Local Testing

Test the production configuration locally before deploying:

### 1. Build WAR

```bash
cd pr1/pr1
./gradlew clean build
```

### 2. Build Docker Image

```bash
cd ../..
docker build -t hcen:local .
```

### 3. Start Infrastructure

```bash
docker-compose -f docker-compose-production.yml up -d postgres mongodb redis
```

Wait for services to be healthy:

```bash
docker-compose -f docker-compose-production.yml ps
```

### 4. Run Application Container

```bash
docker run --rm \
  --network hcen_hcen-network \
  --env-file .env \
  -p 8080:8080 \
  -v $(pwd)/keys:/opt/hcen/keys:ro \
  hcen:local
```

### 5. Test Endpoints

```bash
# Health check
curl http://localhost:8080/hcen/api/health

# Admin login (should redirect to gub.uy)
curl -I http://localhost:8080/hcen/api/auth/login/initiate?clientType=WEB_ADMIN

# Statistics endpoint
curl http://localhost:8080/hcen/api/admin/statistics
```

---

## GitLab CI/CD Pipeline

### 1. Configure GitLab CI/CD Variables

Go to **GitLab Project → Settings → CI/CD → Variables** and add:

#### Protected Variables (main branch only):

| Variable | Value | Protected | Masked |
|----------|-------|-----------|--------|
| `DB_PASSWORD` | `<strong-password>` | ✅ | ✅ |
| `MONGO_ROOT_PASSWORD` | `<mongo-password>` | ✅ | ✅ |
| `REDIS_PASSWORD` | `<redis-password>` | ✅ | ✅ |
| `GUBUY_CLIENT_WEB_PATIENT_ID` | `<client-id>` | ✅ | ❌ |
| `GUBUY_CLIENT_WEB_PATIENT_SECRET` | `<client-secret>` | ✅ | ✅ |
| `GUBUY_CLIENT_WEB_ADMIN_ID` | `<admin-id>` | ✅ | ❌ |
| `GUBUY_CLIENT_WEB_ADMIN_SECRET` | `<admin-secret>` | ✅ | ✅ |
| `GUBUY_CLIENT_MOBILE_ID` | `<mobile-id>` | ✅ | ❌ |
| `PDI_SOAP_USERNAME` | `<pdi-username>` | ✅ | ❌ |
| `PDI_SOAP_PASSWORD` | `<pdi-password>` | ✅ | ✅ |
| `VZ_API_HOST` | `paas.minube.antel.com.uy` | ✅ | ❌ |
| `VZ_ENV_NAME` | `hcen-prod` | ✅ | ❌ |
| `VZ_NODE_ID` | `<node-id>` | ✅ | ❌ |
| `VZ_TOKEN` | `<virtuozzo-token>` | ✅ | ✅ |

#### Standard Variables:

| Variable | Value |
|----------|-------|
| `GRADLE_OPTS` | `-Dorg.gradle.daemon=false` |
| `IMAGE_TAG` | `$CI_COMMIT_SHORT_SHA` |

### 2. Pipeline Stages

The `.gitlab-ci.yml` defines the following stages:

1. **Build** (`build`): Compile Java code with Gradle
2. **Test** (`test`): Run unit/integration tests
3. **Package** (`package`): Create WAR artifact
4. **Docker Build** (`docker_build`): Build Docker image
5. **Push** (`push`): Push image to GitLab Container Registry
6. **Deploy** (`deploy_virtuozzo`): Deploy to ANTEL mi-nube (manual trigger)

### 3. Trigger Deployment

After pushing to `main` branch:

1. GitLab CI automatically runs: build → test → package → docker_build → push
2. Go to **GitLab → CI/CD → Pipelines**
3. Click on latest pipeline
4. Manually trigger **deploy_virtuozzo** stage
5. Monitor deployment logs

---

## ANTEL mi-nube Deployment

### Initial Setup (One-time)

#### 1. Create Virtuozzo Environment

1. Log in to ANTEL mi-nube dashboard
2. Create new environment: `hcen-prod`
3. Add **Custom Docker Container** node
4. Configure topology:
   - Custom Docker container: 2GB RAM, 2 vCPU
   - PostgreSQL (managed addon or separate container)
   - MongoDB (managed addon or separate container)
   - Redis (managed addon or separate container)

#### 2. Configure Custom Docker Container

In the Docker container settings:

- **Docker Image**: `registry.gitlab.com/your-group/hcen:latest`
- **Registry Authentication**:
  - Username: GitLab deploy token username
  - Password: GitLab deploy token password
- **Environment Variables**: Set all variables from `.env`

#### 3. Configure Networking

- Enable **Public IPv4**
- Configure firewall rules:
  - Allow HTTPS (443) from 0.0.0.0/0
  - Allow HTTP (80) from 0.0.0.0/0 (redirect to HTTPS)
  - Block direct access to ports 8080, 5432, 27017, 6379

#### 4. Add SSL Certificate

In Virtuozzo dashboard → **SSL** section:

- Upload SSL certificate for `hcen.uy`
- Upload certificate chain
- Upload private key

#### 5. Configure Domain

- Point DNS A record: `hcen.uy` → Virtuozzo public IP
- Enable **Let's Encrypt** or use custom SSL

### Continuous Deployment

After initial setup, deployments are triggered via GitLab CI/CD:

```bash
# GitLab CI automatically runs on push to main:
git push origin main
```

The pipeline:
1. Builds the WAR
2. Creates Docker image tagged with `$CI_COMMIT_SHORT_SHA`
3. Pushes to GitLab Container Registry
4. **Manual trigger**: Calls Virtuozzo API to redeploy with new tag

---

## Post-Deployment Verification

### 1. Health Checks

```bash
# Application health
curl https://hcen.uy/hcen/api/health

# Expected: {"status": "UP", ...}
```

### 2. Authentication Flow

```bash
# Test gub.uy redirect
curl -I https://hcen.uy/hcen/api/auth/login/initiate?clientType=WEB_ADMIN

# Expected: 302 redirect to auth.iduruguay.gub.uy
```

### 3. Database Connectivity

```bash
# Test statistics endpoint (requires DB)
curl https://hcen.uy/hcen/api/admin/statistics

# Expected: JSON with user/clinic counts
```

### 4. PDI Integration

```bash
# Test INUS user registration (calls PDI for validation)
curl -X POST https://hcen.uy/hcen/api/inus/users \
  -H "Content-Type: application/json" \
  -d '{"ci":"12345678","firstName":"Test","lastName":"User","dateOfBirth":"1990-01-01"}'

# Expected: 201 Created with user details
```

### 5. Monitoring Endpoints

```bash
# Prometheus metrics
curl https://hcen.uy/metrics

# Application logs
ssh into Virtuozzo container → tail /opt/hcen/logs/application.log
```

---

## Monitoring & Maintenance

### Log Access

#### Virtuozzo Dashboard:

1. Environment → Node → **Logs**
2. Real-time tail or download log files

#### SSH Access (if enabled):

```bash
ssh <virtuozzo-container-ip>
tail -f /opt/hcen/logs/application.log
```

### Metrics & Monitoring

#### Prometheus Metrics:

- Endpoint: `https://hcen.uy/metrics`
- Metrics: JVM stats, HTTP requests, database pool, cache hits

#### External Monitoring (Recommended):

- **UptimeRobot**: Health check monitoring
- **Datadog/New Relic**: APM (if available)
- **Sentry**: Error tracking

### Database Backups

#### PostgreSQL:

```bash
# Automated backups (configure in Virtuozzo)
pg_dump -U hcen hcen > backup-$(date +%Y%m%d).sql

# Restore
psql -U hcen hcen < backup-20251024.sql
```

#### MongoDB:

```bash
# Backup
mongodump --uri="mongodb://admin:password@mongodb:27017" --out=/backup

# Restore
mongorestore --uri="mongodb://admin:password@mongodb:27017" /backup
```

---

## Rollback Procedures

### Rollback to Previous Version

#### Option 1: GitLab CI Revert

```bash
# Find previous successful commit SHA
git log --oneline

# Revert to specific commit
git revert <commit-sha>
git push origin main

# Trigger deployment pipeline
```

#### Option 2: Virtuozzo Manual Redeploy

1. GitLab → Container Registry → find previous image tag
2. Virtuozzo Dashboard → Redeploy Container
3. Enter image tag: `registry.gitlab.com/your-group/hcen:<previous-sha>`

#### Option 3: Script-based Rollback

```bash
# From local machine with VZ credentials
cd scripts
./vz-deploy.sh <previous-image-tag>
```

---

## Troubleshooting

### Issue: Application Won't Start

**Symptoms**: Container starts but health check fails

**Diagnosis**:

```bash
# Check logs
docker logs <container-id>

# Common issues:
# - Missing environment variables
# - Database connection failure
# - Invalid JWT keys
```

**Solution**:

1. Verify all environment variables are set
2. Test database connectivity from container
3. Ensure keys mounted at `/opt/hcen/keys/`

---

### Issue: gub.uy Authentication Fails

**Symptoms**: Redirect to gub.uy works, but callback fails

**Diagnosis**:

```bash
# Check OIDC configuration
curl https://auth.iduruguay.gub.uy/oidc/v1/.well-known/openid-configuration

# Verify redirect URI matches registered value
```

**Solution**:

1. Confirm client ID and secret are correct
2. Check redirect URI in AGESIC configuration
3. Verify production endpoints (not testing)

---

### Issue: Database Connection Pool Exhausted

**Symptoms**: `HikariPool-1 - Connection is not available`

**Diagnosis**:

```bash
# Check PostgreSQL connections
psql -U hcen -c "SELECT count(*) FROM pg_stat_activity;"
```

**Solution**:

1. Increase pool size: `DB_POOL_MAX_SIZE=50`
2. Check for connection leaks in code
3. Restart application container

---

### Issue: High Memory Usage

**Symptoms**: Container OOM killed

**Diagnosis**:

```bash
# Check JVM memory
docker stats <container-id>
```

**Solution**:

1. Adjust JVM heap: `JVM_MAX_HEAP=2g`
2. Increase container memory limit
3. Analyze heap dump: `-XX:+HeapDumpOnOutOfMemoryError`

---

## Security Checklist

Before going to production:

- [ ] SSL/TLS certificate installed and valid
- [ ] All secrets stored in environment variables (not code)
- [ ] Database passwords are strong (20+ characters)
- [ ] Redis requires password authentication
- [ ] MongoDB requires authentication
- [ ] Production gub.uy credentials configured
- [ ] PDI credentials configured
- [ ] JWT signing keys generated (RS256, 2048-bit)
- [ ] CORS origins restricted to production domains
- [ ] WildFly management console disabled or secured
- [ ] Firewall rules configured (only 80/443 public)
- [ ] Log level set to INFO (not DEBUG)
- [ ] HTTPS redirect enabled (HTTP → HTTPS)
- [ ] Security headers configured (CSP, HSTS, X-Frame-Options)

---

## Support & Contacts

**HCEN Development Team**:
- German Rodao (4.796.608-7)
- Agustin Silvano (5.096.964-8)
- Piero Santos (6.614.312-9)

**External Dependencies**:
- **AGESIC (gub.uy)**: integraciones@agesic.gub.uy
- **ANTEL mi-nube**: soporte@antel.com.uy

---

## References

- [WildFly Documentation](https://docs.wildfly.org/)
- [Jakarta EE Specifications](https://jakarta.ee/specifications/)
- [gub.uy Developer Portal](https://www.gub.uy/desarrolladores)
- [ANTEL mi-nube Documentation](https://minube.antel.com.uy/docs)
- [GitLab CI/CD](https://docs.gitlab.com/ee/ci/)

---

**Last Updated**: 2025-10-24
**Version**: 1.0.0
