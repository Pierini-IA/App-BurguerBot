"use client";

import { Fab, Tooltip } from "@mui/material";
import { WhatsApp } from "@mui/icons-material";

/**
 * Botón flotante de WhatsApp
 * Siempre visible en la esquina inferior derecha
 */
export const WhatsAppButton = () => {
  const handleClick = () => {
    window.open("https://wa.me/549349366512?text=Hola,%20quiero%20información%20sobre%20Dio%20Burger", "_blank");
  };

  return (
    <Tooltip title="Contáctanos por WhatsApp" placement="left">
      <Fab
        color="success"
        aria-label="whatsapp"
        onClick={handleClick}
        sx={{
          position: "fixed",
          bottom: { xs: 16, md: 24 },
          right: { xs: 16, md: 24 },
          width: { xs: 56, md: 64 },
          height: { xs: 56, md: 64 },
          boxShadow: (theme) => `0 4px 14px ${theme.palette.success.main}80`,
          zIndex: 1000,
          "&:hover": {
            boxShadow: (theme) => `0 6px 20px ${theme.palette.success.main}B3`,
            transform: "scale(1.1)",
          },
          transition: "all 0.3s ease",
          animation: "pulse 2s ease-in-out infinite",
          "@keyframes pulse": {
            "0%, 100%": {
              transform: "scale(1)",
            },
            "50%": {
              transform: "scale(1.05)",
            },
          },
        }}
      >
        <WhatsApp sx={{ fontSize: { xs: 28, md: 32 } }} />
      </Fab>
    </Tooltip>
  );
};
