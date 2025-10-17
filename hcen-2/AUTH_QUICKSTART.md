# HCEN Authentication System - Quick Start Guide

## For the Next Developer: Complete the Implementation

This guide helps you quickly understand what's been implemented and what remains to complete the HCEN OpenID Connect authentication system.

---

## What's Already Implemented (70% Complete)

### Foundation Layer
- [X] Build configuration with all dependencies (JWT, Redis, Jackson, etc.)
- [X] Configuration classes for OIDC, JWT, and Redis
- [X] All DTO classes for request/response handling
- [X] Exception hierarchy for error handling
- [X] Utility classes (PKCE, State generation)
- [X] JwtTokenService (token generation & validation)
- [X] StateManager (Redis-based OAuth state management)
- [X] Database migration script for refresh_tokens table
- [X] Configuration templates (application.properties)
- [X] Comprehensive documentation (Infrastructure Requirements, Implementation Summary)

**Location**: `C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\auth\`

---

## What Needs to Be Implemented (30% Remaining)

### Critical Components (Implement in This Order)

#### 1. Web Login Pages (JSP)
**Files**:
- `src/main/webapp/login-patient.jsp` - Patient portal login page
- `src/main/webapp/login-admin.jsp` - Admin portal login page

**What it does**: Displays HCEN-branded login page with "Login with ID Uruguay" button

**Simple implementation**:
```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>HCEN - Portal del Paciente</title>
    <style>
        /* Add HCEN branding styles */
    </style>
</head>
<body>
    <div class="login-container">
        <img src="/hcen/assets/logo-hcen.png" alt="HCEN Logo">
        <h1>Portal del Paciente</h1>
        <p>Acceda a su historia clínica electrónica usando ID Uruguay</p>
        <button onclick="window.location.href='/hcen/api/auth/login/initiate?clientType=WEB_PATIENT'">
            Ingresar con ID Uruguay
        </button>
    </div>
</body>
</html>
```

**Why**:
- Better UX - users see HCEN branding before redirecting to gub.uy
- Builds trust
- Allows displaying terms of service, privacy policy
- Future flexibility for additional auth methods

**Reference**: See detailed JSP examples in AUTH_IMPLEMENTATION_SUMMARY.md

#### 2. GubUyOidcClient (Integration with gub.uy)
**File**: `src/main/java/uy/gub/hcen/auth/integration/gubuy/GubUyOidcClient.java`

**What it does**: Communicates with gub.uy OAuth/OIDC endpoints

**Key methods to implement**:
```java
@ApplicationScoped
public class GubUyOidcClient {
    @Inject OidcConfiguration oidcConfig;

    // TODO: Implement these methods
    String buildAuthorizationUrl(ClientType type, String redirectUri,
                                  String state, String codeChallenge);

    GubUyTokenResponse exchangeCode(String code, String redirectUri,
                                     String codeVerifier, ClientType type);

    Map<String, Object> validateIdToken(String idToken);

    Map<String, Object> getUserInfo(String accessToken);
}
```

**Libraries to use**:
- Apache HttpClient 5 (already in dependencies)
- Jackson ObjectMapper (for JSON parsing)
- JJWT (for ID token validation - already in dependencies)

**Reference**: See `docs/GUB_UY.md` for gub.uy API details

#### 3. RefreshToken Document & Repository
**Files**:
- `src/main/java/uy/gub/hcen/auth/document/RefreshToken.java`
- `src/main/java/uy/gub/hcen/auth/repository/RefreshTokenRepository.java`

**What it does**: Manages refresh tokens in MongoDB

**Key methods**:
```java
import org.bson.types.ObjectId;

public class RefreshToken {
    private ObjectId id;
    private String tokenHash;
    private String userCi;
    private ClientType clientType;
    private String deviceId;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private boolean isRevoked;
    // Getters, setters, constructors
}

@ApplicationScoped
public class RefreshTokenRepository {
    @Inject MongoDatabase mongoDb;

    // TODO: Implement CRUD operations
    void save(RefreshToken token);
    Optional<RefreshToken> findByTokenHash(String hash);
    List<RefreshToken> findByUserCi(String ci);
    void revokeAllForUser(String ci);
}
```

#### 4. AuthenticationService (Orchestration)
**File**: `src/main/java/uy/gub/hcen/auth/service/AuthenticationService.java`

**What it does**: Orchestrates the complete OAuth flow

**Dependencies**:
```java
@Stateless
public class AuthenticationService {
    @Inject GubUyOidcClient gubUyClient;
    @Inject JwtTokenService jwtService;
    @Inject StateManager stateManager;
    @Inject InusService inusService;  // You'll need this from INUS module
    @Inject RefreshTokenRepository refreshTokenRepo;

