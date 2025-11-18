# API Key Authentication

This document describes the API key authentication mechanism for the Health Provider component.

## Overview

All endpoints under `/api/documents/*` are protected by API key authentication. Clients must provide a valid API key in the request header to access these endpoints.

## Architecture

### Components

1. **ApiConfigurationService** (`com.prestador.config.ApiConfigurationService`)
   - Singleton EJB that loads and manages API configuration
   - Supports hot reload of configuration without application restart
   - Provides API key validation with constant-time comparison (prevents timing attacks)

2. **ApiKeyAuthenticationFilter** (`com.prestador.filter.ApiKeyAuthenticationFilter`)
   - Servlet filter that intercepts requests to `/api/documents/*`
   - Extracts X-API-Key header from requests
   - Validates API key using ApiConfigurationService
   - Returns 401 Unauthorized if authentication fails

3. **Configuration File** (`api-config.properties`)
   - Stores API key and authentication settings
   - Can be externalized for production deployments
   - Supports runtime reload without restart

### Authentication Flow

```
1. Client Request
   ↓
2. ApiKeyAuthenticationFilter intercepts request
   ↓
3. Extract X-API-Key header
   ↓
4. ApiConfigurationService validates key
   ↓
5a. Valid → Continue to servlet (200/201)
5b. Invalid → Return 401 Unauthorized
```

## Configuration

### External Configuration (Production)

Place `api-config.properties` in WildFly configuration directory:

**Linux/macOS:**
```bash
/opt/wildfly/standalone/configuration/api-config.properties
```

**Windows:**
```
C:\wildfly\standalone\configuration\api-config.properties
```

### Classpath Configuration (Development)

Place `api-config.properties` in:
```
src/main/resources/api-config.properties
```

### Configuration Properties

```properties
# API Key (REQUIRED)
api.key=your-secure-api-key-here

# Enable/Disable Authentication (OPTIONAL, default: true)
api.auth.enabled=true
```

## Usage

### Making Authenticated Requests

Include the `X-API-Key` header in all requests to `/api/documents/*`:

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/prestador/api/documents" \
  -H "X-API-Key: your-secure-api-key-here"
```

**Java Example:**
```java
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setRequestProperty("X-API-Key", "your-secure-api-key-here");
```

**JavaScript/Fetch Example:**
```javascript
fetch('http://localhost:8080/prestador/api/documents', {
  headers: {
    'X-API-Key': 'your-secure-api-key-here'
  }
})
```

**Python/Requests Example:**
```python
import requests

headers = {'X-API-Key': 'your-secure-api-key-here'}
response = requests.get('http://localhost:8080/prestador/api/documents', headers=headers)
```

## Response Codes

| Status Code | Meaning | Description |
|-------------|---------|-------------|
| 200 OK | Success | Request authenticated and processed successfully |
| 201 Created | Success | Document created successfully |
| 401 Unauthorized | Authentication Failed | Invalid or missing API key |
| 404 Not Found | Not Found | Document not found (after successful authentication) |
| 500 Internal Server Error | Server Error | Server error (after successful authentication) |

## Error Responses

### 401 Unauthorized

**Response Body:**
```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or missing API key. Please provide a valid X-API-Key header.",
  "timestamp": "2025-11-18T10:30:00.000Z"
}
```

**Common Causes:**
- X-API-Key header not provided
- Incorrect API key value
- API key contains leading/trailing whitespace

## Security Considerations

### Constant-Time Comparison

The API key validation uses constant-time comparison to prevent timing attacks. This ensures that attackers cannot determine the correct API key by measuring response times.

### Hot Reload Support

The configuration service automatically reloads the configuration file if it has been modified. This allows updating the API key without restarting WildFly.

**Steps to update API key:**
1. Edit `${jboss.server.config.dir}/api-config.properties`
2. Save the file
3. Configuration is automatically reloaded on the next request
4. No application restart required

### Disabling Authentication (Development Only)

For local development and testing, you can disable API authentication:

```properties
api.auth.enabled=false
```

**WARNING:** Never disable authentication in production environments.

## Generating Secure API Keys

### Using OpenSSL (Linux/macOS)

```bash
openssl rand -hex 32
```

Output example:
```
a7f8d3e2c9b1a4e6f8d3c2a1b9e7f6d5c4a3b2e1f9d8c7b6a5e4d3c2b1a9f8e7
```

### Using PowerShell (Windows)

```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

Output example:
```
x8fD3e+9b1A4e6/8d3C2a1B9e7F6d5C4a3B2e1F9d8C7b6A5e4D3c2B1a9F8e7==
```

