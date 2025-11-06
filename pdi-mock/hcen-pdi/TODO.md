# PDI Mock Service - Implementation TODO

## Project Overview
**Component**: PDI (Plataforma de Interoperabilidad) Mock Service
**Purpose**: Simulate DNIC "Servicio Básico de Información" for patient verification
**Protocol**: SOAP Web Services over HTTPS
**Technology Stack**: Jakarta EE (JAX-WS), WildFly, Gradle

---

## Implementation Checklist

### Phase 1: Project Setup and Configuration
- [ ] **1.1** Review existing Gradle configuration (`build.gradle`)
  - Verify JAX-WS dependencies
  - Add SOAP/JAXB dependencies if missing
  - Configure WildFly plugin

- [ ] **1.2** Create project structure
  ```
  src/main/
    ├── java/uy/gub/dnic/pdi/
    │   ├── model/          # Data models (Person, Message, etc.)
    │   ├── service/        # SOAP service implementation
    │   ├── repository/     # Mock data repository
    │   └── exception/      # Custom exceptions
    ├── resources/
    │   ├── wsdl/           # WSDL definitions (if needed)
    │   └── mock-data.json  # Simulated citizen data
    └── webapp/
        └── WEB-INF/
            └── web.xml
  ```

- [ ] **1.3** Configure WildFly for SOAP services
  - Set up SOAP endpoint URL
  - Configure HTTPS/SSL certificates (mandatory per AC002-AC004)

---

### Phase 2: Data Models and DTOs

#### 2.1 Create Request/Response Data Structures

- [ ] **2.1.1** Create `ParamObtPersonaPorDoc` (Request DTO)
  ```java
  - organizacion: String (required)
  - passwordEntidad: String (required)
  - NroDocumento: String (required, no formatting)
  - TipoDocumento: String (default: "DO")
  ```

- [ ] **2.1.2** Create `ObjPersona` (Person Data DTO)
  ```java
  - codTipoDocumento: String
  - nroDocumento: String
  - nombre1: String (first name)
  - nombre2: String (second name)
  - Apellido1: String (first last name)
  - Apellido2: String (second last name)
  - ApellidoAdoptivo1: String
  - ApellidoAdoptivo2: String
  - sexo: Integer (1=male, 2=female)
  - fechaNacimiento: String (format: "yyyy-mmdd")
  - codNacionalidad: Integer (1=Uruguayan, 3=Foreign, 0=Unknown)
  - nombreEnCedula: String
  ```

- [ ] **2.1.3** Create `Mensaje` (Error/Warning Message DTO)
  ```java
  - CodMensaje: Integer
  - Descripcion: String
  - DatoExtra: String
  ```

- [ ] **2.1.4** Create `ResultObtPersonaPorDoc` (Response DTO)
  ```java
  - objPersona: ObjPersona
  - Warnings: List<Mensaje>
  - Errores: List<Mensaje>
  ```

- [ ] **2.1.5** Create `ObtProductInfo` (Service Info DTO)
  ```java
  - version: String
  - modalidad: String ("Testing" or "Producción")
  - descripcion: String
  ```

- [ ] **2.1.6** Create `ResultObtProductDesc` (Product Description Response)
  ```java
  - obtProductInfo: ObtProductInfo
  ```

---

### Phase 3: SOAP Service Implementation

#### 3.1 Core Service Interface

- [ ] **3.1.1** Create `@WebService` interface `PDIService`
  ```java
  @WebService
  @SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL)
  public interface PDIService {
      @WebMethod
      ResultObtPersonaPorDoc obtPersonaPorDoc(ParamObtPersonaPorDoc params);

      @WebMethod
      ResultObtProductDesc productDesc();
  }
  ```

- [ ] **3.1.2** Implement `PDIServiceImpl` with JAX-WS annotations
  - Add `@WebService(endpointInterface = "...")`
  - Inject dependencies (mock data repository, authentication service)

#### 3.2 Operation: ObtPersonaPorDoc

- [ ] **3.2.1** Implement authentication validation
  - Validate `organizacion` parameter
  - Validate `passwordEntidad` parameter
  - Return error 10002 (Unauthorized) if invalid

- [ ] **3.2.2** Implement input validation
  - Validate `NroDocumento` format (numeric, no formatting)
  - Validate `TipoDocumento` (default to "DO" if empty)
  - Return error 10001 (Incorrect parameters) if invalid

