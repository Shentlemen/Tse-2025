# gub.uy (ID Uruguay) - OpenID Connect Integration Guide

This document provides detailed information about integrating with Uruguay's national authentication system (ID Uruguay) using OpenID Connect.

---

## OpenID Configuration Analysis (.well-known endpoint)

**Source**: `https://auth-testing.iduruguay.gub.uy/oidc/v1/.well-known/openid-configuration`
**Analysis Date**: 2025-10-14
**Environment**: Testing/Staging

### Configuration Summary

ID Uruguay implements a **standard-compliant OpenID Connect 1.0** provider with Uruguay-specific extensions for identity assurance levels. The configuration follows OAuth 2.0 and OpenID Connect Core specifications with some notable characteristics:

- **Authorization Flow**: Authorization Code Flow only (most secure option)
- **Token Signing**: Supports both HMAC (HS256) and RSA (RS256)
- **Identity Assurance**: Four levels of authentication assurance (NIDs)
- **Subject Type**: Public identifiers only
- **Claims Support**: Rich set of personal data claims specific to Uruguayan identity documents

---

### Complete Configuration

```json
{
  "issuer": "https://auth-testing.iduruguay.gub.uy",
  "authorization_endpoint": "https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize",
  "token_endpoint": "https://auth-testing.iduruguay.gub.uy/oidc/v1/token",
  "userinfo_endpoint": "https://auth-testing.iduruguay.gub.uy/oidc/v1/userinfo",
  "end_session_endpoint": "https://auth-testing.iduruguay.gub.uy/oidc/v1/logout",
  "jwks_uri": "https://auth-testing.iduruguay.gub.uy/oidc/v1/jwks",
  "service_documentation": "https://centroderecursos.agesic.gub.uy/web/seguridad/wiki/-/wiki/Main/ID+Uruguay+-+Integraci%C3%B3n+con+OpenID+Connect",

  "response_types_supported": ["code"],
  "subject_types_supported": ["public"],
  "id_token_signing_alg_values_supported": ["HS256", "RS256"],
  "claims_parameter_supported": true,

  "scopes_supported": [
    "openid",
    "personal_info",
    "email",
    "document",
    "profile"
  ],

  "acr_values_supported": [
    "urn:iduruguay:nid:0",
    "urn:iduruguay:nid:1",
    "urn:iduruguay:nid:2",
    "urn:iduruguay:nid:3"
  ],

  "claims_supported": [
    "nombre_completo",
    "primer_nombre",
    "segundo_nombre",
    "primer_apellido",
    "segundo_apellido",
    "uid",
    "name",
    "given_name",
    "family_name"
  ]
}
```

---

### Endpoints Discovered

| Endpoint Type | URL | Purpose |
|--------------|-----|---------|
| **Issuer** | `https://auth-testing.iduruguay.gub.uy` | Token issuer identifier |
| **Authorization** | `https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize` | Initial user authentication and authorization |
| **Token** | `https://auth-testing.iduruguay.gub.uy/oidc/v1/token` | Exchange authorization code for tokens |
| **UserInfo** | `https://auth-testing.iduruguay.gub.uy/oidc/v1/userinfo` | Retrieve user profile information |
| **Logout (End Session)** | `https://auth-testing.iduruguay.gub.uy/oidc/v1/logout` | Single sign-out endpoint |
| **JWKS (Keys)** | `https://auth-testing.iduruguay.gub.uy/oidc/v1/jwks` | Public keys for token signature verification |
| **Documentation** | `https://centroderecursos.agesic.gub.uy/web/seguridad/wiki/-/wiki/Main/ID+Uruguay+-+Integraci%C3%B3n+con+OpenID+Connect` | Official integration documentation |

**Note**: All endpoints use HTTPS. The base path `/oidc/v1/` suggests versioned API design.

---

### Supported Features

#### Grant Types / Response Types
- **Supported**: `code` (Authorization Code Flow)
- **Not Supported**:
  - ❌ Implicit Flow (`token`, `id_token`)
  - ❌ Hybrid Flow (`code token`, `code id_token`, etc.)
  - ❌ Client Credentials
  - ❌ Resource Owner Password Credentials

**Analysis**: Only the Authorization Code Flow is supported, which is the **most secure** OAuth 2.0 flow for web applications. This is a deliberate security decision that aligns with current best practices (OAuth 2.0 Security Best Current Practice - BCP).

#### Scopes

| Scope | Description | Claims Included |
|-------|-------------|-----------------|
| `openid` | **Required**. Indicates OpenID Connect request | `sub` (subject identifier) |
| `personal_info` | Access to full personal information | `nombre_completo`, `primer_nombre`, `segundo_nombre`, `primer_apellido`, `segundo_apellido` |
| `email` | User's email address | `email` (not listed in claims_supported, may be dynamic) |
| `document` | Identity document information | `uid` (likely CI - Cédula de Identidad) |
| `profile` | Standard OpenID Connect profile scope | `name`, `given_name`, `family_name` |

**Recommendation**: For HCEN integration, request: `openid personal_info document profile`

#### ID Token Signing Algorithms
- **HS256** (HMAC with SHA-256): Symmetric signing using client secret
- **RS256** (RSA with SHA-256): Asymmetric signing using public/private keys

**Recommendation**: Use **RS256** for production. It's more secure as the client doesn't need the signing key, only the public key for verification (available via JWKS endpoint).

#### Subject Types
- **public**: Subject identifiers are the same across all clients for a given user

**Note**: The lack of `pairwise` subject type means the same `sub` claim will be returned for a user across different applications. This may have privacy implications but simplifies user correlation across government services.

---

### Uruguay-Specific Extensions

#### Authentication Context Class Reference (ACR) Values

ID Uruguay defines four **Niveles de Identificación Digital (NID)** - Digital Identity Levels:

| ACR Value | Level | Description | Use Case |
|-----------|-------|-------------|----------|
| `urn:iduruguay:nid:0` | NID 0 | **Self-asserted identity** (lowest assurance) | Non-critical services, public information |
| `urn:iduruguay:nid:1` | NID 1 | **Email or phone validation** | Low-value transactions, newsletters |
| `urn:iduruguay:nid:2` | NID 2 | **Remote identity proofing** (video, document scan) | Moderate-value services, most government portals |
| `urn:iduruguay:nid:3` | NID 3 | **In-person identity verification** (highest assurance) | High-value transactions, health records, financial services |

**HCEN Recommendation**: Request **NID 2 or NID 3** for patient authentication due to sensitive health data. Specify via `acr_values` parameter in authorization request:

```
acr_values=urn:iduruguay:nid:2 urn:iduruguay:nid:3
```

The resulting ID token will include an `acr` claim indicating the level achieved.

#### Claims (Personal Data)

ID Uruguay provides both **Uruguay-specific** and **standard OpenID Connect** claims:

**Uruguay-Specific Claims**:
- `nombre_completo`: Full name as registered in national identity system
- `primer_nombre`: First given name
- `segundo_nombre`: Second given name (middle name)
- `primer_apellido`: First surname (paternal)
- `segundo_apellido`: Second surname (maternal)
- `uid`: Unique identifier (likely the CI - Cédula de Identidad)

**Standard OpenID Connect Claims** (mapped from above):
- `name`: Maps to `nombre_completo`
- `given_name`: Maps to `primer_nombre` (+ `segundo_nombre` concatenated)
- `family_name`: Maps to `primer_apellido` (+ `segundo_apellido` concatenated)

**Recommendation**: Use the Uruguay-specific claims (`primer_nombre`, `primer_apellido`, etc.) for accurate data storage in INUS, as they preserve the semantic structure of Uruguayan names.

---

### Comparison with Standard OpenID Connect

