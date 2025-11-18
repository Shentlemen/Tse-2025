# Guía para Registro de Clínicas desde HCEN

## Resumen

Esta guía explica cómo el **HCEN Central** debe conectarse al **Sistema Clinic** para registrar nuevas clínicas cuando se crean en el sistema HCEN.

---

## Endpoint de Registro

### Información General

- **Método HTTP**: `POST`
- **URL**: `http://localhost:8080/clinic/api/clinics` (o la URL base configurada del sistema Clinic)
- **Content-Type**: `application/json`
- **Autenticación**: Actualmente no requiere autenticación (puede implementarse en el futuro)

---

## Request

### Headers

```
Content-Type: application/json
```

### Body (JSON)

```json
{
  "code": "clinic-550e8400-e29b-41d4-a716-446655440000",
  "name": "Clínica San José",
  "description": "Clínica privada de atención médica integral",
  "address": "Av. 18 de Julio 1234",
  "phone": "024123456",
  "email": "contacto@clinicasanjose.com.uy",
  "hcen_endpoint": "http://localhost:8080/hcen/api",
  "active": true
}
```

### Campos del Request

| Campo | Tipo | Obligatorio | Descripción | Ejemplo |
|-------|------|-------------|-------------|---------|
| `code` | String | **Sí** | Código único de la clínica (debe ser único). Se recomienda usar el `clinicId` del HCEN central | `"clinic-550e8400..."` |
| `name` | String | **Sí** | Nombre oficial de la clínica | `"Clínica San José"` |
| `description` | String | No | Descripción de la clínica | `"Clínica privada..."` |
| `address` | String | No | Dirección física de la clínica | `"Av. 18 de Julio 1234"` |
| `phone` | String | No | Teléfono de contacto | `"024123456"` |
| `email` | String | No | Email de contacto | `"contacto@clinica.com"` |
| `hcen_endpoint` | String | No | URL del HCEN central (mismo para todas las clínicas) | `"http://localhost:8080/hcen/api"` |
| `active` | Boolean | No | Estado activo/inactivo (default: `true`) | `true` |

**Notas importantes:**
- El campo `code` **debe ser único** en el sistema. Si ya existe una clínica con ese código, retornará un error 409 (Conflict).
- El campo `hcen_endpoint` es opcional pero recomendado. Todas las clínicas deben apuntar a la misma URL del HCEN central.
- Los campos `id`, `created_at` y `updated_at` se generan automáticamente en el sistema Clinic.

---

## Response

### Respuesta Exitosa (201 Created)

```json
{
  "id": 1,
  "code": "clinic-550e8400-e29b-41d4-a716-446655440000",
  "name": "Clínica San José",
  "description": "Clínica privada de atención médica integral",
  "address": "Av. 18 de Julio 1234",
  "phone": "024123456",
  "email": "contacto@clinicasanjose.com.uy",
  "hcen_endpoint": "http://localhost:8080/hcen/api",
  "logo_path": null,
  "theme_colors": null,
  "active": true,
  "created_at": "2025-11-07T10:30:00",
  "updated_at": null
}
```

### Respuesta de Error (400 Bad Request)

```json
{
  "error": "VALIDATION_ERROR",
  "message": "El campo 'code' es requerido"
}
```

### Respuesta de Error (409 Conflict - Clínica ya existe)

```json
{
  "error": "CLINIC_ALREADY_EXISTS",
  "message": "Ya existe una clínica con el código: clinic-550e8400-e29b-41d4-a716-446655440000"
}
```

### Respuesta de Error (500 Internal Server Error)

```json
{
  "error": "INTERNAL_ERROR",
  "message": "Error al registrar la clínica en el sistema"
}
```

---

## Ejemplos de Uso

### Ejemplo 1: cURL

```bash
curl -X POST http://localhost:8080/clinic/api/clinics \
  -H "Content-Type: application/json" \
  -d '{
    "code": "clinic-550e8400-e29b-41d4-a716-446655440000",
    "name": "Clínica San José",
    "description": "Clínica privada de atención médica integral",
    "address": "Av. 18 de Julio 1234",
    "phone": "024123456",
    "email": "contacto@clinicasanjose.com.uy",
    "hcen_endpoint": "http://localhost:8080/hcen/api",
    "active": true
  }'
```

### Ejemplo 2: Java (usando Apache HttpClient)

