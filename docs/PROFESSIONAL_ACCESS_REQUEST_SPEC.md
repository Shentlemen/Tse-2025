# Professional Access Request Creation Feature - Technical Specification

**Project**: hcen.uy - Historia Clínica Electrónica Nacional
**Component**: HCEN Central
**Version**: 1.0
**Date**: 2025-11-13
**Authors**: TSE 2025 Group 9

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Current State Analysis](#2-current-state-analysis)
3. [Requirements](#3-requirements)
4. [Proposed Solution](#4-proposed-solution)
5. [Technical Design](#5-technical-design)
6. [Implementation Plan](#6-implementation-plan)
7. [Testing Strategy](#7-testing-strategy)
8. [Security Considerations](#8-security-considerations)
9. [Open Questions](#9-open-questions)
10. [Success Criteria](#10-success-criteria)
11. [Future Enhancements](#11-future-enhancements)

---

## 1. Executive Summary

### 1.1 Overview

This specification defines the implementation of the **Professional Access Request Creation** feature for HCEN Central. This feature enables healthcare professionals (via peripheral nodes) to explicitly request access to patient documents when policy evaluation results in a PENDING decision.

### 1.2 Problem Statement

**Current Gap**: While the system has complete infrastructure for patients to approve/deny access requests through the patient portal, there is no mechanism for professionals to CREATE these access requests.

**Missing Link**:
- **What exists**: AccessRequest entity, patient approval/denial endpoints, database schema
- **What's missing**: API endpoint for peripheral nodes to create access requests when professionals need document access
- **Impact**: The access request workflow cannot function end-to-end

### 1.3 Business Value

- **Compliance**: Enables patient consent management as required by Ley 18.331 (Uruguay Data Protection Law)
- **Patient Control**: Empowers patients to control who accesses their medical records
- **Traceability**: Creates comprehensive audit trail of all access requests
- **Professional Experience**: Provides clear workflow when access is not automatically granted

### 1.4 Architectural Context

```
┌──────────────────────┐           ┌──────────────────────┐
│  Peripheral Node     │           │    HCEN Central      │
│  (Clinic/Provider)   │           │                      │
│                      │           │                      │
│  Professional        │  HTTPS    │  ┌────────────────┐  │
│  requests access ────┼──────────>│  │ PolicyEngine   │  │
│                      │           │  │ evaluates      │  │
│                      │           │  │ -> PENDING     │  │
│                      │           │  └────────────────┘  │
│                      │           │         │            │
│                      │           │         ▼            │
│                      │  MISSING  │  ┌────────────────┐  │
│  ??? Create      ────┼──────────>│  │ Create         │  │ <- NEW
│  AccessRequest       │  ENDPOINT │  │ AccessRequest  │  │
│                      │           │  └────────────────┘  │
│                      │           │         │            │
│                      │           │         ▼            │
│                      │           │  ┌────────────────┐  │
│                      │           │  │ Patient Portal │  │
│                      │           │  │ (existing)     │  │
│  Patient ────────────┼──────────>│  │ Approve/Deny   │  │
│  approves/denies     │           │  └────────────────┘  │
└──────────────────────┘           └──────────────────────┘
```

**Key Point**: HCEN Central exposes REST APIs for peripheral nodes. Professional authentication/authorization happens at the peripheral node level. HCEN Central receives authenticated requests from registered clinics.

---

## 2. Current State Analysis

### 2.1 What's Already Implemented

#### 2.1.1 Database Schema

**Table**: `policies.access_requests`

**Existing Fields**:
- `id` (BIGSERIAL PRIMARY KEY) - Auto-generated request ID
- `professional_id` (VARCHAR(100) NOT NULL) - Professional identifier
- `patient_ci` (VARCHAR(20) NOT NULL) - Patient's national ID
- `document_id` (BIGINT) - Optional document ID reference
- `request_reason` (TEXT) - Reason provided by professional
- `status` (VARCHAR(20) NOT NULL) - PENDING, APPROVED, DENIED, EXPIRED
- `requested_at` (TIMESTAMP NOT NULL) - Creation timestamp
- `responded_at` (TIMESTAMP) - When patient responded
- `patient_response` (TEXT) - Patient's response explanation
- `expires_at` (TIMESTAMP NOT NULL) - When request expires (default: +48 hours)

**Existing Indexes**:
- `idx_access_requests_professional_id`
- `idx_access_requests_patient_ci`
- `idx_access_requests_status`
- `idx_access_requests_requested_at`
- `idx_access_requests_expires_at`
- `idx_access_requests_patient_ci_status` (composite)
- `idx_access_requests_status_expires_at` (composite)

**Analysis**: Database schema is complete and well-indexed. No schema changes required.

#### 2.1.2 Entity Layer

**File**: `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\policy\entity\AccessRequest.java`

**Key Features**:
- Complete JPA entity with all fields mapped
- `RequestStatus` enum (PENDING, APPROVED, DENIED, EXPIRED)
- Business methods: `approve()`, `deny()`, `expire()`, `isPending()`, `isExpired()`
- Default expiration: 48 hours from creation
- Pre-persist callback for initialization
- Constructors with required fields

**Analysis**: Entity is complete and production-ready. No changes needed.

#### 2.1.3 Repository Layer

**File**: `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\policy\repository\AccessRequestRepository.java`

**Existing Methods**:
- `findById(Long id)` - Find by ID
- `findByPatientCi(String patientCi, int page, int size)` - Patient's requests
- `findByPatientCiAndStatus(String patientCi, RequestStatus status, int page, int size)` - Filtered by status
- `countPendingByPatientCi(String patientCi)` - Count pending requests
- `update(AccessRequest request)` - Update existing request

**Missing Methods**:
- ✗ `save(AccessRequest request)` - Create new request
- ✗ `findByProfessionalIdAndPatientCiAndStatus()` - Check for existing pending requests (deduplication)

**Analysis**: Repository needs `save()` method and deduplication query.

#### 2.1.4 Service Layer

**File**: `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\policy\service\AccessRequestService.java`

**Existing Methods**:
- `getAccessRequests()` - Get patient's requests with pagination
- `countPendingRequests()` - Count pending for patient
- `approveRequest()` - Patient approves request
- `denyRequest()` - Patient denies request
- `requestMoreInfo()` - Patient requests more information

**Missing Methods**:
- ✗ `createAccessRequest()` - Create new access request

**Analysis**: Service has all patient-facing methods. Needs professional-facing creation method.

#### 2.1.5 REST API Layer

**File**: `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\policy\api\rest\AccessRequestResource.java`

**Base Path**: `/api/access-requests`

**Existing Endpoints**:
- `GET /api/access-requests?patientCi={ci}&status={status}` - List requests
- `POST /api/access-requests/{requestId}/approve` - Approve request
- `POST /api/access-requests/{requestId}/deny` - Deny request
- `POST /api/access-requests/{requestId}/request-info` - Request more info

**Missing Endpoints**:
- ✗ `POST /api/access-requests` - Create access request

**Analysis**: All patient-facing endpoints exist. Missing professional-facing creation endpoint.

#### 2.1.6 DTO Layer

**Existing DTOs**:
- `AccessRequestDTO` - Response DTO for displaying requests
- `AccessRequestListResponse` - Paginated list response
- `ApprovalDecisionDTO` - Approval decision payload
- `DenialDecisionDTO` - Denial decision payload
- `InfoRequestDTO` - Information request payload

**Missing DTOs**:
- ✗ `AccessRequestCreationDTO` - Request DTO for creating access requests
- ✗ `AccessRequestCreationResponseDTO` - Response DTO for creation endpoint

**Analysis**: DTOs exist for patient operations. Need creation-specific DTOs.

### 2.2 What's Missing

**Summary of Gaps**:

| Layer | Missing Component | Priority |
|-------|-------------------|----------|
| Repository | `save()` method | HIGH |
| Repository | Deduplication query | HIGH |
| Service | `createAccessRequest()` method | HIGH |
| REST API | `POST /api/access-requests` endpoint | HIGH |
| DTO | `AccessRequestCreationDTO` | HIGH |
| DTO | `AccessRequestCreationResponseDTO` | HIGH |
| Validation | Professional ID format validation | MEDIUM |
| Validation | Clinic authorization check | MEDIUM |
| Integration | Auto-creation on PENDING policy decision | LOW |

### 2.3 Architecture Context

**HCEN Central vs Peripheral Separation**:

- **HCEN Central** (this implementation): Stores AccessRequest entities, manages patient approvals, provides REST APIs
- **Peripheral Nodes** (clinic/provider): Authenticate professionals, make API calls to HCEN Central on behalf of professionals
- **Communication**: Peripheral nodes use API keys to authenticate with HCEN Central

**Authentication Flow**:
1. Professional logs into Peripheral Node (clinic-internal authentication)
2. Peripheral Node validates professional credentials
3. Peripheral Node makes API call to HCEN Central with clinic API key
4. HCEN Central validates clinic API key
5. HCEN Central creates AccessRequest

**Important**: HCEN Central does NOT authenticate individual professionals. It trusts registered peripheral nodes.

---

## 3. Requirements

### 3.1 Functional Requirements

#### FR-1: Professional Access Request Creation
**Description**: Professionals (via peripheral nodes) must be able to request access to patient documents.

**Actor**: Healthcare Professional (via Peripheral Node)

**Preconditions**:
- Professional is authenticated at peripheral node
- Peripheral node is registered with HCEN Central
- Patient exists in INUS
- Document exists in RNDC (if requesting specific document)

**Flow**:
1. Professional attempts to access patient document at peripheral node
2. Peripheral node checks local permissions (fail)
3. Peripheral node queries HCEN Central for document metadata
4. HCEN Central PolicyEngine evaluates policies → PENDING
5. Peripheral node calls `POST /api/access-requests` to create request
6. HCEN Central validates request
7. HCEN Central creates AccessRequest entity
8. HCEN Central returns request ID and expiration time
9. Patient sees pending request in portal
10. Patient approves/denies request
11. Professional receives notification of decision

**Postconditions**:
- AccessRequest entity created in database with status PENDING
- Request visible in patient portal
- Audit log entry created
- Request auto-expires after 48 hours if not responded

#### FR-2: Access Request Data Storage
**Description**: Access requests must be stored persistently in the database.

**Data to Store**:
- Professional identifier (from peripheral node)
- Professional name (for patient display)
- Professional specialty (optional)
- Clinic identifier (from API key)
- Clinic name (for patient display)
- Patient CI (validated against INUS)
- Document ID (optional, validated against RNDC if provided)
- Document type (for patient display)
- Request reason (required, max 500 chars)
- Request urgency (ROUTINE, URGENT, EMERGENCY)
- Creation timestamp
- Expiration timestamp (default: created_at + 48 hours)
- Status (PENDING on creation)

#### FR-3: Patient Notification
**Description**: Patients must be notified when new access requests are created.

**Notification Methods** (Phase 1 - MVP):
- Web portal badge counter update (increment pending count)
- Request appears in patient's pending requests list

**Future Enhancements**:
- Email notification
- SMS notification
- Mobile push notification (Firebase)

#### FR-4: Request Expiration
**Description**: Access requests must automatically expire after 48 hours if patient does not respond.

**Behavior**:
- Default expiration: 48 hours from creation
- Expired requests cannot be approved/denied
- Status changes to EXPIRED automatically
- Expired requests remain in audit trail
- Professional can create new request after expiration

#### FR-5: Audit Trail
**Description**: All access request creation attempts must be logged.

**Logged Events**:
- Successful request creation
- Failed request creation (with reason)
- Duplicate request attempts
- Validation failures
- Patient not found
- Document not found
- Unauthorized clinic attempts

**Audit Data**:
- Timestamp
- Professional ID
- Clinic ID
- Patient CI
- Document ID
- Action outcome (SUCCESS, FAILURE)
- Failure reason (if applicable)

#### FR-6: Request Deduplication
**Description**: System must prevent duplicate pending requests for same professional/patient/document combination.

**Behavior**:
- Before creating new request, check for existing PENDING request
- Criteria: same professionalId + patientCi + documentId + status=PENDING
- If duplicate found: Return existing request (idempotent response)
- If no duplicate: Create new request

**Rationale**: Prevent spam, reduce database clutter, provide idempotent API

### 3.2 Non-Functional Requirements

#### NFR-1: Performance
**Target**: Response time < 500ms for request creation (95th percentile)

**Metrics**:
- Database insert: < 50ms
- Validation logic: < 100ms
- Audit logging: < 50ms (async if possible)
- Total end-to-end: < 500ms

**Optimization Strategies**:
- Use prepared statements
- Leverage existing indexes
- Async audit logging
- Connection pooling

#### NFR-2: Availability
**Target**: 99.9% uptime for access request creation endpoint

**Strategies**:
- Graceful error handling
- Transaction rollback on failure
- No external dependencies (self-contained operation)
- Database connection pool management

#### NFR-3: Scalability
**Target**: Support 100 concurrent request creations without degradation

**Considerations**:
- Stateless service design (already implemented)
- Database connection pool sizing
- Index optimization for concurrent inserts
- Transaction isolation levels

#### NFR-4: Data Integrity
**Requirements**:
- All request creations must be transactional
- Failed requests must not leave partial data
- Foreign key constraints must be enforced
- Status transitions must follow valid state machine

**Validation**:
- Patient CI must exist in INUS
- Document ID must exist in RNDC (if provided)
- Clinic ID must be registered
- All required fields must be non-null

#### NFR-5: Security
**Requirements**:
- Clinic API key authentication required
- Input validation and sanitization
- No SQL injection vulnerabilities
- No sensitive data in logs (truncate patient CI)
- HTTPS only (AC002-AC004)

#### NFR-6: Idempotency
**Requirements**:
- Multiple identical requests must produce same result
- No duplicate AccessRequest entities for same context
- Return existing request if duplicate detected
- HTTP 200 (not 201) when returning existing request

---

## 4. Proposed Solution

### 4.1 Solution Overview

**Recommended Approach**: **Hybrid Model** (Option 3)

Implement both explicit creation endpoint AND auto-creation on policy evaluation, providing maximum flexibility and backward compatibility.

### 4.2 Option Comparison

#### Option 1: Auto-create on PENDING Decision

**Description**: Automatically create AccessRequest when PolicyEngine returns PENDING

**Implementation**:
```java
// In PolicyEngine or ClinicalHistoryService
if (policyDecision.isPending()) {
    // Extract professional context from request
    AccessRequestCreationDTO autoRequest = new AccessRequestCreationDTO(
        professionalId, professionalName, specialty, clinicId, clinicName,
        patientCi, documentId, documentType,
        "Automatic request from policy evaluation",
        UrgencyLevel.ROUTINE
    );
    accessRequestService.createAccessRequest(autoRequest);
}
```

**Pros**:
- Seamless integration with existing policy flow
- No changes needed in peripheral nodes
- Automatic and invisible to professionals
- Consistent behavior across all policy evaluations

**Cons**:
- Requires professional context in policy evaluation calls
- Couples policy evaluation with request creation
- Less explicit control from peripheral nodes
- Harder to test in isolation

**Use Cases**:
- Document retrieval attempts
- Policy evaluation during access checks
- Automatic request creation without explicit API call

#### Option 2: Explicit REST Endpoint

**Description**: Create dedicated endpoint for peripheral nodes to create access requests

**Endpoint**: `POST /api/access-requests`

**Implementation**:
```java
@POST
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response createAccessRequest(
    @Valid AccessRequestCreationDTO request,
    @Context SecurityContext securityContext) {

    // Validate clinic authentication
    String clinicId = extractClinicId(securityContext);

    // Create request
    AccessRequestCreationResponseDTO response =
        accessRequestService.createAccessRequest(request, clinicId);

    // Return 201 Created or 200 OK (if duplicate)
    return Response.status(response.isNewRequest() ? 201 : 200)
        .entity(response)
        .build();
}
```

**Pros**:
- Clear separation of concerns
- Explicit control from peripheral nodes
- Easy to test and debug
- RESTful design
- Flexible - can be called from any context

**Cons**:
- Requires peripheral nodes to implement the call
- Additional endpoint to maintain
- Two-step process (policy check + request creation)

**Use Cases**:
- Professional explicitly requests access
- Peripheral node has custom authorization logic
- Batch request creation
- Manual intervention scenarios

#### Option 3: Hybrid Approach (RECOMMENDED)

**Description**: Implement both Option 1 and Option 2 for maximum flexibility

**Components**:
1. **Explicit Endpoint**: `POST /api/access-requests` (Option 2)
2. **Auto-creation**: Optional auto-creation on PENDING (Option 1)
3. **Deduplication**: Unified deduplication logic prevents duplicates
4. **Configuration**: Feature flag to enable/disable auto-creation

**Architecture**:
```
┌─────────────────────────────────────────────────────────┐
│                  AccessRequestService                   │
│                                                         │
│  ┌───────────────────────────────────────────────────┐ │
│  │  createAccessRequest(request, clinicId)           │ │
│  │  - Validates request data                         │ │
│  │  - Checks for duplicates                          │ │
│  │  - Creates or returns existing request            │ │
│  └───────────────────────────────────────────────────┘ │
│                          ▲                              │
│                          │                              │
│         ┌────────────────┴────────────────┐             │
│         │                                 │             │
│  ┌──────┴──────┐                  ┌──────┴──────┐      │
│  │  REST API   │                  │ Policy      │      │
│  │  Endpoint   │                  │ Integration │      │
│  │  (Explicit) │                  │ (Auto)      │      │
│  └─────────────┘                  └─────────────┘      │
└─────────────────────────────────────────────────────────┘
```

**Benefits**:
- Flexibility for different integration scenarios
- Backward compatible with future enhancements
- Supports both push (peripheral) and pull (HCEN internal) models
- Deduplication prevents double-creation

**Drawbacks**:
- More code to maintain
- More testing scenarios
- Potential confusion about which method to use

**Recommendation**: Implement hybrid approach with clear documentation on when to use each method.

### 4.3 Selected Solution: Hybrid Approach

**Phase 1 (MVP - This Specification)**:
- ✅ Implement explicit REST endpoint (`POST /api/access-requests`)
- ✅ Implement core service method (`createAccessRequest()`)
- ✅ Implement deduplication logic
- ✅ Implement validation and audit logging
- ⏸️ Auto-creation on PENDING (defer to Phase 2)

**Phase 2 (Future Enhancement)**:
- Add auto-creation when PolicyEngine returns PENDING
- Add feature flag to enable/disable auto-creation
- Add configuration for auto-creation behavior

**Rationale**:
- Start with explicit endpoint (simpler, clearer)
- Add auto-creation later when peripheral integration is mature
- Deduplication logic will work for both approaches

---

## 5. Technical Design

### 5.1 Database Schema

**Table**: `policies.access_requests` (already exists)

**Current Schema**: ✅ Complete - no changes needed

**Missing Fields Analysis**:
- `professional_name` - ❌ Not in schema, but should be included for patient display
- `specialty` - ❌ Not in schema, but useful for patient context
- `clinic_id` - ❌ Not in schema, but needed to track which clinic made request
- `clinic_name` - ❌ Not in schema, but needed for patient display
- `document_type` - ❌ Not in schema, but useful for patient context
- `urgency` - ❌ Not in schema, but useful for prioritization

**Schema Migration Required**: YES

**Proposed Migration**: `V004__add_access_request_fields.sql`

```sql
-- Add professional and clinic context fields to access_requests table
ALTER TABLE policies.access_requests
    ADD COLUMN IF NOT EXISTS professional_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS specialty VARCHAR(100),
    ADD COLUMN IF NOT EXISTS clinic_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS clinic_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS document_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS urgency VARCHAR(20) DEFAULT 'ROUTINE'
        CHECK (urgency IN ('ROUTINE', 'URGENT', 'EMERGENCY'));

-- Create index for clinic_id queries
CREATE INDEX IF NOT EXISTS idx_access_requests_clinic_id
    ON policies.access_requests(clinic_id);

-- Create composite index for deduplication query
CREATE INDEX IF NOT EXISTS idx_access_requests_dedup
    ON policies.access_requests(professional_id, patient_ci, document_id, status);

-- Add comments
COMMENT ON COLUMN policies.access_requests.professional_name IS 'Professional full name for patient display';
COMMENT ON COLUMN policies.access_requests.specialty IS 'Professional specialty (e.g., CARDIOLOGY, PEDIATRICS)';
COMMENT ON COLUMN policies.access_requests.clinic_id IS 'Clinic/provider identifier that submitted the request';
COMMENT ON COLUMN policies.access_requests.clinic_name IS 'Clinic name for patient display';
COMMENT ON COLUMN policies.access_requests.document_type IS 'Document type for patient display';
COMMENT ON COLUMN policies.access_requests.urgency IS 'Request urgency level (ROUTINE, URGENT, EMERGENCY)';
```

### 5.2 Entity Updates

**File**: `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\policy\entity\AccessRequest.java`

**Changes Required**:

```java
// Add new fields
@Column(name = "professional_name", length = 255)
private String professionalName;

@Column(name = "specialty", length = 100)
private String specialty;

@Column(name = "clinic_id", length = 100)
private String clinicId;

@Column(name = "clinic_name", length = 255)
private String clinicName;

@Column(name = "document_type", length = 50)
private String documentType;

@Enumerated(EnumType.STRING)
@Column(name = "urgency", length = 20)
private UrgencyLevel urgency = UrgencyLevel.ROUTINE;

// Add enum
public enum UrgencyLevel {
    ROUTINE,
    URGENT,
    EMERGENCY
}

// Update constructors
public AccessRequest(String professionalId, String professionalName, String specialty,
                     String clinicId, String clinicName, String patientCi,
                     Long documentId, String documentType, String requestReason,
                     UrgencyLevel urgency) {
    this();
    this.professionalId = professionalId;
    this.professionalName = professionalName;
    this.specialty = specialty;
    this.clinicId = clinicId;
    this.clinicName = clinicName;
    this.patientCi = patientCi;
    this.documentId = documentId;
    this.documentType = documentType;
    this.requestReason = requestReason;
    this.urgency = urgency != null ? urgency : UrgencyLevel.ROUTINE;
}

// Add getters/setters for new fields
```

### 5.3 Repository Updates

**File**: `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\policy\repository\AccessRequestRepository.java`

**New Methods Required**:

```java
/**
 * Save a new access request
 *
 * @param request Access request to save
 * @return Saved access request with generated ID
 */
AccessRequest save(AccessRequest request);

/**
 * Find existing pending request for deduplication
 *
 * @param professionalId Professional ID
 * @param patientCi Patient CI
 * @param documentId Document ID (nullable)
 * @return Optional existing pending request
 */
Optional<AccessRequest> findPendingRequest(
    String professionalId,
    String patientCi,
    Long documentId
);
```

**Implementation** (in `AccessRequestRepositoryImpl.java`):

```java
@Override
public AccessRequest save(AccessRequest request) {
    entityManager.persist(request);
    entityManager.flush();
    return request;
}

@Override
public Optional<AccessRequest> findPendingRequest(
        String professionalId, String patientCi, Long documentId) {

    String jpql = "SELECT ar FROM AccessRequest ar " +
                  "WHERE ar.professionalId = :professionalId " +
                  "AND ar.patientCi = :patientCi " +
                  "AND ar.status = :status " +
                  "AND ar.expiresAt > :now ";

    if (documentId != null) {
        jpql += "AND ar.documentId = :documentId";
    } else {
        jpql += "AND ar.documentId IS NULL";
    }

    TypedQuery<AccessRequest> query = entityManager.createQuery(jpql, AccessRequest.class)
        .setParameter("professionalId", professionalId)
        .setParameter("patientCi", patientCi)
        .setParameter("status", RequestStatus.PENDING)
        .setParameter("now", LocalDateTime.now());

    if (documentId != null) {
        query.setParameter("documentId", documentId);
    }

    try {
        return Optional.of(query.getSingleResult());
    } catch (NoResultException e) {
        return Optional.empty();
    }
}
```

### 5.4 DTO Definitions

#### 5.4.1 AccessRequestCreationDTO (Request)

**File**: `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\policy\dto\AccessRequestCreationDTO.java`

```java
package uy.gub.hcen.policy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating access requests
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class AccessRequestCreationDTO {

    @NotBlank(message = "Professional ID is required")
    @Size(max = 100, message = "Professional ID must not exceed 100 characters")
    private String professionalId;

    @Size(max = 255, message = "Professional name must not exceed 255 characters")
    private String professionalName;

    @Size(max = 100, message = "Specialty must not exceed 100 characters")
    private String specialty;

    @NotBlank(message = "Patient CI is required")
    @Size(max = 20, message = "Patient CI must not exceed 20 characters")
    private String patientCi;

    private Long documentId;

    @Size(max = 50, message = "Document type must not exceed 50 characters")
    private String documentType;

    @NotBlank(message = "Request reason is required")
    @Size(max = 500, message = "Request reason must not exceed 500 characters")
    private String requestReason;

    private String urgency; // ROUTINE, URGENT, EMERGENCY

    // Constructors, getters, setters

    public AccessRequestCreationDTO() {
    }

    // Builder pattern for easier construction
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AccessRequestCreationDTO dto = new AccessRequestCreationDTO();

        public Builder professionalId(String professionalId) {
            dto.professionalId = professionalId;
            return this;
        }

        public Builder professionalName(String professionalName) {
            dto.professionalName = professionalName;
            return this;
        }

        public Builder specialty(String specialty) {
            dto.specialty = specialty;
            return this;
        }

        public Builder patientCi(String patientCi) {
            dto.patientCi = patientCi;
            return this;
        }

        public Builder documentId(Long documentId) {
            dto.documentId = documentId;
            return this;
        }

        public Builder documentType(String documentType) {
            dto.documentType = documentType;
            return this;
        }

        public Builder requestReason(String requestReason) {
            dto.requestReason = requestReason;
            return this;
        }

        public Builder urgency(String urgency) {
            dto.urgency = urgency;
            return this;
        }

        public AccessRequestCreationDTO build() {
            return dto;
        }
    }

    // Standard getters and setters
    public String getProfessionalId() { return professionalId; }
    public void setProfessionalId(String professionalId) { this.professionalId = professionalId; }

    public String getProfessionalName() { return professionalName; }
    public void setProfessionalName(String professionalName) { this.professionalName = professionalName; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getPatientCi() { return patientCi; }
    public void setPatientCi(String patientCi) { this.patientCi = patientCi; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getRequestReason() { return requestReason; }
    public void setRequestReason(String requestReason) { this.requestReason = requestReason; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
}
```

#### 5.4.2 AccessRequestCreationResponseDTO (Response)

**File**: `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\policy\dto\AccessRequestCreationResponseDTO.java`

```java
package uy.gub.hcen.policy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * DTO for access request creation response
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-13
 */
public class AccessRequestCreationResponseDTO {

    private Long requestId;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    private String message;
    private Boolean isNewRequest; // true if newly created, false if duplicate

    // Constructors

    public AccessRequestCreationResponseDTO() {
    }

    public AccessRequestCreationResponseDTO(Long requestId, String status,
                                           LocalDateTime createdAt, LocalDateTime expiresAt,
                                           String message, Boolean isNewRequest) {
        this.requestId = requestId;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.message = message;
        this.isNewRequest = isNewRequest;
    }

    // Factory method
    public static AccessRequestCreationResponseDTO fromEntity(AccessRequest request, boolean isNew) {
        String message = isNew
            ? "Access request created successfully. Patient will be notified."
            : "An identical pending request already exists. Returning existing request.";

        return new AccessRequestCreationResponseDTO(
            request.getId(),
            request.getStatus().name(),
            request.getRequestedAt(),
            request.getExpiresAt(),
            message,
            isNew
        );
    }

    // Getters and setters

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getIsNewRequest() { return isNewRequest; }
    public void setIsNewRequest(Boolean isNewRequest) { this.isNewRequest = isNewRequest; }
}
```

### 5.5 Service Layer Implementation

**File**: `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\policy\service\AccessRequestService.java`

**New Method**:

```java
/**
 * Create a new access request
 *
 * @param requestDTO Request creation data
 * @param clinicId Clinic ID (extracted from API key)
 * @param clinicName Clinic name (looked up from registry)
 * @return Creation response with request ID and status
 * @throws IllegalArgumentException if validation fails
 */
@Transactional
public AccessRequestCreationResponseDTO createAccessRequest(
        AccessRequestCreationDTO requestDTO,
        String clinicId,
        String clinicName) {

    LOGGER.log(Level.INFO,
        "Creating access request: professional={0}, patient={1}, document={2}, clinic={3}",
        new Object[]{requestDTO.getProfessionalId(), requestDTO.getPatientCi(),
                     requestDTO.getDocumentId(), clinicId});

    try {
        // 1. Validate patient exists in INUS
        if (!inusService.existsByCI(requestDTO.getPatientCi())) {
            LOGGER.log(Level.WARNING, "Patient not found in INUS: {0}",
                requestDTO.getPatientCi());
            throw new IllegalArgumentException(
                "Patient not found: " + requestDTO.getPatientCi());
        }

        // 2. Validate document exists in RNDC (if documentId provided)
        if (requestDTO.getDocumentId() != null) {
            if (!rndcService.existsById(requestDTO.getDocumentId())) {
                LOGGER.log(Level.WARNING, "Document not found in RNDC: {0}",
                    requestDTO.getDocumentId());
                throw new IllegalArgumentException(
                    "Document not found: " + requestDTO.getDocumentId());
            }
        }

        // 3. Check for existing pending request (deduplication)
        Optional<AccessRequest> existingRequest =
            accessRequestRepository.findPendingRequest(
                requestDTO.getProfessionalId(),
                requestDTO.getPatientCi(),
                requestDTO.getDocumentId()
            );

        if (existingRequest.isPresent()) {
            LOGGER.log(Level.INFO,
                "Duplicate request detected, returning existing request: {0}",
                existingRequest.get().getId());

            // Log duplicate attempt
            auditService.logAccessEvent(
                requestDTO.getProfessionalId(),
                "PROFESSIONAL",
                "ACCESS_REQUEST",
                existingRequest.get().getId().toString(),
                ActionOutcome.SUCCESS,
                null,
                null,
                Map.of("action", "DUPLICATE_REQUEST_DETECTED")
            );

            // Return existing request (idempotent)
            return AccessRequestCreationResponseDTO.fromEntity(
                existingRequest.get(), false);
        }

        // 4. Parse urgency level
        AccessRequest.UrgencyLevel urgency = AccessRequest.UrgencyLevel.ROUTINE;
        if (requestDTO.getUrgency() != null && !requestDTO.getUrgency().isEmpty()) {
            try {
                urgency = AccessRequest.UrgencyLevel.valueOf(
                    requestDTO.getUrgency().toUpperCase());
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Invalid urgency level: {0}, using ROUTINE",
                    requestDTO.getUrgency());
            }
        }

        // 5. Create new AccessRequest entity
        AccessRequest newRequest = new AccessRequest(
            requestDTO.getProfessionalId(),
            requestDTO.getProfessionalName(),
            requestDTO.getSpecialty(),
            clinicId,
            clinicName,
            requestDTO.getPatientCi(),
            requestDTO.getDocumentId(),
            requestDTO.getDocumentType(),
            requestDTO.getRequestReason(),
            urgency
        );

        // 6. Save to database
        AccessRequest savedRequest = accessRequestRepository.save(newRequest);

        // 7. Log creation in audit system
        auditService.logAccessEvent(
            requestDTO.getProfessionalId(),
            "PROFESSIONAL",
            "ACCESS_REQUEST",
            savedRequest.getId().toString(),
            ActionOutcome.SUCCESS,
            null,
            null,
            Map.of(
                "action", "REQUEST_CREATED",
                "patientCi", requestDTO.getPatientCi(),
                "documentId", String.valueOf(requestDTO.getDocumentId()),
                "clinicId", clinicId,
                "urgency", urgency.name()
            )
        );

        LOGGER.log(Level.INFO, "Access request created successfully: {0}",
            savedRequest.getId());

        // 8. Return success response
        return AccessRequestCreationResponseDTO.fromEntity(savedRequest, true);

    } catch (IllegalArgumentException e) {
        // Validation error - log and rethrow
        LOGGER.log(Level.WARNING, "Validation error creating access request", e);
        auditService.logAccessEvent(
            requestDTO.getProfessionalId(),
            "PROFESSIONAL",
            "ACCESS_REQUEST",
            null,
            ActionOutcome.FAILURE,
            null,
            null,
            Map.of("error", e.getMessage())
        );
        throw e;

    } catch (Exception e) {
        // Unexpected error
        LOGGER.log(Level.SEVERE, "Error creating access request", e);
        auditService.logAccessEvent(
            requestDTO.getProfessionalId(),
            "PROFESSIONAL",
            "ACCESS_REQUEST",
            null,
            ActionOutcome.FAILURE,
            null,
            null,
            Map.of("error", e.getMessage())
        );
        throw new RuntimeException("Failed to create access request", e);
    }
}
```

**Dependencies to Inject**:
```java
@Inject
private InusService inusService; // For patient validation

@Inject
private RndcService rndcService; // For document validation
```

### 5.6 REST API Implementation

**File**: `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\policy\api\rest\AccessRequestResource.java`

**New Endpoint**:

```java
// ================================================================
// POST /api/access-requests - Create Access Request
// ================================================================

/**
 * Creates a new access request from a professional/clinic.
 *
 * This endpoint is called by peripheral nodes when a professional
 * needs access to a patient's documents but doesn't have explicit
 * permission via policies.
 *
 * Request Body:
 * - AccessRequestCreationDTO with professional/patient/document info
 *
 * @param request Access request creation data
 * @param securityContext Security context (contains clinic ID from API key)
 * @return 201 Created with new request details
 *         200 OK if duplicate request (idempotent)
 *         400 Bad Request if validation fails
 *         401 Unauthorized if clinic not authenticated
 *         404 Not Found if patient/document not found
 *         500 Internal Server Error if operation fails
 */
@POST
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response createAccessRequest(
        @Valid AccessRequestCreationDTO request,
        @Context SecurityContext securityContext) {

    LOGGER.log(Level.INFO, "POST create access request: professional={0}, patient={1}",
            new Object[]{request.getProfessionalId(), request.getPatientCi()});

    try {
        // Extract clinic ID from SecurityContext (set by API key authentication filter)
        String clinicId = securityContext.getUserPrincipal() != null ?
                securityContext.getUserPrincipal().getName() : null;

        if (clinicId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ErrorResponse.unauthorized("Clinic authentication required"))
                    .build();
        }

        // TODO: Look up clinic name from clinic registry
        // For now, use clinic ID as name
        String clinicName = "Clinic " + clinicId;

        // Create access request
        AccessRequestCreationResponseDTO response =
                accessRequestService.createAccessRequest(request, clinicId, clinicName);

        // Determine status code
        int statusCode = response.getIsNewRequest() ?
                Response.Status.CREATED.getStatusCode() :
                Response.Status.OK.getStatusCode();

        LOGGER.log(Level.INFO, "Access request created: requestId={0}, isNew={1}",
                new Object[]{response.getRequestId(), response.getIsNewRequest()});

        return Response.status(statusCode)
                .entity(response)
                .build();

    } catch (IllegalArgumentException e) {
        LOGGER.log(Level.WARNING, "Invalid access request creation", e);
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(ErrorResponse.validationError(e.getMessage()))
                .build();

    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error creating access request", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponse.internalServerError(
                    "Failed to create access request: " + e.getMessage()))
                .build();
    }
}
```

### 5.7 API Documentation

**OpenAPI/Swagger Specification**:

```yaml
paths:
  /api/access-requests:
    post:
      summary: Create Access Request
      description: |
        Creates a new access request for a professional to access patient documents.
        Called by peripheral nodes when professionals need access but lack explicit permissions.

        Returns existing request if identical pending request found (idempotent).
      tags:
        - Access Requests
      security:
        - ClinicApiKey: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AccessRequestCreationDTO'
            examples:
              specific-document:
                summary: Request for specific document
                value:
                  professionalId: "prof-12345"
                  professionalName: "Dr. María García"
                  specialty: "CARDIOLOGY"
                  patientCi: "12345678"
                  documentId: 456
                  documentType: "LAB_RESULT"
                  requestReason: "Evaluación de control cardiológico del paciente"
                  urgency: "ROUTINE"
              general-access:
                summary: General access request
                value:
                  professionalId: "prof-67890"
                  professionalName: "Dr. Juan Pérez"
                  specialty: "PEDIATRICS"
                  patientCi: "87654321"
                  requestReason: "Consulta de emergencia"
                  urgency: "URGENT"
      responses:
        '201':
          description: Access request created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AccessRequestCreationResponseDTO'
              example:
                requestId: 789
                status: "PENDING"
                createdAt: "2025-11-13T15:30:00"
                expiresAt: "2025-11-15T15:30:00"
                message: "Access request created successfully. Patient will be notified."
                isNewRequest: true
        '200':
          description: Duplicate request - returning existing pending request
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AccessRequestCreationResponseDTO'
              example:
                requestId: 789
                status: "PENDING"
                createdAt: "2025-11-13T15:30:00"
                expiresAt: "2025-11-15T15:30:00"
                message: "An identical pending request already exists."
                isNewRequest: false
        '400':
          description: Invalid request data
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              examples:
                missing-field:
                  value:
                    error: "VALIDATION_ERROR"
                    message: "Professional ID is required"
                    timestamp: "2025-11-13T15:30:00"
                patient-not-found:
                  value:
                    error: "VALIDATION_ERROR"
                    message: "Patient not found: 12345678"
                    timestamp: "2025-11-13T15:30:00"
        '401':
          description: Clinic not authenticated
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

components:
  schemas:
    AccessRequestCreationDTO:
      type: object
      required:
        - professionalId
        - patientCi
        - requestReason
      properties:
        professionalId:
          type: string
          maxLength: 100
          description: Professional identifier
          example: "prof-12345"
        professionalName:
          type: string
          maxLength: 255
          description: Professional full name for patient display
          example: "Dr. María García"
        specialty:
          type: string
          maxLength: 100
          description: Professional specialty
          example: "CARDIOLOGY"
        patientCi:
          type: string
          maxLength: 20
          description: Patient CI (national ID)
          example: "12345678"
        documentId:
          type: integer
          format: int64
          description: Document ID (optional - for specific document requests)
          example: 456
        documentType:
          type: string
          maxLength: 50
          description: Document type for patient context
          example: "LAB_RESULT"
        requestReason:
          type: string
          maxLength: 500
          description: Reason for access request
          example: "Evaluación de control cardiológico del paciente"
        urgency:
          type: string
          enum: [ROUTINE, URGENT, EMERGENCY]
          description: Request urgency level
          example: "ROUTINE"

    AccessRequestCreationResponseDTO:
      type: object
      properties:
        requestId:
          type: integer
          format: int64
          description: Created request ID
          example: 789
        status:
          type: string
          description: Request status
          example: "PENDING"
        createdAt:
          type: string
          format: date-time
          description: Creation timestamp
          example: "2025-11-13T15:30:00"
        expiresAt:
          type: string
          format: date-time
          description: Expiration timestamp
          example: "2025-11-15T15:30:00"
        message:
          type: string
          description: Success message
          example: "Access request created successfully"
        isNewRequest:
          type: boolean
          description: True if newly created, false if duplicate
          example: true
```

### 5.8 Error Handling

**Error Scenarios**:

| Error | HTTP Status | Error Code | Message |
|-------|-------------|------------|---------|
| Missing professionalId | 400 | VALIDATION_ERROR | Professional ID is required |
| Missing patientCi | 400 | VALIDATION_ERROR | Patient CI is required |
| Missing requestReason | 400 | VALIDATION_ERROR | Request reason is required |
| Patient not found | 400 | VALIDATION_ERROR | Patient not found: {ci} |
| Document not found | 400 | VALIDATION_ERROR | Document not found: {id} |
| Invalid urgency | 400 | VALIDATION_ERROR | Invalid urgency: {value} |
| No clinic authentication | 401 | UNAUTHORIZED | Clinic authentication required |
| Database error | 500 | INTERNAL_ERROR | Failed to create access request |

**Error Response Format**:
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Patient not found: 12345678",
  "timestamp": "2025-11-13T15:30:00Z"
}
```

### 5.9 Validation Logic

**Validation Rules**:

```java
// Field Validations
- professionalId: NOT NULL, max 100 chars, alphanumeric + dash/underscore
- professionalName: max 255 chars (optional)
- specialty: max 100 chars (optional)
- patientCi: NOT NULL, max 20 chars, numeric only, 7-8 digits
- documentId: positive integer (optional)
- requestReason: NOT NULL, max 500 chars, non-empty after trim
- urgency: enum (ROUTINE, URGENT, EMERGENCY), defaults to ROUTINE

// Business Validations
- Patient must exist in INUS (query inus.inus_users WHERE ci = ?)
- Document must exist in RNDC if provided (query rndc.rndc_documents WHERE id = ?)
- Clinic must be registered and active (future: check clinic registry)

// Deduplication Logic
- Check for existing PENDING request with:
  - Same professionalId
  - Same patientCi
  - Same documentId (or both null)
  - Status = PENDING
  - expiresAt > now (not expired)
- If found: return existing request (HTTP 200)
- If not found: create new request (HTTP 201)
```

### 5.10 Transaction Management

**Transaction Boundaries**:

```java
@Transactional // Service method level
public AccessRequestCreationResponseDTO createAccessRequest(...) {
    // All operations in single transaction:
    // 1. Patient validation (SELECT)
    // 2. Document validation (SELECT)
    // 3. Deduplication check (SELECT)
    // 4. Access request creation (INSERT)
    // 5. Audit logging (INSERT)

    // If any step fails, entire transaction rolls back
}
```

**Isolation Level**: READ_COMMITTED (default)

**Rollback Policy**:
- IllegalArgumentException → Rollback + HTTP 400
- RuntimeException → Rollback + HTTP 500
- No partial commits

---

## 6. Implementation Plan

### 6.1 Phase Breakdown

#### Phase 1: Database and Entity Updates (4-6 hours)

**Tasks**:
- [ ] Create database migration `V004__add_access_request_fields.sql`
- [ ] Add new fields to AccessRequest entity
- [ ] Add UrgencyLevel enum
- [ ] Update entity constructors
- [ ] Add getters/setters for new fields
- [ ] Update existing unit tests for entity

**Deliverables**:
- Migration script
- Updated AccessRequest.java
- Updated entity unit tests

**Testing**:
- Run migration on test database
- Verify all fields created
- Verify indexes created
- Test entity persistence with new fields

#### Phase 2: Repository Layer (3-4 hours)

**Tasks**:
- [ ] Add `save()` method to AccessRequestRepository interface
- [ ] Implement `save()` in AccessRequestRepositoryImpl
- [ ] Add `findPendingRequest()` method to repository
- [ ] Implement deduplication query
- [ ] Add unit tests for new repository methods

**Deliverables**:
- Updated AccessRequestRepository.java
- Updated AccessRequestRepositoryImpl.java
- Repository unit tests

**Testing**:
- Test save() creates new record
- Test findPendingRequest() returns existing request
- Test findPendingRequest() returns empty for no match
- Test deduplication query with null documentId

#### Phase 3: DTO Layer (2-3 hours)

**Tasks**:
- [ ] Create AccessRequestCreationDTO
- [ ] Add JSR-380 validation annotations
- [ ] Add builder pattern for easier construction
- [ ] Create AccessRequestCreationResponseDTO
- [ ] Add factory method fromEntity()
- [ ] Add unit tests for DTOs

**Deliverables**:
- AccessRequestCreationDTO.java
- AccessRequestCreationResponseDTO.java
- DTO unit tests

**Testing**:
- Test validation annotations
- Test builder pattern
- Test fromEntity() mapping
- Test JSON serialization/deserialization

#### Phase 4: Service Layer (6-8 hours)

**Tasks**:
- [ ] Add createAccessRequest() method to AccessRequestService
- [ ] Implement patient validation (INUS check)
- [ ] Implement document validation (RNDC check)
- [ ] Implement deduplication logic
- [ ] Implement audit logging
- [ ] Add error handling
- [ ] Add unit tests for service method

**Deliverables**:
- Updated AccessRequestService.java
- Service unit tests with mocks

**Testing**:
- Test successful request creation
- Test duplicate request handling
- Test patient not found error
- Test document not found error
- Test urgency parsing
- Test audit logging calls
- Test transaction rollback on error

#### Phase 5: REST API Layer (4-5 hours)

**Tasks**:
- [ ] Add POST /api/access-requests endpoint to AccessRequestResource
- [ ] Add clinic authentication extraction
- [ ] Add request validation
- [ ] Add error handling and response formatting
- [ ] Add API documentation (JavaDoc + OpenAPI)
- [ ] Add integration tests

**Deliverables**:
- Updated AccessRequestResource.java
- Integration tests
- OpenAPI documentation

**Testing**:
- Test successful creation (HTTP 201)
- Test duplicate creation (HTTP 200)
- Test missing required fields (HTTP 400)
- Test patient not found (HTTP 400)
- Test no authentication (HTTP 401)
- Test internal error (HTTP 500)

#### Phase 6: Integration Testing (4-6 hours)

**Tasks**:
- [ ] End-to-end test: Create request → Verify in database
- [ ] End-to-end test: Create duplicate → Returns existing
- [ ] End-to-end test: Patient approves → Access granted
- [ ] End-to-end test: Request expires → Status changes
- [ ] Performance test: 100 concurrent requests
- [ ] Load test: Sustained request creation

**Deliverables**:
- Integration test suite
- Performance test results
- Load test results

**Testing**:
- Full workflow from API call to database
- Concurrent request handling
- Performance metrics

#### Phase 7: Documentation and Deployment (3-4 hours)

**Tasks**:
- [ ] Update API documentation
- [ ] Create peripheral node integration guide
- [ ] Update architecture diagrams
- [ ] Add troubleshooting guide
- [ ] Code review
- [ ] Deploy to staging environment
- [ ] Smoke tests on staging

**Deliverables**:
- API documentation
- Integration guide for peripheral nodes
- Updated architecture diagrams
- Deployment checklist

### 6.2 Total Estimated Effort

| Phase | Estimated Hours | Dependencies |
|-------|----------------|--------------|
| Phase 1: Database & Entity | 4-6 | None |
| Phase 2: Repository | 3-4 | Phase 1 |
| Phase 3: DTO | 2-3 | None |
| Phase 4: Service | 6-8 | Phase 1, 2, 3 |
| Phase 5: REST API | 4-5 | Phase 4 |
| Phase 6: Integration Testing | 4-6 | Phase 5 |
| Phase 7: Documentation | 3-4 | Phase 6 |
| **Total** | **26-36 hours** | **(3-5 days)** |

### 6.3 Risk Mitigation

**Risks**:

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Database migration fails in production | Low | High | Test migration on production-like data, rollback script ready |
| Performance degradation under load | Medium | Medium | Load testing, query optimization, proper indexing |
| Integration issues with peripheral nodes | Medium | High | Clear API documentation, example code, sandbox environment |
| Deduplication logic has edge cases | Medium | Medium | Comprehensive unit tests, peer review |
| Audit logging impacts performance | Low | Medium | Async audit logging, batching |

---

## 7. Testing Strategy

### 7.1 Unit Tests

#### 7.1.1 Entity Tests

**File**: `AccessRequestTest.java`

**Test Cases**:
- [ ] Constructor with all fields initializes correctly
- [ ] Default constructor sets status to PENDING
- [ ] Default expiration is 48 hours from creation
- [ ] isPending() returns true for non-expired PENDING request
- [ ] isPending() returns false for expired request
- [ ] isExpired() correctly detects expiration
- [ ] approve() changes status to APPROVED
- [ ] deny() changes status to DENIED
- [ ] approve() throws exception for already approved request
- [ ] deny() throws exception for expired request

#### 7.1.2 Repository Tests

**File**: `AccessRequestRepositoryTest.java`

**Test Cases**:
- [ ] save() persists new request and returns ID
- [ ] findById() returns saved request
- [ ] findPendingRequest() returns existing pending request
- [ ] findPendingRequest() returns empty for different professional
- [ ] findPendingRequest() returns empty for different patient
- [ ] findPendingRequest() returns empty for expired request
- [ ] findPendingRequest() handles null documentId
- [ ] update() modifies existing request

#### 7.1.3 Service Tests

**File**: `AccessRequestServiceTest.java`

**Test Cases**:
- [ ] createAccessRequest() creates new request successfully
- [ ] createAccessRequest() returns existing request for duplicate
- [ ] createAccessRequest() validates patient exists in INUS
- [ ] createAccessRequest() validates document exists in RNDC
- [ ] createAccessRequest() throws exception for missing patient
- [ ] createAccessRequest() throws exception for missing document
- [ ] createAccessRequest() parses urgency level correctly
- [ ] createAccessRequest() defaults to ROUTINE urgency
- [ ] createAccessRequest() logs audit event on success
- [ ] createAccessRequest() logs audit event on failure
- [ ] createAccessRequest() rolls back transaction on error

**Mocking**:
```java
@Mock
private AccessRequestRepository accessRequestRepository;

@Mock
private InusService inusService;

@Mock
private RndcService rndcService;

@Mock
private AuditService auditService;

@InjectMocks
private AccessRequestService accessRequestService;
```

#### 7.1.4 DTO Tests

**File**: `AccessRequestCreationDTOTest.java`

**Test Cases**:
- [ ] Validation fails for null professionalId
- [ ] Validation fails for null patientCi
- [ ] Validation fails for null requestReason
- [ ] Validation fails for requestReason > 500 chars
- [ ] Validation succeeds for valid DTO
- [ ] Builder pattern constructs DTO correctly
- [ ] fromEntity() maps all fields correctly

### 7.2 Integration Tests

#### 7.2.1 REST API Tests

**File**: `AccessRequestResourceIT.java`

**Test Cases**:
- [ ] POST /api/access-requests returns 201 for new request
- [ ] POST /api/access-requests returns 200 for duplicate
- [ ] POST /api/access-requests returns 400 for missing professionalId
- [ ] POST /api/access-requests returns 400 for missing patientCi
- [ ] POST /api/access-requests returns 400 for patient not found
- [ ] POST /api/access-requests returns 400 for document not found
- [ ] POST /api/access-requests returns 401 for no authentication
- [ ] Response contains correct requestId
- [ ] Response contains correct expiration time (48 hours)
- [ ] Database contains created request

**Test Setup**:
```java
@ArquillianResource
private URL base;

@Test
public void testCreateAccessRequest_Success() throws Exception {
    // Arrange
    AccessRequestCreationDTO request = AccessRequestCreationDTO.builder()
        .professionalId("prof-12345")
        .professionalName("Dr. Test")
        .patientCi("12345678")
        .requestReason("Test request")
        .build();

    // Act
    Response response = given()
        .header("Authorization", "Bearer " + clinicApiKey)
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .when()
        .post(base + "api/access-requests")
        .then()
        .statusCode(201)
        .extract()
        .response();

    // Assert
    AccessRequestCreationResponseDTO responseDTO =
        response.as(AccessRequestCreationResponseDTO.class);

    assertNotNull(responseDTO.getRequestId());
    assertEquals("PENDING", responseDTO.getStatus());
    assertTrue(responseDTO.getIsNewRequest());
}
```

#### 7.2.2 End-to-End Tests

**Test Scenarios**:

**Scenario 1: Complete Workflow**
1. Peripheral node creates access request via API
2. Verify request in database with status PENDING
3. Verify request appears in patient portal (GET /api/access-requests?patientCi=...)
4. Patient approves request via API (POST /api/access-requests/{id}/approve)
5. Verify request status changed to APPROVED
6. Verify audit log entries

**Scenario 2: Duplicate Request**
1. Create access request (HTTP 201)
2. Create identical request (HTTP 200, same requestId)
3. Verify only one request in database

**Scenario 3: Request Expiration**
1. Create access request
2. Mock time +48 hours
3. Verify request status becomes EXPIRED
4. Verify patient cannot approve expired request

**Scenario 4: Concurrent Requests**
1. Create 100 concurrent requests with different professionals/patients
2. Verify all requests created successfully
3. Verify no duplicates
4. Verify performance < 500ms per request

### 7.3 Performance Tests

#### 7.3.1 Load Test

**Tool**: JMeter or Gatling

**Scenario**:
- 100 concurrent users
- Each creates 10 access requests
- Total: 1000 requests

**Metrics**:
- Average response time < 500ms
- 95th percentile response time < 1000ms
- Error rate < 1%
- Database connection pool not exhausted

#### 7.3.2 Stress Test

**Scenario**:
- Gradually increase load from 10 to 500 concurrent users
- Find breaking point
- Identify bottlenecks

**Expected Results**:
- System handles at least 100 concurrent users
- Graceful degradation under overload
- No data corruption or lost requests

### 7.4 Coverage Target

**Target**: 80% code coverage (AC017, AC018)

**Coverage by Layer**:
- Entity: 90%+ (simple POJOs, easy to test)
- Repository: 85%+ (CRUD operations)
- Service: 80%+ (business logic, error handling)
- REST API: 75%+ (integration tests)

**Tools**:
- JaCoCo for coverage reports
- SonarQube for quality analysis

---

## 8. Security Considerations

### 8.1 Authentication

**Clinic API Key Authentication**:

**Current State**: JWT authentication exists for patients. Need to add API key authentication for clinics.

**Proposed Implementation**:

```java
@Provider
@Priority(Priorities.AUTHENTICATION)
public class ClinicApiKeyAuthenticationFilter implements ContainerRequestFilter {

    @Inject
    private ClinicRegistryService clinicRegistryService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Extract API key from Authorization header
        String authHeader = requestContext.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("ApiKey ")) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ErrorResponse.unauthorized("API key required"))
                    .build()
            );
            return;
        }

        String apiKey = authHeader.substring(7); // Remove "ApiKey " prefix

        // Validate API key and get clinic ID
        Optional<String> clinicId = clinicRegistryService.validateApiKey(apiKey);

        if (clinicId.isEmpty()) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ErrorResponse.unauthorized("Invalid API key"))
                    .build()
            );
            return;
        }

        // Set clinic ID in SecurityContext
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> clinicId.get();
            }

            @Override
            public boolean isUserInRole(String role) {
                return "CLINIC".equals(role);
            }

            @Override
            public boolean isSecure() {
                return requestContext.getUriInfo().getRequestUri().getScheme().equals("https");
            }

            @Override
            public String getAuthenticationScheme() {
                return "API_KEY";
            }
        });
    }
}
```

**API Key Format**: `ApiKey {base64(clinicId:secretKey)}`

**Example**: `Authorization: ApiKey Y2xpbmljLTAwMTpzZWNyZXQxMjM0NTY=`

### 8.2 Authorization

**Clinic Authorization Checks**:

1. **Clinic Registration**: Verify clinic is registered and active
2. **Clinic-Patient Relationship**: (Future) Verify clinic has treated patient
3. **Clinic Suspension**: Verify clinic is not suspended/blacklisted

**Implementation**:
```java
// In AccessRequestService.createAccessRequest()
if (!clinicRegistryService.isClinicActive(clinicId)) {
    throw new IllegalArgumentException("Clinic is not active: " + clinicId);
}

