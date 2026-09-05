"use client";

import { Alert, Snackbar } from "@mui/material";
import { SnackbarSeverity } from "@/lib/hooks/useSnackbar";
import { defaults } from "@/lib/config/defaults";

interface FeedbackSnackbarProps {
  open: boolean;
  message: string;
  severity: SnackbarSeverity;
  onClose: () => void;
}

/**
 * Snackbar de feedback, pensado para usarse con el hook `useSnackbar`.
 *
 * @example
 * const snackbar = useSnackbar();
 * // ...
 * snackbar.showSnackbar("Guardado", "success");
 * // en el JSX:
 * <FeedbackSnackbar {...snackbar} onClose={snackbar.hideSnackbar} />
 */
export const FeedbackSnackbar: React.FC<FeedbackSnackbarProps> = ({ open, message, severity, onClose }) => (
  <Snackbar
    open={open}
    autoHideDuration={defaults.snackbarAutoHideDuration}
    onClose={onClose}
    anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
  >
    <Alert onClose={onClose} severity={severity} variant="filled" sx={{ width: "100%" }}>
      {message}
    </Alert>
  </Snackbar>
);
