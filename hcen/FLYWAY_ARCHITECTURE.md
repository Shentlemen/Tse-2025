# Flyway Architecture and Workflow - HCEN

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     HCEN Development Workflow                    │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Developer  │────▶│    Flyway    │────▶│  PostgreSQL  │
│              │     │   Migration  │     │   Database   │
└──────────────┘     └──────────────┘     └──────────────┘
       │                     │                     │
       │                     │                     ▼
       │                     │            ┌─────────────────┐
       │                     │            │  Schemas:       │
       │                     │            │  - inus         │
       │                     │            │  - rndc         │
       │                     │            │  - policies     │
       │                     │            │  - audit        │
       │                     │            │  - clinics      │
       │                     │            └─────────────────┘
       │                     ▼
       │            ┌──────────────────┐
       │            │ flyway_schema_   │
       │            │ history (tracks) │
       │            └──────────────────┘
       ▼
┌──────────────┐
│  WildFly +   │
│  Hibernate   │
│  (validates) │
└──────────────┘
```

## Database Schema Management Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                  Schema Management Responsibilities                  │
└─────────────────────────────────────────────────────────────────────┘

1. FLYWAY Creates Schema:
   ┌─────────────────────────────────────────────┐
   │ Migration: V001__create_inus_schema.sql    │
   │ ┌─────────────────────────────────────────┐ │
   │ │ CREATE SCHEMA IF NOT EXISTS inus;       │ │
   │ │ CREATE TABLE inus.inus_users (...);     │ │
   │ │ CREATE INDEX idx_inus_users_ci ...;     │ │
   │ └─────────────────────────────────────────┘ │
   └─────────────────────────────────────────────┘
                     │
                     ▼
   ┌─────────────────────────────────────────────┐
   │        Database Schema Created              │
   └─────────────────────────────────────────────┘

2. HIBERNATE Validates Schema:
   ┌─────────────────────────────────────────────┐
   │ persistence.xml:                            │
   │ hibernate.hbm2ddl.auto = validate           │
   │ ┌─────────────────────────────────────────┐ │
   │ │ @Entity InusUser { ... }                │ │
   │ │ Matches inus.inus_users? ✓              │ │
   │ └─────────────────────────────────────────┘ │
   └─────────────────────────────────────────────┘
                     │
                     ▼
   ┌─────────────────────────────────────────────┐
   │       Application Starts Successfully       │
   └─────────────────────────────────────────────┘

3. IF Mismatch:
   ┌─────────────────────────────────────────────┐
   │ Hibernate Validation Error:                 │
   │ "Schema-validation: missing column"         │
   │                                             │
   │ → Fix: Create Flyway migration              │
   │ → Run: gradlew flywayMigrate                │
   │ → Retry deployment                          │
   └─────────────────────────────────────────────┘
```

## Flyway Migration Workflow

```
┌─────────────────────────────────────────────────────────────────────┐
│                     Migration Execution Flow                         │
└─────────────────────────────────────────────────────────────────────┘

gradlew flywayMigrate
        │
        ▼
┌───────────────────────┐
│ 1. Connect to DB      │
│    - localhost:5432   │
│    - database: hcen   │
└───────┬───────────────┘
        │
        ▼
┌───────────────────────┐
│ 2. Check if           │
│    flyway_schema_     │
│    history exists     │
└───────┬───────────────┘
        │
        ├─ No ──▶ Create table & baseline
        │
        ▼ Yes
┌───────────────────────┐
│ 3. Scan migration     │
│    files in:          │
│    db/migration/      │
└───────┬───────────────┘
        │
        ▼
┌───────────────────────┐
│ 4. Compare with       │
│    history table      │
└───────┬───────────────┘
        │
        ├─ All applied ──▶ Nothing to do
        │
        ▼ Pending migrations found
┌───────────────────────┐
│ 5. Execute pending    │
│    migrations in      │
│    version order      │
└───────┬───────────────┘
        │
        ▼
┌───────────────────────┐
│ 6. For each migration:│
│    - Execute SQL      │
│    - Record in history│
│    - Calculate hash   │
└───────┬───────────────┘
        │
        ▼
┌───────────────────────┐
│ 7. Success!           │
│    Report applied     │
│    migrations         │
└───────────────────────┘
```

