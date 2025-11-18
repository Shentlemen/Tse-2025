# API Documentation: Access Request Document Retrieval

## Endpoint Overview

**GET `/api/access-requests/{requestId}/approved-document`**

Retrieves an approved clinical document in FHIR R4 DocumentReference format for an authenticated professional.

---

## Request

### HTTP Method
```
GET
```

### URL Structure
```
/api/access-requests/{requestId}/approved-document
```

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `requestId` | Long | Yes | Unique identifier of the access request |

### Headers

| Header | Value | Required | Description |
|--------|-------|----------|-------------|
| `Authorization` | `Bearer {api-key}` | Yes | Clinic API key or JWT token |
| `Accept` | `application/fhir+json` | Recommended | FHIR JSON format |

### Authentication

Professional authentication is required via:
- **Option 1**: Clinic API key in Authorization header
- **Option 2**: JWT token with professional claims

The professional ID is extracted from `SecurityContext.getUserPrincipal().getName()`.

---

## Response

### Success Response (200 OK)

**Content-Type**: `application/fhir+json`

**Body**: FHIR R4 DocumentReference resource with embedded base64-encoded document content

**Example**:
```json
{
  "resourceType": "DocumentReference",
  "id": "doc-456",
  "status": "current",
  "type": {
    "coding": [
      {
        "system": "http://loinc.org",
        "code": "18725-2",
        "display": "LAB_RESULT"
      }
    ]
  },
  "subject": {
    "reference": "Patient/12345678",
    "display": "Patient CI: 12345678"
  },
  "author": [
    {
      "reference": "Practitioner/doctor@clinic.uy",
      "display": "doctor@clinic.uy"
    }
  ],
  "date": "2025-11-17T10:00:00Z",
  "content": [
    {
      "attachment": {
        "contentType": "application/pdf",
        "data": "JVBERi0xLjQKJeLjz9MKMyAwIG9iago8PC9UeXBlL...",
        "hash": "OGEyZmI0ZjNkMjQ5YWI5ZmY2ZGI4YzY3ZGQ4YjA3OTI=",
        "title": "Lab Result - Blood Test"
      }
    }
  ]
}
```

**Response Headers**:
```
Content-Type: application/fhir+json
Cache-Control: no-cache, no-store, must-revalidate
Pragma: no-cache
Expires: 0
```

---

## Error Responses

### 400 Bad Request - Request Not Approved

**Scenario**: Access request status is not APPROVED (e.g., PENDING, DENIED, EXPIRED)

**Response**:
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Cannot retrieve document - request status is PENDING. Only APPROVED requests can be retrieved.",
  "timestamp": "2025-11-17T10:30:00Z"
}
```

### 401 Unauthorized - Missing Authentication

**Scenario**: No authentication provided (missing Authorization header)

**Response**:
```json
{
  "error": "UNAUTHORIZED",
  "message": "Professional authentication required",
  "timestamp": "2025-11-17T10:30:00Z"
}
```

### 403 Forbidden - Professional Doesn't Own Request

**Scenario**: Authenticated professional ID doesn't match request's professional ID

**Response**:
```json
{
  "error": "FORBIDDEN",
  "message": "You are not authorized to retrieve this document",
  "timestamp": "2025-11-17T10:30:00Z"
}
```

**Audit Log Entry**: Unauthorized access attempt is logged

### 404 Not Found - Request Not Found

**Scenario**: Access request with given ID doesn't exist

**Response**:
```json
{
  "error": "NOT_FOUND",
  "message": "Resource not found: 123",
  "timestamp": "2025-11-17T10:30:00Z"
}
```

### 404 Not Found - Document Not Found

**Scenario**: Document referenced in access request no longer exists in RNDC

**Response**:
```json
{
  "error": "NOT_FOUND",
  "message": "Document not found: 456",
  "timestamp": "2025-11-17T10:30:00Z"
}
```

### 404 Not Found - Clinic Not Found

**Scenario**: Clinic referenced in document metadata no longer exists

**Response**:
```json
{
  "error": "NOT_FOUND",
  "message": "Clinic not found: clinic-001",
  "timestamp": "2025-11-17T10:30:00Z"
}
```

### 500 Internal Server Error - Document Retrieval Failed

**Scenario**: Document hash verification failed, or internal server error

**Response**:
```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "Failed to retrieve approved document: Document integrity verification failed",
  "timestamp": "2025-11-17T10:30:00Z"
}
```

### 502 Bad Gateway - Peripheral Node Unavailable

**Scenario**: Peripheral node is down or unreachable

**Response**:
```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "Peripheral node unavailable: Failed to retrieve document after 3 attempts",
  "timestamp": "2025-11-17T10:30:00Z"
}
```

---

## Usage Examples

### Example 1: Retrieve Approved Document (cURL)

```bash
curl -X GET \
  "http://localhost:8080/hcen/api/access-requests/123/approved-document" \
  -H "Authorization: Bearer clinic-001-api-key-abc123" \
  -H "Accept: application/fhir+json"
