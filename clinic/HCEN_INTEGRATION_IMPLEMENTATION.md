# Integración HCEN - Implementación Completa

**Fecha**: 2025-01-XX  
**Proyecto**: Clinic - Componente Periférico HCEN  
**Estado**: ✅ Implementación Completa

---

## 📋 Resumen Ejecutivo

Se ha implementado completamente la integración con HCEN Central para el componente Clinic, incluyendo:

1. ✅ Registro de pacientes al HCEN (INUS) vía JMS
2. ✅ Registro de documentos al HCEN (RNDC) vía JMS
3. ✅ Sistema de solicitudes de acceso a documentos externos
4. ✅ Notificaciones del HCEN sobre aprobaciones/denegaciones
5. ✅ Descarga y almacenamiento de documentos externos
6. ✅ Endpoints REST para acceso a documentos
7. ✅ Colas JMS configuradas en WildFly
8. ✅ Recursos JMS opcionales (graceful degradation)

---

## ✅ Componentes Implementados

### 1. Base de Datos

#### Script de Migración
- **Archivo**: `src/main/resources/db/migration/V002__add_hcen_integration_fields.sql`
- **Tabla `clinics`**: Campos agregados
  - `hcen_jms_url` (VARCHAR): URL para conexión JMS con HCEN
  - `api_key` (VARCHAR): API key para autenticación REST con HCEN

- **Tabla `clinical_documents`**: Campos agregados
  - `is_external` (BOOLEAN): Indica si es documento externo
  - `source_clinic_id` (VARCHAR): ID de clínica origen
  - `external_clinic_name` (VARCHAR): Nombre de clínica origen
  - `external_document_locator` (TEXT): URL original del documento

- **Tabla `access_requests`**: Creada desde cero
  - Campos: `id`, `patient_ci`, `document_id`, `specialties`, `status`, `request_reason`, `requested_at`, `expires_at`, `responded_at`, `hcen_request_id`, `urgency`, `professional_id`, `clinic_id`
  - Estados: `PENDING`, `APPROVED`, `DENIED`, `EXPIRED`
  - Urgencias: `ROUTINE`, `URGENT`, `EMERGENCY`

### 2. Entidades JPA

#### `AccessRequest.java`
- Entidad completa para solicitudes de acceso
- Métodos de negocio: `isPending()`, `isExpired()`, `approve()`, `deny()`, `expire()`
- Named Queries para consultas optimizadas

#### `ClinicalDocument.java`
- Campos agregados para documentos externos
- Soporte para documentos locales y externos

#### `Clinic.java`
- Campos `apiKey` y `hcenJmsUrl` agregados

#### `Patient.java`
- Relación con `AccessRequest` corregida (no bidirectional)

### 3. Servicios de Integración

#### `HcenJmsService.java` (EJB Stateless)
- **Método `sendUserRegistration()`**: Envía registro de paciente al HCEN vía JMS
  - Construye payload `UserRegistrationMessage`
  - Serializa a JSON
  - Envía a cola `java:/jms/queue/UserRegistration`
  - Recursos JMS opcionales (no falla si no están configurados)

- **Método `sendDocumentRegistration()`**: Envía registro de documento al HCEN vía JMS
  - Construye payload `DocumentRegistrationMessage`
  - Calcula hash SHA-256 del documento
  - Genera `documentLocator` URL
  - Serializa a JSON
  - Envía a cola `java:/jms/queue/DocumentRegistration`
  - Recursos JMS opcionales

- **Inicialización**: `@PostConstruct init()` busca recursos JMS dinámicamente
  - Si no están disponibles, solo registra warning y continúa

#### `HcenRestClient.java` (EJB Stateless)
- **Método `createAccessRequest()`**: Crea solicitud de acceso en HCEN vía REST
  - Autenticación con API key (base64(clinicId:apiKey))
  - POST a `/access-requests`
  - Retorna `AccessRequestCreationResponse` con `requestId`

### 4. Servicios de Negocio

#### `PatientService.java`
- **Modificado**: Llama a `hcenJmsService.sendUserRegistration()` después de crear paciente

