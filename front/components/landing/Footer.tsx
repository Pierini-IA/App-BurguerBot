"use client";

import { Box, Container, Typography, Stack, Link, Divider, IconButton } from "@mui/material";
import { Email, Phone, Instagram, Facebook } from "@mui/icons-material";

/**
 * Footer de la landing page
 * Incluye información de contacto y links
 */
export const Footer = () => {
  const currentYear = new Date().getFullYear();

  return (
    <Box
      component="footer"
      sx={{
        backgroundColor: "text.primary",
        color: "#FFF",
        py: { xs: 6, md: 8 },
      }}
    >
      <Container maxWidth="lg">
        <Stack direction={{ xs: "column", md: "row" }} spacing={4} justifyContent="space-between" sx={{ mb: 4 }}>
          {/* Información de la empresa */}
          <Stack spacing={2} sx={{ maxWidth: { xs: "100%", md: "400px" } }}>
            <Typography
              variant="h5"
              sx={{
                fontWeight: 700,
                background: (theme) =>
                  `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.secondary.main} 100%)`,
                WebkitBackgroundClip: "text",
                WebkitTextFillColor: "transparent",
              }}
            >
              🍔 Dio Burger
            </Typography>
            <Typography variant="body2" sx={{ color: "rgba(255,255,255,0.7)" }}>
              Sistema completo de gestión para hamburgueserías. Optimiza tu negocio con tecnología de vanguardia.
            </Typography>
          </Stack>

          {/* Links rápidos */}
          <Stack spacing={2}>
            <Typography variant="h6" sx={{ fontWeight: 600 }}>
              Enlaces Rápidos
            </Typography>
            <Link
              href="#caracteristicas"
              sx={{
                color: "rgba(255,255,255,0.7)",
                textDecoration: "none",
                "&:hover": {
                  color: "primary.main",
                },
              }}
            >
              Características
            </Link>
            <Link
              href="#contacto"
              sx={{
                color: "rgba(255,255,255,0.7)",
                textDecoration: "none",
                "&:hover": {
                  color: "primary.main",
                },
              }}
            >
              Contacto
            </Link>
          </Stack>

          {/* Información de contacto */}
          <Stack spacing={2}>
            <Typography variant="h6" sx={{ fontWeight: 600 }}>
              Contacto
            </Typography>
            <Stack direction="row" spacing={1} alignItems="center">
              <Phone sx={{ fontSize: 20, color: "primary.main" }} />
              <Link
                href="https://wa.me/549349366512"
                target="_blank"
                sx={{
                  color: "rgba(255,255,255,0.7)",
                  textDecoration: "none",
                  "&:hover": {
                    color: "primary.main",
                  },
                }}
              >
                +54 9 3493 66512
              </Link>
            </Stack>
            <Stack direction="row" spacing={1} alignItems="center">
              <Email sx={{ fontSize: 20, color: "primary.main" }} />
              <Link
                href="mailto:info@dioburger.com"
                sx={{
                  color: "rgba(255,255,255,0.7)",
                  textDecoration: "none",
                  "&:hover": {
                    color: "primary.main",
                  },
                }}
              >
                info@dioburger.com
              </Link>
            </Stack>

            {/* Redes sociales */}
            <Stack direction="row" spacing={1} sx={{ mt: 2 }}>
              <IconButton
                size="small"
                sx={{
                  color: "rgba(255,255,255,0.7)",
                  "&:hover": {
                    color: "primary.main",
                    backgroundColor: (theme) => `${theme.palette.primary.main}1A`,
                  },
                }}
              >
                <Instagram />
              </IconButton>
              <IconButton
                size="small"
                sx={{
                  color: "rgba(255,255,255,0.7)",
                  "&:hover": {
                    color: "primary.main",
                    backgroundColor: (theme) => `${theme.palette.primary.main}1A`,
                  },
                }}
              >
                <Facebook />
              </IconButton>
            </Stack>
          </Stack>
        </Stack>

        <Divider sx={{ borderColor: "rgba(255,255,255,0.1)", mb: 3 }} />

        {/* Copyright */}
        <Stack direction={{ xs: "column", sm: "row" }} spacing={2} justifyContent="space-between" alignItems="center">
          <Typography variant="body2" sx={{ color: "rgba(255,255,255,0.5)" }}>
            © {currentYear} Dio Burger. Todos los derechos reservados.
          </Typography>
          <Stack direction="row" spacing={2}>
            <Link
              href="#"
              sx={{
                color: "rgba(255,255,255,0.5)",
                textDecoration: "none",
                fontSize: "0.875rem",
                "&:hover": {
                  color: "primary.main",
                },
              }}
            >
              Términos de Servicio
            </Link>
            <Link
              href="#"
              sx={{
                color: "rgba(255,255,255,0.5)",
                textDecoration: "none",
                fontSize: "0.875rem",
                "&:hover": {
                  color: "primary.main",
                },
              }}
            >
              Política de Privacidad
            </Link>
          </Stack>
        </Stack>
      </Container>
    </Box>
  );
};
