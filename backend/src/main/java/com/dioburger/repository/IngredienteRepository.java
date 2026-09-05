package com.dioburger.repository;

import com.dioburger.model.entity.Ingrediente;
import com.dioburger.model.entity.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Ingrediente.
 * Proporciona operaciones CRUD y consultas personalizadas para ingredientes.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Repository
public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {
    
    /**
     * Busca todos los ingredientes de un local.
     * 
     * @param local local del que se buscan los ingredientes
     * @return lista de ingredientes del local
     */
    List<Ingrediente> findByLocal(Local local);
    
    /**
     * Busca un ingrediente por nombre en un local específico.
     * 
     * @param nombre nombre del ingrediente
     * @param local local del ingrediente
     * @return Optional con el ingrediente si existe
     */
    Optional<Ingrediente> findByNombreAndLocal(String nombre, Local local);
}
