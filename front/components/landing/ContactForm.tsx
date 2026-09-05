"use client";

import React, { useState } from "react";
import { Box, Container, Typography, TextField, Button, Stack, Paper, Alert, CircularProgress } from "@mui/material";
import { Send, CheckCircle } from "@mui/icons-material";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

// Schema de validación con Zod
const contactSchema = z.object({
  nombre: z
    .string()
    .min(2, "El nombre debe tener al menos 2 caracteres")
    .max(100, "El nombre no puede superar 100 caracteres"),
  email: z
    .string()
    .email("Email inválido")
    .min(5, "El email es requerido")
    .max(100, "El email no puede superar 100 caracteres"),
  telefono: z
    .string()
    .regex(/^[0-9+\s()-]+$/, "Teléfono inválido")
    .min(10, "El teléfono debe tener al menos 10 dígitos")
    .max(20, "El teléfono no puede superar 20 caracteres"),
  mensaje: z
    .string()
    .min(10, "El mensaje debe tener al menos 10 caracteres")
    .max(1000, "El mensaje no puede superar 1000 caracteres"),
});

type ContactFormData = z.infer<typeof contactSchema>;

/**
 * Formulario de contacto de la landing page
 * Incluye validación con Zod y estado de envío
 */
export const ContactForm = () => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<ContactFormData>({
    resolver: zodResolver(contactSchema),
  });

  const onSubmit = async (data: ContactFormData) => {
    setIsSubmitting(true);
    setSubmitError(null);

    try {
      // Simular envío (en producción esto iría a una API o servicio)
      // Por ahora, redirigir a WhatsApp con el mensaje
      const mensaje = `Hola, soy ${data.nombre}.%0A%0AEmail: ${data.email}%0ATelefono: ${data.telefono}%0A%0AMensaje: ${data.mensaje}`;
      const whatsappUrl = `https://wa.me/549349366512?text=${mensaje}`;

      // Esperar un poco para simular envío
      await new Promise((resolve) => setTimeout(resolve, 1000));

      // Abrir WhatsApp
      window.open(whatsappUrl, "_blank");

      setSubmitSuccess(true);
      reset();

      // Ocultar mensaje de éxito después de 5 segundos
      setTimeout(() => {
        setSubmitSuccess(false);
      }, 5000);
    } catch (error) {
      setSubmitError("Hubo un error al enviar el mensaje. Por favor, intenta nuevamente.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Box
      id="contacto"
      sx={{
        py: { xs: 8, md: 12 },
        backgroundColor: "background.default",
      }}
    >
      <Container maxWidth="md">
        <Stack spacing={2} alignItems="center" textAlign="center" sx={{ mb: 6 }}>
          <Typography
            variant="h2"
            sx={{
              fontSize: { xs: "2rem", md: "2.5rem" },
              fontWeight: 700,
              color: "text.primary",
            }}
          >
            ¿Listo para{" "}
            <Box component="span" sx={{ color: "primary.main" }}>
              transformar
            </Box>{" "}
            tu negocio?
          </Typography>
          <Typography
            variant="body1"
            sx={{
              fontSize: { xs: "1rem", md: "1.2rem" },
              color: "text.secondary",
              maxWidth: "600px",
            }}
          >
            Déjanos tus datos y nos pondremos en contacto contigo para una demo personalizada.
          </Typography>
        </Stack>

        <Paper
          elevation={0}
          sx={{
            p: { xs: 3, md: 5 },
            borderRadius: 3,
            border: "1px solid",
            borderColor: (theme) => `${theme.palette.primary.main}33`,
            boxShadow: (theme) => `0 8px 32px ${theme.palette.primary.main}26`,
          }}
        >
          <Box component="form" onSubmit={handleSubmit(onSubmit)}>
            <Stack spacing={3}>
              {submitSuccess && (
                <Alert icon={<CheckCircle fontSize="inherit" />} severity="success" sx={{ borderRadius: 2 }}>
                  ¡Mensaje enviado! Te redirigimos a WhatsApp para continuar la conversación.
                </Alert>
              )}

              {submitError && (
                <Alert severity="error" sx={{ borderRadius: 2 }}>
                  {submitError}
                </Alert>
              )}

              <TextField
                {...register("nombre")}
                label="Nombre completo"
                placeholder="Juan Pérez"
                error={!!errors.nombre}
                helperText={errors.nombre?.message}
                fullWidth
                required
                disabled={isSubmitting}
              />

              <TextField
                {...register("email")}
                label="Email"
                type="email"
                placeholder="juan@ejemplo.com"
                error={!!errors.email}
                helperText={errors.email?.message}
                fullWidth
                required
                disabled={isSubmitting}
              />

              <TextField
                {...register("telefono")}
                label="Teléfono"
                placeholder="+54 9 11 1234-5678"
                error={!!errors.telefono}
                helperText={errors.telefono?.message}
                fullWidth
                required
                disabled={isSubmitting}
              />

              <TextField
                {...register("mensaje")}
                label="Mensaje"
                placeholder="Cuéntanos sobre tu hamburguesería..."
                error={!!errors.mensaje}
                helperText={errors.mensaje?.message}
                multiline
                rows={4}
                fullWidth
                required
                disabled={isSubmitting}
              />

              <Button
                type="submit"
                variant="contained"
                size="large"
                disabled={isSubmitting}
                startIcon={isSubmitting ? <CircularProgress size={20} color="inherit" /> : <Send />}
                sx={{
                  py: 1.5,
                  fontSize: "1.1rem",
                  boxShadow: "0 4px 14px rgba(255, 155, 133, 0.4)",
                  "&:hover": {
                    boxShadow: "0 6px 20px rgba(255, 155, 133, 0.6)",
                  },
                }}
              >
                {isSubmitting ? "Enviando..." : "Enviar mensaje"}
              </Button>

              <Typography
                variant="caption"
                sx={{
                  textAlign: "center",
                  color: "text.disabled",
                }}
              >
                Al enviar este formulario, serás redirigido a WhatsApp para continuar la conversación.
              </Typography>
            </Stack>
          </Box>
        </Paper>
      </Container>
    </Box>
  );
};