// Future: Check clinic-patient relationship
// if (!clinicPatientService.hasRelationship(clinicId, patientCi)) {
//     throw new IllegalArgumentException("No relationship between clinic and patient");
// }
```

### 8.3 Input Validation and Sanitization

**SQL Injection Prevention**:
- ✅ Use parameterized queries (JPA/JPQL)
- ✅ No string concatenation in queries
- ✅ Input validation before database operations

**XSS Prevention**:
- Sanitize requestReason and professionalName before storage
- Escape HTML/JavaScript when displaying in patient portal

**Validation Library**: Jakarta Bean Validation (JSR-380)

**Example**:
```java
@NotBlank(message = "Request reason is required")
@Size(max = 500, message = "Request reason must not exceed 500 characters")
@Pattern(regexp = "^[a-zA-Z0-9\\s.,;:!?áéíóúÁÉÍÓÚñÑ-]+$",
         message = "Request reason contains invalid characters")
private String requestReason;
```

### 8.4 Data Privacy

**Patient CI Handling**:
- ✅ Do NOT log full patient CI in application logs
- ✅ Log truncated CI: `12345***` (first 5 digits only)
- ✅ Full CI only in audit database (encrypted at rest)

**Example**:
```java
private String maskCI(String ci) {
    if (ci == null || ci.length() < 5) return "***";
    return ci.substring(0, 5) + "***";
}

