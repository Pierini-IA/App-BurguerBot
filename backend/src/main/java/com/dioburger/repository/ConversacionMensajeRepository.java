package com.dioburger.repository;

import com.dioburger.model.entity.ConversacionMensaje;
import com.dioburger.model.entity.Local;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio del historial de conversación del bot.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Repository
public interface ConversacionMensajeRepository extends JpaRepository<ConversacionMensaje, Long> {

    /**
     * Trae los últimos mensajes de un cliente en un local, del más reciente al
     * más viejo. La cantidad la limita el {@code Pageable} que recibe.
     *
     * Viene al revés de como se lee: quien lo llama lo da vuelta antes de
     * armar el prompt. Se hace así para que el límite recorte los mensajes
     * viejos y no los nuevos.
     *
     * @param local  local dueño de la conversación
     * @param remitente identificador del cliente en el canal
     * @param desde  piso de antigüedad: más viejo que esto no se trae
     * @param limite cuántos mensajes traer
     * @return mensajes del más reciente al más viejo
     */
    @Query("""
            SELECT m FROM ConversacionMensaje m
            WHERE m.local = :local
              AND m.remitente = :remitente
              AND m.creadoEn >= :desde
            ORDER BY m.creadoEn DESC, m.id DESC
            """)
    List<ConversacionMensaje> buscarUltimos(
            @Param("local") Local local,
            @Param("remitente") String remitente,
            @Param("desde") LocalDateTime desde,
            Pageable limite);
}
