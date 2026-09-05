/**
 * API Service para Ingredientes.
 *
 * Endpoints reales del backend: `/api/admin/ingredientes` (rol ADMIN/SUPERADMIN).
 * El `telefonoLocal` lo inyecta el interceptor de `axios.ts`.
 *
 * El backend NO expone `GET /{id}` ni filtro server-side de stock bajo:
 * para "stock bajo" se compara en el cliente contra `stockMinimo`.
 */

import { apiClient } from "./axios";
import { Ingrediente, IngredienteFormData } from "@/types/producto";

export const ingredientesApi = {
  /**
   * Lista los ingredientes del local del usuario autenticado.
   */
  getAll: async (): Promise<Ingrediente[]> => {
    const { data } = await apiClient.get<Ingrediente[]>("/admin/ingredientes");
    return data;
  },

  /**
   * Lista los ingredientes de un local específico (uso SUPERADMIN).
   */
  getByLocal: async (telefonoLocal: string): Promise<Ingrediente[]> => {
    const { data } = await apiClient.get<Ingrediente[]>("/admin/ingredientes", {
      params: { telefonoLocal },
    });
    return data;
  },

  /**
   * Crea un ingrediente.
   */
  create: async (ingrediente: IngredienteFormData): Promise<Ingrediente> => {
    const { data } = await apiClient.post<Ingrediente>("/admin/ingredientes", ingrediente);
    return data;
  },

  /**
   * Actualiza nombre / stock / unidad de medida de un ingrediente.
   */
  update: async (id: number, ingrediente: Partial<IngredienteFormData>): Promise<Ingrediente> => {
    const { data } = await apiClient.put<Ingrediente>(`/admin/ingredientes/${id}`, ingrediente);
    return data;
  },

  /**
   * Ajusta solo el stock de un ingrediente.
   * El backend espera `nuevoStock` como query param.
   */
  updateStock: async (id: number, nuevoStock: number): Promise<Ingrediente> => {
    const { data } = await apiClient.put<Ingrediente>(
      `/admin/ingredientes/${id}/stock`,
      null,
      { params: { nuevoStock } }
    );
    return data;
  },

  /**
   * Elimina un ingrediente (falla si está en la receta de algún producto).
   */
  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/admin/ingredientes/${id}`);
  },
};