    // TODO: Implement these methods
    LoginInitiateResponse initiateLogin(LoginInitiateRequest req);
    TokenResponse handleCallback(CallbackRequest req);
    TokenResponse refreshToken(String refreshToken);
    void logout(String accessToken);
}
```

**Flow for handleCallback** (most complex):
1. Validate state with StateManager
2. Exchange code for tokens with GubUyOidcClient
3. Validate ID token
4. Extract CI from `uid` claim
5. Lookup/create user in INUS
6. Generate HCEN JWT tokens
7. Store refresh token
8. Return TokenResponse

#### 5. AuthenticationResource (REST API)
**File**: `src/main/java/uy/gub/hcen/auth/api/rest/AuthenticationResource.java`

**What it does**: Exposes REST endpoints

**Endpoints to implement**:
```java
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthenticationResource {
    @Inject AuthenticationService authService;

    @POST
    @Path("/login/initiate")
    Response initiateLogin(@Valid LoginInitiateRequest request);

    @POST
    @Path("/callback")
    Response handleCallback(@Valid CallbackRequest request);

    @POST
    @Path("/token/refresh")
    Response refreshToken(@Valid TokenRefreshRequest request);

    @GET
    @Path("/session")
    Response getSession(@Context SecurityContext securityContext);

    @POST
    @Path("/logout")
    Response logout(@Context SecurityContext securityContext);
}
```

**Error handling**:
- Use try-catch for AuthenticationException
- Return appropriate HTTP status codes (see CLAUDE.md)
- Return ErrorResponse DTO on errors

#### 6. JwtAuthenticationFilter (Security)
**File**: `src/main/java/uy/gub/hcen/auth/filter/JwtAuthenticationFilter.java`

**What it does**: Validates JWT on protected endpoints

```java
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenticationFilter implements ContainerRequestFilter {
    @Inject JwtTokenService jwtService;
    @Inject JedisPool jedisPool;  // For blacklist check

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // 1. Extract Authorization header
        // 2. Validate JWT
        // 3. Check if blacklisted (Redis)
        // 4. Set SecurityContext
    }
}
```

#### 7. Unit Tests
**Location**: `src/test/java/uy/gub/hcen/auth/`

**Priority tests**:
- `JwtTokenServiceTest` - Token generation/validation
- `StateManagerTest` - State creation/validation
- `AuthenticationServiceTest` - Full flow (with mocks)
- `PkceUtilTest` - PKCE generation/validation

**Target**: 80% code coverage (as per AC017, AC018)

---

## Quick Setup Instructions

### 1. Install Dependencies

**Windows Setup:**

```powershell
# MongoDB
# Option 1: Using Docker (recommended for development)
docker run -d -p 27017:27017 --name mongodb mongo:6.0

# Option 2: Install MongoDB Community Edition
# Download from: https://www.mongodb.com/try/download/community
# Install and run MongoDB as a Windows service

# Redis
# Option 1: Using Docker (recommended)
docker run -d -p 6379:6379 --name redis redis:7-alpine

# Option 2: Install Redis for Windows
# Download from: https://github.com/microsoftarchive/redis/releases
# Run redis-server.exe
```

**Create MongoDB Database and Collections:**

```javascript
// Connect to MongoDB
mongosh

// Switch to HCEN database
use hcen

// Create refresh_tokens collection
db.createCollection("refresh_tokens")

// Create indexes
db.refresh_tokens.createIndex({ "tokenHash": 1 }, { unique: true })
db.refresh_tokens.createIndex({ "userCi": 1 })
db.refresh_tokens.createIndex({ "expiresAt": 1 })
db.refresh_tokens.createIndex({ "isRevoked": 1 })

// Create TTL index for automatic cleanup (30 days)
db.refresh_tokens.createIndex(
  { "expiresAt": 1 },
  { expireAfterSeconds: 0 }
)

// Create authentication_sessions collection
db.createCollection("authentication_sessions")
db.authentication_sessions.createIndex({ "userCi": 1 })
db.authentication_sessions.createIndex({ "sessionId": 1 }, { unique: true })
```

### 2. Configure Application

```bash
# Copy template
cp src/main/resources/application.properties.template src/main/resources/application.properties

# Edit with your values
nano src/main/resources/application.properties

