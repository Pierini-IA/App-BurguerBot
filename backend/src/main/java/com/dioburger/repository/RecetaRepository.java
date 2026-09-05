package com.dioburger.repository;

import com.dioburger.model.entity.Producto;
import com.dioburger.model.entity.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Receta.
 * Proporciona operaciones CRUD y consultas personalizadas para recetas.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Repository
public interface RecetaRepository extends JpaRepository<Receta, Long> {
    
    /**
     * Busca todas las recetas (ingredientes) de un producto.
     * 
     * @param producto producto del que se buscan las recetas
     * @return lista de recetas del producto
     */
    List<Receta> findByProducto(Producto producto);
    
    /**
     * Elimina todas las recetas de un producto.
     * Útil cuando se actualiza completamente la receta de un producto.
     * 
     * @param producto producto del que se eliminan las recetas
     */
    void deleteByProducto(Producto producto);
}
