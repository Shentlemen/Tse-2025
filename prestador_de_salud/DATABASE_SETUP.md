# Database Setup Guide - Prestador de Salud

## Overview

The Prestador de Salud application uses:
- **Database**: PostgreSQL (shared with HCEN Central)
- **Schema**: `health_provider` (isolated from other schemas)
- **ORM**: Hibernate JPA
- **Migrations**: Flyway (automatic on startup)

---

## Database Configuration

### Connection Details

| Property | Value |
|----------|-------|
| **Database Name** | `hcen` |
| **Host** | `localhost` |
| **Port** | `5432` (default PostgreSQL) |
| **Schema** | `health_provider` |
| **User** | `postgres` |
| **Password** | `postgres` |

**JDBC URL**: `jdbc:postgresql://localhost:5432/hcen`

---

## Setup Steps

### 1. Ensure PostgreSQL is Running

```bash
# Check if PostgreSQL is running
pg_isready

# Start PostgreSQL (if not running)
# Linux/Mac
sudo service postgresql start

# Windows
# PostgreSQL should start automatically
```

---

### 2. Database Already Exists (Shared with HCEN)

The `hcen` database is already created by HCEN Central. Prestador de Salud uses the same database but **a different schema** (`health_provider`).

**No need to create a new database!**

---

### 3. Schema Creation (Automatic)

The `health_provider` schema is created automatically by the first Flyway migration (`V001`).

**No manual schema creation required!**

---

### 4. Deploy Application

When you deploy the WAR file, Flyway automatically runs migrations on startup.

```bash
# Build WAR
mvn clean package

# Deploy to Tomcat
cp target/prestador.war $TOMCAT_HOME/webapps/

# OR deploy to WildFly
cp target/prestador.war $WILDFLY_HOME/standalone/deployments/
```

---

### 5. Verify Migrations

Check the application logs on startup:

```
==============================================
Starting Flyway database migrations...
==============================================
Database URL: jdbc:postgresql://localhost:5432/hcen
Target schema: health_provider
==============================================
Successfully applied 2 migration(s)
==============================================
```

---

## Database Schema

### Tables Created

#### 1. `health_provider.patients`

Patient demographic information.

| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary key (auto-increment) |
| name | VARCHAR(255) | Patient first name |
| last_name | VARCHAR(255) | Patient last name |
| document_number | VARCHAR(50) | National ID (CI) |
| inus_id | VARCHAR(50) | HCEN INUS ID reference |
| birth_date | DATE | Date of birth |
| gender | VARCHAR(10) | Gender (M/F/Other) |
| email | VARCHAR(255) | Email address |
| phone | VARCHAR(20) | Phone number |
| address | VARCHAR(500) | Residential address |
| active | BOOLEAN | Active status (default: true) |
| clinic_id | BIGINT | Clinic identifier |
| created_at | TIMESTAMP(6) | Creation timestamp |
| updated_at | TIMESTAMP(6) | Last update timestamp |

**Indexes**:
- `idx_patients_document_number` on `document_number`
- `idx_patients_inus_id` on `inus_id`
- `idx_patients_clinic_id` on `clinic_id`
- `idx_patients_email` on `email`
- `idx_patients_active` on `active`
- `idx_patients_clinic_active` on `(clinic_id, active)`

---

#### 2. `health_provider.clinical_documents`

Clinical document metadata and content.

| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary key (auto-increment) |
| title | VARCHAR(255) | Document title |
| description | VARCHAR(1000) | Document description |
| document_type | VARCHAR(100) | Type (CLINICAL_NOTE, LAB_RESULT, etc.) |
| patient_id | BIGINT | FK to patients table |
| clinic_id | BIGINT | Clinic identifier |
| professional_id | BIGINT | Professional who created document |
| specialty_id | BIGINT | Medical specialty (optional) |
| date_of_visit | DATE | Visit date |
| file_name | VARCHAR(100) | File name (if uploaded) |
| file_path | VARCHAR(500) | File path in storage |
| file_size | BIGINT | File size in bytes |
| mime_type | VARCHAR(100) | MIME type |
| rndc_id | VARCHAR(100) | HCEN RNDC reference |
| chief_complaint | TEXT | Patient complaint |
| current_illness | TEXT | History of present illness |
| vital_signs | TEXT | Vital signs measurements |
| physical_examination | TEXT | Physical exam findings |
| diagnosis | TEXT | Clinical diagnosis |
| treatment | TEXT | Treatment plan |
| prescriptions | TEXT | Medications prescribed |
| observations | TEXT | Additional notes |
| next_appointment | DATE | Next appointment date |
| attachments | TEXT | Attached files (JSON array) |
| created_at | TIMESTAMP(6) | Creation timestamp |
| updated_at | TIMESTAMP(6) | Last update timestamp |

**Indexes**:
- `idx_clinical_documents_patient_id` on `patient_id`
- `idx_clinical_documents_clinic_id` on `clinic_id`
- `idx_clinical_documents_professional_id` on `professional_id`
- `idx_clinical_documents_date_of_visit` on `date_of_visit`
- `idx_clinical_documents_document_type` on `document_type`
- `idx_clinical_documents_rndc_id` on `rndc_id`
- `idx_clinical_documents_patient_date` on `(patient_id, date_of_visit DESC)`
- `idx_clinical_documents_clinic_date` on `(clinic_id, date_of_visit DESC)`

---

