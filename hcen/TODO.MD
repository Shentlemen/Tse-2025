# HCEN Implementation TODO
## Master Implementation Plan - TSE 2025 Group 9

**Project**: Historia Clínica Electrónica Nacional (HCEN)
**Goal**: 80% code coverage, horizontal scalability, HTTPS everywhere, comprehensive audit trail
**Last Updated**: 2025-10-23

---

## 📊 PROJECT COMPLETION STATUS

### Overall Progress: ~58% Complete

| Category | Complete | Total | % | Status |
|----------|----------|-------|---|--------|
| **Database Layer** | 7/7 schemas | 100% | ✅ **COMPLETE** |
| **Caching Layer** | 5/5 services | 100% | ✅ **COMPLETE** |
| **Authentication** | 1/1 system | 100% | ✅ **COMPLETE** |
| **Core Services** | 5/8 services | 63% | ✅ **PHASE 1+2 COMPLETE** |
| **REST APIs** | 37/60+ endpoints | 62% | ⚠️ **PHASE 1+2 COMPLETE** |
| **Web UI Dashboards** | 2/2 dashboards | 100% | ✅ **COMPLETE** |
| **External Integrations** | 2/3 integrations | 67% | ⚠️ **PERIPHERAL + PDI DONE** |
| **Web UI (Full)** | 2/12+ pages | 17% | ⚠️ **DASHBOARDS DONE** |
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

**SSO AUTHENTICATION & DASHBOARDS - COMPLETED 2025-10-22**

**✅ PHASE COMPLETE: Minimal Admin & Patient Dashboards with SSO**

**Backend Fixes & Enhancements (100%)**
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
- ✅ Updated StateManager.java with `peekState()` method
- ✅ Updated gub.uy OAuth credentials in application.properties (client_id: 890192)
- ✅ Configured redirect URI: `http://localhost:8080/hcen/api/auth/callback`

**Admin Dashboard (admin/dashboard.jsp) - 375 lines (100%)**
- ✅ Professional dark theme UI for administrative portal
- ✅ User information display from JWT (name, CI, role)
- ✅ Statistics cards with placeholders (4 cards):
  - Total Users, Total Documents, Active Policies, Total Clinics
- ✅ Complete token management JavaScript
  - ✅ JWT parsing and validation (decodeToken function)
  - ✅ Token storage in sessionStorage (browser-session only)
  - ✅ Auto-redirect to /hcen/api/auth/login/initiate?clientType=WEB_ADMIN on missing/invalid token
- ✅ Role-based access control (ADMIN role verification)
- ✅ Navigation menu (6 admin features):
  - Dashboard, Clinics, Reports, Audit Logs, Users, Settings
- ✅ Logout functionality with token revocation (calls /hcen/api/auth/logout)
- ✅ Responsive design (mobile-friendly)
- ✅ Professional styling (dark blue/gray color scheme)

**Patient Dashboard (patient/dashboard.jsp) - 380 lines (100%)**
- ✅ Friendly light theme UI for patient portal
- ✅ User information display from JWT (name, CI)
- ✅ Statistics cards with placeholders (4 cards):
  - My Documents, My Policies, Recent Accesses, Pending Approvals
- ✅ Complete token management JavaScript
  - ✅ JWT parsing and validation (decodeToken function)
  - ✅ Token storage in sessionStorage (browser-session only)
  - ✅ Auto-redirect to /hcen/api/auth/login/initiate?clientType=WEB_PATIENT on missing/invalid token
- ✅ Role-based access control (PATIENT role verification)
- ✅ Navigation menu (6 patient features):
  - Dashboard, Clinical History, My Policies, Access History, Notifications, Settings
- ✅ Logout functionality with token revocation (calls /hcen/api/auth/logout)
- ✅ Responsive design (mobile-friendly)
- ✅ Friendly styling (blue/white color scheme)

**Security Features Implemented (100%)**
- ✅ JWT token removed from URL after reading (prevents token leakage via browser history)
- ✅ Token stored in sessionStorage (browser-session only, not localStorage)
- ✅ Role-based access control enforced on both dashboards
- ✅ Auto-redirect to login on missing/invalid token
- ✅ Logout with proper token revocation flow
- ✅ Token validation on page load
- ✅ Client-side role verification (ADMIN vs PATIENT)

**Files Modified:**
- `src/main/java/uy/gub/hcen/auth/service/AuthenticationService.java` (role assignment fix)
- `src/main/java/uy/gub/hcen/auth/service/StateManager.java` (added peekState method)
- `src/main/java/uy/gub/hcen/auth/api/rest/AuthenticationResource.java` (redirect logic)
- `src/main/resources/application.properties` (OAuth credentials update)

**Files Created:**
- `src/main/webapp/admin/dashboard.jsp` (375 lines - Admin Portal Dashboard)
- `src/main/webapp/patient/dashboard.jsp` (380 lines - Patient Portal Dashboard)

