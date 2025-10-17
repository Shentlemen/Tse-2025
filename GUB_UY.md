/# ID Uruguay - OpenID Connect Integration Guide

## 1. Overview of ID Uruguay

**ID Uruguay** is the national authentication platform implemented by AGESIC (Agencia de Gobierno Electrónico y Sociedad de la Información y del Conocimiento) to centralize user accounts and facilitate secure web access to Uruguay's digital government services.

### Key Features
- **Centralized Authentication**: Single sign-on (SSO) for multiple government services
- **Standards-Based**: Implements OpenID Connect 1.0 protocol built on OAuth 2.0
- **Identity Verification Levels**: Four-tier authentication assurance levels (NID 0-3)
- **Privacy-Focused**: Granular consent for data sharing between services
- **National Coverage**: Universal authentication for Uruguayan citizens and residents

### Purpose
ID Uruguay allows client applications (Relying Party - RP) to:
- Verify a user's identity based on authentication performed on the Authorization Server (OpenID Provider - OP)
- Obtain personal user information through a REST API
- Implement secure, government-approved authentication for digital services

---

## 2. OpenID Connect Implementation Details

### Protocol Compliance
- **Standard**: OpenID Connect 1.0 (built on OAuth 2.0)
- **Flow**: Authorization Code Flow
- **Subject Type**: Public
- **Token Signing Algorithms**: HS256, RS256

### Supported Features
- **Discovery**: OpenID Connect Discovery 1.0 compliant
- **Claims Parameter**: Supported (allows requesting specific claims)
- **ACR (Authentication Context Class Reference)**: Four levels of identity assurance
- **Logout**: Single logout endpoint for session termination

### User Identifier Format
The User ID (UID) follows the pattern: `xx-yyy-zzzzzzz`

Where:
- `xx` = ISO country code (e.g., "uy" for Uruguay)
- `yyy` = Document type code (ci, dni, psp)
- `zzzzzzz` = Document number

**Example**: A user with Uruguayan Identity Card (CI) 1231231-4 will have UID: `uy-ci-12312314`

---

## 3. Authentication Flow

### Standard Authorization Code Flow

```
┌─────────┐                                            ┌──────────────┐
│         │                                            │              │
│ Client  │                                            │ ID Uruguay   │
│  (RP)   │                                            │    (OP)      │
│         │                                            │              │
└────┬────┘                                            └──────┬───────┘
     │                                                        │
     │ 1. Authentication Request                              │
     │   GET /oidc/v1/authorize?                              │
     │   response_type=code&client_id=...&                    │
     │   redirect_uri=...&scope=openid...                     │
     ├───────────────────────────────────────────────────────>│
     │                                                        │
     │                    2. User Authentication              │
     │                       & Consent                        │
     │                                                        │
     │ 3. Authorization Code                                  │
     │   Redirect: redirect_uri?code=...                      │
     │<───────────────────────────────────────────────────────┤
     │                                                        │
     │ 4. Token Request                                       │
     │   POST /oidc/v1/token                                  │
     │   grant_type=authorization_code&code=...&              │
     │   client_id=...&client_secret=...                      │
     ├───────────────────────────────────────────────────────>│
     │                                                        │
     │ 5. Tokens Response                                     │
     │   {access_token, id_token, token_type}                 │
     │<───────────────────────────────────────────────────────┤
     │                                                        │
     │ 6. UserInfo Request                                    │
     │   GET /oidc/v1/userinfo                                │
     │   Authorization: Bearer {access_token}                 │
     ├───────────────────────────────────────────────────────>│
     │                                                        │
     │ 7. User Claims Response                                │
     │   {uid, name, email, document, ...}                    │
     │<───────────────────────────────────────────────────────┤
     │                                                        │
```

### Flow Steps Explained

1. **Authentication Request**: The RP redirects the user to ID Uruguay's authorization endpoint with required parameters
2. **User Authentication & Consent**: ID Uruguay authenticates the user and obtains authorization to share requested data (claims)
3. **Authorization Code**: ID Uruguay responds with an authorization code via redirect
4. **Token Request**: The RP exchanges the authorization code for tokens using the token endpoint
5. **Tokens Response**: ID Uruguay returns an ID Token (JWT) and Access Token
6. **UserInfo Request**: The RP uses the Access Token to request user information
7. **User Claims Response**: ID Uruguay returns the requested user claims

---

## 4. Endpoints

### Testing Environment

**Base URL**: `https://auth-testing.iduruguay.gub.uy`

