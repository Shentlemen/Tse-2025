# Clinic Registration Endpoint Implementation

## Overview

This document describes the implementation of the clinic registration endpoint (`POST /api/clinics`) in the Clinic peripheral component. This endpoint allows HCEN central to register new clinics when they are onboarded to the platform.

## Implementation Date
**2025-11-19**

## Problem Solved

HCEN central was receiving HTTP 405 "Method Not Allowed" when attempting to register clinics because the endpoint didn't exist in the clinic project. This implementation resolves that issue.

## Files Created

### 1. ClinicRegistrationRequest DTO
**File:** `C:\Users\agust\fing\tse\tse-2025\clinic\src\main\java\uy\gub\clinic\web\api\dto\ClinicRegistrationRequest.java`

**Purpose:** Data Transfer Object for clinic registration requests from HCEN

**Fields:**
- `code` (String, required): Unique clinic identifier from HCEN (format: clinic-{uuid})
- `name` (String, required): Official clinic name
- `description` (String, optional): Clinic description
- `address` (String, optional): Physical address
- `phone` (String, optional): Contact phone number
- `email` (String, optional with @Email validation): Contact email
- `hcen_endpoint` (String, required): HCEN central API URL
- `active` (Boolean, required, default: true): Active status

**Validation:**
- Uses Jakarta Bean Validation annotations (@NotBlank, @Email, @Size)
- Ensures required fields are present
- Validates email format
- Enforces field length constraints

### 2. ClinicResource JAX-RS Resource
**File:** `C:\Users\agust\fing\tse\tse-2025\clinic\src\main\java\uy\gub\clinic\web\api\ClinicResource.java`

**Purpose:** JAX-RS REST endpoint for handling clinic registration requests

**Endpoint:** `POST /api/clinics`

**Authentication:** Requires valid API key in `X-API-Key` header (validated by `ApiKeyAuthenticationFilter`)

**Request Example:**
```json
POST http://localhost:8080/clinic/api/clinics
Content-Type: application/json
X-API-Key: your-api-key-here

{
  "code": "clinic-550e8400-e29b-41d4-a716-446655440000",
  "name": "Clínica San José",
  "description": "Clínica privada de atención médica integral",
  "address": "Av. 18 de Julio 1234",
  "phone": "024123456",
  "email": "contacto@clinicasanjose.com.uy",
  "hcen_endpoint": "https://hcen.uy/api",
  "active": true
}
```

**Response Codes:**

| Code | Description | Example |
|------|-------------|---------|
| 201 Created | Clinic successfully registered | Returns created clinic data |
| 400 Bad Request | Invalid request data (missing required fields, validation errors) | `{"error": "Clinic code is required"}` |
| 401 Unauthorized | Missing or invalid API key | `{"error": "API key required..."}` |
| 409 Conflict | Clinic with this code already exists | `{"error": "Clinic with code '...' already exists"}` |
| 500 Internal Server Error | Server error during registration | `{"error": "Internal server error..."}` |

**Success Response Example:**
```json
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": "clinic-550e8400-e29b-41d4-a716-446655440000",
  "code": "clinic-550e8400-e29b-41d4-a716-446655440000",
  "name": "Clínica San José",
  "description": "Clínica privada de atención médica integral",
  "address": "Av. 18 de Julio 1234",
  "phone": "024123456",
  "email": "contacto@clinicasanjose.com.uy",
  "hcen_endpoint": "https://hcen.uy/api",
  "active": true,
  "createdAt": "2025-11-19T14:30:00"
}
```

## Architecture

### Request Flow

```
1. HCEN Central
   └─> POST /api/clinics (with X-API-Key header)
       │
2. ApiKeyAuthenticationFilter
   ├─> Validates API key against configuration (for clinic registration)
   └─> If valid: allows request to proceed
       │
3. ClinicResource.registerClinic()
   ├─> Validates request data (@Valid annotations)
   ├─> Checks for duplicate clinic code
   ├─> Maps DTO to Clinic entity
   └─> Calls ClinicService.createClinic()
       │
4. ClinicService.createClinic()
   ├─> Stores clinic in hardcoded map (development)
   └─> Returns created clinic
       │
5. ClinicResource
   ├─> Builds response DTO
   └─> Returns HTTP 201 with clinic data
```

### Security

**Authentication:** API Key validation via `ApiKeyAuthenticationFilter`
- For `POST /api/clinics`: Validates against configuration API key (`ApiConfigurationService`)
- For other endpoints: Validates against clinic-specific API key (stored in database)

**Authorization:** Only HCEN central (with valid configuration API key) can register clinics

**Data Validation:**
- Jakarta Bean Validation (@Valid, @NotBlank, @Email)
- Additional validation in resource method
- Duplicate detection (by clinic code)

### Data Mapping

