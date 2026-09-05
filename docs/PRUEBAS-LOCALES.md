# 🧪 Pruebas locales (antes de deployar)

Cómo levantar el stack completo en local y correr las pruebas E2E.

## 1. Backend + base de datos (Docker)

No hace falta tener Java/Maven instalado — se compila y corre todo en Docker.

```bash
cd backend
docker compose -f docker-compose.local.yml up -d --build
# healthcheck:
curl http://localhost:8080/api/health
```

- Postgres queda en el puerto **5433** (para no chocar con otros Postgres locales), efímero (tmpfs — se borra al recrear el contenedor).
- El backend en **8080**, perfil `default`, `DRY_RUN_META_SEND=true` (no llama a la Graph API real).
- **`TZ=America/Argentina/Buenos_Aires`** — importante: sin esto, los filtros "de hoy" (Pedidos, Dashboard) fallan cerca de medianoche UTC. **Setear la misma variable en el deploy.**
- Al arrancar con la base vacía, `DataInitializer` siembra 4 locales de prueba.

### Credenciales sembradas

| Usuario | Password | Rol | Local | Plan |
|---|---|---|---|---|
| `superadmin` | `SuperAdmin123!` | SUPERADMIN | — | — |
| `admin_centro` | `Admin123!` | ADMIN | Burger Express Centro (`+5491112345678`) | BÁSICO |
| `admin_palermo` / `cocina_palermo` | `Admin123!` / `Cocina123!` | ADMIN / COCINA | Dio Burger & Pizza Palermo (`+5491187654321`) | ESTÁNDAR |
| `admin_recoleta` / `cocina_recoleta` | `Admin123!` / `Cocina123!` | ADMIN / COCINA | Dio Gourmet Recoleta (`+5491198765432`) | PREMIUM |

## 2. Frontend

```bash
cd front
npm install
NEXT_PUBLIC_API_URL=http://localhost:8080/api NEXT_PUBLIC_WS_URL=http://localhost:8080 npm run dev
# el backend acepta CORS solo desde localhost:3000 -> correr el front en 3000
```

## 3. E2E (Playwright)

Con el backend corriendo:

```bash
cd front
npm run e2e          # headless
npm run e2e:ui       # modo interactivo
```

El `webServer` de Playwright levanta el `next dev` en :3000 con el env correcto.

### Specs (~30 tests)

| Spec | Qué cubre |
|---|---|
| `smoke.spec.ts` | Landing sin errores; admin y superadmin recorren **todas** sus secciones sin errores de consola. |
| `auth-roles.spec.ts` | Login inválido; cada rol aterriza en su panel; cocina no entra a `/admin`; sin sesión / token borrado → login; **feature-gating**: plan BÁSICO no ve "Reportes" en el sidebar, PREMIUM sí. |
| `superadmin.spec.ts` | Dashboard con totales; crear un local; crear un usuario de cocina. |
| `admin-menu.spec.ts` | Categorías CRUD; extras (con categoría); ingredientes (+ ajustar stock); **productos con editor de receta** + toggle de disponibilidad que persiste. |
| `admin-config.spec.ts` | Editar horarios/modalidades y que persistan; **token de WhatsApp**: se guarda, no se re-expone, y vacío no lo borra; Callback URL del webhook; Reservas deshabilitado en plan BÁSICO. |
| `admin-pedidos.spec.ts` | Lista + tabs + filtro por modalidad + búsqueda + drawer de detalle + stepper + avance de estado; filtro "Ayer" no muestra el pedido de hoy. |
| `cocina-mostrador.spec.ts` | Cocina: el pedido aparece y se avanza Empezar→Listo→Entregar (y desaparece); **tiempo real** (pedido creado por API aparece sin recargar); preferencia de sonido persiste. Mostrador: el pedido aparece y la **comanda se arma al imprimir** (con `window.print` interceptado). |
| `dashboard.spec.ts` | KPIs, últimos pedidos, stock bajo; botón de actualizar; "Ver todo" navega. |

El pedido de prueba se siembra por API (`seedPedido` en `helpers.ts`) porque el bot real necesita Meta.

## 4. Smoke de API (rápido, sin navegador)

```bash
node "<ruta>/scratchpad/smoke.mjs"   # (script de trabajo, no versionado)
```
Verifica los endpoints nuevos/modificados del backend directamente.

---

## Bugs encontrados y corregidos en esta ronda de pruebas