| Endpoint | URL | Method |
|----------|-----|--------|
| **Discovery** | `https://auth-testing.iduruguay.gub.uy/oidc/v1/.well-known/openid-configuration` | GET |
| **Authorization** | `https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize` | GET |
| **Token** | `https://auth-testing.iduruguay.gub.uy/oidc/v1/token` | POST |
| **UserInfo** | `https://auth-testing.iduruguay.gub.uy/oidc/v1/userinfo` | GET |
| **JWKS** | `https://auth-testing.iduruguay.gub.uy/oidc/v1/jwks` | GET |
| **Logout** | `https://auth-testing.iduruguay.gub.uy/oidc/v1/logout` | GET |

### Production Environment

**Base URL**: `https://auth.iduruguay.gub.uy`

| Endpoint | URL | Method |
|----------|-----|--------|
| **Discovery** | `https://auth.iduruguay.gub.uy/oidc/v1/.well-known/openid-configuration` | GET |
| **Authorization** | `https://auth.iduruguay.gub.uy/oidc/v1/authorize` | GET |
| **Token** | `https://auth.iduruguay.gub.uy/oidc/v1/token` | POST |
| **UserInfo** | `https://auth.iduruguay.gub.uy/oidc/v1/userinfo` | GET |
| **JWKS** | `https://auth.iduruguay.gub.uy/oidc/v1/jwks` | GET |
| **Logout** | `https://auth.iduruguay.gub.uy/oidc/v1/logout` | GET |

### Endpoint Details

#### Authorization Endpoint
Initiates the authentication flow by redirecting the user to ID Uruguay.

**Parameters**:
- `response_type` (required): Must be "code"
- `client_id` (required): Your registered client identifier
- `redirect_uri` (required): Must match one of your registered redirect URIs
- `scope` (required): Space-separated list of scopes (must include "openid")
- `state` (recommended): Opaque value to maintain state between request and callback
- `nonce` (recommended): String value used to associate a client session with an ID Token
- `acr_values` (optional): Requested Authentication Context Class Reference values

**Example**:
```
GET https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize?
  response_type=code&
  client_id=890192&
  redirect_uri=https://openidconnect.net/callback&
  scope=openid%20personal_info%20email&
  state=af0ifjsldkj&
  nonce=n-0S6_WzA2Mj
```

#### Token Endpoint
Exchanges the authorization code for tokens.

**Parameters** (application/x-www-form-urlencoded):
- `grant_type` (required): Must be "authorization_code"
- `code` (required): The authorization code received from the authorization endpoint
- `redirect_uri` (required): Must match the redirect_uri used in the authorization request
- `client_id` (required): Your client identifier
- `client_secret` (required): Your client secret

**Example Request**:
```http
POST /oidc/v1/token HTTP/1.1
Host: auth-testing.iduruguay.gub.uy
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&
code=SplxlOBeZQQYbYS6WxSbIA&
redirect_uri=https://openidconnect.net/callback&
client_id=890192&
client_secret=457d52f181bf11804a3365b49ae4d29a2e03bbabe74997a2f510b179
```

**Example Response**:
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "8xLOxBtZp8"
}
```

#### UserInfo Endpoint
Returns claims about the authenticated user.

**Authentication**: Bearer token (access_token) in Authorization header

**Example Request**:
```http
GET /oidc/v1/userinfo HTTP/1.1
Host: auth-testing.iduruguay.gub.uy
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example Response**:
```json
{
  "uid": "uy-ci-12312314",
  "nombre_completo": "Juan Alberto Pérez González",
  "primer_nombre": "Juan",
  "segundo_nombre": "Alberto",
  "primer_apellido": "Pérez",
  "segundo_apellido": "González",
  "pais_documento": "UY",
  "tipo_documento": "ci",
  "numero_documento": "12312314",
  "email": "juan.perez@example.com",
  "email_verified": true,
  "nid": 2
}
```

#### JWKS Endpoint
Provides public keys for verifying JWT signatures.

**Example Request**:
```http
GET /oidc/v1/jwks HTTP/1.1
Host: auth-testing.iduruguay.gub.uy
```

#### Logout Endpoint
Terminates the user's session with ID Uruguay.

**Parameters**:
- `id_token_hint` (optional): ID Token previously issued
- `post_logout_redirect_uri` (optional): URL to redirect after logout
- `state` (optional): Opaque value to maintain state

---

## 5. Scopes and Claims

### Supported Scopes

ID Uruguay defines scopes based on OpenID Connect 1.0 standards. The following scopes are available:

