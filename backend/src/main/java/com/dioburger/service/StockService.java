package com.dioburger.service;

import com.dioburger.exception.StockInsuficienteException;
import com.dioburger.model.entity.*;
import com.dioburger.repository.IngredienteRepository;
import com.dioburger.repository.ProductoRepository;
import com.dioburger.repository.RecetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio para gestionar el stock de ingredientes y la disponibilidad de productos.
 * Implementa la lógica de negocio para verificar stock, descontar ingredientes
 * y actualizar el estado de disponibilidad de los productos.
 *
 * @author Dio Burger Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final RecetaRepository recetaRepository;
    private final IngredienteRepository ingredienteRepository;
    private final ProductoRepository productoRepository;

    /**
     * Verifica si hay stock suficiente para preparar un producto.
     * Itera sobre todas las recetas del producto y verifica que cada
     * ingrediente tenga stock >= cantidadRequerida.
     *
     * @param producto Producto a verificar
     * @return true si hay stock suficiente, false si algún ingrediente está agotado
     */
    public boolean verificarDisponibilidad(Producto producto) {
        List<Receta> recetas = recetaRepository.findByProducto(producto);

        if (recetas.isEmpty()) {
            log.warn("El producto {} no tiene recetas asociadas", producto.getNombre());
            return false;
        }

        for (Receta receta : recetas) {
            Ingrediente ingrediente = receta.getIngrediente();
            BigDecimal stockActual = ingrediente.getStockActual();
            BigDecimal cantidadRequerida = receta.getCantidadRequerida();

            if (stockActual.compareTo(cantidadRequerida) < 0) {
                log.info("Ingrediente {} insuficiente para producto {}: requerido={}, actual={}",
                        ingrediente.getNombre(), producto.getNombre(),
                        cantidadRequerida, stockActual);
                return false;
            }
        }

        return true;
    }

    /**
     * Descuenta el stock de ingredientes para una lista de items de pedido.
     * Este método es transaccional: si falla el descuento de un ingrediente,
     * se hace rollback de todos los cambios.
     *
     * @param items Lista de items del pedido
     * @throws StockInsuficienteException si no hay stock suficiente para algún item
     */
    @Transactional
    public void descontarStock(List<PedidoItem> items) {
        for (PedidoItem item : items) {
            Producto producto = item.getProducto();
            Integer cantidad = item.getCantidad();

            log.info("Descontando stock para {} x{}", producto.getNombre(), cantidad);

            List<Receta> recetas = recetaRepository.findByProducto(producto);

            for (Receta receta : recetas) {
                Ingrediente ingrediente = receta.getIngrediente();
                BigDecimal cantidadRequerida = receta.getCantidadRequerida();
                BigDecimal cantidadTotal = cantidadRequerida.multiply(BigDecimal.valueOf(cantidad));

                BigDecimal stockActual = ingrediente.getStockActual();

                if (stockActual.compareTo(cantidadTotal) < 0) {
                    String mensaje = String.format(
                            "Stock insuficiente para %s: requerido=%.2f %s, disponible=%.2f %s",
                            ingrediente.getNombre(),
                            cantidadTotal, ingrediente.getUnidadMedida(),
                            stockActual, ingrediente.getUnidadMedida()
                    );
                    log.error(mensaje);
                    throw new StockInsuficienteException(mensaje);
                }

                // Descontar stock
                BigDecimal nuevoStock = stockActual.subtract(cantidadTotal);
                ingrediente.setStockActual(nuevoStock);
                ingredienteRepository.save(ingrediente);

                log.info("Stock actualizado para {}: {} -> {} {}",
                        ingrediente.getNombre(),
                        stockActual, nuevoStock,
                        ingrediente.getUnidadMedida());
            }
        }

        log.info("Stock descontado exitosamente para {} items", items.size());
    }

    /**
     * Actualiza el estado 'estaAgotado' de todos los productos de un local
     * basándose en la disponibilidad real de stock de ingredientes.
     * Este método se debe llamar después de cada operación que modifique el stock.
     *
     * @param local Local cuyos productos se van a actualizar
     */
    @Transactional
    public void actualizarDisponibilidadProductos(Local local) {
        List<Producto> productos = productoRepository.findByLocal(local);

        log.info("Actualizando disponibilidad de {} productos del local {}",
                productos.size(), local.getNombre());

        int productosAgotados = 0;

        for (Producto producto : productos) {
            boolean disponible = verificarDisponibilidad(producto);
            boolean estadoAnterior = producto.getEstaAgotado();

            producto.setEstaAgotado(!disponible);
            productoRepository.save(producto);

            if (!disponible) {
                productosAgotados++;
            }

            if (estadoAnterior != !disponible) {
                log.info("Estado de {} cambiado: agotado={}", producto.getNombre(), !disponible);
            }
        }

        log.info("Actualización completada: {} productos disponibles, {} agotados",
                productos.size() - productosAgotados, productosAgotados);
    }

    /**
     * Verifica si hay stock suficiente para N unidades de un producto.
     *
     * @param producto Producto a verificar
     * @param cantidad Cantidad de unidades
     * @return true si hay stock suficiente, false si no
     */
    public boolean verificarStock(Producto producto, Integer cantidad) {
        List<Receta> recetas = recetaRepository.findByProducto(producto);

        if (recetas.isEmpty()) {
            log.warn("El producto {} no tiene recetas asociadas", producto.getNombre());
            return false;
        }

        for (Receta receta : recetas) {
            Ingrediente ingrediente = receta.getIngrediente();
            BigDecimal stockActual = ingrediente.getStockActual();
            BigDecimal cantidadRequerida = receta.getCantidadRequerida().multiply(BigDecimal.valueOf(cantidad));

            if (stockActual.compareTo(cantidadRequerida) < 0) {
                log.info("Ingrediente {} insuficiente para {} x{}: requerido={}, actual={}",
                        ingrediente.getNombre(), producto.getNombre(), cantidad,
                        cantidadRequerida, stockActual);
                return false;
            }
        }

        return true;
    }

    /**
     * Descuenta stock para una cantidad específica de un producto.
     *
     * @param producto Producto cuyo stock se va a descontar
     * @param cantidad Cantidad de unidades
     */
    @Transactional
    public void descontarStock(Producto producto, Integer cantidad) {
        log.info("Descontando stock para {} x{}", producto.getNombre(), cantidad);

        List<Receta> recetas = recetaRepository.findByProducto(producto);

        for (Receta receta : recetas) {
            Ingrediente ingrediente = receta.getIngrediente();
            BigDecimal cantidadRequerida = receta.getCantidadRequerida();
            BigDecimal cantidadTotal = cantidadRequerida.multiply(BigDecimal.valueOf(cantidad));

            BigDecimal stockActual = ingrediente.getStockActual();
            BigDecimal nuevoStock = stockActual.subtract(cantidadTotal);
            
            ingrediente.setStockActual(nuevoStock);
            ingredienteRepository.save(ingrediente);

            log.info("Stock actualizado para {}: {} -> {} {}",
                    ingrediente.getNombre(),
                    stockActual, nuevoStock,
                    ingrediente.getUnidadMedida());
        }
    }

    /**
     * Restaura stock para una cantidad específica de un producto.
     * Se usa al cancelar o modificar pedidos.
     *
     * @param producto Producto cuyo stock se va a restaurar
     * @param cantidad Cantidad de unidades a restaurar
     */
    @Transactional
    public void restaurarStock(Producto producto, Integer cantidad) {
        log.info("♻️ Restaurando stock para {} x{}", producto.getNombre(), cantidad);

        List<Receta> recetas = recetaRepository.findByProducto(producto);

        for (Receta receta : recetas) {
            Ingrediente ingrediente = receta.getIngrediente();
            BigDecimal cantidadRequerida = receta.getCantidadRequerida();
            BigDecimal cantidadTotal = cantidadRequerida.multiply(BigDecimal.valueOf(cantidad));

            BigDecimal stockActual = ingrediente.getStockActual();
            BigDecimal nuevoStock = stockActual.add(cantidadTotal);
            
            ingrediente.setStockActual(nuevoStock);
            ingredienteRepository.save(ingrediente);

            log.info("✅ Stock restaurado para {}: {} -> {} {}",
                    ingrediente.getNombre(),
                    stockActual, nuevoStock,
                    ingrediente.getUnidadMedida());
        }

        // Actualizar disponibilidad del producto
        actualizarDisponibilidadProducto(producto);
    }

    /**
     * Actualiza la disponibilidad de un producto específico.
     *
     * @param producto Producto a actualizar
     */
    @Transactional
    public void actualizarDisponibilidadProducto(Producto producto) {
        boolean disponible = verificarDisponibilidad(producto);
        producto.setEstaAgotado(!disponible);
        productoRepository.save(producto);

        log.info("Disponibilidad actualizada para {}: agotado={}",
                producto.getNombre(), !disponible);
    }

    /**
     * Calcula cuántas unidades de un producto se pueden preparar con el stock actual.
     * Retorna la cantidad máxima basándose en el ingrediente más limitante.
     *
     * @param producto Producto a calcular
     * @return Cantidad máxima de unidades que se pueden preparar
     */
    // readOnly: recorre la receta y toca `ingrediente`, que es un proxy lazy. El
    // agente de IA llama a esto desde el hilo del buffer de mensajes, donde no hay
    // sesión de Hibernate abierta: sin transacción propia salta LazyInitializationException.
    @Transactional(readOnly = true)
    public int calcularCantidadMaximaDisponible(Producto producto) {
        List<Receta> recetas = recetaRepository.findByProducto(producto);

        if (recetas.isEmpty()) {
            log.warn("El producto {} no tiene recetas asociadas", producto.getNombre());
            return 0;
        }

        int cantidadMaxima = Integer.MAX_VALUE;

        for (Receta receta : recetas) {
            Ingrediente ingrediente = receta.getIngrediente();
            BigDecimal stockDisponible = ingrediente.getStockActual();
            BigDecimal cantidadRequerida = receta.getCantidadRequerida();

            if (cantidadRequerida.compareTo(BigDecimal.ZERO) <= 0) {
                // Si no requiere cantidad, no limita la producción
                continue;
            }

            // Calcular cuántas unidades se pueden hacer con este ingrediente
            int unidadesPosibles = stockDisponible.divide(cantidadRequerida, 0, java.math.RoundingMode.DOWN).intValue();

            // El ingrediente más limitante determina la cantidad máxima
            cantidadMaxima = Math.min(cantidadMaxima, unidadesPosibles);
        }

        // Si ningún ingrediente limita, retornar 0 para seguridad
        return cantidadMaxima == Integer.MAX_VALUE ? 0 : cantidadMaxima;
    }
}

