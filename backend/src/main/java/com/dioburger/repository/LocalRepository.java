package com.dioburger.repository;

import com.dioburger.model.entity.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Local.
 * Proporciona operaciones CRUD y consultas personalizadas para locales.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Repository
public interface LocalRepository extends JpaRepository<Local, Long> {
    
    /**
     * Busca un local por su teléfono (Multi-Tenant ID).
     * 
     * @param telefono teléfono del local
     * @return Optional con el local si existe
     */
    Optional<Local> findByTelefono(String telefono);
    
    /**
     * Verifica si existe un local con el teléfono dado.
     * 
     * @param telefono teléfono del local
     * @return true si existe, false en caso contrario
     */
    boolean existsByTelefono(String telefono);

    /**
     * Busca un local por teléfono trayendo también su configuración con fetch join.
     * Necesario para código que corre fuera del ciclo de vida de una request web
     * (ej. el procesamiento en segundo plano de los webhooks de Meta), donde ya
     * no hay una sesión de Hibernate abierta para resolver la relación lazy.
     *
     * @param telefono teléfono del local
     * @return Optional con el local y su configuración ya inicializada
     */
    @Query("SELECT l FROM Local l LEFT JOIN FETCH l.configuracion WHERE l.telefono = :telefono")
    Optional<Local> findByTelefonoConConfiguracion(@Param("telefono") String telefono);
}
