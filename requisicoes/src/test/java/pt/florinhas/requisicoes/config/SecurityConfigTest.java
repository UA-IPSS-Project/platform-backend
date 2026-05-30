package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.florinhas.requisicoes.security.GatewayHeaderAuthenticationFilter;

class SecurityConfigTest {

    private SecurityConfig config;

    @BeforeEach
    void setUp() {

        config =
                new SecurityConfig(
                        mock(GatewayHeaderAuthenticationFilter.class));
    }

    @Test
    void passwordEncoder_DeveCriarBean() {

        PasswordEncoder result =
                config.passwordEncoder();

        assertNotNull(result);
    }
}