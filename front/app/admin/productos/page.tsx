"use client";

import { useEffect, useState } from "react";
import { Box, Button, IconButton, MenuItem, Switch, TextField, Tooltip, Typography } from "@mui/material";
import { Add as AddIcon, Edit as EditIcon, DeleteOutline as DeleteIcon } from "@mui/icons-material";
import {
  PageHeader,
  DataTable,
  FormDialog,
  ConfirmDialog,
  FeedbackSnackbar,
  type Column,
} from "@/components/shared";
import { RecetaEditor, type RecetaRow } from "@/components/admin/RecetaEditor";
import { useProductosStore } from "@/lib/stores/productosStore";
import { useCategoriasStore } from "@/lib/stores/categoriasStore";
import { useIngredientesStore } from "@/lib/stores/ingredientesStore";
import { useSnackbar } from "@/lib/hooks/useSnackbar";
import { getErrorMessage } from "@/lib/api/axios";
import type { Producto } from "@/types/producto";

const precioAR = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 0 });
const nf = (n: number) => (Number.isInteger(n) ? String(n) : String(parseFloat(n.toFixed(2))));

interface FormState {
  nombre: string;
  descripcion: string;
  precio: string;
  categoriaId: string;
  receta: RecetaRow[];
}

const FORM_VACIO: FormState = { nombre: "", descripcion: "", precio: "", categoriaId: "", receta: [] };

