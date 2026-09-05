# 🍔 Dio Burger - Frontend

**Next.js 16** | React 19 | Material UI 7 | TypeScript

Panel de administración y gestión para la plataforma Dio Burger.

---

## 📋 Contenido

- [Quick Start](#-quick-start)
- [Stack Tecnológico](#-stack-tecnológico)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Módulos](#-módulos)
- [Desarrollo](#-desarrollo)
- [Roadmap](#-roadmap)

---

## 🚀 Quick Start

### Prerrequisitos

- **Node.js 20** o superior
- **npm** o **pnpm**
- Backend ejecutándose en `http://localhost:8080`

### 1. Instalar dependencias

```bash
npm install
```

### 2. Configurar variables de entorno

Crear archivo `.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_WS_URL=http://localhost:8080
```

### 3. Ejecutar en desarrollo

```bash
npm run dev
```

La aplicación estará disponible en: **http://localhost:3000**

---

## 🛠 Stack Tecnológico

| Categoría | Tecnología |
|-----------|------------|
| **Framework** | Next.js 16 (App Router) |
| **UI Library** | React 19 |
| **Componentes** | Material UI 7 |
| **Lenguaje** | TypeScript 5 |
| **State Management** | Zustand |
| **HTTP Client** | Axios |
| **Formularios** | React Hook Form + Zod |
| **WebSocket** | Socket.io-client |
| **Date Utils** | date-fns |

---

## 📁 Estructura del Proyecto

```
front/
├── app/                    # App Router (páginas)
│   ├── page.tsx            # Landing page
│   ├── login/              # Autenticación
│   ├── admin/              # Panel de administración
│   │   ├── categorias/     # Gestión de categorías
│   │   ├── productos/      # Gestión de productos
│   │   ├── ingredientes/   # Gestión de ingredientes
│   │   ├── extras/         # Gestión de extras
│   │   ├── mesas/          # Gestión de mesas
│   │   ├── pedidos/        # Gestión de pedidos
│   │   ├── reportes/       # Analytics
│   │   └── configuracion/  # Config del local
│   ├── superadmin/         # Panel SuperAdmin
│   │   ├── locales/        # Gestión de locales
│   │   └── usuarios/       # Gestión de usuarios
│   └── cocina/             # Panel de cocina
│
├── components/             # Componentes reutilizables
│   ├── landing/            # Componentes de landing
│   ├── layout/             # Layouts (sidebar, navbar)
│   ├── shared/             # Componentes compartidos
│   └── superadmin/         # Componentes SuperAdmin
│
├── lib/                    # Utilidades
│   ├── api/                # Servicios API (axios)
│   ├── stores/             # Zustand stores
│   ├── hooks/              # Custom hooks
│   ├── context/            # React contexts
│   ├── theme/              # Tema Material UI
│   └── utils/              # Utilidades
│
├── types/                  # TypeScript interfaces
│   ├── api.ts              # Tipos de API
│   ├── usuario.ts          # Tipos de usuario
│   ├── pedido.ts           # Tipos de pedido
│   └── producto.ts         # Tipos de producto
│
└── public/                 # Assets estáticos
```

---

## 📦 Módulos

### Landing Page ✅
- Hero section
- Features
- Formulario de contacto
- Botón de WhatsApp

### Autenticación ✅
- Login con JWT
- Protección de rutas por rol
- Persistencia de sesión

### Panel SuperAdmin 🚧
- [x] Layout con sidebar
- [x] Gestión de locales
- [x] Gestión de usuarios
- [ ] Dashboard con métricas globales

### Panel Admin 🚧
- [x] Infraestructura de API
- [x] Tipos TypeScript
- [ ] Gestión de pedidos
- [ ] Gestión de productos
- [ ] Gestión de stock
- [ ] Reportes

### Panel Cocina ⏳
- [ ] Vista de pedidos en tiempo real
- [ ] WebSocket para actualizaciones
- [ ] Cambio de estados

---

## 💻 Desarrollo

### Scripts disponibles

```bash
# Desarrollo
npm run dev

# Build de producción
npm run build

# Iniciar producción
npm start

# Lint
npm run lint
```

### Crear nuevo componente

```typescript
// components/shared/MiComponente.tsx
'use client';

import { Box, Typography } from '@mui/material';

interface MiComponenteProps {
  titulo: string;
}

export default function MiComponente({ titulo }: MiComponenteProps) {
  return (
    <Box>
      <Typography variant="h6">{titulo}</Typography>
    </Box>
  );
}
```

### Crear nuevo servicio API

```typescript
// lib/api/miServicio.ts
import api from './axios';

export const miServicioApi = {
  getAll: async () => {
    const response = await api.get('/endpoint');
    return response.data;
  },
  
  create: async (data: CreateDTO) => {
    const response = await api.post('/endpoint', data);
    return response.data;
  }
};
```

---

## 🗺 Roadmap

Ver el [README de la raíz](../README.md) para el estado del proyecto y lo que falta.

### Estado actual

| Fase | Descripción | Estado |
|------|-------------|--------|
| 0 | Setup y configuración | ✅ Completado |
| 1 | Landing Page | ✅ Completado |
| 2 | Autenticación | ✅ Completado |
| 3-5 | Infraestructura Admin | ✅ Completado |
| 6 | Panel Admin - Pedidos | 🚧 En progreso |
| 7 | Panel Admin - Menú | ⏳ Pendiente |
| 8 | Panel Admin - Stock | ⏳ Pendiente |
| 9 | Panel Admin - Reservas | ⏳ Pendiente |
| 10 | Panel Cocina | ⏳ Pendiente |
| 11 | Reportes | ⏳ Pendiente |
| 12 | Optimizaciones | ⏳ Pendiente |

---

## 🎨 Tema

### Colores principales

```typescript
primary: '#FF6B35'    // Naranja hamburguesa
secondary: '#2D3142'  // Gris oscuro
success: '#4CAF50'
warning: '#FFC107'
error: '#F44336'
background: '#F4F4F4'
```

### Tipografía

- Headings: Inter Bold
- Body: Inter Regular

---

## 📄 Licencia

Proyecto privado - Uso interno de Dio Burger.

---

**Dio Burger Team**
