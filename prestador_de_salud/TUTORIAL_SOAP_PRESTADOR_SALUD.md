# Tutorial: Endpoint SOAP para Prestador de Salud - Historia Clínica

## 1. Estructura del Proyecto

Crea la siguiente estructura de paquetes en `src/main/java`:

```
com.example.prestador
├── config/          # Configuraciones
├── model/           # Entidades de dominio
├── dto/             # Data Transfer Objects
├── service/         # Lógica de negocio
├── endpoint/        # Endpoints SOAP
└── Application.java # Clase principal
```

## 2. Clases de Modelo

### 2.1 Paciente.java
```java
package com.example.prestador.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDate;

@XmlRootElement
public class Paciente {
    private String cedula;
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String email;
    
    // Constructores, getters y setters
    public Paciente() {}
    
    public Paciente(String cedula, String nombre, String apellido) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellido = apellido;
    }
    
    @XmlElement
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    
    @XmlElement
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    @XmlElement
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    
    @XmlElement
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    
    @XmlElement
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    @XmlElement
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
```

### 2.2 HistoriaClinica.java
```java
package com.example.prestador.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.List;

@XmlRootElement
public class HistoriaClinica {
    private Long id;
    private String cedulaPaciente;
    private LocalDateTime fechaConsulta;
    private String motivoConsulta;
    private String diagnostico;
    private String tratamiento;
    private List<String> medicamentos;
    private String observaciones;
    
    // Constructores, getters y setters
    public HistoriaClinica() {}
    
    @XmlElement
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    @XmlElement
    public String getCedulaPaciente() { return cedulaPaciente; }
    public void setCedulaPaciente(String cedulaPaciente) { this.cedulaPaciente = cedulaPaciente; }
    
    @XmlElement
    public LocalDateTime getFechaConsulta() { return fechaConsulta; }
    public void setFechaConsulta(LocalDateTime fechaConsulta) { this.fechaConsulta = fechaConsulta; }
    
    @XmlElement
    public String getMotivoConsulta() { return motivoConsulta; }
    public void setMotivoConsulta(String motivoConsulta) { this.motivoConsulta = motivoConsulta; }
    
    @XmlElement
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    
    @XmlElement
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    
    @XmlElement
    public List<String> getMedicamentos() { return medicamentos; }
    public void setMedicamentos(List<String> medicamentos) { this.medicamentos = medicamentos; }
    
    @XmlElement
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
```

## 3. DTOs para SOAP

### 3.1 ConsultarHistoriaRequest.java
```java
package com.example.prestador.dto;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

@XmlRootElement
public class ConsultarHistoriaRequest {
    private String cedulaPaciente;
    private String fechaDesde;
    private String fechaHasta;
    
    public ConsultarHistoriaRequest() {}
    
    @XmlElement
    public String getCedulaPaciente() { return cedulaPaciente; }
    public void setCedulaPaciente(String cedulaPaciente) { this.cedulaPaciente = cedulaPaciente; }
    
    @XmlElement
    public String getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(String fechaDesde) { this.fechaDesde = fechaDesde; }
    
    @XmlElement
    public String getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(String fechaHasta) { this.fechaHasta = fechaHasta; }
}
```

### 3.2 ConsultarHistoriaResponse.java
```java
package com.example.prestador.dto;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@XmlRootElement
public class ConsultarHistoriaResponse {
    private boolean exito;
    private String mensaje;
    private List<HistoriaClinica> historias;
    
    public ConsultarHistoriaResponse() {}
    
    @XmlElement
    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }
    
    @XmlElement
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    
    @XmlElement
    public List<HistoriaClinica> getHistorias() { return historias; }
    public void setHistorias(List<HistoriaClinica> historias) { this.historias = historias; }
}
```

## 4. Servicio de Negocio

### 4.1 HistoriaClinicaService.java
```java
package com.example.prestador.service;

import com.example.prestador.model.HistoriaClinica;
import com.example.prestador.dto.ConsultarHistoriaRequest;
import com.example.prestador.dto.ConsultarHistoriaResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class HistoriaClinicaService {
    
    // Simulamos una base de datos en memoria
    private List<HistoriaClinica> historias = new ArrayList<>();
    
    public ConsultarHistoriaResponse consultarHistoria(ConsultarHistoriaRequest request) {
        ConsultarHistoriaResponse response = new ConsultarHistoriaResponse();
        
        try {
            // Filtrar historias por cédula del paciente
            List<HistoriaClinica> historiasFiltradas = new ArrayList<>();
            
            for (HistoriaClinica historia : historias) {
                if (historia.getCedulaPaciente().equals(request.getCedulaPaciente())) {
                    historiasFiltradas.add(historia);
                }
            }
            
            response.setExito(true);
            response.setMensaje("Consulta realizada exitosamente");
            response.setHistorias(historiasFiltradas);
            
        } catch (Exception e) {
            response.setExito(false);
            response.setMensaje("Error al consultar historia: " + e.getMessage());
        }
        
        return response;
    }
    
    public void agregarHistoria(HistoriaClinica historia) {
        historia.setId(System.currentTimeMillis()); // ID simple
        historia.setFechaConsulta(LocalDateTime.now());
        historias.add(historia);
    }
    
    // Método para agregar datos de prueba
    public void inicializarDatosPrueba() {
        HistoriaClinica historia1 = new HistoriaClinica();
        historia1.setCedulaPaciente("12345678");
        historia1.setMotivoConsulta("Control rutinario");
        historia1.setDiagnostico("Paciente sano");
        historia1.setTratamiento("Continuar con estilo de vida saludable");
        agregarHistoria(historia1);
    }
}
```

