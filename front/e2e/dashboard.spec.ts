import { test, expect } from "@playwright/test";
import { CUENTAS, login, seedPedido, vigilarConsola, assertSinErrores } from "./helpers";

test.describe("Admin — Dashboard", () => {
  test("muestra KPIs, últimos pedidos y stock bajo; refresca", async ({ page, request }) => {
    const errores = vigilarConsola(page);
    const pedido = await seedPedido(request, CUENTAS.adminRecoleta);

    await login(page, CUENTAS.adminRecoleta, /\/admin\/dashboard/);

    await expect(page.getByText(/ingresos de hoy/i)).toBeVisible({ timeout: 15_000 });
    await expect(page.getByText(/pedidos hoy/i)).toBeVisible();
    await expect(page.getByText(/reservas hoy/i)).toBeVisible();
    await expect(page.getByText(/últimos pedidos/i)).toBeVisible();
    await expect(page.getByText(/stock bajo/i)).toBeVisible();

    // el pedido recién creado aparece en "últimos pedidos"
    await expect(page.getByText(new RegExp(`#${pedido.id}\\b`)).first()).toBeVisible({ timeout: 15_000 });

    // botón de actualizar
    await page.getByRole("button", { name: /actualizar dashboard/i }).click();
    await expect(page.getByText(/ingresos de hoy/i)).toBeVisible();

    // link "Ver todo" lleva a Pedidos
    await page.getByRole("button", { name: /ver todo/i }).first().click();
    await expect(page).toHaveURL(/\/admin\/(pedidos|ingredientes)/);

    await assertSinErrores(errores);
  });
});
