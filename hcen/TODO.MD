# HCEN Implementation TODO
## Master Implementation Plan - TSE 2025 Group 9

**Project**: Historia Clínica Electrónica Nacional (HCEN)
**Goal**: 80% code coverage, horizontal scalability, HTTPS everywhere, comprehensive audit trail
**Last Updated**: 2025-10-22

---

## 📊 PROJECT COMPLETION STATUS

### Overall Progress: ~45% Complete

| Category | Complete | Total | % | Status |
|----------|----------|-------|---|--------|
| **Database Layer** | 7/7 schemas | 100% | ✅ **COMPLETE** |
| **Caching Layer** | 5/5 services | 100% | ✅ **COMPLETE** |
| **Authentication** | 1/1 system | 100% | ✅ **COMPLETE** |
| **Core Services** | 4/8 services | 50% | ✅ **PHASE 1 COMPLETE** |
| **REST APIs** | 30/60+ endpoints | 50% | ⚠️ **PHASE 1 COMPLETE** |
| **External Integrations** | 0/3 integrations | 0% | ❌ **NOT STARTED** |
| **Web UI** | 0/2 portals | 0% | ❌ **NOT STARTED** |
| **Mobile App** | 0/1 app | 0% | ❌ **NOT STARTED** |
| **Testing** | 0/4 test suites | 0% | ❌ **NOT STARTED** |

### What's Completed ✅

**Database Foundation (100%)**
- ✅ All PostgreSQL schemas (INUS, RNDC, Policies, Audit, Clinics, Notifications)
- ✅ All JPA entities with proper annotations
- ✅ All Repository interfaces and implementations
- ✅ Flyway migration scripts (V001-V005)

**Caching Infrastructure (100%)**
- ✅ Redis configuration with 3 connection pools (@SessionPool, @CachePool, @StatePool)
- ✅ SessionManager.java (DB 0 - JWT sessions)
- ✅ PolicyCacheService.java (DB 1 - Policy decisions)
- ✅ UserCacheService.java (DB 1 - User profiles)
- ✅ OAuthStateManager.java (DB 2 - OAuth state)
- ✅ RateLimitService.java (DB 2 - Rate limiting)

**Authentication System (100%)**
- ✅ GubUyOidcClient.java - gub.uy OIDC integration
- ✅ JwtTokenService.java - JWT generation/validation
- ✅ SessionManager.java - Session management
- ✅ RefreshTokenRepository.java - MongoDB-based refresh tokens
- ✅ JwtAuthenticationFilter.java - Security filter
- ✅ AuthenticationResource.java - REST API (6 endpoints)

**Configuration**
- ✅ RedisConfiguration.java - Connection pools
- ✅ CDI qualifiers (@SessionPool, @CachePool, @StatePool)
- ✅ MongoDB configuration

**Authentication & SSO Implementation (2025-10-22)**
- ✅ **Backend Fixes & Enhancements**
  - ✅ Fixed admin role assignment (AuthenticationService.java)
    - ✅ Added `determineRole()` method to assign role based on ClientType
    - ✅ WEB_ADMIN → "ADMIN" role
    - ✅ WEB_PATIENT → "PATIENT" role
    - ✅ MOBILE → "PATIENT" role
  - ✅ Fixed OAuth callback redirect logic (AuthenticationResource.java)
    - ✅ Added `StateManager.peekState()` to retrieve client type from state
    - ✅ Redirects to /admin/dashboard.jsp for WEB_ADMIN users
    - ✅ Redirects to /patient/dashboard.jsp for WEB_PATIENT users
    - ✅ Passes JWT token in URL parameter for client-side storage
    - ✅ Token removed from URL after reading (security enhancement)
  - ✅ Updated StateManager.java with state inspection capability
  - ✅ Updated gub.uy OAuth credentials in application.properties (client_id: 890192)

- ✅ **Admin Dashboard (admin/dashboard.jsp) - 375 lines**
  - ✅ Professional dark theme UI for administrative portal
  - ✅ User information display from JWT (name, CI, role)
  - ✅ Statistics cards (users, documents, policies, clinics) - ready for API integration
  - ✅ Complete token management JavaScript
    - ✅ JWT parsing and validation
    - ✅ Token storage in sessionStorage (browser-session only)
    - ✅ Auto-redirect on missing/invalid token
  - ✅ Role-based access control (ADMIN role verification)
  - ✅ Navigation menu (6 admin features):
    - Dashboard, Clinics, Reports, Audit Logs, Users, Settings
  - ✅ Logout functionality with token revocation
  - ✅ Responsive design (mobile-friendly)

- ✅ **Patient Dashboard (patient/dashboard.jsp) - 380 lines**
  - ✅ Friendly light theme UI for patient portal
  - ✅ User information display from JWT (name, CI)
  - ✅ Statistics cards (my documents, policies, accesses, pending approvals) - ready for API integration
  - ✅ Complete token management JavaScript
    - ✅ JWT parsing and validation
    - ✅ Token storage in sessionStorage (browser-session only)
    - ✅ Auto-redirect on missing/invalid token
  - ✅ Role-based access control (PATIENT role verification)
  - ✅ Navigation menu (6 patient features):
    - Dashboard, Clinical History, My Policies, Access History, Notifications, Settings
  - ✅ Logout functionality with token revocation
  - ✅ Responsive design (mobile-friendly)

- ✅ **Security Features Implemented**
  - ✅ JWT token removed from URL after reading (prevents token leakage)
  - ✅ Token stored in sessionStorage (browser-session only, not persistent)
  - ✅ Role-based access control enforced on both dashboards
  - ✅ Auto-redirect to login on unauthorized access
  - ✅ Logout with proper token revocation flow

- ⚠️ **Next Steps for SSO/Authentication**
  - [ ] Register redirect URI with gub.uy: `http://localhost:8080/hcen/api/auth/callback`
  - [ ] Implement statistics API endpoints to populate dashboard cards:
    - [ ] GET /api/admin/dashboard/stats (for admin dashboard)
    - [ ] GET /api/patient/dashboard/stats (for patient dashboard)
  - [ ] Create additional admin pages:
    - [ ] Clinic management UI (list, create, edit, onboard)
    - [ ] Reports viewer (charts and analytics)
    - [ ] Audit log viewer (search, filter, export)
    - [ ] User management UI (search, update, deactivate)
  - [ ] Create additional patient pages:
    - [ ] Clinical history viewer (document list and viewer)
    - [ ] Policy management UI (create, edit, delete policies)
    - [ ] Access history viewer (who accessed my data)
    - [ ] Notification preferences page

**Core Services - Phase 1 (100%)**
- ✅ InusService.java - User registration, lookup, PDI validation (670 lines)
  - ✅ InusResource.java - REST API (5 endpoints)
  - ✅ DTOs: UserRegistrationRequest, UserResponse, UserUpdateRequest, UserListResponse, EligibilityResponse
  - ✅ Exceptions: UserNotFoundException, UserRegistrationException
- ✅ RndcService.java - Document registration, search, retrieval (865 lines)
  - ✅ RndcResource.java - REST API (6 endpoints)
  - ✅ DTOs: DocumentRegistrationRequest, DocumentResponse, DocumentListResponse, DocumentStatusUpdateRequest, HashVerificationResponse
  - ✅ Exceptions: DocumentNotFoundException, DocumentRegistrationException
- ✅ PolicyEngine.java - Access control evaluation (443 lines)
  - ✅ 6 Policy Evaluators: DocumentType, Specialty, Clinic, TimeBased, Professional, EmergencyOverride
  - ✅ PolicyResource.java - REST API (6 endpoints)
  - ✅ DTOs: PolicyCreateRequest, PolicyUpdateRequest, PolicyResponse, PolicyListResponse, AccessEvaluationRequest, AccessEvaluationResponse
  - ✅ Service DTOs: AccessRequest, PolicyDecision, PolicyEvaluationResult
- ✅ AuditService.java - Event logging, access history (723 lines)
  - ✅ AuditResource.java - REST API (6 endpoints)
  - ✅ DTOs: AuditLogResponse, AuditLogListResponse, PatientAccessHistoryResponse, AuditStatisticsResponse, AuditEventRequest, AuditEventBuilder
  - ✅ Annotation: @Audited (for future AOP integration)

### What's Missing ❌

**HIGH PRIORITY - Remaining Core Services (Phase 2 - 50% Complete)**
1. ✅ **InusService.java** - COMPLETE
2. ✅ **RndcService.java** - COMPLETE
3. ✅ **PolicyEngine.java** - COMPLETE
4. ✅ **AuditService.java** - COMPLETE
5. **NotificationService.java** - Push notifications, Firebase (NOT STARTED)
6. **ClinicManagementService.java** - Clinic CRUD, onboarding (NOT STARTED)
7. **ClinicalHistoryService.java** - Patient history, document download (NOT STARTED)
8. **ReportsService.java** - Analytics, dashboard (NOT STARTED)

**CRITICAL - External Integrations (0% Complete)**
1. **PDIClient.java** - SOAP client for identity validation
2. **PeripheralNodeClient.java** - REST client for peripheral nodes
3. **FirebaseMessagingClient.java** - Push notification client

