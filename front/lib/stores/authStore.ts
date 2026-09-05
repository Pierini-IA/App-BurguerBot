import { create } from "zustand";
import { persist } from "zustand/middleware";
import { Usuario, LoginCredentials, Rol } from "@/types/usuario";
import { authApi } from "@/lib/api/auth";
import { defaults } from "@/lib/config/defaults";

/**
 * Estado de autenticación
 */
interface AuthState {
  // State
  user: Usuario | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
  setUser: (user: Usuario) => void;
  clearError: () => void;
  loadUser: () => Promise<void>;
  checkAuth: () => Promise<boolean>;
}

/**
 * Store de autenticación con Zustand
 * Persiste token y usuario en localStorage
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // Initial state
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      // Login
      login: async (credentials: LoginCredentials) => {
        set({ isLoading: true, error: null });
        try {
          const response = await authApi.login(credentials);

          // Guardar token en localStorage
          localStorage.setItem(defaults.storage.token, response.token);

          // Convertir la respuesta del backend a Usuario
          const user: Usuario = {
            username: response.username,
            rol: response.rol as Rol,
            telefonoLocal: response.telefonoLocal,
          };

          set({
            user,
            token: response.token,
            isAuthenticated: true,
            isLoading: false,
          });
        } catch (error: any) {
          const errorMessage = error.response?.data?.message || error.message || "Error al iniciar sesión";

          set({
            error: errorMessage,
            isLoading: false,
            isAuthenticated: false,
          });

          throw new Error(errorMessage);
        }
      },

      // Logout
      logout: () => {
        authApi.logout();
        set({
          user: null,
          token: null,
          isAuthenticated: false,
          error: null,
        });
      },

      // Actualizar usuario
      setUser: (user: Usuario) => {
        set({ user });
      },

      // Limpiar error
      clearError: () => {
        set({ error: null });
      },

      // Cargar usuario desde el store persistido (Zustand ya maneja la persistencia)
      loadUser: async () => {
        const token = localStorage.getItem(defaults.storage.token);
        const state = get();

        if (!token || !state.user) {
          set({ isAuthenticated: false, isLoading: false });
          return;
        }

        // El usuario ya está cargado desde Zustand persist
        set({ isAuthenticated: true, isLoading: false });
      },

      // Verificar si el usuario está autenticado
      checkAuth: async (): Promise<boolean> => {
        const token = localStorage.getItem(defaults.storage.token);
        const state = get();
        return !!(token && state.user && state.isAuthenticated);
      },
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        token: state.token,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
