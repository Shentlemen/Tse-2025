# Audit REST API Documentation

## Overview

The Audit REST API provides read-only endpoints for querying audit logs in the HCEN system. This API fulfills the AC026 compliance requirement: **"Patients can view who accessed their records and when"**.

All audit log entries are immutable (append-only), ensuring a tamper-evident audit trail for compliance with Ley N° 18.331 (Data Protection Law of Uruguay) and AGESIC guidelines.

## Base Path

```
/api/audit
```

All endpoints are under this base path.

## Architecture

```
┌─────────────────────────────────────────┐
│ REST Layer (AuditResource)              │
│ - JAX-RS endpoints                      │
│ - Request validation                    │
│ - Response formatting                   │
├─────────────────────────────────────────┤
│ Service Layer (AuditService)            │
│ - Business logic                        │
│ - Query orchestration                   │
│ - Meta-audit logging                    │
├─────────────────────────────────────────┤
│ Repository Layer (AuditLogRepository)   │
│ - Database queries (JPA)                │
│ - Pagination support                    │
├─────────────────────────────────────────┤
│ Entity Layer (AuditLog)                 │
│ - Immutable entity                      │
│ - JSONB details field                   │
└─────────────────────────────────────────┘
```

## API Endpoints

### 1. Patient Access History (AC026 Compliance)

**Endpoint:** `GET /api/audit/patients/{patientCi}`

**Purpose:** Allow patients to view who accessed their clinical records.

**Authorization:** Patient (JWT) or Admin

**Query Parameters:**
- `page` (optional): Page number (0-based, default: 0)
- `size` (optional): Page size (1-100, default: 20)

**Response Example:**
```json
{
  "patientCi": "12345678",
  "accesses": [
    {
      "accessorId": "prof-123",
      "accessorName": "Dr. Smith",
      "specialty": "CARDIOLOGY",
      "clinicId": "clinic-001",
      "documentType": "LAB_RESULT",
      "accessTime": "2025-10-21T14:30:00",
      "outcome": "SUCCESS"
    },
    {
      "accessorId": "prof-456",
      "clinicId": "clinic-002",
      "documentType": "IMAGING",
      "accessTime": "2025-10-20T10:15:00",
      "outcome": "DENIED"
    }
  ],
  "totalAccesses": 45,
  "page": 0,
  "size": 20,
  "totalPages": 3
}
```

**Status Codes:**
- `200 OK`: Success (returns empty list if no accesses found)
- `400 Bad Request`: Invalid patientCi
- `401 Unauthorized`: Missing or invalid JWT (future)
- `403 Forbidden`: User not authorized to view this patient's history (future)

**Example cURL:**
```bash
curl -X GET "http://localhost:8080/api/audit/patients/12345678?page=0&size=20" \
  -H "Accept: application/json"
```

---

### 2. Search Audit Logs (Admin)

**Endpoint:** `GET /api/audit/logs`

**Purpose:** Flexible search for audit logs with multiple optional filters.

**Authorization:** Admin only (JWT)

**Query Parameters:**
All parameters are optional:
- `eventType`: Filter by event type (ACCESS, MODIFICATION, CREATION, DELETION, POLICY_CHANGE, ACCESS_REQUEST, ACCESS_APPROVAL, ACCESS_DENIAL, AUTHENTICATION_SUCCESS, AUTHENTICATION_FAILURE)
- `actorId`: Filter by actor identifier (CI, professional ID, "system")
- `resourceType`: Filter by resource type (DOCUMENT, USER, POLICY, CLINIC, etc.)
- `fromDate`: Start date (ISO-8601: `2025-10-21T00:00:00`)
- `toDate`: End date (ISO-8601: `2025-10-22T23:59:59`)
- `outcome`: Filter by outcome (SUCCESS, DENIED, FAILURE)
- `page`: Page number (0-based, default: 0)
- `size`: Page size (1-100, default: 20)

**Response Example:**
```json
{
  "logs": [
    {
      "id": 1001,
      "eventType": "ACCESS",
      "actorId": "prof-123",
      "actorType": "PROFESSIONAL",
      "resourceType": "DOCUMENT",
      "resourceId": "456",
      "actionOutcome": "SUCCESS",
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0...",
      "timestamp": "2025-10-21T14:30:00",
      "details": {
        "patientCi": "12345678",
        "documentType": "LAB_RESULT",
        "clinicId": "clinic-001"
      }
    }
  ],
  "totalCount": 1500,
  "page": 0,
  "size": 20,
  "totalPages": 75
}
```

