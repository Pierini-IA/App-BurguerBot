package com.dioburger.repository;

import com.dioburger.model.entity.Categoria;
import com.dioburger.model.entity.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Categoria.
 * Proporciona operaciones CRUD y consultas personalizadas para categorías.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Busca todas las categorías de un local específico.
     * 
     * @param local El local
     * @return Lista de categorías del local
     */
    List<Categoria> findByLocalOrderByOrdenAsc(Local local);

    /**
     * Busca todas las categorías activas de un local.
     * 
     * @param local El local
     * @param activo Estado de la categoría
     * @return Lista de categorías activas ordenadas por orden
     */
    List<Categoria> findByLocalAndActivoOrderByOrdenAsc(Local local, Boolean activo);

    /**
     * Busca una categoría por nombre y local.
     * Útil para validar duplicados.
     * 
     * @param nombre Nombre de la categoría
     * @param local El local
     * @return Optional con la categoría si existe
     */
    Optional<Categoria> findByNombreAndLocal(String nombre, Local local);

    /**
     * Busca categorías por local ID.
     * 
     * @param localId ID del local
     * @return Lista de categorías del local
     */
    List<Categoria> findByLocal_IdOrderByOrdenAsc(Long localId);

    /**
     * Busca categorías activas por local ID.
     * 
     * @param localId ID del local
     * @param activo Estado de la categoría
     * @return Lista de categorías activas
     */
    List<Categoria> findByLocal_IdAndActivoOrderByOrdenAsc(Long localId, Boolean activo);

    /**
     * Verifica si existe una categoría con el nombre dado en el local.
     * 
     * @param nombre Nombre de la categoría
     * @param local El local
     * @return true si existe, false caso contrario
     */
    boolean existsByNombreAndLocal(String nombre, Local local);

    /**
     * Cuenta las categorías de un local.
     * 
     * @param local El local
     * @return Cantidad de categorías
     */
    long countByLocal(Local local);

    /**
     * Elimina todas las categorías de un local.
     * 
     * @param local El local
     */
    void deleteByLocal(Local local);
}
