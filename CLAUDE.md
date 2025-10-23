# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## ⚠️ CRITICAL DEVELOPMENT GUIDELINES ⚠️

### 1. Documentation Updates
**ALWAYS update TODO.md after implementing changes using an agent.**
- After completing any feature, service, or API endpoint
- Use the `general-purpose` agent to update TODO.md
- Document: files created, lines of code, features implemented, build status
- Update progress metrics (overall completion %, services count, endpoints count)

### 2. Implementation Scope - HCEN Central ONLY
**ONLY implement code for the HCEN Central component. DO NOT implement:**
- ❌ PDI Mock (pdi-mock component)
- ❌ Mobile App (mobile/ directory)
- ❌ Peripheral Multi-tenant Component (clinic/ directory)
- ❌ Health Provider Reference Implementation (provider/ directory)

**Focus EXCLUSIVELY on:**
- ✅ HCEN Central backend (`hcen/src/main/java/uy/gub/hcen/*`)
- ✅ HCEN Central web UI (`hcen/src/main/webapp/*`)
- ✅ Integration clients that HCEN uses to communicate with external systems
- ✅ REST APIs that HCEN exposes for peripheral nodes to call

### 3. Communication Protocols
**ALWAYS respect the communication protocols defined in the problem definition.**
- Review `docs/arquitectura-grupo9-tse.pdf` for protocol specifications
- Follow defined patterns:
  - gub.uy: OAuth 2.0 / OpenID Connect
  - PDI: SOAP Web Services
  - Peripheral Nodes: REST/HTTP with API key authentication
  - Firebase: Cloud Messaging API
- Use HTTPS for ALL external communications (AC002-AC004)

### 4. Communication Pattern Decisions
**ALWAYS ask for confirmation/clarification when deciding on communication patterns:**
- ❓ Two-way vs. one-way communication
- ❓ Request-response vs. message broker (asynchronous)
- ❓ Polling vs. push notifications
- ❓ Synchronous vs. asynchronous processing

**Example**: "Should the document registration from peripheral nodes be synchronous (wait for RNDC confirmation) or asynchronous (fire-and-forget with callback)?"

---

## Project Overview

**hcen.uy** (Historia Clínica Electrónica Nacional) is Uruguay's National Electronic Health Record system - a distributed platform enabling secure, traceable sharing of clinical information among patients, healthcare professionals, and clinics. This is a TSE 2025 university project simulating a real national health information exchange infrastructure.

### Core Problem
Enable seamless data exchange and coordinated medical care across different healthcare organizations in Uruguay (labs, health providers, clinics) while respecting patient privacy and consent.

## Repository Structure

```
tse-2025/
├── hcen/                      # Central component (WildFly Jakarta EE)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Backend services (INUS, RNDC, Policy Engine)
│   │   │   ├── webapp/        # Web portals (Admin, Patient)
│   │   │   └── resources/
│   │   └── test/
│   ├── build.gradle           # Gradle build configuration
│   └── README.md
│
├── mobile/                    # React Native mobile application
│   ├── src/
│   │   ├── components/
│   │   ├── screens/
│   │   ├── services/          # Firebase, REST API clients
│   │   └── utils/
│   ├── android/
│   ├── ios/
│   ├── package.json
│   └── README.md
│
├── clinic/                    # Peripheral multi-tenant component (full application)
│   ├── src/                   # For clinics without existing infrastructure
│   │   ├── main/
│   │   │   ├── java/          # Clinic/Professional portals, document storage
│   │   │   ├── webapp/
│   │   │   └── resources/
│   │   └── test/
│   ├── build.gradle
│   └── README.md
│
├── provider/                  # Health provider reference implementation
│   ├── src/                   # Lightweight API for existing providers/hospitals
│   │   ├── main/
│   │   │   ├── java/          # Service interface for document retrieval
│   │   │   └── resources/
│   │   └── test/
│   ├── build.gradle
│   └── README.md
│
├── pdi-mock/                  # PDI simulator (SOAP services)
│   ├── src/
│   │   └── main/
│   │       ├── java/          # Mock SOAP services for identity validation
│   │       └── resources/     # WSDL definitions
│   ├── build.gradle
│   └── README.md
│
├── docs/                      # Architecture and design documents
│   └── arquitectura-grupo9-tse.pdf
│
└── .gitlab-ci.yml             # CI/CD pipeline configuration
```