LOGGER.log(Level.INFO, "Creating request for patient: {0}", maskCI(patientCi));
```

**Sensitive Data in Responses**:
- Do NOT include patient name in AccessRequestCreationResponseDTO
- Do NOT include full medical data in error messages
- Minimal information disclosure

### 8.5 Rate Limiting

**Purpose**: Prevent spam/abuse of access request creation

**Implementation** (Future Enhancement):
```java
@RateLimit(requests = 10, perProfessional = true, window = "1 hour")
public AccessRequestCreationResponseDTO createAccessRequest(...) {
    // ...
}
```

**Limits**:
- 10 requests per professional per hour
- 50 requests per clinic per hour
- 429 Too Many Requests if exceeded

### 8.6 HTTPS Enforcement

**Requirement**: AC002-AC004 - ALL communications use HTTPS

**Implementation**:
- Configure WildFly with SSL/TLS certificates
- Redirect HTTP to HTTPS
- HSTS header: `Strict-Transport-Security: max-age=31536000`

**Example** (WildFly configuration):
```xml
<security-realm name="ApplicationRealm">
    <server-identities>
        <ssl>
            <keystore path="hcen.keystore"
                      relative-to="jboss.server.config.dir"
                      keystore-password="..."
                      alias="hcen"/>
        </ssl>
    </server-identities>