#### ✅ Standard-Compliant Features

1. **Core Endpoints**: All required OpenID Connect Discovery endpoints present
2. **Authorization Code Flow**: Properly implements the most secure OAuth 2.0 flow
3. **JWKS for Key Management**: Public key rotation supported via JWKS endpoint
4. **UserInfo Endpoint**: Standard user profile retrieval
5. **End Session Endpoint**: Proper logout/single sign-out support
6. **Claims Parameter Support**: `claims_parameter_supported: true` allows fine-grained claim requests

#### ⚠️ Deviations and Notable Omissions

1. **No Implicit or Hybrid Flows**:
   - **Deviation**: Only `code` response type supported
   - **Assessment**: This is actually a **positive security decision**. Modern best practices discourage implicit flow due to token exposure in browser history and logs.

2. **Limited Token Signing Algorithms**:
   - **Supported**: Only HS256 and RS256
   - **Missing**: ES256, ES384, ES512 (ECDSA algorithms)
   - **Assessment**: RS256 is sufficient for most use cases. ECDSA would offer better performance but isn't critical.

3. **No Grant Types Explicitly Listed**:
   - **Omission**: `grant_types_supported` not present in configuration
   - **Inference**: Likely supports `authorization_code` and `refresh_token`
   - **Assessment**: Minor documentation issue, not a functional problem

4. **No Refresh Token Information**:
   - **Omission**: No mention of `refresh_token` support or `offline_access` scope
   - **Assessment**: Unclear if long-lived sessions are supported. **Test this during integration.**

5. **No Token Endpoint Auth Methods Listed**:
   - **Omission**: `token_endpoint_auth_methods_supported` not present
   - **Standard Default**: `client_secret_basic` (HTTP Basic Authentication)
   - **Assessment**: Assume `client_secret_basic` and `client_secret_post` are supported

6. **No PKCE Indication**:
   - **Omission**: No mention of `code_challenge_methods_supported` (PKCE)
   - **Assessment**: PKCE is critical for mobile/SPA apps. **Verify if supported despite not being advertised.**

7. **Claims List Incomplete**:
   - **Issue**: The `claims_supported` list appears truncated in the response
   - **Missing Expected Claims**: `sub`, `iss`, `aud`, `exp`, `iat`, `email`, `email_verified`
   - **Assessment**: Standard claims are likely supported but not fully documented. The truncation may be a display issue.

---

### Security Analysis

#### ✅ Security Strengths

1. **HTTPS Everywhere**: All endpoints use TLS/SSL
2. **Authorization Code Flow Only**: Prevents token leakage via URL fragments
3. **RS256 Support**: Asymmetric signing allows secure token verification without shared secrets
4. **Multi-Level Authentication Assurance**: NIDs allow risk-based authentication
5. **End Session Endpoint**: Proper logout mechanism prevents session fixation

#### ⚠️ Security Considerations

1. **HS256 Support**:
   - **Risk**: Symmetric signing means client secret is also the signing key
   - **Mitigation**: If HS256 is used, client secret must be highly protected
   - **Recommendation**: **Use RS256 in production**

2. **Public Subject Type Only**:
   - **Risk**: User identifiers are consistent across all applications
   - **Impact**: User tracking across government services is possible
   - **Assessment**: Acceptable for government SSO, but be aware for privacy audits

3. **No PKCE Mention**:
   - **Risk**: If mobile apps use this provider without PKCE, authorization code interception is possible
   - **Recommendation**: **Verify PKCE support** and enable for mobile app integration

4. **Testing Environment**:
   - **Critical**: This configuration is from `auth-testing.iduruguay.gub.uy`
   - **Action Required**: **Obtain production endpoint** (`auth.iduruguay.gub.uy`) before deployment
   - **Differences Expected**: Rate limits, stricter validation, different client credentials

---

### Anomalies and Special Notes

1. **Truncated Claims List**: The `claims_supported` array appears incomplete in the retrieved configuration. Expected standard claims like `sub`, `iss`, `aud`, `exp`, `iat`, `email`, `email_verified` are missing. This may be:
   - A display/serialization issue in the .well-known endpoint
   - Intentional omission of standard claims (assuming they're implicit)
   - Documentation gap

2. **Service Documentation Link**: The provided documentation URL has URL-encoded characters. Proper URL: `https://centroderecursos.agesic.gub.uy/web/seguridad/wiki/-/wiki/Main/ID+Uruguay+-+Integración+con+OpenID+Connect`

3. **No Registration Endpoint**: `registration_endpoint` is not provided, indicating:
   - Dynamic client registration is not supported
   - Clients must be pre-registered with AGESIC
   - Manual onboarding process required

4. **No Introspection or Revocation Endpoints**: `introspection_endpoint` and `revocation_endpoint` are not advertised. This limits ability to:
   - Validate token status in real-time
   - Explicitly revoke tokens before expiration

---

### Implementation Recommendations for HCEN

#### 1. Client Registration
- **Action**: Contact AGESIC to register HCEN as an OpenID Connect client
- **Provide**: Redirect URIs, application description, contact information
- **Receive**: `client_id` and `client_secret`
- **Separate Registrations**: One for testing environment, one for production

#### 2. Authorization Request Configuration

```java
// Example authorization URL construction
String authorizationUrl = "https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize"
    + "?response_type=code"
    + "&client_id=" + clientId
    + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8")
    + "&scope=openid+personal_info+document+profile"
    + "&state=" + generateState()  // CSRF protection
    + "&nonce=" + generateNonce()  // Replay attack prevention
    + "&acr_values=urn:iduruguay:nid:2+urn:iduruguay:nid:3";  // Require NID 2 or 3
```

**Required Parameters**:
- `response_type=code`
- `client_id` (provided by AGESIC)
- `redirect_uri` (must match registered URI exactly)
- `scope` (minimum: `openid`, recommended: `openid personal_info document profile`)
- `state` (cryptographically random, stored in session, validated on callback)
- `nonce` (included in ID token, prevents replay attacks)

**Recommended Parameters**:
- `acr_values` (specify required authentication level)
- `prompt` (optional: `login` to force re-authentication, `consent` to force consent)

#### 3. Token Exchange

```java
// POST to token endpoint
POST https://auth-testing.iduruguay.gub.uy/oidc/v1/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic Base64(client_id:client_secret)

grant_type=authorization_code
&code=AUTHORIZATION_CODE_FROM_CALLBACK
&redirect_uri=SAME_REDIRECT_URI_AS_AUTHORIZATION
```

**Response** (expected):
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "refresh_token_if_supported"
}
```

#### 4. ID Token Validation

**Critical Steps**:
1. **Signature Verification**:
   - Fetch public keys from JWKS endpoint: `https://auth-testing.iduruguay.gub.uy/oidc/v1/jwks`
   - Verify ID token signature using RS256 and the appropriate key (`kid` claim in token header)
   - Cache keys with reasonable TTL (1 hour), refresh on validation failure

2. **Claims Validation**:
   - `iss` (issuer) must equal `https://auth-testing.iduruguay.gub.uy`
   - `aud` (audience) must equal your `client_id`
   - `exp` (expiration) must be in the future (with clock skew tolerance of 60 seconds)
   - `iat` (issued at) must be in the past
   - `nonce` must match the nonce sent in authorization request
   - `acr` (authentication context) must meet your minimum required level (NID 2 or 3)

3. **Extract User Data**:
   ```json
   {
     "sub": "12345678",  // CI (Cédula de Identidad)
     "nombre_completo": "Juan Pérez Rodríguez",
     "primer_nombre": "Juan",
     "primer_apellido": "Pérez",
     "segundo_apellido": "Rodríguez",
     "uid": "12345678",
     "acr": "urn:iduruguay:nid:2",
     "iss": "https://auth-testing.iduruguay.gub.uy",
     "aud": "hcen-client-id",
     "exp": 1730000000,
     "iat": 1729990000,
     "nonce": "..."
   }
   ```

