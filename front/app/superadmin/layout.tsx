"use client";

import { useState, ReactNode } from "react";
import { useRouter, usePathname } from "next/navigation";
import {
  Box,
  Drawer,
  AppBar,
  Toolbar,
  List,
  Typography,
  Divider,
  IconButton,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Avatar,
  Menu,
  MenuItem,
  useMediaQuery,
  useTheme,
} from "@mui/material";
import {
  Menu as MenuIcon,
  Store as StoreIcon,
  People as PeopleIcon,
  ExitToApp as LogoutIcon,
  AccountCircle,
  Dashboard as DashboardIcon,
} from "@mui/icons-material";
import { useAuth } from "@/lib/hooks/useAuth";

const DRAWER_WIDTH = 260;

interface SuperAdminLayoutProps {
  children: ReactNode;
}

/**
 * Layout del panel de SuperAdmin
 * Incluye Sidebar, AppBar y navegación
 */
export default function SuperAdminLayout({ children }: SuperAdminLayoutProps) {
  const router = useRouter();
  const pathname = usePathname();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));

  const { user, logout } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  const menuItems = [
    {
      text: "Dashboard",
      icon: <DashboardIcon />,
      path: "/superadmin",
      exact: true,
    },
    {
      text: "Locales",
      icon: <StoreIcon />,
      path: "/superadmin/locales",
    },
    {
      text: "Usuarios",
      icon: <PeopleIcon />,
      path: "/superadmin/usuarios",
    },
  ];

  const drawer = (
    <Box>
      {/* Logo / Header */}
      <Box
        sx={{
          p: 3,
          display: "flex",
          alignItems: "center",
          gap: 2,
          backgroundColor: "primary.main",
          color: "white",
        }}
      >
        <Box sx={{ fontSize: "2rem" }}>🍔</Box>
        <Box>
          <Typography variant="h6" sx={{ fontWeight: 700, lineHeight: 1 }}>
            Dio Burger
          </Typography>
          <Typography variant="caption" sx={{ opacity: 0.9 }}>
            SuperAdmin
          </Typography>
        </Box>
      </Box>

      <Divider />

      {/* Menu Items */}
      <List sx={{ px: 2, py: 2 }}>
        {menuItems.map((item) => {
          const isActive = item.exact ? pathname === item.path : pathname.startsWith(item.path);

          return (
            <ListItem key={item.text} disablePadding sx={{ mb: 0.5 }}>
              <ListItemButton
                onClick={() => {
                  router.push(item.path);
                  if (isMobile) setMobileOpen(false);
                }}
                selected={isActive}
                sx={{
                  borderRadius: 2,
                  "&.Mui-selected": {
                    backgroundColor: "primary.light",
                    color: "primary.contrastText",
                    "&:hover": {
                      backgroundColor: "primary.main",
                    },
                    "& .MuiListItemIcon-root": {
                      color: "primary.contrastText",
                    },
                  },
                }}
              >
                <ListItemIcon
                  sx={{
                    color: isActive ? "primary.contrastText" : "inherit",
                    minWidth: 40,
                  }}
                >
                  {item.icon}
                </ListItemIcon>
                <ListItemText
                  primary={item.text}
                  primaryTypographyProps={{
                    fontWeight: isActive ? 600 : 400,
                  }}
                />
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: "flex", minHeight: "100vh" }}>
      {/* AppBar */}
      <AppBar
        position="fixed"
        sx={{
          width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
          ml: { md: `${DRAWER_WIDTH}px` },
          backgroundColor: "background.paper",
          color: "text.primary",
          boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
        }}
      >
        <Toolbar>
          <IconButton color="inherit" edge="start" onClick={handleDrawerToggle} sx={{ mr: 2, display: { md: "none" } }}>
            <MenuIcon />
          </IconButton>

          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            {menuItems.find((item) => pathname.startsWith(item.path))?.text || "Dashboard"}
          </Typography>

          {/* User Menu */}
          <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
            <Box sx={{ display: { xs: "none", sm: "block" }, textAlign: "right" }}>
              <Typography variant="body2" sx={{ fontWeight: 600 }}>
                {user?.username}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {user?.rol}
              </Typography>
            </Box>

            <IconButton onClick={handleMenuOpen} size="small">
              <Avatar
                sx={{
                  width: 40,
                  height: 40,
                  backgroundColor: "primary.main",
                }}
              >
                {user?.username?.charAt(0).toUpperCase()}
              </Avatar>
            </IconButton>
          </Box>

          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleMenuClose}
            anchorOrigin={{
              vertical: "bottom",
              horizontal: "right",
            }}
            transformOrigin={{
              vertical: "top",
              horizontal: "right",
            }}
          >
            <MenuItem onClick={handleMenuClose}>
              <ListItemIcon>
                <AccountCircle fontSize="small" />
              </ListItemIcon>
              Perfil
            </MenuItem>
            <Divider />
            <MenuItem onClick={handleLogout}>
              <ListItemIcon>
                <LogoutIcon fontSize="small" />
              </ListItemIcon>
              Cerrar Sesión
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>

      {/* Drawer */}
      <Box component="nav" sx={{ width: { md: DRAWER_WIDTH }, flexShrink: { md: 0 } }}>
        {/* Mobile drawer */}
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{
            keepMounted: true, // Better open performance on mobile
          }}
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

        {/* Desktop drawer */}
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
          p: 3,
          width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
          minHeight: "100vh",
          backgroundColor: "background.default",
        }}
      >
        <Toolbar /> {/* Spacer for AppBar */}
        {children}
      </Box>
    </Box>
  );
}
