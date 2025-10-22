# Policy Management API Documentation

## Overview

The Policy Management REST API enables patients to create, view, update, and delete their access control policies for clinical documents in the HCEN system. It also provides an evaluation endpoint for authorization checks.

**Base Path**: `/api/policies`

**Authentication**: JWT (to be implemented)

**Content-Type**: `application/json`

---

## Endpoints

### 1. Create Access Policy

Creates a new access control policy for a patient.

**Endpoint**: `POST /api/policies`

**Request Body**:
```json
{
  "patientCi": "12345678",
  "policyType": "DOCUMENT_TYPE",
  "policyConfig": "{\"allowedTypes\": [\"LAB_RESULT\", \"IMAGING\"]}",
  "policyEffect": "PERMIT",
  "validFrom": "2025-10-21T00:00:00",
  "validUntil": "2026-10-21T00:00:00",
  "priority": 10
}
```

**Response** (201 Created):
```json
{
  "id": 456,
  "patientCi": "12345678",
  "policyType": "DOCUMENT_TYPE",
  "policyConfig": "{\"allowedTypes\": [\"LAB_RESULT\", \"IMAGING\"]}",
  "policyEffect": "PERMIT",
  "validFrom": "2025-10-21T00:00:00",
  "validUntil": "2026-10-21T00:00:00",
  "priority": 10,
  "createdAt": "2025-10-21T15:00:00",
  "updatedAt": "2025-10-21T15:00:00"
}
```

**Response Headers**:
- `Location: /api/policies/456`

**Status Codes**:
- `201 Created` - Policy created successfully
- `400 Bad Request` - Validation error or invalid JSON
- `500 Internal Server Error` - Database error

---

### 2. Get Patient Policies

Retrieves all policies for a specific patient with optional filtering and pagination.

**Endpoint**: `GET /api/policies/patient/{patientCi}`

**Query Parameters**:
- `policyType` (optional) - Filter by policy type (e.g., `DOCUMENT_TYPE`, `SPECIALTY`)
- `page` (optional, default: 0) - Page number (zero-based)
- `size` (optional, default: 20, max: 100) - Page size

**Example Request**:
```
GET /api/policies/patient/12345678?policyType=DOCUMENT_TYPE&page=0&size=20
```

**Response** (200 OK):
```json
{
  "policies": [
    {
      "id": 456,
      "patientCi": "12345678",
      "policyType": "DOCUMENT_TYPE",
      "policyConfig": "{\"allowedTypes\": [\"LAB_RESULT\", \"IMAGING\"]}",
      "policyEffect": "PERMIT",
      "validFrom": null,
      "validUntil": null,
      "priority": 10,
      "createdAt": "2025-10-21T15:00:00",
      "updatedAt": "2025-10-21T15:00:00"
    },
    {
      "id": 457,
      "patientCi": "12345678",
      "policyType": "SPECIALTY",
      "policyConfig": "{\"allowedSpecialties\": [\"CARDIOLOGY\"]}",
      "policyEffect": "DENY",
      "validFrom": null,
      "validUntil": null,
      "priority": 5,
      "createdAt": "2025-10-21T16:00:00",
      "updatedAt": "2025-10-21T16:00:00"
    }
  ],
  "totalCount": 2,
  "page": 0,
  "size": 20,
  "totalPages": 1
}
```

**Status Codes**:
- `200 OK` - Policies retrieved (empty list if no policies)
- `400 Bad Request` - Invalid pagination parameters
- `500 Internal Server Error` - Database error

---

### 3. Get Specific Policy

Retrieves a specific policy by its ID.

**Endpoint**: `GET /api/policies/{id}`

**Example Request**:
```
GET /api/policies/456
```

**Response** (200 OK):
```json
{
  "id": 456,
  "patientCi": "12345678",
  "policyType": "DOCUMENT_TYPE",
  "policyConfig": "{\"allowedTypes\": [\"LAB_RESULT\", \"IMAGING\"]}",
  "policyEffect": "PERMIT",
  "validFrom": null,
  "validUntil": null,
  "priority": 10,
  "createdAt": "2025-10-21T15:00:00",
  "updatedAt": "2025-10-21T15:00:00"
}
```