#### 5. UserInfo Endpoint Usage

**When to Use**:
- If additional claims not included in ID token are needed
- If real-time user data freshness is required (e.g., user updated profile)

**Request**:
```
GET https://auth-testing.iduruguay.gub.uy/oidc/v1/userinfo
Authorization: Bearer ACCESS_TOKEN
```

**Response** (expected JSON):
```json
{
  "sub": "12345678",
  "nombre_completo": "Juan Pérez Rodríguez",
  "primer_nombre": "Juan",
  "segundo_nombre": null,
  "primer_apellido": "Pérez",
  "segundo_apellido": "Rodríguez",
  "uid": "12345678",
  "email": "juan.perez@example.com"
}
```

#### 6. Session Management

**Session Creation**:
1. After successful ID token validation, create local HCEN session
2. Store user identifier (`sub` claim = CI) in session
3. Generate HCEN-specific JWT for API authentication
4. Set secure session cookie (HttpOnly, Secure, SameSite=Lax)

**Session Timeout**:
- ID Uruguay session timeout is unknown (not in configuration)
- HCEN should implement independent session timeout (recommended: 30 minutes inactivity, 8 hours maximum)
- Consider periodic UserInfo calls to validate ID Uruguay session is still active

**Logout**:
```java
// Redirect user to ID Uruguay logout endpoint
String logoutUrl = "https://auth-testing.iduruguay.gub.uy/oidc/v1/logout"
    + "?post_logout_redirect_uri=" + URLEncoder.encode(hcenLogoutUri, "UTF-8")
    + "&id_token_hint=" + idToken;  // Optional but recommended

// Then clear local HCEN session
session.invalidate();
```

#### 7. Error Handling

**Authorization Errors** (returned to redirect_uri):
- `access_denied`: User denied consent or authentication failed
- `invalid_request`: Malformed authorization request
- `unauthorized_client`: client_id not recognized or not authorized for OIDC
- `server_error`: ID Uruguay internal error

**Token Endpoint Errors**:
- `invalid_grant`: Authorization code invalid, expired, or already used
- `invalid_client`: client_id/client_secret incorrect
- `unauthorized_client`: Client not authorized for authorization code grant

**Implementation**:
```java
@Path("/callback")
public Response handleCallback(@QueryParam("code") String code,
                               @QueryParam("state") String state,
                               @QueryParam("error") String error,
                               @QueryParam("error_description") String errorDescription) {
    if (error != null) {
        // Log error, show user-friendly message
        logger.error("ID Uruguay authentication failed: {} - {}", error, errorDescription);
        return Response.status(Response.Status.UNAUTHORIZED)
                       .entity(new ErrorDTO("AUTHENTICATION_FAILED", errorDescription))
                       .build();
    }

    // Validate state parameter (CSRF protection)
    if (!validateState(state)) {
        logger.warn("Invalid state parameter, possible CSRF attack");
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity(new ErrorDTO("INVALID_STATE", "Security validation failed"))
                       .build();
    }

    // Exchange code for tokens...
}
```

#### 8. Testing Strategy

**Testing Environment**:
- Base URL: `https://auth-testing.iduruguay.gub.uy`
- Request test credentials from AGESIC
- Verify all flows before requesting production access

**Test Cases**:
1. ✅ Successful authentication with NID 2
2. ✅ Successful authentication with NID 3
3. ✅ User denies consent
4. ✅ Invalid state parameter (CSRF protection)
5. ✅ Expired authorization code
6. ✅ Invalid client credentials
7. ✅ Token expiration and refresh (if supported)
8. ✅ Logout (single sign-out)
9. ✅ Concurrent sessions from same user
10. ✅ Edge cases: special characters in names, users without second name/surname

**Production Checklist**:
- [ ] Obtain production client credentials
- [ ] Update all endpoints to production URLs (remove `-testing`)
- [ ] Verify SSL certificate pinning in mobile app
- [ ] Configure rate limiting and circuit breakers
- [ ] Set up monitoring for authentication failures
- [ ] Document rollback procedure if ID Uruguay is unavailable

#### 9. Fallback Strategy

**ID Uruguay Unavailability**:
- Display user-friendly error message: "Authentication service temporarily unavailable"
- Implement exponential backoff for retries
- Consider emergency fallback authentication (if permitted by AGESIC regulations)
- Monitor ID Uruguay status page (if available)

**Circuit Breaker Configuration**:
```java
@CircuitBreaker(
    failureRateThreshold = 50,        // Open circuit if 50% of requests fail
    waitDurationInOpenState = 60000,  // Wait 60s before trying again
    slidingWindowSize = 10            // Evaluate last 10 requests
)
public AuthenticationResult authenticateWithGubUy(String authorizationCode) {
    // Token exchange logic...
}
```

---

### Integration Checklist

- [ ] **Pre-Integration**
  - [ ] Read official AGESIC documentation
  - [ ] Register HCEN application with AGESIC
  - [ ] Receive `client_id` and `client_secret` for testing
  - [ ] Set up test environment with testing endpoints

- [ ] **Implementation**
  - [ ] Implement authorization request flow (CU01)
  - [ ] Implement callback handler with state validation
  - [ ] Implement token exchange with client authentication
  - [ ] Implement ID token validation (signature, claims)
  - [ ] Implement UserInfo endpoint client (optional)
  - [ ] Implement logout (end session) flow
  - [ ] Create INUS user lookup/registration after authentication
  - [ ] Generate HCEN JWT for subsequent API calls

- [ ] **Security**
  - [ ] Use RS256 for token signing (not HS256)
  - [ ] Implement state parameter (CSRF protection)
  - [ ] Implement nonce parameter (replay attack prevention)
  - [ ] Store client_secret securely (environment variable, secrets manager)
  - [ ] Use HTTPS for all redirect URIs
  - [ ] Implement token caching with appropriate TTL
  - [ ] Implement JWKS caching with refresh logic
  - [ ] Add request logging (sanitize sensitive data)
  - [ ] Implement rate limiting for token endpoint calls

- [ ] **Testing**
  - [ ] Unit tests for token validation logic
  - [ ] Integration tests with ID Uruguay testing environment
  - [ ] Test all error scenarios
  - [ ] Test session timeout and renewal
  - [ ] Test logout (local and SSO)
  - [ ] Performance test authentication flow
  - [ ] Security audit of implementation

- [ ] **Production**
  - [ ] Request production client credentials from AGESIC
  - [ ] Update configuration to production endpoints
  - [ ] Verify production TLS certificates
  - [ ] Set up monitoring and alerting
  - [ ] Document troubleshooting procedures
  - [ ] Plan gradual rollout (pilot group first)

---

### Additional Resources

- **Official Documentation**: https://centroderecursos.agesic.gub.uy/web/seguridad/wiki/-/wiki/Main/ID+Uruguay+-+Integración+con+OpenID+Connect
- **OpenID Connect Core Spec**: https://openid.net/specs/openid-connect-core-1_0.html
- **OAuth 2.0 Security BCP**: https://datatracker.ietf.org/doc/html/draft-ietf-oauth-security-topics
- **AGESIC Contact**: Contact through official channels for client registration and support

---

**Document Version**: 1.1
**Last Updated**: 2025-10-14
**Maintained By**: TSE 2025 - Group 9

---
---

## Centralized Authentication Architecture

### Overview

This section describes the centralized authentication architecture for HCEN, designed to serve three distinct client types through a unified authentication service integrated with gub.uy (ID Uruguay) OpenID Connect provider.

### Client Types

