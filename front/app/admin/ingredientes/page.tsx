"use client";

import { useEffect, useMemo, useState } from "react";
import { Box, Button, Chip, IconButton, MenuItem, TextField, Typography } from "@mui/material";
import {
  Add as AddIcon,
  Edit as EditIcon,
  DeleteOutline as DeleteIcon,
  Tune as TuneIcon,
} from "@mui/icons-material";
import {
  PageHeader,
  DataTable,
  FormDialog,
  ConfirmDialog,
  FeedbackSnackbar,
  type Column,
} from "@/components/shared";
import { useIngredientesStore } from "@/lib/stores/ingredientesStore";
import { useSnackbar } from "@/lib/hooks/useSnackbar";
import { getErrorMessage } from "@/lib/api/axios";
import { UnidadMedida, UNIDAD_MEDIDA_LABEL, type Ingrediente } from "@/types/producto";

const nf = (n: number) => (Number.isInteger(n) ? String(n) : String(parseFloat(n.toFixed(2))));

interface FormState {
  nombre: string;
  stockActual: string;
  unidadMedida: UnidadMedida;
}

const FORM_VACIO: FormState = { nombre: "", stockActual: "0", unidadMedida: UnidadMedida.UNIDAD };

export default function IngredientesPage() {
  const { ingredientes, isLoading, error, fetchIngredientes, createIngrediente, updateIngrediente, ajustarStock, deleteIngrediente } =
    useIngredientesStore();
  const snackbar = useSnackbar();

  const [dialogAbierto, setDialogAbierto] = useState(false);
  const [editando, setEditando] = useState<Ingrediente | null>(null);
  const [form, setForm] = useState<FormState>(FORM_VACIO);
  const [guardando, setGuardando] = useState(false);

  const [ajuste, setAjuste] = useState<Ingrediente | null>(null);
  const [ajusteValor, setAjusteValor] = useState("0");
  const [ajustando, setAjustando] = useState(false);

  const [aEliminar, setAEliminar] = useState<Ingrediente | null>(null);
  const [eliminando, setEliminando] = useState(false);

  useEffect(() => {
    void fetchIngredientes();
  }, [fetchIngredientes]);

  const filas = useMemo(
    () => [...ingredientes].sort((a, b) => (a.stockActual ?? 0) - (b.stockActual ?? 0)),
    [ingredientes]
  );

  const abrirNuevo = () => {
    setEditando(null);
    setForm(FORM_VACIO);
    setDialogAbierto(true);
  };

  const abrirEdicion = (i: Ingrediente) => {
    setEditando(i);
    setForm({
      nombre: i.nombre,
      stockActual: nf(i.stockActual ?? 0),
      unidadMedida: i.unidadMedida ?? UnidadMedida.UNIDAD,
    });
    setDialogAbierto(true);
  };

  const guardar = async () => {
    if (!form.nombre.trim()) {
      snackbar.showSnackbar("El ingrediente necesita un nombre", "warning");
      return;
    }
    const stock = Number(form.stockActual);
    if (Number.isNaN(stock) || stock < 0) {
      snackbar.showSnackbar("Ingresá un stock válido", "warning");
      return;
    }
    setGuardando(true);
    try {
      const datos = { nombre: form.nombre.trim(), stockActual: stock, unidadMedida: form.unidadMedida };
      if (editando) {
        await updateIngrediente(editando.id, datos);
        snackbar.showSnackbar("Ingrediente actualizado", "success");
      } else {
        await createIngrediente(datos);
        snackbar.showSnackbar("Ingrediente creado", "success");
      }
      setDialogAbierto(false);
    } catch (e) {
      snackbar.showSnackbar(getErrorMessage(e) || "No se pudo guardar el ingrediente", "error");
    } finally {
      setGuardando(false);
    }
  };

  const abrirAjuste = (i: Ingrediente) => {
    setAjuste(i);
    setAjusteValor(nf(i.stockActual ?? 0));
  };

  const confirmarAjuste = async () => {
    if (!ajuste) return;
    const valor = Number(ajusteValor);
    if (Number.isNaN(valor) || valor < 0) {
      snackbar.showSnackbar("Ingresá un stock válido", "warning");
      return;
    }
    setAjustando(true);
    try {
      await ajustarStock(ajuste.id, valor);
      snackbar.showSnackbar(`Stock de ${ajuste.nombre} actualizado`, "success");
      setAjuste(null);
    } catch (e) {
      snackbar.showSnackbar(getErrorMessage(e) || "No se pudo ajustar el stock", "error");
    } finally {
      setAjustando(false);
    }
  };

  const confirmarEliminar = async () => {
    if (!aEliminar) return;
    setEliminando(true);
    try {
      await deleteIngrediente(aEliminar.id);
      snackbar.showSnackbar("Ingrediente eliminado", "success");
      setAEliminar(null);
    } catch (e) {
      snackbar.showSnackbar(
        getErrorMessage(e) || "No se pudo eliminar. Puede estar en la receta de un producto.",
        "error"
      );
    } finally {
      setEliminando(false);
    }
  };

  const columnas: Column<Ingrediente>[] = [
    {
      key: "nombre",
      header: "Ingrediente",
      render: (i) => (
        <Typography variant="body2" fontWeight={600}>
          {i.nombre}
        </Typography>
      ),
    },
    {
      key: "stock",
      header: "Stock",
      render: (i) => (
        <Box sx={{ display: "flex", alignItems: "baseline", gap: 0.75 }}>
          <Typography variant="body2" fontWeight={700}>
            {nf(i.stockActual ?? 0)}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {UNIDAD_MEDIDA_LABEL[i.unidadMedida] ?? i.unidadMedida}
          </Typography>
        </Box>
      ),
    },
    {
      key: "estado",
      header: "Estado",
      width: 130,
      render: (i) =>
        (i.stockActual ?? 0) <= 0 ? (
          <Chip label="Sin stock" size="small" color="error" />
        ) : (
          <Chip label="Disponible" size="small" color="success" variant="outlined" />
        ),
    },
    {
      key: "acciones",
      header: "",
      align: "right",
      width: "1%",
      render: (i) => (
        <Box sx={{ whiteSpace: "nowrap", display: "flex", gap: 0.5, justifyContent: "flex-end" }}>
          <Button
            size="small"
            variant="outlined"
            startIcon={<TuneIcon />}
            onClick={(e) => {
              e.stopPropagation();
              abrirAjuste(i);
            }}
          >
            Ajustar
          </Button>
          <IconButton
            size="small"
            aria-label={`Editar ${i.nombre}`}
            onClick={(e) => {
              e.stopPropagation();
              abrirEdicion(i);
            }}
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            aria-label={`Eliminar ${i.nombre}`}
            onClick={(e) => {
              e.stopPropagation();
              setAEliminar(i);
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
        title="Ingredientes"
        subtitle="Los insumos que consumen las recetas. Al confirmarse un pedido, el stock baja solo."
        action={
          <Button variant="contained" startIcon={<AddIcon />} onClick={abrirNuevo}>
            Nuevo ingrediente
          </Button>
        }
      />

      <DataTable
        columns={columnas}
        rows={filas}
        getRowId={(i) => i.id}
        loading={isLoading}
        error={error}
        onRetry={fetchIngredientes}
        onRowClick={abrirEdicion}
        emptyTitle="Todavía no hay ingredientes"
        emptyDescription="Cargá los insumos para poder armar recetas y controlar el stock."
        emptyAction={
          <Button variant="contained" startIcon={<AddIcon />} onClick={abrirNuevo}>
            Nuevo ingrediente
          </Button>
        }
      />

      {/* Alta / edición */}
      <FormDialog
        open={dialogAbierto}
        title={editando ? "Editar ingrediente" : "Nuevo ingrediente"}
        onClose={() => setDialogAbierto(false)}
        onSubmit={guardar}
        submitting={guardando}
        submitLabel={editando ? "Guardar cambios" : "Crear ingrediente"}
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
          label="Stock actual"
          type="number"
          value={form.stockActual}
          onChange={(e) => setForm((f) => ({ ...f, stockActual: e.target.value }))}
          required
          fullWidth
        />
        <TextField
          label="Unidad de medida"
          select
          value={form.unidadMedida}
          onChange={(e) => setForm((f) => ({ ...f, unidadMedida: e.target.value as UnidadMedida }))}
          fullWidth
        >
          {Object.values(UnidadMedida).map((u) => (
            <MenuItem key={u} value={u}>
              {UNIDAD_MEDIDA_LABEL[u]}
            </MenuItem>
          ))}
        </TextField>
      </FormDialog>

      {/* Ajuste rápido de stock */}
      <FormDialog
        open={!!ajuste}
        title={ajuste ? `Ajustar stock — ${ajuste.nombre}` : "Ajustar stock"}
        onClose={() => setAjuste(null)}
        onSubmit={confirmarAjuste}
        submitting={ajustando}
        submitLabel="Guardar stock"
        maxWidth="xs"
      >
        <TextField
          label="Stock actual"
          type="number"
          value={ajusteValor}
          onChange={(e) => setAjusteValor(e.target.value)}
          autoFocus
          fullWidth
          helperText={
            ajuste ? `Unidad: ${UNIDAD_MEDIDA_LABEL[ajuste.unidadMedida] ?? ajuste.unidadMedida}` : undefined
          }
        />
      </FormDialog>

      <ConfirmDialog
        open={!!aEliminar}
        title="Eliminar ingrediente"
        message={aEliminar ? `¿Eliminar "${aEliminar.nombre}"? No se puede si está en alguna receta.` : ""}
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