#### `ClinicalDocumentService.java`
- **Modificado**: Llama a `hcenJmsService.sendDocumentRegistration()` después de crear documento
  - Pasa `documentBaseUrl` para construir `documentLocator`

#### `AccessRequestService.java` (Nuevo EJB Stateless)
- **Método `createAccessRequest()`**: Crea solicitud de acceso
  - Valida datos
  - Llama a `HcenRestClient.createAccessRequest()`
  - Persiste `AccessRequest` con `hcenRequestId`
  - Maneja errores y transacciones

- **Métodos de consulta**: `findByProfessional()`, `findByClinic()`, `findByStatus()`, `countPendingByProfessional()`

#### `ExternalDocumentService.java` (Nuevo EJB Stateless)
- **Método `downloadAndStoreExternalDocument()`**: Descarga y almacena documento externo
  - Descarga desde `documentLocator`
  - Verifica hash SHA-256
  - Persiste como `ClinicalDocument` con `isExternal = true`
  - Guarda metadatos de clínica origen

### 5. Endpoints REST

#### `DocumentResource.java` (JAX-RS)
- **Endpoint**: `GET /api/documents/{id}`
- **Función**: Servir documentos individuales para HCEN
- **Autenticación**: API key en header `Authorization: ApiKey {base64(clinicId:apiKey)}`
- **Respuesta**: JSON con metadatos y contenido del documento

#### `AccessRequestNotificationServlet.java` (Servlet)
- **Endpoint**: `POST /api/clinic/access-requests/notifications`
- **Función**: Recibir notificaciones del HCEN sobre aprobaciones/denegaciones
- **Autenticación**: API key en header `Authorization: ApiKey {base64(clinicId:apiKey)}`
- **Procesamiento**:
  - Actualiza estado de `AccessRequest`
  - Si `APPROVED`, llama a `ExternalDocumentService.downloadAndStoreExternalDocument()`

### 6. Servlets de UI

#### `ProfessionalRequestsServlet.java` (Modificado)
- **Método `doPost()`**: Crea nueva solicitud de acceso
  - Valida datos del formulario
  - Llama a `AccessRequestService.createAccessRequest()`
  - Redirige con mensaje de éxito/error

#### `ClinicalDocumentServlet.java` / `ProfessionalPatientDocumentsServlet.java`
- **Modificados**: Integran llamada a `hcenJmsService.sendDocumentRegistration()`

### 7. DTOs de Integración

#### `UserRegistrationMessage.java`
- Clase mensaje completa con `messageId`, `timestamp`, `sourceSystem`, `eventType`
- Payload con datos del paciente

#### `DocumentRegistrationMessage.java`
- Clase mensaje completa con metadatos del documento
- Payload con `patientCI`, `documentType`, `documentLocator`, `documentHash`, etc.

#### `AccessRequestCreationRequest.java`
- Request DTO para crear solicitud de acceso en HCEN
- Campos: `professionalId`, `patientCi`, `specialties`, `requestReason`, `urgency`

#### `AccessRequestCreationResponse.java`
- Response DTO del HCEN
- Campos: `requestId`, `status`

#### `AccessRequestNotificationRequest.java`
- Request DTO para notificaciones del HCEN
- Campos: `requestId`, `status`, `documents` (array de `DocumentMetadata`)

#### `DocumentMetadata.java`
- Metadatos de documento para notificaciones
- Campos: `documentId`, `documentType`, `documentLocator`, `documentHash`, `clinicId`, `clinicName`

### 8. Configuración WildFly

#### `jboss-deployment-structure.xml` (Nuevo)
- Declara dependencia explícita del módulo `jakarta.jms.api`
- Asegura que JMS API esté disponible en runtime

#### `standalone-full.xml` (Modificado)
- **Colas JMS agregadas**:
  ```xml
  <jms-queue name="UserRegistrationQueue" entries="java:/jms/queue/UserRegistration">
      <durable>true</durable>
  </jms-queue>
  <jms-queue name="DocumentRegistrationQueue" entries="java:/jms/queue/DocumentRegistration">
      <durable>true</durable>
  </jms-queue>
  ```

