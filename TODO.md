# HCEN - Priority Implementation TODO

**Last Updated**: 2025-11-04 (Updated: Completed Patient Access Policy Management, Started Clinical History Visualization)
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

### NEW REQUIREMENT:
- **Patient Pending Access Requests JSP Page**: Instead of mobile notifications, patients will view and approve/deny pending access requests through a web interface in the patient portal

---

## 🎯 CURRENT SPRINT: Policy Engine + Audit System Completion

**Status**: 🔨 IN PROGRESS
**Start Date**: 2025-10-30
**Goal**: Complete core security and compliance features
**Estimated Time**: 40-56 hours

### Sprint Objectives
- Policy Engine: 60% → 100% (24-32 hours)
- Audit System: 60% → 100% (16-24 hours)

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

- [ ] Add audit log export functionality
  - [ ] Export to CSV format
  - [ ] Export to JSON format
  - [ ] Export to PDF report
  - [ ] Add date range filtering for exports
  - [ ] Include export audit trail (who exported what)
  - Files: Create `src/main/java/uy/gub/hcen/audit/export/AuditExportService.java`

- [ ] Create comprehensive audit tests
  - [ ] Unit tests for audit service
  - [ ] Test immutability and tamper detection
  - [ ] Test retention policy enforcement
  - [ ] Test search/query functionality
  - [ ] Integration tests for full audit workflow
  - Files: Create `src/test/java/uy/gub/hcen/audit/`

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

### 7. Clinical History Visualization for Patients - IN PROGRESS
**Status**: Just started
**Completion Date**: Estimated 2025-11-07 (3-4 days)
**Estimated Implementation Time**: 36-40 hours

#### 🔨 Tasks (IN PROGRESS)

##### Backend Service Layer
- [ ] **Clinical History Service** (`ClinicalHistoryService.java`)
  - [ ] Fetch documents from RNDC for patient
  - [ ] Filter documents by type, date range, author
  - [ ] Sort documents by creation date (newest first)
  - [ ] Implement pagination for large result sets
  - [ ] Cache clinical history (15-minute TTL)

- [ ] **Document Metadata Service** (`DocumentMetadataService.java`)
  - [ ] Retrieve document metadata (type, date, author, clinic, size)
  - [ ] Validate document access permissions via Policy Engine
  - [ ] Return document preview/summary if available

##### Backend REST API
- [ ] **REST Endpoints** (`ClinicalHistoryResource.java`)
  - [ ] `GET /api/patients/{ci}/history` - Fetch clinical history with filters
  - [ ] `GET /api/patients/{ci}/history/{documentId}` - Get document details
  - [ ] Query parameters: `type` (filter by document type), `from` (start date), `to` (end date), `page`, `size`
  - [ ] Response includes document list with pagination metadata

##### Frontend UI
- [ ] **Clinical History Page** (`clinical-history.jsp`)
  - [ ] Responsive document list/table view
  - [ ] Document preview cards showing:
    - Document type (icon + label)
    - Creation date
    - Author/Professional name
    - Clinic name
    - Document size
  - [ ] Filter controls:
    - Document type filter (dropdown with all types)
    - Date range pickers (from/to dates)
    - Search by document ID or content
  - [ ] Sorting options (newest first, oldest first, by author, by type)
  - [ ] Pagination controls with page size selector
  - [ ] Document preview modal (abstract/summary display)
  - [ ] Document viewer button (links to peripheral node storage)
  - [ ] Empty state when no documents match filters
  - [ ] Loading skeleton while fetching data

##### Integration with Core Systems
- [ ] **RNDC Integration** - Query RNDC for patient documents (will be implemented later)
- [ ] **Policy Engine Integration** - Verify access permissions before displaying documents
- [ ] **Audit System Integration** - Log when patient views/accesses clinical history

##### Testing
- [ ] Unit tests for clinical history service
- [ ] Unit tests for document filtering and sorting logic
- [ ] Integration tests for REST endpoints
- [ ] UI testing (pagination, filters, sorting)

#### Implementation Notes
- **External Integration Scope**: RNDC query will use existing local data model initially; real RNDC integration will be added in later phase
- **Document Preview**: Will show abstract/summary; actual document retrieval from peripheral nodes will be implemented later
- **Performance**: Implement caching and pagination to handle large document sets
- **Initial Focus**: Service structure + UI mockup (no external integrations yet)

**Estimated Time Breakdown**:
- Backend service layer: 12 hours
- REST API endpoints: 8 hours
- Frontend UI page: 14 hours
- Testing and refinement: 6 hours
- **Total**: 36-40 hours

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
| **Policy Engine** | 🔨 **In Progress** | **60% → 100%** | **~600 → ~1,200** | **🎯 Current Sprint** |
| **Audit System** | 🔨 **In Progress** | **60% → 100%** | **~300 → ~800** | **🎯 Current Sprint** |
| Redis Caching | ⚠️ Partial | 60% | ~200 | - |

**Total Backend LOC**: ~4,900 → ~6,400 (estimated after sprint completion)

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
| Patient Clinical History | 🔨 In Progress | 0% | - |

**Total UI LOC (Completed)**: ~5,689 (INUS User Management: 1,245 LOC + Clinic Management: 2,600 LOC + Patient Pending Requests: 954 LOC + Patient Access Policies: 890 LOC)
**Total UI Completeness**: ✅ 100% (Completed Features)
**Currently In Progress**: Patient Clinical History Visualization (estimated 14 hours / 800+ LOC)

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

**Overall Completion**: ~88%
**Current Focus**: Patient Portal Features + Security & Compliance Backend + Production Deployment
**Estimated Time to Production**: 160-184 hours (patient clinical history: 36-40 hours + policy engine/audit: 40-56 hours + production deployment: 84-100 hours)
**Critical Path**: Patient Features → Security Backend → Production Deployment

