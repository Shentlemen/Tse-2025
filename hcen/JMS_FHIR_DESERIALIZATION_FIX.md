# JMS FHIR DocumentReference Deserialization Fix

## Problem Summary

The `DocumentRegistrationListener` MDB was receiving JMS messages with **FHIR DocumentReference** payloads, but after deserialization, all fields were **null**. This was causing document registration to fail.

## Root Cause Analysis

### Mismatch Between Sender and Receiver

**What the sender (prestador-de-salud) was sending:**
```json
{
  "payload": {
    "resourceType": "DocumentReference",
    "id": "doc-5",
    "subject": {"reference": "Patient/33333333"},
    "custodian": {"reference": "Organization/clinic-1"},
    "author": [{"reference": "Practitioner/professional-5"}],
    "type": {"coding": [{"display": "Note"}]},
    "content": [{
      "attachment": {
        "url": "http://localhost:8080/api/documents/5",
        "hash": "abc123...",
        "creation": "2025-11-18T01:09:08-03:00",
        "title": "Consulta General"
      }
    }],
    "description": "Consulta médica de control general"
  },
  "sourceSystem": "prestador-de-salud",
  "messageId": "msg-...",
  "eventType": "document-create",
  "timestamp": "2025-11-18T01:09:08.961397100"
}
```

**What the receiver (HCEN) was expecting:**
```json
{
  "payload": {
    "patientCI": "33333333",
    "documentType": "CLINICAL_NOTE",
    "documentLocator": "http://localhost:8080/api/documents/5",
    "documentHash": "abc123...",
    "createdBy": "professional-5",
    "createdAt": "2025-11-18T01:09:08",
    "clinicId": "clinic-1"
  },
  "sourceSystem": "prestador-de-salud",
  "messageId": "msg-...",
  "eventType": "document-create",
  "timestamp": "2025-11-18T01:09:08.961397100"
}
```

### Why Deserialization Failed

1. **Field name mismatch**: FHIR uses `resourceType`, `subject`, `content`, etc., but DTO expected `patientCI`, `documentLocator`, etc.
2. **Nested structure**: Patient CI was in `subject.reference`, not a direct field
3. **No extraction logic**: Jackson couldn't map FHIR structure to simple DTO fields
4. **Result**: All fields remained `null` after deserialization

## Solution

### Architecture

Created a **FHIR-aware deserialization layer** that:
1. Automatically detects FHIR vs. simple payload format
2. Extracts required fields from FHIR DocumentReference structure
3. Maps FHIR types to internal DocumentType enum
4. Maintains backward compatibility with simple format

### Components Created

#### 1. FhirDocumentRegistrationPayload
**File**: `src/main/java/uy/gub/hcen/messaging/dto/FhirDocumentRegistrationPayload.java`

Extends `DocumentRegistrationPayload` and adds:
- FHIR field annotations (`@JsonProperty`)
- Automatic field extraction in setters
- FHIR-to-internal type mapping
- Reference parsing (e.g., "Patient/33333333" → "33333333")

**Key Features**:
- Parses `subject.reference` → `patientCI`
- Parses `custodian.reference` → `clinicId`
- Parses `author[0].reference` → `createdBy`
- Parses `content[0].attachment.url` → `documentLocator`
- Parses `content[0].attachment.hash` → `documentHash`
- Parses `content[0].attachment.title` → `documentTitle`
- Parses `description` → `documentDescription`
- Maps `type.coding[0].display` → `documentType` enum

#### 2. DocumentPayloadDeserializer
**File**: `src/main/java/uy/gub/hcen/messaging/deserializer/DocumentPayloadDeserializer.java`

Custom Jackson deserializer that:
- Checks for `resourceType` field to detect FHIR format
- If FHIR: Deserializes to `FhirDocumentRegistrationPayload`
- If simple: Deserializes to `DocumentRegistrationPayload`
- Logs format detection for debugging

#### 3. Updated DocumentRegistrationMessage
**File**: `src/main/java/uy/gub/hcen/messaging/dto/DocumentRegistrationMessage.java`

Changes:
- Added `@JsonDeserialize(using = DocumentPayloadDeserializer.class)` to `payload` field
- Added `@JsonIgnoreProperties(ignoreUnknown = true)` for resilience
- Updated JavaDoc to document FHIR support

#### 4. Updated BaseMessage
**File**: `src/main/java/uy/gub/hcen/messaging/dto/BaseMessage.java`

Changes:
- Added `@JsonIgnoreProperties(ignoreUnknown = true)` for resilience

## Field Mapping

