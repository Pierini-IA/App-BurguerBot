-- Script para reparar el historial de migraciones de Flyway
-- Ejecuta este script en tu base de datos 'postgres' usando pgAdmin, DBeaver o cualquier cliente SQL

-- 1. Eliminar la entrada fallida de la migración V5
DELETE FROM flyway_schema_history 
WHERE version = '5' AND success = false;

-- 2. Verificar que se eliminó correctamente
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