## Technology Stack

### Central Component (hcen/)
- **Application Server**: WildFly (Jakarta EE)
- **Build Tool**: Gradle
- **Backend**: Java (Jakarta EE - JAX-RS, JPA, CDI, EJB)
- **Database**: PostgreSQL (INUS, RNDC, Policies, Audit)
- **Cache**: Redis (session management, distributed caching)
- **Web Services**: JAX-RS (REST), JAX-WS (SOAP for PDI)
- **Security**: Jakarta Security, JWT, OIDC/OAuth 2.0

### Mobile Component (mobile/)
- **Framework**: React Native
- **Notification Service**: Firebase (Cloud Messaging, Cloud Firestore, Cloud Functions)
- **API Communication**: REST (JSON) over HTTPS
- **Standards**: IPS-FHIR (International Patient Summary)

### Peripheral Multi-tenant Component (clinic/)
- **Purpose**: Full application for clinics without existing infrastructure
- **Application Server**: WildFly (Jakarta EE)
- **Build Tool**: Gradle
- **Multi-tenancy**: Schema-per-tenant or discriminator-based
- **Document Storage**: Local filesystem or object storage (AWS S3, Azure Storage)
- **Database**: PostgreSQL (per-tenant isolation)
- **Features**: Clinic Admin Portal, Health Professionals Portal, local document management

### Health Provider Reference Implementation (provider/)
- **Purpose**: Lightweight API for existing health providers (hospitals, labs)
- **Application Server**: WildFly (Jakarta EE)
- **Build Tool**: Gradle
- **Interface**: REST/SOAP service for document retrieval
- **Document Storage**: Delegates to existing provider systems
- **Database**: Optional (can integrate with existing provider databases)

### External Integrations
- **gub.uy**: National authentication (OIDC/OAuth 2.0)
- **PDI**: Identity validation platform (SOAP Web Services)
- **Firebase**: Push notifications and real-time sync

## Building, Testing, and Running

### Central Component (hcen/)

```bash
# Build
cd hcen
./gradlew clean build

# Run tests (must achieve 80% coverage)
./gradlew test

# Generate coverage report
./gradlew jacocoTestReport

# Deploy to WildFly
./gradlew wildFlyDeploy

# Run locally
./gradlew wildFlyRun
```

**WildFly Configuration**:
- Configure PostgreSQL datasource in `standalone.xml`
- Redis connection pool configuration
- HTTPS/SSL certificates (mandatory for all communications)
- CORS settings for mobile/web clients

### Mobile Component (mobile/)

```bash
# Install dependencies
cd mobile
npm install

# Run on Android
npm run android

# Run on iOS
npm run ios

# Run tests
npm test

# Build APK
cd android
./gradlew assembleRelease
```

**Firebase Configuration**:
- Add `google-services.json` (Android) and `GoogleService-Info.plist` (iOS)
- Configure FCM credentials
- Set up Cloud Firestore rules for security

### Peripheral Multi-tenant Component (clinic/)

```bash
# Build
cd clinic
./gradlew clean build

# Run tests
./gradlew test

# Deploy
./gradlew wildFlyDeploy
```

**Multi-tenant Configuration**:
- Database connection per tenant (schema isolation)
- Tenant-specific branding (logos, colors) stored in tenant config table
- Document storage path per tenant

### Health Provider Reference Implementation (provider/)

```bash
# Build
cd provider
./gradlew clean build

# Run tests
./gradlew test

# Deploy
./gradlew wildFlyDeploy
```

**Provider Configuration**:
- Exposes document retrieval API for HCEN integration
- Integrates with existing provider systems (EHR, PACS, LIS)
- Implements required service interface for central component

### PDI Mock (pdi-mock/)

