-- ========================================
-- V7: Agregar Categorías y Extras
-- ========================================
-- Descripción: Añade soporte para categorías de productos y extras/adicionales.
--              Permite que cada local gestione diferentes tipos de productos
--              (hamburguesas, pizzas, bebidas, etc.) y extras personalizables.
-- Autor: Dio Burger Team
-- Fecha: 2025-10-23
-- ========================================

-- ========================================
-- TABLA: categorias
-- ========================================
-- Almacena las categorías de productos por local
-- Ejemplos: Hamburguesas, Pizzas, Bebidas, Postres, Extras
CREATE TABLE categorias (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    local_id BIGINT NOT NULL,
    orden INTEGER DEFAULT 0,
    activo BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_categorias_local FOREIGN KEY (local_id) 
        REFERENCES locales(id) ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT uk_categoria_nombre_local UNIQUE (nombre, local_id)
);

-- Comentarios de columnas
COMMENT ON TABLE categorias IS 'Categorías de productos por local para organizar el menú';
COMMENT ON COLUMN categorias.id IS 'Identificador único de la categoría';
COMMENT ON COLUMN categorias.nombre IS 'Nombre de la categoría (ej: Hamburguesas, Pizzas)';
COMMENT ON COLUMN categorias.descripcion IS 'Descripción opcional de la categoría';
COMMENT ON COLUMN categorias.local_id IS 'Local al que pertenece esta categoría';
COMMENT ON COLUMN categorias.orden IS 'Orden de visualización en el menú';
COMMENT ON COLUMN categorias.activo IS 'Indica si la categoría está activa y visible';

-- Índices para performance
CREATE INDEX idx_categorias_local ON categorias(local_id);
CREATE INDEX idx_categorias_activo ON categorias(activo);
CREATE INDEX idx_categorias_orden ON categorias(orden);

-- ========================================
-- TABLA: extras
-- ========================================
-- Almacena los extras/adicionales disponibles por local
-- Ejemplos: Queso extra, Bacon, Papas fritas, Aceitunas
CREATE TABLE extras (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio_adicional DECIMAL(10,2) NOT NULL CHECK (precio_adicional >= 0),
    local_id BIGINT NOT NULL,
    categoria_id BIGINT,
    activo BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_extras_local FOREIGN KEY (local_id) 
        REFERENCES locales(id) ON DELETE CASCADE,
    CONSTRAINT fk_extras_categoria FOREIGN KEY (categoria_id) 
        REFERENCES categorias(id) ON DELETE SET NULL,
    
    -- Constraints
    CONSTRAINT uk_extra_nombre_local UNIQUE (nombre, local_id)
);

-- Comentarios de columnas
COMMENT ON TABLE extras IS 'Extras y adicionales disponibles para productos';
COMMENT ON COLUMN extras.id IS 'Identificador único del extra';
COMMENT ON COLUMN extras.nombre IS 'Nombre del extra (ej: Queso cheddar, Bacon)';
COMMENT ON COLUMN extras.descripcion IS 'Descripción opcional del extra';
COMMENT ON COLUMN extras.precio_adicional IS 'Precio adicional que se suma al producto base';
COMMENT ON COLUMN extras.local_id IS 'Local al que pertenece este extra';
COMMENT ON COLUMN extras.categoria_id IS 'Categoría opcional para agrupar extras';
COMMENT ON COLUMN extras.activo IS 'Indica si el extra está disponible';

-- Índices para performance
CREATE INDEX idx_extras_local ON extras(local_id);
CREATE INDEX idx_extras_categoria ON extras(categoria_id);
CREATE INDEX idx_extras_activo ON extras(activo);

-- ========================================
-- TABLA: producto_extras
-- ========================================
-- Relación muchos a muchos entre productos y extras
-- Define qué extras están disponibles para cada producto
CREATE TABLE producto_extras (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    extra_id BIGINT NOT NULL,
    es_obligatorio BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_producto_extras_producto FOREIGN KEY (producto_id) 
        REFERENCES productos(id) ON DELETE CASCADE,
    CONSTRAINT fk_producto_extras_extra FOREIGN KEY (extra_id) 
        REFERENCES extras(id) ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT uk_producto_extra UNIQUE (producto_id, extra_id)
);

-- Comentarios de columnas
COMMENT ON TABLE producto_extras IS 'Relación entre productos y sus extras disponibles';
COMMENT ON COLUMN producto_extras.id IS 'Identificador único de la relación';
COMMENT ON COLUMN producto_extras.producto_id IS 'Producto al que se puede agregar el extra';
COMMENT ON COLUMN producto_extras.extra_id IS 'Extra disponible para el producto';
COMMENT ON COLUMN producto_extras.es_obligatorio IS 'Indica si el extra es obligatorio para el producto';

