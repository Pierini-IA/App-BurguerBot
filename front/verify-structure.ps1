# Script para verificar la estructura del proyecto Dio Burger Frontend

Write-Host "=== VERIFICACIÓN DE ESTRUCTURA - DIO BURGER FRONTEND ===" -ForegroundColor Cyan
Write-Host ""

# Verificar archivos críticos
Write-Host "📁 Verificando archivos críticos..." -ForegroundColor Yellow

$criticalFiles = @(
    "app/admin/layout.tsx",
    "app/admin/page.tsx",
    "lib/api/axios.ts",
    "lib/api/index.ts",
    "types/api.ts",
    "lib/context/LocalContext.tsx",
    "lib/utils/features.ts"
)

foreach ($file in $criticalFiles) {
    if (Test-Path $file) {
        Write-Host "  ✅ $file" -ForegroundColor Green
    } else {
        Write-Host "  ❌ $file (NO EXISTE)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "📄 Páginas del Admin:" -ForegroundColor Yellow
$adminPages = Get-ChildItem -Path "app/admin" -Filter "page.tsx" -Recurse | Select-Object -ExpandProperty FullName
foreach ($page in $adminPages) {
    $relativePath = $page.Replace((Get-Location).Path + "\", "")
    Write-Host "  ✅ $relativePath" -ForegroundColor Green
}

Write-Host ""
Write-Host "🔧 Verificando conflictos:" -ForegroundColor Yellow

# Verificar que NO exista el layout viejo
if (Test-Path "app/admin/[localId]/layout.tsx") {
    Write-Host "  ⚠️  CONFLICTO: app/admin/[localId]/layout.tsx existe (debería estar eliminado)" -ForegroundColor Red
} else {
    Write-Host "  ✅ No hay layout conflictivo en [localId]" -ForegroundColor Green
}

# Verificar que exista el layout nuevo
if (Test-Path "app/admin/layout.tsx") {
    Write-Host "  ✅ Layout principal existe en app/admin/layout.tsx" -ForegroundColor Green
} else {
    Write-Host "  ❌ Falta app/admin/layout.tsx" -ForegroundColor Red
}

Write-Host ""
Write-Host "📊 API Services:" -ForegroundColor Yellow
$apiServices = Get-ChildItem -Path "lib/api" -Filter "*.ts" | Where-Object { $_.Name -ne "index.ts" -and $_.Name -ne "axios.ts" } | Select-Object -ExpandProperty Name
Write-Host "  Total de servicios: $($apiServices.Count)" -ForegroundColor Cyan
foreach ($service in $apiServices) {
    Write-Host "    • $service" -ForegroundColor Gray
}

Write-Host ""
Write-Host "=== VERIFICACIÓN COMPLETA ===" -ForegroundColor Cyan
