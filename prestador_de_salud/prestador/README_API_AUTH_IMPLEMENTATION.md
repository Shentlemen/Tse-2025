# API Key Authentication Implementation

## Overview

This document describes the complete implementation of API key authentication for the Health Provider component's Clinical Document endpoints.

## Implementation Summary

API key authentication has been successfully implemented to protect all endpoints under `/api/documents/*`. The implementation follows Jakarta EE best practices and uses a clean architecture approach with:

- **Servlet Filter** for transparent authentication
- **Singleton EJB** for configuration management
- **Externalized configuration** with hot reload support
- **Constant-time comparison** for security
- **Comprehensive logging** for audit trails

## Architecture

### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      HTTP Request                           │
│              GET /api/documents/{id}                        │
│              Header: X-API-Key: <api-key>                   │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│        ApiKeyAuthenticationFilter (@WebFilter)              │
│  - Intercepts all /api/documents/* requests                 │
│  - Extracts X-API-Key header                                │
│  - Delegates validation to ApiConfigurationService          │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│      ApiConfigurationService (@Singleton @Startup)          │
│  - Loads api-config.properties                              │
│  - Validates API key (constant-time comparison)             │
│  - Supports hot reload without restart                      │
└────────────────────┬────────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
        ▼                         ▼
   Valid Key                 Invalid Key
        │                         │
        ▼                         ▼
┌──────────────┐          ┌──────────────┐
│ Continue to  │          │  Return 401  │
│   Servlet    │          │ Unauthorized │
└──────────────┘          └──────────────┘
```

## Files Created

### 1. Configuration File

**File**: `C:\Users\agust\fing\tse\tse-2025\prestador_de_salud\prestador\src\main\resources\api-config.properties`

**Purpose**: Stores API key and authentication settings

**Content**:
```properties
# API Key for authenticating requests from HCEN Central
api.key=change-me-in-production-secure-random-key-12345

# Enable/disable API key authentication (default: true)
api.auth.enabled=true
```

**Key Features**:
- Can be externalized to `${jboss.server.config.dir}/api-config.properties` for production
- Supports hot reload (changes take effect without restart)
- Falls back to classpath resource if external file not found

---

### 2. Configuration Service

**File**: `C:\Users\agust\fing\tse\tse-2025\prestador_de_salud\prestador\src\main\java\com\prestador\config\ApiConfigurationService.java`

**Class**: `com.prestador.config.ApiConfigurationService`

**Bean Type**: `@Singleton @Startup`

**Responsibilities**:
- Load API configuration from external file or classpath
- Provide API key validation
- Support runtime configuration reload
- Use constant-time comparison to prevent timing attacks

**Key Methods**:
```java
public String getApiKey()                           // Get configured API key
public boolean isAuthenticationEnabled()            // Check if auth is enabled
public boolean validateApiKey(String providedKey)   // Validate API key
public void reloadIfModified()                      // Reload config if file changed
```

**Security Features**:
- **Constant-time comparison**: Prevents timing attacks by ensuring comparison always takes the same time
- **Automatic reload**: Detects file modifications and reloads configuration
- **Configuration priority**: External file > Classpath resource

**Code Highlights**:
```java
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ApiConfigurationService {

    @PostConstruct
    public void init() {
        loadConfiguration();
    }

    public boolean validateApiKey(String providedKey) {
        if (!isAuthenticationEnabled()) {
            return true;  // Authentication disabled (dev mode)
        }

        String configuredKey = getApiKey();
        return constantTimeEquals(providedKey, configuredKey);
    }

    private boolean constantTimeEquals(String a, String b) {
        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();

        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }

        return result == 0;
    }
}
```

---

### 3. Authentication Filter

**File**: `C:\Users\agust\fing\tse\tse-2025\prestador_de_salud\prestador\src\main\java\com\prestador\filter\ApiKeyAuthenticationFilter.java`

**Class**: `com.prestador.filter.ApiKeyAuthenticationFilter`

**Filter Type**: `@WebFilter`

**URL Patterns**: `/api/documents`, `/api/documents/*`

**Responsibilities**:
- Intercept all requests to clinical document endpoints
- Extract X-API-Key header
- Validate API key using ApiConfigurationService
- Return 401 Unauthorized if validation fails
- Log all authentication attempts

**Authentication Flow**:
```java
@Override
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // Extract API key from header
    String providedApiKey = httpRequest.getHeader("X-API-Key");

    // Validate API key
    if (configService.validateApiKey(providedApiKey)) {
        // Success - continue to servlet
        chain.doFilter(request, response);
    } else {
        // Failure - return 401
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", "UNAUTHORIZED");
        errorResponse.put("message", "Invalid or missing API key. Please provide a valid X-API-Key header.");
        errorResponse.put("timestamp", java.time.Instant.now().toString());

        httpResponse.getWriter().write(errorResponse.toString());
    }
}
```

**Error Response Format**:
```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or missing API key. Please provide a valid X-API-Key header.",
  "timestamp": "2025-11-18T10:30:00.000Z"
}
```

---

### 4. Documentation Files

#### 4.1 API Authentication Documentation

**File**: `C:\Users\agust\fing\tse\tse-2025\prestador_de_salud\prestador\API_AUTHENTICATION.md`

**Content**:
- Complete API authentication guide
- Usage examples (cURL, Java, JavaScript, Python)
- Configuration instructions
- Security considerations
- Troubleshooting guide

#### 4.2 Infrastructure Requirements

**File**: `C:\Users\agust\fing\tse\tse-2025\prestador_de_salud\INFRASTRUCTURE_REQUIREMENTS.md`

**Content**:
- WildFly configuration requirements
- Database setup instructions
- API security configuration
- JMS configuration for HCEN integration
- Network and SSL/TLS requirements
- File storage configuration
- Deployment checklist

---

### 5. Test Scripts

#### 5.1 Linux/macOS Test Script

**File**: `C:\Users\agust\fing\tse\tse-2025\prestador_de_salud\prestador\test-api-auth.sh`

**Usage**:
```bash
chmod +x test-api-auth.sh
./test-api-auth.sh
```

**Tests**:
1. Request without API key → 401
2. Request with invalid API key → 401
3. Request with valid API key → 200
4. Full error response with invalid key
5. Full response with valid key
6. POST request without API key → 401
7. GET specific document without API key → 401

#### 5.2 Windows Test Script

**File**: `C:\Users\agust\fing\tse\tse-2025\prestador_de_salud\prestador\test-api-auth.bat`

**Usage**:
```cmd
test-api-auth.bat
```

Same test cases as Linux script, adapted for Windows command line.

---

## Integration Points

### 1. ClinicalDocumentServlet (Unchanged)

**File**: `com.prestador.servlet.ClinicalDocumentServlet`

**Impact**: No changes required

The servlet continues to work as before. The authentication filter transparently handles API key validation before requests reach the servlet.

### 2. HCEN Central Integration

HCEN Central must include the API key when calling the Health Provider's endpoints:

**Example Request from HCEN**:
```http
GET /prestador/api/documents/123 HTTP/1.1
Host: provider.example.uy
X-API-Key: shared-secret-api-key-between-hcen-and-provider
```

**Configuration**:
- Same API key must be configured on both sides
- HCEN stores provider API keys in its provider registry
- Health Provider validates incoming requests against configured key

---

## Configuration Management

### Development Environment

**Location**: Classpath resource (packaged in WAR)

```
prestador/src/main/resources/api-config.properties
```

**Configuration**:
```properties
api.key=dev-api-key-not-for-production
api.auth.enabled=true
```

### Production Environment

**Location**: External configuration directory

**Linux/macOS**:
```
/opt/wildfly/standalone/configuration/api-config.properties
```

**Windows**:
```
C:\wildfly\standalone\configuration\api-config.properties
```

**Configuration**:
```properties
# Generate secure key: openssl rand -hex 32
api.key=a7f8d3e2c9b1a4e6f8d3c2a1b9e7f6d5c4a3b2e1f9d8c7b6a5e4d3c2b1a9f8e7
api.auth.enabled=true
```

### Hot Reload Process

1. Edit external configuration file:
   ```bash
   vim /opt/wildfly/standalone/configuration/api-config.properties
   ```

2. Save changes

3. Next request automatically reloads configuration

4. No WildFly restart required

---

## Security Features

### 1. Constant-Time Comparison

**Purpose**: Prevent timing attacks

**Implementation**:
```java
private boolean constantTimeEquals(String a, String b) {
    byte[] aBytes = a.getBytes();
    byte[] bBytes = b.getBytes();

    if (aBytes.length != bBytes.length) {
        return false;
    }

    int result = 0;
    for (int i = 0; i < aBytes.length; i++) {
        result |= aBytes[i] ^ bBytes[i];  // XOR accumulation
    }

    return result == 0;
}
```

**Benefit**: Attackers cannot determine correct API key by measuring response times

### 2. Comprehensive Logging

**Successful Authentication**:
```
INFO [com.prestador.filter.ApiKeyAuthenticationFilter]
  API authentication successful - URI: /prestador/api/documents, Remote IP: 192.168.1.100
```

**Failed Authentication**:
```
WARNING [com.prestador.filter.ApiKeyAuthenticationFilter]
  API authentication failed - Method: GET, URI: /prestador/api/documents, Remote IP: 192.168.1.100
```

**Configuration Events**:
```
INFO [com.prestador.config.ApiConfigurationService]
  API configuration loaded from external file: /opt/wildfly/standalone/configuration/api-config.properties

INFO [com.prestador.config.ApiConfigurationService]
  Configuration file modified, reloading...
```

### 3. Secure API Key Generation

**Recommended Methods**:

**OpenSSL (32 bytes hex)**:
```bash
openssl rand -hex 32
```

**PowerShell (32 bytes base64)**:
```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

**Java (programmatic)**:
```java
SecureRandom random = new SecureRandom();
byte[] bytes = new byte[32];
random.nextBytes(bytes);
String apiKey = Base64.getEncoder().encodeToString(bytes);
```

---

## Testing

### Manual Testing with cURL

**Test 1: Valid API Key**
```bash
curl -X GET "http://localhost:8080/prestador/api/documents" \
  -H "X-API-Key: change-me-in-production-secure-random-key-12345"
```

**Expected**: HTTP 200, document list

**Test 2: Invalid API Key**
```bash
curl -X GET "http://localhost:8080/prestador/api/documents" \
  -H "X-API-Key: wrong-key"
```

**Expected**: HTTP 401, error JSON

**Test 3: Missing API Key**
```bash
curl -X GET "http://localhost:8080/prestador/api/documents"
```

**Expected**: HTTP 401, error JSON

### Automated Testing

Run the test suite:

**Linux/macOS**:
```bash
./test-api-auth.sh
```

**Windows**:
```cmd
test-api-auth.bat
```

**Expected Output**:
```
==========================================
API Key Authentication Test Suite
==========================================

Test 1: Request without API key
Expected: 401 Unauthorized
Result: HTTP 401
✓ PASSED

Test 2: Request with invalid API key
Expected: 401 Unauthorized
Result: HTTP 401
✓ PASSED

Test 3: Request with valid API key
Expected: 200 OK
Result: HTTP 200
✓ PASSED

[...]
```

---

## Deployment Instructions

### Step 1: Build Application

```bash
cd prestador
mvn clean package
```

### Step 2: Create External Configuration

**Linux/macOS**:
```bash
# Generate secure API key
API_KEY=$(openssl rand -hex 32)

# Create configuration file
cat > /opt/wildfly/standalone/configuration/api-config.properties <<EOF
api.key=$API_KEY
api.auth.enabled=true
EOF

# Set permissions
chown wildfly:wildfly /opt/wildfly/standalone/configuration/api-config.properties
chmod 600 /opt/wildfly/standalone/configuration/api-config.properties

# Save API key for HCEN configuration
echo "Provider API Key: $API_KEY"
```

**Windows**:
```cmd
REM Create configuration file manually or with PowerShell
notepad C:\wildfly\standalone\configuration\api-config.properties
```

### Step 3: Deploy WAR

```bash
cp target/prestador.war /opt/wildfly/standalone/deployments/
```

### Step 4: Verify Deployment

```bash
# Check logs
tail -f /opt/wildfly/standalone/log/server.log

# Look for:
# - "API Key Authentication Filter initialized"
# - "API configuration loaded from external file"
```

### Step 5: Test Authentication

```bash
# Test with valid key
curl -H "X-API-Key: <your-api-key>" http://localhost:8080/prestador/api/documents

# Test with invalid key (should return 401)
curl -H "X-API-Key: invalid" http://localhost:8080/prestador/api/documents
```

---

## Troubleshooting

### Issue: All requests return 401

**Check 1**: Verify configuration file exists
```bash
ls -la /opt/wildfly/standalone/configuration/api-config.properties
```

**Check 2**: Verify configuration content
```bash
cat /opt/wildfly/standalone/configuration/api-config.properties
```

**Check 3**: Check server logs
```bash
grep "ApiConfigurationService" /opt/wildfly/standalone/log/server.log
```

### Issue: Configuration changes not taking effect

**Solution**: Configuration reload is automatic, but verify file modification time:

```bash
# Linux/macOS
stat /opt/wildfly/standalone/configuration/api-config.properties

# Check logs for reload message
grep "Configuration file modified" /opt/wildfly/standalone/log/server.log
```

### Issue: Authentication disabled in production

**Check**: Verify `api.auth.enabled=true` in configuration

```bash
grep "api.auth.enabled" /opt/wildfly/standalone/configuration/api-config.properties
```

---

## Jakarta EE Best Practices Applied

### 1. Separation of Concerns
- **Filter**: Request interception and authentication
- **Service (EJB)**: Configuration management and validation logic
- **Configuration**: Externalized, environment-specific settings

### 2. Dependency Injection (CDI)
```java
@EJB
private ApiConfigurationService configService;
```

### 3. Bean Lifecycle Management
```java
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ApiConfigurationService {
    @PostConstruct
    public void init() {
        loadConfiguration();
    }
}
```

### 4. Servlet Filters for Cross-Cutting Concerns
```java
@WebFilter(filterName = "ApiKeyAuthenticationFilter",
           urlPatterns = {"/api/documents", "/api/documents/*"})
```

### 5. Proper Error Handling
- Standard HTTP status codes (401 Unauthorized)
- Structured JSON error responses
- Comprehensive logging

---

## Integration with Existing Code

### No Changes Required to Existing Files

The implementation is **completely transparent** to existing code:

- **ClinicalDocumentServlet**: No modifications needed
- **ClinicalDocumentService**: No modifications needed
- **Other servlets/endpoints**: Not affected

The servlet filter intercepts requests **before** they reach the servlet, providing authentication as a **cross-cutting concern**.

---

## Summary

### What Was Implemented

✅ API key authentication for `/api/documents/*` endpoints
✅ Externalized configuration with hot reload support
✅ Singleton EJB for configuration management
✅ Servlet filter for transparent authentication
✅ Constant-time comparison for security
✅ Comprehensive logging and audit trails
✅ Complete documentation and test scripts
✅ Infrastructure requirements documentation

### Key Benefits

- **Security**: API key protection prevents unauthorized access
- **Flexibility**: Externalized configuration allows per-environment settings
- **Zero Downtime**: Hot reload support eliminates restart requirements
- **Transparency**: No changes to existing servlet code
- **Maintainability**: Clean separation of concerns, Jakarta EE best practices
- **Observability**: Comprehensive logging for security audits

---

## Next Steps

1. **Deploy to Development**: Test with development API key
2. **Generate Production Key**: Use secure random generation
3. **Configure HCEN Central**: Provide API key to HCEN for provider registration
4. **Monitor Logs**: Verify authentication attempts are logged correctly
5. **Security Audit**: Review configuration and ensure best practices
6. **Documentation**: Share API key with authorized HCEN administrators

---

**Implementation completed**: 2025-11-18
**TSE 2025 Group 9**
- German Rodao (4.796.608-7)
- Agustin Silvano (5.096.964-8)
- Piero Santos (6.614.312-9)