```

### Example 2: Decode Base64 Document Content (JavaScript)

```javascript
// Parse FHIR response
const fhirDoc = await response.json();

// Extract base64-encoded document
const base64Data = fhirDoc.content[0].attachment.data;

// Decode to binary
const binaryData = atob(base64Data);

// Create Blob for PDF viewing
const blob = new Blob([new Uint8Array([...binaryData].map(c => c.charCodeAt(0)))],
                       { type: 'application/pdf' });

// Create object URL for viewing
const url = URL.createObjectURL(blob);
window.open(url, '_blank');
```

### Example 3: Verify Document Hash (Python)

```python
import hashlib
import base64

# Extract hash from FHIR response
fhir_hash_base64 = fhir_doc['content'][0]['attachment']['hash']
expected_hash = base64.b64decode(fhir_hash_base64)

# Extract document data
document_base64 = fhir_doc['content'][0]['attachment']['data']
document_bytes = base64.b64decode(document_base64)

# Calculate SHA-256 hash
actual_hash = hashlib.sha256(document_bytes).digest()

# Verify integrity
if actual_hash == expected_hash:
    print("Document integrity verified!")
else:
    print("WARNING: Hash mismatch - document may be corrupted!")
```

### Example 4: Integration with Professional Portal (Java)

```java
@Inject
private AccessRequestClient accessRequestClient;

public void retrieveApprovedDocument(Long requestId, String professionalId) {
    try {
        // Retrieve FHIR document
        DocumentReference fhirDoc = accessRequestClient.getApprovedDocument(
            requestId,
            professionalId
        );

        // Extract document content
        Attachment attachment = fhirDoc.getContent().get(0).getAttachment();
        byte[] documentBytes = attachment.getData();
        String contentType = attachment.getContentType();

        // Display to user
        displayDocument(documentBytes, contentType);

    } catch (ForbiddenException e) {
        showError("You are not authorized to access this document");
    } catch (NotFoundException e) {
        showError("Document not found or request expired");
    } catch (BadRequestException e) {
        showError("Request must be approved before retrieving document");
    }
}
```

---

## Authorization Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│ Professional Document Retrieval Authorization Flow                  │
└─────────────────────────────────────────────────────────────────────┘

1. Professional Request
   ↓
2. Extract professionalId from SecurityContext
   ↓
   ┌─ Not Authenticated? → 401 Unauthorized
   ↓
3. Find Access Request by requestId
   ↓
   ┌─ Not Found? → 404 Not Found
   ↓
4. Verify professionalId matches request.professionalId
   ↓
   ┌─ Mismatch? → 403 Forbidden (+ audit log)
   ↓
5. Verify request.status == APPROVED
   ↓
   ┌─ Not APPROVED? → 400 Bad Request (status is {status})
   ↓
6. Get Document Metadata from RNDC
   ↓
   ┌─ Not Found? → 404 Not Found
   ↓
7. Get Clinic API Key
   ↓
   ┌─ Clinic Not Found? → 404 Not Found
   ↓
8. Retrieve Document from Peripheral Node (with hash verification)
   ↓
   ┌─ Failed? → 502 Bad Gateway or 500 Internal Server Error
   ↓
9. Build FHIR DocumentReference
   ↓
10. Log Access in Audit System (with masked patient CI)
   ↓
11. Return FHIR JSON → 200 OK
```

---

## Audit Logging

Every successful document retrieval is logged in the audit system:

**Audit Log Entry**:
```json
{
  "eventType": "ACCESS",
  "actorId": "prof-123",
  "actorType": "PROFESSIONAL",
  "resourceType": "DOCUMENT",
  "resourceId": "456",
  "actionOutcome": "SUCCESS",
  "timestamp": "2025-11-17T10:30:00Z",
  "details": {
    "action": "APPROVED_DOCUMENT_RETRIEVAL",
    "requestId": "123",
    "patientCi": "12345***",
    "documentType": "LAB_RESULT",
    "clinicId": "clinic-001",
    "documentSize": 524288
  }
}
```

**Patient Access View** (AC026 requirement):
Patients can query this audit log to see:
- Who accessed their documents (professional ID)
- When documents were accessed (timestamp)
- Which documents were accessed (document ID, type)
- Via which clinic (clinic ID)

---

## Performance Considerations

### Response Time Expectations

| Scenario | Expected Response Time |
|----------|------------------------|
| Small document (<1MB) | < 2 seconds |
| Medium document (1-5MB) | < 5 seconds |
| Large document (5-10MB) | < 10 seconds |
| Peripheral node down | ~15 seconds (3 retries with backoff) |

