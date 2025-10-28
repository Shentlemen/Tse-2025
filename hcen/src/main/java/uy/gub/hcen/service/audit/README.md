# Audit Service - Comprehensive Audit Logging

## Overview

The **AuditService** provides comprehensive, immutable audit logging for all HCEN system operations. This implementation fulfills compliance requirement **AC026** (patients can view who accessed their records and when) and supports Uruguayan data protection law (Ley N° 18.331).

## Architecture

### Components

```
service/audit/
├── AuditService.java              # Main service with logging and query methods
├── dto/
│   ├── AuditEventRequest.java     # DTO for programmatic audit logging
│   └── AuditEventBuilder.java     # Fluent builder pattern for audit events
├── annotation/
│   └── Audited.java               # Annotation for AOP-based audit logging (future)
├── package-info.java              # Package documentation with examples
└── README.md                      # This file
```

### Key Features

- **Immutable Audit Trail**: Append-only logging (no updates/deletes)
- **Fail-Safe Design**: Never throws exceptions to business logic
- **Comprehensive Logging**: Who, what, when, where, how, and why
- **Flexible Querying**: Patient access history, advanced search, statistics
- **JSON Context Storage**: Flexible details in PostgreSQL JSONB column
- **Builder Pattern**: Clean, readable API for complex audit events
- **Compliance Ready**: AC026, Ley 18.331, AGESIC guidelines

## Usage Examples

### 1. Log Document Access

```java
@Inject
private AuditService auditService;

public RndcDocument getDocument(Long documentId, String professionalId, String patientCi) {
    RndcDocument document = documentRepository.findById(documentId);

    // Check authorization (policy engine)
    Decision decision = policyEngine.evaluate(...);

    ActionOutcome outcome = (decision == Decision.PERMIT)
        ? ActionOutcome.SUCCESS
        : ActionOutcome.DENIED;

    // Log the access attempt
    auditService.logDocumentAccess(
        professionalId,
        patientCi,
        documentId,
        document.getDocumentType(),
        outcome,
        request.getRemoteAddr(),
        request.getHeader("User-Agent")
    );

    if (outcome == ActionOutcome.DENIED) {
        throw new AccessDeniedException("Access denied");
    }

    return document;
}
```

### 2. Log Authentication Event

```java
public void handleCallback(String authorizationCode, HttpServletRequest request) {
    try {
        UserClaims claims = oidcService.exchangeCode(authorizationCode);

        // Log successful authentication
        auditService.logAuthenticationEvent(
            claims.getCi(),
            ActionOutcome.SUCCESS,
            request.getRemoteAddr(),
            request.getHeader("User-Agent"),
            Map.of(
                "method", "gub.uy",
                "provider", "oidc",
                "timestamp", LocalDateTime.now().toString()
            )
        );

    } catch (AuthenticationException e) {
        // Log failed authentication
        auditService.logAuthenticationEvent(
            "unknown",
            ActionOutcome.FAILURE,
            request.getRemoteAddr(),
            request.getHeader("User-Agent"),
            Map.of(
                "error", e.getMessage(),
                "errorType", e.getClass().getSimpleName()
            )
        );
        throw e;
    }
}
```

### 3. Using Builder Pattern

```java
AuditEventRequest event = AuditEventBuilder.builder()
    .access()                              // eventType = ACCESS
    .professional("prof-123")              // actorId + actorType
    .resourceType("DOCUMENT")
    .resourceId("456")
    .success()                             // outcome = SUCCESS
    .ipAddress("192.168.1.100")
    .userAgent("Mozilla/5.0...")
    .detail("patientCi", "12345678")
    .detail("documentType", "LAB_RESULT")
    .detail("clinicId", "clinic-001")
    .build();

auditService.logEvent(event);
```

### 4. Log Policy Change

```java
public void updatePolicy(String patientCi, Long policyId, PolicyDTO policyData,
                         HttpServletRequest request) {

    AccessPolicy oldPolicy = policyRepository.findById(policyId);

    // Update the policy
    policyRepository.update(policyId, policyData);

    // Log the policy change
    auditService.logPolicyChange(
        patientCi,
        policyId,
        "UPDATE",
        request.getRemoteAddr(),
        request.getHeader("User-Agent")
    );
}
```

