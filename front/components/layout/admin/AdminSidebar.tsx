"use client";

import React, { useState } from "react";
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Box,
  Typography,
  Divider,
  useMediaQuery,
  useTheme,
  IconButton,
} from "@mui/material";
import {
  Dashboard as DashboardIcon,
  ShoppingBag as PedidosIcon,
  Restaurant as MenuIcon,
  Inventory as StockIcon,
  EventNote as ReservasIcon,
  BarChart as ReportesIcon,
  ChevronLeft,
} from "@mui/icons-material";
import { usePathname, useRouter } from "next/navigation";

interface AdminSidebarProps {
  localId: string;
  mobileOpen?: boolean;
  onMobileClose?: () => void;
}

/**
 * Sidebar de navegación para el panel de Admin
 * Incluye links a todas las secciones del panel
 */
export const AdminSidebar: React.FC<AdminSidebarProps> = ({ localId, mobileOpen = false, onMobileClose }) => {
  const theme = useTheme();
  const pathname = usePathname();
  const router = useRouter();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));
  const [collapsed, setCollapsed] = useState(false);

  const drawerWidth = collapsed ? 70 : 240;

  const menuItems = [
    {
      label: "Dashboard",
      icon: <DashboardIcon />,
      path: `/admin/${localId}`,
    },
    {
      label: "Pedidos",
      icon: <PedidosIcon />,
      path: `/admin/${localId}/pedidos`,
    },
    {
      label: "Menú",
      icon: <MenuIcon />,
      path: `/admin/${localId}/menu`,
    },
    {
      label: "Stock",
      icon: <StockIcon />,
      path: `/admin/${localId}/stock`,
    },
    {
      label: "Reservas",
      icon: <ReservasIcon />,
      path: `/admin/${localId}/reservas`,
    },
    {
      label: "Reportes",
      icon: <ReportesIcon />,
      path: `/admin/${localId}/reportes`,
    },
  ];

  const handleNavigation = (path: string) => {
    router.push(path);
    if (isMobile && onMobileClose) {
      onMobileClose();
    }
  };

  const drawerContent = (
    <Box sx={{ height: "100%", display: "flex", flexDirection: "column" }}>
      {/* Logo y Título */}
      <Box
        sx={{
          p: 2,
          display: "flex",
          alignItems: "center",
          justifyContent: collapsed ? "center" : "space-between",
          borderBottom: "1px solid",
          borderColor: "divider",
        }}
      >
        {!collapsed && (
          <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
            <Typography variant="h6" sx={{ fontSize: "1.5rem" }}>
              🍔
            </Typography>
            <Typography
              variant="h6"
              sx={{
                fontWeight: 700,
                background: (theme) =>
                  `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.secondary.main} 100%)`,
                WebkitBackgroundClip: "text",
                WebkitTextFillColor: "transparent",
              }}
            >
              Dio Burger
            </Typography>
          </Box>
        )}

        {!isMobile && (
          <IconButton size="small" onClick={() => setCollapsed(!collapsed)}>
            <ChevronLeft
              sx={{
                transform: collapsed ? "rotate(180deg)" : "rotate(0deg)",
                transition: "transform 0.3s",
              }}
            />
          </IconButton>
        )}
      </Box>

      {/* Menú de Navegación */}
      <List sx={{ flexGrow: 1, px: 1, py: 2 }}>
        {menuItems.map((item) => {
          const isActive = pathname === item.path;

          return (
            <ListItem key={item.path} disablePadding sx={{ mb: 0.5 }}>
              <ListItemButton
                onClick={() => handleNavigation(item.path)}
                sx={{
                  borderRadius: 2,
                  backgroundColor: isActive ? "primary.main" : "transparent",
                  color: isActive ? "primary.contrastText" : "text.primary",
                  "&:hover": {
                    backgroundColor: isActive ? "primary.dark" : (theme) => `${theme.palette.primary.main}1A`,
                  },
                  justifyContent: collapsed ? "center" : "flex-start",
                  px: collapsed ? 1 : 2,
                }}
              >
                <ListItemIcon
                  sx={{
                    color: isActive ? "primary.contrastText" : "text.secondary",
                    minWidth: collapsed ? "auto" : 40,
                  }}
                >
                  {item.icon}
                </ListItemIcon>
                {!collapsed && <ListItemText primary={item.label} />}
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>

      <Divider />

      {/* Footer Info */}
      {!collapsed && (
        <Box sx={{ p: 2 }}>
          <Typography variant="caption" color="text.secondary">
            Panel de Administrador
          </Typography>
        </Box>
      )}
    </Box>
  );

  // Mobile: Drawer temporal
  if (isMobile) {
    return (
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={onMobileClose}
        ModalProps={{
          keepMounted: true, // Better performance on mobile
        }}
        sx={{
          "& .MuiDrawer-paper": {
            width: 240,
            boxSizing: "border-box",
          },
        }}
      >
        {drawerContent}
      </Drawer>
    );
  }

  // Desktop: Drawer permanente
  return (
    <Drawer
      variant="permanent"
      sx={{
        width: drawerWidth,
        flexShrink: 0,
        "& .MuiDrawer-paper": {
          width: drawerWidth,
          boxSizing: "border-box",
          borderRight: "1px solid",
          borderColor: "divider",
          transition: "width 0.3s",
        },
      }}
    >
      {drawerContent}
    </Drawer>
  );
};