| Scope | Description | Claims Included |
|-------|-------------|-----------------|
| `openid` | **Required**. Indicates OpenID Connect authentication | `uid` |
| `personal_info` | User's full name and name components | `nombre_completo`, `primer_nombre`, `segundo_nombre`, `primer_apellido`, `segundo_apellido`, `name`, `given_name`, `family_name` |
| `email` | User's email address | `email`, `email_verified` |
| `document` | Identity document information | `pais_documento`, `tipo_documento`, `numero_documento` |
| `profile` | User profile information | Standard OpenID Connect profile claims |

### Available Claims

#### Identity Claims (scope: openid)

| Claim | Type | Description | Example |
|-------|------|-------------|---------|
| `uid` | String | Unique user identifier in format `xx-yyy-zzzzzzz` | `"uy-ci-12312314"` |

#### Personal Information Claims (scope: personal_info)

| Claim | Type | Description | Example |
|-------|------|-------------|---------|
| `nombre_completo` | String | Full name (Uruguay-specific format) | `"Juan Alberto Pérez González"` |
| `primer_nombre` | String | First given name | `"Juan"` |
| `segundo_nombre` | String | Second given name (middle name) | `"Alberto"` |
| `primer_apellido` | String | First surname | `"Pérez"` |
| `segundo_apellido` | String | Second surname | `"González"` |
| `name` | String | Full name (OpenID Connect standard) | `"Juan Alberto Pérez González"` |
| `given_name` | String | Given names (OpenID Connect standard) | `"Juan Alberto"` |
| `family_name` | String | Surnames (OpenID Connect standard) | `"Pérez González"` |

#### Document Claims (scope: document)

| Claim | Type | Description | Example |
|-------|------|-------------|---------|
| `pais_documento` | String | Document issuing country (ISO code) | `"UY"` |
| `tipo_documento` | String | Document type code | `"ci"`, `"dni"`, `"psp"` |
| `numero_documento` | String | Document number | `"12312314"` |

#### Email Claims (scope: email)

| Claim | Type | Description | Example |
|-------|------|-------------|---------|
| `email` | String | User's email address | `"juan.perez@example.com"` |
| `email_verified` | Boolean | Whether email has been verified | `true` or `false` |

#### Authentication Context Claims

| Claim | Type | Description | Example |
|-------|------|-------------|---------|
| `nid` | Integer | National Identity Level (0-3) | `2` |
| `rid` | String | Registration identifier | Implementation-specific |
| `ae` | String | Authentication event identifier | Implementation-specific |
| `idp` | String | Identity provider used | Implementation-specific |

### Authentication Context Class Reference (ACR) Values

ID Uruguay supports four levels of identity assurance:

| ACR Value | NID Level | Description | Use Cases |
|-----------|-----------|-------------|-----------|
| `urn:iduruguay:nid:0` | 0 | **Very Low** - Self-asserted identity | Basic service access, non-sensitive information |
| `urn:iduruguay:nid:1` | 1 | **Low** - Weak identity verification | General government services, informational queries |
| `urn:iduruguay:nid:2` | 2 | **Medium** - Moderate identity verification | Standard transactions, document access |
| `urn:iduruguay:nid:3` | 3 | **High** - Strong identity verification | Sensitive operations, legal documents, financial transactions |

**Requesting specific ACR levels**:
```
GET /oidc/v1/authorize?
  ...&acr_values=urn:iduruguay:nid:2
```

The `nid` claim in the ID Token or UserInfo response indicates the authentication level achieved.

---

## 6. Special Requirements and Considerations

### Uruguay-Specific Requirements

#### 1. Name Structure
Uruguay uses a two-surname naming system (paternal and maternal surnames). Applications must be prepared to handle:
- `primer_nombre` and `segundo_nombre` (first and middle given names)
- `primer_apellido` and `segundo_apellido` (first and second surnames)
- Both Uruguay-specific claims and standard OpenID Connect claims are provided for compatibility

#### 2. Document Types
Uruguay recognizes multiple document types:
- **CI** (Cédula de Identidad): Uruguayan national ID card
- **DNI** (Documento Nacional de Identidad): National ID from other countries
- **PSP** (Pasaporte): Passport

#### 3. Registration Process
To integrate with ID Uruguay, organizations must:
1. Complete the official registration form
2. Email to: `soporte@agesic.gub.uy` (CC: `identificacion.electronica@agesic.gub.uy`)
3. Receive client credentials (client_id and client_secret)
4. Configure redirect URIs and logout URIs

