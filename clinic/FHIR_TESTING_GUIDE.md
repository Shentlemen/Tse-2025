# FHIR Document Display Testing Guide

## Quick Test Checklist

### Test 1: FHIR Document Display
**Objective**: Verify FHIR Bundle is properly parsed and displayed

**Steps**:
1. Login to the Clinic Professional Portal
2. Navigate to a patient with documents in HCEN
3. Click "Ver Documentos en HCEN"
4. Click the eye icon to view a FHIR document
5. Verify the modal displays organized sections:
   - Patient Information (blue card)
   - Practitioner Information (green card)
   - Encounter Details (light blue card)
   - Conditions/Diagnoses (yellow card)
   - Vital Signs (red card)
   - Medications (gray card)
   - Document Reference (dark card)

**Expected Result**:
- Document displays in nicely formatted cards
- No JavaScript console errors
- Raw FHIR JSON is available in collapsible section
- Server log shows: "Successfully parsed FHIR Bundle for document X"

**Failure Indicators**:
- Empty modal or raw JSON only
- Console error: "Invalid FHIR Bundle"
- Server log shows FHIR parse error

---

### Test 2: Malformed FHIR Handling
**Objective**: Verify graceful error handling for invalid FHIR

**Steps**:
1. Modify HCEN to return invalid JSON (temporarily break the format)
2. Attempt to view the document
3. Check server logs and browser console

**Expected Result**:
- Error message displayed to user
- Raw content still visible
- Server log shows: "Error parsing FHIR JSON for document X"
- Request attribute `fhirParseError` is set

---

### Test 3: Non-FHIR Content
**Objective**: Verify backward compatibility with other content types

**Steps**:
1. View a document with `application/json` content type (not FHIR)
2. View a document with `text/plain` content type

**Expected Result**:
- Content displays as before (raw text/JSON)
- No FHIR-specific processing
- No errors in logs

---

### Test 4: Binary Document (PDF)
**Objective**: Verify binary documents still work

**Steps**:
1. View a PDF document from HCEN
2. Click download

**Expected Result**:
- PDF viewer displays inline
- Download works correctly
- No regression from FHIR changes

---

### Test 5: Access Denied Scenario
**Objective**: Verify error handling when access is denied

**Steps**:
1. Attempt to view a document where access is denied (403 response)
2. Check the error message

**Expected Result**:
- Access denied message displayed
- Modal shows with error and "Request Access" option
- No FHIR parsing attempted

---

## Server Log Monitoring

### Successful FHIR Processing:
```
INFO  [uy.gub.clinic.web.ProfessionalPatientDocumentsServlet] Successfully parsed FHIR Bundle for document 123
```

### FHIR Parse Error:
```
ERROR [uy.gub.clinic.web.ProfessionalPatientDocumentsServlet] Error parsing FHIR JSON for document 123
com.fasterxml.jackson.core.JsonParseException: ...
```

### Invalid FHIR Bundle:
```
WARN  [uy.gub.clinic.web.ProfessionalPatientDocumentsServlet] Document content is not a FHIR Bundle: resourceType=DocumentReference
```

---

## Browser Console Verification

### Successful Display:
```javascript
// No errors
// FHIR sections are populated
```

### Parse Failure:
```javascript
Error parsing FHIR JSON: SyntaxError: Unexpected token...
```

### Invalid Bundle:
```javascript
Invalid FHIR Bundle
```

---

## Request Attributes Verification

You can add debug logging to verify attributes:

```java
logger.debug("Request attributes:");
logger.debug("  isFhirDocument: {}", request.getAttribute("isFhirDocument"));
logger.debug("  fhirParseError: {}", request.getAttribute("fhirParseError"));
logger.debug("  remoteDocumentInlineContentType: {}", request.getAttribute("remoteDocumentInlineContentType"));
```

---

## Common Issues and Solutions

### Issue: "Invalid FHIR Bundle" in console
**Cause**: The JSON is valid but not a FHIR Bundle
**Solution**: Check that HCEN is returning a Bundle with `resourceType: "Bundle"`

### Issue: Raw JSON displayed without formatting
**Cause**: FHIR parsing failed or content type not recognized
**Solution**: Check server logs for parse errors; verify content type is `application/fhir+json`

### Issue: Empty modal
**Cause**: JavaScript error in JSP
**Solution**: Check browser console; verify `remoteDocumentInlineContent` is properly set

### Issue: Access denied but no error message
**Cause**: Exception not properly caught
**Solution**: Verify `extractAccessDeniedException()` is working correctly

---

## Content Type Examples

### FHIR JSON (should trigger FHIR processing):
```
application/fhir+json
application/fhir+json;charset=utf-8
application/json+fhir
```

### Non-FHIR (should bypass FHIR processing):
```
application/json
application/pdf
text/plain
text/html
```

---

## Performance Monitoring

Monitor these metrics:
- **Parse time**: Should be < 100ms for typical bundles
- **Memory usage**: Jackson ObjectMapper is lightweight
- **Request time**: Total request should remain under 2 seconds

Add timing logs if needed:
```java
long startTime = System.currentTimeMillis();
JsonNode fhirBundle = objectMapper.readTree(content.getInlineContent());
logger.info("FHIR parse took {}ms", System.currentTimeMillis() - startTime);
```

---

## Integration with HCEN

### HCEN Endpoint:
```
GET /api/clinical-history/documents/{documentId}/fhir?patientCi={ci}&specialty={specialty}
```

### Expected Response Headers:
```
Content-Type: application/fhir+json;charset=utf-8
```

### Expected Response Body Structure:
```json
{
  "resourceType": "Bundle",
  "type": "collection",
  "id": "bundle-123",
  "entry": [
    {
      "resource": {
        "resourceType": "Patient",
        "id": "456",
        ...
      },
      "fullUrl": "Patient/456"
    },
    ...
  ]
}
```

---

## Debugging Tips

1. **Enable detailed logging**:
   ```xml
   <!-- Add to standalone.xml or logging.properties -->
   <logger category="uy.gub.clinic.web.ProfessionalPatientDocumentsServlet">
       <level name="DEBUG"/>
   </logger>
   ```

2. **Inspect raw response**:
   Use browser DevTools Network tab to see raw HCEN response

3. **Validate FHIR Bundle**:
   Copy raw JSON and validate at https://www.hl7.org/fhir/validator/

4. **Check Jackson version**:
   Ensure Jackson version supports required JSON features

5. **Test with curl**:
   ```bash
   curl -H "X-Clinic-Id: clinic-001" \
        -H "X-API-Key: your-api-key" \
        "http://localhost:8080/hcen/api/clinical-history/documents/123/fhir?patientCi=12345678&specialty=CARDIOLOGY"
   ```

---

**Date**: 2025-11-23
**Version**: 1.0
**Component**: Clinic Peripheral Node
