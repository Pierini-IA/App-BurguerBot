package com.dioburger.model.enums;

/**
 * Tipo de producto según su composición.
 * Define si un producto tiene receta de ingredientes o es simple.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
public enum TipoProducto {
    
    /**
     * Producto simple sin receta.
     * Ejemplo: Bebidas, condimentos, productos preenvasados.
     */
    SIMPLE,
    
    /**
     * Producto con receta de ingredientes.
     * Ejemplo: Hamburguesas, pizzas, ensaladas.
     * Requiere ingredientes y afecta al stock.
     */
    CON_RECETA
}
