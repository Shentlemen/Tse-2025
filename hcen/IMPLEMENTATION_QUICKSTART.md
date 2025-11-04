# Policy Engine & Audit System - Quick Start Guide

**TL;DR**: Step-by-step guide to complete Policy Engine and Audit System implementation.

**Time Estimate**: 48 hours (1 week focused development)
**Current Status**: Both 60% complete (~1,300 LOC existing)
**Remaining Work**: ~1,100 LOC + integration/testing

---

## Quick Reference

### What's Already Done (60%)

**Policy Engine** (~600 LOC):
- Core `PolicyEngine` with evaluation flow
- 6 policy evaluators (DocumentType, Specialty, TimeBased, Clinic, Professional, EmergencyOverride)
- Conflict resolution (DENY > PERMIT > PENDING)
- Redis caching integration
- REST API for policy CRUD
- Database schema (policies.access_policies, policies.access_requests)

**Audit System** (~722 LOC):
- `AuditService` with comprehensive logging methods
- `AuditLog` entity with EventType/ActionOutcome enums
- Repository with custom queries
- REST API for audit queries
- Patient access history (AC026)
- Database schema (audit.audit_logs)

### What's Missing (40%)

**Policy Engine** (~400 LOC):
- RBAC integration (role-based policies)
- Emergency access workflow (patient notifications, review)
- Policy conflict analytics
- Policy versioning (change tracking)
- Batch operations (templates)

**Audit System** (~700 LOC):
- Immutability guarantees (hash chain)
- Retention enforcement (MongoDB archival)
- @Audited interceptor (automatic auditing)
- Export API (CSV/JSON)
- Advanced analytics (anomaly detection)

---

## 5-Day Implementation Sprint

### Day 1: Audit Immutability & Retention

**Time**: 9 hours

**Tasks**:
1. Add hash chain to AuditLog (2h)
   - Modify entity: add `previousHash`, `currentHash` fields
   - Create `AuditIntegrityService` for hash computation
   - Update `AuditService` to compute hashes on save

2. Implement MongoDB archival (4h)
   - Create `AuditRetentionService` with @Schedule job
   - Create `AuditLogArchiveRepository` (MongoDB)
   - Configure MongoDB connection in WildFly

3. Database migration (1h)
   - Create V007__add_audit_hash_chain.sql
   - Test migration on dev database

4. Testing (2h)
   - Unit tests for hash computation
   - Integration tests for archival
   - Test hash chain validation

**Deliverables**:
- Tamper-evident audit logs
- Automated archival after 90 days
- Integrity validation endpoint

---

### Day 2: Automatic Auditing with @Audited

**Time**: 7 hours

**Tasks**:
1. Enhance @Audited annotation (1h)
   - Add SpEL expression support
   - Add actorId/resourceId extraction parameters

2. Implement AuditedInterceptor (4h)
   - Create CDI interceptor
   - Extract audit context from method parameters
   - Handle HTTP context (IP, user agent)
   - Enable in beans.xml

3. Apply to existing services (1h)
   - Annotate RNDC methods
   - Annotate INUS methods
   - Annotate Policy methods

4. Testing (1h)
   - Test interceptor invocation
   - Verify audit logs created automatically

**Deliverables**:
- Automatic audit logging for all annotated methods
- No manual auditService.logEvent() calls needed

---

### Day 3: Emergency Access Workflow

**Time**: 8 hours

**Tasks**:
1. Create EmergencyAccessService (3h)
   - Implement logEmergencyAccess()
   - Create emergency_access_reviews table
   - Integrate with NotificationService for FCM

2. Enhance EmergencyOverridePolicyEvaluator (1h)
   - Call EmergencyAccessService on emergency access
   - Add enhanced logging

3. Patient review endpoints (2h)
   - GET /api/policies/patients/{ci}/emergency-reviews
   - POST /api/policies/emergency-reviews/{id}/confirm
   - POST /api/policies/emergency-reviews/{id}/dispute

4. Integration with mobile app (1h)
   - Send FCM notification format
   - Test mobile notification receipt

5. Testing (1h)
   - Test emergency access triggers notification
   - Test patient confirm/dispute flow

**Deliverables**:
- Complete emergency override workflow
- Patient notifications and review mechanism
- Mobile app integration ready

---

### Day 4: RBAC & Policy Versioning

**Time**: 8 hours

**Tasks**:
1. RBAC Integration (3h)
   - Create RolePolicyEvaluator
   - Add ROLE to PolicyType enum
   - Update AccessRequest DTO to include role
   - Extract role from JWT in RndcService

2. Policy Versioning (4h)
   - Create PolicyVersion entity
   - Create PolicyVersionRepository
   - Add JPA @PreUpdate listener
   - Create policy_versions table (migration)

3. REST endpoints for versioning (1h)
   - GET /api/policies/{id}/versions
   - POST /api/policies/{id}/versions/{version}/restore

