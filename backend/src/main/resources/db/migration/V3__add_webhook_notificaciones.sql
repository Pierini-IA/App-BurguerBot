-- ========================================
-- Dio Burger - Migración V3
-- Script: V3__add_webhook_notificaciones.sql
-- Descripción: Agrega URL webhook para notificaciones de n8n
-- Autor: Dio Burger Team
-- Fecha: 2025-10-22
-- ========================================

-- Agregar columna para URL webhook de notificaciones (n8n)
ALTER TABLE configuracion_local
ADD COLUMN url_webhook_notificaciones VARCHAR(500);

COMMENT ON COLUMN configuracion_local.url_webhook_notificaciones IS 
'URL del webhook de n8n para enviar notificaciones cuando un pedido está listo o en camino';
