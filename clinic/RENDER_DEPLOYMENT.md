# 🚀 Guía de Despliegue en Render - Módulo Clinic

Esta guía explica cómo desplegar el módulo **Clinic** en Render.

## 📋 Requisitos Previos

1. Cuenta en [Render](https://render.com) (plan gratuito disponible)
2. Repositorio en GitHub: `Shentlemen/Tse-2025`
3. Proyecto compilado (el WAR debe estar en `build/libs/clinic.war`)

## 🚀 Pasos para Desplegar

### 1. Compilar el Proyecto Localmente (Opcional)

Si querés compilar antes de hacer push:

```bash
cd clinic
./gradlew clean build
```

**Nota**: Render puede compilar automáticamente si configurás un build command, pero es más fácil compilar localmente y hacer commit del WAR.

### 2. Crear Servicio en Render

1. Ve a [Render Dashboard](https://dashboard.render.com)
2. Haz clic en **"New +"** → **"Web Service"**
3. Conecta tu repositorio de GitHub: `Shentlemen/Tse-2025`
4. Configura el servicio:
   - **Name**: `clinic`
   - **Root Directory**: `clinic` (importante: debe ser `clinic`, no la raíz)
   - **Environment**: `Docker`
   - **Dockerfile Path**: `Dockerfile` (o `clinic/Dockerfile` si estás en la raíz)
   - **Plan**: `Free`

### 3. Agregar Base de Datos PostgreSQL

1. En Render Dashboard, haz clic en **"New +"** → **"PostgreSQL"**
2. Configura:
   - **Name**: `clinic-db`
   - **Database**: `clinic_db`
   - **User**: `clinic_user`
   - **Plan**: `Free`
3. Render creará automáticamente la variable `DATABASE_URL`

### 4. Configurar Variables de Entorno

En la pestaña **"Environment"** de tu servicio web, agrega:

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `DATABASE_URL` | URL de conexión a PostgreSQL (se conecta automáticamente si usas `render.yaml`) | `postgresql://user:pass@host:port/dbname` |
| `HCEN_ENDPOINT` | URL del servicio HCEN central | `https://hcen.render.com/hcen/api` |
| `JAVA_OPTS` | Opciones de JVM | `-Xmx512m -Xms256m` |

### 5. Usar render.yaml (Opcional pero Recomendado)

Si usás `render.yaml`, Render configurará automáticamente:
- El servicio web
- La base de datos PostgreSQL
- Las variables de entorno
- La conexión entre servicios

Para usar `render.yaml`:
1. Asegurate de que el archivo `clinic/render.yaml` esté en el repositorio
2. En Render, cuando crees el servicio, selecciona **"Apply render.yaml"**

### 6. Ejecutar Migraciones de Base de Datos

Después del primer despliegue, necesitás ejecutar las migraciones de Flyway:

#### Opción A: Desde Render Shell

1. En Render Dashboard, ve a tu servicio
2. Click en **"Shell"**
3. Ejecuta:
```bash
cd /opt/jboss/wildfly/bin
./run-migrations.sh
```

#### Opción B: Desde tu máquina local con Render CLI

```bash
# Instalar Render CLI
npm i -g render-cli

# Ejecutar migraciones
render exec clinic -- ./gradlew flywayMigrate
```

### 7. Configurar el Dominio

1. En la pestaña **"Settings"** de tu servicio
2. En **"Custom Domain"**, podés agregar un dominio personalizado
3. O usar el dominio gratuito de Render: `clinic.onrender.com`

## 🔧 Configuración del Dockerfile

El `Dockerfile` está configurado para:
- Usar WildFly 30.0.1 con JDK 21
- Instalar el driver PostgreSQL
- Copiar el WAR compilado
- Configurar WildFly con variables de entorno
- Exponer el puerto 8080

**Importante**: Render asigna puertos dinámicamente. El script `configure-wildfly.sh` debe leer la variable `PORT` y configurar WildFly.

## 🐛 Troubleshooting

### Error: "Cannot connect to database"

**Solución**: 
1. Verifica que la base de datos PostgreSQL esté creada y activa
2. Verifica que `DATABASE_URL` esté configurada correctamente
3. Asegurate de que el servicio web tenga acceso a la base de datos (mismo proyecto en Render)

### Error: "Port already in use"

**Solución**: El script `configure-wildfly.sh` debería configurar el puerto automáticamente desde `PORT`. Si persiste, verifica que la variable `PORT` esté disponible.

### Error: "WAR file not found"

**Solución**: 
1. Asegurate de compilar el proyecto: `./gradlew clean build`
2. Verifica que `build/libs/clinic.war` exista
3. Verifica que el `.dockerignore` no esté excluyendo el WAR

### Error: "Build failed"

**Solución**:
1. Verifica los logs de build en Render
2. Asegurate de que el `Dockerfile` esté en el directorio correcto
3. Verifica que todas las dependencias estén disponibles

## 📝 Notas Importantes

1. **Plan Gratuito**: Render ofrece un plan gratuito con limitaciones:
   - El servicio se "duerme" después de 15 minutos de inactividad
   - El primer request después de dormir puede tardar ~30 segundos
   - 750 horas gratis por mes

2. **Base de Datos**: El plan gratuito de PostgreSQL incluye:
   - 90 días de retención de backups
   - 1 GB de almacenamiento
   - Conexiones limitadas

3. **Auto-deploy**: Por defecto, Render despliega automáticamente cuando hacés push a la rama principal.

4. **Logs**: Podés ver los logs en tiempo real en la pestaña **"Logs"** del servicio.

## 🔗 Enlaces Útiles

- [Render Documentation](https://render.com/docs)
- [Render Free Tier](https://render.com/docs/free)
- [PostgreSQL on Render](https://render.com/docs/databases)

