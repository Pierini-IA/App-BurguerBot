package com.dioburger.model.enums;

/**
 * Enum que representa las funcionalidades/características disponibles en el sistema.
 * Cada feature puede estar disponible o no según el plan de suscripción del local.
 * 
 * @author Dio Burger Team
 * @version 2.2.0
 */
public enum Feature {
    
    // ============ FUNCIONALIDADES BÁSICAS (Todos los planes) ============
    
    /**
     * Panel web de administración del local.
     * Incluye gestión de productos, ingredientes, mesas, etc.
     */
    PANEL_WEB,
    
    /**
     * Gestión completa de productos (CRUD).
     */
    GESTION_PRODUCTOS,
    
    /**
     * Gestión de ingredientes y control de stock.
     */
    GESTION_INGREDIENTES,
    
    /**
     * Gestión de categorías para organizar productos.
     */
    GESTION_CATEGORIAS,
    
    /**
     * Gestión de extras/adicionales con precios.
     */
    GESTION_EXTRAS,
    
    /**
     * Gestión de mesas del local.
     */
    GESTION_MESAS,
    
    /**
     * Creación y gestión de pedidos desde el panel web.
     */
    PEDIDOS_PANEL,
    
    /**
     * Panel de cocina para ver y gestionar pedidos.
     */
    PANEL_COCINA,
    
    /**
     * Estadísticas básicas del día.
     */
    ESTADISTICAS_BASICAS,
    
    // ============ FUNCIONALIDADES ESTÁNDAR (Plan ESTÁNDAR+) ============
    
    /**
     * Bot de WhatsApp con IA para recibir pedidos por texto e imagen.
     * Incluye menú dinámico y gestión automática.
     */
    BOT_WHATSAPP,

    /**
     * Sistema de reservas de mesas.
     * Incluye asignación automática y unión de mesas.
     */
    SISTEMA_RESERVAS,

    /**
     * Notificaciones en tiempo real vía WebSocket.
     */
    WEBSOCKET_TIEMPO_REAL,

    /**
     * Bot con IA responde comentarios de Instagram/Facebook en publicaciones
     * (menú, horarios, delivery) e invita a pedir por WhatsApp. No crea pedidos.
     */
    BOT_COMMENTS_META,
    
    // ============ FUNCIONALIDADES PREMIUM (Plan PREMIUM) ============
    
    /**
     * Reportes avanzados de ventas (diarias, semanales, mensuales).
     */
    REPORTES_AVANZADOS,
    
    /**
     * Webhooks personalizados para integraciones externas.
     */
    WEBHOOKS_PERSONALIZADOS,
    
    /**
     * Impresión automática de pedidos vía webhook.
     */
    IMPRESION_AUTOMATICA,
    
    /**
     * Asignación automática de repartidores para delivery.
     */
    ASIGNACION_REPARTIDORES,
    
    /**
     * Dashboard completo con métricas avanzadas.
     */
    DASHBOARD_AVANZADO,
    
    /**
     * Análisis de productos más vendidos y comparaciones.
     */
    ANALISIS_PRODUCTOS,
    
    /**
     * Acceso completo a la API sin límites de rate limiting.
     */
    API_ACCESO_ILIMITADO,
    
    /**
     * Soporte técnico prioritario.
     */
    SOPORTE_PRIORITARIO;
    
    /**
     * Retorna una descripción legible de la feature.
     * 
     * @return descripción de la funcionalidad
     */
    public String getDescripcion() {
        return switch (this) {
            case PANEL_WEB -> "Panel web de administración";
            case GESTION_PRODUCTOS -> "Gestión de productos";
            case GESTION_INGREDIENTES -> "Gestión de ingredientes y stock";
            case GESTION_CATEGORIAS -> "Gestión de categorías";
            case GESTION_EXTRAS -> "Gestión de extras/adicionales";
            case GESTION_MESAS -> "Gestión de mesas";
            case PEDIDOS_PANEL -> "Pedidos desde panel web";
            case PANEL_COCINA -> "Panel de cocina";
            case ESTADISTICAS_BASICAS -> "Estadísticas básicas";
            case BOT_WHATSAPP -> "Bot de WhatsApp";
            case SISTEMA_RESERVAS -> "Sistema de reservas";
            case WEBSOCKET_TIEMPO_REAL -> "Notificaciones en tiempo real";
            case BOT_COMMENTS_META -> "Respuesta a comentarios de Instagram/Facebook";
            case REPORTES_AVANZADOS -> "Reportes avanzados de ventas";
            case WEBHOOKS_PERSONALIZADOS -> "Webhooks personalizados";
            case IMPRESION_AUTOMATICA -> "Impresión automática";
            case ASIGNACION_REPARTIDORES -> "Asignación de repartidores";
            case DASHBOARD_AVANZADO -> "Dashboard avanzado";
            case ANALISIS_PRODUCTOS -> "Análisis de productos";
            case API_ACCESO_ILIMITADO -> "API sin límites";
            case SOPORTE_PRIORITARIO -> "Soporte prioritario";
        };
    }
}