**Status Codes:**
- `200 OK`: Success (returns empty list if no matches)
- `400 Bad Request`: Invalid enum value or date format
- `401 Unauthorized`: Missing or invalid JWT (future)
- `403 Forbidden`: User not admin (future)

**Example cURL:**
```bash
# Search for all document accesses by a specific professional
curl -X GET "http://localhost:8080/api/audit/logs?eventType=ACCESS&actorId=prof-123&resourceType=DOCUMENT&page=0&size=20" \
  -H "Accept: application/json"

# Search for denied actions in date range
curl -X GET "http://localhost:8080/api/audit/logs?outcome=DENIED&fromDate=2025-10-01T00:00:00&toDate=2025-10-31T23:59:59" \
  -H "Accept: application/json"
```

---

### 3. Audit Statistics (Admin Dashboard)

**Endpoint:** `GET /api/audit/statistics`

**Purpose:** System-wide audit statistics for administrative dashboards.

**Authorization:** Admin only (JWT)

**Response Example:**
```json
{
  "totalEvents": 10000,
  "eventsByType": {
    "ACCESS": 6000,
    "MODIFICATION": 2000,
    "CREATION": 1500,
    "DELETION": 500
  },
  "eventsByOutcome": {
    "SUCCESS": 9500,
    "DENIED": 400,
    "FAILURE": 100
  },
  "topActors": [
    { "actorId": "prof-123", "eventCount": 450 },
    { "actorId": "prof-456", "eventCount": 320 }
  ]
}
```

**Status Codes:**
- `200 OK`: Success
- `401 Unauthorized`: Missing or invalid JWT (future)
- `403 Forbidden`: User not admin (future)

**Example cURL:**
```bash
curl -X GET "http://localhost:8080/api/audit/statistics" \
  -H "Accept: application/json"
```

---

### 4. Actor Audit Trail

**Endpoint:** `GET /api/audit/actors/{actorId}`

**Purpose:** View all actions performed by a specific actor.

**Authorization:** Admin or the actor themselves (JWT)

**Query Parameters:**
- `page` (optional): Page number (0-based, default: 0)
- `size` (optional): Page size (1-100, default: 20)

**Response Example:**
```json
{
  "logs": [
    {
      "id": 1001,
      "eventType": "ACCESS",
      "actorId": "prof-123",
      "actorType": "PROFESSIONAL",
      "resourceType": "DOCUMENT",
      "resourceId": "456",
      "actionOutcome": "SUCCESS",
      "ipAddress": "192.168.1.100",
      "timestamp": "2025-10-21T14:30:00",
      "details": {
        "patientCi": "12345678",
        "documentType": "LAB_RESULT"
      }
    }
  ],
  "totalCount": 450,
  "page": 0,
  "size": 20,
  "totalPages": 23
}
```

**Status Codes:**
- `200 OK`: Success (returns empty list if actor has no events)
- `400 Bad Request`: Invalid or missing actorId
- `401 Unauthorized`: Missing or invalid JWT (future)
- `403 Forbidden`: User not authorized to view this actor's trail (future)

**Example cURL:**
```bash
curl -X GET "http://localhost:8080/api/audit/actors/prof-123?page=0&size=20" \
  -H "Accept: application/json"
```

---

### 5. Health Check

**Endpoint:** `GET /api/audit/health`

**Purpose:** Verify the audit API is running and get basic metrics.

**Authorization:** None (public)

**Response Example:**
```json
{
  "status": "OK",
  "service": "Audit API",
  "timestamp": "2025-10-22T10:30:00",
  "totalEvents": 10000
}
```

**Status Codes:**
- `200 OK`: Service is healthy

**Example cURL:**
```bash
curl -X GET "http://localhost:8080/api/audit/health" \
  -H "Accept: application/json"
```

---

## DTOs (Data Transfer Objects)

### AuditLogResponse

Represents a single audit log entry.

**Fields:**
- `id` (Long): Audit log ID
- `eventType` (String): Event type name (e.g., "ACCESS")
- `actorId` (String): Actor identifier
- `actorType` (String): Actor type (e.g., "PATIENT", "PROFESSIONAL")
- `resourceType` (String): Resource type (e.g., "DOCUMENT")
- `resourceId` (String): Resource identifier
- `actionOutcome` (String): Outcome (e.g., "SUCCESS")
- `ipAddress` (String): IP address (nullable)
- `userAgent` (String): User agent string (nullable)
- `timestamp` (LocalDateTime): Event timestamp
- `details` (Map<String, Object>): Additional context (JSON object)

