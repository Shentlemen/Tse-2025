# SECURITY NOTE: gub.uy Weak RSA Keys

## Issue Summary

The gub.uy test environment uses **1024-bit RSA keys** for JWT signature verification, which violates [RFC 7518 Section 3.3](https://tools.ietf.org/html/rfc7518#section-3.3) security requirements that mandate **minimum 2048-bit keys** for the RS256 algorithm.

## Current Implementation (Test/Development Only)

The HCEN application has been configured to **allow weak keys for testing purposes only** through the following implementation in `GubUyOidcClient.java`:

```java
// TEST ENVIRONMENT ONLY: Allow weak keys (1024-bit RSA)
// SECURITY WARNING: This is ONLY for testing with gub.uy test environment
// Production MUST use keys >= 2048 bits per RFC 7518
private static final boolean ALLOW_WEAK_KEYS_FOR_TESTING = true;
```

### How It Works

1. **First Attempt**: The code attempts standard JWT verification with full security validation
2. **WeakKeyException Caught**: If a weak key is detected, the exception is caught
3. **Warning Logged**: A prominent security warning is logged to the console
4. **Fallback**: Token claims are parsed WITHOUT signature verification (INSECURE)

### Security Warning in Logs

When a weak key is encountered, the following warning appears in the logs:

```
╔═══════════════════════════════════════════════════════════════════════════╗
║ SECURITY WARNING: Weak RSA key detected (likely 1024-bit)                ║
║                                                                           ║
║ The gub.uy test environment is using RSA keys smaller than 2048 bits,    ║
║ which violates RFC 7518 Section 3.3 security requirements.               ║
║                                                                           ║
║ This is ONLY acceptable for TEST/DEVELOPMENT environments.               ║
║ Production deployments MUST use RSA keys >= 2048 bits.                   ║
║                                                                           ║
║ Proceeding with INSECURE signature verification for testing...           ║
╚═══════════════════════════════════════════════════════════════════════════╝
```

## Production Configuration

### ⚠️ CRITICAL: Before Deploying to Production

**IMPORTANT**: This weak key configuration is **ABSOLUTELY UNACCEPTABLE** for production environments.

Before deploying to ANTEL mi-nube or any production environment:

1. **Contact gub.uy Production Team**
   - Verify that production gub.uy environment uses RSA keys >= 2048 bits
   - Obtain production JWKS endpoint URL
   - Confirm compliance with RFC 7518 security requirements

2. **Update Configuration**
   - Set `ALLOW_WEAK_KEYS_FOR_TESTING = false` in `GubUyOidcClient.java`
   - Update OIDC configuration with production endpoints
   - Test authentication flow with production keys

3. **Verify Key Strength**
   - Monitor logs for WeakKeyException
   - If exception occurs in production, **DO NOT DEPLOY**
   - Contact gub.uy team to upgrade keys

### Code Change for Production

```java
// PRODUCTION: Enforce strong keys (>= 2048-bit RSA)
private static final boolean ALLOW_WEAK_KEYS_FOR_TESTING = false;
```

With this setting, the application will **reject weak keys** and throw an exception, preventing insecure authentication.

## Why This Matters

### Security Risks of 1024-bit RSA Keys

- **Factorization Attacks**: 1024-bit RSA keys can be factored with sufficient computational resources
- **NIST Deprecation**: NIST deprecated 1024-bit RSA keys in 2011
- **Compliance**: Violates PCI DSS, HIPAA, and other security standards
- **JWT Specification**: RFC 7518 explicitly requires >= 2048 bits for RS256

### Attack Scenarios

If weak keys are allowed in production:

1. **Token Forgery**: Attacker factors the public key and creates valid-looking tokens
2. **Identity Spoofing**: Attacker impersonates any user (including admins)
3. **Data Breach**: Unauthorized access to patient health records
4. **Compliance Violation**: Legal liability under Uruguay's Ley N° 18.331

## Testing Checklist

### Development/Test Environment ✅
- [x] `ALLOW_WEAK_KEYS_FOR_TESTING = true`
- [x] Warning logs visible in console
- [x] Authentication works with gub.uy test environment
- [x] Team aware this is insecure

### Production Environment ⚠️
- [ ] Contact gub.uy production team
- [ ] Verify production keys >= 2048 bits
- [ ] Update `ALLOW_WEAK_KEYS_FOR_TESTING = false`
- [ ] Update production OIDC endpoints
- [ ] Test authentication with production environment
- [ ] Verify NO WeakKeyException in logs
- [ ] Document key rotation procedures
- [ ] Security audit completed

## Alternative Solutions (If Production Uses Weak Keys)

If gub.uy production environment also uses weak keys (unlikely but possible):

### Option 1: Custom JWT Verification (Not Recommended)
Implement manual RSA signature verification without key size checks. **This is insecure and violates RFC 7518.**

### Option 2: Escalate to gub.uy
**RECOMMENDED**: Work with gub.uy team to upgrade to 2048-bit or 4096-bit RSA keys.

### Option 3: Use Different Identity Provider
If gub.uy cannot upgrade keys, consider alternative authentication methods (not feasible for national ID system).

## References

- [RFC 7518 Section 3.3 - Digital Signature Algorithms](https://tools.ietf.org/html/rfc7518#section-3.3)
- [NIST SP 800-57 Part 1 Rev. 5 - Key Management Recommendations](https://csrc.nist.gov/publications/detail/sp/800-57-part-1/rev-5/final)
- [AGESIC Security Guidelines for Public Sector Systems](https://www.gub.uy/agencia-gobierno-electronico-sociedad-informacion-conocimiento/)
- [Uruguay Ley N° 18.331 - Data Protection](https://www.impo.com.uy/bases/leyes/18331-2008)

## Contact

For questions or concerns about this security configuration:

- **Development Team**: TSE 2025 Group 9
  - German Rodao (4.796.608-7)
  - Agustin Silvano (5.096.964-8)
  - Piero Santos (6.614.312-9)

- **gub.uy Support**: Contact ID Uruguay technical support for production key verification

---

**Last Updated**: 2025-10-24
**Status**: Development/Test Configuration ONLY
**Risk Level**: 🔴 HIGH (if deployed to production without changes)
