package com.dioburger.service;

import com.dioburger.exception.NotFoundException;
import com.dioburger.model.entity.ConfiguracionLocal;
import com.dioburger.model.entity.Local;
import com.dioburger.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para calcular horarios disponibles de pedidos y reservas.
 * Implementa la lógica de negocio para generar slots de tiempo y
 * filtrar aquellos que ya están llenos.
 *
 * @author Dio Burger Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HorarioService {

    private final PedidoRepository pedidoRepository;
    private final LocalService localService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Obtiene los horarios sugeridos para pedidos en un local específico.
     * Genera slots de tiempo basándose en la configuración del local y
     * filtra aquellos que ya alcanzaron el máximo de pedidos permitidos.
     *
     * @param telefonoLocal Teléfono único del local (Multi-Tenant ID)
     * @return Lista de horarios disponibles en formato "HH:mm"
     * @throws NotFoundException si el local no existe
     */
    public List<String> getHorariosSugeridosPedidos(String telefonoLocal) {
        // 1. Buscar el local
        Local local = localService.buscarPorTelefono(telefonoLocal);

        ConfiguracionLocal config = local.getConfiguracion();

        if (config == null) {
            log.error("El local {} no tiene configuración", local.getNombre());
            throw new NotFoundException("El local no tiene configuración de horarios");
        }

        // 2. Obtener parámetros de configuración
        LocalTime horaApertura = config.getHoraApertura();
        LocalTime horaCierre = config.getHoraCierre();
        Integer intervaloMinutos = config.getIntervaloMinutosPedidos();
        Integer maxPorIntervalo = config.getMaxPedidosPorIntervalo();

        log.info("Calculando horarios para {}: {}:{} - {}:{}, intervalo={} min, max={}",
                local.getNombre(),
                horaApertura.getHour(), horaApertura.getMinute(),
                horaCierre.getHour(), horaCierre.getMinute(),
                intervaloMinutos, maxPorIntervalo);

        // 3. Generar todos los slots posibles
        List<LocalDateTime> slots = generarSlots(horaApertura, horaCierre, intervaloMinutos);

        // 4. Filtrar slots que ya están llenos
        List<String> horariosDisponibles = new ArrayList<>();

        for (LocalDateTime slot : slots) {
            // Contar cuántos pedidos hay en este slot
            Long pedidosEnSlot = pedidoRepository.countPedidosEnIntervalo(
                    local,
                    slot,
                    slot.plusMinutes(intervaloMinutos)
            );

            log.debug("Slot {}: {} pedidos de {} permitidos",
                    slot.format(TIME_FORMATTER), pedidosEnSlot, maxPorIntervalo);

            // Si no está lleno, agregarlo a la lista
            if (pedidosEnSlot < maxPorIntervalo) {
                horariosDisponibles.add(slot.format(TIME_FORMATTER));
            }
        }

        log.info("Horarios disponibles: {} de {} slots totales",
                horariosDisponibles.size(), slots.size());

        return horariosDisponibles;
    }

    /**
     * Genera una lista de slots de tiempo entre hora de apertura y cierre.
     *
     * @param horaApertura Hora de apertura del local
     * @param horaCierre Hora de cierre del local
     * @param intervaloMinutos Intervalo en minutos entre cada slot
     * @return Lista de LocalDateTime representando cada slot
     */
    private List<LocalDateTime> generarSlots(LocalTime horaApertura, LocalTime horaCierre, Integer intervaloMinutos) {
        List<LocalDateTime> slots = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        LocalDateTime slotActual = LocalDateTime.of(hoy, horaApertura);
        LocalDateTime ultimoSlot = LocalDateTime.of(hoy, horaCierre);

        while (slotActual.isBefore(ultimoSlot)) {
            slots.add(slotActual);
            slotActual = slotActual.plusMinutes(intervaloMinutos);
        }

        return slots;
    }

    /**
     * Obtiene los horarios sugeridos para reservas en un local específico.
     * Similar a getHorariosSugeridosPedidos pero usa la configuración de reservas.
     *
     * @param telefonoLocal Teléfono único del local
     * @return Lista de horarios disponibles en formato "HH:mm"
     * @throws NotFoundException si el local no existe
     */
    public List<String> getHorariosSugeridosReservas(String telefonoLocal) {
        Local local = localService.buscarPorTelefono(telefonoLocal);

        ConfiguracionLocal config = local.getConfiguracion();

        if (config == null || !config.getPermiteReservas()) {
            log.warn("El local {} no acepta reservas", local.getNombre());
            return List.of();
        }

        LocalTime horaApertura = config.getHoraAperturaReservas();
        LocalTime horaCierre = config.getHoraCierreReservas();
        Integer intervaloMinutos = config.getIntervaloMinutosReservas();

        List<LocalDateTime> slots = generarSlots(horaApertura, horaCierre, intervaloMinutos);
        List<String> horariosDisponibles = new ArrayList<>();

        for (LocalDateTime slot : slots) {
            horariosDisponibles.add(slot.format(TIME_FORMATTER));
        }

        log.info("Horarios de reservas disponibles: {}", horariosDisponibles.size());

        return horariosDisponibles;
    }
}