**Deliverables**:
- Role-based policy evaluation
- Complete policy change history
- Rollback capability

---

### Day 5: Export, Analytics & Integration Testing

**Time**: 8 hours

**Tasks**:
1. Audit Export API (2h)
   - Create AuditExportService
   - Implement CSV streaming
   - Implement JSON streaming
   - Add REST endpoints

2. Policy Conflict Analytics (2h)
   - Create PolicyAnalyticsService
   - Implement detectPermitDenyConflicts()
   - Add REST endpoint

3. Advanced Audit Analytics (optional, 1h)
   - Create AuditAnalyticsService
   - Implement detectBulkAccess()
   - Add anomaly detection endpoint

4. Integration Testing (3h)
   - Test RNDC → PolicyEngine → AuditService flow
   - Test emergency access end-to-end
   - Test policy versioning
   - Performance testing (1000 concurrent audits)

**Deliverables**:
- Audit log export capability
- Policy conflict detection
- Complete integration test suite
- Production-ready system

---

## Quick Command Reference

### Database Migrations

```bash
# Run Flyway migrations
cd /c/Users/agust/fing/tse/tse-2025/hcen
./gradlew flywayMigrate

# Check migration status
./gradlew flywayInfo
```

### Build and Deploy

```bash
# Build WAR
./gradlew clean build

# Deploy to WildFly
./gradlew wildFlyDeploy

# Run tests
./gradlew test

# Generate coverage report
./gradlew jacocoTestReport
# Open: build/reports/jacoco/test/html/index.html
```

### Testing Specific Components

```bash
# Test Policy Engine only
./gradlew test --tests "*PolicyEngine*"

# Test Audit System only
./gradlew test --tests "*Audit*"

# Test with coverage
./gradlew test jacocoTestReport
```

### MongoDB Setup (for archival)

```bash
# Start MongoDB container
docker run -d --name hcen-mongo \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=hcen_user \
  -e MONGO_INITDB_ROOT_PASSWORD=secure_password \
  mongo:6.0

# Connect and create database
docker exec -it hcen-mongo mongosh

use hcen_audit_archive
db.createCollection("audit_logs")
db.audit_logs.createIndex({"timestamp": -1})
db.audit_logs.createIndex({"actorId": 1, "timestamp": -1})
```

### Redis Setup (for policy cache)

```bash
# Start Redis container
docker run -d --name hcen-redis \
  -p 6379:6379 \
  redis:6.2 \
  --requirepass secure_password

# Connect and test
docker exec -it hcen-redis redis-cli
AUTH secure_password
PING
```

---

## File Checklist

### New Files to Create

**Policy Engine** (7 files):
- [ ] `service/policy/evaluator/RolePolicyEvaluator.java` (100 LOC)
- [ ] `policy/service/EmergencyAccessService.java` (150 LOC)
- [ ] `policy/entity/EmergencyAccessReview.java` (80 LOC)
- [ ] `policy/service/PolicyAnalyticsService.java` (120 LOC)
- [ ] `policy/dto/PolicyConflictReport.java` (60 LOC)
- [ ] `policy/entity/PolicyVersion.java` (80 LOC)
- [ ] `policy/repository/PolicyVersionRepository.java` (60 LOC)

**Audit System** (8 files):
- [ ] `service/audit/AuditIntegrityService.java` (150 LOC)
- [ ] `service/audit/dto/AuditIntegrityReport.java` (50 LOC)
- [ ] `service/audit/AuditRetentionService.java` (150 LOC)
- [ ] `audit/repository/AuditLogArchiveRepository.java` (100 LOC)
- [ ] `service/audit/interceptor/AuditedInterceptor.java` (200 LOC)
- [ ] `service/audit/AuditExportService.java` (120 LOC)
- [ ] `service/audit/dto/AuditExportRequest.java` (50 LOC)
- [ ] `service/audit/AuditAnalyticsService.java` (150 LOC)

**Database** (1 file):
- [ ] `resources/db/migration/V007__enhance_policies_and_audit.sql`

### Files to Modify

**Policy Engine**:
- [ ] `service/policy/PolicyEngine.java` (register new evaluators)
- [ ] `policy/entity/AccessPolicy.java` (add ROLE to PolicyType enum)
- [ ] `service/policy/dto/AccessRequest.java` (add role field)
- [ ] `policy/api/rest/PolicyResource.java` (add new endpoints)

**Audit System**:
- [ ] `audit/entity/AuditLog.java` (add previousHash, currentHash fields)
- [ ] `service/audit/AuditService.java` (compute hashes on save)
- [ ] `audit/api/rest/AuditResource.java` (add export/analytics endpoints)

**Configuration**:
- [ ] `webapp/WEB-INF/beans.xml` (enable AuditedInterceptor)
- [ ] `resources/application.properties` (MongoDB/Redis URIs)

