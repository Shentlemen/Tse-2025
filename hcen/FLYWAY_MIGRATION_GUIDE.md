# Flyway Database Migration Guide - HCEN Project

## Overview

Flyway is configured for the HCEN project to manage PostgreSQL database schema migrations. This guide explains how to use Flyway for database versioning and migration management.

## Quick Start

### 1. Prerequisites

- Java 21+ installed
- PostgreSQL 14+ running
- Gradle 8.0+ (or use wrapper: `./gradlew`)

### 2. Start PostgreSQL Database

**Using Docker Compose (Recommended for Development)**:
```bash
cd C:\Users\agust\fing\tse\tse-2025\hcen
docker-compose -f docker-compose-postgres.yml up -d postgres
```

**Or use your local PostgreSQL installation**.

### 3. Run Migrations

```bash
# Using Gradle wrapper (Windows)
gradlew flywayMigrate

# Using Gradle wrapper (Linux/macOS)
./gradlew flywayMigrate

# Using installed Gradle
gradle flywayMigrate
```

## Flyway Configuration

### Database Connection

Flyway connects to PostgreSQL using these settings (defined in `build.gradle`):

**Default (Development)**:
- URL: `jdbc:postgresql://localhost:5432/hcen`
- User: `postgres`
- Password: `postgres`

**Environment Variables (Production)**:
```bash
export FLYWAY_DB_URL=jdbc:postgresql://production-host:5432/hcen_production
export FLYWAY_DB_USER=hcen_app_user
export FLYWAY_DB_PASSWORD=your-secure-password
```

### Schemas Managed

Flyway manages these PostgreSQL schemas:
- `inus` - National User Index
- `rndc` - Clinical Document Registry
- `policies` - Access Policies
- `audit` - Audit Logs
- `clinics` - Clinic Configurations

## Available Flyway Commands

### 1. Migrate (Apply Migrations)

Applies all pending migrations to the database:
```bash
gradlew flywayMigrate
```

**What it does**:
- Checks for unapplied migration files in `src/main/resources/db/migration/`
- Executes them in version order (V001, V002, V003, etc.)
- Records applied migrations in `flyway_schema_history` table

### 2. Info (Migration Status)

Shows the current migration status:
```bash
gradlew flywayInfo
```

**Output Example**:
```
+-----------+---------+---------------------+------+---------------------+---------+
| Category  | Version | Description         | Type | Installed On        | State   |
+-----------+---------+---------------------+------+---------------------+---------+
|           | 1       | create inus schema  | SQL  | 2025-11-04 10:30:00 | Success |
|           | 2       | create rndc schema  | SQL  | 2025-11-04 10:30:01 | Success |
| Pending   | 3       | create policies     | SQL  |                     | Pending |
+-----------+---------+---------------------+------+---------------------+---------+
```

### 3. Validate (Check Migration Integrity)

Validates applied migrations against migration files:
```bash
gradlew flywayValidate
```

**Use Case**: Verify that no one modified migration files after they were applied.

### 4. Clean (⚠️ DANGEROUS - Development Only!)

**WARNING**: Drops all database objects in the configured schemas!

```bash
# This is DISABLED by default to prevent accidents
# To enable, set environment variable:
export FLYWAY_CLEAN_DISABLED=false
gradlew flywayClean
```

**When to use**:
- Fresh start during development
- Reset test databases
- **NEVER use in production!**

### 5. Baseline (Initialize Existing Database)

Creates a baseline for an existing database:
```bash
gradlew flywayBaseline
```

**Use Case**: When adding Flyway to an existing database with tables already created.

### 6. Repair (Fix Migration History)

Repairs the Flyway schema history table:
```bash
gradlew flywayRepair
```

**Use Cases**:
- Remove failed migration entries
- Realign checksums after fixing migration files

## Migration Files

### Location
```
src/main/resources/db/migration/
```

### Naming Convention
```
V{version}__{description}.sql
```

