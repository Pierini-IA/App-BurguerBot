package com.dioburger.service;

import com.dioburger.model.dto.ReporteVentasDTO;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Pedido;
import com.dioburger.model.enums.EstadoPedido;
import com.dioburger.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generación de reportes y estadísticas de ventas.
 * Provee análisis de ventas por diferentes períodos y métricas.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportesService {

    private final PedidoRepository pedidoRepository;
    private final LocalService localService;

    /**
     * Obtiene ventas diarias en un rango de fechas.
     * Solo cuenta pedidos en estado ENTREGADO.
     * 
     * @param telefonoLocal Teléfono del local
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de reportes diarios
     */
    @Transactional(readOnly = true)
    public List<ReporteVentasDTO> obtenerVentasDiarias(
            String telefonoLocal, 
            LocalDate fechaInicio, 
            LocalDate fechaFin) {
        
        log.info("📊 Generando reporte diario para {} desde {} hasta {}", 
                telefonoLocal, fechaInicio, fechaFin);

        Local local = localService.buscarPorTelefono(telefonoLocal);

        List<Pedido> pedidos = pedidoRepository.findByLocalAndHoraPedidoBetween(
                local, 
                fechaInicio.atStartOfDay(), 
                fechaFin.plusDays(1).atStartOfDay()
        );

        // Agrupar por fecha
        Map<LocalDate, List<Pedido>> pedidosPorFecha = pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.ENTREGADO)
                .collect(Collectors.groupingBy(p -> p.getHoraPedido().toLocalDate()));

        // Crear reportes
        List<ReporteVentasDTO> reportes = pedidosPorFecha.entrySet().stream()
                .map(entry -> {
                    LocalDate fecha = entry.getKey();
                    List<Pedido> pedidosDelDia = entry.getValue();
                    
                    long cantidad = pedidosDelDia.size();
                    BigDecimal total = pedidosDelDia.stream()
                            .map(Pedido::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal promedio = cantidad > 0 
                            ? total.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return ReporteVentasDTO.builder()
                            .fecha(fecha)
                            .periodo(fecha.getDayOfWeek() + " " + fecha)
                            .cantidadPedidos(cantidad)
                            .totalVentas(total)
                            .promedioTicket(promedio)
                            .build();
                })
                .sorted(Comparator.comparing(ReporteVentasDTO::getFecha))
                .collect(Collectors.toList());

        log.info("✅ Reporte generado: {} días con ventas", reportes.size());
        return reportes;
    }

    /**
     * Obtiene ventas semanales del mes especificado.
     * 
     * @param telefonoLocal Teléfono del local
     * @param año Año
     * @param mes Mes (1-12)
     * @return Lista de reportes semanales
     */
    @Transactional(readOnly = true)
    public List<ReporteVentasDTO> obtenerVentasSemanales(
            String telefonoLocal, 
            int año, 
            int mes) {
        
        log.info("📊 Generando reporte semanal para {} - {}/{}", 
                telefonoLocal, mes, año);

        LocalDate inicioMes = LocalDate.of(año, mes, 1);
        LocalDate finMes = inicioMes.plusMonths(1).minusDays(1);

        Local local = localService.buscarPorTelefono(telefonoLocal);

        List<Pedido> pedidos = pedidoRepository.findByLocalAndHoraPedidoBetween(
                local,
                inicioMes.atStartOfDay(),
                finMes.plusDays(1).atStartOfDay()
        );

        // Agrupar por semana del mes
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        Map<Integer, List<Pedido>> pedidosPorSemana = pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.ENTREGADO)
                .collect(Collectors.groupingBy(p -> 
                    p.getHoraPedido().toLocalDate().get(weekFields.weekOfMonth())
                ));

        List<ReporteVentasDTO> reportes = pedidosPorSemana.entrySet().stream()
                .map(entry -> {
                    int semana = entry.getKey();
                    List<Pedido> pedidosDeLaSemana = entry.getValue();
                    
                    long cantidad = pedidosDeLaSemana.size();
                    BigDecimal total = pedidosDeLaSemana.stream()
                            .map(Pedido::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal promedio = cantidad > 0 
                            ? total.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return ReporteVentasDTO.builder()
                            .fecha(inicioMes)
                            .periodo("Semana " + semana)
                            .cantidadPedidos(cantidad)
                            .totalVentas(total)
                            .promedioTicket(promedio)
                            .build();
                })
                .sorted(Comparator.comparing(ReporteVentasDTO::getPeriodo))
                .collect(Collectors.toList());

        log.info("✅ Reporte generado: {} semanas con ventas", reportes.size());
        return reportes;
    }

    /**
     * Obtiene ventas mensuales del año especificado.
     * 
     * @param telefonoLocal Teléfono del local
     * @param año Año
     * @return Lista de reportes mensuales
     */
    @Transactional(readOnly = true)
    public List<ReporteVentasDTO> obtenerVentasMensuales(String telefonoLocal, int año) {
        
        log.info("📊 Generando reporte mensual para {} - año {}", telefonoLocal, año);

        LocalDate inicioAño = LocalDate.of(año, 1, 1);
        LocalDate finAño = LocalDate.of(año, 12, 31);

        Local local = localService.buscarPorTelefono(telefonoLocal);

        List<Pedido> pedidos = pedidoRepository.findByLocalAndHoraPedidoBetween(
                local,
                inicioAño.atStartOfDay(),
                finAño.plusDays(1).atStartOfDay()
        );

        // Agrupar por mes
        Map<Integer, List<Pedido>> pedidosPorMes = pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.ENTREGADO)
                .collect(Collectors.groupingBy(p -> p.getHoraPedido().getMonthValue()));

        List<ReporteVentasDTO> reportes = pedidosPorMes.entrySet().stream()
                .map(entry -> {
                    int mes = entry.getKey();
                    List<Pedido> pedidosDelMes = entry.getValue();
                    
                    long cantidad = pedidosDelMes.size();
                    BigDecimal total = pedidosDelMes.stream()
                            .map(Pedido::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal promedio = cantidad > 0 
                            ? total.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return ReporteVentasDTO.builder()
                            .fecha(LocalDate.of(año, mes, 1))
                            .periodo(LocalDate.of(año, mes, 1).getMonth().toString())
                            .cantidadPedidos(cantidad)
                            .totalVentas(total)
                            .promedioTicket(promedio)
                            .build();
                })
                .sorted(Comparator.comparing(ReporteVentasDTO::getFecha))
                .collect(Collectors.toList());

        log.info("✅ Reporte generado: {} meses con ventas", reportes.size());
        return reportes;
    }

    /**
     * Obtiene los productos más vendidos en un período.
     * 
     * @param telefonoLocal Teléfono del local
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @param limit Cantidad de productos a retornar
     * @return Lista de productos con estadísticas
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerProductosTopVentas(
            String telefonoLocal,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            int limit
    ) {
        log.info("📊 Generando top {} productos para {}", limit, telefonoLocal);

        Local local = localService.buscarPorTelefono(telefonoLocal);

        List<Pedido> pedidos = pedidoRepository.findByLocalAndHoraPedidoBetween(
                local,
                fechaInicio.atStartOfDay(),
                fechaFin.plusDays(1).atStartOfDay()
        );

        // Contar productos
        Map<String, Long> conteoProductos = new HashMap<>();
        Map<String, BigDecimal> totalPorProducto = new HashMap<>();

        pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.ENTREGADO)
                .flatMap(p -> p.getItems().stream())
                .forEach(item -> {
                    String nombreProducto = item.getProducto().getNombre();
                    conteoProductos.merge(nombreProducto, (long) item.getCantidad(), Long::sum);
                    
                    // Usar el precio del producto ya que PedidoItem no tiene precioUnitario
                    BigDecimal subtotal = item.getProducto().getPrecio()
                            .multiply(BigDecimal.valueOf(item.getCantidad()));
                    totalPorProducto.merge(nombreProducto, subtotal, BigDecimal::add);
                });

        List<Map<String, Object>> resultado = conteoProductos.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> Map.of(
                        "producto", (Object) entry.getKey(),
                        "cantidadVendida", (Object) entry.getValue(),
                        "totalVentas", (Object) totalPorProducto.get(entry.getKey())
                ))
                .collect(Collectors.toList());

        log.info("✅ Top {} productos generado", resultado.size());
        return resultado;
    }

    /**
     * Obtiene un dashboard con KPIs principales del local.
     * 
     * @param telefonoLocal Teléfono del local
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Mapa con KPIs
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerDashboard(
            String telefonoLocal, 
            LocalDate fechaInicio, 
            LocalDate fechaFin) {
        
        log.info("📊 Generando dashboard para {}", telefonoLocal);

        Local local = localService.buscarPorTelefono(telefonoLocal);

        List<Pedido> pedidos = pedidoRepository.findByLocalAndHoraPedidoBetween(
                local,
                fechaInicio.atStartOfDay(),
                fechaFin.plusDays(1).atStartOfDay()
        );

        List<Pedido> pedidosEntregados = pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.ENTREGADO)
                .toList();

        long totalPedidos = pedidosEntregados.size();
        BigDecimal totalVentas = pedidosEntregados.stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal promedioTicket = totalPedidos > 0 
                ? totalVentas.divide(BigDecimal.valueOf(totalPedidos), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long pedidosCancelados = pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.CANCELADO)
                .count();

        Map<String, Object> dashboard = Map.of(
                "totalPedidos", totalPedidos,
                "totalVentas", totalVentas,
                "promedioTicket", promedioTicket,
                "pedidosCancelados", pedidosCancelados,
                "periodo", fechaInicio + " a " + fechaFin,
                "local", local.getNombre()
        );

        log.info("✅ Dashboard generado: {} pedidos, ${} ventas", totalPedidos, totalVentas);
        return dashboard;
    }

    /**
     * Compara ventas entre dos períodos.
     * 
     * @param telefonoLocal Teléfono del local
     * @param periodo1Inicio Inicio del período 1
     * @param periodo1Fin Fin del período 1
     * @param periodo2Inicio Inicio del período 2
     * @param periodo2Fin Fin del período 2
     * @return Mapa con comparación
     */
    @Transactional(readOnly = true)
    public Map<String, Object> compararPeriodos(
            String telefonoLocal,
            LocalDate periodo1Inicio, LocalDate periodo1Fin,
            LocalDate periodo2Inicio, LocalDate periodo2Fin
    ) {
        log.info("📊 Comparando períodos para {}", telefonoLocal);

        Map<String, Object> dashboard1 = obtenerDashboard(telefonoLocal, periodo1Inicio, periodo1Fin);
        Map<String, Object> dashboard2 = obtenerDashboard(telefonoLocal, periodo2Inicio, periodo2Fin);

        BigDecimal ventas1 = (BigDecimal) dashboard1.get("totalVentas");
        BigDecimal ventas2 = (BigDecimal) dashboard2.get("totalVentas");
        
        BigDecimal crecimiento = ventas2.compareTo(BigDecimal.ZERO) > 0
                ? ventas1.subtract(ventas2)
                        .divide(ventas2, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        Map<String, Object> resultado = Map.of(
                "periodo1", dashboard1,
                "periodo2", dashboard2,
                "crecimientoVentas", crecimiento.setScale(2, RoundingMode.HALF_UP) + "%",
                "diferenciaAbsoluta", ventas1.subtract(ventas2)
        );

        log.info("✅ Comparación generada: {}% de crecimiento", crecimiento);
        return resultado;
    }
}
