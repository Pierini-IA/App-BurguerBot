package com.dioburger.service;

import com.dioburger.exception.HorarioNoDisponibleException;
import com.dioburger.exception.NotFoundException;
import com.dioburger.exception.ReservaDuplicadaException;
import com.dioburger.mapper.ReservaMapper;
import com.dioburger.model.dto.ReservaDTO;
import com.dioburger.model.dto.ReservaResponseDTO;
import com.dioburger.model.entity.*;
import com.dioburger.model.enums.EstadoReserva;
import com.dioburger.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar reservas de mesas.
 * Incluye asignación inteligente de mesas, idempotencia y registro de gastos.
 * 
 * @author Dio Burger Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final MesaRepository mesaRepository;
    private final ClienteRepository clienteRepository;
    private final ReservaMapper reservaMapper;
    private final HorarioService horarioService;
    private final LocalService localService;

    /**
     * Crea una nueva reserva asignando mesas según la capacidad requerida.
     * Implementa idempotencia mediante requestId.
     * 
     * @param telefonoLocal Teléfono del local (Multi-Tenant ID)
     * @param dto Datos de la reserva
     * @return ReservaResponseDTO con la información de la reserva creada
     * @throws NotFoundException Si el local no existe
     * @throws ReservaDuplicadaException Si el requestId ya fue procesado
     * @throws HorarioNoDisponibleException Si no hay mesas disponibles
     */
    @Transactional
    public ReservaResponseDTO crearReserva(String telefonoLocal, ReservaDTO dto) {
        log.info("Iniciando creación de reserva para {} personas en {}", 
                dto.getNumeroPersonas(), telefonoLocal);

        // 1. Verificar idempotencia
        Optional<Reserva> reservaExistente = reservaRepository.findByRequestId(dto.getRequestId());
        if (reservaExistente.isPresent()) {
            log.warn("Reserva duplicada detectada con requestId: {}", dto.getRequestId());
            return reservaMapper.toResponseDTO(reservaExistente.get());
        }

        // 2. Verificar que el local existe y permite reservas
        Local local = localService.buscarPorTelefono(telefonoLocal);

        if (!local.getConfiguracion().getPermiteReservas()) {
            throw new IllegalStateException("El local no acepta reservas actualmente");
        }

        // 3. Verificar que hay horarios disponibles para reservas
        List<String> horariosDisponibles = horarioService.getHorariosSugeridosReservas(telefonoLocal);
        if (horariosDisponibles.isEmpty()) {
            throw new HorarioNoDisponibleException("No hay horarios disponibles para reservas");
        }

        // 4. Buscar mesas disponibles para la hora solicitada
        List<Mesa> mesasDisponibles = mesaRepository.findByLocalAndDisponibleTrue(local);
        
        if (mesasDisponibles.isEmpty()) {
            throw new HorarioNoDisponibleException("No hay mesas disponibles en este momento");
        }

        // 5. Asignar mesas según capacidad requerida
        Set<Mesa> mesasAsignadas = asignarMesas(mesasDisponibles, dto.getNumeroPersonas());

        if (mesasAsignadas.isEmpty()) {
            throw new HorarioNoDisponibleException(
                    String.format("No hay mesas disponibles para %d personas", dto.getNumeroPersonas()));
        }

        // 6. Buscar o crear cliente
        Cliente cliente = clienteRepository.findByTelefono(dto.getCliente().getTelefono())
                .orElseGet(() -> {
                    Cliente nuevoCliente = new Cliente();
                    nuevoCliente.setNombre(dto.getCliente().getNombre());
                    nuevoCliente.setTelefono(dto.getCliente().getTelefono());
                    Cliente clienteGuardado = clienteRepository.save(nuevoCliente);
                    log.info("Nuevo cliente creado: {} ({})", clienteGuardado.getNombre(), clienteGuardado.getTelefono());
                    return clienteGuardado;
                });

        // 7. Crear la reserva
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setMesas(mesasAsignadas);
        reserva.setHoraReserva(dto.getHoraReserva());
        reserva.setNumeroPersonas(dto.getNumeroPersonas());
        reserva.setObservaciones(dto.getObservaciones());
        reserva.setRequestId(dto.getRequestId());
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setGastoTotal(BigDecimal.ZERO);

        // 8. Marcar mesas como no disponibles
        mesasAsignadas.forEach(mesa -> {
            mesa.setDisponible(false);
            log.debug("Mesa {} marcada como no disponible", mesa.getNumero());
        });

        // 9. Guardar reserva
        Reserva reservaGuardada = reservaRepository.save(reserva);

        log.info("Reserva creada exitosamente: ID={}, Cliente={}, Mesas={}, Hora={}",
                reservaGuardada.getId(),
                cliente.getNombre(),
                mesasAsignadas.stream().map(Mesa::getNumero).collect(Collectors.toList()),
                dto.getHoraReserva());

        // 10. Convertir a DTO y retornar
        return reservaMapper.toResponseDTO(reservaGuardada);
    }

    /**
     * Asigna mesas de forma inteligente para un grupo de personas.
     * Puede asignar una o múltiples mesas según la capacidad requerida.
     * 
     * Estrategia:
     * 1. Ordena las mesas por capacidad ascendente
     * 2. Selecciona la combinación más eficiente de mesas
     * 3. Prioriza usar menos mesas cuando sea posible
     * 
     * @param disponibles Lista de mesas disponibles
     * @param numeroPersonas Cantidad de personas a acomodar
     * @return Set de mesas asignadas (vacío si no se puede acomodar)
     */
    private Set<Mesa> asignarMesas(List<Mesa> disponibles, Integer numeroPersonas) {
        // Ordenar por capacidad ascendente para optimizar asignación
        disponibles.sort(Comparator.comparing(Mesa::getCapacidad));

        Set<Mesa> seleccionadas = new LinkedHashSet<>();
        int capacidadTotal = 0;

        // Estrategia greedy: seleccionar mesas hasta cubrir la capacidad
        for (Mesa mesa : disponibles) {
            if (capacidadTotal >= numeroPersonas) {
                break;
            }
            seleccionadas.add(mesa);
            capacidadTotal += mesa.getCapacidad();
            
            log.debug("Mesa {} seleccionada (capacidad: {}). Total acumulado: {}/{}",
                    mesa.getNumero(), mesa.getCapacidad(), capacidadTotal, numeroPersonas);
        }

        // Verificar si se logró cubrir la capacidad requerida
        if (capacidadTotal < numeroPersonas) {
            log.warn("No se pudo asignar mesas suficientes. Requerido: {}, Disponible: {}",
                    numeroPersonas, capacidadTotal);
            return Collections.emptySet();
        }

        log.info("Asignación exitosa: {} mesa(s) para {} personas (capacidad total: {})",
                seleccionadas.size(), numeroPersonas, capacidadTotal);

        return seleccionadas;
    }

    /**
     * Registra el gasto total de una reserva y la marca como finalizada.
     * Libera automáticamente las mesas asignadas.
     * 
     * @param reservaId ID de la reserva
     * @param gasto Monto total gastado
     * @throws NotFoundException Si la reserva no existe
     */
    @Transactional
    public void registrarGasto(Long reservaId, BigDecimal gasto) {
        log.info("Registrando gasto de ${} para reserva ID: {}", gasto, reservaId);

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada con ID: " + reservaId));

        // Actualizar gasto y estado
        reserva.setGastoTotal(gasto);
        reserva.setEstado(EstadoReserva.FINALIZADA);

        // Liberar mesas
        reserva.getMesas().forEach(mesa -> {
            mesa.setDisponible(true);
            log.debug("Mesa {} liberada", mesa.getNumero());
        });

        reservaRepository.save(reserva);

        log.info("Gasto registrado exitosamente. Reserva {} finalizada. Mesas liberadas: {}",
                reservaId,
                reserva.getMesas().stream().map(Mesa::getNumero).collect(Collectors.toList()));
    }

    /**
     * Obtiene una reserva por su ID.
     * 
     * @param telefonoLocal Teléfono del local (validación de pertenencia)
     * @param reservaId ID de la reserva
     * @return ReservaResponseDTO con la información de la reserva
     * @throws NotFoundException Si la reserva no existe
     */
    @Transactional(readOnly = true)
    public ReservaResponseDTO obtenerReservaPorId(String telefonoLocal, Long reservaId) {
        log.debug("Buscando reserva ID: {} para local: {}", reservaId, telefonoLocal);

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada con ID: " + reservaId));

        // Verificar que la reserva pertenece al local correcto (Multi-Tenancy)
        Set<Mesa> mesas = reserva.getMesas();
        if (mesas.isEmpty() || !mesas.iterator().next().getLocal().getTelefono().equals(telefonoLocal)) {
            throw new NotFoundException("La reserva no pertenece a este local");
        }

        return reservaMapper.toResponseDTO(reserva);
    }

    /**
     * Obtiene todas las reservas de un local en un rango de fechas.
     * 
     * @param telefonoLocal Teléfono del local
     * @param desde Fecha inicial (opcional)
     * @param hasta Fecha final (opcional)
     * @return Lista de ReservaResponseDTO
     */
    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> obtenerReservasPorLocal(
            String telefonoLocal, 
            LocalDateTime desde, 
            LocalDateTime hasta) {
        
        log.debug("Obteniendo reservas para local: {} desde {} hasta {}", telefonoLocal, desde, hasta);

        Local local = localService.buscarPorTelefono(telefonoLocal);

        List<Reserva> reservas;
        
        if (desde != null && hasta != null) {
            reservas = reservaRepository.findByMesas_LocalAndHoraReservaBetween(local, desde, hasta);
        } else {
            reservas = reservaRepository.findByMesas_Local(local);
        }

        log.info("Encontradas {} reservas para el local {}", reservas.size(), telefonoLocal);

        return reservas.stream()
                .map(reservaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cancela una reserva y libera las mesas asociadas.
     * 
     * @param reservaId ID de la reserva a cancelar
     * @throws NotFoundException Si la reserva no existe
     */
    @Transactional
    public void cancelarReserva(Long reservaId) {
        log.info("Cancelando reserva ID: {}", reservaId);

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada con ID: " + reservaId));

        reserva.setEstado(EstadoReserva.CANCELADA);

        // Liberar mesas
        reserva.getMesas().forEach(mesa -> {
            mesa.setDisponible(true);
            log.debug("Mesa {} liberada por cancelación", mesa.getNumero());
        });

        reservaRepository.save(reserva);

        log.info("Reserva {} cancelada exitosamente. Mesas liberadas: {}",
                reservaId,
                reserva.getMesas().stream().map(Mesa::getNumero).collect(Collectors.toList()));
    }
}
