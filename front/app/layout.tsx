import type { Metadata } from "next";
import { Inter, Barlow_Condensed } from "next/font/google";
import { AppRouterCacheProvider } from "@mui/material-nextjs/v16-appRouter";
import "./globals.css";
import { ThemeRegistry } from "@/components/ThemeRegistry";

const inter = Inter({
  subsets: ["latin"],
  display: "swap",
});

// Tipografía condensada tipo cartelería de cocina — usada en el panel de cocina.
const barlowCondensed = Barlow_Condensed({
  subsets: ["latin"],
  weight: ["500", "600", "700"],
  variable: "--font-barlow-condensed",
  display: "swap",
});

export const metadata: Metadata = {
  title: "Dio Burger - Gestión Inteligente para tu Hamburguesería",
  description: "Sistema completo de pedidos, inventario y reservas para hamburgueserías",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="es">
      <body className={`${inter.className} ${barlowCondensed.variable}`}>
        <AppRouterCacheProvider options={{ key: "mui" }}>
          <ThemeRegistry>{children}</ThemeRegistry>
        </AppRouterCacheProvider>
      </body>
    </html>
  );
}
