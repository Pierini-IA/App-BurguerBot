package com.dioburger.repository;

import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Pedido;
import com.dioburger.model.enums.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Pedido.
 * Proporciona operaciones CRUD y consultas personalizadas para pedidos.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    /**
     * Busca un pedido por su request ID (idempotencia).
     * 
     * @param requestId ID de la petición
     * @return Optional con el pedido si existe
     */
    Optional<Pedido> findByRequestId(String requestId);
    
    /**
     * Busca todos los pedidos de un local.
     * 
     * @param local local de los pedidos
     * @return lista de pedidos del local
     */
    List<Pedido> findByLocal(Local local);
    
    /**
     * Busca todos los pedidos de un local en un estado específico.
     * 
     * @param local local de los pedidos
     * @param estado estado de los pedidos
     * @return lista de pedidos en el estado
     */
    List<Pedido> findByLocalAndEstado(Local local, EstadoPedido estado);
    
    /**
     * Busca todos los pedidos de un local en múltiples estados.
     * Útil para la vista de cocina (PENDIENTE, EN_PREPARACION).
     * 
     * @param local local de los pedidos
     * @param estados lista de estados
     * @return lista de pedidos en los estados
     */
    List<Pedido> findByLocalAndEstadoIn(Local local, List<EstadoPedido> estados);
    
    /**
     * Cuenta los pedidos en un intervalo de tiempo específico.
     * Útil para validar si hay capacidad en un slot de horario.
     * 
     * @param local local de los pedidos
     * @param horaInicio hora de inicio del intervalo
     * @param horaFin hora de fin del intervalo
     * @return número de pedidos en el intervalo
     */
    @Query("SELECT COUNT(p) FROM Pedido p " +
           "WHERE p.local = :local " +
           "AND p.horaPedido BETWEEN :horaInicio AND :horaFin " +
           "AND p.estado NOT IN ('CANCELADO', 'ENTREGADO')")
    Long countPedidosEnIntervalo(
        @Param("local") Local local,
        @Param("horaInicio") LocalDateTime horaInicio,
        @Param("horaFin") LocalDateTime horaFin
    );
    
    /**
     * Busca pedidos de un local en un rango de fechas.
     * 
     * @param local local de los pedidos
     * @param inicio fecha y hora de inicio
     * @param fin fecha y hora de fin
     * @return lista de pedidos en el rango
     */
    List<Pedido> findByLocalAndHoraPedidoBetween(
        Local local,
        LocalDateTime inicio,
        LocalDateTime fin
    );
    
    /**
     * Busca todos los pedidos de un local ordenados por hora descendente.
     *
     * @param local local de los pedidos
     * @return lista de pedidos ordenados
     */
    List<Pedido> findByLocalOrderByHoraPedidoDesc(Local local);

    /**
     * Busca los pedidos de un cliente (por teléfono) en un local, del más reciente al más antiguo.
     * Usado por el bot para responder "¿en qué estado está mi pedido?".
     *
     * @param local local de los pedidos
     * @param telefonoCliente teléfono del cliente
     * @return lista de pedidos del cliente ordenados por hora descendente
     */
    List<Pedido> findByLocalAndCliente_TelefonoOrderByHoraPedidoDesc(Local local, String telefonoCliente);
}