#### 4. Data Privacy and Consent
- ID Uruguay implements explicit user consent for data sharing
- Users can see which services access their information
- Granular consent per scope
- Compliance with Uruguay's Data Protection Law (Ley N° 18.331)

#### 5. Security Requirements

**HTTPS Mandatory**:
- All communications MUST use HTTPS (TLS/SSL)
- Valid certificates required for production
- Certificate pinning recommended for mobile applications

**Token Security**:
- ID Tokens are signed with RS256 or HS256
- Always validate tokens using JWKS endpoint
- Implement token expiration handling
- Store tokens securely (never in localStorage for web apps)

**State and Nonce**:
- Always use `state` parameter to prevent CSRF attacks
- Use `nonce` parameter to prevent replay attacks
- Validate these values in the callback

#### 6. Session Management
- Implement logout flow using the logout endpoint
- Clear all local session data on logout
- Support single logout if required

#### 7. Error Handling
Handle standard OAuth 2.0 / OpenID Connect errors:
- `invalid_request`: Malformed request
- `unauthorized_client`: Client not authorized
- `access_denied`: User denied consent
- `invalid_scope`: Invalid or unsupported scope
- `server_error`: Server encountered an error

### Technical Considerations

#### Token Validation
When validating ID Tokens, verify:
1. Signature using keys from JWKS endpoint
2. `iss` (issuer) matches expected value
3. `aud` (audience) contains your client_id
4. `exp` (expiration) is in the future
5. `nonce` matches the value sent in the request (if used)

#### Scope Requesting Best Practices
- Only request scopes your application actually needs
- Explain to users why specific information is needed
- More scopes = more consent friction
- `openid` scope is always required

#### Multi-tenancy Support
For systems serving multiple organizations:
- Each organization may need separate client registration
- Maintain separate client_id and client_secret per tenant
- Configure tenant-specific redirect URIs

#### Performance Optimization
- Cache JWKS responses (with appropriate TTL)
- Implement token refresh to avoid repeated authorization flows
- Use connection pooling for HTTP requests

#### Localization
- ID Uruguay interface supports Spanish
- Consider providing localized user guidance in your application

---

## 7. Test Credentials

### Testing Environment

**Base URL**: `https://auth-testing.iduruguay.gub.uy`

**Client ID**: `890192`

**Client Secret**: `457d52f181bf11804a3365b49ae4d29a2e03bbabe74997a2f510b179`

**Redirect URIs**:
- `https://openidconnect.net/callback`
- `http://localhost`
- `http://localhost:8080`

**Logout Redirect URIs**:
- `http://localhost/logout`
- `http://localhost:8080/logout`

### Testing with OpenID Connect Debugger

You can test the integration using https://openidconnect.net/:

1. **Configuration**:
   - Issuer: `https://auth-testing.iduruguay.gub.uy`
   - Discovery Endpoint: `https://auth-testing.iduruguay.gub.uy/oidc/v1/.well-known/openid-configuration`

2. **Client Configuration**:
   - Client ID: `890192`
   - Client Secret: `457d52f181bf11804a3365b49ae4d29a2e03bbabe74997a2f510b179`
   - Redirect URI: `https://openidconnect.net/callback`

3. **Request Configuration**:
   - Scope: `openid personal_info email document`
   - Response Type: `code`
   - Response Mode: `form_post` or `query`

### Test User Accounts

For test user credentials, contact AGESIC at:
- `soporte@agesic.gub.uy`
- `identificacion.electronica@agesic.gub.uy`

**Note**: The testing environment uses simulated user data. Do not use production credentials or real user data in testing.

---

## 8. Integration Guidelines

### Quick Start Integration

#### Step 1: Register Your Application

1. Complete the registration form
2. Send to AGESIC via email
3. Receive your client credentials
4. Configure your redirect URIs

#### Step 2: Implement Authorization Flow

**Authorization Request** (redirect user):
```
https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize?
  response_type=code&
  client_id=YOUR_CLIENT_ID&
  redirect_uri=YOUR_REDIRECT_URI&
  scope=openid%20personal_info%20email&
  state=RANDOM_STATE&
  nonce=RANDOM_NONCE
```

**Callback Handler** (your application):
```javascript
// Validate state parameter
if (receivedState !== sentState) {
  throw new Error('State mismatch - possible CSRF attack');
}

// Extract authorization code
const authCode = req.query.code;
```

