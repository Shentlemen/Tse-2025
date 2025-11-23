# FHIR Document Access Control Implementation

## Overview

Successfully updated the `getFhirDocument` method to support professional access with policy evaluation, matching the exact pattern used in `getDocumentContent`.

## Changes Made

### 1. REST Endpoint (`ClinicalHistoryResource.java`)

**File:** `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\clinicalhistory\api\rest\ClinicalHistoryResource.java`

**Lines Modified:** 799-1067

#### Method Signature Update
- **Old Signature (line 855):**
  ```java
  public Response getFhirDocument(
      @PathParam("documentId") Long documentId,
      @QueryParam("patientCi") String patientCi,
      @Context HttpServletRequest request)
  ```

- **New Signature (line 868):**
  ```java
  public Response getFhirDocument(
      @PathParam("documentId") Long documentId,
      @QueryParam("patientCi") String patientCi,
      @QueryParam("specialty") String specialty,
      @QueryParam("professionalId") String professionalId,
      @Context jakarta.ws.rs.core.SecurityContext securityContext,
      @Context HttpServletRequest request)
  ```

#### Implementation Changes

1. **Clinic ID Extraction (lines 883-890):**
   - Extract `requestingClinicId` from security context
   - Detect if this is a professional/clinic request
   - Log clinic ID for audit trail

2. **Specialty Validation (lines 905-911):**
   - Validate specialty parameter is provided for clinic requests
   - Return 400 Bad Request if specialty is missing
   - Clear error message: "Specialty is required for professional/clinic FHIR document access"

3. **Patient CI Normalization (lines 901-903):**
   - Add "uy-ci-" prefix if not present
   - Ensures consistency with INUS user identifiers

4. **Service Call (lines 919-927):**
   - Pass all parameters to service layer
   - Service handles policy evaluation internally

5. **Exception Handling (lines 940-1066):**
   - Added EJB exception unwrapping (catches wrapped exceptions)
   - Added `ClinicalDocumentAccessDenied` handling (returns 403 Forbidden)
   - Updated audit logging to use `professionalId` and `requestingClinicId`
   - Improved error messages for peripheral node failures

#### JavaDoc Updates

**Updated JavaDoc (lines 799-864):**
- Added description of professional access support
- Added new parameters: `specialty`, `professionalId`, `securityContext`
- Added example for professional access:
  ```
  GET /api/clinical-history/documents/123/fhir?patientCi=12345678&specialty=CAR&professionalId=prof001
  ```
- Updated return codes to include 403 for policy denial
- Clarified access control behavior:
  - Patient access: Only `patientCi` required, no policy evaluation
  - Professional access: Requires `specialty`, evaluates policies and approved access requests

---

### 2. Service Layer (`ClinicalHistoryService.java`)

**File:** `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\clinicalhistory\service\ClinicalHistoryService.java`

**Lines Modified:** 466-631

#### Method Signature Update

- **Old Signature (line 491):**
  ```java
  public String getFhirDocument(Long documentId, String patientCi)
  ```

- **New Signature (line 505):**
  ```java
  public String getFhirDocument(
      Long documentId,
      String patientCi,
      String requestingClinicId,
      String specialty,
      String professionalId)
  ```

#### Implementation Changes

1. **Enhanced Logging (lines 512-513):**
   - Added `clinicId` and `specialty` to log messages
   - Provides better audit trail for debugging

2. **Policy Evaluation (lines 546-552):**
   - Added conditional policy evaluation after patient ownership check
   - Only evaluates policies if `requestingClinicId` is not null/empty
   - Calls existing `evaluateAccessPolicies()` method
   - Logs whether patient or professional request
   - Pattern:
     ```java
     if (requestingClinicId != null && !requestingClinicId.trim().isEmpty()) {
         LOGGER.log(Level.INFO, "Professional/clinic FHIR request detected - evaluating access policies");
         evaluateAccessPolicies(document, requestingClinicId, specialty, professionalId);
     } else {
         LOGGER.log(Level.INFO, "Patient FHIR request - skipping policy evaluation");
     }
     ```

3. **Step Numbering Update (lines 554-604):**
   - Updated step numbers to account for new policy evaluation step
   - Step 3: Policy evaluation (NEW)
   - Step 4: Validate document locator
   - Step 5: Look up clinic API key
   - Step 6: Retrieve from peripheral node
   - Step 7: Convert to JSON string
   - Step 8: Validate JSON structure
   - Step 9: Log successful access

4. **Exception Handling (lines 617-630):**
   - Added explicit `ClinicalDocumentAccessDenied` catch block
   - Re-throws with logging for policy denials
   - Maintains existing exception handling for other failures

#### JavaDoc Updates

**Updated JavaDoc (lines 466-504):**
- Added description: "Supports both patient self-access and professional access with policy evaluation"
- Added new step in flow: "If professional/clinic request: Evaluates access policies (including approved access requests)"
- Added "Access Control" section explaining:
  - Patient Access: No policy evaluation (patient owns their data)
  - Professional Access: Requires `requestingClinicId` and `specialty`, evaluates policies
- Added new parameters: `@param requestingClinicId`, `@param specialty`, `@param professionalId`
- Added `@throws ClinicalDocumentAccessDenied` for policy denial

---

## Expected Behavior

