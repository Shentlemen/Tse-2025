# JMS Message Queue Implementation Summary

## Overview

This document summarizes the JMS (Java Message Service) implementation for HCEN Central, enabling asynchronous message processing from peripheral nodes (clinics and health providers).

**Implementation Date**: 2025-11-13
**Component**: HCEN Central (Jakarta EE / WildFly)
**Message Broker**: ActiveMQ Artemis (embedded in WildFly)

---

## What Was Implemented

### 1. Message Queues (2)

Two JMS queues were configured to receive messages from peripheral nodes:

Run the Wildfly in admin mode using:
```bash
>./standalone.bat --server-config=standalone-full.xml --admin-only
```

Then on a separate terminal connect to it:
```bash
./jboss-cli.bat --connect
/subsystem=messaging-activemq/server=default/jms-queue=UserRegistrationQueue:add(entries=["java:/jms/queue/UserRegistration"])
/subsystem=messaging-activemq/server=default/jms-queue=DocumentRegistrationQueue:add(entries=["java:/jms/queue/DocumentRegistration"])
 ```

| Queue Name | JNDI Name | Purpose |
|------------|-----------|---------|
| `UserRegistrationQueue` | `java:/jms/queue/UserRegistration` | User creation events |
| `DocumentRegistrationQueue` | `java:/jms/queue/DocumentRegistration` | Document creation events |

### 2. Message DTOs (6 classes)
[standalone-full.xml](../../../../../../Program%20Files/wildfly-37.0.0.Final/standalone/configuration/standalone-full.xml)
**Package**: `uy.gub.hcen.messaging.dto`

- `BaseMessage` - Base class with common fields (messageId, timestamp, sourceSystem, eventType)
- `UserRegistrationPayload` - User data payload
- `UserRegistrationMessage` - Complete user registration message
- `DocumentRegistrationPayload` - Document metadata payload
- `DocumentRegistrationMessage` - Complete document registration message

All DTOs are serializable and support JSON deserialization via Jackson.

### 3. Exception Classes (3)

**Package**: `uy.gub.hcen.messaging.exception`

- `MessageProcessingException` - Base exception for message processing errors
- `InvalidMessageException` - Validation failures (permanent error)
- `DuplicateMessageException` - Duplicate message detection (idempotency)

### 4. Message Validation Service (1)

**Package**: `uy.gub.hcen.messaging.validator`

- `MessageValidator` - Centralized validation for all incoming messages
  - Validates base message fields
  - Validates user registration payload
  - Validates document registration payload
  - Checks CI format, URL format, hash format, date constraints

### 5. Message Processors (2)

**Package**: `uy.gub.hcen.messaging.processor`

- `UserRegistrationProcessor` - Business logic for user registration
  - Validates message
  - Calls `InusService.registerUser()`
  - Handles errors (transient vs permanent)
  - Supports idempotency check

- `DocumentRegistrationProcessor` - Business logic for document registration
  - Validates message
  - Calls `RndcService.registerDocument()`
  - Handles errors (transient vs permanent)
  - Supports idempotency check

### 6. Message-Driven Beans (2)

**Package**: `uy.gub.hcen.messaging.listener`

- `UserRegistrationListener` (MDB)
  - Listens to `UserRegistrationQueue`
  - Deserializes JSON messages
  - Delegates processing to `UserRegistrationProcessor`
  - Handles transaction rollback and redelivery
  - Max 10 concurrent consumers
  - Max 5 redelivery attempts with 5-second delay

- `DocumentRegistrationListener` (MDB)
  - Listens to `DocumentRegistrationQueue`
  - Deserializes JSON messages
  - Delegates processing to `DocumentRegistrationProcessor`
  - Handles transaction rollback and redelivery
  - Max 10 concurrent consumers
  - Max 5 redelivery attempts with 5-second delay

---

## Architecture

### Message Processing Flow

