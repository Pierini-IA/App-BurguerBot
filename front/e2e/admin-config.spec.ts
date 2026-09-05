import { test, expect } from "@playwright/test";
import { CUENTAS, login, uniq, vigilarConsola, assertSinErrores } from "./helpers";

test.describe("Admin — Configuración", () => {
  test("edita horarios y modalidad y persiste tras recargar", async ({ page }) => {
    const errores = vigilarConsola(page);
    await login(page, CUENTAS.adminRecoleta, /\/admin\/dashboard/);
    await page.goto("/admin/configuracion");

    await expect(page.getByRole("button", { name: /guardar cambios/i })).toBeDisabled();

    await page.getByLabel("Abre").fill("10:30");
    // apaga "Delivery"
    const delivery = page.getByLabel("Delivery");
    const estabaDelivery = await delivery.isChecked();
    await delivery.click();

    await page.getByRole("button", { name: /guardar cambios/i }).click();
    await expect(page.getByText(/configuración guardada/i)).toBeVisible({ timeout: 15_000 });

    await page.reload();
    await expect(page.getByLabel("Abre")).toHaveValue("10:30", { timeout: 15_000 });
    await expect(page.getByLabel("Delivery")).toBeChecked({ checked: !estabaDelivery });
    await assertSinErrores(errores);
  });

  test("token de WhatsApp: se guarda, no se re-expone, y vacío no lo borra", async ({ page }) => {
    await login(page, CUENTAS.adminRecoleta, /\/admin\/dashboard/);
    await page.goto("/admin/configuracion");

    const phoneId = "PN-" + uniq();
    const token = "EAAG-" + uniq() + "-secret";
    // mostrar tokens => los campos pasan a type="text" (role textbox) y se pueden targetear
    await page.getByRole("button", { name: /ver tokens|ocultar tokens/i }).first().click();
    const tokenInput = page.getByRole("textbox", { name: "Access token", exact: true });
    await page.getByLabel("Phone Number ID").fill(phoneId);
    await tokenInput.fill(token);
    await page.getByRole("button", { name: /guardar cambios/i }).click();
    await expect(page.getByText(/configuración guardada/i)).toBeVisible({ timeout: 15_000 });

    await page.reload();
    await page.getByRole("button", { name: /ver tokens|ocultar tokens/i }).first().click();
    // el phone id sí vuelve; el token NO (campo vacío + hint "ya hay un token guardado")
    await expect(page.getByLabel("Phone Number ID")).toHaveValue(phoneId, { timeout: 15_000 });
    await expect(page.getByRole("textbox", { name: "Access token", exact: true })).toHaveValue("");
    await expect(page.getByText(/ya hay un token guardado/i).first()).toBeVisible();

    // guardar otro cambio sin tocar el token: sigue configurado
    await page.getByLabel("Phone Number ID").fill(phoneId + "x");
    await page.getByRole("button", { name: /guardar cambios/i }).click();
    await expect(page.getByText(/configuración guardada/i)).toBeVisible({ timeout: 15_000 });
    await page.reload();
    await expect(page.getByText(/ya hay un token guardado/i).first()).toBeVisible({ timeout: 15_000 });
  });

  test("muestra la Callback URL del webhook con el teléfono del local", async ({ page }) => {
    await login(page, CUENTAS.adminRecoleta, /\/admin\/dashboard/);
    await page.goto("/admin/configuracion");
    await expect(page.getByText(/para configurar el webhook en meta/i)).toBeVisible({ timeout: 15_000 });
    const url = page.locator('input[readonly]').first();
    await expect(url).toHaveValue(/\/api\/webhooks\/meta\/whatsapp\/\+?5491198765432$/);
  });

  test("plan BÁSICO: la opción de Reservas está deshabilitada", async ({ page }) => {
    await login(page, CUENTAS.adminCentro, /\/admin\/dashboard/);
    await page.goto("/admin/configuracion");
    await expect(page.getByText(/reservas de mesa.*requiere plan/i)).toBeVisible({ timeout: 15_000 });
    await expect(page.getByLabel(/reservas de mesa/i)).toBeDisabled();
  });
});