**Deployment Blockers**:
1. ✅ Priority UI completion (COMPLETE - 100%)
2. ✅ Patient Pending Access Requests UI (COMPLETE - 100%)
3. ✅ Patient Access Policy Management UI (COMPLETE - 100%)
4. 🔨 Patient Clinical History UI (IN PROGRESS - 0% → 100% estimated 3-4 days)
5. 🔨 Security backend features (IN PROGRESS - Policy Engine 60%, Audit System 60%)
6. ❌ Production configuration (pending - 84-100 hours)

**Recent Milestones**:
- ✅ 2025-11-04: Patient Access Policy Management UI Complete (1,700+ LOC across 12 files)
  - RESTful API endpoints for managing patient access policies (CRUD operations)
  - Service layer with Policy Engine and Audit System integration
  - Complete JSP page with modal dialogs for policy creation/editing
  - Patient dashboard integrated with policy management link
  - All patient portal core features now 100% complete (5,689 total LOC)
- 🔨 2025-11-04: Started Clinical History Visualization (just started)
  - Focus: Backend service + REST API + JSP UI for viewing clinical documents
  - Estimated completion: 2025-11-07 (3-4 days, 36-40 hours)
  - Features: Document list, filtering (type/date/author), sorting, pagination, preview
  - Note: External RNDC/peripheral integration will be added in later phase
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
- 🔨 2025-10-30: Started Policy Engine + Audit System Sprint (40-56 hours estimated)
  - Focus: Complete core security and compliance backend features
  - Policy Engine: 60% → 100% (ABAC, RBAC, conflict resolution, time-based, emergency access)
  - Audit System: 60% → 100% (comprehensive logging, immutable storage, retention, query/export)
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

### 🔨 Current Phase: Patient Clinical History Visualization (36-40 hours)
**Start Date**: 2025-11-04
**Estimated Completion**: 2025-11-07 (3-4 days)
**Current Status**: JUST STARTED

**Focus Areas**:
1. Backend Service Layer (12 hours)
   - ClinicalHistoryService: Fetch, filter, sort, paginate documents
   - DocumentMetadataService: Retrieve metadata and validate access

2. REST API Endpoints (8 hours)
   - GET /api/patients/{ci}/history - Fetch clinical history with filters
   - GET /api/patients/{ci}/history/{documentId} - Get document details

3. Frontend UI (14 hours)
   - clinical-history.jsp - Responsive document list with filters/sorting/pagination
   - Document cards with metadata display
   - Filter controls (type, date range, search)
   - Sorting and pagination controls
   - Document preview modal and viewer link

4. Testing & Integration (6 hours)
   - Unit tests for service layer
   - Integration tests for REST endpoints
   - UI testing for filters/sorting/pagination

**Implementation Notes**:
- Initial focus: Service structure + UI mockup (no external RNDC integration yet)
- Will use existing local data model
- RNDC/peripheral node integration to be added in later phase

### 🔨 Next Phase: Security Backend Completion (40-56 hours)
**Start Date**: After Clinical History completion (2025-11-07)
**Priority**: Complete Policy Engine + Audit System

**Focus Areas**:
- Complete Policy Engine implementation (ABAC, RBAC, conflict resolution, time-based, emergency access)
- Complete Audit System implementation (comprehensive logging, immutable storage, retention, query/export APIs)
- Comprehensive testing for both systems

### Final Phase: Production Deployment (84-100 hours)
**Priority 1 - Security Backend** (Weeks 2-3):
- Policy Engine: ABAC, RBAC, conflict resolution, time-based policies, emergency access
- Audit System: Comprehensive logging, immutable storage, retention policies, query/export APIs
- Full test coverage (80%+) for both systems

**Priority 2 - Production Deployment** (Weeks 3-4):
1. Docker and WildFly production configuration
2. Environment configuration and secrets management
3. Health checks, logging, monitoring setup
4. ANTEL mi-nube integration
5. Deployment verification and smoke tests

**Priority 3 - Post-Implementation** (Optional - Advanced Features):
1. Admin Audit Dashboard UI (optional)
   - Audit log viewer with search/filter
   - Export functionality (CSV, JSON, PDF)
   - Audit timeline visualization

2. Admin Global Policies UI (optional)
   - Policy management interface
   - Policy testing/simulation tool
   - Emergency access review dashboard

**Timeline Summary**:
- Week 1: Clinical History Visualization (36-40 hours)
- Week 2-3: Security Backend Completion + Production Setup (124-156 hours total remaining)

---

## 📝 NOTES

- **SCOPE**: Focus on HCEN Central only (do NOT implement PDI mock, mobile app, clinic, or provider components)
- **OUT OF SCOPE**:
  - Professional authentication/login (belongs to Clinic/Peripheral component)
  - Mobile app development (React Native, Firebase Cloud Messaging)
  - Firebase integration (push notifications, Cloud Firestore)
  - Testing coverage requirements (80% coverage, unit tests, integration tests)
- **AUTHENTICATION SCOPE**:
  - HCEN Central: Patient authentication via gub.uy ONLY
  - Professional authentication: Handled by Clinic/Peripheral component (separate project)
- **ACCESS REQUEST WORKFLOW**: Web-based approval via patient portal JSP page (no mobile notifications)
- All communication must use HTTPS (AC002-AC004)
- Follow defined protocols: gub.uy (OIDC), PDI (SOAP), Peripheral Nodes (REST + API key)
- Always update this TODO.md after completing tasks using the general-purpose agent

**Last Review Date**: 2025-11-03 (Scope Clarifications Applied)
**Next Review Date**: 2025-11-06 (Sprint Review)