## Migration File Lifecycle

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Migration File States                             │
└─────────────────────────────────────────────────────────────────────┘

1. CREATION
   Developer creates: V006__add_new_feature.sql
                             │
                             ▼
   ┌─────────────────────────────────────────┐
   │ File Status: PENDING                    │
   │ In database: No                         │
   │ Can modify: Yes ✓                       │
   └─────────────────────────────────────────┘

2. LOCAL TESTING
   gradlew flywayMigrate (dev)
                             │
                             ▼
   ┌─────────────────────────────────────────┐
   │ File Status: APPLIED (locally)          │
   │ In database: Yes (dev)                  │
   │ Can modify: No (but only you affected)  │
   └─────────────────────────────────────────┘

3. COMMITTED TO GIT
   git commit & git push
                             │
                             ▼
   ┌─────────────────────────────────────────┐
   │ File Status: APPLIED (locally)          │
   │              PENDING (team/staging/prod)│
   │ In database: Yes (dev only)             │
   │ Can modify: NO! ✗ (checksum mismatch)   │
   └─────────────────────────────────────────┘

4. APPLIED TO PRODUCTION
   gradlew flywayMigrate (prod)
                             │
                             ▼
   ┌─────────────────────────────────────────┐
   │ File Status: IMMUTABLE                  │
   │ In database: Yes (all environments)     │
   │ Can modify: NEVER! ✗                    │
   └─────────────────────────────────────────┘

5. IF BUG FOUND
   ┌─────────────────────────────────────────┐
   │ DO NOT modify V006!                     │
   │ Instead:                                │
   │ - Create V007__fix_feature.sql          │
   │ - Apply corrective migration            │
   └─────────────────────────────────────────┘
```

## Directory Structure

```
hcen/
├── src/main/resources/db/migration/    ← Migration files here
│   ├── V001__create_inus_schema.sql    ← Applied ✓
│   ├── V002__create_rndc_schema.sql    ← Applied ✓
│   ├── V003__create_policies_schema.sql ← Applied ✓
│   ├── V004__create_audit_schema.sql   ← Applied ✓
│   └── V005__create_clinics_schema.sql ← Applied ✓
│
├── build.gradle                         ← Flyway config
│
├── FLYWAY_MIGRATION_GUIDE.md           ← Full documentation
├── FLYWAY_QUICKSTART.md                ← Quick reference
├── FLYWAY_CONFIGURATION_SUMMARY.md     ← Setup summary
├── FLYWAY_ARCHITECTURE.md              ← This file
│
├── .env.flyway.example                 ← Environment template
├── test-flyway.bat                     ← Windows test
└── test-flyway.sh                      ← Linux/macOS test
```

## Database Schema Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                    PostgreSQL Database: hcen                         │
└─────────────────────────────────────────────────────────────────────┘

┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐
│  Schema: inus      │  │  Schema: rndc      │  │  Schema: policies  │
├────────────────────┤  ├────────────────────┤  ├────────────────────┤
│ Tables:            │  │ Tables:            │  │ Tables:            │
│ - inus_users       │  │ - rndc_documents   │  │ - access_policies  │
│                    │  │                    │  │ - access_requests  │
│ Purpose:           │  │ Purpose:           │  │                    │
│ National user      │  │ Document metadata  │  │ Purpose:           │
│ registry (CI to    │  │ registry (pointers │  │ Patient access     │
│ INUS ID mapping)   │  │ to peripheral docs)│  │ control policies   │
└────────────────────┘  └────────────────────┘  └────────────────────┘

┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐
│  Schema: audit     │  │  Schema: clinics   │  │ flyway_schema_     │
├────────────────────┤  ├────────────────────┤  │ history (internal) │
│ Tables:            │  │ Tables:            │  ├────────────────────┤
│ - audit_logs       │  │ - clinics          │  │ Tracks:            │
│ - notification_    │  │                    │  │ - Applied          │
│   preferences      │  │ Purpose:           │  │   migrations       │
│                    │  │ Registry of clinics│  │ - Checksums        │
│ Purpose:           │  │ and peripheral     │  │ - Timestamps       │
│ Immutable audit    │  │ nodes integrated   │  │ - Success/failure  │
│ trail (AC026)      │  │ with HCEN          │  │                    │
└────────────────────┘  └────────────────────┘  └────────────────────┘
```

