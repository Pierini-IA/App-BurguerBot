import { test, expect } from "@playwright/test";
import { CUENTAS, login, seedPedido, vigilarConsola, assertSinErrores } from "./helpers";

test.describe("Cocina", () => {
  test("un pedido creado aparece y se puede avanzar por sus estados", async ({ page, request }) => {
    const errores = vigilarConsola(page);
    const pedido = await seedPedido(request, CUENTAS.cocinaRecoleta);

    await login(page, CUENTAS.cocinaRecoleta, /\/cocina/);
    const chit = page.locator("article").filter({ hasText: new RegExp(`#${pedido.id}\\b`) });
    await expect(chit).toBeVisible({ timeout: 15_000 });

    // bump bar: Empezar -> Marcar listo -> Entregar (y desaparece)
    await chit.getByRole("button", { name: /empezar/i }).click();
    await expect(chit.getByRole("button", { name: /marcar listo/i })).toBeVisible({ timeout: 10_000 });
    await chit.getByRole("button", { name: /marcar listo/i }).click();
    await expect(chit.getByRole("button", { name: /entregar/i })).toBeVisible({ timeout: 10_000 });
    await chit.getByRole("button", { name: /entregar/i }).click();
    await expect(chit).toHaveCount(0, { timeout: 10_000 });

    await assertSinErrores(errores);
  });

  test("tiempo real: el WebSocket conecta y un pedido nuevo aparece sin recargar", async ({ page, request }) => {
    await login(page, CUENTAS.cocinaRecoleta, /\/cocina/);
    // esperar a que el board cargue (vacío o con datos)
    await expect(page.getByText(/en cola|la cola está vacía/i).first()).toBeVisible({ timeout: 15_000 });

    // el indicador del board debe pasar a "en vivo" (STOMP conectado, no el fallback de polling)
    await expect(page.getByText(/^en vivo$/i)).toBeVisible({ timeout: 15_000 });

    const pedido = await seedPedido(request, CUENTAS.cocinaRecoleta);

    // sin page.reload(): entra por WebSocket
    await expect(page.locator("article").filter({ hasText: new RegExp(`#${pedido.id}\\b`) })).toBeVisible({
      timeout: 15_000,
    });
  });

  test("la preferencia de sonido se persiste", async ({ page }) => {
    await login(page, CUENTAS.cocinaRecoleta, /\/cocina/);
    const btn = page.getByRole("button", { name: /silenciar avisos|activar avisos/i });
    await expect(btn).toBeVisible({ timeout: 15_000 });
    const eraSilenciar = (await btn.getAttribute("aria-label"))!.includes("Silenciar");

    await btn.click();
    // el label se invierte al togglear
    await expect(btn).toHaveAttribute("aria-label", eraSilenciar ? /activar/i : /silenciar/i);

    await page.reload();
    // tras recargar, el estado se lee de localStorage y persiste (toHaveAttribute reintenta)
    await expect(page.getByRole("button", { name: /silenciar avisos|activar avisos/i })).toHaveAttribute(
      "aria-label",
      eraSilenciar ? /activar/i : /silenciar/i,
      { timeout: 15_000 }
    );
  });
});

test.describe("Mostrador", () => {
  test("un pedido aparece y la comanda se arma al imprimir", async ({ page, request }) => {
    const errores = vigilarConsola(page);
    const pedido = await seedPedido(request, CUENTAS.cocinaRecoleta, { modalidad: "RETIRAR" });

    // interceptar window.print y capturar el contenido de la comanda en ese instante
    // (después de imprimir, la comanda se desmonta del DOM)
    await page.addInitScript(() => {
      (window as any).__print = { calls: 0, comanda: "" };
      window.print = () => {
        (window as any).__print.calls++;
        (window as any).__print.comanda = document.querySelector(".comanda-print")?.textContent ?? "";
      };
    });

    await login(page, CUENTAS.cocinaRecoleta, /\/cocina/);
    await page.goto("/mostrador");

    // la card de ESTE pedido (puede haber otras en el board)
    const heading = page.getByRole("heading", { name: `#${pedido.id}`, exact: true });
    await expect(heading).toBeVisible({ timeout: 15_000 });
    const card = page
      .locator("div")
      .filter({ has: heading })
      .filter({ has: page.getByRole("button", { name: /imprimir/i }) })
      .last();
    await card.getByRole("button", { name: /imprimir/i }).click();

    await expect
      .poll(() => page.evaluate(() => (window as any).__print.calls), { timeout: 10_000 })
      .toBeGreaterThan(0);
    const comanda: string = await page.evaluate(() => (window as any).__print.comanda);
    expect(comanda).toContain(`PEDIDO #${pedido.id}`);
    expect(comanda).toContain("TOTAL");

    await assertSinErrores(errores);
  });
});
