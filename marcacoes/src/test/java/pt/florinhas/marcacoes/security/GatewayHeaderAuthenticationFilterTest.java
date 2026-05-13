package pt.florinhas.marcacoes.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GatewayHeaderAuthenticationFilterTest {

    @Test
    @DisplayName("Deve criar GatewayHeaderAuthenticationFilter")
    void deveCriarGatewayHeaderAuthenticationFilter() {

        CustomUserDetailsService service =
                mock(CustomUserDetailsService.class);

        GatewayHeaderAuthenticationFilter filter =
                new GatewayHeaderAuthenticationFilter(
                        service,
                        "secret"
                );

        assertNotNull(filter);
    }

    @Test
    @DisplayName("Classe GatewayHeaderAuthenticationFilter deve carregar")
    void classeDeveCarregar() {

        assertNotNull(
                GatewayHeaderAuthenticationFilter.class
        );
    }
}