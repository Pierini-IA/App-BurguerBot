import { useAuthStore } from "@/lib/stores/authStore";
import { Rol } from "@/types/usuario";

/**
 * Hook personalizado para acceder al estado de autenticación
 * Simplifica el acceso al store de Zustand
 *
 * @example
 * const { user, isAuthenticated, login, logout } = useAuth();
 */
export const useAuth = () => {
  const { user, token, isAuthenticated, isLoading, error, login, logout, setUser, clearError, loadUser, checkAuth } =
    useAuthStore();

  /**
   * Verificar si el usuario tiene un rol específico
   */
  const hasRole = (rol: Rol): boolean => {
    if (!user) return false;
    return user.rol === rol;
  };

  /**
   * Verificar si el usuario tiene alguno de los roles especificados
   */
  const hasAnyRole = (roles: Rol[]): boolean => {
    if (!user) return false;
    return roles.includes(user.rol);
  };

  /**
   * Verificar si el usuario es SuperAdmin
   */
  const isSuperAdmin = (): boolean => {
    return hasRole(Rol.SUPERADMIN);
  };

  /**
   * Verificar si el usuario es Admin
   */
  const isAdmin = (): boolean => {
    return hasRole(Rol.ADMIN);
  };

  /**
   * Verificar si el usuario es Cocina
   */
  const isCocina = (): boolean => {
    return hasRole(Rol.COCINA);
  };

  return {
    // State
    user,
    token,
    isAuthenticated,
    isLoading,
    error,

    // Actions
    login,
    logout,
    setUser,
    clearError,
    loadUser,
    checkAuth,

    // Role helpers
    hasRole,
    hasAnyRole,
    isSuperAdmin,
    isAdmin,
    isCocina,
  };
};
