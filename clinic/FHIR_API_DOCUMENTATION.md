# Documentación del Endpoint FHIR

## Descripción

Este endpoint REST permite obtener todos los documentos clínicos de un paciente en formato FHIR R4, utilizando su cédula de identidad como parámetro de búsqueda.

## URL Base

```
http://localhost:8080/clinic/api/fhir
```

## Endpoints Disponibles

### 1. Obtener Documentos por Cédula

**Endpoint:** `GET /api/fhir/documents`

**Descripción:** Retorna todos los documentos clínicos asociados a un paciente identificado por su cédula de identidad, en formato FHIR Bundle.

**Parámetros de Query:**
- `cedula` (requerido): Número de cédula de identidad del paciente

**Ejemplo de Request:**
```bash
GET http://localhost:8080/clinic/api/fhir/documents?cedula=12345678
```

**Ejemplo con cURL:**
```bash
curl -X GET "http://localhost:8080/clinic/api/fhir/documents?cedula=12345678" \
  -H "Accept: application/json"
```

**Ejemplo con PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/clinic/api/fhir/documents?cedula=12345678" -Method GET
```

**Respuesta Exitosa (200 OK):**

El endpoint retorna un **Bundle FHIR** que contiene:

1. **Patient Resource**: Información del paciente
2. **Practitioner Resources**: Información de los profesionales que atendieron
3. **Encounter Resources**: Cada consulta/encuentro médico
4. **Condition Resources**: Diagnósticos realizados
5. **Observation Resources**: Signos vitales registrados (presión arterial, temperatura, pulso, etc.)
6. **MedicationRequest Resources**: Prescripciones médicas
7. **DocumentReference Resources**: Referencias a los documentos clínicos

**Ejemplo de Respuesta (JSON):**
```json
{
  "resourceType": "Bundle",
  "id": "bundle-patient-1",
  "type": "collection",
  "entry": [
    {
      "fullUrl": "Patient/1",
      "resource": {
        "resourceType": "Patient",
        "id": "1",
        "identifier": [
          {
            "system": "urn:oid:2.16.858.1.1.3",
            "value": "12345678",
            "type": {
              "coding": [
                {
                  "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
                  "code": "SS",
                  "display": "Social Security Number"
                }
              ]
            }
          },
          {
            "system": "urn:oid:2.16.858.1.1.1",
            "value": "INUS001"
          }
        ],
        "name": [
          {
            "family": "Silva",
            "given": ["Ana"],
            "use": "official"
          }
        ],
        "birthDate": "1986-03-15",
        "gender": "female"
      }
    },
    {
      "fullUrl": "Encounter/encounter-1",
      "resource": {
        "resourceType": "Encounter",
        "id": "encounter-1",
        "status": "finished",
        "class": {
          "system": "http://terminology.hl7.org/CodeSystem/v3-ActCode",
          "code": "AMB",
          "display": "ambulatory"
        },
        "subject": {
          "reference": "Patient/1"
        },
        "period": {
          "start": "2024-12-15"
        },
        "participant": [
          {
            "individual": {
              "reference": "Practitioner/1"
            }
          }
        ],
        "reasonCode": [
          {
            "coding": [
              {
                "display": "Dolor de pecho"
              }
            ]
          }
        ]
      }
    },
    {
      "fullUrl": "Condition/condition-1",
      "resource": {
        "resourceType": "Condition",
        "id": "condition-1",
        "clinicalStatus": {
          "coding": [
            {
              "system": "http://terminology.hl7.org/CodeSystem/condition-clinical",
              "code": "active",
              "display": "Active"
            }
          ]
        },
        "subject": {
          "reference": "Patient/1"
        },
        "encounter": {
          "reference": "Encounter/encounter-1"
        },
        "code": {
          "text": "Hipertensión arterial"
        },
        "onsetDateTime": "2024-12-15"
      }
    }
    // ... más recursos (Observations, MedicationRequests, etc.)
  ]
}
```

**Códigos de Respuesta:**

- **200 OK**: Documentos encontrados y retornados exitosamente
- **400 Bad Request**: Parámetro `cedula` faltante o vacío
- **404 Not Found**: No se encontró un paciente con la cédula proporcionada
- **500 Internal Server Error**: Error interno del servidor

**Ejemplo de Error (404):**
```json
{
  "error": "Paciente no encontrado con la cédula proporcionada"
}
```

**Ejemplo de Error (400):**
```json
{
  "error": "El parámetro 'cedula' es requerido"
}
```

### 2. Health Check

**Endpoint:** `GET /api/fhir/health`

**Descripción:** Endpoint de verificación de salud del servicio.

**Ejemplo:**
```bash
GET http://localhost:8080/clinic/api/fhir/health
```

**Respuesta:**
```json
{
  "status": "OK",
  "service": "FHIR Document Endpoint"
}
```

## Mapeo de Datos a FHIR

### Estructura de Recursos FHIR Generados

| Dato del Sistema | Recurso FHIR | Descripción |
|-----------------|--------------|-------------|
| Patient | `Patient` | Información del paciente |
| Professional | `Practitioner` | Información del profesional de salud |
| ClinicalDocument.dateOfVisit | `Encounter` | Encuentro/consulta médica |
| ClinicalDocument.diagnosis | `Condition` | Diagnóstico médico |
| ClinicalDocument.vitalSigns | `Observation` (múltiples) | Signos vitales (presión, temperatura, pulso, etc.) |
| ClinicalDocument.prescriptions | `MedicationRequest` (múltiples) | Prescripciones de medicamentos |
| ClinicalDocument | `DocumentReference` | Referencia al documento clínico completo |

### Signos Vitales Mapeados

Los signos vitales se mapean a recursos `Observation` con códigos LOINC:

| Signo Vital | Código LOINC | Display |
|------------|--------------|---------|
| Presión Arterial | 85354-9 | Blood pressure panel |
| Temperatura | 8310-5 | Body temperature |
| Pulso | 8867-4 | Heart rate |
| Frecuencia Respiratoria | 9279-1 | Respiratory rate |
| Saturación O2 | 2708-6 | Oxygen saturation in Arterial blood |
| Peso | 29463-7 | Body weight |
| Altura | 8302-2 | Body height |

## Ejemplos de Uso

### Ejemplo 1: Obtener documentos de un paciente

```bash
# Usando cURL
curl -X GET "http://localhost:8080/clinic/api/fhir/documents?cedula=12345678" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json"
```

### Ejemplo 2: Usando Postman

1. Método: `GET`
2. URL: `http://localhost:8080/clinic/api/fhir/documents?cedula=12345678`
3. Headers:
   - `Accept: application/json`
   - `Content-Type: application/json`

