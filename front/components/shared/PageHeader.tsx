import { Box, Stack, Typography } from "@mui/material";
import { ReactNode } from "react";

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  /** Acción principal a la derecha (ej. botón "Nuevo producto"). */
  action?: ReactNode;
}

/**
 * Encabezado estándar de las páginas del panel: título, bajada y una acción.
 */
export const PageHeader: React.FC<PageHeaderProps> = ({ title, subtitle, action }) => (
  <Stack
    direction={{ xs: "column", sm: "row" }}
    spacing={2}
    justifyContent="space-between"
    alignItems={{ xs: "flex-start", sm: "center" }}
    sx={{ mb: 4 }}
  >
    <Box>
      <Typography variant="h4" fontWeight={700} gutterBottom>
        {title}
      </Typography>
      {subtitle && (
        <Typography variant="body1" color="text.secondary">
          {subtitle}
        </Typography>
      )}
    </Box>
    {action && <Box sx={{ flexShrink: 0 }}>{action}</Box>}
  </Stack>
);
