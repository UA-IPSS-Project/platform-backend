package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.florinhas.requisicoes.security.GatewayHeaderAuthenticationFilter;

class SecurityConfigTest {

    @Test
    void passwordEncoder_DeveCriarBCryptPasswordEncoder() {

        GatewayHeaderAuthenticationFilter filter =
                mock(GatewayHeaderAuthenticationFilter.class);

        SecurityConfig config = new SecurityConfig(filter);

        PasswordEncoder encoder = config.passwordEncoder();

        assertNotNull(encoder);

        String encoded = encoder.encode("password123");

        assertTrue(encoder.matches("password123", encoded));
    }
}