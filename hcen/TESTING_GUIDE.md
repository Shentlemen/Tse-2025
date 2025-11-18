# Testing Guide - RNDC Integration

## Quick Start Testing

### Prerequisites
1. WildFly server running
2. PostgreSQL database with RNDC data
3. Sample documents registered in RNDC
4. Clinics registered in clinic registry

### Test Scenario 1: Patient with Documents

**Setup:**
```sql
-- Check if patient has documents in RNDC
SELECT * FROM rndc.rndc_documents WHERE patient_ci = '12345678';

-- Verify clinics exist
SELECT * FROM clinics.clinics WHERE clinic_id IN (
    SELECT DISTINCT clinic_id FROM rndc.rndc_documents WHERE patient_ci = '12345678'
);
```

**Test Steps:**
1. Login as patient with CI: 12345678
2. Navigate to Clinical History page
3. **Expected Results:**
   - Document list displays with real data
   - Clinic names show actual names (not IDs)
   - Document count matches database
   - Pagination works

**Verification:**
- Check browser console: No errors
- Check WildFly logs: `Found X documents for patient: 12345678`
- Verify clinic names: Should show "Hospital de Clínicas" not "clinic-hc"

### Test Scenario 2: Patient without Documents

**Setup:**
```sql
-- Create new patient in INUS without documents
INSERT INTO inus.inus_users (ci, inus_id, first_name, last_name, date_of_birth, status, created_at)
VALUES ('99999999', 'inus-999999', 'Test', 'Patient', '1990-01-01', 'ACTIVE', NOW());
```

**Test Steps:**
1. Login as patient with CI: 99999999
2. Navigate to Clinical History page
3. **Expected Results:**
   - Empty state displayed
   - Message: "No tiene documentos clínicos registrados"
   - NO mock data displayed

**Verification:**
- Check WildFly logs: `Found 0 documents for patient: 99999999`
- Browser shows empty state component

### Test Scenario 3: FHIR Document Viewing

**Setup:**
```sql
-- Find a document with valid locator URL
SELECT id, document_title, document_locator, clinic_id
FROM rndc.rndc_documents
WHERE patient_ci = '12345678'
  AND document_locator IS NOT NULL
LIMIT 1;

-- Verify clinic has API key configured
SELECT clinic_id, clinic_name, api_key, peripheral_node_url
FROM clinics.clinics
WHERE clinic_id = 'clinic-hc';
```

**Test Steps:**
1. Login as patient with CI: 12345678
2. Navigate to Clinical History page
3. Click "Ver Documento" button on any document
4. **Expected Results:**
   - Loading indicator appears
   - FHIR document retrieved from peripheral node
   - Document content displayed in viewer
   - No errors in console

**Verification:**
- Check WildFly logs:
  ```
  Retrieving FHIR document for documentId: X, patient: 12345678
  Fetching FHIR document from peripheral node - locator: https://..., clinic: clinic-hc
  Successfully retrieved and validated FHIR document X (Y bytes)
  ```
- Check peripheral node logs: Document retrieval request received
- Verify audit log: Document access event logged

### Test Scenario 4: Filtering and Pagination

**Test Steps:**
1. Login as patient with many documents (>20)
2. Navigate to Clinical History page
3. **Filter by Document Type:**
   - Select "Resultados de Laboratorio"
   - Verify only LAB_RESULT documents shown
4. **Filter by Date Range:**
   - Select last 30 days
   - Verify only recent documents shown
5. **Filter by Clinic:**
   - Select specific clinic
   - Verify only documents from that clinic shown
6. **Pagination:**
   - Navigate to page 2
   - Verify next 20 documents loaded

**Verification:**
- Check network tab: Correct query parameters sent
- Check WildFly logs: Filter parameters logged
- Verify total count remains accurate

### Test Scenario 5: Clinic Name Fallback

**Setup:**
```sql
-- Create document with non-existent clinic ID
INSERT INTO rndc.rndc_documents
(patient_ci, clinic_id, document_type, document_title, status, created_at, created_by)
VALUES
('12345678', 'unknown-clinic', 'CLINICAL_NOTE', 'Test Note', 'ACTIVE', NOW(), 'doctor@test.com');
```

