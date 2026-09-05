"use client";

import { Box, Container, Typography, Grid, Paper, Stack, useTheme } from "@mui/material";
import { ShoppingBag, Inventory, Notifications, BarChart, Restaurant, LocalShipping } from "@mui/icons-material";

/**
 * Sección de características principales de Dio Burger
 * Muestra 6 features en un grid responsive
 */
export const Features = () => {
  const theme = useTheme();

  const features = [
    {
      icon: <ShoppingBag sx={{ fontSize: 50 }} />,
      title: "Gestión de Pedidos Multi-Canal",
      description:
        "Pedidos por WhatsApp (bot automático), delivery, take away, mostrador y reservas. Todo centralizado en un solo sistema.",
      colorType: "primary",
    },
    {
      icon: <Notifications sx={{ fontSize: 50 }} />,
      title: "Bot de WhatsApp Inteligente",
      description:
        "El bot gestiona automáticamente los mensajes de WhatsApp: toma pedidos, responde consultas, confirma órdenes y notifica estados. Disponible 24/7.",
      colorType: "success",
    },
    {
      icon: <Restaurant sx={{ fontSize: 50 }} />,
      title: "Panel de Mostrador",
      description:
        "Toma pedidos presenciales con la misma facilidad. Los clientes que vienen al local también se registran en el sistema con su orden.",
      colorType: "secondary",
    },
    {
      icon: <Inventory sx={{ fontSize: 50 }} />,
      title: "Control de Stock Inteligente",
      description:
        "Inventario en tiempo real con alertas automáticas. El bot de WhatsApp notifica cuando un producto se agota. Nunca más perderás ventas.",
      colorType: "error",
    },
    {
      icon: <LocalShipping sx={{ fontSize: 50 }} />,
      title: "Gestión de Delivery Automática",
      description:
        "Asignación de repartidores, tracking en tiempo real y tiempos estimados. Tus clientes siempre informados por WhatsApp.",
      colorType: "warning",
    },
    {
      icon: <BarChart sx={{ fontSize: 50 }} />,
      title: "Reportes y Analíticas Avanzadas",
      description:
        "Análisis de ventas por canal (WhatsApp, mostrador, delivery), productos más pedidos, horarios pico. Decisiones basadas en datos reales.",
      colorType: "info",
    },
  ];

  return (
    <Box
      id="caracteristicas"
      sx={{
        py: { xs: 8, md: 12 },
        backgroundColor: "#FFFFFF",
      }}
    >
      <Container maxWidth="lg">
        <Stack spacing={2} alignItems="center" textAlign="center" sx={{ mb: 8 }}>
          <Typography
            variant="h2"
            sx={{
              fontSize: { xs: "2rem", md: "2.5rem" },
              fontWeight: 700,
              color: "text.primary",
            }}
          >
            ¿Qué es{" "}
            <Box component="span" sx={{ color: "primary.main" }}>
              Dio Burger
            </Box>
            ?
          </Typography>
          <Typography
            variant="body1"
            sx={{
              fontSize: { xs: "1rem", md: "1.2rem" },
              color: "text.secondary",
              maxWidth: "700px",
            }}
          >
            Una plataforma completa de gestión diseñada específicamente para hamburgueserías. Todo lo que necesitas en
            un solo lugar.
          </Typography>
        </Stack>

        <Grid container spacing={4}>
          {features.map((feature, index) => {
            const getColor = (colorType: string) => {
              switch (colorType) {
                case "primary":
                  return theme.palette.primary.main;
                case "secondary":
                  return theme.palette.secondary.main;
                case "success":
                  return theme.palette.success.main;
                case "error":
                  return theme.palette.error.main;
                case "warning":
                  return theme.palette.warning.main;
                case "info":
                  return theme.palette.info.main;
                default:
                  return theme.palette.primary.main;
              }
            };

            const featureColor = getColor(feature.colorType);

            return (
              <Grid key={index} size={{ xs: 12, sm: 6, md: 4 }}>
                <Paper
                  elevation={0}
                  sx={{
                    p: 4,
                    height: "100%",
                    borderRadius: 3,
                    border: "1px solid",
                    borderColor: (theme) => `${theme.palette.primary.main}33`,
                    transition: "all 0.3s ease",
                    "&:hover": {
                      borderColor: featureColor,
                      transform: "translateY(-8px)",
                      boxShadow: `0 12px 24px ${featureColor}33`,
                    },
                  }}
                >
                  <Stack spacing={2} alignItems="flex-start">
                    <Box
                      sx={{
                        color: featureColor,
                        backgroundColor: `${featureColor}15`,
                        borderRadius: 2,
                        p: 1.5,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}
                    >
                      {feature.icon}
                    </Box>

                    <Typography
                      variant="h6"
                      sx={{
                        fontWeight: 600,
                        color: "text.primary",
                      }}
                    >
                      {feature.title}
                    </Typography>

                    <Typography
                      variant="body2"
                      sx={{
                        color: "text.secondary",
                        lineHeight: 1.7,
                      }}
                    >
                      {feature.description}
                    </Typography>
                  </Stack>
                </Paper>
              </Grid>
            );
          })}
        </Grid>
      </Container>
    </Box>
  );
};
