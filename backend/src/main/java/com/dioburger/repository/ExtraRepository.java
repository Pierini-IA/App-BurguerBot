package com.dioburger.repository;

import com.dioburger.model.entity.Categoria;
import com.dioburger.model.entity.Extra;
import com.dioburger.model.entity.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Extra.
 * Proporciona operaciones CRUD y consultas personalizadas para extras.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Repository
public interface ExtraRepository extends JpaRepository<Extra, Long> {

    /**
     * Busca todos los extras de un local.
     * 
     * @param local El local
     * @return Lista de extras del local
     */
    List<Extra> findByLocal(Local local);

    /**
     * Busca todos los extras activos de un local.
     * 
     * @param local El local
     * @param activo Estado del extra
     * @return Lista de extras activos
     */
    List<Extra> findByLocalAndActivo(Local local, Boolean activo);

    /**
     * Busca extras por categoría.
     * 
     * @param categoria La categoría
     * @return Lista de extras de la categoría
     */
    List<Extra> findByCategoria(Categoria categoria);

    /**
     * Busca extras activos por categoría.
     * 
     * @param categoria La categoría
     * @param activo Estado del extra
     * @return Lista de extras activos de la categoría
     */
    List<Extra> findByCategoriaAndActivo(Categoria categoria, Boolean activo);

    /**
     * Busca un extra por nombre y local.
     * Útil para validar duplicados.
     * 
     * @param nombre Nombre del extra
     * @param local El local
     * @return Optional con el extra si existe
     */
    Optional<Extra> findByNombreAndLocal(String nombre, Local local);

    /**
     * Busca extras por local ID.
     * 
     * @param localId ID del local
     * @return Lista de extras del local
     */
    List<Extra> findByLocal_Id(Long localId);

    /**
     * Busca extras activos por local ID.
     * 
     * @param localId ID del local
     * @param activo Estado del extra
     * @return Lista de extras activos
     */
    List<Extra> findByLocal_IdAndActivo(Long localId, Boolean activo);

    /**
     * Busca extras por categoría ID.
     * 
     * @param categoriaId ID de la categoría
     * @return Lista de extras de la categoría
     */
    List<Extra> findByCategoria_Id(Long categoriaId);

    /**
     * Verifica si existe un extra con el nombre dado en el local.
     * 
     * @param nombre Nombre del extra
     * @param local El local
     * @return true si existe, false caso contrario
     */
    boolean existsByNombreAndLocal(String nombre, Local local);

    /**
     * Cuenta los extras de un local.
     * 
     * @param local El local
     * @return Cantidad de extras
     */
    long countByLocal(Local local);

    /**
     * Busca extras disponibles para un producto específico.
     * Retorna los extras que están asociados al producto en la tabla producto_extras.
     * 
     * @param productoId ID del producto
     * @return Lista de extras disponibles para el producto
     */
    @Query("SELECT e FROM Extra e JOIN e.productoExtras pe JOIN pe.producto p WHERE p.id = :productoId AND e.activo = true")
    List<Extra> findExtrasDisponiblesParaProducto(@Param("productoId") Long productoId);

    /**
     * Busca extras obligatorios para un producto específico.
     * 
     * @param productoId ID del producto
     * @return Lista de extras obligatorios
     */
    @Query("SELECT e FROM Extra e " +
           "JOIN ProductoExtra pe ON e.id = pe.extra.id " +
           "WHERE pe.producto.id = :productoId " +
           "AND pe.esObligatorio = true " +
           "AND e.activo = true")
    List<Extra> findExtrasObligatoriosParaProducto(@Param("productoId") Long productoId);

    /**
     * Elimina todos los extras de un local.
     * 
     * @param local El local
     */
    void deleteByLocal(Local local);

    /**
     * Elimina todos los extras de una categoría.
     * 
     * @param categoria La categoría
     */
    void deleteByCategoria(Categoria categoria);
}
