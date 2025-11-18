# FHIR Document Retrieval Endpoint Implementation

## Overview

Implemented a new REST endpoint for retrieving clinical documents in FHIR format from peripheral health providers. The endpoint returns FHIR-formatted documents as-is without transformation, enabling the frontend to display structured clinical data.

## Implementation Summary

### New REST Endpoint

**Endpoint**: `GET /api/clinical-history/documents/{documentId}/fhir`

**URL Pattern**: `/api/clinical-history/documents/123/fhir?patientCi=12345678`

**Response Content-Type**: `application/fhir+json`

**HTTP Status Codes**:
- `200 OK` - Document retrieved successfully
- `400 Bad Request` - Missing or invalid patientCi parameter
- `403 Forbidden` - Patient doesn't own the document
- `404 Not Found` - Document doesn't exist in RNDC
- `502 Bad Gateway` - Peripheral node unreachable
- `500 Internal Server Error` - Other failures

### Service Layer

**New Method**: `ClinicalHistoryService.getFhirDocument(Long documentId, String patientCi)`

**Flow**:
1. Retrieves document metadata from RNDC (RndcDocument)
2. Verifies patient authorization (patientCi matches document owner)
3. Looks up clinic configuration to get API key
4. Calls `PeripheralNodeClient.retrieveDocument()` with Accept header: `application/fhir+json`
5. Validates response is valid JSON
6. Logs access in audit system
7. Returns FHIR document as JSON string (no transformation)

**Key Features**:
- Authorization check ensures patient owns document
- Comprehensive audit logging (success and failure)
- JSON validation ensures FHIR format
- Proper error handling with descriptive messages
- Skip hash verification for FHIR (content may be dynamically generated)

### Integration

**PeripheralNodeClient** (already implemented):
- Accept header already includes: `application/fhir+json`
- Returns document bytes from peripheral node
- Handles retries, circuit breaker, HTTPS validation

**Peripheral Node Response**:
The peripheral node should return FHIR resources such as:
- `Bundle` (type: document) - Complete clinical document
- `DocumentReference` - Reference to a document
- `DiagnosticReport` - Lab results, imaging reports
- `Observation` - Vital signs, allergies, individual observations

### Example FHIR Response

```json
{
  "resourceType": "Bundle",
  "type": "document",
  "entry": [
    {
      "resource": {
        "resourceType": "Composition",
        "title": "Clinical Note",
        "date": "2025-11-18T10:30:00Z",
        "author": [{"reference": "Practitioner/123"}],
        "section": [
          {
            "title": "Chief Complaint",
            "text": {"status": "generated", "div": "<div>Patient presents with chest pain</div>"}
          }
        ]
      }
    }
  ]
}
```

## Files Modified

### 1. ClinicalHistoryService.java
**Path**: `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\clinicalhistory\service\ClinicalHistoryService.java`

**Added**:
- `getFhirDocument(Long documentId, String patientCi)` method
- Lines: 381-489
- Handles complete document retrieval flow with FHIR format

### 2. ClinicalHistoryResource.java
**Path**: `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\clinicalhistory\api\rest\ClinicalHistoryResource.java`

**Added**:
- `GET /documents/{documentId}/fhir` endpoint
- Lines: 703-804
- Returns raw FHIR JSON with proper content type headers
- Comprehensive error handling and audit logging

## Security Considerations

### Authorization
- Patient CI extracted from query parameter (development mode)
- **TODO for production**: Extract from JWT SecurityContext
- Validates patient owns the document before retrieval
- Logs all access attempts (success and denied)

### Audit Logging
All access attempts are logged with:
- Event type: ACCESS
- Actor: Patient CI
- Resource type: DOCUMENT
- Resource ID: documentId
- Outcome: SUCCESS or FAILURE
- IP address and user agent

### Communication Security
- Peripheral node URL must be HTTPS (validated in PeripheralNodeClient)
- API key authentication via Bearer token
- No cache headers to prevent sensitive data caching

