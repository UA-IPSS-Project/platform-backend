package pt.florinhas.marcacoes.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.web.DefaultSecurityFilterChain;

import pt.florinhas.marcacoes.security.GatewayHeaderAuthenticationFilter;

class SecurityConfigTest {

    @Test
    @DisplayName("PasswordEncoder deve existir")
    void passwordEncoder_DeveExistir() {

        GatewayHeaderAuthenticationFilter filter =
                mock(GatewayHeaderAuthenticationFilter.class);

        SecurityConfig config =
                new SecurityConfig(filter);

        PasswordEncoder encoder =
                config.passwordEncoder();

        assertNotNull(encoder);
    }

    @Test
    @DisplayName("SecurityConfig deve ser criada")
    void securityConfig_DeveSerCriada() {

        GatewayHeaderAuthenticationFilter filter =
                mock(GatewayHeaderAuthenticationFilter.class);

        SecurityConfig config =
                new SecurityConfig(filter);

        assertNotNull(config);
    }
}