| Client Type | User Type | Technology | Authentication Flow |
|-------------|-----------|------------|---------------------|
| **Mobile App** | Patients/Citizens | React Native | System Browser + Deep Linking + PKCE |
| **Patient Portal** | Patients/Citizens | Web (JSP/React) | Standard Authorization Code Flow |
| **Admin Portal** | HCEN Administrators | Web (JSP) | Standard Authorization Code Flow |

### Design Principles

1. **Centralized Authentication Service**: Single authentication service in HCEN central component handles all OAuth/OIDC flows
2. **Stateless Architecture**: JWT-based tokens for horizontal scalability (AC006, AC007)
3. **Security-First**: HTTPS everywhere, PKCE for mobile, CSRF protection for web
4. **Standards-Compliant**: OAuth 2.0 RFC 6749, OIDC Core 1.0, OAuth 2.0 BCP RFC 8252
5. **Seamless User Experience**: Native browser for mobile, server-side sessions for web

---

### High-Level Architecture Diagram

```
┌───────────────────────────────────────────────────────────────────────────┐
│                    gub.uy (ID Uruguay) - OIDC Provider                     │
│  - Authorization: /oidc/v1/authorize                                       │
│  - Token:        /oidc/v1/token                                           │
│  - UserInfo:     /oidc/v1/userinfo                                        │
│  - Logout:       /oidc/v1/logout                                          │
└────────────────────▲──────────────────────────────────▲──────────────────┘
                     │                                  │
                     │ 3. Token Exchange                │ 3. Token Exchange
                     │                                  │
┌────────────────────┴──────────────────────────────────┴──────────────────┐
│              HCEN Central Component (WildFly Jakarta EE)                  │
│                                                                           │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │        Centralized Authentication Service (JAX-RS)              │    │
│  │                                                                  │    │
│  │  Endpoints:                                                      │    │
│  │  - POST /api/auth/login/initiate     (start OAuth flow)        │    │
│  │  - GET  /api/auth/callback           (handle callback)         │    │
│  │  - POST /api/auth/callback           (mobile code exchange)    │    │
│  │  - POST /api/auth/token/refresh      (refresh tokens)          │    │
│  │  - GET  /api/auth/session            (get user session info)   │    │
│  │  - POST /api/auth/logout             (logout + revoke tokens)  │    │
│  └──────────────────────┬──────────────────────────────────────────┘    │
│                         │                                                │
│  ┌──────────────────────▼──────────────────────────────────────────┐   │
│  │      GubUyOidcClient (CDI Bean - Adapter Pattern)                │   │
│  │  - Build authorization URL                                       │   │
│  │  - Exchange code for tokens (with PKCE support)                 │   │
│  │  - Validate ID tokens (signature, claims)                       │   │
│  │  - Fetch UserInfo                                                │   │
│  │  - JWKS key management                                           │   │
│  └──────────────────────┬──────────────────────────────────────────┘   │
│                         │                                                │
│  ┌──────────────────────▼──────────────────────────────────────────┐   │
│  │         JwtTokenService (CDI Bean)                               │   │
│  │  - Generate HCEN-issued JWT tokens                              │   │
│  │  - Validate token signatures                                     │   │
│  │  - Manage token lifecycle                                        │   │
│  │  - Token refresh logic                                           │   │
│  └──────────────────────┬──────────────────────────────────────────┘   │
│                         │                                                │
│  ┌──────────────────────▼──────────────────────────────────────────┐   │
│  │            INUS Service (Business Logic)                         │   │
│  │  - User lookup by CI                                             │   │
│  │  - User registration (first-time login)                          │   │
│  │  - Profile synchronization with gub.uy claims                   │   │
│  └──────────────────────┬──────────────────────────────────────────┘   │
│                         │                                                │
│  ┌──────────────────────▼──────────────────────────────────────────┐   │
│  │      Audit Service (MongoDB - Authentication Events)             │   │
│  │  - Log all authentication events                                 │   │
│  │  - Track login/logout/token refresh                              │   │
│  │  - Security incident detection                                   │   │
│  └──────────────────────────────────────────────────────────────────┘   │
└─────────▲─────────────────────▲─────────────────────▲──────────────────┘
          │                     │                     │
          │ 2. Initiate         │ 2. Redirect         │ 2. Redirect
          │ OAuth Flow          │ to gub.uy           │ to gub.uy
          │                     │                     │
┌─────────┴────────┐  ┌─────────┴──────────┐  ┌──────┴────────────┐
│   Mobile App     │  │  Patient Portal    │  │  Admin Portal     │
│ (React Native)   │  │  (Web JSP/React)   │  │  (Web JSP)        │
│                  │  │                    │  │                   │
│ - Deep linking   │  │ - Server-side      │  │ - Server-side     │
│ - PKCE           │  │   sessions         │  │   sessions        │
│ - SecureStore    │  │ - HttpOnly cookies │  │ - HttpOnly cookies│
└──────────────────┘  └────────────────────┘  └───────────────────┘
```

---

### Mobile App Integration: Deep Dive

#### The Challenge: OAuth 2.0 on Mobile

Mobile applications face unique challenges with OAuth 2.0:

1. **Security Risks**: Embedded WebViews can intercept credentials
2. **User Experience**: In-app browsers don't support SSO (separate cookie jar)
3. **Trust**: Users are trained not to enter credentials in apps
4. **Token Theft**: Authorization code interception attacks

#### Recommended Approach: System Browser with Deep Linking + PKCE

**Why this is the ONLY secure approach:**

| Aspect | Embedded WebView (BAD) | System Browser + Deep Linking (GOOD) |
|--------|------------------------|--------------------------------------|
| **Security** | App can inject JavaScript, steal credentials | Isolated from app, OS-level security |
| **SSO Support** | No (separate cookies) | Yes (shared browser cookies) |
| **User Trust** | Low (in-app login is suspicious) | High (native browser, familiar UI) |
| **OAuth BCP Compliance** | Violates RFC 8252 | Recommended by RFC 8252 |
| **Auto-fill** | No password manager access | Native password manager works |
| **PKCE Protection** | Optional | Required for public clients |

**OAuth 2.0 Best Current Practice (RFC 8252) explicitly states:**
> "For authorization requests, native apps SHOULD use an external user-agent (system browser) rather than embedded user-agents (WebViews)."

#### Mobile Authentication Flow with Deep Linking

