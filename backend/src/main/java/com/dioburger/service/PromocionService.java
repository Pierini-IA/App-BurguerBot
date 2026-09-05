package com.dioburger.service;

import com.dioburger.model.entity.Producto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar promociones y calcular precios dinámicos.
 * Determina si una promoción está activa según el horario y día actual.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromocionService {

    /**
     * Calcula el precio actual de un producto considerando promociones activas.
     * Si la promoción está activa (según horario y día), devuelve precioPromocion.
     * Si no, devuelve el precioBase.
     *
     * @param producto Producto a evaluar
     * @return Precio actual calculado
     */
    public BigDecimal calcularPrecioActual(Producto producto) {
        // Si no tiene promoción configurada, devolver precio base
        if (producto.getTienePromocion() == null || !producto.getTienePromocion()) {
            return producto.getPrecioBase() != null ? producto.getPrecioBase() : producto.getPrecio();
        }

        // Si la promoción está activa, devolver precio de promoción
        if (esPromocionActiva(producto)) {
            log.debug("Promoción activa para {}: ${} -> ${}",
                    producto.getNombre(),
                    producto.getPrecioBase(),
                    producto.getPrecioPromocion());
            return producto.getPrecioPromocion();
        }

        // Promoción no activa, devolver precio base
        return producto.getPrecioBase();
    }

    /**
     * Determina si la promoción de un producto está activa en este momento.
     * Verifica el horario actual y el día de la semana.
     *
     * @param producto Producto a evaluar
     * @return true si la promoción está activa ahora
     */
    public boolean esPromocionActiva(Producto producto) {
        if (producto.getTienePromocion() == null || !producto.getTienePromocion()) {
            return false;
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalTime horaActual = ahora.toLocalTime();
        DayOfWeek diaActual = ahora.getDayOfWeek();

        // Verificar si el día actual está en los días de promoción
        if (!esDiaDePromocion(producto, diaActual)) {
            return false;
        }

        // Verificar si la hora actual está dentro del rango de promoción
        return esHorarioDePromocion(producto, horaActual);
    }

    /**
     * Verifica si el día actual está en los días de promoción del producto.
     *
     * @param producto Producto a evaluar
     * @param dia Día de la semana a verificar
     * @return true si el día tiene promoción
     */
    private boolean esDiaDePromocion(Producto producto, DayOfWeek dia) {
        String diasPromocion = producto.getDiasPromocion();

        if (diasPromocion == null || diasPromocion.isBlank()) {
            // Si no hay días configurados, la promoción aplica todos los días
            return true;
        }

        // Parsear JSON array de días: ["MONDAY","FRIDAY","SATURDAY"]
        List<DayOfWeek> dias = parsearDiasPromocion(diasPromocion);

        return dias.contains(dia);
    }

    /**
     * Verifica si la hora actual está dentro del rango de promoción.
     *
     * @param producto Producto a evaluar
     * @param hora Hora a verificar
     * @return true si la hora está en el rango de promoción
     */
    private boolean esHorarioDePromocion(Producto producto, LocalTime hora) {
        LocalTime inicio = producto.getHoraInicioPromo();
        LocalTime fin = producto.getHoraFinPromo();

        if (inicio == null || fin == null) {
            // Si no hay horarios configurados, la promoción aplica todo el día
            return true;
        }

        // Verificar si la hora actual está entre inicio y fin
        return !hora.isBefore(inicio) && !hora.isAfter(fin);
    }

    /**
     * Parsea el string JSON de días de promoción a una lista de DayOfWeek.
     * Formato esperado: ["MONDAY","TUESDAY","WEDNESDAY"]
     *
     * @param diasJson String JSON con los días
     * @return Lista de DayOfWeek
     */
    private List<DayOfWeek> parsearDiasPromocion(String diasJson) {
        try {
            // Remover corchetes y comillas, luego dividir por comas
            String limpio = diasJson.replaceAll("[\\[\\]\"]", "");
            return Arrays.stream(limpio.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(DayOfWeek::valueOf)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error parseando días de promoción '{}': {}", diasJson, e.getMessage());
            return List.of(); // Lista vacía = no aplica ningún día
        }
    }

    /**
     * Calcula el porcentaje de descuento de una promoción.
     *
     * @param producto Producto a evaluar
     * @return Porcentaje de descuento (0-100)
     */
    public BigDecimal calcularPorcentajeDescuento(Producto producto) {
        if (producto.getPrecioBase() == null || producto.getPrecioPromocion() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal diferencia = producto.getPrecioBase().subtract(producto.getPrecioPromocion());
        BigDecimal porcentaje = diferencia
                .divide(producto.getPrecioBase(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return porcentaje.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Obtiene información detallada de la promoción para mostrar al cliente.
     *
     * @param producto Producto a evaluar
     * @return Información de la promoción como String
     */
    public String obtenerInfoPromocion(Producto producto) {
        if (!producto.getTienePromocion()) {
            return null;
        }

        boolean activa = esPromocionActiva(producto);
        BigDecimal descuento = calcularPorcentajeDescuento(producto);

        StringBuilder info = new StringBuilder();
        info.append(String.format("%.0f%% OFF", descuento));

        if (producto.getHoraInicioPromo() != null && producto.getHoraFinPromo() != null) {
            info.append(String.format(" de %s a %s",
                    producto.getHoraInicioPromo(),
                    producto.getHoraFinPromo()));
        }

        if (!activa) {
            info.append(" (no activa ahora)");
        }

        return info.toString();
    }
}
