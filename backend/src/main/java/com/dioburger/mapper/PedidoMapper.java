package com.dioburger.mapper;

import com.dioburger.model.dto.PedidoResponseDTO;
import com.dioburger.model.entity.Pedido;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper de MapStruct para convertir entre entidades Pedido y DTOs.
 * MapStruct genera automáticamente la implementación en tiempo de compilación.
 *
 * @author Dio Burger Team
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface PedidoMapper {

    /**
     * Convierte una entidad Pedido a PedidoResponseDTO.
     *
     * @param pedido Entidad JPA del pedido
     * @return DTO de respuesta del pedido
     */
    @Mapping(target = "estado", expression = "java(pedido.getEstado().name())")
    @Mapping(target = "modalidad", expression = "java(pedido.getModalidad().name())")
    @Mapping(target = "medioPago", expression = "java(pedido.getMedioPago().name())")
    @Mapping(target = "estadoPago", expression = "java(pedido.getEstadoPago().name())")
    PedidoResponseDTO toResponseDTO(Pedido pedido);
}
