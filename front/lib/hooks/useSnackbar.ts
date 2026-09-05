import { useState, useCallback } from "react";

export type SnackbarSeverity = "success" | "error" | "warning" | "info";

interface SnackbarState {
  open: boolean;
  message: string;
  severity: SnackbarSeverity;
}

/**
 * Hook para manejar Snackbars (notificaciones)
 *
 * @example
 * const { open, message, severity, showSnackbar, hideSnackbar } = useSnackbar();
 *
 * // Mostrar notificación de éxito
 * showSnackbar("Guardado correctamente", "success");
 *
 * // Mostrar notificación de error
 * showSnackbar("Error al guardar", "error");
 */
export const useSnackbar = () => {
  const [snackbar, setSnackbar] = useState<SnackbarState>({
    open: false,
    message: "",
    severity: "info",
  });

  const showSnackbar = useCallback((message: string, severity: SnackbarSeverity = "info") => {
    setSnackbar({
      open: true,
      message,
      severity,
    });
  }, []);

  const hideSnackbar = useCallback(() => {
    setSnackbar((prev) => ({ ...prev, open: false }));
  }, []);

  return {
    open: snackbar.open,
    message: snackbar.message,
    severity: snackbar.severity,
    showSnackbar,
    hideSnackbar,
  };
};
