# ========================================
# Script de Inicio - Dio Burger API (PowerShell)
# ========================================
# Este script facilita el inicio de la aplicación
# con Docker Compose en Windows
#
# Uso: .\start.ps1 [opciones]
#

param(
    [switch]$Build,
    [switch]$Clean,
    [switch]$Logs,
    [switch]$Status,
    [switch]$Stop,
    [switch]$Help
)

# Banner
Write-Host ""
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║       🍔 DIO BURGER API v1.4.0       ║" -ForegroundColor Cyan
Write-Host "║    Sistema de Gestión de Hamburguesería ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Función de ayuda
function Show-Help {
    Write-Host "Uso: .\start.ps1 [opciones]" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Opciones:"
    Write-Host "  -Build       Reconstruir imágenes antes de iniciar"
    Write-Host "  -Clean       Detener servicios y limpiar volúmenes"
    Write-Host "  -Logs        Mostrar logs en tiempo real"
    Write-Host "  -Status      Mostrar estado de los servicios"
    Write-Host "  -Stop        Detener servicios"
    Write-Host "  -Help        Mostrar esta ayuda"
    Write-Host ""
    exit 0
}

# Mostrar ayuda si se solicita
if ($Help) {
    Show-Help
}

# Verificar que Docker está instalado
try {
    $dockerVersion = docker --version
    Write-Host "✅ Docker encontrado: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: Docker no está instalado" -ForegroundColor Red
    Write-Host "Instala Docker Desktop desde: https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
    exit 1
}

# Verificar Docker Compose
try {
    $composeVersion = docker compose version
    Write-Host "✅ Docker Compose encontrado: $composeVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: Docker Compose no está disponible" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Verificar archivos necesarios
if (-not (Test-Path "Dockerfile")) {
    Write-Host "❌ Error: Dockerfile no encontrado" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path "docker-compose.yml")) {
    Write-Host "❌ Error: docker-compose.yml no encontrado" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Archivos de configuración encontrados" -ForegroundColor Green
Write-Host ""

# Modo clean
if ($Clean) {
    Write-Host "🧹 Limpiando servicios y volúmenes..." -ForegroundColor Yellow
    docker compose down -v
    Write-Host "✅ Limpieza completada" -ForegroundColor Green
    exit 0
}

# Modo stop
if ($Stop) {
    Write-Host "🛑 Deteniendo servicios..." -ForegroundColor Yellow
    docker compose down
    Write-Host "✅ Servicios detenidos" -ForegroundColor Green
    exit 0
}

# Modo status
if ($Status) {
    Write-Host "📊 Estado de los servicios:" -ForegroundColor Cyan
    docker compose ps
    exit 0
}

# Iniciar servicios
Write-Host "🚀 Iniciando servicios..." -ForegroundColor Cyan
Write-Host ""

$buildFlag = ""
if ($Build) {
    Write-Host "🔨 Reconstruyendo imágenes..." -ForegroundColor Yellow
    $buildFlag = "--build"
}

if ($buildFlag) {
    docker compose up -d --build
} else {
    docker compose up -d
}

Write-Host ""
Write-Host "✅ Servicios iniciados correctamente" -ForegroundColor Green
Write-Host ""

# Esperar a que los servicios estén listos
Write-Host "⏳ Esperando a que los servicios estén listos..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Verificar estado
Write-Host ""
Write-Host "📊 Estado de los servicios:" -ForegroundColor Cyan
docker compose ps
Write-Host ""

# Mostrar URLs de acceso
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║        🌐 URLs de Acceso              ║" -ForegroundColor Green
Write-Host "╠════════════════════════════════════════╣" -ForegroundColor Green
Write-Host "║ API:      http://localhost:8080        ║" -ForegroundColor White
Write-Host "║ Health:   http://localhost:8080/actuator/health ║" -ForegroundColor White
Write-Host "║ pgAdmin:  http://localhost:5050        ║" -ForegroundColor White
Write-Host "║ PostgreSQL: localhost:5432             ║" -ForegroundColor White
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

# Mostrar credenciales
Write-Host "🔑 Credenciales:" -ForegroundColor Cyan
Write-Host "  PostgreSQL:"
Write-Host "    - Usuario: postgres"
Write-Host "    - Contraseña: postgres"
Write-Host "    - Base de datos: dioburger"
Write-Host ""
Write-Host "  pgAdmin:"
Write-Host "    - Email: admin@dioburger.com"
Write-Host "    - Contraseña: admin"
Write-Host ""

# Modo logs
if ($Logs) {
    Write-Host "📋 Mostrando logs (Ctrl+C para salir)..." -ForegroundColor Cyan
    Write-Host ""
    docker compose logs -f
    exit 0
}

# Comandos útiles
Write-Host "💡 Comandos útiles:" -ForegroundColor Yellow
Write-Host "  Ver logs:           docker compose logs -f"
Write-Host "  Ver logs de API:    docker compose logs -f api"
Write-Host "  Detener servicios:  docker compose down"
Write-Host "  Reiniciar API:      docker compose restart api"
Write-Host "  Estado:             docker compose ps"
Write-Host ""

Write-Host "✨ ¡Dio Burger API está listo!" -ForegroundColor Green
Write-Host ""