**Token Exchange**:
```javascript
const tokenResponse = await fetch('https://auth-testing.iduruguay.gub.uy/oidc/v1/token', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded'
  },
  body: new URLSearchParams({
    grant_type: 'authorization_code',
    code: authCode,
    redirect_uri: YOUR_REDIRECT_URI,
    client_id: YOUR_CLIENT_ID,
    client_secret: YOUR_CLIENT_SECRET
  })
});

const tokens = await tokenResponse.json();
// tokens.access_token, tokens.id_token
```

**Get User Information**:
```javascript
const userInfoResponse = await fetch('https://auth-testing.iduruguay.gub.uy/oidc/v1/userinfo', {
  headers: {
    'Authorization': `Bearer ${tokens.access_token}`
  }
});

const userInfo = await userInfoResponse.json();
// userInfo.uid, userInfo.nombre_completo, userInfo.email, etc.
```

#### Step 3: Validate ID Token

```javascript
const jwt = require('jsonwebtoken');
const jwksClient = require('jwks-rsa');

const client = jwksClient({
  jwksUri: 'https://auth-testing.iduruguay.gub.uy/oidc/v1/jwks'
});

function getKey(header, callback) {
  client.getSigningKey(header.kid, function(err, key) {
    const signingKey = key.publicKey || key.rsaPublicKey;
    callback(null, signingKey);
  });
}

jwt.verify(tokens.id_token, getKey, {
  audience: YOUR_CLIENT_ID,
  issuer: 'https://auth-testing.iduruguay.gub.uy',
  algorithms: ['RS256', 'HS256']
}, (err, decoded) => {
  if (err) {
    console.error('Token validation failed:', err);
  } else {
    console.log('User ID:', decoded.uid);
    console.log('NID Level:', decoded.nid);
  }
});
```

#### Step 4: Implement Logout

```javascript
function logout(idToken, postLogoutRedirectUri) {
  const logoutUrl = new URL('https://auth-testing.iduruguay.gub.uy/oidc/v1/logout');
  logoutUrl.searchParams.append('id_token_hint', idToken);
  logoutUrl.searchParams.append('post_logout_redirect_uri', postLogoutRedirectUri);

  // Redirect user to logout URL
  window.location.href = logoutUrl.toString();
}
```

### Integration Patterns

#### Pattern 1: Web Application (Server-Side)

**Recommended for**: Traditional web applications with backend servers

**Flow**:
1. User clicks "Login with ID Uruguay"
2. Backend generates state/nonce, stores in session
3. Redirect to authorization endpoint
4. Handle callback, validate state
5. Exchange code for tokens (server-side)
6. Validate ID token
7. Create application session
8. Store user information

**Security Notes**:
- Client secret never exposed to browser
- Tokens stored server-side in session
- HTTPS required
- CSRF protection via state parameter

#### Pattern 2: Single Page Application (SPA)

**Recommended for**: React, Angular, Vue.js applications

**Considerations**:
- Use Authorization Code Flow with PKCE (if supported)
- Consider using a Backend-for-Frontend (BFF) pattern
- Never expose client_secret in browser
- Use secure token storage (memory, not localStorage)

**Note**: Check with AGESIC if PKCE is supported for public clients.

#### Pattern 3: Mobile Application

**Recommended for**: iOS, Android, React Native applications

**Flow**:
1. Use in-app browser or system browser (not WebView)
2. Implement custom URI scheme for callback
3. Use PKCE for additional security
4. Store tokens in secure storage (Keychain, Keystore)
5. Implement certificate pinning

**Example (React Native)**:
```javascript
import { authorize } from 'react-native-app-auth';

const config = {
  issuer: 'https://auth-testing.iduruguay.gub.uy',
  clientId: '890192',
  clientSecret: '457d52f181bf11804a3365b49ae4d29a2e03bbabe74997a2f510b179',
  redirectUrl: 'com.yourapp://callback',
  scopes: ['openid', 'personal_info', 'email']
};

const result = await authorize(config);
// result.accessToken, result.idToken
```

### Common Integration Scenarios

#### Scenario 1: Patient Portal (HCEN Use Case)

**Requirements**:
- High authentication level (NID 2 or 3)
- Access to full name and document information
- Email for notifications

**Configuration**:
```javascript
const authUrl = new URL('https://auth.iduruguay.gub.uy/oidc/v1/authorize');
authUrl.searchParams.append('response_type', 'code');
authUrl.searchParams.append('client_id', YOUR_CLIENT_ID);
authUrl.searchParams.append('redirect_uri', YOUR_REDIRECT_URI);
authUrl.searchParams.append('scope', 'openid personal_info email document');
authUrl.searchParams.append('acr_values', 'urn:iduruguay:nid:2');
authUrl.searchParams.append('state', generateRandomState());
authUrl.searchParams.append('nonce', generateRandomNonce());
```

