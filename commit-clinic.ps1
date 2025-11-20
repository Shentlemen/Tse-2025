# Script para hacer commit solo de cambios en clinic
# Uso: .\commit-clinic.ps1 "mensaje del commit"

param(
    [Parameter(Mandatory=$true)]
    [string]$Mensaje
)

Write-Host "Agregando solo cambios en clinic..." -ForegroundColor Green
git add clinic/

Write-Host "Haciendo commit..." -ForegroundColor Green
git commit -m $Mensaje

Write-Host "`nEstado actual:" -ForegroundColor Yellow
git status --short

