/**
 * API Service para la configuración del local del usuario autenticado.
 * `GET`/`PUT /api/local/mi-local/configuracion` — solo ADMIN puede editar.
 */

import { apiClient } from "./axios";
import { MiConfiguracion } from "@/types/local";

export const configuracionApi = {
  /**
   * Configuración actual del local. Los tokens de Meta llegan como flags,
   * nunca como texto.
   */
  get: async (): Promise<MiConfiguracion> => {
    const { data } = await apiClient.get<MiConfiguracion>("/local/mi-local/configuracion");
    return data;
  },

  /**
   * Actualización parcial: los campos `null`/`undefined` no se tocan,
   * y un token vacío deja el existente sin cambios.
   */
  update: async (cambios: Partial<MiConfiguracion>): Promise<MiConfiguracion> => {
    const { data } = await apiClient.put<MiConfiguracion>("/local/mi-local/configuracion", cambios);
    return data;
  },
};