## 5. Endpoint SOAP

### 5.1 PrestadorSaludEndpoint.java
```java
package com.example.prestador.endpoint;

import com.example.prestador.dto.ConsultarHistoriaRequest;
import com.example.prestador.dto.ConsultarHistoriaResponse;
import com.example.prestador.service.HistoriaClinicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class PrestadorSaludEndpoint {
    
    private static final String NAMESPACE_URI = "http://prestador.example.com/historia";
    
    @Autowired
    private HistoriaClinicaService historiaService;
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "consultarHistoriaRequest")
    @ResponsePayload
    public ConsultarHistoriaResponse consultarHistoria(@RequestPayload ConsultarHistoriaRequest request) {
        return historiaService.consultarHistoria(request);
    }
}
```

## 6. Configuración

### 6.1 WebServiceConfig.java
```java
package com.example.prestador.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {
    
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }
    
    @Bean(name = "prestadorSalud")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema prestadorSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("PrestadorSaludPort");
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setTargetNamespace("http://prestador.example.com/historia");
        wsdl11Definition.setSchema(prestadorSchema);
        return wsdl11Definition;
    }
    
    @Bean
    public XsdSchema prestadorSchema() {
        return new SimpleXsdSchema(new ClassPathResource("prestador.xsd"));
    }
}
```

## 7. Schema XSD

### 7.1 src/main/resources/prestador.xsd
```xml
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           targetNamespace="http://prestador.example.com/historia"
           xmlns:tns="http://prestador.example.com/historia"
           elementFormDefault="qualified">

    <xs:element name="consultarHistoriaRequest">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="cedulaPaciente" type="xs:string"/>
                <xs:element name="fechaDesde" type="xs:string" minOccurs="0"/>
                <xs:element name="fechaHasta" type="xs:string" minOccurs="0"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <xs:element name="consultarHistoriaResponse">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="exito" type="xs:boolean"/>
                <xs:element name="mensaje" type="xs:string"/>
                <xs:element name="historias" type="tns:historiaClinica" minOccurs="0" maxOccurs="unbounded"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <xs:complexType name="historiaClinica">
        <xs:sequence>
            <xs:element name="id" type="xs:long"/>
            <xs:element name="cedulaPaciente" type="xs:string"/>
            <xs:element name="fechaConsulta" type="xs:dateTime"/>
            <xs:element name="motivoConsulta" type="xs:string"/>
            <xs:element name="diagnostico" type="xs:string"/>
            <xs:element name="tratamiento" type="xs:string"/>
            <xs:element name="medicamentos" type="xs:string" minOccurs="0" maxOccurs="unbounded"/>
            <xs:element name="observaciones" type="xs:string" minOccurs="0"/>
        </xs:sequence>
    </xs:complexType>
</xs:schema>
```

## 8. Clase Principal

### 8.1 Application.java
```java
package com.example.prestador;

import com.example.prestador.service.HistoriaClinicaService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {
    
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        
        // Inicializar datos de prueba
        HistoriaClinicaService historiaService = context.getBean(HistoriaClinicaService.class);
        historiaService.inicializarDatosPrueba();
        
        System.out.println("Servicio SOAP iniciado en: http://localhost:8080/ws/prestadorSalud.wsdl");
    }
}
```

## 9. Archivo de Propiedades

### 9.1 application.properties
```properties
server.port=8080
spring.application.name=prestador-salud-soap

# Configuración de logging
logging.level.com.example.prestador=DEBUG
logging.level.org.springframework.ws=DEBUG
```

## 10. Probar el Servicio

### 10.1 Ejecutar la aplicación
```bash
mvn spring-boot:run
```

### 10.2 Verificar WSDL
Abrir en el navegador: `http://localhost:8080/ws/prestadorSalud.wsdl`

### 10.3 Ejemplo de Request SOAP
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                  xmlns:hist="http://prestador.example.com/historia">
   <soapenv:Header/>
   <soapenv:Body>
      <hist:consultarHistoriaRequest>
         <hist:cedulaPaciente>12345678</hist:cedulaPaciente>
      </hist:consultarHistoriaRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

## 11. Comandos Útiles

### 11.1 Compilar y ejecutar
```bash
mvn clean compile
mvn spring-boot:run
```

### 11.2 Generar JAR ejecutable
```bash
mvn clean package
java -jar target/demo-1.0-SNAPSHOT.jar
```

### 11.3 Ejecutar tests
```bash
mvn test
```

## 12. Próximos Pasos

1. **Agregar validaciones** a las requests
2. **Implementar persistencia** con base de datos
3. **Agregar autenticación** y autorización
4. **Implementar logging** detallado
5. **Agregar más operaciones** (crear, actualizar, eliminar historia)
6. **Implementar manejo de errores** robusto
7. **Agregar documentación** con Swagger/OpenAPI

¡Listo! Ahora tienes un endpoint SOAP completo para prestador de salud con historias clínicas.
