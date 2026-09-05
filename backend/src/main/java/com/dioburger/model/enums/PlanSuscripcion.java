package com.dioburger.model.enums;

/**
 * Enum que representa los planes de suscripción disponibles.
 * Cada plan incluye un conjunto específico de funcionalidades.
 * 
 * @author Dio Burger Team
 * @version 2.2.0
 */
public enum PlanSuscripcion {
    
    /**
     * Plan BÁSICO - Gestión local únicamente.
     * Ideal para restaurantes chicos.
     */
    BASICO("Básico", "Restaurantes chicos"),
    
    /**
     * Plan ESTÁNDAR - Gestión + Bot + Reservas.
     * Ideal para negocios con reservas o delivery.
     */
    ESTANDAR("Estándar", "Negocios con reservas o delivery"),
    
    /**
     * Plan PREMIUM - Funcionalidades completas.
     * Ideal para franquicias o locales con IA + impresión automática.
     */
    PREMIUM("Premium", "Franquicias o locales con IA + impresión automática");
    
    private final String nombre;
    private final String descripcionCorta;
    
    PlanSuscripcion(String nombre, String descripcionCorta) {
        this.nombre = nombre;
        this.descripcionCorta = descripcionCorta;
    }
    
    /**
     * Obtiene el nombre legible del plan.
     * 
     * @return nombre del plan
     */
    public String getNombre() {
        return nombre;
    }
    
    /**
     * Obtiene la descripción corta del plan.
     * 
     * @return descripción del plan
     */
    public String getDescripcionCorta() {
        return descripcionCorta;
    }
    
    /**
     * Verifica si este plan incluye todas las funcionalidades de otro plan.
     * 
     * @param otroPlan plan a comparar
     * @return true si este plan es mayor o igual al otro
     */
    public boolean incluyeA(PlanSuscripcion otroPlan) {
        return this.ordinal() >= otroPlan.ordinal();
    }
    
    /**
     * Retorna el precio de setup inicial en pesos argentinos.
     * Costo único que se cobra al contratar el servicio.
     * 
     * @return precio de setup en ARS
     */
    public Double getPrecioSetupInicial() {
        return switch (this) {
            case BASICO -> 100000.0;    // $100.000 ARS
            case ESTANDAR -> 180000.0;  // $180.000 ARS
            case PREMIUM -> 250000.0;   // $250.000 ARS
        };
    }
    
    /**
     * Retorna el precio mensual en pesos argentinos.
     * Costo recurrente mensual del plan.
     * 
     * @return precio mensual en ARS
     */
    public Double getPrecioMensual() {
        return switch (this) {
            case BASICO -> 35000.0;     // $35.000 ARS
            case ESTANDAR -> 75000.0;   // $75.000 ARS
            case PREMIUM -> 125000.0;   // $125.000 ARS
        };
    }
    
    /**
     * Retorna el precio mensual sugerido en pesos argentinos.
     * Método legacy mantenido por compatibilidad.
     * 
     * @return precio mensual en ARS
     * @deprecated Usar {@link #getPrecioMensual()} en su lugar
     */
    @Deprecated
    public Double getPrecioMensualSugerido() {
        return getPrecioMensual();
    }
}
