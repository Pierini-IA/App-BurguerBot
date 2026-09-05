/**
 * API Service para Extras.
 *
 * Endpoints reales del backend: `/api/admin/extras` (rol ADMIN/SUPERADMIN).
 * El `telefonoLocal` lo inyecta el interceptor de `axios.ts`.
 * El backend devuelve `ExtraDTO`. No hay endpoint de toggle: se usa `update`.
 */

import { apiClient } from "./axios";
import { Extra, ExtraFormData } from "@/types/producto";

export const extrasApi = {
  /**
   * Lista los extras del local. `soloActivos` filtra los inactivos;
   * `categoriaId` acota a una categoría.
   */
  getAll: async (params?: { soloActivos?: boolean; categoriaId?: number }): Promise<Extra[]> => {
    const { data } = await apiClient.get<Extra[]>("/admin/extras", { params });
    return data;
  },

  /**
   * Atajo: solo extras activos.
   */
  getActivos: async (): Promise<Extra[]> => {
    return extrasApi.getAll({ soloActivos: true });
  },

  /**
   * Extras de una categoría.
   */
  getByCategoria: async (categoriaId: number): Promise<Extra[]> => {
    return extrasApi.getAll({ categoriaId });
  },

  /**
   * Obtiene un extra por ID.
   */
  getById: async (id: number): Promise<Extra> => {
    const { data } = await apiClient.get<Extra>(`/admin/extras/${id}`);
    return data;
  },

  /**
   * Crea un extra.
   */
  create: async (extra: ExtraFormData): Promise<Extra> => {
    const { data } = await apiClient.post<Extra>("/admin/extras", extra);
    return data;
  },

  /**
   * Actualiza un extra (incluye activar/desactivar vía el campo `activo`).
   */
  update: async (id: number, extra: Partial<ExtraFormData>): Promise<Extra> => {
    const { data } = await apiClient.put<Extra>(`/admin/extras/${id}`, extra);
    return data;
  },

  /**
   * Elimina un extra.
   */
  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/admin/extras/${id}`);
  },
};
