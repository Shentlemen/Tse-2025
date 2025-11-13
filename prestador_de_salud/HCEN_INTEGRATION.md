# HCEN Central Integration Documentation

## Overview

This document explains how the **Prestador de Salud** (Health Provider) system integrates with **HCEN Central** (Historia Clínica Electrónica Nacional) using JMS messaging.

**Integration Pattern**: Event-Driven Messaging
**Protocol**: JMS (Java Message Service) over WildFly ActiveMQ Artemis
**Communication**: Asynchronous, fire-and-forget

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                    Prestador de Salud (Health Provider)          │
│                                                                    │
│  ┌─────────────┐      ┌──────────────────┐                       │
│  │   Patient   │──────│ PatientServlet   │                       │
│  │   Created   │      │  POST /patients  │                       │
│  └─────────────┘      └────────┬─────────┘                       │
│                                 │                                 │
│                                 │ 1. Save to local DB             │
│                                 │ 2. Send JMS message             │
│                                 │                                 │
│  ┌─────────────┐      ┌────────▼──────────┐                      │
│  │  Document   │──────│ DocumentServlet   │                      │
│  │   Created   │      │  POST /documents  │                      │
│  └─────────────┘      └────────┬──────────┘                      │
│                                 │                                 │
│                       ┌─────────▼──────────┐                     │
│                       │ HcenMessageSender  │                     │
│                       │   (JMS Producer)   │                     │
│                       └─────────┬──────────┘                     │
└─────────────────────────────────┼────────────────────────────────┘
                                  │
                                  │ JMS over HTTP-Remoting
                                  │
┌─────────────────────────────────▼────────────────────────────────┐
│                        HCEN Central (WildFly)                     │
│                                                                    │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  ActiveMQ Artemis - JMS Queues                             │  │
│  │                                                              │  │
│  │  - java:/jms/queue/UserRegistration                        │  │
│  │  - java:/jms/queue/DocumentRegistration                    │  │
│  └────────────────────┬───────────────────────────────────────┘  │
│                       │                                           │
│                       │ Messages delivered to MDBs                │
│                       │                                           │
│  ┌────────────────────▼───────────────┐                          │
│  │  Message-Driven Beans (MDBs)       │                          │
│  │                                     │                          │
│  │  - UserRegistrationListener        │                          │
│  │  - DocumentRegistrationListener    │                          │
│  └────────────────────┬───────────────┘                          │
│                       │                                           │
│                       │ Process messages                          │
│                       │                                           │
│  ┌────────────────────▼───────────────┐                          │
│  │  Domain Services                   │                          │
│  │                                     │                          │
│  │  - InusService (User Registry)     │                          │
│  │  - RndcService (Document Registry) │                          │
│  └────────────────────┬───────────────┘                          │
│                       │                                           │
│                       ▼                                           │
│  ┌────────────────────────────────────┐                          │
│  │  PostgreSQL Database               │                          │
│  │                                     │                          │
│  │  - inus.inus_users                 │                          │
│  │  - rndc.rndc_documents (metadata)  │                          │
│  └────────────────────────────────────┘                          │
└───────────────────────────────────────────────────────────────────┘
```

---

## Events Sent to HCEN

### 1. Patient Registration Event

**Trigger**: When a new patient is created via `POST /api/patients`

**Queue**: `java:/jms/queue/UserRegistration`

**Message Format**:
```json
{
  "messageId": "msg-550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-11-13T10:30:00",
  "sourceSystem": "prestador-de-salud",
  "eventType": "USER_CREATED",
  "payload": {
    "ci": "12345678",
    "firstName": "Juan",
    "lastName": "Pérez",
    "dateOfBirth": "1990-01-15",
    "email": "juan.perez@example.com",
    "phoneNumber": "099123456",
    "clinicId": "clinic-1"
  }
}
```

**HCEN Processing**:
1. MDB receives message
2. Validates patient data
3. Registers in INUS (National User Index)
4. Assigns unique INUS ID
5. Optionally validates age via PDI integration

---

### 2. Document Metadata Event

**Trigger**: When a clinical document is created via `POST /api/documents`

**Queue**: `java:/jms/queue/DocumentRegistration`

**Message Format**:
```json
{
  "messageId": "msg-660e8400-e29b-41d4-a716-446655440001",
  "timestamp": "2025-11-13T10:45:00",
  "sourceSystem": "prestador-de-salud",
  "eventType": "DOCUMENT_CREATED",
  "payload": {
    "patientCI": "12345678",
    "documentType": "CLINICAL_NOTE",
    "documentLocator": "http://localhost:8080/prestador/api/documents/1",
    "documentHash": "sha256:a1b2c3d4e5f678901234567890123456...",
    "createdBy": "professional-5",
    "createdAt": "2025-11-13T10:45:00",
    "clinicId": "clinic-1",
    "specialtyId": 2,
    "documentTitle": "Consulta General",
    "documentDescription": "Control de rutina"
  }
}
```

**Key Fields**:
- `documentLocator` - **URL to retrieve the actual document** from Prestador de Salud
- `documentHash` - SHA-256 hash for integrity verification
- `patientCI` - Patient's national ID (links to INUS)

**HCEN Processing**:
1. MDB receives message
2. Validates document metadata
3. Registers metadata in RNDC (National Clinical Document Registry)
4. **Stores the document locator URL** for future retrieval
5. Links document to patient via CI

**Important**: HCEN stores **ONLY metadata**, not the actual document. The document remains in Prestador de Salud storage.

---

## Document Retrieval Flow

When a patient or professional requests a document through HCEN:

```
┌────────────┐
│   Patient  │
│  or        │
│Professional│
└─────┬──────┘
      │
      │ 1. Request clinical history
      │
      ▼