# Generate JWT signing key (if using RS256)
openssl genrsa -out jwt-private.pem 4096
```

### 3. Build and Deploy

```bash
# Build
./gradlew clean build

# Run tests
./gradlew test

# Deploy to WildFly
./gradlew deployToWildFly
```

### 4. Test Authentication Flow

```bash
# Health check
curl http://localhost:8080/hcen/api/health

# Initiate login (should return authorization URL)
curl -X POST http://localhost:8080/hcen/api/auth/login/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "clientType": "MOBILE",
    "redirectUri": "hcenuy://auth/callback",
    "codeChallenge": "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
    "codeChallengeMethod": "S256"
  }'
```

---

## File Structure Reference

```
hcen/src/main/java/uy/gub/hcen/auth/
├── api/
│   └── rest/
│       └── AuthenticationResource.java     [TODO]
├── config/
│   ├── OidcConfiguration.java              [DONE]
│   ├── JwtConfiguration.java               [DONE]
│   ├── RedisConfiguration.java             [DONE]
│   └── MongoConfiguration.java             [DONE]
├── dto/
│   ├── LoginInitiateRequest.java           [DONE]
│   ├── LoginInitiateResponse.java          [DONE]
│   ├── CallbackRequest.java                [DONE]
│   ├── TokenResponse.java                  [DONE]
│   ├── UserInfoDTO.java                    [DONE]
│   ├── TokenRefreshRequest.java            [DONE]
│   ├── SessionInfoResponse.java            [DONE]
│   └── ErrorResponse.java                  [DONE]
├── document/
│   └── RefreshToken.java                   [TODO]
├── exception/
│   ├── AuthenticationException.java        [DONE]
│   ├── InvalidTokenException.java          [DONE]
│   ├── InvalidStateException.java          [DONE]
│   ├── OAuthException.java                 [DONE]
│   └── TokenExpiredException.java          [DONE]
├── filter/
│   └── JwtAuthenticationFilter.java        [TODO]
├── integration/
│   └── gubuy/
│       └── GubUyOidcClient.java            [TODO]
├── repository/
│   └── RefreshTokenRepository.java         [TODO]
├── service/
│   ├── AuthenticationService.java          [TODO]
│   ├── JwtTokenService.java                [DONE]
│   └── StateManager.java                   [DONE]
└── util/
    ├── PkceUtil.java                       [DONE]
    └── StateUtil.java                      [DONE]
```

---

## Common Issues and Solutions

### Issue: "Failed to load OIDC configuration"
**Solution**: Ensure `application.properties` exists (not .template) and has all required properties

### Issue: "Redis connection refused"
**Solution**:
- Docker: `docker start redis`
- Windows: Ensure Redis service is running

### Issue: "MongoDB connection failed"
**Solution**:
- Docker: `docker start mongodb`
- Windows: Ensure MongoDB service is running
- Check connection string in application.properties

### Issue: "JWT signature verification failed"
**Solution**: Ensure signing key exists at the path specified in jwt.signing.key.path

---

## Testing with Mock gub.uy

For development without actual gub.uy access, create a mock:

```java
// Create: src/test/java/uy/gub/hcen/auth/mock/MockGubUyServer.java
public class MockGubUyServer {
    // Use WireMock to simulate gub.uy responses
    // Useful for integration tests
}
```

---

## Next Steps Checklist

- [ ] Create web login pages (login-patient.jsp, login-admin.jsp)
- [ ] Implement GubUyOidcClient
- [ ] Implement RefreshToken entity and repository
- [ ] Implement AuthenticationService
- [ ] Implement AuthenticationResource
- [ ] Implement JwtAuthenticationFilter
- [ ] Write unit tests (aim for 80% coverage)
- [ ] Write integration tests
- [ ] Manual testing with Postman/curl
- [ ] Security review
- [ ] Performance testing
- [ ] Update AUTH_IMPLEMENTATION_SUMMARY.md

---

## Documentation References

- **GUB_UY.md** - Complete gub.uy API documentation
- **AUTH_IMPLEMENTATION_SUMMARY.md** - Detailed implementation guide
- **INFRASTRUCTURE_REQUIREMENTS.md** - Infrastructure setup guide
- **CLAUDE.md** - Overall HCEN architecture and conventions

---

## Contact for Questions

**Team**: TSE 2025 Group 9
- German Rodao (4.796.608-7)
- Agustin Silvano (5.096.964-8)
- Piero Santos (6.614.312-9)

**Estimated Time to Complete**: 2-3 days for experienced Jakarta EE developer

Good luck! The foundation is solid - you just need to connect the pieces.