**Examples**:
- `V001__create_inus_schema.sql`
- `V002__create_rndc_schema.sql`
- `V003__create_policies_schema.sql`
- `V004__create_audit_schema.sql`
- `V005__create_clinics_schema.sql`

### Current Migrations

| Version | File | Description | Status |
|---------|------|-------------|--------|
| V001 | V001__create_inus_schema.sql | Creates INUS schema and tables | ✅ Ready |
| V002 | V002__create_rndc_schema.sql | Creates RNDC schema and tables | ✅ Ready |
| V003 | V003__create_policies_schema.sql | Creates Policies schema | ✅ Ready |
| V004 | V004__create_audit_schema.sql | Creates Audit schema | ✅ Ready |
| V005 | V005__create_clinics_schema.sql | Creates Clinics schema | ✅ Ready |

## Creating New Migrations

### Step 1: Create Migration File

Create a new SQL file with the next version number:
```bash
# Windows
type nul > src\main\resources\db\migration\V006__add_user_preferences_table.sql

# Linux/macOS
touch src/main/resources/db/migration/V006__add_user_preferences_table.sql
```

### Step 2: Write SQL Migration

```sql
-- V006__add_user_preferences_table.sql
-- Description: Add user preferences table to INUS schema

CREATE TABLE IF NOT EXISTS inus.user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_ci VARCHAR(20) NOT NULL REFERENCES inus.inus_users(ci),
    notification_enabled BOOLEAN DEFAULT true,
    language VARCHAR(10) DEFAULT 'es',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_preferences_ci ON inus.user_preferences(user_ci);

COMMENT ON TABLE inus.user_preferences IS 'User application preferences';
```

### Step 3: Test Migration Locally

```bash
# Check migration status
gradlew flywayInfo

# Apply migration
gradlew flywayMigrate

# Verify
gradlew flywayValidate
```

### Step 4: Commit Migration File

```bash
git add src/main/resources/db/migration/V006__add_user_preferences_table.sql
git commit -m "feat: add user preferences table migration"
```

## Best Practices

### ✅ DO

1. **Use Descriptive Names**: `V006__add_user_preferences_table.sql` (not `V006__changes.sql`)
2. **One Change Per Migration**: Keep migrations focused on a single logical change
3. **Include Rollback Plan**: Document how to undo changes (in comments)
4. **Test Locally First**: Always run migrations locally before committing
5. **Use Idempotent SQL**: Use `IF NOT EXISTS`, `IF EXISTS` where possible
6. **Add Comments**: Document why changes are being made
7. **Index Creation**: Create indexes for frequently queried columns
8. **Version Control**: Always commit migration files to Git

### ❌ DON'T

1. **Don't Modify Applied Migrations**: Once applied to any environment, never change the file
2. **Don't Skip Versions**: Keep version numbers sequential
3. **Don't Use Dynamic SQL**: Avoid functions/procedures that generate SQL
4. **Don't Commit Sensitive Data**: No passwords or secrets in migration files
5. **Don't Use `flywayClean` in Production**: It will delete all data!
6. **Don't Run Migrations Manually**: Always use Flyway commands

## Environment-Specific Configurations

### Development (Local)
```bash
# Use Docker Compose PostgreSQL
docker-compose -f docker-compose-postgres.yml up -d
gradlew flywayMigrate
```

### Development (Custom Database)
```bash
# Set environment variables
export FLYWAY_DB_URL=jdbc:postgresql://dev-server:5432/hcen_dev
export FLYWAY_DB_USER=dev_user
export FLYWAY_DB_PASSWORD=dev_password

gradlew flywayMigrate
```

### Production (Virtuozzo)
```bash
# Set production environment variables
export FLYWAY_DB_URL=jdbc:postgresql://production-db:5432/hcen
export FLYWAY_DB_USER=hcen_app
export FLYWAY_DB_PASSWORD=${SECURE_PASSWORD}
export FLYWAY_CLEAN_DISABLED=true

gradlew flywayMigrate
```

