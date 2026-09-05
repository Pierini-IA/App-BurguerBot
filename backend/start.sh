#!/bin/bash

# ========================================
# Script de Inicio - Dio Burger API
# ========================================
# Este script facilita el inicio de la aplicación
# con Docker Compose
#
# Uso: ./start.sh [opciones]
#

set -e  # Salir si algún comando falla

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Banner
echo -e "${BLUE}"
echo "╔════════════════════════════════════════╗"
echo "║       🍔 DIO BURGER API v1.4.0       ║"
echo "║    Sistema de Gestión de Hamburguesería ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"

# Verificar que Docker está instalado
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Error: Docker no está instalado${NC}"
    echo "Instala Docker desde: https://www.docker.com/get-started"
    exit 1
fi

# Verificar que Docker Compose está disponible
if ! docker compose version &> /dev/null; then
    echo -e "${RED}❌ Error: Docker Compose no está disponible${NC}"
    echo "Instala Docker Compose desde: https://docs.docker.com/compose/install/"
    exit 1
fi

echo -e "${GREEN}✅ Docker y Docker Compose encontrados${NC}"
echo ""

# Verificar archivos necesarios
if [ ! -f "Dockerfile" ]; then
    echo -e "${RED}❌ Error: Dockerfile no encontrado${NC}"
    exit 1
fi

if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}❌ Error: docker-compose.yml no encontrado${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Archivos de configuración encontrados${NC}"
echo ""

# Función para mostrar ayuda
show_help() {
    echo "Uso: ./start.sh [opciones]"
    echo ""
    echo "Opciones:"
    echo "  --build       Reconstruir imágenes antes de iniciar"
    echo "  --clean       Detener servicios y limpiar volúmenes"
    echo "  --logs        Mostrar logs en tiempo real"
    echo "  --status      Mostrar estado de los servicios"
    echo "  --stop        Detener servicios"
    echo "  --help        Mostrar esta ayuda"
    echo ""
}

# Parsear argumentos
BUILD_FLAG=""
CLEAN_MODE=false
LOGS_MODE=false
STATUS_MODE=false
STOP_MODE=false

for arg in "$@"
do
    case $arg in
        --build)
            BUILD_FLAG="--build"
            shift
            ;;
        --clean)
            CLEAN_MODE=true
            shift
            ;;
        --logs)
            LOGS_MODE=true
            shift
            ;;
        --status)
            STATUS_MODE=true
            shift
            ;;
        --stop)
            STOP_MODE=true
            shift
            ;;
        --help)
            show_help
            exit 0
            ;;
        *)
            echo -e "${RED}❌ Opción desconocida: $arg${NC}"
            show_help
            exit 1
            ;;
    esac
done

# Modo clean
if [ "$CLEAN_MODE" = true ]; then
    echo -e "${YELLOW}🧹 Limpiando servicios y volúmenes...${NC}"
    docker compose down -v
    echo -e "${GREEN}✅ Limpieza completada${NC}"
    exit 0
fi

# Modo stop
if [ "$STOP_MODE" = true ]; then
    echo -e "${YELLOW}🛑 Deteniendo servicios...${NC}"
    docker compose down
    echo -e "${GREEN}✅ Servicios detenidos${NC}"
    exit 0
fi

# Modo status
if [ "$STATUS_MODE" = true ]; then
    echo -e "${BLUE}📊 Estado de los servicios:${NC}"
    docker compose ps
    exit 0
fi

# Iniciar servicios
echo -e "${BLUE}🚀 Iniciando servicios...${NC}"
echo ""

if [ -n "$BUILD_FLAG" ]; then
    echo -e "${YELLOW}🔨 Reconstruyendo imágenes...${NC}"
fi

docker compose up -d $BUILD_FLAG

echo ""
echo -e "${GREEN}✅ Servicios iniciados correctamente${NC}"
echo ""

# Esperar a que los servicios estén listos
echo -e "${YELLOW}⏳ Esperando a que los servicios estén listos...${NC}"
sleep 5

# Verificar estado de los servicios
echo ""
echo -e "${BLUE}📊 Estado de los servicios:${NC}"
docker compose ps
echo ""

# Mostrar URLs de acceso
echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║        🌐 URLs de Acceso              ║${NC}"
echo -e "${GREEN}╠════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║${NC} API:      http://localhost:8080      ${GREEN}║${NC}"
echo -e "${GREEN}║${NC} Health:   http://localhost:8080/actuator/health ${GREEN}║${NC}"
echo -e "${GREEN}║${NC} pgAdmin:  http://localhost:5050      ${GREEN}║${NC}"
echo -e "${GREEN}║${NC} PostgreSQL: localhost:5432           ${GREEN}║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════╝${NC}"
echo ""

# Mostrar credenciales
echo -e "${BLUE}🔑 Credenciales:${NC}"
echo "  PostgreSQL:"
echo "    - Usuario: postgres"
echo "    - Contraseña: postgres"
echo "    - Base de datos: dioburger"
echo ""
echo "  pgAdmin:"
echo "    - Email: admin@dioburger.com"
echo "    - Contraseña: admin"
echo ""

# Modo logs
if [ "$LOGS_MODE" = true ]; then
    echo -e "${BLUE}📋 Mostrando logs (Ctrl+C para salir)...${NC}"
    echo ""
    docker compose logs -f
fi

# Comandos útiles
echo -e "${YELLOW}💡 Comandos útiles:${NC}"
echo "  Ver logs:           docker compose logs -f"
echo "  Ver logs de API:    docker compose logs -f api"
echo "  Detener servicios:  docker compose down"
echo "  Reiniciar API:      docker compose restart api"
echo "  Estado:             docker compose ps"
echo ""

echo -e "${GREEN}✨ ¡Dio Burger API está listo!${NC}"
