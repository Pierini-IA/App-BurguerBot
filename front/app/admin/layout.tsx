/**
 * Layout del Panel de Administración.
 * Incluye sidebar con navegación, header y área de contenido.
 */

"use client";

import React, { useState } from "react";
import {
  Box,
  Drawer,
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Divider,
  Avatar,
  Menu,
  MenuItem,
  Chip,
  useMediaQuery,
  useTheme,
  Breadcrumbs,
  Link,
} from "@mui/material";
import {
  Menu as MenuIcon,
  Dashboard as DashboardIcon,
  Restaurant as RestaurantIcon,
  Kitchen as KitchenIcon,
  TableBar as TableBarIcon,
  Category as CategoryIcon,
  AddCircle as AddCircleIcon,
  Assessment as AssessmentIcon,
  Settings as SettingsIcon,
  ExitToApp as ExitToAppIcon,
  Person as PersonIcon,
  ChevronRight as ChevronRightIcon,
  ReceiptLong as ReceiptLongIcon,
  PointOfSale as PointOfSaleIcon,
} from "@mui/icons-material";
import { useRouter, usePathname } from "next/navigation";
import { useAuth } from "@/lib/hooks/useAuth";
import { ProtectedRoute } from "@/components/shared/ProtectedRoute";
import { LocalProvider, useLocal } from "@/lib/context/LocalContext";
import { Rol } from "@/types/usuario";

const DRAWER_WIDTH = 260;

interface MenuItem {
  id: string;
  label: string;
  icon: React.ReactNode;
  path: string;
  requiredRoles?: Rol[];
  badge?: string | number;
  /** Nombre del enum `Feature` del backend; si el plan no lo tiene, se oculta el item. */
  requiredFeature?: string;
}

const menuItems: MenuItem[] = [
  {
    id: "dashboard",
    label: "Dashboard",
    icon: <DashboardIcon />,
    path: "/admin/dashboard",
  },
  {
    id: "productos",
    label: "Productos",
    icon: <RestaurantIcon />,
    path: "/admin/productos",
  },
  {
    id: "ingredientes",
    label: "Ingredientes",
    icon: <KitchenIcon />,
    path: "/admin/ingredientes",
  },
  {
    id: "mesas",
    label: "Mesas",
    icon: <TableBarIcon />,
    path: "/admin/mesas",
  },
  {
    id: "categorias",
    label: "Categorías",
    icon: <CategoryIcon />,
    path: "/admin/categorias",
  },
  {
    id: "extras",
    label: "Extras",
    icon: <AddCircleIcon />,
    path: "/admin/extras",
  },
  {
    id: "pedidos",
    label: "Pedidos",
    icon: <ReceiptLongIcon />,
    path: "/admin/pedidos",
  },
  {
    id: "cocina",
    label: "Panel de cocina",
    icon: <KitchenIcon />,
    path: "/cocina",
  },
  {
    id: "mostrador",
    label: "Mostrador",
    icon: <PointOfSaleIcon />,
    path: "/mostrador",
  },
  {
    id: "reportes",
    label: "Reportes",
    icon: <AssessmentIcon />,
    path: "/admin/reportes",
    badge: "PREMIUM",
    requiredFeature: "REPORTES_AVANZADOS",
  },
];

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    <ProtectedRoute allowedRoles={[Rol.ADMIN, Rol.SUPERADMIN]}>
      <LocalProvider>
        <AdminLayoutContent>{children}</AdminLayoutContent>
      </LocalProvider>
    </ProtectedRoute>
  );
}

