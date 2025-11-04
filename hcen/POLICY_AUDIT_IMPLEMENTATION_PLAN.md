# Policy Engine & Audit System - Comprehensive Implementation Plan

**Project**: HCEN (Historia Clínica Electrónica Nacional)
**Status**: Policy Engine 60% Complete (~600 LOC) | Audit System 60% Complete (~300 LOC)
**Target**: 100% Complete with Production-Ready Implementation
**Author**: TSE 2025 Group 9
**Date**: 2025-10-30

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Current State Analysis](#current-state-analysis)
3. [Architecture Design](#architecture-design)
4. [Policy Engine Completion](#policy-engine-completion)
5. [Audit System Completion](#audit-system-completion)
6. [Database Schema](#database-schema)
7. [Implementation Sequence](#implementation-sequence)
8. [Testing Strategy](#testing-strategy)
9. [Integration Points](#integration-points)
10. [Infrastructure Requirements](#infrastructure-requirements)

---

## Executive Summary

This plan provides a step-by-step roadmap to complete the **Policy Engine** and **Audit System** features in HCEN. Both systems are currently ~60% implemented with strong foundations already in place.

### What Exists:
- **Policy Engine**: Core evaluation logic, 6 policy evaluators, caching, conflict resolution
- **Audit System**: Basic audit logging, entity model, repository pattern
- **Database**: PostgreSQL schemas for policies and audit logs
- **Integration**: PolicyCacheService (Redis), partial AuditService

### What's Missing:
- **Policy Engine**: RBAC integration, emergency override workflow, policy conflict analytics, policy versioning
- **Audit System**: Immutability guarantees, retention enforcement, tamper detection, comprehensive event interceptors, audit export API
- **Integration**: Full RNDC integration with policy enforcement, mobile notification for PENDING decisions

### Estimated Completion Time:
- **Policy Engine**: 12-16 hours (400-500 additional LOC)
- **Audit System**: 10-14 hours (500-600 additional LOC)
- **Integration & Testing**: 8-10 hours
- **Total**: 30-40 hours (~1 week of focused development)

---

## Current State Analysis

### Policy Engine (60% Complete - ~600 LOC)

#### Existing Components:

**Entity Models**:
- `AccessPolicy` (350 LOC) - Complete JPA entity with PolicyType, PolicyEffect enums
- `AccessRequest` - Entity for pending approval requests

**Evaluators** (Strategy Pattern):
- `PolicyEvaluator` (92 LOC) - Interface
- `DocumentTypePolicyEvaluator` (123 LOC) - COMPLETE
- `SpecialtyPolicyEvaluator` (130 LOC) - COMPLETE
- `TimeBasedPolicyEvaluator` (203 LOC) - COMPLETE
- `ClinicPolicyEvaluator` (127 LOC) - COMPLETE
- `ProfessionalPolicyEvaluator` (138 LOC) - COMPLETE
- `EmergencyOverridePolicyEvaluator` (158 LOC) - COMPLETE

**Core Service**:
- `PolicyEngine` (442 LOC) - Main evaluation orchestrator
  - Cache integration (Redis via PolicyCacheService)
  - Conflict resolution algorithm (DENY > PERMIT > PENDING)
  - Policy evaluation flow
  - Logging integration

**DTOs**:
- `PolicyDecision` (166 LOC) - PERMIT, DENY, PENDING
- `PolicyEvaluationResult` (302 LOC) - Complete result object
- `AccessRequest` DTO (service layer)

**Repository**:
- `AccessPolicyRepository` + Impl (351 LOC combined)
- `AccessRequestRepository` + Impl

**REST API**:
- `PolicyResource` (530 LOC) - JAX-RS endpoints for policy CRUD

**Caching**:
- `PolicyCacheService` (320 LOC in policy package, 232 LOC in cache package)

#### Missing Components:

1. **RBAC Integration** (~100 LOC)
   - Role-based policy evaluation for clinic-internal permissions
   - Integration with clinic user management
   - Role hierarchy (ADMIN > DOCTOR > NURSE > RECEPTIONIST)

2. **Emergency Override Workflow** (~80 LOC)
   - Patient notification on emergency access
   - Post-emergency review mechanism
   - Emergency access audit trail enhancement

3. **Policy Conflict Analytics** (~60 LOC)
   - Detect conflicting policies (e.g., PERMIT + DENY for same criteria)
   - Policy effectiveness reports
   - Recommendations for policy optimization

4. **Policy Versioning** (~120 LOC)
   - Track policy changes over time
   - Audit who changed what when
   - Restore previous policy versions

5. **Batch Policy Operations** (~40 LOC)
   - Bulk policy creation (e.g., "deny all except my primary clinic")
   - Policy templates
   - Import/export policies

---

### Audit System (60% Complete - ~300 LOC)

#### Existing Components:

**Entity Model**:
- `AuditLog` (302 LOC) - Complete JPA entity
  - EventType enum (ACCESS, MODIFICATION, CREATION, DELETION, etc.)
  - ActionOutcome enum (SUCCESS, FAILURE, DENIED)
  - Comprehensive fields (actor, resource, timestamp, IP, user agent)
  - JSONB details field for flexible context

**Repository**:
- `AuditLogRepository` (136 LOC) - Interface with query methods
- `AuditLogRepositoryImpl` (252 LOC) - Implementation with custom queries

**Service**:
- `AuditService` (722 LOC) - Core audit logging service
  - logEvent() - Generic event logging
  - logDocumentAccess() - Specialized method
  - logAuthenticationEvent() - Specialized method
  - Query methods (getPatientAccessHistory, etc.)
  - Fail-safe design (never throws exceptions)

**DTOs**:
- `AuditEventRequest` (296 LOC)
- `AuditEventBuilder` (411 LOC) - Builder pattern for audit events
- `AuditLogResponse` (270 LOC)
- `AuditLogListResponse` (196 LOC)
- `AuditStatisticsResponse` (172 LOC)

**REST API**:
- `AuditResource` (564 LOC) - JAX-RS endpoints for audit queries
  - GET /audit/logs - Query audit logs
  - GET /audit/patients/{ci}/access-history - Patient access history
  - GET /audit/statistics - Audit statistics

**Annotations**:
- `@Audited` (110 LOC) - Annotation for automatic audit logging (not yet fully implemented)

#### Missing Components:

1. **Immutability Guarantees** (~150 LOC)
   - Hash chain linking consecutive audit logs
   - Digital signature for tamper detection
   - Validation endpoint to verify audit log integrity
   - Prevent UPDATE/DELETE operations at database level

2. **Retention Policy Enforcement** (~100 LOC)
   - Automatic archival to MongoDB after 90 days
   - Scheduled cleanup job (EJB @Schedule)
   - Retention configuration per event type
   - Archive restoration endpoint

3. **Comprehensive Event Interceptors** (~200 LOC)
   - CDI interceptor for @Audited annotation
   - Automatic audit of all RNDC operations
   - Automatic audit of all INUS operations
   - Automatic audit of policy changes

4. **Audit Export API** (~80 LOC)
   - Export to CSV format
   - Export to JSON format
   - Date range filtering
   - Streaming for large exports

5. **Advanced Query Capabilities** (~100 LOC)
   - Full-text search in details field
   - Aggregation queries (most accessed documents, top professionals, etc.)
   - Anomaly detection (unusual access patterns)
   - Compliance reports

6. **Patient Audit Dashboard Integration** (~70 LOC)
   - Enhanced REST endpoints for mobile/web
   - Real-time notifications on access events
   - Access analytics (who accesses my data most often?)

---

## Architecture Design

### Policy Engine Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    JAX-RS Layer                              │
│  PolicyResource - REST API for policy management             │
└───────────────────┬─────────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────────┐
│                 Service Layer                                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │           PolicyEngine (Orchestrator)                 │   │
│  │  • evaluate(AccessRequest): PolicyEvaluationResult   │   │
│  │  • resolveConflicts(List<PolicyDecision>)            │   │
│  │  • checkCache() / cacheDecision()                    │   │
│  └──┬──────────────────────────────────────────┬────────┘   │
│     │                                          │            │
│  ┌──▼───────────────────────┐     ┌──────────▼──────────┐  │
│  │  PolicyCacheService      │     │  AuditService       │  │
│  │  (Redis integration)     │     │  (Log evaluations)  │  │
│  └──────────────────────────┘     └─────────────────────┘  │
└───────────────────┬─────────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────────┐
│           Policy Evaluator Layer (Strategy Pattern)          │
│  ┌────────────────────┐  ┌────────────────────┐             │
│  │ DocumentTypePolicy │  │  SpecialtyPolicy   │             │
│  │    Evaluator       │  │    Evaluator       │             │
│  └────────────────────┘  └────────────────────┘             │
│  ┌────────────────────┐  ┌────────────────────┐             │
│  │  TimeBasedPolicy   │  │   ClinicPolicy     │             │
│  │    Evaluator       │  │    Evaluator       │             │
│  └────────────────────┘  └────────────────────┘             │
│  ┌────────────────────┐  ┌────────────────────┐             │
│  │ ProfessionalPolicy │  │ EmergencyOverride  │             │
│  │    Evaluator       │  │    Evaluator       │             │
│  └────────────────────┘  └────────────────────┘             │
└───────────────────┬─────────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────────┐
│              Repository Layer (Data Access)                  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  AccessPolicyRepository                              │   │
│  │  • findActivePoliciesByPatientCi()                   │   │
│  │  • findByPolicyType()                                │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  AccessRequestRepository                             │   │
│  │  • findPendingRequestsByPatient()                    │   │
│  │  • createRequest() / approveRequest()                │   │
│  └──────────────────────────────────────────────────────┘   │
└───────────────────┬─────────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────────┐
│            Persistence Layer (PostgreSQL)                    │
│  • policies.access_policies                                  │
│  • policies.access_requests                                  │
└─────────────────────────────────────────────────────────────┘
```

### Audit System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    JAX-RS Layer                              │
│  AuditResource - REST API for audit queries & export         │
└───────────────────┬─────────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────────┐
│                 Service Layer                                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              AuditService (Core)                      │   │
│  │  • logEvent() - Generic event logging                │   │
│  │  • logDocumentAccess() - Specialized                 │   │
│  │  • logPolicyEvaluation() - Policy decisions          │   │
│  │  • getPatientAccessHistory() - CU05                  │   │
│  │  • validateAuditIntegrity() - Hash chain check       │   │
│  └──┬───────────────────────────────────────────────────┘   │
│     │                                                        │
│  ┌──▼───────────────────────┐     ┌──────────────────────┐ │
│  │  AuditRetentionService   │     │  AuditExportService  │ │
│  │  (Archive to MongoDB)    │     │  (CSV/JSON export)   │ │
│  └──────────────────────────┘     └──────────────────────┘ │
└───────────────────┬─────────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────────┐
│          Interceptor Layer (Automatic Audit)                 │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  @Audited CDI Interceptor                            │   │
│  │  • Intercepts annotated methods                      │   │
│  │  • Extracts audit context from method params         │   │
│  │  • Calls AuditService.logEvent() automatically       │   │
│  └──────────────────────────────────────────────────────┘   │
└───────────────────┬─────────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────────┐
│              Repository Layer (Data Access)                  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  AuditLogRepository                                  │   │
│  │  • save() - Append-only (no update/delete)           │   │
│  │  • findByPatientCi() / findByActor()                 │   │
│  │  • findByEventType() / findByDateRange()             │   │
│  │  • aggregate() - Statistics queries                  │   │
│  └──────────────────────────────────────────────────────┘   │
└───────────────────┬─────────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────────┐
│        Persistence Layer (PostgreSQL + MongoDB)              │
│  PostgreSQL (90 days):                                       │
│  • audit.audit_logs (append-only, hash chain)                │
│                                                              │
│  MongoDB (archive):                                          │
│  • audit_logs_archive (documents older than 90 days)         │
└─────────────────────────────────────────────────────────────┘
```

### Integration Flow: RNDC Document Access with Policy Enforcement

```
┌──────────────────────────────────────────────────────────────┐
│  1. Professional Requests Document                           │
│     GET /api/rndc/documents/456 (via peripheral node)        │
└───────────────────┬──────────────────────────────────────────┘
                    │
┌───────────────────▼──────────────────────────────────────────┐
│  2. RndcService.getDocument()                                │
│     • Extract professional context (ID, specialty, clinic)   │
│     • Extract patient CI from document metadata              │
│     • Extract document type                                  │
└───────────────────┬──────────────────────────────────────────┘
                    │
┌───────────────────▼──────────────────────────────────────────┐
│  3. Call PolicyEngine.evaluate()                             │
│     AccessRequest {                                          │
│       professionalId, specialties, clinicId,                 │
│       patientCi, documentType, documentId                    │
│     }                                                         │
└───────────────────┬──────────────────────────────────────────┘
                    │
        ┌───────────▼───────────┐
        │  PolicyEngine Logic   │
        │  • Check cache        │
        │  • Load policies      │
        │  • Evaluate all       │
        │  • Resolve conflicts  │
        │  • Cache result       │
        └───────────┬───────────┘
                    │
        ┌───────────▼───────────────────────────────────────────┐
        │  PolicyEvaluationResult                               │
        │  • decision: PERMIT / DENY / PENDING                  │
        │  • reason: "Access denied by policy 123"              │
        │  • decidingPolicy: 123                                │
        └───────────┬───────────────────────────────────────────┘
                    │
    ┌───────────────┴────────────────┐
    │                                │
┌───▼────────────┐   ┌──────────────▼─────────────────────┐
│  4a. PERMIT    │   │  4b. DENY                          │
│  • Log access  │   │  • Log denied access               │
│  • Return doc  │   │  • Return 403 Forbidden            │
└────────────────┘   └────────────────────────────────────┘
                     ┌──────────────▼─────────────────────┐
                     │  4c. PENDING                       │
                     │  • Create AccessRequest entity     │
                     │  • Send mobile notification        │
                     │  • Return 202 Accepted (pending)   │
                     └────────────────────────────────────┘
```

---

## Policy Engine Completion

### Task 1: RBAC Integration (Priority: Medium)

**Objective**: Enable clinic-internal role-based access control policies.

**Files to Create**:
1. `src/main/java/uy/gub/hcen/service/policy/evaluator/RolePolicyEvaluator.java` (~100 LOC)

**Implementation Details**:

```java
package uy.gub.hcen.service.policy.evaluator;

import jakarta.enterprise.context.ApplicationScoped;
import uy.gub.hcen.policy.entity.AccessPolicy;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyEffect;
import uy.gub.hcen.policy.entity.AccessPolicy.PolicyType;
import uy.gub.hcen.service.policy.dto.AccessRequest;
import uy.gub.hcen.service.policy.dto.PolicyDecision;

/**
 * Role-Based Policy Evaluator
 *
 * Evaluates policies based on professional role within their clinic.
 * Supports role hierarchy: ADMIN > DOCTOR > NURSE > RECEPTIONIST
 *
 * Policy Configuration Format (JSON):
 * {
 *   "allowedRoles": ["DOCTOR", "ADMIN"],
 *   "minimumRole": "NURSE"  // Alternative: allow this role and above
 * }
 *
 * Use Cases:
 * - Allow only doctors to view lab results
 * - Allow nurses and above to view vital signs
 * - Deny receptionists access to psychiatric notes
 */
@ApplicationScoped
public class RolePolicyEvaluator implements PolicyEvaluator {

    private static final Map<String, Integer> ROLE_HIERARCHY = Map.of(
        "RECEPTIONIST", 1,
        "NURSE", 2,
        "DOCTOR", 3,
        "ADMIN", 4
    );

    @Override
    public PolicyDecision evaluate(AccessPolicy policy, AccessRequest request) {
        // Implementation:
        // 1. Extract professional role from request (add to AccessRequest DTO)
        // 2. Parse allowedRoles or minimumRole from policy config
        // 3. Check if professional's role is allowed
        // 4. Return PERMIT/DENY based on policy effect
        // TODO: Implement
        return null;
    }

    @Override
    public boolean supports(PolicyType policyType) {
        return policyType == PolicyType.ROLE; // Add to PolicyType enum
    }
}
```

**Database Changes**:
- Add `ROLE` to `PolicyType` enum (migration script)
- No schema changes needed (JSONB config handles it)

**Integration Points**:
- Extend `AccessRequest` DTO to include `professionalRole` field
- Update `RndcService` to extract role from JWT claims
- Register evaluator in PolicyEngine's CDI injection

**Estimated Time**: 3 hours

---

### Task 2: Emergency Override Workflow Enhancement (Priority: High)

**Objective**: Improve emergency access with patient notifications and post-access review.

**Files to Modify**:
1. `src/main/java/uy/gub/hcen/service/policy/evaluator/EmergencyOverridePolicyEvaluator.java` (add notification)
2. `src/main/java/uy/gub/hcen/service/policy/PolicyEngine.java` (enhance logging)

**Files to Create**:
1. `src/main/java/uy/gub/hcen/policy/service/EmergencyAccessService.java` (~150 LOC)

**Implementation Details**:

```java
package uy.gub.hcen.policy.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import uy.gub.hcen.notification.service.NotificationService;
import uy.gub.hcen.service.audit.AuditService;

/**
 * Emergency Access Service
 *
 * Handles workflow for emergency override access to patient records.
 *
 * Features:
 * - Immediate patient notification via FCM
 * - Post-emergency review tracking
 * - Escalation if patient disputes emergency claim
 */
@Stateless
public class EmergencyAccessService {

    @Inject
    private NotificationService notificationService;

    @Inject
    private AuditService auditService;

    /**
     * Logs emergency access and sends immediate notification to patient.
     *
     * @param professionalId Professional who accessed records
     * @param patientCi Patient whose records were accessed
     * @param documentId Document that was accessed
     * @param justification Emergency justification provided by professional
     */
    public void logEmergencyAccess(String professionalId, String patientCi,
                                    Long documentId, String justification) {
        // 1. Log to audit with SEVERE level
        auditService.logEvent(
            EventType.ACCESS,
            professionalId,
            "PROFESSIONAL",
            "DOCUMENT",
            documentId.toString(),
            ActionOutcome.SUCCESS,
            null, null,
            Map.of(
                "emergencyAccess", true,
                "justification", justification,
                "requiresReview", true
            )
        );

        // 2. Send immediate push notification to patient
        notificationService.sendEmergencyAccessNotification(
            patientCi,
            professionalId,
            documentId,
            justification
        );

        // 3. Create emergency review record for patient to confirm/dispute
        // TODO: Create emergency_access_reviews table and entity
    }

    /**
     * Retrieves pending emergency access reviews for a patient.
     * Patient can confirm (legitimate emergency) or dispute (investigate).
     */
    public List<EmergencyAccessReview> getPendingReviews(String patientCi) {
        // TODO: Implement
        return null;
    }

    /**
     * Patient confirms emergency access was legitimate.
     */
    public void confirmEmergencyAccess(String patientCi, Long reviewId) {
        // TODO: Update review status to CONFIRMED
    }

    /**
     * Patient disputes emergency access - escalate for investigation.
     */
    public void disputeEmergencyAccess(String patientCi, Long reviewId, String reason) {
        // TODO: Update review status to DISPUTED, notify HCEN admins
    }
}
```

**Database Changes**:
```sql
-- Add to V003__create_policies_schema.sql (or new migration V006)
CREATE TABLE IF NOT EXISTS policies.emergency_access_reviews (
    id BIGSERIAL PRIMARY KEY,
    professional_id VARCHAR(100) NOT NULL,
    patient_ci VARCHAR(20) NOT NULL,
    document_id BIGINT,
    justification TEXT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'DISPUTED')),
    accessed_at TIMESTAMP NOT NULL,
    reviewed_at TIMESTAMP,
    patient_comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_emergency_reviews_patient_status
    ON policies.emergency_access_reviews(patient_ci, status);
```

**Integration Points**:
- Modify `EmergencyOverridePolicyEvaluator` to call `EmergencyAccessService.logEmergencyAccess()`
- Add REST endpoints in `PolicyResource` for patient emergency review
- Update mobile app to show emergency access notifications

**Estimated Time**: 4 hours

---

### Task 3: Policy Conflict Analytics (Priority: Low)

**Objective**: Detect and report conflicting policies to help patients optimize their access rules.

**Files to Create**:
1. `src/main/java/uy/gub/hcen/policy/service/PolicyAnalyticsService.java` (~120 LOC)
2. `src/main/java/uy/gub/hcen/policy/dto/PolicyConflictReport.java` (~60 LOC)

**Implementation Details**:

```java
package uy.gub.hcen.policy.service;

/**
 * Policy Analytics Service
 *
 * Analyzes patient policies to detect conflicts and inefficiencies.
 *
 * Conflict Detection Examples:
 * - Policy 1: PERMIT cardiologists to see all documents
 * - Policy 2: DENY cardiologists to see psychiatric notes
 *   → Conflict: Overlapping rules with opposite effects
 *
 * - Policy 3: PERMIT all professionals from Clinic A
 * - Policy 4: DENY Dr. Smith from Clinic A
 *   → Potential conflict (specific overrides general)
 */
@Stateless
public class PolicyAnalyticsService {

    @Inject
    private AccessPolicyRepository policyRepository;

    /**
     * Detects conflicting policies for a patient.
     */
    public PolicyConflictReport analyzePatientPolicies(String patientCi) {
        List<AccessPolicy> policies = policyRepository.findActivePoliciesByPatientCi(patientCi);

        PolicyConflictReport report = new PolicyConflictReport();

        // Check for PERMIT + DENY conflicts on same criteria
        detectPermitDenyConflicts(policies, report);

        // Check for overlapping time-based policies
        detectTimeBasedConflicts(policies, report);

        // Check for redundant policies
        detectRedundantPolicies(policies, report);

        return report;
    }

    private void detectPermitDenyConflicts(List<AccessPolicy> policies,
                                          PolicyConflictReport report) {
        // Group policies by type
        Map<PolicyType, List<AccessPolicy>> byType = policies.stream()
            .collect(Collectors.groupingBy(AccessPolicy::getPolicyType));

        // For each type, check if there are both PERMIT and DENY
        byType.forEach((type, typePolicies) -> {
            boolean hasPermit = typePolicies.stream()
                .anyMatch(p -> p.getPolicyEffect() == PolicyEffect.PERMIT);
            boolean hasDeny = typePolicies.stream()
                .anyMatch(p -> p.getPolicyEffect() == PolicyEffect.DENY);

            if (hasPermit && hasDeny) {
                report.addConflict(new PolicyConflict(
                    "PERMIT_DENY_CONFLICT",
                    "You have both PERMIT and DENY policies for " + type,
                    "DENY policies will always take precedence",
                    typePolicies.stream().map(AccessPolicy::getId).collect(Collectors.toList())
                ));
            }
        });
    }

    // Similar methods for other conflict types...
}
```

**REST Endpoints**:
```java
// Add to PolicyResource.java
@GET
@Path("/patients/{ci}/conflicts")
@Produces(MediaType.APPLICATION_JSON)
public Response getPatientPolicyConflicts(@PathParam("ci") String patientCi) {
    PolicyConflictReport report = policyAnalyticsService.analyzePatientPolicies(patientCi);
    return Response.ok(report).build();
}
```

**Estimated Time**: 3 hours

---

### Task 4: Policy Versioning (Priority: Medium)

**Objective**: Track policy changes over time for audit and rollback capabilities.

**Files to Create**:
1. `src/main/java/uy/gub/hcen/policy/entity/PolicyVersion.java` (~80 LOC)
2. `src/main/java/uy/gub/hcen/policy/repository/PolicyVersionRepository.java` (~60 LOC)

**Database Schema**:
```sql
-- Add to new migration V006__add_policy_versioning.sql
CREATE TABLE IF NOT EXISTS policies.policy_versions (
    id BIGSERIAL PRIMARY KEY,
    policy_id BIGINT NOT NULL, -- References access_policies.id
    version_number INTEGER NOT NULL,

    -- Snapshot of policy at this version
    policy_type VARCHAR(50) NOT NULL,
    policy_config JSONB NOT NULL,
    policy_effect VARCHAR(10) NOT NULL,
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,
    priority INTEGER,

    -- Version metadata
    changed_by VARCHAR(100) NOT NULL, -- Who made the change
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    change_reason TEXT, -- Optional reason for change

    UNIQUE(policy_id, version_number)
);

CREATE INDEX idx_policy_versions_policy_id ON policies.policy_versions(policy_id);
CREATE INDEX idx_policy_versions_changed_at ON policies.policy_versions(changed_at DESC);
```

**Implementation**:
```java
/**
 * Automatically create policy version on every update.
 * Use JPA @PreUpdate lifecycle callback or database trigger.
 */
@Entity
@EntityListeners(PolicyVersionListener.class)
public class AccessPolicy {
    // ... existing fields ...
}

public class PolicyVersionListener {
    @PreUpdate
    public void onUpdate(AccessPolicy policy) {
        // Create PolicyVersion entity with snapshot of current state
        // Save to policies.policy_versions table
    }
}
```

**REST Endpoints**:
```java
// View policy history
GET /api/policies/{policyId}/versions

// Restore previous version
POST /api/policies/{policyId}/versions/{versionNumber}/restore
```

**Estimated Time**: 4 hours

---

### Task 5: Batch Policy Operations (Priority: Low)

**Objective**: Simplify policy management with templates and bulk operations.

**Files to Create**:
1. `src/main/java/uy/gub/hcen/policy/service/PolicyTemplateService.java` (~100 LOC)
2. `src/main/java/uy/gub/hcen/policy/dto/PolicyTemplate.java` (~50 LOC)

**Policy Templates**:
```json
{
  "templates": [
    {
      "id": "default_restrictive",
      "name": "Default Restrictive",
      "description": "Deny all except primary care clinic",
      "policies": [
        {
          "policyType": "CLINIC",
          "policyEffect": "DENY",
          "policyConfig": {"deniedClinics": ["*"]}
        },
        {
          "policyType": "CLINIC",
          "policyEffect": "PERMIT",
          "policyConfig": {"allowedClinics": ["${PRIMARY_CLINIC}"]}
        }
      ]
    },
    {
      "id": "open_medical_restricted_psychiatric",
      "name": "Open Medical, Restricted Psychiatric",
      "description": "Allow general medical access, require approval for psychiatric",
      "policies": [
        {
          "policyType": "DOCUMENT_TYPE",
          "policyEffect": "DENY",
          "policyConfig": {"allowedTypes": ["PSYCHIATRIC_NOTE", "PSYCHOLOGICAL_EVAL"]}
        }
      ]
    }
  ]
}
```

**REST Endpoints**:
```java
// List available templates
GET /api/policies/templates

// Apply template to patient
POST /api/policies/patients/{ci}/apply-template
{
  "templateId": "default_restrictive",
  "parameters": {
    "PRIMARY_CLINIC": "clinic-001"
  }
}

// Bulk delete policies
DELETE /api/policies/patients/{ci}/bulk?policyIds=1,2,3
```

**Estimated Time**: 2 hours

---

## Audit System Completion

### Task 6: Immutability Guarantees with Hash Chain (Priority: High)

**Objective**: Make audit logs tamper-evident using cryptographic hash chains.

**Approach**: Each audit log contains a hash of the previous log entry, creating a blockchain-like structure.

**Files to Modify**:
1. `src/main/java/uy/gub/hcen/audit/entity/AuditLog.java` (add previousHash, currentHash fields)
2. `src/main/java/uy/gub/hcen/service/audit/AuditService.java` (compute hashes on save)

**Files to Create**:
1. `src/main/java/uy/gub/hcen/service/audit/AuditIntegrityService.java` (~150 LOC)

**Database Migration**:
```sql
-- Add to new migration V007__add_audit_hash_chain.sql
ALTER TABLE audit.audit_logs ADD COLUMN previous_hash VARCHAR(64);
ALTER TABLE audit.audit_logs ADD COLUMN current_hash VARCHAR(64);

CREATE INDEX idx_audit_current_hash ON audit.audit_logs(current_hash);
```

**Entity Changes**:
```java
@Entity
public class AuditLog {
    // ... existing fields ...

    /**
     * SHA-256 hash of the previous audit log entry.
     * Creates a tamper-evident chain.
     */
    @Column(name = "previous_hash", length = 64)
    private String previousHash;

    /**
     * SHA-256 hash of this audit log entry.
     * Computed as: SHA256(id + eventType + actorId + resourceId + timestamp + previousHash)
     */
    @Column(name = "current_hash", length = 64, unique = true)
    private String currentHash;

    // Add getters (no setters - immutable)
}
```

**Service Implementation**:
```java
package uy.gub.hcen.service.audit;

/**
 * Audit Integrity Service
 *
 * Ensures audit log integrity using cryptographic hash chains.
 * Each audit log entry contains:
 * - currentHash: SHA-256(current entry fields + previousHash)
 * - previousHash: currentHash of the previous entry
 *
 * This creates a blockchain-like structure where any tampering
 * breaks the hash chain and can be detected.
 */
@Stateless
public class AuditIntegrityService {

    @Inject
    private AuditLogRepository auditLogRepository;

    /**
     * Computes SHA-256 hash for an audit log entry.
     */
    public String computeHash(AuditLog log, String previousHash) {
        String data = String.join("|",
            log.getId().toString(),
            log.getEventType().name(),
            log.getActorId(),
            log.getResourceType(),
            log.getResourceId(),
            log.getTimestamp().toString(),
            previousHash != null ? previousHash : "GENESIS"
        );

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Validates entire audit log chain integrity.
     * Returns list of tampered entries (if any).
     */
    public AuditIntegrityReport validateIntegrity() {
        List<AuditLog> allLogs = auditLogRepository.findAllOrderedById();

        AuditIntegrityReport report = new AuditIntegrityReport();
        String expectedPreviousHash = null;

        for (AuditLog log : allLogs) {
            // Check if previousHash matches expected
            if (!Objects.equals(log.getPreviousHash(), expectedPreviousHash)) {
                report.addViolation(log.getId(), "Previous hash mismatch");
            }

            // Recompute currentHash and verify
            String expectedCurrentHash = computeHash(log, log.getPreviousHash());
            if (!Objects.equals(log.getCurrentHash(), expectedCurrentHash)) {
                report.addViolation(log.getId(), "Current hash mismatch (tampered)");
            }

            expectedPreviousHash = log.getCurrentHash();
        }

        return report;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
```

**Integration in AuditService**:
```java
@Stateless
public class AuditService {

    @Inject
    private AuditIntegrityService integrityService;

    public void logEvent(...) {
        // ... existing code to create AuditLog ...

        // Get last audit log to obtain previous hash
        Optional<AuditLog> lastLog = auditLogRepository.findLatest();
        String previousHash = lastLog.map(AuditLog::getCurrentHash).orElse(null);

        // Save audit log (without hash first to get ID)
        auditLog = auditLogRepository.save(auditLog);

        // Compute current hash
        String currentHash = integrityService.computeHash(auditLog, previousHash);

        // Update audit log with hashes (use native query to bypass immutability)
        auditLogRepository.updateHashes(auditLog.getId(), previousHash, currentHash);
    }
}
```

**REST Endpoints**:
```java
// Add to AuditResource.java
@GET
@Path("/integrity/validate")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public Response validateAuditIntegrity() {
    AuditIntegrityReport report = auditIntegrityService.validateIntegrity();
    return Response.ok(report).build();
}
```

**Estimated Time**: 4 hours

---

### Task 7: Retention Policy Enforcement with MongoDB Archival (Priority: High)

**Objective**: Automatically archive audit logs older than 90 days to MongoDB, keeping PostgreSQL lean.

**Files to Create**:
1. `src/main/java/uy/gub/hcen/service/audit/AuditRetentionService.java` (~150 LOC)
2. `src/main/java/uy/gub/hcen/audit/repository/AuditLogArchiveRepository.java` (MongoDB) (~100 LOC)

**MongoDB Configuration**:
```java
// Add to MongoDBConfiguration.java
@Produces
@ApplicationScoped
public MongoDatabase auditArchiveDatabase() {
    MongoClient mongoClient = MongoClients.create(mongoUri);
    return mongoClient.getDatabase("hcen_audit_archive");
}
```

**Implementation**:
```java
package uy.gub.hcen.service.audit;

/**
 * Audit Retention Service
 *
 * Implements retention policy: 90 days in PostgreSQL, archive to MongoDB.
 *
 * Retention Rules:
 * - Standard events: 90 days in PostgreSQL → MongoDB → Delete after 7 years
 * - Critical events (emergency access, policy changes): Never delete
 * - Authentication events: 30 days in PostgreSQL → MongoDB → Delete after 1 year
 */
@Stateless
public class AuditRetentionService {

    @Inject
    private AuditLogRepository postgresRepository;

    @Inject
    private AuditLogArchiveRepository mongoRepository;

    /**
     * Scheduled job to archive old logs.
     * Runs daily at 2 AM.
     */
    @Schedule(hour = "2", minute = "0", persistent = false)
    public void archiveOldLogs() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);

        // Find logs older than 90 days
        List<AuditLog> logsToArchive = postgresRepository.findOlderThan(cutoffDate);

        LOGGER.info("Archiving " + logsToArchive.size() + " audit logs to MongoDB");

        int archived = 0;
        for (AuditLog log : logsToArchive) {
            try {
                // Convert to MongoDB document
                Document mongoDoc = convertToMongoDocument(log);

                // Save to MongoDB
                mongoRepository.insert(mongoDoc);

                // Delete from PostgreSQL (only if not critical)
                if (!isCriticalEvent(log)) {
                    postgresRepository.deleteById(log.getId());
                    archived++;
                }

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to archive audit log " + log.getId(), e);
            }
        }

        LOGGER.info("Successfully archived " + archived + " audit logs");
    }

    private boolean isCriticalEvent(AuditLog log) {
        return log.getEventType() == EventType.POLICY_CHANGE
            || log.getEventType() == EventType.ACCESS_DENIAL
            || (log.getDetails() != null && log.getDetails().contains("emergencyAccess"));
    }

    private Document convertToMongoDocument(AuditLog log) {
        return new Document()
            .append("_id", log.getId())
            .append("eventType", log.getEventType().name())
            .append("actorId", log.getActorId())
            .append("actorType", log.getActorType())
            .append("resourceType", log.getResourceType())
            .append("resourceId", log.getResourceId())
            .append("actionOutcome", log.getActionOutcome().name())
            .append("timestamp", Date.from(log.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()))
            .append("ipAddress", log.getIpAddress())
            .append("userAgent", log.getUserAgent())
            .append("details", log.getDetails())
            .append("previousHash", log.getPreviousHash())
            .append("currentHash", log.getCurrentHash())
            .append("archivedAt", new Date());
    }

    /**
     * Retrieves archived logs from MongoDB for patient queries.
     */
    public List<AuditLog> getArchivedPatientHistory(String patientCi, LocalDateTime from, LocalDateTime to) {
        return mongoRepository.findByPatientCiAndDateRange(patientCi, from, to);
    }
}
```

**MongoDB Repository**:
```java
package uy.gub.hcen.audit.repository;

/**
 * Audit Log Archive Repository (MongoDB)
 */
@ApplicationScoped
public class AuditLogArchiveRepository {

    @Inject
    @Named("auditArchiveDatabase")
    private MongoDatabase database;

    private MongoCollection<Document> getCollection() {
        return database.getCollection("audit_logs");
    }

    public void insert(Document auditLog) {
        getCollection().insertOne(auditLog);
    }

    public List<AuditLog> findByPatientCiAndDateRange(String patientCi, LocalDateTime from, LocalDateTime to) {
        // Query MongoDB with date range
        // Convert Documents back to AuditLog entities
        // Return results
    }
}
```

**Configuration**:
```java
// Add to application.properties or standalone.xml
audit.retention.days=90
audit.archive.critical.events=true
audit.mongodb.uri=mongodb://localhost:27017
```

**Estimated Time**: 5 hours

---

### Task 8: Comprehensive Event Interceptors with @Audited (Priority: High)

**Objective**: Automatically audit all critical operations using CDI interceptors.

**Files to Modify**:
1. `src/main/java/uy/gub/hcen/service/audit/annotation/Audited.java` (enhance annotation)

**Files to Create**:
1. `src/main/java/uy/gub/hcen/service/audit/interceptor/AuditedInterceptor.java` (~200 LOC)

**Enhanced Annotation**:
```java
package uy.gub.hcen.service.audit.annotation;

/**
 * Annotation to automatically audit method invocations.
 *
 * Example usage:
 * @Audited(eventType = EventType.ACCESS, resourceType = "DOCUMENT")
 * public Document getDocument(Long documentId) { ... }
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Audited {

    /**
     * Event type for this audit entry
     */
    AuditLog.EventType eventType();

    /**
     * Resource type (DOCUMENT, USER, POLICY, etc.)
     */
    String resourceType();

    /**
     * SpEL expression to extract resource ID from method parameters
     * Example: "#documentId" extracts first parameter named documentId
     */
    String resourceIdExpression() default "#p0"; // Default to first parameter

    /**
     * SpEL expression to extract actor ID from context
     * Default: extracts from SecurityContext
     */
    String actorIdExpression() default "";

    /**
     * Whether to log on success only (default) or always
     */
    boolean logOnSuccessOnly() default true;
}
```

**Interceptor Implementation**:
```java
package uy.gub.hcen.service.audit.interceptor;

/**
 * CDI Interceptor for @Audited annotation.
 *
 * Automatically logs audit events for annotated methods.
 */
@Interceptor
@Audited
@Priority(Interceptor.Priority.APPLICATION)
public class AuditedInterceptor {

    @Inject
    private AuditService auditService;

    @Inject
    private SecurityContext securityContext; // For extracting current user

    @AroundInvoke
    public Object auditMethodInvocation(InvocationContext context) throws Exception {
        Audited annotation = context.getMethod().getAnnotation(Audited.class);

        // Extract audit parameters from annotation and method context
        String actorId = extractActorId(annotation, context);
        String resourceId = extractResourceId(annotation, context);
        String resourceType = annotation.resourceType();
        AuditLog.EventType eventType = annotation.eventType();

        // Extract HTTP context (IP, user agent) if available
        String ipAddress = extractIpAddress();
        String userAgent = extractUserAgent();

        Object result = null;
        ActionOutcome outcome = ActionOutcome.SUCCESS;
        Map<String, Object> details = new HashMap<>();

        try {
            // Invoke the actual method
            result = context.proceed();

        } catch (Exception e) {
            outcome = ActionOutcome.FAILURE;
            details.put("error", e.getMessage());

            if (!annotation.logOnSuccessOnly()) {
                // Log failure
                auditService.logEvent(
                    eventType, actorId, "PROFESSIONAL",
                    resourceType, resourceId, outcome,
                    ipAddress, userAgent, details
                );
            }

            throw e; // Re-throw exception
        }

        // Log success
        if (outcome == ActionOutcome.SUCCESS) {
            details.put("method", context.getMethod().getName());
            details.put("parameters", extractParameterNames(context));

            auditService.logEvent(
                eventType, actorId, "PROFESSIONAL",
                resourceType, resourceId, outcome,
                ipAddress, userAgent, details
            );
        }

        return result;
    }

    private String extractActorId(Audited annotation, InvocationContext context) {
        if (!annotation.actorIdExpression().isEmpty()) {
            // Use SpEL or simple parameter extraction
            return evaluateExpression(annotation.actorIdExpression(), context);
        }

        // Default: extract from SecurityContext/JWT
        Principal principal = securityContext.getUserPrincipal();
        return principal != null ? principal.getName() : "SYSTEM";
    }

    private String extractResourceId(Audited annotation, InvocationContext context) {
        String expression = annotation.resourceIdExpression();
        return evaluateExpression(expression, context);
    }

    private String evaluateExpression(String expression, InvocationContext context) {
        // Simple parameter extraction: #p0, #p1, #paramName
        if (expression.startsWith("#p")) {
            int index = Integer.parseInt(expression.substring(2));
            Object param = context.getParameters()[index];
            return param != null ? param.toString() : "UNKNOWN";
        }

        if (expression.startsWith("#")) {
            String paramName = expression.substring(1);
            // Match parameter name to method parameter
            // (requires Java 8+ reflection with -parameters compiler flag)
            // Simplified: just use first parameter
            return context.getParameters()[0].toString();
        }

        return "UNKNOWN";
    }

    private String extractIpAddress() {
        // Extract from HTTP request context if available
        // Use CDI to inject HttpServletRequest
        return null; // TODO: Implement
    }

    private String extractUserAgent() {
        // Extract from HTTP request headers
        return null; // TODO: Implement
    }
}
```

**Usage in Services**:
```java
// RNDC Service - automatically audit document access
@Audited(eventType = EventType.ACCESS, resourceType = "DOCUMENT", resourceIdExpression = "#documentId")
public Document getDocumentById(Long documentId) {
    // ... fetch document ...
}

// Policy Service - automatically audit policy changes
@Audited(eventType = EventType.POLICY_CHANGE, resourceType = "POLICY", resourceIdExpression = "#policyId")
public void updatePolicy(Long policyId, PolicyUpdateRequest request) {
    // ... update policy ...
}

// INUS Service - automatically audit user creation
@Audited(eventType = EventType.CREATION, resourceType = "USER", resourceIdExpression = "#userCi")
public InusUser createUser(String userCi, CreateUserRequest request) {
    // ... create user ...
}
```

**Enable Interceptor** (beans.xml):
```xml
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                           https://jakarta.ee/xml/ns/jakartaee/beans_3_0.xsd"
       bean-discovery-mode="all">
    <interceptors>
        <class>uy.gub.hcen.service.audit.interceptor.AuditedInterceptor</class>
    </interceptors>
</beans>
```

**Estimated Time**: 6 hours

---

### Task 9: Audit Export API (Priority: Medium)

**Objective**: Enable admins and patients to export audit logs in CSV/JSON formats.

**Files to Create**:
1. `src/main/java/uy/gub/hcen/service/audit/AuditExportService.java` (~120 LOC)
2. `src/main/java/uy/gub/hcen/audit/dto/AuditExportRequest.java` (~50 LOC)

**Implementation**:
```java
package uy.gub.hcen.service.audit;

/**
 * Audit Export Service
 *
 * Exports audit logs to CSV or JSON formats.
 * Supports streaming for large result sets.
 */
@Stateless
public class AuditExportService {

    @Inject
    private AuditLogRepository auditLogRepository;

    /**
     * Exports audit logs to CSV format.
     * Uses streaming to handle large datasets.
     */
    public StreamingOutput exportToCsv(AuditExportRequest request) {
        return outputStream -> {
            try (PrintWriter writer = new PrintWriter(outputStream)) {
                // CSV header
                writer.println("ID,Timestamp,Event Type,Actor ID,Actor Type,Resource Type,Resource ID,Outcome,IP Address");

                // Stream results (pagination)
                int page = 0;
                int pageSize = 1000;
                List<AuditLog> logs;

                do {
                    logs = auditLogRepository.findByDateRange(
                        request.getFromDate(), request.getToDate(), page, pageSize);

                    for (AuditLog log : logs) {
                        writer.println(formatCsvRow(log));
                    }

                    page++;
                } while (logs.size() == pageSize);
            }
        };
    }

    /**
     * Exports audit logs to JSON format.
     */
    public StreamingOutput exportToJson(AuditExportRequest request) {
        return outputStream -> {
            try (JsonGenerator json = Json.createGenerator(outputStream)) {
                json.writeStartArray();

                int page = 0;
                int pageSize = 1000;
                List<AuditLog> logs;

                do {
                    logs = auditLogRepository.findByDateRange(
                        request.getFromDate(), request.getToDate(), page, pageSize);

                    for (AuditLog log : logs) {
                        writeJsonAuditLog(json, log);
                    }

                    page++;
                } while (logs.size() == pageSize);

                json.writeEnd();
            }
        };
    }

    private String formatCsvRow(AuditLog log) {
        return String.join(",",
            log.getId().toString(),
            log.getTimestamp().toString(),
            log.getEventType().name(),
            escapeCsv(log.getActorId()),
            escapeCsv(log.getActorType()),
            log.getResourceType(),
            log.getResourceId(),
            log.getActionOutcome().name(),
            escapeCsv(log.getIpAddress())
        );
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
```

**REST Endpoints**:
```java
// Add to AuditResource.java
@POST
@Path("/export/csv")
@Produces("text/csv")
@RolesAllowed({"ADMIN", "PATIENT"})
public Response exportAuditLogsCsv(AuditExportRequest request) {
    StreamingOutput stream = auditExportService.exportToCsv(request);

    return Response.ok(stream)
        .header("Content-Disposition", "attachment; filename=\"audit_logs.csv\"")
        .build();
}

@POST
@Path("/export/json")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "PATIENT"})
public Response exportAuditLogsJson(AuditExportRequest request) {
    StreamingOutput stream = auditExportService.exportToJson(request);

    return Response.ok(stream)
        .header("Content-Disposition", "attachment; filename=\"audit_logs.json\"")
        .build();
}
```

**Estimated Time**: 3 hours

---

### Task 10: Advanced Query Capabilities (Priority: Low)

**Objective**: Provide analytics and anomaly detection for audit logs.

**Files to Create**:
1. `src/main/java/uy/gub/hcen/service/audit/AuditAnalyticsService.java` (~150 LOC)

**Implementation**:
```java
/**
 * Audit Analytics Service
 *
 * Provides analytics and anomaly detection for audit logs.
 */
@Stateless
public class AuditAnalyticsService {

    @Inject
    private AuditLogRepository auditLogRepository;

    /**
     * Finds most accessed documents.
     */
    public List<DocumentAccessStats> getMostAccessedDocuments(LocalDateTime from, LocalDateTime to, int limit) {
        // SQL aggregation query
        String sql = """
            SELECT resource_id, COUNT(*) as access_count
            FROM audit.audit_logs
            WHERE event_type = 'ACCESS' AND resource_type = 'DOCUMENT'
              AND timestamp BETWEEN :from AND :to
            GROUP BY resource_id
            ORDER BY access_count DESC
            LIMIT :limit
            """;

        // Execute native query and map results
    }

    /**
     * Detects unusual access patterns (anomaly detection).
     *
     * Anomalies:
     * - Same professional accessing many different patients in short time
     * - Access outside normal working hours
     * - Multiple failed authentication attempts
     * - Emergency access without recent clinical interaction
     */
    public List<AnomalyReport> detectAnomalies(LocalDateTime from, LocalDateTime to) {
        List<AnomalyReport> anomalies = new ArrayList<>();

        // Detect bulk access pattern
        anomalies.addAll(detectBulkAccess(from, to));

        // Detect off-hours access
        anomalies.addAll(detectOffHoursAccess(from, to));

        // Detect failed authentication patterns
        anomalies.addAll(detectBruteForceAttempts(from, to));

        return anomalies;
    }

    private List<AnomalyReport> detectBulkAccess(LocalDateTime from, LocalDateTime to) {
        // Find professionals who accessed > 50 different patients in 1 hour
        String sql = """
            SELECT actor_id, COUNT(DISTINCT resource_id) as patient_count,
                   DATE_TRUNC('hour', timestamp) as hour
            FROM audit.audit_logs
            WHERE event_type = 'ACCESS' AND resource_type = 'DOCUMENT'
              AND timestamp BETWEEN :from AND :to
            GROUP BY actor_id, DATE_TRUNC('hour', timestamp)
            HAVING COUNT(DISTINCT resource_id) > 50
            """;

        // Execute and create anomaly reports
    }
}
```

**REST Endpoints**:
```java
@GET
@Path("/analytics/most-accessed")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public Response getMostAccessedDocuments(
    @QueryParam("from") String from,
    @QueryParam("to") String to,
    @QueryParam("limit") @DefaultValue("10") int limit) {

    List<DocumentAccessStats> stats = analyticsService.getMostAccessedDocuments(
        LocalDateTime.parse(from), LocalDateTime.parse(to), limit);

    return Response.ok(stats).build();
}

@GET
@Path("/analytics/anomalies")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public Response detectAnomalies(
    @QueryParam("from") String from,
    @QueryParam("to") String to) {

    List<AnomalyReport> anomalies = analyticsService.detectAnomalies(
        LocalDateTime.parse(from), LocalDateTime.parse(to));

    return Response.ok(anomalies).build();
}
```

**Estimated Time**: 4 hours

---

## Database Schema

### Complete PostgreSQL Schema

```sql
-- ================================================================
-- V006__enhance_policies_and_audit.sql
-- ================================================================

-- =======================================================
-- POLICIES ENHANCEMENTS
-- =======================================================

-- Add ROLE policy type (requires enum update in Java)
-- Note: PostgreSQL doesn't enforce Java enums, so this is documentation
COMMENT ON COLUMN policies.access_policies.policy_type IS
  'Policy type: DOCUMENT_TYPE, SPECIALTY, TIME_BASED, CLINIC, PROFESSIONAL, EMERGENCY_OVERRIDE, ROLE';

-- Emergency access reviews table
CREATE TABLE IF NOT EXISTS policies.emergency_access_reviews (
    id BIGSERIAL PRIMARY KEY,
    professional_id VARCHAR(100) NOT NULL,
    patient_ci VARCHAR(20) NOT NULL,
    document_id BIGINT,
    justification TEXT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'DISPUTED')),
    accessed_at TIMESTAMP NOT NULL,
    reviewed_at TIMESTAMP,
    patient_comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_emergency_reviews_patient_status
    ON policies.emergency_access_reviews(patient_ci, status);
CREATE INDEX idx_emergency_reviews_accessed_at
    ON policies.emergency_access_reviews(accessed_at DESC);

-- Policy versions table (for policy change tracking)
CREATE TABLE IF NOT EXISTS policies.policy_versions (
    id BIGSERIAL PRIMARY KEY,
    policy_id BIGINT NOT NULL,
    version_number INTEGER NOT NULL,

    -- Snapshot of policy at this version
    policy_type VARCHAR(50) NOT NULL,
    policy_config JSONB NOT NULL,
    policy_effect VARCHAR(10) NOT NULL,
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,
    priority INTEGER,

    -- Version metadata
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    change_reason TEXT,

    UNIQUE(policy_id, version_number)
);

CREATE INDEX idx_policy_versions_policy_id ON policies.policy_versions(policy_id);
CREATE INDEX idx_policy_versions_changed_at ON policies.policy_versions(changed_at DESC);

-- =======================================================
-- AUDIT ENHANCEMENTS
-- =======================================================

-- Add hash chain columns for immutability
ALTER TABLE audit.audit_logs ADD COLUMN previous_hash VARCHAR(64);
ALTER TABLE audit.audit_logs ADD COLUMN current_hash VARCHAR(64);

CREATE INDEX idx_audit_current_hash ON audit.audit_logs(current_hash);

-- Add partitioning by timestamp (for performance with large datasets)
-- This requires restructuring the table as a partitioned table
-- For simplicity, we'll add an index instead
CREATE INDEX idx_audit_timestamp_desc ON audit.audit_logs(timestamp DESC);

-- Add composite index for common query patterns
CREATE INDEX idx_audit_resource_timestamp
    ON audit.audit_logs(resource_type, resource_id, timestamp DESC);

-- Audit integrity violations table (for tracking detected tampering)
CREATE TABLE IF NOT EXISTS audit.integrity_violations (
    id BIGSERIAL PRIMARY KEY,
    audit_log_id BIGINT NOT NULL,
    violation_type VARCHAR(50) NOT NULL,
    violation_details TEXT,
    detected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_integrity_violations_detected_at
    ON audit.integrity_violations(detected_at DESC);

-- =======================================================
-- COMMENTS
-- =======================================================

COMMENT ON TABLE policies.emergency_access_reviews IS
  'Tracks emergency access to patient records for post-access review';

COMMENT ON TABLE policies.policy_versions IS
  'Version history of access policies for audit and rollback';

COMMENT ON COLUMN audit.audit_logs.previous_hash IS
  'SHA-256 hash of previous audit log entry (creates tamper-evident chain)';

COMMENT ON COLUMN audit.audit_logs.current_hash IS
  'SHA-256 hash of this audit log entry';

COMMENT ON TABLE audit.integrity_violations IS
  'Detected audit log tampering attempts or integrity violations';
```

### MongoDB Collections

```javascript
// hcen_audit_archive database

// audit_logs collection (archived from PostgreSQL after 90 days)
db.createCollection("audit_logs", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "eventType", "actorId", "resourceType", "resourceId", "timestamp", "actionOutcome"],
      properties: {
        _id: { bsonType: "long" },
        eventType: { bsonType: "string" },
        actorId: { bsonType: "string" },
        actorType: { bsonType: "string" },
        resourceType: { bsonType: "string" },
        resourceId: { bsonType: "string" },
        actionOutcome: { bsonType: "string" },
        timestamp: { bsonType: "date" },
        ipAddress: { bsonType: ["string", "null"] },
        userAgent: { bsonType: ["string", "null"] },
        details: { bsonType: ["string", "null"] },
        previousHash: { bsonType: ["string", "null"] },
        currentHash: { bsonType: "string" },
        archivedAt: { bsonType: "date" }
      }
    }
  }
});

// Indexes for performance
db.audit_logs.createIndex({ "timestamp": -1 });
db.audit_logs.createIndex({ "actorId": 1, "timestamp": -1 });
db.audit_logs.createIndex({ "resourceType": 1, "resourceId": 1, "timestamp": -1 });
db.audit_logs.createIndex({ "eventType": 1 });

// TTL index to auto-delete after 7 years (2555 days)
db.audit_logs.createIndex({ "archivedAt": 1 }, { expireAfterSeconds: 220752000 });
```

---

## Implementation Sequence

### Phase 1: Critical Audit Enhancements (Priority 1)

**Objective**: Complete audit system immutability and retention enforcement

**Tasks**:
1. Task 6: Immutability Guarantees with Hash Chain (4 hours)
2. Task 7: Retention Policy Enforcement with MongoDB Archival (5 hours)
3. Task 8: Comprehensive Event Interceptors with @Audited (6 hours)

**Deliverables**:
- Tamper-evident audit logs with hash chain validation
- Automated archival to MongoDB after 90 days
- Automatic audit logging for all critical operations

**Testing**:
- Unit tests for hash computation and validation
- Integration tests for MongoDB archival
- Test @Audited interceptor on sample methods

**Estimated Total**: 15 hours

---

### Phase 2: Policy Engine Workflow Enhancements (Priority 1)

**Objective**: Complete emergency access workflow and RBAC support

**Tasks**:
1. Task 2: Emergency Override Workflow Enhancement (4 hours)
2. Task 1: RBAC Integration (3 hours)
3. Task 4: Policy Versioning (4 hours)

**Deliverables**:
- Emergency access with patient notifications and review mechanism
- Role-based policy evaluation
- Policy change tracking and versioning

**Testing**:
- Test emergency access notification flow
- Test role hierarchy evaluation
- Test policy version creation on updates

**Estimated Total**: 11 hours

---

### Phase 3: Analytics and Export Features (Priority 2)

**Objective**: Add analytics, conflict detection, and export capabilities

**Tasks**:
1. Task 9: Audit Export API (3 hours)
2. Task 10: Advanced Query Capabilities (4 hours)
3. Task 3: Policy Conflict Analytics (3 hours)

**Deliverables**:
- CSV/JSON audit log export
- Anomaly detection and analytics
- Policy conflict detection and recommendations

**Testing**:
- Test large export streaming
- Test anomaly detection algorithms
- Test conflict detection edge cases

**Estimated Total**: 10 hours

---

### Phase 4: User Experience Enhancements (Priority 3)

**Objective**: Simplify policy management and improve usability

**Tasks**:
1. Task 5: Batch Policy Operations (2 hours)

**Deliverables**:
- Policy templates
- Bulk policy operations

**Testing**:
- Test template application
- Test bulk operations

**Estimated Total**: 2 hours

---

### Phase 5: Integration and End-to-End Testing (Priority 1)

**Objective**: Ensure all components work together seamlessly

**Tasks**:
1. Integrate PolicyEngine with RndcService for document access (2 hours)
2. Integrate AuditService with all services via @Audited (1 hour)
3. End-to-end testing of complete workflows (5 hours)
   - Professional requests document → policy evaluation → audit log
   - Emergency access → patient notification → review
   - Policy change → versioning → audit
4. Performance testing and optimization (2 hours)

**Deliverables**:
- Complete integration of all components
- End-to-end test suite
- Performance benchmarks

**Estimated Total**: 10 hours

---

## Testing Strategy

### Unit Tests

**Policy Engine**:
```java
// PolicyEngineTest.java
@Test
public void testConflictResolution_DenyWins() {
    // Given: Two policies - one PERMIT, one DENY for same criteria
    AccessPolicy permitPolicy = createPolicy(PolicyType.SPECIALTY, PolicyEffect.PERMIT);
    AccessPolicy denyPolicy = createPolicy(PolicyType.SPECIALTY, PolicyEffect.DENY);

    // When: Evaluate both policies
    List<PolicyDecision> decisions = Arrays.asList(
        PolicyDecision.PERMIT,
        PolicyDecision.DENY
    );

    // Then: DENY should win
    PolicyDecision result = policyEngine.resolveConflicts(decisions);
    assertEquals(PolicyDecision.DENY, result);
}

@Test
public void testEmergencyOverride_SendsNotification() {
    // Given: Emergency access policy
    AccessRequest request = createEmergencyRequest();

    // When: Evaluate policy
    PolicyEvaluationResult result = policyEngine.evaluate(request);

    // Then: Should permit and trigger notification
    assertEquals(PolicyDecision.PERMIT, result.getDecision());
    verify(emergencyAccessService).logEmergencyAccess(any(), any(), any(), any());
    verify(notificationService).sendEmergencyAccessNotification(any(), any(), any(), any());
}
```

**Audit System**:
```java
// AuditIntegrityServiceTest.java
@Test
public void testHashChain_ValidSequence() {
    // Given: Three consecutive audit logs
    AuditLog log1 = createAuditLog();
    AuditLog log2 = createAuditLog();
    AuditLog log3 = createAuditLog();

    // When: Validate chain
    AuditIntegrityReport report = integrityService.validateIntegrity();

    // Then: Should have no violations
    assertTrue(report.isValid());
    assertEquals(0, report.getViolations().size());
}

@Test
public void testHashChain_DetectsTampering() {
    // Given: Audit logs with tampered entry
    AuditLog log1 = createAuditLog();
    AuditLog log2 = createAuditLog();

    // Simulate tampering (change actorId without updating hash)
    log2.setActorId("TAMPERED"); // Should not be possible with immutable entity

    // When: Validate chain
    AuditIntegrityReport report = integrityService.validateIntegrity();

    // Then: Should detect violation
    assertFalse(report.isValid());
    assertTrue(report.getViolations().size() > 0);
}
```

### Integration Tests

```java
// PolicyAuditIntegrationTest.java
@Test
@InSequence(1)
public void testDocumentAccess_WithPolicyEvaluation_CreatesAuditLog() {
    // Given: Patient with DENY policy for cardiologists
    String patientCi = "12345678";
    createPolicy(patientCi, PolicyType.SPECIALTY, PolicyEffect.DENY,
        "{\"allowedSpecialties\": [\"CARDIOLOGY\"]}");

    // When: Cardiologist tries to access document
    Response response = given()
        .header("Authorization", "Bearer " + cardiologistJwt)
        .when()
        .get("/api/rndc/documents/456")
        .then()
        .statusCode(403) // Should be denied
        .extract().response();

    // Then: Audit log should be created
    List<AuditLog> logs = auditLogRepository.findByPatientCi(patientCi);
    assertEquals(1, logs.size());
    assertEquals(EventType.ACCESS, logs.get(0).getEventType());
    assertEquals(ActionOutcome.DENIED, logs.get(0).getActionOutcome());
}
```

### Performance Tests

```java
// AuditPerformanceTest.java
@Test
public void testAuditLogging_HighThroughput() {
    // Simulate 1000 concurrent audit log writes
    int numThreads = 100;
    int logsPerThread = 10;

    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    List<Future<Long>> futures = new ArrayList<>();

    long start = System.currentTimeMillis();

    for (int i = 0; i < numThreads; i++) {
        futures.add(executor.submit(() -> {
            for (int j = 0; j < logsPerThread; j++) {
                auditService.logEvent(...);
            }
            return System.currentTimeMillis();
        }));
    }

    // Wait for all threads to complete
    for (Future<Long> future : futures) {
        future.get();
    }

    long duration = System.currentTimeMillis() - start;

    // Should complete within 5 seconds (200 logs/second minimum)
    assertTrue(duration < 5000, "Duration: " + duration + "ms");

    // Verify all logs were created
    long totalLogs = auditLogRepository.count();
    assertEquals(1000, totalLogs);
}
```

### Coverage Goals

- **Overall Coverage**: 80%+ (as required by AC017, AC018)
- **Policy Engine**: 90%+ (critical security component)
- **Audit System**: 85%+ (compliance requirement)
- **Integration Tests**: Cover all major workflows

---

## Integration Points

### RNDC ↔ Policy Engine Integration

**Modify**: `src/main/java/uy/gub/hcen/rndc/service/RndcService.java`

```java
@Stateless
public class RndcService {

    @Inject
    private PolicyEngine policyEngine;

    @Inject
    private AuditService auditService;

    /**
     * Get document by ID with policy enforcement.
     */
    @Audited(eventType = EventType.ACCESS, resourceType = "DOCUMENT", resourceIdExpression = "#documentId")
    public Document getDocument(Long documentId, SecurityContext securityContext) {
        // 1. Fetch document metadata from RNDC
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new NotFoundException("Document not found"));

        // 2. Extract professional context from JWT
        String professionalId = securityContext.getUserPrincipal().getName();
        // Extract specialty, clinic, role from JWT claims

        // 3. Build access request
        AccessRequest accessRequest = AccessRequest.builder()
            .professionalId(professionalId)
            .specialties(extractSpecialties(securityContext))
            .clinicId(extractClinicId(securityContext))
            .patientCi(document.getPatientCi())
            .documentType(document.getDocumentType())
            .documentId(documentId)
            .build();

        // 4. Evaluate policies
        PolicyEvaluationResult evaluation = policyEngine.evaluate(accessRequest);

        // 5. Handle decision
        if (evaluation.isDenied()) {
            // Access denied by policy
            throw new ForbiddenException(evaluation.getReason());
        }

        if (evaluation.isPending()) {
            // Create access request for patient approval
            accessRequestService.createPendingRequest(accessRequest, evaluation);
            throw new PendingApprovalException("Access requires patient approval");
        }

        // 6. Access permitted - return document
        return document;
    }
}
```

### Mobile App ↔ Policy Engine Integration

**Mobile Notification Flow**:

1. **Professional requests access** → Policy evaluates to PENDING
2. **HCEN creates AccessRequest** entity
3. **HCEN sends FCM notification** to patient's mobile device
4. **Patient opens notification** → Mobile app shows access request details
5. **Patient approves/denies** → Mobile app calls `POST /api/policies/access-requests/{id}/approve`
6. **HCEN updates AccessRequest** status
7. **HCEN notifies professional** via peripheral node callback

**REST Endpoints** (add to `PolicyResource.java`):

```java
// Patient approves access request
@POST
@Path("/access-requests/{requestId}/approve")
@RolesAllowed("PATIENT")
public Response approveAccessRequest(@PathParam("requestId") Long requestId) {
    accessRequestService.approveRequest(requestId);
    return Response.ok().build();
}

// Patient denies access request
@POST
@Path("/access-requests/{requestId}/deny")
@RolesAllowed("PATIENT")
public Response denyAccessRequest(@PathParam("requestId") Long requestId,
                                   @QueryParam("reason") String reason) {
    accessRequestService.denyRequest(requestId, reason);
    return Response.ok().build();
}

// Get pending requests for patient
@GET
@Path("/patients/{ci}/access-requests/pending")
@RolesAllowed("PATIENT")
public Response getPendingAccessRequests(@PathParam("ci") String patientCi) {
    List<AccessRequest> pending = accessRequestService.getPendingRequests(patientCi);
    return Response.ok(pending).build();
}
```

---

## Infrastructure Requirements

Update `INFRASTRUCTURE_REQUIREMENTS.md` with the following:

```markdown
## Infrastructure Requirements - Policy Engine & Audit System

### PostgreSQL Database

**Version**: 14+

**Databases**:
- `hcen_db` - Main application database

**Schemas**:
- `policies` - Access policies, access requests, emergency reviews, policy versions
- `audit` - Audit logs, integrity violations

**Required Extensions**:
- `uuid-ossp` - UUID generation
- `pgcrypto` - Cryptographic functions (for hash computations)

**Connection Pool**:
- Minimum: 10
- Maximum: 50
- Timeout: 30 seconds

**Disk Space**:
- Policies: ~100 MB (for 100K policies)
- Audit logs: ~10 GB/year (90-day retention before archival)

### MongoDB

**Version**: 6.0+

**Databases**:
- `hcen_audit_archive` - Archived audit logs (90+ days old)

**Collections**:
- `audit_logs` - Archived audit log documents

**Storage**:
- Estimated: ~50 GB/year for audit archives
- TTL: 7 years (automatic deletion after 7 years)

**Replica Set**:
- Recommended: 3-node replica set for high availability
- Write Concern: `majority`

**Connection String**:
```
mongodb://hcen_user:password@mongo1:27017,mongo2:27017,mongo3:27017/hcen_audit_archive?replicaSet=hcenRS
```

### Redis Cache

**Version**: 6.2+

**Purpose**: Policy decision caching (5-minute TTL)

**Memory**:
- Estimated: 1 GB
- Eviction Policy: `allkeys-lru`

**Key Patterns**:
- `policy:decision:{patientCi}:{specialty}:{documentType}` → PERMIT/DENY/PENDING

**Connection**:
```
redis://hcen_user:password@redis-host:6379
```

### WildFly Configuration

**standalone-full.xml** additions:

```xml
<!-- PostgreSQL Datasource -->
<datasource jndi-name="java:jboss/datasources/HcenDS" pool-name="HcenDS">
    <connection-url>jdbc:postgresql://localhost:5432/hcen_db</connection-url>
    <driver>postgresql</driver>
    <security>
        <user-name>hcen_user</user-name>
        <password>${POSTGRES_PASSWORD}</password>
    </security>
    <pool>
        <min-pool-size>10</min-pool-size>
        <max-pool-size>50</max-pool-size>
    </pool>
</datasource>

<!-- MongoDB Connection Factory (custom resource adapter) -->
<!-- Configure via system properties or environment variables -->
```

**System Properties**:
```properties
# MongoDB
mongodb.uri=mongodb://hcen_user:password@mongo1:27017,mongo2:27017,mongo3:27017/hcen_audit_archive?replicaSet=hcenRS

# Redis
redis.uri=redis://hcen_user:password@redis-host:6379

# Audit Retention
audit.retention.days=90
audit.archive.enabled=true

# Policy Cache
policy.cache.ttl.minutes=5
```

### Network Configuration

**Required Ports**:
- 5432: PostgreSQL
- 27017: MongoDB
- 6379: Redis
- 8080: WildFly HTTP (development)
- 8443: WildFly HTTPS (production)
- 9990: WildFly Management Console

**Firewall Rules**:
- Allow WildFly → PostgreSQL (5432)
- Allow WildFly → MongoDB (27017)
- Allow WildFly → Redis (6379)

### Scheduled Jobs

**EJB @Schedule Tasks**:

1. **Audit Archival Job**:
   - Schedule: Daily at 2:00 AM
   - Task: Archive audit logs older than 90 days to MongoDB
   - Bean: `AuditRetentionService.archiveOldLogs()`

2. **Access Request Expiration Job**:
   - Schedule: Every hour
   - Task: Expire access requests older than 48 hours
   - Bean: `AccessRequestService.expirePendingRequests()`

3. **Audit Integrity Check Job**:
   - Schedule: Weekly on Sunday at 3:00 AM
   - Task: Validate audit log hash chain integrity
   - Bean: `AuditIntegrityService.validateIntegrity()`

### Monitoring and Alerts

**Metrics to Monitor**:
- Audit log write rate (events/second)
- Policy evaluation time (milliseconds)
- Cache hit rate (%)
- Database connection pool usage
- MongoDB archival job success rate

**Alerts**:
- Audit integrity violation detected
- Audit log write failures
- Database connection pool exhausted
- MongoDB archival job failure

### Backup and Disaster Recovery

**PostgreSQL Backups**:
- Full backup: Daily at 1:00 AM
- Retention: 30 days
- Backup policies and audit schemas

**MongoDB Backups**:
- Full backup: Weekly on Saturday
- Retention: 1 year
- Backup audit_logs collection

**Recovery Time Objective (RTO)**: 4 hours
**Recovery Point Objective (RPO)**: 24 hours
```

---

## Summary and Next Steps

### What Has Been Delivered

This comprehensive implementation plan provides:

1. **Complete Architecture Design**
   - Component diagrams for Policy Engine and Audit System
   - Integration flow diagrams
   - Database schemas (PostgreSQL + MongoDB)

2. **Detailed Task Breakdown**
   - 10 specific implementation tasks
   - Estimated time for each task
   - Code samples and implementation guidelines

3. **Implementation Sequence**
   - 5 phases with prioritization
   - Total estimated time: 48 hours (~1 week)

4. **Testing Strategy**
   - Unit tests, integration tests, performance tests
   - Coverage goals and test examples

5. **Infrastructure Requirements**
   - PostgreSQL, MongoDB, Redis configuration
   - WildFly setup, network requirements
   - Monitoring and backup strategies

### Current Status

**Policy Engine**: 60% complete
- Existing: 600 LOC (core evaluators, engine, DTOs)
- Missing: 400 LOC (RBAC, emergency workflow, versioning, analytics)

**Audit System**: 60% complete
- Existing: 722 LOC (core service, repository, DTOs)
- Missing: 700 LOC (immutability, retention, interceptors, export, analytics)

**Total Remaining Work**: ~1,100 LOC + integration/testing

### Recommended Implementation Order

**Week 1: Critical Features**
1. Day 1-2: Audit immutability (hash chain) + retention (MongoDB archival)
2. Day 3: @Audited interceptor for automatic auditing
3. Day 4: Emergency access workflow + notifications
4. Day 5: RBAC integration + policy versioning

**Week 2: Analytics and Polish**
1. Day 1: Audit export API (CSV/JSON)
2. Day 2: Advanced analytics and anomaly detection
3. Day 3: Policy conflict analytics
4. Day 4-5: Integration testing, performance optimization, documentation

### Key Success Metrics

- **Policy Engine**: Sub-100ms evaluation time, 90%+ cache hit rate
- **Audit System**: 1000+ events/second write throughput, zero hash chain violations
- **Code Coverage**: 80%+ overall, 90%+ for policy/audit modules
- **Integration**: All RNDC operations automatically audited, all policy evaluations logged

### Files to Create/Modify

**New Files** (~15 files):
- RolePolicyEvaluator.java
- EmergencyAccessService.java
- EmergencyAccessReview.java (entity)
- PolicyAnalyticsService.java
- PolicyConflictReport.java (DTO)
- PolicyVersion.java (entity)
- PolicyVersionRepository.java
- PolicyTemplateService.java
- AuditIntegrityService.java
- AuditIntegrityReport.java (DTO)
- AuditRetentionService.java
- AuditLogArchiveRepository.java
- AuditedInterceptor.java
- AuditExportService.java
- AuditAnalyticsService.java

**Modified Files** (~8 files):
- PolicyEngine.java (integrate new evaluators)
- AuditLog.java (add hash fields)
- AuditService.java (hash computation)
- RndcService.java (policy enforcement)
- PolicyResource.java (new endpoints)
- AuditResource.java (new endpoints)
- beans.xml (enable interceptor)
- V006__enhance_policies_and_audit.sql (new migration)

---

## Contact and Support

For questions or clarifications on this implementation plan:

**Team**: TSE 2025 Group 9
**Project**: HCEN - Historia Clínica Electrónica Nacional

---

**End of Implementation Plan**

This plan is ready for execution. Each task has clear objectives, implementation details, database schemas, and testing strategies. Follow the phases sequentially for optimal results.
