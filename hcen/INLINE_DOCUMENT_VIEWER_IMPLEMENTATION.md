# Inline Document Viewer Implementation

## Overview

This document describes the implementation of inline document viewing functionality for the clinical history feature. Documents are now displayed directly in the UI rather than being downloaded.

**Implementation Date:** 2025-11-13
**Author:** TSE 2025 Group 9
**Status:** Implemented ✅

---

## Changes Summary

### 1. New DTO: DocumentMetadata.java

**File:** `src/main/java/uy/gub/hcen/clinicalhistory/dto/DocumentMetadata.java`

**Purpose:** Contains contextual metadata about clinical documents for inline display.

**Key Fields:**
- `patientName` - Patient's full name
- `professionalName` - Healthcare professional's name
- `clinicName` - Clinic/institution name
- `createdAt` - Document creation timestamp
- `documentTitle` - Document title
- `documentDescription` - Document summary
- `documentType` - Document type display name
- `documentHash` - Hash for integrity verification
- `clinicId` - Clinic identifier
- `professionalSpecialty` - Professional's specialty

**Usage:**
```java
DocumentMetadata metadata = new DocumentMetadata();
metadata.setPatientName("Juan Pérez");
metadata.setProfessionalName("Dr. María García");
metadata.setClinicName("Clínica San José");
metadata.setCreatedAt(LocalDateTime.now());
```

---

### 2. Enhanced DTO: DocumentContentResponse.java

**File:** `src/main/java/uy/gub/hcen/clinicalhistory/dto/DocumentContentResponse.java`

**Changes:**
- Added support for inline content display
- New field: `documentId` (Long)
- New field: `documentType` (String)
- New field: `content` (Object) - Parsed document content
- New field: `metadata` (DocumentMetadata)
- Deprecated: `contentUrl` field (for backward compatibility)

**Response Modes:**

#### Mode 1: Inline Structured Content (JSON/FHIR/HL7)
```json
{
  "documentId": 123,
  "documentType": "ALLERGY_RECORD",
  "contentType": "application/fhir+json",
  "content": {
    "resourceType": "AllergyIntolerance",
    "clinicalStatus": "active",
    "verificationStatus": "confirmed",
    "type": "allergy",
    "category": ["medication"],
    "criticality": "high",
    "code": {
      "text": "Penicilina"
    },
    "patient": {
      "display": "Juan Pérez"
    }
  },
  "metadata": {
    "patientName": "Juan Pérez",
    "professionalName": "Dr. María García",
    "clinicName": "Clínica San José",
    "createdAt": "2025-11-10T14:30:00",
    "documentType": "Allergy Record",
    "documentHash": "sha256:abc123..."
  },
  "available": true
}
```

#### Mode 2: Binary Content (PDF)
- Returns raw PDF bytes
- HTTP headers:
  - `Content-Type: application/pdf`
  - `Content-Disposition: inline; filename="document_123.pdf"`
  - `X-Frame-Options: SAMEORIGIN`
- Browser displays PDF in built-in viewer

---

### 3. Updated REST Endpoint: ClinicalHistoryResource.java

**File:** `src/main/java/uy/gub/hcen/clinicalhistory/api/rest/ClinicalHistoryResource.java`

**Method:** `getDocumentContent(Long documentId, String patientCi, HttpServletRequest request)`

**Key Changes:**

#### Content Type Detection
```java
private String determineContentType(DocumentType documentType) {
    switch (documentType) {
        // FHIR-compatible structured data
        case ALLERGY_RECORD:
        case VITAL_SIGNS:
            return "application/fhir+json";

        // PDF documents
        case LAB_RESULT:
        case IMAGING:
        case PRESCRIPTION:
        // ... other PDF types
            return "application/pdf";

        default:
            return "application/json";
    }
}
```

