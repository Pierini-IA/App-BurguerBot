package com.dioburger.mapper;

import com.dioburger.model.dto.ExtraDTO;
import com.dioburger.model.dto.ProductoDTO;
import com.dioburger.model.entity.Extra;
import com.dioburger.model.entity.Producto;
import com.dioburger.model.entity.ProductoExtra;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper de MapStruct para convertir entre entidades Producto y ProductoDTO.
 * MapStruct genera automáticamente la implementación en tiempo de compilación.
 *
 * @author Dio Burger Team
 * @version 2.0.0
 */
@Mapper(componentModel = "spring")
public interface ProductoMapper {

    /**
     * Convierte una entidad Producto a ProductoDTO.
     *
     * @param producto Entidad JPA del producto
     * @return DTO del producto
     */
    @Mapping(target = "disponible", source = "estaAgotado", qualifiedByName = "invertirAgotado")
    @Mapping(target = "localId", source = "local.id")
    @Mapping(target = "localNombre", source = "local.nombre")
    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    @Mapping(target = "extrasDisponibles", source = "extras", qualifiedByName = "mapExtras")
    @Mapping(target = "promocion", source = "producto", qualifiedByName = "mapPromocion")
    ProductoDTO toDTO(Producto producto);

    /**
     * Método personalizado para invertir el campo estaAgotado.
     * Si estaAgotado = true, entonces disponible = false.
     *
     * @param estaAgotado Estado de agotamiento del producto
     * @return Estado de disponibilidad (inverso)
     */
    @Named("invertirAgotado")
    default Boolean invertirAgotado(Boolean estaAgotado) {
        return estaAgotado == null ? true : !estaAgotado;
    }

    /**
     * Mapea la lista de ProductoExtra a una lista de ExtraDTO.
     *
     * @param productoExtras Lista de relaciones Producto-Extra
     * @return Lista de ExtraDTO con información básica
     */
    @Named("mapExtras")
    default List<ExtraDTO> mapExtras(List<ProductoExtra> productoExtras) {
        if (productoExtras == null) {
            return List.of();
        }
        return productoExtras.stream()
            .map(pe -> {
                Extra extra = pe.getExtra();
                return ExtraDTO.builder()
                    .id(extra.getId())
                    .nombre(extra.getNombre())
                    .descripcion(extra.getDescripcion())
                    .precioAdicional(extra.getPrecioAdicional())
                    .activo(extra.getActivo())
                    .esObligatorio(pe.getEsObligatorio())
                    .categoriaId(extra.getCategoria() != null ? extra.getCategoria().getId() : null)
                    .categoriaNombre(extra.getCategoria() != null ? extra.getCategoria().getNombre() : null)
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Mapea la información de promoción del producto.
     *
     * @param producto Entidad del producto
     * @return DTO con información de promoción o null
     */
    @Named("mapPromocion")
    default ProductoDTO.PromocionInfoDTO mapPromocion(Producto producto) {
        if (producto.getTienePromocion() == null || !producto.getTienePromocion()) {
            return null;
        }
        return ProductoDTO.PromocionInfoDTO.builder()
            .horaInicio(producto.getHoraInicioPromo() != null ? producto.getHoraInicioPromo().toString() : null)
            .horaFin(producto.getHoraFinPromo() != null ? producto.getHoraFinPromo().toString() : null)
            .diasPromocion(producto.getDiasPromocion())
            .build();
    }
}