```
┌──────────────────┐
│ Peripheral Node  │
│ (Clinic/Provider)│
└────────┬─────────┘
         │
         │ 1. Send JSON Message
         │
         ▼
┌──────────────────────────────────────────┐
│         ActiveMQ Artemis Queue           │
│  java:/jms/queue/UserRegistration OR    │
│  java:/jms/queue/DocumentRegistration   │
└────────┬─────────────────────────────────┘
         │
         │ 2. Deliver to MDB
         │
         ▼
┌──────────────────────────────────────────┐
│  Message-Driven Bean (MDB)               │
│  - UserRegistrationListener OR           │
│  - DocumentRegistrationListener          │
│                                           │
│  ┌────────────────────────────────────┐  │
│  │ 3. Deserialize JSON → DTO          │  │
│  └────────────────────────────────────┘  │
│                                           │
│  ┌────────────────────────────────────┐  │
│  │ 4. Validate Message Structure      │  │
│  │    (MessageValidator)              │  │
│  └────────────────────────────────────┘  │
└────────┬─────────────────────────────────┘
         │
         │ 5. Delegate to Processor
         │
         ▼
┌──────────────────────────────────────────┐
│  Message Processor (Stateless EJB)       │
│  - UserRegistrationProcessor OR          │
│  - DocumentRegistrationProcessor         │
│                                           │
│  ┌────────────────────────────────────┐  │
│  │ 6. Business Logic Validation       │  │
│  └────────────────────────────────────┘  │
│                                           │
│  ┌────────────────────────────────────┐  │
│  │ 7. Call Domain Service             │  │
│  │    - InusService OR                │  │
│  │    - RndcService                   │  │
│  └────────────────────────────────────┘  │
└────────┬─────────────────────────────────┘
         │
         │ 8. Persist to Database
         │
         ▼
┌──────────────────────────────────────────┐
│  PostgreSQL Database (XA Transaction)    │
│  - inus.inus_users OR                    │
│  - rndc.rndc_documents                   │
└──────────────────────────────────────────┘
         │
         │ 9. Commit Transaction
         │
         ▼
┌──────────────────────────────────────────┐
│  Message Acknowledged & Removed from Q   │
└──────────────────────────────────────────┘

         (On Error)
         │
         │ Transaction Rollback
         │
         ▼
┌──────────────────────────────────────────┐
│  Message Redelivered (max 5 attempts)    │
│  OR Moved to DLQ (permanent errors)      │
└──────────────────────────────────────────┘
```

### Error Handling Strategy

| Error Type | Example | Action | Retry |
|------------|---------|--------|-------|
| **Validation Error** (Permanent) | Missing CI, invalid JSON | Move to DLQ | No |
| **Business Rule** (Permanent) | Invalid CI format, bad URL | Move to DLQ | No |
| **Database Error** (Transient) | Connection timeout | Redeliver | Yes (5x) |
| **Network Error** (Transient) | Service unavailable | Redeliver | Yes (5x) |

### Transaction Management

- **Type**: Container-Managed Transactions (CMT) with XA
- **Scope**: JMS message consumption + database operations
- **Atomicity**: If any operation fails, entire transaction rolls back
- **Isolation**: Read Committed (default)
- **Redelivery**: Automatic on rollback (up to 5 attempts, 5-second delay)

---

## Message Formats

### User Registration Message

```json
{
  "messageId": "msg-550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-11-13T10:30:00Z",
  "sourceSystem": "clinic-001",
  "eventType": "USER_CREATED",
  "payload": {
    "ci": "12345678",
    "firstName": "Juan",
    "lastName": "Pérez",
    "dateOfBirth": "1990-01-15",
    "email": "juan.perez@example.com",
    "phoneNumber": "099123456",
    "clinicId": "clinic-001"
  }
}
```

### Document Registration Message

```json
{
  "messageId": "msg-660e8400-e29b-41d4-a716-446655440001",
  "timestamp": "2025-11-13T10:30:00Z",
  "sourceSystem": "clinic-001",
  "eventType": "DOCUMENT_CREATED",
  "payload": {
    "patientCI": "12345678",
    "documentType": "CLINICAL_NOTE",
    "documentLocator": "https://clinic-001.hcen.uy/api/documents/doc-123",
    "documentHash": "sha256:a1b2c3d4e5f678901234567890123456789012345678901234567890123456",
    "createdBy": "doctor@clinic.com",
    "createdAt": "2025-11-13T10:30:00Z",
    "clinicId": "clinic-001",
    "documentTitle": "Consulta general",
    "documentDescription": "Consulta de control"
  }
}
```

---

## Key Features

### 1. Idempotency

Both user and document registration are idempotent:

- **User Registration**: Duplicate CI returns existing user (no error)
- **Document Registration**: Duplicate documentLocator returns existing document (no error)
- **Message ID Tracking**: Can track processed message IDs to prevent duplicate processing

### 2. Validation

Three-tier validation:

1. **Structural Validation** (MDB): JSON deserialization, message type check
2. **Format Validation** (MessageValidator): CI format, URL format, hash format, date constraints
3. **Business Validation** (Service Layer): INUS lookup, age verification, policy checks

### 3. Error Handling

- **Permanent Errors**: Move to DLQ after first attempt (validation, business rules)
- **Transient Errors**: Retry up to 5 times with 5-second delay (database, network)
- **Logging**: All errors logged with context (messageId, error type, stack trace)

### 4. Concurrency

- **Max 10 concurrent consumers per queue** (configurable via `maxSession` activation property)
- **Thread-safe processing** (stateless EJBs)
- **Connection pooling** (database, JMS)

### 5. Durability

- **Durable Queues**: Messages persisted to disk (survive server restart)
- **Journal Persistence**: ASYNCIO journal for message durability
- **XA Transactions**: Atomic message + database operations

