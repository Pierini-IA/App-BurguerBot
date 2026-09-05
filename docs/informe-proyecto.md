# Proyecto Final — Sistema de Gestión para Hamburgueserías

**Alumno:** Adrian Perez

**Materia:** Proyecto Final

**Año:** 2026

---

## Temática del sitio web

El proyecto es un sistema de gestión para hamburgueserías, pero pensado de forma que pueda adaptarse a cualquier tipo de negocio gastronómico. El sistema maneja pedidos, menú, stock e ingredientes con cálculos automáticos según lo que se prepare. Tiene paneles separados para cocina y mostrador, y un asistente con IA que interpreta mensajes de texto (usando OpenAI) e imágenes (usando Gemini) que llegan por canales de Meta para crear pedidos sin cargar datos manualmente.

---

## Descripción del sitio web

La aplicación es un producto para cualquier hamburguesería o local gastronómico que quiera centralizar la gestión. Funciona para varios locales simultáneamente y cada uno mantiene sus datos separados sin poder acceder a la información de otros.

El sistema tiene cuatro tipos de usuario según su rol:

- **Superadministrador:** gestiona todos los locales y sus usuarios.
- **Administrador del local:** carga el menú, controla los pedidos y el stock del negocio.
- **Usuario de cocina:** ve los pedidos activos y los marca como listos o en preparación.
- **Usuario de mostrador:** imprime pedidos, marca cuando están en preparación e informa al cliente sobre tiempos de retiro.

**Funcionalidades principales:**

- Gestión completa del menú con productos, categorías y extras.
- Sistema de pedidos con estados: pendiente, en preparación, listo, entregado.
- Cálculo automático de ingredientes según lo que se prepare, pensado para calcular stock.
- Módulo de delivery para seguimiento de repartos.
- Panel de cocina en tiempo real.
- Impresión de pedidos en formato tipo postnet para el mostrador.
- Cambio de estado "en preparación" desde cocina o mostrador para informar al cliente sobre tiempos de retiro.
- Asistente con IA para crear pedidos desde mensajes de texto.
- Integración con WhatsApp aprovechando la ventana de 24 horas que Meta permite para responder consultas para informar estado del pedido.

---

## Estructura del sitio web

La aplicación está dividida en secciones según el rol del usuario:

**Landing page** — página inicial con información del producto, funcionalidades, planes y contacto.

**Login** — autenticación con usuario y contraseña.

**Panel de administración** — la sección principal. El administrador del local accede desde acá para:
- Ver y gestionar pedidos
- Configurar productos, categorías y extras
- Controlar ingredientes y stock
- Administrar delivery
- Ver reportes
- Configurar el local

**Panel de superadministrador** — solo para gestionar todos los locales y usuarios del sistema.

**Panel de cocina** — interfaz simplificada para el personal de cocina. Muestra los pedidos activos y permite marcarlos como "en preparación" o "listo" sin acceso al resto del sistema.

**Mostrador / impresión rápida** — una vista operativa para imprimir pedidos y actualizar estados sin entrar al panel completo.

Cada sección tiene acceso restringido según el rol. En total, seis secciones diferenciadas que cubren todos los procesos del negocio.

---

## Tecnologías, librerías y frameworks

**Frontend:** Next.js con React y TypeScript. Los componentes visuales vienen de Material UI o Tailwind (tengo que analizarlo bien todavía). La comunicación en tiempo real con el servidor es a través de WebSockets.

**Backend:** Spring Boot con Java. La base de datos es PostgreSQL. La autenticación usa JWT. El backend se conecta con OpenAI para procesar mensajes de texto y tomar los pedidos.