#### Content Parsing
```java
private Object parseDocumentContent(byte[] documentBytes, String contentType) {
    String contentString = new String(documentBytes, StandardCharsets.UTF_8);

    // Parse JSON/FHIR
    if (contentType.contains("json") || contentType.contains("fhir")) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(contentString);
    }

    // Return XML as formatted string
    if (contentType.contains("xml") || contentType.contains("hl7")) {
        return contentString;
    }

    return contentString;
}
```

#### Metadata Building
```java
private DocumentMetadata buildDocumentMetadata(RndcDocument document) {
    DocumentMetadata metadata = new DocumentMetadata();
    metadata.setClinicName(document.getClinicId());
    metadata.setProfessionalName(document.getCreatedBy());
    metadata.setCreatedAt(document.getCreatedAt());
    metadata.setDocumentType(document.getDocumentType().getDisplayName());
    metadata.setDocumentHash(document.getDocumentHash());
    metadata.setClinicId(document.getClinicId());
    return metadata;
}
```

#### Response Flow

**For PDF documents:**
```java
if (contentType.equals("application/pdf")) {
    return Response.ok(documentBytes, contentType)
        .header("Content-Disposition", "inline; filename=\"document_123.pdf\"")
        .header("X-Frame-Options", "SAMEORIGIN")
        .build();
}
```

**For structured content:**
```java
Object parsedContent = parseDocumentContent(documentBytes, contentType);
DocumentMetadata metadata = buildDocumentMetadata(document);

DocumentContentResponse response = DocumentContentResponse.inline(
    documentId,
    document.getDocumentType().name(),
    contentType,
    parsedContent,
    metadata
);

return Response.ok(response, MediaType.APPLICATION_JSON).build();
```

---

## Document Type Classification

### Structured Formats (JSON Response)
- **ALLERGY_RECORD** → `application/fhir+json`
- **VITAL_SIGNS** → `application/fhir+json`

Content is parsed and returned as JSON with metadata wrapper.

### Binary Formats (PDF Inline Display)
- **LAB_RESULT** → `application/pdf`
- **IMAGING** → `application/pdf`
- **PRESCRIPTION** → `application/pdf`
- **CLINICAL_NOTE** → `application/pdf`
- **DISCHARGE_SUMMARY** → `application/pdf`
- **SURGICAL_REPORT** → `application/pdf`
- **PATHOLOGY_REPORT** → `application/pdf`
- **CONSULTATION** → `application/pdf`
- **EMERGENCY_REPORT** → `application/pdf`
- **PROGRESS_NOTE** → `application/pdf`
- **TREATMENT_PLAN** → `application/pdf`
- **VACCINATION_RECORD** → `application/pdf`
- **REFERRAL** → `application/pdf`
- **INFORMED_CONSENT** → `application/pdf`
- **DIAGNOSTIC_REPORT** → `application/pdf`

Binary content is returned with `inline` disposition for browser display.

---

## Security Considerations

### Maintained Security Features
✅ **Patient Authorization** - Only document owner can view content
✅ **Hash Verification** - Document integrity checked on every retrieval
✅ **Audit Logging** - All access attempts logged
✅ **No Caching** - Documents fetched fresh from peripheral node
✅ **HTTPS Only** - All communication encrypted

### New Security Headers
- `Content-Disposition: inline` (instead of `attachment`)
- `X-Frame-Options: SAMEORIGIN` (prevents clickjacking)
- `Cache-Control: no-cache, no-store, must-revalidate`

---

## API Examples

### Example 1: View FHIR Allergy Record