┌─────────────────┐
│  HCEN Central   │
│                 │
│  2. Query RNDC  │
│     for patient │
│     documents   │
└────────┬────────┘
         │
         │ 3. Get document locator URL
         │    http://localhost:8080/prestador/api/documents/1
         │
         ▼
┌──────────────────────┐
│  Prestador de Salud  │
│                      │
│  4. GET /api/documents/1
│                      │
│  5. Return document  │
└──────────────────────┘
         │
         │ 6. Document returned to HCEN
         │
         ▼
┌─────────────────┐
│  HCEN displays  │
│  document to    │
│  requester      │
└─────────────────┘
```

**This allows**:
- Documents stay at the source (Prestador)
- HCEN has a centralized index
- Patients see all their documents from all providers
- Providers can access documents from other providers (with patient consent)

---

## Configuration

### 1. HCEN Central Configuration (WildFly)

**File**: `standalone-full.xml` or via CLI

**Create JMS Queues**:
```bash
./bin/jboss-cli.sh --connect

/subsystem=messaging-activemq/server=default/jms-queue=UserRegistrationQueue:add(entries=["java:/jms/queue/UserRegistration"], durable=true)

/subsystem=messaging-activemq/server=default/jms-queue=DocumentRegistrationQueue:add(entries=["java:/jms/queue/DocumentRegistration"], durable=true)

reload
```

**Enable Remote Access**:
```bash
# Add application user for remote JMS access
./bin/add-user.sh -a -u jmsuser -p jmspassword -g guest
```

---

### 2. Prestador de Salud Configuration

**File**: `src/main/resources/jndi.properties`

```properties
# Initial Context Factory
java.naming.factory.initial=org.wildfly.naming.client.WildFlyInitialContextFactory

# HCEN Central WildFly Server URL
java.naming.provider.url=http-remoting://localhost:8080

# Authentication (if required)
java.naming.security.principal=jmsuser
java.naming.security.credentials=jmspassword

# Connection Factory
jms.connectionFactoryNames=jms/RemoteConnectionFactory

