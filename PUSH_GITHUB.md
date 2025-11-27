# Cómo hacer push a GitHub

## Opción 1: Usar GitHub Desktop (MÁS FÁCIL)

1. Abre GitHub Desktop
2. File → Add Local Repository
3. Selecciona: `C:\TSEGrupo\tse-2025`
4. Si te dice que no es un repositorio de GitHub, haz clic en "Publish repository"
5. Selecciona el remoto "github" y publica

## Opción 2: Usar Token de Acceso Personal

1. Ve a: https://github.com/settings/tokens
2. Click en "Generate new token (classic)"
3. Nombre: "Railway Deploy"
4. Selecciona scope: `repo` (todos los permisos de repo)
5. Click en "Generate token"
6. **COPIA EL TOKEN** (solo se muestra una vez)

7. Luego ejecuta en PowerShell:
```powershell
cd C:\TSEGrupo\tse-2025
git push https://TU_TOKEN@github.com/Shentlemen/Tse-2025.git main
```

O cuando te pida credenciales:
- Username: `Shentlemen`
- Password: `TU_TOKEN` (el token que copiaste)

## Opción 3: Cambiar la URL del remoto para incluir el token

```powershell
cd C:\TSEGrupo\tse-2025
git remote set-url github https://TU_TOKEN@github.com/Shentlemen/Tse-2025.git
git push github main
```

## Verificar que se subió

Ve a: https://github.com/Shentlemen/Tse-2025

