package pt.florinhas.api_gateway.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.florinhas.api_gateway.security.JwtAuthenticationFilter;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {

        JwtAuthenticationFilter jwtAuthenticationFilter =
                org.mockito.Mockito.mock(
                        JwtAuthenticationFilter.class);

        UserDetailsService userDetailsService =
                username -> User
                        .withUsername(username)
                        .password("password")
                        .authorities("USER")
                        .build();

        securityConfig =
                new SecurityConfig(
                        jwtAuthenticationFilter,
                        userDetailsService);
    }

    @Test
    void passwordEncoder_DeveCriarBCryptPasswordEncoder() {

        PasswordEncoder encoder =
                securityConfig.passwordEncoder();

        assertNotNull(encoder);

        assertInstanceOf(
                BCryptPasswordEncoder.class,
                encoder);
    }

    @Test
    void authenticationProvider_DeveCriarDaoAuthenticationProvider() {

        AuthenticationProvider provider =
                securityConfig.authenticationProvider();

        assertNotNull(provider);

        assertInstanceOf(
                DaoAuthenticationProvider.class,
                provider);
    }

    @Test
    void authenticationManager_DeveCriarProviderManager() {

        AuthenticationProvider provider =
                securityConfig.authenticationProvider();

        AuthenticationManager manager =
                securityConfig.authenticationManager(provider);

        assertNotNull(manager);

        assertInstanceOf(
                ProviderManager.class,
                manager);
    }

    @Test
    void websocketAuthHeaderPropagator_DeveCriarFiltro() {

        var filter =
                securityConfig.websocketAuthHeaderPropagator();

        assertNotNull(filter);
    }
}