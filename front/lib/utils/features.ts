/**
 * Utilidades para Feature-Gating basado en planes de suscripción.
 */

import { PlanSuscripcion } from "@/types/api";

/**
 * Features disponibles por plan.
 */
export const FEATURES_BY_PLAN: Record<PlanSuscripcion, string[]> = {
  [PlanSuscripcion.FREE]: [],
  [PlanSuscripcion.BASIC]: ["RESERVAS_HABILITADAS"],
  [PlanSuscripcion.PREMIUM]: ["RESERVAS_HABILITADAS", "REPORTES_AVANZADOS", "IMPRESION_AUTOMATICA"],
  [PlanSuscripcion.ENTERPRISE]: [
    "RESERVAS_HABILITADAS",
    "REPORTES_AVANZADOS",
    "IMPRESION_AUTOMATICA",
    "MULTI_SUCURSAL",
    "API_ACCESS",
  ],
};

/**
 * Verifica si un plan tiene acceso a una feature.
 */
export const hasFeatureAccess = (plan: PlanSuscripcion, feature: string, planActivo: boolean = true): boolean => {
  if (!planActivo) return false;
  const features = FEATURES_BY_PLAN[plan] || [];
  return features.includes(feature);
};

/**
 * Obtiene el plan mínimo requerido para una feature.
 */
export const getMinPlanForFeature = (feature: string): PlanSuscripcion | null => {
  for (const [plan, features] of Object.entries(FEATURES_BY_PLAN)) {
    if (features.includes(feature)) {
      return plan as PlanSuscripcion;
    }
  }
  return null;
};

/**
 * Obtiene todos los planes que incluyen una feature.
 */
export const getPlansWithFeature = (feature: string): PlanSuscripcion[] => {
  return Object.entries(FEATURES_BY_PLAN)
    .filter(([_, features]) => features.includes(feature))
    .map(([plan]) => plan as PlanSuscripcion);
};

/**
 * Verifica si un plan es superior a otro.
 */
export const isPlanHigherThan = (plan1: PlanSuscripcion, plan2: PlanSuscripcion): boolean => {
  const planOrder = [PlanSuscripcion.FREE, PlanSuscripcion.BASIC, PlanSuscripcion.PREMIUM, PlanSuscripcion.ENTERPRISE];

  return planOrder.indexOf(plan1) > planOrder.indexOf(plan2);
};

/**
 * Features disponibles en la aplicación.
 */
export enum Feature {
  RESERVAS_HABILITADAS = "RESERVAS_HABILITADAS",
  REPORTES_AVANZADOS = "REPORTES_AVANZADOS",
  IMPRESION_AUTOMATICA = "IMPRESION_AUTOMATICA",
  MULTI_SUCURSAL = "MULTI_SUCURSAL",
  API_ACCESS = "API_ACCESS",
}