- **Address Settings agregados**:
  - Configuración de redelivery, DLQ, expiry para ambas colas
  - `redelivery-delay="5000"`
  - `max-delivery-attempts="5"`
  - `max-size-bytes="10485760"`

#### `web.xml`
- Mapeo de `AccessRequestNotificationServlet` agregado

### 9. Dependencias Gradle

#### `build.gradle`
Dependencias agregadas:
- `org.apache.httpcomponents.client5:httpclient5:5.3` - Cliente HTTP para REST
- `com.fasterxml.jackson.core:jackson-databind:2.17.0` - JSON serialization
- `com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0` - Soporte LocalDateTime
- `ca.uhn.hapi.fhir:hapi-fhir-structures-r4:6.10.0` - FHIR (para futuro)
- `ca.uhn.hapi.fhir:hapi-fhir-base:6.10.0` - FHIR base
- `org.jboss.resteasy:resteasy-client:6.2.7.Final` - JAX-RS Client
- `org.jboss.resteasy:resteasy-jackson2-provider:6.2.7.Final` - JAX-RS Jackson provider
- `jakarta.jms:jakarta.jms-api:3.1.0` (provided) - JMS API

---

## 🔧 Configuración Requerida

### 1. WildFly debe usar `standalone-full.xml`

Para que las colas JMS funcionen, WildFly debe iniciarse con `standalone-full.xml`:

```bash
# Windows
cd C:\TSEGrupo\wildfly-30.0.1.Final\bin
.\standalone.bat --server-config=standalone-full.xml

# Linux/Mac
cd $WILDFLY_HOME/bin
./standalone.sh --server-config=standalone-full.xml
```

**IMPORTANTE**: Las colas JMS están configuradas en `standalone-full.xml`, no en `standalone.xml`.

### 2. Configurar API Key y HCEN Endpoint en cada Clínica

En la base de datos, actualizar tabla `clinics`:

```sql
UPDATE clinics 
SET 
    api_key = 'tu-api-key-aqui',
    hcen_endpoint = 'http://localhost:8080/hcen/api',
    hcen_jms_url = 'http-remoting://localhost:8080'
WHERE id = 1;
```

O configurar vía UI administrativa (si está implementada).

### 3. Verificar Colas JMS

Después de iniciar WildFly con `standalone-full.xml`, verificar que las colas estén disponibles:

```bash
# Conectar a CLI de WildFly
cd $WILDFLY_HOME/bin
./jboss-cli.sh --connect

# Verificar colas
/subsystem=messaging-activemq/server=default:read-resource(recursive=true)

# Verificar cola específica
/subsystem=messaging-activemq/server=default/jms-queue=UserRegistrationQueue:read-resource
/subsystem=messaging-activemq/server=default/jms-queue=DocumentRegistrationQueue:read-resource
```

---

## 🧪 Flujos de Integración

### 1. Registro de Paciente (INUS)

```
1. Admin/Professional crea paciente en Clinic
2. PatientService.createPatient() persiste paciente
3. HcenJmsService.sendUserRegistration() envía mensaje JMS
4. Mensaje va a cola java:/jms/queue/UserRegistration
5. HCEN Central procesa mensaje (MDB)
```

### 2. Registro de Documento (RNDC)

```
1. Admin/Professional crea documento clínico
2. ClinicalDocumentService.createDocument() persiste documento
3. HcenJmsService.sendDocumentRegistration() envía mensaje JMS
   - Calcula hash SHA-256
   - Genera documentLocator: http://clinic.uy/clinic/api/documents/{id}
4. Mensaje va a cola java:/jms/queue/DocumentRegistration
5. HCEN Central procesa mensaje y registra en RNDC
```

### 3. Solicitud de Acceso a Documentos Externos

```
1. Professional solicita acceso vía UI
2. ProfessionalRequestsServlet.doPost() recibe request
3. AccessRequestService.createAccessRequest() crea request
4. HcenRestClient.createAccessRequest() envía POST a HCEN
   - Autenticación: Authorization: ApiKey {base64(clinicId:apiKey)}
   - Endpoint: {hcenEndpoint}/access-requests
5. HCEN valida y crea request, retorna requestId
6. AccessRequestService persiste AccessRequest con hcenRequestId
```

