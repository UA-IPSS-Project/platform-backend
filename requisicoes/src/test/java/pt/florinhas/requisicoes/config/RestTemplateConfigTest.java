package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class RestTemplateConfigTest {

    @Test
    void restTemplate_DeveCriarInstancia() {

        RestTemplateConfig config = new RestTemplateConfig();

        RestTemplate restTemplate = config.restTemplate();

        assertNotNull(restTemplate);
    }
}