```
┌────────────────────────────────────────────────────────────────────────┐
│                  Mobile App Authentication Flow                         │
│                   (System Browser + PKCE)                               │
└────────────────────────────────────────────────────────────────────────┘

Step 1: User taps "Login with gub.uy" button in React Native app
         ↓
Step 2: App generates PKCE parameters
        - code_verifier = random 43-128 character string
        - code_challenge = BASE64URL(SHA256(code_verifier))
         ↓
Step 3: App calls HCEN backend to initiate flow
        POST /api/auth/login/initiate
        {
          "clientType": "MOBILE",
          "redirectUri": "hcenuy://auth/callback",
          "codeChallenge": "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
          "codeChallengeMethod": "S256",
          "state": "random-csrf-token"
        }
         ↓
Step 4: HCEN backend returns authorization URL
        {
          "authorizationUrl": "https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize?
            client_id=hcen-mobile&
            redirect_uri=hcenuy://auth/callback&
            response_type=code&
            scope=openid+personal_info+document+profile&
            state=random-csrf-token&
            code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM&
            code_challenge_method=S256&
            acr_values=urn:iduruguay:nid:2+urn:iduruguay:nid:3",
          "state": "random-csrf-token"
        }
         ↓
Step 5: App opens SYSTEM BROWSER with authorization URL
        (Using expo-web-browser or react-native-inappbrowser-reborn)
         ↓
Step 6: User authenticates in system browser
        - Enters gub.uy credentials (secure, isolated from app)
        - Completes NID 2 or NID 3 verification
        - Grants consent to share data with HCEN
         ↓
Step 7: gub.uy redirects to: hcenuy://auth/callback?code=AUTH_CODE&state=...
         ↓
Step 8: OS detects custom URL scheme and opens React Native app
        (Deep linking mechanism)
         ↓
Step 9: App extracts authorization code from deep link URL
         ↓
Step 10: App calls HCEN backend to exchange code for tokens
         POST /api/auth/callback
         {
           "code": "AUTH_CODE",
           "codeVerifier": "original_code_verifier",  // PKCE proof
           "state": "random-csrf-token",
           "clientType": "MOBILE",
           "redirectUri": "hcenuy://auth/callback"
         }
         ↓
Step 11: HCEN backend validates state (CSRF protection)
         ↓
Step 12: HCEN backend exchanges code with gub.uy
         POST https://auth-testing.iduruguay.gub.uy/oidc/v1/token
         {
           "grant_type": "authorization_code",
           "code": "AUTH_CODE",
           "redirect_uri": "hcenuy://auth/callback",
           "code_verifier": "original_code_verifier",  // PKCE proof
           "client_id": "hcen-mobile"
           // Note: NO client_secret for public mobile client
         }
         ↓
Step 13: gub.uy validates code_verifier against stored code_challenge
         - Computes: SHA256(code_verifier)
         - Compares with stored code_challenge
         - Only returns tokens if they match (PKCE security)
         ↓
Step 14: gub.uy returns tokens
         {
           "access_token": "gubuy_access_token",
           "id_token": "gubuy_id_token_with_ci",
           "token_type": "Bearer",
           "expires_in": 3600
         }
         ↓
Step 15: HCEN backend validates ID token
         - Verifies signature using JWKS
         - Validates claims (iss, aud, exp, nonce, acr)
         - Extracts CI from uid or sub claim
         ↓
Step 16: HCEN backend looks up user in INUS by CI
         - If not found: create new INUS user with gub.uy claims
         - If found: sync profile data
         ↓
Step 17: HCEN backend generates HCEN-issued JWT tokens
         {
           "access_token": "hcen_jwt_token",
           "refresh_token": "hcen_refresh_token",
           "expires_in": 3600,
           "user": {
             "ci": "12345678",
             "inusId": "inus-uuid-1234",
             "firstName": "Juan",
             "lastName": "Pérez",
             "role": "PATIENT"
           }
         }
         ↓
Step 18: HCEN backend logs authentication event to MongoDB audit
         ↓
Step 19: HCEN backend returns response to mobile app
         ↓
Step 20: Mobile app stores tokens in SecureStore (encrypted keychain)
         ↓
Step 21: Mobile app navigates to main screen (authenticated state)
         ↓
Step 22: Subsequent API calls use HCEN JWT
         GET /api/patients/12345678/history
         Authorization: Bearer hcen_jwt_token
```

#### Why NOT to Use Embedded WebView

**Security Issues:**
1. App can inject JavaScript and intercept credentials
2. User credentials exposed to app (violates OAuth security model)
3. No isolation between app and authentication
4. Vulnerable to malicious apps impersonating gub.uy

**User Experience Issues:**
1. No Single Sign-On (separate cookie jar)
2. No password manager integration
3. Users must re-enter credentials every time
4. Less trust (unfamiliar login UI)

**Standards Compliance:**
- Violates OAuth 2.0 BCP (RFC 8252)
- Violates OIDC Native Apps Best Practices
- gub.uy may block embedded WebView requests (user-agent detection)

**Only acceptable if:** The identity provider explicitly requires it (very rare, never seen in practice)

#### React Native Implementation

##### 1. Deep Link Configuration

**Android** (`android/app/src/main/AndroidManifest.xml`):
```xml
<activity android:name=".MainActivity">
  <intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="hcenuy"
          android:host="auth"
          android:pathPrefix="/callback" />
  </intent-filter>
</activity>
```

**iOS** (`ios/hcen/Info.plist`):
```xml
<key>CFBundleURLTypes</key>
<array>
  <dict>
    <key>CFBundleURLName</key>
    <string>uy.gub.hcen</string>
    <key>CFBundleURLSchemes</key>
    <array>
      <string>hcenuy</string>
    </array>
  </dict>
</array>
```

##### 2. PKCE Helper (TypeScript)

```typescript
import * as Crypto from 'expo-crypto';
import { Buffer } from 'buffer';

/**
 * PKCE (Proof Key for Code Exchange) helper functions
 * Implements RFC 7636 for mobile OAuth 2.0 security
 */
export class PKCEHelper {
  /**
   * Generates a cryptographically random code verifier
   * @returns Base64URL-encoded random string (43-128 characters)
   */
  static generateCodeVerifier(): string {
    const randomBytes = Crypto.getRandomBytes(32); // 32 bytes = 256 bits
    return this.base64URLEncode(randomBytes);
  }

  /**
   * Generates code challenge from verifier using SHA-256
   * @param verifier The code verifier
   * @returns Base64URL-encoded SHA-256 hash
   */
  static async generateCodeChallenge(verifier: string): Promise<string> {
    const hash = await Crypto.digestStringAsync(
      Crypto.CryptoDigestAlgorithm.SHA256,
      verifier
    );
    return this.base64URLEncode(Buffer.from(hash, 'hex'));
  }

  /**
   * Base64URL encoding (RFC 4648 Section 5, without padding)
   */
  private static base64URLEncode(buffer: Buffer | Uint8Array): string {
    return Buffer.from(buffer)
      .toString('base64')
      .replace(/\+/g, '-')  // Replace + with -
      .replace(/\//g, '_')  // Replace / with _
      .replace(/=/g, '');   // Remove padding
  }
}
```

##### 3. Authentication Service (TypeScript)

