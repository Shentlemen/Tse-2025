# JMS Error Fix Summary

## Problem

When prestador application tried to send JMS messages to HCEN, it failed with:
```
javax.naming.NameNotFoundException: comp -- service jboss.naming.context.java.jboss.exported.comp
```

## Root Cause

**Primary Issue**: Using `@Inject @JMSConnectionFactory` for JMSContext injection causes WildFly to create a context that uses **remote naming client** for transaction synchronization. When `createProducer()` is called, it attempts to look up `TransactionSynchronizationRegistry` via remote JNDI (`java:jboss/exported/comp`), which doesn't exist.

**Evidence**: The error stacktrace shows:
```
at org.wildfly.naming-client@2.0.1.Final//org.wildfly.naming.client.remote.RemoteServerTransport.handleLookup
```

This indicates the JMS context is trying to use remote naming instead of local naming, even though both apps are on the same server.

## Solution Applied

### 1. Changed Injection Mechanism
- **Before**: `@Inject @JMSConnectionFactory("java:/ConnectionFactory") JMSContext jmsContext`
- **After**: `@Resource(lookup = "java:/ConnectionFactory") ConnectionFactory connectionFactory`
- **Why**: `@Resource` injection uses local JNDI without remote naming client issues

### 2. Create JMSContext Per Method Call
- **Before**: Injected JMSContext reused across all calls
- **After**: `try (JMSContext context = connectionFactory.createContext()) { ... }`
- **Why**: Explicitly create context to avoid CDI remote naming issues

### 3. Changed EJB Type
- **Before**: `@Singleton`
- **After**: `@Stateless`
- **Why**: Better transaction support for JMS operations

### 4. Fixed Queue JNDI Names
- **Before**: `java:jboss/exported/jms/queue/DocumentRegistration` (for remote clients)
- **After**: `java:/jms/queue/DocumentRegistration` (for same-server messaging)
- **Why**: Both prestador and HCEN run on the same WildFly instance, so use local JNDI

### 3. Updated WildFly Configuration
Added local JNDI entries to queues in standalone-full.xml.

## Implementation Steps

### Step 1: Rebuild prestador application
```bash
cd C:\Users\agust\fing\tse\tse-2025\prestador_de_salud\prestador
mvn clean package
```

### Step 2: Update WildFly queue configuration
Run the CLI script to add local JNDI entries:
```bash
cd C:\wildfly\bin
jboss-cli.bat --file=C:\Users\agust\fing\tse\tse-2025\prestador_de_salud\add-local-jndi-queues.cli
```

This script will:
- Add `java:/jms/queue/UserRegistration` entry to UserRegistrationQueue
- Add `java:/jms/queue/DocumentRegistration` entry to DocumentRegistrationQueue
- Reload WildFly

### Step 3: Redeploy prestador
```bash
# Copy the new WAR to WildFly
copy C:\Users\agust\fing\tse\tse-2025\prestador_de_salud\prestador\target\prestador.war C:\wildfly\standalone\deployments\
```

Or use WildFly CLI:
```bash
cd C:\wildfly\bin
jboss-cli.bat --connect
deploy --force C:\Users\agust\fing\tse\tse-2025\prestador_de_salud\prestador\target\prestador.war
```

### Step 4: Test
Send a POST request to create a patient:
```bash
curl -X POST http://localhost:8080/prestador/api/patients \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test",
    "lastName": "Patient",
    "documentNumber": "12345678",
    "birthDate": "1990-01-01",
    "gender": "M",
    "clinicId": 1
  }'
```

Check the logs - you should see:
```
INFO  [com.prestador.servlet.PatientServlet] Patient created successfully
INFO  [com.prestador.messaging.HcenMessageSender] Sent FHIR patient registration to HCEN
```

And in HCEN logs:
```
INFO  [uy.gub.hcen.messaging.listener.UserRegistrationListener] Received user registration message
INFO  [uy.gub.hcen.messaging.listener.UserRegistrationListener] User registration completed successfully
```

## Technical Details

### JNDI Name Comparison

| JNDI Name | Purpose | Use Case |
|-----------|---------|----------|
| `java:/jms/queue/...` | Local access within WildFly | Same-server apps (prestador → HCEN) ✓ |
| `java:jboss/exported/jms/queue/...` | Remote access | External clients connecting via remote protocols ❌ |
| `jms/queue/...` | Portable JNDI | Works but less explicit |

### Connection Factory

- **Used**: `java:/ConnectionFactory` (in-VM connector)
- **Correct for**: Same-server messaging
- **Fast**: No network overhead, uses in-VM transport

### Transaction Behavior

With `@Stateless` EJB:
- Container-Managed Transactions (CMT) enabled by default
- JMS send is automatically transactional
- If servlet method fails, JMS message is rolled back
- Message won't be sent if patient creation fails

## Files Changed

1. **HcenMessageSender.java**
   - Line 261: Changed `@Singleton` to `@Stateless`
   - Line 298: Changed to `java:/jms/queue/UserRegistration`
   - Line 301: Changed to `java:/jms/queue/DocumentRegistration`

2. **standalone-full.xml** (via CLI script)
   - Added `java:/jms/queue/UserRegistration` to UserRegistrationQueue entries
   - Added `java:/jms/queue/DocumentRegistration` to DocumentRegistrationQueue entries

## Expected Queue Configuration After Fix

```xml
<jms-queue name="UserRegistrationQueue"
           entries="java:/jms/queue/UserRegistration
                    java:jboss/exported/jms/queue/UserRegistration
                    jms/queue/UserRegistration"/>

<jms-queue name="DocumentRegistrationQueue"
           entries="java:/jms/queue/DocumentRegistration
                    java:jboss/exported/jms/queue/DocumentRegistration
                    jms/queue/DocumentRegistration"/>
```

## Verification

After applying the fix, verify:

1. **Prestador sends messages successfully**
   ```
   grep "Sent FHIR patient registration" C:\wildfly\standalone\log\server.log
   ```

2. **HCEN receives and processes messages**
   ```
   grep "User registration completed successfully" C:\wildfly\standalone\log\server.log
   ```

3. **No more "comp" namespace errors**
   ```
   grep "NameNotFoundException: comp" C:\wildfly\standalone\log\server.log
   # Should return nothing
   ```

## Rollback (if needed)

If you need to rollback:
1. Change `@Stateless` back to `@Singleton` in HcenMessageSender.java
2. Change queue lookups back to `java:jboss/exported/...`
3. Rebuild and redeploy

But the fix is recommended and follows Jakarta EE best practices.