```bash
# Build
cd pdi-mock
./gradlew clean build

# Run SOAP service
./gradlew wildFlyRun
```

**SOAP Endpoints**:
- Servicio Básico de Información: Returns simulated user data (name, date of birth)
- Must respect PDI interface specifications

## Key Architectural Concepts

### INUS (Índice Nacional de Usuarios de Salud)
**Purpose**: National registry of all health system users (the "phone book")

**Responsibilities**:
- Central storage of user identification (CI - Cédula de Identidad)
- Personal data (name, date of birth, health user status)
- Cross-clinic unique identifier (inusid)
- Integration with PDI for identity validation

**Key Operations**:
- User registration (from peripheral nodes)
- User lookup by CI
- Age verification (legal requirements)

### RNDC (Registro Nacional de Documentos Clínicos)
**Purpose**: Metadata registry pointing to where actual clinical documents are stored

**Responsibilities**:
- Document metadata storage (type, creation date, author, patient)
- Document locator (URL/reference to peripheral storage)
- Document hash (integrity verification)
- Document status tracking (active/inactive)

**Key Operations**:
- Document registration (from peripheral nodes)
- Document search by patient ID
- Document retrieval (returns locator, not actual document)

**Important**: RNDC stores metadata only. Actual documents remain in peripheral node storage.

### Policy Engine
**Purpose**: Centralized access control decision point

**Models**:
- **ABAC (Attribute-Based Access Control)**: Policies based on user attributes (role, specialty), resource attributes (document type, sensitivity), context (time, location, purpose)
- **RBAC (Role-Based Access Control)**: Clinic-internal roles and permissions

**Policy Evaluation**:
1. Health professional requests document access
2. Policy Engine evaluates against patient-defined policies
3. Returns decision: **Permit**, **Deny**, or **Pending** (requires patient approval)
4. All decisions logged in audit system

**Patient Control**:
- Granular policies (by document type, professional specialty, clinic)
- Immediate policy updates propagate to RNDC
- Access audit trails visible to patients

### Multi-tenancy (Peripheral Nodes)
**Architecture**: Schema-per-tenant or discriminator-based isolation

**Tenant Identification**:
- Tenant ID in request headers or JWT claims
- Tenant context established at request entry
- All queries filtered by tenant ID

**Isolation Requirements**:
- Strict data segregation (AC024, AC025)
- Per-tenant configuration (branding, workflows)
- No cross-tenant data leakage

## Integration Points and Protocols

### Central ↔ Mobile
- **Protocol**: HTTPS, REST (JSON)
- **Authentication**: JWT tokens (issued after gub.uy authentication)
- **Endpoints**:
  - `GET /api/patients/{ci}/history` - Retrieve clinical history
  - `GET /api/patients/{ci}/policies` - Get access policies
  - `POST /api/patients/{ci}/policies` - Update policies
  - `GET /api/patients/{ci}/audit` - Access audit logs

### Central ↔ Peripheral Nodes

**From Peripheral to Central**:
- **INUS Registration** (AC013): `POST /api/inus/users`
  ```json
  {
    "ci": "12345678",
    "firstName": "Juan",
    "lastName": "Pérez",
    "dateOfBirth": "1990-01-15",
    "clinicId": "clinic-001"
  }
  ```

- **RNDC Registration** (AC014): `POST /api/rndc/documents`
  ```json
  {
    "patientCI": "12345678",
    "documentType": "CLINICAL_NOTE",
    "documentLocator": "https://clinic-001.hcen.uy/documents/abc123",
    "documentHash": "sha256:...",
    "createdBy": "doctor@clinic.uy",
    "createdAt": "2025-10-12T10:30:00Z"
  }
  ```

**From Central to Peripheral**:
- **Document Retrieval** (AC015): `GET /api/documents/{documentId}`
  - Peripheral returns actual document (PDF, XML, FHIR)

- **Clinic Onboarding** (AC016): `POST /api/clinics/onboard`
  - Central sends clinic configuration to peripheral node

