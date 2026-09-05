/**
 * Zustand Store para Extras
 */

import { create } from "zustand";
import { Extra, ExtraFormData } from "@/types/producto";
import { extrasApi } from "@/lib/api/extras";

interface ExtrasState {
  // State
  extras: Extra[];
  extraActual: Extra | null;
  isLoading: boolean;
  error: string | null;

  // Actions
  fetchExtras: () => Promise<void>;
  fetchExtrasActivos: () => Promise<void>;
  fetchExtrasByCategoria: (categoriaId: number) => Promise<void>;
  fetchExtraById: (id: number) => Promise<void>;
  createExtra: (data: ExtraFormData) => Promise<Extra>;
  updateExtra: (id: number, data: Partial<ExtraFormData>) => Promise<Extra>;
  deleteExtra: (id: number) => Promise<void>;
  toggleActivo: (id: number) => Promise<void>;
  clearError: () => void;
  clearExtraActual: () => void;
}

export const useExtrasStore = create<ExtrasState>((set, get) => ({
  // Initial state
  extras: [],
  extraActual: null,
  isLoading: false,
  error: null,

  // Actions
  fetchExtras: async () => {
    set({ isLoading: true, error: null });
    try {
      const extras = await extrasApi.getAll();
      set({ extras, isLoading: false });
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al cargar extras",
        isLoading: false,
      });
    }
  },

  fetchExtrasActivos: async () => {
    set({ isLoading: true, error: null });
    try {
      const extras = await extrasApi.getActivos();
      set({ extras, isLoading: false });
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al cargar extras",
        isLoading: false,
      });
    }
  },

  fetchExtrasByCategoria: async (categoriaId: number) => {
    set({ isLoading: true, error: null });
    try {
      const extras = await extrasApi.getByCategoria(categoriaId);
      set({ extras, isLoading: false });
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al cargar extras",
        isLoading: false,
      });
    }
  },

  fetchExtraById: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      const extra = await extrasApi.getById(id);
      set({ extraActual: extra, isLoading: false });
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al cargar extra",
        isLoading: false,
      });
    }
  },

  createExtra: async (data: ExtraFormData) => {
    set({ isLoading: true, error: null });
    try {
      const extra = await extrasApi.create(data);
      set((state) => ({
        extras: [...state.extras, extra],
        isLoading: false,
      }));
      return extra;
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al crear extra",
        isLoading: false,
      });
      throw error;
    }
  },

  updateExtra: async (id: number, data: Partial<ExtraFormData>) => {
    set({ isLoading: true, error: null });
    try {
      const extra = await extrasApi.update(id, data);
      set((state) => ({
        extras: state.extras.map((e) => (e.id === id ? extra : e)),
        extraActual: state.extraActual?.id === id ? extra : state.extraActual,
        isLoading: false,
      }));
      return extra;
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al actualizar extra",
        isLoading: false,
      });
      throw error;
    }
  },

  deleteExtra: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      await extrasApi.delete(id);
      set((state) => ({
        extras: state.extras.filter((e) => e.id !== id),
        isLoading: false,
      }));
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al eliminar extra",
        isLoading: false,
      });
      throw error;
    }
  },

  /**
   * Activa/desactiva un extra. El backend no tiene endpoint de toggle:
   * se hace con un update del campo `activo`.
   */
  toggleActivo: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      const actual = get().extras.find((e) => e.id === id);
      const extra = await extrasApi.update(id, { activo: !actual?.activo });
      set((state) => ({
        extras: state.extras.map((e) => (e.id === id ? extra : e)),
        isLoading: false,
      }));
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al cambiar estado",
        isLoading: false,
      });
      throw error;
    }
  },

  clearError: () => {
    set({ error: null });
  },

  clearExtraActual: () => {
    set({ extraActual: null });
  },
}));
