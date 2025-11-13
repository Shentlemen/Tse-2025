# Prestador de Salud - REST API Documentation

## Overview

This document describes the REST API endpoints for the Health Provider (Prestador de Salud) system. The API allows you to manage patients and clinical documents through simple HTTP requests.

**Base URL**: `http://localhost:8080/prestador`

---

## Database Setup

### Create Database

```sql
CREATE DATABASE prestador_db;
CREATE USER prestador_user WITH PASSWORD 'prestador_pass';
GRANT ALL PRIVILEGES ON DATABASE prestador_db TO prestador_user;
```

### Run Migrations

The Flyway migrations will run automatically on application startup. Ensure the migration files are in:
- `src/main/resources/db/migration/`

Migrations:
- `V001__create_patients_table.sql` - Creates patients table
- `V002__create_clinical_documents_table.sql` - Creates clinical_documents table

---

## API Endpoints

### Patient Endpoints

#### 1. Create Patient

**Endpoint**: `POST /api/patients`

**Description**: Creates a new patient in the system.

**Request Body**:
```json
{
  "name": "Juan",
  "lastName": "Pérez",
  "documentNumber": "12345678",
  "inusId": "INUS-123456",
  "birthDate": "1990-01-15",
  "gender": "M",
  "email": "juan.perez@example.com",
  "phone": "099123456",
  "address": "Av. 18 de Julio 1234, Montevideo",
  "clinicId": 1,
  "active": true
}
```

**Required Fields**:
- `name` (string, max 255 chars)
- `clinicId` (long)

**Optional Fields**:
- `lastName` (string, max 255 chars)
- `documentNumber` (string, max 50 chars) - National ID (CI)
- `inusId` (string, max 50 chars) - HCEN INUS ID
- `birthDate` (date, format: YYYY-MM-DD)
- `gender` (string, max 10 chars)
- `email` (string, max 255 chars)
- `phone` (string, max 20 chars)
- `address` (string, max 500 chars)
- `active` (boolean, default: true)

**Response** (201 Created):
```json
{
  "id": 1,
  "name": "Juan",
  "lastName": "Pérez",
  "documentNumber": "12345678",
  "inusId": "INUS-123456",
  "birthDate": "1990-01-15",
  "gender": "M",
  "email": "juan.perez@example.com",
  "phone": "099123456",
  "address": "Av. 18 de Julio 1234, Montevideo",
  "active": true,
  "clinicId": 1,
  "createdAt": "2025-11-13T10:30:00",
  "updatedAt": "2025-11-13T10:30:00"
}
```

**Postman Example**:
```
POST http://localhost:8080/prestador/api/patients
Content-Type: application/json

{
  "name": "Juan",
  "lastName": "Pérez",
  "documentNumber": "12345678",
  "birthDate": "1990-01-15",
  "clinicId": 1
}
```

---

#### 2. Get Patient by ID

**Endpoint**: `GET /api/patients/{id}`

**Description**: Retrieves a specific patient by ID.

**Path Parameters**:
- `id` (long) - Patient ID

**Response** (200 OK):
```json
{
  "id": 1,
  "name": "Juan",
  "lastName": "Pérez",
  "documentNumber": "12345678",
  "inusId": "INUS-123456",
  "birthDate": "1990-01-15",
  "gender": "M",
  "email": "juan.perez@example.com",
  "phone": "099123456",
  "address": "Av. 18 de Julio 1234, Montevideo",
  "active": true,
  "clinicId": 1,
  "createdAt": "2025-11-13T10:30:00",
  "updatedAt": "2025-11-13T10:30:00"
}
```

**Response** (404 Not Found):
```json
{
  "error": "Patient not found"
}
```

**Postman Example**:
```
GET http://localhost:8080/prestador/api/patients/1
```

---

#### 3. List All Patients

**Endpoint**: `GET /api/patients`

