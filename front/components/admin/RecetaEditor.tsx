"use client";

import { Box, Button, IconButton, MenuItem, TextField, Typography } from "@mui/material";
import { Add as AddIcon, Close as CloseIcon } from "@mui/icons-material";
import { UNIDAD_MEDIDA_LABEL, type Ingrediente } from "@/types/producto";

export interface RecetaRow {
  ingredienteId: number | "";
  cantidad: string;
}

interface RecetaEditorProps {
  value: RecetaRow[];
  onChange: (rows: RecetaRow[]) => void;
  ingredientes: Ingrediente[];
}

/**
 * Editor de receta: qué ingredientes consume un producto y en qué cantidad.
 * Ese consumo es lo que descuenta stock cuando se confirma un pedido.
 */
export function RecetaEditor({ value, onChange, ingredientes }: RecetaEditorProps) {
  const usados = new Set(value.map((r) => r.ingredienteId).filter((x): x is number => x !== ""));

  const setRow = (i: number, patch: Partial<RecetaRow>) => {
    onChange(value.map((r, idx) => (idx === i ? { ...r, ...patch } : r)));
  };
  const addRow = () => onChange([...value, { ingredienteId: "", cantidad: "1" }]);
  const removeRow = (i: number) => onChange(value.filter((_, idx) => idx !== i));

  return (
    <Box>
      <Typography variant="subtitle2" sx={{ mb: 1 }}>
        Receta
      </Typography>

      {value.length === 0 && (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
          Sin ingredientes. El producto no descuenta stock.
        </Typography>
      )}

      <Box sx={{ display: "flex", flexDirection: "column", gap: 1 }}>
        {value.map((row, i) => {
          const ing = ingredientes.find((x) => x.id === row.ingredienteId);
          return (
            <Box key={i} sx={{ display: "flex", gap: 1, alignItems: "flex-start" }}>
              <TextField
                select
                label="Ingrediente"
                value={row.ingredienteId === "" ? "" : String(row.ingredienteId)}
                onChange={(e) => setRow(i, { ingredienteId: e.target.value ? Number(e.target.value) : "" })}
                size="small"
                sx={{ flex: 1 }}
              >
                {ingredientes.map((x) => (
                  <MenuItem
                    key={x.id}
                    value={String(x.id)}
                    disabled={x.id !== row.ingredienteId && usados.has(x.id)}
                  >
                    {x.nombre}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                label="Cantidad"
                type="number"
                value={row.cantidad}
                onChange={(e) => setRow(i, { cantidad: e.target.value })}
                size="small"
                sx={{ width: 120 }}
                helperText={ing ? UNIDAD_MEDIDA_LABEL[ing.unidadMedida] : " "}
              />
              <IconButton aria-label="Quitar ingrediente" onClick={() => removeRow(i)} sx={{ mt: 0.5 }}>
                <CloseIcon fontSize="small" />
              </IconButton>
            </Box>
          );
        })}
      </Box>

      <Button
        size="small"
        startIcon={<AddIcon />}
        onClick={addRow}
        disabled={usados.size >= ingredientes.length}
        sx={{ mt: 1 }}
      >
        Agregar ingrediente
      </Button>
    </Box>
  );
}