**Status Codes**:
- `200 OK` - Policy found
- `404 Not Found` - Policy does not exist
- `500 Internal Server Error` - Database error

---

### 4. Update Policy

Updates an existing policy. Only non-null fields are updated.

**Endpoint**: `PUT /api/policies/{id}`

**Request Body** (all fields optional, at least one required):
```json
{
  "policyConfig": "{\"allowedTypes\": [\"LAB_RESULT\", \"IMAGING\", \"PRESCRIPTION\"]}",
  "policyEffect": "DENY",
  "validFrom": "2025-10-21T00:00:00",
  "validUntil": "2026-10-21T00:00:00",
  "priority": 20
}
```

**Response** (200 OK):
```json
{
  "id": 456,
  "patientCi": "12345678",
  "policyType": "DOCUMENT_TYPE",
  "policyConfig": "{\"allowedTypes\": [\"LAB_RESULT\", \"IMAGING\", \"PRESCRIPTION\"]}",
  "policyEffect": "DENY",
  "validFrom": "2025-10-21T00:00:00",
  "validUntil": "2026-10-21T00:00:00",
  "priority": 20,
  "createdAt": "2025-10-21T15:00:00",
  "updatedAt": "2025-10-22T10:30:00"
}
```

**Status Codes**:
- `200 OK` - Policy updated successfully
- `400 Bad Request` - Validation error or no fields provided
- `404 Not Found` - Policy does not exist
- `500 Internal Server Error` - Database error

**Note**: Patient CI and policy type cannot be changed.

---

### 5. Delete Policy

Deletes a policy by its ID.

**Endpoint**: `DELETE /api/policies/{id}`

**Example Request**:
```
DELETE /api/policies/456
```

**Response** (204 No Content):
No response body.

**Status Codes**:
- `204 No Content` - Policy deleted successfully
- `404 Not Found` - Policy does not exist
- `500 Internal Server Error` - Database error

---

### 6. Evaluate Access

Evaluates access policies to determine if a professional can access a document. This endpoint is intended for internal use by document access services (RNDC).

**Endpoint**: `POST /api/policies/evaluate`

**Request Body**:
```json
{
  "professionalId": "prof-123",
  "specialties": ["CARDIOLOGY", "GENERAL_MEDICINE"],
  "clinicId": "clinic-001",
  "patientCi": "12345678",
  "documentId": 789,
  "documentType": "LAB_RESULT",
  "requestReason": "Reviewing cardiac panel results"
}
```

**Response** (200 OK):
```json
{
  "decision": "PERMIT",
  "reason": "Access granted by DOCUMENT_TYPE policy (id: 456)",
  "evaluatedPolicies": [456, 457, 458],
  "decidingPolicy": 456
}
```

**Decision Values**:
- `PERMIT` - Access granted (return document, HTTP 200)
- `DENY` - Access denied (return forbidden, HTTP 403)
- `PENDING` - Requires patient approval (send notification, HTTP 202)

**Status Codes**:
- `200 OK` - Evaluation completed
- `400 Bad Request` - Validation error
- `500 Internal Server Error` - Evaluation error

---

## Policy Types

### DOCUMENT_TYPE
Restrict access by document type.

**Configuration Example**:
```json
{
  "allowedTypes": ["LAB_RESULT", "IMAGING", "PRESCRIPTION"]
}
```

### SPECIALTY
Restrict access by professional specialty.

**Configuration Example**:
```json
{
  "allowedSpecialties": ["CARDIOLOGY", "GENERAL_MEDICINE"]
}
```

### TIME_BASED
Restrict access by time and day.

**Configuration Example**:
```json
{
  "allowedDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
  "allowedHours": "09:00-17:00"
}
```

### CLINIC
Restrict access by clinic.

**Configuration Example**:
```json
{
  "allowedClinics": ["clinic-001", "clinic-002"]
}
```

### PROFESSIONAL
Restrict access to specific professionals (whitelist/blacklist).

**Configuration Example**:
```json
{
  "allowedProfessionals": ["prof-123", "prof-456"]
}
```

### EMERGENCY_OVERRIDE
Emergency access policy (requires heavy audit logging).

