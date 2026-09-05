package com.dioburger.repository;

import com.dioburger.model.entity.Cliente;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Reserva;
import com.dioburger.model.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Reserva.
 * Proporciona operaciones CRUD y consultas personalizadas para reservas.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
    /**
     * Busca una reserva por su request ID (idempotencia).
     * 
     * @param requestId ID de la petición
     * @return Optional con la reserva si existe
     */
    Optional<Reserva> findByRequestId(String requestId);
    
    /**
     * Busca todas las reservas de un cliente.
     * 
     * @param cliente cliente de las reservas
     * @return lista de reservas del cliente
     */
    List<Reserva> findByCliente(Cliente cliente);
    
    /**
     * Busca todas las reservas en un estado específico.
     * 
     * @param estado estado de las reservas
     * @return lista de reservas en el estado
     */
    List<Reserva> findByEstado(EstadoReserva estado);
    
    /**
     * Busca todas las reservas en un rango de fechas.
     * 
     * @param inicio fecha y hora de inicio
     * @param fin fecha y hora de fin
     * @return lista de reservas en el rango
     */
    List<Reserva> findByHoraReservaBetween(LocalDateTime inicio, LocalDateTime fin);
    
    /**
     * Cuenta las reservas en un intervalo de tiempo específico.
     * Útil para validar si hay capacidad en un slot de horario.
     * 
     * @param horaInicio hora de inicio del intervalo
     * @param horaFin hora de fin del intervalo
     * @return número de reservas en el intervalo
     */
    @Query("SELECT COUNT(r) FROM Reserva r " +
           "WHERE r.horaReserva BETWEEN :horaInicio AND :horaFin " +
           "AND r.estado IN ('CONFIRMADA', 'OCUPADA')")
    Long countReservasEnIntervalo(
        @Param("horaInicio") LocalDateTime horaInicio,
        @Param("horaFin") LocalDateTime horaFin
    );
    
    /**
     * Busca reservas de un local en una fecha específica.
     * 
     * @param telefonoLocal teléfono del local
     * @param inicio inicio del día
     * @param fin fin del día
     * @return lista de reservas del día
     */
    @Query("SELECT r FROM Reserva r " +
           "JOIN r.mesas m " +
           "WHERE m.local.telefono = :telefonoLocal " +
           "AND r.horaReserva BETWEEN :inicio AND :fin " +
           "ORDER BY r.horaReserva ASC")
    List<Reserva> findReservasDelDia(
        @Param("telefonoLocal") String telefonoLocal,
        @Param("inicio") LocalDateTime inicio,
        @Param("fin") LocalDateTime fin
    );
    
    /**
     * Verifica si existe una reserva con el requestId dado.
     * 
     * @param requestId ID de la petición
     * @return true si existe, false en caso contrario
     */
    boolean existsByRequestId(String requestId);
    
    /**
     * Busca todas las reservas asociadas a un local específico.
     * 
     * @param local Entidad Local
     * @return Lista de reservas del local
     */
    @Query("SELECT r FROM Reserva r JOIN r.mesas m WHERE m.local = :local")
    List<Reserva> findByMesas_Local(@Param("local") Local local);
    
    /**
     * Busca reservas de un local en un rango de fechas.
     * 
     * @param local Entidad Local
     * @param desde Fecha inicial
     * @param hasta Fecha final
     * @return Lista de reservas en el rango
     */
    @Query("SELECT r FROM Reserva r JOIN r.mesas m " +
           "WHERE m.local = :local " +
           "AND r.horaReserva BETWEEN :desde AND :hasta " +
           "ORDER BY r.horaReserva ASC")
    List<Reserva> findByMesas_LocalAndHoraReservaBetween(
        @Param("local") Local local,
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta
    );
}
