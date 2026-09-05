"use client";

import { useState } from "react";
import {
  AppBar,
  Toolbar,
  Container,
  Button,
  IconButton,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  Box,
  useScrollTrigger,
  Slide,
} from "@mui/material";
import { Menu as MenuIcon, Close, WhatsApp } from "@mui/icons-material";
import { useRouter } from "next/navigation";

interface NavbarProps {
  window?: () => Window;
}

/**
 * Navbar principal de la landing page
 * Incluye navegación a secciones y botón de login
 * Se oculta al hacer scroll down y aparece al hacer scroll up
 */
export const Navbar = ({ window }: NavbarProps) => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const router = useRouter();

  // Efecto de hide/show al hacer scroll
  const trigger = useScrollTrigger({
    target: window ? window() : undefined,
  });

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const scrollToSection = (sectionId: string) => {
    const element = document.getElementById(sectionId);
    element?.scrollIntoView({ behavior: "smooth" });
    setMobileOpen(false);
  };

  const handleLogin = () => {
    router.push("/login");
  };

  const scrollToTop = () => {
    if (typeof globalThis.window !== "undefined") {
      globalThis.window.scrollTo({ top: 0, behavior: "smooth" });
    }
  };

  const menuItems = [
    { label: "Inicio", action: scrollToTop },
    { label: "Características", action: () => scrollToSection("caracteristicas") },
    { label: "Planes", action: () => scrollToSection("planes") },
    { label: "Contacto", action: () => scrollToSection("contacto") },
  ];

  // Drawer para mobile
  const drawer = (
    <Box sx={{ width: 250, pt: 2 }}>
      <Box sx={{ display: "flex", justifyContent: "flex-end", px: 2, mb: 2 }}>
        <IconButton onClick={handleDrawerToggle}>
          <Close />
        </IconButton>
      </Box>
      <List>
        {menuItems.map((item, index) => (
          <ListItem key={index} disablePadding>
            <ListItemButton onClick={item.action}>
              <ListItemText primary={item.label} />
            </ListItemButton>
          </ListItem>
        ))}
        <ListItem disablePadding>
          <ListItemButton onClick={handleLogin}>
            <ListItemText primary="Iniciar Sesión" />
          </ListItemButton>
        </ListItem>
      </List>
    </Box>
  );

  return (
    <>
      <Slide appear={false} direction="down" in={!trigger}>
        <AppBar
          position="fixed"
          elevation={0}
          sx={{
            backgroundColor: (theme) => `${theme.palette.background.default}F2`,
            backdropFilter: "blur(10px)",
            borderBottom: (theme) => `1px solid ${theme.palette.primary.main}1A`,
          }}
        >
          <Container maxWidth="lg">
            <Toolbar disableGutters>
              {/* Logo */}
              <Box
                sx={{
                  display: "flex",
                  alignItems: "center",
                  gap: 1,
                  flexGrow: 1,
                  cursor: "pointer",
                }}
                onClick={scrollToTop}
              >
                <Box sx={{ fontSize: "2rem" }}>🍔</Box>
                <Box
                  sx={{
                    fontWeight: 700,
                    fontSize: "1.5rem",
                    background: (theme) =>
                      `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.secondary.main} 100%)`,
                    WebkitBackgroundClip: "text",
                    WebkitTextFillColor: "transparent",
                  }}
                >
                  Dio Burger
                </Box>
              </Box>

              {/* Desktop Menu */}
              <Box sx={{ display: { xs: "none", md: "flex" }, gap: 2 }}>
                {menuItems.map((item, index) => (
                  <Button
                    key={index}
                    onClick={item.action}
                    sx={{
                      color: "text.primary",
                      "&:hover": {
                        backgroundColor: (theme) => `${theme.palette.primary.main}1A`,
                      },
                    }}
                  >
                    {item.label}
                  </Button>
                ))}
                <Button
                  variant="outlined"
                  color="primary"
                  onClick={handleLogin}
                  sx={{
                    fontWeight: 600,
                  }}
                >
                  Iniciar Sesión
                </Button>
                <Button
                  variant="contained"
                  color="primary"
                  startIcon={<WhatsApp />}
                  href="https://wa.me/549349366512?text=Hola,%20quiero%20información%20sobre%20Dio%20Burger"
                  target="_blank"
                >
                  WhatsApp
                </Button>
              </Box>

              {/* Mobile Menu Button */}
              <IconButton
                color="inherit"
                aria-label="open drawer"
                edge="start"
                onClick={handleDrawerToggle}
                sx={{ display: { md: "none" }, color: "primary.main" }}
              >
                <MenuIcon />
              </IconButton>
            </Toolbar>
          </Container>
        </AppBar>
      </Slide>

      {/* Mobile Drawer */}
      <Drawer
        anchor="right"
        open={mobileOpen}
        onClose={handleDrawerToggle}
        ModalProps={{
          keepMounted: true, // Better open performance on mobile
        }}
        sx={{
          display: { xs: "block", md: "none" },
          "& .MuiDrawer-paper": {
            boxSizing: "border-box",
            width: 250,
          },
        }}
      >
        {drawer}
      </Drawer>

      {/* Spacer para que el contenido no quede detrás del navbar */}
      <Toolbar />
    </>
  );
};