**Test Steps:**
1. Login as patient with CI: 12345678
2. Navigate to Clinical History page
3. Find the test document
4. **Expected Results:**
   - Document displays with clinic name: "Clínica unknown-clinic"
   - Warning logged in WildFly
   - No errors in UI

**Verification:**
- Check WildFly logs: `Clinic not found in registry: unknown-clinic`
- Document displays correctly with fallback clinic name

### Test Scenario 6: Missing Clinic ID

**Setup:**
```sql
-- Create document with NULL clinic_id
INSERT INTO rndc.rndc_documents
(patient_ci, clinic_id, document_type, document_title, status, created_at, created_by)
VALUES
('12345678', NULL, 'CLINICAL_NOTE', 'Legacy Note', 'ACTIVE', NOW(), 'doctor@test.com');
```

**Test Steps:**
1. Login as patient with CI: 12345678
2. Navigate to Clinical History page
3. Find the legacy document
4. **Expected Results:**
   - Document displays with clinic name: "Clínica Desconocida"
   - No errors in UI

**Verification:**
- Document displays correctly with "Clínica Desconocida"

## Database Verification Queries

### Check RNDC Documents
```sql
-- Count documents per patient
SELECT patient_ci, COUNT(*) as doc_count, status
FROM rndc.rndc_documents
GROUP BY patient_ci, status
ORDER BY doc_count DESC;

-- Check document types
SELECT document_type, COUNT(*) as count
FROM rndc.rndc_documents
WHERE patient_ci = '12345678'
GROUP BY document_type;

-- Check clinics referenced in documents
SELECT DISTINCT clinic_id
FROM rndc.rndc_documents
WHERE patient_ci = '12345678';
```

### Check Clinic Registry
```sql
-- List all registered clinics
SELECT clinic_id, clinic_name, status, peripheral_node_url
FROM clinics.clinics
ORDER BY clinic_name;

-- Check clinics with missing data
SELECT clinic_id, clinic_name
FROM clinics.clinics
WHERE api_key IS NULL OR peripheral_node_url IS NULL;
```

### Check Audit Logs
```sql
-- Recent document access events
SELECT actor_id, event_type, resource_id, action_outcome, timestamp
FROM audit.audit_logs
WHERE resource_type = 'DOCUMENT'
  AND actor_id = '12345678'
ORDER BY timestamp DESC
LIMIT 20;

-- Failed access attempts
SELECT actor_id, resource_id, details, timestamp
FROM audit.audit_logs
WHERE action_outcome = 'DENIED'
ORDER BY timestamp DESC
LIMIT 20;
```

## WildFly Log Monitoring

### Successful Document Retrieval
```
INFO: Fetching clinical history for patient: 12345678, page: 0, size: 20
INFO: Found 15 documents for patient: 12345678 (total: 15)
```

### Clinic Name Lookup
```
WARNING: Clinic not found in registry: unknown-clinic (document 42)
```

### FHIR Document Retrieval
```
INFO: Retrieving FHIR document for documentId: 123, patient: 12345678
INFO: Fetching FHIR document from peripheral node - locator: https://clinic.hcen.uy/api/documents/abc123, clinic: clinic-hc
INFO: Successfully retrieved and validated FHIR document 123 (25601 bytes)
```

### Error Scenarios
```
SEVERE: Error fetching clinical history for patient: 12345678
java.lang.Exception: Database connection timeout
...
```

## REST API Testing (Manual)

### Get Clinical History
```bash
# Using curl
curl -X GET "http://localhost:8080/hcen/api/clinical-history?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Accept: application/json"

# Expected Response
{
  "documents": [
    {
      "id": 1,
      "documentType": "LAB_RESULT",
      "documentTypeDisplayName": "Resultados de Laboratorio",
      "title": "Hemograma Completo",
      "clinicName": "Hospital de Clínicas",
      "clinicId": "clinic-hc",
      "professionalName": "Dr. Juan García",
      "createdAt": "2025-11-15T10:30:00",
      "status": "ACTIVE",
      "hasContent": true
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  }
}
```