### CI/CD (GitLab CI)

Add to `.gitlab-ci.yml`:
```yaml
migrate:
  stage: deploy
  script:
    - export FLYWAY_DB_URL=${CI_DATABASE_URL}
    - export FLYWAY_DB_USER=${CI_DATABASE_USER}
    - export FLYWAY_DB_PASSWORD=${CI_DATABASE_PASSWORD}
    - ./gradlew flywayMigrate
  only:
    - main
```

## Troubleshooting

### Issue: "Database connection failed"

**Solution**:
1. Check PostgreSQL is running:
   ```bash
   docker ps  # If using Docker
   pg_isready -h localhost -p 5432  # If using local PostgreSQL
   ```
2. Verify connection details in environment variables
3. Check firewall/network connectivity

### Issue: "Migration checksum mismatch"

**Cause**: Migration file was modified after being applied.

**Solution**:
```bash
# Option 1: Repair (if file was corrected)
gradlew flywayRepair

# Option 2: Create new migration to fix the issue
# DO NOT modify the original file
```

### Issue: "Schema not found"

**Cause**: PostgreSQL schemas don't exist.

**Solution**: The migration files should create schemas with `CREATE SCHEMA IF NOT EXISTS`. If not, run:
```sql
CREATE SCHEMA IF NOT EXISTS inus;
CREATE SCHEMA IF NOT EXISTS rndc;
CREATE SCHEMA IF NOT EXISTS policies;
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS clinics;
```

### Issue: "Flyway task not found"

**Cause**: Flyway plugin not applied.

**Solution**: Check `build.gradle` has:
```gradle
plugins {
    id 'org.flywaydb.flyway' version '10.0.0'
}
```

### Issue: "Permission denied"

**Cause**: Database user lacks necessary permissions.

**Solution**: Grant permissions:
```sql
GRANT ALL PRIVILEGES ON DATABASE hcen TO postgres;
GRANT ALL ON SCHEMA inus, rndc, policies, audit, clinics TO postgres;
```

## Integration with WildFly

### JPA Configuration

The `persistence.xml` is configured with:
```xml
<property name="hibernate.hbm2ddl.auto" value="validate"/>
```

This means:
- **Hibernate validates** the schema matches JPA entities
- **Flyway creates** the actual schema
- No auto-generation of DDL by Hibernate

### Deployment Workflow

1. **Before Deployment**: Run Flyway migrations
   ```bash
   gradlew flywayMigrate
   ```

2. **Deploy WAR**: Deploy to WildFly
   ```bash
   gradlew war
   cp build/libs/hcen.war $WILDFLY_HOME/standalone/deployments/
   ```

3. **Startup**: WildFly validates schema against JPA entities

## Flyway Schema History

Flyway tracks applied migrations in the `flyway_schema_history` table:

```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

**Columns**:
- `installed_rank`: Order of execution
- `version`: Migration version (1, 2, 3, etc.)
- `description`: Migration description
- `type`: Migration type (SQL, JDBC)
- `script`: Migration filename
- `checksum`: File checksum for validation
- `installed_by`: Database user who applied it
- `installed_on`: Timestamp of application
- `execution_time`: How long it took (ms)
- `success`: True if successful

## Additional Resources

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Flyway Gradle Plugin](https://flywaydb.org/documentation/usage/gradle/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [HCEN Architecture Document](docs/arquitectura-grupo9-tse.pdf)

## Support

For issues or questions:
1. Check this guide first
2. Review Flyway logs: `build/reports/flyway/`
3. Consult team members (German Rodao, Agustin Silvano, Piero Santos)
4. Check PostgreSQL logs for database errors

---

**Last Updated**: 2025-11-04
**Project**: HCEN - Historia Clínica Electrónica Nacional
**Team**: TSE 2025 Group 9
