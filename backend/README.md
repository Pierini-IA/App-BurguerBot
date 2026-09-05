# 🍔 Dio Burger - Backend API

**Versión 2.3.0** | Spring Boot 3.2 | Java 21 | PostgreSQL

Backend Multi-Tenancy para la gestión de hamburgueserías con sistema de planes de suscripción.

---

## 📋 Contenido

- [Quick Start](#-quick-start)
- [Stack Tecnológico](#-stack-tecnológico)
- [Arquitectura](#-arquitectura)
- [Documentación](#-documentación)
- [Testing](#-testing)
- [Docker](#-docker)

---

## 🚀 Quick Start

### Prerrequisitos

- **Java 21** o superior
- **PostgreSQL 15** o superior  
- **Maven 3.8+**

### 1. Configurar Base de Datos

```sql
CREATE DATABASE dioburger;
CREATE USER dioburger_user WITH PASSWORD 'tu_password';
GRANT ALL PRIVILEGES ON DATABASE dioburger TO dioburger_user;
```

### 2. Configurar Variables de Entorno

```bash
cp .env.example .env
```

Editar `.env`:
```env
DATABASE_PASSWORD=tu_password
JWT_SECRET=tu_secret_de_al_menos_32_caracteres
```

### 3. Ejecutar

```bash
# Compilar
mvn clean install -DskipTests

# Ejecutar
mvn spring-boot:run
```

### 4. Verificar

- **API**: http://localhost:8080
- **Swagger**: http://localhost:8080/swagger-ui.html
- **Health**: http://localhost:8080/api/health

### 5. Credenciales por Defecto

| Usuario | Password | Rol |
|---------|----------|-----|
| superadmin | SuperAdmin123! | SUPERADMIN |

> ⚠️ Cambiar en producción

---

## 🛠 Stack Tecnológico

| Categoría | Tecnología |
|-----------|------------|
| **Framework** | Spring Boot 3.2.0 |
| **Lenguaje** | Java 21 |
| **Base de Datos** | PostgreSQL 15+ |
| **Migraciones** | Flyway |
| **Seguridad** | Spring Security + JWT |
| **WebSocket** | Spring WebSocket (STOMP) |
| **AOP** | Spring AOP (validación de planes) |
| **Mapeo** | MapStruct |
| **Documentación** | OpenAPI 3.0 (Swagger) |
| **Build** | Maven |

---

## 🏗 Arquitectura

```
com.dioburger/
├── config/           # Configuración Spring
├── controller/       # REST Controllers
├── service/          # Lógica de negocio
├── repository/       # JPA Repositories
├── model/
│   ├── entity/       # Entidades JPA
│   ├── dto/          # Data Transfer Objects
│   └── enums/        # Enumeraciones
├── mapper/           # MapStruct Mappers
├── security/         # JWT & Filtros
├── aspect/           # AOP (validación planes)
├── channels/         # Adapters de canales Meta (WhatsApp, IG, FB) - reemplaza a n8n
├── ai/               # Agente de IA (OpenAI + Gemini) que arma pedidos desde el chat
└── exception/        # Excepciones custom
```

### Bot de WhatsApp/Instagram/Facebook (integración directa con Meta)

Los webhooks de Meta llegan a `MetaWebhookController` (`/api/webhooks/meta/{canal}/{telefonoLocal}`, público). El flujo es: `ChannelAdapter` normaliza el mensaje → `RouterService` clasifica la intención → `AgentEngineService` corre el loop de tool-calling contra OpenAI/Gemini, con `BurgerToolsService` invocando los servicios de dominio existentes (`PedidoService`, `MenuService`, `ReservaService`).

Variables de entorno necesarias (ver `.env.example`):
- `META_WEBHOOK_VERIFY_TOKEN`, `META_GRAPH_API_VERSION`, `DRY_RUN_META_SEND`
- `OPENAI_API_KEY`, `OPENAI_MODEL`
- `GOOGLE_AI_API_KEY`, `GEMINI_MODEL`

Los tokens específicos por local (WhatsApp Phone ID, IG/FB tokens) se guardan en `configuracion_local` (migración `V9__add_meta_config.sql`), no en variables de entorno globales.

### Principios

- ✅ **Multi-Tenancy** por teléfono único del local
- ✅ **SOLID** principles
- ✅ **DRY** - Código reutilizable
- ✅ **Feature-gating** con @RequiresFeature

---

## 📚 Documentación

| Documento | Descripción |
|-----------|-------------|
| [docs/API.md](docs/API.md) | Documentación de endpoints |
| [docs/PLANES.md](docs/PLANES.md) | Sistema de suscripción |
| [docs/SEGURIDAD.md](docs/SEGURIDAD.md) | Configuración de seguridad |
| [docs/TESTS.md](docs/TESTS.md) | Guía de testing |

### Swagger UI

Documentación interactiva: http://localhost:8080/swagger-ui.html

---

## 🧪 Testing

```bash
# Ejecutar tests
mvn clean test

# Con cobertura
mvn clean test jacoco:report
start target/site/jacoco/index.html
```

**Estado actual:** 66 tests | 100% passing

Ver [docs/TESTS.md](docs/TESTS.md) para más detalles.

---

## 🐳 Docker

### Build

```bash
docker build -t dioburger-api .
```

### Docker Compose

```bash
docker-compose up -d
```

El archivo `docker-compose.yml` incluye PostgreSQL configurado.

---

## 📝 Changelog

### v2.3.0 (Julio 2026)
- ✨ Integración directa con Meta Graph API (WhatsApp, comentarios IG/FB), sin n8n
- ✨ Agente de IA con tool-calling (OpenAI para texto, Gemini para imágenes)
- ✨ Credenciales de Meta por local (migración V9)

### v2.2.0 (Octubre 2025)
- ✨ Sistema de planes de suscripción (BÁSICO, ESTÁNDAR, PREMIUM)
- ✨ Validación AOP con @RequiresFeature
- ✨ 21 features configurables por plan
- 🔒 Migración de credenciales a variables de entorno

### v2.1.0 (Septiembre 2025)
- ✨ Sistema de categorías personalizables
- ✨ Endpoint público `/api/menu/{telefono}`
- ✨ Extras obligatorios y opcionales

### v2.0.0 (Agosto 2025)
- ✨ Multi-tenancy por teléfono
- ✨ Sistema de extras/adicionales
- ✨ Integración con bot WhatsApp

---

## 🤝 Contribución

1. Fork el proyecto
2. Crear rama (`git checkout -b feature/nueva-funcionalidad`)
3. Commit (`git commit -m 'Add: nueva funcionalidad'`)
4. Push (`git push origin feature/nueva-funcionalidad`)
5. Pull Request

---

## 📄 Licencia

Proyecto privado - Uso interno de Dio Burger.

---

**Dio Burger Team** | Versión 2.3.0
