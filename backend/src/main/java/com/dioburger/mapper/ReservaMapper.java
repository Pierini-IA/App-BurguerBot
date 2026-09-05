package com.dioburger.mapper;

import com.dioburger.model.dto.MesaDTO;
import com.dioburger.model.dto.ReservaResponseDTO;
import com.dioburger.model.entity.Mesa;
import com.dioburger.model.entity.Reserva;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entidades Reserva a DTOs.
 * Utiliza MapStruct para generar implementaciones automáticas.
 * 
 * @author Dio Burger Team
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface ReservaMapper {

    /**
     * Convierte una entidad Reserva a ReservaResponseDTO.
     * 
     * @param reserva Entidad Reserva
     * @return ReservaResponseDTO con toda la información de la reserva
     */
    @Mapping(target = "mesas", source = "mesas", qualifiedByName = "mesasToDTO")
    @Mapping(target = "fechaCreacion", ignore = true)
    ReservaResponseDTO toResponseDTO(Reserva reserva);

    /**
     * Convierte un Set de mesas a Set de MesaDTO.
     */
    @Named("mesasToDTO")
    default Set<MesaDTO> mesasToDTO(Set<Mesa> mesas) {
        return mesas.stream()
                .map(this::mesaToDTO)
                .collect(Collectors.toSet());
    }

    /**
     * Convierte una entidad Mesa a MesaDTO.
     * 
     * @param mesa Entidad Mesa
     * @return MesaDTO con información básica de la mesa
     */
    MesaDTO mesaToDTO(Mesa mesa);
}
