-- ========================================
-- V6: Permitir local_id NULL en usuarios
-- ========================================
-- Razón: El SUPERADMIN no tiene local asociado
-- Fecha: 2025-10-23
-- ========================================

-- Modificar columna local_id para permitir valores NULL
ALTER TABLE usuarios 
    ALTER COLUMN local_id DROP NOT NULL;

-- Modificar foreign key para permitir NULL
-- Primero eliminamos la constraint existente
ALTER TABLE usuarios 
    DROP CONSTRAINT IF EXISTS usuarios_local_id_fkey;

-- Recreamos la constraint sin NOT NULL
ALTER TABLE usuarios 
    ADD CONSTRAINT usuarios_local_id_fkey 
    FOREIGN KEY (local_id) 
    REFERENCES locales(id) 
    ON DELETE CASCADE;

-- Comentario para documentar
COMMENT ON COLUMN usuarios.local_id IS 'Local asociado al usuario. NULL para SUPERADMIN sin local específico';