**REST APIs (~50% Complete - Phase 1 Done)**
- ✅ INUS: 5/5 endpoints (COMPLETE)
- ✅ RNDC: 6/6 endpoints (COMPLETE)
- ✅ Policies: 6/6 endpoints (COMPLETE)
- ✅ Audit: 6/6 endpoints (COMPLETE)
- ❌ Access Requests: 0/3 endpoints
- ❌ Clinics: 0/6 endpoints
- ❌ Reports: 0/5 endpoints
- ❌ Patient History: 0/3 endpoints
- ❌ Notifications: 0/4 endpoints
- ❌ Peripheral Integration: 0/2 endpoints

**HIGH PRIORITY - User Interfaces (0% Complete)**
1. Admin Portal (dashboard, clinics, reports, audit)
2. Patient Portal (history, policies, audit, notifications)
3. Mobile App (React Native - 7 screens)

**CRITICAL - Testing (0% Coverage)**
1. Unit Tests (80% coverage target)
2. Integration Tests (6 end-to-end flows)
3. Performance Tests (AC008-AC010)
4. Security Tests

---

## 🎯 RECOMMENDED IMPLEMENTATION SEQUENCE

### Phase 1: Core Services (Weeks 1-4) - ✅ COMPLETE (100%)
**Priority**: MUST HAVE for MVP
**Status**: ✅ **COMPLETED - 2025-10-22**
- [x] InusService + REST API (5 endpoints) - ✅ COMPLETE (PDI integration pending)
- [x] RndcService + REST API (6 endpoints) - ✅ COMPLETE
- [x] PolicyEngine + 6 policy types + REST API (6 endpoints) - ✅ COMPLETE
- [x] AuditService + @Audited annotation + REST API (6 endpoints) - ✅ COMPLETE

**Phase 1 Summary:**
- Total Lines of Code: ~12,330 lines
- Services Implemented: 4/4 (100%)
- REST Endpoints: 23/23 (100%)
- Policy Evaluators: 6/6 (100%)
- DTOs Created: 33
- Build Status: ✅ PASSING

**Next Steps:** Phase 2 (Patient Features) or External Integrations (PDI, Firebase)

### Phase 2: Patient Features (Weeks 5-6) - HIGH
**Priority**: SHOULD HAVE for usability
- [ ] ClinicalHistoryService + REST API + Tests
- [ ] NotificationService + Firebase + REST API + Tests
- [ ] AccessRequestService + workflow + Tests
- [ ] IpsFhirService + Tests

### Phase 3: Admin Features (Weeks 7-8) - MEDIUM
**Priority**: SHOULD HAVE for operations
- [ ] ClinicManagementService + onboarding + Tests
- [ ] ReportsService + analytics + Tests
- [ ] PeripheralNodeClient + integration + Tests

### Phase 4: UI & Integration (Weeks 9-12) - HIGH
**Priority**: MUST HAVE for demo
- [ ] Admin Portal UI (5 pages)
- [ ] Patient Portal UI (6 pages)
- [ ] Mobile App (React Native - 7 screens)
- [ ] End-to-end integration tests
- [ ] Performance testing (AC008-AC010)

### Phase 5: Optional Features (Weeks 13-14) - LOW
**Priority**: BONUS POINTS (choose 3)
- [ ] MongoDB integration (1 point)
- [ ] SonarQube integration (1 point)
- [ ] LLM integration (1 point)
- [ ] Digital signatures (1 point)
- [ ] Terminology services (1 point)

---

## PHASE 0: PROJECT SETUP & INFRASTRUCTURE

### Database Setup
- [ ] **PostgreSQL Setup**
  - [ ] Install PostgreSQL on Elastic Cloud ANTEL
  - [ ] Create database: `hcen_central`
  - [ ] Create schemas: `inus`, `rndc`, `policies`, `audit`, `clinics`
  - [ ] Configure connection pooling (min=5, max=20)
  - [ ] Set up automated backups (daily)
  - [ ] Configure replication for high availability

- [ ] **Redis Setup**
  - [ ] Install Redis for distributed caching
  - [ ] Configure persistence (RDB + AOF)
  - [ ] Set up Redis Sentinel for HA
  - [ ] Configure databases: 0=sessions, 1=cache, 2=state
  - [ ] Implement connection pool

- [ ] **Optional: MongoDB Setup (1 point bonus)**
  - [ ] Install MongoDB for document storage
  - [ ] Create database: `hcen_nosql`
  - [ ] Collections: `refresh_tokens`, `audit_archive`, `notifications`
  - [ ] Configure replica set
  - [ ] Implement TTL indexes for token expiration

