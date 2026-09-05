-- V5: Sistema de delivery automático con asignación de repartidores
-- Fecha: 2025-10-22
-- Descripción: Añade campos para gestionar repartidores y webhooks de asignación automática

-- Agregar campos de repartidor a pedidos
ALTER TABLE pedidos
ADD COLUMN repartidor_id VARCHAR(100),
ADD COLUMN repartidor_nombre VARCHAR(200),
ADD COLUMN repartidor_telefono VARCHAR(20),
ADD COLUMN hora_asignacion_repartidor TIMESTAMP,
ADD COLUMN url_tracking_delivery VARCHAR(500);

-- Agregar webhook de asignación de delivery a configuracion_local
ALTER TABLE configuracion_local
ADD COLUMN url_webhook_asignacion_delivery VARCHAR(500);

-- Agregar comentarios
COMMENT ON COLUMN pedidos.repartidor_id IS 'ID del repartidor asignado (viene de n8n)';
COMMENT ON COLUMN pedidos.repartidor_nombre IS 'Nombre del repartidor asignado';
COMMENT ON COLUMN pedidos.repartidor_telefono IS 'Teléfono del repartidor para contacto';
COMMENT ON COLUMN pedidos.hora_asignacion_repartidor IS 'Momento en que se asignó el repartidor';
COMMENT ON COLUMN pedidos.url_tracking_delivery IS 'URL para trackear el pedido en tiempo real';
COMMENT ON COLUMN configuracion_local.url_webhook_asignacion_delivery IS 'Webhook que n8n llama para asignar repartidor automáticamente';