function AdminLayoutContent({ children }: { children: React.ReactNode }) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const router = useRouter();
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const { hasFeature, planSuscripcion, nombreLocal } = useLocal();

  const visibleMenuItems = menuItems.filter(
    (item) => !item.requiredFeature || hasFeature(item.requiredFeature)
  );

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleProfileMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  const handleNavigate = (path: string) => {
    router.push(path);
    if (isMobile) {
      setMobileOpen(false);
    }
  };

  // Sidebar Content
  const drawer = (
    <Box sx={{ height: "100%", display: "flex", flexDirection: "column" }}>
      {/* Logo */}
      <Box
        sx={{
          p: 3,
          background: (theme) =>
            `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.primary.dark} 100%)`,
        }}
      >
        <Typography
          variant="h5"
          sx={{
            fontWeight: 700,
            color: "white",
            textAlign: "center",
          }}
        >
          🍔 Dio Burger
        </Typography>
        <Typography
          variant="caption"
          sx={{
            color: "white",
            opacity: 0.9,
            display: "block",
            textAlign: "center",
            mt: 0.5,
          }}
        >
          Panel de Admin
        </Typography>
      </Box>

      <Divider />

      {/* Navigation */}
      <List sx={{ flexGrow: 1, py: 2 }}>
        {visibleMenuItems.map((item) => {
          const isActive = pathname === item.path;

          return (
            <ListItem key={item.id} disablePadding sx={{ px: 2, mb: 0.5 }}>
              <ListItemButton
                onClick={() => handleNavigate(item.path)}
                sx={{
                  borderRadius: 2,
                  backgroundColor: isActive ? "primary.main" : "transparent",
                  color: isActive ? "white" : "text.primary",
                  "&:hover": {
                    backgroundColor: isActive ? "primary.dark" : "action.hover",
                  },
                }}
              >
                <ListItemIcon
                  sx={{
                    color: isActive ? "white" : "primary.main",
                    minWidth: 40,
                  }}
                >
                  {item.icon}
                </ListItemIcon>
                <ListItemText primary={item.label} />
                {item.badge && (
                  <Chip label={item.badge} size="small" color="secondary" sx={{ height: 20, fontSize: "0.625rem" }} />
                )}
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>

      <Divider />

      {/* Settings */}
      <List sx={{ py: 2 }}>
        <ListItem disablePadding sx={{ px: 2 }}>
          <ListItemButton onClick={() => handleNavigate("/admin/configuracion")} sx={{ borderRadius: 2 }}>
            <ListItemIcon sx={{ minWidth: 40 }}>
              <SettingsIcon />
            </ListItemIcon>
            <ListItemText primary="Configuración" />
          </ListItemButton>
        </ListItem>
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: "flex", minHeight: "100vh", backgroundColor: "background.default" }}>
      {/* AppBar */}
      <AppBar
        position="fixed"
        elevation={0}
        sx={{
          width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
          ml: { md: `${DRAWER_WIDTH}px` },
          backgroundColor: "white",
          borderBottom: "1px solid",
          borderColor: "divider",
        }}
      >
        <Toolbar>
          {/* Mobile Menu Button */}
          <IconButton
            color="inherit"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { md: "none" }, color: "text.primary" }}
          >
            <MenuIcon />
          </IconButton>

          {/* Breadcrumbs */}
          <Box sx={{ flexGrow: 1 }}>
            <Breadcrumbs separator={<ChevronRightIcon fontSize="small" />} sx={{ color: "text.secondary" }}>
              <Link underline="hover" color="inherit" onClick={() => router.push("/admin")} sx={{ cursor: "pointer" }}>
                Admin
              </Link>
              {pathname !== "/admin" && (
                <Typography color="text.primary" fontWeight={500}>
                  {menuItems.find((item) => item.path === pathname)?.label || "Página"}
                </Typography>
              )}
            </Breadcrumbs>
          </Box>

          {/* User Info */}
          <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
            <Box sx={{ textAlign: "right", display: { xs: "none", sm: "block" } }}>
              <Typography variant="body2" fontWeight={600}>
                {user?.username}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {nombreLocal ?? (user?.rol === Rol.SUPERADMIN ? "Super Admin" : "Admin")}
              </Typography>
            </Box>

            {planSuscripcion && (
              <Chip
                label={planSuscripcion}
                size="small"
                color="secondary"
                variant="outlined"
                sx={{ display: { xs: "none", md: "flex" }, fontWeight: 600 }}
              />
            )}

            <IconButton onClick={handleProfileMenuOpen} size="small">
              <Avatar
                sx={{
                  width: 40,
                  height: 40,
                  backgroundColor: "primary.main",
                }}
              >
                <PersonIcon />
              </Avatar>
            </IconButton>
          </Box>

          {/* Profile Menu */}
          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleProfileMenuClose}
            transformOrigin={{ horizontal: "right", vertical: "top" }}
            anchorOrigin={{ horizontal: "right", vertical: "bottom" }}
          >
            <MenuItem onClick={handleLogout}>
              <ListItemIcon>
                <ExitToAppIcon fontSize="small" />
              </ListItemIcon>
              <ListItemText>Cerrar Sesión</ListItemText>
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>

      {/* Sidebar */}
      <Box component="nav" sx={{ width: { md: DRAWER_WIDTH }, flexShrink: { md: 0 } }}>
        {/* Mobile Drawer */}
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: "block", md: "none" },
            "& .MuiDrawer-paper": {
              boxSizing: "border-box",
              width: DRAWER_WIDTH,
            },
          }}
        >
          {drawer}
        </Drawer>

        {/* Desktop Drawer */}
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: "none", md: "block" },
            "& .MuiDrawer-paper": {
              boxSizing: "border-box",
              width: DRAWER_WIDTH,
              borderRight: "1px solid",
              borderColor: "divider",
            },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>

      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
          minHeight: "100vh",
        }}
      >
        {/* Toolbar Spacer */}
        <Toolbar />

        {/* Page Content */}
        <Box sx={{ p: { xs: 2, sm: 3, md: 4 } }}>{children}</Box>
      </Box>
    </Box>
  );
}
