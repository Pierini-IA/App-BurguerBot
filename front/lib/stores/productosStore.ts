/**
 * Zustand Store para Productos.
 *
 * La receta se edita embebida en el propio producto (`recetas` en el form),
 * el backend no expone sub-recursos de receta.
 */

import { create } from "zustand";
import { Producto, ProductoFormData } from "@/types/producto";
import { productosApi } from "@/lib/api/productos";

interface ProductosState {
  // State
  productos: Producto[];
  productoActual: Producto | null;
  isLoading: boolean;
  error: string | null;

  // Actions
  fetchProductos: () => Promise<void>;
  fetchProductosByLocal: (telefonoLocal: string) => Promise<void>;
  selectProducto: (id: number | null) => void;
  createProducto: (data: ProductoFormData) => Promise<Producto>;
  updateProducto: (id: number, data: Partial<ProductoFormData>) => Promise<Producto>;
  deleteProducto: (id: number) => Promise<void>;
  toggleDisponibilidad: (id: number) => Promise<void>;
  clearError: () => void;
  clearProductoActual: () => void;
}

export const useProductosStore = create<ProductosState>((set, get) => ({
  // Initial state
  productos: [],
  productoActual: null,
  isLoading: false,
  error: null,

  // Actions
  fetchProductos: async () => {
    set({ isLoading: true, error: null });
    try {
      const productos = await productosApi.getAll();
      set({ productos, isLoading: false });
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al cargar productos",
        isLoading: false,
      });
    }
  },

  fetchProductosByLocal: async (telefonoLocal: string) => {
    set({ isLoading: true, error: null });
    try {
      const productos = await productosApi.getByLocal(telefonoLocal);
      set({ productos, isLoading: false });
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al cargar productos",
        isLoading: false,
      });
    }
  },

  /**
   * Selecciona un producto ya cargado en la lista (el backend no tiene GET by id).
   */
  selectProducto: (id: number | null) => {
    if (id === null) {
      set({ productoActual: null });
      return;
    }
    const producto = get().productos.find((p) => p.id === id) ?? null;
    set({ productoActual: producto });
  },

  createProducto: async (data: ProductoFormData) => {
    set({ isLoading: true, error: null });
    try {
      const producto = await productosApi.create(data);
      set((state) => ({
        productos: [...state.productos, producto],
        isLoading: false,
      }));
      return producto;
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al crear producto",
        isLoading: false,
      });
      throw error;
    }
  },

  updateProducto: async (id: number, data: Partial<ProductoFormData>) => {
    set({ isLoading: true, error: null });
    try {
      const producto = await productosApi.update(id, data);
      set((state) => ({
        productos: state.productos.map((p) => (p.id === id ? producto : p)),
        productoActual: state.productoActual?.id === id ? producto : state.productoActual,
        isLoading: false,
      }));
      return producto;
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al actualizar producto",
        isLoading: false,
      });
      throw error;
    }
  },

  deleteProducto: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      await productosApi.delete(id);
      set((state) => ({
        productos: state.productos.filter((p) => p.id !== id),
        isLoading: false,
      }));
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al eliminar producto",
        isLoading: false,
      });
      throw error;
    }
  },

  toggleDisponibilidad: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      const producto = await productosApi.toggleDisponibilidad(id);
      set((state) => ({
        productos: state.productos.map((p) => (p.id === id ? producto : p)),
        isLoading: false,
      }));
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al cambiar disponibilidad",
        isLoading: false,
      });
      throw error;
    }
  },

  clearError: () => {
    set({ error: null });
  },

  clearProductoActual: () => {
    set({ productoActual: null });
  },
}));
