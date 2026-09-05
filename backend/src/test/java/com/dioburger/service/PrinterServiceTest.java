package com.dioburger.service;

import com.dioburger.model.entity.ConfiguracionLocal;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Pedido;
import com.dioburger.model.entity.PedidoItem;
import com.dioburger.model.entity.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de PrinterService")
class PrinterServiceTest {

    @Mock
    private WebClient webClient;

    @InjectMocks
    private PrinterService printerService;

    @Test
    @DisplayName("enviarTicket - Sin configuración de impresión - No hace llamada al WebClient")
    void testEnviarTicket_SinConfiguracion_NoLlamaWebClient() {
        Local local = Local.builder().id(1L).nombre("L").build();
        Pedido pedido = Pedido.builder().id(1L).local(local).build();

        // No config
        printerService.enviarTicket(pedido);

        verifyNoInteractions(webClient);
    }

    @Test
    @DisplayName("enviarTicket - Impresion desactivada - No llama al WebClient")
    void testEnviarTicket_ImpresionDesactivada_NoLlamaWebClient() {
        ConfiguracionLocal config = ConfiguracionLocal.builder().impresionActiva(false).build();
        Local local = Local.builder().id(1L).nombre("L").configuracion(config).build();
        Pedido pedido = Pedido.builder().id(1L).local(local).build();

        printerService.enviarTicket(pedido);

        verifyNoInteractions(webClient);
    }

    @Test
    @DisplayName("enviarTicket - URL vacía - No llama al WebClient")
    void testEnviarTicket_UrlVacia_NoLlamaWebClient() {
        ConfiguracionLocal config = ConfiguracionLocal.builder().impresionActiva(true).urlWebhookImpresora("").build();
        Local local = Local.builder().id(1L).nombre("L").configuracion(config).build();
        Pedido pedido = Pedido.builder().id(1L).local(local).build();

        printerService.enviarTicket(pedido);

        verifyNoInteractions(webClient);
    }

    @Test
    @DisplayName("enviarTicket - Impresion activa con URL - Llama a WebClient.post")
    void testEnviarTicket_Activa_LlamaWebClient() {
        // Mocks para la cadena reactiva de WebClient
        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(any(String.class))).thenReturn(bodySpec);
        doReturn(headersSpec).when(bodySpec).bodyValue(any(Object.class));
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());        // Construir pedido con items y configuracion
        Producto prod = Producto.builder().id(1L).nombre("X").precio(BigDecimal.valueOf(100)).build();
        PedidoItem item = PedidoItem.builder().producto(prod).cantidad(2).build();
        ConfiguracionLocal config = ConfiguracionLocal.builder().impresionActiva(true).urlWebhookImpresora("http://printer.local/print").build();
        Local local = Local.builder().id(1L).nombre("L").telefono("549111").configuracion(config).build();

        com.dioburger.model.entity.Cliente cliente = com.dioburger.model.entity.Cliente.builder()
            .id(1L)
            .nombre("Juan Perez")
            .telefono("549123456789")
            .build();

        Pedido pedido = Pedido.builder()
            .id(99L)
            .local(local)
            .cliente(cliente)
            .horaPedido(LocalDateTime.now())
            .estado(com.dioburger.model.enums.EstadoPedido.PENDIENTE)
            .modalidad(com.dioburger.model.enums.Modalidad.DELIVERY)
            .items(List.of(item))
            .total(BigDecimal.valueOf(200))
            .direccionEnvio("Calle Falsa 123")
            .build();

        // Act
        printerService.enviarTicket(pedido);

        // Verify que la cadena fue invocada
        verify(webClient).post();
        verify(uriSpec).uri("http://printer.local/print");
        verify(bodySpec).bodyValue(any(Map.class));
        verify(headersSpec).retrieve();
        verify(responseSpec).toBodilessEntity();
    }
}
