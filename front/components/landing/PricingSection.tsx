"use client";

import React from "react";
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  Button,
  Chip,
  Stack,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  useTheme,
} from "@mui/material";
import { Check as CheckIcon, Star as StarIcon } from "@mui/icons-material";

interface PlanFeature {
  text: string;
  included: boolean;
}

interface Plan {
  id: string;
  nombre: string;
  descripcion: string;
  setupInicial: number;
  precioMensual: number;
  destacado?: boolean;
  features: PlanFeature[];
  colorVariant: "light" | "main" | "secondary";
}

const planes: Plan[] = [
  {
    id: "basico",
    nombre: "Básico",
    descripcion: "Restaurantes chicos",
    setupInicial: 100000,
    precioMensual: 35000,
    colorVariant: "light",
    features: [
      { text: "Panel web de administración", included: true },
      { text: "Gestión de productos y menú", included: true },
      { text: "Control de ingredientes y stock", included: true },
      { text: "Gestión de mesas", included: true },
      { text: "Panel de cocina", included: true },
      { text: "Estadísticas básicas del día", included: true },
      { text: "Bot de WhatsApp", included: false },
      { text: "Sistema de reservas", included: false },
      { text: "Reportes avanzados", included: false },
    ],
  },
  {
    id: "estandar",
    nombre: "Estándar",
    descripcion: "Negocios con reservas o delivery",
    setupInicial: 180000,
    precioMensual: 75000,
    destacado: true,
    colorVariant: "main",
    features: [
      { text: "Todo lo del plan Básico", included: true },
      { text: "Bot de WhatsApp automático", included: true },
      { text: "Sistema de reservas inteligente", included: true },
      { text: "Notificaciones en tiempo real", included: true },
      { text: "Gestión automática de pedidos", included: true },
      { text: "Menú dinámico por WhatsApp", included: true },
      { text: "Reportes avanzados", included: false },
      { text: "Webhooks personalizados", included: false },
      { text: "Impresión automática", included: false },
    ],
  },
  {
    id: "premium",
    nombre: "Premium",
    descripcion: "Franquicias o locales con IA + impresión automática",
    setupInicial: 250000,
    precioMensual: 125000,
    colorVariant: "secondary",
    features: [
      { text: "Todo lo del plan Estándar", included: true },
      { text: "Reportes avanzados de ventas", included: true },
      { text: "Análisis de productos top", included: true },
      { text: "Dashboard completo con KPIs", included: true },
      { text: "Webhooks personalizados", included: true },
      { text: "Impresión automática de pedidos", included: true },
      { text: "Asignación de repartidores", included: true },
      { text: "Integraciones externas", included: true },
      { text: "Soporte prioritario", included: true },
    ],
  },
];

