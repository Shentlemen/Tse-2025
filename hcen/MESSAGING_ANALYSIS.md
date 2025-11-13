# HCEN Messaging and Integration Architecture Analysis

## EXECUTIVE SUMMARY

The HCEN Central component **DOES NOT** have any JMS/message broker infrastructure.
All communications use **SYNCHRONOUS REST API calls** over HTTPS.


## KEY FINDINGS

### 1. Current Messaging Infrastructure: NONE

**What IS Implemented:**
- REST API endpoints (JAX-RS) for user and document registration
- HTTP clients with retry logic and circuit breaker pattern
- Synchronous database persistence (PostgreSQL)
- Caching with Redis
- Audit logging with MongoDB

**What IS NOT Implemented:**
- JMS (Java Message Service)
- Message brokers (ActiveMQ, RabbitMQ, Kafka)
- Message-Driven Beans (MDBs)
- Asynchronous event processing
- Persistent message queues
- Topic/queue definitions in WildFly

### 2. How Peripheral Nodes Currently Register Data

**User Registration (AC013):**
```
POST /api/inus/users (REST endpoint)
  -> InusResource.registerUser()
  -> InusService.registerUser()
  -> Validate CI, check duplicates
  -> Call PDI SOAP service (age verification)
  -> InusRepository.save() to PostgreSQL
  -> Return 201 Created immediately
Response time: ~100-200ms
Synchronous: Caller waits
Idempotent: Duplicate registrations return existing user
```

**Document Registration (AC014):**
```
POST /api/rndc/documents (REST endpoint)
  -> RndcResource.registerDocument()
  -> RndcService.registerDocument()
  -> Validate locator URL (must be HTTPS)
  -> Validate hash format (sha256:...)
  -> Check for duplicates
  -> RndcRepository.save() to PostgreSQL
  -> Return 201 Created immediately
Response time: ~100-300ms
Synchronous: Caller waits
Idempotent: Unique constraint on document_locator
```

### 3. Database Tables

**INUS Users (inus.inus_users):**
- pk: ci (Cédula de Identidad - national ID)
- inus_id (UUID, unique cross-clinic identifier)
- first_name, last_name, date_of_birth
- email, phone_number (optional)
- status (ACTIVE, INACTIVE, SUSPENDED)
- age_verified (PDI verification flag)
- created_at, updated_at
- Indexes: ci, inus_id, status, email, created_at

**RNDC Documents (rndc.rndc_documents):**
- pk: id (BIGSERIAL)
- patient_ci (references INUS)
- document_locator (UNIQUE, URL to document)
- document_hash (SHA-256)
- document_type (18 types: CLINICAL_NOTE, LAB_RESULT, etc.)
- created_by, clinic_id
- status (ACTIVE, INACTIVE, DELETED - soft delete)
- document_title, document_description (optional)
- created_at
- Indexes: patient_ci, clinic_id, document_type, patient_ci+status, patient_ci+type+status

### 4. External Integration Clients

**PeripheralNodeClient:**
- Purpose: Communicate with peripheral nodes
- Features: 
  * HTTP timeouts: 5s connection, 30s read
  * Retry: 3 attempts with exponential backoff (1s, 2s, 4s)
  * Circuit breaker: 5 failures threshold, 60s reset
  * HTTPS mandatory, SHA-256 verification
- Methods:
  * sendOnboardingData() -> POST /api/onboard
  * retrieveDocument() -> GET {documentLocator}

**ClinicServiceClient:**
- Purpose: Register clinics
- Features:
  * HTTP timeouts: 10s connection, 30s read
  * Retry: 3 attempts with backoff
  * Special handling: no retry for 400/409 errors
- Methods:
  * registerClinic() -> POST /api/clinics

### 5. Business Logic Services

**InusService (@Stateless EJB):**
- registerUser() - Validate CI, generate INUS ID, check PDI, persist
- findUserByCi() - Query + cache (15min TTL)
- updateUserProfile() - Allow mutable fields only
- validateUserEligibility() - Check age verified + active status
- Idempotent: Same registration returns existing user

