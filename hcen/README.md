# HCEN - Central Component

Central component of the National Electronic Health Record (Historia Clínica Electrónica Nacional) system for Uruguay.

## Prerequisites

- Java 17 or higher
- WildFly 30.0+ (Jakarta EE 10)
- PostgreSQL 14+ (relational data)
- MongoDB 6.0+ (NoSQL for audit logs and notifications)
- Gradle 8.0+ (or use included wrapper)

## Project Structure

```
hcen/
├── src/
│   ├── main/
│   │   ├── java/uy/gub/hcen/
│   │   │   ├── api/            # REST API endpoints
│   │   │   │   ├── rest/       # JAX-RS resources
│   │   │   │   └── dto/        # Data Transfer Objects
│   │   │   ├── service/        # Business logic
│   │   │   │   ├── inus/       # National User Index
│   │   │   │   ├── rndc/       # Clinical Document Registry
│   │   │   │   ├── policy/     # Access policy engine
│   │   │   │   ├── audit/      # Audit logging
│   │   │   │   └── notification/ # Notifications
│   │   │   ├── integration/    # External integrations
│   │   │   │   ├── gubuy/      # gub.uy authentication
│   │   │   │   ├── pdi/        # PDI platform
│   │   │   │   └── peripheral/ # Peripheral nodes
│   │   │   ├── repository/     # Data access layer
│   │   │   ├── entity/         # JPA entities
│   │   │   ├── config/         # Configuration classes
│   │   │   ├── exception/      # Custom exceptions
│   │   │   └── util/           # Utilities
│   │   ├── resources/
│   │   │   └── META-INF/
│   │   │       └── persistence.xml
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   ├── web.xml
│   │       │   ├── beans.xml
│   │       │   └── jboss-web.xml
│   │       ├── admin/          # Admin portal (JSP)
│   │       ├── usuario/        # Health user portal (JSP)
│   │       └── index.jsp
│   └── test/
│       └── java/
└── build.gradle
```

## Architectural Decision: Hybrid Database Approach

**Decision**: Use a hybrid database architecture combining PostgreSQL (relational) and MongoDB (NoSQL document database).

**Rationale**:
- **Fulfills optional requirement**: "NoSQL database usage (e.g., document, graph) for part of system data" (1 point)
- **Performance optimization**: High-volume write operations benefit from document storage
- **Schema flexibility**: Audit logs and notifications have variable structures
- **Scalability**: MongoDB handles high-throughput audit logging better than relational DBs

**Data Distribution Strategy**:

| Data Type | Database | Reason |
|-----------|----------|--------|
| INUS (Users) | PostgreSQL | Structured relational data with ACID requirements |
| RNDC (Document Metadata) | PostgreSQL | Complex relationships, transactional integrity |
| Access Policies | PostgreSQL | Relational queries, referential integrity |
| Clinical Documents | PostgreSQL | Structured metadata with foreign keys |
| **Audit Logs** | **MongoDB** | High write volume, append-only, flexible schema |
| **Notifications** | **MongoDB** | High throughput, time-series data, flexible content |
| **System Events** | **MongoDB** | Variable structure, analytics-friendly |

**Benefits**:
- ✅ Reduced load on PostgreSQL for high-volume writes
- ✅ Better query performance for audit analytics (MongoDB aggregation pipeline)
- ✅ Flexible schema for evolving audit/notification requirements
- ✅ Horizontal scalability for audit data through MongoDB sharding
- ✅ Time-series optimization with MongoDB's TTL indexes

## Database Setup

1. Create PostgreSQL database:
```bash
createdb hcen_db
```

2. Configure WildFly datasource (standalone.xml):
```xml
<datasource jndi-name="java:jboss/datasources/HcenDS" pool-name="HcenDS">
    <connection-url>jdbc:postgresql://localhost:5432/hcen_db</connection-url>
    <driver>postgresql</driver>
    <security>
        <user-name>your_username</user-name>
        <password>your_password</password>
    </security>
</datasource>
```

3. Add PostgreSQL driver to WildFly:
```bash
# Download driver
wget https://jdbc.postgresql.org/download/postgresql-42.7.0.jar

# Deploy to WildFly
cp postgresql-42.7.0.jar $WILDFLY_HOME/standalone/deployments/
```

### MongoDB Setup

1. Install MongoDB (if not already installed):
```bash
# Ubuntu/Debian
sudo apt-get install -y mongodb-org

# macOS
brew tap mongodb/brew
brew install mongodb-community

# Windows: Download from https://www.mongodb.com/try/download/community
```

