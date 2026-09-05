-- ========================================
-- Dio Burger - Migración Inicial
-- Script: V1__init_schema.sql
-- Descripción: Creación de todas las tablas del sistema Multi-Tenancy
-- Autor: Dio Burger Team
-- Fecha: 2025-10-21
-- ========================================

-- Tabla: locales
-- Representa cada local de la red Dio Burger
CREATE TABLE locales (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    direccion VARCHAR(500) NOT NULL,
    telefono VARCHAR(20) UNIQUE NOT NULL
);

-- Tabla: configuracion_local
-- Configuración operativa de cada local
CREATE TABLE configuracion_local (
    id BIGSERIAL PRIMARY KEY,
    local_id BIGINT UNIQUE NOT NULL,
    hora_apertura TIME NOT NULL,
    hora_cierre TIME NOT NULL,
    intervalo_minutos_pedidos INTEGER NOT NULL DEFAULT 15,
    max_pedidos_por_intervalo INTEGER NOT NULL DEFAULT 5,
    hora_apertura_reservas TIME NOT NULL,
    hora_cierre_reservas TIME NOT NULL,
    intervalo_minutos_reservas INTEGER NOT NULL DEFAULT 30,
    max_reservas_por_intervalo INTEGER NOT NULL DEFAULT 3,
    permite_delivery BOOLEAN NOT NULL DEFAULT FALSE,
    permite_take_away BOOLEAN NOT NULL DEFAULT TRUE,
    permite_reservas BOOLEAN NOT NULL DEFAULT FALSE,
    impresion_activa BOOLEAN NOT NULL DEFAULT FALSE,
    url_webhook_impresora VARCHAR(500),
    FOREIGN KEY (local_id) REFERENCES locales(id) ON DELETE CASCADE
);

-- Tabla: ingredientes
-- Ingredientes disponibles en cada local
CREATE TABLE ingredientes (
    id BIGSERIAL PRIMARY KEY,
    local_id BIGINT NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    stock_actual DECIMAL(10, 2) NOT NULL,
    unidad_medida VARCHAR(20) NOT NULL,
    FOREIGN KEY (local_id) REFERENCES locales(id) ON DELETE CASCADE
);

-- Tabla: productos
-- Productos del menú de cada local
CREATE TABLE productos (
    id BIGSERIAL PRIMARY KEY,
    local_id BIGINT NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    descripcion VARCHAR(500),
    esta_agotado BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (local_id) REFERENCES locales(id) ON DELETE CASCADE
);

-- Tabla: recetas
-- Relación entre productos e ingredientes (join table)
CREATE TABLE recetas (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    ingrediente_id BIGINT NOT NULL,
    cantidad_requerida DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,
    FOREIGN KEY (ingrediente_id) REFERENCES ingredientes(id) ON DELETE CASCADE
);

-- Tabla: clientes
-- Clientes identificados por teléfono
CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    telefono VARCHAR(20) UNIQUE NOT NULL
);

-- Tabla: mesas
-- Mesas disponibles en cada local (para reservas)
CREATE TABLE mesas (
    id BIGSERIAL PRIMARY KEY,
    local_id BIGINT NOT NULL,
    numero INTEGER NOT NULL,
    capacidad INTEGER NOT NULL,
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (local_id) REFERENCES locales(id) ON DELETE CASCADE,
    UNIQUE (local_id, numero)
);

-- Tabla: reservas
-- Reservas de mesas realizadas por clientes
CREATE TABLE reservas (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    hora_reserva TIMESTAMP NOT NULL,
    numero_personas INTEGER NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADA',
    gasto_total DECIMAL(10, 2) DEFAULT 0.00,
    observaciones VARCHAR(500),
    request_id VARCHAR(255) UNIQUE NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
);

-- Tabla: reserva_mesas
-- Relación muchos a muchos entre reservas y mesas (para unión de mesas)
CREATE TABLE reserva_mesas (
    reserva_id BIGINT NOT NULL,
    mesa_id BIGINT NOT NULL,
    PRIMARY KEY (reserva_id, mesa_id),
    FOREIGN KEY (reserva_id) REFERENCES reservas(id) ON DELETE CASCADE,
    FOREIGN KEY (mesa_id) REFERENCES mesas(id) ON DELETE CASCADE
);