**Next Steps - Dashboard API Integration:**
- [ ] **CRITICAL**: Register redirect URI with gub.uy: `http://localhost:8080/hcen/api/auth/callback`
- [ ] Implement statistics API endpoints to populate dashboard cards:
  - [ ] GET /api/admin/dashboard/stats (for admin dashboard)
    - Return: { totalUsers, totalDocuments, activePolicies, totalClinics }
  - [ ] GET /api/patient/dashboard/stats (for patient dashboard)
    - Return: { myDocuments, myPolicies, recentAccesses, pendingApprovals }
- [ ] Update dashboards to fetch real data from API endpoints
- [ ] Add loading states and error handling for API calls

**Next Steps - Additional Admin Pages:**
- [ ] Clinic management UI (`admin/clinics.jsp`)
  - List, create, edit, onboard clinics
  - Integrate with /api/admin/clinics endpoints
- [ ] Reports viewer (`admin/reports.jsp`)
  - Charts and analytics dashboard
  - Integrate with /api/admin/reports endpoints
- [ ] Audit log viewer (`admin/audit.jsp`)
  - Search, filter, export audit logs
  - Integrate with /api/audit/logs endpoints
- [ ] User management UI (`admin/users.jsp`)
  - Search, view, update, deactivate users
  - Integrate with /api/inus/users endpoints

**Next Steps - Additional Patient Pages:**
- [ ] Clinical history viewer (`patient/history.jsp`)
  - Document list and document viewer
  - Integrate with /api/patients/{ci}/history endpoints
- [ ] Policy management UI (`patient/policies.jsp`)
  - Create, edit, delete access policies
  - Integrate with /api/policies endpoints
- [ ] Access history viewer (`patient/access-history.jsp`)
  - View who accessed my data
  - Integrate with /api/audit/patient/{ci}/access-history endpoint
- [ ] Notification preferences page (`patient/notifications.jsp`)
  - Configure notification preferences
  - Integrate with /api/patients/{ci}/notifications/preferences endpoint

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

**HIGH PRIORITY - Remaining Core Services (Phase 2 - 63% Complete)**
1. ✅ **InusService.java** - COMPLETE
2. ✅ **RndcService.java** - COMPLETE
3. ✅ **PolicyEngine.java** - COMPLETE
4. ✅ **AuditService.java** - COMPLETE
5. ✅ **ClinicManagementService.java** - COMPLETE (2025-10-23)
6. **NotificationService.java** - Push notifications, Firebase (NOT STARTED)
7. **ClinicalHistoryService.java** - Patient history, document download (NOT STARTED)
8. **ReportsService.java** - Analytics, dashboard (NOT STARTED)

**CRITICAL - External Integrations (67% Complete)**
1. ✅ **PDIClient.java** - SOAP client for identity validation (COMPLETE - 2025-10-23)
2. ✅ **PeripheralNodeClient.java** - REST client for peripheral nodes (COMPLETE - 2025-10-23)
3. **FirebaseMessagingClient.java** - Push notification client (NOT STARTED)

**REST APIs (~62% Complete - Phase 1+2)**
- ✅ INUS: 5/5 endpoints (COMPLETE)
- ✅ RNDC: 6/6 endpoints (COMPLETE)
- ✅ Policies: 6/6 endpoints (COMPLETE)
- ✅ Audit: 6/6 endpoints (COMPLETE)
- ✅ Clinics: 7/7 endpoints (COMPLETE - 2025-10-23)
- ❌ Access Requests: 0/3 endpoints
- ❌ Reports: 0/5 endpoints
- ❌ Patient History: 0/3 endpoints
- ❌ Notifications: 0/4 endpoints
- ❌ Peripheral Integration: 0/2 endpoints

**HIGH PRIORITY - User Interfaces (17% Complete)**
1. ✅ Admin Portal Dashboard (COMPLETE - 375 lines)
2. ✅ Patient Portal Dashboard (COMPLETE - 380 lines)
3. Admin Portal Pages (0/5 pages): clinics, reports, audit, users, settings
4. Patient Portal Pages (0/5 pages): history, policies, access-history, notifications, settings
5. Mobile App (React Native - 0/7 screens)

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
- Total Lines of Code: ~12,330 lines (services only)
- Services Implemented: 4/4 (100%)
- REST Endpoints: 23/23 (100%)
- Policy Evaluators: 6/6 (100%)
- DTOs Created: 33
- Build Status: ✅ PASSING

**SSO & Dashboards Summary (2025-10-22):**
- Admin Dashboard: 375 lines (COMPLETE)
- Patient Dashboard: 380 lines (COMPLETE)
- Backend SSO Fixes: COMPLETE
- Total UI Lines Added: ~755 lines
- Build Status: ✅ PASSING