- [ ] **3.2.3** Implement mock data lookup
  - Search mock repository by CI number
  - Handle person not found (error 500)
  - Handle annulled CI numbers (error 1003)

- [ ] **3.2.4** Implement response building
  - Map mock data to `ObjPersona`
  - Add warnings if needed (code 701 - data needs regularization)
  - Return complete `ResultObtPersonaPorDoc`

#### 3.3 Operation: ProductDesc

- [ ] **3.3.1** Implement `productDesc()` method
  - Return version: "1.0" (or appropriate version)
  - Return modalidad: "Testing"
  - Return descripcion: "Servicio de Información D.N.I.C. - Mock"

---

### Phase 4: Error Handling and Response Codes

- [ ] **4.1** Implement error code enumeration
  ```java
  500  - Persona inexistente (Person not found)
  701  - Datos de Persona a regularizar (Warning - data needs update)
  1001 - No se pudo completar la consulta (Could not complete query)
  1002 - Limite de consultas Excedido (Rate limit exceeded)
  1003 - Número de cédula anulado (Annulled ID)
  10001 - Parámetros incorrectos (Invalid parameters)
  10002 - Acceso No Autorizado (Unauthorized access)
  ```

- [ ] **4.2** Create error response builder utility
  - Build `Mensaje` objects with appropriate codes
  - Add to `Errores` or `Warnings` collections

- [ ] **4.3** Implement SOAP fault handling
  - Create custom `PDIServiceException`
  - Map exceptions to SOAP faults

---

### Phase 5: Mock Data Repository

- [ ] **5.1** Create `MockPersonRepository` interface
  ```java
  Optional<ObjPersona> findByDocumento(String nroDocumento);
  List<ObjPersona> getAllPersons();
  boolean isDocumentoAnulado(String nroDocumento);
  ```

- [ ] **5.2** Implement `MockPersonRepositoryImpl`
  - Load test data from JSON file or hard-coded
  - Provide in-memory storage
  - Support various test scenarios

- [ ] **5.3** Create realistic test data
  - **Minimum 10 test citizens** with:
    - Valid CI numbers (8 digits)
    - Realistic names (Uruguayan naming conventions)
    - Various birth dates (adults, near-18, minors for testing)
    - Both sexes
    - Different nationalities

  **Example test cases:**
  ```json
  {
    "nroDocumento": "12345678",
    "nombre1": "Juan",
    "nombre2": "Carlos",
    "Apellido1": "Pérez",
    "Apellido2": "García",
    "sexo": 1,
    "fechaNacimiento": "1990-05-15",
    "codNacionalidad": 1,
    "nombreEnCedula": "PEREZ GARCIA JUAN CARLOS"
  }
  ```

- [ ] **5.4** Add edge cases for testing
  - Invalid CI (for error 500)
  - Annulled CI (for error 1003)
  - Minor (under 18) for age verification testing
  - Person with warnings (error 701)

---

### Phase 6: Authentication and Security

- [ ] **6.1** Create `AuthenticationService`
  - Store valid organization credentials
  - Validate organizacion/password combinations

- [ ] **6.2** Configure test credentials
  - Organization: "HCEN"
  - Password: "test-password-123" (configurable)

- [ ] **6.3** Implement HTTPS configuration
  - Generate self-signed certificate for development
  - Configure WildFly SSL/TLS
  - Document certificate setup for HCEN integration

- [ ] **6.4** Add security logging
  - Log authentication attempts
  - Log unauthorized access attempts
  - Track query rate per organization (for rate limiting)

---

### Phase 7: Integration Testing

#### 7.1 Unit Tests

- [ ] **7.1.1** Test `ObtPersonaPorDoc` operation
  - Valid CI returns person data
  - Invalid CI returns error 500
  - Invalid credentials return error 10002
  - Invalid parameters return error 10001

- [ ] **7.1.2** Test `ProductDesc` operation
  - Returns correct version and modalidad

- [ ] **7.1.3** Test authentication service
  - Valid credentials pass
  - Invalid credentials fail

- [ ] **7.1.4** Test mock repository
  - Lookup by CI works
  - Missing CI returns empty

#### 7.2 Integration Tests

- [ ] **7.2.1** Deploy to WildFly and test WSDL generation
  - Access `http://localhost:8080/pdi-mock/PDIService?wsdl`
  - Verify WSDL structure matches specification

