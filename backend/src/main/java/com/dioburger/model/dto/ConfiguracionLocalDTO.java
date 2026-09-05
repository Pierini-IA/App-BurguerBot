package com.dioburger.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO de la configuración operativa del local del usuario autenticado.
 *
 * <p>Usado por {@code GET}/{@code PUT /api/local/mi-local/configuracion}.</p>
 *
 * <p><b>Secretos.</b> Los tokens de la Graph API ({@code waAccessToken},
 * {@code igToken}, {@code fbPageAccessToken}) NO se devuelven en el GET: en su
 * lugar viajan los flags {@code *Configurado}. En el PUT, un token vacío o nulo
 * significa "no cambiar"; solo se reemplaza si llega un valor no vacío.</p>
 *
 * <p>En el PUT, cualquier campo que llegue {@code null} se deja como está
 * (actualización parcial).</p>
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionLocalDTO {

    // === Horarios e intervalos ===
    private LocalTime horaApertura;
    private LocalTime horaCierre;
    private Integer intervaloMinutosPedidos;
    private Integer maxPedidosPorIntervalo;
    private LocalTime horaAperturaReservas;
    private LocalTime horaCierreReservas;
    private Integer intervaloMinutosReservas;
    private Integer maxReservasPorIntervalo;
    private Integer minutosAnticipacionCancelacion;

    // === Modalidades ===
    private Boolean permiteDelivery;
    private Boolean permiteTakeAway;
    private Boolean permiteReservas;

    // === Impresión y webhooks ===
    private Boolean impresionActiva;
    private String urlWebhookImpresora;
    private String urlWebhookNotificaciones;
    private String urlWebhookAsignacionDelivery;

    // === Meta: datos no secretos ===
    private String waPhoneId;
    private String fbPageId;

    // === Meta: secretos (solo escritura; vacío = no cambiar) ===
    private String waAccessToken;
    private String igToken;
    private String fbPageAccessToken;

    // === Meta: flags de solo lectura (se ignoran en el PUT) ===
    private Boolean waConfigurado;
    private Boolean igConfigurado;
    private Boolean fbConfigurado;
}