### Central ↔ gub.uy
- **Protocol**: OIDC/OAuth 2.0
- **Flow**:
  1. User initiates login
  2. Redirect to gub.uy authorization endpoint
  3. User authenticates, gub.uy returns authorization code
  4. Central exchanges code for access token and ID token
  5. Extract user claims (CI, name), establish session
  6. Issue JWT for subsequent API calls

### Central ↔ PDI
- **Protocol**: HTTPS, SOAP (WS-Security)
- **Service**: Servicio Básico de Información de DNIC
- **Operation**: Query user data by CI, receive date of birth
- **Use Case**: Age verification during INUS registration

### Central ↔ Firebase
- **Service**: Firebase Cloud Messaging (FCM)
- **Flow**:
  1. Central detects event (access request, policy change, alert)
  2. POST to FCM REST API with device tokens
  3. FCM delivers push notification to mobile app
  4. Mobile app displays notification, optionally fetches details

## Security Requirements and Constraints

### Authentication
- **AC001**: Passwords hashed with salt (use bcrypt, PBKDF2, or Argon2)
- **Citizens/Patients**: gub.uy (OIDC/OAuth 2.0) - CU01, CU03
- **Healthcare Professionals**: Clinic-internal authentication
- **Session Management**: JWT tokens, no local state (stateless servers for horizontal scaling - AC006, AC007)

### Data in Transit
- **AC002-AC004**: ALL communications use HTTPS (TLS/SSL)
- **Certificate Management**: Valid certificates for production deployments
- **Mobile**: Pin SSL certificates to prevent MITM attacks

### Authorization
- **AC005**: Document access respects user-defined policies
- **Policy Enforcement**: Every document access request evaluated by Policy Engine
- **Three-tier Decisions**: Permit, Deny, Pending (requires patient approval)
- **Least Privilege**: Users/professionals only see data they're authorized for

### Audit and Traceability
- **AC026**: Patients can view who accessed their records and when
- **Comprehensive Logging**: All access, modification, administrative events
- **Immutable Audit Logs**: Append-only, tamper-evident
- **Retention**: Define retention policy (e.g., 7 years per Uruguayan law)

### Compliance
- **R009**: Ley N° 18.331 (Data Protection Law of Uruguay)
- **AGESIC Guidelines**: Information security for public sector systems
- **Multi-tenant Isolation**: AC024, AC025 - strict data segregation
- **Data Sovereignty**: Central component hosted on national infrastructure (Elastic Cloud ANTEL)

## Testing Requirements

### Coverage Target
- **AC017, AC018**: Approximately 80% code coverage
- **Scope**: All service layers, business logic, integration adapters
- **Tools**: JaCoCo (Java), Jest/React Native Testing Library (mobile)

### Test Types
- **Unit Tests**: Service methods, policy evaluation logic, data transformations
- **Integration Tests**: Database operations, REST/SOAP clients, authentication flows
- **Performance Tests** (AC008, AC009, AC010):
  - Verify response times don't degrade under load
  - Find system breaking point
  - Identify bottlenecks (database queries, network latency)

### Test Data
- **Synthetic Patients**: Generate realistic test users with CI numbers
- **Clinical Documents**: Sample PDFs, FHIR resources
- **Policy Scenarios**: Various consent configurations

### CI/CD Pipeline
- **Automated Tests**: Run on every commit
- **Build Failure**: Block merge if tests fail or coverage drops below 80%
- **Deployment Gates**: Require passing tests before deploying to staging/production

## Architectural Patterns and Design Principles

### Layered Architecture (Central Component)
```
┌─────────────────────────────────────────┐
│ Presentation Layer                      │
│ (Web Portals, REST API)                 │
├─────────────────────────────────────────┤
│ Service Layer                           │
│ (Business Logic, Policy Enforcement)    │
├─────────────────────────────────────────┤
│ Integration Layer                       │
│ (gub.uy, PDI, Peripheral Node Adapters) │
├─────────────────────────────────────────┤
│ Persistence Layer                       │
│ (INUS, RNDC, Policy, Audit Repositories)│
└─────────────────────────────────────────┘
```

