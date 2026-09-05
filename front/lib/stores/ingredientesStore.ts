/**
 * Zustand Store para Ingredientes (control de stock).
 */

import { create } from "zustand";
import { Ingrediente, IngredienteFormData } from "@/types/producto";
import { ingredientesApi } from "@/lib/api/ingredientes";
import { getErrorMessage } from "@/lib/api/axios";

interface IngredientesState {
  ingredientes: Ingrediente[];
  isLoading: boolean;
  error: string | null;

  fetchIngredientes: () => Promise<void>;
  createIngrediente: (data: IngredienteFormData) => Promise<Ingrediente>;
  updateIngrediente: (id: number, data: Partial<IngredienteFormData>) => Promise<Ingrediente>;
  ajustarStock: (id: number, nuevoStock: number) => Promise<Ingrediente>;
  deleteIngrediente: (id: number) => Promise<void>;
  clearError: () => void;
}

export const useIngredientesStore = create<IngredientesState>((set) => ({
  ingredientes: [],
  isLoading: false,
  error: null,

  fetchIngredientes: async () => {
    set({ isLoading: true, error: null });
    try {
      const ingredientes = await ingredientesApi.getAll();
      set({ ingredientes, isLoading: false });
    } catch (error) {
      set({ error: getErrorMessage(error) || "No se pudieron cargar los ingredientes", isLoading: false });
    }
  },

  createIngrediente: async (data) => {
    const ing = await ingredientesApi.create(data);
    set((s) => ({ ingredientes: [...s.ingredientes, ing] }));
    return ing;
  },

  updateIngrediente: async (id, data) => {
    const ing = await ingredientesApi.update(id, data);
    set((s) => ({ ingredientes: s.ingredientes.map((i) => (i.id === id ? ing : i)) }));
    return ing;
  },

  ajustarStock: async (id, nuevoStock) => {
    const ing = await ingredientesApi.updateStock(id, nuevoStock);
    set((s) => ({ ingredientes: s.ingredientes.map((i) => (i.id === id ? ing : i)) }));
    return ing;
  },

  deleteIngrediente: async (id) => {
    await ingredientesApi.delete(id);
    set((s) => ({ ingredientes: s.ingredientes.filter((i) => i.id !== id) }));
  },

  clearError: () => set({ error: null }),
}));
