"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import {
  Box,
  Container,
  Typography,
  Paper,
  TextField,
  Button,
  Stack,
  Alert,
  InputAdornment,
  IconButton,
  CircularProgress,
  Link as MuiLink,
} from "@mui/material";
import { Visibility, VisibilityOff, Login as LoginIcon, ArrowBack } from "@mui/icons-material";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import Link from "next/link";
import { useAuth } from "@/lib/hooks/useAuth";

// Schema de validación con Zod
const loginSchema = z.object({
  username: z
    .string()
    .min(3, "El username debe tener al menos 3 caracteres")
    .max(50, "El username no puede superar 50 caracteres"),
  password: z.string().min(6, "La contraseña debe tener al menos 6 caracteres"),
});

type LoginFormData = z.infer<typeof loginSchema>;

/**
 * Página de Login con autenticación JWT
 * Redirige según el rol del usuario:
 * - SUPERADMIN → /superadmin
 * - ADMIN → /admin
 * - COCINA → /cocina
 */
export default function LoginPage() {
  const router = useRouter();
  const { login, isAuthenticated, isLoading: authLoading, error, clearError, user } = useAuth();

  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  // Redirigir si ya está autenticado
  useEffect(() => {
    if (isAuthenticated && user) {
      redirectByRole();
    }
  }, [isAuthenticated, user]);

  // Limpiar error cuando se desmonta el componente
  useEffect(() => {
    return () => {
      clearError();
    };
  }, [clearError]);

  const redirectByRole = () => {
    if (!user) return;

    switch (user.rol) {
      case "ROLE_SUPERADMIN":
        router.push("/superadmin");
        break;
      case "ROLE_ADMIN":
        router.push("/admin/dashboard");
        break;
      case "ROLE_COCINA":
        router.push("/cocina");
        break;
      default:
        router.push("/");
    }
  };

  const onSubmit = async (data: LoginFormData) => {
    setIsSubmitting(true);
    clearError();

    try {
      await login(data);
      // Redirigir después del login exitoso
      redirectByRole();
    } catch (err: any) {
      // El error ya está en el store
      console.error("Error de login:", err);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        background: "linear-gradient(135deg, #FFF5F2 0%, #FFE8DF 100%)",
        py: 4,
      }}
    >
      <Container maxWidth="sm">
        <Paper
          elevation={0}
          sx={{
            p: { xs: 3, md: 5 },
            borderRadius: 3,
            boxShadow: "0 8px 32px rgba(255, 155, 133, 0.2)",
          }}
        >
          {/* Header */}
          <Stack spacing={2} alignItems="center" mb={4}>
            <Box
              sx={{
                width: 80,
                height: 80,
                borderRadius: "50%",
                backgroundColor: "primary.main",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                fontSize: "3rem",
              }}
            >
              🍔
            </Box>

            <Typography
              variant="h4"
              sx={{
                fontWeight: 700,
                textAlign: "center",
                color: "text.primary",
              }}
            >
              Iniciar Sesión
            </Typography>

            <Typography variant="body2" color="text.secondary" textAlign="center">
              Ingresa con tu nombre de usuario
            </Typography>
          </Stack>

          {/* Error Alert */}
          {error && (
            <Alert severity="error" onClose={clearError} sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          {/* Formulario */}
          <Box component="form" onSubmit={handleSubmit(onSubmit)}>
            <Stack spacing={3}>
              <TextField
                {...register("username")}
                label="Username"
                placeholder="usuario123"
                error={!!errors.username}
                helperText={errors.username?.message}
                fullWidth
                autoFocus
                disabled={isSubmitting}
              />

              <TextField
                {...register("password")}
                label="Contraseña"
                type={showPassword ? "text" : "password"}
                error={!!errors.password}
                helperText={errors.password?.message}
                fullWidth
                disabled={isSubmitting}
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton onClick={() => setShowPassword(!showPassword)} edge="end" disabled={isSubmitting}>
                        {showPassword ? <VisibilityOff /> : <Visibility />}
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />

              <Button
                type="submit"
                variant="contained"
                size="large"
                fullWidth
                disabled={isSubmitting || authLoading}
                startIcon={isSubmitting || authLoading ? <CircularProgress size={20} /> : <LoginIcon />}
                sx={{
                  py: 1.5,
                  fontSize: "1rem",
                  fontWeight: 600,
                }}
              >
                {isSubmitting || authLoading ? "Iniciando sesión..." : "Iniciar Sesión"}
              </Button>
            </Stack>
          </Box>

          {/* Footer */}
          <Stack spacing={2} mt={4} alignItems="center">
            <MuiLink
              component={Link}
              href="/"
              sx={{
                display: "flex",
                alignItems: "center",
                gap: 0.5,
                textDecoration: "none",
                color: "text.secondary",
                "&:hover": {
                  color: "primary.main",
                },
              }}
            >
              <ArrowBack fontSize="small" />
              Volver al inicio
            </MuiLink>

            <Typography variant="caption" color="text.secondary" textAlign="center">
              ¿No tienes cuenta? Contacta al administrador del sistema
            </Typography>
          </Stack>
        </Paper>
      </Container>
    </Box>
  );
}
