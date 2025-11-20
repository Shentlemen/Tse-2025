# Quick Test Guide: Clinic Registration Endpoint

## Endpoint
```
POST http://localhost:8080/clinic/api/clinics
```

## Headers Required
```
Content-Type: application/json
X-API-Key: <your-api-key-from-configuration>
```

## Test 1: Successful Registration

### Request
```bash
curl -X POST http://localhost:8080/clinic/api/clinics \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key-here" \
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

### Expected Response
```
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": "clinic-550e8400-e29b-41d4-a716-446655440000",
  "code": "clinic-550e8400-e29b-41d4-a716-446655440000",
  "name": "Clínica San José",
  "description": "Clínica privada de atención médica integral",
  "address": "Av. 18 de Julio 1234",
  "phone": "024123456",
  "email": "contacto@clinicasanjose.com.uy",
  "hcen_endpoint": "http://localhost:8080/hcen/api",
  "active": true,
  "createdAt": "2025-11-19T..."
}
```

## Test 2: Missing Required Field (400)

### Request
```bash
curl -X POST http://localhost:8080/clinic/api/clinics \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key-here" \
  -d '{
    "name": "Test Clinic"
  }'
```

### Expected Response
```
HTTP/1.1 400 Bad Request

{"error": "Clinic code is required"}
```

## Test 3: Missing API Key (401)

### Request
```bash
curl -X POST http://localhost:8080/clinic/api/clinics \
  -H "Content-Type: application/json" \
  -d '{
    "code": "clinic-001",
    "name": "Test Clinic",
    "hcen_endpoint": "http://test"
  }'
```

### Expected Response
```
HTTP/1.1 401 Unauthorized

{"error": "API key required. Please provide X-API-Key or Authorization: Bearer header."}
```

## Test 4: Duplicate Clinic (409)

### Request (send twice)
```bash
# First request - succeeds
curl -X POST http://localhost:8080/clinic/api/clinics \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key-here" \
  -d '{
    "code": "clinic-duplicate-test",
    "name": "Duplicate Test",
    "hcen_endpoint": "http://localhost:8080/hcen/api"
  }'

# Second request - fails
curl -X POST http://localhost:8080/clinic/api/clinics \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key-here" \
  -d '{
    "code": "clinic-duplicate-test",
    "name": "Duplicate Test 2",
    "hcen_endpoint": "http://localhost:8080/hcen/api"
  }'
```

### Expected Response (second request)
```
HTTP/1.1 409 Conflict

{"error": "Clinic with code 'clinic-duplicate-test' already exists"}
```

## Postman Collection

### Import this JSON into Postman:
```json
{
  "info": {
    "name": "Clinic Registration API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Register Clinic - Success",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "X-API-Key",
            "value": "{{api_key}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"code\": \"clinic-{{$guid}}\",\n  \"name\": \"Test Clinic\",\n  \"description\": \"Test clinic for development\",\n  \"address\": \"Test Address 123\",\n  \"phone\": \"099123456\",\n  \"email\": \"test@clinic.com\",\n  \"hcen_endpoint\": \"http://localhost:8080/hcen/api\",\n  \"active\": true\n}"
        },
        "url": {
          "raw": "http://localhost:8080/clinic/api/clinics",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["clinic", "api", "clinics"]
        }
      }
    },
    {
      "name": "Register Clinic - Missing Code (400)",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "X-API-Key",
            "value": "{{api_key}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"name\": \"Test Clinic\",\n  \"hcen_endpoint\": \"http://localhost:8080/hcen/api\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/clinic/api/clinics",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["clinic", "api", "clinics"]
        }
      }
    },
    {
      "name": "Register Clinic - No API Key (401)",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"code\": \"clinic-test\",\n  \"name\": \"Test Clinic\",\n  \"hcen_endpoint\": \"http://localhost:8080/hcen/api\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/clinic/api/clinics",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["clinic", "api", "clinics"]
        }
      }
    }
  ],
  "variable": [
    {
      "key": "api_key",
      "value": "your-api-key-here"
    }
  ]
}
```

## How to Get API Key

The API key is configured in the `ApiConfigurationService`. Check the following:

1. **Environment Variable:**
   ```bash
   export HCEN_REGISTRATION_API_KEY=your-secure-api-key
   ```

2. **WildFly Configuration:**
   Check `standalone.xml` for system properties

3. **Application Properties:**
   Check `application.properties` or `config.properties`

4. **Default Development Key:**
   Contact the team for the development API key

## Checking Logs

### View WildFly Server Logs
```bash
# Linux/Mac
tail -f wildfly/standalone/log/server.log

# Windows PowerShell
Get-Content wildfly/standalone/log/server.log -Wait -Tail 50
```

### Expected Log Output (Success)
```
INFO  [uy.gub.clinic.web.api.ClinicResource] Clinic registration request received - code: clinic-550e8400..., name: Clínica San José
INFO  [uy.gub.clinic.service.ClinicService] Clínica creada: Clinic{id=clinic-550e8400..., name='Clínica San José', code='clinic-550e8400...', active=true}
INFO  [uy.gub.clinic.web.api.ClinicResource] Clinic registered successfully - ID: clinic-550e8400..., code: clinic-550e8400..., name: Clínica San José
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Connection refused | Start WildFly: `gradlew wildFlyRun` |
| 404 Not Found | Verify deployment and application path `/api` |
| 405 Method Not Allowed | Old issue - should be fixed now |
| 401 Unauthorized | Check API key header and configuration |
| 500 Internal Server Error | Check server logs for stack trace |

## Integration with HCEN

When HCEN sends a registration request, it will:

1. Generate a unique clinic code (clinic-{uuid})
2. Collect clinic information (name, address, etc.)
3. Send POST request to `http://<clinic-host>:8080/clinic/api/clinics`
4. Include configured API key in X-API-Key header
5. Receive 201 response with created clinic data
6. Store clinic information in HCEN database

## Next Steps After Registration

Once a clinic is registered:

1. Clinic can authenticate with returned API key (if configured)
2. Clinic can register patients via INUS
3. Clinic can register documents via RNDC
4. Professionals can access clinic portal
5. Patients can access their documents

## Files to Reference

- **Implementation:** `C:\Users\agust\fing\tse\tse-2025\clinic\src\main\java\uy\gub\clinic\web\api\ClinicResource.java`
- **DTO:** `C:\Users\agust\fing\tse\tse-2025\clinic\src\main\java\uy\gub\clinic\web\api\dto\ClinicRegistrationRequest.java`
- **Documentation:** `C:\Users\agust\fing\tse\tse-2025\clinic\CLINIC_REGISTRATION_ENDPOINT.md`
