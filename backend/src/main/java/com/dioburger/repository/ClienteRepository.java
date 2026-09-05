package com.dioburger.repository;

import com.dioburger.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Cliente.
 * Proporciona operaciones CRUD y consultas personalizadas para clientes.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    /**
     * Busca un cliente por su teléfono.
     * 
     * @param telefono teléfono del cliente
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByTelefono(String telefono);
    
    /**
     * Verifica si existe un cliente con el teléfono dado.
     * 
     * @param telefono teléfono del cliente
     * @return true si existe, false en caso contrario
     */
    boolean existsByTelefono(String telefono);
}