**RndcService (@Stateless EJB):**
- registerDocument() - Validate locator/hash, check duplicate, persist
- searchDocuments() - Paginated search with filters
- getPatientDocuments() - Get docs for patient
- updateDocumentStatus() - Mark as inactive/deleted
- Idempotent: Same locator returns existing document

### 6. REST API Endpoints

**INUS Endpoints:**
- POST /api/inus/users - Register user
- GET /api/inus/users/{ci} - Get user by CI
- PUT /api/inus/users/{ci} - Update user profile
- GET /api/inus/users - List/search users (paginated)
- GET /api/inus/users/{ci}/validate - Check eligibility

**RNDC Endpoints:**
- POST /api/rndc/documents - Register document
- GET /api/rndc/documents/{id} - Get document metadata
- GET /api/rndc/documents - Search documents (filters, pagination)
- GET /api/rndc/patients/{patientCi}/documents - Patient's docs
- PATCH /api/rndc/documents/{id}/status - Update status
- GET /api/rndc/documents/{id}/verify - Verify hash

### 7. Build Dependencies (NO JMS)

Included:
- Jakarta EE 11.0.0 (JAX-RS, JPA, CDI, EJB)
- PostgreSQL JDBC 42.7.1
- MongoDB driver 4.11.1
- Apache HTTP Client 5.3
- Redis (Jedis) 5.1.0
- JWT (JJWT) 0.12.3
- Jackson JSON 2.17.0
- Flyway 10.0.0

NOT Included:
- jakarta.jms-api
- ActiveMQ, RabbitMQ, Kafka clients

### 8. Synchronous Architecture - Pros/Cons

**PROS:**
- Simple request-response model
- Immediate feedback to caller
- Idempotent by design (safe to retry)
- Built-in HTTP retry logic
- No additional infrastructure needed

**CONS:**
- Blocking calls (caller waits)
- Cascading failures (circuit breaker helps, not solves)
- Tight coupling between systems
- Difficult to replay failed operations
- No guaranteed delivery if response lost

## RECOMMENDATIONS

**Current State:** Production-ready with synchronous REST

**When to Add Message Queues:**
1. Audit logging (low risk) - MongoDB writes could be async
2. User registration events (medium risk) - Fire-and-forget after persistence
3. Document registration events (medium risk) - Async verification/indexing
4. Peripheral node sync (higher risk) - Push updates asynchronously

**When to Keep Synchronous:**
1. Clinic onboarding (AC016) - needs immediate feedback
2. Document retrieval (AC015) - user waiting
3. Policy evaluation - real-time access control

**Recommended Message Broker:**
- ActiveMQ Artemis (embedded, no external dependency)
- Or RabbitMQ (modern, requires external service)

**Implementation Path:**
Phase 1: Async audit logging (no API changes)
Phase 2: User registration events (with status tracking)
Phase 3: Document registration events (with status tracking)
Phase 4: Peripheral node synchronization (with callbacks)

## KEY FILES

Core Services:
- src/main/java/uy/gub/hcen/service/inus/InusService.java
- src/main/java/uy/gub/hcen/service/rndc/RndcService.java

REST Endpoints:
- src/main/java/uy/gub/hcen/inus/api/rest/InusResource.java
- src/main/java/uy/gub/hcen/rndc/api/rest/RndcResource.java

Entity Models:
- src/main/java/uy/gub/hcen/inus/entity/InusUser.java
- src/main/java/uy/gub/hcen/rndc/entity/RndcDocument.java

Integration Clients:
- src/main/java/uy/gub/hcen/integration/peripheral/PeripheralNodeClient.java
- src/main/java/uy/gub/hcen/integration/clinic/ClinicServiceClient.java

Configuration:
- src/main/resources/META-INF/persistence.xml
- src/main/resources/application.properties
- build.gradle

Database Migrations:
- src/main/resources/db/migration/V001__create_inus_schema.sql
- src/main/resources/db/migration/V002__create_rndc_schema.sql