**Benefits**: Separation of concerns, testability, maintainability

### Service-Oriented Architecture (SOA)
- **Well-defined Interfaces**: REST/SOAP contracts
- **Loose Coupling**: Components interact via standardized protocols
- **Interoperability**: Multiple healthcare actors integrate seamlessly

### Repository Pattern
- **Data Access Abstraction**: Repositories encapsulate database operations
- **Example**: `InusRepository`, `RndcRepository`, `PolicyRepository`
- **Benefits**: Testability (mock repositories), flexibility to change storage

### Adapter Pattern
- **External System Integration**: `GubUyAdapter`, `PDIAdapter`
- **Decoupling**: Business logic isolated from external API details
- **Example**:
  ```java
  public interface GubUyAdapter {
      AuthenticationResult authenticate(String authorizationCode);
      UserClaims getUserClaims(String accessToken);
  }
  ```

### Strategy Pattern (Policy Engine)
- **Dynamic Evaluation**: Runtime policy decisions
- **Extensibility**: Add new policy types without modifying core engine
- **Example**:
  ```java
  public interface AccessPolicy {
      Decision evaluate(AccessRequest request);
  }

  public class DocumentTypePolicy implements AccessPolicy { ... }
  public class SpecialtyPolicy implements AccessPolicy { ... }
  public class TimeBasedPolicy implements AccessPolicy { ... }
  ```

### Design Principles
- **Separation of Concerns**: Each component has single, well-defined responsibility
- **Stateless Servers** (AC006, AC007): No local session storage for horizontal scaling
- **Fail-Safe Defaults**: Deny access unless explicitly permitted
- **Defense in Depth**: Multiple security layers (authentication, authorization, audit, encryption)
- **API-First**: Well-documented REST/SOAP contracts before implementation

## Core Entities and Data Models

### INUS User
```java
@Entity
@Table(name = "inus_users")
public class InusUser {
    @Id
    private String ci;  // Cédula de Identidad (national ID)

    private String inusId;  // Unique cross-clinic identifier
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String email;
    private String phoneNumber;
    private UserStatus status;  // ACTIVE, INACTIVE, SUSPENDED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### RNDC Document Metadata
```java
@Entity
@Table(name = "rndc_documents")
public class RndcDocument {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "patient_ci")
    private String patientCI;

    @Column(name = "document_locator")
    private String documentLocator;  // URL to peripheral storage

    @Column(name = "document_hash")
    private String documentHash;  // SHA-256 for integrity

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;  // CLINICAL_NOTE, LAB_RESULT, IMAGING, etc.

    @Column(name = "created_by")
    private String createdBy;  // Professional ID

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;  // ACTIVE, INACTIVE, DELETED

    @Column(name = "clinic_id")
    private String clinicId;
}
```

### Access Policy
```java
@Entity
@Table(name = "access_policies")
public class AccessPolicy {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "patient_ci")
    private String patientCI;

    @Enumerated(EnumType.STRING)
    private PolicyType policyType;  // DOCUMENT_TYPE, SPECIALTY, TIME_BASED, CLINIC

    @Column(name = "policy_config", columnDefinition = "jsonb")
    private String policyConfig;  // JSON configuration

    @Enumerated(EnumType.STRING)
    private PolicyEffect effect;  // PERMIT, DENY

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

### Audit Log
```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private EventType eventType;  // ACCESS, MODIFICATION, CREATION, DELETION

    @Column(name = "actor_id")
    private String actorId;  // User or system that performed action

    @Column(name = "resource_type")
    private String resourceType;  // DOCUMENT, USER, POLICY

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "action_outcome")
    private ActionOutcome actionOutcome;  // SUCCESS, FAILURE, DENIED

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "details", columnDefinition = "jsonb")
    private String details;  // Additional context
}
```

