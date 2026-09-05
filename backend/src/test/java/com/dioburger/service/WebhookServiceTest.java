package com.dioburger.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de WebhookService")
class WebhookServiceTest {

    @Mock
    private WebClient webClient;

    @InjectMocks
    private WebhookService webhookService;

    @Test
    @DisplayName("validarWebhookUrl - Null o vacía devuelve false")
    void testValidarWebhookUrl_NullVacia_False() {
        boolean r1 = webhookService.validarWebhookUrl(null, "Local A");
        boolean r2 = webhookService.validarWebhookUrl("", "Local A");

        assertThat(r1).isFalse();
        assertThat(r2).isFalse();
    }

    @Test
    @DisplayName("validarWebhookUrl - URL válida devuelve true")
    void testValidarWebhookUrl_Valida_True() {
        boolean r = webhookService.validarWebhookUrl("http://example.com/hook", "Local A");
        assertThat(r).isTrue();
    }

    @Test
    @DisplayName("enviarSync - Devuelve respuesta del webhook")
    void testEnviarSync_DevuelveRespuesta() {
        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(any(String.class))).thenReturn(bodySpec);
    doReturn(headersSpec).when(bodySpec).bodyValue(any(Object.class));
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("OK"));

        Mono<String> mono = webhookService.enviarSync("http://n8n/hook", Map.of("k","v"), "TEST", 1L);
        String resp = mono.block();

        assertThat(resp).isEqualTo("OK");
        verify(webClient).post();
    }

    @Test
    @DisplayName("enviarAsync - Invoca envío asincrónico (no lanza excepciones)")
    void testEnviarAsync_NoLanza() {
        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(any(String.class))).thenReturn(bodySpec);
    doReturn(headersSpec).when(bodySpec).bodyValue(any(Object.class));
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("OK"));

        webhookService.enviarAsync("http://n8n/hook", Map.of("k","v"), "ASYNC_TEST", 2L);

        // Verificar que se inició la llamada (post) correctamente
        verify(webClient).post();
    }
}
