"use client";

import { ProtectedRoute } from "@/components/shared/ProtectedRoute";
import { LocalProvider } from "@/lib/context/LocalContext";
import { CocinaBoard } from "@/components/cocina/CocinaBoard";
import { Rol } from "@/types/usuario";

/**
 * Panel de Cocina.
 *
 * Pantalla operativa a pantalla completa: riel de chits de pedidos que se
 * actualiza en tiempo real (WebSocket) y un bump bar por chit para avanzar
 * el estado (Empezar → Marcar listo → Entregar).
 */
export default function CocinaPage() {
  return (
    <ProtectedRoute allowedRoles={[Rol.COCINA, Rol.ADMIN, Rol.SUPERADMIN]}>
      <LocalProvider>
        <CocinaBoard />
      </LocalProvider>
    </ProtectedRoute>
  );
}
