import { test, expect } from "@playwright/test";
import { CUENTAS, login, seedPedido, vigilarConsola, assertSinErrores } from "./helpers";

test.describe("Admin — Pedidos", () => {
  test("lista, filtros, detalle y avance de estado", async ({ page, request }) => {
    const errores = vigilarConsola(page);
    const pedido = await seedPedido(request, CUENTAS.adminRecoleta, { modalidad: "RETIRAR" });

    await login(page, CUENTAS.adminRecoleta, /\/admin\/dashboard/);
    await page.goto("/admin/pedidos");

    const fila = page.getByRole("row", { name: new RegExp(`#${pedido.id}\\b`) });
    await expect(fila).toBeVisible({ timeout: 15_000 });
    await expect(fila.getByText(/para retirar/i)).toBeVisible();

    // tabs
    await page.getByRole("tab", { name: /activos/i }).click();
    await expect(fila).toBeVisible();
    await page.getByRole("tab", { name: /entregados/i }).click();
    await expect(fila).toHaveCount(0);
    await page.getByRole("tab", { name: /todos/i }).click();

    // filtro por modalidad: Delivery lo oculta
    await page.getByRole("button", { name: "Delivery" }).click();
    await expect(fila).toHaveCount(0);
    await page.getByRole("button", { name: "Todas" }).click();
    await expect(fila).toBeVisible();

    // búsqueda
    await page.getByPlaceholder(/buscar por pedido o cliente/i).fill(String(pedido.id));
    await expect(fila).toBeVisible();
    await page.getByPlaceholder(/buscar por pedido o cliente/i).fill("zzz-no-existe");
    await expect(fila).toHaveCount(0);
    await page.getByPlaceholder(/buscar por pedido o cliente/i).fill("");

    // detalle
    await fila.click();
    const drawer = page.getByRole("dialog").or(page.locator(".MuiDrawer-paper"));
    await expect(page.getByRole("heading", { name: `Pedido #${pedido.id}` })).toBeVisible({ timeout: 10_000 });
    // stepper con los pasos del recorrido
    await expect(drawer.getByText("Pendiente").first()).toBeVisible();
    await expect(drawer.getByText("Entregado").first()).toBeVisible();

    // avanzar: Empezar -> En preparación
    await drawer.getByRole("button", { name: /empezar/i }).click();
    await expect(page.getByText(/actualizado/i)).toBeVisible({ timeout: 15_000 });
    await expect(drawer.getByText(/en preparación/i).first()).toBeVisible({ timeout: 10_000 });

    // siguiente: Marcar listo
    await drawer.getByRole("button", { name: /marcar listo/i }).click();
    await expect(drawer.getByText(/^listo$/i).first()).toBeVisible({ timeout: 10_000 });

    await assertSinErrores(errores);
  });

  test("filtro de fechas: 'Ayer' no muestra el pedido de hoy", async ({ page, request }) => {
    const pedido = await seedPedido(request, CUENTAS.adminRecoleta);
    await login(page, CUENTAS.adminRecoleta, /\/admin\/dashboard/);
    await page.goto("/admin/pedidos");
    const fila = page.getByRole("row", { name: new RegExp(`#${pedido.id}\\b`) });
    await expect(fila).toBeVisible({ timeout: 15_000 });

    await page.getByRole("button", { name: "Ayer", exact: true }).click();
    await expect(fila).toHaveCount(0, { timeout: 10_000 });

    await page.getByRole("button", { name: "Hoy", exact: true }).click();
    await expect(fila).toBeVisible({ timeout: 10_000 });
  });
});
