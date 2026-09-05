/**
 * API Service para Productos.
 *
 * Endpoints reales del backend: `/api/admin/productos` (rol ADMIN/SUPERADMIN).
 * El `telefonoLocal` lo inyecta automáticamente el interceptor de `axios.ts`
 * desde la sesión, así que no hace falta pasarlo en cada llamada.
 */

import { apiClient } from "./axios";
import { Producto, ProductoFormData } from "@/types/producto";
import { Extra } from "@/types/producto";

export const productosApi = {
  /**
   * Lista los productos del local del usuario autenticado.
   */
  getAll: async (): Promise<Producto[]> => {
    const { data } = await apiClient.get<Producto[]>("/admin/productos");
    return data;
  },

  /**
   * Lista los productos de un local específico (uso SUPERADMIN).
   */
  getByLocal: async (telefonoLocal: string): Promise<Producto[]> => {
    const { data } = await apiClient.get<Producto[]>("/admin/productos", {
      params: { telefonoLocal },
    });
    return data;
  },

  /**
   * Crea un producto. El body admite `recetas` embebidas (se guardan en cascada).
   */
  create: async (producto: ProductoFormData): Promise<Producto> => {
    const { data } = await apiClient.post<Producto>("/admin/productos", producto);
    return data;
  },

  /**
   * Actualiza nombre, descripción y precio de un producto.
   */
  update: async (id: number, producto: Partial<ProductoFormData>): Promise<Producto> => {
    const { data } = await apiClient.put<Producto>(`/admin/productos/${id}`, producto);
    return data;
  },

  /**
   * Elimina un producto.
   */
  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/admin/productos/${id}`);
  },

  /**
   * Alterna la disponibilidad (agotado / disponible) de un producto.
   */
  toggleDisponibilidad: async (id: number): Promise<Producto> => {
    const { data } = await apiClient.patch<Producto>(`/admin/productos/${id}/disponibilidad`);
    return data;
  },

  /**
   * Lista los extras disponibles para un producto.
   */
  getExtras: async (id: number): Promise<Extra[]> => {
    const { data } = await apiClient.get<Extra[]>(`/admin/productos/${id}/extras`);
    return data;
  },
};
