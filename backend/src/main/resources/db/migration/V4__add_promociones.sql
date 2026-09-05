-- V4: Agregar sistema de promociones a productos
-- Fecha: 2025-10-22
-- Descripción: Añade campos para manejar promociones con precios dinámicos por horario y día

-- Agregar campos de promoción a productos
ALTER TABLE productos
ADD COLUMN precio_base DECIMAL(10,2),
ADD COLUMN precio_promocion DECIMAL(10,2),
ADD COLUMN tiene_promocion BOOLEAN DEFAULT FALSE,
ADD COLUMN hora_inicio_promo TIME,
ADD COLUMN hora_fin_promo TIME,
ADD COLUMN dias_promocion VARCHAR(100);

-- Migrar precio actual a precio_base (para productos existentes)
UPDATE productos
SET precio_base = precio
WHERE precio_base IS NULL;

-- Establecer tiene_promocion en FALSE para productos existentes
UPDATE productos
SET tiene_promocion = FALSE
WHERE tiene_promocion IS NULL;

-- Agregar comentarios
COMMENT ON COLUMN productos.precio_base IS 'Precio normal del producto sin descuento';
COMMENT ON COLUMN productos.precio_promocion IS 'Precio del producto cuando está en promoción';
COMMENT ON COLUMN productos.tiene_promocion IS 'Indica si el producto tiene promoción activa';
COMMENT ON COLUMN productos.hora_inicio_promo IS 'Hora de inicio de la promoción (ej: 18:00)';
COMMENT ON COLUMN productos.hora_fin_promo IS 'Hora de fin de la promoción (ej: 21:00)';
COMMENT ON COLUMN productos.dias_promocion IS 'Días de la semana con promoción (JSON array: ["MONDAY","TUESDAY"])';
