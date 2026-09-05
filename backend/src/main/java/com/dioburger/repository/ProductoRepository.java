package com.dioburger.repository;

import com.dioburger.model.entity.Categoria;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Producto.
 * Proporciona operaciones CRUD y consultas personalizadas para productos.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    /**
     * Busca todos los productos de un local.
     * 
     * @param local local del que se buscan los productos
     * @return lista de productos del local
     */
    List<Producto> findByLocal(Local local);
    
    /**
     * Busca todos los productos disponibles (no agotados) de un local.
     * 
     * @param local local del que se buscan los productos
     * @param estaAgotado estado de agotamiento
     * @return lista de productos disponibles
     */
    List<Producto> findByLocalAndEstaAgotado(Local local, Boolean estaAgotado);
    
    /**
     * Busca un producto por ID y local (para validación Multi-Tenant).
     * 
     * @param id ID del producto
     * @param local local del producto
     * @return Optional con el producto si existe
     */
    Optional<Producto> findByIdAndLocal(Long id, Local local);
    
    /**
     * Busca un producto por nombre en un local específico.
     * 
     * @param nombre nombre del producto
     * @param local local del producto
     * @return Optional con el producto si existe
     */
    Optional<Producto> findByNombreAndLocal(String nombre, Local local);
    
    /**
     * Busca todos los productos disponibles (no agotados) de una categoría específica.
     * 
     * @param categoria categoría de los productos
     * @return lista de productos disponibles de la categoría
     */
    List<Producto> findByCategoriaAndEstaAgotadoFalse(Categoria categoria);
    
    /**
     * Busca todos los productos de una categoría específica.
     * 
     * @param categoria categoría de los productos
     * @return lista de productos de la categoría
     */
    List<Producto> findByCategoria(Categoria categoria);
}
