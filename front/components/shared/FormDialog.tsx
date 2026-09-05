"use client";

import { FormEvent, ReactNode } from "react";
import {
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
} from "@mui/material";

interface FormDialogProps {
  open: boolean;
  title: string;
  onClose: () => void;
  /** Se llama al enviar el form. Manejá el guardado y el cierre acá. */
  onSubmit: () => void;
  children: ReactNode;
  submitting?: boolean;
  submitLabel?: string;
  cancelLabel?: string;
  /** Deshabilita el botón de submit (ej. form inválido). */
  submitDisabled?: boolean;
  maxWidth?: "xs" | "sm" | "md";
}

/**
 * Dialog genérico para formularios de alta / edición.
 * Envuelve el contenido en un <form>: Enter envía, y el botón principal
 * muestra spinner mientras `submitting`.
 */
export const FormDialog: React.FC<FormDialogProps> = ({
  open,
  title,
  onClose,
  onSubmit,
  children,
  submitting = false,
  submitLabel = "Guardar",
  cancelLabel = "Cancelar",
  submitDisabled = false,
  maxWidth = "sm",
}) => {
  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!submitting) onSubmit();
  };

  return (
    <Dialog open={open} onClose={submitting ? undefined : onClose} maxWidth={maxWidth} fullWidth>
      <form onSubmit={handleSubmit} noValidate>
        <DialogTitle>{title}</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 1, display: "flex", flexDirection: "column", gap: 2 }}>{children}</Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={submitting}>
            {cancelLabel}
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={submitting || submitDisabled}
            startIcon={submitting ? <CircularProgress size={16} color="inherit" /> : undefined}
          >
            {submitLabel}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};
