-- ========================================
-- Dio Burger - Migración V9
-- Script: V9__add_meta_config.sql
-- Descripción: Credenciales Meta (WhatsApp/Instagram/Facebook) por local,
--              para integración directa con Graph API sin n8n.
-- Autor: Dio Burger Team
-- Fecha: 2026-07-29
-- ========================================

ALTER TABLE configuracion_local
ADD COLUMN wa_phone_id VARCHAR(100),
ADD COLUMN wa_access_token VARCHAR(500),
ADD COLUMN ig_token VARCHAR(500),
ADD COLUMN fb_page_id VARCHAR(100),
ADD COLUMN fb_page_access_token VARCHAR(500);

COMMENT ON COLUMN configuracion_local.wa_phone_id IS
'Phone Number ID de WhatsApp Business API (Meta) asignado a este local';
COMMENT ON COLUMN configuracion_local.wa_access_token IS
'Access token de WhatsApp Business API (Meta) para enviar mensajes';
COMMENT ON COLUMN configuracion_local.ig_token IS
'Access token de Instagram Graph API para responder comentarios de posts';
COMMENT ON COLUMN configuracion_local.fb_page_id IS
'ID de la página de Facebook vinculada a este local';
COMMENT ON COLUMN configuracion_local.fb_page_access_token IS
'Access token de la página de Facebook para responder comentarios de posts';
