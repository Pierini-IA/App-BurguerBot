package com.dioburger.repository;

import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Usuario.
 * Proporciona operaciones CRUD y consultas personalizadas para usuarios.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    /**
     * Busca un usuario por su nombre de usuario.
     * 
     * @param username nombre de usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByUsername(String username);
    
    /**
     * Busca todos los usuarios de un local.
     * 
     * @param local local de los usuarios
     * @return lista de usuarios del local
     */
    List<Usuario> findByLocal(Local local);
    
    /**
     * Verifica si existe un usuario con el nombre dado.
     * 
     * @param username nombre de usuario
     * @return true si existe, false en caso contrario
     */
    boolean existsByUsername(String username);
}