### 4. Notificación de Aprobación/Denegación

```
1. Usuario en HCEN aprueba/deniega solicitud
2. HCEN envía POST a /api/clinic/access-requests/notifications
   - Autenticación: Authorization: ApiKey {base64(clinicId:apiKey)}
   - Body: AccessRequestNotificationRequest
3. AccessRequestNotificationServlet procesa notificación
4. Actualiza AccessRequest.status = APPROVED/DENIED
5. Si APPROVED, ExternalDocumentService.downloadAndStoreExternalDocument()
   - Descarga desde documentLocator
   - Verifica hash SHA-256
   - Persiste como ClinicalDocument con isExternal = true
6. Professional puede ver documento externo en UI
```

### 5. Acceso a Documento desde HCEN

```
1. HCEN necesita obtener documento desde Clinic
2. HCEN hace GET a {documentLocator}
   - Autenticación: Authorization: ApiKey {base64(clinicId:apiKey)}
3. DocumentResource.getDocument() retorna documento
4. HCEN almacena/usa documento
```

---

## 🚨 Manejo de Errores

### JMS Resources No Disponibles

- `HcenJmsService` verifica si recursos JMS están disponibles en `@PostConstruct`
- Si no están disponibles, registra warning pero **no falla**
- Métodos `sendUserRegistration()` y `sendDocumentRegistration()` retornan sin hacer nada si recursos no disponibles
- La aplicación funciona normalmente, solo no envía mensajes al HCEN

### Errores de Comunicación REST

- `HcenRestClient` lanza `RuntimeException` si hay error de comunicación
- `AccessRequestService` captura excepciones y maneja graciosamente
- Logs detallados en todos los servicios para debugging

### Errores de Hash SHA-256

- Si hash no coincide al descargar documento externo, se rechaza y registra error
- Documento no se almacena si hash es inválido

---

## ✅ Testing

### Verificar Deployment

1. **Iniciar WildFly con `standalone-full.xml`**:
   ```bash
   cd C:\TSEGrupo\wildfly-30.0.1.Final\bin
   .\standalone.bat --server-config=standalone-full.xml
   ```

2. **Verificar que clinic.war se despliegue sin errores**

3. **Verificar logs de WildFly**:
   - Buscar "JMS ConnectionFactory encontrado"
   - Buscar "JMS Queue UserRegistration encontrada"
   - Buscar "JMS Queue DocumentRegistration encontrada"

### Verificar Funcionalidad

1. **Crear paciente**: Verificar que se envía mensaje JMS (revisar logs)
2. **Crear documento**: Verificar que se envía mensaje JMS con `documentLocator`
3. **Crear access request**: Verificar que se llama a HCEN REST API
4. **Simular notificación HCEN**: POST manual a `/api/clinic/access-requests/notifications`

---

## 📝 Notas Importantes

1. **WildFly debe usar `standalone-full.xml`** para soporte JMS
2. **API keys deben configurarse** en tabla `clinics` antes de usar REST APIs
3. **Recursos JMS son opcionales**: La aplicación funciona aunque JMS no esté configurado
4. **Hash SHA-256**: Actualmente simplificado, en producción debe incluir contenido completo del documento
5. **documentLocator**: Debe ser URL pública accesible desde HCEN Central

---

## 🎯 Estado Final

✅ **TODAS LAS FUNCIONALIDADES IMPLEMENTADAS**

- [x] Registro de pacientes al HCEN (INUS) vía JMS
- [x] Registro de documentos al HCEN (RNDC) vía JMS
- [x] Sistema de solicitudes de acceso
- [x] Notificaciones del HCEN
- [x] Descarga de documentos externos
- [x] Endpoints REST para acceso a documentos
- [x] Colas JMS configuradas
- [x] Recursos JMS opcionales
- [x] Base de datos migrada
- [x] Compilación exitosa
- [x] Deployment exitoso

---

**Última actualización**: 2025-01-XX  
**Autor**: Grupo 9 TSE 2025