### Multi-tenant Clinic Configuration (Peripheral)
```java
@Entity
@Table(name = "clinic_configurations")
public class ClinicConfiguration {
    @Id
    private String clinicId;

    private String clinicName;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;

    @Column(name = "database_schema")
    private String databaseSchema;  // For schema-per-tenant model

    @Column(name = "document_storage_path")
    private String documentStoragePath;

    @Column(name = "hcen_central_url")
    private String hcenCentralUrl;  // Central component endpoint

    @Column(name = "hcen_api_key")
    private String hcenApiKey;  // Authentication for central component
}
```

## Authentication/Authorization Mechanisms

### Flow 1: Patient Authentication (gub.uy)
```
1. Patient → Central: Click "Login with gub.uy"
2. Central → gub.uy: Redirect to /authorize?client_id=...&redirect_uri=...
3. Patient → gub.uy: Enter credentials, consent to data sharing
4. gub.uy → Central: Redirect to /callback?code=...
5. Central → gub.uy: POST /token (exchange code for tokens)
6. gub.uy → Central: Return access_token, id_token
7. Central: Validate id_token, extract claims (CI, name)
8. Central → INUS: Lookup or create user by CI
9. Central: Generate JWT for subsequent API calls
10. Central → Patient: Set JWT in cookie/header
```

### Flow 2: Professional Authentication (Clinic-Internal)
```
1. Professional → Peripheral: POST /login {username, password}
2. Peripheral: Validate credentials (hashed password comparison)
3. Peripheral: Load professional profile (name, specialty, clinic)
4. Peripheral: Generate JWT with claims (professionalId, specialties, clinicId)
5. Peripheral → Professional: Return JWT
6. Professional: Include JWT in Authorization header for subsequent requests
```

### Flow 3: Document Access Authorization
```
1. Professional → Peripheral: Request patient document
2. Peripheral → Central: Query RNDC for document metadata by patientCI
3. Central → Policy Engine: Evaluate access policies
   - Input: {professionalId, specialties, clinicId, documentType, patientCI}
   - Output: PERMIT / DENY / PENDING
4a. If PERMIT:
    - Central → Peripheral: Return document locator
    - Peripheral: Retrieve document from storage
    - Peripheral → Professional: Return document
    - Central: Log access in audit system
4b. If DENY:
    - Central → Professional: Return 403 Forbidden with reason
    - Central: Log denied access in audit system
4c. If PENDING:
    - Central → Patient (mobile): Send push notification requesting approval
    - Central → Professional: Return 202 Accepted (pending patient approval)
    - Patient → Central: Approve/deny via mobile app
    - If approved: Follow PERMIT flow
```

### JWT Claims Structure
```json
{
  "sub": "12345678",           // CI for patients, professionalId for professionals
  "name": "Juan Pérez",
  "role": "PATIENT",           // PATIENT, PROFESSIONAL, CLINIC_ADMIN, HCEN_ADMIN
  "specialties": ["CARDIOLOGY"], // For professionals only
  "clinicId": "clinic-001",    // For clinic-scoped users
  "exp": 1730000000,           // Expiration timestamp
  "iat": 1729990000,           // Issued at timestamp
  "iss": "https://hcen.uy"     // Issuer
}
```

## Development Guidelines

### Code Organization
- **Package by Feature**: Group related classes (controllers, services, repositories) by feature
  - `uy.edu.fing.hcen.inus.*`
  - `uy.edu.fing.hcen.rndc.*`
  - `uy.edu.fing.hcen.policy.*`

### Naming Conventions
- **REST Endpoints**: Plural nouns (`/api/users`, `/api/documents`)
- **Service Classes**: `*Service` (e.g., `InusService`, `PolicyEnforcementService`)
- **Repository Classes**: `*Repository` (e.g., `InusRepository`)
- **DTO Classes**: `*DTO` (e.g., `DocumentMetadataDTO`)

### Error Handling
- **REST Responses**: Use standard HTTP status codes
  - 200 OK, 201 Created, 204 No Content
  - 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found
  - 500 Internal Server Error
- **Error Response Format**:
  ```json
  {
    "error": "ACCESS_DENIED",
    "message": "You do not have permission to access this document",
    "timestamp": "2025-10-12T10:30:00Z"
  }
  ```

