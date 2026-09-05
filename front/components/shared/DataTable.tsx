"use client";

import { ReactNode } from "react";
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Paper,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@mui/material";
import { EmptyState } from "./EmptyState";

export interface Column<T> {
  /** Clave única de la columna. */
  key: string;
  /** Encabezado visible. */
  header: ReactNode;
  /** Contenido de la celda para una fila. */
  render: (row: T) => ReactNode;
  align?: "left" | "right" | "center";
  /** Ancho fijo opcional (ej. "1%" para columnas de acciones). */
  width?: string | number;
}

interface DataTableProps<T> {
  columns: Column<T>[];
  rows: T[];
  getRowId: (row: T) => string | number;
  loading?: boolean;
  error?: string | null;
  onRetry?: () => void;
  /** Mensaje cuando no hay filas (y no está cargando). */
  emptyTitle?: string;
  emptyDescription?: string;
  emptyAction?: ReactNode;
  onRowClick?: (row: T) => void;
}

/**
 * Tabla genérica con estados de carga / error / vacío ya resueltos.
 *
 * @example
 * <DataTable
 *   columns={[
 *     { key: "nombre", header: "Nombre", render: (p) => p.nombre },
 *     { key: "precio", header: "Precio", align: "right", render: (p) => `$${p.precio}` },
 *   ]}
 *   rows={productos}
 *   getRowId={(p) => p.id}
 *   loading={isLoading}
 *   error={error}
 *   onRetry={fetchProductos}
 *   emptyTitle="Todavía no cargaste productos"
 * />
 */
export function DataTable<T>({
  columns,
  rows,
  getRowId,
  loading = false,
  error = null,
  onRetry,
  emptyTitle = "No hay datos para mostrar",
  emptyDescription,
  emptyAction,
  onRowClick,
}: DataTableProps<T>) {
  if (error) {
    return (
      <Box sx={{ display: "flex", flexDirection: "column", gap: 2, alignItems: "flex-start" }}>
        <Alert severity="error" sx={{ width: "100%" }}>
          {error}
        </Alert>
        {onRetry && (
          <Button variant="outlined" onClick={onRetry}>
            Reintentar
          </Button>
        )}
      </Box>
    );
  }

  if (!loading && rows.length === 0) {
    return <EmptyState title={emptyTitle} description={emptyDescription} action={emptyAction} />;
  }

  return (
    <TableContainer component={Paper} elevation={0} sx={{ border: "1px solid", borderColor: "divider", borderRadius: 3 }}>
      <Table>
        <TableHead>
          <TableRow>
            {columns.map((col) => (
              <TableCell key={col.key} align={col.align} sx={{ fontWeight: 700, width: col.width }}>
                {col.header}
              </TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {loading
            ? Array.from({ length: 4 }).map((_, i) => (
                <TableRow key={`sk-${i}`}>
                  {columns.map((col) => (
                    <TableCell key={col.key} align={col.align}>
                      <Skeleton variant="text" />
                    </TableCell>
                  ))}
                </TableRow>
              ))
            : rows.map((row) => (
                <TableRow
                  key={getRowId(row)}
                  hover={!!onRowClick}
                  onClick={onRowClick ? () => onRowClick(row) : undefined}
                  sx={{ cursor: onRowClick ? "pointer" : "default" }}
                >
                  {columns.map((col) => (
                    <TableCell key={col.key} align={col.align}>
                      {col.render(row)}
                    </TableCell>
                  ))}
                </TableRow>
              ))}
        </TableBody>
      </Table>
      {loading && rows.length > 0 && (
        <Box sx={{ display: "flex", justifyContent: "center", py: 2 }}>
          <CircularProgress size={24} />
        </Box>
      )}
    </TableContainer>
  );
}
