"use client";

import React from "react";
import { ThemeProvider } from "@mui/material/styles";
import CssBaseline from "@mui/material/CssBaseline";
import { theme } from "@/lib/theme/theme";

interface ThemeRegistryProps {
  children: React.ReactNode;
}

/**
 * Componente wrapper para configurar Material UI con Next.js App Router
 * Incluye ThemeProvider y CssBaseline
 */
export function ThemeRegistry({ children }: ThemeRegistryProps) {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {children}
    </ThemeProvider>
  );
}