```typescript
import * as Linking from 'expo-linking';
import * as WebBrowser from 'expo-web-browser';
import * as SecureStore from 'expo-secure-store';
import { PKCEHelper } from './PKCEHelper';

interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: {
    ci: string;
    inusId: string;
    firstName: string;
    lastName: string;
    role: string;
  };
}

/**
 * Authentication service for HCEN mobile app
 * Implements OAuth 2.0 with PKCE via system browser + deep linking
 */
export class AuthService {
  private static readonly HCEN_API_BASE = 'https://api.hcen.uy';
  private static readonly REDIRECT_URI = 'hcenuy://auth/callback';

  /**
   * Initiates OAuth 2.0 authorization flow with PKCE
   * Opens system browser for secure authentication
   */
  static async login(): Promise<AuthTokens> {
    try {
      // Step 1: Generate PKCE parameters
      const codeVerifier = PKCEHelper.generateCodeVerifier();
      const codeChallenge = await PKCEHelper.generateCodeChallenge(codeVerifier);
      const state = this.generateRandomState();

      // Step 2: Store PKCE parameters for later use
      await SecureStore.setItemAsync('pkce_verifier', codeVerifier);
      await SecureStore.setItemAsync('oauth_state', state);

      // Step 3: Request authorization URL from HCEN backend
      const initiateResponse = await fetch(
        `${this.HCEN_API_BASE}/api/auth/login/initiate`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            clientType: 'MOBILE',
            redirectUri: this.REDIRECT_URI,
            codeChallenge,
            codeChallengeMethod: 'S256',
            state,
          }),
        }
      );

      if (!initiateResponse.ok) {
        throw new Error('Failed to initiate authentication');
      }

      const { authorizationUrl } = await initiateResponse.json();

      // Step 4: Open system browser with authorization URL
      const result = await WebBrowser.openAuthSessionAsync(
        authorizationUrl,
        this.REDIRECT_URI
      );

      // Step 5: Handle browser result
      if (result.type !== 'success') {
        throw new Error('Authentication cancelled or failed');
      }

      // Step 6: Extract code and state from redirect URL
      const { code, state: returnedState } = this.parseCallbackUrl(result.url);

      // Step 7: Validate state (CSRF protection)
      const storedState = await SecureStore.getItemAsync('oauth_state');
      if (returnedState !== storedState) {
        throw new Error('Invalid state - possible CSRF attack');
      }

      // Step 8: Exchange code for tokens via HCEN backend
      const tokens = await this.exchangeCodeForTokens(code, codeVerifier);

      // Step 9: Store tokens securely
      await this.storeTokens(tokens);

      // Step 10: Clean up temporary values
      await SecureStore.deleteItemAsync('pkce_verifier');
      await SecureStore.deleteItemAsync('oauth_state');

      return tokens;

    } catch (error) {
      // Clean up on error
      await SecureStore.deleteItemAsync('pkce_verifier');
      await SecureStore.deleteItemAsync('oauth_state');
      throw error;
    }
  }

  /**
   * Exchanges authorization code for tokens via HCEN backend
   * Backend handles actual token exchange with gub.uy
   */
  private static async exchangeCodeForTokens(
    code: string,
    codeVerifier: string
  ): Promise<AuthTokens> {
    const response = await fetch(`${this.HCEN_API_BASE}/api/auth/callback`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        code,
        codeVerifier,
        clientType: 'MOBILE',
        redirectUri: this.REDIRECT_URI,
      }),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Token exchange failed');
    }

    return response.json();
  }

  /**
   * Stores authentication tokens securely using platform keychain
   */
  private static async storeTokens(tokens: AuthTokens): Promise<void> {
    await SecureStore.setItemAsync('access_token', tokens.accessToken);
    await SecureStore.setItemAsync('refresh_token', tokens.refreshToken);
    await SecureStore.setItemAsync(
      'token_expiry',
      (Date.now() + tokens.expiresIn * 1000).toString()
    );
    await SecureStore.setItemAsync('user_data', JSON.stringify(tokens.user));
  }

  /**
   * Retrieves current access token, refreshing if expired
   */
  static async getAccessToken(): Promise<string | null> {
    const accessToken = await SecureStore.getItemAsync('access_token');
    const expiryStr = await SecureStore.getItemAsync('token_expiry');

    if (!accessToken || !expiryStr) {
      return null;
    }

    const expiry = parseInt(expiryStr, 10);
    const now = Date.now();

    // If token expires in less than 5 minutes, refresh it proactively
    if (expiry - now < 5 * 60 * 1000) {
      try {
        return await this.refreshAccessToken();
      } catch (error) {
        // Refresh failed - user needs to re-login
        return null;
      }
    }

    return accessToken;
  }

  /**
   * Refreshes access token using refresh token
   */
  static async refreshAccessToken(): Promise<string> {
    const refreshToken = await SecureStore.getItemAsync('refresh_token');

    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const response = await fetch(
      `${this.HCEN_API_BASE}/api/auth/token/refresh`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      }
    );

    if (!response.ok) {
      // Refresh token expired or invalid - clear tokens
      await this.clearTokens();
      throw new Error('Refresh token expired - please login again');
    }

    const tokens = await response.json();
    await this.storeTokens(tokens);

    return tokens.accessToken;
  }

  /**
   * Logs out user by revoking tokens and clearing local storage
   */
  static async logout(): Promise<void> {
    const accessToken = await SecureStore.getItemAsync('access_token');

    if (accessToken) {
      // Notify backend to revoke tokens
      try {
        await fetch(`${this.HCEN_API_BASE}/api/auth/logout`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`,
          },
        });
      } catch (error) {
        // Ignore errors - clear local tokens anyway
        console.warn('Logout request failed:', error);
      }
    }

    await this.clearTokens();
  }

  /**
   * Clears all stored authentication data
   */
  private static async clearTokens(): Promise<void> {
    await SecureStore.deleteItemAsync('access_token');
    await SecureStore.deleteItemAsync('refresh_token');
    await SecureStore.deleteItemAsync('token_expiry');
    await SecureStore.deleteItemAsync('user_data');
  }

  /**
   * Generates cryptographically random state for CSRF protection
   */
  private static generateRandomState(): string {
    const randomBytes = Crypto.getRandomBytes(16);
    return Buffer.from(randomBytes).toString('hex');
  }

  /**
   * Parses callback URL to extract code and state parameters
   */
  private static parseCallbackUrl(url: string): { code: string; state: string } {
    const parsed = Linking.parse(url);
    const code = parsed.queryParams?.code as string;
    const state = parsed.queryParams?.state as string;

    if (!code || !state) {
      throw new Error('Invalid callback URL - missing parameters');
    }

    return { code, state };
  }

  /**
   * Checks if user is authenticated (has valid token)
   */
  static async isAuthenticated(): Promise<boolean> {
    const token = await this.getAccessToken();
    return token !== null;
  }

  /**
   * Gets current user data from secure storage
   */
  static async getCurrentUser() {
    const userDataStr = await SecureStore.getItemAsync('user_data');
    return userDataStr ? JSON.parse(userDataStr) : null;
  }
}
```

##### 4. API Client with Auto-Refresh (TypeScript)

```typescript
import axios, { AxiosInstance } from 'axios';
import { AuthService } from './AuthService';

/**
 * API client with automatic token refresh
 * Intercepts 401 responses and refreshes token transparently
 */
class APIClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: 'https://api.hcen.uy',
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Request interceptor - add access token to all requests
    this.client.interceptors.request.use(
      async (config) => {
        const accessToken = await AuthService.getAccessToken();

        if (accessToken) {
          config.headers.Authorization = `Bearer ${accessToken}`;
        }

        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor - handle token expiration
    this.client.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;

        // If 401 and we haven't retried yet
        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;

          try {
            // Try to refresh token
            const newAccessToken = await AuthService.refreshAccessToken();
            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

            // Retry original request with new token
            return this.client(originalRequest);
          } catch (refreshError) {
            // Refresh failed - redirect to login
            // (This would trigger app navigation to login screen)
            throw refreshError;
          }
        }

        return Promise.reject(error);
      }
    );
  }

  /**
   * Get Axios instance for making API calls
   */
  getInstance(): AxiosInstance {
    return this.client;
  }
}

// Export singleton instance
export default new APIClient().getInstance();
```

---

### Web Portal Authentication Flow

For web portals (Patient Portal and Admin Portal), the authentication flow uses a branded login page before redirecting to gub.uy:

```
Step 1: User navigates to HCEN web portal (Patient or Admin)
         ↓
Step 2: User sees HCEN-branded login page at /login
         - HCEN logo and branding
         - Button: "Login with ID Uruguay"
         - Optional: Terms of service, privacy policy links
         ↓
Step 3: User clicks "Login with ID Uruguay" button
         ↓
Step 4: Browser sends GET request to HCEN backend
        GET /api/auth/login/initiate?clientType=WEB_PATIENT
         ↓
Step 5: HCEN backend generates state, stores in Redis (5min TTL)
         ↓
Step 6: HCEN backend responds with 302 redirect to gub.uy
        Location: https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize?
          client_id=hcen-web-patient&
          redirect_uri=https://hcen.uy/api/auth/callback&
          response_type=code&
          scope=openid+personal_info+document+profile&
          state=CSRF_TOKEN&
          acr_values=urn:iduruguay:nid:2+urn:iduruguay:nid:3
         ↓
Step 7: Browser redirects to gub.uy, user authenticates
         - User enters gub.uy credentials
         - User completes NID 2 or NID 3 verification
         - User grants consent to share data with HCEN
         ↓
