package com.dioburger.service;

import com.dioburger.exception.NotFoundException;
import com.dioburger.mapper.ProductoMapper;
import com.dioburger.model.dto.MenuResponseDTO;
import com.dioburger.model.dto.ProductoDTO;
import com.dioburger.model.entity.ConfiguracionLocal;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Producto;
import com.dioburger.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar el menú dinámico del local.
 * Integra la información de productos, stock, horarios y promociones.
 *
 * @author Dio Burger Team
 * @version 2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final ProductoRepository productoRepository;
    private final StockService stockService;
    private final HorarioService horarioService;
    private final ProductoMapper productoMapper;
    private final PromocionService promocionService;
    private final LocalService localService;

    /**
     * Obtiene el menú completo de un local incluyendo:
     * - Información del local
     * - Lista de productos con su disponibilidad actualizada
     * - Horarios disponibles para pedidos
     * - Modalidades permitidas
     * - Si acepta reservas
     *
     * @param telefonoLocal Teléfono único del local (Multi-Tenant ID)
     * @return DTO con toda la información del menú
     * @throws NotFoundException si el local no existe
     */
    public MenuResponseDTO obtenerMenuCompleto(String telefonoLocal) {
        log.info("Obteniendo menú para local: {}", telefonoLocal);

        // 1. Buscar el local
        Local local = localService.buscarPorTelefono(telefonoLocal);

        ConfiguracionLocal config = local.getConfiguracion();

        if (config == null) {
            log.error("El local {} no tiene configuración", local.getNombre());
            throw new NotFoundException("El local no tiene configuración");
        }

        // 2. Actualizar disponibilidad de todos los productos (basado en stock)
        stockService.actualizarDisponibilidadProductos(local);

        // 3. Obtener productos y convertirlos a DTOs
        List<Producto> productos = productoRepository.findByLocal(local);
        List<ProductoDTO> productoDTOs = productos.stream()
                .map(productoMapper::toDTO)
                .collect(Collectors.toList());

        log.info("Productos encontrados: {}", productoDTOs.size());

        // 4. Obtener horarios disponibles
        List<String> horariosSugeridos = horarioService.getHorariosSugeridosPedidos(telefonoLocal);

        // 5. Construir lista de modalidades permitidas
        List<String> modalidadesPermitidas = new ArrayList<>();
        if (config.getPermiteDelivery()) {
            modalidadesPermitidas.add("DELIVERY");
        }
        if (config.getPermiteTakeAway()) {
            modalidadesPermitidas.add("RETIRAR");
        }

        // 6. Construir el DTO de respuesta
        MenuResponseDTO.LocalInfoDTO localInfo = MenuResponseDTO.LocalInfoDTO.builder()
                .nombre(local.getNombre())
                .direccion(local.getDireccion())
                .telefono(local.getTelefono())
                .build();

        MenuResponseDTO response = MenuResponseDTO.builder()
                .local(localInfo)
                .productos(productoDTOs)
                .horariosSugeridos(horariosSugeridos)
                .modalidadesPermitidas(modalidadesPermitidas)
                .permiteReservas(config.getPermiteReservas())
                .build();

        log.info("Menú generado: {} productos, {} horarios disponibles, {} modalidades",
                productoDTOs.size(), horariosSugeridos.size(), modalidadesPermitidas.size());

        return response;
    }

    /**
     * Obtiene las hamburguesas disponibles según el stock actual de ingredientes.
     * Calcula la cantidad máxima que se puede preparar de cada hamburguesa.
     *
     * @param telefonoLocal Teléfono único del local (Multi-Tenant ID)
     * @return Map con lista de hamburguesas y estadísticas de disponibilidad
     * @throws NotFoundException si el local no existe
     */
    public java.util.Map<String, Object> obtenerHamburguesasConStock(String telefonoLocal) {
        log.info("🍔 Consultando hamburguesas con stock para local: {}", telefonoLocal);

        // 1. Buscar el local
        Local local = localService.buscarPorTelefono(telefonoLocal);

        // 2. Obtener todos los productos del local
        List<Producto> productos = productoRepository.findByLocal(local);

        // 3. Calcular disponibilidad y precios con promociones para cada hamburguesa
        List<java.util.Map<String, Object>> hamburguesas = productos.stream()
                .map(producto -> {
                    int cantidadDisponible = stockService.calcularCantidadMaximaDisponible(producto);
                    
                    // Calcular precio actual considerando promociones
                    java.math.BigDecimal precioActual = promocionService.calcularPrecioActual(producto);
                    boolean enPromocion = promocionService.esPromocionActiva(producto);
                    java.math.BigDecimal descuento = enPromocion ? 
                            promocionService.calcularPorcentajeDescuento(producto) : 
                            java.math.BigDecimal.ZERO;

                    java.util.Map<String, Object> hamburguesaMap = new java.util.HashMap<>();
                    hamburguesaMap.put("id", producto.getId());
                    hamburguesaMap.put("nombre", producto.getNombre());
                    hamburguesaMap.put("descripcion", producto.getDescripcion() != null ? producto.getDescripcion() : "");
                    hamburguesaMap.put("precioBase", producto.getPrecioBase() != null ? producto.getPrecioBase() : producto.getPrecio());
                    hamburguesaMap.put("precioActual", precioActual);
                    hamburguesaMap.put("enPromocion", enPromocion);
                    
                    if (enPromocion) {
                        hamburguesaMap.put("descuentoPorcentaje", descuento);
                        hamburguesaMap.put("infoPromocion", promocionService.obtenerInfoPromocion(producto));
                    }
                    
                    hamburguesaMap.put("cantidadDisponible", cantidadDisponible);

                    return hamburguesaMap;
                })
                .collect(Collectors.toList());

        // 4. Calcular estadísticas
        long totalHamburguesas = hamburguesas.size();
        long hamburguesasConStock = hamburguesas.stream()
                .filter(h -> (int) h.get("cantidadDisponible") > 0)
                .count();
        long hamburguesasAgotadas = totalHamburguesas - hamburguesasConStock;

        log.info("✅ Hamburguesas procesadas: {} total, {} con stock, {} agotadas",
                totalHamburguesas, hamburguesasConStock, hamburguesasAgotadas);

        // 5. Construir respuesta
        return java.util.Map.of(
                "local", java.util.Map.of(
                        "nombre", local.getNombre(),
                        "telefono", local.getTelefono()
                ),
                "hamburguesas", hamburguesas,
                "totalHamburguesas", totalHamburguesas,
                "hamburguesasConStock", hamburguesasConStock,
                "hamburguesasAgotadas", hamburguesasAgotadas
        );
    }
}

