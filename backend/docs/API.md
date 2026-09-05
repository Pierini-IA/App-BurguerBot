# 📚 Documentación de la API - Dio Burger

## 🔗 Acceso Rápido

| Recurso | URL |
|---------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/v3/api-docs |
| **Health Check** | http://localhost:8080/api/health |

---

## 📋 Índice de Módulos

1. [Autenticación](#autenticación)
2. [Menú Público](#menú-público)
3. [Bot WhatsApp](#bot-whatsapp)
4. [Panel de Cocina](#panel-de-cocina)
5. [Administración](#administración)
6. [Super Admin](#super-admin)
7. [Planes de Suscripción](#planes-de-suscripción)
8. [Reportes](#reportes)

---

## 🔐 Autenticación

### POST `/api/auth/login`

Autentica usuarios y devuelve un token JWT.

**Request:**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "rol": "ROLE_ADMIN",
  "telefonoLocal": "+5491187654321"
}
```

**Uso del token:**
```
Authorization: Bearer {token}
```

---

## 🍔 Menú Público

**Sin autenticación requerida**

### GET `/api/menu/{telefonoLocal}`

Obtiene el menú completo del local agrupado por categorías.

**Response (200):**
```json
{
  "local": {
    "nombre": "Dio Burger Palermo",
    "direccion": "Av. Córdoba 1234",
    "telefono": "5491187654321"
  },
  "categorias": [
    {
      "id": 1,
      "nombre": "Hamburguesas",
      "productos": [
        {
          "id": 1,
          "nombre": "Clásica",
          "precio": 9500.0,
          "extrasDisponibles": [...]
        }
      ]
    }
  ],
  "horariosSugeridos": ["20:00", "20:15", "20:30"],
  "modalidadesPermitidas": ["DELIVERY", "RETIRAR"]
}
```

---

## 🤖 Bot WhatsApp

**Sin autenticación (acceso público)**

| Método | Endpoint | Descripción | Plan |
|--------|----------|-------------|------|
| GET | `/api/bot/menu/{telefono}` | Menú completo | ESTÁNDAR+ |
| POST | `/api/bot/pedido/{telefono}` | Crear pedido | ESTÁNDAR+ |
| POST | `/api/bot/reserva/{telefono}` | Crear reserva | ESTÁNDAR+ |
| POST | `/api/bot/pedido/{tel}/{id}/asignar-repartidor` | Asignar delivery | PREMIUM |

---

## 👨‍🍳 Panel de Cocina

**Requiere: ROLE_COCINA o ROLE_ADMIN**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/cocina/pedidos` | Pedidos activos |
| POST | `/api/cocina/pedidos/{id}/marcar-listo` | Marcar como listo |
| POST | `/api/cocina/pedidos/{id}/marcar-en-camino` | Marcar en camino |
| POST | `/api/cocina/pedidos/{id}/entregar` | Marcar entregado |
| PATCH | `/api/cocina/pedidos/{id}/estado` | Cambiar estado |

---

## 🔧 Administración

**Requiere: ROLE_ADMIN o ROLE_SUPERADMIN**

### Productos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/admin/productos` | Listar productos |
| POST | `/api/admin/productos` | Crear producto |
| PUT | `/api/admin/productos/{id}` | Actualizar |
| DELETE | `/api/admin/productos/{id}` | Eliminar |

### Categorías

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/admin/categorias` | Listar categorías |
| POST | `/api/admin/categorias` | Crear categoría |
| PUT | `/api/admin/categorias/{id}` | Actualizar |
| DELETE | `/api/admin/categorias/{id}` | Eliminar |

### Extras

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/admin/extras` | Listar extras |
| POST | `/api/admin/extras` | Crear extra |
| PUT | `/api/admin/extras/{id}` | Actualizar |
| DELETE | `/api/admin/extras/{id}` | Eliminar |

### Ingredientes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/admin/ingredientes` | Listar ingredientes |
| POST | `/api/admin/ingredientes` | Crear ingrediente |
| PUT | `/api/admin/ingredientes/{id}/stock` | Actualizar stock |

### Mesas

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/admin/mesas` | Listar mesas |
| POST | `/api/admin/mesas` | Crear mesa |
| DELETE | `/api/admin/mesas/{id}` | Eliminar mesa |

---

## 👑 Super Admin

**Requiere: ROLE_SUPERADMIN**

### Locales

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/admin/locales` | Listar locales |
| POST | `/api/admin/locales` | Crear local |
| PUT | `/api/admin/locales/{id}` | Actualizar local |
| DELETE | `/api/admin/locales/{id}` | Eliminar local |

### Usuarios

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/admin/usuarios` | Listar usuarios |
| POST | `/api/admin/usuarios` | Crear usuario |

---

## 💳 Planes de Suscripción

**Requiere: ROLE_SUPERADMIN**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/superadmin/planes` | Listar planes disponibles |
| GET | `/api/superadmin/locales/{id}/plan` | Ver plan del local |
| PUT | `/api/superadmin/locales/{id}/plan` | Cambiar plan |
| POST | `/api/superadmin/locales/{id}/plan/activar` | Activar plan |
| POST | `/api/superadmin/locales/{id}/plan/desactivar` | Desactivar plan |

### HTTP 402 - Feature no disponible

Cuando un endpoint requiere una feature del plan no disponible:

```json
{
  "error": "Feature no disponible",
  "feature": "REPORTES_AVANZADOS",
  "planActual": "BASICO",
  "planMinimo": "PREMIUM",
  "mensaje": "Esta funcionalidad requiere plan PREMIUM o superior"
}
```

---

## 📊 Reportes

**Requiere: Plan PREMIUM**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/reportes/ventas/diarias` | Ventas por día |
| GET | `/api/reportes/ventas/semanales` | Ventas por semana |
| GET | `/api/reportes/ventas/mensuales` | Ventas por mes |
| GET | `/api/reportes/productos/top` | Productos más vendidos |
| GET | `/api/reportes/dashboard` | Dashboard KPIs |

---

## 🌐 WebSocket

### Conexión STOMP

```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  stompClient.subscribe('/topic/pedidos/{telefonoLocal}', (message) => {
    const pedido = JSON.parse(message.body);
    console.log('Nuevo pedido:', pedido);
  });
});
```

### Topics Disponibles

- `/topic/pedidos/{telefonoLocal}` - Nuevos pedidos
- `/topic/pedidos/{telefonoLocal}/actualizaciones` - Cambios de estado

---

## 📝 Códigos de Estado

| Código | Significado |
|--------|-------------|
| 200 | OK |
| 201 | Creado |
| 204 | Sin contenido (eliminado) |
| 400 | Request inválido |
| 401 | No autenticado |
| 402 | Feature no disponible (plan) |
| 403 | No autorizado |
| 404 | No encontrado |
| 409 | Conflicto (duplicado) |
| 422 | Error de validación |
| 500 | Error del servidor |