### Get FHIR Document
```bash
curl -X GET "http://localhost:8080/hcen/api/clinical-history/documents/123/fhir" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Accept: application/fhir+json"

# Expected Response: FHIR Bundle or DocumentReference JSON
{
  "resourceType": "Bundle",
  "type": "document",
  "entry": [
    {
      "resource": {
        "resourceType": "Composition",
        "title": "Hemograma Completo",
        ...
      }
    }
  ]
}
```

### Filter by Document Type
```bash
curl -X GET "http://localhost:8080/hcen/api/clinical-history?documentType=LAB_RESULT&page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Accept: application/json"
```

### Filter by Date Range
```bash
curl -X GET "http://localhost:8080/hcen/api/clinical-history?fromDate=2025-10-01T00:00:00&toDate=2025-11-18T23:59:59&page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Accept: application/json"
```

## Performance Testing

### Measure Query Performance
```sql
-- Enable query timing
\timing on

-- Test RNDC search query
EXPLAIN ANALYZE
SELECT * FROM rndc.rndc_documents
WHERE patient_ci = '12345678'
  AND status = 'ACTIVE'
ORDER BY created_at DESC
LIMIT 20 OFFSET 0;

-- Should use index on patient_ci and status
-- Expected: Index Scan, execution time < 10ms
```

### Measure Clinic Lookup Performance
```sql
EXPLAIN ANALYZE
SELECT * FROM clinics.clinics
WHERE clinic_id IN ('clinic-hc', 'clinic-hb', 'clinic-cm');

-- Should use index on clinic_id (primary key)
-- Expected: Index Scan, execution time < 5ms
```

### Load Testing
```bash
# Install Apache Bench (ab) or similar tool
# Test 100 concurrent requests
ab -n 100 -c 10 -H "Authorization: Bearer YOUR_JWT_TOKEN" \
   http://localhost:8080/hcen/api/clinical-history?page=0&size=20

# Analyze response times
# Expected: 95th percentile < 500ms
```

## Troubleshooting

### Issue: Empty Document List (but documents exist in database)

**Diagnosis:**
```sql
-- Check document status
SELECT status, COUNT(*) FROM rndc.rndc_documents
WHERE patient_ci = '12345678'
GROUP BY status;

-- Service only returns ACTIVE documents
```

**Solution:**
- Verify documents have status = 'ACTIVE'
- Update status if needed: `UPDATE rndc.rndc_documents SET status = 'ACTIVE' WHERE ...`

### Issue: Clinic Name Shows as "Clínica unknown-clinic"

**Diagnosis:**
```sql
-- Check if clinic exists
SELECT * FROM clinics.clinics WHERE clinic_id = 'unknown-clinic';
```

**Solution:**
- Register clinic in clinic registry
- Or update document to reference valid clinic ID

### Issue: FHIR Document Retrieval Fails

**Diagnosis:**
- Check WildFly logs for peripheral node errors
- Verify clinic has API key configured
- Test peripheral node endpoint directly

**Solution:**
```bash
# Test peripheral node directly
curl -X GET "https://clinic-hc.hcen.uy/api/documents/abc123" \
  -H "X-API-Key: YOUR_API_KEY" \
  -H "Accept: application/fhir+json"
```

### Issue: Performance Degradation

**Diagnosis:**
- Check database query execution times
- Monitor number of clinic lookup queries
- Check for missing indexes

**Solution:**
- Add database indexes if missing
- Implement clinic name caching (future enhancement)
- Consider batch clinic lookup (future enhancement)

## Success Criteria Checklist

- [ ] Patient with documents sees real RNDC data
- [ ] Patient without documents sees empty state (no mock data)
- [ ] Clinic names display correctly (not clinic IDs)
- [ ] Document filtering works (type, date, clinic)
- [ ] Pagination works correctly
- [ ] FHIR document viewing works
- [ ] Audit logs record all access events
- [ ] No errors in WildFly logs
- [ ] No errors in browser console
- [ ] Response times acceptable (<500ms)

## Next Steps

After successful testing:
1. Deploy to staging environment
2. Perform user acceptance testing
3. Monitor performance metrics
4. Implement future enhancements (caching, batch lookup)
5. Remove deprecated mock data methods

---

**Last Updated:** 2025-11-18
**Build Status:** SUCCESS
**Testing Status:** Ready for Manual Verification