| # | Síntoma | Causa | Fix |
|---|---|---|---|
| 1 | `GET /admin/productos`, `/ingredientes`, `/mesas`, `/local/pedidos`, `/cocina/pedidos` → **500** con datos reales | Recursión infinita de Jackson al serializar entidades (`Local ↔ ConfiguracionLocal`, `Local ↔ Ingrediente/Mesa/Pedido`) + proxies lazy de Hibernate sin manejar | `@JsonIgnore` en `ConfiguracionLocal.local`; `@JsonIgnoreProperties` en `Ingrediente/Mesa/Pedido.local` y `Pedido`; `+"configuracion"` en `Producto.local`; **`jackson-datatype-hibernate6` + `JacksonConfig`** (fix global de los proxies lazy) |
| 2 | Hidratación de React rota en **todas** las páginas (FOUC + spam en consola) | MUI/Emotion sin configurar para el App Router de Next | `@mui/material-nextjs` + `<AppRouterCacheProvider>` en `app/layout.tsx` |
| 3 | El filtro "Hoy" de Pedidos y el Dashboard **no muestran los pedidos de la tarde/noche** | Backend en UTC, frontend en hora local → "hoy" no coincide entre 21 y 24 hs | `TZ=America/Argentina/Buenos_Aires` en el backend (y en el deploy) |
| 4 | Pedidos creados desde el panel aparecen con el badge "Por WhatsApp" | `PedidoService` hardcodeaba `origenPedido = BOT` | Sobrecarga `crearPedido(dto, tel, origen)`; `LocalController` pasa `LOCAL`, `BotController` sigue en `BOT` |
| 5 | **El WebSocket nunca conectaba** (`/ws/info` → 403, luego 400). El "tiempo real" de Cocina/Mostrador funcionaba solo por el polling de respaldo de 15 s. | (a) `/ws/**` no estaba en el `permitAll` de `SecurityConfig` → 403. (b) `WebSocketConfig.setAllowedOrigins("*")` es incompatible con el filtro CORS de Security (`allowCredentials=true`) → 400. | `.requestMatchers("/ws/**").permitAll()` + `JwtAuthenticationFilter` saltea `/ws` + `WebSocketConfig.setAllowedOriginPatterns("*")` |
| 6 | `GET /api/menu/{tel}` (menú público, ej. QR) y `/api` raíz pedían login | Faltaban en el `permitAll`; caían en `anyRequest().authenticated()` | Agregados a `permitAll` (`/api/menu/**` solo GET, `/api`, `/api/status`) |
| 7 | `Usuario.local` mapeado como `@JoinColumn(nullable=false)` pero el SUPERADMIN no tiene local (y la migración V6 ya lo hizo nullable en Postgres) | Entidad desalineada con el schema real y con el código | `@JoinColumn(name = "local_id")` (sin `nullable=false`). Rompía el sembrado en H2 (`PlanValidationIntegrationTest`, preexistente desde el commit inicial). |
| 8 | Los nuevos endpoints devolvían **500** en vez de **404** ante "usuario/local no encontrado" | Se usó `ResponseStatusException`, que el `GlobalExceptionHandler` cae en el catch-all `Exception` → 500 | Usar `NotFoundException` (la excepción del proyecto, mapeada a 404) |
| 9 | CORS hardcodeado a `localhost:3000` → **el frontend deployado (Vercel) sería bloqueado** | `SecurityConfig.corsConfigurationSource` con orígenes fijos | Se lee de `CORS_ALLOWED_ORIGINS` (coma-separado), default localhost para dev |
| — | `LocalControllerMiLocalTest` no cargaba el contexto (mockea `LocalRepository` → `DataInitializer` cree la DB vacía y `save()` no-op) | Test que mockea el repo usado por el seeding | `@MockBean(name="initData") ApplicationRunner` para anular el seeding en ese test |

Todos los cambios de backend **compilan, arrancan y pasan los tests en Docker** — validado con `docker compose -f docker-compose.local.yml up --build`, `mvn test` en container Maven, 18/18 smoke de API y **30/30 Playwright E2E**.

## Pendiente / notas para el deploy

- **`backend/Dockerfile` (prod) está roto**: referencia el Maven wrapper (`mvnw`, `.mvn/`) que no está en el repo. Usar `maven:3.9-eclipse-temurin-21` como imagen base (como `Dockerfile.local`) o agregar el wrapper (`mvn -N wrapper:wrapper`).
- **Variables de entorno obligatorias en el deploy del backend**: `TZ=America/Argentina/Buenos_Aires`, `CORS_ALLOWED_ORIGINS=https://<dominio-del-front>`, `DATABASE_URL`, `DATABASE_PASSWORD`, `JWT_SECRET` (≥32 chars), `BOT_API_KEY`, `META_WEBHOOK_VERIFY_TOKEN`, `DRY_RUN_META_SEND=true` (hasta configurar Meta), `OPENAI_API_KEY`/`GOOGLE_AI_API_KEY` (opcionales).
- **Deuda de seguridad (post-MVP)**: el canal STOMP no autentica el frame CONNECT ni valida el `telefonoLocal` del `SUBSCRIBE` → cualquier cliente conectado puede escuchar los pedidos de cualquier local. Los endpoints HTTP sí están bien aislados por tenant.
- **`WebSocketConfig.setAllowedOriginPatterns("*")`**: restringir a los dominios del frontend en producción.
