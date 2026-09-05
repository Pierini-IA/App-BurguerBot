import { Page, APIRequestContext, expect } from "@playwright/test";

export const API = "http://localhost:8080/api";

export const CUENTAS = {
  superadmin: { username: "superadmin", password: "SuperAdmin123!" },
  // Burger Express Centro — plan BÁSICO (sin bot, sin reservas, sin reportes)
  adminCentro: { username: "admin_centro", password: "Admin123!", tel: "+5491112345678" },
  // Dio Burger & Pizza Palermo — plan ESTÁNDAR (bot + reservas, sin reportes)
  adminPalermo: { username: "admin_palermo", password: "Admin123!", tel: "+5491187654321" },
  cocinaPalermo: { username: "cocina_palermo", password: "Cocina123!", tel: "+5491187654321" },
  // Dio Gourmet Recoleta — plan PREMIUM (todas las features)
  adminRecoleta: { username: "admin_recoleta", password: "Admin123!", tel: "+5491198765432" },
  cocinaRecoleta: { username: "cocina_recoleta", password: "Cocina123!", tel: "+5491198765432" },
};

export const uniq = () => Math.random().toString(36).slice(2, 7);

/** Inicia sesión por la UI y espera el redirect según rol. */
export async function login(page: Page, cuenta: { username: string; password: string }, esperaUrl: RegExp) {
  // arrancar siempre desde una sesión limpia (evita el redirect de /login si ya hay sesión)
  await page.goto("/login");
  await page.evaluate(() => {
    try {
      localStorage.clear();
    } catch {
      /* noop */
    }
  });
  await page.goto("/login");
  await page.getByLabel("Username").fill(cuenta.username);
  await page.getByLabel("Contraseña").fill(cuenta.password);
  await page.getByRole("button", { name: /iniciar sesión/i }).click();
  await page.waitForURL(esperaUrl, { timeout: 20_000 });
}

/** El admin de cada local (para seedear pedidos cuando el test es de un rol COCINA). */
export function adminDe(tel: string) {
  if (tel === CUENTAS.adminCentro.tel) return CUENTAS.adminCentro;
  if (tel === CUENTAS.adminPalermo.tel) return CUENTAS.adminPalermo;
  return CUENTAS.adminRecoleta;
}

/** Token JWT vía API (para seed y para inyectar sesión sin pasar por el form). */
export async function tokenApi(request: APIRequestContext, cuenta: { username: string; password: string }) {
  const res = await request.post(`${API}/auth/login`, { data: cuenta });
  expect(res.ok(), "login API").toBeTruthy();
  return (await res.json()).token as string;
}

/** Crea un pedido de prueba por API y devuelve el pedido creado. */
export async function seedPedido(
  request: APIRequestContext,
  cuenta: { tel: string },
  opts: { modalidad?: "RETIRAR" | "DELIVERY"; cantidad?: number } = {}
) {
  // el listado de productos requiere ADMIN; si el test es de COCINA, usamos el admin del local
  const admin = adminDe(cuenta.tel);
  const token = await tokenApi(request, admin);
  const prodsRes = await request.get(
    `${API}/admin/productos?telefonoLocal=${encodeURIComponent(cuenta.tel)}`,
    { headers: { Authorization: `Bearer ${token}` } }
  );
  expect(prodsRes.ok(), `listar productos (status ${prodsRes.status()})`).toBeTruthy();
  const prods = await prodsRes.json();
  const productoId = (prods as any[]).find((p) => !p.estaAgotado)?.id ?? (prods as any[])[0]?.id;
  expect(productoId, "hay al menos un producto para el pedido").toBeTruthy();

  const res = await request.post(`${API}/local/pedido?telefonoLocal=${encodeURIComponent(cuenta.tel)}`, {
    headers: { Authorization: `Bearer ${token}` },
    data: {
      requestId: "e2e-" + Date.now() + "-" + uniq(),
      cliente: { nombre: "E2E " + uniq(), telefono: "+54911" + String(Date.now()).slice(-8) },
      modalidad: opts.modalidad ?? "RETIRAR",
      direccionEnvio: opts.modalidad === "DELIVERY" ? "Calle Falsa 123" : undefined,
      medioPago: "EFECTIVO",
      items: [{ productoId, cantidad: opts.cantidad ?? 2, extrasIds: [] }],
    },
  });
  expect(res.ok(), `crear pedido (status ${res.status()})`).toBeTruthy();
  return res.json();
}

/** Falla el test si hay errores en consola (más allá de ruido conocido). */
export function vigilarConsola(page: Page): string[] {
  const errores: string[] = [];
  page.on("console", (msg) => {
    if (msg.type() !== "error") return;
    const t = msg.text();
    if (/Failed to load resource|favicon|net::ERR_|WebSocket connection|Download the React DevTools/i.test(t)) return;
    errores.push(t);
  });
  page.on("pageerror", (err) => errores.push("pageerror: " + err.message));
  return errores;
}

export async function assertSinErrores(errores: string[]) {
  expect(errores, `errores de consola:\n${errores.join("\n")}`).toHaveLength(0);
}
