import { create } from "zustand";
import { Local, LocalFormData, ConfiguracionLocal, ConfiguracionLocalFormData } from "@/types/local";
import { localesApi } from "@/lib/api/locales";

interface LocalesState {
  // State
  locales: Local[];
  selectedLocal: Local | null;
  configuracion: ConfiguracionLocal | null;
  isLoading: boolean;
  error: string | null;

  // Actions
  fetchLocales: () => Promise<void>;
  fetchLocalById: (id: number) => Promise<void>;
  createLocal: (data: LocalFormData) => Promise<Local>;
  updateLocal: (id: number, data: Partial<LocalFormData>) => Promise<Local>;
  deleteLocal: (id: number) => Promise<void>;
  fetchConfig: (localId: number) => Promise<void>;
  updateConfig: (localId: number, data: Partial<ConfiguracionLocalFormData>) => Promise<ConfiguracionLocal>;
  clearError: () => void;
  clearSelected: () => void;
}

/**
 * Store de Locales con Zustand
 */
export const useLocalesStore = create<LocalesState>((set, get) => ({
  // Initial state
  locales: [],
  selectedLocal: null,
  configuracion: null,
  isLoading: false,
  error: null,

  // Obtener todos los locales
  fetchLocales: async () => {
    set({ isLoading: true, error: null });
    try {
      const locales = await localesApi.getAll();
      set({ locales, isLoading: false });
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al cargar locales",
        isLoading: false,
      });
    }
  },

  // Obtener un local por ID
  fetchLocalById: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      const local = await localesApi.getById(id);
      set({ selectedLocal: local, isLoading: false });
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al cargar local",
        isLoading: false,
      });
    }
  },

  // Crear local
  createLocal: async (data: LocalFormData) => {
    set({ isLoading: true, error: null });
    try {
      const newLocal = await localesApi.create(data);
      set((state) => ({
        locales: [...state.locales, newLocal],
        isLoading: false,
      }));
      return newLocal;
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || "Error al crear local";
      set({ error: errorMessage, isLoading: false });
      throw new Error(errorMessage);
    }
  },

  // Actualizar local
  updateLocal: async (id: number, data: Partial<LocalFormData>) => {
    set({ isLoading: true, error: null });
    try {
      const updatedLocal = await localesApi.update(id, data);
      set((state) => ({
        locales: state.locales.map((l) => (l.id === id ? updatedLocal : l)),
        selectedLocal: state.selectedLocal?.id === id ? updatedLocal : state.selectedLocal,
        isLoading: false,
      }));
      return updatedLocal;
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || "Error al actualizar local";
      set({ error: errorMessage, isLoading: false });
      throw new Error(errorMessage);
    }
  },

  // Eliminar local
  deleteLocal: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      await localesApi.delete(id);
      set((state) => ({
        locales: state.locales.filter((l) => l.id !== id),
        selectedLocal: state.selectedLocal?.id === id ? null : state.selectedLocal,
        isLoading: false,
      }));
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || "Error al eliminar local";
      set({ error: errorMessage, isLoading: false });
      throw new Error(errorMessage);
    }
  },

  // Obtener configuración de un local
  fetchConfig: async (localId: number) => {
    set({ isLoading: true, error: null });
    try {
      const config = await localesApi.getConfig(localId);
      set({ configuracion: config, isLoading: false });
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al cargar configuración",
        isLoading: false,
      });
    }
  },

  // Actualizar configuración
  updateConfig: async (localId: number, data: Partial<ConfiguracionLocalFormData>) => {
    set({ isLoading: true, error: null });
    try {
      const updatedConfig = await localesApi.updateConfig(localId, data);
      set({ configuracion: updatedConfig, isLoading: false });
      return updatedConfig;
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || "Error al actualizar configuración";
      set({ error: errorMessage, isLoading: false });
      throw new Error(errorMessage);
    }
  },

  // Limpiar error
  clearError: () => {
    set({ error: null });
  },

  // Limpiar selección
  clearSelected: () => {
    set({ selectedLocal: null, configuracion: null });
  },
}));
