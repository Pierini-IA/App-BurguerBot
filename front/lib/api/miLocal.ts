/**
 * API Service para "mi local".
 *
 * `GET /api/local/mi-local` — devuelve el local del usuario autenticado
 * (ADMIN o COCINA), su plan y las funcionalidades habilitadas. Es la fuente
 * de verdad para el `LocalContext` y el feature-gating de la UI.
 */

import { apiClient } from "./axios";
import { MiLocal } from "@/types/local";

export const miLocalApi = {
  /**
   * Obtiene los datos del local del usuario autenticado.
   */
  get: async (): Promise<MiLocal> => {
    const { data } = await apiClient.get<MiLocal>("/local/mi-local");
    return data;
  },
};