**Description**: Retrieves all patients ordered by ID.

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "name": "Juan",
    "lastName": "Pérez",
    "documentNumber": "12345678",
    ...
  },
  {
    "id": 2,
    "name": "María",
    "lastName": "González",
    "documentNumber": "87654321",
    ...
  }
]
```

**Postman Example**:
```
GET http://localhost:8080/prestador/api/patients
```

---

### Clinical Document Endpoints

#### 1. Create Clinical Document

**Endpoint**: `POST /api/documents`

**Description**: Creates a new clinical document.

**Request Body**:
```json
{
  "title": "Consulta General",
  "description": "Control de rutina",
  "documentType": "CLINICAL_NOTE",
  "patientId": 1,
  "clinicId": 1,
  "professionalId": 5,
  "specialtyId": 2,
  "dateOfVisit": "2025-11-13",
  "chiefComplaint": "Dolor de cabeza",
  "currentIllness": "Cefalea de 2 días de evolución",
  "vitalSigns": "PA: 120/80, FC: 75, Temp: 36.5°C",
  "physicalExamination": "Paciente en buen estado general",
  "diagnosis": "Cefalea tensional",
  "treatment": "Reposo, hidratación",
  "prescriptions": "Paracetamol 500mg cada 8 horas",
  "observations": "Control en 7 días",
  "nextAppointment": "2025-11-20",
  "fileName": "consulta_001.pdf",
  "filePath": "/documents/2025/11/consulta_001.pdf",
  "fileSize": 204800,
  "mimeType": "application/pdf"
}
```

**Required Fields**:
- `title` (string, max 255 chars)
- `documentType` (string, max 100 chars)
- `patientId` (long)
- `clinicId` (long)
- `professionalId` (long)
- `dateOfVisit` (date, format: YYYY-MM-DD)

**Optional Fields**:
- `description` (string, max 1000 chars)
- `specialtyId` (long)
- `fileName` (string, max 100 chars)
- `filePath` (string, max 500 chars)
- `fileSize` (long)
- `mimeType` (string, max 100 chars)
- `rndcId` (string, max 100 chars) - HCEN RNDC reference
- `chiefComplaint` (text)
- `currentIllness` (text)
- `vitalSigns` (text)
- `physicalExamination` (text)
- `diagnosis` (text)
- `treatment` (text)
- `prescriptions` (text)
- `observations` (text)
- `nextAppointment` (date, format: YYYY-MM-DD)
- `attachments` (text)

**Document Types**:
- `CLINICAL_NOTE` - Clinical notes
- `LAB_RESULT` - Laboratory results
- `IMAGING` - Imaging studies (X-ray, CT, MRI, etc.)
- `PRESCRIPTION` - Prescriptions
- `DISCHARGE_SUMMARY` - Discharge summaries
- `REFERRAL` - Referrals
- `CONSENT_FORM` - Consent forms

**Response** (201 Created):
```json
{
  "id": 1,
  "title": "Consulta General",
  "description": "Control de rutina",
  "documentType": "CLINICAL_NOTE",
  "patientId": 1,
  "clinicId": 1,
  "professionalId": 5,
  "specialtyId": 2,
  "dateOfVisit": "2025-11-13",
  "chiefComplaint": "Dolor de cabeza",
  "diagnosis": "Cefalea tensional",
  "treatment": "Reposo, hidratación",
  "prescriptions": "Paracetamol 500mg cada 8 horas",
  "nextAppointment": "2025-11-20",
  "fileName": "consulta_001.pdf",
  "filePath": "/documents/2025/11/consulta_001.pdf",
  "fileSize": 204800,
  "mimeType": "application/pdf",
  "rndcId": null,
  "createdAt": "2025-11-13T10:45:00",
  "updatedAt": "2025-11-13T10:45:00"
}
```

**Postman Example**:
```
POST http://localhost:8080/prestador/api/documents
Content-Type: application/json