---

## Configuration Required

### 1. WildFly JMS Queue Configuration

**File**: `standalone-full.xml`

Add to `<subsystem xmlns="urn:jboss:domain:messaging-activemq:17.0">`:

```xml
<jms-queue name="UserRegistrationQueue"
           entries="java:/jms/queue/UserRegistration">
    <durable>true</durable>
</jms-queue>

<jms-queue name="DocumentRegistrationQueue"
           entries="java:/jms/queue/DocumentRegistration">
    <durable>true</durable>
</jms-queue>

<address-setting name="jms.queue.UserRegistration"
                 dead-letter-address="jms.queue.DLQ"
                 expiry-address="jms.queue.ExpiryQueue"
                 redelivery-delay="5000"
                 max-delivery-attempts="5"
                 max-size-bytes="10485760"
                 message-counter-history-day-limit="10"/>

<address-setting name="jms.queue.DocumentRegistration"
                 dead-letter-address="jms.queue.DLQ"
                 expiry-address="jms.queue.ExpiryQueue"
                 redelivery-delay="5000"
                 max-delivery-attempts="5"
                 max-size-bytes="10485760"
                 message-counter-history-day-limit="10"/>
```

**OR** use WildFly CLI:

```bash
./bin/jboss-cli.sh --connect

/subsystem=messaging-activemq/server=default/jms-queue=UserRegistrationQueue:add(entries=["java:/jms/queue/UserRegistration"], durable=true)

/subsystem=messaging-activemq/server=default/jms-queue=DocumentRegistrationQueue:add(entries=["java:/jms/queue/DocumentRegistration"], durable=true)

/subsystem=messaging-activemq/server=default/address-setting=jms.queue.UserRegistration:add(dead-letter-address="jms.queue.DLQ", expiry-address="jms.queue.ExpiryQueue", redelivery-delay=5000, max-delivery-attempts=5, max-size-bytes=10485760)

/subsystem=messaging-activemq/server=default/address-setting=jms.queue.DocumentRegistration:add(dead-letter-address="jms.queue.DLQ", expiry-address="jms.queue.ExpiryQueue", redelivery-delay=5000, max-delivery-attempts=5, max-size-bytes=10485760)

reload
```

### 2. Dependencies

**File**: `build.gradle`

```gradle
// JMS (Java Message Service)
providedCompile 'jakarta.jms:jakarta.jms-api:3.1.0'
```

---

## Testing

### Manual Testing via WildFly CLI

#### Send Test User Registration Message

```bash
./bin/jboss-cli.sh --connect

/subsystem=messaging-activemq/server=default/jms-queue=UserRegistrationQueue:send-text-message(body='{"messageId":"test-001","timestamp":"2025-11-13T10:00:00Z","sourceSystem":"test","eventType":"USER_CREATED","payload":{"ci":"12345678","firstName":"Test","lastName":"User","dateOfBirth":"1990-01-01","email":"test@example.com","phoneNumber":"099123456","clinicId":"test-clinic"}}')
```

#### Send Test Document Registration Message

```bash
./bin/jboss-cli.sh --connect

/subsystem=messaging-activemq/server=default/jms-queue=DocumentRegistrationQueue:send-text-message(body='{"messageId":"test-002","timestamp":"2025-11-13T10:00:00Z","sourceSystem":"test","eventType":"DOCUMENT_CREATED","payload":{"patientCI":"12345678","documentType":"CLINICAL_NOTE","documentLocator":"https://test.hcen.uy/doc/123","documentHash":"sha256:0000000000000000000000000000000000000000000000000000000000000000","createdBy":"test@doctor.com","createdAt":"2025-11-13T10:00:00Z","clinicId":"test-clinic","documentTitle":"Test Document","documentDescription":"Test"}}')
```

#### View Queue Statistics

```bash
# Count messages
/subsystem=messaging-activemq/server=default/jms-queue=UserRegistrationQueue:count-messages()

# View DLQ
/subsystem=messaging-activemq/server=default/jms-queue=DLQ:count-messages()
```

### Unit Testing (Future Enhancement)

Create unit tests for:

- `MessageValidator` - All validation rules
- `UserRegistrationProcessor` - Message processing logic
- `DocumentRegistrationProcessor` - Message processing logic

Use Mockito to mock `InusService` and `RndcService`.

---

## Monitoring

### Key Metrics to Monitor

1. **Queue Depth**: Number of messages waiting in queue
2. **Message Rate**: Messages processed per second
3. **DLQ Size**: Number of failed messages
4. **Consumer Count**: Number of active MDBs
5. **Processing Time**: Average time to process message
6. **Error Rate**: Percentage of messages going to DLQ

### Monitoring via WildFly Management Console