**After Authentication**:
```javascript
// Verify NID level meets requirements
if (userInfo.nid < 2) {
  throw new Error('Insufficient authentication level');
}

// Store user in INUS (National User Index)
await inusService.registerOrUpdateUser({
  ci: userInfo.numero_documento,
  firstName: userInfo.primer_nombre,
  lastName: userInfo.primer_apellido,
  dateOfBirth: userInfo.date_of_birth, // if available
  email: userInfo.email
});

// Create application session
session.user = {
  ci: userInfo.numero_documento,
  fullName: userInfo.nombre_completo,
  email: userInfo.email,
  nidLevel: userInfo.nid
};
```

#### Scenario 2: Healthcare Professional Login (Peripheral Node)

**Note**: Healthcare professionals may use clinic-internal authentication, not ID Uruguay. However, if using ID Uruguay:

**Requirements**:
- Medium authentication level (NID 1-2)
- Access to professional identity
- Email for communication

**Implementation**:
```javascript
// After ID Uruguay authentication
const professionalCI = userInfo.numero_documento;

// Look up professional in clinic database
const professional = await professionalRepository.findByCI(professionalCI);

if (!professional) {
  throw new Error('Professional not registered in this clinic');
}

// Create session with professional context
session.user = {
  ci: professionalCI,
  fullName: userInfo.nombre_completo,
  role: professional.role,
  specialties: professional.specialties,
  clinicId: professional.clinicId
};

// Generate JWT for API access
const jwt = generateJWT({
  sub: professionalCI,
  name: userInfo.nombre_completo,
  role: 'PROFESSIONAL',
  specialties: professional.specialties,
  clinicId: professional.clinicId
});
```

### Error Handling Best Practices

```javascript
async function handleOAuthCallback(req, res) {
  try {
    // Check for OAuth errors
    if (req.query.error) {
      const error = req.query.error;
      const description = req.query.error_description;

      switch (error) {
        case 'access_denied':
          return res.redirect('/login?error=user_cancelled');
        case 'invalid_scope':
          logger.error('Invalid scope requested:', description);
          return res.redirect('/login?error=configuration_error');
        default:
          logger.error('OAuth error:', error, description);
          return res.redirect('/login?error=auth_failed');
      }
    }

    // Validate state
    if (req.query.state !== req.session.oauthState) {
      throw new Error('State mismatch');
    }

    // Exchange code for tokens
    const tokens = await exchangeCodeForTokens(req.query.code);

    // Validate ID token
    const idToken = await validateIdToken(tokens.id_token);

    // Get user info
    const userInfo = await getUserInfo(tokens.access_token);

    // Create session
    req.session.user = userInfo;

    res.redirect('/dashboard');

  } catch (error) {
    logger.error('OAuth callback error:', error);
    res.redirect('/login?error=auth_failed');
  }
}
```

### Testing Your Integration

#### Unit Testing

Mock ID Uruguay responses:

```javascript
// Mock token response
const mockTokenResponse = {
  access_token: 'mock_access_token',
  token_type: 'Bearer',
  expires_in: 3600,
  id_token: 'mock_id_token'
};

// Mock user info response
const mockUserInfo = {
  uid: 'uy-ci-12312314',
  nombre_completo: 'Test User',
  primer_nombre: 'Test',
  primer_apellido: 'User',
  email: 'test@example.com',
  email_verified: true,
  pais_documento: 'UY',
  tipo_documento: 'ci',
  numero_documento: '12312314',
  nid: 2
};

// Test your authentication flow
test('should authenticate user successfully', async () => {
  // Mock fetch calls
  global.fetch = jest.fn()
    .mockResolvedValueOnce({
      json: async () => mockTokenResponse
    })
    .mockResolvedValueOnce({
      json: async () => mockUserInfo
    });

  const result = await authenticateUser('mock_auth_code');

  expect(result.ci).toBe('12312314');
  expect(result.fullName).toBe('Test User');
});
```

#### Integration Testing

Test against the testing environment:

