package com.dioburger.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para reportes de ventas.
 * Contiene información agregada de ventas por período.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteVentasDTO {

    /**
     * Fecha del reporte.
     */
    private LocalDate fecha;

    /**
     * Descripción del período (ej: "Semana 1", "Enero", "Lunes 21/10").
     */
    private String periodo;

    /**
     * Cantidad total de pedidos en el período.
     */
    private Long cantidadPedidos;

    /**
     * Total de ventas en el período.
     */
    private BigDecimal totalVentas;

    /**
     * Ticket promedio (total / cantidad).
     */
    private BigDecimal promedioTicket;
}