**Integration**:
- [ ] `rndc/service/RndcService.java` (policy enforcement + @Audited)
- [ ] `inus/service/InusService.java` (@Audited annotations)

---

## Testing Checklist

### Unit Tests

**Policy Engine**:
- [ ] PolicyEngine conflict resolution (DENY > PERMIT > PENDING)
- [ ] RolePolicyEvaluator hierarchy evaluation
- [ ] PolicyAnalyticsService conflict detection
- [ ] PolicyVersioning listener

**Audit System**:
- [ ] AuditIntegrityService hash computation
- [ ] AuditIntegrityService chain validation
- [ ] AuditIntegrityService tamper detection
- [ ] AuditRetentionService archival logic
- [ ] AuditedInterceptor parameter extraction
- [ ] AuditExportService CSV formatting

### Integration Tests

- [ ] RNDC document access → policy evaluation → audit log
- [ ] Emergency access → notification → patient review
- [ ] Policy update → versioning → audit
- [ ] Audit archival → MongoDB → retrieval
- [ ] @Audited annotation → automatic audit log creation
- [ ] Export large audit log set (streaming)

### Performance Tests

- [ ] 1000 concurrent audit log writes (< 5 seconds)
- [ ] Policy evaluation time (< 100ms average)
- [ ] Cache hit rate (> 80%)
- [ ] Audit export 100K records (streaming, no memory overflow)

### Coverage Goals

- [ ] Overall: 80%+
- [ ] Policy Engine: 90%+
- [ ] Audit System: 85%+

---

## Common Issues and Solutions

### Issue: Hash Chain Breaks After Database Restore

**Symptom**: AuditIntegrityService reports violations after restoring from backup

**Solution**: Recalculate hash chain from genesis entry
```java
// Run once after restore
auditIntegrityService.rebuildHashChain();
```

### Issue: MongoDB Archival Job Fails

**Symptom**: Old audit logs remain in PostgreSQL after 90 days

**Solution**: Check MongoDB connection and verify collection exists
```bash
# Test MongoDB connection
docker exec -it hcen-mongo mongosh
use hcen_audit_archive
db.audit_logs.count()
```

### Issue: @Audited Interceptor Not Invoked

**Symptom**: Methods annotated with @Audited don't create audit logs

**Solution**: Ensure beans.xml has correct interceptor configuration
```xml
<beans bean-discovery-mode="all">
    <interceptors>
        <class>uy.gub.hcen.service.audit.interceptor.AuditedInterceptor</class>
    </interceptors>
</beans>
```

### Issue: Emergency Access Notifications Not Received

**Symptom**: Patient doesn't get mobile notification for emergency access

**Solution**: Verify FCM token is registered and valid
```sql
SELECT fcm_token FROM notification_preferences WHERE user_ci = '12345678';
```

---

## Success Criteria

### Policy Engine

- [x] All 6 existing evaluators working
- [ ] RBAC evaluator implemented and tested
- [ ] Emergency access workflow complete with notifications
- [ ] Policy versioning tracks all changes
- [ ] Conflict detection identifies overlapping policies
- [ ] Evaluation time < 100ms (average)
- [ ] Cache hit rate > 80%
- [ ] 90%+ code coverage

### Audit System

- [x] Basic audit logging working
- [ ] Hash chain ensures immutability
- [ ] Integrity validation detects tampering
- [ ] MongoDB archival runs daily
- [ ] @Audited interceptor automates logging
- [ ] Export API streams large datasets
- [ ] Analytics detects anomalies
- [ ] 1000+ events/second write throughput
- [ ] 85%+ code coverage

### Integration

- [ ] All RNDC operations enforce policies
- [ ] All critical operations automatically audited
- [ ] Mobile app receives emergency access notifications
- [ ] Patients can view complete access history
- [ ] Admin can export audit logs for compliance
- [ ] End-to-end tests pass for all workflows

---

## Next Steps After Completion

1. **Documentation**:
   - Update README.md with Policy Engine usage
   - Update CLAUDE.md with new features completed
   - Update TODO.md with implementation details

2. **Deployment**:
   - Deploy to staging environment
   - Run smoke tests
   - Performance testing under load
   - Deploy to production

3. **Monitoring**:
   - Set up Prometheus metrics for audit write rate
   - Set up alerts for integrity violations
   - Monitor MongoDB archival job success rate
   - Track policy evaluation latency

4. **User Training**:
   - Document patient policy management workflows
   - Document admin audit query procedures
   - Create video tutorials for mobile app

---

## Resources

- **Full Implementation Plan**: `POLICY_AUDIT_IMPLEMENTATION_PLAN.md` (1,100+ lines)
- **Architecture Document**: `docs/arquitectura-grupo9-tse.pdf`
- **Project README**: `README.md`
- **TODO Tracking**: `../TODO.md`

---

**Ready to Start?** Begin with Day 1 tasks and follow the 5-day sprint!
