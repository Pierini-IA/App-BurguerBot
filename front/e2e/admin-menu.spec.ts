import { test, expect } from "@playwright/test";
import { CUENTAS, login, uniq, vigilarConsola, assertSinErrores } from "./helpers";

test.describe("Admin — gestión de menú", () => {
  test.beforeEach(async ({ page }) => {
    await login(page, CUENTAS.adminRecoleta, /\/admin\/dashboard/);
  });

  test("categorías: crear, editar y eliminar", async ({ page }) => {
    const errores = vigilarConsola(page);
    const nombre = `Cat ${uniq()}`;
    await page.goto("/admin/categorias");

    await page.getByRole("button", { name: /nueva categoría/i }).first().click();
    await page.getByLabel("Nombre").fill(nombre);
    await page.getByLabel("Descripción").fill("hecha por e2e");
    await page.getByRole("button", { name: /crear categoría/i }).click();
    await expect(page.getByText(/categoría creada/i)).toBeVisible({ timeout: 15_000 });
    const fila = page.getByRole("row", { name: new RegExp(nombre) });
    await expect(fila).toBeVisible();

    // editar
    await fila.getByRole("button", { name: new RegExp(`editar ${nombre}`, "i") }).click();
    await page.getByLabel("Nombre").fill(`${nombre} v2`);
    await page.getByRole("button", { name: /guardar cambios/i }).click();
    await expect(page.getByText(/categoría actualizada/i)).toBeVisible({ timeout: 15_000 });
    await expect(page.getByRole("row", { name: new RegExp(`${nombre} v2`) })).toBeVisible();

    // eliminar
    await page
      .getByRole("row", { name: new RegExp(`${nombre} v2`) })
      .getByRole("button", { name: /eliminar/i })
      .click();
    await page.getByRole("button", { name: /^eliminar$|eliminando/i }).click();
    await expect(page.getByText(/categoría eliminada/i)).toBeVisible({ timeout: 15_000 });
    await assertSinErrores(errores);
  });

  test("extras: crear con categoría y desactivar", async ({ page }) => {
    const nombre = `Extra ${uniq()}`;
    await page.goto("/admin/extras");
    await page.getByRole("button", { name: /nuevo extra/i }).first().click();
    await page.getByLabel("Nombre").fill(nombre);
    await page.getByLabel(/precio adicional/i).fill("1200");
    await page.getByRole("button", { name: /crear extra/i }).click();
    await expect(page.getByText(/extra creado/i)).toBeVisible({ timeout: 15_000 });

    const fila = page.getByRole("row", { name: new RegExp(nombre) });
    await expect(fila).toBeVisible();
    await expect(fila.getByText("$")).toBeVisible();
  });

  test("ingredientes: crear y ajustar stock", async ({ page }) => {
    const nombre = `Ing ${uniq()}`;
    await page.goto("/admin/ingredientes");
    await page.getByRole("button", { name: /nuevo ingrediente/i }).first().click();
    await page.getByLabel("Nombre").fill(nombre);
    await page.getByLabel("Stock actual").fill("25");
    await page.getByRole("button", { name: /crear ingrediente/i }).click();
    await expect(page.getByText(/ingrediente creado/i)).toBeVisible({ timeout: 15_000 });

    const fila = page.getByRole("row", { name: new RegExp(nombre) });
    await expect(fila).toBeVisible();

    // ajustar stock
    await fila.getByRole("button", { name: /ajustar/i }).click();
    await page.getByLabel("Stock actual").fill("3");
    await page.getByRole("button", { name: /guardar stock/i }).click();
    await expect(page.getByText(/stock.*actualizado/i)).toBeVisible({ timeout: 15_000 });
    await expect(page.getByRole("row", { name: new RegExp(nombre) }).getByText("3")).toBeVisible();
  });

  test("productos: crear con receta y togglear disponibilidad", async ({ page }) => {
    const errores = vigilarConsola(page);
    const suf = uniq();
    const ing = `Ing ${suf}`;
    const prod = `Prod ${suf}`;

    // ingrediente para la receta
    await page.goto("/admin/ingredientes");
    await page.getByRole("button", { name: /nuevo ingrediente/i }).first().click();
    await page.getByLabel("Nombre").fill(ing);
    await page.getByLabel("Stock actual").fill("50");
    await page.getByRole("button", { name: /crear ingrediente/i }).click();
    await expect(page.getByText(/ingrediente creado/i)).toBeVisible({ timeout: 15_000 });

    // producto con receta
    await page.goto("/admin/productos");
    await page.getByRole("button", { name: /nuevo producto/i }).first().click();
    await page.getByLabel("Nombre").fill(prod);
    await page.getByLabel(/precio/i).fill("8300");
    await page.getByRole("button", { name: /agregar ingrediente/i }).click();
    await page.getByRole("combobox", { name: "Ingrediente" }).click();
    await page.getByRole("option", { name: ing }).click();
    await page.getByLabel("Cantidad").fill("3");
    await page.getByRole("button", { name: /crear producto/i }).click();
    await expect(page.getByText(/producto creado/i)).toBeVisible({ timeout: 15_000 });

    const fila = page.getByRole("row", { name: new RegExp(prod) });
    await expect(fila).toBeVisible({ timeout: 15_000 });
    await expect(fila.getByText(/1 ingrediente/i)).toBeVisible();

    // togglear disponibilidad (MUI Switch => role "switch")
    const sw = fila.getByRole("switch");
    await expect(sw).toBeChecked();
    await sw.click();
    await expect(sw).not.toBeChecked({ timeout: 10_000 });
    // persiste tras recargar
    await page.reload();
    await expect(page.getByRole("row", { name: new RegExp(prod) }).getByRole("switch")).not.toBeChecked({
      timeout: 15_000,
    });

    await assertSinErrores(errores);
  });
});
