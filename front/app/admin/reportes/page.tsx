"use client";

import { Box, Typography, Paper, Alert } from "@mui/material";
import { Assessment as AssessmentIcon } from "@mui/icons-material";

/**
 * Reportes y Analytics.
 * Requiere plan PREMIUM (feature-gated).
 */
export default function ReportesPage() {
  return (
    <Box>
      <Typography variant="h4" fontWeight={700} gutterBottom>
        Reportes
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Analytics y reportes avanzados
      </Typography>

      <Alert severity="info" sx={{ mb: 4 }}>
        Esta funcionalidad requiere el plan <strong>PREMIUM</strong> o superior.
      </Alert>

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
        <AssessmentIcon sx={{ fontSize: 80, color: "primary.main", mb: 2 }} />
        <Typography variant="h5" gutterBottom>
          Reportes Avanzados en Desarrollo
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Gráficos de ventas, top productos, comparación de períodos, exportación a PDF/Excel.
        </Typography>
      </Paper>
    </Box>
  );
}
