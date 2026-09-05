"use client";

import { useEffect, useState } from "react";
import {
  Box,
  Button,
  Chip,
  FormControlLabel,
  IconButton,
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
import { useCategoriasStore } from "@/lib/stores/categoriasStore";
import { useSnackbar } from "@/lib/hooks/useSnackbar";
import { getErrorMessage } from "@/lib/api/axios";
import type { Categoria } from "@/types/producto";

interface FormState {
  nombre: string;
  descripcion: string;
  orden: string;
  activo: boolean;
}

const FORM_VACIO: FormState = { nombre: "", descripcion: "", orden: "0", activo: true };

export default function CategoriasPage() {
  const { categorias, isLoading, error, fetchCategorias, createCategoria, updateCategoria, deleteCategoria } =
    useCategoriasStore();
  const snackbar = useSnackbar();

  const [dialogAbierto, setDialogAbierto] = useState(false);
  const [editando, setEditando] = useState<Categoria | null>(null);
  const [form, setForm] = useState<FormState>(FORM_VACIO);
  const [guardando, setGuardando] = useState(false);
  const [aEliminar, setAEliminar] = useState<Categoria | null>(null);
  const [eliminando, setEliminando] = useState(false);

  useEffect(() => {
    void fetchCategorias();
  }, [fetchCategorias]);

  const abrirNueva = () => {
    setEditando(null);
    setForm(FORM_VACIO);
    setDialogAbierto(true);
  };

  const abrirEdicion = (c: Categoria) => {
    setEditando(c);
    setForm({
      nombre: c.nombre,
      descripcion: c.descripcion ?? "",
      orden: String(c.orden ?? 0),
      activo: c.activo,
    });
    setDialogAbierto(true);
  };

  const guardar = async () => {
    if (!form.nombre.trim()) {
      snackbar.showSnackbar("La categoría necesita un nombre", "warning");
      return;
    }
    setGuardando(true);
    try {
      const datos = {
        nombre: form.nombre.trim(),
        descripcion: form.descripcion.trim() || undefined,
        orden: Number(form.orden) || 0,
        activo: form.activo,
      };
      if (editando) {
        await updateCategoria(editando.id, datos);
        snackbar.showSnackbar("Categoría actualizada", "success");
      } else {
        await createCategoria(datos);
        snackbar.showSnackbar("Categoría creada", "success");
      }
      setDialogAbierto(false);
    } catch (e) {
      snackbar.showSnackbar(getErrorMessage(e) || "No se pudo guardar la categoría", "error");
    } finally {
      setGuardando(false);
    }
  };

  const confirmarEliminar = async () => {
    if (!aEliminar) return;
    setEliminando(true);
    try {
      await deleteCategoria(aEliminar.id);
      snackbar.showSnackbar("Categoría eliminada", "success");
      setAEliminar(null);
    } catch (e) {
      snackbar.showSnackbar(getErrorMessage(e) || "No se pudo eliminar la categoría", "error");
    } finally {
      setEliminando(false);
    }
  };

  const columnas: Column<Categoria>[] = [
    {
      key: "nombre",
      header: "Nombre",
      render: (c) => (
        <Box>
          <Typography variant="body2" fontWeight={600}>
            {c.nombre}
          </Typography>
          {c.descripcion && (
            <Typography variant="caption" color="text.secondary">
              {c.descripcion}
            </Typography>
          )}
        </Box>
      ),
    },
    { key: "orden", header: "Orden", align: "center", width: 80, render: (c) => c.orden ?? 0 },
    {
      key: "productos",
      header: "Productos",
      align: "center",
      width: 110,
      render: (c) => c.cantidadProductos ?? 0,
    },
    {
      key: "estado",
      header: "Estado",
      width: 120,
      render: (c) => (
        <Chip
          label={c.activo ? "Activa" : "Inactiva"}
          size="small"
          color={c.activo ? "success" : "default"}
          variant={c.activo ? "filled" : "outlined"}
        />
      ),
    },
    {
      key: "acciones",
      header: "",
      align: "right",
      width: "1%",
      render: (c) => (
        <Box sx={{ whiteSpace: "nowrap" }}>
          <IconButton
            size="small"
            aria-label={`Editar ${c.nombre}`}
            onClick={(e) => {
              e.stopPropagation();
              abrirEdicion(c);
            }}
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            aria-label={`Eliminar ${c.nombre}`}
            onClick={(e) => {
              e.stopPropagation();
              setAEliminar(c);
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
        title="Categorías"
        subtitle="Las secciones en las que se agrupa el menú"
        action={
          <Button variant="contained" startIcon={<AddIcon />} onClick={abrirNueva}>
            Nueva categoría
          </Button>
        }
      />

      <DataTable
        columns={columnas}
        rows={[...categorias].sort((a, b) => (a.orden ?? 0) - (b.orden ?? 0))}
        getRowId={(c) => c.id}
        loading={isLoading}
        error={error}
        onRetry={fetchCategorias}
        onRowClick={abrirEdicion}
        emptyTitle="Todavía no hay categorías"
        emptyDescription="Creá la primera para empezar a ordenar el menú."
        emptyAction={
          <Button variant="contained" startIcon={<AddIcon />} onClick={abrirNueva}>
            Nueva categoría
          </Button>
        }
      />

      <FormDialog
        open={dialogAbierto}
        title={editando ? "Editar categoría" : "Nueva categoría"}
        onClose={() => setDialogAbierto(false)}
        onSubmit={guardar}
        submitting={guardando}
        submitLabel={editando ? "Guardar cambios" : "Crear categoría"}
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
          label="Orden"
          type="number"
          value={form.orden}
          onChange={(e) => setForm((f) => ({ ...f, orden: e.target.value }))}
          helperText="Menor número, aparece primero"
          fullWidth
        />
        <FormControlLabel
          control={
            <Switch
              checked={form.activo}
              onChange={(e) => setForm((f) => ({ ...f, activo: e.target.checked }))}
            />
          }
          label="Categoría activa"
        />
      </FormDialog>

      <ConfirmDialog
        open={!!aEliminar}
        title="Eliminar categoría"
        message={
          aEliminar
            ? `¿Eliminar "${aEliminar.nombre}"? Los productos que la usen quedan sin categoría.`
            : ""
        }
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
