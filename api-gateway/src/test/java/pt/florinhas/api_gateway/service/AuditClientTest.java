package pt.florinhas.api_gateway.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class AuditClientTest {

    @Test
    void constructor_DeveCriarInstancia() {

        WebClient.Builder builder = WebClient.builder();

        AuditClient client = new AuditClient(
                builder,
                "http://localhost:8081",
                "secret"
        );

        assertNotNull(client);
    }
}