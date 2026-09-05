# 🔒 Guía de Seguridad

---

## 📋 Variables de Entorno

### Configuración Inicial

```bash
# Copiar template
cp .env.example .env

# Editar con tus credenciales
notepad .env  # Windows
nano .env     # Linux/Mac
```

### Variables Requeridas

| Variable | Descripción | Requerida | Default |
|----------|-------------|:---------:|---------|
| `DATABASE_URL` | URL PostgreSQL | ⚠️ | jdbc:postgresql://localhost:5432/dioburger |
| `DATABASE_USERNAME` | Usuario DB | ⚠️ | postgres |
| `DATABASE_PASSWORD` | Password DB | 🔴 **SÍ** | - |
| `JWT_SECRET` | Secret para JWT (32+ chars) | 🔴 **SÍ** | - |
| `JWT_EXPIRATION` | Expiración JWT (ms) | ⚠️ | 86400000 (24h) |
| `BOT_API_KEY` | API Key del bot | ⚠️ | - |

---

## 🔑 Generación de Secrets

### JWT Secret (recomendado 64 caracteres)

```bash
# Linux/Mac/Git Bash
openssl rand -base64 32

# PowerShell (Windows)
-join ((65..90) + (97..122) + (48..57) | Get-Random -Count 32 | ForEach-Object {[char]$_})
```

### Requisitos del JWT_SECRET

- ✅ Mínimo 32 caracteres
- ✅ Combinar letras, números y símbolos
- ✅ Diferente en cada entorno (dev, staging, prod)
- ❌ NUNCA commitear a Git

---

## ⚠️ Mejores Prácticas

### ❌ NO HACER

- ❌ Commitear `.env` a Git
- ❌ Compartir credenciales por email/Slack
- ❌ Usar contraseñas débiles
- ❌ Reutilizar secrets entre entornos

### ✅ SÍ HACER

- ✅ Usar `.env.example` como template
- ✅ Generar secrets aleatorios fuertes
- ✅ Rotar secrets periódicamente
- ✅ Usar gestores de secrets en producción

---

## 🏭 Configuración en Producción

### Docker Compose

```yaml
services:
  app:
    environment:
      - DATABASE_PASSWORD=${DATABASE_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
    env_file:
      - .env
```

### Kubernetes Secrets

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: dioburger-secrets
type: Opaque
data:
  database-password: <base64-encoded>
  jwt-secret: <base64-encoded>
```

### Cloud Secrets Manager

```bash
# AWS Secrets Manager
aws secretsmanager create-secret \
  --name dioburger/jwt-secret \
  --secret-string "your-secret-here"

# Azure Key Vault
az keyvault secret set \
  --vault-name dioburger-vault \
  --name jwt-secret \
  --value "your-secret-here"
```

---

## 🧪 Validación

La aplicación valida automáticamente al iniciar:

**✅ Éxito:**
```
✅ JWT Secret validado correctamente (64 caracteres)
✅ Variables de entorno cargadas desde .env (8 variables)
```

**❌ Error:**
```
❌ IllegalStateException: JWT_SECRET no está configurado.
   Debes configurar la variable de entorno JWT_SECRET antes de iniciar.
```

---

## 🔐 Autenticación JWT

### Flujo de Autenticación

1. Usuario envía credenciales a `POST /api/auth/login`
2. Backend valida y genera JWT
3. Cliente guarda token en localStorage
4. Todas las requests incluyen `Authorization: Bearer {token}`
5. Backend valida token en cada request

### Estructura del Token

```json
{
  "sub": "admin",
  "rol": "ROLE_ADMIN",
  "telefonoLocal": "+5491187654321",
  "iat": 1698052800,
  "exp": 1698139200
}
```

### Niveles de Acceso

| Rol | Descripción | Acceso |
|-----|-------------|--------|
| **SUPERADMIN** | Administrador global | Todo el sistema |
| **ADMIN** | Administrador del local | Solo su local |
| **COCINA** | Personal de cocina | Ver/actualizar pedidos |

---

## 🛡️ Protección de Endpoints

### Públicos (Sin autenticación)
- `/api/auth/login`
- `/api/menu/{telefono}`
- `/api/bot/**`
- `/api/ping`
- `/api/health`

### Autenticados
- `/api/admin/**` → ADMIN o SUPERADMIN
- `/api/cocina/**` → COCINA, ADMIN o SUPERADMIN
- `/api/local/**` → ADMIN
- `/api/superadmin/**` → Solo SUPERADMIN
- `/api/reportes/**` → ADMIN + Plan PREMIUM

---

## 📝 Cambios de Seguridad v2.2.0

### Antes (🔴 Inseguro)
```yaml
datasource:
  password: "contraseña_hardcodeada"
security:
  jwt:
    secret: ${JWT_SECRET:default-inseguro}
```

### Después (✅ Seguro)
```yaml
datasource:
  password: ${DATABASE_PASSWORD}  # Sin default
security:
  jwt:
    secret: ${JWT_SECRET}  # Sin default - obligatorio
```

### Validación en Runtime
```java
@PostConstruct
private void validateSecret() {
    if (secret == null || secret.length() < 32) {
        throw new IllegalStateException("JWT_SECRET inválido");
    }
}
```