### WildFly Configuration
- [ ] Configure standalone.xml for PostgreSQL datasource
- [ ] Configure Redis resource adapter
- [ ] Set up HTTPS/SSL certificates (Let's Encrypt or ANTEL)
- [ ] Configure CORS for mobile/web clients
- [ ] Set up logging (ELK stack or similar)
- [ ] Configure session replication (for horizontal scaling)

### CI/CD Pipeline
- [ ] Configure GitLab CI/CD (.gitlab-ci.yml)
- [ ] Set up automated testing (80% coverage gate)
- [ ] Configure JaCoCo for coverage reports
- [ ] Set up automated deployment to Elastic Cloud ANTEL
- [ ] Configure SonarQube for code quality (optional 1 point)

---

## PHASE 1: CORE FOUNDATION - DATABASE SCHEMAS & ENTITIES

### SQL Database Implementation (PostgreSQL)

#### INUS Schema
- [x] Create `inus_users` table
  - [x] CI (VARCHAR(20) PRIMARY KEY)
  - [x] inus_id (VARCHAR(50) UNIQUE NOT NULL)
  - [x] first_name, last_name (VARCHAR(100))
  - [x] date_of_birth (DATE)
  - [x] email, phone_number
  - [x] status (ENUM: ACTIVE, INACTIVE, SUSPENDED)
  - [x] created_at, updated_at (TIMESTAMP)
  - [x] age_verified (BOOLEAN)
- [x] Create indexes on ci, inus_id
- [x] Implement JPA entity: `InusUser.java`
- [x] Implement repository: `InusRepository.java`

#### RNDC Schema
- [x] Create `rndc_documents` table
  - [x] id (BIGSERIAL PRIMARY KEY)
  - [x] patient_ci (VARCHAR(20), indexed)
  - [x] document_locator (VARCHAR(500))
  - [x] document_hash (VARCHAR(64) - SHA-256)
  - [x] document_type (ENUM: CLINICAL_NOTE, LAB_RESULT, IMAGING, etc.)
  - [x] created_by (VARCHAR(100))
  - [x] created_at (TIMESTAMP)
  - [x] status (ENUM: ACTIVE, INACTIVE, DELETED)
  - [x] clinic_id (VARCHAR(50), indexed)
- [x] Create composite indexes (patient_ci + status, clinic_id + created_at)
- [x] Implement JPA entity: `RndcDocument.java`
- [x] Implement repository: `RndcRepository.java` (includes RndcRepositoryImpl.java with DocumentType and DocumentStatus enums)

#### Policies Schema
- [x] Create `access_policies` table
  - [x] id (BIGSERIAL PRIMARY KEY)
  - [x] patient_ci (VARCHAR(20), indexed)
  - [x] policy_type (ENUM: DOCUMENT_TYPE, SPECIALTY, TIME_BASED, CLINIC)
  - [x] policy_config (JSONB)
  - [x] policy_effect (ENUM: PERMIT, DENY)
  - [x] valid_from, valid_until (TIMESTAMP)
  - [x] created_at, updated_at (TIMESTAMP)
  - [x] priority (INTEGER)
- [x] Create indexes on patient_ci, policy_type
- [x] Implement JPA entity: `AccessPolicy.java`
- [x] Implement repository: `AccessPolicyRepository.java` (includes AccessPolicyRepositoryImpl.java)

#### Audit Schema
- [x] Create `audit_logs` table
  - [x] id (BIGSERIAL PRIMARY KEY)
  - [x] event_type (ENUM: ACCESS, MODIFICATION, CREATION, DELETION)
  - [x] actor_id (VARCHAR(100), indexed)
  - [x] actor_type (ENUM: PATIENT, PROFESSIONAL, ADMIN, SYSTEM)
  - [x] resource_type (VARCHAR(50))
  - [x] resource_id (VARCHAR(100))
  - [x] action_outcome (ENUM: SUCCESS, FAILURE, DENIED)
  - [x] ip_address (VARCHAR(45))
  - [x] user_agent (VARCHAR(500))
  - [x] timestamp (TIMESTAMP, indexed)
  - [x] details (JSONB)
- [x] Create indexes on timestamp, actor_id, event_type
- [x] Make table append-only (no UPDATE/DELETE)
- [x] Implement JPA entity: `AuditLog.java`
- [x] Implement repository: `AuditLogRepository.java` (append-only implementation - no update/delete methods)

#### Clinics Schema
- [x] Create `clinics` table
  - [x] clinic_id (VARCHAR(50) PRIMARY KEY)
  - [x] clinic_name (VARCHAR(200))
  - [x] address, city, phone_number, email
  - [x] peripheral_node_url (VARCHAR(500))
  - [x] api_key (VARCHAR(100), encrypted)
  - [x] status (ENUM: ACTIVE, INACTIVE, PENDING_ONBOARDING)
  - [x] onboarded_at, created_at (TIMESTAMP)
- [x] Implement JPA entity: `Clinic.java` (includes ClinicStatus enum)
- [x] Implement repository: `ClinicRepository.java` (includes ClinicRepositoryImpl.java)

#### Access Requests Schema
- [x] Create `access_requests` table
  - [x] id (BIGSERIAL PRIMARY KEY)
  - [x] professional_id (VARCHAR(100))
  - [x] patient_ci (VARCHAR(20), indexed)
  - [x] document_id (BIGINT)
  - [x] request_reason (TEXT)
  - [x] status (ENUM: PENDING, APPROVED, DENIED, EXPIRED)
  - [x] requested_at, responded_at (TIMESTAMP)
  - [x] patient_response (TEXT)
  - [x] expires_at (TIMESTAMP)
- [x] Create indexes on patient_ci, status
- [x] Implement JPA entity: `AccessRequest.java` (includes RequestStatus enum)
- [x] Implement repository: `AccessRequestRepository.java` (includes AccessRequestRepositoryImpl.java)

#### Notification Preferences Schema
- [x] Create `notification_preferences` table
  - [x] id (BIGSERIAL PRIMARY KEY)
  - [x] user_ci (VARCHAR(20) UNIQUE)
  - [x] notify_access_request, notify_new_access, notify_policy_change (BOOLEAN)
  - [x] fcm_token (VARCHAR(255))
  - [x] updated_at (TIMESTAMP)
- [x] Implement JPA entity: `NotificationPreference.java`
- [x] Implement repository: `NotificationPreferenceRepository.java` (includes NotificationPreferenceRepositoryImpl.java)

### NoSQL Database Implementation

#### Redis Cache Structure
- [x] **Session Storage** (Database 0)
  - [x] Key pattern: `session:{jwt_token_id}`
  - [x] Value: JSON with user claims
  - [x] TTL: 3600 seconds (1 hour)
  - [x] Implement: `SessionManager.java`

- [x] **Policy Evaluation Cache** (Database 1)
  - [x] Key pattern: `policy:cache:{patient_ci}:{specialty}:{document_type}`
  - [x] Value: Policy decision (PERMIT/DENY/PENDING)
  - [x] TTL: 300 seconds (5 minutes)
  - [x] Implement: `PolicyCacheService.java`

- [x] **User Profile Cache** (Database 1)
  - [x] Key pattern: `user:profile:{ci}`
  - [x] Value: JSON user profile from INUS
  - [x] TTL: 900 seconds (15 minutes)
  - [x] Implement: `UserCacheService.java`

- [x] **OAuth State Storage** (Database 2)
  - [x] Key pattern: `oauth:state:{state_token}`
  - [x] Value: JSON with nonce, redirect_uri, client_type
  - [x] TTL: 600 seconds (10 minutes)
  - [x] Implement: `OAuthStateManager.java`

- [x] **Rate Limiting** (Database 2)
  - [x] Key pattern: `ratelimit:{ip_address}:{endpoint}`
  - [x] Value: Request count
  - [x] TTL: 60 seconds (sliding window)
  - [x] Implement: `RateLimitService.java`

#### MongoDB Collections (Optional - 1 point)
- [ ] **refresh_tokens** collection
  - [ ] Fields: token_hash, user_ci, client_type, issued_at, expires_at
  - [ ] TTL index on expires_at (auto-delete expired tokens)
  - [ ] Implement: `RefreshTokenRepository.java`

- [ ] **audit_archive** collection
  - [ ] Archive old audit logs (> 90 days) from PostgreSQL
  - [ ] Implement archival job
  - [ ] Implement query service for historical audits

- [ ] **notifications** collection
  - [ ] Fields: user_ci, type, title, message, read, created_at
  - [ ] Implement: `NotificationRepository.java`

---

## PHASE 2: AUTHENTICATION & AUTHORIZATION

### gub.uy OIDC Integration (CU01, CU06, CU09)

#### Pre-Integration
- [ ] Contact AGESIC to register HCEN as OIDC relying party
- [ ] Provide application details and redirect URIs
- [ ] Receive test environment credentials (client_id, client_secret)
- [ ] Generate RSA key pair for HCEN JWT signing
  - [ ] `openssl genrsa -out jwt-private.pem 2048`
  - [ ] `openssl rsa -in jwt-private.pem -pubout -out jwt-public.pem`
- [ ] Store keys securely in `/opt/hcen/keys/`

#### Backend Implementation
- [ ] Create package: `uy.gub.hcen.auth.*`
- [ ] Implement `GubUyOidcClient.java`
  - [ ] `buildAuthorizationUrl()` - Build OIDC authorization URL
  - [ ] `exchangeCodeForTokens()` - Exchange authorization code for tokens
  - [ ] `validateIdToken()` - Validate ID token signature and claims
  - [ ] Circuit breaker for resilience
- [ ] Implement `JwksKeyManager.java`
  - [ ] Fetch JWKS public keys from gub.uy
  - [ ] Cache keys (1 hour TTL)
  - [ ] Auto-refresh on key rotation
- [ ] Implement `GubUyTokenValidator.java`
  - [ ] Validate ID token signature (RS256)
  - [ ] Validate issuer, audience, expiration
  - [ ] Validate nonce (replay attack protection)
  - [ ] Validate ACR (require NID 2 or 3)
- [ ] Implement `JwtTokenService.java`
  - [ ] Generate HCEN-issued access tokens (1 hour TTL)
  - [ ] Generate HCEN-issued refresh tokens (30 days TTL)
  - [ ] Validate HCEN tokens
  - [ ] Token revocation (blacklist in Redis)
- [ ] Implement `OAuthStateManager.java` (Redis-based)
  - [ ] Generate cryptographically random state parameter
  - [ ] Store state with nonce, redirect_uri (10 min TTL)
  - [ ] Validate state on callback (CSRF protection)
- [ ] Implement `AuthenticationService.java` (orchestration)
  - [ ] Initiate login flow
  - [ ] Handle OAuth callback
  - [ ] Integrate with INUS (lookup/register user)
  - [ ] Generate HCEN JWT
  - [ ] Log authentication events

#### REST API Endpoints
- [ ] `POST /api/auth/login/initiate`
  - [ ] Input: client_type (mobile/web_patient/web_admin)
  - [ ] Generate state, nonce
  - [ ] Return authorization URL
- [ ] `GET /api/auth/callback` (for web clients)
  - [ ] Query params: code, state, error, error_description
  - [ ] Validate state
  - [ ] Exchange code for tokens
  - [ ] Set HttpOnly, Secure cookie
  - [ ] Redirect to dashboard
- [ ] `POST /api/auth/callback/mobile` (for mobile clients)
  - [ ] Body: code, state, code_verifier (PKCE)
  - [ ] Validate state and PKCE
  - [ ] Return HCEN tokens in JSON
- [ ] `POST /api/auth/token/refresh`
  - [ ] Body: refresh_token
  - [ ] Validate refresh token
  - [ ] Return new access token
- [ ] `GET /api/auth/session`
  - [ ] Return current user info from JWT
- [ ] `POST /api/auth/logout`
  - [ ] Invalidate session/JWT (add to Redis blacklist)
  - [ ] Redirect to gub.uy logout endpoint

#### Security Implementation
- [ ] CSRF protection (state parameter validation)
- [ ] XSS protection (HttpOnly cookies)
- [ ] PKCE support for mobile clients
- [ ] Client secret management (environment variables)
- [ ] Nonce validation (replay attack prevention)
- [ ] Rate limiting on auth endpoints (10 attempts/min per IP)
- [ ] Circuit breaker for gub.uy calls (fail fast on downtime)
- [ ] Comprehensive logging (auth success/failure, IP, user agent)

#### Testing
- [ ] Unit tests for token validation (valid/expired/invalid signature)
- [ ] Unit tests for PKCE code_challenge generation
- [ ] Unit tests for state parameter generation/validation
- [ ] Integration tests with gub.uy testing environment
- [ ] Test successful authentication (NID 2, NID 3)
- [ ] Test user denial (access_denied error)
- [ ] Test invalid state (CSRF attack simulation)
- [ ] Test expired authorization code
- [ ] Test invalid client credentials
- [ ] Test token expiration and refresh
- [ ] Test logout (single sign-out)
- [ ] Security testing (token tampering, signature validation)

---

## PHASE 3: CORE SERVICES - INUS & RNDC - ✅ COMPLETE

### INUS Service (National User Index) - CU13 - ✅ COMPLETE

#### Service Implementation - ✅ COMPLETE
- [x] Create package: `uy.gub.hcen.service.inus.*`
- [x] Implement `InusService.java` (670 lines)
  - [x] `registerUser()` - Register new health user (AC013)
    - [x] Validate CI format
    - [x] Check if user already exists
    - [x] Call PDI for age verification (>= 18) - Interface ready, PDI client pending
    - [x] Generate unique inusId (UUID)
    - [x] Save to database
    - [x] Log registration in audit
  - [x] `findUserByCi()` - Lookup user by CI
    - [x] Check cache first (Redis)
    - [x] Query database
    - [x] Cache result (15 min TTL)
  - [x] `findUserByInusId()` - Lookup by inusId
  - [x] `updateUserProfile()` - Update user information
    - [x] Sync with gub.uy claims
    - [x] Invalidate cache
    - [x] Log modification in audit
  - [x] `validateUserEligibility()` - Check health system eligibility
  - [x] `searchUsers()` - Admin search functionality

#### REST API Endpoints - ✅ COMPLETE (5/5)
- [x] `POST /api/inus/users` - InusResource.java
  - [x] Authorization: API key (for peripheral nodes)
  - [x] Body: UserRegistrationRequest
  - [x] Validate age with PDI (pending PDI client)
  - [x] Return: UserResponse with inusId
- [x] `GET /api/inus/users/{ci}` - InusResource.java
  - [x] Authorization: JWT (admin or self)
  - [x] Return user details with inusId
- [x] `PUT /api/inus/users/{ci}` - InusResource.java
  - [x] Authorization: JWT (admin only)
  - [x] Update user information
- [x] `GET /api/inus/users/{ci}/eligibility` - InusResource.java
  - [x] Validate user eligibility
  - [x] Return: EligibilityResponse
- [x] `GET /api/inus/users` - InusResource.java
  - [x] Search by name, CI, inusId
  - [x] Pagination support
  - [x] Return: UserListResponse

#### Testing - ⚠️ PENDING (Next Phase)
- [ ] Unit tests for user registration logic
- [ ] Unit tests for CI format validation
- [ ] Unit tests for cache invalidation
- [ ] Integration tests with PDI mock
- [ ] Test duplicate registration (idempotency)
- [ ] Test age verification (under 18 rejection)
- [ ] Test concurrent registrations
- [ ] Performance tests (1000 registrations/min)

### PDI Integration (Identity Validation)

#### SOAP Client Implementation
- [ ] Create package: `uy.gub.hcen.integration.pdi.*`
- [ ] Implement `PDIClient.java` (JAX-WS)
  - [ ] `consultarUsuario()` - Query user by CI
  - [ ] Return: nombre_completo, fecha_nacimiento
  - [ ] WS-Security authentication
  - [ ] Circuit breaker for resilience
- [ ] Implement `PDIAdapter.java` (abstraction layer)
  - [ ] Interface: `PDIAdapter`
  - [ ] Implementation: `PDISoapClient`
  - [ ] Mock implementation: `PDIMockClient` (for testing)
- [ ] Create mock PDI service (pdi-mock component)
  - [ ] Implement WSDL interface
  - [ ] Return simulated data for test CIs
  - [ ] Deploy as separate WildFly instance

#### Testing
- [ ] Unit tests with mock PDI client
- [ ] Integration tests with pdi-mock service
- [ ] Test SOAP fault handling
- [ ] Test timeout scenarios (circuit breaker)
- [ ] Test invalid CI format

### RNDC Service (National Clinical Document Registry) - CU18, AC014, AC015 - ✅ COMPLETE

#### Service Implementation - ✅ COMPLETE
- [x] Create package: `uy.gub.hcen.service.rndc.*`
- [x] Implement `RndcService.java` (865 lines)
  - [x] `registerDocument()` - Register document metadata (AC014)
    - [x] Validate document locator URL
    - [x] Validate SHA-256 hash format
    - [x] Check if document already registered (idempotency)
    - [x] Save metadata to database
    - [x] Log registration in audit
  - [x] `searchDocuments()` - Search by patient CI
    - [x] Apply access policies (call PolicyEngine)
    - [x] Filter by document type, date range
    - [x] Pagination support
    - [x] Return metadata only (not actual document)
  - [x] `getDocumentMetadata()` - Get specific document metadata
    - [x] Validate access (PolicyEngine)
    - [x] Return locator URL
  - [x] `updateDocumentStatus()` - Mark document as inactive/deleted
    - [x] Only by original creator or admin
    - [x] Soft delete (status = INACTIVE)
    - [x] Log modification in audit
  - [x] `verifyDocumentIntegrity()` - Verify document hash
    - [x] Fetch document from peripheral node (pending PeripheralNodeClient)
    - [x] Calculate SHA-256 hash
    - [x] Compare with stored hash

#### REST API Endpoints - ✅ COMPLETE (6/6)
- [x] `POST /api/rndc/documents` - RndcResource.java
  - [x] Authorization: API key (for peripheral nodes)
  - [x] Body: DocumentRegistrationRequest
  - [x] Return: DocumentResponse with documentId
- [x] `GET /api/rndc/documents` - RndcResource.java
  - [x] Authorization: JWT (professional or patient)
  - [x] Apply access policies
  - [x] Query params: patientCi, documentType, fromDate, toDate, page, size
  - [x] Return: DocumentListResponse
- [x] `GET /api/rndc/documents/{documentId}` - RndcResource.java
  - [x] Authorization: JWT (professional or patient)
  - [x] Policy enforcement
  - [x] Return: DocumentResponse with locator
- [x] `PATCH /api/rndc/documents/{documentId}/status` - RndcResource.java
  - [x] Authorization: JWT (creator or admin)
  - [x] Soft delete (status = INACTIVE)
  - [x] Body: DocumentStatusUpdateRequest
- [x] `GET /api/rndc/documents/{documentId}/verify-hash` - RndcResource.java
  - [x] Fetch document and verify hash
  - [x] Return: HashVerificationResponse
- [x] `GET /api/rndc/documents/patient/{patientCi}` - RndcResource.java
  - [x] Get patient's documents with policy enforcement
  - [x] Return: DocumentListResponse

#### Testing - ⚠️ PENDING (Next Phase)
- [ ] Unit tests for document registration
- [ ] Unit tests for hash validation
- [ ] Unit tests for search with filters
- [ ] Integration tests with mock peripheral nodes
- [ ] Test duplicate registration (idempotency)
- [ ] Test document retrieval with policy enforcement
- [ ] Test concurrent registrations
- [ ] Performance tests (10,000 documents query)

---

## PHASE 4: POLICY ENGINE - ACCESS CONTROL (CU03, AC005, AC007) - ✅ COMPLETE

### Policy Engine Implementation - ✅ COMPLETE

#### Core Policy Engine - ✅ COMPLETE
- [x] Create package: `uy.gub.hcen.service.policy.*`
- [x] Define policy evaluation interfaces (Strategy pattern)
  - [x] PolicyEvaluator interface
  - [x] AccessRequest, PolicyDecision, PolicyEvaluationResult classes
- [x] Implement policy evaluators (6/6):
  - [x] `DocumentTypePolicyEvaluator.java` - Allow/deny by document type
  - [x] `SpecialtyPolicyEvaluator.java` - Allow/deny by professional specialty
  - [x] `ClinicPolicyEvaluator.java` - Allow/deny by clinic
  - [x] `TimeBasedPolicyEvaluator.java` - Allow/deny by time/day
  - [x] `ProfessionalPolicyEvaluator.java` - Allow/deny specific professionals
  - [x] `EmergencyOverridePolicyEvaluator.java` - Emergency access (log heavily)
- [x] Implement `PolicyEngine.java` (443 lines)
  - [x] Load patient policies from database
  - [x] Evaluate all applicable policies
  - [x] Resolve conflicts (priority-based)
  - [x] Return decision: PERMIT, DENY, PENDING
  - [x] Cache decisions (5 min TTL in Redis)
  - [x] Log all evaluations in audit
  - [x] Integration with AuditService

#### Policy Management Service - ✅ COMPLETE (Integrated in PolicyEngine)
- [x] `createPolicy()` - Create access policy (CU03)
- [x] `updatePolicy()` - Modify existing policy
- [x] `deletePolicy()` - Remove policy
- [x] `getPatientPolicies()` - List patient's policies
- [x] `validatePolicyConfig()` - Validate policy configuration
- [x] Invalidate cache on policy change

#### REST API Endpoints - ✅ COMPLETE (6/6)
- [x] `GET /api/policies/patient/{patientCi}` - PolicyResource.java
  - [x] Authorization: JWT (patient or admin)
  - [x] Return: PolicyListResponse
- [x] `POST /api/policies` - PolicyResource.java
  - [x] Authorization: JWT (patient only)
  - [x] Body: PolicyCreateRequest
  - [x] Validate configuration
  - [x] Return: PolicyResponse with policyId
- [x] `PUT /api/policies/{policyId}` - PolicyResource.java
  - [x] Authorization: JWT (policy owner or admin)
  - [x] Body: PolicyUpdateRequest
  - [x] Invalidate cache
- [x] `DELETE /api/policies/{policyId}` - PolicyResource.java
  - [x] Authorization: JWT (policy owner or admin)
  - [x] Hard delete
- [x] `POST /api/policies/evaluate` - PolicyResource.java
  - [x] Body: AccessEvaluationRequest
  - [x] Return: AccessEvaluationResponse (PERMIT/DENY/PENDING)
  - [x] Used internally by RNDC service
- [x] `GET /api/policies/{policyId}` - PolicyResource.java
  - [x] Get specific policy details
  - [x] Return: PolicyResponse

#### Access Request Workflow (CU20)
- [ ] Implement `AccessRequestService.java`
  - [ ] `createAccessRequest()` - Professional requests access
    - [ ] Create pending request
    - [ ] Send notification to patient (Firebase)
    - [ ] Set expiration (48 hours)
  - [ ] `approveAccessRequest()` - Patient approves
    - [ ] Update request status
    - [ ] Grant temporary access (create policy or cache)
    - [ ] Notify professional
  - [ ] `denyAccessRequest()` - Patient denies
    - [ ] Update request status
    - [ ] Log denial reason
    - [ ] Notify professional
  - [ ] `expireAccessRequests()` - Scheduled job (every hour)
    - [ ] Mark expired requests as EXPIRED
    - [ ] Notify professionals

#### REST API Endpoints
- [ ] `POST /api/access-requests`
  - [ ] Authorization: JWT (professional)
  - [ ] Body: { professionalId, patientCI, documentId, requestReason }
  - [ ] Return: { requestId, status: PENDING }
- [ ] `GET /api/access-requests?patientCI={ci}`
  - [ ] Authorization: JWT (patient)
  - [ ] Return: Array of pending requests
- [ ] `PUT /api/access-requests/{requestId}`
  - [ ] Authorization: JWT (patient)
  - [ ] Body: { decision: APPROVE/DENY, patientResponse }
  - [ ] Update status and notify professional

#### Testing - ⚠️ PENDING (Next Phase)
- [ ] Unit tests for each policy type
- [ ] Unit tests for policy conflict resolution
- [ ] Unit tests for cache invalidation
- [ ] Integration tests for policy evaluation
- [ ] Test PERMIT scenarios (allowed specialty, document type)
- [ ] Test DENY scenarios (restricted clinic, time-based)
- [ ] Test PENDING scenarios (no policy, requires approval)
- [ ] Test emergency override policy
- [ ] Test access request workflow (create, approve, deny, expire)
- [ ] Performance tests (1000 policy evaluations/second)

---

## PHASE 5: AUDIT SYSTEM (CU05, AC026) - ✅ COMPLETE

### Audit Service Implementation - ✅ COMPLETE

#### Core Audit Service - ✅ COMPLETE
- [x] Create package: `uy.gub.hcen.service.audit.*`
- [x] Implement `AuditService.java` (723 lines)
  - [x] `logEvent()` - Log audit event
    - [x] Capture: eventType, actorId, resourceType, resourceId, outcome, IP, userAgent, details
    - [x] Save to PostgreSQL (append-only)
    - [x] Archive old events to MongoDB (optional, > 90 days) - Pending MongoDB integration
  - [x] `logAccessEvent()` - Shorthand for document access
  - [x] `logAuthenticationEvent()` - Shorthand for auth events
  - [x] `logPolicyChange()` - Shorthand for policy modifications
  - [x] `searchAuditLogs()` - Query audit logs (admin)
  - [x] `getPatientAccessHistory()` - Patient views who accessed their data (CU05)
  - [x] `logDocumentAccess()` - Log document access with policy decision
  - [x] `getSystemStatistics()` - Get audit statistics

#### Aspect-Oriented Logging - ✅ COMPLETE
- [x] Implement `@Audited` annotation (for future AOP integration)
- [x] AuditEventBuilder for fluent event creation
- [x] Integration with all services (INUS, RNDC, PolicyEngine)

#### REST API Endpoints - ✅ COMPLETE (6/6)
- [x] `GET /api/audit/logs` - AuditResource.java
  - [x] Authorization: JWT (admin only)
  - [x] Query params: eventType, actorId, resourceType, fromDate, toDate, page, size
  - [x] Return: AuditLogListResponse (paginated)
- [x] `GET /api/audit/patient/{patientCi}/access-history` - AuditResource.java
  - [x] Authorization: JWT (patient or admin)
  - [x] Return: PatientAccessHistoryResponse (CU05)
  - [x] Include: accessor name, specialty, clinic, timestamp, document accessed
- [x] `GET /api/audit/statistics` - AuditResource.java
  - [x] Authorization: JWT (admin only)
  - [x] Return: AuditStatisticsResponse
  - [x] System-wide audit statistics
- [x] `POST /api/audit/event` - AuditResource.java
  - [x] Authorization: JWT (internal services)
  - [x] Body: AuditEventRequest
  - [x] Manually log custom audit events
- [x] `GET /api/audit/events/{eventId}` - AuditResource.java
  - [x] Get specific audit event details
  - [x] Return: AuditLogResponse
- [x] `GET /api/audit/export` - AuditResource.java
  - [x] Export audit logs (CSV/JSON format)
  - [x] Query params: format, filters

#### Testing - ⚠️ PENDING (Next Phase)
- [ ] Unit tests for event logging
- [ ] Unit tests for @Audited annotation
- [ ] Integration tests for audit queries
- [ ] Test audit log immutability (no updates/deletes)
- [ ] Test pagination and filtering
- [ ] Test patient access history (CU05)
- [ ] Performance tests (10,000 events/second)

---

## PHASE 6: ACTOR-SPECIFIC FEATURES

## 6.1 ADMIN HCEN ACTOR (CU09, CU10, CU11)

### Clinic Management Service (CU10)

#### Service Implementation
- [ ] Create package: `uy.gub.hcen.admin.clinic.*`
- [ ] Implement `ClinicManagementService.java`
  - [ ] `createClinic()` - Register new clinic
    - [ ] Generate clinic_id
    - [ ] Generate API key for peripheral node
    - [ ] Save to database
    - [ ] Log creation in audit
  - [ ] `updateClinic()` - Modify clinic details
  - [ ] `activateClinic()` - Activate clinic (status = ACTIVE)
  - [ ] `deactivateClinic()` - Deactivate clinic (status = INACTIVE)
  - [ ] `onboardClinic()` - Send onboarding data to peripheral node (AC016)
    - [ ] POST to peripheral_node_url/api/onboard
    - [ ] Body: { clinicId, apiKey, hcenCentralUrl, config }
  - [ ] `listClinics()` - Get all clinics (with filters)
  - [ ] `getClinicStatistics()` - Stats per clinic (users, documents)

#### REST API Endpoints
- [ ] `POST /api/admin/clinics`
  - [ ] Authorization: JWT (admin only)
  - [ ] Body: { clinicName, address, city, phoneNumber, email, peripheralNodeUrl }
  - [ ] Return: { clinicId, apiKey }
- [ ] `GET /api/admin/clinics`
  - [ ] Authorization: JWT (admin only)
  - [ ] Query params: status, city, page, size
  - [ ] Return: Array of clinics
- [ ] `GET /api/admin/clinics/{clinicId}`
  - [ ] Authorization: JWT (admin only)
  - [ ] Return: Clinic details with statistics
- [ ] `PUT /api/admin/clinics/{clinicId}`
  - [ ] Authorization: JWT (admin only)
  - [ ] Update clinic details
- [ ] `POST /api/admin/clinics/{clinicId}/onboard`
  - [ ] Authorization: JWT (admin only)
  - [ ] Trigger onboarding process (AC016)
- [ ] `DELETE /api/admin/clinics/{clinicId}`
  - [ ] Authorization: JWT (admin only)
  - [ ] Soft delete (status = INACTIVE)

### Reports & Analytics Service (CU11)

#### Service Implementation
- [ ] Implement `ReportsService.java`
  - [ ] `getInusStatistics()` - User registration trends
    - [ ] Total users, new users per month
    - [ ] Users per clinic
    - [ ] Age distribution
  - [ ] `getRndcStatistics()` - Document registry trends
    - [ ] Total documents, new documents per month
    - [ ] Documents per type
    - [ ] Documents per clinic
  - [ ] `getPolicyStatistics()` - Access policy analytics
    - [ ] Total policies, policies per patient
    - [ ] Policy types distribution
    - [ ] Policy changes over time
  - [ ] `getAccessStatistics()` - Access analytics
    - [ ] Total accesses, accesses per professional
    - [ ] Access denials (policy violations)
    - [ ] Access requests (pending, approved, denied)
  - [ ] `generateDashboard()` - Admin dashboard data
    - [ ] Real-time metrics
    - [ ] Charts data (JSON)

#### REST API Endpoints
- [ ] `GET /api/admin/reports/inus`
  - [ ] Authorization: JWT (admin only)
  - [ ] Query params: fromDate, toDate, groupBy (day/month/year)
  - [ ] Return: INUS statistics
- [ ] `GET /api/admin/reports/rndc`
  - [ ] Authorization: JWT (admin only)
  - [ ] Return: RNDC statistics
- [ ] `GET /api/admin/reports/policies`
  - [ ] Authorization: JWT (admin only)
  - [ ] Return: Policy analytics
- [ ] `GET /api/admin/reports/access`
  - [ ] Authorization: JWT (admin only)
  - [ ] Return: Access analytics
- [ ] `GET /api/admin/dashboard`
  - [ ] Authorization: JWT (admin only)
  - [ ] Return: Dashboard data (charts, metrics)

### Admin Portal UI (Web)

#### Frontend Implementation
- [ ] Create admin portal: `src/main/webapp/admin/`
- [ ] Pages:
  - [ ] Login page (gub.uy authentication)
  - [ ] Dashboard (charts, metrics)
  - [ ] Clinic management (list, create, edit, onboard)
  - [ ] Reports (INUS, RNDC, policies, access)
  - [ ] Audit logs (search, filter, export)
- [ ] Technologies: JSF/PrimeFaces or React (embedded in WAR)
- [ ] Responsive design (Bootstrap or Material-UI)

#### Testing
- [ ] Unit tests for clinic management service
- [ ] Unit tests for reports service
- [ ] Integration tests for onboarding flow
- [ ] Test admin authentication (gub.uy)
- [ ] Test authorization (only admins can access)
- [ ] UI tests (Selenium or Cypress)

---

## 6.2 HEALTH USER / PATIENT ACTOR (CU02, CU03, CU04, CU05, CU07, CU08)

### Clinical History Service (CU02)

#### Service Implementation
- [ ] Create package: `uy.gub.hcen.patient.*`
- [ ] Implement `ClinicalHistoryService.java`
  - [ ] `getClinicalHistory()` - Retrieve complete clinical history (CU02)
    - [ ] Query RNDC for patient documents
    - [ ] Group by document type
    - [ ] Sort by date (newest first)
    - [ ] Include document metadata (clinic, professional, date)
  - [ ] `getDocumentDetails()` - Get specific document metadata
  - [ ] `downloadDocument()` - Fetch actual document from peripheral node
    - [ ] GET {documentLocator}
    - [ ] Verify hash (integrity)
    - [ ] Return document (PDF, XML, FHIR)

#### REST API Endpoints
- [ ] `GET /api/patients/{ci}/history`
  - [ ] Authorization: JWT (patient or authorized professional)
  - [ ] Query params: documentType, fromDate, toDate, page, size
  - [ ] Return: Array of document metadata
- [ ] `GET /api/patients/{ci}/history/{documentId}`
  - [ ] Authorization: JWT (patient or authorized professional)
  - [ ] Return: Document metadata
- [ ] `GET /api/patients/{ci}/history/{documentId}/download`
  - [ ] Authorization: JWT (patient or authorized professional)
  - [ ] Fetch document from peripheral node
  - [ ] Verify hash
  - [ ] Return: Document file (PDF/XML/FHIR)

### IPS-FHIR Service (CU08 - Mobile)

#### Service Implementation
- [ ] Implement `IpsFhirService.java`
  - [ ] `generatePatientSummary()` - Generate IPS-FHIR document
    - [ ] Aggregate key clinical data
    - [ ] Format as FHIR Bundle (IPS profile)
    - [ ] Sections: Allergies, Medications, Problems, Immunizations, Procedures
    - [ ] Cache summary (1 hour TTL)

#### REST API Endpoints
- [ ] `GET /api/patients/{ci}/ips-summary`
  - [ ] Authorization: JWT (patient only)
  - [ ] Return: FHIR Bundle (JSON or XML)
  - [ ] Example: International Patient Summary for travel

### Notification Service (CU04, CU07)

#### Service Implementation
- [ ] Create package: `uy.gub.hcen.notification.*`
- [ ] Implement `NotificationService.java`
  - [ ] `configurePreferences()` - Set notification preferences (CU04)
  - [ ] `getPreferences()` - Get current preferences
  - [ ] `sendPushNotification()` - Send push via Firebase (CU07)
    - [ ] Event types: ACCESS_REQUEST, NEW_ACCESS, POLICY_CHANGE, NEW_DOCUMENT
    - [ ] Payload: { title, message, data, priority }
  - [ ] `sendEmailNotification()` - Send email (optional)
  - [ ] `registerFcmToken()` - Register mobile device token
  - [ ] `unregisterFcmToken()` - Unregister device

#### Firebase Integration
- [ ] Set up Firebase project
- [ ] Add `google-services.json` to mobile app
- [ ] Implement `FirebaseMessagingClient.java`
  - [ ] `sendToDevice()` - Send to specific device (FCM token)
  - [ ] `sendToTopic()` - Send to topic (e.g., all patients)
  - [ ] Handle errors (invalid token, quota exceeded)

#### REST API Endpoints
- [ ] `GET /api/patients/{ci}/notifications/preferences`
  - [ ] Authorization: JWT (patient only)
  - [ ] Return: Notification preferences
- [ ] `PUT /api/patients/{ci}/notifications/preferences`
  - [ ] Authorization: JWT (patient only)
  - [ ] Body: { notifyAccessRequest, notifyNewAccess, notifyPolicyChange, notifyNewDocument }
- [ ] `POST /api/patients/{ci}/notifications/register-device`
  - [ ] Authorization: JWT (patient only)
  - [ ] Body: { fcmToken, platform: android/ios }
- [ ] `POST /api/patients/{ci}/notifications/unregister-device`
  - [ ] Authorization: JWT (patient only)
  - [ ] Body: { fcmToken }

### Patient Portal UI (Web)

#### Frontend Implementation
- [ ] Create patient portal: `src/main/webapp/patient/`
- [ ] Pages:
  - [ ] Login page (gub.uy authentication)
  - [ ] Dashboard (recent documents, pending requests)
  - [ ] Clinical history (CU02) - list and view documents
  - [ ] Access policies (CU03) - create, edit, delete policies
  - [ ] Access audit (CU05) - who accessed my data
  - [ ] Notification preferences (CU04)
  - [ ] Access requests (approve/deny)
- [ ] Technologies: JSF/PrimeFaces or React
- [ ] Responsive design

### Mobile App (React Native) - CU06, CU07, CU08

#### Mobile Implementation
- [ ] Set up React Native project (in `mobile/` directory)
- [ ] Screens:
  - [ ] Login screen (gub.uy OAuth via system browser)
  - [ ] Dashboard (IPS-FHIR summary)
  - [ ] Clinical history (list documents)
  - [ ] Document viewer (PDF, FHIR)
  - [ ] Notifications (push notifications)
  - [ ] Access requests (approve/deny)
  - [ ] Settings (notification preferences)
- [ ] Implement `AuthService.ts`
  - [ ] PKCE flow (code_verifier, code_challenge)
  - [ ] Deep linking (handle hcenuy://auth/callback)
  - [ ] Token storage (SecureStore)
  - [ ] Automatic token refresh
- [ ] Implement `ApiClient.ts`
  - [ ] Axios with Authorization header
  - [ ] Token refresh interceptor
  - [ ] Error handling
- [ ] Implement `NotificationService.ts`
  - [ ] Firebase Cloud Messaging setup
  - [ ] Handle push notifications
  - [ ] Register FCM token with backend
- [ ] Configure deep linking
  - [ ] Android: AndroidManifest.xml
  - [ ] iOS: Info.plist

#### Testing
- [ ] Unit tests for clinical history service
- [ ] Unit tests for notification service
- [ ] Integration tests for document download
- [ ] Integration tests with Firebase
- [ ] Test IPS-FHIR generation
- [ ] Test notification delivery (Firebase)
- [ ] UI tests (web portal)
- [ ] Mobile app testing (Android emulator/device)

---

## 6.3 HEALTH PROFESSIONAL ACTOR (CU17, CU18, CU19, CU20)

### Note: Professional features are in PERIPHERAL component, not central
### Central component provides APIs for peripheral nodes to call

#### APIs for Peripheral Nodes

- [ ] `GET /api/rndc/documents?patientCI={ci}` - Search documents (CU19)
  - [ ] Called by peripheral when professional requests patient history
  - [ ] Policy enforcement (PERMIT/DENY/PENDING)
  - [ ] Return document metadata with locators

- [ ] `POST /api/access-requests` - Request access (CU20)
  - [ ] Called by peripheral when professional needs approval
  - [ ] Create pending request
  - [ ] Send push notification to patient

- [ ] `POST /api/rndc/documents` - Register document (CU18)
  - [ ] Called by peripheral after professional uploads document
  - [ ] Register metadata in RNDC

---

## PHASE 7: PERIPHERAL NODE INTEGRATION (AC003, AC013-AC016)

### Peripheral Node API Client

#### Client Implementation
- [ ] Create package: `uy.gub.hcen.integration.peripheral.*`
- [ ] Implement `PeripheralNodeClient.java`
  - [ ] `retrieveDocument()` - Fetch document from peripheral node (AC015)
    - [ ] GET {documentLocator}
    - [ ] Authorization: HCEN API key
    - [ ] Return: Document file (PDF, XML, FHIR)
    - [ ] Verify hash (integrity)
  - [ ] `sendOnboardingData()` - Send clinic config to peripheral (AC016)
    - [ ] POST {peripheralNodeUrl}/api/onboard
    - [ ] Body: { clinicId, apiKey, hcenCentralUrl, config }
  - [ ] Circuit breaker for resilience

#### REST API for Peripheral Nodes (Inbound)

- [ ] `POST /api/peripheral/users` - INUS registration (AC013)
  - [ ] Authorization: API key (validate clinic)
  - [ ] Body: { ci, firstName, lastName, dateOfBirth, clinicId }
  - [ ] Call InusService.registerUser()
  - [ ] Return: { inusId, status }

- [ ] `POST /api/peripheral/documents` - RNDC registration (AC014)
  - [ ] Authorization: API key (validate clinic)
  - [ ] Body: { patientCI, documentType, documentLocator, documentHash, createdBy, createdAt, clinicId }
  - [ ] Call RndcService.registerDocument()
  - [ ] Return: { documentId, status }

#### Testing
- [ ] Unit tests for peripheral node client
- [ ] Integration tests with mock peripheral nodes
- [ ] Test document retrieval (AC015)
- [ ] Test onboarding flow (AC016)
- [ ] Test INUS registration from peripheral (AC013)
- [ ] Test RNDC registration from peripheral (AC014)
- [ ] Test circuit breaker (peripheral node down)

---

## PHASE 8: TESTING & QUALITY ASSURANCE (AC017, AC018)

### Unit Testing (Target: 80% Coverage)

#### Service Layer Tests
- [ ] InusService tests (registration, lookup, update)
- [ ] RndcService tests (registration, search, retrieval)
- [ ] PolicyEngine tests (evaluation, conflict resolution)
- [ ] AuthenticationService tests (login, callback, token generation)
- [ ] AuditService tests (event logging, queries)
- [ ] NotificationService tests (push notifications, preferences)
- [ ] ClinicManagementService tests (create, update, onboard)
- [ ] ReportsService tests (statistics, dashboard)

#### Repository Layer Tests
- [ ] All JPA repositories (CRUD operations)
- [ ] Test with H2 in-memory database
- [ ] Test transaction boundaries
- [ ] Test query methods

#### Integration Layer Tests
- [ ] GubUyOidcClient tests (mock HTTP calls)
- [ ] PDIClient tests (mock SOAP calls)
- [ ] PeripheralNodeClient tests (mock HTTP calls)
- [ ] FirebaseMessagingClient tests (mock Firebase)

#### REST API Tests
- [ ] All endpoints with valid inputs
- [ ] All endpoints with invalid inputs
- [ ] Authorization tests (JWT validation)
- [ ] Error handling tests (4xx, 5xx responses)

### Integration Testing

- [ ] End-to-end authentication flow (gub.uy → INUS → JWT)
- [ ] End-to-end document registration (peripheral → RNDC → audit)
- [ ] End-to-end document access (professional → policy → RNDC → peripheral)
- [ ] End-to-end access request (professional → pending → patient → approval)
- [ ] Firebase notification delivery
- [ ] Database transactions (rollback on error)

### Performance Testing (AC008, AC009, AC010)

#### Load Testing
- [ ] Define peak usage scenarios:
  - [ ] 100 concurrent patients viewing clinical history
  - [ ] 50 concurrent professionals accessing documents
  - [ ] 10 peripheral nodes registering documents simultaneously
  - [ ] 20 auth requests/second (gub.uy login)
- [ ] Tools: JMeter or Gatling
- [ ] Test objectives:
  - [ ] **AC008 Objective A**: Verify response times don't degrade
  - [ ] **AC008 Objective B**: Find system breaking point
  - [ ] **AC008 Objective C**: Identify bottlenecks (database, Redis, gub.uy)
- [ ] Metrics:
  - [ ] Response time (p50, p95, p99)
  - [ ] Throughput (requests/second)
  - [ ] Error rate
  - [ ] Resource utilization (CPU, memory, database connections)

#### Stress Testing
- [ ] Gradually increase load until system fails
- [ ] Identify bottlenecks:
  - [ ] Database connection pool exhaustion
  - [ ] Redis connection limits
  - [ ] gub.uy rate limits
  - [ ] Memory leaks
- [ ] Document findings and mitigations

### Security Testing

- [ ] Penetration testing (SQL injection, XSS, CSRF)
- [ ] JWT token tampering tests
- [ ] OAuth security tests (CSRF, code injection)
- [ ] HTTPS enforcement (redirect HTTP → HTTPS)
- [ ] Rate limiting tests (brute force protection)
- [ ] Access control tests (unauthorized access attempts)
- [ ] Audit log immutability tests

### Code Quality

- [ ] SonarQube integration (optional 1 point)
  - [ ] Set up SonarQube server
  - [ ] Configure Gradle plugin
  - [ ] Define quality gates (80% coverage, 0 critical bugs)
  - [ ] Integrate with CI/CD pipeline
- [ ] Code review checklist
- [ ] Static analysis (FindBugs, PMD)

---

## PHASE 9: DEPLOYMENT & OPERATIONS

### Production Deployment

#### Elastic Cloud ANTEL Setup
- [ ] Provision PostgreSQL instance
- [ ] Provision Redis instance
- [ ] Provision WildFly cluster (2+ instances)
- [ ] Configure load balancer (NGINX or ANTEL-provided)
- [ ] Set up HTTPS certificates (Let's Encrypt or ANTEL-provided)
- [ ] Configure DNS (hcen.uy, admin.hcen.uy)

#### Environment Configuration
- [ ] Production environment variables
- [ ] Database connection strings
- [ ] Redis connection strings
- [ ] gub.uy production credentials
- [ ] Firebase production credentials
- [ ] API keys for peripheral nodes

#### Monitoring & Alerting
- [ ] Set up application monitoring (Prometheus + Grafana or ANTEL-provided)
- [ ] Metrics:
  - [ ] Request rate, error rate, latency
  - [ ] Database connection pool usage
  - [ ] Redis cache hit rate
  - [ ] gub.uy authentication success rate
  - [ ] Policy evaluation latency
- [ ] Set up alerting (email, SMS, Slack)
  - [ ] Alert on high error rate (> 5%)
  - [ ] Alert on high latency (p95 > 2 seconds)
  - [ ] Alert on database connection pool exhaustion
  - [ ] Alert on gub.uy downtime (circuit breaker open)

#### Logging
- [ ] Centralized logging (ELK stack or ANTEL-provided)
- [ ] Log levels: ERROR, WARN, INFO, DEBUG
- [ ] Structured logging (JSON format)
- [ ] Log retention (30 days for INFO, 90 days for ERROR)
- [ ] Never log sensitive data (passwords, full CI, document content)

#### Backup & Recovery
- [ ] Automated PostgreSQL backups (daily)
- [ ] Backup retention (30 days)
- [ ] Test restore procedure
- [ ] Document disaster recovery plan (RPO, RTO)

#### CI/CD Pipeline
- [ ] GitLab CI/CD configuration (.gitlab-ci.yml)
- [ ] Stages:
  - [ ] Build (compile, package WAR)
  - [ ] Test (unit, integration, 80% coverage gate)
  - [ ] Quality (SonarQube analysis)
  - [ ] Deploy (staging → production)
- [ ] Automated rollback on deployment failure

---

## PHASE 10: OPTIONAL FEATURES (Choose 3 for Engineering, 1 for Technology)

### Option 1: NoSQL Database Integration (1 point) - SELECTED

- [ ] MongoDB setup (completed in Phase 0)
- [ ] Implement refresh token storage in MongoDB
- [ ] Implement audit log archival (> 90 days)
- [ ] Implement notification history in MongoDB
- [ ] Document NoSQL usage in architecture doc

### Option 2: SonarQube Integration (1 point) - SELECTED

- [ ] Set up SonarQube server
- [ ] Integrate with Gradle
- [ ] Configure quality gates
- [ ] Integrate with CI/CD pipeline
- [ ] Document code quality metrics

### Option 3: LLM Integration (1 point) - CANDIDATE

- [ ] Integrate OpenAI API or similar
- [ ] Use cases:
  - [ ] Clinical note summarization (extract key findings)
  - [ ] Policy recommendation (suggest policies based on patient profile)
  - [ ] Symptom analysis (search similar cases)
- [ ] Implement `LlmService.java`
- [ ] Add REST endpoint: `POST /api/llm/summarize`
- [ ] Document LLM usage in architecture doc

### Option 4: Digital Document Signature (1 point) - CANDIDATE

- [ ] Integrate digital signature library (BouncyCastle, iText)
- [ ] Implement `DocumentSignatureService.java`
- [ ] Sign documents with professional's digital certificate
- [ ] Verify document signatures
- [ ] Store signature metadata in RNDC
- [ ] Document signature usage in architecture doc

### Option 5: Terminology Services Integration (1 point) - CANDIDATE

- [ ] Integrate with AGESIC Terminology Services
- [ ] Use cases:
  - [ ] Standardized coding (SNOMED CT, ICD-10, LOINC)
  - [ ] Term lookup and validation
  - [ ] Code mapping
- [ ] Implement `TerminologyServiceClient.java`
- [ ] Add REST endpoint: `GET /api/terminology/search?term={term}`
- [ ] Document terminology usage in architecture doc

---

## PHASE 11: DOCUMENTATION & DELIVERABLES

### Code Documentation
- [ ] JavaDoc for all public classes and methods
- [ ] README.md (< 1 page, deployment instructions)
- [ ] API documentation (OpenAPI/Swagger)
- [ ] Database schema documentation (ERD diagrams)

### Architecture & Design Document
- [ ] System architecture diagram
- [ ] Component interaction diagrams
- [ ] Database schema (ERD)
- [ ] Deployment architecture
- [ ] Security architecture
- [ ] Policy evaluation flow
- [ ] Authentication flow (gub.uy OIDC)
- [ ] Document registration flow (peripheral → RNDC)
- [ ] Document access flow (professional → policy → RNDC → peripheral)
- [ ] Technology stack justification
- [ ] Design patterns used
- [ ] Non-functional requirements (scalability, security, performance)

### Technical Paper (8-12 pages for Engineering students)
- [ ] Introduction (problem statement, objectives)
- [ ] Related work (existing EHR systems)
- [ ] Architecture (detailed design decisions)
- [ ] Implementation (key algorithms, technologies)
- [ ] Testing (coverage, performance, security)
- [ ] Results (metrics, findings)
- [ ] Discussion (challenges, lessons learned)
- [ ] Conclusion and future work
- [ ] References

### Test Data Set
- [ ] 100 synthetic patients (realistic CI numbers)
- [ ] 20 synthetic health professionals (specialties: cardiology, general medicine, pediatrics)
- [ ] 10 clinics
- [ ] 500 clinical documents (various types)
- [ ] 50 access policies (various types)
- [ ] SQL scripts to populate test data
- [ ] Document test data generation process

### Demo Video
- [ ] Script and storyboard
- [ ] Record demo (5-10 minutes)
- [ ] Scenarios:
  - [ ] Patient login (gub.uy), view clinical history, configure policies
  - [ ] Professional requests access, patient approves
  - [ ] Admin onboards new clinic, views reports
  - [ ] Mobile app: view IPS-FHIR summary, receive push notification
- [ ] Edit and narrate video
- [ ] Upload to YouTube (unlisted)

### Screenshots
- [ ] Patient portal (login, dashboard, clinical history, policies, audit)
- [ ] Admin portal (dashboard, clinic management, reports)
- [ ] Mobile app (login, IPS summary, notifications)
- [ ] Policy evaluation flow
- [ ] Access request flow

---

## IMPLEMENTATION STRATEGY

### Agent Assignment by Actor

#### Agent 1: HCEN Admin Actor Implementation
**Responsibility**: All admin-related features (CU09, CU10, CU11)
**Tasks**:
- Phase 6.1: Admin features (clinic management, reports, audit)
- Admin portal UI
- Testing for admin features

#### Agent 2: Health User/Patient Actor Implementation
**Responsibility**: All patient-related features (CU02, CU03, CU04, CU05, CU07, CU08)
**Tasks**:
- Phase 6.2: Patient features (clinical history, policies, notifications, IPS-FHIR)
- Patient portal UI
- Mobile app (React Native)
- Testing for patient features

#### Agent 3: Database & Infrastructure
**Responsibility**: All database setup and core services
**Tasks**:
- Phase 0: Project setup, database setup (PostgreSQL, Redis, MongoDB)
- Phase 1: Database schemas, JPA entities, repositories
- Phase 3: INUS and RNDC services
- Phase 5: Audit service
- Testing for core services

#### Agent 4: Authentication & Authorization
**Responsibility**: All authentication and policy-related features
**Tasks**:
- Phase 2: gub.uy OIDC integration, JWT service
- Phase 4: Policy Engine, access request workflow
- Security implementation
- Testing for auth and policy features

#### Agent 5: Integration & Testing
**Responsibility**: External integrations and comprehensive testing
**Tasks**:
- Phase 7: Peripheral node integration, PDI integration, Firebase integration
- Phase 8: Testing (unit, integration, performance, security)
- Phase 9: Deployment and operations
- CI/CD pipeline

#### Agent 6: Documentation & Optional Features
**Responsibility**: Documentation and bonus features
**Tasks**:
- Phase 10: Optional features (NoSQL, SonarQube, LLM, etc.)
- Phase 11: Documentation (architecture, technical paper, demo video)

---

## PROGRESS TRACKING

### Week 1-2: Foundation
- [ ] Phase 0: Project setup
- [ ] Phase 1: Database schemas (50%)

### Week 3-4: Core Services
- [ ] Phase 1: Database schemas (100%)
- [ ] Phase 2: Authentication (50%)
- [ ] Phase 3: INUS & RNDC (50%)

### Week 5-6: Policy & Audit
- [ ] Phase 2: Authentication (100%)
- [ ] Phase 3: INUS & RNDC (100%)
- [ ] Phase 4: Policy Engine (100%)
- [ ] Phase 5: Audit System (100%)

### Week 7-8: Actor Features
- [ ] Phase 6.1: Admin features (100%)
- [ ] Phase 6.2: Patient features (50%)

### Week 9-10: Integration
- [ ] Phase 6.2: Patient features (100%)
- [ ] Phase 7: Peripheral integration (100%)

### Week 11-12: Testing
- [ ] Phase 8: Testing (unit, integration, performance) (100%)

### Week 13-14: Deployment & Optional
- [ ] Phase 9: Deployment (100%)
- [ ] Phase 10: Optional features (100%)

### Week 15-16: Documentation & Demo
- [ ] Phase 11: Documentation (100%)
- [ ] Demo video, screenshots
- [ ] Final submission

---

## CRITICAL SUCCESS FACTORS

1. **80% Test Coverage** - Gate for every commit
2. **HTTPS Everywhere** - All communications encrypted
3. **Horizontal Scalability** - Stateless architecture with Redis
4. **Multi-tenancy Isolation** - Schema-per-tenant in peripheral component
5. **Policy Enforcement** - Every document access checked
6. **Comprehensive Audit** - Complete traceability (AC026)
7. **INUS/RNDC Integration** - Correct metadata registration (AC013-AC015)
8. **gub.uy Authentication** - Proper OIDC flow with NID 2/3
9. **Performance Testing** - Meet AC008-010 objectives
10. **Optional Requirements** - Complete 3 for Engineering

---

## RISK MITIGATION

### Technical Risks
- **Risk**: gub.uy integration complexity
  - **Mitigation**: Start with mock authentication, implement real OIDC in parallel
- **Risk**: Performance bottlenecks
  - **Mitigation**: Early load testing (week 8), identify bottlenecks, optimize
- **Risk**: 80% coverage hard to achieve
  - **Mitigation**: TDD approach, write tests first, use JaCoCo from day 1
- **Risk**: Elastic Cloud ANTEL delays
  - **Mitigation**: Develop on local WildFly, Docker containers, deploy early to staging

### Project Risks
- **Risk**: Team member unavailability
  - **Mitigation**: Knowledge sharing sessions, pair programming, documentation
- **Risk**: Scope creep
  - **Mitigation**: Strict prioritization, focus on core features first, optional features last
- **Risk**: Deadline pressure
  - **Mitigation**: Weekly milestones, Agile sprints, adjust scope if needed

---

## 🎉 PHASE 1 COMPLETION SUMMARY (2025-10-22)

### ✅ What Was Completed

**Phase 1: Core Services - 100% COMPLETE**

1. **InusService** (670 lines)
   - User registration with CI validation
   - User lookup with Redis caching
   - Profile management
   - Eligibility validation
   - PDI integration interface (client pending)
   - REST API: 5 endpoints
   - DTOs: 5 (UserRegistrationRequest, UserResponse, UserUpdateRequest, UserListResponse, EligibilityResponse)
   - Exceptions: 2 (UserNotFoundException, UserRegistrationException)

2. **RndcService** (865 lines)
   - Document registration with hash validation
   - Document search with policy enforcement
   - Document metadata management
   - Document status updates (soft delete)
   - Hash verification
   - REST API: 6 endpoints
   - DTOs: 5 (DocumentRegistrationRequest, DocumentResponse, DocumentListResponse, DocumentStatusUpdateRequest, HashVerificationResponse)
   - Exceptions: 2 (DocumentNotFoundException, DocumentRegistrationException)

3. **PolicyEngine** (443 lines)
   - 6 Policy Evaluators implemented:
     - DocumentTypePolicyEvaluator
     - SpecialtyPolicyEvaluator
     - ClinicPolicyEvaluator
     - TimeBasedPolicyEvaluator
     - ProfessionalPolicyEvaluator
     - EmergencyOverridePolicyEvaluator
   - Policy conflict resolution (priority-based)
   - Redis caching (5-min TTL)
   - Audit integration
   - REST API: 6 endpoints
   - DTOs: 6 (PolicyCreateRequest, PolicyUpdateRequest, PolicyResponse, PolicyListResponse, AccessEvaluationRequest, AccessEvaluationResponse)
   - Service DTOs: 3 (AccessRequest, PolicyDecision, PolicyEvaluationResult)

4. **AuditService** (723 lines)
   - Comprehensive event logging
   - Patient access history (CU05)
   - Audit statistics and reporting
   - Export functionality (CSV/JSON)
   - Append-only PostgreSQL storage
   - REST API: 6 endpoints
   - DTOs: 5 (AuditLogResponse, AuditLogListResponse, PatientAccessHistoryResponse, AuditStatisticsResponse, AuditEventRequest)
   - Annotation: @Audited (for future AOP)

**Statistics:**
- **Total Java Files**: 113 files
- **Total Lines of Code**: ~12,330 lines (services only)
- **Services**: 4/4 (100%)
- **REST Endpoints**: 23 endpoints
- **Policy Evaluators**: 6/6 (100%)
- **DTOs**: 33 DTOs total
- **Build Status**: ✅ PASSING (compiles successfully)

### 🎯 What's Next: Recommended Path Forward

**Option A: Continue with Phase 2 (Patient Features)**
- ClinicalHistoryService
- NotificationService (Firebase integration)
- AccessRequestService (workflow)
- IpsFhirService

**Option B: External Integrations First**
- PDIClient.java (SOAP - identity validation)
- PeripheralNodeClient.java (REST - document retrieval)
- FirebaseMessagingClient.java (Push notifications)

**Option C: Testing Phase**
- Write unit tests for all 4 services (80% coverage target)
- Integration tests
- Performance tests
- Security tests

**Recommendation**: Start with **Testing (Option C)** to achieve 80% coverage on Phase 1 services before moving forward. This ensures a solid foundation and meets AC017/AC018 requirements.

### ⚠️ Important Notes

1. **PDI Integration**: Interface ready in InusService, but PDIClient.java implementation is pending
2. **Peripheral Node Client**: Interface ready in RndcService, but PeripheralNodeClient.java implementation is pending
3. **Testing**: NO tests written yet - this is CRITICAL for 80% coverage requirement
4. **SSO Implementation**: Authentication system exists, but gub.uy OIDC integration needs completion
5. **Access Request Workflow**: Schema exists, but AccessRequestService not yet implemented

### 📋 Immediate Next Steps (Choose One)

1. **If focusing on testing**:
   - Write unit tests for InusService (10-15 test cases)
   - Write unit tests for RndcService (10-15 test cases)
   - Write unit tests for PolicyEngine (15-20 test cases for all evaluators)
   - Write unit tests for AuditService (10-15 test cases)
   - Target: 80% code coverage with JaCoCo

2. **If focusing on external integrations**:
   - Implement PDIClient.java (SOAP client for identity validation)
   - Implement PeripheralNodeClient.java (REST client for document retrieval)
   - Set up pdi-mock service for testing
   - Complete PDI integration in InusService

3. **If focusing on Phase 2 services**:
   - Implement ClinicalHistoryService
   - Implement NotificationService with Firebase
   - Implement AccessRequestService
   - Add 10-15 more REST endpoints

---

**END OF TODO.MD**

This document will be updated as implementation progresses. Each completed task should be marked with [x].

**Last Major Update**: 2025-10-22 - Phase 1 Core Services Complete (InusService, RndcService, PolicyEngine, AuditService)