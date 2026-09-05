import { test, expect } from "@playwright/test";
import { CUENTAS, login, uniq, vigilarConsola, assertSinErrores } from "./helpers";

test.describe("SuperAdmin", () => {
  test.beforeEach(async ({ page }) => {
    await login(page, CUENTAS.superadmin, /\/superadmin/);
  });

  test("dashboard muestra los totales", async ({ page }) => {
    const errores = vigilarConsola(page);
    await page.goto("/superadmin");
    await expect(page.getByText(/locales totales/i)).toBeVisible({ timeout: 15_000 });
    await expect(page.getByText(/usuarios totales/i)).toBeVisible();
    await assertSinErrores(errores);
  });

  test("crea un local nuevo y aparece en la lista", async ({ page }) => {
    const nombre = `Local E2E ${uniq()}`;
    const tel = "+54911" + String(Date.now()).slice(-8);
    await page.goto("/superadmin/locales");

    await page.getByRole("button", { name: /nuevo local/i }).click();
    const dialog = page.getByRole("dialog");
    await dialog.getByLabel(/nombre del local/i).fill(nombre);
    await dialog.getByLabel(/dirección/i).fill("Av. Siempre Viva 742");
    await dialog.getByLabel(/teléfono/i).fill(tel);
    await dialog.getByRole("button", { name: /crear|guardar/i }).last().click();

    await expect(page.getByText(new RegExp(nombre))).toBeVisible({ timeout: 15_000 });
  });

  test("crea un usuario de cocina para un local", async ({ page }) => {
    const username = `coci_${uniq()}`;
    await page.goto("/superadmin/usuarios");

    await page.getByRole("button", { name: /nuevo usuario/i }).click();
    const dialog = page.getByRole("dialog");
    await dialog.getByLabel("Username").fill(username);
    await dialog.getByLabel(/contraseña/i).fill("Cocina123!");
    await dialog.getByLabel("Rol").click();
    await page.getByRole("option", { name: /cocina/i }).click();
    await dialog.getByLabel(/teléfono del local/i).fill(CUENTAS.adminRecoleta.tel);
    await dialog.getByRole("button", { name: /^crear$/i }).click();

    await expect(page.getByText(username)).toBeVisible({ timeout: 15_000 });
  });
});