### Patient Access (No Change)
```http
GET /api/clinical-history/documents/123/fhir?patientCi=12345678
```
**Flow:**
1. Validate patient CI
2. Get document from RNDC
3. Verify patient owns document
4. **Skip policy evaluation** (patient access)
5. Retrieve FHIR JSON from peripheral node
6. Return 200 OK with FHIR JSON

**Result:** Patient can access their own documents without policy evaluation

---

### Professional Access (NEW)
```http
GET /api/clinical-history/documents/123/fhir?patientCi=12345678&specialty=CAR&professionalId=prof001
```
**Flow:**
1. Extract `clinicId` from security context (JWT)
2. Validate patient CI and specialty
3. Get document from RNDC
4. Verify patient owns document
5. **Evaluate access policies** (including approved access requests)
   - Check document type policies
   - Check specialty policies
   - Check approved access requests
6. If access PERMITTED → Retrieve FHIR JSON
7. If access DENIED/PENDING → Return 403 Forbidden

**Result:** Professional access is controlled by patient-defined policies

---

## HTTP Status Codes

### Success
- **200 OK** - Document retrieved successfully

### Client Errors
- **400 Bad Request**
  - Missing `patientCi` parameter
  - Missing `specialty` parameter for clinic requests
- **403 Forbidden**
  - Patient doesn't own document
  - Policy denies access
  - Access pending patient approval
- **404 Not Found**
  - Document doesn't exist in RNDC

### Server Errors
- **502 Bad Gateway** - Peripheral node unavailable
- **500 Internal Server Error** - Other failures (invalid JSON, etc.)

---

## Security Features

### Authentication
- **Patient Access:** Validated via JWT (patientCi claim)
- **Professional Access:** Validated via JWT (clinicId extracted from security context)

### Authorization
- **Patient Ownership Check:** Always validates document belongs to patient
- **Policy Evaluation:** Evaluates patient-defined access policies for professional requests
- **Approved Access Requests:** Checks for manually approved access requests

### Audit Logging
- **Success:** Logs successful access with actor, document, outcome
- **Failure:** Logs failed access attempts with reason
- **Denial:** Logs policy denials with details

---

## Testing Recommendations

### Unit Tests
1. **Test patient access without specialty parameter**
   - Should succeed if patient owns document
   - Should not evaluate policies

2. **Test professional access without specialty**
   - Should return 400 Bad Request
   - Should include clear error message

3. **Test professional access with PERMIT policy**
   - Should evaluate policies
   - Should return FHIR JSON
   - Should log access

4. **Test professional access with DENY policy**
   - Should return 403 Forbidden
   - Should log denial

5. **Test professional access with approved access request**
   - Should grant access even if no matching policy
   - Should log access

### Integration Tests
1. **End-to-end patient access flow**
   - Patient requests their own document
   - Verify no policy evaluation
   - Verify document returned

2. **End-to-end professional access flow**
   - Professional with valid policy requests document
   - Verify policy evaluation
   - Verify document returned

3. **End-to-end policy denial flow**
   - Professional without valid policy requests document
   - Verify 403 Forbidden
   - Verify audit log

---

## Consistency with `getDocumentContent`

This implementation follows the **exact same pattern** as `getDocumentContent`:

| Aspect | getDocumentContent | getFhirDocument |
|--------|-------------------|----------------|
| **Signature** | Has specialty, professionalId, securityContext | Has specialty, professionalId, securityContext |
| **Clinic ID Extraction** | Extracts from securityContext | Extracts from securityContext |
| **Specialty Validation** | Required for clinic requests | Required for clinic requests |
| **Policy Evaluation** | Calls evaluateAccessPolicies() | Calls evaluateAccessPolicies() |
| **Exception Handling** | Catches ClinicalDocumentAccessDenied | Catches ClinicalDocumentAccessDenied |
| **Audit Logging** | Uses professionalId and clinicId | Uses professionalId and clinicId |

---

## Related Files

### Modified Files
1. **ClinicalHistoryResource.java** - REST endpoint for FHIR document retrieval
2. **ClinicalHistoryService.java** - Service layer with policy evaluation

### Unchanged Files (Used by Implementation)
1. **PolicyEngine.java** - Evaluates access policies
2. **AccessRequestRepository.java** - Checks approved access requests
3. **AuditService.java** - Logs access events
4. **RndcRepository.java** - Retrieves document metadata
5. **PeripheralNodeClient.java** - Retrieves documents from peripheral nodes

---

## Build Status

**Build:** SUCCESS
**Compiler Warnings:** None related to changes
**Tests Skipped:** Yes (run with `./gradlew test` for full test suite)

---

## Next Steps

1. **Run Tests:**
   ```bash
   ./gradlew test --tests "*ClinicalHistory*"
   ```

2. **Deploy to WildFly:**
   ```bash
   ./gradlew wildFlyDeploy
   ```

3. **Test Endpoints:**
   - Patient access: `GET /api/clinical-history/documents/123/fhir?patientCi=12345678`
   - Professional access: `GET /api/clinical-history/documents/123/fhir?patientCi=12345678&specialty=CAR&professionalId=prof001`

4. **Verify Audit Logs:**
   - Check database for access logs in `audit_logs` table
   - Verify actor, outcome, and timestamp

---

## Implementation Completed

**Date:** 2025-11-23
**Status:** COMPLETE
**Build Status:** SUCCESS
**Pattern Consistency:** VERIFIED (matches getDocumentContent)
