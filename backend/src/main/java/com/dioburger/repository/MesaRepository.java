package com.dioburger.repository;

import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Mesa.
 * Proporciona operaciones CRUD y consultas personalizadas para mesas.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {
    
    /**
     * Busca todas las mesas de un local.
     * 
     * @param local local del que se buscan las mesas
     * @return lista de mesas del local
     */
    List<Mesa> findByLocal(Local local);
    
    /**
     * Busca todas las mesas disponibles de un local.
     * 
     * @param local local del que se buscan las mesas
     * @param disponible estado de disponibilidad
     * @return lista de mesas disponibles
     */
    List<Mesa> findByLocalAndDisponible(Local local, Boolean disponible);
    
    /**
     * Busca todas las mesas disponibles (true) de un local.
     * 
     * @param local local del que se buscan las mesas
     * @return lista de mesas disponibles
     */
    List<Mesa> findByLocalAndDisponibleTrue(Local local);
    
    /**
     * Busca una mesa por número en un local específico.
     * 
     * @param numero número de la mesa
     * @param local local de la mesa
     * @return Optional con la mesa si existe
     */
    Optional<Mesa> findByNumeroAndLocal(Integer numero, Local local);
    
    /**
     * Busca mesas disponibles en un horario específico.
     * Una mesa está disponible si no tiene reservas activas en ese horario.
     * 
     * @param local local de las mesas
     * @param horaInicio hora de inicio del intervalo
     * @param horaFin hora de fin del intervalo
     * @return lista de mesas disponibles en el horario
     */
    @Query("SELECT m FROM Mesa m WHERE m.local = :local " +
           "AND m.id NOT IN (" +
           "  SELECT rm.id FROM Reserva r " +
           "  JOIN r.mesas rm " +
           "  WHERE r.horaReserva BETWEEN :horaInicio AND :horaFin " +
           "  AND r.estado IN ('CONFIRMADA', 'OCUPADA')" +
           ")")
    List<Mesa> findMesasDisponiblesEnHorario(
        @Param("local") Local local,
        @Param("horaInicio") LocalDateTime horaInicio,
        @Param("horaFin") LocalDateTime horaFin
    );
}
