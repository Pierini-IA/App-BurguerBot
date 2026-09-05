"use client";

import React, { useEffect, useState } from "react";
import {
  Box,
  Button,
  Card,
  CardContent,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  Stack,
  CircularProgress,
  Alert,
  Snackbar,
} from "@mui/material";
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, Key as KeyIcon } from "@mui/icons-material";
import { useUsuariosStore } from "@/lib/stores/usuariosStore";
import { Usuario, Rol, UsuarioCreateData } from "@/types/usuario";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

// Schema de validación con Zod
const usuarioSchema = z.object({
  username: z.string().min(3, "El username debe tener al menos 3 caracteres"),
  password: z.string().min(6, "La contraseña debe tener al menos 6 caracteres").optional(),
  rol: z.nativeEnum(Rol),
  telefonoLocal: z.string().optional(),
});

type UsuarioFormData = z.infer<typeof usuarioSchema>;

export default function UsuariosPage() {
  const {
    usuarios,
    isLoading,
    error,
    fetchUsuarios,
    createUsuario,
    updateUsuario,
    deleteUsuario,
    changePassword,
    clearError,
  } = useUsuariosStore();

  const [openDialog, setOpenDialog] = useState(false);
  const [openPasswordDialog, setOpenPasswordDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [selectedUsuario, setSelectedUsuario] = useState<Usuario | null>(null);
  const [newPassword, setNewPassword] = useState("");
  const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "success" as "success" | "error" });

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<UsuarioFormData>({
    resolver: zodResolver(usuarioSchema),
    defaultValues: {
      username: "",
      password: "",
      rol: Rol.ADMIN,
      telefonoLocal: "",
    },
  });

  useEffect(() => {
    fetchUsuarios();
  }, [fetchUsuarios]);

  const handleOpenDialog = (usuario?: Usuario) => {
    if (usuario) {
      setSelectedUsuario(usuario);
      reset({
        username: usuario.username,
        password: "", // No mostrar la contraseña
        rol: usuario.rol,
        telefonoLocal: usuario.telefonoLocal || "",
      });
    } else {
      setSelectedUsuario(null);
      reset({
        username: "",
        password: "",
        rol: Rol.ADMIN,
        telefonoLocal: "",
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedUsuario(null);
    reset();
  };

  const handleOpenPasswordDialog = (usuario: Usuario) => {
    setSelectedUsuario(usuario);
    setNewPassword("");
    setOpenPasswordDialog(true);
  };

  const handleClosePasswordDialog = () => {
    setOpenPasswordDialog(false);
    setSelectedUsuario(null);
    setNewPassword("");
  };

  const handleOpenDeleteDialog = (usuario: Usuario) => {
    setSelectedUsuario(usuario);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedUsuario(null);
  };

  const onSubmit = async (data: UsuarioFormData) => {
    try {
      if (selectedUsuario && selectedUsuario.id) {
        // Actualizar
        await updateUsuario(selectedUsuario.id, {
          username: data.username,
          password: data.password || undefined,
          rol: data.rol,
          telefonoLocal: data.telefonoLocal || undefined,
        });
        setSnackbar({ open: true, message: "Usuario actualizado exitosamente", severity: "success" });
      } else {
        // Crear
        const createData: UsuarioCreateData = {
          username: data.username,
          password: data.password || "",
          rol: data.rol,
          telefonoLocal: data.telefonoLocal || undefined,
        };
        await createUsuario(createData);
        setSnackbar({ open: true, message: "Usuario creado exitosamente", severity: "success" });
      }
      handleCloseDialog();
      fetchUsuarios();
    } catch (error: any) {
      setSnackbar({
        open: true,
        message: error.response?.data?.message || "Error al guardar usuario",
        severity: "error",
      });
    }
  };

  const handleDelete = async () => {
    if (selectedUsuario && selectedUsuario.id) {
      try {
        await deleteUsuario(selectedUsuario.id);
        setSnackbar({ open: true, message: "Usuario eliminado exitosamente", severity: "success" });
        handleCloseDeleteDialog();
        fetchUsuarios();
      } catch (error: any) {
        setSnackbar({
          open: true,
          message: error.response?.data?.message || "Error al eliminar usuario",
          severity: "error",
        });
      }
    }
  };

  const handleChangePassword = async () => {
    if (selectedUsuario && selectedUsuario.id && newPassword) {
      try {
        await changePassword(selectedUsuario.id, newPassword);
        setSnackbar({ open: true, message: "Contraseña cambiada exitosamente", severity: "success" });
        handleClosePasswordDialog();
      } catch (error: any) {
        setSnackbar({
          open: true,
          message: error.response?.data?.message || "Error al cambiar contraseña",
          severity: "error",
        });
      }
    }
  };

  const getRolColor = (rol: Rol) => {
    switch (rol) {
      case Rol.SUPERADMIN:
        return "error";
      case Rol.ADMIN:
        return "primary";
      case Rol.COCINA:
        return "secondary";
      default:
        return "default";
    }
  };

  return (
    <Box>
      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 3 }}>
        <Typography variant="h4" component="h1">
          Gestión de Usuarios
        </Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
          Nuevo Usuario
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={clearError}>
          {error}
        </Alert>
      )}

      <Card>
        <CardContent>
          {isLoading ? (
            <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
              <CircularProgress />
            </Box>
          ) : (
            <TableContainer component={Paper} elevation={0}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>ID</TableCell>
                    <TableCell>Username</TableCell>
                    <TableCell>Rol</TableCell>
                    <TableCell>Local</TableCell>
                    <TableCell>Teléfono Local</TableCell>
                    <TableCell align="right">Acciones</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {usuarios.map((usuario) => (
                    <TableRow key={usuario.id}>
                      <TableCell>{usuario.id}</TableCell>
                      <TableCell>{usuario.username}</TableCell>
                      <TableCell>
                        <Chip label={usuario.rol.replace("ROLE_", "")} color={getRolColor(usuario.rol)} size="small" />
                      </TableCell>
                      <TableCell>{usuario.localNombre || "-"}</TableCell>
                      <TableCell>{usuario.telefonoLocal || "-"}</TableCell>
                      <TableCell align="right">
                        <IconButton size="small" onClick={() => handleOpenDialog(usuario)} title="Editar">
                          <EditIcon fontSize="small" />
                        </IconButton>
                        <IconButton
                          size="small"
                          onClick={() => handleOpenPasswordDialog(usuario)}
                          title="Cambiar contraseña"
                        >
                          <KeyIcon fontSize="small" />
                        </IconButton>
                        <IconButton
                          size="small"
                          color="error"
                          onClick={() => handleOpenDeleteDialog(usuario)}
                          title="Eliminar"
                        >
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                  {usuarios.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={6} align="center">
                        No hay usuarios registrados
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </CardContent>
      </Card>

      {/* Dialog para crear/editar usuario */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <form onSubmit={handleSubmit(onSubmit)}>
          <DialogTitle>{selectedUsuario ? "Editar Usuario" : "Nuevo Usuario"}</DialogTitle>
          <DialogContent>
            <Stack spacing={3} sx={{ mt: 1 }}>
              <Controller
                name="username"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Username"
                    error={!!errors.username}
                    helperText={errors.username?.message}
                    fullWidth
                    required
                  />
                )}
              />

              <Controller
                name="password"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label={selectedUsuario ? "Nueva Contraseña (dejar vacío para mantener)" : "Contraseña"}
                    type="password"
                    error={!!errors.password}
                    helperText={errors.password?.message}
                    fullWidth
                    required={!selectedUsuario}
                  />
                )}
              />

              <Controller
                name="rol"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Rol"
                    select
                    error={!!errors.rol}
                    helperText={errors.rol?.message}
                    fullWidth
                    required
                  >
                    <MenuItem value={Rol.SUPERADMIN}>SUPERADMIN</MenuItem>
                    <MenuItem value={Rol.ADMIN}>ADMIN</MenuItem>
                    <MenuItem value={Rol.COCINA}>COCINA</MenuItem>
                  </TextField>
                )}
              />

              <Controller
                name="telefonoLocal"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Teléfono del Local"
                    error={!!errors.telefonoLocal}
                    helperText={errors.telefonoLocal?.message || "Requerido para roles ADMIN y COCINA"}
                    fullWidth
                  />
                )}
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseDialog}>Cancelar</Button>
            <Button type="submit" variant="contained">
              {selectedUsuario ? "Actualizar" : "Crear"}
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Dialog para cambiar contraseña */}
      <Dialog open={openPasswordDialog} onClose={handleClosePasswordDialog} maxWidth="xs" fullWidth>
        <DialogTitle>Cambiar Contraseña</DialogTitle>
        <DialogContent>
          <TextField
            label="Nueva Contraseña"
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            fullWidth
            sx={{ mt: 2 }}
            required
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClosePasswordDialog}>Cancelar</Button>
          <Button onClick={handleChangePassword} variant="contained" disabled={!newPassword}>
            Cambiar
          </Button>
        </DialogActions>
      </Dialog>

      {/* Dialog de confirmación para eliminar */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>Confirmar Eliminación</DialogTitle>
        <DialogContent>
          <Typography>
            ¿Estás seguro de que deseas eliminar al usuario <strong>{selectedUsuario?.username}</strong>?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>Cancelar</Button>
          <Button onClick={handleDelete} variant="contained" color="error">
            Eliminar
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar para mensajes */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
      >
        <Alert
          onClose={() => setSnackbar({ ...snackbar, open: false })}
          severity={snackbar.severity}
          sx={{ width: "100%" }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}
