package pt.florinhas.api_gateway.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;

class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void florinhasOpenAPI_DeveConfigurarOpenApi() {
        OpenAPI openAPI = config.florinhasOpenAPI();

        assertNotNull(openAPI);
        assertEquals("Florinhas Platform API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());

        assertNotNull(openAPI.getComponents());
        assertTrue(openAPI.getComponents()
                .getSecuritySchemes()
                .containsKey("cookieAuth"));
    }
}