## Deployment Workflow

```
┌─────────────────────────────────────────────────────────────────────┐
│                  Complete Deployment Workflow                        │
└─────────────────────────────────────────────────────────────────────┘

DEVELOPMENT ENVIRONMENT:
┌─────────────────────────────────────────────────────────────────────┐
│ 1. Start PostgreSQL (Docker)                                        │
│    $ docker-compose -f docker-compose-postgres.yml up -d postgres   │
│                                                                      │
│ 2. Run Flyway Migrations                                            │
│    $ gradlew flywayMigrate                                          │
│    ✓ Creates all schemas (inus, rndc, policies, audit, clinics)    │
│    ✓ Creates all tables                                             │
│    ✓ Creates indexes                                                │
│    ✓ Records migration history                                      │
│                                                                      │
│ 3. Build Application                                                │
│    $ gradlew clean build                                            │
│    ✓ Compiles Java code                                             │
│    ✓ Bundles PostgreSQL driver                                      │
│    ✓ Creates WAR file                                               │
│                                                                      │
│ 4. Deploy to WildFly                                                │
│    $ cp build/libs/hcen.war $WILDFLY_HOME/standalone/deployments/   │
│                                                                      │
│ 5. Start WildFly                                                    │
│    $ $WILDFLY_HOME/bin/standalone.sh                                │
│    ✓ Loads datasource (java:jboss/datasources/HcenDS)              │
│    ✓ Hibernate validates schema (hbm2ddl.auto=validate)             │
│    ✓ Application starts                                             │
└─────────────────────────────────────────────────────────────────────┘

PRODUCTION ENVIRONMENT (Virtuozzo):
┌─────────────────────────────────────────────────────────────────────┐
│ 1. Set Environment Variables                                        │
│    $ export FLYWAY_DB_URL=jdbc:postgresql://prod-db:5432/hcen      │
│    $ export FLYWAY_DB_USER=hcen_app                                │
│    $ export FLYWAY_DB_PASSWORD=***                                 │
│    $ export FLYWAY_CLEAN_DISABLED=true                             │
│                                                                      │
│ 2. Run Flyway Migrations                                            │
│    $ gradlew flywayMigrate                                          │
│    ✓ Applies migrations to production database                      │
│                                                                      │
│ 3. Deploy WAR to Virtuozzo                                         │
│    (Via FTP or CI/CD pipeline)                                      │
│                                                                      │
│ 4. WildFly Auto-deploys                                             │
│    ✓ Validates schema                                               │
│    ✓ Application runs                                               │
└─────────────────────────────────────────────────────────────────────┘
```

## Integration with Other Systems

```
┌─────────────────────────────────────────────────────────────────────┐
│                     HCEN System Integration                          │
└─────────────────────────────────────────────────────────────────────┘

┌──────────────────┐
│   PostgreSQL     │ ← Flyway manages schema
│   (Relational)   │ ← Stores: INUS, RNDC, Policies, Clinics
└────────┬─────────┘
         │
         │ JDBC Connection
         │
         ▼
┌──────────────────┐
│    WildFly       │
│  Application     │
│    Server        │
└────────┬─────────┘
         │
         ├─────────────────────────────────────┐
         │                                     │
         ▼                                     ▼
┌──────────────────┐              ┌──────────────────┐
│    MongoDB       │              │      Redis       │
│   (NoSQL)        │              │    (Cache)       │
│                  │              │                  │
│ Stores:          │              │ Stores:          │
│ - Audit logs     │              │ - Sessions       │
│ - Notifications  │              │ - Policy cache   │
│ - System events  │              │ - User profiles  │
└──────────────────┘              └──────────────────┘

Note: Flyway manages ONLY PostgreSQL schemas.
      MongoDB and Redis are managed separately.
```