- [ ] **7.2.2** Create SOAP client test
  - Use JAX-WS client to call service
  - Test full request/response cycle
  - Verify XML marshalling/unmarshalling

- [ ] **7.2.3** Test HCEN integration flow
  - HCEN calls PDI mock during INUS registration
  - Verify date of birth extraction
  - Verify age calculation (18+ validation)

---

### Phase 8: Documentation and Deployment

- [ ] **8.1** Create API documentation
  - Document SOAP operations
  - Document error codes
  - Provide example SOAP requests/responses

- [ ] **8.2** Create deployment guide
  - WildFly configuration steps
  - Certificate setup instructions
  - Environment variables (credentials, endpoint URLs)

- [ ] **8.3** Document test credentials
  - Organization names
  - Passwords
  - Test CI numbers and expected responses

- [ ] **8.4** Update main project `TODO.md`
  - Document PDI mock completion status
  - Update integration checklist

---

## WSDL Endpoint Structure (Expected)

**Service Endpoint**: `http://localhost:8080/pdi-mock/PDIService`
**WSDL URL**: `http://localhost:8080/pdi-mock/PDIService?wsdl`

**Operations**:
1. `obtPersonaPorDoc`
   - Input: `paramobtPersonaPorDoc`
   - Output: `resultObtPersonaPorDoc`

2. `productDesc`
   - Input: (none)
   - Output: `resultObtProductDesc`

---

## Critical Integration Points with HCEN

### Use Case: INUS User Registration
```
1. User submits registration in HCEN (provides CI)
2. HCEN validates CI format
3. HCEN calls PDI: obtPersonaPorDoc(organizacion="HCEN", passwordEntidad="...", NroDocumento="12345678", TipoDocumento="DO")
4. PDI Mock returns: fechaNacimiento="1990-05-15"
5. HCEN calculates age: 2025 - 1990 = 35 years
6. HCEN validates: age >= 18 ✓
7. HCEN proceeds with INUS registration
```

**Key Data Point**: `fechaNacimiento` is **critical** - must be in format `yyyy-mmdd` for HCEN to parse correctly.

---

## Dependencies to Add (if missing)

```gradle
dependencies {
    // JAX-WS for SOAP
    implementation 'jakarta.xml.ws:jakarta.xml.ws-api:3.0.1'
    implementation 'jakarta.jws:jakarta.jws-api:3.0.0'

    // JAXB for XML binding
    implementation 'jakarta.xml.bind:jakarta.xml.bind-api:3.0.1'
    implementation 'org.glassfish.jaxb:jaxb-runtime:3.0.2'

    // Servlet API
    providedCompile 'jakarta.servlet:jakarta.servlet-api:5.0.0'

    // Testing
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:4.8.0'
}
```

---

## Timeline Estimate

| Phase | Description | Estimated Time |
|-------|-------------|----------------|
| 1 | Project setup | 1 hour |
| 2 | Data models | 2 hours |
| 3 | SOAP service implementation | 4 hours |
| 4 | Error handling | 2 hours |
| 5 | Mock data repository | 2 hours |
| 6 | Authentication/security | 3 hours |
| 7 | Testing | 4 hours |
| 8 | Documentation | 2 hours |
| **Total** | | **20 hours** |

---

## Success Criteria

✅ SOAP service deployed on WildFly
✅ WSDL accessible at `/PDIService?wsdl`
✅ `obtPersonaPorDoc` returns valid person data for test CIs
✅ Authentication validates organization credentials
✅ All error codes implemented and testable
✅ HCEN can successfully integrate and retrieve birth dates
✅ Test coverage ≥ 80%
✅ HTTPS communication configured

---

## References

- **TSE Problem Statement**: `docs/obligatorio-tse-2025-v1_0.pdf`
- **DNIC Service Documentation**: `docs/Documentacion funcional-DNIC servicio basico info (1).pdf`
- **PDI Catalog**: https://www.gub.uy/agencia-gobierno-electronico-sociedad-informacion-conocimiento/tematica/catalogo-plataforma-interoperabilidad
- **AGESIC Basic Information Service**: https://www.gub.uy/agencia-gobierno-electronico-sociedad-informacion-conocimiento/politicas-y-gestion/servicio-basico-informacion

---

**Last Updated**: 2025-11-05
**Status**: Planning Phase
**Next Step**: Begin Phase 1 - Project Setup