## Flyway Migrations

### Migration Files

Located in: `src/main/resources/db/migration/`

| File | Description |
|------|-------------|
| `V001__create_patients_table.sql` | Creates patients table with indexes |
| `V002__create_clinical_documents_table.sql` | Creates clinical_documents table with indexes |

### Migration Tracking

Flyway tracks applied migrations in the `flyway_schema_history` table:

```sql
SELECT * FROM health_provider.flyway_schema_history;
```

**Result**:
```
installed_rank | version | description                        | type | script                                    | checksum    | installed_by | installed_on         | execution_time | success
---------------|---------|-------------------------------------|------|-------------------------------------------|-------------|--------------|----------------------|----------------|--------
1              | 1       | create patients table               | SQL  | V001__create_patients_table.sql          | 1234567890  | postgres     | 2025-11-13 10:30:00 | 45             | true
2              | 2       | create clinical documents table     | SQL  | V002__create_clinical_documents_table.sql| 9876543210  | postgres     | 2025-11-13 10:30:01 | 62             | true
```

---

## Manual Database Verification

### Connect to Database

```bash
psql -U postgres -d hcen
```

### Check Schema

```sql
-- List all schemas
\dn

-- Should show:
-- health_provider
-- inus
-- rndc
-- policies
-- audit
-- clinics
```

### Check Tables

```sql
-- Set search path
SET search_path TO health_provider;

-- List tables
\dt

-- Should show:
-- patients
-- clinical_documents
-- flyway_schema_history
```

### Verify Table Structure

```sql
-- Describe patients table
\d patients

-- Describe clinical_documents table
\d clinical_documents
```

### Query Data

```sql
-- Count patients
SELECT COUNT(*) FROM health_provider.patients;

-- Count documents
SELECT COUNT(*) FROM health_provider.clinical_documents;

-- View recent patients
SELECT id, name, last_name, document_number, created_at
FROM health_provider.patients
ORDER BY created_at DESC
LIMIT 10;
```

---

## Troubleshooting

### Issue: Flyway Migration Fails

**Symptoms**: Application fails to start with Flyway error

**Solutions**:

1. **Check database connection**:
   ```bash
   psql -U postgres -h localhost -d hcen
   ```

2. **Check if schema exists**:
   ```sql
   SELECT schema_name FROM information_schema.schemata WHERE schema_name = 'health_provider';
   ```

3. **Check migration history**:
   ```sql
   SELECT * FROM health_provider.flyway_schema_history;
   ```

4. **Repair Flyway metadata** (if checksums don't match):
   ```sql
   DELETE FROM health_provider.flyway_schema_history WHERE success = false;
   ```

5. **Restart application** to re-run migrations

---

### Issue: "Schema health_provider does not exist"

**Solution**: This should not happen as V001 migration creates the schema. If it does:

```sql
CREATE SCHEMA IF NOT EXISTS health_provider;
```

Then restart the application.

---

### Issue: "Table already exists"

**Solution**: Flyway tracks applied migrations. If a migration was partially applied:

```sql
-- Check what's applied
SELECT * FROM health_provider.flyway_schema_history;

-- If table exists but migration not recorded, manually insert:
INSERT INTO health_provider.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES (1, '1', 'create patients table', 'SQL', 'V001__create_patients_table.sql', 0, 'postgres', NOW(), 0, true);
```

---

## Configuration Files

### 1. `persistence.xml`

Location: `src/main/resources/META-INF/persistence.xml`

Key settings:
```xml
<property name="jakarta.persistence.jdbc.url" value="jdbc:postgresql://localhost:5432/hcen"/>
<property name="jakarta.persistence.jdbc.user" value="postgres"/>
<property name="jakarta.persistence.jdbc.password" value="postgres"/>
<property name="hibernate.default_schema" value="health_provider"/>
<property name="hibernate.hbm2ddl.auto" value="validate"/>
```

### 2. `DatabaseMigrationListener.java`

Location: `src/main/java/com/prestador/config/DatabaseMigrationListener.java`

Runs Flyway on application startup automatically.

---

## Production Considerations

### 1. Change Default Credentials

**⚠️ IMPORTANT**: For production, change the default PostgreSQL credentials!

Update `persistence.xml`:
```xml
<property name="jakarta.persistence.jdbc.user" value="${DB_USER}"/>
<property name="jakarta.persistence.jdbc.password" value="${DB_PASSWORD}"/>
```

### 2. Use Environment Variables

```java
// In DatabaseMigrationListener.java
private static final String DB_URL = System.getenv("DB_URL");
private static final String DB_USER = System.getenv("DB_USER");
private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");
```

### 3. Disable `show_sql` in Production

Update `persistence.xml`:
```xml
<property name="hibernate.show_sql" value="false"/>
```

---

## Schema Isolation

The `health_provider` schema is **completely isolated** from other HCEN schemas:

```
hcen database
├── inus (HCEN INUS registry)
├── rndc (HCEN document registry)
├── policies (Access policies)
├── audit (Audit logs)
├── clinics (Clinic management)
└── health_provider (Prestador de Salud) ← Isolated!
```

**Benefits**:
- ✅ No conflicts with HCEN tables
- ✅ Independent schema versioning
- ✅ Easy to backup/restore just this component
- ✅ Clear separation of concerns

---

**Author**: TSE 2025 Group 9
**Date**: 2025-11-13
**Version**: 1.0
