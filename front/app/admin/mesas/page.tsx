"use client";

import { Box, Typography, Paper } from "@mui/material";
import { TableBar as TableBarIcon } from "@mui/icons-material";

/**
 * Gestión de Mesas.
 * Administración de mesas del local.
 */
export default function MesasPage() {
  return (
    <Box>
      <Typography variant="h4" fontWeight={700} gutterBottom>
        Mesas
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Administra las mesas de tu local
      </Typography>

      <Paper
        elevation={0}
        sx={{
          p: 6,
          borderRadius: 3,
          border: "1px solid",
          borderColor: "divider",
          textAlign: "center",
        }}
      >
        <TableBarIcon sx={{ fontSize: 80, color: "primary.main", mb: 2 }} />
        <Typography variant="h5" gutterBottom>
          Gestión de Mesas en Desarrollo
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Visualización de estado, edición de capacidad, gestión de disponibilidad.
        </Typography>
      </Paper>
    </Box>
  );
}
