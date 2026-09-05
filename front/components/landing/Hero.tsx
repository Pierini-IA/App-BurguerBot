"use client";

import { Box, Container, Typography, Button, Stack } from "@mui/material";
import { WhatsApp, ArrowForward } from "@mui/icons-material";

/**
 * Hero section de la landing page
 * Incluye título principal, descripción y CTAs
 */
export const Hero = () => {
  const scrollToPlanes = () => {
    const planesSection = document.getElementById("planes");
    planesSection?.scrollIntoView({ behavior: "smooth" });
  };

  const scrollToContact = () => {
    const contactSection = document.getElementById("contacto");
    contactSection?.scrollIntoView({ behavior: "smooth" });
  };

  return (
    <Box
      sx={{
        minHeight: "100vh",
        background: (theme) =>
          `linear-gradient(135deg, ${theme.palette.background.default} 0%, ${theme.palette.primary.light} 15%, ${theme.palette.background.default} 100%)`,
        display: "flex",
        alignItems: "center",
        position: "relative",
        overflow: "hidden",
      }}
    >
      {/* Decoración de fondo */}
      <Box
        sx={{
          position: "absolute",
          top: "-10%",
          right: "-5%",
          width: "40%",
          height: "40%",
          borderRadius: "50%",
          bgcolor: (theme) => `${theme.palette.primary.main}15`,
          filter: "blur(100px)",
        }}
      />
      <Box
        sx={{
          position: "absolute",
          bottom: "-10%",
          left: "-5%",
          width: "40%",
          height: "40%",
          borderRadius: "50%",
          bgcolor: (theme) => `${theme.palette.secondary.main}15`,
          filter: "blur(100px)",
        }}
      />

      <Container maxWidth="lg" sx={{ position: "relative", zIndex: 1 }}>
        <Stack spacing={4} alignItems="center" textAlign="center" sx={{ py: 8 }}>
          {/* Logo/Emoji grande */}
          <Box
            sx={{
              fontSize: { xs: "4rem", md: "6rem" },
              animation: "bounce 2s ease-in-out infinite",
              "@keyframes bounce": {
                "0%, 100%": {
                  transform: "translateY(0)",
                },
                "50%": {
                  transform: "translateY(-20px)",
                },
              },
            }}
          >
            🍔
          </Box>

          {/* Título principal */}
          <Typography
            variant="h1"
            sx={{
              fontSize: { xs: "2.5rem", md: "3.5rem", lg: "4rem" },
              fontWeight: 700,
              color: "text.primary",
              maxWidth: "900px",
            }}
          >
            Dio Burger
            <br />
            <Box
              component="span"
              sx={{
                background: (theme) =>
                  `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.secondary.main} 100%)`,
                WebkitBackgroundClip: "text",
                WebkitTextFillColor: "transparent",
              }}
            >
              Gestión Inteligente
            </Box>
            <br />
            para tu Hamburguesería
          </Typography>

          {/* Subtítulo */}
          <Typography
            variant="h5"
            sx={{
              fontSize: { xs: "1.1rem", md: "1.5rem" },
              color: "text.secondary",
              maxWidth: "700px",
              lineHeight: 1.6,
            }}
          >
            Automatiza tu restaurante con IA, Bot de WhatsApp y reportes en tiempo real.
            <br />
            Desde $35.000/mes con setup profesional incluido.
          </Typography>

          {/* CTAs */}
          <Stack direction={{ xs: "column", sm: "row" }} spacing={2} sx={{ mt: 4 }}>
            <Button
              variant="contained"
              size="large"
              endIcon={<ArrowForward />}
              onClick={scrollToPlanes}
              sx={{
                px: 4,
                py: 1.5,
                fontSize: "1.1rem",
                boxShadow: "0 4px 14px rgba(255, 155, 133, 0.4)",
                "&:hover": {
                  boxShadow: "0 6px 20px rgba(255, 155, 133, 0.6)",
                  transform: "translateY(-2px)",
                },
                transition: "all 0.3s ease",
              }}
            >
              Ver planes y precios
            </Button>

            <Button
              variant="outlined"
              size="large"
              startIcon={<WhatsApp />}
              href="https://wa.me/549349366512?text=Hola,%20quiero%20información%20sobre%20Dio%20Burger"
              target="_blank"
              sx={{
                px: 4,
                py: 1.5,
                fontSize: "1.1rem",
                borderColor: "primary.main",
                color: "primary.main",
                "&:hover": {
                  borderColor: "primary.dark",
                  backgroundColor: "rgba(255, 155, 133, 0.1)",
                },
              }}
            >
              Consultar por WhatsApp
            </Button>
          </Stack>

          {/* Badge */}
          <Box
            sx={{
              mt: 6,
              px: 3,
              py: 1,
              borderRadius: 2,
              backgroundColor: "rgba(255, 255, 255, 0.8)",
              backdropFilter: "blur(10px)",
              border: "1px solid rgba(255, 155, 133, 0.2)",
            }}
          >
            <Typography variant="body2" color="text.secondary">
              🤖 Bot IA WhatsApp • 📊 Reportes Avanzados • 🖨️ Impresión Automática • � 3 Planes disponibles
            </Typography>
          </Box>
        </Stack>
      </Container>
    </Box>
  );
};
