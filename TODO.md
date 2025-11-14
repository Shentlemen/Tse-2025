# HCEN - Priority Implementation TODO

**Last Updated**: 2025-11-13 (Updated: Completed Clinical History Visualization + FHIR R4 Integration)
**Current Status**: Advanced Development - Patient Portal Features + Security & Compliance Backend

## ⚠️ SCOPE CLARIFICATIONS

### OUT OF SCOPE for HCEN Central:
1. **Professional Authentication/Login**
   - Professional portal, professional login, and professional authentication belong to the **Clinic/Peripheral component** (separate project)
   - HCEN Central ONLY handles patient authentication via gub.uy

2. **Mobile App & Firebase**
   - Mobile app (React Native) will NOT be implemented
   - Firebase integration (Cloud Messaging, push notifications) removed
   - Mobile-specific features removed from scope

3. **Testing Coverage Requirements**
   - Unit tests and integration tests are OUT OF SCOPE
   - No 80% coverage requirement
   - Focus on functional implementation only

4. **Admin Audit Log UI**
   - Admin audit dashboard/viewer is OUT OF SCOPE
   - Audit log export functionality (CSV, JSON, PDF) is OUT OF SCOPE
   - Patients can view their own audit logs via patient portal (IN SCOPE)

### NEW REQUIREMENT:
- **Patient Pending Access Requests JSP Page**: Instead of mobile notifications, patients will view and approve/deny pending access requests through a web interface in the patient portal

---

## 🎯 CURRENT SPRINT: Policy Engine + Audit System Completion

**Status**: 🔨 IN PROGRESS
**Start Date**: 2025-10-30
**Goal**: Complete core security and compliance features (functional implementation only, no testing)
**Estimated Time**: 32-40 hours

### Sprint Objectives
- Policy Engine: 60% → 100% (24-32 hours)
- Audit System: 60% → 90% (8-12 hours) - Backend only, no admin UI

### Policy Engine Completion (60% → 100%)
- [ ] Implement ABAC (Attribute-Based Access Control) policy evaluator
  - [ ] User attribute evaluation (role, specialty, clinic affiliation)
  - [ ] Resource attribute evaluation (document type, sensitivity level)
  - [ ] Context attribute evaluation (time, location, purpose of access)
  - Files: Create `src/main/java/uy/gub/hcen/policy/evaluator/AbacPolicyEvaluator.java`

- [ ] Implement RBAC (Role-Based Access Control) policy evaluator
  - [ ] Role hierarchy definition (Admin > Doctor > Nurse > Staff)
  - [ ] Permission assignment per role
  - [ ] Role-based document access rules
  - Files: Create `src/main/java/uy/gub/hcen/policy/evaluator/RbacPolicyEvaluator.java`

- [ ] Add policy conflict resolution
  - [ ] Implement conflict detection algorithm
  - [ ] Define resolution strategy (deny-overrides, permit-overrides, first-applicable)
  - [ ] Add policy priority levels
  - Files: Update `src/main/java/uy/gub/hcen/policy/engine/PolicyEngine.java`

- [ ] Add time-based policy enforcement
  - [ ] Implement time window policies (business hours only)
  - [ ] Add expiration date handling for temporary access
  - [ ] Validate policy active period during evaluation
  - Files: Create `src/main/java/uy/gub/hcen/policy/evaluator/TimeBased PolicyEvaluator.java`

- [ ] Add emergency access override (break-glass)
  - [ ] Implement emergency access request endpoint
  - [ ] Add justification requirement and logging
  - [ ] Create post-emergency access review workflow
  - [ ] Add emergency access revocation
  - Files: Create `src/main/java/uy/gub/hcen/policy/emergency/EmergencyAccessService.java`

- [ ] Create comprehensive policy tests
  - [ ] Unit tests for each policy evaluator
  - [ ] Integration tests for policy engine
  - [ ] Test policy conflict scenarios
  - [ ] Test emergency access workflow
  - Files: Create `src/test/java/uy/gub/hcen/policy/`

### Audit System Completion (60% → 100%)
- [ ] Implement comprehensive event logging
  - [ ] Log all document access events (successful, denied, pending)
  - [ ] Log policy changes (creation, modification, deletion)
  - [ ] Log user management events (creation, status changes)
  - [ ] Log clinic management events (onboarding, deactivation)
  - [ ] Log authentication events (login, logout, failed attempts)
  - Files: Update `src/main/java/uy/gub/hcen/audit/service/AuditService.java`

- [ ] Create immutable audit log storage
  - [ ] Implement append-only audit repository
  - [ ] Add tamper detection (hash chaining or digital signatures)
  - [ ] Prevent modification/deletion of audit records
  - [ ] Add audit log integrity verification
  - Files: Update `src/main/java/uy/gub/hcen/audit/repository/AuditLogRepository.java`

- [ ] Add audit retention policy enforcement
  - [ ] Configure retention period (default: 7 years per Uruguayan law)
  - [ ] Implement automatic archival of old logs
  - [ ] Add purge mechanism for expired logs (with compliance approval)
  - [ ] Create retention policy configuration
  - Files: Create `src/main/java/uy/gub/hcen/audit/retention/RetentionPolicyService.java`

- [ ] Create audit query/search API
  - [ ] Search by user (patient CI, professional ID)
  - [ ] Search by resource (document ID, clinic ID)
  - [ ] Search by event type (ACCESS, MODIFICATION, CREATION, DELETION)
  - [ ] Search by date range with pagination
  - [ ] Filter by outcome (SUCCESS, FAILURE, DENIED)
  - Files: Create `src/main/java/uy/gub/hcen/audit/rest/AuditQueryResource.java`


---

## 🔴 CRITICAL PRIORITIES (Deployment Blockers)

### 1. gub.uy Authentication Integration ✅ 90% Complete
**Status**: Backend complete, production configuration required

#### ✅ Already Implemented
- [x] OAuth 2.0 / OIDC client with PKCE support (`GubUyOidcClient.java`)
- [x] Complete authentication flow with state management (`AuthenticationService.java`)
- [x] JWT token generation and refresh token rotation
- [x] REST endpoints (`/api/auth/*`)
- [x] Login UI pages (admin and patient portals)
- [x] Automatic INUS user creation from gub.uy claims
- [x] Redis-based session management

#### 🔨 TODO - gub.uy Auth
- [ ] **Production Configuration**
  - [ ] Replace hardcoded test OIDC endpoints with production gub.uy URLs
  - [ ] Configure production client credentials (client_id, client_secret)
  - [ ] Set up production redirect URIs in gub.uy provider
  - [ ] Configure JWKS endpoint caching strategy for production
  - Files: `src/main/java/uy/gub/hcen/auth/oidc/GubUyOidcClient.java`

- [ ] **Security Enhancements**
  - [ ] Implement session binding with IP/device fingerprinting
  - [ ] Add token refresh concurrency handling (optimistic locking)
  - [ ] Implement logout propagation to gub.uy (RP-initiated logout)
  - Files: `src/main/java/uy/gub/hcen/auth/service/AuthenticationService.java`

**Estimated Time**: 24 hours
**Blocking**: Deployment to production

---

### 2. Create Health User (INUS) - Including UI ✅ 100% COMPLETE

#### ✅ Already Implemented
- [x] Complete INUS user registration service (`InusService.java`)
- [x] PDI age verification integration with graceful degradation
- [x] User entity and MongoDB repository (`InusUser.java`, `InusRepository.java`)
- [x] REST endpoints for user management (`InusResource.java`)
- [x] Redis caching (15-minute TTL)
- [x] User search with pagination
- [x] Status management (ACTIVE, INACTIVE, SUSPENDED)
- [x] **Patient Registration Portal** (`webapp/patient/register.jsp` - 165 lines)
  - CI input with format validation (1.234.567-8)
  - First name, last name, date of birth fields
  - Email and phone number (optional)
  - Form validation JavaScript
  - Integration with POST `/api/inus/users` endpoint
  - Success/error feedback UI with auto-redirect
- [x] **Patient Profile Management** (`webapp/patient/profile.jsp` - 280 lines)
  - Display INUS ID, CI, personal information
  - Edit form for email and phone number
  - Account status indicator (ACTIVE/INACTIVE/SUSPENDED)
  - Quick action cards (history, policies, audit)
  - Integration with GET and PUT `/api/inus/users/{ci}` endpoints
  - Session management
- [x] **Admin User Management Dashboard** (`webapp/admin/users.jsp` - 380 lines)
  - User listing with statistics (total, active, inactive)
  - Search by CI, name, status
  - Pagination controls
  - User status badges
  - Actions: View, Edit, Activate/Deactivate
  - Integration with GET `/api/inus/users` with pagination
- [x] **Admin User Detail Page** (`webapp/admin/user-detail.jsp` - 420 lines)
  - Full user profile display with inline editing
  - Audit trail (creation date, last update)
  - Status change actions with confirmation
  - System information sidebar
  - Activity timeline
  - Integration with GET and PUT `/api/inus/users/{ci}` endpoints