**DTO to Entity Mapping:**
```java
ClinicRegistrationRequest → Clinic entity
├─ code → id (clinic ID from HCEN becomes the primary key)
├─ code → code (also stored as code field)
├─ name → name
├─ description → description
├─ address → address
├─ phone → phone
├─ email → email
├─ hcen_endpoint → hcenEndpoint
└─ active → active (default: true)
```

**Entity to Response Mapping:**
```java
Clinic entity → ClinicRegistrationResponse
├─ id → id
├─ code → code
├─ name → name
├─ description → description
├─ address → address
├─ phone → phone
├─ email → email
├─ hcenEndpoint → hcenEndpoint
├─ active → active
└─ createdAt → createdAt (auto-generated timestamp)
```

## Integration with Existing Code

### 1. FhirApplication
**File:** `C:\Users\agust\fing\tse\tse-2025\clinic\src\main\java\uy\gub\clinic\web\FhirApplication.java`

- Defines application path as `/api`
- Auto-discovers JAX-RS resources (including new `ClinicResource`)
- No changes required

### 2. ApiKeyAuthenticationFilter
**File:** `C:\Users\agust\fing\tse\tse-2025\clinic\src\main\java\uy\gub\clinic\web\api\filter\ApiKeyAuthenticationFilter.java`

- Already configured to handle `POST /api/clinics` with configuration API key validation
- No changes required

### 3. ClinicService
**File:** `C:\Users\agust\fing\tse\tse-2025\clinic\src\main\java\uy\gub\clinic\service\ClinicService.java`

- `createClinic(Clinic clinic)` method (line 214) already exists
- Stores clinic in hardcoded map for development
- Will persist to database in production
- No changes required

### 4. Clinic Entity
**File:** `C:\Users\agust\fing\tse\tse-2025\clinic\src\main\java\uy\gub\clinic\entity\Clinic.java`

- Entity with all required fields
- Auto-generates ID in constructor (overridden by `setId()` in mapping)
- Includes validation annotations
- No changes required

## Testing

### Manual Testing with cURL

```bash
# Test clinic registration
curl -X POST http://localhost:8080/clinic/api/clinics \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-configured-api-key" \
  -d '{
    "code": "clinic-test-001",
    "name": "Test Clinic",
    "description": "Test clinic for development",
    "address": "Test Address 123",
    "phone": "099123456",
    "email": "test@clinic.com",
    "hcen_endpoint": "http://localhost:8080/hcen/api",
    "active": true
  }'

# Expected: HTTP 201 with created clinic data
```

### Error Cases to Test

1. **Missing API Key:**
```bash
curl -X POST http://localhost:8080/clinic/api/clinics \
  -H "Content-Type: application/json" \
  -d '{"code": "clinic-001", "name": "Test"}'

# Expected: HTTP 401 Unauthorized
```

2. **Invalid API Key:**
```bash
curl -X POST http://localhost:8080/clinic/api/clinics \
  -H "Content-Type: application/json" \
  -H "X-API-Key: invalid-key" \
  -d '{"code": "clinic-001", "name": "Test"}'

# Expected: HTTP 401 Unauthorized
```

3. **Missing Required Fields:**
```bash
curl -X POST http://localhost:8080/clinic/api/clinics \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-configured-api-key" \
  -d '{"description": "Missing required fields"}'

# Expected: HTTP 400 Bad Request
```

4. **Duplicate Clinic Code:**
```bash
# First request
curl -X POST http://localhost:8080/clinic/api/clinics \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-configured-api-key" \
  -d '{"code": "clinic-001", "name": "Test", "hcen_endpoint": "http://test"}'

# Second request with same code
curl -X POST http://localhost:8080/clinic/api/clinics \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-configured-api-key" \
  -d '{"code": "clinic-001", "name": "Duplicate", "hcen_endpoint": "http://test"}'

# Expected: HTTP 409 Conflict
```

5. **Invalid Email Format:**
```bash
curl -X POST http://localhost:8080/clinic/api/clinics \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-configured-api-key" \
  -d '{
    "code": "clinic-001",
    "name": "Test",
    "email": "not-an-email",
    "hcen_endpoint": "http://test"
  }'

# Expected: HTTP 400 Bad Request (validation error)
```

### Integration Testing with HCEN

Once both HCEN and Clinic applications are running:

1. **Start Clinic Application:**
```bash
cd C:\Users\agust\fing\tse\tse-2025\clinic
gradlew wildFlyRun
```

2. **Start HCEN Application:**
```bash
cd C:\Users\agust\fing\tse\tse-2025\hcen
gradlew wildFlyRun
```

3. **Trigger Clinic Registration from HCEN:**
- Access HCEN admin portal
- Navigate to clinic management
- Click "Register New Clinic"
- Fill in clinic details
- Submit

4. **Verify Registration:**
- Check HCEN logs for successful registration
- Check Clinic logs for received registration request
- Verify clinic appears in Clinic application

