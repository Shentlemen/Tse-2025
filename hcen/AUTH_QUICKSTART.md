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

#### 1. GubUyOidcClient (Integration with gub.uy)
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

#### 2. RefreshToken Entity & Repository
**Files**:
- `src/main/java/uy/gub/hcen/auth/entity/RefreshToken.java`
- `src/main/java/uy/gub/hcen/auth/repository/RefreshTokenRepository.java`

**What it does**: Manages refresh tokens in PostgreSQL

**Key methods**:
```java
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    // See AUTH_IMPLEMENTATION_SUMMARY.md for full schema
}

@ApplicationScoped
public class RefreshTokenRepository {
    @PersistenceContext EntityManager em;

    // TODO: Implement CRUD operations
    void save(RefreshToken token);
    Optional<RefreshToken> findByTokenHash(String hash);
    List<RefreshToken> findByUserCi(String ci);
    void revokeAllForUser(String ci);
}
```

#### 3. AuthenticationService (Orchestration)
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

#### 4. AuthenticationResource (REST API)
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

#### 5. JwtAuthenticationFilter (Security)
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

#### 6. Unit Tests
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

```bash
# PostgreSQL
sudo apt install postgresql-14
createdb hcen
psql -d hcen -f src/main/resources/db/migration/V001__create_refresh_tokens_table.sql

# Redis
sudo apt install redis-server
sudo systemctl start redis

# MongoDB (for audit logs)
sudo apt install mongodb
sudo systemctl start mongodb
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
│   └── RedisConfiguration.java             [DONE]
├── dto/
│   ├── LoginInitiateRequest.java           [DONE]
│   ├── LoginInitiateResponse.java          [DONE]
│   ├── CallbackRequest.java                [DONE]
│   ├── TokenResponse.java                  [DONE]
│   ├── UserInfoDTO.java                    [DONE]
│   ├── TokenRefreshRequest.java            [DONE]
│   ├── SessionInfoResponse.java            [DONE]
│   └── ErrorResponse.java                  [DONE]
├── entity/
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
**Solution**: Start Redis: `sudo systemctl start redis`

### Issue: "Database connection failed"
**Solution**: Run migration script and verify PostgreSQL is running

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
