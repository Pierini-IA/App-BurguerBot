# 🍔 Dio Burger

Sistema de gestión para hamburgueserías, con un bot de WhatsApp que toma los pedidos solo.

**Proyecto Final** · Adrian Perez · 2026

---

## 🌐 Probalo ahora

| | |
|---|---|
| **El sistema** | **https://dioburger-front.onrender.com** |
| **Video explicativo** | **https://drive.google.com/file/d/1rW-rmuCcCEKDbVV8X7fS24NzCr7xQI3a/view?usp=sharing** |

| Usuario | Contraseña | Quién es |
|---|---|---|
| `superadmin` | `SuperAdmin123!` | Administra la plataforma, da de alta hamburgueserías |
| `admin_belgrano` | `Admin123!` | Dueño de Dio Burger Belgrano — menú, stock, números |
| `cocina_belgrano` | `Cocina123!` | Cocina (`/cocina`) y mostrador (`/mostrador`) |

> ⏱️ **La primera vez tarda entre 3 y 4 minutos en cargar.** No está roto: el plan gratuito de Render apaga el servidor cuando nadie lo usa. Ver [Limitaciones](#7-limitaciones-conocidas).

---

## Índice

1. [Qué es](#1-qué-es)
2. [Qué hace](#2-qué-hace)
3. [Correrlo en tu máquina](#3-correrlo-en-tu-máquina)
4. [Cómo está hecho](#4-cómo-está-hecho)
5. [Estructura del proyecto](#5-estructura-del-proyecto)
6. [Documentación](#6-documentación)
7. [Limitaciones conocidas](#7-limitaciones-conocidas)
8. [Qué falta / próximos pasos](#8-qué-falta--próximos-pasos)

---

## 1. Qué es

**El problema.** Una hamburguesería recibe pedidos por WhatsApp, por teléfono y en el mostrador. Alguien los anota en un papel, se los canta a la cocina, y al final del día nadie sabe bien cuánto se vendió ni cuánta carne queda. Se pierden pedidos, se venden cosas que no hay, y el dueño no tiene números para decidir.

**La solución.** El cliente le escribe por WhatsApp a la hamburguesería como le escribiría a una persona. Del otro lado hay un asistente que entiende el pedido, lo carga solo y lo manda directo a la pantalla de la cocina. El dueño ve todo desde un panel: el menú, el stock, los pedidos y la facturación del día.

**El cliente no entra al sistema.** Solo escribe por WhatsApp. No hay que enseñarle a usar nada.

**Varias hamburgueserías en el mismo sistema.** Cada una es un local independiente: su menú, sus pedidos, sus usuarios. Ninguna ve nada de las otras.

---

## 2. Qué hace

### Los cuatro perfiles

| Quién | Qué puede hacer |
|---|---|
| **Superadmin** | Da de alta hamburgueserías y sus usuarios. Ve todos los locales |
| **Dueño** (admin) | Carga el menú y las recetas, controla el stock, ve los pedidos y la facturación |
| **Cocina** | Ve entrar los pedidos en tiempo real y los va marcando mientras los prepara |
| **Mostrador** | Imprime la comanda y marca el pedido como entregado |

### El bot de WhatsApp

Entiende lo que le escriben y actúa. Sabe:

- **Armar un pedido** a partir de un mensaje suelto ("quiero 2 dobles y unas papas para retirar")
- **Modificar** un pedido que ya hizo
- **Cancelarlo** — y devolver los ingredientes al stock
- **Decir en qué estado está** el pedido
- **Contestar** horarios, dirección y si hacen delivery

Y **se acuerda de la conversación**: el cliente puede pedir de a poco ("quiero dos dobles" → "¿para retirar o delivery?" → "para retirar, confirmá") sin tener que repetir nada.

### El stock que se lleva solo

Cada producto tiene su **receta**: de qué está hecho y cuánto lleva de cada cosa. Cuando se vende una Doble Cheddar, el sistema descuenta el pan, los dos medallones y las dos fetas de queso. Si se cancela el pedido, los devuelve.

Además calcula cuántas unidades de cada hamburguesa se pueden hacer, mirando el ingrediente que primero se acaba.

### Los planes

Cada hamburguesería contrata un plan, y eso define qué funciones tiene habilitadas:

| | Básico | Estándar | Premium |
|---|:---:|:---:|:---:|
| Panel y pantalla de cocina | ✅ | ✅ | ✅ |
| Bot de WhatsApp | ❌ | ✅ | ✅ |
| Reservas | ❌ | ✅ | ✅ |
| Reportes avanzados | ❌ | ❌ | ✅ |

---

## 3. Correrlo en tu máquina

### Lo que hace falta

- **Java 21** y **Maven 3.8+**
- **Node.js 20+**
- **PostgreSQL 15+** (o Docker, ver más abajo)

### 1. Bajar el código

```bash
git clone https://github.com/Pierini-IA/App-BurguerBot.git
cd App-BurguerBot
```

### 2. Levantar el servidor

```bash
cd backend
cp .env.example .env      # completar DATABASE_PASSWORD y JWT_SECRET
mvn clean install
mvn spring-boot:run
```

Queda en **http://localhost:8080**.

> **Atajo con Docker**, si no querés instalar Java ni PostgreSQL:
> ```bash
> cd backend
> docker compose -f docker-compose.local.yml up -d --build
> ```
> Levanta la base y el servidor juntos.

### 3. Levantar el panel

```bash
cd front
cp .env.example .env.local
npm install
npm run dev
```

Queda en **http://localhost:3000**.

### 4. Entrar

La primera vez que arranca contra una base vacía, el sistema **carga datos de ejemplo solo**: tres hamburgueserías con su menú, sus recetas y sus usuarios.

| Usuario | Contraseña | Rol | Local |
|---|---|---|---|
| `superadmin` | `SuperAdmin123!` | SUPERADMIN | — |
| `admin_recoleta` | `Admin123!` | ADMIN | Dio Gourmet Recoleta (premium) |
| `cocina_recoleta` | `Cocina123!` | COCINA | Dio Gourmet Recoleta |
| `admin_palermo` | `Admin123!` | ADMIN | Dio Burger & Pizza Palermo (estándar) |
| `cocina_palermo` | `Cocina123!` | COCINA | Dio Burger & Pizza Palermo |
| `admin_centro` | `Admin123!` | ADMIN | Burger Express Centro (básico) |

> El bot de WhatsApp **no funciona en local** salvo que configures credenciales de Meta. Todo lo demás sí: el panel, la cocina, el mostrador y los pedidos cargados a mano.

### 5. Probar que anda

```bash
cd backend && mvn test          # tests del servidor
cd front && npm run e2e         # recorrido completo en el navegador
```

Los del navegador necesitan el servidor corriendo — ver [`docs/PRUEBAS-LOCALES.md`](docs/PRUEBAS-LOCALES.md).

---

## 4. Cómo está hecho

```
   Cliente por WhatsApp
            │
            ▼
   ┌─────────────────┐        ┌──────────────┐
   │    Servidor     │───────▶│  OpenAI      │  interpreta el mensaje
   │  (Java/Spring)  │        └──────────────┘
   │                 │
   │                 │───────▶  PostgreSQL      guarda todo
   │                 │
   └────────┬────────┘
            │  avisa al instante
            ▼
   ┌─────────────────┐
   │  Panel web      │  dueño · cocina · mostrador
   │  (Next.js)      │
   └─────────────────┘
```

| Parte | Con qué |
|---|---|
| Servidor | Java 21, Spring Boot 3.2, PostgreSQL, autenticación con JWT |
| Panel | Next.js 16, React 19, TypeScript, Material UI |
| Tiempo real | WebSocket — los pedidos entran a la pantalla de cocina sin refrescar |
| Bot | Integración directa con WhatsApp (API de Meta) + OpenAI para entender los mensajes |
| Deploy | **Render** — servidor, panel y base de datos |

### Decisiones que vale la pena contar

**Cada hamburguesería se identifica por su teléfono.** No hay una base por cliente ni un servidor por cliente: es una sola base, y cada consulta filtra por el local. Simple y suficiente para la escala del problema.

**El asistente decide, no sigue un árbol.** No hay una lista de respuestas programadas. Al modelo se le dan herramientas —crear pedido, modificarlo, cancelarlo, consultar horarios— y él decide cuál usar según lo que le escribieron.

**Los permisos se controlan en el servidor.** No alcanza con esconder botones: un usuario de cocina que intente tocar la configuración por fuera del panel recibe un rechazo igual.

---

## 5. Estructura del proyecto

```
App-BurguerBot/
├── backend/              # Servidor Java + Spring Boot
│   ├── src/main/java/    # Código
│   ├── src/main/resources/
│   │   ├── db/migration/ # Migraciones de la base
│   │   └── data/         # Datos de ejemplo
│   ├── src/test/         # Tests
│   └── docs/             # Documentación técnica del servidor
│
├── front/                # Panel web Next.js
│   ├── app/              # Pantallas
│   ├── components/       # Componentes
│   ├── lib/              # Utilidades y llamadas al servidor
│   └── e2e/              # Tests de navegador
│
├── docs/                 # Documentación del proyecto
├── render.yaml           # Configuración del deploy
└── README.md             # Este archivo
```

---

## 6. Documentación

| Documento | De qué habla |
|---|---|
| [`docs/informe-proyecto.md`](docs/informe-proyecto.md) | Informe de cátedra |
| [`docs/PRUEBAS-LOCALES.md`](docs/PRUEBAS-LOCALES.md) | Cómo levantar todo en local y correr las pruebas |
| [`backend/README.md`](backend/README.md) | Guía del servidor |
| [`backend/docs/API.md`](backend/docs/API.md) | Los endpoints, uno por uno |
| [`backend/docs/PLANES.md`](backend/docs/PLANES.md) | Cómo funcionan los planes por dentro |
| [`backend/docs/SEGURIDAD.md`](backend/docs/SEGURIDAD.md) | Autenticación y permisos |
| [`backend/docs/TESTS.md`](backend/docs/TESTS.md) | Cómo están armados los tests |
| [`front/README.md`](front/README.md) | Guía del panel |

---

## 7. Limitaciones conocidas

Cosas que hoy son así y conviene saber antes de probar.

### Del deploy en Render (plan gratuito)

**El servidor se apaga solo.** Si nadie usa el sistema por 15 minutos, Render lo apaga. La siguiente visita lo tiene que prender, y eso **tarda entre 3 y 4 minutos**. Después anda normal. Si vas a mostrarlo, abrilo 10 minutos antes.

**La base de datos gratuita vence.** Las bases del plan gratuito de Render duran 30 días. La actual **vence el 2026-10-04**; después de esa fecha hay que crear una nueva o pasar a un plan pago.

**Poca memoria.** El servidor tiene 512 MB, que para Java es ajustado. Anda bien con el uso de una demo, pero no aguanta carga real sin subir de plan.

### Del bot

**Tarda unos 30 segundos por mensaje.** Espera unos segundos por si el cliente sigue escribiendo, y después consulta al modelo de IA. En una charla de tres mensajes son un minuto y medio de esperas.

**Se acuerda de las últimas 2 horas, y de los últimos 10 mensajes.** Más viejo que eso lo olvida. Los dos límites se ajustan por variable de entorno (`MEMORIA_MINUTOS_VIGENCIA` y `MEMORIA_MAX_MENSAJES`); cuanto más recuerda, más caro y más lento responde.

**El número de WhatsApp es de prueba.** Está usando un número en modo de desarrollo de Meta, no uno productivo de la hamburguesería.

### Del sistema

**Los ingresos del día suman los pedidos cancelados.** Un pedido cancelado deja de ocupar la cocina y devuelve el stock correctamente, pero sigue contando en la facturación del panel. Es un error de cálculo del reporte, no del pedido.

**Faltan pantallas de mesas y reservas.** El servidor ya las soporta; el panel todavía no las muestra.

**Los reportes con gráficos no están hechos.** Los números del día sí se ven en el panel principal.

---

## 8. Qué falta / próximos pasos

- **Pantallas de mesas y reservas** en el panel
- **Reportes con gráficos**: ventas por día, productos más vendidos
- **App para el celular** de la cocina
- **Más canales**: Instagram, Facebook y chat en la web

---

## Glosario

| Palabra | Qué significa acá |
|---|---|
| **Local** | Una hamburguesería. Cada una tiene su menú, sus pedidos y sus usuarios, separados del resto |
| **Comanda** | El papelito del pedido que va a la cocina |
| **Receta** | De qué ingredientes está hecho un producto, y cuánto lleva de cada uno |
| **Plan** | Lo que la hamburguesería contrató; define qué funciones tiene habilitadas |
| **Tiempo real** | Que el pedido aparece en la pantalla de cocina solo, sin que nadie refresque |

---

*Proyecto Final — Adrian Perez, 2026. Última actualización: 2026-09-05.*