**Configuration Example**:
```json
{
  "enabled": true,
  "requiresAudit": true
}
```

---

## Policy Effects

- **PERMIT**: Allows access when policy matches
- **DENY**: Blocks access when policy matches (takes precedence over PERMIT)

---

## Conflict Resolution

When multiple policies apply:
1. **DENY always wins** (fail-safe)
2. **PERMIT wins if no DENY**
3. **PENDING if no applicable policy**

Higher priority policies are evaluated first.

---

## Cache Invalidation

After any policy change (create, update, delete), the patient's policy cache is automatically invalidated to ensure immediate enforcement.

**Cache Key Pattern**: `policy:cache:{patient_ci}:{specialty}:{document_type}`

**Cache TTL**: 5 minutes (300 seconds)

---

## Error Response Format

All errors follow the standard HCEN error format:

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Policy configuration must be valid JSON",
  "timestamp": "2025-10-22T10:30:00"
}
```

**Error Codes**:
- `VALIDATION_ERROR` - Invalid input data
- `POLICY_NOT_FOUND` - Policy does not exist
- `BAD_REQUEST` - Malformed request
- `INTERNAL_SERVER_ERROR` - System error

---

## Examples

### Example 1: Create a DOCUMENT_TYPE Policy

**Request**:
```bash
curl -X POST http://localhost:8080/api/policies \
  -H "Content-Type: application/json" \
  -d '{
    "patientCi": "12345678",
    "policyType": "DOCUMENT_TYPE",
    "policyConfig": "{\"allowedTypes\": [\"LAB_RESULT\"]}",
    "policyEffect": "PERMIT",
    "priority": 10
  }'
```

**Response** (201 Created):
```json
{
  "id": 1,
  "patientCi": "12345678",
  "policyType": "DOCUMENT_TYPE",
  "policyConfig": "{\"allowedTypes\": [\"LAB_RESULT\"]}",
  "policyEffect": "PERMIT",
  "validFrom": null,
  "validUntil": null,
  "priority": 10,
  "createdAt": "2025-10-22T10:00:00",
  "updatedAt": "2025-10-22T10:00:00"
}
```

---

### Example 2: Evaluate Access

**Request**:
```bash
curl -X POST http://localhost:8080/api/policies/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "professionalId": "prof-123",
    "specialties": ["CARDIOLOGY"],
    "clinicId": "clinic-001",
    "patientCi": "12345678",
    "documentType": "LAB_RESULT",
    "requestReason": "Reviewing test results"
  }'
```

**Response** (200 OK):
```json
{
  "decision": "PERMIT",
  "reason": "Access granted by DOCUMENT_TYPE policy (id: 1)",
  "evaluatedPolicies": [1],
  "decidingPolicy": 1
}
```

---

## Testing

Use tools like:
- **cURL** - Command-line HTTP client
- **Postman** - API testing platform
- **HTTPie** - Modern command-line HTTP client

---

## Implementation Details

### Files Created

**DTOs**:
1. `PolicyCreateRequest.java` - Create policy request with Bean Validation
2. `PolicyUpdateRequest.java` - Update policy request (optional fields)
3. `PolicyResponse.java` - Immutable policy response
4. `PolicyListResponse.java` - Paginated policy list
5. `AccessEvaluationRequest.java` - Access evaluation request
6. `AccessEvaluationResponse.java` - Access evaluation result

**REST Resource**:
7. `PolicyResource.java` - JAX-RS resource with 6 endpoints

**Configuration**:
8. `RestApplication.java` - Updated to register PolicyResource

---

## Next Steps

1. **Authentication**: Implement JWT authentication and authorization
2. **Authorization**: Ensure patients can only manage their own policies
3. **Unit Tests**: Write unit tests for PolicyResource (target: 80% coverage)
4. **Integration Tests**: Test full request/response cycles
5. **API Documentation**: Generate OpenAPI/Swagger documentation
6. **Frontend Integration**: Connect patient portal to policy API

---

## Contact

**TSE 2025 Group 9**
- German Rodao (4.796.608-7)
- Agustin Silvano (5.096.964-8)
- Piero Santos (6.614.312-9)

---

**Last Updated**: 2025-10-22