## Configuration Required

### API Key Configuration

The `ApiKeyAuthenticationFilter` expects an API key to be configured. Ensure the following is set:

**File:** `C:\Users\agust\fing\tse\tse-2025\clinic\src\main\java\uy\gub\clinic\config\ApiConfigurationService.java`

This service should provide a method to validate the API key used for clinic registration.

**Example Configuration (environment variable or properties file):**
```properties
# API key for HCEN to register clinics
hcen.registration.api.key=your-secure-api-key-here
```

## Logging

The implementation includes comprehensive logging:

**INFO Level:**
- Clinic registration request received (code, name)
- Clinic registered successfully (ID, code, name)

**WARN Level:**
- Validation failures (missing code, missing name, missing HCEN endpoint)
- Duplicate clinic code attempts

**ERROR Level:**
- Internal errors during registration

**Log Examples:**
```
INFO  [uy.gub.clinic.web.api.ClinicResource] Clinic registration request received - code: clinic-001, name: Test Clinic
INFO  [uy.gub.clinic.service.ClinicService] Clínica creada: Clinic{id=clinic-001, name='Test Clinic', code='clinic-001', active=true}
INFO  [uy.gub.clinic.web.api.ClinicResource] Clinic registered successfully - ID: clinic-001, code: clinic-001, name: Test Clinic
```

## Future Enhancements

1. **Database Persistence:**
   - Currently uses in-memory hardcoded map for development
   - Production should persist to PostgreSQL database
   - Requires `@PersistenceContext` and `entityManager.persist()`

2. **Unit Tests:**
   - Create JUnit tests for `ClinicResource`
   - Mock `ClinicService` to test validation logic
   - Test all error cases (400, 409, 500)

3. **Integration Tests:**
   - Test full request/response cycle with WildFly
   - Test API key authentication
   - Test database persistence

4. **Enhanced Validation:**
   - Validate HCEN endpoint URL format
   - Validate phone number format
   - Custom validators for business rules

5. **Audit Logging:**
   - Log who registered each clinic
   - Track registration timestamp
   - Store registration source (HCEN admin user)

6. **Idempotency:**
   - Support idempotent registration (PUT instead of POST)
   - Allow updates if clinic already exists
   - Return 200 OK if clinic unchanged

## Related Files

### HCEN Side

**DTO:** `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\integration\clinic\dto\ClinicServiceRegistrationRequest.java`

**Client:** Should exist in HCEN to send registration requests

### Clinic Side

**Resource:** `C:\Users\agust\fing\tse\tse-2025\clinic\src\main\java\uy\gub\clinic\web\api\ClinicResource.java`

**DTO:** `C:\Users\agust\fing\tse\tse-2025\clinic\src\main\java\uy\gub\clinic\web\api\dto\ClinicRegistrationRequest.java`

**Service:** `C:\Users\agust\fing\tse\tse-2025\clinic\src\main\java\uy\gub\clinic\service\ClinicService.java`

**Entity:** `C:\Users\agust\fing\tse\tse-2025\clinic\src\main\java\uy\gub\clinic\entity\Clinic.java`

**Filter:** `C:\Users\agust\fing\tse\tse-2025\clinic\src\main\java\uy\gub\clinic\web\api\filter\ApiKeyAuthenticationFilter.java`

## Troubleshooting

### Issue: HTTP 405 Method Not Allowed
**Cause:** JAX-RS resource not discovered or @POST annotation missing
**Solution:** Verify `ClinicResource` is in correct package and has `@POST` annotation

### Issue: HTTP 401 Unauthorized
**Cause:** Missing or invalid API key
**Solution:** Check `X-API-Key` header and `ApiConfigurationService` configuration

### Issue: HTTP 409 Conflict
**Cause:** Clinic with this code already exists
**Solution:** Use different clinic code or delete existing clinic

### Issue: HTTP 500 Internal Server Error
**Cause:** Exception during clinic creation
**Solution:** Check server logs for stack trace and fix underlying issue

### Issue: Request Not Reaching Resource
**Cause:** Filter blocking request
**Solution:** Verify `ApiKeyAuthenticationFilter` is allowing POST to /api/clinics

## Summary

This implementation successfully creates the missing JAX-RS endpoint for clinic registration in the Clinic peripheral component. HCEN central can now register clinics via `POST /api/clinics` with proper authentication, validation, and error handling.

**Key Features:**
- RESTful API endpoint with JAX-RS
- Bean validation for request data
- API key authentication
- Duplicate detection
- Comprehensive error handling
- Detailed logging
- Clean architecture (DTO → Entity mapping)

**Next Steps:**
1. Deploy to WildFly
2. Test with HCEN integration
3. Add unit tests
4. Configure API key in environment
5. Verify database persistence (if enabled)
