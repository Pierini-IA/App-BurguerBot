/**
 * API Service para Mesas.
 *
 * Endpoints reales del backend: `/api/admin/mesas` (rol ADMIN/SUPERADMIN).
 * El `telefonoLocal` lo inyecta el interceptor de `axios.ts` en list y create.
 * El backend NO expone `GET /mesas/{id}`.
 */

import { apiClient } from "./axios";
import { Mesa, CreateMesaDTO, UpdateMesaDTO } from "@/types/api";

export const mesasApi = {
  /**
   * Lista todas las mesas del local.
   */
  getAll: async (): Promise<Mesa[]> => {
    const { data } = await apiClient.get<Mesa[]>("/admin/mesas");
    return data;
  },

  /**
   * Crea una nueva mesa.
   */
  create: async (mesa: CreateMesaDTO): Promise<Mesa> => {
    const { data } = await apiClient.post<Mesa>("/admin/mesas", mesa);
    return data;
  },

  /**
   * Actualiza una mesa existente.
   */
  update: async (id: number, mesa: UpdateMesaDTO): Promise<Mesa> => {
    const { data } = await apiClient.put<Mesa>(`/admin/mesas/${id}`, mesa);
    return data;
  },

  /**
   * Elimina una mesa.
   */
  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/admin/mesas/${id}`);
  },
};
