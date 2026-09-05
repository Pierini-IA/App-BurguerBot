"use client";

import { ProtectedRoute } from "@/components/shared/ProtectedRoute";
import { LocalProvider } from "@/lib/context/LocalContext";
import { MostradorBoard } from "@/components/mostrador/MostradorBoard";
import { Rol } from "@/types/usuario";

/**
 * Vista de Mostrador.
 *
 * Pantalla operativa para el mostrador: lista de pedidos en curso con impresión
 * de comanda (formato ~80 mm) y avance de estado. No hay rol MOSTRADOR en el
 * backend, así que la usan COCINA y ADMIN.
 */
export default function MostradorPage() {
  return (
    <ProtectedRoute allowedRoles={[Rol.COCINA, Rol.ADMIN, Rol.SUPERADMIN]}>
      <LocalProvider>
        <MostradorBoard />
      </LocalProvider>
    </ProtectedRoute>
  );
}
