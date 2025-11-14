# FHIR Integration Documentation

## Overview

The **Prestador de Salud** system uses **FHIR R4** (Fast Healthcare Interoperability Resources) for exchanging healthcare data with **HCEN Central**. FHIR is the international standard for healthcare data interoperability.

**FHIR Version**: R4 (4.0.1)
**Library**: HAPI FHIR 6.10.0
**Resources Used**: Patient, DocumentReference

---

## Why FHIR?

✅ **International Standard** - HL7 FHIR is the global standard for healthcare data exchange
✅ **Interoperability** - Works with any FHIR-compliant system
✅ **Modern** - RESTful, JSON-based, web-friendly
✅ **Extensible** - Can add custom extensions for Uruguay-specific data
✅ **Validated** - Built-in validation and profiling
✅ **Future-proof** - Adopted by major EHR vendors worldwide

---

## FHIR Resources

### 1. Patient Resource

Represents patient demographic and identification information.

**FHIR Profile**: `http://hcen.gub.uy/fhir/StructureDefinition/hcen-patient`

**Example**:
```json
{
  "resourceType": "Patient",
  "id": "12345678",
  "meta": {
    "lastUpdated": "2025-11-13T10:30:00Z",
    "profile": [
      "http://hcen.gub.uy/fhir/StructureDefinition/hcen-patient"
    ]
  },
  "identifier": [
    {
      "use": "official",
      "type": {
        "coding": [
          {
            "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
            "code": "NN",
            "display": "National unique individual identifier"
          }
        ]
      },
      "system": "http://hcen.gub.uy/identifiers/ci",
      "value": "12345678"
    }
  ],
  "active": true,
  "name": [
    {
      "use": "official",
      "family": "Pérez",
      "given": ["Juan"],
      "text": "Juan Pérez"
    }
  ],
  "telecom": [
    {
      "system": "email",
      "value": "juan.perez@example.com",
      "use": "home"
    },
    {
      "system": "phone",
      "value": "099123456",
      "use": "mobile"
    }
  ],
  "gender": "male",
  "birthDate": "1990-01-15",
  "address": [
    {
      "use": "home",
      "text": "Av. 18 de Julio 1234, Montevideo",
      "city": "Montevideo",
      "country": "UY"
    }
  ],
  "managingOrganization": {
    "reference": "Organization/clinic-1",
    "display": "Clinic 1"
  }
}
```

**Key Elements**:
- `identifier` - National ID (CI) from Uruguay
- `name` - Patient's official name
- `telecom` - Contact information (email, phone)
- `gender` - Administrative gender (male/female/other)
- `birthDate` - Date of birth
- `address` - Residential address
- `managingOrganization` - Reference to the clinic

---

### 2. DocumentReference Resource

Represents metadata about a clinical document, with a URL to retrieve the actual content.

**FHIR Profile**: `http://hcen.gub.uy/fhir/StructureDefinition/hcen-documentreference`

**Example**:
```json
{
  "resourceType": "DocumentReference",
  "id": "doc-1",
  "meta": {
    "lastUpdated": "2025-11-13T10:45:00Z",
    "profile": [
      "http://hcen.gub.uy/fhir/StructureDefinition/hcen-documentreference"
    ],
    "security": [
      {
        "system": "http://terminology.hl7.org/CodeSystem/v3-Confidentiality",
        "code": "N",
        "display": "Normal"
      }
    ]
  },
  "masterIdentifier": {
    "system": "http://prestador-de-salud.uy/documents",
    "value": "1"
  },
  "status": "current",
  "type": {
    "coding": [
      {
        "system": "http://loinc.org",
        "code": "34109-9",
        "display": "Note"
      }
    ]
  },
  "category": [
    {
      "coding": [
        {
          "system": "http://hl7.org/fhir/us/core/CodeSystem/us-core-documentreference-category",
          "code": "clinical-note",
          "display": "Clinical Note"
        }
      ]
    }
  ],
  "subject": {
    "reference": "Patient/12345678",
    "display": "Patient CI: 12345678"
  },
  "date": "2025-11-13T10:45:00Z",
  "author": [
    {
      "reference": "Practitioner/professional-5",
      "display": "professional-5"
    }
  ],
  "custodian": {
    "reference": "Organization/clinic-1",
    "display": "Clinic 1"
  },
  "description": "Control de rutina",
  "content": [
    {
      "attachment": {
        "contentType": "application/json",
        "url": "http://localhost:8080/prestador/api/documents/1",
        "title": "Consulta General",
        "creation": "2025-11-13T10:45:00Z"
      }
    }
  ],
  "context": {
    "practiceSetting": {
      "coding": [
        {
          "system": "http://hcen.gub.uy/fhir/CodeSystem/specialty",
          "code": "2",
          "display": "Specialty 2"
        }
      ]
    }
  }
}
```

