package pt.florinhas.marcacoes.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.florinhas.marcacoes.security.GatewayHeaderAuthenticationFilter;

class SecurityConfigTest {

    private final GatewayHeaderAuthenticationFilter filter =
            org.mockito.Mockito.mock(
                    GatewayHeaderAuthenticationFilter.class);

    private final SecurityConfig config =
            new SecurityConfig(filter);

    @Test
    void passwordEncoder_DeveCriarBCrypt() {

        PasswordEncoder encoder =
                config.passwordEncoder();

        assertInstanceOf(
                BCryptPasswordEncoder.class,
                encoder);
    }
}