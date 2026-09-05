import { create } from "zustand";
import { Usuario, UsuarioCreateData } from "@/types/usuario";
import { usuariosApi, UsuarioUpdateData } from "@/lib/api/usuarios";

interface UsuariosState {
  // State
  usuarios: Usuario[];
  selectedUsuario: Usuario | null;
  isLoading: boolean;
  error: string | null;

  // Actions
  fetchUsuarios: () => Promise<void>;
  fetchUsuariosByLocal: (localId: number) => Promise<void>;
  fetchUsuarioById: (id: number) => Promise<void>;
  createUsuario: (data: UsuarioCreateData) => Promise<Usuario>;
  updateUsuario: (id: number, data: UsuarioUpdateData) => Promise<Usuario>;
  deleteUsuario: (id: number) => Promise<void>;
  toggleActive: (id: number) => Promise<void>;
  changePassword: (id: number, newPassword: string) => Promise<void>;
  clearError: () => void;
  clearSelected: () => void;
}

/**
 * Store de Usuarios con Zustand
 */
export const useUsuariosStore = create<UsuariosState>((set, get) => ({
  // Initial state
  usuarios: [],
  selectedUsuario: null,
  isLoading: false,
  error: null,

  // Obtener todos los usuarios
  fetchUsuarios: async () => {
    set({ isLoading: true, error: null });
    try {
      const usuarios = await usuariosApi.getAll();
      set({ usuarios, isLoading: false });
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al cargar usuarios",
        isLoading: false,
      });
    }
  },

  // Obtener usuarios por local
  fetchUsuariosByLocal: async (localId: number) => {
    set({ isLoading: true, error: null });
    try {
      const usuarios = await usuariosApi.getByLocal(localId);
      set({ usuarios, isLoading: false });
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al cargar usuarios",
        isLoading: false,
      });
    }
  },

  // Obtener un usuario por ID
  fetchUsuarioById: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      const usuario = await usuariosApi.getById(id);
      set({ selectedUsuario: usuario, isLoading: false });
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al cargar usuario",
        isLoading: false,
      });
    }
  },

  // Crear usuario
  createUsuario: async (data: UsuarioCreateData) => {
    set({ isLoading: true, error: null });
    try {
      const newUsuario = await usuariosApi.create(data);
      set((state) => ({
        usuarios: [...state.usuarios, newUsuario],
        isLoading: false,
      }));
      return newUsuario;
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || "Error al crear usuario";
      set({ error: errorMessage, isLoading: false });
      throw new Error(errorMessage);
    }
  },

  // Actualizar usuario
  updateUsuario: async (id: number, data: UsuarioUpdateData) => {
    set({ isLoading: true, error: null });
    try {
      const updatedUsuario = await usuariosApi.update(id, data);
      set((state) => ({
        usuarios: state.usuarios.map((u) => (u.id === id ? updatedUsuario : u)),
        selectedUsuario: state.selectedUsuario?.id === id ? updatedUsuario : state.selectedUsuario,
        isLoading: false,
      }));
      return updatedUsuario;
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || "Error al actualizar usuario";
      set({ error: errorMessage, isLoading: false });
      throw new Error(errorMessage);
    }
  },

  // Eliminar usuario
  deleteUsuario: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      await usuariosApi.delete(id);
      set((state) => ({
        usuarios: state.usuarios.filter((u) => u.id !== id),
        selectedUsuario: state.selectedUsuario?.id === id ? null : state.selectedUsuario,
        isLoading: false,
      }));
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || "Error al eliminar usuario";
      set({ error: errorMessage, isLoading: false });
      throw new Error(errorMessage);
    }
  },

  // Activar/Desactivar usuario
  toggleActive: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      const updatedUsuario = await usuariosApi.toggleActive(id);
      set((state) => ({
        usuarios: state.usuarios.map((u) => (u.id === id ? updatedUsuario : u)),
        isLoading: false,
      }));
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Error al cambiar estado del usuario",
        isLoading: false,
      });
    }
  },

  // Cambiar contraseña
  changePassword: async (id: number, newPassword: string) => {
    set({ isLoading: true, error: null });
    try {
      await usuariosApi.changePassword(id, newPassword);
      set({ isLoading: false });
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || "Error al cambiar contraseña";
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
    set({ selectedUsuario: null });
  },
}));
