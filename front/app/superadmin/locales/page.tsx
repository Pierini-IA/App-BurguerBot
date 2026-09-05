"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  Box,
  Typography,
  Button,
  TextField,
  InputAdornment,
  Stack,
  Snackbar,
  Alert,
  CircularProgress,
} from "@mui/material";
import { Add, Search } from "@mui/icons-material";
import { ProtectedRoute } from "@/components/shared/ProtectedRoute";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { LocalesTable } from "@/components/superadmin/LocalesTable";
import { LocalFormDialog } from "@/components/superadmin/LocalFormDialog";
import { Rol } from "@/types/usuario";
import { Local, LocalFormData } from "@/types/local";
import { useLocalesStore } from "@/lib/stores/localesStore";
import { useSnackbar } from "@/lib/hooks/useSnackbar";

/**
 * Página de gestión de Locales
 */
function LocalesContent() {
  const router = useRouter();
  const { locales, isLoading, error, fetchLocales, createLocal, updateLocal, deleteLocal, clearError } =
    useLocalesStore();

  const { open, message, severity, showSnackbar, hideSnackbar } = useSnackbar();

  const [searchTerm, setSearchTerm] = useState("");
  const [formDialog, setFormDialog] = useState<{
    open: boolean;
    mode: "create" | "edit";
    local?: Local;
  }>({
    open: false,
    mode: "create",
  });
  const [confirmDialog, setConfirmDialog] = useState<{
    open: boolean;
    local?: Local;
  }>({
    open: false,
  });

  // Cargar locales al montar
  useEffect(() => {
    fetchLocales();
  }, [fetchLocales]);

  // Filtrar locales por búsqueda
  const filteredLocales = locales.filter(
    (local) =>
      local.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
      local.direccion.toLowerCase().includes(searchTerm.toLowerCase()) ||
      local.telefono.includes(searchTerm)
  );

  // Abrir dialog de crear
  const handleOpenCreate = () => {
    setFormDialog({ open: true, mode: "create" });
  };

  // Abrir dialog de editar
  const handleOpenEdit = (local: Local) => {
    setFormDialog({ open: true, mode: "edit", local });
  };

  // Cerrar dialog de formulario
  const handleCloseForm = () => {
    setFormDialog({ open: false, mode: "create" });
  };

  // Crear local
  const handleCreate = async (data: LocalFormData) => {
    try {
      await createLocal(data);
      handleCloseForm();
      showSnackbar("Local creado correctamente", "success");
    } catch (error: any) {
      showSnackbar(error.message || "Error al crear local", "error");
    }
  };

  // Actualizar local
  const handleUpdate = async (data: LocalFormData) => {
    if (!formDialog.local) return;

    try {
      await updateLocal(formDialog.local.id, data);
      handleCloseForm();
      showSnackbar("Local actualizado correctamente", "success");
    } catch (error: any) {
      showSnackbar(error.message || "Error al actualizar local", "error");
    }
  };

  // Abrir dialog de confirmación para eliminar
  const handleOpenDeleteConfirm = (local: Local) => {
    setConfirmDialog({ open: true, local });
  };

  // Cerrar dialog de confirmación
  const handleCloseDeleteConfirm = () => {
    setConfirmDialog({ open: false });
  };

  // Eliminar local
  const handleDelete = async () => {
    if (!confirmDialog.local) return;

    try {
      await deleteLocal(confirmDialog.local.id);
      handleCloseDeleteConfirm();
      showSnackbar("Local eliminado correctamente", "success");
    } catch (error: any) {
      showSnackbar(error.message || "Error al eliminar local", "error");
    }
  };

  // Ir a configuración
  const handleConfig = (local: Local) => {
    router.push(`/superadmin/locales/${local.id}/config`);
  };

  if (isLoading && locales.length === 0) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", py: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      {/* Header */}
      <Stack
        direction={{ xs: "column", sm: "row" }}
        justifyContent="space-between"
        alignItems={{ xs: "stretch", sm: "center" }}
        spacing={2}
        sx={{ mb: 4 }}
      >
        <Typography variant="h4" sx={{ fontWeight: 700 }}>
          Locales
        </Typography>

        <Button variant="contained" startIcon={<Add />} onClick={handleOpenCreate} sx={{ minWidth: { sm: 180 } }}>
          Nuevo Local
        </Button>
      </Stack>

      {/* Search */}
      <Box sx={{ mb: 3 }}>
        <TextField
          placeholder="Buscar por nombre, dirección o teléfono..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          fullWidth
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <Search />
              </InputAdornment>
            ),
          }}
        />
      </Box>

      {/* Table */}
      <LocalesTable
        locales={filteredLocales}
        onEdit={handleOpenEdit}
        onDelete={handleOpenDeleteConfirm}
        onConfig={handleConfig}
      />

      {/* Form Dialog */}
      <LocalFormDialog
        open={formDialog.open}
        onClose={handleCloseForm}
        onSubmit={formDialog.mode === "create" ? handleCreate : handleUpdate}
        initialData={formDialog.local}
        title={formDialog.mode === "create" ? "Crear Nuevo Local" : "Editar Local"}
        isLoading={isLoading}
      />

      {/* Confirm Delete Dialog */}
      <ConfirmDialog
        open={confirmDialog.open}
        title="Eliminar Local"
        message={`¿Estás seguro de eliminar el local "${confirmDialog.local?.nombre}"? Esta acción no se puede deshacer.`}
        onConfirm={handleDelete}
        onCancel={handleCloseDeleteConfirm}
        confirmText="Eliminar"
        severity="error"
      />

      {/* Snackbar */}
      <Snackbar
        open={open}
        autoHideDuration={5000}
        onClose={hideSnackbar}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
      >
        <Alert onClose={hideSnackbar} severity={severity} variant="filled">
          {message}
        </Alert>
      </Snackbar>
    </Box>
  );
}

export default function LocalesPage() {
  return (
    <ProtectedRoute allowedRoles={[Rol.SUPERADMIN]}>
      <LocalesContent />
    </ProtectedRoute>
  );
}