-- Índices para performance
CREATE INDEX idx_producto_extras_producto ON producto_extras(producto_id);
CREATE INDEX idx_producto_extras_extra ON producto_extras(extra_id);

-- ========================================
-- TABLA: pedido_item_extras
-- ========================================
-- Almacena los extras seleccionados para cada item de pedido
CREATE TABLE pedido_item_extras (
    pedido_item_id BIGINT NOT NULL,
    extra_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_pedido_item_extras_item FOREIGN KEY (pedido_item_id) 
        REFERENCES pedido_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_pedido_item_extras_extra FOREIGN KEY (extra_id) 
        REFERENCES extras(id) ON DELETE RESTRICT,
    
    -- Primary Key compuesta
    PRIMARY KEY (pedido_item_id, extra_id)
);

-- Comentarios de columnas
COMMENT ON TABLE pedido_item_extras IS 'Extras seleccionados para cada item de pedido';
COMMENT ON COLUMN pedido_item_extras.pedido_item_id IS 'Item del pedido';
COMMENT ON COLUMN pedido_item_extras.extra_id IS 'Extra seleccionado';

-- Índices para performance
CREATE INDEX idx_pedido_item_extras_item ON pedido_item_extras(pedido_item_id);
CREATE INDEX idx_pedido_item_extras_extra ON pedido_item_extras(extra_id);

-- ========================================
-- MODIFICAR TABLA: productos
-- ========================================
-- Agregar nuevas columnas para soportar categorías y extras

-- Agregar columna de categoría
ALTER TABLE productos 
ADD COLUMN categoria_id BIGINT,
ADD CONSTRAINT fk_productos_categoria FOREIGN KEY (categoria_id) 
    REFERENCES categorias(id) ON DELETE SET NULL;

-- Agregar columna para identificar si es un producto extra
ALTER TABLE productos 
ADD COLUMN es_extra BOOLEAN DEFAULT false;

-- Agregar columna para indicar si permite agregar extras
ALTER TABLE productos 
ADD COLUMN permite_extras BOOLEAN DEFAULT true;

-- Agregar columna para tipo de producto (SIMPLE o CON_RECETA)
ALTER TABLE productos 
ADD COLUMN tipo_producto VARCHAR(20) DEFAULT 'CON_RECETA'
    CHECK (tipo_producto IN ('SIMPLE', 'CON_RECETA'));

-- Comentarios de nuevas columnas
COMMENT ON COLUMN productos.categoria_id IS 'Categoría a la que pertenece el producto';
COMMENT ON COLUMN productos.es_extra IS 'Indica si este producto es en sí un extra/adicional';
COMMENT ON COLUMN productos.permite_extras IS 'Indica si se pueden agregar extras a este producto';
COMMENT ON COLUMN productos.tipo_producto IS 'Tipo de producto: SIMPLE (sin receta) o CON_RECETA (con ingredientes)';

-- Índice para categoria_id
CREATE INDEX idx_productos_categoria ON productos(categoria_id);

-- ========================================
-- NOTA: DATOS INICIALES
-- ========================================
-- Los datos iniciales (categorías, extras, productos) se cargan
-- mediante DataInitializer desde el archivo initial-data.json
-- No se crean aquí para evitar conflictos con locales inexistentes

-- ========================================
-- VERIFICACIÓN
-- ========================================
-- Consultas para verificar la migración

-- Verificar categorías creadas
-- SELECT l.nombre as local, c.nombre as categoria, c.orden 
-- FROM categorias c 
-- JOIN locales l ON c.local_id = l.id 
-- ORDER BY l.nombre, c.orden;

-- Verificar extras creados
-- SELECT l.nombre as local, e.nombre as extra, e.precio_adicional 
-- FROM extras e 
-- JOIN locales l ON e.local_id = l.id 
-- ORDER BY l.nombre, e.nombre;

-- Verificar productos con categoría
-- SELECT l.nombre as local, c.nombre as categoria, p.nombre as producto, p.tipo_producto 
-- FROM productos p 
-- JOIN locales l ON p.local_id = l.id 
-- LEFT JOIN categorias c ON p.categoria_id = c.id 
-- ORDER BY l.nombre, c.nombre, p.nombre;

-- Verificar relación producto-extras
-- SELECT l.nombre as local, p.nombre as producto, e.nombre as extra, pe.es_obligatorio
-- FROM producto_extras pe
-- JOIN productos p ON pe.producto_id = p.id
-- JOIN extras e ON pe.extra_id = e.id
-- JOIN locales l ON p.local_id = l.id
-- ORDER BY l.nombre, p.nombre, e.nombre;
