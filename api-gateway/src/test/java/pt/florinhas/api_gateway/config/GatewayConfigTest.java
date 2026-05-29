package pt.florinhas.api_gateway.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class GatewayConfigTest {

    private final GatewayConfig config = new GatewayConfig();

    @Test
    void webClient_DeveCriarBean() {
        WebClient webClient = config.webClient();

        assertNotNull(webClient);
    }

    @Test
    void webClient_DeveSerInstanciaWebClient() {
        assertInstanceOf(WebClient.class, config.webClient());
    }
}