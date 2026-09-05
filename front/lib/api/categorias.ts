/**
 * API Service para Categorías.
 *
 * Endpoints reales del backend: `/api/admin/categorias` (rol ADMIN/SUPERADMIN).
 * El `telefonoLocal` lo inyecta el interceptor de `axios.ts`.
 * El backend devuelve `CategoriaDTO` (incluye `localId`, `cantidadProductos`, etc.).
 */

import { apiClient } from "./axios";
import { Categoria, CategoriaFormData } from "@/types/producto";

export const categoriasApi = {
  /**
   * Lista las categorías del local. Con `soloActivas` filtra las inactivas.
   */
  getAll: async (soloActivas = false): Promise<Categoria[]> => {
    const { data } = await apiClient.get<Categoria[]>("/admin/categorias", {
      params: { soloActivas },
    });
    return data;
  },

  /**
   * Atajo: solo categorías activas.
   */
  getActivas: async (): Promise<Categoria[]> => {
    return categoriasApi.getAll(true);
  },

  /**
   * Obtiene una categoría por ID.
   */
  getById: async (id: number): Promise<Categoria> => {
    const { data } = await apiClient.get<Categoria>(`/admin/categorias/${id}`);
    return data;
  },

  /**
   * Crea una categoría.
   */
  create: async (categoria: CategoriaFormData): Promise<Categoria> => {
    const { data } = await apiClient.post<Categoria>("/admin/categorias", categoria);
    return data;
  },

  /**
   * Actualiza una categoría.
   */
  update: async (id: number, categoria: Partial<CategoriaFormData>): Promise<Categoria> => {
    const { data } = await apiClient.put<Categoria>(`/admin/categorias/${id}`, categoria);
    return data;
  },

  /**
   * Elimina una categoría.
   */
  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/admin/categorias/${id}`);
  },
};