**Key Elements**:
- `masterIdentifier` - Local document ID from Prestador
- `status` - Document status (current, superseded, entered-in-error)
- `type` - Document type using LOINC codes
- `subject` - Reference to the Patient
- `author` - Professional who created the document
- `custodian` - Organization (Clinic) that maintains the document
- **`content.attachment.url`** - **URL to retrieve the actual document from Prestador**
- `context.practiceSetting` - Medical specialty

---

## Document Type Mapping (LOINC Codes)

| Local Type | LOINC Code | Display |
|------------|------------|---------|
| CLINICAL_NOTE | 34109-9 | Note |
| LAB_RESULT | 11502-2 | Laboratory report |
| IMAGING | 18748-4 | Diagnostic imaging study |
| PRESCRIPTION | 57833-6 | Prescription for medication |
| DISCHARGE_SUMMARY | 18842-5 | Discharge summary |
| Other | 34133-9 | Summarization of episode note |

---

## Message Envelope Format

FHIR resources are wrapped in a message envelope for JMS transmission:

```json
{
  "messageId": "msg-550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-11-13T10:30:00",
  "sourceSystem": "prestador-de-salud",
  "eventType": "patient-create",
  "fhirVersion": "R4",
  "resource": {
    "resourceType": "Patient",
    ...
  }
}
```

**Event Types**:
- `patient-create` - New patient registration
- `document-create` - New clinical document metadata

---

## Implementation

### 1. Building FHIR Messages

**Code Location**: `FhirMessageBuilder.java`

**Build Patient Resource**:
```java
String fhirPatient = FhirMessageBuilder.buildPatientResource(
    ci,           // "12345678"
    firstName,    // "Juan"
    lastName,     // "Pérez"
    birthDate,    // LocalDate
    gender,       // "M"
    email,        // "juan.perez@example.com"
    phoneNumber,  // "099123456"
    address,      // "Av. 18 de Julio 1234"
    clinicId      // 1L
);
```

**Build DocumentReference Resource**:
```java
String fhirDocRef = FhirMessageBuilder.buildDocumentReferenceResource(
    patientCI,            // "12345678"
    documentId,           // 1L
    documentType,         // "CLINICAL_NOTE"
    documentTitle,        // "Consulta General"
    documentDescription,  // "Control de rutina"
    documentLocatorUrl,   // "http://localhost:8080/prestador/api/documents/1"
    documentHash,         // "sha256:..."
    createdBy,            // "professional-5"
    createdAt,            // LocalDateTime
    clinicId,             // 1L
    specialtyId           // 2L
);
```

---

### 2. Sending to HCEN

**Code Location**: `HcenMessageSender.java`

**Send Patient**:
```java
HcenMessageSender.sendPatientRegistration(
    patient.getDocumentNumber(),
    patient.getName(),
    patient.getLastName(),
    patient.getBirthDate(),
    patient.getGender(),
    patient.getEmail(),
    patient.getPhone(),
    patient.getAddress(),
    patient.getClinicId()
);
```

**Send Document**:
```java
HcenMessageSender.sendDocumentMetadata(
    patient.getDocumentNumber(),
    document.getId(),
    document.getDocumentType(),
    document.getTitle(),
    document.getDescription(),
    "professional-" + document.getProfessionalId(),
    document.getCreatedAt(),
    document.getClinicId(),
    document.getSpecialtyId(),
    documentLocatorUrl
);
```

---

### 3. Receiving at HCEN (Future Implementation)

**HCEN MDBs need to be updated to parse FHIR**:

```java
// In UserRegistrationListener.java
@Override
public void onMessage(Message message) {
    try {
        TextMessage textMessage = (TextMessage) message;
        String messageBody = textMessage.getText();

        // Parse envelope
        JSONObject envelope = new JSONObject(messageBody);
        String fhirJson = envelope.getJSONObject("resource").toString();

        // Parse FHIR Patient
        Patient patient = FhirMessageBuilder.parsePatient(fhirJson);

        // Extract data
        String ci = patient.getIdentifierFirstRep().getValue();
        String firstName = patient.getNameFirstRep().getGivenAsSingleString();
        String lastName = patient.getNameFirstRep().getFamily();

        // Register in INUS
        inusService.registerUser(...);

    } catch (Exception e) {
        // Handle error
    }
}
```