{
  "title": "Consulta General",
  "documentType": "CLINICAL_NOTE",
  "patientId": 1,
  "clinicId": 1,
  "professionalId": 5,
  "dateOfVisit": "2025-11-13",
  "diagnosis": "Cefalea tensional"
}
```

---

#### 2. Get Clinical Document by ID

**Endpoint**: `GET /api/documents/{id}`

**Description**: Retrieves a specific clinical document by ID.

**Path Parameters**:
- `id` (long) - Document ID

**Response** (200 OK):
```json
{
  "id": 1,
  "title": "Consulta General",
  "description": "Control de rutina",
  "documentType": "CLINICAL_NOTE",
  ...
}
```

**Response** (404 Not Found):
```json
{
  "error": "Clinical document not found"
}
```

**Postman Example**:
```
GET http://localhost:8080/prestador/api/documents/1
```

---

#### 3. List All Clinical Documents

**Endpoint**: `GET /api/documents`

**Description**: Retrieves all clinical documents ordered by date of visit (most recent first).

**Response** (200 OK):
```json
[
  {
    "id": 2,
    "title": "Laboratorio - Hemograma",
    "documentType": "LAB_RESULT",
    "dateOfVisit": "2025-11-13",
    ...
  },
  {
    "id": 1,
    "title": "Consulta General",
    "documentType": "CLINICAL_NOTE",
    "dateOfVisit": "2025-11-12",
    ...
  }
]
```

**Postman Example**:
```
GET http://localhost:8080/prestador/api/documents
```

---

## Error Responses

All endpoints return consistent error responses:

**500 Internal Server Error**:
```json
{
  "error": "Failed to create patient",
  "message": "Detailed error message here"
}
```

**404 Not Found**:
```json
{
  "error": "Resource not found"
}
```

---

## Postman Collection

### Import These Requests

**1. Create Patient**
```
POST http://localhost:8080/prestador/api/patients
Headers:
  Content-Type: application/json
Body (raw JSON):
{
  "name": "Juan",
  "lastName": "Pérez",
  "documentNumber": "12345678",
  "birthDate": "1990-01-15",
  "email": "juan.perez@example.com",
  "clinicId": 1
}
```

**2. Get All Patients**
```
GET http://localhost:8080/prestador/api/patients
```

**3. Get Patient by ID**
```
GET http://localhost:8080/prestador/api/patients/1
```

**4. Create Clinical Document**
```
POST http://localhost:8080/prestador/api/documents
Headers:
  Content-Type: application/json
Body (raw JSON):
{
  "title": "Consulta General",
  "documentType": "CLINICAL_NOTE",
  "patientId": 1,
  "clinicId": 1,
  "professionalId": 5,
  "dateOfVisit": "2025-11-13",
  "diagnosis": "Cefalea tensional",
  "treatment": "Paracetamol"
}
```

**5. Get All Documents**
```
GET http://localhost:8080/prestador/api/documents
```

**6. Get Document by ID**
```
GET http://localhost:8080/prestador/api/documents/1
```

---

## Configuration

### Database Configuration

Edit `src/main/resources/META-INF/persistence.xml`:

```xml
<property name="jakarta.persistence.jdbc.url" value="jdbc:postgresql://localhost:5432/prestador_db"/>
<property name="jakarta.persistence.jdbc.user" value="prestador_user"/>
<property name="jakarta.persistence.jdbc.password" value="prestador_pass"/>
```

### Application Server

Deploy to Tomcat, WildFly, or any Jakarta EE compatible server.

---

## Testing Workflow

1. **Create a Patient**:
   ```
   POST /api/patients
   {
     "name": "Test Patient",
     "clinicId": 1
   }
   ```
   Response: `{ "id": 1, ... }`

2. **Create a Clinical Document for that Patient**:
   ```
   POST /api/documents
   {
     "title": "Test Document",
     "documentType": "CLINICAL_NOTE",
     "patientId": 1,
     "clinicId": 1,
     "professionalId": 1,
     "dateOfVisit": "2025-11-13"
   }
   ```

3. **Retrieve All Patients**:
   ```
   GET /api/patients
   ```

4. **Retrieve All Documents**:
   ```
   GET /api/documents
   ```

---

## Next Steps

1. Deploy application to server
2. Run database migrations
3. Test endpoints with Postman
4. Integrate with HCEN Central (send patient/document data to HCEN)

---

**Author**: TSE 2025 Group 9
**Date**: 2025-11-13
**Version**: 1.0