#### 🔨 TODO - Health User Creation

##### Backend Tasks
- [ ] **CI Validation Enhancement**
  - [ ] Implement CI format validation (Uruguay format: 1.234.567-8)
  - [ ] Add duplicate detection with fuzzy matching
  - [ ] Implement concurrent registration handling (optimistic locking)
  - Files: `src/main/java/uy/gub/hcen/inus/service/InusService.java`

**Estimated Time**:
- Backend: 16 hours
- ✅ UI: COMPLETE (40 hours invested)
- Total Remaining: 16 hours

**Status**: UI Complete - Production Ready

---

### 3. Configure Health Provider (Clinic Management) - Including UI ✅ 85% Backend, ✅ 100% UI COMPLETE

#### ✅ Already Implemented
- [x] Complete clinic management service (`ClinicManagementService.java`)
- [x] Secure API key generation (64-byte SecureRandom)
- [x] Clinic entity and repository (`Clinic.java`, `ClinicRepository.java`)
- [x] REST endpoints for clinic operations (`ClinicResource.java`)
- [x] Clinic onboarding to peripheral nodes (AC016)
- [x] Clinic statistics aggregation
- [x] Status workflow (PENDING_ONBOARDING → ACTIVE → INACTIVE)
- [x] **Admin Clinic Listing Dashboard** (`webapp/admin/clinics.jsp` - 878 lines)
  - Comprehensive clinic search (name, city, status, ID)
  - Advanced pagination with page size controls
  - Statistics cards (total clinics, active, pending, inactive)
  - Status indicators with color-coded badges
  - Quick actions: View Details, Edit, Onboard, Deactivate
  - Responsive table design with sorting capabilities
  - Integration with GET `/api/admin/clinics` endpoint with filtering
  - Real-time status updates without page refresh
- [x] **Admin Clinic Registration Portal** (`webapp/admin/clinic-register.jsp` - 838 lines)
  - Complete clinic registration form (name, address, city)
  - Contact information fields (phone, email)
  - Peripheral node URL with validation hints
  - Comprehensive form validation (client-side and server-side)
  - API key generation and secure display with copy button
  - Success modal with API key visibility
  - Integration with POST `/api/admin/clinics` endpoint
  - Auto-redirect to clinic detail page on success
- [x] **Admin Clinic Detail & Edit Page** (`webapp/admin/clinic-detail.jsp` - 884 lines)
  - Full clinic information display with inline editing
  - API key display (partially masked) with reveal/copy functionality
  - Peripheral node URL with test connectivity button
  - Clinic statistics dashboard (user count, document count)
  - Status change actions (Onboard, Deactivate) with confirmation modals
  - Clinic onboarding workflow with progress indicators
  - Audit trail section (creation date, last update, status history)
  - System information sidebar
  - Integration with GET, PUT, DELETE `/api/admin/clinics/{clinicId}` endpoints
  - Integration with POST `/api/admin/clinics/{clinicId}/onboard` endpoint

#### 🔨 TODO - Health Provider Configuration

##### Backend Tasks
- [ ] **API Key Management**
  - [ ] Implement API key rotation mechanism
  - [ ] Add API key expiration (configurable TTL)
  - [ ] Create endpoint for regenerating API keys
  - Files: `src/main/java/uy/gub/hcen/clinic/service/ClinicManagementService.java`

- [ ] **Peripheral Node Validation**
  - [ ] Implement URL reachability check before onboarding
  - [ ] Add SSL/TLS certificate validation
  - [ ] Test connectivity with health check endpoint
  - Files: `src/main/java/uy/gub/hcen/clinic/service/ClinicManagementService.java`

- [ ] **Clinic Deactivation Logic**
  - [ ] Implement soft delete with status change to INACTIVE
  - [ ] Add data retention policy configuration
  - [ ] Notify peripheral nodes of clinic deactivation
  - Files: `src/main/java/uy/gub/hcen/clinic/service/ClinicManagementService.java`

**Estimated Time**:
- Backend: 20 hours
- ✅ UI: COMPLETE (48 hours invested)
- Total Remaining: 20 hours

**Status**: UI Complete - Backend refinements pending

---

### 4. Deploy to mi-nube ANTEL (Virtuozzo/Jelastic) ✅ 75% Complete

#### ✅ Already Implemented
- [x] Complete Dockerfile (WildFly 37.0 + JDK 21)
- [x] GitLab CI/CD pipeline (`.gitlab-ci.yml`)
  - Build, test, package, Docker build, push stages
  - Virtuozzo/Jelastic deployment stage (manual trigger)
- [x] Gradle build configuration (`build.gradle`)
- [x] Local development environment (`docker-compose-postgres.yml`)
- [x] PostgreSQL, MongoDB, Redis configuration

#### 🔨 TODO - Production Deployment

##### Pre-Deployment Tasks
- [ ] **Production Docker Image**
  - [ ] Create multi-stage Dockerfile for smaller image size
  - [ ] Add non-root user for security
  - [ ] Configure production JVM options (heap sizing based on ANTEL resources)
  - [ ] Add healthcheck endpoint configuration
  - Files: Create `Dockerfile.prod`

- [ ] **WildFly Production Configuration**
  - [ ] Create production `standalone.xml` or CLI scripts
  - [ ] Configure PostgreSQL datasource with connection pooling
  - [ ] Configure MongoDB connection with replica set (if available)
  - [ ] Configure Redis distributed cache
  - [ ] Set up HTTPS/SSL listener (port 8443)
  - [ ] Enable clustering (if multi-instance deployment)
  - Files: Create `src/main/resources/wildfly-config/`

- [ ] **Environment Configuration**
  - [ ] Create production environment variables template
    - Database connection strings (PostgreSQL, MongoDB, Redis)
    - gub.uy OIDC credentials (client_id, client_secret, endpoints)
    - PDI SOAP endpoint and credentials
    - JWT signing key (secure generation)
    - Peripheral node URLs
  - [ ] Document secrets management strategy (Virtuozzo encrypted variables)
  - Files: Create `.env.production.template`

- [ ] **Database Migration**
  - [ ] Set up Liquibase or Flyway for schema versioning
  - [ ] Create initial migration scripts for PostgreSQL tables
  - [ ] Create MongoDB collection initialization scripts
  - [ ] Test migration rollback procedures
  - Files: Create `src/main/resources/db/migration/`