**Next Steps:**
1. **CRITICAL**: Register redirect URI with gub.uy: `http://localhost:8080/hcen/api/auth/callback`
2. Implement dashboard statistics API endpoints (GET /api/admin/dashboard/stats, GET /api/patient/dashboard/stats)
3. Option A: Continue Phase 2 (Patient Features)
4. Option B: External Integrations (PDI, Firebase)
5. Option C: Testing Phase (80% coverage)

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

### PDI Integration (Identity Validation) - ✅ COMPLETE (2025-10-23)

#### SOAP Client Implementation - ✅ COMPLETE
- [x] Create package: `uy.gub.hcen.integration.pdi.*`
- [x] Implement `PDIClient.java` (~450 lines) - Manual SOAP 1.1/1.2 client
  - [x] `consultarUsuario(String ci)` - Query user by CI from PDI
  - [x] `verifyAge(String ci, int minimumAge)` - Age verification (18+)
  - [x] Return: PDIUserData (ci, nombreCompleto, fechaNacimiento)
  - [x] WS-Security username/password authentication
  - [x] Circuit breaker for resilience (5 failures → 60s cooldown)
  - [x] Retry logic with exponential backoff (3 attempts)
- [x] Implement `PDIUserData.java` - DTO for response data
- [x] Implement `PDIException.java` - Custom exception
- [x] Integration with InusService for age verification
- [ ] Create mock PDI service (pdi-mock component) - OPTIONAL for testing
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

### Clinic Management Service (CU10) - ✅ COMPLETE (2025-10-23)

#### Service Implementation - ✅ COMPLETE
- [x] Create package: `uy.gub.hcen.service.clinic.*`
- [x] Implement `ClinicManagementService.java` (~650 lines)
  - [x] `registerClinic()` - Register new clinic (CU10)
    - [x] Generate clinic_id (UUID-based)
    - [x] Generate secure API key (SecureRandom, 64 bytes, Base64-encoded)
    - [x] Save to database
    - [x] Log creation in audit
  - [x] `updateClinic()` - Modify clinic details
    - [x] Validate clinic exists
    - [x] Update profile information
    - [x] Log modification in audit
  - [x] `activateClinic()` - Activate clinic (status = ACTIVE)
  - [x] `deactivateClinic()` - Deactivate clinic (status = INACTIVE)
  - [x] `onboardClinic()` - Send onboarding data to peripheral node (AC016)
    - [x] Interface ready for PeripheralNodeClient integration
    - [x] POST to peripheral_node_url/api/onboard
    - [x] Body: { clinicId, apiKey, hcenCentralUrl, config }
    - [x] Handles onboarding failures
    - ⚠️ **NOTE**: PeripheralNodeClient.java implementation pending
  - [x] `listClinics()` - Get all clinics (with filters: status, city)
    - [x] Pagination support
    - [x] Search functionality
  - [x] `getClinicById()` - Get specific clinic details
  - [x] `getClinicStatistics()` - Stats per clinic (users, documents, policies)

#### REST API Endpoints - ✅ COMPLETE (7/7)
- [x] `POST /api/admin/clinics` - ClinicResource.java
  - [x] Authorization: JWT (admin only)
  - [x] Body: ClinicRegistrationRequest (with Bean Validation)
  - [x] Return: ClinicResponse with clinicId and API key
- [x] `GET /api/admin/clinics` - ClinicResource.java
  - [x] Authorization: JWT (admin only)
  - [x] Query params: status, city, page, size
  - [x] Return: ClinicListResponse (paginated)
- [x] `GET /api/admin/clinics/{clinicId}` - ClinicResource.java
  - [x] Authorization: JWT (admin only)
  - [x] Return: ClinicResponse with full details
- [x] `PUT /api/admin/clinics/{clinicId}` - ClinicResource.java
  - [x] Authorization: JWT (admin only)
  - [x] Body: ClinicUpdateRequest
  - [x] Update clinic details
- [x] `POST /api/admin/clinics/{clinicId}/onboard` - ClinicResource.java
  - [x] Authorization: JWT (admin only)
  - [x] Body: OnboardingRequest (optional config)
  - [x] Trigger onboarding process (AC016)
  - [x] Return: OnboardingResponse with status
- [x] `DELETE /api/admin/clinics/{clinicId}` - ClinicResource.java
  - [x] Authorization: JWT (admin only)
  - [x] Soft delete (status = INACTIVE)
- [x] `GET /api/admin/clinics/{clinicId}/statistics` - ClinicResource.java
  - [x] Authorization: JWT (admin only)
  - [x] Return: ClinicStatisticsResponse (users, documents, policies)

#### DTOs Created (7 files)
- [x] ClinicRegistrationRequest.java (Bean Validation)
- [x] ClinicUpdateRequest.java (Bean Validation)
- [x] ClinicResponse.java (API key masking for security)
- [x] ClinicListResponse.java (paginated)
- [x] ClinicStatisticsResponse.java
- [x] OnboardingRequest.java (AC016)
- [x] OnboardingResponse.java (AC016)

#### Exception Classes (3 files)
- [x] ClinicNotFoundException.java
- [x] ClinicRegistrationException.java
- [x] OnboardingException.java