**Request:**
```http
GET /api/clinical-history/documents/123/content?patientCi=12345678
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Accept: application/json
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "documentId": 123,
  "documentType": "ALLERGY_RECORD",
  "contentType": "application/fhir+json",
  "content": {
    "resourceType": "AllergyIntolerance",
    "clinicalStatus": {
      "coding": [
        {
          "system": "http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical",
          "code": "active"
        }
      ]
    },
    "verificationStatus": {
      "coding": [
        {
          "system": "http://terminology.hl7.org/CodeSystem/allergyintolerance-verification",
          "code": "confirmed"
        }
      ]
    },
    "type": "allergy",
    "category": ["medication"],
    "criticality": "high",
    "code": {
      "text": "Penicilina"
    },
    "patient": {
      "display": "Juan Pérez"
    },
    "recordedDate": "2024-01-15"
  },
  "metadata": {
    "patientName": "Juan Pérez",
    "professionalName": "Dr. María García",
    "clinicName": "Clínica San José",
    "createdAt": "2024-01-15T10:30:00",
    "documentType": "Allergy Record",
    "documentHash": "sha256:a1b2c3d4e5f6...",
    "clinicId": "clinic-001"
  },
  "available": true
}
```

### Example 2: View PDF Lab Result

**Request:**
```http
GET /api/clinical-history/documents/456/content?patientCi=12345678
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Accept: application/pdf
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/pdf
Content-Disposition: inline; filename="document_456.pdf"
Content-Length: 245678
X-Frame-Options: SAMEORIGIN
Cache-Control: no-cache, no-store, must-revalidate

%PDF-1.4
[PDF binary content]
```

Browser will display PDF in built-in viewer.

---

## Frontend Integration (JSP)

### Current Implementation
The `clinical-history.jsp` file currently uses the "Ver" (View) button to open a modal with document metadata, and a separate "Descargar" (Download) button.

### Recommended Updates

#### Update "Ver" Button Handler
```javascript
/**
 * View document inline (updated)
 */
async function viewDocumentInline(documentId) {
    try {
        const response = await fetch(
            `/hcen/api/clinical-history/documents/${documentId}/content?patientCi=${patientCi}`,
            {
                headers: {
                    'Authorization': 'Bearer ' + getToken(),
                    'Accept': 'application/json, application/pdf'
                }
            }
        );

        const contentType = response.headers.get('Content-Type');

        // PDF - open in modal with iframe
        if (contentType.includes('pdf')) {
            const blob = await response.blob();
            const pdfUrl = URL.createObjectURL(blob);
            showPdfModal(pdfUrl, documentId);
            return;
        }

        // JSON/FHIR - display structured content
        const data = await response.json();
        if (data.available) {
            showStructuredContentModal(data);
        } else {
            showInfo(data.message);
        }

    } catch (error) {
        console.error('Error viewing document:', error);
        showError('Error al visualizar el documento');
    }
}

/**
 * Show PDF in modal with iframe
 */
function showPdfModal(pdfUrl, documentId) {
    const modal = document.getElementById('pdfViewerModal');
    const iframe = document.getElementById('pdfIframe');
    iframe.src = pdfUrl;
    modal.classList.add('show');
}

/**
 * Show structured content (JSON/FHIR) in modal
 */
function showStructuredContentModal(data) {
    const modal = document.getElementById('structuredContentModal');

    // Display metadata
    document.getElementById('metaPatient').textContent = data.metadata.patientName || '-';
    document.getElementById('metaProfessional').textContent = data.metadata.professionalName;
    document.getElementById('metaClinic').textContent = data.metadata.clinicName;
    document.getElementById('metaDate').textContent = formatDate(data.metadata.createdAt);
    document.getElementById('metaType').textContent = data.metadata.documentType;

    // Display content
    const contentDiv = document.getElementById('structuredContent');
    contentDiv.innerHTML = renderFhirContent(data.content, data.contentType);

    modal.classList.add('show');
}

/**
 * Render FHIR content as HTML
 */
function renderFhirContent(content, contentType) {
    if (contentType.includes('fhir')) {
        return renderFhirResource(content);
    }
    // Fallback: pretty-print JSON
    return '<pre>' + JSON.stringify(content, null, 2) + '</pre>';
}

/**
 * Render FHIR resource based on type
 */
function renderFhirResource(resource) {
    const type = resource.resourceType;

    if (type === 'AllergyIntolerance') {
        return renderAllergyIntolerance(resource);
    } else if (type === 'Observation') {
        return renderObservation(resource);
    }

    // Default: JSON view
    return '<pre>' + JSON.stringify(resource, null, 2) + '</pre>';
}

/**
 * Render AllergyIntolerance resource
 */
function renderAllergyIntolerance(allergy) {
    return `
        <div class="fhir-resource">
            <h3>🚨 Alergia: ${allergy.code.text}</h3>
            <div class="fhir-field">
                <strong>Estado:</strong> ${allergy.clinicalStatus?.coding?.[0]?.code || 'N/A'}
            </div>
            <div class="fhir-field">
                <strong>Categoría:</strong> ${allergy.category?.join(', ') || 'N/A'}
            </div>
            <div class="fhir-field">
                <strong>Criticidad:</strong> <span class="criticality-${allergy.criticality}">${allergy.criticality || 'N/A'}</span>
            </div>
            <div class="fhir-field">
                <strong>Paciente:</strong> ${allergy.patient?.display || 'N/A'}
            </div>
            <div class="fhir-field">
                <strong>Fecha de registro:</strong> ${allergy.recordedDate || 'N/A'}
            </div>
        </div>
    `;
}
```

