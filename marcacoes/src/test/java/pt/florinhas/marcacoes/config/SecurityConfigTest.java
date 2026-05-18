package pt.florinhas.marcacoes.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import pt.florinhas.marcacoes.security.GatewayHeaderAuthenticationFilter;

class SecurityConfigTest {

    @Test
    @DisplayName("PasswordEncoder deve ser instanciado como BCrypt e funcionar corretamente")
    void passwordEncoder_DeveExistirEFuncionar() {
        GatewayHeaderAuthenticationFilter filter = mock(GatewayHeaderAuthenticationFilter.class);
        SecurityConfig config = new SecurityConfig(filter);

        PasswordEncoder encoder = config.passwordEncoder();

        assertNotNull(encoder);
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);

        String rawPassword = "minhaSenhaSegura123";
        String encodedPassword = encoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword));
        assertFalse(encoder.matches("outraSenha", encodedPassword));
    }

    @Test
    @DisplayName("SecurityConfig deve ser instanciada com construtor de injeção")
    void securityConfig_DeveSerCriada() {
        GatewayHeaderAuthenticationFilter filter = mock(GatewayHeaderAuthenticationFilter.class);
        SecurityConfig config = new SecurityConfig(filter);
        assertNotNull(config);
    }

    @Test
    @DisplayName("securityFilterChain deve configurar filtros e retornar SecurityFilterChain válida")
    void securityFilterChain_DeveConfigurarCadeiaDeSeguranca() throws Exception {
        GatewayHeaderAuthenticationFilter filter = mock(GatewayHeaderAuthenticationFilter.class);
        SecurityConfig config = new SecurityConfig(filter);

        // Moca o HttpSecurity com fluent API
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        DefaultSecurityFilterChain expectedChain = mock(DefaultSecurityFilterChain.class);

        when(http.csrf(any())).thenReturn(http);
        when(http.httpBasic(any())).thenReturn(http);
        when(http.formLogin(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.sessionManagement(any())).thenReturn(http);
        when(http.addFilterBefore(any(), eq(UsernamePasswordAuthenticationFilter.class))).thenReturn(http);
        when(http.headers(any())).thenReturn(http);
        when(http.build()).thenReturn(expectedChain);

        SecurityFilterChain result = config.securityFilterChain(http);

        assertNotNull(result);
        assertEquals(expectedChain, result);

        // Verifica as chamadas de configuração
        verify(http).csrf(any());
        verify(http).httpBasic(any());
        verify(http).formLogin(any());
        verify(http).authorizeHttpRequests(any());
        verify(http).sessionManagement(any());
        verify(http).addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        verify(http).headers(any());
        verify(http).build();
    }
}