## Testing Recommendations

### Unit Tests
1. Test successful FHIR document retrieval
2. Test authorization failure (patient doesn't own document)
3. Test document not found scenario
4. Test peripheral node unavailable (circuit breaker)
5. Test invalid JSON response handling

### Integration Tests
1. End-to-end FHIR retrieval from mock peripheral node
2. Verify Content-Type header is `application/fhir+json`
3. Verify audit logging for all scenarios
4. Test with various FHIR resource types (Bundle, DocumentReference, DiagnosticReport)

### Manual Testing

```bash
# Test successful retrieval
curl -X GET "http://localhost:8080/hcen/api/clinical-history/documents/1/fhir?patientCi=12345678" \
  -H "Accept: application/fhir+json"

# Expected: 200 OK with FHIR JSON
# Content-Type: application/fhir+json; charset=utf-8

# Test authorization failure
curl -X GET "http://localhost:8080/hcen/api/clinical-history/documents/1/fhir?patientCi=99999999" \
  -H "Accept: application/fhir+json"

# Expected: 403 Forbidden

# Test missing patientCi
curl -X GET "http://localhost:8080/hcen/api/clinical-history/documents/1/fhir" \
  -H "Accept: application/fhir+json"

# Expected: 400 Bad Request
```

## Error Handling

### Business Logic Errors
- `DocumentRetrievalException("Documento no encontrado")` → 404 Not Found
- `DocumentRetrievalException("Acceso no autorizado")` → 403 Forbidden
- `DocumentRetrievalException("Error comunicándose con nodo periférico")` → 502 Bad Gateway
- `DocumentRetrievalException("Formato de documento inválido")` → 500 Internal Server Error

### Response Format (Error)
```json
{
  "error": "FORBIDDEN",
  "message": "Access denied to document 123",
  "timestamp": "2025-11-18T10:30:00"
}
```

## Performance Considerations

- FHIR documents returned as raw strings (no parsing overhead)
- Connection pooling handled by PeripheralNodeClient
- Circuit breaker prevents cascading failures
- No caching (medical data is sensitive and may change)

## Future Enhancements

1. **JWT Authentication**: Replace query parameter with JWT token from SecurityContext
2. **FHIR Validation**: Add optional HAPI FHIR validation for compliance checking
3. **Caching**: Consider short-lived cache for frequently accessed documents
4. **Compression**: Enable gzip compression for large FHIR documents
5. **Pagination**: Support pagination for FHIR Bundles with many entries
6. **Format Negotiation**: Support XML format (application/fhir+xml) via Accept header

## Compliance

### Architecture Requirements
- ✅ **AC015**: Document retrieval from peripheral nodes
- ✅ **AC002-AC004**: HTTPS communication with peripheral nodes
- ✅ **AC026**: Audit logging of all access attempts
- ✅ **AC005**: Patient authorization enforced

### FHIR Standards
- Supports FHIR R4 format (most common)
- Compatible with IPS-FHIR (International Patient Summary)
- Returns raw FHIR without transformation (peripheral node responsible for compliance)

## Build and Deployment

### Build Status
- ✅ Compilation successful
- ✅ No compilation errors
- ✅ Integration with existing codebase verified

### Deployment Notes
- No database schema changes required
- No WildFly configuration changes required
- Backward compatible with existing endpoints
- Can be deployed independently

## Acceptance Criteria

All acceptance criteria met:
- ✅ REST endpoint accepts document ID and returns FHIR JSON
- ✅ Authorization checks patient owns document
- ✅ Calls peripheral node with correct Accept header
- ✅ Returns FHIR document as-is (no transformation)
- ✅ Logs access to audit system
- ✅ Handles all error cases gracefully
- ✅ Compiles successfully
- ✅ Content-Type header is `application/fhir+json`

## Author
TSE 2025 Group 9

## Date
2025-11-18
