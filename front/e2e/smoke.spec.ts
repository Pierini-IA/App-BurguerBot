import { test, expect } from "@playwright/test";
import { CUENTAS, login, vigilarConsola, assertSinErrores } from "./helpers";

test.describe("Smoke", () => {
  test("landing pública carga sin errores", async ({ page }) => {
    const errores = vigilarConsola(page);
    await page.goto("/");
    await expect(
      page
        .getByRole("button", { name: /iniciar sesión/i })
        .or(page.getByRole("link", { name: /iniciar sesión/i }))
        .first()
    ).toBeVisible();
    await assertSinErrores(errores);
  });

  test("admin recorre todas las secciones sin errores de consola", async ({ page }) => {
    const errores = vigilarConsola(page);
    await login(page, CUENTAS.adminRecoleta, /\/admin\/dashboard/);
    for (const ruta of [
      "/admin/dashboard",
      "/admin/pedidos",
      "/admin/productos",
      "/admin/ingredientes",
      "/admin/categorias",
      "/admin/extras",
      "/admin/mesas",
      "/admin/configuracion",
      "/admin/reportes",
    ]) {
      await page.goto(ruta);
      await expect(page.locator("h1, h2, h4").first()).toBeVisible({ timeout: 15_000 });
      await page.waitForTimeout(300);
    }
    await assertSinErrores(errores);
  });

  test("superadmin recorre sus secciones sin errores", async ({ page }) => {
    const errores = vigilarConsola(page);
    await login(page, CUENTAS.superadmin, /\/superadmin/);
    for (const ruta of ["/superadmin", "/superadmin/locales", "/superadmin/usuarios"]) {
      await page.goto(ruta);
      await expect(page.locator("h1, h2, h4, h6").first()).toBeVisible({ timeout: 15_000 });
      await page.waitForTimeout(300);
    }
    await assertSinErrores(errores);
  });
});
