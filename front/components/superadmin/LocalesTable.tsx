"use client";

import { useState } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Chip,
  Tooltip,
  Box,
  Typography,
} from "@mui/material";
import { Edit, Delete, Settings, Star, Verified, CheckCircle } from "@mui/icons-material";
import { Local, PlanSuscripcion } from "@/types/local";
import { format } from "date-fns";
import { es } from "date-fns/locale";

interface LocalesTableProps {
  locales: Local[];
  onEdit: (local: Local) => void;
  onDelete: (local: Local) => void;
  onConfig: (local: Local) => void;
}

/**
 * Tabla de Locales
 * Muestra todos los locales con acciones y plan de suscripción
 */
export const LocalesTable: React.FC<LocalesTableProps> = ({ locales, onEdit, onDelete, onConfig }) => {
  /**
   * Obtener el color del chip según el plan
   */
  const getPlanColor = (plan?: PlanSuscripcion): "default" | "primary" | "secondary" | "success" => {
    switch (plan) {
      case PlanSuscripcion.PREMIUM:
        return "success";
      case PlanSuscripcion.ESTANDAR:
        return "primary";
      case PlanSuscripcion.BASICO:
        return "secondary";
      default:
        return "default";
    }
  };

  /**
   * Obtener el icono según el plan
   */
  const getPlanIcon = (plan?: PlanSuscripcion) => {
    switch (plan) {
      case PlanSuscripcion.PREMIUM:
        return <Star fontSize="small" />;
      case PlanSuscripcion.ESTANDAR:
        return <Verified fontSize="small" />;
      case PlanSuscripcion.BASICO:
        return <CheckCircle fontSize="small" />;
      default:
        return null;
    }
  };

  /**
   * Formatear fecha de vencimiento
   */
  const formatearFechaVencimiento = (fecha?: string | null): string => {
    if (!fecha) return "Indefinido";
    try {
      return format(new Date(fecha), "dd/MM/yyyy", { locale: es });
    } catch {
      return "N/A";
    }
  };

  if (locales.length === 0) {
    return (
      <Paper
        elevation={0}
        sx={{
          p: 4,
          textAlign: "center",
          border: "1px solid",
          borderColor: "divider",
          borderRadius: 2,
        }}
      >
        <Typography variant="body1" color="text.secondary">
          No hay locales registrados
        </Typography>
      </Paper>
    );
  }

  return (
    <TableContainer
      component={Paper}
      elevation={0}
      sx={{
        border: "1px solid",
        borderColor: "divider",
        borderRadius: 2,
      }}
    >
      <Table>
        <TableHead>
          <TableRow sx={{ backgroundColor: "grey.50" }}>
            <TableCell sx={{ fontWeight: 600 }}>Nombre</TableCell>
            <TableCell sx={{ fontWeight: 600 }}>Dirección</TableCell>
            <TableCell sx={{ fontWeight: 600 }}>Teléfono</TableCell>
            <TableCell sx={{ fontWeight: 600 }} align="center">
              Plan
            </TableCell>
            <TableCell sx={{ fontWeight: 600 }} align="center">
              Estado
            </TableCell>
            <TableCell sx={{ fontWeight: 600 }} align="center">
              Vencimiento
            </TableCell>
            <TableCell sx={{ fontWeight: 600 }} align="center">
              Acciones
            </TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {locales.map((local) => (
            <TableRow
              key={local.id}
              hover
              sx={{
                "&:last-child td, &:last-child th": { border: 0 },
              }}
            >
              <TableCell>
                <Typography variant="body2" sx={{ fontWeight: 600 }}>
                  {local.nombre}
                </Typography>
              </TableCell>
              <TableCell>
                <Typography variant="body2" color="text.secondary">
                  {local.direccion}
                </Typography>
              </TableCell>
              <TableCell>
                <Typography variant="body2" color="text.secondary">
                  {local.telefono}
                </Typography>
              </TableCell>
              <TableCell align="center">
                {local.planSuscripcion ? (
                  (() => {
                    const icon = getPlanIcon(local.planSuscripcion);
                    return (
                      <Chip
                        {...(icon && { icon })}
                        label={local.planSuscripcion}
                        color={getPlanColor(local.planSuscripcion)}
                        size="small"
                        sx={{ fontWeight: 600 }}
                      />
                    );
                  })()
                ) : (
                  <Typography variant="body2" color="text.disabled">
                    N/A
                  </Typography>
                )}
              </TableCell>
              <TableCell align="center">
                {local.planActivo !== undefined ? (
                  <Chip
                    label={local.planActivo ? "Activo" : "Inactivo"}
                    color={local.planActivo ? "success" : "error"}
                    size="small"
                    variant="outlined"
                  />
                ) : (
                  <Typography variant="body2" color="text.disabled">
                    N/A
                  </Typography>
                )}
              </TableCell>
              <TableCell align="center">
                <Typography variant="body2" color="text.secondary">
                  {formatearFechaVencimiento(local.fechaFinPlan)}
                </Typography>
              </TableCell>
              <TableCell align="center">
                <Box sx={{ display: "flex", gap: 0.5, justifyContent: "center" }}>
                  <Tooltip title="Configuración">
                    <IconButton size="small" onClick={() => onConfig(local)} color="info">
                      <Settings />
                    </IconButton>
                  </Tooltip>

                  <Tooltip title="Editar">
                    <IconButton size="small" onClick={() => onEdit(local)} color="primary">
                      <Edit />
                    </IconButton>
                  </Tooltip>

                  <Tooltip title="Eliminar">
                    <IconButton size="small" onClick={() => onDelete(local)} color="error">
                      <Delete />
                    </IconButton>
                  </Tooltip>
                </Box>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};