| FHIR DocumentReference Path | DocumentRegistrationPayload Field | Extraction Logic |
|-----------------------------|-----------------------------------|------------------|
| `subject.reference` | `patientCI` | Extract ID after "/" (e.g., "Patient/33333333" → "33333333") |
| `custodian.reference` | `clinicId` | Extract ID after "/" (e.g., "Organization/clinic-1" → "clinic-1") |
| `author[0].reference` | `createdBy` | Extract ID after "/" (e.g., "Practitioner/professional-5" → "professional-5") |
| `content[0].attachment.url` | `documentLocator` | Direct mapping |
| `content[0].attachment.hash` | `documentHash` | Direct mapping |
| `content[0].attachment.title` | `documentTitle` | Direct mapping |
| `content[0].attachment.creation` | `createdAt` | Parse ISO-8601 to LocalDateTime |
| `description` | `documentDescription` | Direct mapping |
| `type.coding[0].display` | `documentType` | Map string to enum (e.g., "Note" → CLINICAL_NOTE) |

## Type Mapping

FHIR document types are mapped to internal `DocumentType` enum:

| FHIR Type (display) | Internal DocumentType |
|---------------------|----------------------|
| Contains "Lab" or "Laboratory" | `LAB_RESULT` |
| Contains "Image" or "Imaging" | `IMAGING` |
| Contains "Prescription" or "Medication" | `PRESCRIPTION` |
| Contains "Discharge" | `DISCHARGE_SUMMARY` |
| Contains "Referral" | `REFERRAL` |
| Contains "Consent" | `INFORMED_CONSENT` |
| Contains "Surgical" or "Surgery" | `SURGICAL_REPORT` |
| Contains "Pathology" | `PATHOLOGY_REPORT` |
| Contains "Consultation" | `CONSULTATION` |
| Contains "Emergency" | `EMERGENCY_REPORT` |
| Contains "Progress" | `PROGRESS_NOTE` |
| Contains "Allergy" | `ALLERGY_RECORD` |
| Contains "Vital" | `VITAL_SIGNS` |
| Contains "Diagnostic" | `DIAGNOSTIC_REPORT` |
| Contains "Treatment" or "Care Plan" | `TREATMENT_PLAN` |
| Contains "Vaccination" or "Immunization" | `VACCINATION_RECORD` |
| Contains "Note" | `CLINICAL_NOTE` |
| Unknown | `OTHER` |

## Benefits

### 1. Interoperability
- HCEN Central can now receive FHIR messages from peripheral nodes
- Peripheral nodes don't need to transform to proprietary format
- Standards-based integration (HL7 FHIR R4)

### 2. Backward Compatibility
- Simple format still works (for legacy peripheral nodes)
- No breaking changes to existing code
- Automatic format detection (zero configuration)

### 3. Maintainability
- Clean separation of concerns (deserializer handles format detection)
- Easy to extend (add new FHIR resources or fields)
- Well-documented mapping logic

### 4. Robustness
- `@JsonIgnoreProperties(ignoreUnknown = true)` prevents failures on extra fields
- Fallback handling for missing fields
- Comprehensive logging for debugging

## Testing

### Test Case 1: FHIR DocumentReference Deserialization
```java
String fhirJson = """
{
  "payload": {
    "resourceType": "DocumentReference",
    "id": "doc-5",
    "subject": {"reference": "Patient/33333333"},
    "custodian": {"reference": "Organization/clinic-1"},
    "author": [{"reference": "Practitioner/professional-5"}],
    "type": {"coding": [{"display": "Note"}]},
    "content": [{
      "attachment": {
        "url": "http://localhost:8080/api/documents/5",
        "hash": "abc123",
        "creation": "2025-11-18T01:09:08-03:00",
        "title": "Consulta General"
      }
    }],
    "description": "Consulta médica"
  },
  "sourceSystem": "prestador-de-salud",
  "messageId": "msg-123",
  "eventType": "document-create",
  "timestamp": "2025-11-18T01:09:08.961397100"
}
""";

ObjectMapper mapper = new ObjectMapper()
    .registerModule(new JavaTimeModule());

DocumentRegistrationMessage message = mapper.readValue(fhirJson, DocumentRegistrationMessage.class);

// Assertions
assertNotNull(message.getPayload());
assertEquals("33333333", message.getPayload().getPatientCI());
assertEquals("clinic-1", message.getPayload().getClinicId());
assertEquals("professional-5", message.getPayload().getCreatedBy());
assertEquals("http://localhost:8080/api/documents/5", message.getPayload().getDocumentLocator());
assertEquals("abc123", message.getPayload().getDocumentHash());
assertEquals("Consulta General", message.getPayload().getDocumentTitle());
assertEquals("Consulta médica", message.getPayload().getDocumentDescription());
assertEquals(DocumentType.CLINICAL_NOTE, message.getPayload().getDocumentType());
```