#### Add CSS for FHIR Content Display
```css
.fhir-resource {
    background: #f8f9fa;
    border-left: 4px solid #667eea;
    padding: 20px;
    border-radius: 8px;
    margin: 15px 0;
}

.fhir-field {
    margin: 10px 0;
    font-size: 14px;
}

.fhir-field strong {
    color: #2c3e50;
    margin-right: 8px;
}

.criticality-high {
    color: #e74c3c;
    font-weight: bold;
}

.criticality-low {
    color: #27ae60;
}

.criticality-unable-to-assess {
    color: #95a5a6;
}
```

#### Add PDF Viewer Modal HTML
```html
<!-- PDF Viewer Modal -->
<div id="pdfViewerModal" class="modal">
    <div class="modal-content modal-large">
        <div class="modal-header">
            <div class="modal-title">Visualización de Documento</div>
            <button class="modal-close" onclick="closePdfModal()">×</button>
        </div>
        <div class="modal-body modal-body-pdf">
            <iframe id="pdfIframe" width="100%" height="600px" frameborder="0"></iframe>
        </div>
        <div class="modal-actions">
            <button class="btn-modal btn-secondary" onclick="closePdfModal()">Cerrar</button>
        </div>
    </div>
</div>

<!-- Structured Content Modal -->
<div id="structuredContentModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <div class="modal-title">Contenido del Documento</div>
            <button class="modal-close" onclick="closeStructuredModal()">×</button>
        </div>
        <div class="modal-body">
            <!-- Metadata -->
            <div class="metadata-section">
                <h4>Información del Documento</h4>
                <div class="metadata-grid">
                    <div class="meta-field">
                        <strong>Paciente:</strong> <span id="metaPatient">-</span>
                    </div>
                    <div class="meta-field">
                        <strong>Profesional:</strong> <span id="metaProfessional">-</span>
                    </div>
                    <div class="meta-field">
                        <strong>Centro:</strong> <span id="metaClinic">-</span>
                    </div>
                    <div class="meta-field">
                        <strong>Fecha:</strong> <span id="metaDate">-</span>
                    </div>
                    <div class="meta-field">
                        <strong>Tipo:</strong> <span id="metaType">-</span>
                    </div>
                </div>
            </div>

            <!-- Content -->
            <div class="content-section">
                <h4>Contenido</h4>
                <div id="structuredContent"></div>
            </div>
        </div>
        <div class="modal-actions">
            <button class="btn-modal btn-secondary" onclick="closeStructuredModal()">Cerrar</button>
        </div>
    </div>
</div>
```

---

## Testing

### Manual Testing Checklist

