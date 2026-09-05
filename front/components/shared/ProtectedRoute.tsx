"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Box, CircularProgress } from "@mui/material";
import { useAuth } from "@/lib/hooks/useAuth";
import { Rol } from "@/types/usuario";

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: Rol[];
  redirectTo?: string;
}

/**
 * Componente de Ruta Protegida
 * Valida autenticación y roles antes de renderizar el contenido
 *
 * @param children - Contenido a proteger
 * @param allowedRoles - Array de roles permitidos (opcional, por defecto permite cualquier rol autenticado)
 * @param redirectTo - Ruta a redirigir si no está autenticado (por defecto /login)
 *
 * @example
 * // Proteger ruta solo para SuperAdmin
 * <ProtectedRoute allowedRoles={[Rol.SUPERADMIN]}>
 *   <SuperAdminPanel />
 * </ProtectedRoute>
 *
 * @example
 * // Proteger ruta para Admin y Cocina
 * <ProtectedRoute allowedRoles={[Rol.ADMIN, Rol.COCINA]}>
 *   <Panel />
 * </ProtectedRoute>
 */
export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, allowedRoles, redirectTo = "/login" }) => {
  const router = useRouter();
  const { isAuthenticated, isLoading, user, loadUser } = useAuth();
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      // Si no hay usuario cargado, intentar cargar desde localStorage
      if (!user && !isLoading) {
        await loadUser();
      }

      setIsChecking(false);
    };

    checkAuth();
  }, [user, isLoading, loadUser]);

  useEffect(() => {
    // Si ya terminamos de verificar y no está autenticado, redirigir
    if (!isChecking && !isLoading && !isAuthenticated) {
      router.push(redirectTo);
      return;
    }

    // Si está autenticado pero no tiene el rol necesario
    if (!isChecking && !isLoading && isAuthenticated && user && allowedRoles) {
      if (!allowedRoles.includes(user.rol)) {
        // Redirigir a la página correspondiente según su rol
        switch (user.rol) {
          case Rol.SUPERADMIN:
            router.push("/superadmin");
            break;
          case Rol.ADMIN:
            router.push("/admin/dashboard");
            break;
          case Rol.COCINA:
            router.push("/cocina");
            break;
          default:
            router.push("/");
        }
      }
    }
  }, [isChecking, isLoading, isAuthenticated, user, allowedRoles, redirectTo, router]);

  // Mostrar loading mientras verifica autenticación
  if (isChecking || isLoading) {
    return (
      <Box
        sx={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          minHeight: "100vh",
        }}
      >
        <CircularProgress size={60} />
      </Box>
    );
  }

  // Si no está autenticado, no renderizar nada (se redirigirá)
  if (!isAuthenticated) {
    return null;
  }

  // Si tiene allowedRoles y no tiene el rol, no renderizar nada (se redirigirá)
  if (allowedRoles && user && !allowedRoles.includes(user.rol)) {
    return null;
  }

  // Usuario autenticado y con el rol correcto
  return <>{children}</>;
};
