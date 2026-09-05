-- =============================================================================
-- V8: Agregar Sistema de Planes de Suscripción
-- =============================================================================
-- Descripción: Agrega campos para gestionar planes de suscripción (BASICO, ESTANDAR, PREMIUM)
--              Permite activar/desactivar funcionalidades según el plan contratado
-- Versión: 2.2.0
-- Fecha: 2025
-- =============================================================================

-- Agregar columna plan_suscripcion (enum: BASICO, ESTANDAR, PREMIUM)
ALTER TABLE locales 
ADD COLUMN plan_suscripcion VARCHAR(20) NOT NULL DEFAULT 'PREMIUM';

-- Agregar columna plan_activo (boolean)
ALTER TABLE locales 
ADD COLUMN plan_activo BOOLEAN NOT NULL DEFAULT TRUE;

-- Agregar columna fecha_inicio_plan (date, nullable)
ALTER TABLE locales 
ADD COLUMN fecha_inicio_plan DATE;

-- Agregar columna fecha_fin_plan (date, nullable)
ALTER TABLE locales 
ADD COLUMN fecha_fin_plan DATE;

-- Crear índice para búsquedas por plan
CREATE INDEX idx_locales_plan_suscripcion ON locales(plan_suscripcion);

-- Crear índice para búsquedas por estado del plan
CREATE INDEX idx_locales_plan_activo ON locales(plan_activo);

-- Crear índice compuesto para búsquedas de planes activos por tipo
CREATE INDEX idx_locales_plan_activo_tipo ON locales(plan_activo, plan_suscripcion);

-- Agregar check constraint para validar valores del enum PlanSuscripcion
ALTER TABLE locales 
ADD CONSTRAINT chk_plan_suscripcion 
CHECK (plan_suscripcion IN ('BASICO', 'ESTANDAR', 'PREMIUM'));

-- =============================================================================
-- ACTUALIZACIÓN DE REGISTROS EXISTENTES
-- =============================================================================

-- Actualizar todos los locales existentes a PREMIUM con plan activo
-- Esto asegura compatibilidad hacia atrás: los clientes actuales mantienen todas las funcionalidades
UPDATE locales 
SET plan_suscripcion = 'PREMIUM',
    plan_activo = TRUE,
    fecha_inicio_plan = CURRENT_DATE
WHERE plan_suscripcion IS NULL OR plan_suscripcion = 'PREMIUM';

-- =============================================================================
-- COMENTARIOS DE DOCUMENTACIÓN
-- =============================================================================

COMMENT ON COLUMN locales.plan_suscripcion IS 'Plan de suscripción del local: BASICO, ESTANDAR o PREMIUM. Determina las funcionalidades disponibles.';
COMMENT ON COLUMN locales.plan_activo IS 'Indica si el plan está activo. Si es false, el local no puede usar el sistema.';
COMMENT ON COLUMN locales.fecha_inicio_plan IS 'Fecha de inicio del plan actual. Útil para tracking y reportes.';
COMMENT ON COLUMN locales.fecha_fin_plan IS 'Fecha de vencimiento del plan. Si es NULL, el plan es indefinido.';

-- =============================================================================
-- NOTAS TÉCNICAS
-- =============================================================================
-- 1. Todos los locales existentes se actualizan a PREMIUM para mantener compatibilidad
-- 2. Los nuevos locales tendrán PREMIUM por defecto (definido en entidad JPA)
-- 3. Los índices mejoran el rendimiento en consultas de validación de planes
-- 4. El check constraint asegura integridad referencial a nivel de BD
-- 5. Las fechas son opcionales: si fecha_fin_plan es NULL, el plan no expira
-- =============================================================================