#### PDF Documents
- [ ] View lab result PDF inline in browser
- [ ] Verify PDF displays in browser's native viewer
- [ ] Check Content-Disposition header is "inline"
- [ ] Verify X-Frame-Options header prevents external framing
- [ ] Test zooming and scrolling in PDF viewer

#### Structured Documents (FHIR)
- [ ] View allergy record as JSON
- [ ] Verify parsed content is valid JSON
- [ ] Check metadata fields are populated correctly
- [ ] Test with malformed JSON (should fallback gracefully)
- [ ] Verify content renders as formatted HTML

#### Security
- [ ] Attempt to view another patient's document (should fail with 403)
- [ ] Verify all access is logged in audit system
- [ ] Check hash verification is performed
- [ ] Test with expired JWT token (should redirect to login)
- [ ] Verify no caching headers are set

#### Error Handling
- [ ] Document not found (404)
- [ ] Peripheral node unavailable (503)
- [ ] Hash mismatch (500)
- [ ] Invalid document ID (400)
- [ ] Network timeout (handle gracefully)

### Sample Test Documents

#### FHIR Allergy Record (JSON)
```json
{
  "resourceType": "AllergyIntolerance",
  "id": "allergy-penicillin",
  "clinicalStatus": {
    "coding": [
      {
        "system": "http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical",
        "code": "active"
      }
    ]
  },
  "verificationStatus": {
    "coding": [
      {
        "system": "http://terminology.hl7.org/CodeSystem/allergyintolerance-verification",
        "code": "confirmed"
      }
    ]
  },
  "type": "allergy",
  "category": ["medication"],
  "criticality": "high",
  "code": {
    "text": "Penicilina"
  },
  "patient": {
    "display": "Juan Pérez"
  },
  "recordedDate": "2024-01-15",
  "note": [
    {
      "text": "Reacción alérgica severa con urticaria y dificultad respiratoria"
    }
  ]
}
```

#### FHIR Vital Signs (JSON)
```json
{
  "resourceType": "Observation",
  "id": "vital-signs-bp",
  "status": "final",
  "category": [
    {
      "coding": [
        {
          "system": "http://terminology.hl7.org/CodeSystem/observation-category",
          "code": "vital-signs"
        }
      ]
    }
  ],
  "code": {
    "coding": [
      {
        "system": "http://loinc.org",
        "code": "85354-9",
        "display": "Blood pressure panel"
      }
    ],
    "text": "Presión Arterial"
  },
  "subject": {
    "display": "Juan Pérez"
  },
  "effectiveDateTime": "2024-03-10T10:30:00Z",
  "component": [
    {
      "code": {
        "coding": [
          {
            "system": "http://loinc.org",
            "code": "8480-6",
            "display": "Systolic blood pressure"
          }
        ],
        "text": "Presión Sistólica"
      },
      "valueQuantity": {
        "value": 120,
        "unit": "mmHg"
      }
    },
    {
      "code": {
        "coding": [
          {
            "system": "http://loinc.org",
            "code": "8462-4",
            "display": "Diastolic blood pressure"
          }
        ],
        "text": "Presión Diastólica"
      },
      "valueQuantity": {
        "value": 80,
        "unit": "mmHg"
      }
    }
  ]
}
```

---

## Performance Considerations

### Document Retrieval
- Document is **never cached** - always fetched fresh from peripheral node
- For large documents (>10MB), consider implementing streaming
- JSON parsing is efficient for documents up to 5MB

### Network Optimization
- Consider implementing ETag/If-None-Match for conditional requests
- Compress JSON responses with gzip
- Use connection pooling for peripheral node HTTP client

### Browser Performance
- PDF rendering handled by browser's native viewer (efficient)
- JSON content limited to 5MB to prevent browser slowdown
- Use virtual scrolling for large structured documents

---

## Future Enhancements

