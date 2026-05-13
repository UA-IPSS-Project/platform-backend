package pt.florinhas.api_gateway.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class GatewayConfigTest {

    private final GatewayConfig gatewayConfig = new GatewayConfig();

    @Test
    void webClient_DeveRetornarInstancia() {
        WebClient webClient = gatewayConfig.webClient();

        assertNotNull(webClient);
    }
}