### Test Case 2: Simple Payload Deserialization (Backward Compatibility)
```java
String simpleJson = """
{
  "payload": {
    "patientCI": "33333333",
    "documentType": "CLINICAL_NOTE",
    "documentLocator": "http://localhost:8080/api/documents/5",
    "documentHash": "abc123",
    "createdBy": "professional-5",
    "createdAt": "2025-11-18T01:09:08",
    "clinicId": "clinic-1"
  },
  "sourceSystem": "prestador-de-salud",
  "messageId": "msg-123",
  "eventType": "document-create",
  "timestamp": "2025-11-18T01:09:08"
}
""";

DocumentRegistrationMessage message = mapper.readValue(simpleJson, DocumentRegistrationMessage.class);

// Assertions (should still work)
assertNotNull(message.getPayload());
assertEquals("33333333", message.getPayload().getPatientCI());
```

## Deployment

### Build and Deploy
```bash
cd hcen
./gradlew clean build
./gradlew war

# Deploy to WildFly
cp build/libs/hcen.war $WILDFLY_HOME/standalone/deployments/
```

### Verification Steps

1. **Check WildFly logs** for successful MDB deployment:
   ```
   INFO  [org.jboss.as.ejb3.deployment] (MSC service thread 1-2) WFLYEJB0473: JNDI bindings for session bean named 'DocumentRegistrationListener' in deployment unit 'hcen.war' are as follows:
   ```

2. **Send test message** to queue:
   ```bash
   # Using JMS client or peripheral node
   ```

3. **Check logs** for deserialization success:
   ```
   INFO  Detected FHIR payload with resourceType: DocumentReference
   INFO  Successfully deserialized FHIR payload - Patient CI: 33333333, Locator: http://..., Type: CLINICAL_NOTE
   INFO  Document registration completed successfully
   ```

4. **Verify database** for registered document:
   ```sql
   SELECT * FROM rndc.documents WHERE patient_ci = '33333333';
   ```

## Future Enhancements

1. **Full FHIR Support**:
   - Use HAPI FHIR library for parsing (already in dependencies)
   - Support all FHIR DocumentReference fields
   - Validate against FHIR StructureDefinition

2. **Terminology Mapping**:
   - Use FHIR terminology service for type mapping
   - Support LOINC, SNOMED CT codes
   - Configurable mapping tables

3. **Validation**:
   - Validate FHIR resources against profiles
   - Check required fields
   - Verify reference integrity

4. **Performance**:
   - Cache parsed FHIR resources
   - Optimize deserialization for high throughput
   - Connection pooling for terminology services

## Files Modified/Created

### Created
- `src/main/java/uy/gub/hcen/messaging/dto/FhirDocumentRegistrationPayload.java`
- `src/main/java/uy/gub/hcen/messaging/deserializer/DocumentPayloadDeserializer.java`
- `JMS_FHIR_DESERIALIZATION_FIX.md` (this file)

### Modified
- `src/main/java/uy/gub/hcen/messaging/dto/DocumentRegistrationMessage.java`
- `src/main/java/uy/gub/hcen/messaging/dto/BaseMessage.java`

### Not Modified (No Changes Needed)
- `src/main/java/uy/gub/hcen/messaging/listener/DocumentRegistrationListener.java`
- `src/main/java/uy/gub/hcen/messaging/processor/DocumentRegistrationProcessor.java`
- `src/main/java/uy/gub/hcen/messaging/dto/DocumentRegistrationPayload.java`

## Summary

The fix successfully enables HCEN Central to receive and process FHIR DocumentReference messages from peripheral nodes while maintaining backward compatibility with the simple payload format. The solution is:

- **Automatic**: No configuration needed, format detection is automatic
- **Robust**: Handles missing fields gracefully with fallbacks
- **Standards-based**: Uses HL7 FHIR R4 DocumentReference structure
- **Maintainable**: Clean code with comprehensive documentation
- **Tested**: Successfully builds and compiles

**Status**: Ready for deployment and testing
**Build Status**: Successful (no compilation errors)
**Breaking Changes**: None (backward compatible)

---

**Author**: Claude Code (Anthropic)
**Date**: 2025-11-18
**Version**: 1.0
**HCEN Version**: 1.0-SNAPSHOT