export default function ProductosPage() {
  const { productos, isLoading, error, fetchProductos, createProducto, updateProducto, deleteProducto, toggleDisponibilidad } =
    useProductosStore();
  const { categorias, fetchCategorias } = useCategoriasStore();
  const { ingredientes, fetchIngredientes } = useIngredientesStore();
  const snackbar = useSnackbar();

  const [dialogAbierto, setDialogAbierto] = useState(false);
  const [editando, setEditando] = useState<Producto | null>(null);
  const [form, setForm] = useState<FormState>(FORM_VACIO);
  const [guardando, setGuardando] = useState(false);
  const [aEliminar, setAEliminar] = useState<Producto | null>(null);
  const [eliminando, setEliminando] = useState(false);
  const [alternando, setAlternando] = useState<number | null>(null);

  useEffect(() => {
    void fetchProductos();
    void fetchCategorias();
    void fetchIngredientes();
  }, [fetchProductos, fetchCategorias, fetchIngredientes]);

  const abrirNuevo = () => {
    setEditando(null);
    setForm(FORM_VACIO);
    setDialogAbierto(true);
  };

  const abrirEdicion = (p: Producto) => {
    setEditando(p);
    setForm({
      nombre: p.nombre,
      descripcion: p.descripcion ?? "",
      precio: p.precio != null ? nf(p.precio) : "",
      categoriaId: p.categoria?.id ? String(p.categoria.id) : "",
      receta: (p.recetas ?? []).map((r) => ({
        ingredienteId: (r.ingrediente as { id?: number })?.id ?? "",
        cantidad: nf(r.cantidadRequerida ?? 0),
      })),
    });
    setDialogAbierto(true);
  };

  const guardar = async () => {
    if (!form.nombre.trim()) {
      snackbar.showSnackbar("El producto necesita un nombre", "warning");
      return;
    }
    const precio = Number(form.precio);
    if (Number.isNaN(precio) || precio <= 0) {
      snackbar.showSnackbar("Ingresá un precio válido", "warning");
      return;
    }
    setGuardando(true);
    try {
      const datos = {
        nombre: form.nombre.trim(),
        descripcion: form.descripcion.trim() || undefined,
        precio,
        categoria: form.categoriaId ? { id: Number(form.categoriaId) } : undefined,
        recetas: form.receta
          .filter((r) => r.ingredienteId !== "")
          .map((r) => ({ ingrediente: { id: Number(r.ingredienteId) }, cantidadRequerida: Number(r.cantidad) || 0 })),
      };
      if (editando) {
        await updateProducto(editando.id, datos);
        snackbar.showSnackbar("Producto actualizado", "success");
      } else {
        await createProducto(datos);
        snackbar.showSnackbar("Producto creado", "success");
      }
      setDialogAbierto(false);
    } catch (e) {
      snackbar.showSnackbar(getErrorMessage(e) || "No se pudo guardar el producto", "error");
    } finally {
      setGuardando(false);
    }
  };

  const alternarDisponible = async (p: Producto) => {
    setAlternando(p.id);
    try {
      await toggleDisponibilidad(p.id);
    } catch (e) {
      snackbar.showSnackbar(getErrorMessage(e) || "No se pudo cambiar la disponibilidad", "error");
    } finally {
      setAlternando(null);
    }
  };

  const confirmarEliminar = async () => {
    if (!aEliminar) return;
    setEliminando(true);
    try {
      await deleteProducto(aEliminar.id);
      snackbar.showSnackbar("Producto eliminado", "success");
      setAEliminar(null);
    } catch (e) {
      snackbar.showSnackbar(getErrorMessage(e) || "No se pudo eliminar el producto", "error");
    } finally {
      setEliminando(false);
    }
  };

  const columnas: Column<Producto>[] = [
    {
      key: "nombre",
      header: "Producto",
      render: (p) => (
        <Box>
          <Typography variant="body2" fontWeight={600}>
            {p.nombre}
          </Typography>
          {p.descripcion && (
            <Typography variant="caption" color="text.secondary">
              {p.descripcion}
            </Typography>
          )}
        </Box>
      ),
    },
    { key: "categoria", header: "Categoría", width: 150, render: (p) => p.categoria?.nombre ?? "—" },
    {
      key: "precio",
      header: "Precio",
      align: "right",
      width: 110,
      render: (p) => (p.precio != null ? precioAR.format(p.precio) : "—"),
    },
    {
      key: "receta",
      header: "Receta",
      width: 130,
      render: (p) =>
        p.recetas && p.recetas.length > 0 ? `${p.recetas.length} ingrediente${p.recetas.length > 1 ? "s" : ""}` : "Sin receta",
    },
    {
      key: "disponible",
      header: "Disponible",
      width: 120,
      render: (p) => (
        <Tooltip title={p.estaAgotado ? "Marcar como disponible" : "Marcar como agotado"}>
          <Switch
            checked={!p.estaAgotado}
            disabled={alternando === p.id}
            onClick={(e) => e.stopPropagation()}
            onChange={() => alternarDisponible(p)}
            inputProps={{ "aria-label": `Disponibilidad de ${p.nombre}` }}
          />
        </Tooltip>
      ),
    },
    {
      key: "acciones",
      header: "",
      align: "right",
      width: "1%",
      render: (p) => (
        <Box sx={{ whiteSpace: "nowrap" }}>
          <IconButton
            size="small"
            aria-label={`Editar ${p.nombre}`}
            onClick={(e) => {
              e.stopPropagation();
              abrirEdicion(p);
            }}
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            aria-label={`Eliminar ${p.nombre}`}
            onClick={(e) => {
              e.stopPropagation();
              setAEliminar(p);
            }}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <PageHeader
        title="Productos"
        subtitle="El menú que ven tus clientes y con el que arma pedidos el bot"
        action={
          <Button variant="contained" startIcon={<AddIcon />} onClick={abrirNuevo}>
            Nuevo producto
          </Button>
        }
      />

      <DataTable
        columns={columnas}
        rows={productos}
        getRowId={(p) => p.id}
        loading={isLoading}
        error={error}
        onRetry={fetchProductos}
        onRowClick={abrirEdicion}
        emptyTitle="Todavía no hay productos"
        emptyDescription="Cargá el primero para empezar a armar el menú."
        emptyAction={
          <Button variant="contained" startIcon={<AddIcon />} onClick={abrirNuevo}>
            Nuevo producto
          </Button>
        }
      />

      <FormDialog
        open={dialogAbierto}
        title={editando ? "Editar producto" : "Nuevo producto"}
        onClose={() => setDialogAbierto(false)}
        onSubmit={guardar}
        submitting={guardando}
        submitLabel={editando ? "Guardar cambios" : "Crear producto"}
        maxWidth="md"
      >
        <TextField
          label="Nombre"
          value={form.nombre}
          onChange={(e) => setForm((f) => ({ ...f, nombre: e.target.value }))}
          autoFocus
          required
          fullWidth
        />
        <TextField
          label="Descripción"
          value={form.descripcion}
          onChange={(e) => setForm((f) => ({ ...f, descripcion: e.target.value }))}
          multiline
          minRows={2}
          fullWidth
        />
        <Box sx={{ display: "flex", gap: 2, flexWrap: "wrap" }}>
          <TextField
            label="Precio ($)"
            type="number"
            value={form.precio}
            onChange={(e) => setForm((f) => ({ ...f, precio: e.target.value }))}
            required
            sx={{ flex: 1, minWidth: 160 }}
          />
          <TextField
            label="Categoría"
            select
            value={form.categoriaId}
            onChange={(e) => setForm((f) => ({ ...f, categoriaId: e.target.value }))}
            sx={{ flex: 1, minWidth: 160 }}
          >
            <MenuItem value="">Sin categoría</MenuItem>
            {categorias.map((c) => (
              <MenuItem key={c.id} value={String(c.id)}>
                {c.nombre}
              </MenuItem>
            ))}
          </TextField>
        </Box>

        <RecetaEditor
          value={form.receta}
          onChange={(receta) => setForm((f) => ({ ...f, receta }))}
          ingredientes={ingredientes}
        />
      </FormDialog>

      <ConfirmDialog
        open={!!aEliminar}
        title="Eliminar producto"
        message={aEliminar ? `¿Eliminar "${aEliminar.nombre}" del menú?` : ""}
        confirmText={eliminando ? "Eliminando…" : "Eliminar"}
        severity="error"
        onConfirm={confirmarEliminar}
        onCancel={() => setAEliminar(null)}
      />

      <FeedbackSnackbar
        open={snackbar.open}
        message={snackbar.message}
        severity={snackbar.severity}
        onClose={snackbar.hideSnackbar}
      />
    </Box>
  );
}