```java
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClinicRegistrationClient {
    
    private static final String CLINIC_SERVICE_URL = "http://localhost:8080/clinic/api/clinics";
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public ClinicResponse registerClinic(ClinicRegistrationRequest request) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            HttpPost httpPost = new HttpPost(CLINIC_SERVICE_URL);
            httpPost.setHeader("Content-Type", "application/json");
            
            String jsonRequest = objectMapper.writeValueAsString(request);
            httpPost.setEntity(new StringEntity(jsonRequest));
            
            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (statusCode == 201) {
                    return objectMapper.readValue(responseBody, ClinicResponse.class);
                } else {
                    ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
                    throw new RuntimeException("Error al registrar clínica: " + error.getMessage());
                }
            });
        } finally {
            httpClient.close();
        }
    }
}
```

### Ejemplo 3: JavaScript/Fetch

```javascript
const registerClinicInSystem = async (clinicData) => {
  try {
    const response = await fetch('http://localhost:8080/clinic/api/clinics', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        code: clinicData.clinicId,  // Usar el clinicId del HCEN como code
        name: clinicData.clinicName,
        description: clinicData.description || '',
        address: clinicData.address || '',
        phone: clinicData.phoneNumber || '',
        email: clinicData.email || '',
        hcen_endpoint: 'http://localhost:8080/hcen/api',  // Mismo para todas
        active: true
      })
    });

    if (response.status === 201) {
      const result = await response.json();
      console.log('Clínica registrada en sistema Clinic:', result.id);
      return result;
    } else {
      const error = await response.json();
      console.error('Error al registrar clínica:', error.message);
      throw new Error(error.message);
    }
  } catch (error) {
    console.error('Error en la petición:', error);
    throw error;
  }
};
```

---

## Flujo de Integración Completo

### Escenario: Registrar Nueva Clínica desde HCEN

1. **HCEN Central** registra la clínica en su propia base de datos (tabla `clinics.clinics`)
2. **HCEN Central** genera un `clinicId` único (formato: `clinic-{uuid}`) y un `apiKey`
3. **HCEN Central** llama al endpoint `POST /api/clinics` del Sistema Clinic para registrar la clínica
4. **Sistema Clinic** valida que el `code` no exista ya
5. **Sistema Clinic** crea el registro en su tabla `clinics` usando el `clinicId` como `code`
6. **Sistema Clinic** retorna el registro creado con el `id` interno generado (201 Created)
7. **HCEN Central** puede almacenar el `id` del sistema Clinic para referencia futura

---

## Códigos de Estado HTTP

| Código | Significado | Descripción |
|--------|-------------|-------------|
| `201 Created` | Éxito | Clínica creada exitosamente |
| `400 Bad Request` | Error de validación | Datos inválidos o campos requeridos faltantes |
| `409 Conflict` | Conflicto | Ya existe una clínica con ese código |
| `500 Internal Server Error` | Error del servidor | Error interno al procesar la solicitud |

---

## Notas Importantes

1. **Código Único**: El campo `code` debe ser único. Se recomienda usar el `clinicId` generado por el HCEN central para mantener la correspondencia entre ambos sistemas.

2. **URL Base**: Todas las clínicas comparten la misma URL base del Sistema Clinic (ejemplo: `http://localhost:8080/clinic/api`). Las clínicas se diferencian por su `code` único.

3. **HCEN Endpoint**: Todas las clínicas deben tener el mismo `hcen_endpoint` (URL del HCEN central), ya que todas se comunican con el mismo servidor central.

4. **Campos Opcionales**: Muchos campos son opcionales. El sistema Clinic maneja valores `null` correctamente.

5. **Manejo de Errores**: El Sistema Clinic retorna códigos de estado HTTP apropiados. El HCEN debe manejar estos códigos correctamente.

6. **Idempotencia**: Si se intenta registrar una clínica con un `code` que ya existe, el sistema retornará 409 Conflict. El HCEN debe verificar esto antes de intentar registrar nuevamente.

---

## Configuración en HCEN

Para que el HCEN pueda llamar a este endpoint, debe configurar la URL del sistema Clinic. Esto se hace típicamente en el `ClinicServiceClient` del HCEN:

```java
// En ClinicServiceConfiguration
private String clinicServiceUrl = "http://localhost:8080/clinic/api";
```

El HCEN ya tiene implementado el `ClinicServiceClient` que llama automáticamente a este endpoint cuando se registra una nueva clínica en el HCEN central.

---

## Contacto y Soporte

Si tienes problemas con la integración o necesitas ayuda adicional, contacta al equipo de desarrollo del Sistema Clinic.

