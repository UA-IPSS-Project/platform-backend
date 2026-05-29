package pt.florinhas.api_gateway.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void florinhasOpenAPI_DeveCriarOpenAPI() {
        OpenAPI api = config.florinhasOpenAPI();

        assertNotNull(api);
    }

    @Test
    void florinhasOpenAPI_DeveConfigurarInfo() {
        OpenAPI api = config.florinhasOpenAPI();

        assertEquals("Florinhas Platform API", api.getInfo().getTitle());

        assertEquals(
                "API da plataforma de gestão de serviços sociais Florinhas (IPSS)",
                api.getInfo().getDescription());

        assertEquals("1.0.0", api.getInfo().getVersion());
    }

    @Test
    void florinhasOpenAPI_DeveConfigurarSecurityScheme() {
        OpenAPI api = config.florinhasOpenAPI();

        SecurityScheme scheme = api.getComponents()
                .getSecuritySchemes()
                .get("cookieAuth");

        assertNotNull(scheme);

        assertEquals(SecurityScheme.Type.APIKEY, scheme.getType());
        assertEquals(SecurityScheme.In.COOKIE, scheme.getIn());
        assertEquals("jwt", scheme.getName());
    }

    @Test
    void florinhasOpenAPI_DeveAdicionarSecurityRequirement() {
        OpenAPI api = config.florinhasOpenAPI();

        assertFalse(api.getSecurity().isEmpty());

        assertTrue(api.getSecurity().get(0).containsKey("cookieAuth"));
    }
}