# Migración del Sistema de Autenticación

## Resumen

Se ha migrado el sistema de autenticación de usuarios hardcodeados a un sistema basado en base de datos. Esto resuelve el problema del "huevo y la gallina" donde no se podía acceder al sistema para crear el primer usuario.

## Archivos Creados/Modificados

### Nuevos Archivos
- `create-users-table.sql` - Script para crear la tabla de usuarios
- `initialize-users-system.sql` - Script de inicialización del sistema
- `src/main/java/uy/gub/clinic/service/UserService.java` - Servicio para gestión de usuarios
- `src/main/java/uy/gub/clinic/web/UserManagementServlet.java` - Servlet para gestión de usuarios
- `src/main/java/uy/gub/clinic/util/PasswordUtil.java` - Utilidad para hash de contraseñas

### Archivos Modificados
- `src/main/java/uy/gub/clinic/entity/User.java` - Entidad actualizada con nuevos campos
- `src/main/java/uy/gub/clinic/auth/AuthServlet.java` - Autenticación basada en BD
- `src/main/webapp/index.jsp` - Página de login actualizada

## Pasos para la Migración

### 1. Ejecutar Scripts de Base de Datos

```bash
# 1. Crear la tabla de usuarios
psql -U clinic_user -d clinic_db -f create-users-table.sql

# 2. Inicializar el sistema con usuarios por defecto
psql -U clinic_user -d clinic_db -f initialize-users-system.sql
```

### 2. Agregar Dependencia BCrypt

Agregar al `build.gradle`:

```gradle
dependencies {
    // ... otras dependencias existentes
    implementation 'org.mindrot:jbcrypt:0.4'
}
```

### 3. Configurar Servlet de Gestión de Usuarios

Agregar al `web.xml`:

```xml
<servlet>
    <servlet-name>UserManagementServlet</servlet-name>
    <servlet-class>uy.gub.clinic.web.UserManagementServlet</servlet-class>
</servlet>

<servlet-mapping>
    <servlet-name>UserManagementServlet</servlet-name>
    <url-pattern>/admin/users/*</url-pattern>
</servlet-mapping>
```

### 4. Compilar y Desplegar

```bash
./gradlew build
# Desplegar el WAR en WildFly
```

## Usuarios por Defecto

Después de ejecutar los scripts, tendrás estos usuarios disponibles:

### Super Administrador
- **Usuario:** `superadmin`
- **Contraseña:** `admin123`
- **Rol:** `SUPER_ADMIN`
- **Acceso:** Completo al sistema

### Clínica del Corazón (ID 4)
- **Admin:** `admin` / `admin123`
- **Profesional:** `prof` / `admin123`

### Centro Neurológico (ID 5)
- **Admin:** `admin2` / `admin123`
- **Profesional:** `prof2` / `admin123`

## Características del Nuevo Sistema

### Seguridad
- Contraseñas hasheadas con BCrypt
- Validación de usuarios activos/inactivos
- Registro de último login
- Roles diferenciados (SUPER_ADMIN, ADMIN_CLINIC, PROFESSIONAL)

### Gestión de Usuarios
- Creación de nuevos usuarios
- Edición de información de usuarios
- Cambio de contraseñas
- Desactivación de usuarios
- Asociación con clínicas y profesionales

### Multi-Tenancy
- Usuarios asociados a clínicas específicas
- Super administrador con acceso global
- Aislamiento de datos por clínica

## Solución al Problema del "Primer Usuario"

El problema se resuelve mediante:

1. **Script de Inicialización:** Crea automáticamente usuarios por defecto
2. **Super Administrador:** Usuario con acceso completo para gestionar el sistema
3. **Usuarios Pre-configurados:** Mantiene compatibilidad con usuarios existentes
4. **Verificación de Estado:** El sistema detecta si ya tiene usuarios configurados

## Verificación del Sistema

Para verificar que el sistema está funcionando correctamente:

```sql
-- Verificar usuarios creados
SELECT username, role, active, clinic_id FROM users ORDER BY role, username;

-- Verificar estado del sistema
SELECT * FROM get_system_info();

-- Verificar si el sistema está listo
SELECT is_system_ready();
```

## Próximos Pasos Recomendados

1. **Cambiar Contraseñas:** Cambiar todas las contraseñas temporales
2. **Crear Usuarios Reales:** Crear usuarios para personal real de las clínicas
3. **Configurar Políticas:** Establecer políticas de contraseñas más estrictas
4. **Auditoría:** Implementar logs de auditoría para cambios de usuarios
5. **Backup:** Configurar respaldos regulares de la tabla de usuarios

## Troubleshooting

### Error: "Usuario no encontrado"
- Verificar que se ejecutaron los scripts de inicialización
- Comprobar que la tabla `users` existe y tiene datos

### Error: "Contraseña incorrecta"
- Las contraseñas están hasheadas, no se pueden comparar directamente
- Usar `PasswordUtil.verifyPassword()` para verificar contraseñas

### Error: "Usuario desactivado"
- El usuario existe pero está marcado como inactivo
- Activar el usuario desde la gestión de usuarios

### Error de Conexión a BD
- Verificar configuración de conexión en `persistence.xml`
- Comprobar que PostgreSQL está ejecutándose
- Verificar credenciales de la base de datos

## Contacto

Si encuentras problemas durante la migración, revisa los logs de la aplicación y verifica que todos los pasos se hayan ejecutado correctamente.