#### Configuration
- [x] Registered ClinicResource in RestApplication.java

**Implementation Summary:**
- **Total Lines**: ~1,800 lines
- **Files Created**: 12 files
- **Build Status**: ✅ PASSING
- **Date Completed**: 2025-10-23

**Key Features:**
- Secure API key generation (SecureRandom, 64 bytes)
- Comprehensive audit logging integration
- Clinic search and filtering
- Statistics aggregation (users, documents, policies)
- Onboarding workflow (AC016) - ready for PeripheralNodeClient
- Bean Validation on all requests

**Integration Status:**
- ✅ PeripheralNodeClient.java (COMPLETE - 2025-10-23) - AC016 fully functional

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
- [x] Create admin portal: `src/main/webapp/admin/`
- [ ] Pages:
  - [x] Login flow (gub.uy SSO authentication via /api/auth/login/initiate?clientType=WEB_ADMIN)
  - [x] Dashboard (dashboard.jsp - 375 lines) - Statistics cards with placeholders
  - [ ] Clinic management (list, create, edit, onboard)
  - [ ] Reports (INUS, RNDC, policies, access)
  - [ ] Audit logs (search, filter, export)
  - [ ] User management (search, view, update, deactivate)
  - [ ] Settings page
- [x] Technology Stack: JSP with inline JavaScript/CSS (no framework dependencies)
- [x] Responsive design (custom CSS with mobile-friendly layout)
- [x] Token management (sessionStorage, JWT parsing, auto-redirect)
- [x] Role-based access control (ADMIN role verification)

#### Dashboard Features Implemented (dashboard.jsp)
- [x] Professional dark theme (blue/gray color scheme)
- [x] User info display from JWT (name, CI, role)
- [x] Statistics cards (4 cards with placeholders):
  - Total Users, Total Documents, Active Policies, Total Clinics
- [x] Navigation menu (6 items): Dashboard, Clinics, Reports, Audit Logs, Users, Settings
- [x] Logout functionality with token revocation
- [x] Security features (token validation, role verification, auto-redirect)

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
- [x] Create patient portal: `src/main/webapp/patient/`
- [ ] Pages:
  - [x] Login flow (gub.uy SSO authentication via /api/auth/login/initiate?clientType=WEB_PATIENT)
  - [x] Dashboard (dashboard.jsp - 380 lines) - Statistics cards with placeholders
  - [ ] Clinical history (CU02) - list and view documents
  - [ ] Access policies (CU03) - create, edit, delete policies
  - [ ] Access audit (CU05) - who accessed my data
  - [ ] Notification preferences (CU04)
  - [ ] Access requests (approve/deny)
  - [ ] Settings page
- [x] Technology Stack: JSP with inline JavaScript/CSS (no framework dependencies)
- [x] Responsive design (custom CSS with mobile-friendly layout)
- [x] Token management (sessionStorage, JWT parsing, auto-redirect)
- [x] Role-based access control (PATIENT role verification)

#### Dashboard Features Implemented (dashboard.jsp)
- [x] Friendly light theme (blue/white color scheme)
- [x] User info display from JWT (name, CI)
- [x] Statistics cards (4 cards with placeholders):
  - My Documents, My Policies, Recent Accesses, Pending Approvals
- [x] Navigation menu (6 items): Dashboard, Clinical History, My Policies, Access History, Notifications, Settings
- [x] Logout functionality with token revocation
- [x] Security features (token validation, role verification, auto-redirect)

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

## PHASE 7: PERIPHERAL NODE INTEGRATION (AC003, AC013-AC016) - ✅ COMPLETE (2025-10-23)

### Peripheral Node API Client - ✅ COMPLETE

#### Client Implementation - ✅ COMPLETE
- [x] Create package: `uy.gub.hcen.integration.peripheral.*`
- [x] Implement `PeripheralNodeClient.java` (~525 lines)
  - [x] `retrieveDocument()` - Fetch document from peripheral node (AC015)
    - [x] GET {documentLocator}
    - [x] Authorization: Bearer token with API key
    - [x] Return: Document file (PDF, XML, FHIR)

### PDI Integration (Identity Validation) - ✅ COMPLETE (2025-10-23)

#### SOAP Client Implementation - ✅ COMPLETE
- [x] Create package: `uy.gub.hcen.integration.pdi.*`
- [x] Implement `PDIClient.java` (~450 lines) - SOAP 1.1/1.2 client
  - [x] `consultarUsuario(String ci)` - Query user by CI from PDI
    - [x] Returns: PDIUserData (ci, nombreCompleto, fechaNacimiento)
    - [x] Manual SOAP XML envelope construction (no JAX-WS dependency)
    - [x] WS-Security username/password authentication
    - [x] Apache HttpClient 5 for HTTPS communication
  - [x] `verifyAge(String ci, int minimumAge)` - Age verification (18+)
    - [x] Queries PDI, calculates age from date of birth
    - [x] Returns: true if user is at least minimumAge years old
  - [x] Circuit breaker pattern (5 failures threshold, 60s reset)
  - [x] Retry logic with exponential backoff (3 attempts: 1s, 2s, 4s)
  - [x] Configurable timeouts (connection: 5s, read: 30s)
  - [x] XML DOM parser for SOAP response handling
  - [x] Comprehensive error handling and logging