Step 8: gub.uy redirects back to HCEN
        GET /api/auth/callback?code=AUTH_CODE&state=CSRF_TOKEN
         ↓
Step 9: HCEN backend validates state (checks Redis)
         ↓
Step 10: HCEN backend exchanges code for tokens with gub.uy
         (includes client_secret for confidential web client)
         ↓
Step 11: HCEN backend validates ID token, looks up user in INUS
         ↓
Step 12: HCEN backend generates HCEN JWT, creates session
         ↓
Step 13: HCEN backend sets secure cookie and redirects to dashboard
         Set-Cookie: hcen_token=JWT; HttpOnly; Secure; SameSite=Lax
         Location: /usuario/dashboard
         ↓
Step 14: User lands on authenticated dashboard
```

**Key Differences from Mobile:**
- No PKCE (web client is confidential, has client_secret)
- Server-side redirects (browser follows automatically)
- HttpOnly cookies (not accessible to JavaScript, prevents XSS)
- State stored in Redis (not in client)
- Client secret used for token exchange (mobile clients don't have secrets)

---

### Jakarta EE Implementation Components

#### Package Structure

```
uy.gub.hcen.auth/
├── api/
│   └── rest/
│       └── AuthenticationResource.java     # JAX-RS REST endpoints
├── service/
│   ├── AuthenticationService.java          # Orchestration service
│   ├── JwtTokenService.java                # JWT generation/validation
│   └── StateManager.java                   # OAuth state management (Redis)
├── integration/
│   └── gubuy/
│       ├── GubUyOidcClient.java           # HTTP client for gub.uy
│       ├── GubUyTokenValidator.java       # ID token validation
│       └── JwksKeyManager.java            # JWKS key caching
├── dto/
│   ├── LoginInitiateRequest.java          # Request/Response DTOs
│   ├── LoginInitiateResponse.java
│   ├── CallbackRequest.java
│   ├── CallbackResponse.java
│   ├── TokenRefreshRequest.java
│   ├── TokenRefreshResponse.java
│   └── SessionInfoResponse.java
├── entity/
│   └── RefreshToken.java                  # JPA entity for refresh tokens
├── config/
│   ├── OidcConfiguration.java             # gub.uy OIDC config
│   └── JwtConfiguration.java              # HCEN JWT config
├── security/
│   ├── JwtAuthenticationFilter.java       # JAX-RS filter for JWT validation
│   └── CsrfProtectionFilter.java          # CSRF protection for web
└── exception/
    ├── AuthenticationException.java       # Custom exceptions
    ├── TokenExpiredException.java
    └── InvalidStateException.java
