"use client";

import { Box, Container, Typography, Grid, Paper, Stack, Chip } from "@mui/material";
import { WhatsApp, SmartToy, AutoAwesome, Speed, CheckCircle, Timer } from "@mui/icons-material";

/**
 * Sección destacada del Bot de WhatsApp
 * Explica los beneficios de la automatización
 */
export const WhatsAppBotSection = () => {
  const benefits = [
    {
      icon: <Speed sx={{ fontSize: 40 }} />,
      title: "Respuestas Instantáneas",
      description: "El bot responde en menos de 1 segundo, 24/7. Nunca pierdas un cliente por demora.",
    },
    {
      icon: <SmartToy sx={{ fontSize: 40 }} />,
      title: "Toma de Pedidos Automática",
      description: "El cliente pide, el bot procesa y confirma. Sin intervención humana hasta la cocina.",
    },
    {
      icon: <CheckCircle sx={{ fontSize: 40 }} />,
      title: "Confirmación y Tracking",
      description: "Confirma pedidos automáticamente y notifica cada cambio de estado al cliente.",
    },
    {
      icon: <Timer sx={{ fontSize: 40 }} />,
      title: "Tiempo de Entrega Estimado",
      description: "Calcula y envía tiempos estimados según la carga de pedidos actual.",
    },
  ];

  return (
    <Box
      sx={{
        py: { xs: 8, md: 12 },
        background: (theme) =>
          `linear-gradient(135deg, ${theme.palette.success.light}33 0%, ${theme.palette.success.light}1A 100%)`,
        position: "relative",
        overflow: "hidden",
      }}
    >
      {/* Decoración de fondo */}
      <Box
        sx={{
          position: "absolute",
          top: "-20%",
          right: "-10%",
          width: "50%",
          height: "50%",
          borderRadius: "50%",
          background: (theme) => `${theme.palette.success.main}1A`,
          filter: "blur(100px)",
        }}
      />

      <Container maxWidth="lg" sx={{ position: "relative", zIndex: 1 }}>
        <Stack spacing={6}>
          {/* Encabezado */}
          <Stack spacing={2} alignItems="center" textAlign="center">
            <Chip
              icon={<AutoAwesome />}
              label="Tecnología de Vanguardia"
              color="success"
              sx={{
                fontWeight: 600,
                px: 2,
                py: 3,
                fontSize: "0.9rem",
              }}
            />

            <Typography
              variant="h2"
              sx={{
                fontSize: { xs: "2rem", md: "2.5rem" },
                fontWeight: 700,
                color: "text.primary",
              }}
            >
              Bot de WhatsApp
              <br />
              <Box
                component="span"
                sx={{
                  color: "success.main",
                }}
              >
                100% Automático
              </Box>
            </Typography>

            <Typography
              variant="body1"
              sx={{
                fontSize: { xs: "1rem", md: "1.2rem" },
                color: "text.secondary",
                maxWidth: "700px",
              }}
            >
              Olvídate de contestar mensajes manualmente. Nuestro bot gestiona
              <strong> toda la conversación</strong> con tus clientes: desde la consulta inicial hasta la entrega del
              pedido.
            </Typography>
          </Stack>

          {/* Beneficios en grid */}
          <Grid container spacing={3}>
            {benefits.map((benefit, index) => (
              <Grid key={index} size={{ xs: 12, sm: 6 }}>
                <Paper
                  elevation={0}
                  sx={{
                    p: 3,
                    height: "100%",
                    backgroundColor: "rgba(255, 255, 255, 0.9)",
                    borderRadius: 3,
                    border: (theme) => `2px solid ${theme.palette.success.main}`,
                    transition: "all 0.3s ease",
                    "&:hover": {
                      transform: "translateY(-4px)",
                      boxShadow: (theme) => `0 8px 24px ${theme.palette.success.main}4D`,
                    },
                  }}
                >
                  <Stack direction="row" spacing={2} alignItems="flex-start">
                    <Box
                      sx={{
                        color: "success.main",
                        backgroundColor: (theme) => `${theme.palette.success.main}1A`,
                        borderRadius: 2,
                        p: 1.5,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}
                    >
                      {benefit.icon}
                    </Box>

                    <Stack spacing={1}>
                      <Typography
                        variant="h6"
                        sx={{
                          fontWeight: 600,
                          color: "text.primary",
                          fontSize: "1.1rem",
                        }}
                      >
                        {benefit.title}
                      </Typography>

                      <Typography
                        variant="body2"
                        sx={{
                          color: "text.secondary",
                          lineHeight: 1.6,
                        }}
                      >
                        {benefit.description}
                      </Typography>
                    </Stack>
                  </Stack>
                </Paper>
              </Grid>
            ))}
          </Grid>

          {/* Sección de canales */}
          <Paper
            elevation={0}
            sx={{
              p: { xs: 3, md: 4 },
              borderRadius: 3,
              backgroundColor: "rgba(255, 255, 255, 0.95)",
              border: (theme) => `2px solid ${theme.palette.success.main}4D`,
            }}
          >
            <Stack spacing={3} alignItems="center" textAlign="center">
              <WhatsApp sx={{ fontSize: 60, color: "success.main" }} />

              <Typography variant="h5" sx={{ fontWeight: 600 }}>
                Todos los Canales en un Solo Lugar
              </Typography>

              <Typography variant="body1" color="text.secondary" maxWidth="700px">
                Ya sea que el cliente pida por{" "}
                <Box component="span" sx={{ color: "success.main", fontWeight: 600 }}>
                  WhatsApp
                </Box>
                , llegue al{" "}
                <Box component="span" sx={{ color: "primary.main", fontWeight: 600 }}>
                  mostrador
                </Box>{" "}
                o llame para{" "}
                <Box component="span" sx={{ color: "primary.main", fontWeight: 600 }}>
                  delivery
                </Box>
                , todos los pedidos se centralizan en el mismo sistema. Tu equipo ve todo en un solo panel.
              </Typography>

              <Stack direction={{ xs: "column", sm: "row" }} spacing={2} sx={{ mt: 2 }}>
                <Chip
                  label="🤖 WhatsApp (Bot Automático)"
                  color="success"
                  sx={{
                    fontWeight: 600,
                    px: 2,
                    py: 3,
                  }}
                />
                <Chip
                  label="🏪 Mostrador (Manual)"
                  color="primary"
                  sx={{
                    fontWeight: 600,
                    px: 2,
                    py: 3,
                  }}
                />
                <Chip
                  label="📞 Llamadas (Manual)"
                  color="primary"
                  sx={{
                    fontWeight: 600,
                    px: 2,
                    py: 3,
                  }}
                />
              </Stack>
            </Stack>
          </Paper>
        </Stack>
      </Container>
    </Box>
  );
};
