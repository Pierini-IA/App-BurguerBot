"use client";

import { useEffect } from "react";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Stack,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormHelperText,
  Switch,
  FormControlLabel,
  Typography,
  Divider,
  Box,
} from "@mui/material";
import { LocalFormData, PlanSuscripcion } from "@/types/local";

// Schema de validación
const localSchema = z.object({
  nombre: z
    .string()
    .min(3, "El nombre debe tener al menos 3 caracteres")
    .max(100, "El nombre no puede superar 100 caracteres"),
  direccion: z
    .string()
    .min(5, "La dirección debe tener al menos 5 caracteres")
    .max(500, "La dirección no puede superar 500 caracteres"),
  telefono: z
    .string()
    .regex(/^\+[1-9]\d{1,14}$/, "Formato internacional requerido (ej: +5491187654321)")
    .min(10, "El teléfono debe tener al menos 10 caracteres"),
  planSuscripcion: z.nativeEnum(PlanSuscripcion).optional(),
  planActivo: z.boolean().optional(),
  fechaInicioPlan: z.string().optional(),
  fechaFinPlan: z.string().nullable().optional(),
});

interface LocalFormDialogProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: LocalFormData) => Promise<void>;
  initialData?: Partial<LocalFormData>;
  title: string;
  isLoading?: boolean;
}

/**
 * Dialog con formulario para crear/editar Local
 */
export const LocalFormDialog: React.FC<LocalFormDialogProps> = ({
  open,
  onClose,
  onSubmit,
  initialData,
  title,
  isLoading = false,
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    control,
    watch,
  } = useForm<LocalFormData>({
    resolver: zodResolver(localSchema),
    defaultValues: {
      nombre: "",
      direccion: "",
      telefono: "",
      planSuscripcion: PlanSuscripcion.PREMIUM,
      planActivo: true,
      fechaInicioPlan: new Date().toISOString().split("T")[0],
      fechaFinPlan: null,
      ...initialData,
    },
  });

  // Resetear formulario cuando cambia initialData
  useEffect(() => {
    if (initialData) {
      reset({
        planSuscripcion: PlanSuscripcion.PREMIUM,
        planActivo: true,
        fechaInicioPlan: new Date().toISOString().split("T")[0],
        fechaFinPlan: null,
        ...initialData,
      });
    }
  }, [initialData, reset]);

  const handleClose = () => {
    reset();
    onClose();
  };

  const handleFormSubmit = async (data: LocalFormData) => {
    // Convertir fechaFinPlan vacía a null
    const processedData = {
      ...data,
      fechaFinPlan: data.fechaFinPlan && data.fechaFinPlan.trim() !== "" ? data.fechaFinPlan : null,
    };

    await onSubmit(processedData);
    reset();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>{title}</DialogTitle>

      <form onSubmit={handleSubmit(handleFormSubmit)}>
        <DialogContent>
          <Stack spacing={3}>
            {/* Información Básica */}
            <Typography variant="subtitle2" color="text.secondary" sx={{ fontWeight: 600 }}>
              Información Básica
            </Typography>

            <TextField
              {...register("nombre")}
              label="Nombre del Local"
              error={!!errors.nombre}
              helperText={errors.nombre?.message}
              fullWidth
              required
              autoFocus
              disabled={isLoading}
            />

            <TextField
              {...register("direccion")}
              label="Dirección"
              error={!!errors.direccion}
              helperText={errors.direccion?.message}
              fullWidth
              required
              multiline
              rows={2}
              disabled={isLoading}
            />

            <TextField
              {...register("telefono")}
              label="Teléfono (Formato Internacional)"
              error={!!errors.telefono}
              helperText={errors.telefono?.message || "Ejemplo: +5491187654321"}
              placeholder="+5491187654321"
              fullWidth
              required
              disabled={isLoading}
            />

            <Divider sx={{ my: 2 }} />

            {/* Plan de Suscripción */}
            <Typography variant="subtitle2" color="text.secondary" sx={{ fontWeight: 600 }}>
              Plan de Suscripción
            </Typography>

            <Controller
              name="planSuscripcion"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth error={!!errors.planSuscripcion}>
                  <InputLabel>Plan de Suscripción</InputLabel>
                  <Select {...field} label="Plan de Suscripción" disabled={isLoading}>
                    <MenuItem value={PlanSuscripcion.BASICO}>
                      <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                        <Typography>BÁSICO</Typography>
                        <Typography variant="caption" color="text.secondary">
                          ($35.000/mes)
                        </Typography>
                      </Box>
                    </MenuItem>
                    <MenuItem value={PlanSuscripcion.ESTANDAR}>
                      <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                        <Typography>ESTÁNDAR</Typography>
                        <Typography variant="caption" color="text.secondary">
                          ($75.000/mes)
                        </Typography>
                      </Box>
                    </MenuItem>
                    <MenuItem value={PlanSuscripcion.PREMIUM}>
                      <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                        <Typography>PREMIUM</Typography>
                        <Typography variant="caption" color="text.secondary">
                          ($125.000/mes)
                        </Typography>
                      </Box>
                    </MenuItem>
                  </Select>
                  {errors.planSuscripcion && <FormHelperText>{errors.planSuscripcion.message}</FormHelperText>}
                </FormControl>
              )}
            />

            <Controller
              name="planActivo"
              control={control}
              render={({ field }) => (
                <FormControlLabel
                  control={<Switch {...field} checked={field.value} disabled={isLoading} />}
                  label="Plan Activo"
                />
              )}
            />

            <TextField
              {...register("fechaInicioPlan")}
              label="Fecha Inicio Plan"
              type="date"
              error={!!errors.fechaInicioPlan}
              helperText={errors.fechaInicioPlan?.message}
              InputLabelProps={{ shrink: true }}
              fullWidth
              disabled={isLoading}
            />

            <TextField
              {...register("fechaFinPlan")}
              label="Fecha Fin Plan (Vencimiento)"
              type="date"
              error={!!errors.fechaFinPlan}
              helperText={errors.fechaFinPlan?.message || "Dejar vacío para plan indefinido"}
              InputLabelProps={{ shrink: true }}
              fullWidth
              disabled={isLoading}
            />
          </Stack>
        </DialogContent>

        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={handleClose} disabled={isLoading}>
            Cancelar
          </Button>
          <Button type="submit" variant="contained" disabled={isLoading}>
            {isLoading ? "Guardando..." : "Guardar"}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};