```

#### Key Interfaces

**AuthenticationService** (Stateless EJB):
```java
@Stateless
public class AuthenticationService {
    LoginInitiateResponse initiateLogin(LoginInitiateRequest request);
    CallbackResponse handleCallback(CallbackRequest request);
    TokenRefreshResponse refreshToken(TokenRefreshRequest request);
    SessionInfoResponse getSessionInfo(String accessToken);
    void logout(String accessToken);
}
```

**GubUyOidcClient** (Application Scoped CDI Bean):
```java
@ApplicationScoped
public class GubUyOidcClient {
    String buildAuthorizationUrl(String clientId, String redirectUri, String state,
                                  String codeChallenge, String scope, String acrValues);
    GubUyTokenResponse exchangeCodeForTokens(String code, String redirectUri,
                                             String codeVerifier, String clientSecret);
    Map<String, Object> getUserInfo(String accessToken);
    Map<String, Object> validateIdToken(String idToken);
}
```

**JwtTokenService** (Application Scoped CDI Bean):
```java
@ApplicationScoped
public class JwtTokenService {
    String generateAccessToken(String ci, String inusId, String role,
                                Map<String, Object> additionalClaims);
    String generateRefreshToken(String ci, String inusId);
    Map<String, Object> validateToken(String token);
    boolean isTokenExpired(String token);
}
```

---

### REST API Endpoints Specification

#### 1. POST /api/auth/login/initiate

Initiates OAuth 2.0 authorization flow.

**Request:**
```json
{
  "clientType": "MOBILE | WEB_PATIENT | WEB_ADMIN",
  "redirectUri": "hcenuy://auth/callback | https://hcen.uy/api/auth/callback",
  "codeChallenge": "BASE64URL(SHA256(code_verifier))",  // Required for MOBILE
  "codeChallengeMethod": "S256",                         // Required for MOBILE
  "state": "optional-client-generated-state"
}
```

**Response 200 OK:**
```json
{
  "authorizationUrl": "https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize?...",
  "state": "generated-or-provided-state",
  "expiresIn": 300
}
```

#### 2. POST /api/auth/callback

Handles OAuth 2.0 callback (mobile clients use POST, web uses GET).

**Request (Mobile):**
```json
{
  "code": "authorization_code_from_gubuy",
  "state": "state_from_initiate",
  "codeVerifier": "original_code_verifier",
  "clientType": "MOBILE",
  "redirectUri": "hcenuy://auth/callback"
}
```

**Response 200 OK:**
```json
{
  "accessToken": "hcen_jwt_access_token",
  "refreshToken": "hcen_refresh_token",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "ci": "12345678",
    "inusId": "inus-uuid-1234",
    "firstName": "Juan",
    "lastName": "Pérez",
    "role": "PATIENT"
  }
}
```

#### 3. POST /api/auth/token/refresh

Refreshes expired access token.

**Request:**
```json
{
  "refreshToken": "hcen_refresh_token"
}
```

**Response 200 OK:**
```json
{
  "accessToken": "new_hcen_jwt_access_token",
  "refreshToken": "new_hcen_refresh_token",  // Rotated
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

#### 4. GET /api/auth/session

Gets current session information.

**Request Headers:**
```
Authorization: Bearer hcen_jwt_access_token
```

**Response 200 OK:**
```json
{
  "user": {
    "ci": "12345678",
    "inusId": "inus-uuid-1234",
    "firstName": "Juan",
    "lastName": "Pérez",
    "role": "PATIENT"
  },
  "session": {
    "authenticatedAt": "2025-10-14T10:00:00Z",
    "expiresAt": "2025-10-14T11:00:00Z",
    "remainingSeconds": 3456
  }
}
```

#### 5. POST /api/auth/logout

Logs out user and revokes tokens.

**Request Headers:**
```
Authorization: Bearer hcen_jwt_access_token
```

**Request Body (optional):**
```json
{
  "refreshToken": "hcen_refresh_token"
}
```

**Response:** 204 No Content

---

### Security Implementation Details

#### 1. PKCE (Proof Key for Code Exchange)

**Why:** Prevents authorization code interception attacks on mobile devices.

**Flow:**
1. Mobile app generates `code_verifier` (random 43-128 char string)
2. Mobile app computes `code_challenge = BASE64URL(SHA256(code_verifier))`
3. Mobile app sends `code_challenge` in authorization request
4. gub.uy stores `code_challenge` with authorization code
5. Mobile app sends `code_verifier` with token exchange
6. HCEN backend includes `code_verifier` in token request to gub.uy
7. gub.uy verifies `SHA256(code_verifier) == stored_code_challenge`

**Attack Prevented:**
```
Attacker intercepts deep link: hcenuy://auth/callback?code=STOLEN_CODE
Attacker tries to exchange code for tokens
→ FAILS because attacker doesn't have code_verifier
→ gub.uy rejects token request
```

#### 2. State Parameter (CSRF Protection)

**Why:** Prevents Cross-Site Request Forgery attacks.

**Implementation:**
- Generate cryptographically random state (16+ bytes)
- Store state in Redis with 5-minute TTL
- Include state in authorization URL
- Validate state matches when handling callback
- Delete state after validation (single-use)

**Attack Prevented:**
```
Attacker initiates OAuth flow with victim's account
Attacker captures callback URL with their code
Attacker tricks victim into clicking malicious link with attacker's code
→ FAILS because state doesn't match victim's session
→ HCEN rejects callback
```

#### 3. Token Storage

| Client | Storage Method | Security Features |
|--------|----------------|-------------------|
| Mobile | React Native SecureStore | Hardware-backed encryption (iOS Keychain, Android Keystore) |
| Web | HttpOnly, Secure, SameSite cookies | Not accessible to JavaScript, prevents XSS |

**Never store tokens in:**
- LocalStorage (vulnerable to XSS)
- SessionStorage (vulnerable to XSS)
- Regular cookies without HttpOnly
- Mobile SharedPreferences without encryption

#### 4. Token Refresh with Rotation

**Implementation:**
```
Old Refresh Token → Exchange → New Access Token + New Refresh Token
                                (old refresh token invalidated)
```

**Benefits:**
- Limits damage if refresh token is stolen (expires after one use)
- Enables detection of token theft (both user and attacker try to use same token)
- Follows OAuth 2.0 security best practices

#### 5. Certificate Pinning (Mobile)

**Purpose:** Prevent Man-in-the-Middle attacks using rogue SSL certificates.

**Android Configuration** (`android/app/src/main/res/xml/network_security_config.xml`):
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">hcen.uy</domain>
        <pin-set expiration="2026-01-01">
            <!-- Pin for current certificate -->
            <pin digest="SHA-256">base64_cert_hash_1</pin>
            <!-- Pin for backup certificate -->
            <pin digest="SHA-256">base64_cert_hash_2</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

**How to get certificate hash:**
```bash
openssl s_client -connect hcen.uy:443 | \
  openssl x509 -pubkey -noout | \
  openssl rsa -pubin -outform der | \
  openssl dgst -sha256 -binary | \
  openssl enc -base64
```

---

### Infrastructure Requirements

#### 1. gub.uy Client Registration

**Register three separate OAuth 2.0 clients:**

**Client 1: Mobile App (Public Client)**
- Client ID: `hcen-mobile-app`
- Client Type: Public (no client secret)
- Redirect URIs: `hcenuy://auth/callback`, `uy.gub.hcen://auth/callback`
- Grant Types: `authorization_code`
- PKCE: Required
- Scopes: `openid`, `personal_info`, `document`, `profile`

**Client 2: Patient Web Portal (Confidential Client)**
- Client ID: `hcen-web-patient`
- Client Type: Confidential (has client secret)
- Client Secret: `<provided-by-agesic>`
- Redirect URIs: `https://hcen.uy/api/auth/callback`
- Grant Types: `authorization_code`
- Scopes: `openid`, `personal_info`, `document`, `profile`

**Client 3: Admin Web Portal (Confidential Client)**
- Client ID: `hcen-web-admin`
- Client Type: Confidential
- Client Secret: `<provided-by-agesic>`
- Redirect URIs: `https://admin.hcen.uy/api/auth/callback`
- Grant Types: `authorization_code`
- Scopes: `openid`, `personal_info`, `document`, `profile`

#### 2. Environment Variables

```bash
# gub.uy OIDC Configuration
GUBUY_OIDC_ISSUER=https://auth-testing.iduruguay.gub.uy
GUBUY_OIDC_AUTHORIZATION_ENDPOINT=https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize
GUBUY_OIDC_TOKEN_ENDPOINT=https://auth-testing.iduruguay.gub.uy/oidc/v1/token
GUBUY_OIDC_USERINFO_ENDPOINT=https://auth-testing.iduruguay.gub.uy/oidc/v1/userinfo
GUBUY_OIDC_JWKS_URI=https://auth-testing.iduruguay.gub.uy/oidc/v1/jwks

# OAuth 2.0 Clients
GUBUY_OIDC_CLIENT_ID_MOBILE=hcen-mobile-app
GUBUY_OIDC_CLIENT_ID_WEB_PATIENT=hcen-web-patient
GUBUY_OIDC_CLIENT_SECRET_WEB_PATIENT=<secret>
GUBUY_OIDC_CLIENT_ID_WEB_ADMIN=hcen-web-admin
GUBUY_OIDC_CLIENT_SECRET_WEB_ADMIN=<secret>

# JWT Configuration (HCEN-issued tokens)
JWT_ISSUER=https://hcen.uy
JWT_ACCESS_TOKEN_TTL=3600       # 1 hour
JWT_REFRESH_TOKEN_TTL=2592000   # 30 days
JWT_ALGORITHM=RS256
JWT_SIGNING_KEY_PATH=/opt/hcen/keys/jwt-private.pem

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=<secure-password>
```

#### 3. PostgreSQL Schema

```sql
-- Refresh tokens table
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token_hash VARCHAR(128) UNIQUE NOT NULL,
    user_ci VARCHAR(20) NOT NULL,
    client_type VARCHAR(20) NOT NULL,
    device_id VARCHAR(128),
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_ci) REFERENCES inus_users(ci) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_ci ON refresh_tokens(user_ci);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
```

#### 4. Redis Configuration

**Purpose:** Store OAuth state, rate limiting, token blacklist.

**Key Patterns:**
- `oauth:state:{state}` → Client info, code challenge (TTL: 5 minutes)
- `ratelimit:{ip}:{endpoint}` → Request count (TTL: varies)
- `token:blacklist:{jti}` → Revoked token (TTL: token lifetime)

#### 5. Dependencies (build.gradle)

```gradle
dependencies {
    // JWT processing
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'

    // HTTP client for gub.uy
    implementation 'org.apache.httpcomponents.client5:httpclient5:5.3'

    // Redis client
    implementation 'redis.clients:jedis:5.1.0'

    // JSON processing
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.3'
}
```

---

### Summary and Recommendations

#### Key Takeaways

1. **Mobile App MUST use system browser + deep linking + PKCE**
   - Never use embedded WebView
   - Follows OAuth 2.0 Best Current Practice (RFC 8252)
   - Provides best security and user experience

2. **Web portals use standard server-side OAuth flow**
   - Server-side redirects
   - HttpOnly cookies for session management
   - Client secrets for confidential clients

3. **Centralized authentication service in HCEN backend**
   - Single point of integration with gub.uy
   - Consistent JWT token format for all clients
   - Shared audit logging and user management

4. **Security is paramount**
   - PKCE for mobile
   - State parameter for CSRF protection
   - Token rotation for refresh tokens
   - Certificate pinning for mobile
   - HTTPS everywhere

#### Next Steps

1. **Register OAuth 2.0 clients with AGESIC**
   - Provide redirect URIs
   - Obtain client IDs and secrets
   - Request testing credentials

2. **Implement Jakarta EE backend components**
   - AuthenticationService (orchestration)
   - GubUyOidcClient (integration adapter)
   - JwtTokenService (token management)
   - REST endpoints

3. **Implement React Native mobile authentication**
   - Deep link configuration
   - PKCE helper
   - AuthService with system browser
   - Secure token storage

4. **Implement web portal authentication**
   - JSP/React login pages
   - Server-side callback handlers
   - Session management with cookies

5. **Testing and validation**
   - Unit tests (80% coverage)
   - Integration tests with gub.uy testing environment
   - Security audit
   - Performance testing

6. **Production deployment**
   - Update to production gub.uy endpoints
   - SSL certificate installation
   - Monitoring and alerting
   - Documentation and runbooks

---

**Section Author**: TSE 2025 Group 9 (German Rodao, Agustin Silvano, Piero Santos)
**Section Version**: 1.0
**Last Updated**: 2025-10-14