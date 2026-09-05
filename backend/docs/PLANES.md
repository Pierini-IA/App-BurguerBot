# 💳 Sistema de Planes de Suscripción

**Versión**: 2.2.0

---

## 📋 Planes Disponibles

### 📦 Plan BÁSICO - $15.000/mes
**Ideal para locales pequeños que recién empiezan**

**Incluye:**
- ✅ Panel Web de Gestión
- ✅ Gestión de Productos y Menú
- ✅ Gestión de Ingredientes y Stock
- ✅ Panel de Cocina
- ✅ Estadísticas Básicas de Ventas
- ✅ Gestión de Clientes

### 🌟 Plan ESTÁNDAR - $35.000/mes
**El más popular - Todo lo básico + atención automatizada**

**Incluye todo lo del Plan Básico +**
- ✅ Bot de WhatsApp Inteligente
- ✅ Sistema de Reservas de Mesas
- ✅ Actualizaciones en Tiempo Real (WebSocket)

### 💎 Plan PREMIUM - $65.000/mes
**Para operaciones profesionales y grandes volúmenes**

**Incluye todo lo del Plan Estándar +**
- ✅ Reportes Avanzados y Analytics
- ✅ Webhooks Personalizados
- ✅ Impresión Automática de Tickets
- ✅ Dashboard Avanzado
- ✅ Análisis de Productos
- ✅ Asignación de Repartidores
- ✅ Soporte Prioritario

---

## 🔧 Implementación Técnica

### Arquitectura

```
Controller (@RequiresFeature) 
    → AOP (PlanValidationAspect) 
    → PlanService.validarAcceso() 
    → PlanFeatureMatrix
```

### Anotación @RequiresFeature

```java
@RequiresFeature(Feature.REPORTES_AVANZADOS)
@GetMapping("/reportes/ventas")
public ResponseEntity<?> getReporteVentas() {
    // Solo se ejecuta si el local tiene acceso
}
```

### Features Disponibles

#### Básicas (Todos los planes)
| Feature | Descripción |
|---------|-------------|
| `PANEL_WEB` | Panel web de gestión |
| `GESTION_PRODUCTOS` | CRUD de productos |
| `GESTION_INGREDIENTES` | CRUD de ingredientes |
| `GESTION_CATEGORIAS` | CRUD de categorías |
| `GESTION_EXTRAS` | CRUD de extras |
| `PANEL_COCINA` | Panel de cocina |
| `PEDIDOS_PANEL` | Gestión de pedidos |
| `CLIENTES_PANEL` | Gestión de clientes |
| `ESTADISTICAS_BASICAS` | Estadísticas básicas |

#### Estándar (Plan ESTÁNDAR+)
| Feature | Descripción |
|---------|-------------|
| `BOT_WHATSAPP` | Bot de WhatsApp |
| `SISTEMA_RESERVAS` | Sistema de reservas |
| `WEBSOCKET_TIEMPO_REAL` | WebSocket en tiempo real |

#### Premium (Solo PREMIUM)
| Feature | Descripción |
|---------|-------------|
| `REPORTES_AVANZADOS` | Reportes y analytics |
| `WEBHOOKS_PERSONALIZADOS` | Webhooks custom |
| `IMPRESION_AUTOMATICA` | Impresión automática |
| `DASHBOARD_AVANZADO` | Dashboard avanzado |
| `ANALISIS_PRODUCTOS` | Análisis de productos |
| `ASIGNACION_REPARTIDORES` | Asignar repartidores |

---

## 🔌 API Endpoints

### Listar Planes
```
GET /api/superadmin/planes
```

**Response:**
```json
[
  {
    "nombre": "BASICO",
    "precio": 15000,
    "features": ["PANEL_WEB", "GESTION_PRODUCTOS", ...]
  },
  {
    "nombre": "ESTANDAR",
    "precio": 35000,
    "features": ["BOT_WHATSAPP", "SISTEMA_RESERVAS", ...]
  },
  {
    "nombre": "PREMIUM",
    "precio": 65000,
    "features": ["REPORTES_AVANZADOS", ...]
  }
]
```

### Consultar Plan de un Local
```
GET /api/superadmin/locales/{localId}/plan
```

**Response:**
```json
{
  "localId": 1,
  "nombreLocal": "Dio Burger Centro",
  "plan": "ESTANDAR",
  "planActivo": true,
  "fechaCambioPlan": "2024-10-15T10:30:00",
  "featuresDisponibles": [
    "PANEL_WEB",
    "GESTION_PRODUCTOS",
    "BOT_WHATSAPP",
    "SISTEMA_RESERVAS"
  ]
}
```

### Cambiar Plan
```
PUT /api/superadmin/locales/{localId}/plan
```

**Request:**
```json
{
  "nuevoPlan": "PREMIUM",
  "motivo": "Cliente solicitó upgrade para reportes"
}
```

### Activar/Desactivar Plan
```
POST /api/superadmin/locales/{localId}/plan/activar
POST /api/superadmin/locales/{localId}/plan/desactivar
```

---

## ⚠️ Manejo de Errores

### HTTP 402 - Payment Required

Cuando se intenta acceder a una feature no disponible:

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

## 🗄️ Base de Datos

### Campos en tabla `locales`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `plan_suscripcion` | VARCHAR | BASICO, ESTANDAR, PREMIUM |
| `fecha_cambio_plan` | TIMESTAMP | Fecha del último cambio |
| `motivo_cambio_plan` | TEXT | Motivo del cambio |
| `plan_activo` | BOOLEAN | Si el plan está activo |

### Migración

```sql
-- V8__add_subscription_plans.sql
ALTER TABLE locales 
ADD COLUMN plan_suscripcion VARCHAR(20) DEFAULT 'BASICO',
ADD COLUMN fecha_cambio_plan TIMESTAMP,
ADD COLUMN motivo_cambio_plan TEXT,
ADD COLUMN plan_activo BOOLEAN DEFAULT true;
```