### Short Term
1. **INUS Integration** - Populate patient name in metadata from INUS
2. **Professional Registry** - Enrich metadata with professional specialty
3. **Clinic Lookup** - Display actual clinic name instead of ID
4. **XML Formatting** - Add syntax highlighting for XML/HL7 content
5. **Download Button** - Re-enable download with "attachment" disposition

### Medium Term
1. **FHIR Resource Renderers** - Add specialized renderers for more FHIR types
   - Observation (vital signs, lab results)
   - MedicationRequest (prescriptions)
   - DiagnosticReport (imaging reports)
   - Immunization (vaccination records)
2. **Search/Filter** - Filter documents by content (full-text search)
3. **Annotations** - Allow patients to annotate documents
4. **Sharing** - Share documents with other professionals

### Long Term
1. **IPS-FHIR Export** - Export complete clinical history as IPS bundle
2. **Translations** - Multi-language support for FHIR content
3. **Accessibility** - Screen reader support for structured content
4. **Offline Mode** - Cache documents for offline viewing
5. **Version History** - Track document versions over time

---

## Troubleshooting

### Issue: PDF not displaying inline
**Symptom:** PDF downloads instead of displaying in browser

**Solution:**
1. Check Content-Disposition header is set to "inline"
2. Verify browser supports PDF viewing (update browser if needed)
3. Check if X-Frame-Options is preventing display

### Issue: JSON parsing error
**Symptom:** Error parsing FHIR content

**Solution:**
1. Verify content is valid JSON (use JSON validator)
2. Check character encoding (should be UTF-8)
3. Review parseDocumentContent() fallback logic

### Issue: 403 Forbidden error
**Symptom:** Patient cannot view their own document

**Solution:**
1. Verify patientCi matches document owner
2. Check JWT token is valid and not expired
3. Review authorization logic in service layer

### Issue: Hash verification failed
**Symptom:** 500 error with hash mismatch message

**Solution:**
1. Re-register document in RNDC with correct hash
2. Verify peripheral node is returning correct document
3. Check hash algorithm matches (SHA-256)

---

## Related Files

### Modified Files
1. `src/main/java/uy/gub/hcen/clinicalhistory/api/rest/ClinicalHistoryResource.java`
2. `src/main/java/uy/gub/hcen/clinicalhistory/dto/DocumentContentResponse.java`

### New Files
1. `src/main/java/uy/gub/hcen/clinicalhistory/dto/DocumentMetadata.java`
2. `INLINE_DOCUMENT_VIEWER_IMPLEMENTATION.md` (this file)

### Unchanged Files (but relevant)
- `src/main/java/uy/gub/hcen/clinicalhistory/service/ClinicalHistoryService.java`
- `src/main/java/uy/gub/hcen/integration/peripheral/PeripheralNodeClient.java`
- `src/main/webapp/patient/clinical-history.jsp` (needs frontend updates)

---

## References

- **FHIR R4 Specification:** https://hl7.org/fhir/R4/
- **IPS Implementation Guide:** https://hl7.org/fhir/uv/ips/
- **AGESIC Security Guidelines:** https://www.gub.uy/agencia-gobierno-electronico-sociedad-informacion-conocimiento/
- **Ley 18.331 (Uruguay Data Protection):** https://www.gub.uy/unidad-reguladora-control-datos-personales/

---

## Changelog

### Version 2.0 - 2025-11-13
- ✅ Added inline document viewing support
- ✅ Created DocumentMetadata DTO
- ✅ Enhanced DocumentContentResponse for structured content
- ✅ Updated REST endpoint to parse and return JSON/FHIR
- ✅ Added PDF inline display with proper headers
- ✅ Maintained all existing security features
- ✅ Build successful - ready for testing

### Version 1.0 - 2025-11-04
- Initial implementation with download-only functionality

---

**Status:** Implementation Complete ✅
**Next Steps:** Frontend integration in clinical-history.jsp
**Documentation:** Complete
