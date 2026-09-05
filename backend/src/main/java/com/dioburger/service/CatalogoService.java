package com.dioburger.service;

import com.dioburger.exception.NotFoundException;
import com.dioburger.mapper.ProductoMapper;
import com.dioburger.model.dto.MenuCompletoDTO;
import com.dioburger.model.dto.ProductoDTO;
import com.dioburger.model.entity.Categoria;
import com.dioburger.model.entity.ConfiguracionLocal;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Producto;
import com.dioburger.repository.CategoriaRepository;
import com.dioburger.repository.LocalRepository;
import com.dioburger.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar el catálogo completo del local.
 * Centraliza la lógica de obtención de productos, categorías y extras.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogoService {

    private final LocalRepository localRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    /**
     * Obtiene el catálogo completo del local agrupado por categorías.
     * Incluye productos con sus extras disponibles.
     * 
     * @param telefonoLocal Teléfono del local (con código de país, ej: "5491112345678")
     * @return MenuCompletoDTO con toda la información del catálogo
     * @throws NotFoundException si el local no existe
     */
    @Transactional(readOnly = true)
    public MenuCompletoDTO obtenerCatalogoCompleto(String telefonoLocal) {
        log.info("Obteniendo catálogo completo para local con teléfono: {}", telefonoLocal);

        // Buscar local
        Local local = localRepository.findByTelefono(telefonoLocal)
            .orElseThrow(() -> new NotFoundException("Local no encontrado con teléfono: " + telefonoLocal));

        // Obtener configuración desde el local
        ConfiguracionLocal config = local.getConfiguracion();
        if (config == null) {
            throw new NotFoundException("Configuración no encontrada para el local");
        }

        // Obtener categorías activas con orden
        List<Categoria> categorias = categoriaRepository.findByLocalAndActivoOrderByOrdenAsc(local, true);

        // Construir DTOs de categorías con productos
        List<MenuCompletoDTO.CategoriaConProductosDTO> categoriasDTO = categorias.stream()
            .map(categoria -> {
                // Obtener productos activos de esta categoría
                List<Producto> productos = productoRepository.findByCategoriaAndEstaAgotadoFalse(categoria);
                
                // Convertir productos a DTO (ProductoMapper ya incluye extras)
                List<ProductoDTO> productosDTO = productos.stream()
                    .map(productoMapper::toDTO)
                    .collect(Collectors.toList());

                return MenuCompletoDTO.CategoriaConProductosDTO.builder()
                    .id(categoria.getId())
                    .nombre(categoria.getNombre())
                    .descripcion(categoria.getDescripcion())
                    .orden(categoria.getOrden())
                    .productos(productosDTO)
                    .build();
            })
            .filter(cat -> !cat.getProductos().isEmpty()) // Solo categorías con productos
            .collect(Collectors.toList());

        // Construir información del local
        MenuCompletoDTO.LocalInfoDTO localInfo = MenuCompletoDTO.LocalInfoDTO.builder()
            .nombre(local.getNombre())
            .direccion(local.getDireccion())
            .telefono(local.getTelefono())
            .build();

        // Construir configuración de horarios
        MenuCompletoDTO.ConfiguracionHorariosDTO configuracionDTO = MenuCompletoDTO.ConfiguracionHorariosDTO.builder()
            .horaApertura(config.getHoraApertura().format(DateTimeFormatter.ofPattern("HH:mm")))
            .horaCierre(config.getHoraCierre().format(DateTimeFormatter.ofPattern("HH:mm")))
            .build();

        // Calcular horarios sugeridos
        List<String> horariosSugeridos = calcularHorariosSugeridos(
            config.getHoraApertura(),
            config.getHoraCierre(),
            config.getIntervaloMinutosPedidos()
        );

        // Modalidades permitidas
        List<String> modalidadesPermitidas = new ArrayList<>();
        if (config.getPermiteDelivery()) {
            modalidadesPermitidas.add("DELIVERY");
        }
        if (config.getPermiteTakeAway()) {
            modalidadesPermitidas.add("RETIRAR");
        }

        // Construir respuesta completa
        MenuCompletoDTO menu = MenuCompletoDTO.builder()
            .local(localInfo)
            .categorias(categoriasDTO)
            .horariosSugeridos(horariosSugeridos)
            .modalidadesPermitidas(modalidadesPermitidas)
            .permiteReservas(config.getPermiteReservas())
            .configuracion(configuracionDTO)
            .build();

        log.info("Catálogo completo obtenido: {} categorías, {} modalidades",
            categoriasDTO.size(), modalidadesPermitidas.size());

        return menu;
    }

    /**
     * Calcula los horarios sugeridos para pedidos según el intervalo configurado.
     * 
     * @param horaApertura Hora de apertura del local
     * @param horaCierre Hora de cierre del local
     * @param intervaloMinutos Intervalo en minutos entre horarios
     * @return Lista de horarios en formato "HH:mm"
     */
    private List<String> calcularHorariosSugeridos(LocalTime horaApertura, LocalTime horaCierre, Integer intervaloMinutos) {
        List<String> horarios = new ArrayList<>();

        if (horaApertura == null || horaCierre == null || intervaloMinutos == null || intervaloMinutos <= 0) {
            return horarios;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        // Se cuenta en minutos del dia y no con LocalTime.plusMinutes porque este
        // ultimo da la vuelta a medianoche: con apertura 11:00, cierre 23:59 e
        // intervalo 15 el paso siguiente a 23:45 es 00:00, que no es posterior al
        // cierre, y el while original no terminaba nunca -- agotaba el heap y se
        // llevaba puesto el proceso entero.
        int minutoInicio = horaApertura.toSecondOfDay() / 60;
        int minutoFin = horaCierre.toSecondOfDay() / 60;

        // Cierre despues de medianoche (por ejemplo abre 20:00 y cierra 01:00):
        // se cuenta sobre el dia siguiente.
        if (minutoFin < minutoInicio) {
            minutoFin += 24 * 60;
        }

        for (int minuto = minutoInicio; minuto <= minutoFin; minuto += intervaloMinutos) {
            horarios.add(LocalTime.ofSecondOfDay((minuto % (24 * 60)) * 60L).format(formatter));
        }

        return horarios;
    }
}