</security-realm>
```

### 8.7 Audit Logging

**What to Log**:
- ✅ All request creation attempts (success and failure)
- ✅ Duplicate request detections
- ✅ Validation failures (patient not found, document not found)
- ✅ Unauthorized access attempts
- ✅ API key validation failures

**What NOT to Log**:
- ❌ Full patient CI (use masked version)
- ❌ API keys
- ❌ Sensitive medical data
- ❌ Internal implementation details

**Audit Entry Format**:
```json
{
  "timestamp": "2025-11-13T15:30:00Z",
  "eventType": "ACCESS_REQUEST_CREATED",
  "actorId": "prof-12345",
  "actorType": "PROFESSIONAL",
  "resourceType": "ACCESS_REQUEST",
  "resourceId": "789",
  "actionOutcome": "SUCCESS",
  "clinicId": "clinic-001",
  "patientCi": "12345***",
  "documentId": 456,
  "urgency": "ROUTINE"
}
```

---

## 9. Open Questions

### 9.1 Notification Mechanism

**Question**: How should patients be notified of new access requests?

**Options**:
1. **Web Portal Only** (MVP):
   - Badge counter in patient portal
   - Request appears in pending list
   - ✅ Simple, already implemented
   - ❌ Patient must actively check portal

2. **Email Notification**:
   - Send email when new request created
   - ✅ Proactive notification
   - ❌ Requires email service integration
   - ❌ Email may be delayed/unread

3. **SMS Notification**:
   - Send SMS for urgent requests
   - ✅ High visibility
   - ❌ Cost per SMS
   - ❌ Requires SMS gateway integration

4. **Mobile Push Notification** (Firebase):
   - Push to mobile app
   - ✅ Real-time, high visibility
   - ❌ Requires Firebase integration
   - ❌ Only for mobile app users

**Recommendation**: Start with Option 1 (web portal), add push notifications in Phase 2.

### 9.2 Request Limits and Quotas

**Question**: Should there be limits on access request creation?

**Considerations**:
- Max pending requests per professional?
- Max pending requests per patient?
- Max requests per document?
- Max requests per time window (rate limiting)?

**Proposed Limits**:
- 10 requests per professional per hour
- 50 pending requests per patient (across all professionals)
- 5 pending requests per document
- Auto-deny if limits exceeded

**Open for Discussion**: Are these limits appropriate? Too restrictive? Too permissive?

### 9.3 Auto-Approval Rules

**Question**: Should some access requests be auto-approved?

**Scenarios for Auto-Approval**:
1. **Emergency Access**:
   - Professional marks request as EMERGENCY
   - Auto-approve, but notify patient immediately
   - Patient can revoke access retroactively

2. **Same-Clinic Relationship**:
   - Professional and patient have established relationship
   - Professional from same clinic that created document
   - Auto-approve based on trust

3. **Recurring Access**:
   - Patient previously approved access from this professional
   - Auto-approve subsequent requests (patient sets preference)

**Compliance Concerns**:
- Does auto-approval violate patient consent requirements?
- Does Ley 18.331 require explicit approval for each access?

**Recommendation**: No auto-approval in MVP. Add as optional feature with patient opt-in.

### 9.4 Request Cancellation

**Question**: Can professionals cancel access requests?

**Use Cases**:
- Professional created request by mistake
- Patient already provided information verbally
- Professional no longer needs access

**Proposed API**: `DELETE /api/access-requests/{requestId}`

**Authorization**: Only the requesting professional (or clinic admin) can cancel

**Status Transition**: PENDING → CANCELLED (new status)

**Open for Discussion**: Should cancelled requests appear in patient portal?

### 9.5 Request History and Archival

**Question**: How long should access requests be stored?

**Options**:
1. **Keep Forever**: All requests in audit trail permanently
2. **Archive After Response**: Move to archive table after approval/denial
3. **Delete After Expiration**: Delete expired requests after 30 days
4. **Retention Policy**: Keep for 7 years per Uruguayan law

**Recommendation**: Option 4 (7-year retention), with archival to separate table after 1 year.

### 9.6 Multi-Document Requests

**Question**: Should professionals be able to request access to multiple documents at once?

**Current Design**: One document per request (or general access with null documentId)

**Alternative**: Batch request with array of document IDs

**API Design**:
```json
{
  "professionalId": "prof-12345",
  "patientCi": "12345678",
  "documentIds": [456, 457, 458],
  "requestReason": "Full patient history review"
}
```

**Complexity**: Higher implementation complexity, unclear UX for patient approval

**Recommendation**: Defer to future enhancement. Start with single-document requests.

### 9.7 Request Communication Thread

**Question**: Should professionals and patients be able to communicate about access requests?

**Use Case**: Patient requests more info (already implemented), professional responds

**Proposed Feature**:
- Comment thread on each access request
- Professional can add clarification
- Patient can ask questions
- Notifications for new comments

**API Design**:
```
POST /api/access-requests/{requestId}/comments
GET /api/access-requests/{requestId}/comments
```

**Recommendation**: Defer to future enhancement. Current "request more info" is sufficient for MVP.

---

## 10. Success Criteria

The implementation will be considered complete and successful when:

### 10.1 Functional Completeness

- ✅ Professionals (via peripheral nodes) can create access requests via REST API
- ✅ Access requests are stored in database with all required fields
- ✅ Patients see new requests in patient portal (real-time or on page refresh)
- ✅ Duplicate requests are detected and handled idempotently
- ✅ Requests expire after 48 hours if not responded
- ✅ Expired requests cannot be approved/denied
- ✅ All request creation attempts are logged in audit trail
- ✅ Validation errors return appropriate HTTP status codes
- ✅ API responses match OpenAPI specification

### 10.2 Quality Metrics

- ✅ Unit tests achieve 80%+ code coverage
- ✅ All integration tests pass
- ✅ End-to-end workflow test passes
- ✅ No critical/high severity SonarQube issues
- ✅ Code review approved by 2+ team members
- ✅ Documentation complete (API docs, integration guide)

### 10.3 Performance Metrics

- ✅ Response time < 500ms for request creation (95th percentile)
- ✅ System handles 100 concurrent requests without errors
- ✅ Database queries use appropriate indexes
- ✅ No N+1 query problems
- ✅ Transaction duration < 200ms

### 10.4 Security Validation

- ✅ Clinic API key authentication works correctly
- ✅ Unauthorized requests return HTTP 401
- ✅ Input validation prevents SQL injection
- ✅ Input validation prevents XSS attacks
- ✅ Patient CI is masked in application logs
- ✅ HTTPS enforced for all API calls
- ✅ Security review passes (no critical vulnerabilities)

### 10.5 Operational Readiness

- ✅ Database migration script tested on production-like data
- ✅ Rollback script available and tested
- ✅ Deployment checklist created
- ✅ Monitoring/alerting configured for new endpoint
- ✅ Smoke tests pass on staging environment
- ✅ Integration guide published for peripheral nodes
- ✅ API documentation published (OpenAPI/Swagger)

### 10.6 Compliance

- ✅ Patient consent workflow compliant with Ley 18.331
- ✅ Audit trail meets AGESIC guidelines
- ✅ Data retention policy defined and documented
- ✅ Privacy impact assessment completed
- ✅ No personal data exposed in error messages

### 10.7 Integration Testing

- ✅ End-to-end test: Professional request → Patient approval → Access granted
- ✅ End-to-end test: Professional request → Patient denial → Access blocked
- ✅ End-to-end test: Professional request → Expiration → Status EXPIRED
- ✅ End-to-end test: Duplicate request → Returns existing request
- ✅ Peripheral node integration guide validated by external team

---

## 11. Future Enhancements

### 11.1 Short-Term (Next 3-6 months)

#### 11.1.1 Auto-Creation on PENDING Policy Decision
**Description**: Automatically create AccessRequest when PolicyEngine returns PENDING

**Benefits**:
- Seamless integration with existing policy flow
- No explicit API call needed from peripheral nodes
- Consistent behavior across all access attempts

**Implementation**: Add hook in PolicyEngine or ClinicalHistoryService

#### 11.1.2 Email Notifications
**Description**: Send email to patient when new access request created

**Benefits**:
- Proactive notification
- Higher patient engagement
- Faster response times

**Requirements**:
- Email service integration
- Email template design
- Opt-in/opt-out preferences

#### 11.1.3 Request Cancellation
**Description**: Allow professionals to cancel pending requests

**API**: `DELETE /api/access-requests/{requestId}`

**Benefits**:
- Corrects mistakes
- Reduces clutter in patient portal
- Improves professional experience

### 11.2 Medium-Term (6-12 months)

#### 11.2.1 Mobile Push Notifications
**Description**: Send Firebase push notifications for new requests

**Benefits**:
- Real-time notification
- High visibility
- Better patient engagement

**Requirements**:
- Firebase integration
- Device token management
- Notification preferences

#### 11.2.2 Multi-Document Requests
**Description**: Request access to multiple documents in single request

**API**: Array of documentIds in AccessRequestCreationDTO

**Benefits**:
- Fewer API calls
- Better UX for comprehensive access needs
- Batch approval for patients

#### 11.2.3 Request Communication Thread
**Description**: Enable comments/questions on access requests

**Benefits**:
- Clarification without separate channels
- Contextual conversation
- Better patient understanding

**API**:
- `POST /api/access-requests/{id}/comments`
- `GET /api/access-requests/{id}/comments`

#### 11.2.4 Auto-Approval Rules Engine
**Description**: Configurable rules for auto-approving certain requests

**Examples**:
- Emergency access (with retroactive notification)
- Established professional-patient relationship
- Same-clinic trust relationships

**Requirements**:
- Rules engine implementation
- Patient consent for auto-approval
- Retroactive audit logging

### 11.3 Long-Term (12+ months)

#### 11.3.1 Advanced Analytics and Reporting
**Description**: Dashboard for access request patterns

**Metrics**:
- Most common denial reasons
- Average response time by patient
- Request volume by specialty
- Expiration rate (requests not responded)

**Benefits**:
- Insights into access patterns
- Identify bottlenecks
- Improve patient education

#### 11.3.2 Request Prioritization and Queuing
**Description**: Priority queue based on urgency level

**Features**:
- URGENT requests appear first in patient portal
- EMERGENCY requests trigger immediate notification
- ROUTINE requests batched for daily digest

**Benefits**:
- Faster response for critical cases
- Better resource allocation
- Reduced notification fatigue

#### 11.3.3 Delegation and Family Access
**Description**: Allow patients to delegate approval authority to family members

**Use Cases**:
- Elderly patients
- Minors (parent approval)
- Patients with disabilities

**Features**:
- Delegate can approve/deny on behalf of patient
- Audit trail shows delegation
- Revocable delegation

#### 11.3.4 Request Templates
**Description**: Pre-defined request reasons for common scenarios

**Examples**:
- "Routine follow-up examination"
- "Emergency consultation"
- "Surgical planning"
- "Second opinion review"

**Benefits**:
- Faster request creation
- Consistent reason descriptions
- Better patient understanding

#### 11.3.5 Integration with External Systems
**Description**: Interoperability with regional/national health systems

**Standards**:
- FHIR Consent resources
- IHE XDS (Cross-Enterprise Document Sharing)
- HL7 FHIR Subscription API for notifications

**Benefits**:
- Broader ecosystem integration
- Standards compliance
- Future-proof architecture

---

## 12. References

### 12.1 Internal Documentation

- `C:\Users\agust\fing\tse\tse-2025\docs\arquitectura-grupo9-tse.pdf` - Architecture document
- `C:\Users\agust\fing\tse\tse-2025\CLAUDE.md` - Project guidelines
- `C:\Users\agust\fing\tse\tse-2025\hcen\README.md` - HCEN Central README

### 12.2 Code References

- `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\policy\entity\AccessRequest.java`
- `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\policy\service\AccessRequestService.java`
- `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\policy\api\rest\AccessRequestResource.java`
- `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\resources\db\migration\V003__create_policies_schema.sql`

### 12.3 External Standards

- **Ley N° 18.331**: Protección de Datos Personales (Uruguay)
- **AGESIC Guidelines**: Information security for public sector
- **Jakarta EE 10**: Enterprise Java specifications
- **JAX-RS 3.1**: RESTful Web Services API
- **JPA 3.1**: Java Persistence API
- **OpenAPI 3.0**: API documentation standard

### 12.4 Technology Documentation

- **WildFly 31**: https://docs.wildfly.org/31/
- **PostgreSQL 16**: https://www.postgresql.org/docs/16/
- **Jakarta Bean Validation 3.0**: https://jakarta.ee/specifications/bean-validation/3.0/
- **JaCoCo**: https://www.jacoco.org/jacoco/trunk/doc/

---

## Appendix A: Request/Response Examples

### A.1 Create Access Request - Specific Document

**Request**:
```bash
POST /api/access-requests HTTP/1.1
Host: hcen.uy
Authorization: ApiKey Y2xpbmljLTAwMTpzZWNyZXQxMjM0NTY=
Content-Type: application/json

