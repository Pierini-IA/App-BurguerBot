import { test, expect } from "@playwright/test";
import { CUENTAS, login } from "./helpers";

test.describe("Auth y roles", () => {
  test("login inválido muestra error y no redirige", async ({ page }) => {
    await page.goto("/login");
    await page.getByLabel("Username").fill("noexiste");
    await page.getByLabel("Contraseña").fill("malmal123");
    await page.getByRole("button", { name: /iniciar sesión/i }).click();
    await expect(page.getByText(/credenciales|inválid|incorrect|error/i).first()).toBeVisible({ timeout: 15_000 });
    await expect(page).toHaveURL(/\/login/);
  });

  for (const [rol, cuenta, url] of [
    ["superadmin", CUENTAS.superadmin, /\/superadmin/],
    ["admin", CUENTAS.adminRecoleta, /\/admin\/dashboard/],
    ["cocina", CUENTAS.cocinaRecoleta, /\/cocina/],
  ] as const) {
    test(`rol ${rol} aterriza en su panel`, async ({ page }) => {
      await login(page, cuenta, url);
      await expect(page).toHaveURL(url);
    });
  }

  test("cocina no puede entrar al panel de admin", async ({ page }) => {
    await login(page, CUENTAS.cocinaRecoleta, /\/cocina/);
    await page.goto("/admin/productos");
    await expect(page).toHaveURL(/\/cocina/, { timeout: 15_000 });
  });

  test("sin sesión, una ruta protegida redirige a login", async ({ page }) => {
    await page.goto("/admin/dashboard");
    await expect(page).toHaveURL(/\/login/, { timeout: 15_000 });
  });

  test("token borrado en runtime -> vuelve a login", async ({ page }) => {
    await login(page, CUENTAS.adminRecoleta, /\/admin\/dashboard/);
    await page.evaluate(() => {
      localStorage.clear();
    });
    await page.goto("/admin/productos");
    await expect(page).toHaveURL(/\/login/, { timeout: 15_000 });
  });

  // ítem del sidebar del admin (hay drawer mobile + desktop montados; tomamos el visible)
  const itemSidebar = (page: import("@playwright/test").Page, nombre: string) =>
    page
      .locator("nav")
      .filter({ has: page.getByRole("button", { name: "Pedidos" }) })
      .getByRole("button", { name: nombre })
      .filter({ visible: true });

  test("plan BÁSICO: el sidebar no muestra Reportes", async ({ page }) => {
    await login(page, CUENTAS.adminCentro, /\/admin\/dashboard/);
    await expect(itemSidebar(page, "Productos")).toBeVisible({ timeout: 15_000 });
    await expect(itemSidebar(page, "Reportes")).toHaveCount(0);
  });

  test("plan PREMIUM: el sidebar sí muestra Reportes", async ({ page }) => {
    await login(page, CUENTAS.adminRecoleta, /\/admin\/dashboard/);
    await expect(itemSidebar(page, "Reportes")).toBeVisible({ timeout: 15_000 });
  });
});
