"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { Box, CircularProgress } from "@mui/material";

/**
 * `/admin` no tiene contenido propio: el dashboard real vive en `/admin/dashboard`
 * (así coincide con el ítem del sidebar). Esto solo evita un 404 si alguien
 * entra a la raíz del panel.
 */
export default function AdminIndexPage() {
  const router = useRouter();

  useEffect(() => {
    router.replace("/admin/dashboard");
  }, [router]);

  return (
    <Box sx={{ display: "flex", justifyContent: "center", py: 8 }}>
      <CircularProgress size={32} />
    </Box>
  );
}
