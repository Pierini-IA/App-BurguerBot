"use client";

import { useEffect, useState } from "react";
import {
  Box,
  Button,
  Chip,
  FormControlLabel,
  IconButton,
  MenuItem,
  Switch,
  TextField,
  Typography,
} from "@mui/material";
import {
  Add as AddIcon,
  Edit as EditIcon,
  DeleteOutline as DeleteIcon,
} from "@mui/icons-material";
import {
  PageHeader,
  DataTable,
  FormDialog,
  ConfirmDialog,
  FeedbackSnackbar,
  type Column,
} from "@/components/shared";
import { useExtrasStore } from "@/lib/stores/extrasStore";
import { useCategoriasStore } from "@/lib/stores/categoriasStore";
import { useSnackbar } from "@/lib/hooks/useSnackbar";
import { getErrorMessage } from "@/lib/api/axios";
import type { Extra } from "@/types/producto";

const precioAR = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 0 });

interface FormState {
  nombre: string;
  descripcion: string;
  precio: string;
  categoriaId: string; // "" = sin categoría
  activo: boolean;
}

const FORM_VACIO: FormState = { nombre: "", descripcion: "", precio: "", categoriaId: "", activo: true };

export default function ExtrasPage() {
  const { extras, isLoading, error, fetchExtras, createExtra, updateExtra, deleteExtra } = useExtrasStore();
  const { categorias, fetchCategorias } = useCategoriasStore();
  const snackbar = useSnackbar();

  const [dialogAbierto, setDialogAbierto] = useState(false);
  const [editando, setEditando] = useState<Extra | null>(null);
  const [form, setForm] = useState<FormState>(FORM_VACIO);
  const [guardando, setGuardando] = useState(false);
  const [aEliminar, setAEliminar] = useState<Extra | null>(null);
  const [eliminando, setEliminando] = useState(false);

  useEffect(() => {
    void fetchExtras();
    void fetchCategorias();
  }, [fetchExtras, fetchCategorias]);

  const abrirNuevo = () => {
    setEditando(null);
    setForm(FORM_VACIO);
    setDialogAbierto(true);
  };

  const abrirEdicion = (x: Extra) => {
    setEditando(x);
    setForm({
      nombre: x.nombre,
      descripcion: x.descripcion ?? "",
      precio: String(x.precioAdicional ?? ""),
      categoriaId: x.categoriaId ? String(x.categoriaId) : "",
      activo: x.activo,
    });
    setDialogAbierto(true);
  };

  const guardar = async () => {
    if (!form.nombre.trim()) {
      snackbar.showSnackbar("El extra necesita un nombre", "warning");
      return;
    }
    const precio = Number(form.precio);
    if (Number.isNaN(precio) || precio < 0) {
      snackbar.showSnackbar("Ingresá un precio válido", "warning");
      return;
    }
    setGuardando(true);
    try {
      const datos = {
        nombre: form.nombre.trim(),
        descripcion: form.descripcion.trim() || undefined,
        precioAdicional: precio,
        categoriaId: form.categoriaId ? Number(form.categoriaId) : undefined,
        activo: form.activo,
      };
      if (editando) {
        await updateExtra(editando.id, datos);
        snackbar.showSnackbar("Extra actualizado", "success");
      } else {
        await createExtra(datos);
        snackbar.showSnackbar("Extra creado", "success");
      }
      setDialogAbierto(false);
    } catch (e) {
      snackbar.showSnackbar(getErrorMessage(e) || "No se pudo guardar el extra", "error");
    } finally {
      setGuardando(false);
    }
  };

  const confirmarEliminar = async () => {
    if (!aEliminar) return;
    setEliminando(true);
    try {
      await deleteExtra(aEliminar.id);
      snackbar.showSnackbar("Extra eliminado", "success");
      setAEliminar(null);
    } catch (e) {
      snackbar.showSnackbar(getErrorMessage(e) || "No se pudo eliminar el extra", "error");
    } finally {
      setEliminando(false);
    }
  };

  const columnas: Column<Extra>[] = [
    {
      key: "nombre",
      header: "Nombre",
      render: (x) => (
        <Box>
          <Typography variant="body2" fontWeight={600}>
            {x.nombre}
          </Typography>
          {x.descripcion && (
            <Typography variant="caption" color="text.secondary">
              {x.descripcion}
            </Typography>
          )}
        </Box>
      ),
    },
    {
      key: "precio",
      header: "Precio",
      align: "right",
      width: 120,
      render: (x) => precioAR.format(x.precioAdicional ?? 0),
    },
    {
      key: "categoria",
      header: "Categoría",
      width: 160,
      render: (x) => x.categoriaNombre ?? "—",
    },
    {
      key: "estado",
      header: "Estado",
      width: 120,
      render: (x) => (
        <Chip
          label={x.activo ? "Activo" : "Inactivo"}
          size="small"
          color={x.activo ? "success" : "default"}
          variant={x.activo ? "filled" : "outlined"}
        />
      ),
    },
    {
      key: "acciones",
      header: "",
      align: "right",
      width: "1%",
      render: (x) => (
        <Box sx={{ whiteSpace: "nowrap" }}>
          <IconButton
            size="small"
            aria-label={`Editar ${x.nombre}`}
            onClick={(e) => {
              e.stopPropagation();
              abrirEdicion(x);
            }}
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            aria-label={`Eliminar ${x.nombre}`}
            onClick={(e) => {
              e.stopPropagation();
              setAEliminar(x);
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
        title="Extras"
        subtitle="Adicionales que el cliente puede sumar a un producto"
        action={
          <Button variant="contained" startIcon={<AddIcon />} onClick={abrirNuevo}>
            Nuevo extra
          </Button>
        }
      />

      <DataTable
        columns={columnas}
        rows={extras}
        getRowId={(x) => x.id}
        loading={isLoading}
        error={error}
        onRetry={fetchExtras}
        onRowClick={abrirEdicion}
        emptyTitle="Todavía no hay extras"
        emptyDescription="Creá adicionales como “Bacon”, “Cheddar extra” o “Papas”."
        emptyAction={
          <Button variant="contained" startIcon={<AddIcon />} onClick={abrirNuevo}>
            Nuevo extra
          </Button>
        }
      />

      <FormDialog
        open={dialogAbierto}
        title={editando ? "Editar extra" : "Nuevo extra"}
        onClose={() => setDialogAbierto(false)}
        onSubmit={guardar}
        submitting={guardando}
        submitLabel={editando ? "Guardar cambios" : "Crear extra"}
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
        <TextField
          label="Precio adicional ($)"
          type="number"
          value={form.precio}
          onChange={(e) => setForm((f) => ({ ...f, precio: e.target.value }))}
          required
          fullWidth
        />
        <TextField
          label="Categoría"
          select
          value={form.categoriaId}
          onChange={(e) => setForm((f) => ({ ...f, categoriaId: e.target.value }))}
          helperText="Opcional: acota a qué productos aplica"
          fullWidth
        >
          <MenuItem value="">Sin categoría</MenuItem>
          {categorias.map((c) => (
            <MenuItem key={c.id} value={String(c.id)}>
              {c.nombre}
            </MenuItem>
          ))}
        </TextField>
        <FormControlLabel
          control={
            <Switch
              checked={form.activo}
              onChange={(e) => setForm((f) => ({ ...f, activo: e.target.checked }))}
            />
          }
          label="Extra activo"
        />
      </FormDialog>

      <ConfirmDialog
        open={!!aEliminar}
        title="Eliminar extra"
        message={aEliminar ? `¿Eliminar "${aEliminar.nombre}"?` : ""}
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
