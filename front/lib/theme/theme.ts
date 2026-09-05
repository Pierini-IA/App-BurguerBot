"use client";

import { createTheme } from "@mui/material/styles";

/**
 * Tema personalizado de Material UI para Dio Burger
 * Colores principales: Tonos naranja/durazno vibrantes
 * Diseñado para máximo contraste con blanco y negro
 */
export const theme = createTheme({
  palette: {
    // ===== PRIMARIOS (Naranja/Durazno vibrante) =====
    primary: {
      main: "#FF6B35", // Naranja durazno vibrante
      light: "#FF8C5A", // Naranja claro
      dark: "#E85A2A", // Naranja oscuro
      contrastText: "#FFFFFF", // Blanco puro para máximo contraste
    },

    // ===== SECUNDARIOS (Naranja complementario) =====
    secondary: {
      main: "#FF8A5B", // Naranja coral
      light: "#FFAA80", // Coral claro
      dark: "#E66B3C", // Coral oscuro
      contrastText: "#FFFFFF",
    },

    // ===== ESTADOS =====
    success: {
      main: "#4CAF50", // Verde
      light: "#81C784",
      dark: "#388E3C",
      contrastText: "#FFFFFF",
    },
    warning: {
      main: "#FFA726", // Naranja alerta (más vibrante)
      light: "#FFB74D",
      dark: "#F57C00",
      contrastText: "#000000", // Negro para mejor contraste
    },
    error: {
      main: "#EF5350", // Rojo vibrante
      light: "#E57373",
      dark: "#C62828",
      contrastText: "#FFFFFF",
    },
    info: {
      main: "#29B6F6", // Azul vibrante
      light: "#4FC3F7",
      dark: "#0288D1",
      contrastText: "#FFFFFF",
    },

    // ===== BACKGROUNDS =====
    background: {
      default: "#FFF8F5", // Blanco cálido con tinte durazno muy sutil
      paper: "#FFFFFF", // Blanco puro para cards/modals
    },

    // ===== TEXTO =====
    text: {
      primary: "#1A1A1A", // Negro casi puro para máximo contraste
      secondary: "#4A4A4A", // Gris oscuro
      disabled: "#9E9E9E", // Gris medio
    },

    // ===== GRISES =====
    grey: {
      50: "#FAFAFA",
      100: "#F5F5F5",
      200: "#EEEEEE",
      300: "#E0E0E0",
      400: "#BDBDBD",
      500: "#9E9E9E",
      600: "#757575",
      700: "#616161",
      800: "#424242",
      900: "#212121",
    },

    // ===== DIVISORES =====
    divider: "rgba(0, 0, 0, 0.12)",
  },

  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',

    h1: {
      fontSize: "2.5rem",
      fontWeight: 700,
      lineHeight: 1.2,
      color: "#1A1A1A",
    },
    h2: {
      fontSize: "2rem",
      fontWeight: 600,
      lineHeight: 1.3,
      color: "#1A1A1A",
    },
    h3: {
      fontSize: "1.75rem",
      fontWeight: 600,
      lineHeight: 1.4,
      color: "#1A1A1A",
    },
    h4: {
      fontSize: "1.5rem",
      fontWeight: 600,
      lineHeight: 1.4,
      color: "#1A1A1A",
    },
    h5: {
      fontSize: "1.25rem",
      fontWeight: 500,
      lineHeight: 1.5,
      color: "#1A1A1A",
    },
    h6: {
      fontSize: "1rem",
      fontWeight: 500,
      lineHeight: 1.5,
      color: "#1A1A1A",
    },
    body1: {
      fontSize: "1rem",
      lineHeight: 1.5,
      color: "#1A1A1A",
    },
    body2: {
      fontSize: "0.875rem",
      lineHeight: 1.43,
      color: "#4A4A4A",
    },
    button: {
      textTransform: "none", // NO uppercase en botones
      fontWeight: 600,
      letterSpacing: "0.02em",
    },
  },

  shape: {
    borderRadius: 8, // Border radius default
  },

  // Sombras personalizadas con tinte naranja
  shadows: [
    "none",
    "0px 2px 4px rgba(255, 107, 53, 0.08)",
    "0px 4px 8px rgba(255, 107, 53, 0.1)",
    "0px 8px 16px rgba(255, 107, 53, 0.12)",
    "0px 12px 24px rgba(255, 107, 53, 0.14)",
    "0px 16px 32px rgba(255, 107, 53, 0.16)",
    "0px 20px 40px rgba(255, 107, 53, 0.18)",
    "0px 24px 48px rgba(255, 107, 53, 0.2)",
    "0px 28px 56px rgba(255, 107, 53, 0.22)",
    "0px 32px 64px rgba(255, 107, 53, 0.24)",
    "0px 36px 72px rgba(255, 107, 53, 0.26)",
    "0px 40px 80px rgba(255, 107, 53, 0.28)",
    "0px 44px 88px rgba(255, 107, 53, 0.3)",
    "0px 48px 96px rgba(255, 107, 53, 0.32)",
    "0px 52px 104px rgba(255, 107, 53, 0.34)",
    "0px 56px 112px rgba(255, 107, 53, 0.36)",
    "0px 60px 120px rgba(255, 107, 53, 0.38)",
    "0px 64px 128px rgba(255, 107, 53, 0.4)",
    "0px 68px 136px rgba(255, 107, 53, 0.42)",
    "0px 72px 144px rgba(255, 107, 53, 0.44)",
    "0px 76px 152px rgba(255, 107, 53, 0.46)",
    "0px 80px 160px rgba(255, 107, 53, 0.48)",
    "0px 84px 168px rgba(255, 107, 53, 0.5)",
    "0px 88px 176px rgba(255, 107, 53, 0.52)",
    "0px 92px 184px rgba(255, 107, 53, 0.54)",
  ],

  components: {
    // ===== BOTONES =====
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          padding: "10px 24px",
          fontSize: "1rem",
          fontWeight: 600,
          boxShadow: "none",
          transition: "all 0.2s ease-in-out",
        },
        containedPrimary: {
          backgroundColor: "#FF6B35",
          color: "#FFFFFF",
          "&:hover": {
            backgroundColor: "#E85A2A",
            boxShadow: "0px 4px 12px rgba(255, 107, 53, 0.3)",
          },
          "&:active": {
            backgroundColor: "#D14D20",
          },
        },
        containedSecondary: {
          backgroundColor: "#FF8A5B",
          color: "#FFFFFF",
          "&:hover": {
            backgroundColor: "#E66B3C",
            boxShadow: "0px 4px 12px rgba(255, 138, 91, 0.3)",
          },
        },
        outlined: {
          borderWidth: "2px",
          "&:hover": {
            borderWidth: "2px",
            backgroundColor: "rgba(255, 107, 53, 0.04)",
          },
        },
        text: {
          "&:hover": {
            backgroundColor: "rgba(255, 107, 53, 0.08)",
          },
        },
      },
    },

    // ===== CARDS =====
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          boxShadow: "0px 4px 12px rgba(0, 0, 0, 0.08)",
          transition: "box-shadow 0.2s ease-in-out",
          "&:hover": {
            boxShadow: "0px 8px 24px rgba(255, 107, 53, 0.12)",
          },
        },
      },
    },

    // ===== CHIPS =====
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          fontWeight: 500,
        },
        colorPrimary: {
          backgroundColor: "#FF6B35",
          color: "#FFFFFF",
        },
        colorSecondary: {
          backgroundColor: "#FF8A5B",
          color: "#FFFFFF",
        },
      },
    },

    // ===== APPBAR =====
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: "#FFFFFF",
          color: "#1A1A1A",
          boxShadow: "0px 2px 8px rgba(0, 0, 0, 0.08)",
        },
        colorPrimary: {
          backgroundColor: "#FF6B35",
          color: "#FFFFFF",
        },
      },
    },

    // ===== PAPER =====
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: "none", // Sin degradado
        },
        elevation1: {
          boxShadow: "0px 2px 4px rgba(0, 0, 0, 0.08)",
        },
        elevation2: {
          boxShadow: "0px 4px 8px rgba(0, 0, 0, 0.08)",
        },
        elevation3: {
          boxShadow: "0px 8px 16px rgba(0, 0, 0, 0.1)",
        },
      },
    },

    // ===== TABS =====
    MuiTab: {
      styleOverrides: {
        root: {
          textTransform: "none",
          fontWeight: 600,
          fontSize: "1rem",
          "&.Mui-selected": {
            color: "#FF6B35",
          },
        },
      },
    },

    // ===== INPUTS =====
    MuiTextField: {
      styleOverrides: {
        root: {
          "& .MuiOutlinedInput-root": {
            "&:hover fieldset": {
              borderColor: "#FF8A5B",
            },
            "&.Mui-focused fieldset": {
              borderColor: "#FF6B35",
              borderWidth: "2px",
            },
          },
        },
      },
    },

    // ===== CHECKBOX & RADIO =====
    MuiCheckbox: {
      styleOverrides: {
        root: {
          "&.Mui-checked": {
            color: "#FF6B35",
          },
        },
      },
    },
    MuiRadio: {
      styleOverrides: {
        root: {
          "&.Mui-checked": {
            color: "#FF6B35",
          },
        },
      },
    },

    // ===== SWITCH =====
    MuiSwitch: {
      styleOverrides: {
        switchBase: {
          "&.Mui-checked": {
            color: "#FF6B35",
            "+ .MuiSwitch-track": {
              backgroundColor: "#FF8A5B",
            },
          },
        },
      },
    },

    // ===== TABLE =====
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: "#FFF8F5",
          "& .MuiTableCell-head": {
            fontWeight: 700,
            color: "#1A1A1A",
            borderBottom: "2px solid #FF6B35",
          },
        },
      },
    },
  },
});