-- Tabla: pedidos
-- Pedidos realizados por clientes
CREATE TABLE pedidos (
    id BIGSERIAL PRIMARY KEY,
    local_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    modalidad VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    medio_pago VARCHAR(20) NOT NULL,
    estado_pago VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    origen_pedido VARCHAR(20) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    direccion_envio VARCHAR(500),
    hora_pedido TIMESTAMP NOT NULL,
    request_id VARCHAR(255) UNIQUE NOT NULL,
    FOREIGN KEY (local_id) REFERENCES locales(id) ON DELETE CASCADE,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
);

-- Tabla: pedido_items
-- Items individuales de cada pedido
CREATE TABLE pedido_items (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL,
    observaciones VARCHAR(500),
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE
);

-- Tabla: usuarios
-- Usuarios del sistema (empleados del local)
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    local_id BIGINT NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    FOREIGN KEY (local_id) REFERENCES locales(id) ON DELETE CASCADE
);

-- ========================================
-- ÍNDICES para mejorar performance
-- ========================================

-- Índices para pedidos
CREATE INDEX idx_pedidos_local_estado ON pedidos(local_id, estado);
CREATE INDEX idx_pedidos_hora ON pedidos(hora_pedido);
CREATE INDEX idx_pedidos_request_id ON pedidos(request_id);

-- Índices para reservas
CREATE INDEX idx_reservas_hora ON reservas(hora_reserva);
CREATE INDEX idx_reservas_estado ON reservas(estado);
CREATE INDEX idx_reservas_request_id ON reservas(request_id);

-- Índices para ingredientes y productos
CREATE INDEX idx_ingredientes_local ON ingredientes(local_id);
CREATE INDEX idx_productos_local ON productos(local_id);
CREATE INDEX idx_productos_agotado ON productos(esta_agotado);

-- Índices para mesas
CREATE INDEX idx_mesas_local ON mesas(local_id);
CREATE INDEX idx_mesas_disponible ON mesas(disponible);

-- Índices para usuarios
CREATE INDEX idx_usuarios_local ON usuarios(local_id);
CREATE INDEX idx_usuarios_username ON usuarios(username);

-- ========================================
-- Comentarios descriptivos
-- ========================================

COMMENT ON TABLE locales IS 'Locales de la red Dio Burger';
COMMENT ON COLUMN locales.telefono IS 'Teléfono único que actúa como Multi-Tenant ID';

COMMENT ON TABLE configuracion_local IS 'Configuración operativa de cada local';
COMMENT ON COLUMN configuracion_local.permite_delivery IS 'Indica si el local acepta pedidos delivery';
COMMENT ON COLUMN configuracion_local.permite_take_away IS 'Indica si el local acepta pedidos para retirar';
COMMENT ON COLUMN configuracion_local.permite_reservas IS 'Indica si el local acepta reservas de mesas';

COMMENT ON TABLE ingredientes IS 'Ingredientes disponibles en cada local';
COMMENT ON COLUMN ingredientes.stock_actual IS 'Stock actual del ingrediente';

COMMENT ON TABLE productos IS 'Productos del menú de cada local';
COMMENT ON COLUMN productos.esta_agotado IS 'Indica si el producto está agotado por falta de ingredientes';

COMMENT ON TABLE recetas IS 'Recetas: relación entre productos e ingredientes';
COMMENT ON COLUMN recetas.cantidad_requerida IS 'Cantidad del ingrediente necesaria para el producto';

COMMENT ON TABLE pedidos IS 'Pedidos realizados por clientes';
COMMENT ON COLUMN pedidos.request_id IS 'ID único para idempotencia (evita duplicados)';
COMMENT ON COLUMN pedidos.origen_pedido IS 'Origen del pedido: BOT (WhatsApp) o LOCAL (panel web)';

COMMENT ON TABLE reservas IS 'Reservas de mesas realizadas por clientes';
COMMENT ON COLUMN reservas.request_id IS 'ID único para idempotencia (evita duplicados)';
COMMENT ON COLUMN reservas.gasto_total IS 'Gasto total registrado al finalizar la reserva';

COMMENT ON TABLE reserva_mesas IS 'Relación muchos a muchos entre reservas y mesas (permite unir mesas)';