### Ejemplo 3: Usando JavaScript (fetch)

```javascript
fetch('http://localhost:8080/clinic/api/fhir/documents?cedula=12345678', {
  method: 'GET',
  headers: {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  console.log('Bundle FHIR:', data);
  // Procesar los recursos del bundle
  data.entry.forEach(entry => {
    console.log('Recurso:', entry.resource.resourceType, entry.resource.id);
  });
})
.catch(error => console.error('Error:', error));
```

### Ejemplo 4: Usando Java (HttpClient)

```java
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/clinic/api/fhir/documents?cedula=12345678"))
    .header("Accept", "application/json")
    .GET()
    .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
System.out.println("Respuesta: " + response.body());
```

## Notas Importantes

1. **Formato de Respuesta**: El endpoint retorna JSON en formato FHIR R4.

2. **Cédula de Identidad**: Debe ser el número exacto de cédula registrado en el sistema. El sistema busca pacientes activos.

3. **Bundle Vacío**: Si el paciente existe pero no tiene documentos, se retorna un Bundle con solo el recurso `Patient`.

4. **Múltiples Documentos**: Si un paciente tiene múltiples documentos, todos se incluyen en el mismo Bundle. Los recursos se deduplican automáticamente (no habrá duplicados de Patient, Practitioner, etc.).

5. **Referencias entre Recursos**: Los recursos FHIR usan referencias relativas (ej: `Patient/1`) que se resuelven dentro del mismo Bundle.

6. **Identificadores del Paciente**: El sistema incluye dos identificadores:
   - **Cédula de Identidad**: Sistema `urn:oid:2.16.858.1.1.3` con código `SS` (Social Security Number)
   - **ID INUS**: Sistema `urn:oid:2.16.858.1.1.1` (si está disponible)

7. **Códigos LOINC**: Los signos vitales usan códigos LOINC estándar para interoperabilidad.

8. **Diagnósticos**: Actualmente los diagnósticos se almacenan como texto libre. Para producción, se recomienda usar códigos SNOMED CT.

9. **Recursos Condicionales**: Algunos recursos solo aparecen si tienen datos:
   - `Condition`: Solo si el documento tiene diagnóstico
   - `Observation`: Solo si el documento tiene signos vitales registrados

## Integración con HCEN Central

Este endpoint está diseñado para ser consumido por el componente central del HCEN. El formato FHIR permite:

- **Interoperabilidad**: Estándar internacional para intercambio de información médica
- **Extensibilidad**: Fácil agregar nuevos recursos o campos
- **Validación**: Los recursos FHIR pueden ser validados contra el esquema oficial
- **Compatibilidad**: Compatible con sistemas que usen FHIR

## Troubleshooting

### Error 404 - Paciente no encontrado
- Verificar que la cédula esté correctamente registrada en el sistema
- Verificar que el paciente esté activo
- Revisar los logs del servidor para más detalles

### Error 500 - Error interno
- Revisar los logs de WildFly
- Verificar que la base de datos esté accesible
- Verificar que las dependencias FHIR estén correctamente desplegadas

### Respuesta vacía
- Verificar que el paciente tenga documentos clínicos asociados
- Revisar que los documentos estén correctamente guardados en la base de datos

## Versión

- **API Version**: 1.0.0
- **FHIR Version**: R4 (4.0.1)
- **Última actualización**: Enero 2025

## Cambios Recientes

- **Enero 2025**: 
  - Actualizado identificador de cédula: ahora usa código `SS` (Social Security Number) con sistema `urn:oid:2.16.858.1.1.3`
  - Implementada deduplicación automática de recursos en el Bundle
  - Agregado soporte para ID INUS como identificador secundario del paciente