Navigate to: **Runtime → Server → Messaging → Queues**

View:
- Message count (current, added, expired)
- Consumer count
- Message rate
- DLQ status

### Monitoring via CLI

```bash
# Queue statistics
/subsystem=messaging-activemq/server=default/jms-queue=UserRegistrationQueue:read-resource(include-runtime=true)

# DLQ messages
/subsystem=messaging-activemq/server=default/jms-queue=DLQ:list-messages()
```

---

## Deployment Checklist

- [ ] JMS queues created in WildFly (via standalone-full.xml or CLI)
- [ ] Address settings configured (redelivery, DLQ)
- [ ] WildFly using standalone-full.xml profile (required for JMS)
- [ ] Application deployed (hcen.war)
- [ ] MDBs deployed and listening (check server.log)
- [ ] Consumer count > 0 for each queue
- [ ] Test message sent and processed successfully
- [ ] DLQ monitoring configured
- [ ] Logs reviewed for errors

---

## Troubleshooting

### Problem: Messages Not Being Consumed

**Symptoms**: Queue depth increasing, consumer count = 0

**Solutions**:
1. Check MDB deployment: `./bin/jboss-cli.sh --connect --command="/deployment=hcen.war:read-resource"`
2. Verify standalone-full.xml is used (not standalone.xml)
3. Check server.log for MDB deployment errors
4. Restart WildFly

### Problem: Messages Going to DLQ Immediately

**Symptoms**: All messages end up in DLQ

**Solutions**:
1. Check message format (must be valid JSON)
2. Verify all required fields present
3. Review DLQ messages for validation errors
4. Check server.log for InvalidMessageException

### Problem: Database Errors

**Symptoms**: Messages redelivered repeatedly, database connection errors

**Solutions**:
1. Verify database is running
2. Check datasource configuration
3. Verify INUS and RNDC schemas exist
4. Check connection pool size (increase if exhausted)

---

## Future Enhancements

1. **Message Priority**: Support high-priority messages (emergency registrations)
2. **Message Expiry**: Set TTL on messages (reject stale messages)
3. **Message Filtering**: Selector-based routing (route by clinicId, documentType)
4. **Dead Letter Queue Processing**: Automated retry of DLQ messages after fixing issues
5. **Message Archival**: Archive processed messages for audit/debugging
6. **Performance Metrics**: Prometheus/Grafana integration for queue monitoring
7. **Message Replay**: Replay messages from archive for disaster recovery

---

## Files Created

### DTOs (6 files)
- `src/main/java/uy/gub/hcen/messaging/dto/BaseMessage.java`
- `src/main/java/uy/gub/hcen/messaging/dto/UserRegistrationPayload.java`
- `src/main/java/uy/gub/hcen/messaging/dto/UserRegistrationMessage.java`
- `src/main/java/uy/gub/hcen/messaging/dto/DocumentRegistrationPayload.java`
- `src/main/java/uy/gub/hcen/messaging/dto/DocumentRegistrationMessage.java`

### Exceptions (3 files)
- `src/main/java/uy/gub/hcen/messaging/exception/MessageProcessingException.java`
- `src/main/java/uy/gub/hcen/messaging/exception/InvalidMessageException.java`
- `src/main/java/uy/gub/hcen/messaging/exception/DuplicateMessageException.java`

### Validation (1 file)
- `src/main/java/uy/gub/hcen/messaging/validator/MessageValidator.java`

### Processors (2 files)
- `src/main/java/uy/gub/hcen/messaging/processor/UserRegistrationProcessor.java`
- `src/main/java/uy/gub/hcen/messaging/processor/DocumentRegistrationProcessor.java`

### MDBs (2 files)
- `src/main/java/uy/gub/hcen/messaging/listener/UserRegistrationListener.java`
- `src/main/java/uy/gub/hcen/messaging/listener/DocumentRegistrationListener.java`

### Documentation (2 files)
- `INFRASTRUCTURE_REQUIREMENTS.md` (updated with JMS section)
- `JMS_IMPLEMENTATION_SUMMARY.md` (this file)

### Build Configuration (1 file)
- `build.gradle` (added JMS dependency)

**Total**: 17 files created/modified

---

## References

- [Jakarta EE 11 - JMS Specification](https://jakarta.ee/specifications/messaging/3.1/)
- [ActiveMQ Artemis Documentation](https://activemq.apache.org/components/artemis/documentation/)
- [WildFly Messaging Configuration](https://docs.wildfly.org/31/Admin_Guide.html#Messaging)
- [Message-Driven Beans (MDB) Guide](https://jakarta.ee/specifications/enterprise-beans/4.0/jakarta-enterprise-beans-spec-core-4.0.html#a4133)

---

**Implementation Version**: 1.0
**Date**: 2025-11-13
**Author**: TSE 2025 Group 9 (via Claude Code)