{
  "professionalId": "prof-12345",
  "professionalName": "Dr. María García",
  "specialty": "CARDIOLOGY",
  "patientCi": "12345678",
  "documentId": 456,
  "documentType": "LAB_RESULT",
  "requestReason": "Evaluación de resultados de laboratorio para control cardiológico del paciente",
  "urgency": "ROUTINE"
}
```

**Response** (201 Created):
```json
{
  "requestId": 789,
  "status": "PENDING",
  "createdAt": "2025-11-13T15:30:00",
  "expiresAt": "2025-11-15T15:30:00",
  "message": "Access request created successfully. Patient will be notified.",
  "isNewRequest": true
}
```

### A.2 Create Access Request - General Access

**Request**:
```bash
POST /api/access-requests HTTP/1.1
Host: hcen.uy
Authorization: ApiKey Y2xpbmljLTAwMjpzZWNyZXQ3ODkwMTI=
Content-Type: application/json

{
  "professionalId": "prof-67890",
  "professionalName": "Dr. Juan Pérez",
  "specialty": "EMERGENCY_MEDICINE",
  "patientCi": "87654321",
  "requestReason": "Consulta de emergencia - paciente ingresado con dolor torácico",
  "urgency": "URGENT"
}
```

**Response** (201 Created):
```json
{
  "requestId": 790,
  "status": "PENDING",
  "createdAt": "2025-11-13T16:00:00",
  "expiresAt": "2025-11-15T16:00:00",
  "message": "Access request created successfully. Patient will be notified.",
  "isNewRequest": true
}
```

### A.3 Duplicate Request

**Request** (same as A.1):
```bash
POST /api/access-requests HTTP/1.1
Host: hcen.uy
Authorization: ApiKey Y2xpbmljLTAwMTpzZWNyZXQxMjM0NTY=
Content-Type: application/json