### Optimizations Implemented

1. **PeripheralNodeClient Circuit Breaker**:
   - Opens after 5 consecutive failures
   - Prevents cascading failures
   - Auto-resets after 60 seconds

2. **Retry Logic**:
   - 3 attempts with exponential backoff (1s, 2s, 4s)
   - Only for network failures (not business errors)

3. **Hash Verification**:
   - SHA-256 computed on peripheral node response
   - Ensures document integrity
   - Prevents man-in-the-middle attacks

4. **No Caching**:
   - Response headers prevent browser caching
   - Ensures latest document version
   - Protects sensitive medical data

---

## Security Considerations

### Data Protection

1. **Patient Privacy**:
   - Patient CI masked in logs (12345***)
   - Only authorized professional can retrieve
   - Full audit trail maintained

2. **Document Integrity**:
   - SHA-256 hash verification
   - Detects tampering or corruption
   - Fails request if hash mismatch

3. **HTTPS Required**:
   - All peripheral node communication over HTTPS
   - PeripheralNodeClient validates HTTPS URLs
   - Rejects HTTP requests

4. **No Caching**:
   - Cache-Control headers prevent storage
   - Sensitive medical data not cached
   - Reduces data breach risk

### Authorization Layers

1. **Authentication**: Professional must be authenticated
2. **Ownership**: Professional must own the access request
3. **Approval**: Request must be APPROVED by patient
4. **Audit**: All access attempts logged (even denied ones)

---

## Testing Checklist

- [ ] **Happy Path**: Retrieve approved document successfully
- [ ] **Auth: Missing Token**: 401 Unauthorized
- [ ] **Auth: Wrong Professional**: 403 Forbidden
- [ ] **Status: PENDING**: 400 Bad Request
- [ ] **Status: DENIED**: 400 Bad Request
- [ ] **Status: EXPIRED**: 400 Bad Request
- [ ] **Not Found: Request**: 404 Not Found
- [ ] **Not Found: Document**: 404 Not Found
- [ ] **Not Found: Clinic**: 404 Not Found
- [ ] **Peripheral Node: Down**: 502 Bad Gateway
- [ ] **Peripheral Node: Hash Mismatch**: 500 Internal Server Error
- [ ] **FHIR: Valid Structure**: DocumentReference schema valid
- [ ] **FHIR: Base64 Decodes**: Content decodes to valid PDF
- [ ] **FHIR: Hash Matches**: Attachment hash matches decoded content
- [ ] **Audit: Logged**: Entry created in audit_logs table
- [ ] **Audit: Masked CI**: Patient CI masked in logs
- [ ] **Performance: <5s**: Response time under 5 seconds for typical document

---

## Troubleshooting

### Issue: 403 Forbidden - Professional authorized but still denied

**Possible Causes**:
1. Professional ID in authentication doesn't match request.professionalId
2. Access request created by different professional
3. Professional ID case-sensitive mismatch

**Solution**:
```sql
-- Verify professional ownership
SELECT professional_id, patient_ci, status
FROM policies.access_requests
WHERE id = 123;

-- Check audit log for details
SELECT details
FROM audit.audit_logs
WHERE resource_id = '123'
  AND actor_type = 'PROFESSIONAL'
ORDER BY timestamp DESC
LIMIT 1;
```

### Issue: 502 Bad Gateway - Peripheral node unreachable

**Possible Causes**:
1. Peripheral node server is down
2. Network firewall blocking HTTPS traffic
3. Document locator URL incorrect
4. SSL certificate expired

**Solution**:
```bash
# Test peripheral node connectivity
curl -v https://clinic-001.hcen.uy/api/documents/123 \
  -H "Authorization: Bearer clinic-api-key"

# Check circuit breaker state
# (Monitor logs for "Circuit breaker OPENED" messages)
```

### Issue: 500 Internal Server Error - Hash mismatch

**Possible Causes**:
1. Document modified in peripheral node storage
2. Hash corrupted in RNDC metadata
3. Network transmission error

**Solution**:
```sql
-- Verify document hash in RNDC
SELECT id, document_hash, document_locator
FROM rndc.rndc_documents
WHERE id = 456;

-- Recalculate hash if document was updated
-- Update RNDC metadata with new hash
UPDATE rndc.rndc_documents
SET document_hash = 'sha256:new-hash-value'
WHERE id = 456;
```

---

## Related Endpoints

- `POST /api/access-requests` - Create access request (professional)
- `GET /api/access-requests?patientCi={ci}` - List access requests (patient)
- `POST /api/access-requests/{id}/approve` - Approve request (patient)
- `POST /api/access-requests/{id}/deny` - Deny request (patient)

---

**Version**: 1.0
**Last Updated**: 2025-11-17
**Author**: TSE 2025 Group 9
