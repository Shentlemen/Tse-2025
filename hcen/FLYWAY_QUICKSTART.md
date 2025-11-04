# Flyway Quick Start - HCEN

## 1. Start PostgreSQL

**Using Docker Compose (Recommended)**:
```bash
docker-compose -f docker-compose-postgres.yml up -d postgres
```

Wait for database to be ready (~10 seconds).

## 2. Run Migrations

**Windows**:
```bash
gradlew.bat flywayMigrate
```

**Linux/macOS**:
```bash
./gradlew flywayMigrate
```

## 3. Verify

```bash
# Windows
gradlew.bat flywayInfo

# Linux/macOS
./gradlew flywayInfo
```

You should see 5 migrations applied:
- V001: INUS schema
- V002: RNDC schema
- V003: Policies schema
- V004: Audit schema
- V005: Clinics schema

## Common Commands

| Command | Description |
|---------|-------------|
| `flywayMigrate` | Apply pending migrations |
| `flywayInfo` | Show migration status |
| `flywayValidate` | Validate applied migrations |
| `flywayRepair` | Fix migration history |
| `flywayClean` | ⚠️ Drop all schemas (dev only!) |

## Environment Variables (Optional)

For custom database connection:

**Windows**:
```cmd
set FLYWAY_DB_URL=jdbc:postgresql://localhost:5432/hcen
set FLYWAY_DB_USER=postgres
set FLYWAY_DB_PASSWORD=postgres
gradlew.bat flywayMigrate
```

**Linux/macOS**:
```bash
export FLYWAY_DB_URL=jdbc:postgresql://localhost:5432/hcen
export FLYWAY_DB_USER=postgres
export FLYWAY_DB_PASSWORD=postgres
./gradlew flywayMigrate
```

## Troubleshooting

**Issue**: "Connection refused"
- **Fix**: Start PostgreSQL first: `docker-compose -f docker-compose-postgres.yml up -d postgres`

**Issue**: "Flyway task not found"
- **Fix**: Ensure you're in the `hcen/` directory

**Issue**: "Authentication failed"
- **Fix**: Check PostgreSQL credentials in `docker-compose-postgres.yml` or environment variables

## Next Steps

- Read full guide: `FLYWAY_MIGRATION_GUIDE.md`
- Create new migration: See "Creating New Migrations" section in guide
- Deploy to WildFly: `gradlew war` after migrations succeed

---

**For detailed documentation, see FLYWAY_MIGRATION_GUIDE.md**