{
  "professionalId": "prof-12345",
  "professionalName": "Dr. María García",
  "specialty": "CARDIOLOGY",
  "patientCi": "12345678",
  "documentId": 456,
  "documentType": "LAB_RESULT",
  "requestReason": "Evaluación de resultados de laboratorio para control cardiológico del paciente",
  "urgency": "ROUTINE"
}
```

**Response** (200 OK):
```json
{
  "requestId": 789,
  "status": "PENDING",
  "createdAt": "2025-11-13T15:30:00",
  "expiresAt": "2025-11-15T15:30:00",
  "message": "An identical pending request already exists. Returning existing request.",
  "isNewRequest": false
}
```

### A.4 Validation Error - Patient Not Found

**Request**:
```bash
POST /api/access-requests HTTP/1.1
Host: hcen.uy
Authorization: ApiKey Y2xpbmljLTAwMTpzZWNyZXQxMjM0NTY=
Content-Type: application/json

{
  "professionalId": "prof-12345",
  "professionalName": "Dr. María García",
  "patientCi": "99999999",
  "requestReason": "Test request"
}
```

**Response** (400 Bad Request):
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Patient not found: 99999999",
  "timestamp": "2025-11-13T15:30:00"
}
```

### A.5 Authentication Error

**Request** (missing Authorization header):
```bash
POST /api/access-requests HTTP/1.1
Host: hcen.uy
Content-Type: application/json

{
  "professionalId": "prof-12345",
  "patientCi": "12345678",
  "requestReason": "Test request"
}
```