### 5. Query Patient Access History (CU05 - AC026)

```java
@GET
@Path("/patients/{ci}/access-history")
@Produces(MediaType.APPLICATION_JSON)
public Response getPatientAccessHistory(
        @PathParam("ci") String patientCi,
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("20") int size) {

    // Get all access events for this patient's documents
    List<AuditLog> history = auditService.getPatientAccessHistory(patientCi, page, size);

    return Response.ok(history).build();
}
```

### 6. Advanced Search

```java
// Search all failed access attempts in the last 24 hours
LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
LocalDateTime now = LocalDateTime.now();

List<AuditLog> failedAccess = auditService.searchAuditLogs(
    EventType.ACCESS,          // event type
    null,                      // any actor
    "DOCUMENT",                // only documents
    yesterday,                 // from
    now,                       // to
    ActionOutcome.DENIED,      // only denied attempts
    0,                         // page
    50                         // size
);

// Analyze security incidents
failedAccess.forEach(log -> {
    String actorId = log.getActorId();
    String resourceId = log.getResourceId();
    String details = log.getDetails(); // JSON with context
    // Alert security team if too many failures...
});
```

### 7. Statistics for Admin Dashboard

```java
// Get statistics for admin dashboard
long totalAccess = auditService.countEventsByType(EventType.ACCESS);
long successfulAccess = auditService.countEventsByOutcome(ActionOutcome.SUCCESS);
long deniedAccess = auditService.countEventsByOutcome(ActionOutcome.DENIED);

double denialRate = (double) deniedAccess / totalAccess * 100;

Map<String, Object> stats = Map.of(
    "totalAccess", totalAccess,
    "successfulAccess", successfulAccess,
    "deniedAccess", deniedAccess,
    "denialRate", denialRate
);
```

## Event Types

| Event Type | Description | Use Case |
|------------|-------------|----------|
| `ACCESS` | Resource access by user | Professional views document |
| `CREATION` | New resource creation | New document registered in RNDC |
| `MODIFICATION` | Resource modification | Policy updated |
| `DELETION` | Resource deletion | Document marked as inactive |
| `POLICY_CHANGE` | Access policy change | Patient modifies access rules |
| `ACCESS_REQUEST` | Professional requests access | Access requires patient approval |
| `ACCESS_APPROVAL` | Patient approves access | Patient grants permission |
| `ACCESS_DENIAL` | Patient denies access | Patient rejects permission |
| `AUTHENTICATION_SUCCESS` | Successful login | User logs in via gub.uy |
| `AUTHENTICATION_FAILURE` | Failed login attempt | Invalid credentials |

## Action Outcomes

| Outcome | Description | Use Case |
|---------|-------------|----------|
| `SUCCESS` | Action completed successfully | Document accessed, policy updated |
| `FAILURE` | Action failed due to technical error | Database error, network timeout |
| `DENIED` | Action denied due to policy/authorization | Access policy blocks professional |

## Actor Types

| Actor Type | Description | Example ID |
|------------|-------------|------------|
| `PATIENT` | Patient/citizen | CI: "12345678" |
| `PROFESSIONAL` | Healthcare professional | ID: "prof-123" |
| `ADMIN` | System administrator | ID: "admin-456" |
| `SYSTEM` | Automated system action | "system" |

## Service Methods

### Core Logging

```java
// Primary method - all others delegate to this
void logEvent(EventType, actorId, actorType, resourceType, resourceId,
              outcome, ipAddress, userAgent, details)

// Using DTO
void logEvent(AuditEventRequest request)
```

### Convenience Methods

```java
// Event type shortcuts
void logAccessEvent(...)
void logCreationEvent(...)
void logModificationEvent(...)
void logDeletionEvent(...)

// Domain-specific shortcuts
void logAuthenticationEvent(actorId, outcome, ipAddress, userAgent, details)
void logPolicyChange(patientCi, policyId, action, ipAddress, userAgent)
void logDocumentAccess(professionalId, patientCi, documentId, documentType,
                       outcome, ipAddress, userAgent)
void logDocumentRegistration(clinicId, documentId, patientCi, documentType, createdBy)
void logAccessRequest(professionalId, patientCi, documentId, ipAddress, userAgent)
void logAccessApproval(patientCi, professionalId, documentId, ipAddress, userAgent)
void logAccessDenial(patientCi, professionalId, documentId, ipAddress, userAgent)
```

