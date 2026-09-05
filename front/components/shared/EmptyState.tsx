import { Box, Paper, Typography } from "@mui/material";
import { Inbox as InboxIcon } from "@mui/icons-material";
import { ReactNode } from "react";

interface EmptyStateProps {
  title: string;
  description?: string;
  icon?: ReactNode;
  action?: ReactNode;
}

/**
 * Estado vacío reutilizable para listas y tablas sin datos.
 */
export const EmptyState: React.FC<EmptyStateProps> = ({ title, description, icon, action }) => (
  <Paper
    elevation={0}
    sx={{
      p: 6,
      borderRadius: 3,
      border: "1px dashed",
      borderColor: "divider",
      textAlign: "center",
    }}
  >
    <Box sx={{ color: "primary.main", mb: 2, "& svg": { fontSize: 64 } }}>{icon ?? <InboxIcon />}</Box>
    <Typography variant="h6" gutterBottom>
      {title}
    </Typography>
    {description && (
      <Typography variant="body2" color="text.secondary" sx={{ mb: action ? 3 : 0 }}>
        {description}
      </Typography>
    )}
    {action}
  </Paper>
);