### AuditLogListResponse

Paginated list of audit logs.

**Fields:**
- `logs` (List<AuditLogResponse>): List of audit log entries
- `totalCount` (long): Total count across all pages
- `page` (int): Current page (0-based)
- `size` (int): Page size
- `totalPages` (int): Total number of pages

**Helper Methods:**
- `hasNext()`: Check if there's a next page
- `hasPrevious()`: Check if there's a previous page
- `isFirst()`: Check if this is the first page
- `isLast()`: Check if this is the last page
- `isEmpty()`: Check if the result set is empty

### PatientAccessHistoryResponse

Specialized response for patient access history (AC026 compliance).

**Fields:**
- `patientCi` (String): Patient's CI
- `accesses` (List<AccessRecord>): List of access events
- `totalAccesses` (int): Total count of accesses
- `page` (int): Current page
- `size` (int): Page size
- `totalPages` (int): Total pages

**AccessRecord (inner class):**
- `accessorId` (String): Professional ID
- `accessorName` (String): Professional name (optional)
- `specialty` (String): Professional specialty (optional)
- `clinicId` (String): Clinic identifier
- `documentType` (String): Type of document accessed
- `accessTime` (LocalDateTime): Timestamp of access
- `outcome` (String): Outcome (SUCCESS, DENIED)

### AuditStatisticsResponse

System-wide audit statistics.

**Fields:**
- `totalEvents` (long): Total number of audit events
- `eventsByType` (Map<String, Long>): Event counts by type
- `eventsByOutcome` (Map<String, Long>): Event counts by outcome
- `topActors` (List<ActorStat>): Top actors by event count

**ActorStat (inner class):**
- `actorId` (String): Actor identifier
- `eventCount` (long): Number of events by this actor

---

## Error Handling

All endpoints return errors in a consistent format using `ErrorResponse` DTO:

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid eventType: INVALID_TYPE. Valid values: [ACCESS, MODIFICATION, ...]",
  "timestamp": "2025-10-22T10:30:00"
}
```

**Common Error Codes:**
- `BAD_REQUEST`: Invalid parameters (enum, date format, missing required field)
- `UNAUTHORIZED`: Missing or invalid JWT token (future)
- `FORBIDDEN`: User not authorized for this operation (future)
- `INTERNAL_SERVER_ERROR`: Unexpected server error

---

## Pagination

All list endpoints support pagination with these query parameters:
- `page`: Page number (0-based, default: 0)
- `size`: Page size (1-100, default: 20)

**Pagination Constraints:**
- Minimum page size: 1
- Maximum page size: 100
- Default page size: 20
- Invalid page numbers are clamped to valid range (negative → 0)

**Response Metadata:**
All paginated responses include:
- `page`: Current page number
- `size`: Page size
- `totalCount`: Total records across all pages
- `totalPages`: Total number of pages

---

## Meta-Audit (Audit-the-Auditor)

All audit queries are themselves audited. When a user queries audit logs, a new audit log entry is created with:
- `eventType`: ACCESS
- `resourceType`: AUDIT_LOG
- `details`: Contains query parameters (action, filters, page, size)

This provides full traceability of who is viewing audit logs and when.

---

## Compliance Notes

### AC026 Compliance

The `/api/audit/patients/{patientCi}` endpoint fulfills AC026:
- ✅ Patients can view **who** accessed their records (accessorId, accessorName)
- ✅ Patients can view **when** access occurred (accessTime)
- ✅ Patients can view **what** was accessed (documentType, resourceId)
- ✅ Patients can view **where** access occurred (clinicId, ipAddress)
- ✅ Patients can view **outcome** (SUCCESS, DENIED)

### Ley N° 18.331 (Data Protection Law of Uruguay)

The audit system ensures:
- ✅ Immutable audit trail (append-only, no updates/deletes)
- ✅ Comprehensive event logging (all accesses, modifications)
- ✅ Patient transparency (patients can view their access history)
- ✅ Data retention (configurable, default 90 days in PostgreSQL)

### AGESIC Guidelines

The audit system follows AGESIC guidelines for:
- ✅ Information security in public sector systems
- ✅ Traceability of all operations
- ✅ Privacy by design (no sensitive data in logs)

---

## Authentication & Authorization (Future Enhancement)

Currently, the API does not enforce authentication or authorization. This is a **placeholder for future implementation**.

**Planned Enhancement:**
1. Extract JWT from `Authorization` header
2. Validate JWT signature and expiration
3. Extract user claims (CI, role, permissions)
4. Enforce role-based access control:
   - **Patient Access History**: Patient (own records) or Admin
   - **Search Logs**: Admin only
   - **Statistics**: Admin only
   - **Actor Trail**: Actor themselves or Admin

**Example Implementation:**
```java
// Extract JWT from request
@Context
private HttpServletRequest request;