```javascript
const testConfig = {
  clientId: '890192',
  clientSecret: '457d52f181bf11804a3365b49ae4d29a2e03bbabe74997a2f510b179',
  authUrl: 'https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize',
  tokenUrl: 'https://auth-testing.iduruguay.gub.uy/oidc/v1/token',
  userinfoUrl: 'https://auth-testing.iduruguay.gub.uy/oidc/v1/userinfo',
  redirectUri: 'http://localhost:8080/callback'
};

// Use Playwright or Selenium for end-to-end tests
test('full authentication flow', async () => {
  // Navigate to your app
  await page.goto('http://localhost:8080/login');

  // Click "Login with ID Uruguay"
  await page.click('#id-uruguay-login');

  // Wait for redirect to ID Uruguay
  await page.waitForURL(/auth-testing\.iduruguay\.gub\.uy/);

  // Enter test credentials (if available)
  // ... authenticate on ID Uruguay

  // Wait for redirect back to app
  await page.waitForURL(/localhost:8080/);

  // Verify user is logged in
  const userName = await page.textContent('#user-name');
  expect(userName).toBeTruthy();
});
```

### Troubleshooting Common Issues

#### Issue 1: "invalid_client" error

**Cause**: Incorrect client_id or client_secret

**Solution**:
- Verify credentials match those provided by AGESIC
- Check for extra spaces or encoding issues
- Ensure client_id is sent correctly in token request

#### Issue 2: "redirect_uri_mismatch" error

**Cause**: Redirect URI doesn't match registered URIs

**Solution**:
- Verify exact match including protocol, domain, port, and path
- Check for trailing slashes
- Ensure URI is registered with AGESIC
- For testing, use one of the registered test URIs

#### Issue 3: State mismatch / CSRF protection

**Cause**: State parameter doesn't match

**Solution**:
- Generate unique state value for each request
- Store state in session (server-side) or sessionStorage (client-side)
- Validate state in callback before processing
- Check for session timeouts

#### Issue 4: Token validation fails

**Cause**: Invalid signature or expired token

**Solution**:
- Fetch latest JWKS from the JWKS endpoint
- Verify issuer matches expected value
- Verify audience contains your client_id
- Check token expiration (exp claim)
- Ensure system clocks are synchronized (NTP)

#### Issue 5: Missing claims in UserInfo

**Cause**: Insufficient scopes requested

**Solution**:
- Review scope mappings in Section 5
- Request all necessary scopes in authorization request
- Check user consent - user may have denied specific scopes
- Verify scopes in access token claims

### Migration from Testing to Production

When moving from testing to production:

1. **Update Endpoints**:
   - Replace `auth-testing.iduruguay.gub.uy` with `auth.iduruguay.gub.uy`

2. **Register Production Client**:
   - Request production credentials from AGESIC
   - Register production redirect URIs (must be HTTPS)

3. **Update Configuration**:
   ```javascript
   const prodConfig = {
     issuer: 'https://auth.iduruguay.gub.uy',
     clientId: 'YOUR_PROD_CLIENT_ID',
     clientSecret: 'YOUR_PROD_CLIENT_SECRET',
     redirectUri: 'https://your-app.com/callback'
   };
   ```

4. **Security Checklist**:
   - [ ] HTTPS enabled on all production endpoints
   - [ ] Valid SSL/TLS certificates installed
   - [ ] Client secret stored securely (environment variables, secrets manager)
   - [ ] State and nonce validation implemented
   - [ ] Token validation implemented (signature, expiration, audience)
   - [ ] Secure token storage
   - [ ] Logout flow implemented
   - [ ] Error handling in place
   - [ ] Logging and monitoring configured

5. **Test in Production**:
   - Verify authentication flow works
   - Test with real user accounts
   - Verify correct claims are received
   - Test logout flow
   - Monitor error rates and response times

---

## Additional Resources

### Official Documentation
- **AGESIC Wiki**: https://centroderecursos.agesic.gub.uy/web/seguridad/wiki/-/wiki/Main/ID+Uruguay+-+Integración+con+OpenID+Connect
- **Service Documentation**: Available in discovery endpoint response
- **gub.uy Portal**: https://www.gub.uy/agencia-gobierno-electronico-sociedad-informacion-conocimiento/

### OpenID Connect Standards
- **OpenID Connect Core 1.0**: https://openid.net/specs/openid-connect-core-1_0.html
- **OpenID Connect Discovery 1.0**: https://openid.net/specs/openid-connect-discovery-1_0.html
- **OAuth 2.0 (RFC 6749)**: https://tools.ietf.org/html/rfc6749

### Support and Contact
- **Technical Support**: soporte@agesic.gub.uy
- **Electronic Identification**: identificacion.electronica@agesic.gub.uy

### Testing Tools
- **OpenID Connect Debugger**: https://openidconnect.net/
- **JWT Decoder**: https://jwt.io/
- **OAuth 2.0 Playground**: https://www.oauth.com/playground/

