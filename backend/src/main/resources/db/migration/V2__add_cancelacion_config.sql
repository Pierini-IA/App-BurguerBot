-- ========================================
-- Dio Burger - Migración V2
-- Script: V2__add_cancelacion_config.sql
-- Descripción: Agrega configuración de cancelación de pedidos
-- Autor: Dio Burger Team
-- Fecha: 2025-10-22
-- ========================================

-- Agregar columna para tiempo mínimo de anticipación para cancelar pedidos
ALTER TABLE configuracion_local
ADD COLUMN minutos_anticipacion_cancelacion INTEGER NOT NULL DEFAULT 30;

COMMENT ON COLUMN configuracion_local.minutos_anticipacion_cancelacion IS 
'Tiempo mínimo en minutos de anticipación requerido para cancelar un pedido. Si se intenta cancelar con menos tiempo, se rechaza la cancelación';
