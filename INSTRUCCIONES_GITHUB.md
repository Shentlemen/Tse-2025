# Instrucciones para subir el proyecto a GitHub

## Opción 1: Agregar GitHub como remoto adicional (Recomendada)

### Paso 1: Crear repositorio en GitHub
1. Ve a https://github.com
2. Click en "New repository"
3. Nombre: `tse-2025` (o el que prefieras)
4. **NO** marques "Initialize with README" ni agregues .gitignore
5. Click en "Create repository"

### Paso 2: Agregar GitHub como remoto
Ejecuta estos comandos (reemplaza TU_USUARIO con tu usuario de GitHub):

```bash
cd C:\TSEGrupo\tse-2025
git remote add github https://github.com/TU_USUARIO/tse-2025.git
```

### Paso 3: Subir a GitHub
```bash
git push github main
```

### Paso 4: Verificar remotos
```bash
git remote -v
```

Deberías ver:
- `origin` → GitLab
- `github` → GitHub

### Para futuros cambios
Puedes hacer push a ambos:
```bash
git push origin main    # A GitLab
git push github main    # A GitHub
```

---

## Opción 2: Usar GitHub Desktop

### Paso 1: Crear repositorio en GitHub
1. Ve a https://github.com
2. Click en "New repository"
3. Nombre: `tse-2025`
4. **NO** marques "Initialize with README"
5. Click en "Create repository"

### Paso 2: En GitHub Desktop
1. Abre GitHub Desktop
2. File → Clone Repository
3. Selecciona la pestaña "URL"
4. URL: `https://gitlab.fing.edu.uy/agustin.silvano/tse-2025.git`
5. Local Path: Elige una carpeta (ej: `C:\Users\TuUsuario\Documents\tse-2025`)
6. Click "Clone"

### Paso 3: Agregar remoto de GitHub
1. En GitHub Desktop, ve a Repository → Repository Settings
2. O abre la terminal integrada (Repository → Open in Git Bash)
3. Ejecuta:
```bash
git remote add github https://github.com/TU_USUARIO/tse-2025.git
git push github main
```

---

## Opción 3: Cambiar origin a GitHub (si solo quieres GitHub)

```bash
cd C:\TSEGrupo\tse-2025
git remote set-url origin https://github.com/TU_USUARIO/tse-2025.git
git push origin main
```

**Nota:** Esto cambiará el remoto principal a GitHub. Si quieres mantener GitLab, usa la Opción 1.