### Libraries and SDKs

#### JavaScript/Node.js
- **openid-client**: https://github.com/panva/node-openid-client
- **passport-openidconnect**: https://github.com/jaredhanson/passport-openidconnect

#### Java
- **Nimbus OAuth 2.0 SDK**: https://connect2id.com/products/nimbus-oauth-openid-connect-sdk
- **Spring Security OAuth 2.0**: https://spring.io/projects/spring-security-oauth

#### Python
- **Authlib**: https://authlib.org/
- **PyOIDC**: https://github.com/OpenIDC/pyoidc

#### React Native
- **react-native-app-auth**: https://github.com/FormidableLabs/react-native-app-auth

---

**Document Version**: 1.0
**Last Updated**: 2025-10-14
**Maintained by**: TSE 2025 - Group 9 (German Rodao, Agustin Silvano, Piero Santos)

---

## Appendix: Complete Example Implementation

### Example: Node.js/Express Integration

```javascript
const express = require('express');
const session = require('express-session');
const { Issuer, generators } = require('openid-client');

const app = express();

// Session configuration
app.use(session({
  secret: 'your-session-secret',
  resave: false,
  saveUninitialized: false,
  cookie: { secure: true, httpOnly: true }
}));

// ID Uruguay configuration
const ID_URUGUAY_CONFIG = {
  issuer: 'https://auth-testing.iduruguay.gub.uy',
  clientId: '890192',
  clientSecret: '457d52f181bf11804a3365b49ae4d29a2e03bbabe74997a2f510b179',
  redirectUri: 'http://localhost:8080/callback'
};

let client;

// Initialize OpenID Client
async function initializeOIDC() {
  const issuer = await Issuer.discover(ID_URUGUAY_CONFIG.issuer);

  client = new issuer.Client({
    client_id: ID_URUGUAY_CONFIG.clientId,
    client_secret: ID_URUGUAY_CONFIG.clientSecret,
    redirect_uris: [ID_URUGUAY_CONFIG.redirectUri],
    response_types: ['code']
  });
}

// Login route
app.get('/login', (req, res) => {
  const state = generators.state();
  const nonce = generators.nonce();

  // Store state and nonce in session
  req.session.oauthState = state;
  req.session.oauthNonce = nonce;

  const authorizationUrl = client.authorizationUrl({
    scope: 'openid personal_info email document',
    state: state,
    nonce: nonce,
    acr_values: 'urn:iduruguay:nid:2'
  });

  res.redirect(authorizationUrl);
});

// Callback route
app.get('/callback', async (req, res) => {
  try {
    // Validate state
    if (req.query.state !== req.session.oauthState) {
      throw new Error('State mismatch');
    }

    // Exchange code for tokens
    const params = client.callbackParams(req);
    const tokenSet = await client.callback(
      ID_URUGUAY_CONFIG.redirectUri,
      params,
      {
        state: req.session.oauthState,
        nonce: req.session.oauthNonce
      }
    );

    // Get user info
    const userInfo = await client.userinfo(tokenSet.access_token);

    // Store user in session
    req.session.user = {
      ci: userInfo.numero_documento,
      fullName: userInfo.nombre_completo,
      email: userInfo.email,
      nidLevel: userInfo.nid
    };

    // Clean up OAuth state
    delete req.session.oauthState;
    delete req.session.oauthNonce;

    res.redirect('/dashboard');

  } catch (error) {
    console.error('Authentication error:', error);
    res.redirect('/login?error=auth_failed');
  }
});

// Protected route
app.get('/dashboard', (req, res) => {
  if (!req.session.user) {
    return res.redirect('/login');
  }

  res.json({
    message: 'Welcome to your dashboard',
    user: req.session.user
  });
});

// Logout route
app.get('/logout', async (req, res) => {
  const logoutUrl = client.endSessionUrl({
    post_logout_redirect_uri: 'http://localhost:8080'
  });

  req.session.destroy();
  res.redirect(logoutUrl);
});

// Start server
initializeOIDC().then(() => {
  app.listen(8080, () => {
    console.log('Server running on http://localhost:8080');
  });
});
```

This example demonstrates a complete integration with:
- OpenID Connect discovery
- Authorization code flow
- State and nonce validation
- Token exchange
- UserInfo retrieval
- Session management
- Logout flow

---

*This document provides comprehensive guidance for integrating with ID Uruguay's OpenID Connect implementation. For the most current information, always refer to the official AGESIC documentation and discovery endpoints.*