2. Start MongoDB:
```bash
# Linux/macOS
sudo systemctl start mongod
# or
mongod --dbpath /path/to/data

# Windows: Start as service or run mongod.exe
```

3. Create MongoDB database and collections:
```bash
mongosh

use hcen_audit
db.createCollection("audit_logs")
db.audit_logs.createIndex({ "timestamp": -1 })
db.audit_logs.createIndex({ "userId": 1 })
db.audit_logs.createIndex({ "eventType": 1 })

db.createCollection("notifications")
db.notifications.createIndex({ "timestamp": -1 })
db.notifications.createIndex({ "userId": 1, "read": 1 })
db.notifications.createIndex({ "createdAt": 1 }, { expireAfterSeconds: 2592000 })  # 30 days TTL

db.createCollection("system_events")
db.system_events.createIndex({ "timestamp": -1 })
```

4. MongoDB connection configuration:
The application uses MongoDB Java Driver to connect. Connection details are configured in `src/main/resources/mongodb.properties`:
```properties
mongodb.uri=mongodb://localhost:27017
mongodb.database=hcen_audit
```

## Build & Run

### Build the project
```bash
# Using Gradle wrapper (recommended)
./gradlew clean build

# Or using system Gradle
gradle clean build
```

### Run tests
```bash
./gradlew test

# Run tests with coverage report
./gradlew test jacocoTestReport
# Report available at: build/reports/jacoco/test/html/index.html
```

### Deploy to WildFly

#### Option 1: Manual deployment
```bash
# Build WAR file
./gradlew war

# Copy to WildFly deployments directory
cp build/libs/hcen.war $WILDFLY_HOME/standalone/deployments/
```

#### Option 2: Using Gradle task
```bash
# Set WILDFLY_HOME environment variable first
export WILDFLY_HOME=/path/to/wildfly

# Deploy
./gradlew deployToWildFly

# Undeploy
./gradlew undeployFromWildFly
```

#### Option 3: WildFly CLI
```bash
$WILDFLY_HOME/bin/jboss-cli.sh --connect
deploy build/libs/hcen.war
```

### Start WildFly
```bash
$WILDFLY_HOME/bin/standalone.sh
```

### Access the application
- Main portal: http://localhost:8080/hcen/
- Admin portal: http://localhost:8080/hcen/admin/
- Health user portal: http://localhost:8080/hcen/usuario/
- REST API: http://localhost:8080/hcen/api/
- Health check: http://localhost:8080/hcen/api/health

## Development

### Hot reload
For development, use WildFly's deployment scanner with auto-deploy:
```bash
# Build and deploy on file changes
./gradlew build deployToWildFly --continuous
```

### Code coverage
Target: 80% code coverage (configured in build.gradle)
```bash
# Check coverage
./gradlew test jacocoTestCoverageVerification
```

## Key Components

- **INUS Service**: National Index of Health Users - manages user registry
- **RNDC Service**: National Clinical Document Registry - manages document metadata
- **Policy Engine**: Evaluates access policies using ABAC/RBAC
- **Audit Service**: Logs all system events for compliance
- **Integration Layer**: Adapters for gub.uy, PDI, and peripheral nodes

## Security

- All communications use HTTPS (configured in web.xml)
- Passwords hashed with BCrypt + salt
- Session cookies marked as HttpOnly and Secure
- OAuth 2.0/OIDC for patient authentication via gub.uy
- ABAC/RBAC for authorization

## Testing

- Unit tests: JUnit 5 + Mockito
- Integration tests: Arquillian + WildFly container
- Code coverage: Jacoco (80% target)

## Troubleshooting

### Port already in use
```bash
# Check what's using port 8080
lsof -i :8080
netstat -ano | findstr :8080  # Windows

# Change WildFly port in standalone.xml
```

### Database connection errors

**PostgreSQL**:
- Verify PostgreSQL is running: `systemctl status postgresql` or `pg_isready`
- Check datasource configuration in WildFly
- Ensure PostgreSQL driver is deployed
- Test connection: `psql -h localhost -U your_username -d hcen_db`

**MongoDB**:
- Verify MongoDB is running: `systemctl status mongod` or `mongosh --eval "db.version()"`
- Check connection string in mongodb.properties
- Ensure MongoDB is accepting connections: `mongosh --host localhost --port 27017`
- Check MongoDB logs: `/var/log/mongodb/mongod.log` (Linux) or Event Viewer (Windows)

### ClassNotFoundException
- Clean and rebuild: `./gradlew clean build`
- Check dependencies in build.gradle
- Verify WildFly modules are correctly configured

## Contributors

German Rodao, Agustin Silvano, Piero Santos - Grupo 9 TSE 2025
