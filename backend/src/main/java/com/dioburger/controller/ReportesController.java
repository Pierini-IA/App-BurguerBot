package com.dioburger.controller;

import com.dioburger.model.dto.ReporteVentasDTO;
import com.dioburger.model.entity.Local;
import com.dioburger.model.enums.Feature;
import com.dioburger.repository.LocalRepository;
import com.dioburger.security.RequiresFeature;
import com.dioburger.service.LocalService;
import com.dioburger.service.ReportesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para reportes y estadísticas de ventas.
 * Proporciona análisis de ventas por diferentes períodos y métricas.
 * 
 * Acceso: ROLE_ADMIN o ROLE_SUPERADMIN
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reportes", description = "Endpoints de reportes y estadísticas de ventas")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
public class ReportesController {

    private final ReportesService reportesService;
    private final LocalService localService;
    @SuppressWarnings("unused")
    private final LocalRepository localRepository;  // Mantener para compatibilidad con AOP

    /**
     * Obtiene ventas totales por día en un rango de fechas.
     * 
     * @param telefonoLocal Teléfono del local (Multi-Tenant ID)
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de reportes diarios
     */
    @GetMapping("/ventas/diarias")
    @RequiresFeature(Feature.REPORTES_AVANZADOS)
    @Operation(
        summary = "Ventas diarias",
        description = "Obtiene el total de ventas por día en un rango de fechas. Solo cuenta pedidos entregados."
    )
    public ResponseEntity<List<ReporteVentasDTO>> ventasDiarias(
            @Parameter(description = "Teléfono del local", example = "+5491112345678")
            @RequestParam String telefonoLocal,
            
            @Parameter(description = "Fecha de inicio", example = "2025-10-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            
            @Parameter(description = "Fecha de fin", example = "2025-10-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        log.info("📊 GET /api/reportes/ventas/diarias - Local: {}, Rango: {} a {}", 
                telefonoLocal, fechaInicio, fechaFin);

        // Obtener local para validación de plan (requerido por @RequiresFeature)
        @SuppressWarnings("unused")
        Local local = localService.buscarPorTelefono(telefonoLocal);

        List<ReporteVentasDTO> reporte = reportesService.obtenerVentasDiarias(
                telefonoLocal, fechaInicio, fechaFin
        );

        return ResponseEntity.ok(reporte);
    }

    /**
     * Obtiene ventas totales por semana del mes especificado.
     * 
     * @param telefonoLocal Teléfono del local
     * @param año Año
     * @param mes Mes (1-12)
     * @return Lista de reportes semanales
     */
    @GetMapping("/ventas/semanales")
    @RequiresFeature(Feature.REPORTES_AVANZADOS)
    @Operation(
        summary = "Ventas semanales",
        description = "Obtiene ventas por semana del mes especificado"
    )
    public ResponseEntity<List<ReporteVentasDTO>> ventasSemanales(
            @Parameter(description = "Teléfono del local", example = "+5491112345678")
            @RequestParam String telefonoLocal,
            
            @Parameter(description = "Año", example = "2025")
            @RequestParam int año,
            
            @Parameter(description = "Mes (1-12)", example = "10")
            @RequestParam int mes
    ) {
        log.info("📊 GET /api/reportes/ventas/semanales - Local: {}, Mes: {}/{}", 
                telefonoLocal, mes, año);

        // Obtener local para validación de plan (requerido por @RequiresFeature)
        @SuppressWarnings("unused")
        Local local = localService.buscarPorTelefono(telefonoLocal);

        List<ReporteVentasDTO> reporte = reportesService.obtenerVentasSemanales(
                telefonoLocal, año, mes
        );

        return ResponseEntity.ok(reporte);
    }

    /**
     * Obtiene ventas totales por mes del año especificado.
     * 
     * @param telefonoLocal Teléfono del local
     * @param año Año
     * @return Lista de reportes mensuales
     */
    @GetMapping("/ventas/mensuales")
    @RequiresFeature(Feature.REPORTES_AVANZADOS)
    @Operation(
        summary = "Ventas mensuales",
        description = "Obtiene ventas por mes del año especificado"
    )
    public ResponseEntity<List<ReporteVentasDTO>> ventasMensuales(
            @Parameter(description = "Teléfono del local", example = "+5491112345678")
            @RequestParam String telefonoLocal,
            
            @Parameter(description = "Año", example = "2025")
            @RequestParam int año
    ) {
        log.info("📊 GET /api/reportes/ventas/mensuales - Local: {}, Año: {}", 
                telefonoLocal, año);

        // Obtener local para validación de plan (requerido por @RequiresFeature)
        @SuppressWarnings("unused")
        Local local = localService.buscarPorTelefono(telefonoLocal);

        List<ReporteVentasDTO> reporte = reportesService.obtenerVentasMensuales(
                telefonoLocal, año
        );

        return ResponseEntity.ok(reporte);
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
    @GetMapping("/productos/top")
    @RequiresFeature(Feature.REPORTES_AVANZADOS)
    @Operation(
        summary = "Productos más vendidos",
        description = "Obtiene el top de productos más vendidos en un período"
    )
    public ResponseEntity<List<Map<String, Object>>> productosTopVentas(
            @Parameter(description = "Teléfono del local", example = "+5491112345678")
            @RequestParam String telefonoLocal,
            
            @Parameter(description = "Fecha de inicio", example = "2025-10-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            
            @Parameter(description = "Fecha de fin", example = "2025-10-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            
            @Parameter(description = "Cantidad de productos a retornar", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("📊 GET /api/reportes/productos/top - Local: {}, Top: {}", 
                telefonoLocal, limit);

        // Obtener local para validación de plan (requerido por @RequiresFeature)
        @SuppressWarnings("unused")
        Local local = localService.buscarPorTelefono(telefonoLocal);

        List<Map<String, Object>> reporte = reportesService.obtenerProductosTopVentas(
                telefonoLocal, fechaInicio, fechaFin, limit
        );

        return ResponseEntity.ok(reporte);
    }

    /**
     * Dashboard con KPIs principales del local.
     * 
     * @param telefonoLocal Teléfono del local
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Mapa con KPIs principales
     */
    @GetMapping("/dashboard")
    @RequiresFeature(Feature.REPORTES_AVANZADOS)
    @Operation(
        summary = "Dashboard de KPIs",
        description = "Resumen de métricas principales: total pedidos, ventas, ticket promedio, cancelaciones"
    )
    public ResponseEntity<Map<String, Object>> dashboard(
            @Parameter(description = "Teléfono del local", example = "+5491112345678")
            @RequestParam String telefonoLocal,
            
            @Parameter(description = "Fecha de inicio", example = "2025-10-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            
            @Parameter(description = "Fecha de fin", example = "2025-10-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        log.info("📊 GET /api/reportes/dashboard - Local: {}", telefonoLocal);

        // Obtener local para validación de plan (requerido por @RequiresFeature)
        @SuppressWarnings("unused")
        Local local = localService.buscarPorTelefono(telefonoLocal);

        Map<String, Object> dashboard = reportesService.obtenerDashboard(
                telefonoLocal, fechaInicio, fechaFin
        );

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Compara ventas entre dos períodos.
     * 
     * @param telefonoLocal Teléfono del local
     * @param periodo1Inicio Inicio del período 1
     * @param periodo1Fin Fin del período 1
     * @param periodo2Inicio Inicio del período 2
     * @param periodo2Fin Fin del período 2
     * @return Comparación de métricas
     */
    @GetMapping("/ventas/comparacion")
    @RequiresFeature(Feature.REPORTES_AVANZADOS)
    @Operation(
        summary = "Comparar períodos",
        description = "Compara ventas entre dos períodos de tiempo. Útil para análisis mes a mes."
    )
    public ResponseEntity<Map<String, Object>> compararPeriodos(
            @Parameter(description = "Teléfono del local", example = "+5491112345678")
            @RequestParam String telefonoLocal,
            
            @Parameter(description = "Inicio período 1", example = "2025-10-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodo1Inicio,
            
            @Parameter(description = "Fin período 1", example = "2025-10-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodo1Fin,
            
            @Parameter(description = "Inicio período 2", example = "2025-09-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodo2Inicio,
            
            @Parameter(description = "Fin período 2", example = "2025-09-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodo2Fin
    ) {
        log.info("📊 GET /api/reportes/ventas/comparacion - Local: {}", telefonoLocal);

        // Obtener local para validación de plan (requerido por @RequiresFeature)
        @SuppressWarnings("unused")
        Local local = localService.buscarPorTelefono(telefonoLocal);

        Map<String, Object> comparacion = reportesService.compararPeriodos(
                telefonoLocal, 
                periodo1Inicio, periodo1Fin,
                periodo2Inicio, periodo2Fin
        );

        return ResponseEntity.ok(comparacion);
    }
}