- [x] Implement `PDIUserData.java` - DTO for PDI response
  - [x] Fields: ci, nombreCompleto, fechaNacimiento
- [x] Implement `PDIException.java` - Custom exception
- [x] Update `InusService.java` - PDI integration
  - [x] Inject PDIClient via CDI
  - [x] Call PDI for age verification in registerUser()
  - [x] Graceful degradation: continues registration if PDI unavailable
  - [x] Logs warnings when PDI is down
- [x] Update `application.properties` - PDI configuration
  - [x] pdi.soap.endpoint (testing: https://pdi-testing.gub.uy/dnic/servicio-basico)
  - [x] pdi.soap.username
  - [x] pdi.soap.password
  - [x] pdi.soap.timeout.connect=5000
  - [x] pdi.soap.timeout.read=30000
- [x] Create `INFRASTRUCTURE_REQUIREMENTS.md` - PDI dependency documentation
  - [x] Registration requirements with AGESIC
  - [x] Testing and production endpoints
  - [x] Error handling strategy

**PDI Integration Summary:**
- Files Created: 3 (PDIClient.java, PDIUserData.java, PDIException.java)
- Files Modified: 3 (InusService.java, application.properties, INFRASTRUCTURE_REQUIREMENTS.md)
- Total Lines: ~450 lines
- Build Status: ✅ PASSING
- Date Completed: 2025-10-23

**Key Features:**
- Complete SOAP 1.1/1.2 client for identity validation
- Circuit breaker prevents overloading PDI service (5 failures → 60s cooldown)
- Retry logic handles transient failures (3 attempts with exponential backoff)
- HTTPS enforcement (AC002-AC004)
- Graceful degradation (system continues if PDI down)
- Age verification (18+ requirement) integrated into INUS registration
- WS-Security authentication with username/password
- Manual SOAP envelope construction (no JAX-WS dependency)
    - [x] Verify hash (SHA-256 integrity check)
  - [x] `sendOnboardingData()` - Send clinic config to peripheral (AC016)
    - [x] POST {peripheralNodeUrl}/api/onboard
    - [x] Body: { clinicId, apiKey, hcenCentralUrl, config }
  - [x] Circuit breaker for resilience
    - [x] Tracks consecutive failures (threshold: 5)
    - [x] Opens circuit on threshold
    - [x] Auto-resets after 60 seconds (half-open state)
  - [x] Retry logic with exponential backoff
    - [x] 3 attempts maximum
    - [x] Delays: 1s, 2s, 4s
    - [x] Only retries on network failures (IOException)
  - [x] Configurable timeouts
    - [x] Connection timeout: 5 seconds
    - [x] Read timeout: 30 seconds
  - [x] HTTPS validation (enforces AC002-AC004)
  - [x] Document integrity verification (SHA-256)
- [x] Implement `PeripheralNodeException.java`
- [x] Integration with ClinicManagementService
  - [x] Injected PeripheralNodeClient
  - [x] Removed temporary simulation code
  - [x] Full onboarding flow functional (AC016)

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

## 🎉 RECENT COMPLETIONS

### ✅ PDI SOAP CLIENT (2025-10-23)

**What Was Completed:**

**PDI Integration (SOAP Client) - COMPLETE (2025-10-23)**

1. **PDIClient.java** (~450 lines)
   - SOAP 1.1/1.2 client for PDI's Servicio Básico de Información de DNIC
   - WS-Security username/password authentication
   - Manual SOAP XML envelope construction (no JAX-WS dependency)
   - Apache HttpClient 5 for HTTPS communication
   - Circuit breaker pattern (5 failures threshold, 60s reset)
   - Retry logic with exponential backoff (3 attempts: 1s, 2s, 4s)
   - Configurable timeouts (connection: 5s, read: 30s)
   - XML DOM parser for SOAP response handling
   - Methods:
     * `consultarUsuario(String ci)` - Query user by CI
     * `verifyAge(String ci, int minimumAge)` - Age verification (18+)
     * `getCircuitBreakerState()` - Monitoring

2. **PDIUserData.java**
   - DTO for PDI response data
   - Fields: ci, nombreCompleto, fechaNacimiento

3. **PDIException.java**
   - Custom exception for PDI integration errors

4. **InusService.java Integration**
   - Injected PDIClient via CDI
   - Modified registerUser() to call PDI for age verification
   - Graceful degradation: continues registration if PDI unavailable
   - Logs warnings when PDI is down

5. **application.properties**
   - Added PDI configuration section:
     * pdi.soap.endpoint (testing: https://pdi-testing.gub.uy/dnic/servicio-basico)
     * pdi.soap.username
     * pdi.soap.password
     * pdi.soap.timeout.connect=5000
     * pdi.soap.timeout.read=30000

6. **INFRASTRUCTURE_REQUIREMENTS.md**
   - Added PDI external dependency documentation
   - Registration requirements with AGESIC
   - Testing and production endpoints
   - Error handling strategy

**Files created:** 3 files
**Files modified:** 3 files
**Total lines:** ~450 lines
**Build status:** ✅ PASSING
**Date completed:** 2025-10-23

**Key features:**
- Complete SOAP client for identity validation
- Circuit breaker prevents overloading PDI service
- Retry logic handles transient failures
- HTTPS enforcement (AC002-AC004)
- Graceful degradation (system continues if PDI down)
- Age verification (18+ requirement) integrated

**Integration points:**
- InusService.registerUser() now calls PDI for age verification
- Policy Engine can use PDI data for enhanced authorization
- Audit system logs all PDI queries

---

### ✅ PERIPHERAL NODE CLIENT (2025-10-23)

**What Was Completed:**

**PeripheralNodeClient (100%)**
- Created `src/main/java/uy/gub/hcen/integration/peripheral/PeripheralNodeClient.java` (~525 lines)
- Clinic onboarding integration (AC016)
  - POST to `{peripheralNodeUrl}/api/onboard`
  - Sends clinic configuration (clinicId, apiKey, hcenCentralUrl, config)
  - Returns true if peripheral node confirms (HTTP 200-299)
- Document retrieval integration (AC015)
  - GET `{documentLocator}` with Bearer authentication
  - Returns document bytes (PDF, XML, FHIR)
  - SHA-256 hash verification for integrity
- Circuit breaker pattern for resilience
  - Tracks consecutive failures (opens after 5 failures)
  - Auto-resets after 60 seconds (half-open state)
  - Prevents cascading failures to peripheral nodes
- Retry logic with exponential backoff
  - 3 attempts maximum
  - Delays: 1s, 2s, 4s
  - Only retries on network failures (IOException)
  - No retry on business logic errors (4xx responses)
- Configurable HTTP timeouts
  - Connection timeout: 5 seconds
  - Read timeout: 30 seconds
- Security features
  - HTTPS validation (enforces AC002-AC004)
  - API key in Authorization: Bearer header
  - Document integrity verification (SHA-256)

**PeripheralNodeException (100%)**
- Created `src/main/java/uy/gub/hcen/integration/peripheral/PeripheralNodeException.java`
- Checked exception for peripheral node communication failures

**ClinicManagementService Integration (100%)**
- Updated `src/main/java/uy/gub/hcen/service/clinic/ClinicManagementService.java`
- Injected PeripheralNodeClient via CDI
- Removed temporary simulation code
- Full AC016 onboarding flow now functional

**Statistics:**
- **Files Created**: 2 files
- **Total Lines**: ~565 lines
- **Build Status**: ✅ PASSING
- **Integration Status**: ✅ COMPLETE

**Key Features Implemented:**
- Complete HTTP client for peripheral node communication
- Circuit breaker prevents overloading failed peripheral nodes
- Retry logic handles transient network failures gracefully
- HTTPS enforcement for all peripheral node communication
- Document integrity verification ensures data hasn't been tampered with
- Comprehensive error handling and logging

**AC Requirements Completed:**
- ✅ **AC016 (Clinic Onboarding)**: HCEN can now send configuration to peripheral nodes
- ✅ **AC015 (Document Retrieval)**: HCEN can now fetch actual documents from peripheral storage
- ✅ **AC002-AC004 (HTTPS)**: All peripheral communication uses HTTPS

**Next Steps:**
1. Create mock peripheral node service for testing
2. Write unit tests for PeripheralNodeClient (circuit breaker, retry logic, etc.)
3. Write integration tests for complete onboarding flow (AC016)
4. Test document retrieval with hash verification (AC015)

---

### ✅ CLINIC MANAGEMENT SERVICE (2025-10-23)

**What Was Completed:**

**ClinicManagementService (100%)**
- Created `src/main/java/uy/gub/hcen/service/clinic/ClinicManagementService.java` (~650 lines)
- Clinic registration with secure API key generation (SecureRandom, 64 bytes, Base64)
- Clinic lookup and search (by ID, status, city)
- Clinic updates (profile information)
- Clinic activation/deactivation
- Clinic onboarding workflow (AC016) - ready for PeripheralNodeClient integration
- Clinic statistics (users, documents, policies)
- Complete audit logging integration

**ClinicResource REST API (100%)**
- Created `src/main/java/uy/gub/hcen/api/rest/ClinicResource.java` (~450 lines)
- 7 REST endpoints:
  - POST /api/admin/clinics - Register clinic (CU10)
  - GET /api/admin/clinics - List/search with pagination and filters
  - GET /api/admin/clinics/{clinicId} - Get clinic details
  - PUT /api/admin/clinics/{clinicId} - Update clinic
  - POST /api/admin/clinics/{clinicId}/onboard - Trigger onboarding (AC016)
  - DELETE /api/admin/clinics/{clinicId} - Deactivate (soft delete)
  - GET /api/admin/clinics/{clinicId}/statistics - Get clinic stats

**DTOs Created (7 files with Bean Validation)**
- ClinicRegistrationRequest.java
- ClinicUpdateRequest.java
- ClinicResponse.java (with API key masking)
- ClinicListResponse.java (paginated)
- ClinicStatisticsResponse.java
- OnboardingRequest.java (AC016)
- OnboardingResponse.java (AC016)

**Exception Classes (3 files)**
- ClinicNotFoundException.java
- ClinicRegistrationException.java
- OnboardingException.java

**Configuration**
- Registered ClinicResource in RestApplication.java

**Statistics:**
- **Files Created**: 12 files
- **Total Lines**: ~1,800 lines
- **REST Endpoints**: 7 endpoints
- **Build Status**: ✅ PASSING

**Key Features Implemented:**
- Secure API key generation (SecureRandom, 64 bytes, Base64-encoded)
- Comprehensive audit logging for all operations
- Clinic search with filters (status, city)
- Pagination support for clinic listing
- Statistics aggregation (users, documents, policies per clinic)
- Onboarding workflow (AC016) - interface ready, pending PeripheralNodeClient
- Bean Validation on all request DTOs
- API key masking in responses (security)

**Pending Dependencies:**
- PeripheralNodeClient.java (needed for full AC016 implementation)

**Next Steps:**
1. Implement PeripheralNodeClient.java to complete AC016 onboarding
2. Create admin UI page for clinic management (admin/clinics.jsp)
3. Write unit tests for ClinicManagementService (80% coverage target)
4. Integration tests for onboarding flow

---

### ✅ SSO AUTHENTICATION & DASHBOARDS (2025-10-22)

**What Was Completed:**

**Backend SSO Fixes (100%)**
- Fixed role assignment in `AuthenticationService.java` with `determineRole()` method
- Updated `StateManager.java` with `peekState()` for client type detection
- Fixed OAuth callback redirect logic in `AuthenticationResource.java`
- Configured redirect URI: `http://localhost:8080/hcen/api/auth/callback`
- Updated gub.uy OAuth credentials (client_id: 890192)

**Admin Dashboard (100%)**
- Created `src/main/webapp/admin/dashboard.jsp` (375 lines)
- Professional dark theme (blue/gray color scheme)
- JWT token management (sessionStorage, parsing, validation)
- Role-based access control (ADMIN only)
- Statistics cards (4 placeholders): Users, Documents, Policies, Clinics
- Navigation menu (6 items)
- Logout with token revocation
- Security features (token removal from URL, auto-redirect on unauthorized)

**Patient Dashboard (100%)**
- Created `src/main/webapp/patient/dashboard.jsp` (380 lines)
- Friendly light theme (blue/white color scheme)
- JWT token management (sessionStorage, parsing, validation)
- Role-based access control (PATIENT only)
- Statistics cards (4 placeholders): Documents, Policies, Accesses, Approvals
- Navigation menu (6 items)
- Logout with token revocation
- Security features (token removal from URL, auto-redirect on unauthorized)

**Critical Next Steps:**
1. Register redirect URI with gub.uy: `http://localhost:8080/hcen/api/auth/callback`
2. Implement statistics API endpoints:
   - GET /api/admin/dashboard/stats
   - GET /api/patient/dashboard/stats
3. Create additional admin pages (clinics, reports, audit, users, settings)
4. Create additional patient pages (history, policies, access-history, notifications, settings)

---

## 🎉 PHASE 1+2 COMPLETION SUMMARY (Updated 2025-10-23)

### ✅ What Was Completed

**Phase 1: Core Services - 100% COMPLETE (2025-10-22)**

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

**Phase 2: Admin Features - CLINIC MANAGEMENT COMPLETE (2025-10-23)**

5. **ClinicManagementService** (650 lines)
   - Clinic registration with secure API key generation
   - Clinic lookup and search with pagination
   - Clinic profile management
   - Clinic activation/deactivation
   - Clinic onboarding workflow (AC016) - ready for PeripheralNodeClient
   - Clinic statistics aggregation
   - REST API: 7 endpoints
   - DTOs: 7 (ClinicRegistrationRequest, ClinicUpdateRequest, ClinicResponse, ClinicListResponse, ClinicStatisticsResponse, OnboardingRequest, OnboardingResponse)
   - Exceptions: 3 (ClinicNotFoundException, ClinicRegistrationException, OnboardingException)

**Cumulative Statistics (Phase 1+2):**
- **Total Java Files**: 125 files
- **Total Lines of Code**: ~14,130 lines (services only)
- **Services**: 5/8 (63%)
- **REST Endpoints**: 37 endpoints (30 from Phase 1 + 7 from Phase 2)
- **Policy Evaluators**: 6/6 (100%)
- **DTOs**: 40 DTOs total
- **Build Status**: ✅ PASSING (compiles successfully)

### 🎯 What's Next: Recommended Path Forward

**Option A: Complete Phase 2 (Remaining Admin & Patient Features)**
- NotificationService (Firebase integration)
- ClinicalHistoryService
- AccessRequestService (workflow)
- ReportsService (Analytics, dashboard)
- IpsFhirService

**Option B: External Integrations First**
- PeripheralNodeClient.java (REST - document retrieval, onboarding) - **NEEDED for AC016**
- PDIClient.java (SOAP - identity validation)
- FirebaseMessagingClient.java (Push notifications)

**Option C: Testing Phase**
- Write unit tests for all 5 services (80% coverage target)
- Integration tests
- Performance tests
- Security tests

**Option D: Admin UI Pages**
- Clinic management page (admin/clinics.jsp) - integrate with new API
- Reports page (admin/reports.jsp)
- Audit log viewer (admin/audit.jsp)
- User management (admin/users.jsp)

**Recommendation** (Updated 2025-10-23):
1. ~~**Priority 1**: Implement **PeripheralNodeClient.java** (Option B)~~ ✅ **COMPLETE**
2. ~~**Priority 2**: Implement **PDIClient.java** (SOAP integration)~~ ✅ **COMPLETE**
3. **NEW Priority 1**: Start **Testing (Option C)** to achieve 80% coverage on existing services (meets AC017/AC018)
4. **Priority 2**: Complete remaining Phase 2 services (Option A) - NotificationService, ClinicalHistoryService, ReportsService
5. **Priority 3**: Implement FirebaseMessagingClient.java (final external integration)

### ⚠️ Important Notes

1. ~~**PeripheralNodeClient**~~: ✅ **COMPLETE** (2025-10-23) - AC016 and AC015 fully functional
2. ~~**PDI Integration**~~: ✅ **COMPLETE** (2025-10-23) - PDIClient.java fully integrated with InusService
3. **Testing**: NO tests written yet - this is **CRITICAL** for 80% coverage requirement (AC017/AC018)
4. **SSO Implementation**: Authentication system exists and working, gub.uy OAuth configured (client_id: 890192)
5. **Access Request Workflow**: Schema exists, but AccessRequestService not yet implemented
6. **External Integrations**: 2/3 complete (Peripheral + PDI done, Firebase pending)

### 📋 Immediate Next Steps (Choose One)

1. **If focusing on testing**:
   - Write unit tests for InusService (10-15 test cases)
   - Write unit tests for RndcService (10-15 test cases)
   - Write unit tests for PolicyEngine (15-20 test cases for all evaluators)
   - Write unit tests for AuditService (10-15 test cases)
   - Write unit tests for ClinicManagementService (10-15 test cases)
   - Target: 80% code coverage with JaCoCo

2. **If focusing on external integrations**:
   - ~~**HIGH PRIORITY**: Implement PeripheralNodeClient.java~~ ✅ **COMPLETE** (2025-10-23)
   - ~~**HIGH PRIORITY**: Implement PDIClient.java (SOAP client for identity validation)~~ ✅ **COMPLETE** (2025-10-23)
   - ~~Complete PDI integration in InusService~~ ✅ **COMPLETE** (2025-10-23)
   - ~~Complete AC016 onboarding in ClinicManagementService~~ ✅ **COMPLETE**
   - **NEW HIGH PRIORITY**: Implement FirebaseMessagingClient.java (last external integration)
   - Set up pdi-mock service for testing PDI integration

3. **If focusing on Phase 2 services**:
   - Implement NotificationService with Firebase
   - Implement ClinicalHistoryService
   - Implement AccessRequestService
   - Implement ReportsService
   - Add 10-15 more REST endpoints

4. **If focusing on Admin UI**:
   - Create admin/clinics.jsp page (clinic management)
   - Integrate with /api/admin/clinics endpoints
   - Add clinic listing, search, create, edit, onboard functionality
   - Create admin/reports.jsp for analytics dashboard
   - Create admin/audit.jsp for audit log viewer

---

**END OF TODO.MD**

This document will be updated as implementation progresses. Each completed task should be marked with [x].

**Last Major Update**: 2025-10-23 - PDI SOAP Client Complete (Identity validation fully integrated)
**Previous Updates**:
- 2025-10-23 - PeripheralNodeClient Complete (AC016 + AC015 fully functional)
- 2025-10-23 - Phase 2 Clinic Management Complete (ClinicManagementService + 7 REST endpoints)
- 2025-10-22 - Phase 1 Core Services Complete (InusService, RndcService, PolicyEngine, AuditService)
- 2025-10-22 - SSO Authentication & Dashboards Complete