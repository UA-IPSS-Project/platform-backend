package pt.florinhas.notificacoes.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class SecurityConfigTest {

    @Test
    void passwordEncoder_DeveCriarEncoder() {

        var filter =
                org.mockito.Mockito.mock(
                        pt.florinhas.notificacoes.security.GatewayHeaderAuthenticationFilter.class);

        SecurityConfig config =
                new SecurityConfig(filter);

        assertNotNull(
                config.passwordEncoder());
    }
}