---

## Testing

### Test Patient Creation

**POST** `http://localhost:8080/prestador/api/patients`
```json
{
  "name": "Juan",
  "lastName": "Pérez",
  "documentNumber": "12345678",
  "birthDate": "1990-01-15",
  "gender": "M",
  "email": "juan.perez@example.com",
  "phone": "099123456",
  "address": "Av. 18 de Julio 1234",
  "clinicId": 1
}
```

**Expected JMS Message** (sent to HCEN):
```json
{
  "messageId": "msg-...",
  "timestamp": "2025-11-13T10:30:00",
  "sourceSystem": "prestador-de-salud",
  "eventType": "patient-create",
  "fhirVersion": "R4",
  "resource": {
    "resourceType": "Patient",
    "id": "12345678",
    "identifier": [...],
    "name": [...],
    ...
  }
}
```

---

### Test Document Creation

**POST** `http://localhost:8080/prestador/api/documents`
```json
{
  "title": "Consulta General",
  "documentType": "CLINICAL_NOTE",
  "patientId": 1,
  "clinicId": 1,
  "professionalId": 5,
  "dateOfVisit": "2025-11-13",
  "diagnosis": "Cefalea tensional",
  "specialtyId": 2
}
```

**Expected JMS Message** (sent to HCEN):
```json
{
  "messageId": "msg-...",
  "timestamp": "2025-11-13T10:45:00",
  "sourceSystem": "prestador-de-salud",
  "eventType": "document-create",
  "fhirVersion": "R4",
  "resource": {
    "resourceType": "DocumentReference",
    "id": "doc-1",
    "type": {...},
    "subject": {"reference": "Patient/12345678"},
    "content": [{
      "attachment": {
        "url": "http://localhost:8080/prestador/api/documents/1"
      }
    }],
    ...
  }
}
```

---

## FHIR Validation

**Validate resources**:
```java
FhirContext ctx = FhirContext.forR4();
FhirValidator validator = ctx.newValidator();

// Validate Patient
ValidationResult result = validator.validateWithResult(patient);
if (result.isSuccessful()) {
    System.out.println("Valid FHIR Patient!");
} else {
    result.getMessages().forEach(System.out::println);
}
```

---

## Benefits Over Custom JSON

| Aspect | Custom JSON | FHIR R4 |
|--------|-------------|---------|
| Standardization | Proprietary | International standard (HL7) |
| Validation | Manual | Built-in validation |
| Interoperability | HCEN-specific | Works with any FHIR system |
| Documentation | Custom docs | Extensive HL7 documentation |
| Tooling | Limited | Rich ecosystem (validators, servers, libraries) |
| Future-proofing | Requires updates | Standard evolves with healthcare |
| International Exchange | Not possible | Compatible with IPS (International Patient Summary) |

---

## Dependencies

**Added to pom.xml**:
```xml
<!-- HAPI FHIR Core -->
<dependency>
  <groupId>ca.uhn.hapi.fhir</groupId>
  <artifactId>hapi-fhir-base</artifactId>
  <version>6.10.0</version>
</dependency>

<!-- HAPI FHIR Structures for R4 -->
<dependency>
  <groupId>ca.uhn.hapi.fhir</groupId>
  <artifactId>hapi-fhir-structures-r4</artifactId>
  <version>6.10.0</version>
</dependency>

<!-- HAPI FHIR Validation -->
<dependency>
  <groupId>ca.uhn.hapi.fhir</groupId>
  <artifactId>hapi-fhir-validation-resources-r4</artifactId>
  <version>6.10.0</version>
</dependency>
```

---

## References

- **FHIR R4 Specification**: https://hl7.org/fhir/R4/
- **Patient Resource**: https://hl7.org/fhir/R4/patient.html
- **DocumentReference Resource**: https://hl7.org/fhir/R4/documentreference.html
- **HAPI FHIR Documentation**: https://hapifhir.io/hapi-fhir/docs/
- **LOINC Codes**: https://loinc.org/
- **IPS (International Patient Summary)**: https://hl7.org/fhir/uv/ips/

---

**Author**: TSE 2025 Group 9
**Date**: 2025-11-13
**Version**: 1.0 (FHIR R4)