### Using Java

```java
import java.security.SecureRandom;
import java.util.Base64;

SecureRandom random = new SecureRandom();
byte[] bytes = new byte[32];
random.nextBytes(bytes);
String apiKey = Base64.getEncoder().encodeToString(bytes);
System.out.println(apiKey);
```

## Testing

### Test with Valid API Key

```bash
curl -v -X GET "http://localhost:8080/prestador/api/documents" \
  -H "X-API-Key: change-me-in-production-secure-random-key-12345"
```

**Expected Response:**
```
< HTTP/1.1 200 OK
< Content-Type: application/fhir+json
[...]
```

### Test with Invalid API Key

```bash
curl -v -X GET "http://localhost:8080/prestador/api/documents" \
  -H "X-API-Key: invalid-key"
```

**Expected Response:**
```
< HTTP/1.1 401 Unauthorized
< Content-Type: application/json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or missing API key. Please provide a valid X-API-Key header.",
  "timestamp": "2025-11-18T10:30:00.000Z"
}
```

### Test without API Key

```bash
curl -v -X GET "http://localhost:8080/prestador/api/documents"
```

**Expected Response:**
```
< HTTP/1.1 401 Unauthorized
< Content-Type: application/json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or missing API key. Please provide a valid X-API-Key header.",
  "timestamp": "2025-11-18T10:30:00.000Z"
}
```

## Logging

The authentication filter logs all authentication attempts:

**Successful Authentication:**
```
INFO [com.prestador.filter.ApiKeyAuthenticationFilter] API authentication successful - URI: /prestador/api/documents, Remote IP: 127.0.0.1
```

**Failed Authentication:**
```
WARNING [com.prestador.filter.ApiKeyAuthenticationFilter] API authentication failed - Method: GET, URI: /prestador/api/documents, Remote IP: 127.0.0.1
```

**Configuration Reload:**
```
INFO [com.prestador.config.ApiConfigurationService] Configuration file modified, reloading...
INFO [com.prestador.config.ApiConfigurationService] API configuration loaded from external file: C:\wildfly\standalone\configuration\api-config.properties
```

## Integration with HCEN Central

HCEN Central must provide the configured API key when calling the Health Provider's document retrieval endpoint:

**HCEN Central → Health Provider Request:**
```http
GET /prestador/api/documents/123 HTTP/1.1
Host: provider.example.uy
X-API-Key: shared-secret-api-key-between-hcen-and-provider
```

**Configuration:**
- The same API key must be configured in both systems
- HCEN Central stores the API key for each registered provider
- Health Provider validates the key on each incoming request

## Troubleshooting

### Issue: "Invalid or missing API key" when key is correct

**Possible Causes:**
1. Leading/trailing whitespace in configuration or header
2. Configuration file not loaded correctly
3. External configuration file not found

**Solutions:**
1. Trim whitespace from API key in configuration file
2. Check server logs for configuration loading messages
3. Verify external configuration file path: `${jboss.server.config.dir}/api-config.properties`

### Issue: Configuration changes not taking effect

**Possible Causes:**
1. External configuration file not being monitored
2. File permissions prevent reading

**Solutions:**
1. Verify file exists in `${jboss.server.config.dir}/`
2. Check file permissions (WildFly process must have read access)
3. Check server logs for configuration reload messages

### Issue: All requests returning 401

**Possible Causes:**
1. API key not configured
2. Configuration file not found
3. Authentication enabled but no key provided

**Solutions:**
1. Create `api-config.properties` with valid API key
2. Verify configuration file location
3. Check server logs for configuration errors

---

## Files Modified/Created

### New Files

1. **C:\Users\agust\fing\tse\tse-2025\prestador_de_salud\prestador\src\main\resources\api-config.properties**
   - Configuration file for API key and authentication settings

2. **C:\Users\agust\fing\tse\tse-2025\prestador_de_salud\prestador\src\main\java\com\prestador\config\ApiConfigurationService.java**
   - Singleton EJB for managing API configuration
   - Supports hot reload of configuration
   - Provides secure API key validation

3. **C:\Users\agust\fing\tse\tse-2025\prestador_de_salud\prestador\src\main\java\com\prestador\filter\ApiKeyAuthenticationFilter.java**
   - Servlet filter for API key authentication
   - Intercepts all requests to `/api/documents/*`
   - Returns 401 for invalid/missing API keys

### Existing Files (No Changes Required)

- **ClinicalDocumentServlet.java** - No changes required, filter handles authentication transparently

---

**Author**: TSE 2025 Group 9
**Last Updated**: 2025-11-18