const formatPrice = (price: number) => {
  return new Intl.NumberFormat("es-AR", {
    style: "currency",
    currency: "ARS",
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(price);
};

export const PricingSection: React.FC = () => {
  const theme = useTheme();

  const getPlanColor = (colorVariant: "light" | "main" | "secondary") => {
    switch (colorVariant) {
      case "light":
        return theme.palette.primary.light;
      case "main":
        return theme.palette.primary.main;
      case "secondary":
        return theme.palette.secondary.main;
      default:
        return theme.palette.primary.main;
    }
  };

  return (
    <Box
      id="planes"
      sx={{
        py: 10,
        background: (theme) =>
          `linear-gradient(180deg, ${theme.palette.background.default} 0%, ${theme.palette.background.paper} 100%)`,
      }}
    >
      <Container maxWidth="lg">
        {/* Header */}
        <Box sx={{ textAlign: "center", mb: 8 }}>
          <Typography
            variant="h2"
            sx={{
              fontWeight: 700,
              color: "text.primary",
              mb: 2,
            }}
          >
            Elige el plan perfecto para tu negocio
          </Typography>
          <Typography
            variant="h5"
            sx={{
              color: "text.secondary",
              maxWidth: "800px",
              mx: "auto",
            }}
          >
            Desde pequeños restaurantes hasta franquicias, tenemos la solución ideal para automatizar tu operación
          </Typography>
        </Box>

        {/* Plans Grid */}
        <Stack
          direction={{ xs: "column", md: "row" }}
          spacing={4}
          sx={{
            alignItems: { md: "stretch" },
            justifyContent: "center",
          }}
        >
          {planes.map((plan) => {
            const planColor = getPlanColor(plan.colorVariant);

            return (
              <Card
                key={plan.id}
                elevation={plan.destacado ? 8 : 2}
                sx={{
                  flex: 1,
                  maxWidth: { md: "380px" },
                  position: "relative",
                  borderRadius: 3,
                  border: plan.destacado ? `3px solid ${theme.palette.primary.main}` : "1px solid",
                  borderColor: plan.destacado ? undefined : "divider",
                  transform: plan.destacado ? "scale(1.05)" : "scale(1)",
                  transition: "all 0.3s ease-in-out",
                  "&:hover": {
                    transform: plan.destacado ? "scale(1.08)" : "scale(1.03)",
                    boxShadow: plan.destacado
                      ? `0 12px 40px ${theme.palette.primary.main}66`
                      : "0 8px 24px rgba(0,0,0,0.15)",
                  },
                }}
              >
                {/* Badge "Más Popular" */}
                {plan.destacado && (
                  <Chip
                    icon={<StarIcon sx={{ color: "#FFF !important" }} />}
                    label="Más Popular"
                    size="small"
                    color="secondary"
                    sx={{
                      position: "absolute",
                      top: -12,
                      right: 20,
                      fontWeight: 600,
                      px: 1,
                    }}
                  />
                )}

                <CardContent sx={{ p: 4 }}>
                  {/* Header del Plan */}
                  <Box
                    sx={{
                      textAlign: "center",
                      mb: 3,
                      pb: 3,
                      borderBottom: `2px solid ${planColor}`,
                    }}
                  >
                    <Typography
                      variant="h4"
                      sx={{
                        fontWeight: 700,
                        color: "text.primary",
                        mb: 1,
                      }}
                    >
                      {plan.nombre}
                    </Typography>
                    <Typography
                      variant="body2"
                      sx={{
                        color: "text.secondary",
                        mb: 3,
                        minHeight: "40px",
                      }}
                    >
                      {plan.descripcion}
                    </Typography>

                    {/* Setup Inicial */}
                    <Box sx={{ mb: 2 }}>
                      <Typography
                        variant="body2"
                        sx={{
                          color: "text.secondary",
                          fontWeight: 500,
                        }}
                      >
                        Setup inicial
                      </Typography>
                      <Typography
                        variant="h5"
                        sx={{
                          fontWeight: 700,
                          color: plan.destacado ? "secondary.main" : "primary.main",
                        }}
                      >
                        {formatPrice(plan.setupInicial)}
                      </Typography>
                      <Typography
                        variant="caption"
                        sx={{
                          color: "text.disabled",
                        }}
                      >
                        Pago único
                      </Typography>
                    </Box>

                    {/* Precio Mensual */}
                    <Box>
                      <Typography
                        variant="h3"
                        sx={{
                          fontWeight: 800,
                          color: "text.primary",
                        }}
                      >
                        {formatPrice(plan.precioMensual)}
                      </Typography>
                      <Typography
                        variant="body1"
                        sx={{
                          color: "text.secondary",
                          fontWeight: 500,
                        }}
                      >
                        /mes
                      </Typography>
                    </Box>
                  </Box>

                  {/* Features List */}
                  <List sx={{ mb: 3 }}>
                    {plan.features.map((feature, index) => (
                      <ListItem
                        key={index}
                        sx={{
                          py: 0.75,
                          px: 0,
                        }}
                      >
                        <ListItemIcon sx={{ minWidth: 36 }}>
                          <CheckIcon
                            sx={{
                              color: feature.included ? "success.main" : "grey.300",
                              fontSize: 20,
                            }}
                          />
                        </ListItemIcon>
                        <ListItemText
                          primary={feature.text}
                          primaryTypographyProps={{
                            variant: "body2",
                            sx: {
                              color: feature.included ? "text.primary" : "text.disabled",
                              fontWeight: feature.included ? 500 : 400,
                              textDecoration: feature.included ? "none" : "line-through",
                            },
                          }}
                        />
                      </ListItem>
                    ))}
                  </List>

                  {/* CTA Button */}
                  <Button
                    variant={plan.destacado ? "contained" : "outlined"}
                    color="primary"
                    fullWidth
                    size="large"
                    href="https://wa.me/549349366512?text=Hola!%20Quiero%20información%20sobre%20Dio%20Burger"
                    target="_blank"
                    sx={{
                      py: 1.5,
                      borderRadius: 2,
                      fontWeight: 600,
                      fontSize: "1rem",
                      textTransform: "none",
                    }}
                  >
                    {plan.destacado ? "¡Quiero este plan!" : "Contactar"}
                  </Button>
                </CardContent>
              </Card>
            );
          })}
        </Stack>

        {/* Footer Note */}
        <Box sx={{ textAlign: "center", mt: 6 }}>
          <Typography
            variant="body2"
            sx={{
              color: "text.secondary",
              maxWidth: "700px",
              mx: "auto",
            }}
          >
            * Los precios son en pesos argentinos e incluyen IVA. Setup inicial es un pago único para configuración e
            instalación. Todos los planes incluyen soporte técnico y actualizaciones.
          </Typography>
        </Box>
      </Container>
    </Box>
  );
};
