# Policy Engine & Audit System - Architecture Diagrams

Visual representations of system architecture, data flows, and component interactions.

---

## Table of Contents

1. [High-Level System Architecture](#high-level-system-architecture)
2. [Policy Engine Architecture](#policy-engine-architecture)
3. [Audit System Architecture](#audit-system-architecture)
4. [Integration Flow: Document Access with Policy Enforcement](#integration-flow-document-access-with-policy-enforcement)
5. [Emergency Access Workflow](#emergency-access-workflow)
6. [Audit Log Hash Chain](#audit-log-hash-chain)
7. [MongoDB Archival Flow](#mongodb-archival-flow)
8. [Database Schema Relationships](#database-schema-relationships)

---

## High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        HCEN Central Component                        │
│                     (WildFly Jakarta EE 10)                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌────────────────────┐  ┌────────────────────┐  ┌───────────────┐ │
│  │  INUS              │  │  RNDC              │  │  Policy       │ │
│  │  (User Registry)   │  │  (Doc Registry)    │  │  Management   │ │
│  │                    │  │                    │  │               │ │
│  │  ✓ User lookup     │  │  ✓ Doc metadata    │  │  ✓ CRUD       │ │
│  │  ✓ Registration    │  │  ✓ Search          │  │  ✓ Evaluate   │ │
│  │  ✓ PDI integration │  │  ✓ Policy check ◄──┼──┘  ✓ Cache      │ │
│  └────────────────────┘  └────────────────────┘  └───────────────┘ │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │                    Policy Engine                                │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────────────┐  │ │
│  │  │ Evaluators   │  │ Conflict     │  │ Cache Service       │  │ │
│  │  │ • DocumentType│  │ Resolution   │  │ (Redis)             │  │ │
│  │  │ • Specialty  │  │ DENY >       │  │ 5-min TTL           │  │ │
│  │  │ • TimeBased  │  │ PERMIT >     │  │                     │  │ │
│  │  │ • Clinic     │  │ PENDING      │  │                     │  │ │
│  │  │ • Professional│ │              │  │                     │  │ │
│  │  │ • Emergency  │  │              │  │                     │  │ │
│  │  │ • Role [NEW] │  │              │  │                     │  │ │
│  │  └──────────────┘  └──────────────┘  └─────────────────────┘  │ │
│  └────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │                    Audit System                                 │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────────────┐  │ │
│  │  │ AuditService │  │ Hash Chain   │  │ Retention Service   │  │ │
│  │  │ • logEvent() │  │ Integrity    │  │ PostgreSQL → Mongo  │  │ │
│  │  │ • @Audited   │  │ Validation   │  │ 90-day cutoff       │  │ │
│  │  │ interceptor  │  │              │  │                     │  │ │
│  │  └──────────────┘  └──────────────┘  └─────────────────────┘  │ │
│  └────────────────────────────────────────────────────────────────┘ │
│                                                                      │
└──┬───────────────────────┬──────────────────────┬──────────────────┘
   │                       │                      │
   │                       │                      │
┌──▼───────────┐   ┌──────▼──────────┐   ┌──────▼──────────┐
│ PostgreSQL   │   │ MongoDB         │   │ Redis           │
│              │   │                 │   │                 │
│ • INUS       │   │ • Audit Archive │   │ • Policy Cache  │
│ • RNDC       │   │   (90+ days)    │   │ • Session       │
│ • Policies   │   │ • 7-year TTL    │   │                 │
│ • Audit      │   │                 │   │                 │
│   (90 days)  │   │                 │   │                 │
└──────────────┘   └─────────────────┘   └─────────────────┘
```

---

## Policy Engine Architecture

### Component Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                       Policy Engine                                  │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │                   PolicyEngine (Orchestrator)                   │ │
│  │                      @Stateless EJB                             │ │
│  ├────────────────────────────────────────────────────────────────┤ │
│  │                                                                 │ │
│  │  evaluate(AccessRequest): PolicyEvaluationResult                │ │
│  │  ┌──────────────────────────────────────────────────────────┐  │ │
│  │  │  1. Check cache (Redis)                                   │  │ │
│  │  │     ↓ miss                                                │  │ │
│  │  │  2. Load active policies for patient                      │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  3. Find appropriate evaluator for each policy            │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  4. Evaluate all policies                                 │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  5. Resolve conflicts (DENY > PERMIT > PENDING)           │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  6. Cache decision (5-min TTL)                            │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  7. Log evaluation (AuditService)                         │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  8. Return PolicyEvaluationResult                         │  │ │
│  │  └──────────────────────────────────────────────────────────┘  │ │
│  └────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │                Strategy Pattern: Policy Evaluators              │ │
│  ├────────────────────────────────────────────────────────────────┤ │
│  │                                                                 │ │
│  │  ┌─────────────────────────┐   ┌─────────────────────────┐    │ │
│  │  │ DocumentTypePolicyEval  │   │ SpecialtyPolicyEval     │    │ │
│  │  │ supports(DOCUMENT_TYPE) │   │ supports(SPECIALTY)     │    │ │
│  │  │ evaluate(policy, req)   │   │ evaluate(policy, req)   │    │ │
│  │  │ → PERMIT/DENY/null      │   │ → PERMIT/DENY/null      │    │ │
│  │  └─────────────────────────┘   └─────────────────────────┘    │ │
│  │                                                                 │ │
│  │  ┌─────────────────────────┐   ┌─────────────────────────┐    │ │
│  │  │ TimeBasedPolicyEval     │   │ ClinicPolicyEval        │    │ │
│  │  │ supports(TIME_BASED)    │   │ supports(CLINIC)        │    │ │
│  │  │ evaluate(policy, req)   │   │ evaluate(policy, req)   │    │ │
│  │  │ → PERMIT/DENY/null      │   │ → PERMIT/DENY/null      │    │ │
│  │  └─────────────────────────┘   └─────────────────────────┘    │ │
│  │                                                                 │ │
│  │  ┌─────────────────────────┐   ┌─────────────────────────┐    │ │
│  │  │ ProfessionalPolicyEval  │   │ EmergencyOverrideEval   │    │ │
│  │  │ supports(PROFESSIONAL)  │   │ supports(EMERGENCY)     │    │ │
│  │  │ evaluate(policy, req)   │   │ evaluate(policy, req)   │    │ │
│  │  │ → PERMIT/DENY/null      │   │ → PERMIT + NOTIFY       │    │ │
│  │  └─────────────────────────┘   └─────────────────────────┘    │ │
│  │                                                                 │ │
│  │  ┌─────────────────────────┐                                   │ │
│  │  │ RolePolicyEval [NEW]    │                                   │ │
│  │  │ supports(ROLE)          │                                   │ │
│  │  │ evaluate(policy, req)   │                                   │ │
│  │  │ • Role hierarchy        │                                   │ │
│  │  │ • ADMIN > DOCTOR >      │                                   │ │
│  │  │   NURSE > RECEPTIONIST  │                                   │ │
│  │  └─────────────────────────┘                                   │ │
│  └────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │               Supporting Services                               │ │
│  ├────────────────────────────────────────────────────────────────┤ │
│  │                                                                 │ │
│  │  ┌─────────────────────┐  ┌──────────────────────────────┐    │ │
│  │  │ PolicyCacheService  │  │ EmergencyAccessService [NEW] │    │ │
│  │  │ (Redis)             │  │ • logEmergencyAccess()       │    │ │
│  │  │ • cachePolicyDec()  │  │ • sendNotification()         │    │ │
│  │  │ • getCachedDec()    │  │ • createReview()             │    │ │
│  │  │ • 5-min TTL         │  │ • confirmReview()            │    │ │
│  │  └─────────────────────┘  └──────────────────────────────┘    │ │
│  │                                                                 │ │
│  │  ┌─────────────────────┐  ┌──────────────────────────────┐    │ │
│  │  │ PolicyAnalytics     │  │ PolicyVersioning [NEW]       │    │ │
│  │  │ [NEW]               │  │ • JPA @PreUpdate listener    │    │ │
│  │  │ • detectConflicts() │  │ • Snapshot policy state      │    │ │
│  │  │ • recommendOpt()    │  │ • Track who changed          │    │ │
│  │  └─────────────────────┘  └──────────────────────────────┘    │ │
│  └────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

### Policy Evaluation Flow

```
Professional → RNDC → PolicyEngine
                        │
                        ├──[1]──> Check Cache (Redis)
                        │         "policy:decision:12345678:CARDIOLOGY:LAB_RESULT"
                        │         └─> HIT? Return cached decision
                        │         └─> MISS? Continue...
                        │
                        ├──[2]──> Load Active Policies
                        │         SELECT * FROM policies.access_policies
                        │         WHERE patient_ci = '12345678'
                        │           AND (valid_until IS NULL OR valid_until > NOW())
                        │         ORDER BY priority DESC
                        │
                        ├──[3]──> For Each Policy:
                        │         ┌─────────────────────────────────┐
                        │         │ Find matching evaluator         │
                        │         │   • DOCUMENT_TYPE → DocTypeEval │
                        │         │   • SPECIALTY → SpecialtyEval   │
                        │         │   • etc.                        │
                        │         └─────────────────────────────────┘
                        │
                        ├──[4]──> Evaluate:
                        │         Policy 1 (SPECIALTY, DENY, cardiologists)
                        │           → evaluate() → DENY
                        │         Policy 2 (DOCUMENT_TYPE, PERMIT, lab results)
                        │           → evaluate() → PERMIT
                        │         Policy 3 (TIME_BASED, DENY, after hours)
                        │           → evaluate() → null (not applicable)
                        │
                        ├──[5]──> Resolve Conflicts:
                        │         Decisions: [DENY, PERMIT]
                        │         Hierarchy: DENY > PERMIT > PENDING
                        │         Result: DENY (fail-safe)
                        │
                        ├──[6]──> Cache Decision:
                        │         SET "policy:decision:12345678:CARDIOLOGY:LAB_RESULT" "DENY"
                        │         EXPIRE ... 300  (5 minutes)
                        │
                        ├──[7]──> Log Evaluation:
                        │         auditService.logEvent(
                        │           POLICY_EVALUATION,
                        │           professionalId,
                        │           decision: DENY,
                        │           reason: "Denied by policy 123"
                        │         )
                        │
                        └──[8]──> Return PolicyEvaluationResult
                                  {
                                    decision: DENY,
                                    reason: "Access denied by policy 123",
                                    decidingPolicy: 123,
                                    evaluatedPolicies: [123, 456]
                                  }
```

---

## Audit System Architecture

### Component Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                       Audit System                                   │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │                   AuditService (Core)                           │ │
│  │                    @Stateless EJB                               │ │
│  ├────────────────────────────────────────────────────────────────┤ │
│  │                                                                 │ │
│  │  logEvent(eventType, actorId, resourceType, outcome, ...)      │ │
│  │  ┌──────────────────────────────────────────────────────────┐  │ │
│  │  │  1. Validate required parameters                          │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  2. Serialize details to JSON                             │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  3. Create AuditLog entity (immutable)                    │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  4. Get last log's currentHash → previousHash             │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  5. Save to PostgreSQL (get auto-generated ID)            │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  6. Compute currentHash = SHA256(id + fields + prevHash)  │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  7. Update audit_logs with hashes (native query)          │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  8. Log success (never throw exception)                   │  │ │
│  │  └──────────────────────────────────────────────────────────┘  │ │
│  │                                                                 │ │
│  │  Specialized logging methods:                                  │ │
│  │  • logDocumentAccess(professionalId, patientCi, docId, ...)   │ │
│  │  • logAuthenticationEvent(userId, outcome, ...)               │ │
│  │  • logPolicyChange(patientCi, policyId, ...)                  │ │
│  │  • logEmergencyAccess(professionalId, patientCi, ...)         │ │
│  └────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │             @Audited Interceptor (Automatic Auditing) [NEW]     │ │
│  │                      CDI Interceptor                            │ │
│  ├────────────────────────────────────────────────────────────────┤ │
│  │                                                                 │ │
│  │  @AroundInvoke auditMethodInvocation(InvocationContext ctx)    │ │
│  │  ┌──────────────────────────────────────────────────────────┐  │ │
│  │  │  1. Extract @Audited annotation parameters                │  │ │
│  │  │     • eventType, resourceType, resourceIdExpression        │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  2. Extract actorId from SecurityContext (JWT)            │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  3. Extract resourceId from method parameters (SpEL)      │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  4. Extract HTTP context (IP, user agent)                 │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  5. Invoke actual method: result = ctx.proceed()          │  │ │
│  │  │     ↓ success                         ↓ exception         │  │ │
│  │  │  6. Call AuditService.logEvent()                          │  │ │
│  │  │     with outcome = SUCCESS            with outcome = FAIL │  │ │
│  │  │     ↓                                  ↓                   │  │ │
│  │  │  7. Return result                  7. Re-throw exception  │  │ │
│  │  └──────────────────────────────────────────────────────────┘  │ │
│  │                                                                 │ │
│  │  Usage example:                                                 │ │
│  │  @Audited(eventType = ACCESS, resourceType = "DOCUMENT",       │ │
│  │           resourceIdExpression = "#documentId")                │ │
│  │  public Document getDocument(Long documentId) { ... }          │ │
│  └────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │              AuditIntegrityService (Hash Chain) [NEW]           │ │
│  ├────────────────────────────────────────────────────────────────┤ │
│  │                                                                 │ │
│  │  computeHash(log, previousHash): String                        │ │
│  │  ┌──────────────────────────────────────────────────────────┐  │ │
│  │  │  data = id + "|" + eventType + "|" + actorId + "|" +      │  │ │
│  │  │         resourceId + "|" + timestamp + "|" + previousHash │  │ │
│  │  │  hash = SHA256(data)                                       │  │ │
│  │  │  return hex(hash)                                          │  │ │
│  │  └──────────────────────────────────────────────────────────┘  │ │
│  │                                                                 │ │
│  │  validateIntegrity(): AuditIntegrityReport                     │ │
│  │  ┌──────────────────────────────────────────────────────────┐  │ │
│  │  │  For each audit log (ordered by ID):                      │  │ │
│  │  │    1. Verify previousHash matches expected                │  │ │
│  │  │    2. Recompute currentHash and verify                    │  │ │
│  │  │    3. If mismatch → add to violations list                │  │ │
│  │  │  Return report with all violations                        │  │ │
│  │  └──────────────────────────────────────────────────────────┘  │ │
│  └────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │          AuditRetentionService (MongoDB Archival) [NEW]         │ │
│  │                    @Schedule(hour="2")                          │ │
│  ├────────────────────────────────────────────────────────────────┤ │
│  │                                                                 │ │
│  │  @Schedule(hour = "2", minute = "0")                           │ │
│  │  archiveOldLogs()                                              │ │
│  │  ┌──────────────────────────────────────────────────────────┐  │ │
│  │  │  1. Find logs older than 90 days from PostgreSQL          │  │ │
│  │  │     SELECT * FROM audit.audit_logs                        │  │ │
│  │  │     WHERE timestamp < NOW() - INTERVAL '90 days'          │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  2. For each log:                                         │  │ │
│  │  │     a. Convert to MongoDB document                        │  │ │
│  │  │     b. Insert into MongoDB audit_logs collection          │  │ │
│  │  │     c. Delete from PostgreSQL (if not critical event)     │  │ │
│  │  │     ↓                                                      │  │ │
│  │  │  3. Log archival statistics                               │  │ │
│  │  └──────────────────────────────────────────────────────────┘  │ │
│  │                                                                 │ │
│  │  getArchivedPatientHistory(patientCi, from, to)                │ │
│  │  • Query MongoDB for patient access history                    │ │
│  │  • Convert documents back to AuditLog entities                 │ │
│  └────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │              AuditExportService (CSV/JSON Export) [NEW]         │ │
│  ├────────────────────────────────────────────────────────────────┤ │
│  │                                                                 │ │
│  │  exportToCsv(exportRequest): StreamingOutput                   │ │
│  │  • Pagination (1000 logs/page)                                 │ │
│  │  • Stream to output (avoid memory overflow)                    │ │
│  │  • CSV format: ID, Timestamp, EventType, Actor, ...            │ │
│  │                                                                 │ │
│  │  exportToJson(exportRequest): StreamingOutput                  │ │
│  │  • Pagination (1000 logs/page)                                 │ │
│  │  • Stream JSON array                                           │ │
│  └────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │          AuditAnalyticsService (Anomaly Detection) [NEW]        │ │
│  ├────────────────────────────────────────────────────────────────┤ │
│  │                                                                 │ │
│  │  detectAnomalies(from, to): List<AnomalyReport>                │ │
│  │  • Bulk access (>50 patients in 1 hour)                        │ │
│  │  • Off-hours access (outside 9-5)                              │ │
│  │  • Failed authentication patterns (brute force)                │ │
│  │  • Emergency access without prior interaction                  │ │
│  │                                                                 │ │
│  │  getMostAccessedDocuments(from, to, limit)                     │ │
│  │  • SQL aggregation: GROUP BY resource_id                       │ │
│  └────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Integration Flow: Document Access with Policy Enforcement

```
┌─────────────┐
│ Professional│ (via Peripheral Node)
└──────┬──────┘
       │
       │ GET /api/rndc/documents/456
       │ Authorization: Bearer <JWT>
       ▼
┌──────────────────────────────────────────────────────────────┐
│                    RndcResource (JAX-RS)                      │
└───────────────────────┬──────────────────────────────────────┘
                        │
                        │ getDocument(456, securityContext)
                        ▼
┌──────────────────────────────────────────────────────────────┐
│                    RndcService                                │
│  @Audited(eventType=ACCESS, resourceType="DOCUMENT")         │
├──────────────────────────────────────────────────────────────┤
│  1. Fetch document metadata from database                    │
│     • patientCi, documentType, locator                       │
│                                                               │
│  2. Extract professional context from JWT                    │
│     • professionalId, specialties[], clinicId, role          │
│                                                               │
│  3. Build AccessRequest                                      │
│     • professionalId, specialties, clinicId, role            │
│     • patientCi, documentType, documentId                    │
└───────────────────────┬──────────────────────────────────────┘
                        │
                        │ evaluate(accessRequest)
                        ▼
┌──────────────────────────────────────────────────────────────┐
│                  PolicyEngine.evaluate()                      │
├──────────────────────────────────────────────────────────────┤
│  1. Check Redis cache                                        │
│     key = "policy:12345678:CARDIOLOGY:LAB_RESULT"            │
│     └─> MISS (not cached)                                    │
│                                                               │
│  2. Load patient policies from database                      │
│     SELECT * FROM policies.access_policies                   │
│     WHERE patient_ci = '12345678' AND active                 │
│     └─> Found 3 policies                                     │
│                                                               │
│  3. Evaluate each policy                                     │
│     Policy 1 (SPECIALTY, DENY, cardiologists)                │
│       → SpecialtyPolicyEvaluator.evaluate() → DENY           │
│     Policy 2 (DOCUMENT_TYPE, PERMIT, all)                    │
│       → DocumentTypePolicyEvaluator.evaluate() → PERMIT      │
│     Policy 3 (TIME_BASED, DENY, after-hours)                 │
│       → TimeBasedPolicyEvaluator.evaluate() → null           │
│                                                               │
│  4. Resolve conflicts                                        │
│     Decisions: [DENY, PERMIT]                                │
│     Result: DENY (DENY > PERMIT)                             │
│                                                               │
│  5. Cache decision                                           │
│     SET "policy:12345678:CARDIOLOGY:LAB_RESULT" "DENY" EX 300│
│                                                               │
│  6. Log policy evaluation                                    │
│     auditService.logEvent(POLICY_EVALUATION, ...)            │
└───────────────────────┬──────────────────────────────────────┘
                        │
                        │ PolicyEvaluationResult
                        │ { decision: DENY, reason: "...", ... }
                        ▼
┌──────────────────────────────────────────────────────────────┐
│                  RndcService (continued)                      │
├──────────────────────────────────────────────────────────────┤
│  4. Handle decision                                          │
│                                                               │
│     IF decision == DENY:                                     │
│       → throw ForbiddenException("Access denied...")         │
│                                                               │
│     IF decision == PENDING:                                  │
│       → accessRequestService.createPendingRequest()          │
│       → notificationService.sendMobileNotification()         │
│       → throw PendingApprovalException("Requires approval")  │
│                                                               │
│     IF decision == PERMIT:                                   │
│       → return Document (with locator URL)                   │
└───────────────────────┬──────────────────────────────────────┘
                        │
                        │ (Automatically audited by @Audited)
                        ▼
┌──────────────────────────────────────────────────────────────┐
│              AuditedInterceptor (CDI)                         │
├──────────────────────────────────────────────────────────────┤
│  Extract audit context from @Audited annotation:             │
│  • eventType = ACCESS                                        │
│  • resourceType = "DOCUMENT"                                 │
│  • resourceId = "456"                                        │
│  • actorId = professionalId (from JWT)                       │
│  • outcome = DENIED (exception thrown)                       │
│                                                               │
│  Call AuditService.logEvent(...)                             │
└───────────────────────┬──────────────────────────────────────┘
                        │
                        │ logEvent(ACCESS, prof-123, DOCUMENT, 456, DENIED)
                        ▼
┌──────────────────────────────────────────────────────────────┐
│                     AuditService                              │
├──────────────────────────────────────────────────────────────┤
│  1. Create AuditLog entity                                   │
│     • eventType = ACCESS                                     │
│     • actorId = "prof-123"                                   │
│     • resourceType = "DOCUMENT"                              │
│     • resourceId = "456"                                     │
│     • actionOutcome = DENIED                                 │
│     • timestamp = NOW()                                      │
│                                                               │
│  2. Get previous hash                                        │
│     SELECT current_hash FROM audit.audit_logs                │
│     ORDER BY id DESC LIMIT 1                                 │
│     └─> previousHash = "a1b2c3..."                           │
│                                                               │
│  3. Save audit log (get ID)                                  │
│     INSERT INTO audit.audit_logs VALUES (...)                │
│     └─> id = 98765                                           │
│                                                               │
│  4. Compute current hash                                     │
│     data = "98765|ACCESS|prof-123|DOCUMENT|456|2025-10-30T..|a1b2c3..."│
│     currentHash = SHA256(data) = "d4e5f6..."                 │
│                                                               │
│  5. Update with hashes                                       │
│     UPDATE audit.audit_logs                                  │
│     SET previous_hash = 'a1b2c3...', current_hash = 'd4e5f6...'│
│     WHERE id = 98765                                         │
└──────────────────────────────────────────────────────────────┘
                        │
                        │ (Hash chain complete)
                        ▼
              ┌────────────────────┐
              │  Audit Log Saved   │
              │  • Immutable       │
              │  • Tamper-evident  │
              │  • Traceable       │
              └────────────────────┘
                        │
                        │ 90 days later...
                        ▼
┌──────────────────────────────────────────────────────────────┐
│          AuditRetentionService (@Schedule)                    │
├──────────────────────────────────────────────────────────────┤
│  Daily job at 2 AM:                                          │
│  1. Find logs older than 90 days                             │
│  2. Convert to MongoDB document                              │
│  3. Insert into MongoDB audit_logs collection                │
│  4. Delete from PostgreSQL (if not critical)                 │
└──────────────────────────────────────────────────────────────┘
                        │
                        ▼
              ┌────────────────────┐
              │   MongoDB Archive  │
              │   • 7-year TTL     │
              │   • Queryable      │
              │   • Restorable     │
              └────────────────────┘
```

---

## Emergency Access Workflow

```
┌─────────────────┐
│ Professional    │ Emergency situation: patient unconscious
│ (ER Doctor)     │ Needs access to medical history immediately
└────────┬────────┘
         │
         │ GET /api/rndc/documents/789?emergency=true
         │ Authorization: Bearer <JWT>
         │ X-Emergency-Justification: "Patient unconscious, needs allergy info"
         ▼
┌──────────────────────────────────────────────────────────────┐
│                   RndcService                                 │
├──────────────────────────────────────────────────────────────┤
│  Extract emergency context from request                      │
│  Build AccessRequest with emergency flag                     │
└────────┬─────────────────────────────────────────────────────┘
         │
         │ evaluate(accessRequest)
         ▼
┌──────────────────────────────────────────────────────────────┐
│                 PolicyEngine                                  │
├──────────────────────────────────────────────────────────────┤
│  Load patient policies (including EMERGENCY_OVERRIDE if set) │
│                                                               │
│  IF patient has EMERGENCY_OVERRIDE policy:                   │
│    → EmergencyOverridePolicyEvaluator.evaluate()             │
│       Returns: PERMIT                                        │
│                                                               │
│  ELSE:                                                        │
│    → Evaluate normal policies                                │
│    → Result: DENY (no permission for ER doctor)              │
│    → BUT emergency flag is set                               │
│    → Override decision to PERMIT (emergency access)          │
└────────┬─────────────────────────────────────────────────────┘
         │
         │ PolicyEvaluationResult { decision: PERMIT, emergency: true }
         ▼
┌──────────────────────────────────────────────────────────────┐
│              EmergencyAccessService [NEW]                     │
├──────────────────────────────────────────────────────────────┤
│  logEmergencyAccess(professionalId, patientCi, docId, just.) │
│                                                               │
│  1. Create emergency_access_reviews entry                    │
│     INSERT INTO policies.emergency_access_reviews            │
│     VALUES (prof-123, 12345678, 789, 'unconscious...', ...)  │
│                                                               │
│  2. Log to audit with SEVERE level                           │
│     auditService.logEvent(ACCESS, prof-123, DOCUMENT, 789,   │
│                           SUCCESS, details: {                │
│                             emergencyAccess: true,           │
│                             justification: "...",            │
│                             requiresReview: true             │
│                           })                                 │
│                                                               │
│  3. Send IMMEDIATE notification to patient                   │
│     notificationService.sendEmergencyAccessNotification()    │
└────────┬─────────────────────────────────────────────────────┘
         │
         │ (in parallel)
         ├───────────────────────────────────────────────────────┐
         │                                                       │
         ▼                                                       ▼
┌─────────────────────┐                          ┌─────────────────────┐
│ Firebase FCM        │                          │ Email Notification  │
│ (Mobile Push)       │                          │ (Backup)            │
├─────────────────────┤                          ├─────────────────────┤
│ Send to patient's   │                          │ Send to patient's   │
│ registered device   │                          │ email address       │
│                     │                          │                     │
│ Notification:       │                          │ Subject:            │
│ "EMERGENCY ACCESS   │                          │ "Emergency Access   │
│  to your records"   │                          │  Alert"             │
│                     │                          │                     │
│ Professional:       │                          │ Details:            │
│  Dr. Smith (ER)     │                          │  Professional,      │
│ Document: Med Hist  │                          │  Document, Time     │
│ Reason: Patient     │                          │                     │
│   unconscious       │                          │ "Please review via  │
│                     │                          │  mobile app"        │
│ [Review Access]     │                          │                     │
│ [Contact HCEN]      │                          │                     │
└──────────┬──────────┘                          └──────────┬──────────┘
           │                                                │
           │ Patient opens notification                    │
           ▼                                                ▼
┌──────────────────────────────────────────────────────────────┐
│              Mobile App (Patient Portal)                      │
├──────────────────────────────────────────────────────────────┤
│  Screen: Emergency Access Review                             │
│                                                               │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Emergency Access Alert                                 │  │
│  │                                                        │  │
│  │ Professional: Dr. John Smith (ER - Hospital Central)  │  │
│  │ Accessed: Medical History (Document #789)             │  │
│  │ Time: 2025-10-30 15:23                                │  │
│  │                                                        │  │
│  │ Justification:                                         │  │
│  │ "Patient unconscious in ER, needs allergy information"│  │
│  │                                                        │  │
│  │ This was an EMERGENCY ACCESS that bypassed your       │  │
│  │ normal privacy settings. Please review and confirm:   │  │
│  │                                                        │  │
│  │  [✓ Confirm - This was a legitimate emergency]        │  │
│  │                                                        │  │
│  │  [✗ Dispute - Report unauthorized access]             │  │
│  │                                                        │  │
│  │  [View Full Audit Log]                                │  │
│  └────────────────────────────────────────────────────────┘  │
└────────────┬─────────────────────────────────────────────────┘
             │
             │ Patient clicks "Confirm" or "Dispute"
             ▼
┌──────────────────────────────────────────────────────────────┐
│        POST /api/policies/emergency-reviews/{id}/confirm     │
│        POST /api/policies/emergency-reviews/{id}/dispute     │
└────────────┬─────────────────────────────────────────────────┘
             │
             ▼
┌──────────────────────────────────────────────────────────────┐
│            EmergencyAccessService                             │
├──────────────────────────────────────────────────────────────┤
│  IF confirmed:                                               │
│    UPDATE emergency_access_reviews                           │
│    SET status = 'CONFIRMED', reviewed_at = NOW()             │
│    WHERE id = ...                                            │
│                                                               │
│    Log confirmation to audit                                 │
│                                                               │
│  IF disputed:                                                │
│    UPDATE emergency_access_reviews                           │
│    SET status = 'DISPUTED', patient_comment = '...'          │
│    WHERE id = ...                                            │
│                                                               │
│    Send alert to HCEN administrators for investigation       │
│    Potentially lock professional's access pending review     │
└──────────────────────────────────────────────────────────────┘
```

---

## Audit Log Hash Chain

Visual representation of tamper-evident hash chain:

```
Genesis Entry (First Audit Log)
┌─────────────────────────────────────────────────────────────┐
│ ID: 1                                                        │
│ EventType: CREATION                                          │
│ ActorId: SYSTEM                                              │
│ ResourceType: SYSTEM                                         │
│ ResourceId: INIT                                             │
│ Timestamp: 2025-10-15 10:00:00                               │
│                                                              │
│ previousHash: NULL (genesis entry)                          │
│ currentHash: SHA256(1|CREATION|SYSTEM|...|NULL)             │
│            = "a1b2c3d4e5f6..."                               │
└─────────────────────────────────────────────────────────────┘
                        │
                        │ Links to next entry
                        ▼
Entry 2
┌─────────────────────────────────────────────────────────────┐
│ ID: 2                                                        │
│ EventType: ACCESS                                            │
│ ActorId: prof-123                                            │
│ ResourceType: DOCUMENT                                       │
│ ResourceId: 456                                              │
│ Timestamp: 2025-10-15 11:30:00                               │
│                                                              │
│ previousHash: "a1b2c3d4e5f6..." (Entry 1's currentHash)     │
│ currentHash: SHA256(2|ACCESS|prof-123|...|a1b2c3d4e5f6...)  │
│            = "f7e8d9c0b1a2..."                               │
└─────────────────────────────────────────────────────────────┘
                        │
                        │ Links to next entry
                        ▼
Entry 3
┌─────────────────────────────────────────────────────────────┐
│ ID: 3                                                        │
│ EventType: POLICY_CHANGE                                     │
│ ActorId: 12345678                                            │
│ ResourceType: POLICY                                         │
│ ResourceId: 123                                              │
│ Timestamp: 2025-10-15 14:00:00                               │
│                                                              │
│ previousHash: "f7e8d9c0b1a2..." (Entry 2's currentHash)     │
│ currentHash: SHA256(3|POLICY_CHANGE|12345678|...|f7e8d9...)  │
│            = "3c4d5e6f7a8b..."                               │
└─────────────────────────────────────────────────────────────┘
                        │
                        ▼
                       ...

Tamper Detection:
═════════════════════════════════════════════════════════════

Scenario 1: Attacker modifies Entry 2's actorId
┌─────────────────────────────────────────────────────────────┐
│ ID: 2                                                        │
│ ActorId: prof-999  ◄────────────────────── MODIFIED         │
│ previousHash: "a1b2c3d4e5f6..."                             │
│ currentHash: "f7e8d9c0b1a2..."  ◄────────── Still old hash  │
└─────────────────────────────────────────────────────────────┘
                        │
                        │
                        ▼
Validation Fails:
─────────────────────────────────────────────────────────────
Expected currentHash: SHA256(2|ACCESS|prof-999|...|a1b2c3...)
                    = "9z8y7x6w5v4u..."  ◄──── Different!

Actual currentHash:  "f7e8d9c0b1a2..."

Violation Detected: "Current hash mismatch - entry was tampered"
─────────────────────────────────────────────────────────────


Scenario 2: Attacker modifies Entry 2 AND updates its hash
┌─────────────────────────────────────────────────────────────┐
│ ID: 2                                                        │
│ ActorId: prof-999  ◄────────────────────── MODIFIED         │
│ previousHash: "a1b2c3d4e5f6..."                             │
│ currentHash: "9z8y7x6w5v4u..."  ◄────────── Hash recalculated│
└─────────────────────────────────────────────────────────────┘
                        │
                        │ Links to next entry
                        ▼
Entry 3
┌─────────────────────────────────────────────────────────────┐
│ ID: 3                                                        │
│ previousHash: "f7e8d9c0b1a2..."  ◄──── Still old hash       │
│                                   (doesn't match Entry 2)    │
└─────────────────────────────────────────────────────────────┘

Validation Fails:
─────────────────────────────────────────────────────────────
Expected previousHash (from Entry 2): "9z8y7x6w5v4u..."
Actual previousHash:                   "f7e8d9c0b1a2..."

Violation Detected: "Previous hash mismatch - chain broken"
─────────────────────────────────────────────────────────────

Conclusion: Attacker would need to recalculate ALL subsequent
            hashes to maintain chain integrity → impractical
```

---

## MongoDB Archival Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                  PostgreSQL (audit.audit_logs)                   │
│                   Active Audit Logs (0-90 days)                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Day 1-89:  Standard operations                                 │
│             • High read/write throughput                         │
│             • Indexed queries                                    │
│             • Patient access history                             │
│             • Compliance queries                                 │
│                                                                  │
│  Day 90:    Archival threshold reached                          │
└────────┬────────────────────────────────────────────────────────┘
         │
         │ @Schedule(hour="2", minute="0") - Daily at 2 AM
         ▼
┌─────────────────────────────────────────────────────────────────┐
│            AuditRetentionService.archiveOldLogs()                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. Query old logs:                                             │
│     SELECT * FROM audit.audit_logs                              │
│     WHERE timestamp < (NOW() - INTERVAL '90 days')              │
│     LIMIT 10000  -- Process in batches                          │
│                                                                  │
│  2. For each log:                                               │
│     ┌───────────────────────────────────────────────────────┐   │
│     │ a. Convert AuditLog entity → MongoDB Document         │   │
│     │    {                                                  │   │
│     │      "_id": 12345,                                    │   │
│     │      "eventType": "ACCESS",                           │   │
│     │      "actorId": "prof-123",                           │   │
│     │      "resourceType": "DOCUMENT",                      │   │
│     │      "resourceId": "456",                             │   │
│     │      "actionOutcome": "SUCCESS",                      │   │
│     │      "timestamp": ISODate("2025-07-30T10:00:00Z"),   │   │
│     │      "ipAddress": "192.168.1.100",                    │   │
│     │      "userAgent": "Mozilla/5.0...",                   │   │
│     │      "details": "{\"documentType\":\"LAB_RESULT\"}", │   │
│     │      "previousHash": "a1b2c3...",                     │   │
│     │      "currentHash": "d4e5f6...",                      │   │
│     │      "archivedAt": ISODate("2025-10-30T02:00:00Z")   │   │
│     │    }                                                  │   │
│     └───────────────────────────────────────────────────────┘   │
│                                                                  │
│  3. Insert into MongoDB:                                        │
│     db.audit_logs.insertOne(document)                           │
│                                                                  │
│  4. Verify successful insert                                    │
│                                                                  │
│  5. Check if critical event:                                    │
│     IF eventType IN (POLICY_CHANGE, ACCESS_DENIAL, ...)         │
│        OR details LIKE '%emergency%'                            │
│     THEN                                                        │
│        KEEP in PostgreSQL (don't delete)                        │
│     ELSE                                                        │
│        DELETE FROM audit.audit_logs WHERE id = 12345            │
│                                                                  │
│  6. Log archival statistics:                                    │
│     "Archived 8,432 logs, kept 156 critical events"             │
└────────┬────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│              MongoDB (hcen_audit_archive.audit_logs)             │
│                  Archived Audit Logs (90+ days)                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Storage:                                                        │
│  • Documents older than 90 days                                 │
│  • All original fields preserved                                │
│  • Hash chain intact (previousHash, currentHash)                │
│  • archivedAt timestamp for tracking                            │
│                                                                  │
│  Indexes:                                                        │
│  • {timestamp: -1}                                              │
│  • {actorId: 1, timestamp: -1}                                  │
│  • {resourceType: 1, resourceId: 1, timestamp: -1}              │
│                                                                  │
│  TTL Index (7-year automatic deletion):                         │
│  • {archivedAt: 1}, expireAfterSeconds: 220752000               │
│    (7 years = 2555 days * 86400 seconds/day)                    │
│                                                                  │
│  Query Examples:                                                 │
│                                                                  │
│  // Patient access history from archive                         │
│  db.audit_logs.find({                                           │
│    "resourceType": "DOCUMENT",                                  │
│    "eventType": "ACCESS",                                       │
│    "details": { $regex: "12345678" },  // patientCi             │
│    "timestamp": {                                               │
│      $gte: ISODate("2025-01-01"),                               │
│      $lte: ISODate("2025-12-31")                                │
│    }                                                             │
│  }).sort({ timestamp: -1 })                                     │
│                                                                  │
│  // Aggregate: Most active professionals (archived data)        │
│  db.audit_logs.aggregate([                                      │
│    { $match: { eventType: "ACCESS" } },                         │
│    { $group: {                                                  │
│        _id: "$actorId",                                         │
│        count: { $sum: 1 }                                       │
│      }                                                           │
│    },                                                            │
│    { $sort: { count: -1 } },                                    │
│    { $limit: 10 }                                               │
│  ])                                                              │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
         │
         │ After 7 years (automatic TTL deletion)
         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Compliance Archive                            │
│            (Optional: Offload to cold storage)                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Before TTL deletion, optionally export to:                     │
│  • AWS S3 Glacier (long-term cold storage)                      │
│  • Azure Archive Storage                                        │
│  • Tape backups                                                 │
│                                                                  │
│  For legal/regulatory compliance requiring 7+ years retention   │
└─────────────────────────────────────────────────────────────────┘

Timeline Visualization:
═══════════════════════════════════════════════════════════════════

Day 0           Day 90          Day 180         ... Year 7
│               │               │                   │
│  PostgreSQL   │  PostgreSQL   │  PostgreSQL       │
│  (0-90 days)  │  (0-90 days)  │  (0-90 days)      │
│               │               │                   │
│               ├───────────────┼───────────────────┼───────────────────┐
│               │  MongoDB      │  MongoDB          │  MongoDB          │
│               │  (90-180)     │  (90-180, ...)    │  (90 days-7 yrs)  │
│               │               │                   │                   │
│               │               │                   ├───────────────────┤
│               │               │                   │  TTL Deletion     │
│               │               │                   │  (7+ years)       │
│               │               │                   └───────────────────┘

Benefits:
─────────────────────────────────────────────────────────────────
✓ PostgreSQL remains fast (small dataset, hot data)
✓ MongoDB handles large historical queries efficiently
✓ Automatic cleanup (TTL) reduces operational overhead
✓ Hash chain preserved across both databases
✓ Patient can still query old access history (unified API)
✓ Compliance: 7-year retention as required by Uruguayan law
```

---

## Database Schema Relationships

```
┌─────────────────────────────────────────────────────────────────┐
│                       PostgreSQL Schemas                         │
└─────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────────┐
│ Schema: inus                                                                │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ Table: inus_users                                                   │   │
│  ├─────────────────────────────────────────────────────────────────────┤   │
│  │ PK: ci (VARCHAR)                                                    │   │
│  │ • inus_id (unique cross-clinic ID)                                  │   │
│  │ • first_name, last_name                                             │   │
│  │ • date_of_birth                                                     │   │
│  │ • email, phone                                                      │   │
│  │ • status (ACTIVE, INACTIVE, SUSPENDED)                              │   │
│  │ • created_at, updated_at                                            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└────────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────────┐
│ Schema: rndc                                                                │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ Table: rndc_documents                                               │   │
│  ├─────────────────────────────────────────────────────────────────────┤   │
│  │ PK: id (BIGSERIAL)                                                  │   │
│  │ FK: patient_ci → inus.inus_users.ci                                 │   │
│  │ • document_locator (URL to peripheral storage)                      │   │
│  │ • document_hash (SHA-256 for integrity)                             │   │
│  │ • document_type (enum: LAB_RESULT, IMAGING, CLINICAL_NOTE, ...)    │   │
│  │ • created_by (professional ID)                                      │   │
│  │ • clinic_id                                                         │   │
│  │ • status (ACTIVE, INACTIVE, DELETED)                                │   │
│  │ • created_at, updated_at                                            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└────────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────────┐
│ Schema: policies                                                            │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ Table: access_policies                                              │   │
│  ├─────────────────────────────────────────────────────────────────────┤   │
│  │ PK: id (BIGSERIAL)                                                  │   │
│  │ FK: patient_ci → inus.inus_users.ci                                 │   │
│  │ • policy_type (enum: DOCUMENT_TYPE, SPECIALTY, TIME_BASED, ...)    │   │
│  │ • policy_config (JSONB - flexible per type)                         │   │
│  │ • policy_effect (PERMIT or DENY)                                    │   │
│  │ • valid_from, valid_until (optional time constraints)               │   │
│  │ • priority (for conflict resolution)                                │   │
│  │ • created_at, updated_at                                            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                               │                                             │
│                               │ 1:N                                         │
│                               ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ Table: policy_versions [NEW]                                        │   │
│  ├─────────────────────────────────────────────────────────────────────┤   │
│  │ PK: id (BIGSERIAL)                                                  │   │
│  │ FK: policy_id → access_policies.id                                  │   │
│  │ • version_number (incremental)                                      │   │
│  │ • policy_type, policy_config, policy_effect (snapshot)              │   │
│  │ • changed_by (who made the change)                                  │   │
│  │ • changed_at, change_reason                                         │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ Table: access_requests                                              │   │
│  ├─────────────────────────────────────────────────────────────────────┤   │
│  │ PK: id (BIGSERIAL)                                                  │   │
│  │ FK: patient_ci → inus.inus_users.ci                                 │   │
│  │ FK: document_id → rndc.rndc_documents.id (optional)                 │   │
│  │ • professional_id                                                   │   │
│  │ • request_reason                                                    │   │
│  │ • status (PENDING, APPROVED, DENIED, EXPIRED)                       │   │
│  │ • requested_at, responded_at, expires_at                            │   │
│  │ • patient_response                                                  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ Table: emergency_access_reviews [NEW]                               │   │
│  ├─────────────────────────────────────────────────────────────────────┤   │
│  │ PK: id (BIGSERIAL)                                                  │   │
│  │ FK: patient_ci → inus.inus_users.ci                                 │   │
│  │ FK: document_id → rndc.rndc_documents.id (optional)                 │   │
│  │ • professional_id                                                   │   │
│  │ • justification                                                     │   │
│  │ • status (PENDING, CONFIRMED, DISPUTED)                             │   │
│  │ • accessed_at, reviewed_at                                          │   │
│  │ • patient_comment                                                   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└────────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────────┐
│ Schema: audit                                                               │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ Table: audit_logs (APPEND-ONLY, 90-day retention)                   │   │
│  ├─────────────────────────────────────────────────────────────────────┤   │
│  │ PK: id (BIGSERIAL)                                                  │   │
│  │ • event_type (enum: ACCESS, MODIFICATION, CREATION, ...)            │   │
│  │ • actor_id (professional ID, patient CI, or "SYSTEM")               │   │
│  │ • actor_type (PROFESSIONAL, PATIENT, ADMIN, SYSTEM)                 │   │
│  │ • resource_type (DOCUMENT, USER, POLICY, CLINIC, ...)               │   │
│  │ • resource_id (document ID, user CI, policy ID, ...)                │   │
│  │ • action_outcome (SUCCESS, FAILURE, DENIED)                         │   │
│  │ • timestamp                                                          │   │
│  │ • ip_address, user_agent                                            │   │
│  │ • details (JSONB - flexible context)                                │   │
│  │ • previous_hash (SHA-256 of previous log) [NEW]                     │   │
│  │ • current_hash (SHA-256 of this log) [NEW]                          │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│            │                                                                │
│            │ Hash chain links (immutable blockchain-like structure)        │
│            │ previous_hash[N] = current_hash[N-1]                          │
│            │                                                                │
│            └─> Tamper Detection: Recalculate hashes and compare            │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ Table: integrity_violations [NEW]                                   │   │
│  ├─────────────────────────────────────────────────────────────────────┤   │
│  │ PK: id (BIGSERIAL)                                                  │   │
│  │ FK: audit_log_id → audit_logs.id                                    │   │
│  │ • violation_type (PREVIOUS_HASH_MISMATCH, CURRENT_HASH_MISMATCH)    │   │
│  │ • violation_details                                                 │   │
│  │ • detected_at                                                       │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└────────────────────────────────────────────────────────────────────────────┘

Relationships:
══════════════════════════════════════════════════════════════════════════════

inus.inus_users (1) ────────────> (N) rndc.rndc_documents
   (patient_ci)                        (patient_ci FK)

inus.inus_users (1) ────────────> (N) policies.access_policies
   (patient_ci)                        (patient_ci FK)

rndc.rndc_documents (1) ───────> (0..N) policies.access_requests
   (id)                                  (document_id FK, optional)

rndc.rndc_documents (1) ───────> (0..N) policies.emergency_access_reviews
   (id)                                  (document_id FK, optional)

policies.access_policies (1) ──> (N) policies.policy_versions
   (id)                               (policy_id FK)

audit.audit_logs (1) ──────────> (0..N) audit.integrity_violations
   (id)                                 (audit_log_id FK)

audit.audit_logs[N] ────────────> audit.audit_logs[N-1]
   (previous_hash)                 (current_hash)
   Hash chain linking
```

---

**End of Architecture Diagrams**

These diagrams provide complete visual documentation of the Policy Engine and Audit System architecture, data flows, and database relationships. Use them for implementation reference and team communication.
