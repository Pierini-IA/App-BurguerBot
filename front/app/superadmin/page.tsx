"use client";

import { useEffect } from "react";
import { ProtectedRoute } from "@/components/shared/ProtectedRoute";
import { Box, Grid, Paper, Typography, Stack } from "@mui/material";
import { Store, People, TrendingUp, CheckCircle } from "@mui/icons-material";
import { Rol } from "@/types/usuario";
import { useLocalesStore } from "@/lib/stores/localesStore";
import { useUsuariosStore } from "@/lib/stores/usuariosStore";

/**
 * Dashboard del SuperAdmin
 * Muestra estadísticas generales y accesos rápidos
 */
function SuperAdminContent() {
  const { locales, fetchLocales } = useLocalesStore();
  const { usuarios, fetchUsuarios } = useUsuariosStore();

  useEffect(() => {
    fetchLocales();
    fetchUsuarios();
  }, [fetchLocales, fetchUsuarios]);

  const stats = [
    {
      title: "Locales Totales",
      value: locales.length,
      icon: <Store sx={{ fontSize: 40 }} />,
      color: "primary.main",
      bgColor: "rgba(255, 155, 133, 0.1)",
    },
    {
      title: "Configuraciones",
      value: locales.length,
      icon: <CheckCircle sx={{ fontSize: 40 }} />,
      color: "success.main",
      bgColor: "rgba(76, 175, 80, 0.1)",
    },
    {
      title: "Usuarios Totales",
      value: usuarios.length,
      icon: <People sx={{ fontSize: 40 }} />,
      color: "info.main",
      bgColor: "rgba(33, 150, 243, 0.1)",
    },
    {
      title: "Roles Únicos",
      value: new Set(usuarios.map((u) => u.rol)).size,
      icon: <TrendingUp sx={{ fontSize: 40 }} />,
      color: "warning.main",
      bgColor: "rgba(255, 193, 7, 0.1)",
    },
  ];

  return (
    <Box>
      <Typography variant="h4" sx={{ fontWeight: 700, mb: 4 }}>
        Dashboard
      </Typography>

      {/* Stats Grid */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {stats.map((stat, index) => (
          <Grid key={index} size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper
              elevation={0}
              sx={{
                p: 3,
                height: "100%",
                borderRadius: 3,
                border: "1px solid",
                borderColor: "divider",
                transition: "all 0.3s ease",
                "&:hover": {
                  transform: "translateY(-4px)",
                  boxShadow: "0 8px 24px rgba(0,0,0,0.1)",
                },
              }}
            >
              <Stack spacing={2}>
                <Box
                  sx={{
                    width: 60,
                    height: 60,
                    borderRadius: 2,
                    backgroundColor: stat.bgColor,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    color: stat.color,
                  }}
                >
                  {stat.icon}
                </Box>

                <Box>
                  <Typography variant="h3" sx={{ fontWeight: 700, color: stat.color }}>
                    {stat.value}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {stat.title}
                  </Typography>
                </Box>
              </Stack>
            </Paper>
          </Grid>
        ))}
      </Grid>

      {/* Quick Actions */}
      <Paper
        elevation={0}
        sx={{
          p: 4,
          borderRadius: 3,
          border: "1px solid",
          borderColor: "divider",
        }}
      >
        <Typography variant="h6" sx={{ fontWeight: 600, mb: 3 }}>
          Accesos Rápidos
        </Typography>

        <Typography variant="body2" color="text.secondary">
          Usa el menú lateral para navegar entre Locales y Usuarios
        </Typography>
      </Paper>
    </Box>
  );
}

export default function SuperAdminPage() {
  return (
    <ProtectedRoute allowedRoles={[Rol.SUPERADMIN]}>
      <SuperAdminContent />
    </ProtectedRoute>
  );
}