### Query Methods

```java
// Patient access history (AC026)
List<AuditLog> getPatientAccessHistory(patientCi, page, size)

// Advanced search
List<AuditLog> searchAuditLogs(eventType, actorId, resourceType,
                               fromDate, toDate, outcome, page, size)

// Specific queries
List<AuditLog> getAuditLogsByActor(actorId, page, size)
List<AuditLog> getAuditLogsByResource(resourceType, resourceId, page, size)
List<AuditLog> getAuditLogsByEventType(eventType, page, size)
List<AuditLog> getAuditLogsByDateRange(fromDate, toDate, page, size)
```

### Statistics

```java
long countEventsByType(EventType eventType)
long countEventsByOutcome(ActionOutcome outcome)
long countEventsByActor(String actorId)
long getTotalAuditLogCount()
```

## Design Principles

### 1. Never Throw Exceptions

The AuditService **never throws exceptions** to business logic. Audit failures should not break application functionality.

```java
try {
    auditLogRepository.save(auditLog);
    LOGGER.info("Audit event logged successfully");
} catch (Exception e) {
    // Log error but DON'T throw - audit failures should not break business logic
    LOGGER.severe("Failed to save audit log: " + e.getMessage());
}
```

### 2. Fail Gracefully

If required parameters are missing, log a warning and return (don't throw):

```java
if (eventType == null || actorId == null || ...) {
    LOGGER.warning("Missing required audit parameters - event not logged");
    return; // Fail gracefully
}
```

### 3. Immutable Audit Trail

The AuditLog entity is immutable (no setters). The service uses the constructor:

```java
AuditLog auditLog = new AuditLog(
    eventType, actorId, actorType, resourceType, resourceId,
    outcome, ipAddress, userAgent, detailsJson
);
```

No update or delete operations are supported - only `save()` and query methods.

### 4. JSON Context Storage

Additional context is stored as JSON in PostgreSQL JSONB column:

```java
Map<String, Object> details = Map.of(
    "patientCi", "12345678",
    "documentType", "LAB_RESULT",
    "clinicId", "clinic-001",
    "reason", "Emergency access"
);

// Serialized to: {"patientCi":"12345678","documentType":"LAB_RESULT",...}
```

### 5. Privacy by Design

**Never log sensitive data**:
- ❌ Passwords, tokens, API keys
- ❌ Full CI in plaintext (hash or mask if needed)
- ❌ Document content
- ✅ Document IDs, types, metadata
- ✅ IP addresses, user agents
- ✅ Outcomes, timestamps

### 6. Pagination

All query methods support pagination (max 100 per page):

```java
List<AuditLog> history = auditService.getPatientAccessHistory("12345678", 0, 20);
// Returns max 20 results from page 0
```

## Compliance

### AC026 - Patient Access Visibility

The `getPatientAccessHistory()` method implements AC026:

> "Patients can view who accessed their records and when"

```java
// Patient portal endpoint
@GET
@Path("/my-access-history")
public Response getMyAccessHistory(@QueryParam("page") int page) {
    String patientCi = securityContext.getUserPrincipal().getName();
    List<AuditLog> history = auditService.getPatientAccessHistory(patientCi, page, 20);
    return Response.ok(history).build();
}
```

### Ley N° 18.331 (Data Protection Law of Uruguay)

The audit system supports:
- **Right to Information**: Patients can see who accessed their data
- **Accountability**: Immutable audit trail for investigations
- **Traceability**: All actions tracked with actor, timestamp, outcome
- **Data Minimization**: Only necessary information logged

### AGESIC Guidelines

Complies with AGESIC requirements for public sector systems:
- Comprehensive event logging
- Immutable audit trails
- Secure storage (PostgreSQL with access controls)
- Retention policy support (90 days + archival)

## Testing

Example unit test:

```java
@Test
void testLogDocumentAccess() {
    // Given
    String professionalId = "prof-123";
    String patientCi = "12345678";
    Long documentId = 456L;
    DocumentType documentType = DocumentType.LAB_RESULT;

    // When
    auditService.logDocumentAccess(
        professionalId, patientCi, documentId, documentType,
        ActionOutcome.SUCCESS, "192.168.1.100", "Mozilla/5.0"
    );

    // Then
    verify(auditLogRepository).save(any(AuditLog.class));
}
```

Example integration test:

```java
@Test
void testGetPatientAccessHistory() {
    // Given
    String patientCi = "12345678";
    createTestAuditLogs(patientCi);

    // When
    List<AuditLog> history = auditService.getPatientAccessHistory(patientCi, 0, 20);

    // Then
    assertThat(history).isNotEmpty();
    assertThat(history).allMatch(log ->
        log.getEventType() == EventType.ACCESS &&
        log.getDetails().contains(patientCi)
    );
}
```

## Future Enhancements

### 1. AOP-Based Logging (Optional)

The `@Audited` annotation is included for future AOP implementation:

```java
@Stateless
public class DocumentService {

    @Audited(eventType = EventType.ACCESS, resourceType = "DOCUMENT")
    public RndcDocument getDocument(Long documentId) {
        // AuditInterceptor automatically logs this access
        return documentRepository.findById(documentId);
    }
}
```

Requires implementing `AuditInterceptor` with `@AroundInvoke`.

### 2. Async Logging

For high-traffic operations, implement async logging:

```java
@Asynchronous
public void logEventAsync(AuditEventRequest request) {
    logEvent(request);
}
```

### 3. Archival to MongoDB

After 90 days in PostgreSQL, archive to MongoDB for long-term storage:

```java
@Schedule(hour = "2", minute = "0") // Daily at 2 AM
public void archiveOldAuditLogs() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
    List<AuditLog> oldLogs = auditLogRepository.findOlderThan(cutoff);

    // Archive to MongoDB
    mongoAuditService.archive(oldLogs);

    // Mark as archived (don't delete - immutable trail)
    auditLogRepository.markAsArchived(oldLogs);
}
```

### 4. Real-Time Alerts

Implement security alerts for suspicious patterns:

```java
@Schedule(minute = "*/5") // Every 5 minutes
public void detectSuspiciousActivity() {
    LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

    // Find repeated failures by same actor
    List<AuditLog> failures = searchAuditLogs(
        null, null, null, fiveMinutesAgo, LocalDateTime.now(),
        ActionOutcome.DENIED, 0, 1000
    );

    // Group by actor and count
    Map<String, Long> failuresByActor = failures.stream()
        .collect(Collectors.groupingBy(AuditLog::getActorId, Collectors.counting()));

    // Alert if > 10 failures in 5 minutes
    failuresByActor.entrySet().stream()
        .filter(e -> e.getValue() > 10)
        .forEach(e -> securityAlertService.alert(
            "Multiple access denials for actor: " + e.getKey()
        ));
}
```

## Dependencies

- **Jakarta EE 11**: EJB, CDI, JPA
- **PostgreSQL**: JSONB column for details
- **Jackson**: JSON serialization (`com.fasterxml.jackson.core:jackson-databind`)
- **AuditLog Entity**: `uy.gub.hcen.audit.entity.AuditLog`
- **AuditLogRepository**: `uy.gub.hcen.audit.repository.AuditLogRepository`

## File Locations

```
C:\Users\agust\fing\tse\tse-2025\hcen\src\main\java\uy\gub\hcen\service\audit\
├── AuditService.java
├── dto\
│   ├── AuditEventRequest.java
│   └── AuditEventBuilder.java
├── annotation\
│   └── Audited.java
├── package-info.java
└── README.md
```

## Support

For questions or issues:
- **Team**: TSE 2025 Group 9
- **Authors**: German Rodao, Agustin Silvano, Piero Santos
- **Documentation**: See `package-info.java` for more examples

---

**Version**: 1.0
**Last Updated**: 2025-10-22
**Status**: Production Ready
