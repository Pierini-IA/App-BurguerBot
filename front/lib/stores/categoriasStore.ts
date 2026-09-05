/**
 * Zustand Store para Categorías del menú.
 */

import { create } from "zustand";
import { Categoria, CategoriaFormData } from "@/types/producto";
import { categoriasApi } from "@/lib/api/categorias";
import { getErrorMessage } from "@/lib/api/axios";

interface CategoriasState {
  categorias: Categoria[];
  isLoading: boolean;
  error: string | null;

  fetchCategorias: () => Promise<void>;
  createCategoria: (data: CategoriaFormData) => Promise<Categoria>;
  updateCategoria: (id: number, data: Partial<CategoriaFormData>) => Promise<Categoria>;
  deleteCategoria: (id: number) => Promise<void>;
  clearError: () => void;
}

export const useCategoriasStore = create<CategoriasState>((set) => ({
  categorias: [],
  isLoading: false,
  error: null,

  fetchCategorias: async () => {
    set({ isLoading: true, error: null });
    try {
      const categorias = await categoriasApi.getAll();
      set({ categorias, isLoading: false });
    } catch (error) {
      set({
        error: getErrorMessage(error) || "No se pudieron cargar las categorías",
        isLoading: false,
      });
    }
  },

  createCategoria: async (data) => {
    const categoria = await categoriasApi.create(data);
    set((state) => ({ categorias: [...state.categorias, categoria] }));
    return categoria;
  },

  updateCategoria: async (id, data) => {
    const categoria = await categoriasApi.update(id, data);
    set((state) => ({
      categorias: state.categorias.map((c) => (c.id === id ? categoria : c)),
    }));
    return categoria;
  },

  deleteCategoria: async (id) => {
    await categoriasApi.delete(id);
    set((state) => ({ categorias: state.categorias.filter((c) => c.id !== id) }));
  },

  clearError: () => set({ error: null }),
}));