### Logging
- **Levels**: ERROR (system failures), WARN (policy denials), INFO (access events), DEBUG (detailed flow)
- **Structured Logging**: Include contextual information (userId, requestId, clinicId)
- **No Sensitive Data**: Never log passwords, full CI numbers in plaintext, document content

### Performance Considerations
- **Database Queries**: Use indexes on CI, inusId, patientCI, documentType
- **Caching**: Cache policy evaluations (5-minute TTL), user profiles (15-minute TTL)
- **Pagination**: All list endpoints must support pagination (`?page=0&size=20`)
- **Connection Pooling**: Configure PostgreSQL connection pool (min=5, max=20)

## Common Development Tasks

### Adding a New REST Endpoint
1. Define DTO classes for request/response
2. Create service method in appropriate service class
3. Add JAX-RS resource class with `@Path`, `@GET/@POST/@PUT/@DELETE`
4. Implement authorization checks (validate JWT claims)
5. Add unit tests (mock service layer)
6. Add integration tests (test full request/response cycle)
7. Update API documentation (OpenAPI/Swagger)

### Adding a New Access Policy Type
1. Create policy configuration DTO
2. Implement `AccessPolicy` interface with `evaluate()` method
3. Register policy evaluator in `PolicyEngine`
4. Add database migration for policy configuration schema
5. Update patient portal UI to configure new policy type
6. Add unit tests for policy evaluation logic

### Integrating a New External System
1. Define adapter interface (e.g., `TerminologyServiceAdapter`)
2. Implement adapter with external API client (REST/SOAP)
3. Add configuration properties (endpoint URL, API key)
4. Implement circuit breaker for resilience (e.g., Hystrix)
5. Add integration tests with mock server (WireMock)

## Troubleshooting

### Common Issues

**Issue**: "Database connection pool exhausted"
- **Cause**: Too many concurrent requests, long-running queries
- **Solution**: Increase pool size, optimize slow queries, add connection timeout

**Issue**: "JWT token expired"
- **Cause**: Token TTL too short, clock skew between services
- **Solution**: Increase TTL to 1 hour, implement token refresh, sync clocks (NTP)

**Issue**: "CORS errors from mobile app"
- **Cause**: Missing CORS headers in REST responses
- **Solution**: Configure CORS filter in WildFly or JAX-RS application

**Issue**: "Policy evaluation slow"
- **Cause**: Complex policy rules, no caching
- **Solution**: Cache policy decisions (5-minute TTL), optimize policy query

**Issue**: "Peripheral node can't register documents in RNDC"
- **Cause**: Authentication failure, network connectivity, invalid payload
- **Solution**: Verify API key, check HTTPS certificate, validate JSON schema

### Useful Commands

```bash
# Check WildFly logs
tail -f wildfly/standalone/log/server.log

# Check PostgreSQL connections
psql -U hcen -d hcen -c "SELECT count(*) FROM pg_stat_activity;"

# Check Redis cache
redis-cli
> KEYS *
> GET policy:cache:12345678:CARDIOLOGY

# Monitor HTTP requests (development)
tcpdump -i any -A -s 0 'tcp port 8080 and (((ip[2:2] - ((ip[0]&0xf)<<2)) - ((tcp[12]&0xf0)>>2)) != 0)'

# Generate test coverage report
./gradlew test jacocoTestReport
# Open build/reports/jacoco/test/html/index.html
```

## Team Information

**Group 9 - TSE 2025**
- German Rodao (4.796.608-7)
- Agustin Silvano (5.096.964-8)
- Piero Santos (6.614.312-9)

## Additional Resources

- **Problem Statement**: `docs/arquitectura-grupo9-tse.pdf`
- **Architecture Diagram**: See architecture document
- **CI/CD Pipeline**: `.gitlab-ci.yml`
- **WildFly Documentation**: https://docs.wildfly.org/
- **Jakarta EE Specifications**: https://jakarta.ee/specifications/
- **React Native Docs**: https://reactnative.dev/docs/getting-started
- **Firebase Setup**: https://firebase.google.com/docs/admin/setup

---

**Last Updated**: 2025-10-12
