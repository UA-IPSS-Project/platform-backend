package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class RestTemplateConfigTest {

    private final RestTemplateConfig config =
            new RestTemplateConfig();

    @Test
    void restTemplate_DeveCriarBean() {

        RestTemplate result =
                config.restTemplate();

        assertNotNull(result);
    }
}