**Response** (401 Unauthorized):
```json
{
  "error": "UNAUTHORIZED",
  "message": "Clinic authentication required",
  "timestamp": "2025-11-13T15:30:00"
}
```

---

## Appendix B: Database Query Examples

### B.1 Check for Existing Pending Request

```sql
SELECT ar.*
FROM policies.access_requests ar
WHERE ar.professional_id = 'prof-12345'
  AND ar.patient_ci = '12345678'
  AND ar.document_id = 456
  AND ar.status = 'PENDING'
  AND ar.expires_at > NOW();
```

### B.2 Create New Access Request

```sql
INSERT INTO policies.access_requests (
    professional_id, professional_name, specialty,
    clinic_id, clinic_name,
    patient_ci, document_id, document_type,
    request_reason, urgency, status,
    requested_at, expires_at
) VALUES (
    'prof-12345', 'Dr. María García', 'CARDIOLOGY',
    'clinic-001', 'Clínica San José',
    '12345678', 456, 'LAB_RESULT',
    'Evaluación de control cardiológico', 'ROUTINE', 'PENDING',
    NOW(), NOW() + INTERVAL '48 hours'
)
RETURNING id;
```

### B.3 Get Pending Requests for Patient

```sql
SELECT ar.*
FROM policies.access_requests ar
WHERE ar.patient_ci = '12345678'
  AND ar.status = 'PENDING'
  AND ar.expires_at > NOW()
ORDER BY ar.requested_at DESC;
```

### B.4 Expire Old Requests (Batch Job)

```sql
UPDATE policies.access_requests
SET status = 'EXPIRED'
WHERE status = 'PENDING'
  AND expires_at <= NOW();
```

---

## Document Control

**Version History**:

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-11-13 | TSE 2025 Group 9 | Initial specification |

**Review Status**: Draft

**Approvals Required**:
- [ ] Technical Lead
- [ ] Security Review
- [ ] Product Owner

**Next Review Date**: 2025-11-20

---

**End of Specification Document**