private String extractActorIdFromJWT() {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        // Parse and validate JWT
        // Extract actorId from claims
        return actorId;
    }
    throw new NotAuthorizedException("Missing or invalid JWT");
}
```

---

## Testing

### Unit Tests (Recommended)

Test each endpoint with:
- Valid parameters → 200 OK with expected response
- Invalid enum → 400 Bad Request with error message
- Invalid date format → 400 Bad Request with error message
- Missing required parameter → 400 Bad Request with error message
- Empty results → 200 OK with empty list
- Pagination edge cases (page < 0, size > 100)

### Integration Tests (Recommended)

Test full request/response cycle with:
- Actual database queries
- Multiple filters combined
- Large result sets (pagination)
- Concurrent requests

### Example Test (JUnit + REST Assured):
```java
@Test
public void testGetPatientAccessHistory_Success() {
    given()
        .pathParam("patientCi", "12345678")
        .queryParam("page", 0)
        .queryParam("size", 20)
    .when()
        .get("/api/audit/patients/{patientCi}")
    .then()
        .statusCode(200)
        .body("patientCi", equalTo("12345678"))
        .body("accesses", notNullValue())
        .body("page", equalTo(0))
        .body("size", equalTo(20));
}

@Test
public void testSearchAuditLogs_InvalidEventType() {
    given()
        .queryParam("eventType", "INVALID_TYPE")
    .when()
        .get("/api/audit/logs")
    .then()
        .statusCode(400)
        .body("error", equalTo("BAD_REQUEST"))
        .body("message", containsString("Invalid eventType"));
}
```

---

## Performance Considerations

### Database Indexes

Ensure these indexes exist on `audit.audit_logs` table:
- `idx_audit_event_type` on `event_type`
- `idx_audit_actor_id` on `actor_id`
- `idx_audit_timestamp` on `timestamp`
- `idx_audit_resource_type_id` on `(resource_type, resource_id)`

### Query Optimization

- Use pagination for all list queries (never return unbounded results)
- For patient access history, filter by patientCi in details JSON (JSONB index recommended)
- Cache statistics results (5-minute TTL)

### Caching Strategy (Future)

```java
// Cache statistics for 5 minutes
@Cacheable(value = "auditStatistics", ttl = 300)
public AuditStatisticsResponse getAuditStatistics() {
    // Expensive query
}
```

---

## Known Limitations

1. **Total Count Estimation**: Currently uses heuristic (if page is full, assume more pages exist). In production, implement dedicated COUNT(*) queries.

2. **Top Actors**: Currently returns empty list. Requires GROUP BY query with ORDER BY COUNT(*) DESC LIMIT 10.

3. **Authentication**: Not yet implemented. All endpoints are currently public.

4. **Rate Limiting**: No rate limiting on endpoints. Should implement to prevent abuse.

5. **Bulk Export**: No endpoint for exporting all audit logs (for archival). Should add CSV/JSON export endpoint.

---

## Future Enhancements

1. **JWT Authentication**: Validate JWT tokens and enforce role-based access control
2. **Rate Limiting**: Prevent abuse with rate limiting (e.g., 100 requests/minute per user)
3. **Bulk Export**: Add `/api/audit/export` endpoint for CSV/JSON export
4. **Advanced Filtering**: Add filters for IP address, user agent, date ranges with time
5. **Real-time Notifications**: WebSocket endpoint for real-time audit log streaming
6. **Audit Log Archival**: Automatic archival to MongoDB after 90 days
7. **Anomaly Detection**: ML-based detection of suspicious access patterns
8. **Compliance Reports**: Pre-built reports for AGESIC audits

---

## Contact

**Team:** TSE 2025 Group 9
- German Rodao (4.796.608-7)
- Agustin Silvano (5.096.964-8)
- Piero Santos (6.614.312-9)

**Last Updated:** 2025-10-22
