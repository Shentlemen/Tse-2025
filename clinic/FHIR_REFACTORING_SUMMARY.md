# FHIR Document Handling Refactoring Summary

## Overview
Refactored the `prepareRemoteDocumentView` method in `ProfessionalPatientDocumentsServlet.java` to properly handle FHIR JSON responses from the HCEN central component.

## Problem Statement
The servlet was treating FHIR JSON documents (with content type `application/fhir+json;charset=utf-8`) as generic inline content, without proper validation or parsing. This prevented proper display and could lead to JavaScript errors in the JSP.

## Solution Implemented

### 1. FHIR Content Type Detection
Added a new helper method `isFhirContentType()` that checks if the content type indicates FHIR JSON format:

```java
private boolean isFhirContentType(String contentType) {
    if (contentType == null) {
        return false;
    }
    String normalized = contentType.toLowerCase();
    return normalized.contains("application/fhir+json") ||
           normalized.contains("application/json+fhir");
}
```

This method handles both standard FHIR content types:
- `application/fhir+json`
- `application/json+fhir`

### 2. FHIR Bundle Validation
When FHIR content is detected, the servlet now:

1. **Parses the JSON** using Jackson ObjectMapper to validate it's well-formed
2. **Validates the FHIR Bundle structure** by checking:
   - Presence of `resourceType` field
   - Value of `resourceType` equals `"Bundle"`
3. **Logs successful parsing** for debugging and monitoring
4. **Sets appropriate request attributes** for the JSP

```java
com.fasterxml.jackson.databind.JsonNode fhirBundle = objectMapper.readTree(content.getInlineContent());

if (fhirBundle.has("resourceType") && "Bundle".equals(fhirBundle.get("resourceType").asText())) {
    logger.info("Successfully parsed FHIR Bundle for document {}", documentId);
    request.setAttribute("remoteDocumentInlineContent", content.getInlineContent());
    request.setAttribute("remoteDocumentInlineContentType", content.getContentType());
    request.setAttribute("isFhirDocument", true);
}
```

### 3. Error Handling
Comprehensive error handling ensures graceful degradation:

- **Parse errors**: Caught and logged, with fallback to showing raw content
- **Invalid FHIR structure**: Logged as warning, content still displayed
- **Error attribute set**: `fhirParseError` attribute provides user-friendly error message

```java
catch (Exception parseEx) {
    logger.error("Error parsing FHIR JSON for document {}", documentId, parseEx);
    request.setAttribute("remoteDocumentInlineContent", content.getInlineContent());
    request.setAttribute("remoteDocumentInlineContentType", content.getContentType());
    request.setAttribute("fhirParseError", "Error al analizar el documento FHIR: " + parseEx.getMessage());
}
```

### 4. Backward Compatibility
The refactoring maintains full backward compatibility:

- **Binary documents** (PDFs, images): Handled as before
- **Non-FHIR inline content**: Displayed without FHIR-specific processing
- **Existing functionality**: Access control, error handling, and download features unchanged

## Request Attributes Set

The method sets the following request attributes based on content type:

### For FHIR Documents:
- `remoteDocumentInlineContent`: The FHIR JSON as a string (parsed by JSP JavaScript)
- `remoteDocumentInlineContentType`: The content type (e.g., `application/fhir+json;charset=utf-8`)
- `isFhirDocument`: Boolean flag indicating FHIR content (new)
- `fhirParseError`: Error message if parsing failed (optional)

### For Binary Documents:
- `remoteDocumentHasBinaryContent`: Boolean flag
- `remoteDocumentBinaryId`: Document ID for download
- `remoteDocumentBinaryContentType`: Content type (e.g., `application/pdf`)

### For All Remote Documents:
- `selectedRemoteDocument`: HcenDocumentDetailDTO with metadata
- `viewRemoteDocument`: Boolean flag to show modal

## JSP Integration

The JSP (patient-documents.jsp) already has comprehensive JavaScript to parse and display FHIR Bundles:

```javascript
var fhirJson = <c:out value="${remoteDocumentInlineContent}" escapeXml="false"/>;

if (!fhirJson || !fhirJson.entry) {
    console.error("Invalid FHIR Bundle");
    return;
}

fhirJson.entry.forEach(function(entry) {
    var resource = entry.resource;
    switch(resource.resourceType) {
        case 'Patient': displayPatient(resource); break;
        case 'Practitioner': displayPractitioner(resource); break;
        case 'Encounter': displayEncounter(resource); break;
        case 'Condition': displayCondition(resource); break;
        case 'Observation': displayObservation(resource); break;
        case 'MedicationRequest': displayMedication(resource); break;
        case 'DocumentReference': displayDocumentReference(resource); break;
    }
});
```

The servlet refactoring ensures the JSP receives properly validated FHIR JSON.

## FHIR Bundle Structure

The HCEN component creates FHIR Bundles using the FhirMappingService with these resources:

1. **Patient** - Patient demographic information
2. **Practitioner** - Healthcare professional who created the document
3. **Encounter** - Clinical encounter/visit information
4. **Condition** - Diagnoses
5. **Observation** - Vital signs (blood pressure, temperature, pulse, etc.)
6. **MedicationRequest** - Prescriptions
7. **DocumentReference** - Document metadata

The JSP displays each resource type in a dedicated card with formatted information.

## Benefits

1. **Validation**: Ensures only valid FHIR Bundles are processed
2. **Error Detection**: Early detection of malformed FHIR content
3. **Logging**: Comprehensive logging for debugging and monitoring
4. **Graceful Degradation**: Falls back to raw display if parsing fails
5. **Type Safety**: Proper handling of different content types
6. **Future-Proof**: Easy to extend with additional FHIR processing logic

## Testing Recommendations

1. **Test FHIR Bundle Display**: View a document from HCEN with FHIR content
2. **Test Malformed JSON**: Verify error handling with invalid JSON
3. **Test Non-FHIR Content**: Ensure backward compatibility with other content types
4. **Test Binary Documents**: Verify PDFs and images still work
5. **Test Access Denied**: Verify error handling when access is denied

## Files Modified

- `C:\Users\agust\fing\tse\tse-2025\clinic\src\main\java\uy\gub\clinic\web\ProfessionalPatientDocumentsServlet.java`
  - Enhanced `prepareRemoteDocumentView()` method
  - Added `isFhirContentType()` helper method

## Related Components

These components work together to provide FHIR document support:

- **HcenClinicalHistoryClient**: REST client that fetches documents from HCEN
- **HcenDocumentService**: Service layer that processes document content
- **RemoteDocumentContent**: DTO that encapsulates document content
- **FhirMappingService**: HCEN service that creates FHIR Bundles
- **patient-documents.jsp**: JSP with JavaScript to display FHIR resources

## Build Status

Build successful with no errors or warnings:
```
BUILD SUCCESSFUL in 4s
4 actionable tasks: 4 executed
```

## Next Steps (Optional Enhancements)

1. **Add FHIR validation**: Use HAPI FHIR validator for strict validation
2. **Cache parsed bundles**: Cache parsed FHIR bundles to improve performance
3. **Add resource counters**: Display count of each resource type in the bundle
4. **Enhanced error messages**: Provide more specific error messages for common issues
5. **FHIR version check**: Validate FHIR version (currently assumes R4)

---

**Date**: 2025-11-23
**Author**: Claude Code
**Component**: Clinic Peripheral Node
**Category**: FHIR Integration