## Error Handling Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Flyway Error Scenarios                            │
└─────────────────────────────────────────────────────────────────────┘

SCENARIO 1: Database Connection Error
┌─────────────────────────────────────────┐
│ Error: "Connection refused"             │
│                                         │
│ Possible causes:                        │
│ ✗ PostgreSQL not running                │
│ ✗ Wrong port (not 5432)                 │
│ ✗ Firewall blocking                     │
│                                         │
│ Solution:                               │
│ ✓ docker-compose up -d postgres         │
│ ✓ Check docker ps                       │
│ ✓ Verify connection string              │
└─────────────────────────────────────────┘

SCENARIO 2: Migration Checksum Mismatch
┌─────────────────────────────────────────┐
│ Error: "Migration checksum mismatch"    │
│                                         │
│ Cause:                                  │
│ ✗ Someone modified V00X.sql after apply │
│                                         │
│ Solution:                               │
│ 1. gradlew flywayRepair (fixes history) │
│ 2. OR revert file to original           │
│ 3. OR create new migration to fix       │
└─────────────────────────────────────────┘

SCENARIO 3: SQL Syntax Error
┌─────────────────────────────────────────┐
│ Error: "Syntax error at line 42"        │
│                                         │
│ Cause:                                  │
│ ✗ Invalid SQL in migration file         │
│                                         │
│ Solution:                               │
│ 1. Fix SQL syntax in migration file     │
│ 2. Test locally first                   │
│ 3. gradlew flywayRepair (if needed)     │
│ 4. gradlew flywayMigrate                │
└─────────────────────────────────────────┘

SCENARIO 4: Permission Denied
┌─────────────────────────────────────────┐
│ Error: "Permission denied for schema"   │
│                                         │
│ Cause:                                  │
│ ✗ DB user lacks CREATE SCHEMA privilege │
│                                         │
│ Solution:                               │
│ GRANT ALL ON DATABASE hcen TO postgres; │
└─────────────────────────────────────────┘
```

## Best Practices Summary

```
┌─────────────────────────────────────────────────────────────────────┐
│                      Flyway Best Practices                           │
└─────────────────────────────────────────────────────────────────────┘

DO ✓
├─ Use sequential version numbers (V001, V002, V003...)
├─ Write descriptive migration names
├─ Test migrations locally before committing
├─ Use idempotent SQL (IF NOT EXISTS, IF EXISTS)
├─ Add comments explaining complex changes
├─ Create indexes for performance
├─ Keep migrations small and focused
├─ Commit migrations to Git
├─ Use environment variables for credentials
└─ Run flywayInfo to check status

DON'T ✗
├─ Modify applied migrations
├─ Skip version numbers
├─ Use dynamic SQL in migrations
├─ Commit sensitive data (passwords)
├─ Use flywayClean in production
├─ Run migrations manually (use Flyway)
├─ Delete migration files
└─ Ignore validation errors
```

---

**This architecture document provides a visual understanding of how Flyway integrates with the HCEN project.**

For detailed commands and usage, see:
- `FLYWAY_QUICKSTART.md` - Quick reference
- `FLYWAY_MIGRATION_GUIDE.md` - Complete guide
- `FLYWAY_CONFIGURATION_SUMMARY.md` - Configuration details

---

**Last Updated**: 2025-11-04
**Project**: HCEN - Historia Clínica Electrónica Nacional
**Team**: TSE 2025 Group 9
