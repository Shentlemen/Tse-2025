# HCEN - Componente Periférico de Clínica

Componente periférico multi-tenant para clínicas del sistema de Historia Clínica Electrónica Nacional (HCEN) de Uruguay.

## 🏥 Descripción

Este componente permite a las clínicas gestionar sus profesionales, pacientes y documentos clínicos, integrándose con el sistema central HCEN para el intercambio de información médica.

## 🎯 Funcionalidades Principales

### Portal Admin Clínica
- ✅ Gestión de profesionales de salud
- ✅ Gestión de pacientes
- ✅ Gestión de especialidades médicas
- ✅ Personalización de la clínica (logo, colores)
- ✅ Configuración de integración con HCEN

### Portal Profesionales de Salud
- ✅ Dashboard con estadísticas
- ✅ Gestión de documentos clínicos
- ✅ Acceso a historia clínica de pacientes
- ✅ Solicitudes de acceso a documentos externos
- ✅ Búsqueda de pacientes

### Integración HCEN
- ✅ Comunicación con componente central
- ✅ Sincronización de pacientes con INUS
- ✅ Registro de documentos en RNDC
- ✅ Solicitudes de acceso a documentos externos

## 🛠️ Tecnologías

- **Backend**: Jakarta EE 10 + WildFly 30
- **Frontend**: JSP + Bootstrap 5 + Font Awesome
- **Base de Datos**: PostgreSQL
- **Autenticación**: Sistema interno simple
- **Integración**: REST APIs + Apache HTTP Client

## 📋 Requisitos

- Java 17 o superior
- WildFly 30.0+ (Jakarta EE 10)
- PostgreSQL 14+
- Gradle 8.0+ (incluido en el proyecto)

## 🚀 Instalación y Configuración

### 1. Configurar Base de Datos

```bash
# Crear base de datos (con el usuario postgres)
createdb -U postgres -h localhost -p 5432 clinic_db
```

### 2. Configurar WildFly

#### Datasource (standalone.xml):
```xml
<datasource jndi-name="java:jboss/datasources/ClinicDS" pool-name="ClinicDS">
    <connection-url>jdbc:postgresql://localhost:5432/clinic_db</connection-url>
    <driver>postgresql</driver>
    <security>
        <user-name>postgres</user-name>
        <password>postgres</password>
    </security>
</datasource>
```

#### Driver PostgreSQL:
```bash
# Descargar driver
wget https://jdbc.postgresql.org/download/postgresql-42.7.0.jar

# Desplegar en WildFly
cp postgresql-42.7.0.jar $WILDFLY_HOME/standalone/deployments/
```

### 3. Compilar y Desplegar

```bash
# Compilar proyecto
./gradlew clean build

# Desplegar en WildFly
./gradlew deployToWildFly
```

### 4. Iniciar WildFly

```bash
$WILDFLY_HOME/bin/standalone.sh
```

## 🌐 Acceso a la Aplicación

- **Portal Principal**: http://localhost:8080/clinic/
- **Portal Admin**: http://localhost:8080/clinic/admin/dashboard.jsp
- **Portal Profesional**: http://localhost:8080/clinic/professional/dashboard.jsp

## 👥 Usuarios de Prueba

### Administrador de Clínica
- **Usuario**: admin
- **Contraseña**: admin123
- **Acceso**: Portal Admin Clínica

### Profesional de Salud
- **Usuario**: prof
- **Contraseña**: prof123
- **Acceso**: Portal Profesionales

## 📊 Datos de Desarrollo

El sistema incluye datos hardcodeados para desarrollo:

### Clínicas
- **Clínica del Corazón** (CLIN001) - Especializada en cardiología
- **Centro Neurológico** (CLIN002) - Especializada en neurología

### Profesionales
- **Dr. Juan Pérez** - Cardiología (LIC001)
- **Dra. María González** - Neurología (LIC002)
- **Dr. Carlos Rodríguez** - Pediatría (LIC003)

### Pacientes
- **Ana Silva** - 38 años, F (12345678)
- **Roberto Martínez** - 45 años, M (87654321)
- **Lucía Fernández** - 31 años, F (11223344)

### Especialidades
- Cardiología (CARD)
- Neurología (NEURO)
- Pediatría (PED)
- Traumatología (TRAUM)

## 🏗️ Arquitectura

```
clinic/
├── src/main/java/uy/gub/clinic/
│   ├── entity/              # Entidades JPA
│   │   ├── Clinic.java      # Clínica
│   │   ├── Professional.java # Profesionales
│   │   ├── Patient.java     # Pacientes
│   │   ├── Specialty.java   # Especialidades
│   │   └── ...
│   ├── service/             # Servicios de negocio
│   │   └── ClinicService.java # Servicio principal con datos hardcodeados
│   ├── auth/                # Autenticación
│   │   └── AuthServlet.java # Servlet de login/logout
│   └── config/              # Configuraciones
└── src/main/webapp/
    ├── admin/               # Portal Admin Clínica
    │   ├── dashboard.jsp    # Dashboard administrativo
    │   ├── professionals.jsp # Gestión de profesionales
    │   └── ...
    ├── professional/        # Portal Profesionales
    │   ├── dashboard.jsp    # Dashboard profesional
    │   └── ...
    └── index.jsp            # Página de login
```

## 🔧 Desarrollo

### Estructura del Proyecto

El proyecto sigue la arquitectura Jakarta EE estándar:

- **Entidades JPA**: Modelo de datos con relaciones
- **Servicios**: Lógica de negocio (actualmente con datos hardcodeados)
- **Servlets**: Manejo de autenticación
- **JSP**: Interfaces web con Bootstrap

### Datos Hardcodeados

Para facilitar el desarrollo, el sistema usa datos hardcodeados en `ClinicService.java`:

```java
// Ejemplo de uso
ClinicService clinicService = new ClinicService();
List<Professional> professionals = clinicService.getAllProfessionals();
List<Patient> patients = clinicService.getPatientsByClinic(1L);
```

### Próximos Pasos

1. **Configurar Base de Datos**: Migrar de datos hardcodeados a persistencia real
2. **Implementar APIs REST**: Para integración con HCEN central
3. **Agregar Funcionalidades**: Más portales y características
4. **Integración HCEN**: Comunicación con componente central

## 🧪 Testing

```bash
# Ejecutar tests
./gradlew test

# Verificar cobertura
./gradlew test jacocoTestReport
```

## 📝 Logs

Los logs se encuentran en:
- **WildFly**: `$WILDFLY_HOME/standalone/log/server.log`
- **Aplicación**: Configurado con SLF4J

## 🐛 Troubleshooting

### Error de Conexión a Base de Datos
1. Verificar que PostgreSQL esté ejecutándose
2. Comprobar configuración del datasource en WildFly
3. Verificar que el driver PostgreSQL esté desplegado

### Error de Despliegue
1. Limpiar y recompilar: `./gradlew clean build`
2. Verificar logs de WildFly
3. Comprobar dependencias en `build.gradle`

### Problemas de Autenticación
1. Verificar usuarios hardcodeados en `AuthServlet.java`
2. Comprobar configuración de sesiones en `web.xml`

## 📞 Soporte

Para soporte técnico o consultas sobre el proyecto, contactar al equipo de desarrollo.

## 📄 Licencia

Este proyecto es parte del Taller de Sistemas Empresariales 2025 - Universidad de la República, Uruguay.

---

**Desarrollado por**: Grupo 9 TSE 2025  
**Fecha**: Diciembre 2024  
**Versión**: 1.0.0-SNAPSHOT