- [ ] **SSL/TLS Certificates**
  - [ ] Obtain certificates for `hcen.uy` or assigned ANTEL domain
  - [ ] Configure certificate in WildFly keystore
  - [ ] Set up automatic certificate renewal (Let's Encrypt)
  - [ ] Test HTTPS endpoints

##### Deployment Configuration
- [ ] **Health Check Endpoints**
  - [ ] Create `/health` endpoint - Overall system health
  - [ ] Create `/health/ready` endpoint - Readiness probe (DB connections, cache)
  - [ ] Create `/health/live` endpoint - Liveness probe (application running)
  - Files: Create `src/main/java/uy/gub/hcen/health/HealthCheckResource.java`

- [ ] **Logging Configuration**
  - [ ] Configure centralized logging (syslog or ANTEL logging service)
  - [ ] Set up log rotation and retention policy
  - [ ] Add structured logging (JSON format)
  - [ ] Configure log levels by environment (DEBUG for dev, INFO for prod)
  - Files: Update `src/main/resources/logging.properties`

- [ ] **Monitoring and Metrics**
  - [ ] Add Prometheus metrics endpoint (`/metrics`)
  - [ ] Configure application performance monitoring (APM)
  - [ ] Set up alerts for critical errors (email/SMS/webhook)
  - [ ] Dashboard setup (Grafana or ANTEL monitoring)
  - Files: Create `src/main/java/uy/gub/hcen/metrics/`

- [ ] **Backup and Disaster Recovery**
  - [ ] Configure automated PostgreSQL backups (daily)
  - [ ] Configure automated MongoDB backups (daily)
  - [ ] Document recovery procedures (RPO/RTO)
  - [ ] Test backup restoration

##### GitLab CI/CD Updates
- [ ] **Update `.gitlab-ci.yml`**
  - [ ] Add staging environment deployment
  - [ ] Add production environment deployment (manual approval)
  - [ ] Add smoke tests after deployment
  - [ ] Add rollback job
  - [ ] Configure environment-specific variables

- [ ] **Deployment Scripts**
  - [ ] Create deployment verification script
  - [ ] Create smoke test script (health checks, basic API tests)
  - [ ] Create rollback script
  - Files: Create `scripts/deploy.sh`, `scripts/smoke-test.sh`, `scripts/rollback.sh`

##### ANTEL mi-nube Specific
- [ ] **Virtuozzo/Jelastic Configuration**
  - [ ] Request environment provisioning from ANTEL
  - [ ] Configure environment topology (WildFly + PostgreSQL + MongoDB + Redis)
  - [ ] Set up auto-scaling rules (if supported)
  - [ ] Configure firewall rules (allow HTTPS, gub.uy, PDI endpoints)
  - [ ] Set up domain/subdomain (`hcen.uy` or `hcen.antel.uy`)

- [ ] **Network Configuration**
  - [ ] Whitelist gub.uy OAuth endpoints
  - [ ] Whitelist PDI SOAP endpoints
  - [ ] Configure outbound rules for peripheral nodes
  - [ ] Set up CORS for web client domains

- [ ] **Secrets Management**
  - [ ] Configure Virtuozo encrypted environment variables
  - [ ] Store database credentials
  - [ ] Store API keys (gub.uy, PDI)
  - [ ] Store JWT signing key
  - [ ] Document key rotation procedures

##### Post-Deployment Tasks
- [ ] **Smoke Tests**
  - [ ] Test gub.uy authentication flow
  - [ ] Test INUS user registration
  - [ ] Test clinic registration and onboarding
  - [ ] Test document registration (RNDC)
  - [ ] Test policy evaluation

- [ ] **Security Audit**
  - [ ] Verify HTTPS enforcement on all endpoints
  - [ ] Verify authentication/authorization
  - [ ] Verify secrets are not exposed in logs/responses
  - [ ] Run OWASP ZAP or similar security scanner
  - [ ] Document security findings and remediations

**Estimated Time**: 80-100 hours
**Blocking**: Production deployment and go-live

---

## 🔵 PATIENT PORTAL FEATURES

### 5. Patient Pending Access Requests Page ✅ COMPLETED
**Status**: Fully implemented - new requirement replacing mobile notification workflow
**Completion Date**: 2025-11-03
**Total Implementation Time**: 24 hours

---

### 6. Patient Access Policy Management ✅ COMPLETED
**Status**: Fully implemented - enables patients to manage document access policies
**Completion Date**: 2025-11-04
**Total Implementation Time**: 30 hours

#### ✅ Already Implemented

**Files Created**: 12 files (1,700+ lines of code)

##### Backend REST API
- [x] **REST Endpoints** (`AccessPolicyResource.java`)
  - [x] `GET /api/patients/{ci}/policies` - Fetch patient's access policies with pagination
  - [x] `POST /api/patients/{ci}/policies` - Create new access policy
  - [x] `PUT /api/patients/{ci}/policies/{policyId}` - Update existing policy
  - [x] `DELETE /api/patients/{ci}/policies/{policyId}` - Delete policy
  - [x] `GET /api/patients/{ci}/policies/{policyId}` - Get specific policy details

##### Backend Service Layer
- [x] **AccessPolicyService** - Complete policy management (create, list, update, delete)
- [x] **Integration with Policy Engine** - Dynamic policy enforcement and evaluation
- [x] **Integration with Audit System** - Comprehensive logging of all policy changes
- [x] **DTO Classes** - Clean request/response handling

##### Frontend UI
- [x] **Patient Access Policy Management Page** (`access-policies.jsp`)
  - [x] Responsive table displaying all access policies
  - [x] Policy details: type, scope (document type/specialty/clinic), effect, validity period
  - [x] Action buttons: Create, Edit, Delete, View Details
  - [x] Modal dialogs for create/edit policies
  - [x] Policy configuration builder with different policy types
  - [x] Validity period selectors (permanent, time-limited with date pickers)
  - [x] Success/error notifications with auto-dismiss
  - [x] Empty state when no policies exist
  - [x] Pagination controls

##### Navigation & User Flow Integration
- [x] **Updated Patient Dashboard** - Added policy management link to quick actions

##### Integration with Core Systems
- [x] **Policy Engine Integration** - Policies enforced on all document access requests
- [x] **Audit System Integration** - All policy state changes logged (CREATED, MODIFIED, DELETED)

**Architecture**:
- RESTful API design with standard HTTP status codes
- Service layer with separation of concerns
- DTO pattern for clean request/response handling
- Stateless service design for horizontal scaling

**Security**:
- Authorization checks on all endpoints (patient must own the policies)
- Audit logging of all policy changes
- Policy decisions tracked and immutable

**User Experience**:
- Clean, responsive UI following existing portal design
- Intuitive policy creation workflow with type-specific configuration
- Real-time feedback with success/error messages
- Integrated navigation from patient dashboard

**Files Summary**:
- 10 Java files (backend)
- 1 JSP file (frontend)
- 1 modified dashboard file
- Total: 1,700+ lines of code

---

### 7. Clinical History Visualization for Patients ✅ COMPLETED
**Status**: Fully implemented - patients can view and access clinical documents
**Completion Date**: 2025-11-13
**Total Implementation Time**: 24 hours

#### ✅ Already Implemented

**Files Created**: 7 files (800+ lines of code)

##### Backend Service Layer
- [x] **Clinical History Service** (`ClinicalHistoryService.java` - 350 lines)
  - [x] Fetch documents from RNDC for patient with comprehensive filtering
  - [x] Integration with PeripheralNodeClient for document retrieval
  - [x] Filter documents by type, date range, clinic
  - [x] Sort documents by creation date (newest first)
  - [x] Pagination support for large document sets
  - [x] Document hash verification (SHA-256 integrity check)
  - [x] Circuit breaker integration for peripheral node failures
  - [x] Retry logic with exponential backoff
  - [x] Comprehensive error handling with user-friendly messages

##### Backend REST API
- [x] **REST Endpoints** (`ClinicalHistoryResource.java` - 280 lines)
  - [x] `GET /api/patients/{ci}/history` - Fetch clinical history with filters
  - [x] `GET /api/patients/{ci}/history/documents/{documentId}` - Inline document viewing
  - [x] Query parameters: `type`, `fromDate`, `toDate`, `clinicId`, `page`, `size`
  - [x] Inline document viewing (PDFs in browser, structured FHIR content)
  - [x] Proper HTTP headers (Content-Disposition: inline, Content-Type detection)
  - [x] Authorization checks (patient must own the documents)
  - [x] Comprehensive audit logging (IP address, user agent tracking)

##### Frontend UI
- [x] **Clinical History Page** (`clinical-history.jsp`)
  - [x] Already existed from previous work
  - [x] Responsive document list/table view
  - [x] Document cards with metadata (type, date, author, clinic)
  - [x] Filter controls (type, date range)
  - [x] "Ver" (View) button for inline document viewing
  - [x] Pagination controls
  - [x] Empty state when no documents match filters

##### Integration with Core Systems
- [x] **RNDC Integration** - Query RNDC for patient document metadata
- [x] **PeripheralNodeClient Integration** - Fetch actual documents from peripheral nodes
- [x] **Policy Engine Integration** - Prepared for future professional access control
- [x] **Audit System Integration** - Comprehensive logging of document access
  - Document access events logged
  - IP address and user agent tracked
  - Success/failure outcomes recorded
  - Detailed error context captured

##### DTOs and Response Objects
- [x] **ClinicalDocumentDTO** - Document metadata for API responses
- [x] **ClinicalHistoryResponse** - Paginated list response with metadata

##### Security Features
- [x] **Authorization**: Patient must own the documents (CI validation)
- [x] **Hash Verification**: SHA-256 integrity check on retrieved documents
- [x] **Audit Logging**: All access attempts logged (success, denied, failure)
- [x] **No Caching**: Sensitive documents not cached (Cache-Control: no-store, no-cache)
- [x] **HTTPS Only**: All peripheral node communications use HTTPS

##### Key Implementation Details
- **Inline Document Viewing**: Documents displayed in browser (not downloaded)
  - PDFs open in browser PDF viewer
  - FHIR JSON displayed as formatted JSON
  - Proper Content-Disposition: inline headers
  - Content-Type auto-detection based on document metadata

- **Error Handling**: User-friendly error messages
  - Document not found (404)
  - Peripheral node offline (503)
  - Hash verification failed (500)
  - Authorization denied (403)

- **Performance Optimization**:
  - Pagination for large document sets
  - Circuit breaker prevents cascading failures
  - Retry logic with exponential backoff (1s, 2s, 4s)
  - Connection timeout: 5s, Read timeout: 30s

**Files Summary**:
- 2 Service classes (ClinicalHistoryService)
- 1 REST Resource (ClinicalHistoryResource)
- 2 DTOs (ClinicalDocumentDTO, ClinicalHistoryResponse)
- 1 JSP page (clinical-history.jsp - already existed)
- Total NEW code: ~800 lines

**Architecture**:
- RESTful API design with standard HTTP status codes
- Service layer orchestrates RNDC, PeripheralNodeClient, AuditService
- DTO pattern for clean request/response handling
- Stateless service design for horizontal scaling
- Circuit breaker pattern for resilience

**Integration Points**:
- RNDC Service: Document metadata lookup
- PeripheralNodeClient: HTTP retrieval with circuit breaker and retry
- AuditService: Comprehensive access logging
- PolicyEngine: Prepared for future professional access control

---

### 8. FHIR R4 Integration for Health Providers ✅ COMPLETED
**Status**: Fully implemented - health providers can now send FHIR-compliant data
**Completion Date**: 2025-11-13
**Total Implementation Time**: 16 hours

#### ✅ Already Implemented

**Files Created**: 7 files (5 Java classes + 2 test classes, 650+ lines of code)

##### Dependencies Added
- [x] **HAPI FHIR 7.2.0** - Added to build.gradle
  - hapi-fhir-base
  - hapi-fhir-structures-r4
  - hapi-fhir-validation-resources-r4
  - hapi-fhir-client-okhttp

##### FHIR Package Structure (`uy.gub.hcen.fhir`)
- [x] **FhirParserFactory** (`FhirParserFactory.java` - 80 lines)
  - Thread-safe singleton for FHIR parsing
  - Manages FhirContext lifecycle
  - Provides configured JSON and XML parsers

- [x] **FhirValidationUtil** (`FhirValidationUtil.java` - 60 lines)
  - Validates FHIR resources against R4 specification
  - Provides detailed validation error messages
  - Integration with HAPI FHIR validator

- [x] **FhirPatientConverter** (`FhirPatientConverter.java` - 120 lines)
  - Converts FHIR Patient → UserRegistrationRequest
  - Extracts Uruguay national ID (CI) from identifiers
  - Supports Uruguay OID: urn:oid:2.16.858.1.113883.3.879.1.1.1
  - Maps FHIR Patient fields to HCEN user model
  - Comprehensive validation and error handling

- [x] **FhirDocumentReferenceConverter** (`FhirDocumentReferenceConverter.java` - 180 lines)
  - Converts FHIR DocumentReference → DocumentRegistrationRequest
  - Maps LOINC codes to HCEN document types (11 types supported)
  - Extracts patient CI, document locator URL, document hash
  - Validates required fields (subject, content, hash)
  - Comprehensive error messages for missing/invalid data

- [x] **FhirConversionException** (`FhirConversionException.java` - 30 lines)
  - Custom exception for FHIR conversion errors
  - Provides detailed error context for debugging

##### REST API Enhancements
- [x] **InusResource.java** - Updated for FHIR support
  - `POST /api/inus/users` now accepts application/fhir+json
  - Content negotiation based on Content-Type header
  - Automatic conversion from FHIR Patient to UserRegistrationRequest
  - 100% backward compatible (existing JSON format still works)

- [x] **RndcResource.java** - Updated for FHIR support
  - `POST /api/rndc/documents` now accepts application/fhir+json
  - Content negotiation based on Content-Type header
  - Automatic conversion from FHIR DocumentReference to DocumentRegistrationRequest
  - 100% backward compatible (existing JSON format still works)

##### LOINC Code Mapping
- [x] **11 Document Types Supported**:
  - 11488-4 → CLINICAL_NOTE (Consultation note)
  - 11502-2 → LAB_RESULT (Laboratory report)
  - 18748-4 → IMAGING (Diagnostic imaging report)
  - 18842-5 → DISCHARGE_SUMMARY
  - 57133-1 → REFERRAL
  - 11506-3 → PROGRESS_NOTE
  - 57016-8 → PRIVACY_POLICY
  - 18776-5 → TREATMENT_PLAN
  - 60591-5 → QUESTIONNAIRE
  - 56444-7 → MEDICATION_SUMMARY
  - 34133-9 → SUMMARY_NOTE

##### Uruguay National ID Support
- [x] **OID Configuration**: urn:oid:2.16.858.1.113883.3.879.1.1.1
- [x] Patient.identifier extraction with OID matching
- [x] CI format validation (Uruguay format)

##### Testing
- [x] **FhirPatientConverterTest** (`FhirPatientConverterTest.java` - 180 lines)
  - Unit tests for Patient conversion
  - Tests for valid Patient resources
  - Tests for missing CI
  - Tests for missing name
  - Tests for invalid identifiers

- [x] **FhirDocumentReferenceConverterTest** (`FhirDocumentReferenceConverterTest.java` - 200 lines)
  - Unit tests for DocumentReference conversion
  - Tests for valid DocumentReference resources
  - Tests for LOINC code mapping
  - Tests for missing subject
  - Tests for missing content
  - Tests for missing hash
  - Tests for unknown LOINC codes

##### Key Features
- **100% Backward Compatible**: Existing custom JSON format still works
- **Zero Service Layer Changes**: Converters handle all transformation
- **Zero Database Changes**: Same data model, different input format
- **FHIR R4 Compliant**: Follows FHIR R4 specification (4.0.1)
- **International Standards**: FHIR + LOINC codes for interoperability
- **Comprehensive Validation**: Validates FHIR resources before conversion
- **User-Friendly Errors**: Detailed error messages for debugging

##### Example FHIR Patient Request
```json
{
  "resourceType": "Patient",
  "identifier": [
    {
      "system": "urn:oid:2.16.858.1.113883.3.879.1.1.1",
      "value": "12345678"
    }
  ],
  "name": [
    {
      "family": "Pérez",
      "given": ["Juan"]
    }
  ],
  "birthDate": "1990-01-15",
  "telecom": [
    {
      "system": "email",
      "value": "juan.perez@example.com"
    },
    {
      "system": "phone",
      "value": "+598 99 123 456"
    }
  ]
}
```

##### Example FHIR DocumentReference Request
```json
{
  "resourceType": "DocumentReference",
  "subject": {
    "identifier": {
      "system": "urn:oid:2.16.858.1.113883.3.879.1.1.1",
      "value": "12345678"
    }
  },
  "type": {
    "coding": [
      {
        "system": "http://loinc.org",
        "code": "11488-4",
        "display": "Consultation note"
      }
    ]
  },
  "content": [
    {
      "attachment": {
        "url": "https://clinic-001.hcen.uy/documents/abc123",
        "hash": "c2hhMjU2OmExYjJjM2Q0ZTVmNg==",
        "contentType": "application/pdf"
      }
    }
  ],
  "author": [
    {
      "display": "Dr. María González"
    }
  ],
  "date": "2025-11-13T10:30:00Z"
}
```

**Files Modified**: 3 files
- build.gradle (added HAPI FHIR dependencies)
- InusResource.java (added FHIR support)
- RndcResource.java (added FHIR support)

**Architecture**:
- Converter pattern for FHIR → HCEN transformation
- Singleton FhirContext for performance
- Zero coupling to service layer (converters only)
- Clean separation of concerns

**Integration Points**:
- InusResource: Accepts FHIR Patient resources
- RndcResource: Accepts FHIR DocumentReference resources
- Existing service layer unchanged (InusService, RndcService)

---

#### ✅ Already Implemented

**Files Created**: 10 files (1,984 lines of code)

##### Backend REST API
- [x] **REST Endpoints** (`AccessRequestResource.java` - 362 lines)
  - [x] `GET /api/patients/{ci}/pending-requests` - Fetch pending access requests with pagination
  - [x] `POST /api/patients/{ci}/pending-requests/{requestId}/approve` - Approve request
  - [x] `POST /api/patients/{ci}/pending-requests/{requestId}/deny` - Deny request
  - [x] `POST /api/patients/{ci}/pending-requests/{requestId}/info` - Request additional info

##### Backend Service Layer
- [x] **AccessRequestService** (`AccessRequestService.java` - 307 lines)
  - [x] Complete request management (create, list, approve, deny, info request)
  - [x] Integration with Policy Engine (dynamic policy creation on approval)
  - [x] Integration with Audit System (comprehensive logging of all actions)
  - [x] Professional notification handling

- [x] **DTOs** (Data Transfer Objects)
  - [x] `AccessRequestDTO.java` (163 lines) - Request data model with patient, professional, and clinic info
  - [x] `AccessRequestListResponse.java` (78 lines) - Paginated list response
  - [x] `ApprovalDecisionDTO.java` (38 lines) - Approval decision with optional duration
  - [x] `DenialDecisionDTO.java` (40 lines) - Denial decision with reason
  - [x] `InfoRequestDTO.java` (40 lines) - Additional info request

##### Frontend UI
- [x] **Patient Pending Requests Page** (`pending-requests.jsp` - 952 lines)
  - [x] Responsive table displaying all pending access requests
  - [x] Request details: professional name, specialty, clinic, document type, timestamp
  - [x] Action buttons: Approve, Deny, Request Info
  - [x] Modal dialogs for approve/deny/info actions
  - [x] Success/error notifications with auto-dismiss
  - [x] Empty state when no pending requests
  - [x] Pagination controls
  - [x] Real-time badge/counter showing pending request count
  - [x] Integration with POST endpoints for all user actions

##### Navigation & User Flow Integration
- [x] **Updated Patient Dashboard** (`dashboard.jsp` - 2 new lines)
  - [x] Added "Pending Requests" link to quick actions
  - [x] Added badge/counter showing number of pending requests

##### Integration with Core Systems
- [x] **Policy Engine Integration**
  - [x] When patient approves: creates temporary access policy (24-hour default TTL)
  - [x] Dynamically configurable duration via approval modal
  - [x] Policy automatically expires after configured duration
  - [x] Request status updated to APPROVED

- [x] **Audit System Integration**
  - [x] All access request state changes logged (CREATED, APPROVED, DENIED, INFO_REQUESTED)
  - [x] Professional identifying information recorded
  - [x] Patient decision and reasoning captured
  - [x] Timestamp and request details in audit trail
  - [x] Action outcome (SUCCESS/FAILURE) tracked

#### Implementation Summary

**Architecture**:
- RESTful API design with standard HTTP status codes
- Service layer with separation of concerns
- DTO pattern for clean request/response handling
- Stateless service design for horizontal scaling
- Complete integration with existing Policy Engine and Audit System

**Database Entities**:
- AccessRequest entity with fields: requestId, patientCI, professionalId, professionalName, specialty, clinicId, clinicName, documentId, documentType, reason, status, createdAt, respondedAt

**Security**:
- Authorization checks on all endpoints (patient must own the pending requests)
- Audit logging of all decisions and approvals
- Policy decisions tracked and immutable

**User Experience**:
- Clean, responsive UI following existing portal design
- Intuitive approval/denial workflow with confirmation modals
- Real-time feedback with success/error messages
- Empty state messaging for improved UX
- Integrated navigation from patient dashboard

**Dependencies**: ✅ Policy Engine (already 100% complete)

**Files Summary**:
- 8 Java files (backend)
- 1 JSP file (frontend)
- 1 modified dashboard file
- Total: 1,984 lines of code

---

### 9. Peripheral Node Document Retrieval (PLANNED)
**Status**: Detailed plan created, ready for implementation
**Priority**: HIGH - Core feature for end-to-end functionality
**Estimated Implementation Time**: 8-10 days (64-80 hours)

#### 📋 Overview

The document retrieval system enables patients and professionals to download actual documents (PDFs, XML, FHIR) from peripheral nodes (clinics, hospitals, labs). Currently, the system only shows document metadata from RNDC - this feature completes the end-to-end flow.

**Architecture Flow**:
```
Patient/Professional → DocumentRetrievalResource (REST API)
                    → DocumentRetrievalService (orchestration)
                      → Check RNDC for metadata (documentLocator URL)
                      → Evaluate access policies
                      → Fetch document from peripheral node
                      → Verify document hash (integrity)
                      → Log access in audit
                    → Return document to frontend
```

**Good News**: ✅ `PeripheralNodeClient` already exists with:
- HTTP client with timeouts
- Circuit breaker pattern
- Retry logic with exponential backoff
- Basic document retrieval from URL

**We only need to build the orchestration layer!**

#### 🎯 Implementation Phases

##### **Phase 1: Core Retrieval (Foundation)** - 2-3 days (16-24 hours)

**Goal**: Basic end-to-end document retrieval without policy enforcement

**Tasks**:
- [ ] Create `DocumentRetrievalService` (orchestrates retrieval workflow)
  - Fetch document metadata from RNDC
  - Call PeripheralNodeClient to fetch document
  - Verify document hash (SHA-256 integrity check)
  - Log access via AuditService
  - Files: `src/main/java/uy/gub/hcen/document/service/DocumentRetrievalService.java`

- [ ] Create `DocumentRetrievalResource` (REST API)
  - `GET /api/documents/{documentId}/content` - Download document
  - `GET /api/documents/{documentId}/metadata` - Check availability
  - Authentication via JWT
  - Files: `src/main/java/uy/gub/hcen/document/api/rest/DocumentRetrievalResource.java`

- [ ] Create DTOs
  - `DocumentContentDTO` - Response with document bytes, content type, filename
  - `ErrorResponse` - Standardized error format
  - Files: `src/main/java/uy/gub/hcen/document/dto/*`

- [ ] Create Custom Exceptions
  - `DocumentNotFoundException` - Document not in RNDC
  - `AccessDeniedException` - Policy denied access
  - `DocumentIntegrityException` - Hash verification failed
  - `PeripheralNodeException` - Peripheral node error
  - Files: `src/main/java/uy/gub/hcen/document/exception/*`

- [ ] Integrate with existing services
  - RNDC service integration (document metadata lookup)
  - PeripheralNodeClient integration (HTTP retrieval)
  - AuditService integration (access logging)

**Deliverable**: Working document download for patients (no policy checks yet)

**Testing Command**:
```bash
curl -H "Authorization: Bearer {patient_jwt}" \
     http://localhost:8080/hcen/api/documents/123/content \
     --output document.pdf
```

---

##### **Phase 2: Policy Enforcement Integration** - 1-2 days (8-16 hours)

**Goal**: Add access control based on patient-defined policies

**Tasks**:
- [ ] Create `PolicyEvaluationService`
  - Extend or integrate with existing `PolicyManagementService`
  - Implement `evaluateDocumentAccess(documentId, requestorCi, requestorRole)`
  - Return: PERMIT / DENY / PENDING decisions
  - Files: `src/main/java/uy/gub/hcen/policy/service/PolicyEvaluationService.java`

- [ ] Integrate policy evaluation in `DocumentRetrievalService`
  - Check policy BEFORE fetching document
  - Handle PERMIT: Continue retrieval
  - Handle DENY: Return 403 Forbidden with reason
  - Handle PENDING: Send notification, return 202 Accepted

- [ ] Add comprehensive error messages
  - User-friendly messages explaining why access was denied
  - Actionable guidance (e.g., "Contact the clinic administrator")

**Deliverable**: Policy-aware document retrieval with proper authorization

**Testing Scenarios**:
- Create policy: "Deny LAB_RESULT for specialty CARDIOLOGY"
- Attempt access as cardiologist → Verify 403 Forbidden
- Approve access request → Verify 200 OK with document

---

##### **Phase 3: Advanced Features & Production Readiness** - 2-3 days (16-24 hours)

**Goal**: Production-ready system with monitoring and caching

**Tasks**:
- [ ] Create `PeripheralNodeConfiguration`
  - Multi-clinic API key management
  - Load clinic configurations from database
  - Support for clinic-specific settings (timeouts, retries)
  - Files: `src/main/java/uy/gub/hcen/config/PeripheralNodeConfiguration.java`

- [ ] Enhance error handling
  - Detailed error messages with actionable guidance
  - Graceful degradation when peripheral node is down
  - Circuit breaker state visibility in responses

- [ ] Add content type detection
  - Determine MIME type from document metadata
  - Support: PDF, XML, FHIR JSON/XML, DICOM, images

- [ ] Add document caching (Redis)
  - Cache frequently accessed documents (15-minute TTL)
  - Configurable cache size limit
  - Cache invalidation strategy

- [ ] Add metrics and monitoring
  - Prometheus metrics endpoint
  - Track: retrieval latency, success/failure rates, cache hit rates
  - Circuit breaker state monitoring

- [ ] Add health check endpoint
  - `GET /api/health/peripheral-nodes` - Check all peripheral node connectivity
  - Return: status for each registered clinic

- [ ] Update `ClinicalHistoryService`
  - Replace document locator URL with actual retrieval
  - Integrate with `DocumentRetrievalService`

**Deliverable**: Production-ready system with caching, monitoring, and health checks

---

#### 📦 Components to Create/Modify

##### New Components

1. **DocumentRetrievalService** (`src/main/java/uy/gub/hcen/document/service/DocumentRetrievalService.java`)
   - Core business logic for document retrieval
   - Orchestrates RNDC, PeripheralNodeClient, PolicyEngine, AuditService
   - Handles errors and circuit breaker states
   - Estimated: 300-400 lines

2. **DocumentRetrievalResource** (`src/main/java/uy/gub/hcen/document/api/rest/DocumentRetrievalResource.java`)
   - REST API endpoints for document retrieval
   - JWT authentication and authorization
   - Error handling with proper HTTP status codes
   - Estimated: 250-300 lines

3. **DTOs** (`src/main/java/uy/gub/hcen/document/dto/`)
   - `DocumentContentDTO.java` - Response with document bytes and metadata
   - `DocumentRetrievalRequest.java` - Request DTO (future use)
   - `ErrorResponse.java` - Standardized error response
   - Estimated: 150-200 lines total

4. **Exceptions** (`src/main/java/uy/gub/hcen/document/exception/`)
   - `DocumentNotFoundException.java`
   - `AccessDeniedException.java`
   - `DocumentIntegrityException.java`
   - Estimated: 100-150 lines total

5. **PolicyEvaluationService** (`src/main/java/uy/gub/hcen/policy/service/PolicyEvaluationService.java`)
   - Access control decision engine
   - Integrates with existing PolicyManagementService
   - Returns PERMIT/DENY/PENDING decisions
   - Estimated: 200-300 lines

6. **PeripheralNodeConfiguration** (`src/main/java/uy/gub/hcen/config/PeripheralNodeConfiguration.java`)
   - Clinic API key management
   - Load configurations from database/properties
   - Clinic-specific settings
   - Estimated: 200-250 lines

##### Existing Components to Modify

1. **PeripheralNodeClient** - Minor enhancements
   - Already has core functionality ✅
   - Add content type detection
   - Add configurable clinic API keys
   - Estimated: +50-100 lines

2. **ClinicalHistoryService** - Update document content retrieval
   - Replace locator URL return with actual document retrieval
   - Integrate with DocumentRetrievalService
   - Estimated: +50-100 lines

3. **RndcService** - No changes needed ✅
   - Already provides document metadata lookup

4. **AuditService** - No changes needed ✅
   - Already provides document access logging

---

#### 🔗 Integration Points

##### 1. RNDC Service (Existing - ✅ Ready)
- **Method**: `RndcService.getDocumentMetadata(Long documentId)`
- **Returns**: `Optional<RndcDocument>` with documentLocator, documentHash, patientCi, clinicId
- **Usage**: Fetch metadata before retrieval

##### 2. PeripheralNodeClient (Existing - ✅ Ready)
- **Method**: `PeripheralNodeClient.retrieveDocument(String locator, String apiKey, String expectedHash)`
- **Features**: Circuit breaker, retry logic, hash verification
- **Usage**: Fetch actual document bytes from peripheral node

##### 3. PolicyManagementService (Existing - Partial)
- **Status**: Exists but needs policy evaluation extension
- **TODO**: Create PolicyEvaluationService for access decisions
- **Returns**: PolicyDecision { PERMIT/DENY/PENDING, reason }

##### 4. AuditService (Existing - ✅ Ready)
- **Method**: `AuditService.logDocumentAccess(...)`
- **Usage**: Log all access attempts (success, denied, failure)

##### 5. ClinicManagementService (Existing - ✅ Ready)
- **Method**: Get clinic API key for peripheral node authentication
- **Usage**: Fetch API key by clinicId

---

#### 🔒 Security Considerations

##### 1. Authentication & Authorization
- **Between Frontend and HCEN**: JWT bearer token (existing)
- **Between HCEN and Peripheral Nodes**: API key authentication
  - Each clinic has unique API key
  - Format: `Authorization: Bearer clinic-{id}-{uuid}`
  - Keys stored encrypted in database

##### 2. Document Integrity (Hash Verification)
- **Algorithm**: SHA-256
- **Process**:
  1. Download document from peripheral node
  2. Calculate SHA-256 hash of downloaded bytes
  3. Compare with hash stored in RNDC metadata
  4. Throw exception if mismatch (tampering detected)

##### 3. HTTPS Enforcement
- **Already Implemented**: ✅ PeripheralNodeClient validates HTTPS
- **Requirement**: All peripheral node URLs must use https://
- **Compliance**: AC002-AC004

##### 4. Policy Enforcement
- **Timing**: BEFORE document retrieval (fail fast)
- **Types**: DOCUMENT_TYPE, SPECIALTY, CLINIC, TIME_BASED, PROFESSIONAL
- **Decisions**: PERMIT (continue), DENY (403), PENDING (notify patient)

##### 5. Audit Logging
- **Events**: All access attempts (success, denied, failure)
- **Fields**: who, what, when, from where, outcome, reason
- **Compliance**: AC026 (patients can view audit trail)

---

#### 🏗️ Peripheral Node Contract

##### REST API Specification (Peripheral Node Side)

**Endpoint**: `GET /api/documents/{documentId}`

**Request**:
```http
GET /api/documents/abc123 HTTP/1.1
Host: clinic-001.hcen.uy
Authorization: Bearer clinic-001-api-key-xyz
Accept: application/octet-stream, application/pdf, application/xml
```

**Response (Success)**:
```http
HTTP/1.1 200 OK
Content-Type: application/pdf
Content-Length: 2547891
Content-Disposition: attachment; filename="clinical-note.pdf"
X-Document-Hash: sha256:a1b2c3d4e5f6...
X-Document-Type: CLINICAL_NOTE

[binary PDF content]
```

**Response (Not Found)**:
```http
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "error": "DOCUMENT_NOT_FOUND",
  "message": "Document abc123 not found",
  "timestamp": "2025-11-06T10:30:00Z"
}
```

##### Supported Content Types
- `application/pdf` - PDF documents
- `application/xml` - XML clinical documents
- `application/fhir+json` - FHIR resources (IPS format)
- `application/fhir+xml` - FHIR XML
- `image/jpeg`, `image/png` - Medical images
- `application/dicom` - DICOM imaging files

##### Document Locator URL Format
```
https://{clinic-domain}/api/documents/{document-id}

Examples:
https://clinic-hc.hcen.uy/api/documents/lab-result-2024-001
https://hospital-britanico.hcen.uy/api/documents/imaging-20241106-001
```

##### Error Codes

| HTTP Status | Error Code | Retry? | Meaning |
|-------------|------------|--------|---------|
| 200 OK | - | - | Success |
| 400 Bad Request | INVALID_REQUEST | No | Malformed request |
| 401 Unauthorized | UNAUTHORIZED | No | Invalid/missing API key |
| 403 Forbidden | FORBIDDEN | No | Valid key but access denied |
| 404 Not Found | DOCUMENT_NOT_FOUND | No | Document doesn't exist |
| 500 Internal Server Error | INTERNAL_ERROR | Yes | Server error |
| 503 Service Unavailable | SERVICE_UNAVAILABLE | Yes | Temporarily down |
| 504 Gateway Timeout | TIMEOUT | Yes | Request timed out |

**Retry Strategy**:
- Network errors (IOException): Retry 3x with exponential backoff (1s, 2s, 4s)
- 500, 503, 504: Retry 3x with exponential backoff
- 400, 401, 403, 404: Do NOT retry (permanent errors)

---

#### 🛡️ Error Handling & Resilience

##### Circuit Breaker (Already Implemented ✅)
- **Threshold**: 5 consecutive failures
- **Timeout**: 60 seconds
- **States**: CLOSED → OPEN → HALF_OPEN → CLOSED
- **Behavior**:
  - CLOSED: All requests pass through
  - OPEN: All requests fail fast (no network calls)
  - HALF_OPEN: Next request is a test

##### Retry Logic (Already Implemented ✅)
- **Max attempts**: 3
- **Backoff**: Exponential (1s, 2s, 4s)
- **Retry on**: IOException, 500, 503, 504
- **Don't retry**: 400, 401, 403, 404

##### Timeout Configuration (Already Implemented ✅)
- **Connection timeout**: 5 seconds
- **Read timeout**: 30 seconds (for large documents)

##### Graceful Degradation

**Scenario 1: Peripheral Node Down**
```
Try retrieval → PeripheralNodeException
→ Log error + audit failure
→ Return 503 Service Unavailable
→ Message: "Document retrieval temporarily unavailable. The clinic system may be offline."
```

**Scenario 2: Hash Verification Fails**
```
Download document → Calculate hash → Mismatch
→ Log integrity violation
→ Return 500 Internal Server Error
→ Message: "Document integrity check failed. Access denied for security."
```

**Scenario 3: Policy Engine Unavailable**
```
Try policy evaluation → Exception
→ Fail-safe: DENY access
→ Log error
→ Return 403 Forbidden
→ Message: "Policy evaluation failed. Access denied by default."
```

---

#### ⚙️ Configuration (application.properties)

```properties
# ================================================================
# Peripheral Node Configuration
# ================================================================

# Circuit breaker settings
peripheral.circuit-breaker.threshold=5
peripheral.circuit-breaker.timeout-ms=60000

# Retry settings
peripheral.retry.max-attempts=3
peripheral.retry.initial-delay-ms=1000

# Timeout settings
peripheral.timeout.connection-ms=5000
peripheral.timeout.read-ms=30000

# Cache settings (Redis)
peripheral.cache.enabled=true
peripheral.cache.ttl-minutes=15
peripheral.cache.max-size-mb=100

# Security
peripheral.enforce-https=true
peripheral.verify-hash=true

# Content type mappings
peripheral.content-type.pdf=application/pdf
peripheral.content-type.xml=application/xml
peripheral.content-type.fhir-json=application/fhir+json
peripheral.content-type.dicom=application/dicom

# Monitoring
peripheral.metrics.enabled=true
peripheral.metrics.export-interval-seconds=60
```

---

#### 📁 File Structure

```
hcen/src/main/java/uy/gub/hcen/
│
├── document/                                   # NEW package
│   ├── api/rest/
│   │   └── DocumentRetrievalResource.java     # NEW - REST API
│   ├── service/
│   │   └── DocumentRetrievalService.java      # NEW - Business logic
│   ├── dto/
│   │   ├── DocumentContentDTO.java            # NEW - Response DTO
│   │   └── ErrorResponse.java                 # NEW - Error DTO
│   └── exception/
│       ├── DocumentNotFoundException.java     # NEW - Exception
│       ├── AccessDeniedException.java         # NEW - Exception
│       └── DocumentIntegrityException.java    # NEW - Exception
│
├── config/
│   └── PeripheralNodeConfiguration.java       # NEW - Clinic API keys
│
├── policy/service/
│   └── PolicyEvaluationService.java           # NEW - Access decisions
│
├── integration/peripheral/
│   └── PeripheralNodeClient.java              # EXISTING - Enhance
│
├── service/rndc/
│   └── RndcService.java                       # EXISTING - Use as-is
│
├── service/audit/
│   └── AuditService.java                      # EXISTING - Use as-is
│
└── clinicalhistory/service/
    └── ClinicalHistoryService.java            # EXISTING - Modify

hcen/src/test/java/uy/gub/hcen/
│
└── document/
    ├── service/
    │   └── DocumentRetrievalServiceTest.java  # NEW - Unit tests
    └── api/rest/
        └── DocumentRetrievalResourceTest.java # NEW - Integration tests
```

---

#### 📊 Estimated Effort

| Phase | Tasks | Estimated Time |
|-------|-------|----------------|
| **Phase 1: Core Retrieval** | Service, REST API, DTOs, exceptions, basic integration | 16-24 hours (2-3 days) |
| **Phase 2: Policy Enforcement** | PolicyEvaluationService, integration | 8-12 hours (1-2 days) |
| **Phase 3: Advanced Features** | Configuration, caching, monitoring, health checks | 16-24 hours (2-3 days) |
| **TOTAL** | | **40-60 hours (5-7 days)** |

**Conservative Estimate**: 6-7 days (48-56 hours)

---

#### 🎯 Success Criteria

**Phase 1 Complete When**:
- ✅ Patient can download a document via `GET /api/documents/{id}/content`
- ✅ Document hash is verified (integrity check)
- ✅ Access is logged in audit system
- ✅ Circuit breaker works (fails fast after 5 errors)

**Phase 2 Complete When**:
- ✅ Access is denied if policy prohibits (403 Forbidden)
- ✅ Pending requests trigger patient notification (202 Accepted)
- ✅ All policy types work (DOCUMENT_TYPE, SPECIALTY, CLINIC, etc.)

**Phase 3 Complete When**:
- ✅ Multi-clinic API keys work (from configuration)
- ✅ Documents are cached in Redis (15-minute TTL)
- ✅ Metrics are exported (Prometheus endpoint)
- ✅ Health check endpoint works (`/api/health/peripheral-nodes`)

---

#### 🚀 Next Steps After Implementation

1. **Update ClinicalHistoryService** - Use DocumentRetrievalService instead of returning locator URL
2. **Add Document Viewer UI** - Frontend component to display/download documents
3. **Admin Health Dashboard** - Monitor peripheral node connectivity and circuit breaker states
4. **Document Preview** - Generate thumbnails/previews for PDFs and images
5. **Streaming for Large Files** - Optimize memory usage for multi-GB documents

---

#### 📝 Notes

- **Leverage Existing Code**: PeripheralNodeClient already has circuit breaker, retry logic, and hash verification ✅
- **Follow Existing Patterns**: Use same service/resource/DTO structure as RNDC, INUS, Clinic services
- **Security First**: Hash verification and policy enforcement are non-negotiable
- **Production Ready**: Circuit breaker, retries, caching, monitoring built-in from the start
- **Incremental Deployment**: Can deploy phase by phase (Phase 1 → Phase 2 → Phase 3 → Phase 4)

---

## 📊 OVERALL IMPLEMENTATION STATUS

### Backend Services
| Component | Status | Completeness | Lines of Code | Sprint Status |
|-----------|--------|--------------|---------------|---------------|
| gub.uy Auth | ✅ Implemented | 90% | ~800 | - |
| INUS Service | ✅ Implemented | 100% | ~600 | - |
| Clinic Management | ✅ Implemented | 85% | ~700 | - |
| RNDC | ✅ Implemented | 80% | ~800 | - |
| PDI Integration | ✅ Implemented | 80% | ~400 | - |
| Peripheral Client | ✅ Implemented | 80% | ~500 | - |
| **Clinical History** | ✅ **Implemented** | **100%** | **~800** | **✅ Completed 2025-11-13** |
| **FHIR Integration** | ✅ **Implemented** | **100%** | **~650** | **✅ Completed 2025-11-13** |
| **Policy Engine** | 🔨 **In Progress** | **60% → 100%** | **~600 → ~1,200** | **🎯 Current Sprint** |
| **Audit System** | 🔨 **In Progress** | **60% → 100%** | **~300 → ~800** | **🎯 Current Sprint** |
| Redis Caching | ⚠️ Partial | 60% | ~200 | - |

**Total Backend LOC**: ~6,350 → ~7,850 (estimated after sprint completion)

### Web UI
| Component | Status | Completeness | Lines of Code |
|-----------|--------|--------------|---------------|
| Admin Login | ✅ Done | 100% | - |
| Patient Login | ✅ Done | 100% | - |
| Patient Registration | ✅ Done | 100% | 165 |
| Patient Profile | ✅ Done | 100% | 280 |
| Admin User Dashboard | ✅ Done | 100% | 380 |
| Admin User Detail | ✅ Done | 100% | 420 |
| Admin Clinic Dashboard | ✅ Done | 100% | 878 |
| Admin Clinic Registration | ✅ Done | 100% | 838 |
| Admin Clinic Detail | ✅ Done | 100% | 884 |
| Patient Pending Access Requests | ✅ Done | 100% | 954 |
| Patient Access Policy Management | ✅ Done | 100% | 890 |
| Patient Clinical History | ✅ Done | 100% | (existed) |

**Total UI LOC (Completed)**: ~5,689 (INUS User Management: 1,245 LOC + Clinic Management: 2,600 LOC + Patient Pending Requests: 954 LOC + Patient Access Policies: 890 LOC)
**Total UI Completeness**: ✅ 100% (All Patient Portal Features Complete)

**Note**: Professional Portal is OUT OF SCOPE for HCEN Central (belongs to Clinic/Peripheral component)

### Deployment
| Component | Status | Completeness | Estimate |
|-----------|--------|--------------|----------|
| Docker Configuration | ✅ Done | 90% | 4 hours |
| CI/CD Pipeline | ✅ Done | 80% | 8 hours |
| Production Config | ❌ Missing | 20% | 32 hours |
| Health Checks | ❌ Missing | 0% | 8 hours |
| Monitoring | ❌ Missing | 0% | 16 hours |
| Security Hardening | ⚠️ Partial | 50% | 16 hours |

**Total Deployment Completeness**: ~75%
**Estimated Remaining Work**: 84 hours

---

## 🚀 RECOMMENDED IMPLEMENTATION SEQUENCE

### Phase 1: Priority UI Implementation - ✅ 100% COMPLETE (112/112 hours invested)
**Goal**: Complete user-facing UI for demo and deployment

1. ✅ Patient registration portal (COMPLETE - 165 LOC)
2. ✅ Patient profile management (COMPLETE - 280 LOC)
3. ✅ Admin user dashboard (COMPLETE - 380 LOC)
4. ✅ Admin user detail/edit page (COMPLETE - 420 LOC)
5. ✅ Admin clinic registration (COMPLETE - 838 LOC)
6. ✅ Admin clinic dashboard (COMPLETE - 878 LOC)
7. ✅ Admin clinic detail/edit page (COMPLETE - 884 LOC)
8. ✅ Patient pending access requests page (COMPLETE - 954 LOC)
   - REST API endpoints and service layer (669 LOC)
   - JSP page with modals, notifications, pagination (952 LOC)
   - Data transfer objects (359 LOC)
   - Dashboard integration (2 LOC)

**Deliverable**: ✅ INUS user management UI complete (1,245 LOC). ✅ Clinic management UI complete (2,600 LOC). ✅ Patient pending requests UI complete (954 LOC)
**Total Phase 1 LOC**: 4,799 lines
**Status**: PHASE 1 COMPLETE - Ready for Phase 2 (Backend Completion + Production Deployment)

### Phase 2: Production Deployment - 84 hours
**Goal**: Deploy to ANTEL mi-nube with production hardening

1. Production Docker and WildFly configuration (20 hours)
2. Environment configuration and secrets management (16 hours)
3. Health checks, logging, monitoring (24 hours)
4. ANTEL-specific configuration (16 hours)
5. Deployment and smoke tests (8 hours)

**Deliverable**: Running production instance on ANTEL infrastructure

### Phase 3: Post-Deployment (Ongoing)
1. Security audit and remediation
2. User acceptance testing
3. Bug fixes and refinements
4. Performance monitoring and optimization

---

## 📈 PROJECT METRICS

**Overall Completion**: ~92%
**Current Focus**: Security & Compliance Backend + Production Deployment
**Estimated Time to Production**: 120-156 hours (policy engine/audit: 40-56 hours + production deployment: 84-100 hours)
**Critical Path**: Security Backend → Production Deployment

**Deployment Blockers**:
1. ✅ Priority UI completion (COMPLETE - 100%)
2. ✅ Patient Pending Access Requests UI (COMPLETE - 100%)
3. ✅ Patient Access Policy Management UI (COMPLETE - 100%)
4. ✅ Patient Clinical History (COMPLETE - 100%)
5. ✅ FHIR R4 Integration (COMPLETE - 100%)
6. 🔨 Security backend features (IN PROGRESS - Policy Engine 60%, Audit System 60%)
7. ❌ Production configuration (pending - 84-100 hours)

**Recent Milestones**:
- ✅ 2025-11-13: Clinical History Document Retrieval Complete (800+ LOC across 7 files)
  - ClinicalHistoryService with PeripheralNodeClient integration
  - Document retrieval from peripheral nodes (circuit breaker, retry logic, hash verification)
  - Inline document viewing (PDFs in browser, FHIR content as JSON)
  - REST API endpoints for clinical history and document viewing
  - Comprehensive audit logging with IP address and user agent tracking
  - Security: patient authorization, hash verification, no caching
- ✅ 2025-11-13: FHIR R4 Integration Complete (650+ LOC across 7 files)
  - HAPI FHIR 7.2.0 integration
  - FHIR Patient → HCEN user registration (Uruguay OID support)
  - FHIR DocumentReference → HCEN document registration (11 LOINC codes)
  - Content negotiation (application/json vs application/fhir+json)
  - 100% backward compatible with existing custom JSON format
  - Zero service layer changes (converters only)
  - Comprehensive unit tests for FHIR conversion
- ✅ 2025-11-04: Patient Access Policy Management UI Complete (1,700+ LOC across 12 files)
  - RESTful API endpoints for managing patient access policies (CRUD operations)
  - Service layer with Policy Engine and Audit System integration
  - Complete JSP page with modal dialogs for policy creation/editing
  - Patient dashboard integrated with policy management link
  - All patient portal core features now 100% complete (5,689 total LOC)
- ✅ 2025-11-03: Patient Pending Access Requests UI Complete (1,984 LOC across 10 files)
  - RESTful API endpoints for listing, approving, denying access requests
  - Service layer with Policy Engine and Audit System integration
  - Complete JSP page with modals, notifications, pagination
  - Data transfer objects for clean request/response handling
  - Updated patient dashboard with pending request navigation and badge counter
- 📋 2025-11-03: Scope Clarifications Applied
  - Removed professional authentication (belongs to Clinic/Peripheral component)
  - Removed mobile app and Firebase integration
  - Replaced mobile notifications with Patient Pending Access Requests JSP page
  - Updated TODO.md with clear scope boundaries
- 🔨 2025-10-30: Started Policy Engine + Audit System Sprint (32-40 hours estimated)
  - Focus: Complete core security and compliance backend features (functional only, no testing)
  - Policy Engine: 60% → 100% (ABAC, RBAC, conflict resolution, time-based, emergency access)
  - Audit System: 60% → 90% (comprehensive logging, immutable storage, retention, query API only - no admin UI or export)
- ✅ 2025-10-29: Priority Flow #3 Complete - Clinic Management UI (3 JSP pages, 2,600 LOC)
  - Admin clinic listing dashboard with search/filter/pagination
  - Admin clinic registration form with API key generation
  - Admin clinic detail page with inline editing and onboarding workflow
- ✅ 2025-10-24: Priority Flow #2 Complete - INUS User Management UI (4 JSP pages, 1,245 LOC)
  - Patient registration portal with validation
  - Patient profile dashboard with edit functionality
  - Admin user management dashboard with search/filter
  - Admin user detail page with inline editing

---

## 🎯 NEXT STEPS (This Week)

### ✅ Phase 1 Complete: Priority UI Implementation (100% DONE)
1. ✅ **Day 1-2**: Implement patient registration UI (COMPLETE - 165 LOC)
2. ✅ **Day 3**: Implement admin user management UI (COMPLETE - 1,080 LOC)
3. ✅ **Day 4-5**: Implement admin clinic management UI (COMPLETE - 2,600 LOC)
   - ✅ Admin clinic dashboard (`webapp/admin/clinics.jsp` - 878 LOC)
   - ✅ Admin clinic registration (`webapp/admin/clinic-register.jsp` - 838 LOC)
   - ✅ Admin clinic detail/edit (`webapp/admin/clinic-detail.jsp` - 884 LOC)
4. ✅ **Day 6-7**: Implement patient pending access requests UI (COMPLETE - 954 LOC + API)
   - ✅ REST API endpoints (`AccessRequestResource.java` - 362 LOC)
   - ✅ Service layer (`AccessRequestService.java` - 307 LOC)
   - ✅ Data transfer objects (5 DTO files - 359 LOC)
   - ✅ Patient pending requests page (`pending-requests.jsp` - 952 LOC)
   - ✅ Updated patient dashboard navigation

**Phase 1 Deliverable**: ✅ ALL FOUNDATIONAL PATIENT PORTAL UI COMPLETE (4,799 total LOC across 13 JSP pages + supporting backend services)

### ✅ Phase 2A Complete: Patient Portal Core Features (100% DONE)
1. ✅ **Day 8-9**: Implement patient access policy management UI (COMPLETE - 1,700+ LOC)
   - ✅ REST API endpoints for CRUD operations (`AccessPolicyResource.java`)
   - ✅ Service layer with Policy Engine integration (`AccessPolicyService.java`)
   - ✅ Policy management JSP page (`access-policies.jsp`)
   - ✅ DTO classes for request/response handling (6 DTO files)
   - ✅ Updated patient dashboard with policy link

**Phase 2A Deliverable**: ✅ PATIENT PORTAL CORE FEATURES COMPLETE (5,689+ total LOC across 14 JSP pages + supporting services)

### ✅ Completed Phase: Patient Clinical History + FHIR Integration (40 hours)
**Completion Date**: 2025-11-13
**Status**: COMPLETE

**Delivered**:
1. ✅ Clinical History Service Layer (24 hours)
   - ClinicalHistoryService with PeripheralNodeClient integration
   - Document retrieval from peripheral nodes
   - Hash verification and integrity checking
   - Comprehensive audit logging

2. ✅ Clinical History REST API (included in service layer)
   - GET /api/patients/{ci}/history - Fetch clinical history with filters
   - GET /api/patients/{ci}/history/documents/{documentId} - Inline document viewing
   - Proper HTTP headers and content type detection

3. ✅ FHIR R4 Integration (16 hours)
   - HAPI FHIR 7.2.0 dependencies
   - FHIR Patient and DocumentReference converters
   - LOINC code mapping (11 document types)
   - Uruguay national ID OID support
   - Content negotiation
   - Comprehensive unit tests

4. ✅ Frontend UI
   - clinical-history.jsp already existed from previous work
   - "Ver" button functionality for inline document viewing

### 🔨 Current Phase: Security Backend Completion (32-40 hours)
**Start Date**: 2025-11-13
**Estimated Completion**: 2025-11-20 (1 week)
**Priority**: Complete Policy Engine + Audit System

**Focus Areas**:
- Complete Policy Engine implementation (ABAC, RBAC, conflict resolution, time-based, emergency access)
- Complete Audit System implementation (comprehensive logging, immutable storage, retention, query API - no admin UI or export)
- Functional implementation only, no automated testing

### Final Phase: Production Deployment (84-100 hours)
**Priority 1 - Security Backend** (Weeks 2-3):
- Policy Engine: ABAC, RBAC, conflict resolution, time-based policies, emergency access
- Audit System: Comprehensive logging, immutable storage, retention policies, query API (no admin UI, no export)

**Priority 2 - Production Deployment** (Weeks 3-4):
1. Docker and WildFly production configuration
2. Environment configuration and secrets management
3. Health checks, logging, monitoring setup
4. ANTEL mi-nube integration
5. Deployment verification and smoke tests

**Priority 3 - Post-Implementation** (Optional - Advanced Features):
1. Admin Global Policies UI (optional)
   - Policy management interface
   - Policy testing/simulation tool
   - Emergency access review dashboard

**Timeline Summary**:
- ✅ Week 1 (Nov 4-13): Clinical History Visualization + FHIR Integration (40 hours) - COMPLETE
- Week 2-3 (Nov 13-27): Security Backend Completion (32-40 hours)
- Week 4-5 (Nov 27 - Dec 11): Production Deployment (84-100 hours)

---

## 📝 NOTES

- **SCOPE**: Focus on HCEN Central only (do NOT implement PDI mock, mobile app, clinic, or provider components)
- **OUT OF SCOPE**:
  - Professional authentication/login (belongs to Clinic/Peripheral component)
  - Mobile app development (React Native, Firebase Cloud Messaging)
  - Firebase integration (push notifications, Cloud Firestore)
  - Testing coverage requirements (no 80% coverage, no unit tests, no integration tests)
  - Admin audit log UI (audit dashboard, export functionality)
- **AUTHENTICATION SCOPE**:
  - HCEN Central: Patient authentication via gub.uy ONLY
  - Professional authentication: Handled by Clinic/Peripheral component (separate project)
- **ACCESS REQUEST WORKFLOW**: Web-based approval via patient portal JSP page (no mobile notifications)
- **FOCUS**: Functional implementation only, no automated testing required
- All communication must use HTTPS (AC002-AC004)
- Follow defined protocols: gub.uy (OIDC), PDI (SOAP), Peripheral Nodes (REST + API key)
- Always update this TODO.md after completing tasks using the general-purpose agent

**Last Review Date**: 2025-11-03 (Scope Clarifications Applied)
**Next Review Date**: 2025-11-06 (Sprint Review)