# Queue Names
jms.queue.userRegistration=jms/queue/UserRegistration
jms.queue.documentRegistration=jms/queue/DocumentRegistration
```

**Update for Production**:
- Change `localhost:8080` to actual HCEN Central server URL
- Use secure credentials (not hardcoded)
- Enable SSL/TLS for production

---

## Testing the Integration

### 1. Start HCEN Central

```bash
cd hcen
./gradlew wildFlyRun
```

**Verify JMS queues are created**:
```bash
./bin/jboss-cli.sh --connect
/subsystem=messaging-activemq/server=default/jms-queue=UserRegistrationQueue:read-resource()
```

---

### 2. Deploy Prestador de Salud

```bash
cd prestador_de_salud/prestador
mvn clean package
# Deploy WAR to Tomcat or WildFly
```

---

### 3. Test Patient Registration

**Create a patient** via Postman:
```
POST http://localhost:8080/prestador/api/patients
Content-Type: application/json

{
  "name": "Juan",
  "lastName": "Pérez",
  "documentNumber": "12345678",
  "birthDate": "1990-01-15",
  "email": "juan.perez@example.com",
  "clinicId": 1
}
```

**Check Prestador logs**:
```
INFO: Patient created successfully - ID: 1, CI: 12345678
INFO: Patient registration message sent to HCEN - CI: 12345678
```

**Check HCEN logs**:
```
INFO: Processing user registration message - ID: msg-..., Source: prestador-de-salud
INFO: Registering user - CI: 12345678, Name: Juan Pérez
INFO: User registration successful - INUS ID: INUS-...
```

**Verify in HCEN database**:
```sql
SELECT * FROM inus.inus_users WHERE ci = '12345678';
```

---

### 4. Test Document Registration

**Create a clinical document**:
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

**Check Prestador logs**:
```
INFO: Clinical document created successfully - ID: 1, Patient ID: 1, Type: CLINICAL_NOTE
INFO: Document metadata sent to HCEN RNDC - Document ID: 1, Patient CI: 12345678,
      Locator: http://localhost:8080/prestador/api/documents/1
```

**Check HCEN logs**:
```
INFO: Processing document registration message - ID: msg-..., Source: prestador-de-salud
INFO: Registering document - Patient CI: 12345678, Type: CLINICAL_NOTE
INFO: Document registration successful - Document ID: ...
```

**Verify in HCEN database**:
```sql
SELECT * FROM rndc.rndc_documents WHERE patient_ci = '12345678';
```

**Check document locator**:
```sql
SELECT document_locator FROM rndc.rndc_documents WHERE patient_ci = '12345678';
-- Result: http://localhost:8080/prestador/api/documents/1
```

---

### 5. Test Document Retrieval from HCEN

**HCEN calls the document locator URL**:
```
GET http://localhost:8080/prestador/api/documents/1
```

**Response**: Full clinical document JSON with all details

---

## Error Handling

### Graceful Degradation

If HCEN Central is unavailable:
- Patients and documents are **still saved locally** in Prestador
- JMS message sending fails silently (logged as WARNING)
- Application continues to function
- Messages can be resent manually later

**Log Example**:
```
WARNING: Failed to send patient registration to HCEN (patient already saved locally) - CI: 12345678
javax.naming.NameNotFoundException: jms/RemoteConnectionFactory
```

---

## Monitoring

### Check Queue Status in HCEN

```bash
./bin/jboss-cli.sh --connect

# Check message count
/subsystem=messaging-activemq/server=default/jms-queue=UserRegistrationQueue:count-messages()

# Check consumer count
/subsystem=messaging-activemq/server=default/jms-queue=UserRegistrationQueue:read-resource(include-runtime=true)
```

---

## Security Considerations

1. **Authentication**: Use WildFly application users for remote JMS access
2. **Encryption**: Enable SSL/TLS for production JMS connections
3. **Authorization**: Restrict queue access to authorized systems
4. **Audit**: All messages are logged with timestamps and source systems
5. **Document Access**: HCEN enforces access policies before retrieving documents from Prestador

---

## Next Steps

1. Configure WildFly application users for JMS authentication
2. Enable SSL/TLS for JMS connections in production
3. Implement retry mechanism for failed messages (optional)
4. Set up monitoring/alerting for queue depth
5. Implement HCEN → Prestador authentication when retrieving documents

---

**Author**: TSE 2025 Group 9
**Date**: 2025-11-13
**Version**: 1.0
